package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.contratsarmagnac.domain.ContenuRetiraison;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.ControleStock;
import fr.bnia.contratsarmagnac.domain.Grain;
import fr.bnia.contratsarmagnac.domain.LigneContrat;
import fr.bnia.contratsarmagnac.domain.LigneRetiraison;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.contratsarmagnac.domain.Retiraison;
import fr.bnia.declarations.application.partage.DeclarationsLecture;
import fr.bnia.declarations.domain.partage.RetiraisonValidee;
import fr.bnia.declarations.domain.partage.RetiraisonValidee.LigneRetiree;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Exécution d'un contrat conclu (UC-CA-3, 4, 5) : l'acheteur déclare ou retire une retiraison, la partie attendue la
 * modifie ou la valide ; une partie annule le contrat, ou déclare qu'il n'aura plus de retiraison. Au portail, par
 * l'opérateur actif, partie au contrat. RG-CA-01, 11, 13 à 19.
 */
@UseCase
public class ExecuterContrats {

    /** Le refus montré à l'acheteur, sans aucun chiffre du stock du vendeur (RG-CA-13). */
    static final String STOCK_DU_VENDEUR = "Le stock estimé du vendeur ne couvre pas cette retiraison : voyez avec lui.";

    private final DeclarationsLecture declarations;
    private final PublicationEvenements evenements;
    private final Autorisations autorisations;
    private final Registre registre;
    private final Clock horloge;

    public ExecuterContrats(ContratRepository contrats, JournalContrats journal, EnvoiCourriels envoi,
            PublicationEvenements evenements, OperateursLecture operateurs, DeclarationsLecture declarations,
            ParametresContrats parametres, Clock horloge) {
        this.declarations = declarations;
        this.evenements = evenements;
        this.autorisations = new Autorisations(operateurs);
        this.registre = new Registre(contrats, journal, envoi, operateurs, parametres, horloge);
        this.horloge = horloge;
    }

    /** Une retiraison saisie au portail : sa date d'enlèvement, « dernière » ou non, ses lignes. */
    public record SaisieRetiraison(LocalDate dateEnlevement, boolean derniere, List<SaisieLigneRetiraison> lignes) {

        /** @param rang rang de la ligne du contrat, à partir de 0 */
        public record SaisieLigneRetiraison(int rang, BigDecimal volume) {
        }

        ContenuRetiraison versContenu() {
            return new ContenuRetiraison(dateEnlevement, derniere, lignes == null ? List.of()
                    : lignes.stream().map(l -> new LigneRetiraison(l.rang(), l.volume())).toList());
        }
    }

