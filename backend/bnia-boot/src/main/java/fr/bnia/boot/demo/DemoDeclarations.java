package fr.bnia.boot.demo;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.CommenterDsm;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.application.GererPointDeDepart;
import fr.bnia.declarations.application.VueDsm;
import fr.bnia.declarations.domain.CleConditionnement;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.SaisieLigneStock;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Données de démonstration des déclarations (spec 05, §7.1, lots 1 et 2), en profil {@code demo} seulement,
 * après les opérateurs. Les dates sont calculées depuis la date du jour (décision du 2026-10-13) :
 * <ul>
 * <li>les trois négociants partent du premier du mois courant, avec un stock fixé par le BNIA au dernier
 * jour du mois précédent ; leur première DSM est celle du mois courant :
 * <ul>
 * <li>Armagnacs de Gascogne ({@code N320001}) : envoyée, sorties France 839,11 L et export 2 254,15 L (la
 * facture d'exemple), un commentaire de l'opérateur et un commentaire interne du BNIA ;</li>
 * <li>Maison Larressingle ({@code N320002}) : en compléments demandés, motif « DAE manquants pour
 * l'export » ;</li>
 * <li>Négoce des Landes ({@code N400001}) : en brouillon, avec une anomalie (Σ6 ≠ Σ12) ;</li>
 * </ul></li>
 * <li>Maison Durand, Condom ({@code 000001}, producteur) : point de départ au 1er avril de l'an dernier, DSM
 * de cette année-là validée ; la suivante, à initier, montre le vieillissement et la fusion à {@code 20+} ;</li>
 * <li>Maison Durand, Eauze ({@code 000002}) : pas de point de départ, le BNIA le fixe en démonstration.</li>
 * </ul>
 *
 * <p>Tout passe par les cas d'usage : points de départ, instruction et commentaire interne au nom d'Anne
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
    private final CommenterDsm commentaires;
    private final OperateursLecture lecture;
    private final Clock horloge;
    private Acteur anne;

    public DemoDeclarations(GererPointDeDepart points, GererDsm dsms, CommenterDsm commentaires,
            OperateursLecture lecture, Clock horloge) {
        this.points = points;
        this.dsms = dsms;
        this.commentaires = commentaires;
        this.lecture = lecture;
        this.horloge = horloge;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        anne = new Acteur(DemoPersonnelBnia.anne(lecture));
        armagnacsDeGascogne();
        maisonLarressingle();
        negoceDesLandes();
        maisonDurandCondom();
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

    /** DSM envoyée, qui reproduit les volumes de la facture d'exemple ; la file d'attente du BNIA. */
    private void armagnacsDeGascogne() {
        Long id = aCharger("N320001");
        if (id == null) {
            LOG.info("Démo : déclarations déjà chargées.");
            return;
        }
        points.fixer(anne, id, moisCourant(), List.of(
                stock(CleConditionnement.NB, "XO", "12", "1520.40"),
                stock(CleConditionnement.NB, "VSOP", "5", "2310.75"),
                stock(CleConditionnement.NV, "VSOP", "5", "865.20"),
                stock(CleConditionnement.MB, 2010, null, "642.10"),
                stock(CleConditionnement.MV, 2015, "6", "318.60"),
                stock(CleConditionnement.NB, "Blanche", "00", "1250.00")));
        Acteur julie = membre("agent@negociant.fr");
        // Sorties France (14, 17, 18, 19, 21) : 839,11 L ; export (22 à 24) : 2 254,15 L.
        VueDsm vue = remplir(julie, id, Map.of(
                0, Map.of(ColonneDsm.C12, "40.00", ColonneDsm.C17, "118.35", ColonneDsm.C21, "120.00",
                        ColonneDsm.C22, "256.80"),
                1, Map.of(ColonneDsm.C06, "40.00", ColonneDsm.C17, "204.10", ColonneDsm.C23, "1300.00"),
                2, Map.of(ColonneDsm.C17, "396.66", ColonneDsm.C22, "400.00"),
                4, Map.of(ColonneDsm.C22, "297.35"),
                5, Map.of(ColonneDsm.C04, "310.00", ColonneDsm.C10, "2.50")));
        DeclarationStocksMouvements dsm = vue.dsm();
        commentaires.ecrire(julie, id, dsm.id(), dsm.lignes().getFirst().id(),
                "40 L de XO passés dans l'assemblage VSOP (colonnes 12 et 6).", false);
        dsms.envoyer(julie, id, dsm.id(), null);
        commentaires.ecrire(anne, id, dsm.id(), null,
                "Volumes export cohérents avec les DAE reçus par courrier ; à confirmer à l'instruction.", true);
        LOG.info("Démo : DSM de {} d'Armagnacs de Gascogne envoyée.", dsm.periode().libelle());
    }

    /** DSM renvoyée à l'opérateur : compléments demandés. */
    private void maisonLarressingle() {
        Long id = aCharger("N320002");
        if (id == null) {
            return;
        }
        points.fixer(anne, id, moisCourant(), List.of(
                stock(CleConditionnement.NB, "VSOP", "4", "900.00"),
                stock(CleConditionnement.NB, "XO", "10", "1200.00"),
                stock(CleConditionnement.MV, 2008, "15", "150.00"),
                stock(CleConditionnement.NV, "Napoléon", "6", "300.00")));
        Acteur marie = membre("admin@larressingle.fr");
        DeclarationStocksMouvements dsm = remplir(marie, id, Map.of(
                0, Map.of(ColonneDsm.C17, "120.00"),
                1, Map.of(ColonneDsm.C22, "250.00"),
                3, Map.of(ColonneDsm.C22, "45.50"))).dsm();
        dsms.envoyer(marie, id, dsm.id(), null);
        dsms.prendreEnInstruction(anne, id, dsm.id(), null);
        dsms.demanderComplements(anne, id, dsm.id(), "DAE manquants pour l'export", null);
        LOG.info("Démo : DSM de {} de la Maison Larressingle en compléments demandés.", dsm.periode().libelle());
    }

    /** DSM en brouillon, avec une anomalie qui bloque l'envoi. */
    private void negoceDesLandes() {
        Long id = aCharger("N400001");
        if (id == null) {
            return;
        }
        points.fixer(anne, id, moisCourant(), List.of(
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

    /** Producteur : l'année précédente validée ; la suivante, à initier, vieillit au 1er avril. */
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
        dsms.envoyer(jeanne, id, dsm.id(), null);
        dsms.prendreEnInstruction(anne, id, dsm.id(), null);
        dsms.valider(anne, id, dsm.id(), null);
        LOG.info("Démo : DSM {} de la Maison Durand, Condom, validée.", dsm.periode().libelle());
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
