package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.application.PeriodesAttendues.Attendue;
import fr.bnia.declarations.domain.GrilleCotisation;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Listing de toutes les DSM, pour le BNIA (analyse UC-DEC-7, spec 05 §6.1) : filtré par opérateur, type, mois
 * et état. RG-DEC-01 ; total HT et « facturée », RG-DEC-34 ; échéance, « en retard » et périodes absentes,
 * RG-DEC-39, 40.
 */
@UseCase
public class ListerDsm {

    private final DsmLecture lecture;
    private final CalendriersLecture calendriers;
    private final OperateursLecture operateurs;
    private final Autorisations autorisations;
    private final GrilleCotisationRepository grilles;
    private final Clock horloge;

    public ListerDsm(DsmLecture lecture, CalendriersLecture calendriers, GrilleCotisationRepository grilles,
            OperateursLecture operateurs, Clock horloge) {
        this.lecture = lecture;
        this.calendriers = calendriers;
        this.grilles = grilles;
        this.operateurs = operateurs;
        this.autorisations = new Autorisations(operateurs);
        this.horloge = horloge;
    }

    /**
     * Une ligne du listing : une DSM, ou la période attendue d'un opérateur pour laquelle aucune DSM n'est
     * initiée (RG-DEC-40 : absente). Avec le code et la raison sociale de l'opérateur, lus dans
     * {@code operateurs} pour tout le listing à la fois.
     *
     * @param dsm                  vide pour une période absente
     * @param echeance             RG-DEC-39
     * @param enRetard             RG-DEC-40
     * @param totalHt              facturé ou calculé ; absent si non calculable (RG-DEC-32, 34) ou sans DSM
     * @param operateurFacturable  rien ne manque à l'opérateur pour le facturer (RG-OPE-02, RG-DEC-43)
     */
    public record DsmListee(Optional<DsmLigne> dsm, long operateur, TypeDeclarant type, PeriodeDeclaration periode,
            String code, String raisonSociale, LocalDate echeance, boolean enRetard,
            Optional<BigDecimal> totalHt, boolean operateurFacturable) {

        /** RG-DEC-40 : la période attendue, sans DSM. */
        public boolean absente() {
            return dsm.isEmpty();
        }
    }

    /**
     * RG-DEC-01 : de la période la plus récente à la plus ancienne, puis par code d'opérateur. Les périodes
     * absentes suivent les filtres d'opérateur, de type et de mois, et n'apparaissent pas quand un état est
     * demandé. Sept lectures en tout, quelle que soit la taille du listing : les droits, les DSM, les opérateurs
     * attendus, leurs repères, l'identité des opérateurs, ceux qui sont facturables (RG-DEC-43), la grille.
     */
    public List<DsmListee> lister(Acteur acteur, FiltreDsm filtre) {
        autorisations.exigerLectureBnia(acteur);
        LocalDate aujourdhui = LocalDate.now(horloge);
        List<DsmLigne> dsms = lecture.rechercher(filtre);
        List<Attendue> attendues = PeriodesAttendues.de(operateurs, calendriers, aujourdhui);
        // RG-DEC-40 : seule la DSM non validée d'un opérateur attendu peut être en retard.
        Set<Long> enRetard = attendues.stream()
                .filter(a -> a.situation().enRetard())
                .flatMap(a -> a.situation().dsmEnCours().stream())
                .collect(Collectors.toSet());
        List<Attendue> absentes = filtre.etats().isEmpty()
                ? attendues.stream().filter(a -> a.situation().absente()).filter(a -> retenue(a, filtre)).toList()
                : List.of();
        GrilleCotisation grille = grilles.charger();
        Map<Long, ResultatRecherche> identites = identites(dsms, absentes);
        Set<Long> facturables = identites.isEmpty() ? Set.of() : operateurs.facturables(identites.keySet());
        List<DsmListee> listing = new ArrayList<>();
        for (DsmLigne d : dsms) {
            ResultatRecherche identite = identites.get(d.operateur());
            listing.add(new DsmListee(Optional.of(d), d.operateur(), d.type(), d.periode(), code(identite),
                    raisonSociale(identite, d.operateur()), d.periode().echeance(d.type()), enRetard.contains(d.id()),
                    Cotisations.totalHt(d, grille),
                    facturables.contains(d.operateur())));
        }
        for (Attendue a : absentes) {
            long operateur = a.operateur().id();
            ResultatRecherche identite = identites.get(operateur);
            listing.add(new DsmListee(Optional.empty(), operateur, TypeDeclarant.de(a.operateur().type()),
                    a.situation().periode().orElseThrow(), code(identite), raisonSociale(identite, operateur),
                    a.situation().echeance().orElseThrow(), a.situation().enRetard(), Optional.empty(),
                    facturables.contains(operateur)));
        }
        listing.sort(Comparator.comparing((DsmListee l) -> l.periode().debut()).reversed()
                .thenComparing(DsmListee::code));
        return listing;
    }

    /** Une période absente suit les filtres d'opérateur, de type et de mois du listing. */
    private static boolean retenue(Attendue a, FiltreDsm filtre) {
        PeriodeDeclaration periode = a.situation().periode().orElseThrow();
        return filtre.operateur().map(o -> o == a.operateur().id()).orElse(true)
                && filtre.type().map(t -> t == TypeDeclarant.de(a.operateur().type())).orElse(true)
                && filtre.mois().map(m -> contient(periode, m)).orElse(true);
    }

    private static boolean contient(PeriodeDeclaration periode, YearMonth mois) {
        LocalDate jour = mois.atDay(1);
        return !periode.debut().isAfter(jour) && !periode.fin().isBefore(jour);
    }

    /** Une seule lecture des opérateurs pour tout le listing, jamais une par ligne. */
    private Map<Long, ResultatRecherche> identites(List<DsmLigne> dsms, List<Attendue> absentes) {
        Set<Long> ids = dsms.stream().map(DsmLigne::operateur).collect(Collectors.toCollection(HashSet::new));
        absentes.forEach(a -> ids.add(a.operateur().id()));
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return operateurs.identites(ids).stream()
                .collect(Collectors.toMap(ResultatRecherche::id, Function.identity()));
    }

    private static String code(ResultatRecherche identite) {
        return identite == null ? "?" : identite.code();
    }

    private static String raisonSociale(ResultatRecherche identite, long operateur) {
        return identite == null ? "Opérateur n° " + operateur : identite.raisonSociale();
    }
}
