package fr.bnia.support;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Un navigateur, pour les tests d'intégration : il garde ses cookies (session, jeton CSRF, navigateur de
 * confiance) et renvoie le jeton CSRF en en-tête, comme le front (A-AUT-3).
 */
public final class Navigateur implements ClientHttpRequestInterceptor {

    private final Map<String, String> cookies = new LinkedHashMap<>();

    @Override
    public synchronized ClientHttpResponse intercept(HttpRequest requete, byte[] corps,
            ClientHttpRequestExecution execution) throws IOException {
        if (!cookies.isEmpty()) {
            requete.getHeaders().set(HttpHeaders.COOKIE, cookies.entrySet().stream()
                    .map(c -> c.getKey() + "=" + c.getValue()).collect(Collectors.joining("; ")));
        }
        cookie("XSRF-TOKEN").ifPresent(jeton -> requete.getHeaders().set("X-XSRF-TOKEN", jeton));
        ClientHttpResponse reponse = execution.execute(requete, corps);
        for (String entete : reponse.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE)) {
            for (HttpCookie cookie : HttpCookie.parse(entete)) {
                if (cookie.getMaxAge() == 0) {
                    cookies.remove(cookie.getName());
                } else {
                    cookies.put(cookie.getName(), cookie.getValue());
                }
            }
        }
        return reponse;
    }

    public synchronized Optional<String> cookie(String nom) {
        return Optional.ofNullable(cookies.get(nom));
    }

    /** Oublie un cookie, comme un navigateur qu'on vide. */
    public synchronized void oublier(String nom) {
        cookies.remove(nom);
    }
}
