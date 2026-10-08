package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Opérateur : structure suivie par le BNIA (§4.3 p.18-21, C.1 p.100-105), ou le BNIA lui-même
 * (RG-OPE-39). Racine de l'agrégat {@code Operateur} (ADR-012, amendé par ADR-016) : identité,
 * déclaratif, production, diffusion des millésimes, désignations. Ses personnes (contacts, fiches,
 * rattachements, invitations) sont des agrégats distincts (A-OPE-6).
 */
public final class Operateur {

    private final Long id;
    private TypeOperateur type;
    private CodeOperateur code;
    private IdentiteOperateur identite;
    private Declaratif declaratif;
    private Production production;
    private DiffusionMillesimes diffusion;
    private boolean accesStatistiques;
    private boolean actif;
    private Long contactPrincipal;
    private DestinataireFactures destinataireFactures;
    private final long version;

    private Operateur(Long id, TypeOperateur type, CodeOperateur code, IdentiteOperateur identite,
            Declaratif declaratif, Production production, DiffusionMillesimes diffusion, boolean accesStatistiques,
            boolean actif, Long contactPrincipal, DestinataireFactures destinataireFactures, long version) {
        this.id = id;
        this.type = exigerType(type);
        this.code = code;
        this.identite = identite;
        declaratif.verifierPour(this.type);
        this.declaratif = declaratif;
        this.production = production;
        this.diffusion = diffusion;
        this.accesStatistiques = accesStatistiques;
        this.actif = actif;
        this.contactPrincipal = contactPrincipal;
        this.destinataireFactures = destinataireFactures;
        this.version = version;
    }

    /**
     * RG-OPE-01, 02 : type, code et raison sociale obligatoires ; actif. Valeurs par défaut du type pour
     * le déclaratif (RG-OPE-10, 11), refus de diffusion (RG-OPE-14), pas d'accès aux statistiques
     * (RG-OPE-15). Le type {@code BNIA} ne se crée pas (RG-OPE-39).
     */
    public static Operateur creer(TypeOperateur type, CodeOperateur code, IdentiteOperateur identite) {
        TypeOperateur verifie = exigerType(type);
        if (verifie == TypeOperateur.BNIA) {
            throw new RegleEnfreinte("RG-OPE-39", "L'opérateur BNIA existe déjà et il est unique : choisissez"
                    + " producteur, négociant ou autre.");
        }
        return new Operateur(null, verifie, code, identite, Declaratif.parDefaut(verifie), Production.aucune(),
                DiffusionMillesimes.refus(), false, true, null, null, 0);
    }

    public static Operateur reconstituer(Long id, TypeOperateur type, CodeOperateur code, IdentiteOperateur identite,
            Declaratif declaratif, Production production, DiffusionMillesimes diffusion, boolean accesStatistiques,
            boolean actif, Long contactPrincipal, DestinataireFactures destinataireFactures, long version) {
        return new Operateur(id, type, code, identite, declaratif, production, diffusion, accesStatistiques, actif,
                contactPrincipal, destinataireFactures, version);
    }

    /**
     * RG-OPE-05 : changer le type ou le code postal ne change pas le code ; seule une saisie explicite le
     * change (RG-OPE-04). Le déclaratif ne s'ajuste que si le nouveau type l'exige
     * ({@link Declaratif#ajustePour}). Aucun type ne devient ni ne cesse d'être {@code BNIA} (RG-OPE-39).
     */
    public void modifierIdentite(TypeOperateur nouveauType, CodeOperateur nouveauCode, IdentiteOperateur nouvelle) {
        TypeOperateur verifie = exigerType(nouveauType);
        if ((type == TypeOperateur.BNIA) != (verifie == TypeOperateur.BNIA)) {
            throw new RegleEnfreinte("RG-OPE-39", "Le type BNIA est réservé à l'interprofession : il ne se choisit"
                    + " ni ne se quitte.");
        }
        this.declaratif = declaratif.ajustePour(type, verifie);
        this.type = verifie;
        this.code = nouveauCode;
        this.identite = nouvelle;
    }

    /** Onglet Production : déclaratif contrôlé selon le type (RG-OPE-01, 10 à 13). */
    public void modifierProduction(Declaratif nouveauDeclaratif, Production nouvelle) {
        nouveauDeclaratif.verifierPour(type);
        this.declaratif = nouveauDeclaratif;
        this.production = nouvelle;
    }

    /** RG-OPE-14. */
    public void modifierDiffusion(DiffusionMillesimes nouvelle) {
        this.diffusion = nouvelle;
    }

