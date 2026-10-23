package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.contratsarmagnac.domain.Auteur;
import fr.bnia.contratsarmagnac.domain.ContenuContrat;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.ControleStock;
import fr.bnia.contratsarmagnac.domain.NumeroContrat;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.declarations.application.partage.DeclarationsLecture;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Négociation et conclusion d'un contrat (UC-CA-1, UC-CA-2) : ouvrir un projet, le modifier, l'approuver, le refuser.
 * Au portail, par l'opérateur actif, partie au contrat. RG-CA-01 à 12, 18, 19.
 */
@UseCase
public class NegocierContrats {

    private final Numerotation numerotation;
    private final OperateursLecture operateurs;
    private final DeclarationsLecture declarations;
    private final Autorisations autorisations;
    private final Registre registre;
    private final Clock horloge;

    public NegocierContrats(ContratRepository contrats, Numerotation numerotation, JournalContrats journal,
            EnvoiCourriels envoi, OperateursLecture operateurs, DeclarationsLecture declarations,
            ParametresContrats parametres, Clock horloge) {
        this.numerotation = numerotation;
        this.operateurs = operateurs;
        this.declarations = declarations;
        this.autorisations = new Autorisations(operateurs);
        this.registre = new Registre(contrats, journal, envoi, operateurs, parametres, horloge);
        this.horloge = horloge;
    }

    /**
     * Un projet saisi au portail (UC-CA-1).
     *
     * @param partie      la partie que tient l'opérateur actif
     * @param autrePartie l'autre opérateur, choisi par la recherche restreinte (RG-OPE-37)
     */
    public record SaisieContrat(Partie partie, long autrePartie, SaisieContenu contenu) {
    }

    /** RG-CA-01 à 06, 12, 18, 19 : ouvre le projet ; le vendeur ne propose pas plus que son stock estimé. */
    public ContratAchatArmagnac ouvrir(Acteur acteur, long operateur, SaisieContrat saisie) {
        autorisations.exigerGestion(acteur, operateur);
        Objects.requireNonNull(saisie.partie(), "partie");
        exigerAutrePartie(operateur, saisie.autrePartie());
        Auteur auteur = registre.auteur(acteur, saisie.partie());
        LocalDate aujourdhui = LocalDate.now(horloge);
        ContenuContrat contenu = saisie.contenu().versContenu(aujourdhui);
        if (saisie.partie() == Partie.VENDEUR) {
            ControleStock.verifier(contenu, declarations.stockEstime(operateur));
        }
        long acheteur = saisie.partie() == Partie.ACHETEUR ? operateur : saisie.autrePartie();
        long vendeur = saisie.partie() == Partie.VENDEUR ? operateur : saisie.autrePartie();
        String numero = NumeroContrat.de(aujourdhui.getYear(), numerotation.suivant(aujourdhui.getYear()));
        return registre.inserer(ContratAchatArmagnac.ouvrir(numero, acheteur, vendeur, auteur, contenu));
    }

    /** RG-CA-07, 12 : une nouvelle version, approuvée par son auteur seul. */
    public ContratAchatArmagnac modifier(Acteur acteur, long operateur, long id, int versionVue,
            SaisieContenu saisie) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        Partie partie = Autorisations.partie(contrat, operateur);
        ContenuContrat contenu = saisie.versContenu(LocalDate.now(horloge));
        if (partie == Partie.VENDEUR) {
            ControleStock.verifier(contenu, declarations.stockEstime(operateur));
        }
        contrat.modifier(registre.auteur(acteur, partie), versionVue, contenu);
        return registre.mettreAJour(contrat);
    }

    /** RG-CA-08, 12 : approuve la version courante ; le vendeur, seulement si son stock estimé la couvre encore. */
    public ContratAchatArmagnac approuver(Acteur acteur, long operateur, long id, int versionVue) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        Partie partie = Autorisations.partie(contrat, operateur);
        if (partie == Partie.VENDEUR) {
            ControleStock.verifier(contrat.courante().contenu(), declarations.stockEstime(operateur));
        }
        contrat.approuver(registre.auteur(acteur, partie), versionVue);
        return registre.mettreAJour(contrat);
    }

    /** RG-CA-10 : refuse le projet, définitivement. */
    public ContratAchatArmagnac refuser(Acteur acteur, long operateur, long id, int versionVue) {
        autorisations.exigerGestion(acteur, operateur);
        ContratAchatArmagnac contrat = registre.charger(id);
        contrat.refuser(registre.auteur(acteur, Autorisations.partie(contrat, operateur)), versionVue);
        return registre.mettreAJour(contrat);
    }

    /** RG-CA-02 : un autre opérateur, actif, qui n'est pas le BNIA ; aucun type n'est exclu. */
    private void exigerAutrePartie(long operateur, long autre) {
        if (autre == operateur) {
            throw new RegleEnfreinte("RG-CA-02", "L'autre partie est un autre opérateur que le vôtre.");
        }
        if (autre == operateurs.operateurBnia()) {
            throw new RegleEnfreinte("RG-CA-02", "Le BNIA n'est partie à aucun contrat.");
        }
        OperateurVue vue = operateurs.operateur(autre)
                .orElseThrow(() -> new RegleEnfreinte("RG-CA-02", "L'autre partie est introuvable."));
        if (!vue.actif()) {
            throw new RegleEnfreinte("RG-CA-02", vue.raisonSociale() + " n'est plus actif : il ne peut être partie.");
        }
    }
}
