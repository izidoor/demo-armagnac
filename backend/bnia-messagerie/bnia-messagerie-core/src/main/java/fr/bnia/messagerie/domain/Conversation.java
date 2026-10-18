package fr.bnia.messagerie.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Conversation entre un opérateur et le BNIA (agrégat, spec 15 §3.1), sur un objet métier ou un sujet libre. Elle
 * naît à son premier message (RG-MSG-04, 12) ; ses messages sont une racine à part, pour qu'écrire ou lire ne la
 * verrouille pas.
 */
public final class Conversation {

    private final Long id;
    private final long operateur;
    private final Sujet sujet;
    private final Instant ouverteLe;

    private Conversation(Long id, long operateur, Sujet sujet, Instant ouverteLe) {
        this.id = id;
        this.operateur = operateur;
        this.sujet = Objects.requireNonNull(sujet, "sujet");
        this.ouverteLe = Objects.requireNonNull(ouverteLe, "ouverteLe");
    }

    /** RG-MSG-04 : la conversation d'un objet, avec l'opérateur et le libellé que décrit son contexte. */
    public static Conversation surObjet(ObjetMetier objet, long operateur, Instant le) {
        return new Conversation(null, operateur, objet, le);
    }

    /** RG-MSG-12 : une conversation libre, dans l'opérateur actif de son auteur. */
    public static Conversation libre(SujetLibre sujet, long operateur, Instant le) {
        return new Conversation(null, operateur, sujet, le);
    }

    public static Conversation reconstituer(long id, long operateur, Sujet sujet, Instant ouverteLe) {
        return new Conversation(id, operateur, sujet, ouverteLe);
    }

    public Conversation avecId(long id) {
        return new Conversation(id, operateur, sujet, ouverteLe);
    }

    public boolean libre() {
        return sujet instanceof SujetLibre;
    }

    public Long id() {
        return id;
    }

    public long operateur() {
        return operateur;
    }

    public Sujet sujet() {
        return sujet;
    }

    public Instant ouverteLe() {
        return ouverteLe;
    }
}
