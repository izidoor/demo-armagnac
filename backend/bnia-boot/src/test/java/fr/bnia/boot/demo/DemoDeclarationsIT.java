package fr.bnia.boot.demo;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.AnnoterDsm;
import fr.bnia.declarations.application.FiltreDsm;
import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.application.GererGrilleCotisation;
import fr.bnia.declarations.application.GererPieces;
import fr.bnia.declarations.application.GererPointDeDepart;
import fr.bnia.declarations.application.ListerDsm;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.NoteInterne;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.Manque;
import fr.bnia.declarations.domain.TypeAnomalie;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.support.AbstractIT;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

/**
 * Chargeur de démonstration des déclarations (spec 05, §7.1, lots 1 à 7), après le personnel, les
 * référentiels et les opérateurs : relançable, et conforme à la spec.
 */
class DemoDeclarationsIT extends AbstractIT {

    @Autowired GererPointDeDepart points;
    @Autowired GererDsm dsms;
    @Autowired AnnoterDsm notes;
    @Autowired fr.bnia.messagerie.application.Converser converser;
    @Autowired GererPieces pieces;
    @Autowired GererGrilleCotisation grille;
    @Autowired ListerDsm listing;
    @Autowired ApplicationContext contexte;

    private Acteur anne;

    private long id(String code) {
        return operateurs.operateurParCode(code).orElseThrow().id();
    }

    /** La seule DSM de l'opérateur, dans cet état, qui commence à cette date. */
    private DsmLigne seule(String code, EtatDsm etat, LocalDate debut) {
        List<DsmLigne> declarations = dsms.lister(anne, id(code)).stream().map(GererDsm.DsmDeLOperateur::dsm).toList();
        assertThat(declarations).as(code).singleElement().satisfies(d -> {
            assertThat(d.etat()).isEqualTo(etat);
            assertThat(d.periode().debut()).isEqualTo(debut);
        });
        return declarations.getFirst();
    }

