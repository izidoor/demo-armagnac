package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.Adresse;
import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.Alambic;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.CoordonneesGps;
import fr.bnia.operateurs.domain.Cvi;
import fr.bnia.operateurs.domain.Declaratif;
import fr.bnia.operateurs.domain.DiffusionMillesimes;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentiteOperateur;
import fr.bnia.operateurs.domain.NumeroAccise;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Production;
import fr.bnia.operateurs.domain.SerieCode;
import fr.bnia.operateurs.domain.Siret;
import fr.bnia.operateurs.domain.SiteWeb;
import fr.bnia.operateurs.domain.Textes;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.PaysVue;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.function.Supplier;

/**
 * Opérateurs au back-office : listing, création avec code proposé, identité, nouveau code,
 * désactivation, suppression (§4.3 p.18-21, spec 02 §5.1).
 */
@UseCase
public class GererOperateurs {

    /** Code ISO de la France : seul pays dont le code postal donne un département (RG-OPE-03). */
    private static final String FRANCE = "FR";

    private final OperateurRepository repository;
    private final ListingOperateurs listing;
    private final NumerotationLecture numerotation;
    private final ReferentielsLecture referentiels;
    private final Enregistreur enregistreur;

    public GererOperateurs(OperateurRepository repository, ListingOperateurs listing,
            NumerotationLecture numerotation, ReferentielsLecture referentiels, JournalContacts journal,
            PublicationEvenements publication, Clock horloge) {
        this.repository = repository;
        this.listing = listing;
        this.numerotation = numerotation;
        this.referentiels = referentiels;
        this.enregistreur = new Enregistreur(repository, journal, publication, referentiels, horloge);
    }

    public List<OperateurLigne> lister(FiltreOperateurs filtre) {
        return listing.lister(filtre);
    }

    public Operateur consulter(long id) {
        return repository.parId(id).orElseThrow(() -> new ElementIntrouvable("Opérateur " + id + " introuvable."));
    }

    /** La fiche, avec ce qu'elle signale (RG-OPE-02, 05, 09). */
    public FicheOperateur fiche(long id) {
        Operateur operateur = consulter(id);
        Optional<SerieCode> serieAttendue = operateur.serieAttendueSiCodeNonConforme(enFrance(operateur.identite().adresse().pays()));
        String groupe = operateur.identite().groupeNormalise();
        return new FicheOperateur(
                operateur,
                operateur.champsManquants(),
                serieAttendue.isEmpty(),
                serieAttendue.flatMap(this::suivant),
                groupe == null ? List.of() : listing.duGroupe(groupe, id));
    }

    /**
     * RG-OPE-03 : le code proposé pour un type et un code postal ; rien pour un négociant sans code
     * postal français.
     *
     * @param pays identifiant du pays ; {@code null} : la France
     */
    public Optional<CodeOperateur> proposerCode(TypeOperateur type, String codePostal, Long pays) {
        return SerieCode.pour(type, codePostal, pays == null || enFrance(pays)).flatMap(this::suivant);
    }

    /** RG-OPE-01 à 08, 20, 21, 24. Sans code saisi, le code proposé est attribué (RG-OPE-03). */
    public Operateur creer(SaisieOperateur saisie) {
        Long pays = saisie.pays() != null ? saisie.pays() : france().map(PaysVue::id).orElse(null);
        IdentiteOperateur identite = identite(saisie, pays);
        verifierReferences(identite, null);
        CodeOperateur code = Textes.facultatif(saisie.code()) != null
                ? CodeOperateur.de(saisie.code())
                : proposerCode(saisie.type(), identite.adresse().codePostal(), pays).orElseThrow(() -> new RegleEnfreinte(
                        "RG-OPE-02", "Le code opérateur est obligatoire : aucun code ne peut être proposé pour un"
                                + " négociant sans code postal français."));
        Operateur operateur = Operateur.creer(saisie.type(), code, identite);
        verifierUnicite(operateur, null);
        return repository.inserer(operateur);
    }

