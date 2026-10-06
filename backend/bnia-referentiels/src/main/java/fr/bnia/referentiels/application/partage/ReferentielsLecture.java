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
 * <p>Pour chaque liste, trois méthodes :
 * <ul>
 *   <li>une valeur par son identifiant, celui que les autres contextes stockent (ADR-011), active ou
 *       non, pour l'afficher là où elle est utilisée ;</li>
 *   <li>une valeur par son code, pour les imports et la reprise, dont les fichiers portent des codes ;
 *       le code est comparé sans distinction de casse ;</li>
 *   <li>les valeurs actives, triées par libellé, pour une liste de choix (RG-REF-04).</li>
 * </ul>
 * Le consommateur vérifie lui-même qu'une valeur existe et qu'elle est active au moment de la saisie.
 */
public interface ReferentielsLecture {

    // Géographie

    Optional<ValeurVue> zoneCommerciale(long id);

    Optional<ValeurVue> zoneCommercialeParCode(String code);

    List<ValeurVue> zonesCommercialesActives();

    Optional<PaysVue> pays(long id);

    Optional<PaysVue> paysParCode(String code);

    List<PaysVue> paysActifs();

    Optional<RegionVue> region(long id);

    Optional<RegionVue> regionParCode(String code);

    List<RegionVue> regionsActives();

    // Qualification

    Optional<ValeurVue> titre(long id);

    Optional<ValeurVue> titreParCode(String code);

    List<ValeurVue> titresActifs();

    Optional<ValeurVue> label(long id);

    Optional<ValeurVue> labelParCode(String code);

    List<ValeurVue> labelsActifs();

    Optional<ValeurVue> typeVisite(long id);

    Optional<ValeurVue> typeVisiteParCode(String code);

    List<ValeurVue> typesVisiteActifs();

    Optional<ValeurVue> langue(long id);

    Optional<ValeurVue> langueParCode(String code);

    List<ValeurVue> languesActives();

    Optional<ValeurVue> centreInteret(long id);

    Optional<ValeurVue> centreInteretParCode(String code);

    List<ValeurVue> centresInteretActifs();

    Optional<ValeurVue> membreBnia(long id);

    Optional<ValeurVue> membreBniaParCode(String code);

    List<ValeurVue> membresBniaActifs();

    // Présentation et commercialisation

    Optional<ValeurVue> marque(long id);

    Optional<ValeurVue> marqueParCode(String code);

    List<ValeurVue> marquesActives();

    Optional<ValeurVue> circuitDistribution(long id);

    Optional<ValeurVue> circuitDistributionParCode(String code);

    List<ValeurVue> circuitsDistributionActifs();

    Optional<ValeurVue> produitCommercialise(long id);

    Optional<ValeurVue> produitCommercialiseParCode(String code);

    List<ValeurVue> produitsCommercialisesActifs();

    Optional<ContenanceVue> contenance(long id);

    /** Par volume en litres, pour les imports ; {@code 0.7} et {@code 0.700} sont égaux. */
    Optional<ContenanceVue> contenanceParVolume(BigDecimal volume);

    /** Triées par volume croissant. */
    List<ContenanceVue> contenancesActives();

    // Fiscalité

    Optional<ValeurVue> classeTva(long id);

    Optional<ValeurVue> classeTvaParCode(String code);

    List<ValeurVue> classesTvaActives();

    /**
     * RG-REF-12 : le taux dont la période contient la date, ou rien, jamais une valeur par défaut.
     * Répond aussi pour une classe inactive (RG-REF-13).
     *
     * @param classeTva identifiant de la classe
     */
    Optional<TauxTvaVue> tauxTva(long classeTva, LocalDate date);
}