    @Test
    void charge_le_cycle_des_negociants_et_la_reprise_du_producteur_et_peut_etre_relance() {
        var fabrique = contexte.getAutowireCapableBeanFactory();
        var personnel = fabrique.createBean(DemoPersonnelBnia.class);
        var referentiels = fabrique.createBean(DemoReferentiels.class);
        var demoOperateurs = fabrique.createBean(DemoOperateurs.class);
        var demo = fabrique.createBean(DemoDeclarations.class);
        for (int i = 0; i < 2; i++) {
            personnel.run(new DefaultApplicationArguments());
            referentiels.run(new DefaultApplicationArguments());
            demoOperateurs.run(new DefaultApplicationArguments());
            demo.run(new DefaultApplicationArguments());
        }
        anne = new Acteur(DemoPersonnelBnia.anne(operateurs));
        LocalDate mois = LocalDate.now().withDayOfMonth(1);

        long gascogne = id("N320001");
        assertThat(points.consulter(anne, gascogne).orElseThrow().point().lignes()).hasSize(6);
        // Spec 08 §7.1 : onze mois validés, à facturer, avant M−1 en compléments demandés ; le mois courant reste à
        // initier (spec 06 RG-CA-24).
        List<DsmLigne> declarations = dsms.lister(anne, gascogne).stream().map(GererDsm.DsmDeLOperateur::dsm).toList();
        List<org.assertj.core.groups.Tuple> attendues = new java.util.ArrayList<>();
        attendues.add(org.assertj.core.groups.Tuple.tuple(EtatDsm.COMPLEMENTS_DEMANDES, mois.minusMonths(1)));
        for (int m = 2; m <= 12; m++) {
            attendues.add(org.assertj.core.groups.Tuple.tuple(EtatDsm.VALIDEE, mois.minusMonths(m)));
        }
        assertThat(declarations).extracting(DsmLigne::etat, d -> d.periode().debut()).containsExactlyElementsOf(attendues);
        assertThat(dsms.consulter(anne, gascogne, declarations.get(1).id()).cotisation().ht())
                .contains(new BigDecimal("1855.95"));
        var courante = dsms.consulter(anne, gascogne, declarations.getFirst().id());
        assertThat(courante.anomalies()).isEmpty();
        List<LigneDsm> lignes = courante.dsm().lignes();
        assertThat(somme(lignes, ColonneDsm.C14, ColonneDsm.C17, ColonneDsm.C18, ColonneDsm.C19, ColonneDsm.C21))
                .isEqualTo("839.11");
        assertThat(somme(lignes, ColonneDsm.C22, ColonneDsm.C23, ColonneDsm.C24)).isEqualTo("2254.15");
        assertThat(notes.lister(anne, gascogne, courante.dsm().id())).extracting(NoteInterne::ligne)
                .containsExactly(null, courante.dsm().lignes().getFirst().id());
        assertThat(courante.manques()).extracting(Manque::typeMouvement)
                .containsExactly(TypeMouvement.VENTE_NEGOCE, TypeMouvement.VENTE_EXPORT);
        // La facture d'exemple : 1 855,95 € HT à 60 €/hlAP, 20 % exonérés.
        assertThat(courante.cotisation().ht()).contains(new BigDecimal("1855.95"));
        assertThat(grille.consulter(anne)).extracting(t -> t.taux().debut())
                .contains(LocalDate.of(2021, 1, 1), LocalDate.of(2024, 1, 1));

        long larressingle = id("N320002");
        var aInstruire = dsms.consulter(anne, larressingle,
                seule("N320002", EtatDsm.ENVOYEE, mois.minusMonths(1)).id());
        assertThat(aInstruire.manques()).as("DAE déposés : validable").isEmpty();
        assertThat(aInstruire.enRetard()).as("attend le BNIA").isFalse();
        assertThat(courante.enRetard()).as("M−1 chez l'opérateur, échéance le 5").isEqualTo(LocalDate.now().getDayOfMonth() > 5);
        // RG-DEC-40 : Maison Saint-Puy n'a pas initié la DSM du mois dernier ; le listing ne la montre qu'en retard,
        // dès le 6 du mois, et c'en est la seule période absente.
        var absentes = listing.lister(anne, new FiltreDsm(Optional.empty(), Optional.empty(), Optional.empty(),
                Set.of())).stream().filter(ListerDsm.DsmListee::absente).toList();
        if (LocalDate.now().getDayOfMonth() > 5) {
            assertThat(absentes).singleElement().satisfies(l -> {
                assertThat(l.code()).isEqualTo(OperateursFacturesDemo.EN_RETARD);
                assertThat(l.periode().debut()).isEqualTo(mois.minusMonths(1));
                assertThat(l.enRetard()).isTrue();
            });
        } else {
            assertThat(absentes).isEmpty();
        }
        assertThat(converser.consulterObjet(new fr.bnia.messagerie.api.Acteur(anne.utilisateur()),
                fr.bnia.messagerie.api.ObjetRef.de(fr.bnia.messagerie.api.TypeObjet.DSM, courante.dsm().id())).messages())
                .as("le motif ouvre la conversation")
                .first().satisfies(m -> {
                    assertThat(m.message().texte()).isEqualTo("DAE manquants pour l'export");
                    assertThat(m.message().protege()).isTrue();
                });

        var brouillon = dsms.consulter(anne, id("N400001"), seule("N400001", EtatDsm.BROUILLON, mois.minusMonths(1)).id());
        assertThat(brouillon.anomalies()).extracting(a -> a.type())
                .containsExactly(TypeAnomalie.ASSEMBLAGES_DESEQUILIBRES);

        LocalDate aujourdhui = LocalDate.now();
        int annee = aujourdhui.getMonthValue() >= 4 ? aujourdhui.getYear() : aujourdhui.getYear() - 1;
        List<DsmLigne> deCondom = dsms.lister(anne, id("000001")).stream().map(GererDsm.DsmDeLOperateur::dsm).toList();
        assertThat(deCondom).extracting(DsmLigne::etat, d -> d.periode().debut()).containsExactly(
                org.assertj.core.groups.Tuple.tuple(EtatDsm.BROUILLON, LocalDate.of(annee, 4, 1)),
                org.assertj.core.groups.Tuple.tuple(EtatDsm.VALIDEE, LocalDate.of(annee - 1, 4, 1)));
        assertThat(dsms.consulter(anne, id("000001"), deCondom.getFirst().id()).anomalies()).extracting(a -> a.type())
                .containsExactly(TypeAnomalie.ASSEMBLAGES_DESEQUILIBRES);
        DsmLigne validee = deCondom.get(1);
        var piecesCondom = pieces.pieces(anne, id("000001"), validee.id());
        assertThat(piecesCondom.mois()).hasSize(12).allSatisfy(m -> assertThat(m.drm()).isPresent());
        assertThat(piecesCondom.justificatifs()).singleElement()
                .satisfies(j -> assertThat(j.typePiece()).isEqualTo(TypePiece.DAE));
        assertThat(piecesCondom.manques()).isEmpty();

        assertThat(points.consulter(anne, id("000002"))).isEmpty();

        // Lot 7 : le Domaine de Laubade, en sommeil après sa DSM d'il y a deux ans, attend un nouveau point de départ.
        seule("000003", EtatDsm.VALIDEE, LocalDate.of(annee - 2, 4, 1));
        assertThat(operateurs.operateur(id("000003")).orElseThrow().statutDeclarationStock())
                .isEqualTo(StatutDeclarationStock.SOMMEIL);
        assertThat(points.consulter(anne, id("000003")).orElseThrow().renouvelable()).isTrue();
    }

    private static String somme(List<LigneDsm> lignes, ColonneDsm... colonnes) {
        return lignes.stream()
                .flatMap(l -> Stream.of(colonnes).map(l::volume))
                .reduce(VolumeAlcoolPur.ZERO, VolumeAlcoolPur::plus)
                .toString();
    }
}
