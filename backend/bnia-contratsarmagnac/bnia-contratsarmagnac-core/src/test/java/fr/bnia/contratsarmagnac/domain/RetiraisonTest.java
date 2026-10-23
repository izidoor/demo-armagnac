package fr.bnia.contratsarmagnac.domain;

import static fr.bnia.contratsarmagnac.domain.Exemples.AUJOURDHUI;
import static fr.bnia.contratsarmagnac.domain.Exemples.assemblage;
import static fr.bnia.contratsarmagnac.domain.Exemples.contenu;
import static fr.bnia.contratsarmagnac.domain.Exemples.henri;
import static fr.bnia.contratsarmagnac.domain.Exemples.jeanne;
import static fr.bnia.contratsarmagnac.domain.Exemples.millesime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Retiraisons, annulation et clôture d'un contrat conclu (spec 06 RG-CA-05, 13 à 17). */
class RetiraisonTest {

    private static final LocalDate LE_30 = LocalDate.of(2026, 10, 30);

    /** Conclu le 23 octobre 2026 : 15 hl de 2015 à 47 % (ligne 0), 3 hl de Hors d'âge 20+ à 45 % (ligne 1). */
    private static ContratAchatArmagnac conclu() {
        ContratAchatArmagnac contrat = ContratAchatArmagnac.ouvrir("CA-2026-0001", Exemples.ACHETEUR,
                Exemples.VENDEUR, jeanne(), contenu(millesime(2015, "15", "47", "1750"),
                        assemblage("Hors d'âge", "20+", "3", "45", "2400")));
        contrat.approuver(henri(), 1);
        contrat.releverFaits();
        return ContratAchatArmagnac.reconstituer(1, contrat.numero(), contrat.acheteur(), contrat.vendeur(),
                contrat.ouvertPar(), contrat.etat(), contrat.dateContrat().orElseThrow(), contrat.versions(),
                contrat.approbations(), List.of(), 0, 0);
    }

    private static ContenuRetiraison retiraison(LocalDate date, boolean derniere, String volume0, String volume1) {
        List<LigneRetiraison> lignes = new java.util.ArrayList<>();
        if (volume0 != null) {
            lignes.add(new LigneRetiraison(0, new BigDecimal(volume0)));
        }
        if (volume1 != null) {
            lignes.add(new LigneRetiraison(1, new BigDecimal(volume1)));
        }
        return new ContenuRetiraison(date, derniere, lignes);
    }

    private static String regle(Runnable action) {
        try {
            action.run();
        } catch (RegleEnfreinte e) {
            return e.regle().orElseThrow() + " : " + e.getMessage();
        }
        throw new AssertionError("règle non enfreinte");
    }

