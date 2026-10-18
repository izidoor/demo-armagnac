package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import java.util.Objects;

/**
 * Note interne du BNIA sur une DSM (entité, spec 15 §3.7, RG-DEC-24) : sur la DSM ou une de ses lignes, signée et
 * datée, jamais montrée à l'opérateur. Remplace la note de contrôle d'une ligne (§4.4.5 p.26, C.3.2.1 p.108), le
 * commentaire de contrôle d'un justificatif (C.3.3.1 p.111) et le commentaire interne. Enregistrée à part de la
 * DSM : l'écrire ne change pas sa version.
 */
public final class NoteInterne {

    private final Long id;
    private final long dsm;
    private Long ligne;
    private String ligneSupprimee;
    private final Auteur auteur;
    private final Instant ecriteLe;
    private Instant modifieeLe;
    private String texte;

    private NoteInterne(Long id, long dsm, Long ligne, String ligneSupprimee, Auteur auteur, Instant ecriteLe,
            Instant modifieeLe, String texte) {
        this.id = id;
        this.dsm = dsm;
        this.ligne = ligne;
        this.ligneSupprimee = ligneSupprimee;
        this.auteur = Objects.requireNonNull(auteur, "auteur");
        this.ecriteLe = Objects.requireNonNull(ecriteLe, "ecriteLe");
        this.modifieeLe = modifieeLe;
        this.texte = texte;
    }

    /** RG-DEC-24 : une note sur la DSM ({@code ligne} absente) ou sur une de ses lignes. */
    public static NoteInterne ecrire(long dsm, Long ligne, Auteur auteur, String texte, Instant le) {
        return new NoteInterne(null, dsm, ligne, null, auteur, le, null, exigerTexte(texte));
    }

    public static NoteInterne reconstituer(long id, long dsm, Long ligne, String ligneSupprimee, Auteur auteur,
            Instant ecriteLe, Instant modifieeLe, String texte) {
        return new NoteInterne(id, dsm, ligne, ligneSupprimee, auteur, ecriteLe, modifieeLe, texte);
    }

    /** RG-DEC-24 : l'auteur modifie sa note, « modifiée » est affiché. */
    public void modifier(long utilisateur, String nouveauTexte, Instant le) {
        exigerAuteur(utilisateur, "modifie");
        this.texte = exigerTexte(nouveauTexte);
        this.modifieeLe = le;
    }

    /** RG-DEC-24 : seul l'auteur supprime sa note. */
    public void exigerSupprimable(long utilisateur) {
        exigerAuteur(utilisateur, "supprime");
    }

    /** Quand sa ligne est supprimée, la note passe sur la DSM et garde le libellé de la ligne (décision du 2026-10-13). */
    public void detacher(String libelleLigne) {
        this.ligne = null;
        this.ligneSupprimee = libelleLigne;
    }

    private void exigerAuteur(long utilisateur, String verbe) {
        if (auteur.utilisateur() != utilisateur) {
            throw new RegleEnfreinte("RG-DEC-24", "Seul son auteur, " + auteur.nom() + ", " + verbe + " cette note.");
        }
    }

    private static String exigerTexte(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new RegleEnfreinte("RG-DEC-24", "Une note n'est pas vide.");
        }
        return texte.strip();
    }

    public Long id() {
        return id;
    }

    public long dsm() {
        return dsm;
    }

    public Long ligne() {
        return ligne;
    }

    public String ligneSupprimee() {
        return ligneSupprimee;
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
