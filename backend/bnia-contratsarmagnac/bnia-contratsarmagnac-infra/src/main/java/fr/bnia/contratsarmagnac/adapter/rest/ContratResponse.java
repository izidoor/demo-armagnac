package fr.bnia.contratsarmagnac.adapter.rest;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.contratsarmagnac.application.ConsulterContrats.VueContrat;
import fr.bnia.contratsarmagnac.application.EntreeJournal;
import fr.bnia.contratsarmagnac.domain.ActionContrat;
import fr.bnia.contratsarmagnac.domain.Auteur;
import fr.bnia.contratsarmagnac.domain.ContenuContrat;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.Ecart;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.EtatRetiraison;
import fr.bnia.contratsarmagnac.domain.LigneContrat;
import fr.bnia.contratsarmagnac.domain.LigneRetiraison;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.contratsarmagnac.domain.Retiraison;
import fr.bnia.contratsarmagnac.domain.Solde;
import fr.bnia.contratsarmagnac.domain.VersionContrat;
import fr.bnia.contratsarmagnac.domain.VersionRetiraison;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Schema(name = "Contrat", description = "Un contrat d'achat Armagnac : sa version courante, ses approbations, l'écart et le journal (spec 06 §6)")
public record ContratResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "CA-2026-0001") String numero,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EtatContrat etat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-2027") String campagne,
        @Schema(description = "Date de la conclusion") LocalDate dateContrat,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Partie ouvertPar,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PartieResponse acheteur,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PartieResponse vendeur,
        @Schema(description = "Au portail : la partie de l'opérateur actif") Partie maPartie,
        @Schema(description = "En projet : la partie dont l'approbation manque (RG-CA-08)") Partie attend,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Au portail : l'acteur gère le module dans l'opérateur actif")
        boolean peutAgir,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) VersionResponse version,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Approbations de la version courante")
        List<ApprobationResponse> approbations,
        @Schema(description = "En projet : l'écart montré à la partie qui doit approuver (RG-CA-09)") EcartResponse ecart,
        @Schema(description = "Au vendeur, en projet : le grain que son stock estimé ne couvre plus (RG-CA-12)")
        String stockInsuffisant,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Par ligne du contrat : le volume retiré, en cours de validation, et le reste à retirer (RG-CA-13)")
        List<ResteResponse> restes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Dans l'ordre de leur déclaration (spec 06 §3.4)")
        List<RetiraisonResponse> retiraisons,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<EntreeJournalResponse> journal) {

    @Schema(name = "AuteurContrat")
    public record AuteurResponse(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) Partie partie, @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom, @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant le) {

        static AuteurResponse depuis(Auteur a) {
            return new AuteurResponse(a.partie(), a.nom(), a.le());
        }
    }

    @Schema(name = "ApprobationContrat")
    public record ApprobationResponse(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) Partie partie, @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom, @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant le) {
    }

    @Schema(name = "VersionContrat", description = "Une version du contenu (spec 06 §3.2) et ses calculs (RG-CA-04)")
    public record VersionResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int numero,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AuteurResponse auteur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneResponse> lignes,
            String caracteristiques,
            String conditionsRetiraison,
            LocalDate aRetirerAvantLe,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Solde solde,
            BigDecimal acompte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal volumeAp,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€ HT") BigDecimal montantHt) {

        static VersionResponse depuis(VersionContrat v) {
            ContenuContrat c = v.contenu();
            return new VersionResponse(v.numero(), AuteurResponse.depuis(v.auteur()),
                    c.lignes().stream().map(LigneResponse::depuis).toList(), c.caracteristiques(), c.conditionsRetiraison(), c.aRetirerAvantLe(), c.paiement().solde(),
                    c.paiement().acompte(), c.volumeAp().litres(), c.montantHt());
        }
    }

    @Schema(name = "LigneContrat", description = "Une ligne du contrat et ses calculs (spec 06 §3.3, RG-CA-04)")
    public record LigneResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Appellation appellation,
            Integer millesime,
            String denomination,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "16") String compte,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Hectolitres") BigDecimal volume,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "% vol.") BigDecimal degre,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€ HT / hl AP") BigDecimal prix,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur") BigDecimal volumeAp,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€ HT / litre de volume") BigDecimal prixLitreVolume,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "€ HT") BigDecimal montantHt) {

        static LigneResponse depuis(LigneContrat l) {
            return new LigneResponse(l.appellation(), l.millesime() == null ? null : l.millesime().annee(),
                    l.denomination(), l.compte().toString(), l.volume(), l.degre(), l.prix(), l.volumeAp().litres(),
                    l.prixLitreVolume(), l.montantHt());
        }
    }

    @Schema(name = "EcartContrat", description = "Ce qui a changé depuis la dernière version approuvée par la partie qui doit approuver (RG-CA-09)")
    public record EcartResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Dernière version approuvée par cette partie") int depuis,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Version courante") int jusqua,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Auteurs des versions intermédiaires")
            List<AuteurResponse> auteurs,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ChangementResponse> champs,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<EcartLigneResponse> lignes) {

        static EcartResponse depuis(Ecart e) {
            return new EcartResponse(e.depuis(), e.jusqua(), e.auteurs().stream().map(AuteurResponse::depuis).toList(),
                    e.champs().stream().map(ChangementResponse::depuis).toList(),
                    e.lignes().stream().map(l -> new EcartLigneResponse(l.rang(), l.nature(),
                            l.champs().stream().map(ChangementResponse::depuis).toList())).toList());
        }
    }

    @Schema(name = "ChangementContrat",
            description = "Un champ changé ; valeurs brutes (nombres en notation simple, énumérations par leur nom, dates ISO), absentes si vides")
    public record ChangementResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "prix") String champ,
            String avant,
            String apres) {

        static ChangementResponse depuis(Ecart.Changement c) {
            return new ChangementResponse(c.champ(), c.avant(), c.apres());
        }
    }

    @Schema(name = "EcartLigneContrat")
    public record EcartLigneResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Rang de la ligne, à partir de 0") int rang,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Ecart.Nature nature,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ChangementResponse> champs) {
    }

    @Schema(name = "ResteContrat", description = "Une ligne du contrat et ses retiraisons (RG-CA-13), en hl")
    public record ResteResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Rang de la ligne, à partir de 0") int rang,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal retire,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal enCours,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal reste) {
    }

    @Schema(name = "RetiraisonContrat", description = "Une retiraison (spec 06 §3.4) : sa version courante, ses approbations, son écart")
    public record RetiraisonResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int numero,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EtatRetiraison etat,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "À valider par le vendeur") String libelleEtat,
            @Schema(description = "La partie qui doit la valider ; vide une fois validée") Partie attend,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int version,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AuteurResponse auteur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dateEnlevement,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean derniere,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<LigneRetiraisonResponse> lignes,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Approbations de la version courante")
            List<ApprobationResponse> approbations,
            @Schema(description = "À valider après une modification : l'écart montré à la partie attendue (RG-CA-14)")
            EcartResponse ecart,
            @Schema(description = "Au vendeur qui doit la valider : le grain que son stock estimé ne couvre pas (RG-CA-15)")
            String stockInsuffisant) {
    }

    @Schema(name = "LigneRetiraison")
    public record LigneRetiraisonResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Rang de la ligne du contrat") int rang,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Hectolitres") BigDecimal volume,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Litres d'alcool pur, au degré de la ligne")
            BigDecimal volumeAp) {
    }

    @Schema(name = "EntreeJournalContrat")
    public record EntreeJournalResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant date,
            Partie partie,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nom,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) ActionContrat action,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Modification") String libelle,
            @Schema(description = "Numéro de version ou de retiraison") Integer detail) {

        static EntreeJournalResponse depuis(EntreeJournal e) {
            return new EntreeJournalResponse(e.date(), e.partie(), e.nom(), e.action(), e.action().libelle(),
                    e.detail());
        }
    }

    static ContratResponse depuis(VueContrat vue) {
        ContratAchatArmagnac c = vue.contrat();
        List<ApprobationResponse> approbations = List.of(Partie.values()).stream()
                .flatMap(p -> c.approbationCourante(p).stream())
                .map(a -> new ApprobationResponse(a.partie(), a.auteur().nom(), a.auteur().le()))
                .toList();
        return new ContratResponse(c.id(), c.numero(), c.etat(), vue.campagne(), c.dateContrat().orElse(null),
                c.ouvertPar(), PartieResponse.depuis(vue.acheteur()), PartieResponse.depuis(vue.vendeur()),
                vue.maPartie().orElse(null), c.partieQuiDoitApprouver().orElse(null), vue.peutAgir(),
                VersionResponse.depuis(c.courante()), approbations, vue.ecart().map(EcartResponse::depuis).orElse(null),
                vue.stockInsuffisant().orElse(null), restes(c),
                c.retiraisons().stream().map(r -> retiraison(c, r, vue.retiraisonsSansStock().get(r.numero())))
                        .toList(),
                vue.journal().stream().map(EntreeJournalResponse::depuis).toList());
    }

    /** RG-CA-13 : par ligne, retiré (validé), en cours, reste. */
    private static List<ResteResponse> restes(ContratAchatArmagnac c) {
        List<LigneContrat> lignes = c.courante().contenu().lignes();
        return IntStream.range(0, lignes.size()).mapToObj(rang -> {
            BigDecimal retire = somme(c, rang, true);
            BigDecimal enCours = somme(c, rang, false);
            return new ResteResponse(rang, retire, enCours, c.resteARetirer(rang, null));
        }).toList();
    }

    private static BigDecimal somme(ContratAchatArmagnac c, int rang, boolean validees) {
        return c.retiraisons().stream().filter(r -> r.validee() == validees)
                .flatMap(r -> r.courante().contenu().lignes().stream())
                .filter(l -> l.rang() == rang).map(LigneRetiraison::volume)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private static RetiraisonResponse retiraison(ContratAchatArmagnac c, Retiraison r, String stockInsuffisant) {
        VersionRetiraison v = r.courante();
        List<LigneContrat> lignes = c.courante().contenu().lignes();
        return new RetiraisonResponse(r.numero(), r.etat(), r.etat().libelle(), r.partieQuiDoitValider().orElse(null),
                v.numero(), AuteurResponse.depuis(v.auteur()), v.contenu().dateEnlevement(), v.contenu().derniere(),
                v.contenu().lignes().stream().map(l -> new LigneRetiraisonResponse(l.rang(), l.volume(),
                        lignes.get(l.rang()).volumeAp(l.volume()).litres())).toList(),
                r.approbations().stream().filter(a -> a.version() == v.numero())
                        .map(a -> new ApprobationResponse(a.partie(), a.auteur().nom(), a.auteur().le())).toList(),
                r.partieQuiDoitValider().filter(p -> v.numero() > 1).map(p -> EcartResponse.depuis(r.ecartPour(p)))
                        .orElse(null),
                stockInsuffisant);
    }
}
