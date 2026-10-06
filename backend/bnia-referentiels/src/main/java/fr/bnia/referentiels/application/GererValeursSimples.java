package fr.bnia.referentiels.application;

import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.ValeurSimple;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Cas d'usage d'une liste simple (code, libellé, actif) : chaque liste en a une sous-classe
 * {@code @UseCase} (A-REF-3).
 */
public abstract class GererValeursSimples<T extends ValeurSimple> extends GererValeurs<T> {

    private final ValeursSimplesLecture<T> lecture;
    private final BiFunction<Code, Libelle, T> fabrique;

    protected GererValeursSimples(ValeursRepository<T> repository, ValeursSimplesLecture<T> lecture,
            BiFunction<Code, Libelle, T> fabrique, String nomListe) {
        super(repository, nomListe);
        this.lecture = lecture;
        this.fabrique = fabrique;
    }

    public List<ValeurSimpleLigne> lister(FiltreValeurs filtre) {
        return lecture.lister(filtre);
    }

    /** RG-REF-01, RG-REF-03. */
    public T creer(String code, String libelle) {
        return enregistrerNouvelle(fabrique.apply(Code.de(code), Libelle.de(libelle)));
    }

    /** Seul le libellé change ; le code est immuable (RG-REF-02, RG-REF-03). */
    public T modifier(String code, String libelle) {
        Libelle nouveau = Libelle.de(libelle);
        return modifierValeur(code, valeur -> valeur.renommer(nouveau));
    }
}
