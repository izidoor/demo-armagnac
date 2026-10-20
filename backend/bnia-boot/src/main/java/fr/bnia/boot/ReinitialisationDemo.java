package fr.bnia.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Remise à zéro de la base de démonstration, lancée dans un conteneur ponctuel ({@code scalingo-reset.sh}).
 *
 * <p>Avec {@code bnia.demo.reinitialisation=true}, {@link MigrationDevConfiguration} vide puis recrée la
 * base, les chargeurs du profil {@code demo} (des {@code ApplicationRunner}) la remplissent, puis
 * l'application s'arrête : l'événement « prête » n'est publié qu'après tous les runners.
 */
@Component
@Profile("demo")
@ConditionalOnBooleanProperty("bnia.demo.reinitialisation")
class ReinitialisationDemo {

    private static final Logger LOG = LoggerFactory.getLogger(ReinitialisationDemo.class);

    @EventListener
    void arreterUneFoisLaDemoChargee(ApplicationReadyEvent pret) {
        LOG.info("Base de démonstration remise à zéro et rechargée : arrêt.");
        System.exit(SpringApplication.exit(pret.getApplicationContext()));
    }
}
