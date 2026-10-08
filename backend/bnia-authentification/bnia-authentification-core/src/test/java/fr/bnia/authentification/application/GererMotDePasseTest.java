package fr.bnia.authentification.application;

import static fr.bnia.authentification.application.Banc.ICI;
import static fr.bnia.authentification.application.Banc.MOT_DE_PASSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fr.bnia.authentification.domain.EvenementConnexion;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GererMotDePasseTest {

    private final Banc banc = new Banc();
    private final GererMotDePasse gerer = banc.gererMotDePasse();

    {
        banc.compte(7, "jeanne.durand@exemple.fr");
    }

    private static String regle(Throwable e) {
        return ((RegleEnfreinte) e).regle().orElseThrow();
    }

    @Test
    @DisplayName("RG-AUT-07 : changer son mot de passe exige l'actuel et douze caractères")
    void rg_aut_07_changement() {
        assertThatThrownBy(() -> gerer.changer(7, "faux-mot-de-passe", "nouveau-mot-de-passe", ICI))
                .hasMessageContaining("actuel est incorrect");
        assertThatThrownBy(() -> gerer.changer(7, MOT_DE_PASSE, "court", ICI)).extracting(GererMotDePasseTest::regle)
                .isEqualTo("RG-AUT-07");

        gerer.changer(7, MOT_DE_PASSE, "nouveau-mot-de-passe", ICI);

        assertThat(banc.comptes.parUtilisateur(7).orElseThrow().motDePasse()).contains("h:nouveau-mot-de-passe");
        assertThat(banc.journal.lignes.getLast().evenement()).isEqualTo(EvenementConnexion.MOT_DE_PASSE_CHANGE);
    }

    @Test
    @DisplayName("RG-AUT-08 : mot de passe oublié ; e-mail inconnu signalé ; le lien réinitialise et ferme les sessions")
    void rg_aut_08_mot_de_passe_oublie() {
        assertThatThrownBy(() -> gerer.demanderReinitialisation("personne@exemple.fr", ICI))
                .hasMessage("Aucun compte pour cet e-mail.");

        gerer.demanderReinitialisation("Jeanne.Durand@exemple.fr", ICI);
        assertThat(banc.courriels.getLast().texte()).contains("https://bnia.example/reinitialisation?jeton=");
        gerer.reinitialiser(banc.dernierJeton(), "mot-de-passe-retrouve", ICI);

        assertThat(banc.comptes.parUtilisateur(7).orElseThrow().motDePasse()).contains("h:mot-de-passe-retrouve");
        assertThat(banc.sessionsFermees).containsExactly(7L);
        assertThat(banc.journal.lignes).extracting(LigneConnexion::evenement)
                .containsExactly(EvenementConnexion.REINITIALISATION_DEMANDEE, EvenementConnexion.MOT_DE_PASSE_REINITIALISE);
        assertThat(banc.journal.lignes).allSatisfy(l -> assertThat(l.detail()).isNull());
    }

    @Test
    @DisplayName("RG-AUT-15 : le premier mot de passe se pose avec le jeton de l'invitation acceptée, une seule fois")
    void rg_aut_15_premier_mot_de_passe() {
        banc.gererComptes().creer(8, "marc@lafitte.fr");
        when(banc.operateurs.utilisateurDInvitationAcceptee("jeton-invitation")).thenReturn(Optional.of(8L));

        assertThat(gerer.definirPremierMotDePasse("jeton-invitation", "mot-de-passe-de-marc", ICI)).isEqualTo(8L);

        assertThat(banc.comptes.parUtilisateur(8).orElseThrow().motDePasse()).contains("h:mot-de-passe-de-marc");
        assertThat(banc.journal.lignes).extracting(LigneConnexion::evenement)
                .containsExactly(EvenementConnexion.MOT_DE_PASSE_DEFINI, EvenementConnexion.CONNEXION_REUSSIE);
        assertThatThrownBy(() -> gerer.definirPremierMotDePasse("jeton-invitation", "autre-mot-de-passe", ICI))
                .hasMessageContaining("déjà défini");
        assertThatThrownBy(() -> gerer.definirPremierMotDePasse("jeton-inconnu", "autre-mot-de-passe", ICI))
                .extracting(GererMotDePasseTest::regle).isEqualTo("RG-AUT-15");
    }
}
