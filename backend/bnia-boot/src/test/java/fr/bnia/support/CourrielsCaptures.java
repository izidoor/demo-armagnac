package fr.bnia.support;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Serveur d'e-mails des tests d'intégration : retient les e-mails envoyés (après la validation des
 * transactions), pour y lire un code ou un lien.
 */
public final class CourrielsCaptures implements JavaMailSender {

    private final List<SimpleMailMessage> envoyes = new ArrayList<>();

    @Override
    public synchronized void send(SimpleMailMessage... messages) {
        envoyes.addAll(Arrays.asList(messages));
    }

    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Override
    public MimeMessage createMimeMessage(java.io.InputStream contenu) {
        throw new UnsupportedOperationException("Les e-mails de l'application sont en texte simple.");
    }

    @Override
    public void send(MimeMessage... messages) {
        throw new UnsupportedOperationException("Les e-mails de l'application sont en texte simple.");
    }

    /** Le dernier e-mail envoyé à cette adresse. */
    public synchronized SimpleMailMessage dernier(String destinataire) {
        return envoyes.reversed().stream()
                .filter(m -> m.getTo() != null && Arrays.asList(m.getTo()).contains(destinataire))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Aucun e-mail envoyé à " + destinataire));
    }

    public synchronized List<SimpleMailMessage> envoyesA(String destinataire) {
        return envoyes.stream().filter(m -> m.getTo() != null && Arrays.asList(m.getTo()).contains(destinataire))
                .toList();
    }

    /** Le jeton du lien du dernier e-mail envoyé à cette adresse. */
    public String jeton(String destinataire) {
        String texte = dernier(destinataire).getText();
        int debut = texte.indexOf("jeton=") + "jeton=".length();
        int fin = texte.indexOf('\n', debut);
        return URLDecoder.decode(texte.substring(debut, fin < 0 ? texte.length() : fin), StandardCharsets.UTF_8);
    }
}
