package fr.bnia.referentiels.domain;

import java.util.Objects;

/**
 * Valeur d'une liste de référentiel : identifiant technique, code, libellé, état actif.
 *
 * <p>Chaque liste a sa propre sous-classe (A-REF-3) et peut y ajouter ses attributs et ses règles.
 * Les autres contextes référencent la valeur par son identifiant, attribué par la base à
 * l'enregistrement ; le code et le libellé restent modifiables (RG-REF-02, ADR-011). Une valeur
 * utilisée ailleurs n'est pas supprimée mais désactivée (RG-REF-04).
 */
public abstract class ValeurSimple {

    private final Long id;
    private Code code;
    private Libelle libelle;
    private boolean actif;

    /** @param id {@code null} pour une valeur pas encore enregistrée */
    protected ValeurSimple(Long id, Code code, Libelle libelle, boolean actif) {
        this.id = id;
        this.code = Objects.requireNonNull(code);
        this.libelle = Objects.requireNonNull(libelle);
        this.actif = actif;
    }

    /** Identifiant technique ; {@code null} tant que la valeur n'est pas enregistrée. */
    public Long id() {
        return id;
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

    /** RG-REF-02 : le code se corrige ; son unicité est vérifiée par le cas d'usage (RG-REF-01). */
    public void recoder(Code code) {
        this.code = Objects.requireNonNull(code);
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
