package fr.bnia.boot;

import fr.bnia.operateurs.application.AmorcerBnia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Au démarrage d'une installation neuve, le BNIA n'a aucun membre : la configuration désigne la personne
 * invitée comme premier ADMIN ({@code bnia.amorce.*}), qui reçoit le lien d'activation par e-mail
 * (choix d'implémentation du 2026-10-08). Sans configuration, ou dès que le BNIA a un ADMIN ou une
 * invitation en attente, rien ne se passe.
 */
@Component
@Order(0)
class AmorcageBnia implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AmorcageBnia.class);

    private final AmorcerBnia amorcer;
    private final String email;
    private final String nom;
    private final String prenom;

    AmorcageBnia(AmorcerBnia amorcer, @Value("${bnia.amorce.email:}") String email,
            @Value("${bnia.amorce.nom:}") String nom, @Value("${bnia.amorce.prenom:}") String prenom) {
        this.amorcer = amorcer;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (email.isBlank()) {
            return;
        }
        amorcer.amorcer(email, nom, prenom).ifPresent(emise ->
                LOG.info("Premier ADMIN du BNIA invité : {} (lien envoyé par e-mail).", emise.invitation().email()));
    }
}
