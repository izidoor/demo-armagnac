package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.application.StocksLecture.LigneStock;
import fr.bnia.declarations.domain.AnneeDeclarative;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stocks déclarés (analyse UC-DEC-1, spec 05 §6.1) : celui d'un opérateur, pour sa fiche ; ceux de tous, pour le
 * BNIA, avec leur export Excel pour les douanes ; leur historique d'une année déclarative, mois par mois, pour le
 * tableau croisé. RG-DEC-01, 42, 44.
 */
@UseCase
public class ConsulterStocksDeclares {

    private final StocksLecture stocks;
    private final DsmLecture dsms;
    private final EditionsStocks editions;
    private final OperateursLecture operateurs;
    private final Autorisations autorisations;
    private final Clock horloge;

    public ConsulterStocksDeclares(StocksLecture stocks, DsmLecture dsms, EditionsStocks editions,
            OperateursLecture operateurs, Clock horloge) {
        this.stocks = stocks;
        this.dsms = dsms;
        this.editions = editions;
        this.operateurs = operateurs;
        this.autorisations = new Autorisations(operateurs);
        this.horloge = horloge;
    }

    /** Une ligne du stock déclaré, avec le code et la raison sociale de son opérateur. */
    public record LigneStockListee(LigneStock ligne, String code, String raisonSociale) {
    }

    /**
     * Le stock déclaré d'un opérateur (RG-DEC-42).
     *
     * @param periode la période de sa dernière DSM validée : le stock est daté de sa fin
     */
    public record StockDeclare(long dsm, PeriodeDeclaration periode, Instant valideeLe, List<LigneStock> lignes) {

        public StockDeclare {
            lignes = List.copyOf(lignes);
        }
    }

    /** RG-DEC-44 : une ligne du stock déclaré d'un opérateur à une fin de mois, tirée de sa DSM {@code ligne.dsm}. */
    public record StockALaDate(LocalDate au, LigneStockListee ligne) {
    }

    /**
     * RG-DEC-44 : l'historique des stocks déclarés d'une année déclarative.
     *
     * @param anneesDisponibles les années qui ont une DSM validée, et l'année en cours, de la plus récente à la
     *                          plus ancienne
     */
    public record HistoriqueStocks(AnneeDeclarative annee, List<AnneeDeclarative> anneesDisponibles,
            List<StockALaDate> lignes) {

        public HistoriqueStocks {
            anneesDisponibles = List.copyOf(anneesDisponibles);
            lignes = List.copyOf(lignes);
        }
    }

    /**
     * RG-DEC-01, 42 : tout le personnel du BNIA lit les stocks déclarés de tous les opérateurs (décision du
     * 2026-10-14, comme le listing de toutes les DSM), triés par code d'opérateur ; trois lectures en tout.
     */
    public List<LigneStockListee> deTous(Acteur acteur, Optional<Long> operateur, Optional<TypeDeclarant> type) {
        autorisations.exigerLectureStocks(acteur);
        return listees(stocks.stocksDeclares(operateur, type));
    }

    /** Les lignes avec le code et la raison sociale de leur opérateur, triées par code (tri stable). */
    private List<LigneStockListee> listees(List<LigneStock> lignes) {
        Map<Long, ResultatRecherche> identites = lignes.isEmpty() ? Map.of()
                : operateurs.identites(lignes.stream().map(LigneStock::operateur).collect(Collectors.toSet()))
                        .stream().collect(Collectors.toMap(ResultatRecherche::id, Function.identity()));
        return lignes.stream()
                .map(l -> {
                    ResultatRecherche identite = identites.get(l.operateur());
                    return new LigneStockListee(l, identite == null ? "?" : identite.code(),
                            identite == null ? "Opérateur n° " + l.operateur() : identite.raisonSociale());
                })
                // Tri stable : l'ordre des lignes de chaque DSM est gardé.
                .sorted(Comparator.comparing(LigneStockListee::code))
                .toList();
    }

