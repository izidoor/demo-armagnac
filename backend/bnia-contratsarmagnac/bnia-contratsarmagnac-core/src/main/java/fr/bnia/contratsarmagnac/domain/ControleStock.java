package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.StockEstimeVue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * RG-CA-12 : le contenu proposé par le vendeur ne dépasse pas son stock estimé (RG-CA-11), grain par grain, les lignes
 * du même grain cumulées. Le message s'adresse au vendeur seul : il donne les chiffres.
 */
public final class ControleStock {

    private ControleStock() {
    }

    /** Le stock estimé sous bois du vendeur, par grain ; les lignes sous verre n'y sont pas. */
    public static Map<Grain, VolumeAlcoolPur> disponibles(StockEstimeVue stock) {
        Map<Grain, VolumeAlcoolPur> parGrain = new HashMap<>();
        stock.lignes().forEach(l -> Grain.duStock(l.cle(), l.millesime(), l.denomination(), l.compte())
                .ifPresent(g -> parGrain.merge(g, l.estime(), VolumeAlcoolPur::plus)));
        return parGrain;
    }

    /** @throws RegleEnfreinte (RG-CA-12) au premier grain que le stock estimé ne couvre pas */
    public static void verifier(ContenuContrat contenu, StockEstimeVue stock) {
        depassement(contenu, stock).ifPresent(message -> {
            throw new RegleEnfreinte("RG-CA-12", message);
        });
    }

    /** RG-CA-12 : le message du premier grain que le stock estimé ne couvre pas ; vide si tout est couvert. */
    public static Optional<String> depassement(ContenuContrat contenu, StockEstimeVue stock) {
        return depassement(contenu.volumeApParGrain(), grain -> contenu.lignes().stream()
                .filter(l -> l.grain().equals(grain)).findFirst().map(LigneContrat::libelle).orElseThrow(), "au contrat",
                stock);
    }

    /**
     * RG-CA-12, 13, 15 : le message, chiffré, du premier grain demandé que le stock estimé ne couvre pas ; vide si tout
     * est couvert.
     *
     * @param libelle « millésime 2015 », « Hors d'âge, compte 20+ »
     * @param ou      « au contrat », « à retirer »
     */
    public static Optional<String> depassement(Map<Grain, VolumeAlcoolPur> demande, Function<Grain, String> libelle,
            String ou, StockEstimeVue stock) {
        Map<Grain, VolumeAlcoolPur> disponibles = disponibles(stock);
        return demande.entrySet().stream()
                .filter(e -> e.getValue().compareTo(disponibles.getOrDefault(e.getKey(), VolumeAlcoolPur.ZERO)) > 0)
                .findFirst()
                .map(e -> {
                    VolumeAlcoolPur disponible = disponibles.getOrDefault(e.getKey(), VolumeAlcoolPur.ZERO);
                    return "Votre stock estimé ne couvre pas « " + libelle.apply(e.getKey()) + " » : "
                            + hlAp(e.getValue()) + " hl AP " + ou + ", "
                            + hlAp(disponible.estNegatif() ? VolumeAlcoolPur.ZERO : disponible) + " hl AP en stock.";
                });
    }

    /** « 14,10 » pour 1 410 L AP. */
    static String hlAp(VolumeAlcoolPur volume) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        return format.format(volume.litres().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    }
}
