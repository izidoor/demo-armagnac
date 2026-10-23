package fr.bnia.contratsarmagnac.application;

import static fr.bnia.contratsarmagnac.application.Monde.AGENT_BNIA;
import static fr.bnia.contratsarmagnac.application.Monde.HENRI;
import static fr.bnia.contratsarmagnac.application.Monde.JEANNE;
import static fr.bnia.contratsarmagnac.application.Monde.NEGOCIANT;
import static fr.bnia.contratsarmagnac.application.Monde.OBSERVATEUR_PRODUCTEUR;
import static fr.bnia.contratsarmagnac.application.Monde.PRODUCTEUR;
import static fr.bnia.contratsarmagnac.application.Monde.TIERS;
import static fr.bnia.contratsarmagnac.application.Monde.millesime2015;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.contratsarmagnac.application.ConsulterContrats.ContratListe;
import fr.bnia.contratsarmagnac.application.ConsulterContrats.Onglet;
import fr.bnia.contratsarmagnac.application.ConsulterContrats.VueContrat;
import fr.bnia.contratsarmagnac.application.NegocierContrats.SaisieContrat;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.StockEstimeVue.LigneStockEstimeVue;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Listes, détail et stock pour la saisie (UC-CA-6, 8, 9). */
class ConsulterContratsTest {

    private final Monde monde = new Monde();
    private final NegocierContrats negocier = monde.negocier();
    private final ConsulterContrats cas = monde.consulter();

    private long projetDeJeanne() {
        return negocier.ouvrir(JEANNE, PRODUCTEUR, new SaisieContrat(Partie.VENDEUR, NEGOCIANT,
                millesime2015("15", "1750"))).id();
    }

