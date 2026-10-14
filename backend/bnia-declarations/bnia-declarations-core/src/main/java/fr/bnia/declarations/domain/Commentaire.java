package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.domain.partage.Cote;
import java.time.Instant;
import java.util.Objects;

/**
 * Commentaire d'instruction d'une DSM (entité, spec 05 §3.2.4, H-DEC-19) : sur la DSM ou sur une de ses
 * lignes, dans tout état. Enregistré à part de la DSM : l'écrire ne change pas la version de la DSM, et ne
 * fait donc pas échouer la saisie de l'autre partie (décision du 2026-10-13).
 *
 * @see Cote
 */
public final class Commentaire {

    private final Long id;
    private final long dsm;
    private Long ligne;
    private String ligneSupprimee;
    private final Auteur auteur;
    private final Cote cote;
    private final Instant ecritLe;
    private Instant modifieLe;
    private String texte;
    private boolean interne;
    private final boolean motif;

    private Commentaire(Long id, long dsm, Long ligne, String ligneSupprimee, Auteur auteur, Cote cote,
            Instant ecritLe, Instant modifieLe, String texte, boolean interne, boolean motif) {
        this.id = id;
        this.dsm = dsm;
        this.ligne = ligne;
        this.ligneSupprimee = ligneSupprimee;
        this.auteur = Objects.requireNonNull(auteur, "auteur");
        this.cote = Objects.requireNonNull(cote, "cote");
        this.ecritLe = Objects.requireNonNull(ecritLe, "ecritLe");
        this.modifieLe = modifieLe;
        this.texte = texte;
        this.interne = interne;
        this.motif = motif;
    }

    /**
     * RG-DEC-24 : un commentaire sur la DSM ({@code ligne} absente) ou sur une de ses lignes ; « interne »
     * seulement côté BNIA.
     */
    public static Commentaire ecrire(long dsm, Long ligne, Auteur auteur, Cote cote, String texte,
            boolean interne, Instant le) {
        exigerInterneDuBnia(cote, interne);
        return new Commentaire(null, dsm, ligne, null, auteur, cote, le, null, exigerTexte(texte), interne, false);
    }

    /** RG-DEC-20, 25 : le motif d'une demande de compléments, sur la DSM, visible de l'opérateur, non supprimable. */
    public static Commentaire motif(long dsm, Auteur auteur, String texte, Instant le) {
        if (texte == null || texte.isBlank()) {
            throw new RegleEnfreinte("RG-DEC-20", "Une demande de compléments porte un motif.");
        }
        return new Commentaire(null, dsm, null, null, auteur, Cote.BNIA, le, null, texte.strip(), false,
                true);
    }

    public static Commentaire reconstituer(long id, long dsm, Long ligne, String ligneSupprimee, Auteur auteur,
            Cote cote, Instant ecritLe, Instant modifieLe, String texte, boolean interne, boolean motif) {
        return new Commentaire(id, dsm, ligne, ligneSupprimee, auteur, cote, ecritLe, modifieLe, texte, interne, motif);
    }

    /** RG-DEC-25 : l'auteur modifie son commentaire, « modifié » est affiché ; le BNIA peut changer « interne ». */
    public void modifier(long utilisateur, String nouveauTexte, boolean nouvelInterne, Instant le) {
        exigerAuteur(utilisateur, "modifie");
        if (motif && nouvelInterne) {
            throw new RegleEnfreinte("RG-DEC-24", "Le motif d'une demande de compléments s'adresse à l'opérateur :"
                    + " il ne peut pas être interne.");
        }
        exigerInterneDuBnia(cote, nouvelInterne);
        this.texte = exigerTexte(nouveauTexte);
        this.interne = nouvelInterne;
        this.modifieLe = le;
    }

    /** RG-DEC-25 : seul l'auteur supprime son commentaire ; le motif d'une demande de compléments reste. */
    public void exigerSupprimable(long utilisateur) {
        exigerAuteur(utilisateur, "supprime");
        if (motif) {
            throw new RegleEnfreinte("RG-DEC-25", "Le motif d'une demande de compléments ne se supprime pas ;"
                    + " vous pouvez le modifier.");
        }
    }

    /** RG-DEC-01, 24 : l'opérateur ne voit jamais un commentaire interne. */
    public boolean visibleDe(boolean personnelBnia) {
        return personnelBnia || !interne;
    }

    /**
     * Décision du 2026-10-13 : quand sa ligne est supprimée, le commentaire passe sur la DSM et garde le
     * libellé de la ligne.
     */
    public void detacher(String libelleLigne) {
        this.ligne = null;
        this.ligneSupprimee = libelleLigne;
    }

    private void exigerAuteur(long utilisateur, String verbe) {
        if (auteur.utilisateur() != utilisateur) {
            throw new RegleEnfreinte("RG-DEC-25", "Seul son auteur, " + auteur.nom() + ", " + verbe
                    + " ce commentaire.");
        }
    }

    private static void exigerInterneDuBnia(Cote cote, boolean interne) {
        if (interne && cote != Cote.BNIA) {
            throw new RegleEnfreinte("RG-DEC-24", "Seul le BNIA écrit un commentaire interne.");
        }
    }

    private static String exigerTexte(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new RegleEnfreinte("RG-DEC-24", "Un commentaire n'est pas vide.");
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

    public Cote cote() {
        return cote;
    }

    public Instant ecritLe() {
        return ecritLe;
    }

    public Instant modifieLe() {
        return modifieLe;
    }

    public String texte() {
        return texte;
    }

    public boolean interne() {
        return interne;
    }

    public boolean motif() {
        return motif;
    }
}
