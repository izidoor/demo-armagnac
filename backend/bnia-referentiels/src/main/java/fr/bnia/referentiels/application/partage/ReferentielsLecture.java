package fr.bnia.referentiels.application.partage;

import fr.bnia.referentiels.domain.partage.ContenanceVue;
import fr.bnia.referentiels.domain.partage.PaysVue;
import fr.bnia.referentiels.domain.partage.RegionVue;
import fr.bnia.referentiels.domain.partage.TauxTvaVue;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lecture des référentiels par les autres contextes (ADR-008), seule porte d'entrée avec les types de
 * {@code domain.partage}. Lecture seule ; implémentée dans {@code referentiels}.
 *
 * <p>Pour chaque liste, deux méthodes :
 * <ul>
 *   <li>une valeur par son code, active ou non, pour l'afficher là où elle est utilisée ; le
 *       consommateur vérifie lui-même qu'elle existe et qu'elle est active au moment de la saisie ;</li>
 *   <li>les valeurs actives, triées par libellé, pour une liste de choix (RG-REF-04).</li>
 * </ul>
 * Les codes sont comparés tels quels, sans changer la casse.
 */
public interface ReferentielsLecture {

    // Géographie

    Optional<ValeurVue> zoneCommerciale(String code);

    List<ValeurVue> zonesCommercialesActives();

    Optional<PaysVue> pays(String code);

    List<PaysVue> paysActifs();

    Optional<RegionVue> region(String code);

    List<RegionVue> regionsActives();

    // Qualification

    Optional<ValeurVue> titre(String code);

    List<ValeurVue> titresActifs();

    Optional<ValeurVue> label(String code);

    List<ValeurVue> labelsActifs();

    Optional<ValeurVue> typeVisite(String code);

    List<ValeurVue> typesVisiteActifs();

    Optional<ValeurVue> langue(String code);

    List<ValeurVue> languesActives();

    Optional<ValeurVue> centreInteret(String code);

    List<ValeurVue> centresInteretActifs();

    // Présentation et commercialisation

    Optional<ValeurVue> marque(String code);

    List<ValeurVue> marquesActives();

    Optional<ValeurVue> circuitDistribution(String code);

    List<ValeurVue> circuitsDistributionActifs();

    Optional<ValeurVue> produitCommercialise(String code);

    List<ValeurVue> produitsCommercialisesActifs();

    /** Une contenance est référencée par son volume en litres (A-REF-1) ; {@code 0.7} et {@code 0.700} sont égaux. */
    Optional<ContenanceVue> contenance(BigDecimal volume);

    /** Triées par volume croissant. */
    List<ContenanceVue> contenancesActives();

    // Fiscalité

    Optional<ValeurVue> classeTva(String code);

    List<ValeurVue> classesTvaActives();

    /**
     * RG-REF-12 : le taux dont la période contient la date, ou rien, jamais une valeur par défaut.
     * Répond aussi pour une classe inactive (RG-REF-13).
     */
    Optional<TauxTvaVue> tauxTva(String classeTva, LocalDate date);
}
