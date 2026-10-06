package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Liste de référentiel en mémoire, pour les tests de cas d'usage. */
class ValeursEnMemoire<T extends ValeurSimple> implements ValeursRepository<T> {

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
}
