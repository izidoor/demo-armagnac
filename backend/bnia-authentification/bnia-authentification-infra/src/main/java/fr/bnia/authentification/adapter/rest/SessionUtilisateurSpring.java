package fr.bnia.authentification.adapter.rest;

import fr.bnia.commun.adapter.rest.SessionUtilisateur;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * L'utilisateur de la session, pour le socle REST ({@code Connecte}) : le nom du principal est son
 * identifiant (A-AUT-3) ; la session ne porte rien d'autre, jamais ses droits (RG-AUT-09).
 */
@Component
class SessionUtilisateurSpring implements SessionUtilisateur {

    @Override
    public Optional<Long> utilisateurConnecte() {
        Authentication authentification = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentification instanceof UsernamePasswordAuthenticationToken) || !authentification.isAuthenticated()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(authentification.getName()));
        } catch (NumberFormatException inattendu) {
            return Optional.empty();
        }
    }
}
