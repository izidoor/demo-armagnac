package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Contrat d'achat Armagnac (spec 06 §3.1), agrégat : entre un acheteur et un vendeur, négocié par versions successives,
 * chacune approuvée par son auteur ; conclu quand les deux parties ont approuvé la version courante (H-CA-1). Conclu,
 * il reçoit des retiraisons, jusqu'à son exécution ou son annulation (RG-CA-13 à 17).
 */
public final class ContratAchatArmagnac {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Long id;
    private final String numero;
    private final long acheteur;
    private final long vendeur;
    private final Partie ouvertPar;
    private EtatContrat etat;
    private LocalDate dateContrat;
    private final List<VersionContrat> versions;
    private final List<Approbation> approbations;
    private final List<Retiraison> retiraisons;
    private int retiraisonsNumerotees;
    private final Long version;
    private final List<Fait> faits = new ArrayList<>();

    private ContratAchatArmagnac(Long id, String numero, long acheteur, long vendeur, Partie ouvertPar,
            EtatContrat etat, LocalDate dateContrat, List<VersionContrat> versions, List<Approbation> approbations,
            List<Retiraison> retiraisons, int retiraisonsNumerotees, Long version) {
        this.id = id;
        this.numero = Objects.requireNonNull(numero, "numero");
        this.acheteur = acheteur;
        this.vendeur = vendeur;
        this.ouvertPar = Objects.requireNonNull(ouvertPar, "ouvertPar");
        this.etat = Objects.requireNonNull(etat, "etat");
        this.dateContrat = dateContrat;
        this.versions = new ArrayList<>(versions);
        this.approbations = new ArrayList<>(approbations);
        this.retiraisons = new ArrayList<>(retiraisons);
        this.retiraisonsNumerotees = retiraisonsNumerotees;
        this.version = version;
        if (this.versions.isEmpty()) {
            throw new IllegalArgumentException("Un contrat a au moins une version.");
        }
    }

    /**
     * RG-CA-02, 06 : ouvre le projet ; la version 1 est approuvée par son auteur.
     *
     * @throws RegleEnfreinte (RG-CA-02) si l'acheteur et le vendeur sont le même opérateur
     */
    public static ContratAchatArmagnac ouvrir(String numero, long acheteur, long vendeur, Auteur auteur,
            ContenuContrat contenu) {
        if (acheteur == vendeur) {
            throw new RegleEnfreinte("RG-CA-02", "L'acheteur et le vendeur sont deux opérateurs distincts.");
        }
        ContratAchatArmagnac contrat = new ContratAchatArmagnac(null, numero, acheteur, vendeur, auteur.partie(),
                EtatContrat.PROJET, null, List.of(new VersionContrat(1, auteur, contenu)),
                List.of(new Approbation(1, auteur)), List.of(), 0, null);
        contrat.faits.add(new Fait(ActionContrat.OUVERTURE, auteur, 1));
        return contrat;
    }

    /** @param retiraisonsNumerotees le numéro de la dernière retiraison déclarée, même retirée depuis */
    public static ContratAchatArmagnac reconstituer(long id, String numero, long acheteur, long vendeur,
            Partie ouvertPar, EtatContrat etat, LocalDate dateContrat, List<VersionContrat> versions,
            List<Approbation> approbations, List<Retiraison> retiraisons, int retiraisonsNumerotees, long version) {
        return new ContratAchatArmagnac(id, numero, acheteur, vendeur, ouvertPar, etat, dateContrat, versions,
                approbations, retiraisons, retiraisonsNumerotees, version);
    }

    /** RG-CA-07 : une nouvelle version, approuvée par son auteur seul ; l'approbation de l'autre partie tombe. */
    public void modifier(Auteur auteur, int versionVue, ContenuContrat contenu) {
        exigerProjet("RG-CA-07", "modifié");
        exigerVersionVue(versionVue);
        int numeroVersion = courante().numero() + 1;
        versions.add(new VersionContrat(numeroVersion, auteur, contenu));
        approbations.add(new Approbation(numeroVersion, auteur));
        faits.add(new Fait(ActionContrat.MODIFICATION, auteur, numeroVersion));
    }