    /**
     * RG-DEC-01, 44 : l'historique des stocks déclarés de tous les opérateurs sur une année déclarative (l'année
     * en cours par défaut), pour le personnel du BNIA, filtré par opérateur et par type.
     */
    public HistoriqueStocks historique(Acteur acteur, Optional<AnneeDeclarative> annee, Optional<Long> operateur,
            Optional<TypeDeclarant> type) {
        autorisations.exigerLectureStocks(acteur);
        return historique(annee, operateur, type);
    }

    /** RG-DEC-01, 44 : l'historique des stocks déclarés d'un opérateur, pour tout lecteur de ses déclarations. */
    public HistoriqueStocks historiqueDe(Acteur acteur, long operateur, Optional<AnneeDeclarative> annee) {
        autorisations.exigerLecture(acteur, operateur);
        return historique(annee, Optional.of(operateur), Optional.empty());
    }

    private HistoriqueStocks historique(Optional<AnneeDeclarative> demandee, Optional<Long> operateur,
            Optional<TypeDeclarant> type) {
        LocalDate aujourdhui = LocalDate.now(horloge);
        AnneeDeclarative enCours = AnneeDeclarative.contenant(aujourdhui);
        AnneeDeclarative annee = demandee.orElse(enCours);
        TreeSet<AnneeDeclarative> disponibles = new TreeSet<>(
                Comparator.comparingInt(AnneeDeclarative::debutAnnee).reversed());
        disponibles.addAll(stocks.anneesDeclarees(operateur));
        disponibles.add(enCours);
        List<LigneStockListee> lignes = listees(stocks.stocksDeLAnnee(annee, operateur, type));
        return new HistoriqueStocks(annee, List.copyOf(disponibles), reporter(lignes, annee.finsDeMois(aujourdhui)));
    }

    /**
     * RG-DEC-44 : à chaque fin de mois, chaque opérateur compte pour le stock de sa dernière DSM validée finie à
     * cette date, ses lignes au stock nul omises ; les lignes reçues sont celles d'une seule année. Ordre : par
     * fin de mois, puis dans l'ordre reçu.
     */
    static List<StockALaDate> reporter(List<LigneStockListee> lignes, List<LocalDate> finsDeMois) {
        Map<Long, Map<Long, List<LigneStockListee>>> parOperateur = new LinkedHashMap<>();
        for (LigneStockListee l : lignes) {
            parOperateur.computeIfAbsent(l.ligne().operateur(), o -> new LinkedHashMap<>())
                    .computeIfAbsent(l.ligne().dsm(), d -> new ArrayList<>()).add(l);
        }
        List<StockALaDate> resultat = new ArrayList<>();
        for (LocalDate au : finsDeMois) {
            for (Map<Long, List<LigneStockListee>> parDsm : parOperateur.values()) {
                parDsm.values().stream()
                        .filter(d -> !d.getFirst().ligne().periode().fin().isAfter(au))
                        .max(Comparator.comparing(d -> d.getFirst().ligne().periode().fin()))
                        .ifPresent(d -> d.stream().filter(l -> !l.ligne().stock().estNul())
                                .forEach(l -> resultat.add(new StockALaDate(au, l))));
            }
        }
        return resultat;
    }

    /** UC-DEC-1 : l'export Excel des stocks déclarés, aux mêmes filtres que l'écran. */
    public byte[] exporter(Acteur acteur, Optional<Long> operateur, Optional<TypeDeclarant> type) {
        return editions.excel(deTous(acteur, operateur, type), LocalDate.now(horloge));
    }

    /** RG-DEC-01, 42 : le stock déclaré d'un opérateur, pour tout lecteur de ses déclarations ; vide sans DSM validée. */
    public Optional<StockDeclare> deLOperateur(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        return stockDe(dsms, stocks, operateur);
    }

    /** RG-DEC-42 : vide sans DSM validée ; sans ligne si toutes sont finies à 0. */
    static Optional<StockDeclare> stockDe(DsmLecture dsms, StocksLecture stocks, long operateur) {
        return dsms.deLOperateur(operateur).stream()
                .filter(d -> d.etat() == EtatDsm.VALIDEE)
                .findFirst()
                .map(d -> new StockDeclare(d.id(), d.periode(), d.valideeLe(),
                        stocks.stocksDeclares(Optional.of(operateur), Optional.empty())));
    }
}
