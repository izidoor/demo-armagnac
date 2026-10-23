package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Le stock estimé d'un opérateur (spec 06 RG-CA-11, H-CA-7), sans état propre : par ligne de DSM, la base vieillie à
 * aujourd'hui, plus les achats, moins les ventes de ses mouvements de contrat postérieurs à la base. Seuls des faits
 * confirmés y entrent : DSM validée ou point de départ, retiraisons validées.
 *
 * @param baseAu date de la base, vide sans base (base nulle)
 */
public record StockEstime(Optional<LocalDate> baseAu, List<Ligne> lignes) {

    public StockEstime {
        lignes = List.copyOf(lignes);
    }

    /** Une ligne : base, achats et ventes, en litres d'alcool pur. */
    public record Ligne(IdentiteLigne identite, VolumeAlcoolPur base, VolumeAlcoolPur achats, VolumeAlcoolPur ventes) {

        public VolumeAlcoolPur estime() {
            return base.plus(achats).moins(ventes);
        }
    }

    /**
     * RG-CA-11 : la base, stock daté de {@code baseAu}, vieillie à {@code aujourdhui} (RG-DEC-14) ; une base de fin
     * de période vieillit dès le 1er avril qui la suit, celle d'un point de départ (stock au début) le lendemain. Puis
     * les mouvements de contrat datés après {@code baseAu}, y compris dans le futur : achats ajoutés, ventes déduites,
     * sur la ligne de leur grain vieilli à aujourd'hui ; une ligne que la base n'a pas est ajoutée.
     *
     * @param depuis le lendemain de la date de la base
     */
    public static StockEstime depuis(List<LigneStockInitial> base, LocalDate baseAu, LocalDate depuis,
            LocalDate aujourdhui, List<MouvementContrat> mouvements) {
        Map<IdentiteLigne, Ligne> lignes = new LinkedHashMap<>();
        Vieillissement.vieilli(base, depuis, aujourdhui).forEach(l -> lignes.put(l.identite(),
                new Ligne(l.identite(), l.volume(), VolumeAlcoolPur.ZERO, VolumeAlcoolPur.ZERO)));
        ajouter(lignes, mouvements.stream().filter(m -> m.dateEnlevement().isAfter(baseAu)).toList(), aujourdhui);
        return new StockEstime(Optional.of(baseAu), List.copyOf(lignes.values()));
    }

    /** Sans base (ni DSM validée ni point de départ) : la base est nulle, restent les mouvements de contrat. */
    public static StockEstime sansBase(LocalDate aujourdhui, List<MouvementContrat> mouvements) {
        Map<IdentiteLigne, Ligne> lignes = new LinkedHashMap<>();
        ajouter(lignes, mouvements, aujourdhui);
        return new StockEstime(Optional.empty(), List.copyOf(lignes.values()));
    }

    private static void ajouter(Map<IdentiteLigne, Ligne> lignes, List<MouvementContrat> mouvements,
            LocalDate aujourdhui) {
        for (MouvementContrat m : mouvements) {
            IdentiteLigne identite = aujourdhui(m, aujourdhui);
            Ligne ligne = lignes.getOrDefault(identite,
                    new Ligne(identite, VolumeAlcoolPur.ZERO, VolumeAlcoolPur.ZERO, VolumeAlcoolPur.ZERO));
            lignes.put(identite, m.sens() == SensMouvementContrat.ACHAT
                    ? new Ligne(ligne.identite(), ligne.base(), ligne.achats().plus(m.volumeAp()), ligne.ventes())
                    : new Ligne(ligne.identite(), ligne.base(), ligne.achats(), ligne.ventes().plus(m.volumeAp())));
        }
    }

    /** Le grain du mouvement à aujourd'hui : {@code MB} recalculé, {@code NB} +1 par 1er avril franchi depuis sa date. */
    private static IdentiteLigne aujourdhui(MouvementContrat mouvement, LocalDate aujourdhui) {
        int anniversaires = mouvement.dateEnlevement().isBefore(aujourdhui)
                ? Vieillissement.anniversaires(mouvement.dateEnlevement().plusDays(1), aujourdhui) : 0;
        return mouvement.identite().cle() == CleConditionnement.MB ? mouvement.identiteAu(aujourdhui)
                : anniversaires > 0 ? Vieillissement.vieillie(mouvement.identite(), aujourdhui, anniversaires)
                        : mouvement.identite();
    }
}
