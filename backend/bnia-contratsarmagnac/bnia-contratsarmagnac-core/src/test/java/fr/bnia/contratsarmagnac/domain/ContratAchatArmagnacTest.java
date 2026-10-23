package fr.bnia.contratsarmagnac.domain;

import static fr.bnia.contratsarmagnac.domain.Exemples.ACHETEUR;
import static fr.bnia.contratsarmagnac.domain.Exemples.AUJOURDHUI;
import static fr.bnia.contratsarmagnac.domain.Exemples.assemblage;
import static fr.bnia.contratsarmagnac.domain.Exemples.contenu;
import static fr.bnia.contratsarmagnac.domain.Exemples.henri;
import static fr.bnia.contratsarmagnac.domain.Exemples.jeanne;
import static fr.bnia.contratsarmagnac.domain.Exemples.millesime;
import static fr.bnia.contratsarmagnac.domain.Exemples.projetDeJeanne;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContratAchatArmagnacTest {

    @Test
    @DisplayName("RG-CA-06 : ouvrir crée le projet et sa version 1, approuvée par son auteur ; l'autre partie doit approuver")
    void rg_ca_06_ouverture() {
        ContratAchatArmagnac contrat = projetDeJeanne();

        assertThat(contrat.etat()).isEqualTo(EtatContrat.PROJET);
        assertThat(contrat.ouvertPar()).isEqualTo(Partie.VENDEUR);
        assertThat(contrat.courante().numero()).isEqualTo(1);
        assertThat(contrat.approbationCourante(Partie.VENDEUR)).isPresent();
        assertThat(contrat.partieQuiDoitApprouver()).contains(Partie.ACHETEUR);
        assertThat(contrat.dateContrat()).isEmpty();
        assertThat(contrat.campagne(AUJOURDHUI)).isEqualTo("2026-2027");
        assertThat(contrat.releverFaits()).extracting(Fait::action).containsExactly(ActionContrat.OUVERTURE);
        assertThat(contrat.releverFaits()).isEmpty();
    }

    @Test
    @DisplayName("RG-CA-02 : l'acheteur et le vendeur sont deux opérateurs distincts")
    void rg_ca_02_parties_distinctes() {
        assertThatThrownBy(() -> ContratAchatArmagnac.ouvrir("CA-2026-0002", ACHETEUR, ACHETEUR, henri(),
                contenu(millesime(2015, "1", "40", "1000")))).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-CA-02");
    }

    @Test
    @DisplayName("RG-CA-07 : en projet, une partie modifie : nouvelle version approuvée par elle seule, l'approbation de l'autre tombe")
    void rg_ca_07_modification() {
        ContratAchatArmagnac contrat = projetDeJeanne();
        contrat.releverFaits();

        contrat.modifier(henri(), 1, contenu(millesime(2015, "15", "47", "1700")));

        assertThat(contrat.courante().numero()).isEqualTo(2);
        assertThat(contrat.courante().auteur().partie()).isEqualTo(Partie.ACHETEUR);
        assertThat(contrat.approbationCourante(Partie.ACHETEUR)).isPresent();
        assertThat(contrat.approbationCourante(Partie.VENDEUR)).isEmpty();
        assertThat(contrat.partieQuiDoitApprouver()).contains(Partie.VENDEUR);
        assertThat(contrat.releverFaits()).singleElement().satisfies(f -> {
            assertThat(f.action()).isEqualTo(ActionContrat.MODIFICATION);
            assertThat(f.detail()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("RG-CA-07 : une version périmée ne se modifie ni ne s'approuve")
    void rg_ca_07_version_perimee() {
        ContratAchatArmagnac contrat = projetDeJeanne();
        contrat.modifier(henri(), 1, contenu(millesime(2015, "15", "47", "1700")));

        assertThatThrownBy(() -> contrat.approuver(jeanne(), 1)).isInstanceOf(ModificationConcurrente.class)
                .hasMessageContaining("rechargez");
        assertThatThrownBy(() -> contrat.modifier(jeanne(), 1, contenu(millesime(2015, "1", "47", "1700"))))
                .isInstanceOf(ModificationConcurrente.class);
    }

    @Test
    @DisplayName("RG-CA-08 : les deux approbations sur la même version concluent le contrat, daté du jour ; sa campagne est fixée")
    void rg_ca_08_conclusion() {
        ContratAchatArmagnac contrat = projetDeJeanne();
        contrat.releverFaits();

        assertThatThrownBy(() -> contrat.approuver(jeanne(), 1)).isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("déjà approuvé");
        contrat.approuver(henri(), 1);

        assertThat(contrat.etat()).isEqualTo(EtatContrat.CONCLU);
        assertThat(contrat.dateContrat()).contains(AUJOURDHUI);
        assertThat(contrat.campagne(LocalDate.of(2027, 5, 1))).isEqualTo("2026-2027");
        assertThat(contrat.partieQuiDoitApprouver()).isEmpty();
        assertThat(contrat.releverFaits()).extracting(Fait::action)
                .containsExactly(ActionContrat.APPROBATION, ActionContrat.CONCLUSION);
        assertThatThrownBy(() -> contrat.modifier(henri(), 1, contenu(millesime(2015, "1", "47", "1700"))))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("conclu");
    }

    @Test
    @DisplayName("RG-CA-09 : l'écart montré à la partie qui doit approuver, depuis la dernière version qu'elle a approuvée")
    void rg_ca_09_ecart() {
        ContratAchatArmagnac contrat = projetDeJeanne();
        contrat.modifier(henri(), 1, new ContenuContrat(List.of(millesime(2015, "15", "47", "1700"),
                assemblage("Hors d'âge", "20+", "3", "45", "2400")), "Fût neuf", null, null,
                new Paiement(Solde.TRENTE_JOURS_FIN_DE_MOIS, null)));

        Ecart ecart = contrat.ecartPour(Partie.VENDEUR);

        assertThat(ecart.depuis()).isEqualTo(1);
        assertThat(ecart.jusqua()).isEqualTo(2);
        assertThat(ecart.auteurs()).extracting(Auteur::nom).containsExactly("Henri Castex");
        assertThat(ecart.champs()).containsExactly(new Ecart.Changement("caracteristiques", null, "Fût neuf"),
                new Ecart.Changement("solde", "COMPTANT", "TRENTE_JOURS_FIN_DE_MOIS"));
        assertThat(ecart.lignes()).hasSize(2);
        assertThat(ecart.lignes().get(0)).isEqualTo(new Ecart.EcartLigne(0, Ecart.Nature.MODIFIEE,
                List.of(new Ecart.Changement("prix", "1750.00", "1700.00"))));
        assertThat(ecart.lignes().get(1).nature()).isEqualTo(Ecart.Nature.AJOUTEE);

        contrat.modifier(jeanne(), 2, contenu(millesime(2015, "15", "47", "1720")));
        Ecart pourHenri = contrat.ecartPour(Partie.ACHETEUR);
        assertThat(pourHenri.depuis()).as("sa propre version 2").isEqualTo(2);
        assertThat(pourHenri.lignes()).extracting(Ecart.EcartLigne::nature)
                .containsExactly(Ecart.Nature.MODIFIEE, Ecart.Nature.RETIREE);
        assertThat(projetDeJeanne().ecartPour(Partie.ACHETEUR).vide()).as("rien approuvé : depuis la version 1")
                .isTrue();
    }

    @Test
    @DisplayName("RG-CA-10 : la partie qui doit approuver refuse, définitivement ; l'auteur de la version ne refuse pas")
    void rg_ca_10_refus() {
        ContratAchatArmagnac contrat = projetDeJeanne();
        contrat.releverFaits();

        assertThatThrownBy(() -> contrat.refuser(jeanne(), 1)).isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("modifiez-la");
        contrat.refuser(henri(), 1);

        assertThat(contrat.etat()).isEqualTo(EtatContrat.REFUSE);
        assertThat(contrat.releverFaits()).extracting(Fait::action).containsExactly(ActionContrat.REFUS);
        assertThatThrownBy(() -> contrat.approuver(henri(), 1)).hasMessageContaining("refusé");
    }

    @Test
    @DisplayName("RG-CA-12 : le stock estimé du vendeur couvre chaque grain, lignes du même grain cumulées")
    void rg_ca_12_controle_stock() {
        var stock = StocksExemples.durand();
        ControleStock.verifier(contenu(millesime(2015, "15", "47", "1750")), stock);

        assertThatThrownBy(() -> ControleStock.verifier(contenu(millesime(2015, "30", "47", "1750")), stock))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessageContaining("millésime 2015").hasMessageContaining("14,10 hl AP au contrat")
                .hasMessageContaining("8,17 hl AP en stock")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-CA-12");
        assertThatThrownBy(() -> ControleStock.verifier(contenu(millesime(2015, "10", "47", "1750"),
                millesime(2015, "10", "47", "1800")), stock)).as("9,40 hl AP cumulés").isInstanceOf(RegleEnfreinte.class);
        ControleStock.verifier(contenu(assemblage("hors d'age", "20+", "3", "45", "2400")), stock);
        assertThatThrownBy(() -> ControleStock.verifier(contenu(millesime(2012, "1", "40", "1000")), stock))
                .as("le 2012 n'est que sous verre").hasMessageContaining("0,00 hl AP en stock");
    }
}
