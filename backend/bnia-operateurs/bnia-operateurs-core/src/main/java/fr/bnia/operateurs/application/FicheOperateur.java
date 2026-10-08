package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.partage.Droits;
import java.util.List;
import java.util.Optional;

/**
 * Un opérateur et ce que la fiche en montre : ce qui manque (RG-OPE-02), un code qui n'a plus la forme
 * de son type (RG-OPE-05), les autres fiches du groupe (RG-OPE-09), l'adresse de facturation qui résulte
 * du destinataire (RG-OPE-23), et les droits du lecteur (RG-OPE-31, 36 : commentaire interne et
 * signalements réservés au BNIA).
 *
 * @param codePropose si le code n'est plus conforme : le code que « attribuer un nouveau code » donnerait
 */
public record FicheOperateur(
        Operateur operateur,
        List<String> champsManquants,
        boolean codeConforme,
        Optional<CodeOperateur> codePropose,
        List<OperateurResume> autresDuGroupe,
        Optional<String> emailFacturation,
        Droits droits) {
}
