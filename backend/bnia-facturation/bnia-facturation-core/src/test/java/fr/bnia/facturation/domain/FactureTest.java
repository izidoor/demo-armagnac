package fr.bnia.facturation.domain;

import static fr.bnia.facturation.domain.Exemples.EXONEREE;
import static fr.bnia.facturation.domain.Exemples.TVA_20;
import static fr.bnia.facturation.domain.Exemples.article;
import static fr.bnia.facturation.domain.Exemples.brouillon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import fr.bnia.facturation.domain.LigneFacture.LigneInformative;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FactureTest {

    @Test
    @DisplayName("RG-FAC-04 : un groupe a un titre et ne contient pas d'autre groupe ; son sous-total est la somme de ses HT")
    void rg_fac_04_groupes() {
        LigneGroupe groupe = new LigneGroupe("Commerce", List.of(article("A", "2", "10", TVA_20),
                new LigneInformative("Volumes de juillet"), article("B", "1", "5.5", TVA_20)));

        assertThat(groupe.sousTotalHt()).isEqualByComparingTo("25.50");
        assertThatThrownBy(() -> new LigneGroupe("Externe", List.of(groupe)))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-04"));
        assertThatThrownBy(() -> new LigneGroupe(" ", List.of()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-04"));
    }

    @Test
    @DisplayName("RG-FAC-05 : la facture d'exemple, HT par ligne au centime, TVA par catégorie et taux : 1 855,95 + 296,95 = 2 152,90")
    void rg_fac_05_facture_d_exemple() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());

        assertThat(facture.lignesArticle()).extracting(LigneArticle::montantHt)
                .containsExactly(new BigDecimal("402.77"), new BigDecimal("100.69"), new BigDecimal("1081.99"),
                        new BigDecimal("270.50"));
        assertThat(facture.recapitulatifTva()).extracting(LigneTva::base, LigneTva::tva)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(new BigDecimal("1484.76"), new BigDecimal("296.95")),
                        org.assertj.core.groups.Tuple.tuple(new BigDecimal("371.19"), new BigDecimal("0.00")));
        assertThat(facture.totaux()).isEqualTo(new Totaux(new BigDecimal("1855.95"), new BigDecimal("296.95"),
                new BigDecimal("2152.90")));
        assertThat(((LigneGroupe) facture.lignes().getFirst()).sousTotalHt()).isEqualByComparingTo("1855.95");
    }

    @Test
    @DisplayName("RG-FAC-05 : la TVA se calcule sur la base de la catégorie, pas ligne par ligne")
    void rg_fac_05_tva_par_categorie() {
        // Ligne par ligne : 0,33 × 3 = 0,99 ; sur la base : 4,98 × 20 % = 1,00.
        Facture facture = brouillon(List.of(article("A", "1", "1.66", TVA_20), article("B", "1", "1.66", TVA_20),
                article("C", "1", "1.66", TVA_20)));

        assertThat(facture.totaux().tva()).isEqualByComparingTo("1.00");
    }

    @Test
    @DisplayName("RG-FAC-06 : quantité jamais nulle ; une remise négative est admise, mais pas un TTC négatif")
    void rg_fac_06_quantite_et_ttc() {
        assertThatThrownBy(() -> article("A", "0", "10", TVA_20))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-06"));
        Facture remise = brouillon(List.of(article("A", "1", "100", TVA_20), article("R", "-1", "10", TVA_20)));
        assertThat(remise.totaux().ttc()).isEqualByComparingTo("108.00");
        assertThat(remise.empechements(Optional.of(Exemples.vendeur()))).isEmpty();

        Facture negative = brouillon(List.of(article("R", "-1", "10", TVA_20)));
        assertThat(negative.empechements(Optional.of(Exemples.vendeur()))).containsExactly("total TTC négatif");
    }

    @Test
    @DisplayName("RG-FAC-07 : une pièce qui a une ligne hors champ n'a que des lignes hors champ")
    void rg_fac_07_hors_champ_seul() {
        CategorieTva horsChamp = new CategorieTva(CategorieTva.Code.O, null, "Hors champ");

        assertThat(brouillon(List.of(article("A", "1", "10", horsChamp), article("B", "1", "10", EXONEREE)))
                .empechements(Optional.of(Exemples.vendeur())))
                .containsExactly("une ligne hors champ de la TVA ne se mêle à aucune autre catégorie");
        assertThat(brouillon(List.of(article("A", "1", "10", horsChamp))).empechements(Optional.of(Exemples.vendeur())))
                .isEmpty();
    }

    @Test
    @DisplayName("RG-FAC-08 : biens, services, ou les deux")
    void rg_fac_08_nature_de_l_operation() {
        LigneArticle bien = new LigneArticle(new ArticleRef("boutique", "PLV"), "Présentoir", "u", NatureArticle.BIEN,
                null, BigDecimal.ONE, BigDecimal.TEN, TVA_20);

        assertThat(brouillon(Exemples.lignesDeLExemple()).natureOperation()).contains(NatureOperation.SERVICES);
        assertThat(brouillon(List.of(bien)).natureOperation()).contains(NatureOperation.BIENS);
        assertThat(brouillon(List.of(bien, article("A", "1", "1", TVA_20))).natureOperation())
                .contains(NatureOperation.LES_DEUX);
        assertThat(brouillon(List.of(new LigneInformative("Rien"))).natureOperation()).isEmpty();
    }

    @Test
    @DisplayName("RG-FAC-09 : refus nommé, dans l'ordre : ligne article, acheteur, vendeur")
    void rg_fac_09_empechements() {
        Facture vide = brouillon(List.of(new LigneInformative("Rien à facturer")));

        assertThat(vide.empechements(Optional.empty())).containsExactly("aucune ligne article",
                "vendeur non paramétré (Paramétrage > Facturation)");
        assertThatThrownBy(() -> vide.exigerEmettable(Optional.empty()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> {
                    assertThat(e.regle()).contains("RG-FAC-09");
                    assertThat(e).hasMessage("Le brouillon ne peut pas être émis : aucune ligne article ; vendeur non "
                            + "paramétré (Paramétrage > Facturation).");
                });
    }

    @Test
    @DisplayName("RG-FAC-09, 12, 13 : l'émission attribue le numéro, la date, l'échéance et recopie le vendeur")
    void rg_fac_09_emission() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());

        facture.emettre(new Numerotation(2026, 700), LocalDate.of(2026, 10, 7), Exemples.vendeur());

        assertThat(facture.etat()).isEqualTo(EtatPiece.EMISE);
        assertThat(facture.numero()).contains("2026000700");
        assertThat(facture.echeance()).contains(LocalDate.of(2026, 11, 30));
        assertThat(facture.vendeur()).contains(Exemples.vendeur());
    }

    @Test
    @DisplayName("RG-FAC-11 : l'acheteur d'un client est remplacé par celui relu à l'émission")
    void rg_fac_11_acheteur_relu() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());
        Acheteur a = Exemples.acheteur();
        Acheteur relu = new Acheteur(a.client(), a.categorie(), "Armagnacs de Gascogne SAS", a.siren(), a.siret(),
                a.numeroTva(), a.adresseFacturation(), null, a.email());

        facture.relireAcheteur(relu);

        assertThat(facture.acheteur().nom()).isEqualTo("Armagnacs de Gascogne SAS");
    }

    @Test
    @DisplayName("RG-FAC-14 : une pièce émise ne s'émet ni ne se modifie plus")
    void rg_fac_14_emise_figee() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());
        facture.emettre(new Numerotation(2026, 1), LocalDate.of(2026, 10, 7), Exemples.vendeur());

        assertThatThrownBy(() -> facture.emettre(new Numerotation(2026, 2), LocalDate.of(2026, 10, 7),
                Exemples.vendeur()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-14"));
        assertThatThrownBy(() -> facture.relireAcheteur(Exemples.acheteur()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-14"));
        assertThatThrownBy(() -> facture.modifier(enTete("Rectifiée"), facture.lignes(), facture.version()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-14"));
        assertThatThrownBy(facture::exigerAbandonnable)
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.regle()).contains("RG-FAC-14"));
    }

    private static Facture.EnTete enTete(String periode) {
        return new Facture.EnTete(periode, ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null, null, null);
    }

    /** Le groupe reçu de l'exemple (n° 1) et ses quatre lignes article (n° 2 à 5). */
    private static LigneGroupe groupeRecu(Facture facture) {
        return (LigneGroupe) facture.lignes().getFirst();
    }

    @Test
    @DisplayName("RG-FAC-17 : les lignes d'une demande sont numérotées dans l'ordre d'impression et marquées reçues")
    void rg_fac_17_lignes_recues_numerotees() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());

        LigneGroupe groupe = groupeRecu(facture);
        assertThat(groupe.numero()).isEqualTo(1);
        assertThat(groupe.recue()).isTrue();
        assertThat(groupe.lignes()).extracting(LigneFacture::numero).containsExactly(2, 3, 4, 5);
        assertThat(groupe.lignes()).allMatch(LigneFacture::recue);
    }

    @Test
    @DisplayName("RG-FAC-17 : en-tête modifié ; texte ajouté ; ligne reçue renommée et déplacée dans son groupe")
    void rg_fac_17_modification_admise() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());
        LigneGroupe groupe = groupeRecu(facture);
        List<LigneFacture> reordonnees = new ArrayList<>(groupe.lignes());
        LigneArticle premiere = (LigneArticle) reordonnees.removeFirst();
        reordonnees.add(premiere.saisie("Sorties France, part assujettie", "hl", new BigDecimal("99"),
                new BigDecimal("1")));

        facture.modifier(new Facture.EnTete("DSM juillet 2026, rectifiée", new ConditionPaiement(30, false), "BC-12",
                "Voir contrôle", null), List.of(new LigneInformative("Régularisation suite au contrôle du 02/10"),
                groupe.avecLignes(reordonnees)), facture.version());

        assertThat(facture.libellePeriode()).isEqualTo("DSM juillet 2026, rectifiée");
        assertThat(facture.condition()).isEqualTo(new ConditionPaiement(30, false));
        assertThat(facture.bonDeCommande()).isEqualTo("BC-12");
        LigneFacture texte = facture.lignes().getFirst();
        assertThat(texte.numero()).isEqualTo(6);
        assertThat(texte.recue()).isFalse();
        LigneArticle renommee = (LigneArticle) ((LigneGroupe) facture.lignes().getLast()).lignes().getLast();
        // Ligne reçue : seule la désignation change ; quantité, unité et prix restent ceux de la source.
        assertThat(renommee.numero()).isEqualTo(2);
        assertThat(renommee.designation()).isEqualTo("Sorties France, part assujettie");
        assertThat(renommee.quantite()).isEqualByComparingTo("8.3911");
        assertThat(renommee.unite()).isEqualTo("hlAP");
        assertThat(facture.totaux().ttc()).isEqualByComparingTo("2152.90");
    }

    @Test
    @DisplayName("RG-FAC-17 : une ligne reçue ne se retire pas, ne change ni de quantité ni de groupe")
    void rg_fac_17_ligne_recue_verrouillee() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());
        LigneGroupe groupe = groupeRecu(facture);
        LigneArticle recue = (LigneArticle) groupe.lignes().getFirst();
        List<LigneFacture> sansLaPremiere = groupe.lignes().subList(1, 4);

        assertThatThrownBy(() -> facture.modifier(enTete(null), List.of(groupe.avecLignes(sansLaPremiere)),
                facture.version()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> {
                    assertThat(e.regle()).contains("RG-FAC-17");
                    assertThat(e.getMessage()).contains("ne se retire pas");
                });
        LigneArticle autreQuantite = new LigneArticle(recue.article(), recue.designation(), recue.unite(),
                recue.nature(), recue.codeComptable(), new BigDecimal("1"), recue.prixUnitaire(), recue.tva(),
                recue.numero(), true);
        List<LigneFacture> modifiees = new ArrayList<>(groupe.lignes());
        modifiees.set(0, autreQuantite);
        assertThatThrownBy(() -> facture.modifier(enTete(null), List.of(groupe.avecLignes(modifiees)),
                facture.version()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.getMessage()).contains("quantité"));
        assertThatThrownBy(() -> facture.modifier(enTete(null), List.of(recue, groupe.avecLignes(sansLaPremiere)),
                facture.version()))
                .isInstanceOfSatisfying(RegleEnfreinte.class, e -> assertThat(e.getMessage()).contains("son groupe"));
    }

    @Test
    @DisplayName("RG-FAC-17 : une ligne ajoutée se modifie et se retire librement")
    void rg_fac_17_ligne_ajoutee_libre() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());
        LigneGroupe groupe = groupeRecu(facture);
        facture.modifier(enTete(null), List.of(groupe, article("FRAIS", "1", "10", TVA_20)), facture.version());
        LigneArticle ajoutee = (LigneArticle) facture.lignes().getLast();
        assertThat(ajoutee.numero()).isEqualTo(6);
        assertThat(ajoutee.recue()).isFalse();

        facture.modifier(enTete(null), List.of(groupe, ajoutee.saisie("Frais", "u", new BigDecimal("2"),
                new BigDecimal("15"))), facture.version());
        assertThat(((LigneArticle) facture.lignes().getLast()).montantHt()).isEqualByComparingTo("30.00");

        facture.modifier(enTete(null), List.of(groupe), facture.version());
        assertThat(facture.lignes()).hasSize(1);
    }

    @Test
    @DisplayName("RG-FAC-17 : une saisie faite sur une version périmée est refusée")
    void rg_fac_17_version_perimee() {
        Facture facture = brouillon(Exemples.lignesDeLExemple());

        assertThatThrownBy(() -> facture.modifier(enTete(null), facture.lignes(), facture.version() + 1))
                .isInstanceOf(ModificationConcurrente.class);
    }
}
