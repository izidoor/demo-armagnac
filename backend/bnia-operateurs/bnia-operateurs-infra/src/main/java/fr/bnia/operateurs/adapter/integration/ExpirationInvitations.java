package fr.bnia.operateurs.adapter.integration;

import fr.bnia.operateurs.application.GererInvitations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RG-OPE-41 : les invitations dont l'expiration est passée deviennent {@code EXPIREE}, chaque heure. Une
 * invitation échue entre deux passages est de toute façon refusée à l'acceptation (RG-OPE-42).
 */
@Component
class ExpirationInvitations {

    private final GererInvitations invitations;

    ExpirationInvitations(GererInvitations invitations) {
        this.invitations = invitations;
    }

    @Scheduled(cron = "${bnia.operateurs.expiration-invitations:0 0 * * * *}")
    void expirer() {
        invitations.expirerEchues();
    }
}
