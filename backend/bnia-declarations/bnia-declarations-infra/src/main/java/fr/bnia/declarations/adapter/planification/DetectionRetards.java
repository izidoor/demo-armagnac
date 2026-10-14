package fr.bnia.declarations.adapter.planification;

import fr.bnia.declarations.application.DetecterRetards;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RG-DEC-41 (A-DEC-7, ADR-019) : chaque jour, les périodes passées en retard publient {@code DeclarationEnRetard},
 * une seule fois chacune. Cron réglable par {@code bnia.declarations.detection-retards} ; {@code -} le coupe.
 */
@Component
class DetectionRetards {

    private static final Logger LOG = LoggerFactory.getLogger(DetectionRetards.class);

    private final DetecterRetards detecter;

    DetectionRetards(DetecterRetards detecter) {
        this.detecter = detecter;
    }

    @Scheduled(cron = "${bnia.declarations.detection-retards:0 0 6 * * *}")
    void detecter() {
        int signales = detecter.detecter();
        if (signales > 0) {
            LOG.info("{} déclaration(s) en retard signalée(s).", signales);
        }
    }
}
