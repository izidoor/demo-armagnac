package fr.bnia.authentification.adapter.integration;

import fr.bnia.authentification.application.Courriel;
import fr.bnia.authentification.application.EnvoiCourriels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * E-mails du contexte par SMTP (H-AUT-3 : Mailpit en dev), après la validation de la transaction ; un
 * échec d'envoi est journalisé et n'annule rien. Sans serveur configuré, l'e-mail est seulement journalisé.
 */
@Component
class CourrielsConnexionSmtp implements EnvoiCourriels {

    private static final Logger LOG = LoggerFactory.getLogger(CourrielsConnexionSmtp.class);

    private final ObjectProvider<JavaMailSender> serveur;
    private final String expediteur;

    CourrielsConnexionSmtp(ObjectProvider<JavaMailSender> serveur,
            @Value("${bnia.courriels.expediteur:plateforme@bnia.example}") String expediteur) {
        this.serveur = serveur;
        this.expediteur = expediteur;
    }

    @Override
    public void envoyer(Courriel courriel) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    envoyerMaintenant(courriel);
                }
            });
        } else {
            envoyerMaintenant(courriel);
        }
    }

    private void envoyerMaintenant(Courriel courriel) {
        JavaMailSender smtp = serveur.getIfAvailable();
        if (smtp == null) {
            LOG.info("E-mail non envoyé (aucun serveur SMTP) à {} : {}", courriel.destinataire(), courriel.sujet());
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(courriel.destinataire());
        message.setSubject(courriel.sujet());
        message.setText(courriel.texte());
        try {
            smtp.send(message);
        } catch (MailException echec) {
            LOG.warn("Échec de l'envoi de « {} » à {} : {}", courriel.sujet(), courriel.destinataire(), echec.getMessage());
        }
    }
}
