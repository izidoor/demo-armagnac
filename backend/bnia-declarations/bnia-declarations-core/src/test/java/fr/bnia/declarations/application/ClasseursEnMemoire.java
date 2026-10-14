package fr.bnia.declarations.application;

import fr.bnia.declarations.application.EditionsDsm.EditionDsm;
import fr.bnia.declarations.domain.ClasseurLu;
import fr.bnia.declarations.domain.ImportRefuse;
import fr.bnia.declarations.domain.OngletDsm;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Le modèle Excel simulé : les classeurs « lus » sont déposés par les tests, sous un nom de fichier. */
final class ClasseursEnMemoire implements ClasseursDsm {

    final List<EditionDsm> ecrits = new ArrayList<>();
    private final Map<String, ClasseurLu> fichiers = new HashMap<>();

    /** Le contenu d'un fichier qui se lit comme ce classeur. */
    byte[] deposer(String nom, ClasseurLu classeur) {
        fichiers.put(nom, classeur);
        return nom.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] ecrire(EditionDsm edition) {
        ecrits.add(edition);
        return "PK".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ClasseurLu lire(byte[] contenu) {
        ClasseurLu classeur = fichiers.get(new String(contenu, StandardCharsets.UTF_8));
        if (classeur == null) {
            throw new ImportRefuse(List.of(new ImportRefuse.Erreur(OngletDsm.RECAPITULATIF, 1, "A",
                    "Le fichier n'est pas un classeur Excel au format du modèle.")));
        }
        return classeur;
    }
}
