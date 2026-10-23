package fr.bnia.declarations.adapter.rest;

import fr.bnia.declarations.adapter.rest.DsmResponse.ManqueResponse;
import fr.bnia.declarations.application.GererPieces.PiecesDsm;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.Drm;
import fr.bnia.declarations.domain.Justificatif;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Schema(name = "PiecesDsm", description = "Pièces d'une DSM : justificatifs, mois de DRM, manques (spec 05 §6.3, onglet « Pièces »)")
public record PiecesResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Dans l'ordre de dépôt")
        List<JustificatifResponse> justificatifs,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Pour un producteur, les mois de la période et leur DRM ; vide pour un négociant (RG-DEC-27)")
        List<MoisDrmResponse> mois,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ManqueResponse> manques,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Les retiraisons de contrat inscrites, qui justifient leur type de mouvement sans PDF (RG-CA-23)")
        List<JustificatifContratResponse> contrats,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Les types de mouvement, leurs colonnes et les types de pièce compatibles (RG-DEC-26)")
        List<TypeMouvementResponse> typesMouvement) {

    private static final DateTimeFormatter MOIS = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

    @Schema(name = "DocumentDsm", description = "Fichier déposé (ADR-018)")
    public record DocumentResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Octets") long taille,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de qui l'a déposé")
            String deposePar,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant deposeLe) {
    }

    @Schema(name = "JustificatifDsm", description = "Justificatif d'une DSM (spec 05 §3.2.2)")
    public record JustificatifResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
            @Schema(description = "Ligne visée ; vide pour la DSM") Long ligne,
            @Schema(description = "Libellé de la ligne visée, supprimée depuis ; la pièce porte alors sur la DSM")
            String ligneSupprimee,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeMouvement typeMouvement,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypePiece typePiece,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String libelleTypePiece,
            @Schema(description = "Référence DAE/DAI") String reference,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) DocumentResponse document) {
    }

    @Schema(name = "JustificatifContratDsm", description = "Une retiraison de contrat inscrite, justificatif de son mouvement (spec 06 RG-CA-23)")
    public record JustificatifContratResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeMouvement typeMouvement,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant du contrat") long contrat,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "CA-2026-0001") String numero,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int retiraison,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dateEnlevement,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "« millésime 2015, clé MB, compte d'âge 10 »")
            String ligne,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "3") int colonne,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal volume,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Contrat CA-2026-0001, retiraison 1")
            String libelle) {
    }

    @Schema(name = "MoisDrm", description = "Un mois de la période d'un producteur et sa DRM (RG-DEC-27)")
    public record MoisDrmResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-11") String mois,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "novembre 2025") String libelle,
            @Schema(description = "Vide : DRM attendue") DocumentResponse drm) {
    }

    @Schema(name = "PieceCompatible")
    public record PieceResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypePiece type,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String libelle) {
    }

    @Schema(name = "TypeMouvementDsm", description = "Type de mouvement qui appelle un justificatif (H-DEC-13)")
    public record TypeMouvementResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeMouvement type,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String libelle,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Integer> colonnes,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<PieceResponse> pieces) {
    }

    static PiecesResponse depuis(PiecesDsm pieces) {
        return new PiecesResponse(
                pieces.justificatifs().stream().map(PiecesResponse::justificatif).toList(),
                pieces.mois().stream().map(m -> new MoisDrmResponse(m.mois().toString(), m.mois().format(MOIS),
                        m.drm().map(PiecesResponse::document).orElse(null))).toList(),
                pieces.manques().stream().map(DsmResponse::manque).toList(),
                pieces.contrats().stream().map(m -> new JustificatifContratResponse(
                        TypeMouvement.de(m.colonne()).orElseThrow(), m.contrat(), m.numeroContrat(), m.retiraison(),
                        m.dateEnlevement(), m.identite().libelle(), m.colonne().numero(), m.volumeAp().litres(),
                        m.libelle())).toList(),
                Arrays.stream(TypeMouvement.values()).map(t -> new TypeMouvementResponse(t, t.libelle(),
                        t.colonnes().stream().map(ColonneDsm::numero).toList(),
                        t.pieces().stream().sorted().map(p -> new PieceResponse(p, p.libelle())).toList())).toList());
    }

    static JustificatifResponse justificatif(Justificatif j) {
        return new JustificatifResponse(j.id(), j.ligne(), j.ligneSupprimee(), j.typeMouvement(), j.typePiece(),
                j.typePiece().libelle(), j.reference(), new DocumentResponse(j.document().nom(), j.document().taille(),
                j.deposePar().nom(), j.deposeLe()));
    }

    private static DocumentResponse document(Drm d) {
        return new DocumentResponse(d.document().nom(), d.document().taille(), d.deposePar().nom(), d.deposeLe());
    }
}
