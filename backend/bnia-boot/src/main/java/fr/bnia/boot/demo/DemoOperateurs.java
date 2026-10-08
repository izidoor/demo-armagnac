package fr.bnia.boot.demo;

import fr.bnia.authentification.application.GererMotDePasse;
import fr.bnia.commun.domain.Appellation;
import fr.bnia.operateurs.application.GererContacts;
import fr.bnia.operateurs.application.GererInvitations;
import fr.bnia.operateurs.application.GererMembres;
import fr.bnia.operateurs.application.GererOperateurs;
import fr.bnia.operateurs.application.GererPresentation;
import fr.bnia.operateurs.application.RattachementRepository;
import fr.bnia.operateurs.application.SaisieContact;
import fr.bnia.operateurs.application.SaisieDesignations;
import fr.bnia.operateurs.application.SaisieDiffusion;
import fr.bnia.operateurs.application.SaisieInvitation;
import fr.bnia.operateurs.application.SaisieOperateur;
import fr.bnia.operateurs.application.SaisiePresentation;
import fr.bnia.operateurs.application.SaisieProduction;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.CarteBancaire;
import fr.bnia.operateurs.domain.Distinction;
import fr.bnia.operateurs.domain.ModePresentation;
import fr.bnia.operateurs.domain.Portee;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.TypeAlambic;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Données de démonstration des opérateurs (spec 02, §7.1), en profil {@code demo} seulement, après le
 * personnel du BNIA et les référentiels : une quinzaine de fiches plausibles du Gers, des Landes et du
 * Lot-et-Garonne, présentées comme telles, et les personnes et accès de la Maison Durand et de la
 * Distillerie Lafitte (comptes dans {@code docs/demo.md}).
 *
 * <p>Tout passe par les cas d'usage, au nom d'Anne Lamarque, ADMIN du BNIA. Une fiche dont le code existe
 * déjà n'est pas recréée : le chargeur est relançable.
 */
