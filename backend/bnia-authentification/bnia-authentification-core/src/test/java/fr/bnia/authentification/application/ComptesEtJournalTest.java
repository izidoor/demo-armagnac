package fr.bnia.authentification.application;

import static fr.bnia.authentification.application.Banc.ICI;
import static fr.bnia.authentification.application.Banc.MOT_DE_PASSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ComptesEtJournalTest {

    private static final long BNIA = 1;

    private final Banc banc = new Banc();
    private final GererComptes comptes = banc.gererComptes();
    private final ConsulterConnexions consulter = banc.consulterConnexions();

    {
        banc.compte(7, "jeanne.durand@exemple.fr");
        when(banc.operateurs.operateurBnia()).thenReturn(BNIA);
        when(banc.operateurs.droits(1, BNIA)).thenReturn(new Droits(BNIA, Optional.of(Role.OBSERVATEUR),
                Optional.of(Role.OBSERVATEUR)));
        when(banc.operateurs.droits(7, BNIA)).thenReturn(Droits.aucun(BNIA));
    }

    @Test
    @DisplayName("RG-AUT-10 : l'identifiant de connexion suit l'e-mail ; il reste unique")
    void rg_aut_10_identifiant() {
        comptes.creer(8, "marc@lafitte.fr");

        comptes.changerIdentifiant(7, "Jeanne@Maison-Durand.fr");

        assertThat(banc.comptes.parUtilisateur(7).orElseThrow().identifiant()).isEqualTo("jeanne@maison-durand.fr");
        assertThatThrownBy(() -> comptes.changerIdentifiant(8, "jeanne@maison-durand.fr"))
                .isInstanceOf(ConflitUnicite.class);
    }

    @Test
    @DisplayName("RG-AUT-11 : créé sans mot de passe à la création de l'utilisateur, supprimé avec lui, sessions fermées ; le journal reste")
    void rg_aut_11_cycle_de_vie() {
        banc.seConnecter().verifierMotDePasse("jeanne.durand@exemple.fr", "faux-mot-de-passe", null, ICI);
        assertThat(comptes.creer(8, "marc@lafitte.fr").aUnMotDePasse()).isFalse();
        assertThatThrownBy(() -> comptes.creer(9, "MARC@lafitte.fr")).isInstanceOf(ConflitUnicite.class)
                .extracting(e -> ((ConflitUnicite) e).regle().orElseThrow()).isEqualTo("RG-AUT-10");

        comptes.supprimer(7);

        assertThat(banc.comptes.parUtilisateur(7)).isEmpty();
        assertThat(banc.sessionsFermees).containsExactly(7L);
        assertThat(banc.journal.lister(new FiltreConnexions(7L, null, null, null))).hasSize(1);
    }

    @Test
    @DisplayName("RG-AUT-09 : la session ne vaut qu'autant qu'operateurs autorise la connexion")
    void rg_aut_09_session() {
        assertThat(consulter.sessionValide(7)).isTrue();
        when(banc.operateurs.etatConnexion(7)).thenReturn(EtatConnexion.UTILISATEUR_BLOQUE);
        assertThat(consulter.sessionValide(7)).isFalse();
        assertThat(consulter.sessionValide(99)).as("compte supprimé").isFalse();
    }

    @Test
    @DisplayName("RG-AUT-12 et RG-AUT-13 : le personnel du BNIA lit tout le journal, chacun ses propres connexions")
    void rg_aut_13_journal() {
        banc.compte(1, "anne.lamarque@bnia.example");
        SeConnecter seConnecter = banc.seConnecter();
        seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", "faux-mot-de-passe", null, ICI);
        seConnecter.verifierMotDePasse("anne.lamarque@bnia.example", "faux-mot-de-passe", null, ICI);

        assertThat(consulter.journal(1, new FiltreConnexions(null, null, null, null))).hasSize(2);
        assertThat(consulter.mesConnexions(7)).singleElement().satisfies(l -> {
            assertThat(l.utilisateur()).isEqualTo(7L);
            assertThat(l.toString()).doesNotContain("faux-mot-de-passe").doesNotContain(MOT_DE_PASSE);
        });
        assertThatThrownBy(() -> consulter.journal(7, new FiltreConnexions(null, null, null, null)))
                .isInstanceOf(AccesRefuse.class);
        assertThat(consulter.etats(1, List.of(7L, 1L))).hasSize(2);
        assertThatThrownBy(() -> consulter.etats(7, List.of(7L))).isInstanceOf(AccesRefuse.class);
    }
}
