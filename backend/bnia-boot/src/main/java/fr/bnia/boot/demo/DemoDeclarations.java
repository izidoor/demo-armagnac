package fr.bnia.boot.demo;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.AnnoterDsm;
import fr.bnia.declarations.application.FichierDepose;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.application.GererGrilleCotisation.SaisieTauxCvo;
import fr.bnia.declarations.application.GererGrilleCotisation;
import fr.bnia.declarations.application.GererPieces;
import fr.bnia.declarations.application.GererPointDeDepart;
import fr.bnia.declarations.application.VueDsm;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.SaisieLigneStock;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.operateurs.application.GererOperateurs;
import fr.bnia.operateurs.application.SaisieProduction;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.TypeAlambic;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Données de démonstration des déclarations (spec 05, §7.1, lots 1 à 7), en profil {@code demo} seulement,
 * après les opérateurs. Les dates sont calculées depuis la date du jour (décision du 2026-10-13) :
 * <ul>
 * <li>les négociants partent du premier du mois courant, avec un stock fixé par le BNIA au dernier jour du
 * mois précédent ; leur première DSM est celle du mois courant :
 * <ul>
 * <li>Armagnacs de Gascogne ({@code N320001}) : part de douze mois avant le mois courant (spec 08 §7.1, lots 1
 * et 2) ; ses DSM des onze premiers mois, jusqu'à M−2, sont validées, aux volumes de la facture d'exemple (sorties
 * France 839,11 L, export 2 254,15 L, avec le DAE de l'export), pour que le BNIA les facture, en lot ; celle de M−1,
 * avec les mêmes totaux, deux notes internes du BNIA (sur la DSM et sur sa première ligne) et sans justificatif, est
 * en compléments demandés, motif « DAE manquants pour l'export » (décision du 2026-10-21 : la démonstration du
 * portail tient dans les comptes {@code @negociant.fr} et {@code @producteur.fr}). Le mois courant reste à initier :
 * une DSM ne s'envoie qu'après la fin de sa période (spec 06 RG-CA-24), et l'achat d'une retiraison validée s'y
 * inscrira à l'initiation (RG-CA-22) ;</li>
 * <li>Maison Larressingle ({@code N320002}) : part du premier du mois <em>précédent</em> (décision du
 * 2026-10-14), DSM envoyée avec les DAE de ses exports : elle attend l'instruction du BNIA ;</li>
 * <li>Négoce des Landes ({@code N400001}) : part du premier du mois précédent, DSM de M−1 en brouillon, avec une
 * anomalie (Σ6 ≠ Σ12) ;</li>
 * </ul></li>
 * <li>Maison Durand, Condom ({@code 000001}, producteur) : point de départ au 1er avril de l'an dernier, DSM
 * de cette année-là validée, avec ses douze DRM et le DAE de son export ; la suivante, en brouillon, montre le
 * vieillissement et la fusion à {@code 20+}, avec une anomalie (Σ6 ≠ Σ12) qui bloque l'envoi ;</li>
 * <li>Maison Durand, Eauze ({@code 000002}) : pas de point de départ, le BNIA le fixe en démonstration ;</li>
 * <li>Domaine de Laubade ({@code 000003}, producteur) : point de départ au 1er avril d'il y a deux ans, DSM de
 * cette année-là validée, puis statut de déclaration passé à {@code SOMMEIL} : le BNIA fixe un nouveau point de
 * départ après l'interruption (RG-DEC-09) ;</li>
 * <li>la grille CVO : 55,00 €/hlAP à partir du 1er janvier 2021, puis 60,00 €/hlAP à partir du 1er janvier 2024,
 * 20 % exonérés de TVA ; la DSM de Gascogne y vaut 1 855,95 € HT, comme la facture d'exemple.</li>
 * </ul>
 *
 * <p>Tout passe par les cas d'usage : points de départ, instruction et notes internes au nom d'Anne
 * Lamarque, ADMIN du BNIA ; saisies au nom d'un membre de chaque opérateur. Un opérateur qui a déjà un point
 * de départ n'est pas rechargé : le chargeur est relançable.
 */