    /** RG-OPE-15 : fixé par le BNIA. */
    public void modifierAccesStatistiques(boolean acces) {
        this.accesStatistiques = acces;
    }

    /** RG-OPE-05 : attribution d'un nouveau code, sur demande. */
    public void recoder(CodeOperateur nouveau) {
        this.code = nouveau;
    }

    /**
     * RG-OPE-26 : reste consultable ; ses rattachements sont gelés (les droits ne valent que dans un
     * opérateur actif). L'opérateur BNIA ne se désactive pas (RG-OPE-39).
     */
    public void desactiver() {
        exigerAutreQueBnia("désactive");
        actif = false;
    }

    public void reactiver() {
        actif = true;
    }

    /** RG-OPE-39 : l'opérateur BNIA ne se supprime pas. */
    public void verifierSuppressible() {
        exigerAutreQueBnia("supprime");
    }

    /**
     * RG-OPE-22, 23, 24 : contact principal et destinataire des factures, deux fiches professionnelles de
     * l'opérateur, ou une adresse saisie pour le destinataire ; le cas d'usage vérifie les fiches.
     *
     * @param principal    identifiant de la fiche du contact principal, ou {@code null}
     * @param destinataire {@code null} : aucun
     */
    public void designer(Long principal, DestinataireFactures destinataire) {
        this.contactPrincipal = principal;
        this.destinataireFactures = destinataire;
    }

    /** RG-OPE-22, 23 : la fiche est supprimée, ses désignations tombent ; vrai si l'une est tombée. */
    public boolean oublierFiche(long fiche) {
        boolean tombee = false;
        if (contactPrincipal != null && contactPrincipal == fiche) {
            contactPrincipal = null;
            tombee = true;
        }
        if (destinataireFactures != null && destinataireFactures.fiche() != null && destinataireFactures.fiche() == fiche) {
            destinataireFactures = null;
            tombee = true;
        }
        return tombee;
    }

    /**
     * RG-OPE-02 : ce qui manque pour facturer ou certifier ; une fiche à laquelle il manque quelque chose
     * est « incomplète », mais s'enregistre. Sans objet pour l'opérateur BNIA (RG-OPE-39).
     */
    public List<String> champsManquants() {
        List<String> manquants = new ArrayList<>();
        if (estBnia()) {
            return manquants;
        }
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
        if (production.region() == null) {
            manquants.add("région");
        }
        return manquants;
    }

    /**
     * RG-OPE-05 : la série à laquelle le code devrait appartenir, s'il n'en a plus la forme ; rien si le
     * code est conforme, ou si aucune série ne s'applique (négociant sans code postal français, BNIA).
     *
     * @param enFrance vrai si le pays de l'adresse est la France
     */
    public Optional<SerieCode> serieAttendueSiCodeNonConforme(boolean enFrance) {
        return SerieCode.pour(type, identite.adresse().codePostal(), enFrance).filter(serie -> !serie.contient(code));
    }

    /**
     * RG-OPE-51 : la saisie a été faite sur la version courante ; sinon, quelqu'un a modifié l'opérateur
     * entre-temps.
     *
     * @param lue version lue par l'écran, {@code null} si l'appelant ne la connaît pas
     */
    public void verifierVersion(Long lue) {
        Versions.verifier(lue, version, "La fiche de « " + identite.raisonSociale() + " »");
    }

    private void exigerAutreQueBnia(String verbe) {
        if (estBnia()) {
            throw new RegleEnfreinte("RG-OPE-39", "L'opérateur BNIA ne se " + verbe + " pas.");
        }
    }

    private static TypeOperateur exigerType(TypeOperateur type) {
        if (type == null) {
            throw new RegleEnfreinte("RG-OPE-01", "Le type d'opérateur est obligatoire : producteur, négociant ou autre.");
        }
        return type;
    }

    public boolean estBnia() {
        return type == TypeOperateur.BNIA;
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

    public Declaratif declaratif() {
        return declaratif;
    }

    public Production production() {
        return production;
    }

    public DiffusionMillesimes diffusion() {
        return diffusion;
    }

    public boolean accesStatistiques() {
        return accesStatistiques;
    }

    /** Identifiant de la fiche professionnelle désignée. */
    public Optional<Long> contactPrincipal() {
        return Optional.ofNullable(contactPrincipal);
    }

    public Optional<DestinataireFactures> destinataireFactures() {
        return Optional.ofNullable(destinataireFactures);
    }

    public boolean estActif() {
        return actif;
    }

    public long version() {
        return version;
    }
}