    /**
     * RG-CA-08 : la partie dont l'approbation manque approuve la version courante ; les deux approbations sur la même
     * version concluent le contrat, daté du jour.
     */
    public void approuver(Auteur auteur, int versionVue) {
        exigerProjet("RG-CA-08", "approuvé");
        exigerVersionVue(versionVue);
        if (partieQuiDoitApprouver().filter(auteur.partie()::equals).isEmpty()) {
            throw new RegleEnfreinte("RG-CA-08", "Vous avez déjà approuvé la version " + courante().numero()
                    + " du contrat " + numero + " : elle attend l'autre partie.");
        }
        approbations.add(new Approbation(courante().numero(), auteur));
        faits.add(new Fait(ActionContrat.APPROBATION, auteur, courante().numero()));
        etat = EtatContrat.CONCLU;
        dateContrat = LocalDate.ofInstant(auteur.le(), PARIS);
        faits.add(new Fait(ActionContrat.CONCLUSION, auteur, courante().numero()));
    }

    /** RG-CA-10 : la partie qui doit approuver refuse le projet, définitivement ; l'auteur de la version la modifie. */
    public void refuser(Auteur auteur, int versionVue) {
        exigerProjet("RG-CA-10", "refusé");
        exigerVersionVue(versionVue);
        if (partieQuiDoitApprouver().filter(auteur.partie()::equals).isEmpty()) {
            throw new RegleEnfreinte("RG-CA-10", "Vous êtes l'auteur de la version courante du contrat " + numero
                    + " : modifiez-la plutôt que de la refuser.");
        }
        etat = EtatContrat.REFUSE;
        faits.add(new Fait(ActionContrat.REFUS, auteur, courante().numero()));
    }

    /**
     * RG-CA-13 : sur un contrat conclu, l'acheteur déclare une retiraison, approuvée par lui ; elle attend le vendeur.
     * Date d'enlèvement au plus tôt aujourd'hui ; chaque volume au plus le reste à retirer de sa ligne. Le stock
     * estimé du vendeur est contrôlé par le cas d'usage.
     */
    public Retiraison declarerRetiraison(Auteur auteur, ContenuRetiraison contenu, LocalDate aujourdhui) {
        exigerConclu("RG-CA-13", "recevoir de retiraison");
        if (auteur.partie() != Partie.ACHETEUR) {
            throw new RegleEnfreinte("RG-CA-13", "Seul l'acheteur déclare une retiraison.");
        }
        exigerContenu("RG-CA-13", contenu, aujourdhui, null);
        Retiraison retiraison = Retiraison.declarer(++retiraisonsNumerotees, auteur, contenu);
        retiraisons.add(retiraison);
        faits.add(new Fait(ActionContrat.RETIRAISON_DECLAREE, auteur, retiraison.numero()));
        return retiraison;
    }

    /**
     * RG-CA-14 : la partie qui doit valider une retiraison la modifie (mêmes contrôles) : nouvelle version, approuvée par
     * elle seule, à valider par l'autre.
     */
    public void modifierRetiraison(Auteur auteur, int numeroRetiraison, int versionVue, ContenuRetiraison contenu,
            LocalDate aujourdhui) {
        Retiraison retiraison = retiraisonAttendue("RG-CA-14", auteur, numeroRetiraison, versionVue, "modifier");
        exigerContenu("RG-CA-14", contenu, aujourdhui, numeroRetiraison);
        retiraison.modifier(auteur, contenu);
        faits.add(new Fait(ActionContrat.RETIRAISON_MODIFIEE, auteur, numeroRetiraison));
    }

