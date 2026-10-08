package fr.bnia.authentification.adapter.rest;

import fr.bnia.authentification.application.ConsulterConnexions;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * RG-AUT-09 : à chaque requête, la session ne vaut que si {@code operateurs} autorise encore la connexion
 * (non bloqué, un rattachement utilisable) et que le compte existe : sinon elle est fermée, et la requête
 * continue sans utilisateur (401 si l'endpoint l'exige). Créé par la configuration de sécurité, pas
 * déclaré comme bean : il ne doit pas s'appliquer hors de la chaîne de Spring Security.
 */
class FiltreSessionValide extends OncePerRequestFilter {

    private final ConsulterConnexions connexions;

    FiltreSessionValide(ConsulterConnexions connexions) {
        this.connexions = connexions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse, FilterChain suite)
            throws ServletException, IOException {
        Authentication authentification = SecurityContextHolder.getContext().getAuthentication();
        if (authentification instanceof UsernamePasswordAuthenticationToken && authentification.isAuthenticated()
                && !valide(authentification.getName())) {
            SecurityContextHolder.clearContext();
            HttpSession session = requete.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
        suite.doFilter(requete, reponse);
    }

    private boolean valide(String utilisateur) {
        try {
            return connexions.sessionValide(Long.parseLong(utilisateur));
        } catch (NumberFormatException inattendu) {
            return false;
        }
    }
}
