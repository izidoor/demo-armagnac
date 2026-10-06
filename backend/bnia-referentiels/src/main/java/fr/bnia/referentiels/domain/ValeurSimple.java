package fr.bnia.referentiels.domain;

import java.util.Objects;

/**
 * Valeur d'une liste de référentiel : code, libellé, état actif.
 *
 * <p>Chaque liste a sa propre sous-classe (A-REF-3) et peut y ajouter ses attributs et ses règles.
 * Le code ne change jamais (RG-REF-02). Aucune valeur n'est supprimée : elle est désactivée, puis
 * éventuellement réactivée (RG-REF-04).
 */
public abstract class ValeurSimple {

    private final Code code;
    private Libelle libelle;
    private boolean actif;

    protected ValeurSimple(Code code, Libelle libelle, boolean actif) {
        this.code = Objects.requireNonNull(code);
        this.libelle = Objects.requireNonNull(libelle);
        this.actif = actif;
    }

    public Code code() {
        return code;
    }

    public Libelle libelle() {
        return libelle;
    }

    public boolean estActif() {
        return actif;
    }

    public void renommer(Libelle libelle) {
        this.libelle = Objects.requireNonNull(libelle);
    }

    public void desactiver() {
        actif = false;
    }

    public void reactiver() {
        actif = true;
    }
}