@Component
@Profile("demo")
@Order(3)
public class DemoOperateurs implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoOperateurs.class);
    private static final String EMAIL_DURAND = "admin@producteur.fr";

    private final GererOperateurs operateurs;
    private final GererContacts contacts;
    private final GererPresentation presentation;
    private final GererInvitations invitations;
    private final GererMembres membres;
    private final RattachementRepository rattachements;
    private final OperateursLecture lecture;
    private final ReferentielsLecture referentiels;
    private final ComptesDemo comptes;

    private int crees;
    private Acteur anne;

    public DemoOperateurs(GererOperateurs operateurs, GererContacts contacts, GererPresentation presentation,
            GererInvitations invitations, GererMembres membres, GererMotDePasse motsDePasse,
            RattachementRepository rattachements, OperateursLecture lecture, ReferentielsLecture referentiels) {
        this.operateurs = operateurs;
        this.contacts = contacts;
        this.presentation = presentation;
        this.invitations = invitations;
        this.membres = membres;
        this.rattachements = rattachements;
        this.lecture = lecture;
        this.referentiels = referentiels;
        this.comptes = new ComptesDemo(invitations, motsDePasse, rattachements, lecture);
    }

    @Override
    public void run(ApplicationArguments arguments) {
        crees = 0;
        anne = new Acteur(DemoPersonnelBnia.anne(lecture));
        charger();
        LOG.info("Démo : {} opérateurs créés.", crees);
    }

    void charger() {
        maisonDurand();
        producteur("000003", "Domaine de Laubade", "Château de Laubade", "32110", "Sorbets", "GERS",
                TypeAlambic.ARMAGNACAIS, Appellation.BAS_ARMAGNAC);
        producteur("000004", "Domaine Boingnères", null, "40240", "Le Frêche", "LANDES", TypeAlambic.ARMAGNACAIS,
                Appellation.BAS_ARMAGNAC);
        producteur("000005", "Château de Pellehaut", null, "32250", "Montréal", "GERS", TypeAlambic.DOUBLE_CHAUFFE,
                Appellation.ARMAGNAC_TENAREZE);
        producteur("000006", "Domaine d'Ognoas", null, "40190", "Arthez-d'Armagnac", "LANDES",
                TypeAlambic.ARMAGNACAIS, Appellation.BAS_ARMAGNAC);
        producteur("000007", "Château du Tariquet", "Tariquet", "32800", "Eauze", "GERS", TypeAlambic.ARMAGNACAIS,
                Appellation.BAS_ARMAGNAC);
        producteur("000008", "Domaine de Jaulin", null, "47600", "Nérac", "LOT-ET-GARONNE", null,
                Appellation.ARMAGNAC_TENAREZE);
        producteur("000009", "Domaine du Haut-Armagnac", null, "32700", "Lectoure", "GERS", null,
                Appellation.HAUT_ARMAGNAC);
        negociant("N320001", "Armagnacs de Gascogne", "32100", "Condom");
        armagnacsDeGascogne();
        negociant("N320002", "Maison Larressingle", "32100", "Condom");
        negociant("N400001", "Négoce des Landes", "40000", "Mont-de-Marsan");
        distillerieLafitte();
        autre("000011", "Courtage Sud-Ouest", "Courtier", "47600", "Nérac");
        incomplete();
        inactive();
    }

    /**
     * Groupe « Maison Durand », deux chais (Condom et Eauze). Jeanne Durand ({@code admin@producteur.fr}),
     * ADMIN des deux, opérateur actif Condom ; Sophie Martin ({@code agent@producteur.fr}), AGENT à Condom ;
     * Pierre Roy ({@code observateur@producteur.fr}), OBSERVATEUR à Condom et à Eauze, suspendu à Eauze ;
     * Paul Petit, maître de chai sans compte, contact principal à Condom.
     */
    private void maisonDurand() {
        Optional<Long> condom = creerSiAbsent(saisie(TypeOperateur.PRODUCTEUR, "000001", "Maison Durand, chai de Condom",
                "Maison Durand", "Maison Durand", "12 route d'Agen", "32100", "Condom", "05 62 28 00 01"));
        condom.ifPresent(id -> {
            production(id, PeriodiciteDsm.ANNUELLE, "GERS", List.of(alambic("A-32-0142", TypeAlambic.ARMAGNACAIS, "25")),
                    Appellation.ARMAGNAC, Appellation.BAS_ARMAGNAC, Appellation.ARMAGNAC_TENAREZE);
            operateurs.modifierDiffusion(anne, id, new SaisieDiffusion(NiveauDiffusion.PARTIEL, List.of(1995, 2005, 2010),
                    true, null));
            presentation.modifier(anne, id, presentationComplete());
        });
        Optional<Long> eauze = creerSiAbsent(saisie(TypeOperateur.PRODUCTEUR, "000002", "Maison Durand, chai d'Eauze",
                null, "Maison Durand", "3 avenue de Nogaro", "32800", "Eauze", "05 62 09 00 02"));
        eauze.ifPresent(id -> production(id, PeriodiciteDsm.TRIMESTRIELLE, "GERS",
                List.of(alambic(null, TypeAlambic.DOUBLE_CHAUFFE, "12.5")), Appellation.BAS_ARMAGNAC));
        if (condom.isEmpty() || eauze.isEmpty()) {
            return;
        }
        long idCondom = condom.get();
        long idEauze = eauze.get();
        long jeanne = comptes.membre(anne, idCondom, Role.ADMIN, EMAIL_DURAND, "Durand", "Jeanne", "Gérante");
        comptes.membre(anne, idEauze, Role.ADMIN, EMAIL_DURAND, "Durand", "Jeanne", "Gérante");
        Acteur durand = new Acteur(jeanne);
        comptes.membre(durand, idCondom, Role.AGENT, "agent@producteur.fr", "Martin", "Sophie", "Comptable");
        comptes.membre(durand, idCondom, Role.OBSERVATEUR, "observateur@producteur.fr", "Roy", "Pierre", "Caviste");
        long pierre = comptes.membre(durand, idEauze, Role.OBSERVATEUR, "observateur@producteur.fr", "Roy",
                "Pierre", "Caviste");
        membres.suspendre(durand, idEauze, rattachements.de(idEauze, pierre).map(Rattachement::id).orElseThrow());
        long paul = contacts.creer(durand, idCondom, contact("M", "Petit", "Paul", "Maître de chai",
                "paul.petit@maison-durand.example", "PRODUCTION")).fiche();
        long ficheJeanneCondom = rattachements.de(idCondom, jeanne).orElseThrow().fiche();
        long ficheJeanneEauze = rattachements.de(idEauze, jeanne).orElseThrow().fiche();
        operateurs.designer(durand, idCondom, new SaisieDesignations(paul, ficheJeanneCondom, null, null));
        operateurs.designer(durand, idEauze, new SaisieDesignations(ficheJeanneEauze, ficheJeanneEauze, null, null));
    }

    /**
     * Armagnacs de Gascogne, négociant : Henri Castex ({@code admin@negociant.fr}, ADMIN), Julie Darrigade
     * ({@code agent@negociant.fr}, AGENT), Thomas Barrère ({@code observateur@negociant.fr}, OBSERVATEUR).
     * Relançable : un membre existant est laissé tel quel.
     */
    private void armagnacsDeGascogne() {
        long id = lecture.operateurParCode("N320001").orElseThrow().id();
        Acteur castex = new Acteur(comptes.membre(anne, id, Role.ADMIN, "admin@negociant.fr", "Castex", "Henri",
                "Gérant"));
        comptes.membre(castex, id, Role.AGENT, "agent@negociant.fr", "Darrigade", "Julie", "Responsable des achats");
        comptes.membre(castex, id, Role.OBSERVATEUR, "observateur@negociant.fr", "Barrère", "Thomas", "Commercial");
    }

    /** Distillerie ambulante Lafitte : Marc Lafitte, invité comme ADMIN, n'a pas encore accepté. */
    private void distillerieLafitte() {
        creerSiAbsent(saisie(TypeOperateur.AUTRE, "000010", "Distillerie ambulante Lafitte", null, null,
                "1 place de la Mairie", "32250", "Montréal", null))
                .ifPresent(id -> {
                    production(id, null, "GERS", List.of());
                    invitations.inviter(anne, id, new SaisieInvitation(null, "marc.lafitte@distillerie-lafitte.example",
                            Role.ADMIN, referentiels.titreParCode("M").map(ValeurVue::id).orElse(null), "Lafitte",
                            "Marc", "Distillateur", "06 12 34 56 78"));
                });
    }

    private void producteur(String code, String raisonSociale, String nomCommercial, String codePostal, String ville,
            String region, TypeAlambic alambic, Appellation appellation) {
        creerSiAbsent(saisie(TypeOperateur.PRODUCTEUR, code, raisonSociale, nomCommercial, null, "Lieu-dit "
                + raisonSociale.replace("Domaine ", "").replace("Château ", ""), codePostal, ville, null))
                .ifPresent(id -> {
                    production(id, PeriodiciteDsm.ANNUELLE, region,
                            alambic == null ? List.of() : List.of(alambic(null, alambic, "20")), appellation);
                    contacts.creer(anne, id, contact("M", "Responsable", null, "Chef d'exploitation",
                            "contact@" + code + ".example", "PRODUCTION"));
                });
    }

    private void negociant(String code, String raisonSociale, String codePostal, String ville) {
        creerSiAbsent(saisie(TypeOperateur.NEGOCIANT, code, raisonSociale, null, null, "Zone d'activités",
                codePostal, ville, null))
                .ifPresent(id -> {
                    production(id, PeriodiciteDsm.MENSUELLE, codePostal.startsWith("40") ? "LANDES" : "GERS", List.of(),
                            Appellation.ARMAGNAC, Appellation.BLANCHE_ARMAGNAC);
                    operateurs.modifierDiffusion(anne, id, new SaisieDiffusion(NiveauDiffusion.TOTAL, List.of(), false,
                            null));
                });
    }

    private void autre(String code, String raisonSociale, String fonction, String codePostal, String ville) {
        creerSiAbsent(saisie(TypeOperateur.AUTRE, code, raisonSociale, null, null, "1 place de la Mairie",
                codePostal, ville, null))
                .ifPresent(id -> {
                    production(id, null, codePostal.startsWith("47") ? "LOT-ET-GARONNE" : "GERS", List.of());
                    contacts.creer(anne, id, contact("M", "Lafitte", "Henri", fonction, null, null));
                });
    }

    /** Une fiche incomplète : sans adresse ni région (RG-OPE-02). */
    private void incomplete() {
        creerSiAbsent(saisie(TypeOperateur.PRODUCTEUR, "000012", "Domaine à compléter", null, null, null, null, null,
                null));
    }

    /** Une fiche inactive (RG-OPE-26). */
    private void inactive() {
        creerSiAbsent(saisie(TypeOperateur.PRODUCTEUR, "000013", "Ancien domaine de Barbotan", null, null,
                "Route des thermes", "32150", "Cazaubon", null))
                .ifPresent(id -> {
                    production(id, PeriodiciteDsm.ANNUELLE, "GERS", List.of(), Appellation.BAS_ARMAGNAC);
                    operateurs.desactiver(anne, id);
                });
    }

    private SaisiePresentation presentationComplete() {
        long marque1 = valeur(referentiels.marqueParCode("DEMO-1"));
        long marque2 = valeur(referentiels.marqueParCode("DEMO-2"));
        return new SaisiePresentation(
                List.of(new SaisiePresentation.Marque(Portee.FRANCE, marque1, true),
                        new SaisiePresentation.Marque(Portee.EXPORT, marque1, true),
                        new SaisiePresentation.Marque(Portee.EXPORT, marque2, false)),
                List.of(new SaisiePresentation.Circuit(Portee.FRANCE, valeur(referentiels.circuitDistributionParCode("DIRECT"))),
                        new SaisiePresentation.Circuit(Portee.FRANCE, valeur(referentiels.circuitDistributionParCode("CAVISTES"))),
                        new SaisiePresentation.Circuit(Portee.EXPORT, valeur(referentiels.circuitDistributionParCode("EXPORT")))),
                List.of(new SaisiePresentation.Visite(valeur(referentiels.typeVisiteParCode("CHAI")), "Sur rendez-vous"),
                        new SaisiePresentation.Visite(valeur(referentiels.typeVisiteParCode("DEGUST")), null)),
                List.of(new SaisiePresentation.Label(valeur(referentiels.labelParCode("HVE")), "Depuis 2021")),
                true, Set.of(CarteBancaire.VISA, CarteBancaire.MASTERCARD), true, true, false, true,
                Set.of(valeur(referentiels.langueParCode("FR")), valeur(referentiels.langueParCode("EN"))),
                Set.of(valeur(referentiels.produitCommercialiseParCode("ARMAGNAC")),
                        valeur(referentiels.produitCommercialiseParCode("FLOC"))),
                ModePresentation.ARMAGNAC_ET_AUTRES_PRODUITS,
                "Maison familiale depuis 1920, deux chais en Ténarèze et Bas-Armagnac.",
                "Family house since 1920, with two cellars in Ténarèze and Bas-Armagnac.",
                List.of(new SaisiePresentation.Medaille("Concours général agricole", 2024, Distinction.OR, "Hors d'âge"),
                        new SaisiePresentation.Medaille("Spirits Selection", 2023, Distinction.ARGENT, "VSOP")));
    }

    private void production(long id, PeriodiciteDsm periodicite, String region, List<SaisieProduction.Alambic> alambics,
            Appellation... appellations) {
        StatutDeclarationStock statut = periodicite == null ? StatutDeclarationStock.NON : StatutDeclarationStock.OUI;
        operateurs.modifierProduction(anne, id, new SaisieProduction(periodicite, statut,
                referentiels.regionParCode(region).map(r -> r.id()).orElse(null), Set.of(appellations), alambics, null));
    }

    private static SaisieProduction.Alambic alambic(String agrement, TypeAlambic type, String capacite) {
        return new SaisieProduction.Alambic(agrement, type, new BigDecimal(capacite));
    }

    private SaisieOperateur saisie(TypeOperateur type, String code, String raisonSociale, String nomCommercial,
            String groupe, String adresse, String codePostal, String ville, String telephone) {
        Long france = adresse == null ? null : referentiels.paysParCode("FR").map(p -> p.id()).orElse(null);
        String email = adresse == null ? null : "accueil@" + code.toLowerCase() + ".example";
        return new SaisieOperateur(type, code, groupe, null, null, null, null, null, null, raisonSociale,
                nomCommercial, adresse, null, null, codePostal, ville, france, telephone, null, null, email, null,
                null, null, null, referentiels.membreBniaParCode(type == TypeOperateur.NEGOCIANT ? "NEGO" : "PROD")
                        .map(ValeurVue::id).orElse(null),
                type != TypeOperateur.AUTRE, null, null);
    }

    private SaisieContact contact(String titre, String nom, String prenom, String fonction, String email,
            String centreInteret) {
        Set<Long> centres = centreInteret == null ? Set.of()
                : referentiels.centreInteretParCode(centreInteret).map(c -> Set.of(c.id())).orElse(Set.of());
        return new SaisieContact(referentiels.titreParCode(titre).map(ValeurVue::id).orElse(null), nom, prenom, email,
                fonction, null, null, null, null, null, null, centres, null);
    }

    private static long valeur(Optional<ValeurVue> valeur) {
        return valeur.orElseThrow(() -> new IllegalStateException("Référentiel de démonstration manquant.")).id();
    }

    /** L'identifiant de l'opérateur créé ; rien s'il existait déjà. */
    private Optional<Long> creerSiAbsent(SaisieOperateur saisie) {
        if (lecture.operateurParCode(saisie.code()).isPresent()) {
            return Optional.empty();
        }
        crees++;
        return Optional.of(operateurs.creer(anne, saisie, null).id());
    }
}
