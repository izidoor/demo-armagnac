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
            BiFunction<Code, Libelle, T> fabrique, String nomListe, ControleParametrage controle) {
        super(repository, nomListe, controle);
        this.lecture = lecture;
        this.fabrique = fabrique;
    }

    public List<ValeurSimpleLigne> lister(FiltreValeurs filtre) {
        return lecture.lister(filtre);
    }

    /** RG-REF-01, RG-REF-03. */
    public T creer(long acteur, String code, String libelle) {
        exigerParametrage(acteur);
        return nouvelle(code, libelle);
    }

    /** Sans contrôle de droits : l'appelant les a vérifiés. */
    protected T nouvelle(String code, String libelle) {
        return enregistrerNouvelle(fabrique.apply(Code.de(code), Libelle.de(libelle)));
    }

    /** Le code et le libellé se corrigent (RG-REF-01 à 03). */
    public T modifier(long acteur, long id, String code, String libelle) {
        exigerParametrage(acteur);
        Code nouveauCode = Code.de(code);
        Libelle nouveauLibelle = Libelle.de(libelle);
        return modifierValeur(id, valeur -> {
            valeur.recoder(nouveauCode);
            valeur.renommer(nouveauLibelle);
        });
    }
}
