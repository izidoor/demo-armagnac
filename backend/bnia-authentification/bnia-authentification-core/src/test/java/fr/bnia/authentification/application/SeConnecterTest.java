package fr.bnia.authentification.application;

import static fr.bnia.authentification.application.Banc.ICI;
import static fr.bnia.authentification.application.Banc.MOT_DE_PASSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import fr.bnia.authentification.domain.EvenementConnexion;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SeConnecterTest {

    private final Banc banc = new Banc();
    private final SeConnecter seConnecter = banc.seConnecter();

    {
        banc.compte(7, "jeanne.durand@exemple.fr");
    }

    private static String regle(Resultat resultat) {
        return ((Resultat.Refuse) resultat).regle();
    }

    private static String message(Resultat resultat) {
        return ((Resultat.Refuse) resultat).message();
    }

    @Test
    @DisplayName("RG-AUT-01 et RG-AUT-12 : e-mail sans distinction de casse et mot de passe, puis un code par e-mail ; la connexion est journalisée")
    void rg_aut_01_connexion_en_deux_etapes() {
        Resultat etape = seConnecter.verifierMotDePasse("Jeanne.Durand@Exemple.fr", MOT_DE_PASSE, null, ICI);

        assertThat(etape).isEqualTo(new Resultat.CodeEnvoye(7));
        assertThat(banc.courriels.getLast().destinataire()).isEqualTo("jeanne.durand@exemple.fr");
        assertThat(banc.courriels.getLast().texte()).doesNotContain(MOT_DE_PASSE);
        Resultat fin = seConnecter.verifierCode(7, banc.dernierCode(), false, ICI);

        assertThat(fin).isInstanceOf(Resultat.Connecte.class);
        assertThat(banc.journal.lignes).extracting(LigneConnexion::evenement)
                .containsExactly(EvenementConnexion.CONNEXION_REUSSIE);
        assertThat(banc.journal.lignes.getFirst()).satisfies(l -> {
            assertThat(l.adresseIp()).isEqualTo("192.0.2.1");
            assertThat(l.navigateur()).isEqualTo("Firefox");
            assertThat(l.utilisateur()).isEqualTo(7L);
        });
        assertThat(banc.comptes.parUtilisateur(7).orElseThrow().derniereConnexion()).isPresent();
    }

    @Test
    @DisplayName("RG-AUT-02 : messages explicites, e-mail inconnu ou mot de passe incorrect")
    void rg_aut_02_messages() {
        Resultat inconnu = seConnecter.verifierMotDePasse("personne@exemple.fr", MOT_DE_PASSE, null, ICI);
        Resultat faux = seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", "mauvais-mot-de-passe", null, ICI);

        assertThat(message(inconnu)).isEqualTo("Aucun compte pour cet e-mail.");
        assertThat(regle(faux)).isEqualTo("RG-AUT-02");
        assertThat(message(faux)).isEqualTo("Mot de passe incorrect.");
        assertThat(banc.journal.lignes).extracting(LigneConnexion::identifiantSaisi, LigneConnexion::evenement)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("personne@exemple.fr", EvenementConnexion.ECHEC_MOT_DE_PASSE),
                        org.assertj.core.groups.Tuple.tuple("jeanne.durand@exemple.fr", EvenementConnexion.ECHEC_MOT_DE_PASSE));
    }

    @Test
    @DisplayName("RG-AUT-03 : trois échecs (un code faux compte) bloquent 30 minutes, même le bon mot de passe ; puis tout reprend")
    void rg_aut_03_blocage() {
        seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", "mauvais-1", null, ICI);
        seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI);
        seConnecter.verifierCode(7, "999999", false, ICI);
        Resultat troisieme = seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", "mauvais-3", null, ICI);

        assertThat(regle(troisieme)).isEqualTo("RG-AUT-03");
        assertThat(message(troisieme)).contains("bloqué jusqu'à 10:30", "mot de passe oublié");
        assertThat(regle(seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI)))
                .isEqualTo("RG-AUT-03");
        assertThat(banc.journal.lignes).extracting(LigneConnexion::evenement).contains(EvenementConnexion.ECHEC_CODE,
                EvenementConnexion.BLOCAGE_DECLENCHE, EvenementConnexion.CONNEXION_REFUSEE);

        banc.horloge.avancer(Duration.ofMinutes(30));
        assertThat(seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI))
                .isInstanceOf(Resultat.CodeEnvoye.class);
    }

    @Test
    @DisplayName("RG-AUT-04 : un utilisateur bloqué par le BNIA, ou sans accès actif, est refusé sans recevoir de code")
    void rg_aut_04_refus_par_operateurs() {
        when(banc.operateurs.etatConnexion(7)).thenReturn(EtatConnexion.UTILISATEUR_BLOQUE);
        Resultat bloque = seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI);
        when(banc.operateurs.etatConnexion(7)).thenReturn(EtatConnexion.AUCUN_ACCES_ACTIF);
        Resultat sansAcces = seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI);

        assertThat(message(bloque)).isEqualTo("Compte bloqué, contactez le BNIA.");
        assertThat(message(sansAcces)).startsWith("Aucun accès actif");
        assertThat(banc.courriels).isEmpty();
        assertThat(banc.journal.lignes).extracting(LigneConnexion::detail)
                .containsExactly("UTILISATEUR_BLOQUE", "AUCUN_ACCES_ACTIF");
    }

    @Test
    @DisplayName("RG-AUT-05 : renvoyer un code invalide le précédent")
    void rg_aut_05_renvoi() {
        seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI);
        String premier = banc.dernierCode();
        seConnecter.renvoyerCode(7, ICI);
        String second = banc.dernierCode();

        if (!premier.equals(second)) {
            assertThat(seConnecter.verifierCode(7, premier, false, ICI)).isInstanceOf(Resultat.Refuse.class);
        }
        assertThat(seConnecter.verifierCode(7, second, false, ICI)).isInstanceOf(Resultat.Connecte.class);
    }

    @Test
    @DisplayName("RG-AUT-06 : un navigateur mis en confiance se connecte ensuite sans code")
    void rg_aut_06_navigateur_de_confiance() {
        seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI);
        Resultat.Connecte connecte = (Resultat.Connecte) seConnecter.verifierCode(7, banc.dernierCode(), true, ICI);
        String jeton = connecte.jetonConfiance().orElseThrow();
        int courriels = banc.courriels.size();

        assertThat(seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, jeton, ICI))
                .isEqualTo(new Resultat.Connecte(7, java.util.Optional.empty()));
        assertThat(banc.courriels).hasSize(courriels);
        assertThat(banc.journal.lignes.getLast().detail()).isEqualTo("navigateur de confiance");
        assertThat(banc.journal.lignes).extracting(LigneConnexion::evenement)
                .contains(EvenementConnexion.NAVIGATEUR_DE_CONFIANCE);
    }

    @Test
    @DisplayName("RG-AUT-11 et RG-AUT-15 : un compte sans mot de passe ne permet aucune connexion")
    void rg_aut_11_sans_mot_de_passe() {
        banc.gererComptes().creer(8, "marc@lafitte.fr");

        assertThat(regle(seConnecter.verifierMotDePasse("marc@lafitte.fr", "", null, ICI))).isEqualTo("RG-AUT-02");
    }

    @Test
    @DisplayName("RG-AUT-14 : durées et seuils viennent de la configuration")
    void rg_aut_14_configuration() {
        seConnecter.verifierMotDePasse("jeanne.durand@exemple.fr", MOT_DE_PASSE, null, ICI);
        String code = banc.dernierCode();
        banc.horloge.avancer(banc.parametres.politique().validiteCode());

        assertThat(seConnecter.verifierCode(7, code, false, ICI)).isInstanceOf(Resultat.Refuse.class);
        assertThat(banc.courriels.getFirst().texte()).contains("valable 10 minutes");
    }
}