    @Test
    @DisplayName("RG-CA-25 : au portail, un projet est « à approuver » pour la partie qui manque, « en négociation » pour l'autre ; conclu ensuite")
    void rg_ca_25_onglets() {
        long id = projetDeJeanne();
        long refuse = negocier.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.ACHETEUR, PRODUCTEUR,
                millesime2015("1", "1750"))).id();
        negocier.refuser(JEANNE, PRODUCTEUR, refuse, 1);

        assertThat(cas.duPortail(HENRI, NEGOCIANT)).extracting(l -> l.ligne().id(), l -> l.onglet().orElseThrow())
                .containsExactlyInAnyOrder(tuple(id, Onglet.A_APPROUVER),
                        tuple(refuse, Onglet.ANNULES_REFUSES));
        assertThat(cas.duPortail(JEANNE, PRODUCTEUR)).filteredOn(l -> l.ligne().id() == id)
                .extracting(l -> l.onglet().orElseThrow()).containsExactly(Onglet.EN_NEGOCIATION);

        negocier.approuver(HENRI, NEGOCIANT, id, 1);
        ContratListe conclu = cas.duPortail(OBSERVATEUR_PRODUCTEUR, PRODUCTEUR).stream()
                .filter(l -> l.ligne().id() == id).findFirst().orElseThrow();
        assertThat(conclu.onglet()).contains(Onglet.CONCLUS);
        assertThat(conclu.acheteur().raisonSociale()).isEqualTo("Armagnacs de Gascogne");
        assertThat(conclu.ligne().produit()).isEqualTo("2015");
        assertThat(conclu.ligne().montantHt()).isEqualByComparingTo("12337.50");
    }

    @Test
    @DisplayName("RG-CA-25 : au back-office, tous les contrats, filtrés par partie (fiche opérateur) et par numéro")
    void rg_ca_25_back_office() {
        projetDeJeanne();
        negocier.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.ACHETEUR, TIERS, millesime2015("1", "1750")));

        assertThat(cas.duBackOffice(AGENT_BNIA, Optional.empty(), Optional.empty())).hasSize(2)
                .allSatisfy(l -> assertThat(l.onglet()).isEmpty());
        assertThat(cas.duBackOffice(AGENT_BNIA, Optional.of(TIERS), Optional.empty())).hasSize(1);
        assertThat(cas.duBackOffice(AGENT_BNIA, Optional.empty(), Optional.of("ca-2026-0001"))).hasSize(1);
    }

    @Test
    @DisplayName("RG-CA-09 : le détail montre à la partie qui doit approuver l'écart avec sa dernière version approuvée")
    void rg_ca_09_detail_et_ecart() {
        long id = projetDeJeanne();
        negocier.modifier(HENRI, NEGOCIANT, id, 1, millesime2015("15", "1700"));

        VueContrat vue = cas.auPortail(JEANNE, PRODUCTEUR, id);

        assertThat(vue.maPartie()).contains(Partie.VENDEUR);
        assertThat(vue.peutAgir()).isTrue();
        assertThat(vue.campagne()).isEqualTo("2026-2027");
        assertThat(vue.ecart()).hasValueSatisfying(e -> {
            assertThat(e.depuis()).isEqualTo(1);
            assertThat(e.lignes()).singleElement().satisfies(l -> assertThat(l.champs())
                    .extracting(c -> c.champ() + " " + c.avant() + " → " + c.apres())
                    .containsExactly("prix 1750.00 → 1700.00"));
        });
        assertThat(vue.journal()).hasSize(2);
        assertThat(cas.auPortail(OBSERVATEUR_PRODUCTEUR, PRODUCTEUR, id).peutAgir()).isFalse();
    }

    @Test
    @DisplayName("RG-CA-12 : au vendeur seul, le détail d'un projet que son stock estimé ne couvre pas le dit")
    void rg_ca_12_stock_insuffisant() {
        long id = negocier.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.ACHETEUR, PRODUCTEUR,
                millesime2015("20", "1750"))).id();

        assertThat(cas.auPortail(JEANNE, PRODUCTEUR, id).stockInsuffisant()).hasValueSatisfying(m -> assertThat(m)
                .contains("millésime 2015").contains("9,40 hl AP au contrat").contains("8,17 hl AP en stock"));
        assertThat(cas.auPortail(HENRI, NEGOCIANT, id).stockInsuffisant()).isEmpty();
        assertThat(cas.auBackOffice(AGENT_BNIA, id).stockInsuffisant()).isEmpty();
        assertThat(cas.auPortail(JEANNE, PRODUCTEUR, projetDeJeanne()).stockInsuffisant()).isEmpty();
    }

    @Test
    @DisplayName("RG-CA-01 : le BNIA lit tous les contrats, en lecture seule ; un tiers ne voit pas le contrat des autres")
    void rg_ca_01_lecture() {
        long id = projetDeJeanne();

        VueContrat bnia = cas.auBackOffice(AGENT_BNIA, id);
        assertThat(bnia.peutAgir()).isFalse();
        assertThat(bnia.maPartie()).isEmpty();
        assertThat(bnia.ecart()).isPresent();
        assertThatThrownBy(() -> cas.duBackOffice(HENRI, Optional.empty(), Optional.empty()))
                .isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.auBackOffice(JEANNE, id)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.duPortail(HENRI, PRODUCTEUR)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.auPortail(Monde.INCONNU, TIERS, id)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.auPortail(AGENT_BNIA, TIERS, id)).isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-CA-11 : le vendeur choisit dans son stock estimé sous bois, lignes non nulles seulement")
    void rg_ca_11_stock_sous_bois() {
        assertThat(cas.stockSousBois(JEANNE, PRODUCTEUR)).extracting(LigneStockEstimeVue::cle)
                .containsExactly(CleConditionnement.MB, CleConditionnement.NB);
        assertThatThrownBy(() -> cas.stockSousBois(OBSERVATEUR_PRODUCTEUR, PRODUCTEUR))
                .isInstanceOf(AccesRefuse.class);
    }
}
