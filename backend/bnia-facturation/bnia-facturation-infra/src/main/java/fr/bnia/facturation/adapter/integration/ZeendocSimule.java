package fr.bnia.facturation.adapter.integration;

import fr.bnia.facturation.application.Zeendoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Zeendoc simulé pour le prototype (spec 08 §3.10, bordure, {@code contextes.md} §1.1) : le dépôt n'est que
 * journalisé. Un connecteur réel prendra sa place derrière le même port.
 */
@Component
class ZeendocSimule implements Zeendoc {

    private static final Logger LOG = LoggerFactory.getLogger(ZeendocSimule.class);

    @Override
    public void deposer(String numero, byte[] pdf) {
        LOG.info("Zeendoc simulé : pièce {} déposée ({} octets).", numero, pdf.length);
    }
}
