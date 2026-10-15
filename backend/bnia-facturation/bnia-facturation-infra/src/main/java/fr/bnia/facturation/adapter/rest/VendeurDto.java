package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.domain.Vendeur;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "VendeurFacturation", description = "Le vendeur, le BNIA, recopié sur chaque pièce à son émission (spec 08 §3.7, RG-FAC-16)")
public record VendeurDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AdresseDto siege,
        @Schema(description = "Si elle diffère du siège") AdresseDto adresseFacturation,
        @Schema(description = "Tiré du SIRET s'il est vide", example = "776999955") String siren,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "77699995500029") String siret,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "FR55776999955") String numeroTva,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String iban,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String bic,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String telephone,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "compta@armagnac.fr") String email,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Option pour la TVA d'après les débits")
        boolean tvaSurDebits,
        @Schema(description = "Défaut : « Pas d'escompte pour paiement anticipé »") String escompte,
        @Schema(description = "Défaut : « trois fois le taux d'intérêt légal »") String penalites) {

    static VendeurDto de(Vendeur v) {
        return new VendeurDto(v.nom(), AdresseDto.de(v.siege()), AdresseDto.de(v.adresseFacturation()), v.siren(),
                v.siret(), v.numeroTva(), v.iban(), v.bic(), v.telephone(), v.email(), v.tvaSurDebits(), v.escompte(),
                v.penalites());
    }

    Vendeur versDomaine() {
        return new Vendeur(nom, siege == null ? null : siege.versDomaine(),
                adresseFacturation == null ? null : adresseFacturation.versDomaine(),
                siren, siret, numeroTva, iban, bic, telephone, email, tvaSurDebits, escompte, penalites);
    }
}