    @Test
    @DisplayName("RG-CA-13 : sur un contrat conclu, l'acheteur déclare une retiraison approuvée par lui, à valider par le vendeur")
    void rg_ca_13_declaration() {
        ContratAchatArmagnac contrat = conclu();

        Retiraison r = contrat.declarerRetiraison(henri(), retiraison(LE_30, false, "10", null), AUJOURDHUI);

        assertThat(r.numero()).isEqualTo(1);
        assertThat(r.etat()).isEqualTo(EtatRetiraison.A_VALIDER_VENDEUR);
        assertThat(r.partieQuiDoitValider()).contains(Partie.VENDEUR);
        assertThat(r.approbations()).extracting(Approbation::partie).containsExactly(Partie.ACHETEUR);
        assertThat(contrat.resteARetirer(0, null)).isEqualByComparingTo("5");
        assertThat(contrat.resteARetirer(0, 1)).isEqualByComparingTo("15");
        assertThat(contrat.releverFaits()).singleElement().satisfies(f -> {
            assertThat(f.action()).isEqualTo(ActionContrat.RETIRAISON_DECLAREE);
            assertThat(f.detail()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("RG-CA-13 : refus : contrat non conclu, vendeur, date passée, au-delà du reste à retirer, ligne inconnue, aucune ligne, volume nul")
    void rg_ca_13_refus() {
        ContratAchatArmagnac projet = Exemples.projetDeJeanne();
        assertThat(regle(() -> projet.declarerRetiraison(henri(), retiraison(LE_30, false, "1", null), AUJOURDHUI)))
                .isEqualTo("RG-CA-13 : Le contrat CA-2026-0001 n'est pas encore conclu : il ne peut pas recevoir de retiraison.");
        ContratAchatArmagnac contrat = conclu();
        assertThat(regle(() -> contrat.declarerRetiraison(jeanne(), retiraison(LE_30, false, "1", null), AUJOURDHUI)))
                .isEqualTo("RG-CA-13 : Seul l'acheteur déclare une retiraison.");
        assertThat(regle(() -> contrat.declarerRetiraison(henri(), retiraison(AUJOURDHUI.minusDays(1), false, "1",
                null), AUJOURDHUI))).isEqualTo("RG-CA-13 : La date d'enlèvement est au plus tôt aujourd'hui.");
        contrat.declarerRetiraison(henri(), retiraison(AUJOURDHUI, false, "10", null), AUJOURDHUI);
        assertThat(regle(() -> contrat.declarerRetiraison(henri(), retiraison(LE_30, false, "6", null), AUJOURDHUI)))
                .isEqualTo("RG-CA-13 : Ligne « millésime 2015 » : 6,00 hl demandés, il en reste 5,00 hl à retirer.");
        assertThat(regle(() -> contrat.declarerRetiraison(henri(), new ContenuRetiraison(LE_30, false,
                List.of(new LigneRetiraison(2, BigDecimal.ONE))), AUJOURDHUI)))
                .isEqualTo("RG-CA-13 : La ligne n° 3 n'est pas une ligne du contrat CA-2026-0001.");
        assertThat(regle(() -> new ContenuRetiraison(LE_30, false, List.of())))
                .isEqualTo("RG-CA-13 : Une retiraison porte au moins une ligne du contrat.");
        assertThat(regle(() -> new LigneRetiraison(0, BigDecimal.ZERO)))
                .isEqualTo("RG-CA-13 : Le volume retiré d'une ligne est strictement positif.");
    }

    @Test
    @DisplayName("RG-CA-14 : la partie attendue modifie : nouvelle version approuvée par elle, à valider par l'autre ; l'écart est montré")
    void rg_ca_14_modification() {
        ContratAchatArmagnac contrat = conclu();
        contrat.declarerRetiraison(henri(), retiraison(LE_30, false, "10", "1"), AUJOURDHUI);
        assertThat(regle(() -> contrat.modifierRetiraison(henri(), 1, 1, retiraison(LE_30, false, "9", null),
                AUJOURDHUI))).isEqualTo("RG-CA-14 : La retiraison n° 1 attend l'autre partie : vous ne pouvez pas la modifier.");
        assertThatThrownBy(() -> contrat.modifierRetiraison(jeanne(), 1, 2, retiraison(LE_30, false, "9", null),
                AUJOURDHUI)).isInstanceOf(ModificationConcurrente.class);

        contrat.modifierRetiraison(jeanne(), 1, 1, retiraison(LE_30.plusDays(1), true, "15", null), AUJOURDHUI);

        Retiraison r = contrat.retiraison(1);
        assertThat(r.courante().numero()).isEqualTo(2);
        assertThat(r.etat()).isEqualTo(EtatRetiraison.A_VALIDER_ACHETEUR);
        Ecart ecart = r.ecartPour(Partie.ACHETEUR);
        assertThat(ecart.depuis()).isEqualTo(1);
        assertThat(ecart.auteurs()).extracting(Auteur::nom).containsExactly("Jeanne Durand");
        assertThat(ecart.champs()).extracting(Ecart.Changement::champ, Ecart.Changement::avant, Ecart.Changement::apres)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("dateEnlevement", "2026-10-30", "2026-10-31"),
                        org.assertj.core.groups.Tuple.tuple("derniere", "false", "true"));
        assertThat(ecart.lignes()).extracting(Ecart.EcartLigne::rang, Ecart.EcartLigne::nature).containsExactly(
                org.assertj.core.groups.Tuple.tuple(0, Ecart.Nature.MODIFIEE),
                org.assertj.core.groups.Tuple.tuple(1, Ecart.Nature.RETIREE));
        assertThat(contrat.releverFaits()).extracting(Fait::action)
                .containsExactly(ActionContrat.RETIRAISON_DECLAREE, ActionContrat.RETIRAISON_MODIFIEE);
    }

    @Test
    @DisplayName("RG-CA-15 : la partie attendue valide, au plus tard le jour de l'enlèvement ; la dernière exécute le contrat et fait tomber les autres")
    void rg_ca_15_validation() {
        ContratAchatArmagnac contrat = conclu();
        contrat.declarerRetiraison(henri(), retiraison(LE_30, false, "5", null), AUJOURDHUI);
        contrat.declarerRetiraison(henri(), retiraison(LE_30, true, "10", "3"), AUJOURDHUI);
        contrat.releverFaits();
        assertThat(regle(() -> contrat.validerRetiraison(jeanne(), 1, 1, LE_30.plusDays(1)))).isEqualTo(
                "RG-CA-15 : La retiraison n° 1 était prévue le 30/10/2026 : elle se valide au plus tard le jour de"
                        + " l'enlèvement. Modifiez sa date.");

        contrat.validerRetiraison(jeanne(), 2, 1, LE_30);

        assertThat(contrat.etat()).isEqualTo(EtatContrat.EXECUTE);
        assertThat(contrat.retiraisons()).singleElement().satisfies(r -> {
            assertThat(r.numero()).isEqualTo(2);
            assertThat(r.validee()).isTrue();
            assertThat(r.partieQuiDoitValider()).isEmpty();
        });
        assertThat(contrat.releverFaits()).extracting(Fait::action)
                .containsExactly(ActionContrat.RETIRAISON_VALIDEE, ActionContrat.EXECUTION);
        assertThat(regle(() -> contrat.validerRetiraison(jeanne(), 2, 1, LE_30)))
                .startsWith("RG-CA-15 : Le contrat CA-2026-0001 est exécuté");
    }

    @Test
    @DisplayName("RG-CA-16 : l'acheteur retire une retiraison non validée ; une validée ne se retire plus")
    void rg_ca_16_retrait() {
        ContratAchatArmagnac contrat = conclu();
        contrat.declarerRetiraison(henri(), retiraison(LE_30, false, "5", null), AUJOURDHUI);
        contrat.declarerRetiraison(henri(), retiraison(LE_30, false, "5", null), AUJOURDHUI);
        contrat.validerRetiraison(jeanne(), 2, 1, AUJOURDHUI);
        assertThat(regle(() -> contrat.retirerRetiraison(jeanne(), 1))).isEqualTo(
                "RG-CA-16 : Seul l'acheteur retire une retiraison.");
        assertThat(regle(() -> contrat.retirerRetiraison(henri(), 2))).isEqualTo(
                "RG-CA-16 : La retiraison n° 2 est validée : elle ne se retire plus.");

        contrat.retirerRetiraison(henri(), 1);

        assertThat(contrat.retiraisons()).extracting(Retiraison::numero).containsExactly(2);
        assertThat(contrat.declarerRetiraison(henri(), retiraison(LE_30, false, "1", null), AUJOURDHUI).numero())
                .as("numéro jamais réattribué").isEqualTo(3);
    }

    @Test
    @DisplayName("RG-CA-17 : une partie annule ou clôt le contrat conclu ; les retiraisons non validées tombent, les validées restent")
    void rg_ca_17_annulation_et_cloture() {
        ContratAchatArmagnac annule = conclu();
        annule.declarerRetiraison(henri(), retiraison(LE_30, false, "5", null), AUJOURDHUI);
        annule.validerRetiraison(jeanne(), 1, 1, AUJOURDHUI);
        annule.declarerRetiraison(henri(), retiraison(LE_30, false, "5", null), AUJOURDHUI);
        annule.releverFaits();
        annule.annuler(jeanne());
        assertThat(annule.etat()).isEqualTo(EtatContrat.ANNULE);
        assertThat(annule.retiraisons()).extracting(Retiraison::numero).containsExactly(1);
        assertThat(annule.releverFaits()).extracting(Fait::action).containsExactly(ActionContrat.ANNULATION);
        assertThat(regle(() -> annule.cloturer(henri())))
                .isEqualTo("RG-CA-17 : Le contrat CA-2026-0001 est annulé : il ne peut plus être clos.");

        ContratAchatArmagnac clos = conclu();
        clos.cloturer(henri());
        assertThat(clos.etat()).isEqualTo(EtatContrat.EXECUTE);
        assertThat(clos.releverFaits()).extracting(Fait::action).containsExactly(ActionContrat.CLOTURE);
        assertThat(regle(() -> Exemples.projetDeJeanne().annuler(henri())))
                .isEqualTo("RG-CA-17 : Le contrat CA-2026-0001 n'est pas encore conclu : il ne peut pas être annulé.");
    }

    @Test
    @DisplayName("RG-CA-05 : une retiraison après un 1er avril franchi vise le compte vieilli d'un assemblage (H-CA-11) ; un millésime ne change pas")
    void rg_ca_05_compte_vieilli() {
        LigneContrat xo = assemblage("XO", "10", "2", "40", "2000");
        LocalDate conclu = LocalDate.of(2026, 3, 15);
        assertThat(xo.grainAu(conclu, LocalDate.of(2026, 3, 31)).compte()).isEqualTo(CompteDAgeDsm.de("10"));
        assertThat(xo.grainAu(conclu, LocalDate.of(2026, 4, 1)).compte()).isEqualTo(CompteDAgeDsm.de("11"));
        assertThat(xo.grainAu(conclu, LocalDate.of(2028, 4, 1)).compte()).isEqualTo(CompteDAgeDsm.de("13"));
        assertThat(millesime(2015, "1", "40", "1000").grainAu(conclu, LocalDate.of(2027, 5, 1)))
                .isEqualTo(Grain.millesime(2015));

        ContratAchatArmagnac contrat = conclu();
        assertThat(contrat.volumeApParGrain(retiraison(LE_30, false, "10", "2"), LE_30)).containsExactly(
                java.util.Map.entry(Grain.millesime(2015), VolumeAlcoolPur.litres("470.00")),
                java.util.Map.entry(Grain.assemblage("Hors d'âge", CompteDAgeDsm.VINGT_ET_PLUS),
                        VolumeAlcoolPur.litres("90.00")));
    }
}
