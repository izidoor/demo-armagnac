package fr.bnia.operateurs.application;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HabilitationsTest {

    private final Memoire memoire = new Memoire();
    private final Habilitations habilitations = memoire.habilitations();
    private final long condom = memoire.operateur("000001", "Maison Durand, Condom");
    private final long laubade = memoire.operateur("000003", "Domaine de Laubade");
    private final Acteur jeanne = memoire.membre(condom, Role.ADMIN, "Jeanne", "Durand");
    private final Acteur luc = memoire.membre(memoire.bnia, Role.AGENT, "Luc", "Dubos");

    @Test
    @DisplayName("RG-OPE-31 : les droits sont ceux du rattachement actif, sur son seul opérateur")
    void rg_ope_31_droits_locaux() {
        Droits droits = habilitations.droits(jeanne, condom);

        assertThat(droits.local()).contains(Role.ADMIN);
        assertThat(droits.gere(Module.ACCES)).isTrue();
        assertThat(habilitations.droits(jeanne, laubade).aucunAcces()).isTrue();
        assertThat(habilitations.droits(new Acteur(999), condom).aucunAcces()).as("inconnu").isTrue();
    }

    @Test
    @DisplayName("RG-OPE-32 : le rôle tenu au BNIA vaut pour tous les opérateurs")
    void rg_ope_32_droits_bnia() {
        Droits droits = habilitations.droits(luc, laubade);

        assertThat(droits).isEqualTo(new Droits(laubade, Optional.empty(), Optional.of(Role.AGENT)));
        assertThat(droits.gere(Module.CONTACTS)).isTrue();
        assertThat(habilitations.droitsBnia(luc).gere(Module.PARAMETRAGE)).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-26 : un opérateur inactif gèle ses rattachements ; le rôle tenu au BNIA y vaut toujours")
    void rg_ope_26_operateur_inactif() {
        Operateur operateur = memoire.operateurs.parId(condom).orElseThrow();
        operateur.desactiver();
        memoire.operateurs.mettreAJour(operateur);

        assertThat(habilitations.droits(jeanne, condom).aucunAcces()).isTrue();
        assertThat(habilitations.etatConnexion(jeanne.utilisateur())).isEqualTo(EtatConnexion.AUCUN_ACCES_ACTIF);
        assertThat(habilitations.droits(luc, condom).lit(Module.CONTACTS)).isTrue();
    }

    @Test
    @DisplayName("RG-OPE-50 : un utilisateur bloqué n'a plus aucun droit, ni connexion")
    void rg_ope_50_bloque() {
        var utilisateur = memoire.utilisateurs.parId(luc.utilisateur()).orElseThrow();
        utilisateur.bloquer("Départ");
        memoire.utilisateurs.mettreAJour(utilisateur);

        assertThat(habilitations.droits(luc, laubade).aucunAcces()).isTrue();
        assertThat(habilitations.etatConnexion(luc.utilisateur())).isEqualTo(EtatConnexion.UTILISATEUR_BLOQUE);
        assertThat(habilitations.etatConnexion(999)).isEqualTo(EtatConnexion.INCONNU);
        assertThat(habilitations.etatConnexion(jeanne.utilisateur())).isEqualTo(EtatConnexion.AUTORISE);
    }
}
