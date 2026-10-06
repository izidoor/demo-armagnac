package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Liste simple en mémoire, avec son listing, pour les tests de cas d'usage. */
class ValeursSimplesEnMemoire<T extends ValeurSimple> extends ValeursEnMemoire<T> implements ValeursSimplesLecture<T> {

    @Override
    public List<ValeurSimpleLigne> lister(FiltreValeurs filtre) {
        return valeurs.values().stream()
                .filter(v -> filtre.actif() == null || v.estActif() == filtre.actif())
                .filter(v -> filtre.recherche() == null || contient(v, filtre.recherche()))
                .sorted(Comparator.comparing(v -> v.libelle().valeur()))
                .map(v -> new ValeurSimpleLigne(v.code().valeur(), v.libelle().valeur(), v.estActif()))
                .toList();
    }

    private static boolean contient(ValeurSimple valeur, String texte) {
        String cherche = texte.toLowerCase(Locale.ROOT);
        return valeur.code().valeur().toLowerCase(Locale.ROOT).contains(cherche)
                || valeur.libelle().valeur().toLowerCase(Locale.ROOT).contains(cherche);
    }
}