    /** RG-CA-01, 13, 18, 19 : l'acheteur déclare une retiraison, que le stock estimé du vendeur doit couvrir. */
    public ContratAchatArmagnac declarerRetiraison(Acteur acteur, long operateur, long id, SaisieRetiraison saisie) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        Partie partie = Autorisations.partie(contrat, operateur);
        LocalDate aujourdhui = LocalDate.now(horloge);
        ContenuRetiraison contenu = saisie.versContenu();
        contrat.declarerRetiraison(registre.auteur(acteur, partie), contenu, aujourdhui);
        exigerStock("RG-CA-13", contrat, contenu, partie, aujourdhui);
        return registre.mettreAJour(contrat);
    }

    /** RG-CA-01, 14, 18, 19 : la partie attendue modifie la retiraison ; elle attend alors l'autre. */
    public ContratAchatArmagnac modifierRetiraison(Acteur acteur, long operateur, long id, int numero, int versionVue,
            SaisieRetiraison saisie) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        Partie partie = Autorisations.partie(contrat, operateur);
        LocalDate aujourdhui = LocalDate.now(horloge);
        ContenuRetiraison contenu = saisie.versContenu();
        contrat.modifierRetiraison(registre.auteur(acteur, partie), numero, versionVue, contenu, aujourdhui);
        exigerStock("RG-CA-14", contrat, contenu, partie, aujourdhui);
        return registre.mettreAJour(contrat);
    }

    /**
     * RG-CA-01, 15, 18, 19 : la partie attendue valide la retiraison, si le stock estimé du vendeur la couvre ;
     * {@link RetiraisonValidee} est publié dans la transaction : {@code declarations} en tire les mouvements de
     * contrat et les inscrit dans les DSM, et son échec annule la validation (A-CA-3).
     */
    public ContratAchatArmagnac validerRetiraison(Acteur acteur, long operateur, long id, int numero, int versionVue) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        Partie partie = Autorisations.partie(contrat, operateur);
        LocalDate aujourdhui = LocalDate.now(horloge);
        Retiraison retiraison = contrat.retiraison(numero);
        exigerStock("RG-CA-15", contrat, retiraison.courante().contenu(), partie, aujourdhui);
        contrat.validerRetiraison(registre.auteur(acteur, partie), numero, versionVue, aujourdhui);
        ContratAchatArmagnac enregistre = registre.mettreAJour(contrat);
        evenements.publier(evenement(enregistre, enregistre.retiraison(numero), acteur));
        return enregistre;
    }

    /** RG-CA-01, 16, 19 : l'acheteur retire une retiraison non validée. */
    public ContratAchatArmagnac retirerRetiraison(Acteur acteur, long operateur, long id, int numero) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        contrat.retirerRetiraison(registre.auteur(acteur, Autorisations.partie(contrat, operateur)), numero);
        return registre.mettreAJour(contrat);
    }

    /** RG-CA-01, 17 à 19 : une partie annule le contrat conclu. */
    public ContratAchatArmagnac annuler(Acteur acteur, long operateur, long id) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        contrat.annuler(registre.auteur(acteur, Autorisations.partie(contrat, operateur)));
        return registre.mettreAJour(contrat);
    }

    /** RG-CA-01, 17 à 19 : une partie déclare que le contrat conclu n'aura plus de retiraison ; il est exécuté. */
    public ContratAchatArmagnac cloturer(Acteur acteur, long operateur, long id) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        contrat.cloturer(registre.auteur(acteur, Autorisations.partie(contrat, operateur)));
        return registre.mettreAJour(contrat);
    }

    /**
     * RG-CA-13 à 15 : le stock estimé du vendeur couvre la retiraison, grain par grain ; au vendeur, le refus donne les
     * chiffres, à l'acheteur aucun.
     */
    private void exigerStock(String regle, ContratAchatArmagnac contrat, ContenuRetiraison contenu, Partie partie,
            LocalDate aujourdhui) {
        stockInsuffisant(declarations, contrat, contenu, aujourdhui).ifPresent(message -> {
            throw new RegleEnfreinte(regle, partie == Partie.VENDEUR ? message : STOCK_DU_VENDEUR);
        });
    }

    /** RG-CA-13, 15 : le message chiffré du premier grain de la retiraison que le stock du vendeur ne couvre pas. */
    static Optional<String> stockInsuffisant(DeclarationsLecture declarations, ContratAchatArmagnac contrat,
            ContenuRetiraison contenu, LocalDate aujourdhui) {
        LocalDate conclu = contrat.dateContrat().orElseThrow();
        List<LigneContrat> lignes = contrat.courante().contenu().lignes();
        Map<Grain, VolumeAlcoolPur> demande = contrat.volumeApParGrain(contenu, aujourdhui);
        return ControleStock.depassement(demande, grain -> lignes.stream()
                        .filter(l -> l.grainAu(conclu, aujourdhui).equals(grain)).findFirst()
                        .map(LigneContrat::libelle).orElseThrow(), "à retirer",
                declarations.stockEstime(contrat.vendeur()));
    }

    /** RG-CA-15, 21 : l'événement d'une retiraison validée, grains vieillis à la date d'enlèvement (H-CA-11). */
    private static RetiraisonValidee evenement(ContratAchatArmagnac contrat, Retiraison retiraison, Acteur validePar) {
        ContenuRetiraison contenu = retiraison.courante().contenu();
        LocalDate conclu = contrat.dateContrat().orElseThrow();
        List<LigneRetiree> lignes = contenu.lignes().stream().map(l -> {
            LigneContrat ligne = contrat.courante().contenu().lignes().get(l.rang());
            Grain grain = ligne.grainAu(conclu, contenu.dateEnlevement());
            return new LigneRetiree(grain.cle(), grain.millesime(), ligne.denomination(), grain.compte(),
                    ligne.volumeAp(l.volume()));
        }).toList();
        return new RetiraisonValidee(contrat.id(), contrat.numero(), retiraison.numero(), contenu.dateEnlevement(),
                contrat.vendeur(), contrat.acheteur(), validePar.utilisateur(), lignes);
    }
}
