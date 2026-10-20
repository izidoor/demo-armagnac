package fr.bnia.authentification.application;

import static fr.bnia.authentification.application.Banc.ICI;
import static fr.bnia.authentification.application.Banc.MOT_DE_PASSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import fr.bnia.authentification.domain.EvenementConnexion;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Role;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DoubleAuthentificationTest {

    private static final long BNIA = 1;
    private static final long ADMIN = 2;
    private static final long AGENT = 3;

    private final Banc banc = new Banc();
    private final GererDoubleAuthentification gerer = banc.gererDoubleAuthentification();

    {
        banc.compte(7, "jeanne.durand@exemple.fr");
        banc.compte(ADMIN, "admin@bnia.fr");
        when(banc.operateurs.operateurBnia()).thenReturn(BNIA);
        when(banc.operateurs.droits(ADMIN, BNIA)).thenReturn(new Droits(BNIA, Optional.of(Role.ADMIN),
                Optional.of(Role.ADMIN)));
        when(banc.operateurs.droits(AGENT, BNIA)).thenReturn(new Droits(BNIA, Optional.of(Role.AGENT),
                Optional.of(Role.AGENT)));
        when(banc.operateurs.droits(7, BNIA)).thenReturn(Droits.aucun(BNIA));
    }

    @Test
    @DisplayName("RG-AUT-16 : désactivée, la connexion s'ouvre après le mot de passe, sans code ; le journal le dit")
    void rg_aut_16_connexion_sans_code() {
        banc.reglage.doubleAuthentification = false;

        assertThat(banc.seConnecter().verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI))
                .isEqualTo(new Resultat.Connecte(7, Optional.empty()));
        assertThat(banc.courriels).isEmpty();
        assertThat(banc.journal.lignes.getLast().evenement()).isEqualTo(EvenementConnexion.CONNEXION_REUSSIE);
        assertThat(banc.journal.lignes.getLast().detail()).isEqualTo("double authentification désactivée");
    }

    @Test
    @DisplayName("RG-AUT-16 et RG-AUT-12 : un ADMIN du BNIA l'active ou la désactive ; chaque changement est journalisé")
    void rg_aut_16_admin_bnia() {
        assertThat(gerer.consulter(ADMIN)).isEqualTo(new EtatDoubleAuthentification(true, true));

        gerer.definir(ADMIN, false, ICI);
        gerer.definir(ADMIN, false, ICI);
        gerer.definir(ADMIN, true, ICI);

        assertThat(banc.reglage.doubleAuthentification).isTrue();
        assertThat(banc.journal.lignes).extracting(LigneConnexion::evenement).containsExactly(
                EvenementConnexion.DOUBLE_AUTHENTIFICATION_DESACTIVEE, EvenementConnexion.DOUBLE_AUTHENTIFICATION_ACTIVEE);
        assertThat(banc.journal.lignes.getFirst().identifiantSaisi()).isEqualTo("admin@bnia.fr");
    }

    @Test
    @DisplayName("RG-AUT-16 : le personnel du BNIA voit l'état sans pouvoir le changer ; les autres n'y ont pas accès")
    void rg_aut_16_refus() {
        assertThat(gerer.consulter(AGENT)).isEqualTo(new EtatDoubleAuthentification(true, false));
        assertThatThrownBy(() -> gerer.definir(AGENT, false, ICI)).isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((AccesRefuse) e).regle().orElseThrow()).isEqualTo("RG-AUT-16");
        assertThatThrownBy(() -> gerer.consulter(7)).isInstanceOf(AccesRefuse.class);
        assertThat(banc.reglage.doubleAuthentification).isTrue();
        assertThat(banc.journal.lignes).isEmpty();
    }
}
