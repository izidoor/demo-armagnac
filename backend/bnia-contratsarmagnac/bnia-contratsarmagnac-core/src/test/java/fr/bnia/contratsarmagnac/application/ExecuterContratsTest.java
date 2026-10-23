package fr.bnia.contratsarmagnac.application;

import static fr.bnia.contratsarmagnac.application.Monde.AGENT_BNIA;
import static fr.bnia.contratsarmagnac.application.Monde.BNIA;
import static fr.bnia.contratsarmagnac.application.Monde.HENRI;
import static fr.bnia.contratsarmagnac.application.Monde.JEANNE;
import static fr.bnia.contratsarmagnac.application.Monde.NEGOCIANT;
import static fr.bnia.contratsarmagnac.application.Monde.OBSERVATEUR_PRODUCTEUR;
import static fr.bnia.contratsarmagnac.application.Monde.PRODUCTEUR;
import static fr.bnia.contratsarmagnac.application.Monde.TIERS;
import static fr.bnia.contratsarmagnac.application.Monde.millesime2015;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.contratsarmagnac.application.ConsulterContrats.Onglet;
import fr.bnia.contratsarmagnac.application.ExecuterContrats.SaisieRetiraison;
import fr.bnia.contratsarmagnac.application.ExecuterContrats.SaisieRetiraison.SaisieLigneRetiraison;
import fr.bnia.contratsarmagnac.application.NegocierContrats.SaisieContrat;
import fr.bnia.contratsarmagnac.domain.ActionContrat;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.EtatRetiraison;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import fr.bnia.declarations.domain.partage.RetiraisonValidee;
import fr.bnia.declarations.domain.partage.StockEstimeVue;
import fr.bnia.declarations.domain.partage.StockEstimeVue.LigneStockEstimeVue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Exécution d'un contrat conclu (UC-CA-3, 4, 5) : retiraisons, annulation, clôture, et leurs refus. */
class ExecuterContratsTest {

    private static final LocalDate LE_30 = LocalDate.of(2026, 10, 30);

    private final Monde monde = new Monde();
    private final ExecuterContrats cas = monde.executer();
    private long id;

    /** Le contrat de la démonstration, conclu : Jeanne vend à Henri 15 hl de 2015 à 47 % (7,05 hl AP). */
    @BeforeEach
    void conclu() {
        NegocierContrats negocier = monde.negocier();
        id = negocier.ouvrir(JEANNE, PRODUCTEUR, new SaisieContrat(Partie.VENDEUR, NEGOCIANT,
                millesime2015("15", "1750"))).id();
        negocier.approuver(HENRI, NEGOCIANT, id, 1);
        monde.courriels.envoyes.clear();
    }

    private static SaisieRetiraison retiraison(LocalDate date, boolean derniere, String volume) {
        return new SaisieRetiraison(date, derniere, List.of(new SaisieLigneRetiraison(0, new BigDecimal(volume))));
    }

    /** Le stock estimé de Jeanne réduit à 1 hl AP de 2015. */
    private void stockReduit() {
        VolumeAlcoolPur litres = VolumeAlcoolPur.litres("100");
        when(monde.declarations.stockEstime(PRODUCTEUR)).thenReturn(new StockEstimeVue(PRODUCTEUR,
                Optional.of(LocalDate.of(2026, 3, 31)), List.of(new LigneStockEstimeVue(CleConditionnement.MB, 2015,
                null, CompteDAgeDsm.de("10"), litres, VolumeAlcoolPur.ZERO, VolumeAlcoolPur.ZERO, litres))));
    }

    private static String regle(Throwable e) {
        return ((RegleEnfreinte) e).regle().orElseThrow();
    }

