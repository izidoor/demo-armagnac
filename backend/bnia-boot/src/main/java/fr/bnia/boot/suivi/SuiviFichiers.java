package fr.bnia.boot.suivi;

import fr.bnia.boot.suivi.JournalSuivi.Evenement;
import fr.bnia.boot.suivi.JournalSuivi.Type;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Téléchargements, exports et imports, relevés côté serveur : beaucoup passent par un lien direct ({@code <a download>},
 * {@code window.open}) que le front ne voit pas. Un téléchargement est une réponse {@code GET} réussie qui est un fichier
 * (en-tête {@code Content-Disposition}, ou PDF, Excel, CSV, archive) ; un import, une requête multipart, réussie ou
 * refusée. Les requêtes partielles (206, visionneuse PDF) ne comptent pas : seule la première, complète.
 *
 * <p>Relevé après l'envoi de la réponse ({@code afterCompletion}) : l'événement arrive en base un instant après.
 */
@Component
class SuiviFichiers implements HandlerInterceptor {

    /** Posé par le front sur {@code ?suivi=non} : ce navigateur n'est pas suivi. */
    static final String COOKIE_EXCLUSION = "bnia-suivi";

    private static final List<String> TYPES_FICHIER = List.of("application/pdf", "application/vnd.", "text/csv",
            "application/zip", "application/octet-stream");

    private final JournalSuivi journal;

    SuiviFichiers(JournalSuivi journal) {
        this.journal = journal;
    }

    @Override
    public void afterCompletion(HttpServletRequest requete, HttpServletResponse reponse, Object handler,
            Exception erreur) {
        HttpSession session = requete.getSession(false);
        Optional<Long> utilisateur = utilisateur();
        if (session == null || utilisateur.isEmpty() || exclu(requete)) {
            return;
        }
        evenement(requete, reponse, handler).ifPresent(evenement -> journal.enregistrer(utilisateur.get(),
                session.getId(), journal.appareil(session.getId(), requete.getHeader(HttpHeaders.USER_AGENT)),
                evenement));
    }

    static boolean exclu(HttpServletRequest requete) {
        Cookie[] cookies = requete.getCookies();
        return cookies != null && Arrays.stream(cookies)
                .anyMatch(c -> c.getName().equals(COOKIE_EXCLUSION) && c.getValue().equals("non"));
    }

    private static Optional<Evenement> evenement(HttpServletRequest requete, HttpServletResponse reponse,
            Object handler) {
        int statut = reponse.getStatus();
        String lieu = requete.getMethod() + " " + gabarit(requete, handler);
        if (requete instanceof MultipartHttpServletRequest multipart) {
            return Optional.of(new Evenement(Type.IMPORT, lieu, nomsDesFichiers(multipart), statut));
        }
        if (requete.getMethod().equals("GET") && statut == HttpServletResponse.SC_OK && fichier(reponse)) {
            return Optional.of(new Evenement(Type.TELECHARGEMENT, lieu, nomDuFichier(requete, reponse), statut));
        }
        return Optional.empty();
    }

    private static boolean fichier(HttpServletResponse reponse) {
        String type = reponse.getContentType();
        return reponse.getHeader(HttpHeaders.CONTENT_DISPOSITION) != null
                || (type != null && TYPES_FICHIER.stream().anyMatch(type::startsWith));
    }

    /** Le gabarit de l'endpoint ({@code /api/operateurs/{operateur}/dsm/{id}/import}) ; l'adresse pour un fichier statique. */
    private static String gabarit(HttpServletRequest requete, Object handler) {
        Object gabarit = requete.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return handler instanceof HandlerMethod && gabarit != null ? gabarit.toString() : requete.getRequestURI();
    }

    private static String nomDuFichier(HttpServletRequest requete, HttpServletResponse reponse) {
        String disposition = reponse.getHeader(HttpHeaders.CONTENT_DISPOSITION);
        String nom = disposition == null ? null : ContentDisposition.parse(disposition).getFilename();
        if (nom != null) {
            return nom;
        }
        String adresse = requete.getRequestURI();
        return adresse.substring(adresse.lastIndexOf('/') + 1);
    }

    private static String nomsDesFichiers(MultipartHttpServletRequest requete) {
        return requete.getMultiFileMap().values().stream()
                .flatMap(List::stream)
                .map(MultipartFile::getOriginalFilename)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

    private static Optional<Long> utilisateur() {
        Authentication authentification = SecurityContextHolder.getContext().getAuthentication();
        if (authentification instanceof UsernamePasswordAuthenticationToken && authentification.isAuthenticated()) {
            try {
                return Optional.of(Long.parseLong(authentification.getName()));
            } catch (NumberFormatException inattendu) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