@Component
@Profile("demo")
@Order(4)
public class DemoDeclarations implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDeclarations.class);

    private final GererPointDeDepart points;
    private final GererDsm dsms;
    private final AnnoterDsm notes;
    private final GererPieces pieces;
    private final GererGrilleCotisation grille;
    private final GererOperateurs operateurs;
    private final OperateursLecture lecture;
    private final ReferentielsLecture referentiels;
    private final Clock horloge;
    private Acteur anne;

    public DemoDeclarations(GererPointDeDepart points, GererDsm dsms, AnnoterDsm notes, GererPieces pieces,
            GererGrilleCotisation grille, GererOperateurs operateurs, OperateursLecture lecture,
            ReferentielsLecture referentiels, Clock horloge) {
        this.points = points;
        this.dsms = dsms;
        this.notes = notes;
        this.pieces = pieces;
        this.grille = grille;
        this.operateurs = operateurs;
        this.lecture = lecture;
        this.referentiels = referentiels;
        this.horloge = horloge;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        anne = new Acteur(DemoPersonnelBnia.anne(lecture));
        grilleCvo();
        armagnacsDeGascogne();
        maisonLarressingle();
        negoceDesLandes();
        maisonDurandCondom();
        domaineDeLaubade();
        OperateursFacturesDemo.OPERATEURS.forEach(this::operateurFacture);
    }

    /**
     * Volume de la facturation (spec 08 §7.1, lot 5), saisi par {@code saisie@demo.fr} : un négociant part du
     * 1er janvier de l'année en cours, toutes ses DSM mensuelles jusqu'au mois dernier validées (sauf
     * {@link OperateursFacturesDemo#EN_RETARD}, arrêté au mois d'avant : en retard) ; un producteur part
     * du 1er avril, sa DSM de la dernière année déclarative close validée, avec ses douze DRM. L'export a son DAE
     * (RG-DEC-29). Volumes variés d'un mois et d'un opérateur à l'autre.
     */
    private void operateurFacture(OperateursFacturesDemo.Operateur o) {
        Long id = aCharger(o.code());
        if (id == null) {
            return;
        }
        int rang = OperateursFacturesDemo.rang(o);
        Acteur saisie = membre(OperateursFacturesDemo.EMAIL_SAISIE);
        LocalDate aujourdhui = LocalDate.now(horloge);
        int annee = aujourdhui.getMonthValue() >= 4 ? aujourdhui.getYear() : aujourdhui.getYear() - 1;
        LocalDate depart = o.negociant() ? moisCourant().withMonth(1) : LocalDate.of(annee - 1, Month.APRIL, 1);
        String volume = o.negociant() ? "40000.00" : "6000.00";
        points.fixer(anne, id, depart, List.of(
                stock(CleConditionnement.NB, "VSOP", "5", volume),
                stock(CleConditionnement.NB, "XO", "10", volume),
                stock(CleConditionnement.MB, 2012 + rang % 5, null, volume)));
        int nombre = OperateursFacturesDemo.dsmValidees(o, aujourdhui);
        for (int mois = 0; mois < nombre; mois++) {
            Map<Integer, Map<ColonneDsm, String>> mouvements = o.export() == 0
                    ? Map.of(0, Map.of(ColonneDsm.C17, OperateursFacturesDemo.litres(o.sortiesFrance(), rang, mois)))
                    : Map.of(0, Map.of(ColonneDsm.C17, OperateursFacturesDemo.litres(o.sortiesFrance(), rang, mois)),
                            1, Map.of(ColonneDsm.C22, OperateursFacturesDemo.litres(o.export(), rang, mois)));
            DeclarationStocksMouvements dsm = remplir(saisie, id, mouvements).dsm();
            if (!o.negociant()) {
                for (YearMonth attendu : dsm.moisAttendus()) {
                    pieces.deposerDrm(saisie, id, dsm.id(), attendu, pdf("drm-" + attendu + ".pdf"));
                }
            }
            if (o.export() > 0) {
                pieces.deposerJustificatif(saisie, id, dsm.id(), dsm.lignes().get(1).id(),
                        TypeMouvement.VENTE_EXPORT, TypePiece.DAE,
                        String.format("26FRA%013d", 500_000 + rang * 100 + mois), pdf("dae-export.pdf"));
            }
            dsms.envoyer(saisie, id, dsm.id(), null);
            dsms.prendreEnInstruction(anne, id, dsm.id(), null);
            dsms.valider(anne, id, dsm.id(), null);
        }
        LOG.info("Démo : {} DSM validée(s) pour {}.", nombre, o.raisonSociale());
    }

    /** La grille CVO (spec 05 §7.1) : chaque taux, s'il n'est pas déjà saisi (relançable). */
    private void grilleCvo() {
        List<LocalDate> debuts = grille.consulter(anne).stream().map(t -> t.taux().debut()).toList();
        for (SaisieTauxCvo taux : List.of(
                new SaisieTauxCvo(new BigDecimal("55.00"), new BigDecimal("20.00"), LocalDate.of(2021, 1, 1)),
                new SaisieTauxCvo(new BigDecimal("60.00"), new BigDecimal("20.00"), LocalDate.of(2024, 1, 1)))) {
            if (!debuts.contains(taux.debut())) {
                grille.ajouter(anne, taux);
            }
        }
    }

    /** Le premier du mois courant : début des négociants. */
    private LocalDate moisCourant() {
        return LocalDate.now(horloge).withDayOfMonth(1);
    }

    /** L'opérateur, s'il n'a pas encore de point de départ (relançable). */
    private Long aCharger(String code) {
        long id = lecture.operateurParCode(code).orElseThrow().id();
        return points.consulter(anne, id).isPresent() ? null : id;
    }

    private Acteur membre(String email) {
        return new Acteur(lecture.utilisateurParEmail(email).orElseThrow());
    }

    /** Mois validés d'Armagnacs de Gascogne, à facturer : une facture émise, dix à facturer en lot (spec 08 §7.1). */
    private static final int MOIS_VALIDES_GASCOGNE = 11;

    /**
     * Onze DSM validées aux volumes de la facture d'exemple, à facturer (spec 08 §7.1) ; puis la DSM de M−1, aux mêmes
     * totaux, envoyée sans ses DAE : le BNIA demande les compléments. Le stock de départ couvre les sorties des onze
     * mois et laisse à M−1 le même reliquat qu'avec deux.
     */
    private void armagnacsDeGascogne() {
        Long id = aCharger("N320001");
        if (id == null) {
            LOG.info("Démo : déclarations déjà chargées.");
            return;
        }
        points.fixer(anne, id, moisCourant().minusMonths(MOIS_VALIDES_GASCOGNE + 1), List.of(
                stock(CleConditionnement.NB, "XO", "12", "10750.61"),
                stock(CleConditionnement.NB, "VSOP", "5", "27106.40"),
                stock(CleConditionnement.NV, "VSOP", "5", "865.20"),
                stock(CleConditionnement.MB, 2010, null, "642.10"),
                stock(CleConditionnement.MV, 2015, "6", "318.60"),
                stock(CleConditionnement.NB, "Blanche", "00", "1250.00")));
        Acteur julie = membre("agent@negociant.fr");
        for (int mois = 0; mois < MOIS_VALIDES_GASCOGNE; mois++) {
            DeclarationStocksMouvements validee = remplir(julie, id, Map.of(
                    0, Map.of(ColonneDsm.C17, "839.11"),
                    1, Map.of(ColonneDsm.C22, "2254.15"))).dsm();
            pieces.deposerJustificatif(julie, id, validee.id(), validee.lignes().get(1).id(), TypeMouvement.VENTE_EXPORT,
                    TypePiece.DAE, "26FRA00000000" + (98765 + mois), pdf("dae-export.pdf"));
            dsms.envoyer(julie, id, validee.id(), null);
            dsms.prendreEnInstruction(anne, id, validee.id(), null);
            dsms.valider(anne, id, validee.id(), null);
        }
        // Sorties France (14, 17, 18, 19, 21) : 839,11 L ; export (22 à 24) : 2 254,15 L.
        VueDsm vue = remplir(julie, id, Map.of(
                0, Map.of(ColonneDsm.C12, "40.00", ColonneDsm.C17, "118.35", ColonneDsm.C21, "120.00",
                        ColonneDsm.C22, "256.80"),
                1, Map.of(ColonneDsm.C06, "40.00", ColonneDsm.C17, "204.10", ColonneDsm.C23, "1300.00"),
                2, Map.of(ColonneDsm.C17, "396.66", ColonneDsm.C22, "400.00"),
                4, Map.of(ColonneDsm.C22, "297.35"),
                5, Map.of(ColonneDsm.C04, "310.00", ColonneDsm.C10, "2.50")));
        DeclarationStocksMouvements dsm = vue.dsm();
        dsms.envoyer(julie, id, dsm.id(), null);
        notes.ecrire(anne, id, dsm.id(), null,
                "Volumes export cohérents avec les DAE reçus par courrier ; à confirmer à l'instruction.");
        notes.ecrire(anne, id, dsm.id(), dsm.lignes().getFirst().id(),
                "Sorties export à rapprocher des DAE.");
        dsms.prendreEnInstruction(anne, id, dsm.id(), null);
        dsms.demanderComplements(anne, id, dsm.id(), "DAE manquants pour l'export", null);
        LOG.info("Démo : DSM de {} d'Armagnacs de Gascogne en compléments demandés.", dsm.periode().libelle());
    }

    /** DSM du mois précédent envoyée avec les DAE de ses exports : elle attend l'instruction du BNIA. */
    private void maisonLarressingle() {
        Long id = aCharger("N320002");
        if (id == null) {
            return;
        }
        points.fixer(anne, id, moisCourant().minusMonths(1), List.of(
                stock(CleConditionnement.NB, "VSOP", "4", "900.00"),
                stock(CleConditionnement.NB, "XO", "10", "1200.00"),
                stock(CleConditionnement.MV, 2008, "15", "150.00"),
                stock(CleConditionnement.NV, "Napoléon", "6", "300.00")));
        Acteur marie = membre("admin@larressingle.fr");
        DeclarationStocksMouvements dsm = remplir(marie, id, Map.of(
                0, Map.of(ColonneDsm.C17, "120.00"),
                1, Map.of(ColonneDsm.C22, "250.00"),
                3, Map.of(ColonneDsm.C22, "45.50"))).dsm();
        for (int ligne : List.of(1, 3)) {
            pieces.deposerJustificatif(marie, id, dsm.id(), dsm.lignes().get(ligne).id(), TypeMouvement.VENTE_EXPORT,
                    TypePiece.DAE, "26FRA00000000" + (54320 + ligne), pdf("dae-export.pdf"));
        }
        dsms.envoyer(marie, id, dsm.id(), null);
        LOG.info("Démo : DSM de {} de la Maison Larressingle envoyée.", dsm.periode().libelle());
    }

    /** DSM du mois précédent en brouillon, avec une anomalie qui bloque l'envoi. */
    private void negoceDesLandes() {
        Long id = aCharger("N400001");
        if (id == null) {
            return;
        }
        points.fixer(anne, id, moisCourant().minusMonths(1), List.of(
                stock(CleConditionnement.NB, "VSOP", "4", "1800.00"),
                stock(CleConditionnement.NB, "XO", "10", "650.00"),
                stock(CleConditionnement.MB, 2012, null, "400.00")));
        // Assemblages : 60 L sortis (colonne 12), 50 L entrés (colonne 6) : Σ6 ≠ Σ12.
        VueDsm vue = remplir(membre("admin@landes.fr"), id, Map.of(
                0, Map.of(ColonneDsm.C06, "50.00", ColonneDsm.C17, "310.00"),
                1, Map.of(ColonneDsm.C12, "60.00", ColonneDsm.C22, "80.00")));
        LOG.info("Démo : DSM de {} de Négoce des Landes en brouillon ({} anomalie).", vue.dsm().periode().libelle(),
                vue.anomalies().size());
    }

    /** Producteur : l'année précédente validée ; la suivante, en brouillon, vieillit au 1er avril et a une anomalie. */
    private void maisonDurandCondom() {
        Long id = aCharger("000001");
        if (id == null) {
            return;
        }
        LocalDate aujourdhui = LocalDate.now(horloge);
        int annee = aujourdhui.getMonthValue() >= 4 ? aujourdhui.getYear() : aujourdhui.getYear() - 1;
        points.fixer(anne, id, LocalDate.of(annee - 1, Month.APRIL, 1), List.of(
                stock(CleConditionnement.NB, "Hors d'âge", "19", "300.00"),
                stock(CleConditionnement.NB, "Hors d'âge", "20+", "450.00"),
                stock(CleConditionnement.MB, 2015, null, "820.00"),
                stock(CleConditionnement.MV, 2012, "10", "120.00"),
                stock(CleConditionnement.NV, "VSOP", "5", "210.00"),
                stock(CleConditionnement.NB, "Blanche", "00", "180.00")));
        Acteur jeanne = membre("admin@producteur.fr");
        DeclarationStocksMouvements dsm = remplir(jeanne, id, Map.of(
                0, Map.of(ColonneDsm.C17, "50.00"),
                1, Map.of(ColonneDsm.C22, "80.00"),
                2, Map.of(ColonneDsm.C10, "3.20"),
                4, Map.of(ColonneDsm.C17, "95.40"),
                5, Map.of(ColonneDsm.C04, "600.00"))).dsm();
        // Lot 3 : ses douze DRM et le DAE de l'export (ligne 2), sans quoi la validation est refusée (RG-DEC-29).
        for (YearMonth mois : dsm.moisAttendus()) {
            pieces.deposerDrm(jeanne, id, dsm.id(), mois, pdf("drm-" + mois + ".pdf"));
        }
        pieces.deposerJustificatif(jeanne, id, dsm.id(), dsm.lignes().get(1).id(), TypeMouvement.VENTE_EXPORT,
                TypePiece.DAE, "26FRA0000000123456", pdf("dae-export.pdf"));
        dsms.envoyer(jeanne, id, dsm.id(), null);
        dsms.prendreEnInstruction(anne, id, dsm.id(), null);
        dsms.valider(anne, id, dsm.id(), null);
        // Assemblages : 60 L sortis (colonne 12), 50 L entrés (colonne 6) : Σ6 ≠ Σ12.
        VueDsm suivante = remplir(jeanne, id, Map.of(
                0, Map.of(ColonneDsm.C06, "50.00", ColonneDsm.C17, "40.00"),
                1, Map.of(ColonneDsm.C12, "60.00")));
        LOG.info("Démo : DSM {} de la Maison Durand, Condom, validée ; la suivante en brouillon ({} anomalie).",
                dsm.periode().libelle(), suivante.anomalies().size());
    }

    /**
     * Producteur sorti du circuit : sa DSM d'il y a deux ans validée, puis son statut de déclaration passé à
     * {@code SOMMEIL}. Le BNIA fixe à la main un nouveau point de départ, puis repasse le statut à {@code OUI}.
     */
    private void domaineDeLaubade() {
        Long id = aCharger("000003");
        if (id == null) {
            return;
        }
        LocalDate aujourdhui = LocalDate.now(horloge);
        int annee = aujourdhui.getMonthValue() >= 4 ? aujourdhui.getYear() : aujourdhui.getYear() - 1;
        points.fixer(anne, id, LocalDate.of(annee - 2, Month.APRIL, 1), List.of(
                stock(CleConditionnement.NB, "Hors d'âge", "15", "420.00"),
                stock(CleConditionnement.MB, 2008, null, "260.00"),
                stock(CleConditionnement.NB, "VSOP", "4", "380.00")));
        Acteur helene = membre("admin@laubade.fr");
        DeclarationStocksMouvements dsm = remplir(helene, id, Map.of(
                0, Map.of(ColonneDsm.C17, "60.00"),
                2, Map.of(ColonneDsm.C04, "150.00", ColonneDsm.C17, "90.00"))).dsm();
        for (YearMonth mois : dsm.moisAttendus()) {
            pieces.deposerDrm(helene, id, dsm.id(), mois, pdf("drm-" + mois + ".pdf"));
        }
        dsms.envoyer(helene, id, dsm.id(), null);
        dsms.prendreEnInstruction(anne, id, dsm.id(), null);
        dsms.valider(anne, id, dsm.id(), null);
        operateurs.modifierProduction(anne, id, new SaisieProduction(PeriodiciteDsm.ANNUELLE,
                StatutDeclarationStock.SOMMEIL, referentiels.regionParCode("GERS").map(r -> r.id()).orElse(null),
                Set.of(Appellation.BAS_ARMAGNAC),
                List.of(new SaisieProduction.Alambic(null, TypeAlambic.ARMAGNACAIS, new BigDecimal("20"))), null));
        LOG.info("Démo : DSM {} du Domaine de Laubade validée, statut de déclaration en sommeil.",
                dsm.periode().libelle());
    }

    /** Le PDF de démonstration, sous ce nom (spec 05 §7.1 : un même document pour toutes les pièces). */
    private static FichierDepose pdf(String nom) {
        try (InputStream flux = DemoDeclarations.class.getResourceAsStream("/demo/piece-demonstration.pdf")) {
            return new FichierDepose(nom, flux.readAllBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Initie la DSM de l'opérateur et l'enregistre avec ces mouvements, par rang de ligne. */
    private VueDsm remplir(Acteur membre, long operateur, Map<Integer, Map<ColonneDsm, String>> mouvements) {
        DeclarationStocksMouvements dsm = dsms.initier(membre, operateur).dsm();
        List<SaisieLigneDsm> lignes = new ArrayList<>();
        for (int rang = 0; rang < dsm.lignes().size(); rang++) {
            lignes.add(saisie(dsm.lignes().get(rang), mouvements.getOrDefault(rang, Map.of())));
        }
        return dsms.enregistrer(membre, operateur, dsm.id(), new SaisieDsm(null, lignes), null);
    }

    private static SaisieLigneStock stock(CleConditionnement cle, Object millesimeOuDenomination, String compte,
            String volume) {
        return millesimeOuDenomination instanceof Integer millesime
                ? new SaisieLigneStock(cle, millesime, null, compte, VolumeAlcoolPur.litres(volume))
                : new SaisieLigneStock(cle, null, (String) millesimeOuDenomination, compte,
                        VolumeAlcoolPur.litres(volume));
    }

    private static SaisieLigneDsm saisie(LigneDsm ligne, Map<ColonneDsm, String> volumes) {
        IdentiteLigne identite = ligne.identite();
        Map<ColonneDsm, VolumeAlcoolPur> saisis = new EnumMap<>(ColonneDsm.class);
        volumes.forEach((colonne, litres) -> saisis.put(colonne, VolumeAlcoolPur.litres(litres)));
        return new SaisieLigneDsm(ligne.id(), identite.cle(),
                identite.millesime() == null ? null : identite.millesime().annee(),
                identite.denomination() == null ? null : identite.denomination().texte(), identite.compte().toString(),
                saisis);
    }
}