    /**
     * RG-CA-15 : la partie qui doit valider une retiraison la valide, au plus tard le jour de l'enlèvement ; la
     * dernière exécute le contrat, et les autres retiraisons non validées tombent. Le stock estimé du vendeur est
     * contrôlé par le cas d'usage.
     */
    public void validerRetiraison(Auteur auteur, int numeroRetiraison, int versionVue, LocalDate aujourdhui) {
        Retiraison retiraison = retiraisonAttendue("RG-CA-15", auteur, numeroRetiraison, versionVue, "valider");
        LocalDate date = retiraison.courante().contenu().dateEnlevement();
        if (date.isBefore(aujourdhui)) {
            throw new RegleEnfreinte("RG-CA-15", "La retiraison n° " + numeroRetiraison + " était prévue le "
                    + date.format(JOUR) + " : elle se valide au plus tard le jour de l'enlèvement. Modifiez sa date.");
        }
        retiraison.valider(auteur);
        faits.add(new Fait(ActionContrat.RETIRAISON_VALIDEE, auteur, numeroRetiraison));
        if (retiraison.courante().contenu().derniere()) {
            etat = EtatContrat.EXECUTE;
            retiraisons.removeIf(r -> !r.validee());
            faits.add(new Fait(ActionContrat.EXECUTION, auteur, numeroRetiraison));
        }
    }

    /** RG-CA-16 : l'acheteur retire une retiraison non validée ; elle est supprimée, le journal le garde. */
    public void retirerRetiraison(Auteur auteur, int numeroRetiraison) {
        exigerConclu("RG-CA-16", "changer de retiraison");
        if (auteur.partie() != Partie.ACHETEUR) {
            throw new RegleEnfreinte("RG-CA-16", "Seul l'acheteur retire une retiraison.");
        }
        Retiraison retiraison = retiraison(numeroRetiraison);
        if (retiraison.validee()) {
            throw new RegleEnfreinte("RG-CA-16", "La retiraison n° " + numeroRetiraison
                    + " est validée : elle ne se retire plus.");
        }
        retiraisons.remove(retiraison);
        faits.add(new Fait(ActionContrat.RETIRAISON_RETIREE, auteur, numeroRetiraison));
    }

    /** RG-CA-17 : une partie annule le contrat conclu ; ses retiraisons non validées tombent, les validées restent. */
    public void annuler(Auteur auteur) {
        exigerConclu("RG-CA-17", "être annulé");
        clore(EtatContrat.ANNULE, new Fait(ActionContrat.ANNULATION, auteur, null));
    }

    /** RG-CA-17 : une partie déclare que le contrat conclu n'aura plus de retiraison : il est exécuté. */
    public void cloturer(Auteur auteur) {
        exigerConclu("RG-CA-17", "être clos");
        clore(EtatContrat.EXECUTE, new Fait(ActionContrat.CLOTURE, auteur, null));
    }

    private void clore(EtatContrat nouvelEtat, Fait fait) {
        etat = nouvelEtat;
        retiraisons.removeIf(r -> !r.validee());
        faits.add(fait);
    }

