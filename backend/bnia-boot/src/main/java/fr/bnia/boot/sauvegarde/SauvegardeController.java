package fr.bnia.boot.sauvegarde;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Copie des données, pour la démonstration (« Données » des paramètres de l'application) : la base, au format de
 * {@code pg_dump}, et les documents déposés, en zip. Fichiers fixes, tirés d'une base de démonstration neuve par
 * {@code ./sauvegarde-demo.sh}. Pour un ADMIN du BNIA seul, vérifié ici : pas de cas d'usage pour deux fichiers
 * servis tels quels. Hors du contrat OpenAPI : le front les télécharge par un lien.
 */
@RestController
@RequestMapping("/api/sauvegarde")
@Hidden
class SauvegardeController {

    private static final MediaType ZIP = MediaType.parseMediaType("application/zip");

    private final OperateursLecture operateurs;

    SauvegardeController(OperateursLecture operateurs) {
        this.operateurs = operateurs;
    }

    @GetMapping("/base")
    ResponseEntity<Resource> base(Connecte connecte) {
        return fichier(connecte, "bnia.dump", MediaType.APPLICATION_OCTET_STREAM);
    }

    @GetMapping("/documents")
    ResponseEntity<Resource> documents(Connecte connecte) {
        return fichier(connecte, "bnia-documents.zip", ZIP);
    }

    private ResponseEntity<Resource> fichier(Connecte connecte, String nom, MediaType type) {
        if (!operateurs.droits(connecte.utilisateur(), operateurs.operateurBnia()).adminBnia()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La copie des données est réservée aux administrateurs du BNIA.");
        }
        Resource fichier = new ClassPathResource("sauvegarde/" + nom);
        if (!fichier.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nom).build().toString())
                .body(fichier);
    }
}
