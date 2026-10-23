package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.ColonneDsm.C06;
import static fr.bnia.declarations.domain.ColonneDsm.C12;
import static fr.bnia.declarations.domain.ColonneDsm.C17;
import static fr.bnia.declarations.domain.Exemples.BNIA;
import static fr.bnia.declarations.domain.Exemples.AUJOURDHUI;
import static fr.bnia.declarations.domain.Exemples.MAINTENANT;
import static fr.bnia.declarations.domain.Exemples.OPERATEUR;
import static fr.bnia.declarations.domain.Exemples.PERIODE_JUIN;
import static fr.bnia.declarations.domain.Exemples.dsmDeJuin;
import static fr.bnia.declarations.domain.Exemples.l;
import static fr.bnia.declarations.domain.Exemples.refuse;
import static fr.bnia.declarations.domain.Exemples.saisie;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.declarations.domain.partage.Cote;
import fr.bnia.declarations.domain.partage.EtatDsm;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CycleDsmTest {

    private static DeclarationStocksMouvements dans(EtatDsm etat) {
        return DeclarationStocksMouvements.reconstituer(50, OPERATEUR, TypeDeclarant.NEGOCIANT, PERIODE_JUIN, etat,
                null, dsmDeJuin().lignes(), null, null, null, 3);
    }

    /** Chaque transition, et les états d'où elle part (RG-DEC-20). */
    private static final Map<String, Consumer<DeclarationStocksMouvements>> TRANSITIONS = Map.of(
            "envoyer", d -> d.envoyer(MAINTENANT, AUJOURDHUI, null),
            "prendre en instruction", d -> d.prendreEnInstruction(null),
            "demander des compléments", d -> d.demanderComplements(null),
            "valider", d -> d.valider(BNIA, MAINTENANT, null, List.of()));

    private static final Map<String, Set<EtatDsm>> DEPARTS = Map.of(
            "envoyer", EnumSet.of(EtatDsm.BROUILLON, EtatDsm.COMPLEMENTS_DEMANDES),
            "prendre en instruction", EnumSet.of(EtatDsm.ENVOYEE),
            "demander des compléments", EnumSet.of(EtatDsm.EN_INSTRUCTION, EtatDsm.COMPLEMENTS_RECUS),
            "valider", EnumSet.of(EtatDsm.EN_INSTRUCTION, EtatDsm.COMPLEMENTS_RECUS));

    @Test
    @DisplayName("RG-DEC-20 : envoi, prise en instruction, compléments, renvoi (compléments reçus), validation directe")
    void rg_dec_20_cycle_complet() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        assertThat(dsm.envoyer(MAINTENANT, AUJOURDHUI, 0L)).isFalse();
        assertThat(dsm.etat()).isEqualTo(EtatDsm.ENVOYEE);
        assertThat(dsm.envoyeeLe()).isEqualTo(MAINTENANT);
        dsm.prendreEnInstruction(null);
        assertThat(dsm.etat()).isEqualTo(EtatDsm.EN_INSTRUCTION);
        dsm.demanderComplements(null);
        assertThat(dsm.etat()).isEqualTo(EtatDsm.COMPLEMENTS_DEMANDES);
        assertThat(dsm.envoyer(MAINTENANT.plusSeconds(60), AUJOURDHUI, null)).isTrue();
        assertThat(dsm.etat()).isEqualTo(EtatDsm.COMPLEMENTS_RECUS);
        assertThat(dsm.envoyeeLe()).isEqualTo(MAINTENANT.plusSeconds(60));
        refuse("RG-DEC-20", () -> dsm.prendreEnInstruction(null));
        dsm.valider(BNIA, MAINTENANT.plusSeconds(120), null, List.of());
        assertThat(dsm.etat()).isEqualTo(EtatDsm.VALIDEE);
        assertThat(dsm.valideePar()).isEqualTo(BNIA);
        assertThat(dsm.valideeLe()).isEqualTo(MAINTENANT.plusSeconds(120));
    }

    @ParameterizedTest
    @EnumSource(EtatDsm.class)
    @DisplayName("RG-DEC-20 : chaque transition part de ses seuls états ; aucune ne sort de VALIDEE")
    void rg_dec_20_transitions_permises_et_refusees(EtatDsm etat) {
        TRANSITIONS.forEach((nom, transition) -> {
            DeclarationStocksMouvements dsm = dans(etat);
            if (DEPARTS.get(nom).contains(etat)) {
                transition.accept(dsm);
                assertThat(dsm.etat()).as(nom).isNotEqualTo(etat);
            } else {
                refuse("RG-DEC-20", () -> transition.accept(dsm));
                assertThat(dsm.etat()).as(nom).isEqualTo(etat);
            }
        });
    }

    @Test
    @DisplayName("RG-DEC-20 : une anomalie de calcul bloque l'envoi, pas l'enregistrement")
    void rg_dec_20_anomalie_bloque_envoi() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        dsm.enregistrer(saisie(dsm, Map.of(0, Map.of(C06, l("40")), 1, Map.of(C12, l("50")))), null);
        assertThatThrownBy(() -> dsm.envoyer(MAINTENANT, AUJOURDHUI, null)).hasMessageContaining("1 anomalie");
        refuse("RG-DEC-20", () -> dsm.envoyer(MAINTENANT, AUJOURDHUI, null));
        assertThat(dsm.etat()).isEqualTo(EtatDsm.BROUILLON);
    }

    @Test
    @DisplayName("Chaque côté lit l'état de son point de vue : envoyée, à prendre en charge ; compléments envoyés, reçus")
    void libelles_selon_le_cote() {
        assertThat(EtatDsm.ENVOYEE.libelle(Cote.OPERATEUR)).isEqualTo("Envoyée");
        assertThat(EtatDsm.ENVOYEE.libelle(Cote.BNIA)).isEqualTo("À prendre en charge");
        assertThat(EtatDsm.COMPLEMENTS_RECUS.libelle(Cote.OPERATEUR)).isEqualTo("Compléments envoyés");
        assertThat(EtatDsm.COMPLEMENTS_RECUS.libelle(Cote.BNIA)).isEqualTo("Compléments reçus");
    }

    @Test
    void une_transition_sur_une_version_perimee_est_refusee() {
        assertThatThrownBy(() -> dans(EtatDsm.ENVOYEE).prendreEnInstruction(2L))
                .isInstanceOf(ModificationConcurrente.class);
    }

    @ParameterizedTest
    @EnumSource(value = EtatDsm.class, names = {"ENVOYEE", "EN_INSTRUCTION", "VALIDEE"})
    @DisplayName("RG-DEC-21 : la saisie ne se modifie qu'en brouillon ou en compléments demandés")
    void rg_dec_21_saisie_figee(EtatDsm etat) {
        DeclarationStocksMouvements dsm = dans(etat);
        SaisieDsm saisie = saisie(dsm, Map.of(0, Map.of(C17, l("10"))));
        refuse("RG-DEC-21", () -> dsm.enregistrer(saisie, null));
        refuse("RG-DEC-21", () -> dsm.apercu(saisie));
    }

    @Test
    @DisplayName("RG-DEC-21 : en compléments demandés, l'opérateur modifie sa saisie")
    void rg_dec_21_complements_modifiables() {
        DeclarationStocksMouvements dsm = dans(EtatDsm.COMPLEMENTS_DEMANDES);
        dsm.enregistrer(saisie(dsm, Map.of(0, Map.of(C17, l("10")))), null);
        assertThat(dsm.total(C17)).isEqualTo(l("10"));
        assertThat(dsm.etat()).isEqualTo(EtatDsm.COMPLEMENTS_DEMANDES);
    }
}