    /**
     * Onglet Général : identité, type et code compris (RG-OPE-01 à 09, 20, 21, 24). Changer le type ou le
     * code postal ne change pas le code (RG-OPE-05).
     */
    public Operateur modifierIdentite(long id, SaisieOperateur saisie) {
        Operateur operateur = consulter(id);
        IdentiteOperateur identite = identite(saisie, saisie.pays());
        verifierReferences(identite, operateur.identite());
        operateur.modifierIdentite(saisie.type(), CodeOperateur.de(saisie.code()), identite);
        verifierUnicite(operateur, id);
        enregistreur.enregistrer(operateur, Canal.BACK_OFFICE);
        return operateur;
    }

    /** Onglet Production (RG-OPE-01, 10 à 13, 21). */
    public Operateur modifierProduction(long id, SaisieProduction saisie) {
        Operateur operateur = consulter(id);
        verifierReference("région", saisie.region(), operateur.production().region(),
                region -> referentiels.region(region).map(v -> new Reference(v.libelle(), v.actif())));
        List<Alambic> alambics = saisie.alambics() == null ? List.of() : saisie.alambics().stream()
                .map(a -> new Alambic(a.numeroAgrement(), a.type(), a.capacite()))
                .toList();
        operateur.modifierProduction(
                new Declaratif(saisie.periodicite(), saisie.statutDeclarationStock()),
                new Production(saisie.region(), saisie.appellations(), alambics));
        enregistreur.enregistrer(operateur, Canal.BACK_OFFICE);
        return operateur;
    }

    /** Onglet Diffusion : millésimes diffusables et accès aux statistiques du portail (RG-OPE-14, 15). */
    public Operateur modifierDiffusion(long id, SaisieDiffusion saisie) {
        Operateur operateur = consulter(id);
        operateur.modifierDiffusion(DiffusionMillesimes.saisie(saisie.niveau(), saisie.millesimes()));
        operateur.modifierAccesStatistiques(saisie.accesStatistiques());
        enregistreur.enregistrer(operateur, Canal.BACK_OFFICE);
        return operateur;
    }

    /** RG-OPE-05 : attribue le code proposé pour le type et le code postal actuels (RG-OPE-03). */
    public Operateur attribuerNouveauCode(long id) {
        Operateur operateur = consulter(id);
        CodeOperateur code = proposerCode(operateur.type(), operateur.identite().adresse().codePostal(),
                operateur.identite().adresse().pays())
                .orElseThrow(() -> new RegleEnfreinte("RG-OPE-03",
                        "Aucun code ne peut être proposé : un négociant a besoin d'un code postal français."));
        operateur.recoder(code);
        verifierUnicite(operateur, id);
        enregistreur.enregistrer(operateur, Canal.BACK_OFFICE);
        return operateur;
    }

    /**
     * RG-OPE-26 : reste consultable, n'est plus proposé ; ses contacts perdent l'accès au portail
     * (événements « accès portail retiré »).
     */
    public Operateur desactiver(long id) {
        Operateur operateur = consulter(id);
        operateur.desactiver();
        enregistreur.enregistrer(operateur, Canal.BACK_OFFICE);
        return operateur;
    }

    public Operateur reactiver(long id) {
        Operateur operateur = consulter(id);
        operateur.reactiver();
        enregistreur.enregistrer(operateur, Canal.BACK_OFFICE);
        return operateur;
    }

    /** RG-OPE-25 : seulement si rien ne le référence ; sinon, le désactiver. */
    public void supprimer(long id) {
        consulter(id);
        repository.supprimer(id);
    }

    /**
     * RG-OPE-06, 08 : avertissements non bloquants pendant la saisie. Une valeur mal formée, en cours de
     * frappe, est ignorée.
     *
     * @param sauf l'opérateur modifié, ou {@code null} à la création
     */
    public List<Doublon> doublons(String raisonSociale, String cvi, String siret, String accise, Long sauf) {
        String raison = Textes.facultatif(raisonSociale);
        return listing.doublons(raison == null ? null : Textes.normalise(raison), ouRien(() -> Cvi.facultatif(cvi)),
                ouRien(() -> Siret.facultatif(siret)), ouRien(() -> NumeroAccise.facultatif(accise)), sauf);
    }

