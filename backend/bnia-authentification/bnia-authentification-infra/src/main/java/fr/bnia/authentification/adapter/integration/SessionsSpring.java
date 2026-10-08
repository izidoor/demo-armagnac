package fr.bnia.authentification.adapter.integration;

import fr.bnia.authentification.application.SessionsOuvertes;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

/**
 * Les sessions d'un utilisateur, retrouvées par le nom de leur principal (son identifiant), dans les
 * tables de Spring Session JDBC (A-AUT-3) ; fermées à la réinitialisation du mot de passe (RG-AUT-08) ou
 * à la suppression du compte (RG-AUT-11).
 */
@Component
class SessionsSpring<S extends Session> implements SessionsOuvertes {

    private final FindByIndexNameSessionRepository<S> sessions;

    SessionsSpring(FindByIndexNameSessionRepository<S> sessions) {
        this.sessions = sessions;
    }

    @Override
    public void fermer(long utilisateur) {
        sessions.findByPrincipalName(String.valueOf(utilisateur)).keySet().forEach(sessions::deleteById);
    }
}
