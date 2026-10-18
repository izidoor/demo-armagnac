package fr.bnia.facturation.adapter.rest;

import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.application.VuePiece;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.facturation.domain.CategorieAcheteur;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.EtatZeendoc;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import fr.bnia.facturation.domain.LigneFacture.LigneInformative;
import fr.bnia.facturation.domain.LigneFacture;
import fr.bnia.facturation.domain.NatureOperation;
import fr.bnia.facturation.domain.SensPiece;
import fr.bnia.facturation.domain.SuiviZeendoc;
import fr.bnia.facturation.domain.Totaux;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "Piece", description = "Une facture ou un avoir, avec ses lignes, son récapitulatif de TVA et ses totaux (spec 08 §3.1, §6.1)")
public record PieceResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SensPiece sens,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EtatPiece etat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Libellé de la nature d'origine, ou « Avoir »")
        String titre,
        @Schema(description = "Vide pour un brouillon") String numero,
        @Schema(description = "Vide pour un brouillon") LocalDate dateEmission,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dateOperation,
        String libellePeriode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) ConditionResponse condition,
        @Schema(description = "Calculée à l'émission (RG-FAC-13)") LocalDate echeance,
        @Schema(description = "Vide pour un avoir") OrigineResponse origine,
        String bonDeCommande,
        String noteEntete,
        String notePied,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AcheteurResponse acheteur,
        @Schema(description = "Recopié à l'émission ; vide en brouillon") VendeurDto vendeur,
        @Schema(description = "RG-FAC-08 ; vide sans ligne article") NatureOperation natureOperation,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneResponse> lignes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneTvaResponse> recapitulatifTva,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalHt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalTva,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalTtc,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Ce qui empêche l'émission d'un brouillon (RG-FAC-09) ; vide sinon")
        List<String> empechements,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Brouillon que l'utilisateur connecté peut modifier, émettre ou abandonner (RG-FAC-26)")
        boolean emettre,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "L'utilisateur connecté gère la facturation : il annote la pièce (RG-FAC-21, 26)")
        boolean gerer,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Le PDF est disponible (pièce émise)")
        boolean pdf,
        @Schema(description = "État dans Zeendoc ; vide tant que la pièce n'est pas transmise (RG-FAC-32, 33)")
        EtatZeendoc zeendoc,
        @Schema(description = "Vide tant que la pièce n'est pas transmise") Instant transmiseLe,
        @Schema(description = "Vide tant que la pièce n'est pas payée") LocalDate payeeLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant creeeLe,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Copie du nom de l'auteur de la demande")
        String creeePar,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Version de la pièce, renvoyée à la modification d'un brouillon (RG-FAC-17)")
        long version) {

    @Schema(name = "ConditionPaiement", description = "Condition de paiement (spec 08 §3.6)")
    public record ConditionResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int jours,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean finDeMois,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "30 jours fin de mois") String libelle) {
    }

    @Schema(name = "OriginePiece", description = "Ce qu'une facture facture, fourni par la source (spec 08 §3.4)")
    public record OrigineResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "declarations") String source,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "DSM") String nature,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String libelleNature,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identifiant opaque chez la source")
            String identifiant,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "DSM juillet 2026") String reference) {

        static OrigineResponse de(Origine o) {
            return o == null ? null : new OrigineResponse(o.source(), o.nature(), o.libelleNature(), o.identifiant(),
                    o.reference());
        }
    }

    @Schema(name = "AcheteurPiece", description = "L'acheteur recopié sur la pièce (spec 08 §3.5)")
    public record AcheteurResponse(
            @Schema(description = "Identifiant du client chez l'hôte ; vide pour un client libre") String clientIdentifiant,
            @Schema(description = "N° BNIA du client") String clientCode,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CategorieAcheteur categorie,
            String nom,
            String siren,
            String siret,
            String numeroTva,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AdresseDto adresseFacturation,
            AdresseDto adresseLivraison,
            String email,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Ce qui manque pour qu'il soit complet (RG-FAC-10)")
            List<String> manques) {

        static AcheteurResponse de(Acheteur a) {
            return new AcheteurResponse(a.client() == null ? null : a.client().identifiant(),
                    a.client() == null ? null : a.client().code(), a.categorie(), a.nom(), a.siren(), a.siret(),
                    a.numeroTva(), AdresseDto.de(a.adresseFacturation()), AdresseDto.de(a.adresseLivraison()),
                    a.email(), a.manques());
        }
    }

    @Schema(name = "LignePiece", description = "Une ligne de la pièce : article, texte, ou groupe et ses lignes (spec 08 §3.2, RG-FAC-04)")
    public record LigneResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identité stable de la ligne dans la pièce")
            int numero,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                    description = "Reçue de la source : seules sa désignation et sa position se modifient (RG-FAC-17)")
            boolean recue,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Sorte sorte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                    description = "Désignation d'un article, texte, ou titre d'un groupe") String texte,
            @Schema(description = "Article : source") String source,
            @Schema(description = "Article : code dans sa source", example = "CVO-SORTIES_FRANCE-S") String code,
            String unite,
            NatureArticle nature,
            BigDecimal quantite,
            @Schema(description = "HT, 4 décimales") BigDecimal prixUnitaire,
            CategorieTva.Code tvaCode,
            BigDecimal tvaTaux,
            @Schema(example = "20,00 %") String tvaLibelle,
            @Schema(description = "Article : HT de la ligne ; groupe : sous-total HT") BigDecimal montantHt,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Groupe : ses lignes ; vide sinon")
            List<LigneResponse> lignes) {

        public enum Sorte {
            ARTICLE,
            TEXTE,
            GROUPE
        }

        static LigneResponse de(LigneFacture ligne) {
            return switch (ligne) {
                case LigneArticle a -> new LigneResponse(a.numero(), a.recue(), Sorte.ARTICLE, a.designation(), a.article().source(),
                        a.article().code(), a.unite(), a.nature(), a.quantite(), a.prixUnitaire(), a.tva().code(),
                        a.tva().taux(), a.tva().libelle(), a.montantHt(), List.of());
                case LigneInformative i -> new LigneResponse(i.numero(), i.recue(), Sorte.TEXTE, i.texte(), null, null, null, null, null, null,
                        null, null, null, null, List.of());
                case LigneGroupe g -> new LigneResponse(g.numero(), g.recue(), Sorte.GROUPE, g.titre(), null, null, null, null, null, null,
                        null, null, null, g.sousTotalHt(), g.lignes().stream().map(LigneResponse::de).toList());
            };
        }
    }

    @Schema(name = "LigneTvaPiece", description = "Une ligne du récapitulatif de TVA (RG-FAC-05)")
    public record LigneTvaResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CategorieTva.Code code,
            @Schema(description = "Vide hors champ") BigDecimal taux,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "20,00 %") String libelle,
            @Schema(description = "Motif d'exonération") String motif,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal base,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal tva) {
    }

    static PieceResponse de(VuePiece vue) {
        Facture p = vue.piece();
        Totaux totaux = p.totaux();
        return new PieceResponse(p.id(), p.sens(), p.etat(), p.titre(), p.numero().orElse(null),
                p.dateEmission().orElse(null), p.dateOperation(), p.libellePeriode(),
                new ConditionResponse(p.condition().jours(), p.condition().finDeMois(), p.condition().libelle()),
                p.echeance().orElse(null), OrigineResponse.de(p.origine()), p.bonDeCommande(), p.noteEntete(),
                p.notePied(), AcheteurResponse.de(p.acheteur()), p.vendeur().map(VendeurDto::de).orElse(null),
                p.natureOperation().orElse(null), p.lignes().stream().map(LigneResponse::de).toList(),
                p.recapitulatifTva().stream().map(t -> new LigneTvaResponse(t.categorie().code(), t.categorie().taux(),
                        t.categorie().libelle(), t.categorie().motif(), t.base(), t.tva())).toList(),
                totaux.ht(), totaux.tva(), totaux.ttc(), vue.empechements(), vue.emettre(), vue.gerer(),
                p.pdf().isPresent(), p.zeendoc().map(SuiviZeendoc::etat).orElse(null),
                p.zeendoc().map(SuiviZeendoc::transmiseLe).orElse(null),
                p.zeendoc().map(SuiviZeendoc::payeeLe).orElse(null), p.creeeLe(), p.creePar().nom(), p.version());
    }
}
