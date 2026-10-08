package fr.bnia.operateurs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.Role;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DroitsTest {

    @ParameterizedTest(name = "{0} : contacts {1}, accès {2}, paramétrage {3}")
    @CsvSource({"OBSERVATEUR, false, false, false", "AGENT, true, false, true", "ADMIN, true, true, true"})
    @DisplayName("RG-OPE-31 : tableau rôles × modules ; tout rôle lit")
    void rg_ope_31_roles_et_modules(Role role, boolean contacts, boolean acces, boolean parametrage) {
        assertThat(role.gere(Module.CONTACTS)).isEqualTo(contacts);
        assertThat(role.gere(Module.ACCES)).isEqualTo(acces);
        assertThat(role.gere(Module.PARAMETRAGE)).isEqualTo(parametrage);
        Droits local = new Droits(5, Optional.of(role), Optional.empty());
        assertThat(local.lit(Module.CONTACTS)).isTrue();
        assertThat(local.lit(Module.ACCES)).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-31 : sans rattachement actif, aucun droit ; le Paramétrage n'est qu'au BNIA")
    void rg_ope_31_sans_acces() {
        Droits aucun = Droits.aucun(5);
        assertThat(aucun.aucunAcces()).isTrue();
        assertThat(aucun.lit(Module.CONTACTS)).isFalse();

        Droits adminLocal = new Droits(5, Optional.of(Role.ADMIN), Optional.empty());
        assertThat(adminLocal.gere(Module.PARAMETRAGE)).isFalse();
        assertThat(adminLocal.lit(Module.PARAMETRAGE)).isFalse();
        assertThat(adminLocal.personnelBnia()).isFalse();
    }

    @Test
    @DisplayName("RG-OPE-32 : un rôle tenu au BNIA vaut pour tous les opérateurs, et s'ajoute au rôle local")
    void rg_ope_32_role_bnia() {
        Droits agentBnia = new Droits(5, Optional.empty(), Optional.of(Role.AGENT));
        assertThat(agentBnia.gere(Module.CONTACTS)).isTrue();
        assertThat(agentBnia.gere(Module.ACCES)).isFalse();
        assertThat(agentBnia.personnelBnia()).isTrue();
        assertThat(agentBnia.adminBnia()).isFalse();

        Droits cumul = new Droits(5, Optional.of(Role.ADMIN), Optional.of(Role.OBSERVATEUR));
        assertThat(cumul.gere(Module.ACCES)).as("le rôle local suffit").isTrue();
        assertThat(cumul.gere(Module.PARAMETRAGE)).as("mais pas pour le Paramétrage").isFalse();
        assertThat(new Droits(1, Optional.of(Role.ADMIN), Optional.of(Role.ADMIN)).adminBnia()).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-35 : le dernier ADMIN est protégé ; un ADMIN BNIA passe outre, sauf pour le BNIA lui-même")
    void rg_ope_35_dernier_admin() {
        Rattachement dernier = Rattachement.creer(5, 9, Role.ADMIN, 3, Instant.EPOCH);
        Droits adminLocal = new Droits(5, Optional.of(Role.ADMIN), Optional.empty());
        Droits adminBnia = new Droits(5, Optional.empty(), Optional.of(Role.ADMIN));

        assertThatThrownBy(() -> DernierAdmin.verifier(dernier, 1, adminLocal, false, "Jeanne Durand"))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("adressez-vous au BNIA");
        assertThatCode(() -> DernierAdmin.verifier(dernier, 2, adminLocal, false, "Jeanne Durand"))
                .as("un autre ADMIN reste").doesNotThrowAnyException();
        assertThatCode(() -> DernierAdmin.verifier(dernier, 1, adminBnia, false, "Jeanne Durand"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> DernierAdmin.verifier(dernier, 1, adminBnia, true, "Anne Lamarque"))
                .hasMessageContaining("dernier ADMIN du BNIA");
        Rattachement agent = Rattachement.creer(5, 9, Role.AGENT, 3, Instant.EPOCH);
        assertThatCode(() -> DernierAdmin.verifier(agent, 0, adminLocal, false, "x")).doesNotThrowAnyException();
    }
}