    /**
     * RG-CA-13 : le reste à retirer d'une ligne du contrat, en hl : son volume moins ses retiraisons validées ou en
     * cours, sauf celle qui est modifiée.
     *
     * @param sauf numéro de la retiraison à ne pas compter ; {@code null} : toutes
     */
    public BigDecimal resteARetirer(int rang, Integer sauf) {
        BigDecimal retire = retiraisons.stream()
                .filter(r -> sauf == null || r.numero() != sauf)
                .flatMap(r -> r.courante().contenu().lignes().stream())
                .filter(l -> l.rang() == rang)
                .map(LigneRetiraison::volume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return courante().contenu().lignes().get(rang).volume().subtract(retire);
    }

    /**
     * RG-CA-13, 15 : le volume d'alcool pur d'une retiraison par grain, à une date (compte d'un assemblage vieilli
     * depuis la conclusion, H-CA-11), lignes du même grain cumulées.
     */
    public Map<Grain, VolumeAlcoolPur> volumeApParGrain(ContenuRetiraison contenu, LocalDate date) {
        Map<Grain, VolumeAlcoolPur> parGrain = new LinkedHashMap<>();
        contenu.lignes().forEach(l -> {
            LigneContrat ligne = courante().contenu().lignes().get(l.rang());
            parGrain.merge(ligne.grainAu(dateContrat, date), ligne.volumeAp(l.volume()), VolumeAlcoolPur::plus);
        });
        return parGrain;
    }

    public Retiraison retiraison(int numeroRetiraison) {
        return retiraisons.stream().filter(r -> r.numero() == numeroRetiraison).findFirst()
                .orElseThrow(() -> new ElementIntrouvable("Retiraison n° " + numeroRetiraison + " introuvable dans le"
                        + " contrat " + numero + "."));
    }

    private Retiraison retiraisonAttendue(String regle, Auteur auteur, int numeroRetiraison, int versionVue,
            String action) {
        exigerConclu(regle, "changer de retiraison");
        Retiraison retiraison = retiraison(numeroRetiraison);
        if (retiraison.validee()) {
            throw new RegleEnfreinte(regle, "La retiraison n° " + numeroRetiraison + " est déjà validée.");
        }
        if (versionVue != retiraison.courante().numero()) {
            throw new ModificationConcurrente(null, "La retiraison n° " + numeroRetiraison + " a été modifiée"
                    + " entre-temps (version " + retiraison.courante().numero() + ") : rechargez l'écran.");
        }
        if (retiraison.partieQuiDoitValider().filter(auteur.partie()::equals).isEmpty()) {
            throw new RegleEnfreinte(regle, "La retiraison n° " + numeroRetiraison + " attend l'autre partie : vous"
                    + " ne pouvez pas la " + action + ".");
        }
        return retiraison;
    }

    /** RG-CA-13, 14 : des lignes du contrat, une date au plus tôt aujourd'hui, au plus le reste à retirer. */
    private void exigerContenu(String regle, ContenuRetiraison contenu, LocalDate aujourdhui, Integer sauf) {
        if (contenu.dateEnlevement().isBefore(aujourdhui)) {
            throw new RegleEnfreinte(regle, "La date d'enlèvement est au plus tôt aujourd'hui.");
        }
        List<LigneContrat> lignes = courante().contenu().lignes();
        for (LigneRetiraison l : contenu.lignes()) {
            if (l.rang() < 0 || l.rang() >= lignes.size()) {
                throw new RegleEnfreinte(regle, "La ligne n° " + (l.rang() + 1) + " n'est pas une ligne du contrat "
                        + numero + ".");
            }
            BigDecimal reste = resteARetirer(l.rang(), sauf);
            if (l.volume().compareTo(reste) > 0) {
                throw new RegleEnfreinte(regle, "Ligne « " + lignes.get(l.rang()).libelle() + " » : "
                        + l.volume().toPlainString().replace('.', ',') + " hl demandés, il en reste "
                        + reste.toPlainString().replace('.', ',') + " hl à retirer.");
            }
        }
    }

    private void exigerConclu(String regle, String action) {
        if (etat == EtatContrat.PROJET) {
            throw new RegleEnfreinte(regle, "Le contrat " + numero + " n'est pas encore conclu : il ne peut pas "
                    + action + ".");
        }
        if (etat != EtatContrat.CONCLU) {
            throw new RegleEnfreinte(regle, "Le contrat " + numero + " est " + etat.libelle().toLowerCase()
                    + " : il ne peut plus " + action + ".");
        }
    }

    private void exigerProjet(String regle, String action) {
        if (etat != EtatContrat.PROJET) {
            throw new RegleEnfreinte(regle, "Le contrat " + numero + " est " + etat.libelle().toLowerCase()
                    + " : il ne peut plus être " + action + ".");
        }
    }

    private void exigerVersionVue(int versionVue) {
        if (versionVue != courante().numero()) {
            throw new ModificationConcurrente(null, "Le contrat " + numero + " a été modifié entre-temps (version "
                    + courante().numero() + ") : rechargez l'écran pour voir ce qui a changé.");
        }
    }

    /** RG-CA-08 : en projet, la partie qui n'a pas approuvé la version courante. */
    public Optional<Partie> partieQuiDoitApprouver() {
        if (etat != EtatContrat.PROJET) {
            return Optional.empty();
        }
        int courante = courante().numero();
        for (Partie partie : Partie.values()) {
            if (approbations.stream().noneMatch(a -> a.partie() == partie && a.version() == courante)) {
                return Optional.of(partie);
            }
        }
        return Optional.empty();
    }

    /** L'approbation de la version courante par cette partie, s'il y en a une. */
    public Optional<Approbation> approbationCourante(Partie partie) {
        int courante = courante().numero();
        return approbations.stream().filter(a -> a.partie() == partie && a.version() == courante).findFirst();
    }

    /**
     * RG-CA-09 : l'écart entre la dernière version approuvée par la partie (la version 1 si elle n'en a approuvé
     * aucune) et la courante, avec les auteurs des versions intermédiaires.
     */
    public Ecart ecartPour(Partie partie) {
        int depuis = approbations.stream().filter(a -> a.partie() == partie).mapToInt(Approbation::version).max()
                .orElse(1);
        VersionContrat courante = courante();
        List<Auteur> auteurs = versions.stream().filter(v -> v.numero() > depuis).map(VersionContrat::auteur)
                .toList();
        return Ecart.entre(versionNumero(depuis), courante, auteurs);
    }

    private VersionContrat versionNumero(int numeroVersion) {
        return versions.stream().filter(v -> v.numero() == numeroVersion).findFirst().orElseThrow();
    }

    /** La partie que tient cet opérateur dans le contrat ; vide s'il n'y est pas partie. */
    public Optional<Partie> partieDe(long operateur) {
        return operateur == acheteur ? Optional.of(Partie.ACHETEUR)
                : operateur == vendeur ? Optional.of(Partie.VENDEUR) : Optional.empty();
    }

    public long operateur(Partie partie) {
        return partie == Partie.ACHETEUR ? acheteur : vendeur;
    }

    /** Les faits produits depuis le dernier relevé, pour le journal et les e-mails ; la liste est vidée. */
    public List<Fait> releverFaits() {
        List<Fait> releves = List.copyOf(faits);
        faits.clear();
        return releves;
    }

    /** RG-CA-08, H-CA-6 : la campagne, à la date du contrat, sinon à la date donnée (le jour, pour un projet). */
    public String campagne(LocalDate aujourdhui) {
        return Campagne.de(dateContrat != null ? dateContrat : aujourdhui);
    }

    public VersionContrat courante() {
        return versions.getLast();
    }

    public Long id() {
        return id;
    }

    public String numero() {
        return numero;
    }

    public long acheteur() {
        return acheteur;
    }

    public long vendeur() {
        return vendeur;
    }

    public Partie ouvertPar() {
        return ouvertPar;
    }

    public EtatContrat etat() {
        return etat;
    }

    public Optional<LocalDate> dateContrat() {
        return Optional.ofNullable(dateContrat);
    }

    public List<VersionContrat> versions() {
        return List.copyOf(versions);
    }

    public List<Approbation> approbations() {
        return List.copyOf(approbations);
    }

    /** Les retiraisons, dans l'ordre de leur déclaration. */
    public List<Retiraison> retiraisons() {
        return List.copyOf(retiraisons);
    }

    public int retiraisonsNumerotees() {
        return retiraisonsNumerotees;
    }

    public Long version() {
        return version;
    }
}
