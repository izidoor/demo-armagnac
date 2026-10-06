package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Opérateur : structure suivie par le BNIA (§4.3 p.18-21, C.1 p.100-105). Racine de l'agrégat
 * {@code Operateur} (ADR-012), qui portera aussi le déclaratif, la production, la diffusion et les
 * contacts.
 */
public final class Operateur {

    private final Long id;
    private TypeOperateur type;
    private CodeOperateur code;
    private IdentiteOperateur identite;
    private boolean actif;

    private Operateur(Long id, TypeOperateur type, CodeOperateur code, IdentiteOperateur identite, boolean actif) {
        this.id = id;
        this.type = exigerType(type);
        this.code = code;
        this.identite = identite;
        this.actif = actif;
    }

    /** RG-OPE-01, RG-OPE-02 : type, code et raison sociale obligatoires ; actif à la création. */
    public static Operateur creer(TypeOperateur type, CodeOperateur code, IdentiteOperateur identite) {
        return new Operateur(null, type, code, identite, true);
    }

    public static Operateur reconstituer(Long id, TypeOperateur type, CodeOperateur code, IdentiteOperateur identite,
            boolean actif) {
        return new Operateur(id, type, code, identite, actif);
    }

    /**
     * RG-OPE-05 : changer le type ou le code postal ne change pas le code ; seule une saisie explicite le
     * change (RG-OPE-04).
     */
    public void modifierIdentite(TypeOperateur nouveauType, CodeOperateur nouveauCode, IdentiteOperateur nouvelle) {
        this.type = exigerType(nouveauType);
        this.code = nouveauCode;
        this.identite = nouvelle;
    }

    /** RG-OPE-05 : attribution d'un nouveau code, sur demande. */
    public void recoder(CodeOperateur nouveau) {
        this.code = nouveau;
    }

    /** RG-OPE-25, 26. */
    public void desactiver() {
        actif = false;
    }

    public void reactiver() {
        actif = true;
    }

    /**
     * RG-OPE-02 : ce qui manque pour facturer ou certifier ; une fiche à laquelle il manque quelque chose
     * est « incomplète », mais s'enregistre.
     */
    public List<String> champsManquants() {
        List<String> manquants = new ArrayList<>();
        Adresse adresse = identite.adresse();
        if (adresse.ligne1() == null) {
            manquants.add("adresse");
        }
        if (adresse.codePostal() == null) {
            manquants.add("code postal");
        }
        if (adresse.ville() == null) {
            manquants.add("ville");
        }
        if (adresse.pays() == null) {
            manquants.add("pays");
        }
        return manquants;
    }

    /**
     * RG-OPE-05 : la série à laquelle le code devrait appartenir, s'il n'en a plus la forme ; rien si le
     * code est conforme, ou si aucune série ne s'applique (négociant sans code postal français).
     *
     * @param enFrance vrai si le pays de l'adresse est la France
     */
    public Optional<SerieCode> serieAttendueSiCodeNonConforme(boolean enFrance) {
        return SerieCode.pour(type, identite.adresse().codePostal(), enFrance).filter(serie -> !serie.contient(code));
    }

    private static TypeOperateur exigerType(TypeOperateur type) {
        if (type == null) {
            throw new RegleEnfreinte("RG-OPE-01", "Le type d'opérateur est obligatoire : producteur, négociant ou autre.");
        }
        return type;
    }

    public Long id() {
        return id;
    }

    public TypeOperateur type() {
        return type;
    }

    public CodeOperateur code() {
        return code;
    }

    public IdentiteOperateur identite() {
        return identite;
    }

    public boolean estActif() {
        return actif;
    }
}
