package fr.bnia.referentiels.application;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.function.Consumer;

/**
 * Socle des cas d'usage d'une liste de référentiel : consultation, désactivation, réactivation, et
 * les opérations d'écriture que les sous-classes composent.
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

    public T consulter(String code) {
        Code cle = Code.de(code);
        return repository.parCode(cle).orElseThrow(() -> new ElementIntrouvable(
                "Code « " + cle + " » introuvable (" + nomListe + ")."));
    }

    /** RG-REF-04 : la valeur n'est plus proposée à la saisie, mais reste lisible. */
    public T desactiver(String code) {
        return modifierValeur(code, ValeurSimple::desactiver);
    }

    public T reactiver(String code) {
        return modifierValeur(code, ValeurSimple::reactiver);
    }

    /** Insère une nouvelle valeur après avoir vérifié l'unicité de son code (RG-REF-01). */
    protected T enregistrerNouvelle(T valeur) {
        if (repository.existeCode(valeur.code())) {
            throw new ConflitUnicite("RG-REF-01",
                    "Le code « " + valeur.code() + " » existe déjà (" + nomListe + ").");
        }
        repository.inserer(valeur);
        return valeur;
    }

    protected T modifierValeur(String code, Consumer<T> modification) {
        T valeur = consulter(code);
        modification.accept(valeur);
        repository.mettreAJour(valeur);
        return valeur;
    }

    protected String nomListe() {
        return nomListe;
    }
}
