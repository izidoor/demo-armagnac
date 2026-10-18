package fr.bnia.messagerie.domain;

import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Message d'une conversation (agrégat, spec 15 §3.4) : écrit par un utilisateur d'un côté, à l'adresse de l'autre,
 * sur l'objet entier ou le sujet libre. Lu ou non lu par côté, sans dire qui l'a lu (RG-MSG-09).
 */
public final class Message {

    /** RG-MSG-05 : longueur maximale du texte. */
    public static final int TEXTE_MAXIMAL = 4000;

    /** RG-MSG-14 : nombre maximal de pièces jointes. */
    public static final int PIECES_MAXIMALES = 5;

    private final Long id;
    private final long conversation;
    private final Auteur auteur;
    private final Cote cote;
    private final Instant ecritLe;
    private Instant modifieLe;
    private String texte;
    private final boolean protege;
    private boolean luParOperateur;
    private boolean luParBnia;
    private final List<DocumentDepose> pieces;

    private Message(Long id, long conversation, Auteur auteur, Cote cote, Instant ecritLe, Instant modifieLe,
            String texte, boolean protege, boolean luParOperateur, boolean luParBnia, List<DocumentDepose> pieces) {
        this.id = id;
        this.conversation = conversation;
        this.auteur = Objects.requireNonNull(auteur, "auteur");
        this.cote = Objects.requireNonNull(cote, "cote");
        this.ecritLe = Objects.requireNonNull(ecritLe, "ecritLe");
        this.modifieLe = modifieLe;
        this.texte = texte;
        this.protege = protege;
        this.luParOperateur = luParOperateur;
        this.luParBnia = luParBnia;
        this.pieces = List.copyOf(pieces);
    }

    /**
     * RG-MSG-05, 09, 14 : un message écrit, lu pour le côté de son auteur, non lu pour l'autre ; ses pièces jointes,
     * cinq au plus.
     */
    public static Message ecrire(long conversation, Auteur auteur, Cote cote, String texte,
            List<DocumentDepose> pieces, Instant le) {
        if (pieces.size() > PIECES_MAXIMALES) {
            throw new RegleEnfreinte("RG-MSG-14", "Un message porte au plus " + PIECES_MAXIMALES
                    + " pièces jointes ; vous en joignez " + pieces.size() + ".");
        }
        return new Message(null, conversation, auteur, cote, le, null, exigerTexte(texte), false, cote == Cote.OPERATEUR,
                cote == Cote.BNIA, pieces);
    }

    /** RG-MSG-08 : le motif d'une demande de compléments, côté BNIA, protégé. */
    public static Message motif(long conversation, Auteur auteur, String texte, Instant le) {
        return new Message(null, conversation, auteur, Cote.BNIA, le, null, exigerTexte(texte), true, false, true,
                List.of());
    }

    public static Message reconstituer(long id, long conversation, Auteur auteur, Cote cote, Instant ecritLe,
            Instant modifieLe, String texte, boolean protege, boolean luParOperateur, boolean luParBnia,
            List<DocumentDepose> pieces) {
        return new Message(id, conversation, auteur, cote, ecritLe, modifieLe, texte, protege, luParOperateur,
                luParBnia, pieces);
    }

    /** RG-MSG-06 : l'auteur modifie son message, « modifié » est affiché ; son état de lecture ne change pas. */
    public void modifier(long utilisateur, String nouveauTexte, Instant le) {
        exigerAuteur(utilisateur, "modifie");
        this.texte = exigerTexte(nouveauTexte);
        this.modifieLe = le;
    }

    /** RG-MSG-06 : seul l'auteur supprime son message ; un message protégé ne se supprime pas. */
    public void exigerSupprimable(long utilisateur) {
        exigerAuteur(utilisateur, "supprime");
        if (protege) {
            throw new RegleEnfreinte("RG-MSG-06", "Le motif d'une demande de compléments ne se supprime pas ;"
                    + " vous pouvez le modifier.");
        }
    }

    /** RG-MSG-10 : remis non lu pour ce côté, jusqu'au prochain affichage de sa conversation. */
    public void marquerNonLu(Cote pour) {
        if (pour == Cote.OPERATEUR) {
            luParOperateur = false;
        } else {
            luParBnia = false;
        }
    }

    /** RG-MSG-09 : lu ou non pour ce côté. */
    public boolean luPar(Cote pour) {
        return pour == Cote.OPERATEUR ? luParOperateur : luParBnia;
    }

    private void exigerAuteur(long utilisateur, String verbe) {
        if (auteur.utilisateur() != utilisateur) {
            throw new RegleEnfreinte("RG-MSG-06", "Seul son auteur, " + auteur.nom() + ", " + verbe + " ce message.");
        }
    }

    private static String exigerTexte(String texte) {
        if (texte == null || texte.isBlank()) {
            throw new RegleEnfreinte("RG-MSG-05", "Un message n'est pas vide.");
        }
        String nettoye = texte.strip();
        if (nettoye.length() > TEXTE_MAXIMAL) {
            throw new RegleEnfreinte("RG-MSG-05", "Le message compte " + nettoye.length() + " caractères ; "
                    + TEXTE_MAXIMAL + " au plus.");
        }
        return nettoye;
    }

    public Long id() {
        return id;
    }

    public long conversation() {
        return conversation;
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

    public boolean protege() {
        return protege;
    }

    public boolean luParOperateur() {
        return luParOperateur;
    }

    public boolean luParBnia() {
        return luParBnia;
    }

    public List<DocumentDepose> pieces() {
        return pieces;
    }
}
