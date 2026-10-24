package fr.bnia.contratsarmagnac.application;

import static fr.bnia.contratsarmagnac.application.Monde.AGENT_BNIA;
import static fr.bnia.contratsarmagnac.application.Monde.BNIA;
import static fr.bnia.contratsarmagnac.application.Monde.HENRI;
import static fr.bnia.contratsarmagnac.application.Monde.INACTIF;
import static fr.bnia.contratsarmagnac.application.Monde.INCONNU;
import static fr.bnia.contratsarmagnac.application.Monde.JEANNE;
import static fr.bnia.contratsarmagnac.application.Monde.NEGOCIANT;
import static fr.bnia.contratsarmagnac.application.Monde.OBSERVATEUR_PRODUCTEUR;
import static fr.bnia.contratsarmagnac.application.Monde.PRODUCTEUR;
import static fr.bnia.contratsarmagnac.application.Monde.TIERS;
import static fr.bnia.contratsarmagnac.application.Monde.millesime2015;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.contratsarmagnac.application.NegocierContrats.SaisieContrat;
import fr.bnia.contratsarmagnac.domain.ActionContrat;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.contratsarmagnac.domain.Solde;
import fr.bnia.contratsarmagnac.domain.StocksExemples;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Négociation et conclusion (UC-CA-1, 2) : le fil de la démonstration et ses refus. */
class NegocierContratsTest {

    private final Monde monde = new Monde();
    private final NegocierContrats cas = monde.negocier();

    private ContratAchatArmagnac projetDeJeanne(String volume) {
        return cas.ouvrir(JEANNE, PRODUCTEUR, new SaisieContrat(Partie.VENDEUR, NEGOCIANT, millesime2015(volume, "1750")));
    }

    private static String regle(Throwable e) {
        return ((RegleEnfreinte) e).regle().orElseThrow();
    }

    @Test
    @DisplayName("RG-CA-12 : le projet de Jeanne, 30 hl de 2015, dépasse son stock estimé ; ramené à 15 hl, il s'ouvre")
    void rg_ca_12_projet_bloque_puis_corrige() {
        assertThatThrownBy(() -> projetDeJeanne("30")).isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("14,10 hl AP au contrat").hasMessageContaining("8,17 hl AP en stock")
                .extracting(NegocierContratsTest::regle).isEqualTo("RG-CA-12");

        ContratAchatArmagnac contrat = projetDeJeanne("15");

        assertThat(contrat.id()).isNotNull();
        assertThat(contrat.numero()).isEqualTo("CA-2026-0001");
        assertThat(contrat.vendeur()).isEqualTo(PRODUCTEUR);
        assertThat(contrat.acheteur()).isEqualTo(NEGOCIANT);
        assertThat(contrat.courante().auteur().nom()).isEqualTo("Jeanne Durand");
    }

