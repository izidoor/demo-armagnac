package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Liste de référentiel en mémoire, pour les tests de cas d'usage. */
class ValeursEnMemoire<T extends ValeurSimple> implements ValeursRepository<T>, ValeursSimplesLecture<T> {

    final Map<String, T> valeurs = new LinkedHashMap<>();

    @Override
    public Optional<T> parCode(Code code) {
        return Optional.ofNullable(valeurs.get(code.valeur()));
    }

    @Override
    public boolean existeCode(Code code) {
        return valeurs.keySet().stream().anyMatch(cle -> cle.equalsIgnoreCase(code.valeur()));
    }

    @Override
    public void inserer(T valeur) {
        valeurs.put(valeur.code().valeur(), valeur);
    }

    @Override
    public void mettreAJour(T valeur) {
        valeurs.put(valeur.code().valeur(), valeur);
    }

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
