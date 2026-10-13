package fr.bnia.boot.demo;

import fr.bnia.commun.domain.VolumeAlcoolPur;
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
 * Données de démonstration des déclarations (spec 05, §7.1, lot 1), en profil {@code demo} seulement, après
 * les opérateurs. Les dates sont calculées depuis la date du jour : le point de départ d'Armagnacs de
 * Gascogne ({@code N320001}) est le premier du mois, quatre mois avant le mois courant ; sa DSM de ce mois
 * est en brouillon, remplie par Julie Darrigade ({@code agent@negociant.fr}), avec une anomalie (Σ6 ≠ Σ12).
 * La Maison Durand d'Eauze ({@code 000002}) n'a pas de point de départ : le BNIA le fixe en démonstration.
 *
 * <p>Tout passe par les cas d'usage : le point de départ au nom d'Anne Lamarque, ADMIN du BNIA ; la DSM au
 * nom de Julie Darrigade. Un opérateur qui a déjà un point de départ n'est pas rechargé : le chargeur est
 * relançable.
 */
@Component
@Profile("demo")
@Order(4)
public class DemoDeclarations implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDeclarations.class);

    private final GererPointDeDepart points;
    private final GererDsm dsms;
    private final OperateursLecture lecture;
    private final Clock horloge;

    public DemoDeclarations(GererPointDeDepart points, GererDsm dsms, OperateursLecture lecture, Clock horloge) {
        this.points = points;
        this.dsms = dsms;
        this.lecture = lecture;
        this.horloge = horloge;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        Acteur anne = new Acteur(DemoPersonnelBnia.anne(lecture));
        long negociant = lecture.operateurParCode("N320001").orElseThrow().id();
        if (points.consulter(anne, negociant).isPresent()) {
            LOG.info("Démo : déclarations déjà chargées.");
            return;
        }
        LocalDate debut = LocalDate.now(horloge).withDayOfMonth(1).minusMonths(4);
        points.fixer(anne, negociant, debut, List.of(
                stock(CleConditionnement.NB, "XO", "12", "1520.40"),
                stock(CleConditionnement.NB, "VSOP", "5", "2310.75"),
                stock(CleConditionnement.NV, "VSOP", "5", "865.20"),
                stock(CleConditionnement.MB, 2010, null, "642.10"),
                stock(CleConditionnement.MV, 2015, "6", "318.60"),
                stock(CleConditionnement.NB, "Blanche", "00", "1250.00")));

        Acteur julie = new Acteur(lecture.utilisateurParEmail("agent@negociant.fr").orElseThrow());
        VueDsm initiee = dsms.initier(julie, negociant);
        DeclarationStocksMouvements dsm = initiee.dsm();
        Map<Integer, Map<ColonneDsm, String>> mouvements = Map.of(
                0, Map.of(ColonneDsm.C06, "40.00", ColonneDsm.C17, "118.35", ColonneDsm.C22, "256.80"),
                1, Map.of(ColonneDsm.C12, "50.00", ColonneDsm.C17, "204.10", ColonneDsm.C21, "120.00"),
                2, Map.of(ColonneDsm.C17, "96.45"),
                5, Map.of(ColonneDsm.C04, "310.00", ColonneDsm.C10, "2.50", ColonneDsm.C18, "75.00"));
        List<SaisieLigneDsm> lignes = new ArrayList<>();
        for (int rang = 0; rang < dsm.lignes().size(); rang++) {
            lignes.add(saisie(dsm.lignes().get(rang), mouvements.getOrDefault(rang, Map.of())));
        }
        VueDsm enregistree = dsms.enregistrer(julie, negociant, dsm.id(), new SaisieDsm(null, lignes), null);
        LOG.info("Démo : point de départ et DSM de {} d'Armagnacs de Gascogne ({} anomalie).",
                dsm.periode().libelle(), enregistree.anomalies().size());
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
