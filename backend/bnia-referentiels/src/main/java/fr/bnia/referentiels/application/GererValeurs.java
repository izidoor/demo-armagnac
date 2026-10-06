package fr.bnia.referentiels.application;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.function.Consumer;

/**
 * Socle des cas d'usage d'une liste de référentiel : consultation, désactivation, réactivation,
 * suppression, et les opérations d'écriture que les sous-classes composent.
 */
public abstract class GererValeurs<T extends ValeurSimple> {

    private final ValeursRepository<T> repository;
    private final String nomListe;

    /**
     * @param nomListe nom de la liste au pluriel, pour les messages (« zones commerciales »)
     */
    protected GererValeurs(ValeursRepository<T> repository, String nomListe) {
        this.repository = repository;
        this.nomListe = nomListe;
    }

    public T consulter(long id) {
        return repository.parId(id).orElseThrow(() -> new ElementIntrouvable(
                "Valeur " + id + " introuvable (" + nomListe + ")."));
    }

    /** RG-REF-04 : la valeur n'est plus proposée à la saisie, mais reste lisible. */
    public T desactiver(long id) {
        return modifierValeur(id, ValeurSimple::desactiver);
    }

    public T reactiver(long id) {
        return modifierValeur(id, ValeurSimple::reactiver);
    }

    /** RG-REF-04 : seulement si aucune autre donnée n'y fait référence ; sinon, la désactiver. */
    public void supprimer(long id) {
        consulter(id);
        repository.supprimer(id);
    }

    /** Insère une nouvelle valeur après avoir vérifié l'unicité de son code (RG-REF-01). */
    protected T enregistrerNouvelle(T valeur) {
        verifierCodeLibre(valeur.code(), null);
        return repository.inserer(valeur);
    }

    /** Applique la modification, puis vérifie que le code, peut-être changé, reste unique (RG-REF-01, 02). */
    protected T modifierValeur(long id, Consumer<T> modification) {
        T valeur = consulter(id);
        modification.accept(valeur);
        verifierCodeLibre(valeur.code(), id);
        repository.mettreAJour(valeur);
        return valeur;
    }

    protected String nomListe() {
        return nomListe;
    }

    private void verifierCodeLibre(Code code, Long sauf) {
        if (repository.existeCode(code, sauf)) {
            throw new ConflitUnicite("RG-REF-01", "Le code « " + code + " » existe déjà (" + nomListe + ").");
        }
    }
}