    @Test
    @DisplayName("RG-CA-13, 15, 18, 19 : Henri déclare, Jeanne valide : RetiraisonValidee est publié, l'autre partie reçoit chaque fois un e-mail")
    void rg_ca_15_le_fil_de_la_retiraison() {
        cas.declarerRetiraison(HENRI, NEGOCIANT, id, retiraison(LE_30, false, "10"));
        assertThat(monde.courriels.a("admin@producteur.fr")).singleElement().satisfies(c -> {
            assertThat(c.sujet()).isEqualTo("Retiraison à valider, contrat CA-2026-0001");
            assertThat(c.texte()).contains("Henri Castex (Armagnacs de Gascogne) a déclaré la retiraison n° 1")
                    .contains("pour le 30 octobre 2026").contains("https://bnia.example/portail/contrats/" + id);
        });
        assertThat(monde.consulter().duPortail(JEANNE, PRODUCTEUR)).singleElement()
                .satisfies(c -> assertThat(c.onglet()).contains(Onglet.A_APPROUVER));
        assertThat(monde.consulter().duPortail(HENRI, NEGOCIANT)).singleElement()
                .satisfies(c -> assertThat(c.onglet()).contains(Onglet.EN_NEGOCIATION));

        ContratAchatArmagnac contrat = cas.validerRetiraison(JEANNE, PRODUCTEUR, id, 1, 1);

        assertThat(contrat.retiraison(1).etat()).isEqualTo(EtatRetiraison.VALIDEE);
        assertThat(contrat.etat()).isEqualTo(EtatContrat.CONCLU);
        assertThat(monde.evenements).singleElement().isInstanceOfSatisfying(RetiraisonValidee.class, e -> {
            assertThat(e.contrat()).isEqualTo(id);
            assertThat(e.numeroContrat()).isEqualTo("CA-2026-0001");
            assertThat(e.retiraison()).isEqualTo(1);
            assertThat(e.dateEnlevement()).isEqualTo(LE_30);
            assertThat(e.vendeur()).isEqualTo(PRODUCTEUR);
            assertThat(e.acheteur()).isEqualTo(NEGOCIANT);
            assertThat(e.validePar()).isEqualTo(JEANNE.utilisateur());
            assertThat(e.lignes()).singleElement().satisfies(l -> {
                assertThat(l.cle()).isEqualTo(CleConditionnement.MB);
                assertThat(l.millesime()).isEqualTo(2015);
                assertThat(l.volumeAp()).isEqualTo(VolumeAlcoolPur.litres("470.00"));
            });
        });
        assertThat(monde.courriels.a("admin@negociant.fr")).singleElement()
                .satisfies(c -> assertThat(c.sujet()).isEqualTo("Retiraison validée, contrat CA-2026-0001"));
        assertThat(monde.journal.duContrat(id)).extracting(EntreeJournal::action).endsWith(
                ActionContrat.RETIRAISON_DECLAREE, ActionContrat.RETIRAISON_VALIDEE);
        assertThat(monde.consulter().duPortail(JEANNE, PRODUCTEUR)).singleElement()
                .satisfies(c -> assertThat(c.onglet()).contains(Onglet.CONCLUS));
    }

    @Test
    @DisplayName("RG-CA-13 : le stock estimé du vendeur doit couvrir la retiraison ; le refus ne montre aucun chiffre à l'acheteur")
    void rg_ca_13_refus_sans_chiffre_pour_l_acheteur() {
        stockReduit();
        assertThatThrownBy(() -> cas.declarerRetiraison(HENRI, NEGOCIANT, id, retiraison(LE_30, false, "10")))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Le stock estimé du vendeur ne couvre pas cette retiraison : voyez avec lui.")
                .extracting(ExecuterContratsTest::regle).isEqualTo("RG-CA-13");
        assertThat(monde.contrats.parId(id).orElseThrow().retiraisons()).isEmpty();
    }

