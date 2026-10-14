package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Calendrier déclaratif (spec 05 §3.6), sans état propre : la période de la prochaine DSM d'un opérateur, et
 * ce qui en empêche l'initiation (RG-DEC-02 à 05). Échéances et retards : lot 7.
 */
public final class CalendrierDeclaratif {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private CalendrierDeclaratif() {
    }

    /** Une DSM de l'opérateur, telle que le calendrier la lit. */
    public record DsmPassee(long id, PeriodeDeclaration periode, EtatDsm etat) {
    }

    /** Ce qui empêche l'initiation (spec 05 §6.2). */
    public enum Empechement {
        TYPE_NON_DECLARANT("RG-DEC-02"),
        STATUT_NON_DECLARANT("RG-DEC-02"),
        SANS_POINT_DE_DEPART("RG-DEC-05"),
        DSM_EN_COURS("RG-DEC-05"),
        PERIODE_PAS_COMMENCEE("RG-DEC-05");

        private final String regle;

        Empechement(String regle) {
            this.regle = regle;
        }

        public String regle() {
            return regle;
        }
    }

    /**
     * La prochaine DSM d'un opérateur.
     *
     * @param periode      la période à initier, ou celle de la DSM en cours ; absente sans point de départ
     * @param empechement  absent si l'opérateur peut initier maintenant
     * @param dsmEnCours   la DSM non validée, s'il en a une
     * @param message      la raison, pour l'écran ; vide si rien n'empêche
     */
    public record Situation(Optional<PeriodeDeclaration> periode, Optional<Empechement> empechement,
            Optional<Long> dsmEnCours, String message) {

        public boolean initiable() {
            return empechement.isEmpty();
        }

        /** @throws RegleEnfreinte si l'initiation est empêchée */
        public PeriodeDeclaration exigerInitiable() {
            empechement.ifPresent(e -> {
                throw new RegleEnfreinte(e.regle(), message);
            });
            return periode.orElseThrow();
        }
    }

    /**
     * RG-DEC-02 à 05 : la période commence le lendemain de la dernière DSM validée qui commence au début
     * du point de départ ou après, sinon au début du point de départ ; une seule DSM non validée ; pas
     * avant le premier jour de la période.
     */
    public static Situation prochaine(TypeOperateur typeOperateur, StatutDeclarationStock statut,
            Optional<PointDeDepartDeclaratif> pointDeDepart, List<DsmPassee> dsms, LocalDate aujourdhui) {
        if (!typeOperateur.declare()) {
            return empechee(Optional.empty(), Empechement.TYPE_NON_DECLARANT, Optional.empty(),
                    "Seul un producteur ou un négociant déclare ses stocks.");
        }
        TypeDeclarant type = TypeDeclarant.de(typeOperateur);
        Optional<DsmPassee> enCours = dsms.stream().filter(d -> d.etat() != EtatDsm.VALIDEE).findFirst();
        if (enCours.isPresent()) {
            DsmPassee dsm = enCours.get();
            return empechee(Optional.of(dsm.periode()), Empechement.DSM_EN_COURS, Optional.of(dsm.id()),
                    "La DSM de " + dsm.periode().libelle() + " n'est pas encore validée.");
        }
        // RG-DEC-02 : une DSM en cours quand le statut change va à son terme.
        if (statut != StatutDeclarationStock.OUI) {
            return empechee(Optional.empty(), Empechement.STATUT_NON_DECLARANT, Optional.empty(),
                    "Le statut de déclaration de stock est « " + statut.libelle() + " » : aucune DSM n'est attendue.");
        }
        if (pointDeDepart.isEmpty()) {
            return empechee(Optional.empty(), Empechement.SANS_POINT_DE_DEPART, Optional.empty(),
                    "Le BNIA n'a pas encore fixé le point de départ de vos déclarations.");
        }
        PointDeDepartDeclaratif point = pointDeDepart.get();
        PeriodeDeclaration periode = dsms.stream()
                .filter(d -> d.etat() == EtatDsm.VALIDEE && !d.periode().debut().isBefore(point.debut()))
                .map(DsmPassee::periode)
                .max(Comparator.comparing(PeriodeDeclaration::debut))
                .map(p -> p.suivante(type))
                .orElseGet(() -> point.premierePeriode(type));
        if (!periode.commencee(aujourdhui)) {
            return empechee(Optional.of(periode), Empechement.PERIODE_PAS_COMMENCEE, Optional.empty(),
                    "La DSM de " + periode.libelle() + " pourra être initiée à partir du "
                            + DATE.format(periode.debut()) + ".");
        }
        return new Situation(Optional.of(periode), Optional.empty(), Optional.empty(), "");
    }

    private static Situation empechee(Optional<PeriodeDeclaration> periode, Empechement empechement,
            Optional<Long> dsmEnCours, String message) {
        return new Situation(periode, Optional.of(empechement), dsmEnCours, message);
    }
}
