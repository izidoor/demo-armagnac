package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.ControleStock;
import fr.bnia.contratsarmagnac.domain.Ecart;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.Mercuriale;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.declarations.application.partage.DeclarationsLecture;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.StockEstimeVue.LigneStockEstimeVue;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Consultation des contrats (UC-CA-8, 9) : listes du portail et du back-office, détail avec l'écart et le journal,
 * stock sous bois du vendeur pour la saisie (UC-CA-6), mercuriales (UC-CA-10). RG-CA-01, 09, 11, 19, 25, 26.
 */
@UseCase
public class ConsulterContrats {

    private final ContratRepository contrats;
    private final ContratsLecture lecture;
    private final JournalContrats journal;
    private final OperateursLecture operateurs;
    private final DeclarationsLecture declarations;
    private final Autorisations autorisations;
    private final Clock horloge;

    public ConsulterContrats(ContratRepository contrats, ContratsLecture lecture, JournalContrats journal,
            OperateursLecture operateurs, DeclarationsLecture declarations, Clock horloge) {
        this.contrats = contrats;
        this.lecture = lecture;
        this.journal = journal;
        this.operateurs = operateurs;
        this.declarations = declarations;
        this.autorisations = new Autorisations(operateurs);
        this.horloge = horloge;
    }

    /** RG-CA-26 : les mercuriales d'une période, bornes comprises. */
    public record Mercuriales(LocalDate du, LocalDate au, List<Mercuriale> produits) {
    }

    /**
     * RG-CA-01, 26 : les mercuriales des contrats conclus ou exécutés de la période, pour le personnel du BNIA ; par
     * défaut, les 12 derniers mois.
     */
    public Mercuriales mercuriales(Acteur acteur, Optional<LocalDate> du, Optional<LocalDate> au) {
        autorisations.exigerPersonnelBnia(acteur);
        LocalDate fin = au.orElseGet(() -> LocalDate.now(horloge));
        LocalDate debut = du.orElseGet(() -> fin.minusMonths(12).plusDays(1));
        if (debut.isAfter(fin)) {
            throw new RegleEnfreinte("RG-CA-26", "La période commence après sa fin.");
        }
        return new Mercuriales(debut, fin, Mercuriale.calculer(lecture.lignesConclues(debut, fin)));
    }

    /** RG-CA-25 : les onglets de la liste du portail, vus par une partie. */
    public enum Onglet {
        A_APPROUVER, EN_NEGOCIATION, CONCLUS, EXECUTES, ANNULES_REFUSES
    }

    /**
     * Un contrat listé, avec l'identité de ses parties.
     *
     * @param onglet au portail seulement : l'onglet où il se range pour l'opérateur actif
     */
    public record ContratListe(ContratLigne ligne, ResultatRecherche acheteur, ResultatRecherche vendeur,
            Optional<Onglet> onglet) {
    }

    /**
     * Le détail d'un contrat.
     *
     * @param maPartie au portail, la partie de l'opérateur actif
     * @param ecart    en projet, l'écart montré à la partie qui doit approuver (RG-CA-09)
     * @param peutAgir         au portail, l'acteur gère le module dans l'opérateur actif
     * @param stockInsuffisant au vendeur, en projet : le grain que son stock estimé ne couvre plus (RG-CA-12) ; il ne
     *                         peut alors qu'approuver une version réduite
     * @param retiraisonsSansStock au vendeur, les retiraisons qui l'attendent et que son stock estimé ne couvre plus
     *                             (RG-CA-15), par numéro : le message chiffré
     */
    public record VueContrat(ContratAchatArmagnac contrat, OperateurVue acheteur, OperateurVue vendeur,
            String campagne, Optional<Partie> maPartie, Optional<Ecart> ecart, boolean peutAgir,
            Optional<String> stockInsuffisant, Map<Integer, String> retiraisonsSansStock,
            List<EntreeJournal> journal) {
    }

    /** RG-CA-01, 25 : les contrats de l'opérateur actif, rangés par onglet. */
    public List<ContratListe> duPortail(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return listees(lecture.lister(Optional.of(operateur), Optional.empty()), Optional.of(operateur));
    }

    /** RG-CA-01, 25 : tous les contrats, pour le personnel du BNIA ; filtrés par partie (fiche opérateur) et numéro. */
    public List<ContratListe> duBackOffice(Acteur acteur, Optional<Long> operateur, Optional<String> numero) {
        autorisations.exigerPersonnelBnia(acteur);
        return listees(lecture.lister(operateur, numero.filter(n -> !n.isBlank())), Optional.empty());
    }

