package fr.bnia.declarations.adapter.rest;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.ActionDsm;
import fr.bnia.declarations.application.VueDsm;
import fr.bnia.declarations.domain.Anomalie;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.Manque;
import fr.bnia.declarations.domain.NiveauAnomalie;
import fr.bnia.declarations.domain.OngletDsm;
import fr.bnia.declarations.domain.OrigineLigne;
import fr.bnia.declarations.domain.SensColonne;
import fr.bnia.declarations.domain.TypeAnomalie;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.EtatDsm;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Vue d'une DSM (spec 05 §6.3). Les volumes sont des tableaux de 26 valeurs en litres d'alcool pur,
 * l'indice 0 portant la colonne 1 ; une colonne grisée vaut 0.
 */
@Schema(name = "Dsm", description = "Déclaration de stocks et mouvements, avec ses totaux et ses anomalies (spec 05 §3.2, §6.3)")
public record DsmResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long operateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "N320001") String codeOperateur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeDeclarant type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PeriodeResponse periode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EtatDsm etat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Envoyée",
                description = "Du point de vue de l'utilisateur connecté : « Envoyée » pour l'opérateur, « À prendre en charge » pour le BNIA")
        String libelleEtat,
        @Schema(description = "Date de la DIA, facultative (RG-DEC-19)") LocalDate dateDia,
        Instant envoyeeLe,
        Instant valideeLe,
        @Schema(description = "Copie du nom de qui l'a validée") String valideePar,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Actions permises à l'utilisateur connecté, selon l'état et son côté (RG-DEC-01, 20 à 22, 24)")
        List<ActionDsm> actions,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ColonneResponse> colonnes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneResponse> lignes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Total de chaque colonne, 26 valeurs (RG-DEC-17)")
        List<BigDecimal> totaux,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<AnomalieResponse> anomalies,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Pièces manquantes, bloquantes à la validation, pas à l'envoi (RG-DEC-29)")
        List<ManqueResponse> manques,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CotisationResponse cotisation,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Échéance de la période (RG-DEC-39)")
        LocalDate echeance,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Échéance passée, DSM en brouillon ou en compléments demandés, opérateur au statut OUI (RG-DEC-40)")
        boolean enRetard,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long version) {

    @Schema(name = "ColonneDsm", description = "Une des 26 colonnes du format unique (spec 05 §3.1)")
    public record ColonneResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int numero,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OngletDsm onglet,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String libelleOnglet,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String libelle,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SensColonne sens,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                    description = "Grisée pour ce déclarant : visible, vide, non saisissable (RG-DEC-16)") boolean grisee,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Colonnes 2 à 25 non grisées")
            boolean saisissable) {
    }

    @Schema(name = "LigneDsm", description = "Ligne d'une DSM (spec 05 §3.2.1)")
    public record LigneResponse(
            @Schema(description = "Vide pour une ligne ajoutée, dans une vue provisoire") Long id,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CleConditionnement cle,
            @Schema(description = "Lignes MB et MV") Integer millesime,
            @Schema(description = "Lignes NB et NV") String denomination,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "MB : calculé (RG-DEC-12)", example = "20+")
            String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                    description = "REPRISE : ne se supprime pas, identité figée (RG-DEC-13)") OrigineLigne origine,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "26 valeurs, colonne 26 calculée")
            List<BigDecimal> volumes,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalEntrees,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalSorties) {
    }

    @Schema(name = "AnomalieDsm", description = "Anomalie de calcul, bloquante à l'envoi (RG-DEC-18)")
    public record AnomalieResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeAnomalie type,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NiveauAnomalie niveau,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OngletDsm onglet,
            @Schema(description = "Numéro de la colonne concernée") Integer colonne,
            @Schema(description = "Rang de la ligne concernée, 0 pour la première") Integer ligne,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String message,
            @Schema(description = "Litres en cause") BigDecimal ecart) {
    }

    @Schema(name = "ManqueDsm", description = "Pièce manquante, au niveau de la DSM (RG-DEC-29)")
    public record ManqueResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Manque.TypeManque type,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NiveauAnomalie niveau,
            @Schema(description = "Type de mouvement sans justificatif") TypeMouvement typeMouvement,
            @Schema(description = "Mois sans DRM", example = "2025-11") String mois,
            @Schema(description = "Litres du type de mouvement en cause") BigDecimal total,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String message) {
    }

    static DsmResponse depuis(VueDsm vue) {
        DeclarationStocksMouvements dsm = vue.dsm();
        List<ColonneResponse> colonnes = Arrays.stream(ColonneDsm.values())
                .map(c -> new ColonneResponse(c.numero(), c.onglet(), c.onglet().libelle(), c.libelle(), c.sens(),
                        c.griseePour(dsm.type()), c.saisissablePour(dsm.type())))
                .toList();
        return new DsmResponse(dsm.id(), dsm.operateur(), vue.operateur().code(), vue.operateur().raisonSociale(),
                dsm.type(), PeriodeResponse.depuis(dsm.periode()), dsm.etat(), dsm.etat().libelle(vue.cote()), dsm.dateDia(),
                dsm.envoyeeLe(), dsm.valideeLe(), dsm.valideePar() == null ? null : dsm.valideePar().nom(),
                vue.actions().stream().sorted().toList(), colonnes,
                dsm.lignes().stream().map(DsmResponse::ligne).toList(), volumes(dsm::total),
                vue.anomalies().stream().map(DsmResponse::anomalie).toList(),
                vue.manques().stream().map(DsmResponse::manque).toList(),
                CotisationResponse.depuis(vue.cotisation(), dsm.periode().fin()), vue.echeance(), vue.enRetard(),
                dsm.version());
    }

    private static LigneResponse ligne(LigneDsm ligne) {
        IdentiteLigne i = ligne.identite();
        return new LigneResponse(ligne.id(), i.cle(), i.millesime() == null ? null : i.millesime().annee(),
                i.denomination() == null ? null : i.denomination().texte(), i.compte().toString(), ligne.origine(),
                volumes(ligne::volume), ligne.totalEntrees().litres(), ligne.totalSorties().litres());
    }

    private static List<BigDecimal> volumes(Function<ColonneDsm, VolumeAlcoolPur> volume) {
        return Arrays.stream(ColonneDsm.values()).map(c -> volume.apply(c).litres()).toList();
    }

    static ManqueResponse manque(Manque m) {
        return new ManqueResponse(m.type(), m.niveau(), m.typeMouvement(), m.mois() == null ? null : m.mois().toString(),
                m.total() == null ? null : m.total().litres(), m.message());
    }

    private static AnomalieResponse anomalie(Anomalie a) {
        return new AnomalieResponse(a.type(), a.niveau(), a.onglet(), a.colonne() == null ? null : a.colonne().numero(),
                a.ligne(), a.message(), a.ecart() == null ? null : a.ecart().litres());
    }
}
