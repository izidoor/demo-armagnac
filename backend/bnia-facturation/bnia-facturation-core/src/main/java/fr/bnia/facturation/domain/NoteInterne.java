package fr.bnia.facturation.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import java.util.Objects;

/**
 * Note interne du BNIA sur une pièce (RG-FAC-21), comme celle d'une DSM : signée et datée, sur un brouillon comme
 * sur une pièce émise, jamais imprimée, transmise ni montrée au client. Enregistrée à part de la pièce et de son
 * journal : l'écrire ne change pas sa version.
 */
public final class NoteInterne {

    private final Long id;
    private final long piece;
    private final Auteur auteur;
    private final Instant ecriteLe;
    private Instant modifieeLe;
    private String texte;

    private NoteInterne(Long id, long piece, Auteur auteur, Instant ecriteLe, Instant modifieeLe, String texte) {
        this.id = id;
        this.piece = piece;
        this.auteur = Objects.requireNonNull(auteur, "auteur");
        this.ecriteLe = Objects.requireNonNull(ecriteLe, "ecriteLe");
        this.modifieeLe = modifieeLe;
        this.texte = texte;
    }

    /** RG-FAC-21 : une note a un texte. */
    public static NoteInterne ecrire(long piece, Auteur auteur, String texte, Instant le) {
        return new NoteInterne(null, piece, auteur, le, null, exigerTexte(texte));
    }

    public static NoteInterne reconstituer(long id, long piece, Auteur auteur, Instant ecriteLe, Instant modifieeLe,
            String texte) {
        return new NoteInterne(id, piece, auteur, ecriteLe, modifieeLe, texte);
    }

    /** RG-FAC-21 : l'auteur modifie sa note, « modifiée » est affiché. */
    public void modifier(long utilisateur, String nouveauTexte, Instant le) {
        exigerAuteur(utilisateur, "modifie");
        this.texte = exigerTexte(nouveauTexte);
        this.modifieeLe = le;
    }

    /** RG-FAC-21 : seul l'auteur supprime sa note. */
    public void exigerSupprimable(long utilisateur) {
        exigerAuteur(utilisateur, "supprime");
    }

    private void exigerAuteur(long utilisateur, String verbe) {
        if (auteur.utilisateur() != utilisateur) {
            throw new RegleEnfreinte("RG-FAC-21", "Seul son auteur, " + auteur.nom() + ", " + verbe + " cette note.");
        }
    }

    private static String exigerTexte(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new RegleEnfreinte("RG-FAC-21", "Une note a un texte.");
        }
        return texte.strip();
    }

    public Long id() {
        return id;
    }

    public long piece() {
        return piece;
    }

    public Auteur auteur() {
        return auteur;
    }

    public Instant ecriteLe() {
        return ecriteLe;
    }

    public Instant modifieeLe() {
        return modifieeLe;
    }

    public String texte() {
        return texte;
    }
}
