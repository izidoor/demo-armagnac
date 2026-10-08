package fr.bnia.operateurs.domain;

import java.time.Instant;
import java.util.List;

/**
 * Fiche professionnelle (vision §2.2, spec 02 §3.2.3) : les informations d'une personne dans un
 * opérateur, propriété de l'opérateur. Racine de son agrégat (A-OPE-6). Elle a un seul porteur, contact
 * ou rattachement du même opérateur, et reste la même ligne quand la personne change d'état
 * (RG-OPE-30).
 */
public final class FicheProfessionnelle {

    private final Long id;
    private final long operateur;
    private TypeFiche type;
    private CoordonneesProfessionnelles coordonnees;
    private String modifieePar;
    private Instant modifieeLe;
    private final long version;

    private FicheProfessionnelle(Long id, long operateur, TypeFiche type, CoordonneesProfessionnelles coordonnees,
            String modifieePar, Instant modifieeLe, long version) {
        this.id = id;
        this.operateur = operateur;
        this.type = type;
        this.coordonnees = coordonnees;
        this.modifieePar = modifieePar;
        this.modifieeLe = modifieeLe;
        this.version = version;
    }

    /**
     * RG-OPE-30 : la fiche d'un contact déclaré, ou d'une personne nouvelle à l'acceptation de son
     * invitation.
     *
     * @param auteur copie du nom de l'auteur (RG-OPE-46)
     */
    public static FicheProfessionnelle creer(long operateur, TypeFiche type, CoordonneesProfessionnelles coordonnees,
            String auteur, Instant maintenant) {
        return new FicheProfessionnelle(null, operateur, type, coordonnees, auteur, maintenant, 0);
    }

    public static FicheProfessionnelle reconstituer(Long id, long operateur, TypeFiche type,
            CoordonneesProfessionnelles coordonnees, String modifieePar, Instant modifieeLe, long version) {
        return new FicheProfessionnelle(id, operateur, type, coordonnees, modifieePar, modifieeLe, version);
    }

    /** RG-OPE-46, 51 : sur la version lue ; l'auteur et la date sont enregistrés. Renvoie les champs changés. */
    public List<Changement> modifier(CoordonneesProfessionnelles nouvelles, Long versionLue, String nomPersonne,
            String auteur, Instant maintenant) {
        Versions.verifier(versionLue, version, "La fiche professionnelle de " + nomPersonne);
        List<Changement> changements = Changement.coordonnees(coordonnees, nouvelles);
        this.coordonnees = nouvelles;
        this.modifieePar = auteur;
        this.modifieeLe = maintenant;
        return changements;
    }

    /** RG-OPE-30 : la personne devient membre (RG-OPE-42) ou redevient contact (RG-OPE-44) ; même ligne. */
    public void porterPar(TypeFiche nouveau) {
        this.type = nouveau;
    }

    public Long id() {
        return id;
    }

    public long operateur() {
        return operateur;
    }

    public TypeFiche type() {
        return type;
    }

    public CoordonneesProfessionnelles coordonnees() {
        return coordonnees;
    }

    public String modifieePar() {
        return modifieePar;
    }

    public Instant modifieeLe() {
        return modifieeLe;
    }

    public long version() {
        return version;
    }
}
