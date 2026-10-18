package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.NoteInterne;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.time.Clock;
import java.util.List;

/**
 * Carnet de notes internes d'une DSM (spec 15 §3.7, lot 1) : lu par tout le personnel du BNIA, écrit par qui gère
 * les Déclarations au BNIA, jamais montré à l'opérateur ; la note vise la DSM ou une ligne ; l'auteur modifie ou
 * supprime la sienne. RG-DEC-01, 24.
 */
@UseCase
public class AnnoterDsm {

    private final DsmRepository dsms;
    private final NotesInternes notes;
    private final Autorisations autorisations;
    private final Journaliste journaliste;

    public AnnoterDsm(DsmRepository dsms, NotesInternes notes, JournalDeclarations journal, OperateursLecture operateurs,
            Clock horloge) {
        this.dsms = dsms;
        this.notes = notes;
        this.autorisations = new Autorisations(operateurs);
        this.journaliste = new Journaliste(journal, operateurs, horloge);
    }

    /** RG-DEC-01, 24 : les notes de la DSM, du plus ancien au plus récent ; personnel du BNIA seulement. */
    public List<NoteInterne> lister(Acteur acteur, long operateur, long dsm) {
        exigerLecture(acteur, operateur);
        dsm(operateur, dsm);
        return notes.deLaDsm(dsm);
    }

    /** RG-DEC-01, 24 : une note sur la DSM ({@code ligne} absente) ou sur une de ses lignes. */
    public NoteInterne ecrire(Acteur acteur, long operateur, long dsm, Long ligne, String texte) {
        autorisations.exigerBnia(acteur, operateur);
        DeclarationStocksMouvements declaration = dsm(operateur, dsm);
        if (ligne != null && declaration.lignes().stream().noneMatch(l -> ligne.equals(l.id()))) {
            throw new ElementIntrouvable("La ligne n° " + ligne + " n'est pas une ligne de la DSM de "
                    + declaration.periode().libelle() + " : rechargez l'écran.");
        }
        return notes.inserer(NoteInterne.ecrire(dsm, ligne, journaliste.auteur(acteur), texte,
                journaliste.maintenant()));
    }

    /** RG-DEC-24 : l'auteur modifie sa note. */
    public NoteInterne modifier(Acteur acteur, long operateur, long dsm, long id, String texte) {
        autorisations.exigerBnia(acteur, operateur);
        dsm(operateur, dsm);
        NoteInterne note = note(dsm, id);
        note.modifier(acteur.utilisateur(), texte, journaliste.maintenant());
        notes.mettreAJour(note);
        return note;
    }

    /** RG-DEC-24 : l'auteur supprime sa note, sans trace au journal, que l'opérateur lit. */
    public void supprimer(Acteur acteur, long operateur, long dsm, long id) {
        autorisations.exigerBnia(acteur, operateur);
        dsm(operateur, dsm);
        note(dsm, id).exigerSupprimable(acteur.utilisateur());
        notes.supprimer(id);
    }

    private void exigerLecture(Acteur acteur, long operateur) {
        autorisations.exigerLecture(acteur, operateur);
        if (!autorisations.droits(acteur, operateur).personnelBnia()) {
            throw new AccesRefuse("RG-DEC-24", "Les notes internes sont réservées au personnel du BNIA.");
        }
    }

    private NoteInterne note(long dsm, long id) {
        return notes.parId(id).filter(n -> n.dsm() == dsm)
                .orElseThrow(() -> new ElementIntrouvable("Note interne introuvable : n° " + id + "."));
    }

    private DeclarationStocksMouvements dsm(long operateur, long id) {
        return dsms.parId(id).filter(d -> d.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("DSM introuvable : n° " + id + "."));
    }
}