    /** RG-OPE-09 : groupes existants proposés à la saisie. */
    public List<String> groupes(String recherche) {
        return listing.groupes(Textes.facultatif(recherche));
    }

    private Optional<CodeOperateur> suivant(SerieCode serie) {
        return serie.suivant(numerotation.plusGrandNumero(serie));
    }

    private static IdentiteOperateur identite(SaisieOperateur s, Long pays) {
        return new IdentiteOperateur(
                s.groupe(),
                Cvi.facultatif(s.cvi()),
                Siret.facultatif(s.siret()),
                NumeroAccise.facultatif(s.accise()),
                s.bureauDouanes(),
                s.identifiantOdg(),
                s.titre(),
                s.raisonSociale(),
                s.nomCommercial(),
                new Adresse(s.adresseLigne1(), s.adresseLigne2(), s.adresseLigne3(), s.codePostal(), s.ville(), pays),
                s.telephone1(),
                s.telephone2(),
                s.portable(),
                Email.facultatif(s.emailGeneral(), "RG-OPE-24"),
                SiteWeb.facultatif(s.siteWeb()),
                SiteWeb.facultatif(s.siteCommercial()),
                CoordonneesGps.facultatives(s.latitude(), s.longitude()),
                s.membreBnia(),
                s.accordDiffusionGeneral(),
                s.commentaireInterne());
    }

    /**
     * RG-OPE-21 : une valeur de référentiel choisie doit exister et être active ; celle que la fiche porte
     * déjà reste acceptée, même devenue inactive.
     */
    private void verifierReferences(IdentiteOperateur nouvelle, IdentiteOperateur actuelle) {
        verifierReference("titre", nouvelle.titre(), actuelle == null ? null : actuelle.titre(),
                id -> referentiels.titre(id).map(v -> new Reference(v.libelle(), v.actif())));
        verifierReference("pays", nouvelle.adresse().pays(), actuelle == null ? null : actuelle.adresse().pays(),
                id -> referentiels.pays(id).map(v -> new Reference(v.libelle(), v.actif())));
        verifierReference("membre BNIA", nouvelle.membreBnia(), actuelle == null ? null : actuelle.membreBnia(),
                id -> referentiels.membreBnia(id).map(v -> new Reference(v.libelle(), v.actif())));
    }

    private static void verifierReference(String nom, Long choisi, Long actuel, LongFunction<Optional<Reference>> lire) {
        if (choisi == null || Objects.equals(choisi, actuel)) {
            return;
        }
        Reference reference = lire.apply(choisi).orElseThrow(() -> new RegleEnfreinte("RG-OPE-21",
                "Valeur inconnue (" + nom + ") : " + choisi + "."));
        if (!reference.actif()) {
            throw new RegleEnfreinte("RG-OPE-21",
                    "Valeur inactive (" + nom + ") : « " + reference.libelle() + " ». Choisissez une valeur active.");
        }
    }

    /** RG-OPE-04, RG-OPE-07. */
    private void verifierUnicite(Operateur operateur, Long sauf) {
        if (repository.existeCode(operateur.code(), sauf)) {
            throw new ConflitUnicite("RG-OPE-04", "Le code « " + operateur.code() + " » est déjà attribué à un autre opérateur.");
        }
        String odg = operateur.identite().identifiantOdg();
        if (odg != null && repository.existeIdentifiantOdg(odg, sauf)) {
            throw new ConflitUnicite("RG-OPE-07", "L'identifiant tiers ODG « " + odg + " » est déjà porté par un autre opérateur.");
        }
    }

    private boolean enFrance(Long pays) {
        return pays != null && referentiels.pays(pays).map(p -> FRANCE.equals(p.codeIso())).orElse(false);
    }

    private Optional<PaysVue> france() {
        return referentiels.paysActifs().stream().filter(p -> FRANCE.equals(p.codeIso())).findFirst();
    }

    private static <T> T ouRien(Supplier<T> lecture) {
        try {
            return lecture.get();
        } catch (RegleEnfreinte malForme) {
            return null;
        }
    }

    private record Reference(String libelle, boolean actif) {
    }
}