    @Test
    @DisplayName("RG-CA-14, 15 : au vendeur, le refus donne les chiffres ; au détail, la retiraison qu'il ne peut valider est signalée")
    void rg_ca_15_refus_chiffre_pour_le_vendeur() {
        cas.declarerRetiraison(HENRI, NEGOCIANT, id, retiraison(LE_30, false, "10"));
        stockReduit();

        assertThatThrownBy(() -> cas.validerRetiraison(JEANNE, PRODUCTEUR, id, 1, 1))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessage("Votre stock estimé ne couvre pas « millésime 2015 » : 4,70 hl AP à retirer, 1,00 hl AP"
                        + " en stock.")
                .extracting(ExecuterContratsTest::regle).isEqualTo("RG-CA-15");
        assertThat(monde.evenements).isEmpty();
        assertThatThrownBy(() -> cas.modifierRetiraison(JEANNE, PRODUCTEUR, id, 1, 1, retiraison(LE_30, false, "5")))
                .extracting(ExecuterContratsTest::regle).isEqualTo("RG-CA-14");
        assertThat(monde.consulter().auPortail(JEANNE, PRODUCTEUR, id).retiraisonsSansStock())
                .containsOnlyKeys(1);
        assertThat(monde.consulter().auPortail(HENRI, NEGOCIANT, id).retiraisonsSansStock()).isEmpty();

        cas.modifierRetiraison(JEANNE, PRODUCTEUR, id, 1, 1, retiraison(LE_30, false, "2"));
        assertThat(monde.courriels.a("admin@negociant.fr")).last()
                .satisfies(c -> assertThat(c.sujet()).isEqualTo("Retiraison modifiée, contrat CA-2026-0001"));
        assertThat(monde.consulter().auPortail(HENRI, NEGOCIANT, id).contrat().retiraison(1).ecartPour(Partie.ACHETEUR)
                .lignes()).hasSize(1);
    }

    @Test
    @DisplayName("RG-CA-16, 17 : Henri retire une retiraison ; Jeanne annule le contrat, Henri en est averti")
    void rg_ca_17_retrait_et_annulation() {
        cas.declarerRetiraison(HENRI, NEGOCIANT, id, retiraison(LE_30, false, "10"));
        cas.retirerRetiraison(HENRI, NEGOCIANT, id, 1);
        assertThat(monde.contrats.parId(id).orElseThrow().retiraisons()).isEmpty();

        ContratAchatArmagnac annule = cas.annuler(JEANNE, PRODUCTEUR, id);

        assertThat(annule.etat()).isEqualTo(EtatContrat.ANNULE);
        assertThat(monde.courriels.a("admin@negociant.fr")).last()
                .satisfies(c -> assertThat(c.sujet()).isEqualTo("Contrat CA-2026-0001 annulé"));
        assertThat(monde.journal.duContrat(id)).extracting(EntreeJournal::action)
                .endsWith(ActionContrat.RETIRAISON_RETIREE, ActionContrat.ANNULATION);
    }

    @Test
    @DisplayName("RG-CA-17 : une partie déclare qu'il n'y aura plus de retiraison : exécuté, l'autre est avertie")
    void rg_ca_17_cloture() {
        assertThat(cas.cloturer(HENRI, NEGOCIANT, id).etat()).isEqualTo(EtatContrat.EXECUTE);
        assertThat(monde.courriels.a("admin@producteur.fr")).singleElement()
                .satisfies(c -> assertThat(c.sujet()).isEqualTo("Contrat CA-2026-0001 exécuté"));
    }

    @Test
    @DisplayName("RG-CA-01 : le BNIA, un observateur et un non-partie n'agissent sur aucune retiraison")
    void rg_ca_01_refus() {
        assertThatThrownBy(() -> cas.declarerRetiraison(AGENT_BNIA, BNIA, id, retiraison(LE_30, false, "1")))
                .isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.annuler(OBSERVATEUR_PRODUCTEUR, PRODUCTEUR, id)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.cloturer(HENRI, TIERS, id)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.validerRetiraison(JEANNE, PRODUCTEUR, id + 1, 1, 1))
                .isInstanceOf(ElementIntrouvable.class);
    }
}
