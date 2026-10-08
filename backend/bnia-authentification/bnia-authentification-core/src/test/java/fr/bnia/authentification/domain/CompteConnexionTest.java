package fr.bnia.authentification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CompteConnexionTest {

    static final Politique POLITIQUE = new Politique(Duration.ofMinutes(10), Duration.ofHours(1), Duration.ofDays(30),
            3, Duration.ofMinutes(30), 12, Optional.empty());
    private static final Instant MAINTENANT = Instant.parse("2026-10-08T08:00:00Z");

    private static CompteConnexion compte() {
        CompteConnexion compte = CompteConnexion.creer(7, " Jeanne.Durand@Exemple.fr ");
        compte.definirMotDePasse("empreinte", MAINTENANT);
        return compte;
    }

    @Test
    @DisplayName("RG-AUT-03 : trois échecs consécutifs bloquent le compte 30 minutes ; une réussite remet le compteur à zéro")
    void rg_aut_03_blocage() {
        CompteConnexion compte = compte();

        assertThat(compte.enregistrerEchec(MAINTENANT, POLITIQUE)).isFalse();
        compte.reussir(MAINTENANT);
        assertThat(compte.echecs()).isZero();
        compte.enregistrerEchec(MAINTENANT, POLITIQUE);
        compte.enregistrerEchec(MAINTENANT, POLITIQUE);
        assertThat(compte.enregistrerEchec(MAINTENANT, POLITIQUE)).isTrue();

        assertThat(compte.estBloque(MAINTENANT.plus(Duration.ofMinutes(29)))).isTrue();
        assertThat(compte.estBloque(MAINTENANT.plus(Duration.ofMinutes(30)))).isFalse();
        assertThat(compte.bloqueJusqua()).contains(MAINTENANT.plus(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("RG-AUT-05 : le code est valable 10 minutes, une seule fois ; un nouveau code invalide le précédent")
    void rg_aut_05_code() {
        CompteConnexion compte = compte();
        String premier = compte.emettreCode(MAINTENANT, POLITIQUE);
        String second = compte.emettreCode(MAINTENANT, POLITIQUE);

        assertThat(second).matches("[0-9]{6}");
        assertThat(premier.equals(second) || !compte.verifierCode(premier, MAINTENANT)).isTrue();
        assertThat(compte.verifierCode(second, MAINTENANT.plus(Duration.ofMinutes(10)))).as("expiré").isFalse();
        String troisieme = compte.emettreCode(MAINTENANT, POLITIQUE);
        assertThat(compte.verifierCode(" " + troisieme + " ", MAINTENANT.plus(Duration.ofMinutes(9)))).isTrue();
        assertThat(compte.verifierCode(troisieme, MAINTENANT.plus(Duration.ofMinutes(9)))).as("une seule fois").isFalse();
    }

    @Test
    @DisplayName("RG-AUT-01 : en dev et en démo, le code vaut toujours 000000")
    void rg_aut_01_code_fixe() {
        Politique demo = new Politique(Duration.ofMinutes(10), Duration.ofHours(1), Duration.ofDays(30), 3,
                Duration.ofMinutes(30), 12, Optional.of("000000"));

        assertThat(compte().emettreCode(MAINTENANT, demo)).isEqualTo("000000");
        assertThat(CompteConnexion.identifiant(" Jeanne.Durand@Exemple.fr ")).isEqualTo("jeanne.durand@exemple.fr");
    }

    @Test
    @DisplayName("RG-AUT-06 : un navigateur de confiance dispense de code 30 jours ; un nouveau mot de passe l'oublie")
    void rg_aut_06_navigateur_de_confiance() {
        CompteConnexion compte = compte();
        String jeton = compte.faireConfiance(MAINTENANT, POLITIQUE);

        assertThat(compte.estDeConfiance(jeton, MAINTENANT.plus(Duration.ofDays(29)))).isTrue();
        assertThat(compte.estDeConfiance(jeton, MAINTENANT.plus(Duration.ofDays(30)))).isFalse();
        assertThat(compte.estDeConfiance("autre", MAINTENANT)).isFalse();
        assertThat(compte.navigateurs().getFirst().empreinte()).isEqualTo(Secrets.empreinte(jeton));

        compte.definirMotDePasse("nouvelle", MAINTENANT.plusSeconds(60));
        assertThat(compte.estDeConfiance(jeton, MAINTENANT.plusSeconds(120))).isFalse();
    }

    @Test
    @DisplayName("RG-AUT-07 : le mot de passe compte douze caractères au moins")
    void rg_aut_07_longueur() {
        assertThatThrownBy(() -> CompteConnexion.verifierMotDePasse("onze-caract", POLITIQUE))
                .isInstanceOf(RegleEnfreinte.class).hasMessageContaining("12 caractères")
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-AUT-07");
        CompteConnexion.verifierMotDePasse("douze-caract", POLITIQUE);
    }

    @Test
    @DisplayName("RG-AUT-08 : réinitialiser lève le blocage, remet le compteur à zéro, oublie les navigateurs ; le lien sert une fois")
    void rg_aut_08_reinitialisation() {
        CompteConnexion compte = compte();
        String confiance = compte.faireConfiance(MAINTENANT, POLITIQUE);
        compte.enregistrerEchec(MAINTENANT, POLITIQUE);
        compte.enregistrerEchec(MAINTENANT, POLITIQUE);
        compte.enregistrerEchec(MAINTENANT, POLITIQUE);
        String premier = compte.demanderReinitialisation(MAINTENANT, POLITIQUE);
        String jeton = compte.demanderReinitialisation(MAINTENANT, POLITIQUE);

        assertThatThrownBy(() -> compte.reinitialiser(premier, "x", MAINTENANT)).as("remplacé").hasMessageContaining("expiré");
        compte.reinitialiser(jeton, "nouvelle", MAINTENANT.plus(Duration.ofMinutes(5)));

        assertThat(compte.estBloque(MAINTENANT.plus(Duration.ofMinutes(5)))).isFalse();
        assertThat(compte.echecs()).isZero();
        assertThat(compte.motDePasse()).contains("nouvelle");
        assertThat(compte.estDeConfiance(confiance, MAINTENANT.plus(Duration.ofMinutes(6)))).isFalse();
        assertThatThrownBy(() -> compte.reinitialiser(jeton, "encore", MAINTENANT.plus(Duration.ofMinutes(6))))
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-AUT-08");
    }

    @Test
    @DisplayName("RG-AUT-08 : le lien de réinitialisation est valable une heure")
    void rg_aut_08_expiration() {
        CompteConnexion compte = compte();
        String jeton = compte.demanderReinitialisation(MAINTENANT, POLITIQUE);

        assertThatThrownBy(() -> compte.reinitialiser(jeton, "nouvelle", MAINTENANT.plus(Duration.ofHours(1))))
                .hasMessageContaining("expiré");
    }

    @Test
    @DisplayName("RG-AUT-10 et RG-AUT-11 : créé sans mot de passe ; l'identifiant suit l'e-mail")
    void rg_aut_11_creation() {
        CompteConnexion compte = CompteConnexion.creer(9, "marc@lafitte.fr");

        assertThat(compte.aUnMotDePasse()).isFalse();
        compte.changerIdentifiant("Marc.Lafitte@Exemple.fr");
        assertThat(compte.identifiant()).isEqualTo("marc.lafitte@exemple.fr");
    }
}
