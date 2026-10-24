package fr.bnia.boot.suivi;

import fr.bnia.boot.suivi.JournalSuivi.Appareil;
import fr.bnia.boot.suivi.JournalSuivi.Evenement;
import fr.bnia.boot.suivi.JournalSuivi.Type;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Suivi de l'utilisation ({@link SuiviConfiguration}). Hors du contrat OpenAPI : le front l'appelle sans client
 * généré, et l'export n'a pas à y figurer.
 */
@RestController
@RequestMapping("/api/suivi")
@Hidden
class SuiviController {

    /** {@code lieu} : gabarit de la route ({@code /bo/operateurs/$id}) ou section de l'offre ({@code offre/01-synthese}). */
    record EvenementFront(Type type, String lieu, Appareil appareil) {
    }

    private static final Set<Type> TYPES_FRONT = EnumSet.of(Type.ECRAN, Type.OFFRE, Type.PRESENCE);
    private static final int LONGUEUR_LIEU = 200;
    private static final LocalDate TOUJOURS = LocalDate.of(2000, 1, 1);
    private static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final JdbcClient jdbc;
    private final JournalSuivi journal;
    private final JetonSuivi jeton;

    SuiviController(JdbcClient jdbc, JournalSuivi journal, JetonSuivi jeton) {
        this.jdbc = jdbc;
        this.journal = journal;
        this.jeton = jeton;
    }

    /** Un écran affiché, une section de l'offre, ou la présence sur la page, signalée chaque minute. Sous session. */
    @PostMapping("/evenements")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void enregistrer(@RequestBody EvenementFront evenement, Principal principal, HttpSession session,
            HttpServletRequest requete) {
        if (!TYPES_FRONT.contains(evenement.type()) || evenement.appareil() == null || !lieuValide(evenement.lieu())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Événement de suivi invalide.");
        }
        if (!SuiviFichiers.exclu(requete)) {
            journal.enregistrer(Long.parseLong(principal.getName()), session.getId(), evenement.appareil(),
                    new Evenement(evenement.type(), evenement.lieu(), null, null));
        }
    }

    /**
     * Un rapport en CSV (séparateur {@code ;}), avec l'en-tête {@code Authorization: Bearer <jeton>}. Sans jeton
     * configuré, l'export n'existe pas (404).
     */
    @GetMapping("/export/{rapport}")
    ResponseEntity<String> exporter(@PathVariable String rapport,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate depuis,
            @RequestHeader(name = "Authorization", required = false) String autorisation) {
        if (!jeton.configure()) {
            return ResponseEntity.notFound().build();
        }
        if (!jeton.accepte(autorisation)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return RapportSuivi.parNom(rapport)
                .map(r -> ResponseEntity.ok().contentType(CSV).body(csv(r, depuis == null ? TOUJOURS : depuis)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Efface tout le suivi, avec le même jeton que l'export ; renvoie le nombre d'événements effacés. Sans jeton
     * configuré, la remise à zéro n'existe pas (404).
     */
    @PostMapping("/reset")
    ResponseEntity<String> remettreAZero(
            @RequestHeader(name = "Authorization", required = false) String autorisation) {
        if (!jeton.configure()) {
            return ResponseEntity.notFound().build();
        }
        if (!jeton.accepte(autorisation)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        int effaces = jdbc.sql("delete from suivi.evenement").update();
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(Integer.toString(effaces));
    }

    private String csv(RapportSuivi rapport, LocalDate depuis) {
        return jdbc.sql(rapport.sql()).param("depuis", depuis).query((ResultSet lignes) -> {
            ResultSetMetaData colonnes = lignes.getMetaData();
            StringBuilder csv = new StringBuilder();
            for (int i = 1; i <= colonnes.getColumnCount(); i++) {
                csv.append(i > 1 ? ";" : "").append(colonnes.getColumnLabel(i));
            }
            csv.append('\n');
            while (lignes.next()) {
                ligne(lignes, colonnes.getColumnCount(), csv);
            }
            return csv.toString();
        });
    }

    private static void ligne(ResultSet lignes, int nombre, StringBuilder csv) throws SQLException {
        for (int i = 1; i <= nombre; i++) {
            String valeur = lignes.getString(i);
            csv.append(i > 1 ? ";" : "").append(valeur == null ? "" : valeur.replaceAll("[;\\r\\n]", " "));
        }
        csv.append('\n');
    }

    private static boolean lieuValide(String lieu) {
        return lieu != null && lieu.length() <= LONGUEUR_LIEU && (lieu.startsWith("/") || lieu.startsWith("offre/"));
    }
}