    /** RG-CA-01, 09, 12, 19 : un contrat dont l'opérateur actif est partie ; au vendeur, son stock qui manque. */
    public VueContrat auPortail(Acteur acteur, long operateur, long id) {
        autorisations.exigerLecture(acteur, operateur);
        ContratAchatArmagnac contrat = charger(id);
        Partie partie = Autorisations.partie(contrat, operateur);
        boolean peutAgir = operateur != operateurs.operateurBnia()
                && operateurs.droits(acteur.utilisateur(), operateur).gere(Module.CONTRATS);
        Optional<String> stockInsuffisant = partie == Partie.VENDEUR && contrat.etat() == EtatContrat.PROJET
                ? ControleStock.depassement(contrat.courante().contenu(), declarations.stockEstime(operateur))
                : Optional.empty();
        Map<Integer, String> retiraisonsSansStock = new TreeMap<>();
        if (partie == Partie.VENDEUR && contrat.etat() == EtatContrat.CONCLU) {
            LocalDate aujourdhui = LocalDate.now(horloge);
            contrat.retiraisons().stream()
                    .filter(r -> r.partieQuiDoitValider().filter(Partie.VENDEUR::equals).isPresent())
                    .forEach(r -> ExecuterContrats.stockInsuffisant(declarations, contrat, r.courante().contenu(),
                            aujourdhui).ifPresent(m -> retiraisonsSansStock.put(r.numero(), m)));
        }
        return vue(contrat, Optional.of(partie), peutAgir, stockInsuffisant, retiraisonsSansStock);
    }

    /** RG-CA-01 : un contrat, en lecture, pour le personnel du BNIA. */
    public VueContrat auBackOffice(Acteur acteur, long id) {
        autorisations.exigerPersonnelBnia(acteur);
        return vue(charger(id), Optional.empty(), false, Optional.empty(), Map.of());
    }

    /**
     * RG-CA-11, 12 : le stock estimé sous bois de l'opérateur actif, où le vendeur choisit les lignes de son contrat ;
     * les lignes à zéro ou négatives n'y sont pas.
     */
    public List<LigneStockEstimeVue> stockSousBois(Acteur acteur, long operateur) {
        autorisations.exigerGestion(acteur, operateur);
        return declarations.stockEstime(operateur).lignes().stream()
                .filter(l -> l.cle() == CleConditionnement.MB || l.cle() == CleConditionnement.NB)
                .filter(l -> !l.estime().estNul() && !l.estime().estNegatif())
                .toList();
    }

    private VueContrat vue(ContratAchatArmagnac contrat, Optional<Partie> maPartie, boolean peutAgir,
            Optional<String> stockInsuffisant, Map<Integer, String> retiraisonsSansStock) {
        OperateurVue acheteur = operateurs.operateur(contrat.acheteur()).orElseThrow();
        OperateurVue vendeur = operateurs.operateur(contrat.vendeur()).orElseThrow();
        Optional<Ecart> ecart = contrat.partieQuiDoitApprouver().map(contrat::ecartPour);
        return new VueContrat(contrat, acheteur, vendeur, contrat.campagne(LocalDate.now(horloge)), maPartie, ecart,
                peutAgir, stockInsuffisant, Map.copyOf(retiraisonsSansStock), journal.duContrat(contrat.id()));
    }

    private ContratAchatArmagnac charger(long id) {
        return contrats.parId(id).orElseThrow(() -> new ElementIntrouvable("Contrat introuvable."));
    }

    private List<ContratListe> listees(List<ContratLigne> lignes, Optional<Long> operateur) {
        Map<Long, ResultatRecherche> identites = lignes.isEmpty() ? Map.of()
                : operateurs.identites(lignes.stream().flatMap(l -> Stream.of(l.acheteur(),
                        l.vendeur())).collect(Collectors.toSet())).stream()
                        .collect(Collectors.toMap(ResultatRecherche::id, Function.identity()));
        return lignes.stream().map(l -> new ContratListe(l, identite(identites, l.acheteur()),
                identite(identites, l.vendeur()), operateur.map(o -> onglet(l, o)))).toList();
    }

    /**
     * RG-CA-25 : un projet, ou une retiraison d'un contrat conclu, est à approuver s'il attend ma partie, en
     * négociation s'il attend l'autre.
     */
    static Onglet onglet(ContratLigne ligne, long operateur) {
        Partie maPartie = ligne.acheteur() == operateur ? Partie.ACHETEUR : Partie.VENDEUR;
        return switch (ligne.etat()) {
            case PROJET -> ligne.attend().filter(maPartie::equals).isPresent() ? Onglet.A_APPROUVER
                    : Onglet.EN_NEGOCIATION;
            case CONCLU -> ligne.retiraisonsAttendent().contains(maPartie) ? Onglet.A_APPROUVER
                    : ligne.retiraisonsAttendent().isEmpty() ? Onglet.CONCLUS : Onglet.EN_NEGOCIATION;
            case EXECUTE -> Onglet.EXECUTES;
            case ANNULE, REFUSE -> Onglet.ANNULES_REFUSES;
        };
    }

    private static ResultatRecherche identite(Map<Long, ResultatRecherche> identites, long id) {
        ResultatRecherche identite = identites.get(id);
        return identite != null ? identite
                : new ResultatRecherche(id, "?", "Opérateur n° " + id, Optional.empty(), Optional.empty());
    }
}