    @Test
    @DisplayName("RG-CA-12 : l'acheteur n'a aucun contrôle de stock ; le vendeur n'approuve pas ce que son stock ne couvre pas")
    void rg_ca_12_acheteur_et_approbation_du_vendeur() {
        ContratAchatArmagnac contrat = cas.ouvrir(HENRI, NEGOCIANT,
                new SaisieContrat(Partie.ACHETEUR, PRODUCTEUR, millesime2015("30", "1750")));
        assertThat(contrat.partieQuiDoitApprouver()).contains(Partie.VENDEUR);

        assertThatThrownBy(() -> cas.approuver(JEANNE, PRODUCTEUR, contrat.id(), 1))
                .extracting(NegocierContratsTest::regle).isEqualTo("RG-CA-12");
        assertThatThrownBy(() -> cas.modifier(JEANNE, PRODUCTEUR, contrat.id(), 1, millesime2015("20", "1750")))
                .extracting(NegocierContratsTest::regle).isEqualTo("RG-CA-12");
        assertThat(cas.modifier(JEANNE, PRODUCTEUR, contrat.id(), 1, millesime2015("15", "1750")).courante().numero())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("RG-CA-12 : le vendeur qui modifie un assemblage pris dans son stock ne dépasse pas ce stock, en alcool pur")
    void rg_ca_12_modification_d_un_assemblage() {
        long id = projetDeJeanne("15").id();

        assertThatThrownBy(() -> cas.modifier(JEANNE, PRODUCTEUR, id, 1, horsDAge("20", "40")))
                .hasMessageContaining("Hors d'âge, compte 20+").hasMessageContaining("8,00 hl AP au contrat")
                .hasMessageContaining("7,50 hl AP en stock").extracting(NegocierContratsTest::regle)
                .isEqualTo("RG-CA-12");
        assertThat(cas.modifier(JEANNE, PRODUCTEUR, id, 1, horsDAge("15", "50")).courante().numero()).isEqualTo(2);
    }

    /** Une ligne de Hors d'âge {@code 20+} (750 L AP au stock de Jeanne). */
    private static SaisieContenu horsDAge(String volume, String degre) {
        return new SaisieContenu(List.of(new SaisieContenu.SaisieLigne(Appellation.BAS_ARMAGNAC, null, "Hors d'âge",
                "20+", new BigDecimal(volume), new BigDecimal(degre), new BigDecimal("2400"))), null, null, null,
                Solde.COMPTANT, null);
    }

    @Test
    @DisplayName("RG-CA-07, 08 : Henri modifie le prix, Jeanne approuve : conclu, daté du jour")
    void rg_ca_08_fil_de_la_demonstration() {
        long id = projetDeJeanne("15").id();

        cas.modifier(HENRI, NEGOCIANT, id, 1, millesime2015("15", "1700"));
        ContratAchatArmagnac conclu = cas.approuver(JEANNE, PRODUCTEUR, id, 2);

        assertThat(conclu.etat()).isEqualTo(EtatContrat.CONCLU);
        assertThat(conclu.dateContrat()).contains(LocalDate.of(2026, 10, 23));
        assertThat(conclu.courante().contenu().montantHt()).isEqualByComparingTo("11985.00");
    }

    @Test
    @DisplayName("RG-CA-19 : le journal garde chaque action, acteur et nom copiés")
    void rg_ca_19_journal() {
        long id = projetDeJeanne("15").id();
        cas.modifier(HENRI, NEGOCIANT, id, 1, millesime2015("15", "1700"));
        cas.approuver(JEANNE, PRODUCTEUR, id, 2);

        assertThat(monde.journal.duContrat(id)).extracting(EntreeJournal::action, EntreeJournal::nom,
                EntreeJournal::detail).containsExactly(
                        tuple(ActionContrat.OUVERTURE, "Jeanne Durand", 1),
                        tuple(ActionContrat.MODIFICATION, "Henri Castex", 2),
                        tuple(ActionContrat.APPROBATION, "Jeanne Durand", 2),
                        tuple(ActionContrat.CONCLUSION, "Jeanne Durand", 2));
    }

    @Test
    @DisplayName("RG-CA-18 : un e-mail aux AGENT et ADMIN de l'autre partie à l'ouverture, à la modification, à la conclusion, au refus")
    void rg_ca_18_courriels() {
        long id = projetDeJeanne("15").id();
        assertThat(monde.courriels.a("admin@negociant.fr")).singleElement().satisfies(c -> {
            assertThat(c.sujet()).isEqualTo("Projet de contrat CA-2026-0001 à approuver");
            assertThat(c.texte()).contains("Jeanne Durand (Maison Durand)").contains("acheteur")
                    .contains("https://bnia.example/portail/contrats/" + id);
        });
        assertThat(monde.courriels.a("lecteur@negociant.fr")).as("observateur").isEmpty();
        assertThat(monde.courriels.a("contact@negociant.fr")).as("contact sans compte").isEmpty();

        cas.modifier(HENRI, NEGOCIANT, id, 1, millesime2015("15", "1700"));
        assertThat(monde.courriels.a("admin@producteur.fr")).singleElement()
                .satisfies(c -> assertThat(c.sujet()).isEqualTo("Projet de contrat CA-2026-0001 modifié"));
        cas.approuver(JEANNE, PRODUCTEUR, id, 2);
        assertThat(monde.courriels.a("admin@negociant.fr")).last()
                .satisfies(c -> assertThat(c.texte()).contains("conclu le 23 octobre 2026"));

        long refuse = projetDeJeanne("1").id();
        cas.refuser(HENRI, NEGOCIANT, refuse, 1);
        assertThat(monde.courriels.a("admin@producteur.fr")).last()
                .satisfies(c -> assertThat(c.sujet()).isEqualTo("Projet de contrat CA-2026-0002 refusé"));
    }

    @Test
    @DisplayName("RG-CA-18 : une notification push aux AGENT et ADMIN de l'autre partie, à l'ouverture, à la modification,"
            + " à la conclusion, au refus ; une phrase sans montant ni volume, et le lien du contrat")
    void rg_ca_18_notifications() {
        long id = projetDeJeanne("15").id();
        assertThat(monde.notifications.envoyees).singleElement().satisfies(n -> {
            assertThat(n.utilisateurs()).as("Henri ; ni l'observateur ni le contact").containsExactly(70L);
            assertThat(n.titre()).isEqualTo("Projet de contrat CA-2026-0001 à approuver");
            assertThat(n.texte()).isEqualTo(
                    "Jeanne Durand (Maison Durand) vous propose un projet de contrat ; il attend votre approbation.");
            assertThat(n.lien()).isEqualTo("/portail/contrats/" + id);
        });

        cas.modifier(HENRI, NEGOCIANT, id, 1, millesime2015("15", "1700"));
        assertThat(monde.notifications.a(80)).singleElement().satisfies(n -> {
            assertThat(n.titre()).isEqualTo("Projet de contrat CA-2026-0001 modifié");
            assertThat(n.texte()).doesNotContain("1700").doesNotContain("15");
        });
        cas.approuver(JEANNE, PRODUCTEUR, id, 2);
        assertThat(monde.notifications.a(70)).last().satisfies(n -> {
            assertThat(n.titre()).isEqualTo("Contrat CA-2026-0001 conclu");
            assertThat(n.texte()).isEqualTo("Jeanne Durand (Maison Durand) a approuvé le contrat : il est conclu.");
        });

        long refuse = projetDeJeanne("1").id();
        cas.refuser(HENRI, NEGOCIANT, refuse, 1);
        assertThat(monde.notifications.a(80)).last()
                .satisfies(n -> assertThat(n.titre()).isEqualTo("Projet de contrat CA-2026-0002 refusé"));
    }

    @Test
    @DisplayName("RG-CA-01 : le BNIA n'agit sur aucun contrat ; un observateur lit sans agir ; un tiers ne voit pas le contrat")
    void rg_ca_01_refus() {
        long id = projetDeJeanne("15").id();

        assertThatThrownBy(() -> cas.ouvrir(AGENT_BNIA, BNIA, new SaisieContrat(Partie.ACHETEUR, PRODUCTEUR,
                millesime2015("1", "1750")))).isInstanceOf(AccesRefuse.class).hasMessageContaining("BNIA");
        assertThatThrownBy(() -> cas.approuver(AGENT_BNIA, NEGOCIANT, id, 1)).isInstanceOf(AccesRefuse.class)
                .extracting(NegocierContratsTest::regle).isEqualTo("RG-CA-01");
        assertThatThrownBy(() -> cas.modifier(OBSERVATEUR_PRODUCTEUR, PRODUCTEUR, id, 1, millesime2015("1", "1750")))
                .isInstanceOf(AccesRefuse.class).hasMessageContaining("lire");
        assertThatThrownBy(() -> cas.refuser(INCONNU, NEGOCIANT, id, 1)).isInstanceOf(AccesRefuse.class);
        cas.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.ACHETEUR, TIERS, millesime2015("1", "1750")));
        assertThatThrownBy(() -> cas.approuver(JEANNE, PRODUCTEUR, 2, 1)).as("contrat d'Henri et des Landes")
                .isInstanceOf(ElementIntrouvable.class);
    }

    @Test
    @DisplayName("RG-CA-02 : l'autre partie est un autre opérateur, actif, qui n'est pas le BNIA ; aucun type n'est exclu")
    void rg_ca_02_autre_partie() {
        assertThatThrownBy(() -> cas.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.ACHETEUR, NEGOCIANT,
                millesime2015("1", "1750")))).extracting(NegocierContratsTest::regle).isEqualTo("RG-CA-02");
        assertThatThrownBy(() -> cas.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.ACHETEUR, BNIA,
                millesime2015("1", "1750")))).hasMessageContaining("BNIA");
        assertThatThrownBy(() -> cas.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.ACHETEUR, INACTIF,
                millesime2015("1", "1750")))).hasMessageContaining("plus actif");
        assertThatThrownBy(() -> cas.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.ACHETEUR, 999,
                millesime2015("1", "1750")))).hasMessageContaining("introuvable");
        assertThatThrownBy(() -> cas.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.VENDEUR, PRODUCTEUR,
                millesime2015("1", "1750")))).as("sans stock estimé").extracting(NegocierContratsTest::regle)
                .isEqualTo("RG-CA-12");
        when(monde.declarations.stockEstime(NEGOCIANT)).thenReturn(StocksExemples.durand());
        assertThat(cas.ouvrir(HENRI, NEGOCIANT, new SaisieContrat(Partie.VENDEUR, PRODUCTEUR,
                millesime2015("1", "1750"))).vendeur()).as("un négociant vend à un producteur").isEqualTo(NEGOCIANT);
    }
}
