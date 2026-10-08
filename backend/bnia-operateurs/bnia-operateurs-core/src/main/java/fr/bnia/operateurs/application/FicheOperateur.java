package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Operateur;
import java.util.List;
import java.util.Optional;

/**
 * Un opérateur et ce que la fiche en montre : ce qui manque (RG-OPE-02), un code qui n'a plus la forme
 * de son type (RG-OPE-05), les autres fiches du groupe (RG-OPE-09).
 *
 * @param codePropose si le code n'est plus conforme : le code que « attribuer un nouveau code » donnerait
 */
public record FicheOperateur(
        Operateur operateur,
        List<String> champsManquants,
        boolean codeConforme,
        Optional<CodeOperateur> codePropose,
        List<OperateurResume> autresDuGroupe) {
}
