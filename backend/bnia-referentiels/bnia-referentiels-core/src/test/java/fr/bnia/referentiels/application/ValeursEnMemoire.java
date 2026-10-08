package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Liste de référentiel en mémoire, pour les tests de cas d'usage. Attribue les identifiants à
 * l'insertion, comme la base.
 */
class ValeursEnMemoire<T extends ValeurSimple> implements ValeursRepository<T> {

    final Map<Long, T> valeurs = new LinkedHashMap<>();
    private final BiFunction<Long, T, T> avecId;
    private long prochainId = 1;

    /** @param avecId reconstitue la valeur avec l'identifiant attribué */
    ValeursEnMemoire(BiFunction<Long, T, T> avecId) {
        this.avecId = avecId;
    }

    @Override
    public Optional<T> parId(long id) {
        return Optional.ofNullable(valeurs.get(id));
    }

    @Override
    public boolean existeCode(Code code, Long sauf) {
        return valeurs.values().stream()
                .anyMatch(v -> v.code().valeur().equalsIgnoreCase(code.valeur()) && !v.id().equals(sauf));
    }

    @Override
    public T inserer(T valeur) {
        T enregistree = avecId.apply(prochainId++, valeur);
        valeurs.put(enregistree.id(), enregistree);
        return enregistree;
    }

    @Override
    public void mettreAJour(T valeur) {
        valeurs.put(valeur.id(), valeur);
    }

    @Override
    public void supprimer(long id) {
        valeurs.remove(id);
    }

    /** L'identifiant de la valeur qui porte ce code. */
    long id(String code) {
        return valeurs.values().stream().filter(v -> v.code().valeur().equals(code)).findFirst().orElseThrow().id();
    }
}
