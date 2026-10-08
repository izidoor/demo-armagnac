package fr.bnia.operateurs.adapter.rest;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.Millesime;
import fr.bnia.operateurs.application.FicheOperateur;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.DestinataireFactures;
import fr.bnia.operateurs.domain.IdentiteOperateur;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.TypeAlambic;
import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Schema(name = "FicheOperateur", description = "Un opérateur, avec ce que sa fiche signale (RG-OPE-02, 05, 09) et les droits du lecteur")
public record FicheOperateurResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeOperateur type,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "000012") String code,
        String groupe,
        String cvi,
        String siret,
        String accise,
        String bureauDouanes,
        String identifiantOdg,
        @Schema(description = "Identifiant du titre") Long titre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String raisonSociale,
        String nomCommercial,
        String adresseLigne1,
        String adresseLigne2,
        String adresseLigne3,
        String codePostal,
        String ville,
        @Schema(description = "Identifiant du pays") Long pays,
        String telephone1,
        String telephone2,
        String portable,
        String emailGeneral,
        String siteWeb,
        String siteCommercial,
        BigDecimal latitude,
        BigDecimal longitude,
        @Schema(description = "Identifiant du membre BNIA") Long membreBnia,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean accordDiffusionGeneral,
        @Schema(description = "Visible du seul personnel du BNIA (RG-OPE-36)") String commentaireInterne,
        @Schema(description = "Absente pour un opérateur AUTRE ou BNIA") PeriodiciteDsm periodicite,
        @Schema(description = "Périodicité par défaut du type, suggérée à l'écran quand elle diffère (RG-OPE-10)")
        PeriodiciteDsm periodiciteParDefaut,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) StatutDeclarationStock statutDeclarationStock,
        @Schema(description = "Identifiant de la région") Long region,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<Appellation> appellations,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<AlambicResponse> alambics,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Calculé : au moins un alambic (RG-OPE-13)")
        boolean detenteurAlambic,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NiveauDiffusion niveauDiffusion,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Années, croissantes ; en accord partiel seulement")
        List<Integer> millesimesDiffusables,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean accesStatistiques,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean actif,
        @Schema(description = "Fiche professionnelle du contact principal (RG-OPE-22)") Long contactPrincipal,
        @Schema(description = "Fiche professionnelle du destinataire des factures (RG-OPE-23)") Long destinataireFacturesFiche,
        @Schema(description = "Adresse saisie du destinataire des factures (RG-OPE-23)") String destinataireFacturesEmail,
        @Schema(description = "Adresse qui en résulte, lue par la facturation (RG-OPE-23)") String emailFacturation,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Ce qui manque pour facturer ou certifier (RG-OPE-02) ; vide si la fiche est complète")
        List<String> champsManquants,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Faux si le code n'a plus la forme de son type ou de son département (RG-OPE-05)")
        boolean codeConforme,
        @Schema(description = "Si le code n'est plus conforme : le code qu'« attribuer un nouveau code » donnerait") String codePropose,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Autres fiches du même groupe (RG-OPE-09)")
        List<OperateurResumeResponse> autresDuGroupe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "À renvoyer avec chaque modification (RG-OPE-51)")
        long version,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) DroitsResponse droits) {

    /**
     * RG-OPE-36 : hors du personnel du BNIA, le commentaire interne est masqué, et les signalements propres
     * au BNIA (code proposé, autres fiches du groupe) ne sont pas montrés.
     */
    static FicheOperateurResponse depuis(FicheOperateur fiche) {
        boolean bnia = fiche.droits().personnelBnia();
        Operateur o = fiche.operateur();
        IdentiteOperateur i = o.identite();
        var a = i.adresse();
        return new FicheOperateurResponse(o.id(), o.type(), o.code().valeur(), i.groupe(), texte(i.cvi()),
                texte(i.siret()), texte(i.accise()), i.bureauDouanes(), i.identifiantOdg(), i.titre(),
                i.raisonSociale(), i.nomCommercial(), a.ligne1(), a.ligne2(), a.ligne3(), a.codePostal(), a.ville(),
                a.pays(), i.telephone1(), i.telephone2(), i.portable(), texte(i.emailGeneral()), texte(i.siteWeb()),
                texte(i.siteCommercial()), i.gps() == null ? null : i.gps().latitude(),
                i.gps() == null ? null : i.gps().longitude(), i.membreBnia(), i.accordDiffusionGeneral(),
                bnia ? i.commentaireInterne() : null, o.declaratif().periodicite(),
                PeriodiciteDsm.parDefaut(o.type()).orElse(null), o.declaratif().statut(), o.production().region(),
                o.production().appellations().stream().sorted().toList(),
                o.production().alambics().stream()
                        .map(al -> new AlambicResponse(al.numeroAgrement(), al.type(), al.capacite())).toList(),
                o.production().detenteurAlambic(), o.diffusion().niveau(),
                o.diffusion().millesimes().stream().map(Millesime::annee).toList(), o.accesStatistiques(),
                o.estActif(), o.contactPrincipal().orElse(null),
                o.destinataireFactures().map(DestinataireFactures::fiche).orElse(null),
                o.destinataireFactures().map(d -> texte(d.adresse())).orElse(null),
                fiche.emailFacturation().orElse(null),
                fiche.champsManquants(), fiche.codeConforme(),
                bnia ? fiche.codePropose().map(CodeOperateur::valeur).orElse(null) : null,
                bnia ? fiche.autresDuGroupe().stream().map(OperateurResumeResponse::depuis).toList() : List.of(),
                o.version(), DroitsResponse.depuis(fiche.droits()));
    }

    @Schema(name = "Alambic")
    public record AlambicResponse(
            String numeroAgrement,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) TypeAlambic type,
            BigDecimal capacite) {
    }

    private static String texte(Object valeur) {
        return valeur == null ? null : Objects.toString(valeur);
    }
}
