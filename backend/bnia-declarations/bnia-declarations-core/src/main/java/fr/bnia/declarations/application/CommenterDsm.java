package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.Commentaire;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.partage.Cote;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.time.Clock;
import java.util.List;

/**
 * Commentaires d'instruction d'une DSM (spec 05 §3.2.4, §6.3) : l'opérateur et le BNIA commentent la DSM ou
 * une ligne, dans tout état ; le BNIA seul écrit un commentaire interne, jamais montré à l'opérateur ;
 * l'auteur modifie ou supprime le sien. RG-DEC-01, 23 à 25.
 */
@UseCase
public class CommenterDsm {

    private final DsmRepository dsms;
    private final Commentaires commentaires;
    private final Autorisations autorisations;
    private final Journaliste journaliste;

    public CommenterDsm(DsmRepository dsms, Commentaires commentaires, JournalDeclarations journal,
            OperateursLecture operateurs, Clock horloge) {
        this.dsms = dsms;
        this.commentaires = commentaires;
        this.autorisations = new Autorisations(operateurs);
        this.journaliste = new Journaliste(journal, operateurs, horloge);
    }

    /** RG-DEC-01, 24 : le fil de la DSM, du plus ancien au plus récent ; sans les internes pour l'opérateur. */
    public List<Commentaire> lister(Acteur acteur, long operateur, long dsm) {
        autorisations.exigerLecture(acteur, operateur);
        dsm(operateur, dsm);
        boolean personnelBnia = autorisations.droits(acteur, operateur).personnelBnia();
        return commentaires.deLaDsm(dsm).stream().filter(c -> c.visibleDe(personnelBnia)).toList();
    }

    /** RG-DEC-01, 24 : commente la DSM ({@code ligne} absente) ou une de ses lignes. */
    public Commentaire ecrire(Acteur acteur, long operateur, long dsm, Long ligne, String texte, boolean interne) {
        Cote cote = autorisations.exigerCommentaire(acteur, operateur);
        DeclarationStocksMouvements declaration = dsm(operateur, dsm);
        if (ligne != null && declaration.lignes().stream().noneMatch(l -> ligne.equals(l.id()))) {
            throw new ElementIntrouvable("La ligne n° " + ligne + " n'est pas une ligne de la DSM de "
                    + declaration.periode().libelle() + " : rechargez l'écran.");
        }
        return commentaires.inserer(Commentaire.ecrire(dsm, ligne, journaliste.auteur(acteur), cote, texte, interne,
                journaliste.maintenant()));
    }

    /** RG-DEC-01, 25 : l'auteur modifie son commentaire. */
    public Commentaire modifier(Acteur acteur, long operateur, long dsm, long id, String texte, boolean interne) {
        autorisations.exigerCommentaire(acteur, operateur);
        dsm(operateur, dsm);
        Commentaire commentaire = commentaire(dsm, id);
        commentaire.modifier(acteur.utilisateur(), texte, interne, journaliste.maintenant());
        commentaires.mettreAJour(commentaire);
        return commentaire;
    }

    /** RG-DEC-01, 23, 25 : l'auteur supprime son commentaire, sauf le motif d'une demande de compléments. */
    public void supprimer(Acteur acteur, long operateur, long dsm, long id) {
        autorisations.exigerCommentaire(acteur, operateur);
        DeclarationStocksMouvements declaration = dsm(operateur, dsm);
        Commentaire commentaire = commentaire(dsm, id);
        commentaire.exigerSupprimable(acteur.utilisateur());
        commentaires.supprimer(id);
        journaliste.noter(acteur, declaration, ActionJournalDeclarations.COMMENTAIRE_SUPPRIME, null);
    }

    private Commentaire commentaire(long dsm, long id) {
        return commentaires.parId(id).filter(c -> c.dsm() == dsm)
                .orElseThrow(() -> new ElementIntrouvable("Commentaire introuvable : n° " + id + "."));
    }

    private DeclarationStocksMouvements dsm(long operateur, long id) {
        return dsms.parId(id).filter(d -> d.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("DSM introuvable : n° " + id + "."));
    }
}
