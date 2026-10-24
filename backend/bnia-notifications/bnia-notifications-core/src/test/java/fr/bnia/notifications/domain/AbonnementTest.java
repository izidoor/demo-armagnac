package fr.bnia.notifications.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AbonnementTest {

    /** Clé et secret de l'appareil de l'exemple de RFC 8291 (annexe A). */
    static final String CLE = "BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4";
    static final String SECRET = "BTBZMqHH6r4Tts7J_aSIgg";
    static final String ADRESSE = "https://fcm.googleapis.com/fcm/send/abc123";
    private static final Instant LE = Instant.parse("2026-10-24T08:00:00Z");

    private static void enfreint(Runnable appel) {
        assertThatThrownBy(appel::run).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-NOT-01");
    }

    @Test
    @DisplayName("RG-NOT-01 : un abonnement a une adresse HTTPS, une clé P-256 et un secret de 16 octets")
    void rg_not_01_abonnement_valide() {
        Abonnement abonnement = Abonnement.nouveau(7, " " + ADRESSE + " ", CLE, SECRET + "==", LE);
        assertThat(abonnement.adresse()).isEqualTo(ADRESSE);
        assertThat(abonnement.utilisateur()).isEqualTo(7);
        assertThat(abonnement.creeLe()).isEqualTo(LE);
    }

    @Test
    @DisplayName("RG-NOT-01 : une adresse qui n'est pas en HTTPS, une clé ou un secret invalides sont refusés")
    void rg_not_01_refus() {
        enfreint(() -> Abonnement.nouveau(7, "http://fcm.googleapis.com/fcm/send/abc", CLE, SECRET, LE));
        enfreint(() -> Abonnement.nouveau(7, "pas une adresse", CLE, SECRET, LE));
        enfreint(() -> Abonnement.nouveau(7, null, CLE, SECRET, LE));
        enfreint(() -> Abonnement.nouveau(7, ADRESSE, "BCVx", SECRET, LE));
        enfreint(() -> Abonnement.nouveau(7, ADRESSE, "Aa" + CLE.substring(2), SECRET, LE));
        enfreint(() -> Abonnement.nouveau(7, ADRESSE, CLE, "BTBZ", LE));
        enfreint(() -> Abonnement.nouveau(7, ADRESSE, CLE, "%%%", LE));
    }
}
