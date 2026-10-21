package fr.bnia.messagerie.application;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.domain.Conversation;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.messagerie.domain.ObjetMetier;
import fr.bnia.messagerie.domain.SujetLibre;
import java.util.Optional;

/** Les droits d'un acteur sur une conversation (RG-MSG-01 à 03, 13), évalués à chaque appel. */
final class Acces {

    private final DroitsMessagerie droits;

    Acces(DroitsMessagerie droits) {
        this.droits = droits;
    }

    /**
     * Ce que l'acteur peut sur une conversation.
     *
     * @param cote             son côté
     * @param peutEcrire       il y écrit
     * @param lectureSeule     pourquoi il n'y écrit pas ; vide s'il écrit
     * @param auteurParti      conversation libre dont l'auteur n'est plus membre actif (RG-MSG-13)
     * @param regle            la règle qui refuse l'écriture
     */
    record Droit(Cote cote, boolean peutEcrire, Optional<String> lectureSeule, boolean auteurParti, String regle) {

        void exigerEcriture() {
            lectureSeule.ifPresent(motif -> {
                throw new AccesRefuse(regle, motif);
            });
        }
    }

    /** RG-MSG-01 : la conversation, existante ou à naître, d'un objet de cet opérateur. */
    Droit surObjet(Acteur acteur, TypeObjet type, long operateur, String libelle) {
        Cote cote = droits.cote(acteur, operateur).filter(c -> droits.lit(acteur, type, operateur))
                .orElseThrow(() -> new AccesRefuse("RG-MSG-01", "Vous n'avez pas accès à la conversation de " + libelle
                        + "."));
        boolean ecrit = droits.gere(acteur, type, operateur)
                || type == TypeObjet.FACTURE;
        return new Droit(cote, ecrit, ecrit ? Optional.empty()
                : Optional.of("Votre rôle permet de lire cette conversation, pas d'y écrire."), false, "RG-MSG-01");
    }

    /** RG-MSG-01, 02, 13 : une conversation existante. */
    Droit sur(Acteur acteur, Conversation conversation) {
        return switch (conversation.sujet()) {
            case ObjetMetier objet -> surObjet(acteur, objet.type(), conversation.operateur(), objet.libelle());
            case SujetLibre libre -> surLibre(acteur, conversation.operateur(), libre);
        };
    }

    /**
     * RG-MSG-02, 13 : tout rôle lit le module Messagerie, seuls {@code AGENT} et {@code ADMIN} le gèrent, donc y
     * écrivent ; côté opérateur, la conversation n'est lue que par son auteur.
     */
    private Droit surLibre(Acteur acteur, long operateur, SujetLibre libre) {
        Cote cote = droits.cote(acteur, operateur).orElseThrow(() -> new AccesRefuse("RG-MSG-02",
                "Vous n'avez pas accès à la conversation « " + libre.titre() + " »."));
        boolean auteurActif = droits.membreActif(libre.auteur().utilisateur(), operateur);
        if (cote == Cote.OPERATEUR && (libre.auteur().utilisateur() != acteur.utilisateur() || !auteurActif)) {
            throw new AccesRefuse("RG-MSG-02", "La conversation « " + libre.titre() + " » n'est visible que de "
                    + libre.auteur().nom() + " et du BNIA.");
        }
        if (cote == Cote.BNIA && !auteurActif) {
            return new Droit(Cote.BNIA, false, Optional.of(libre.auteur().nom()
                    + " n'est plus membre de l'opérateur : la conversation est en lecture seule."), true, "RG-MSG-13");
        }
        boolean ecrit = droits.gereMessagerie(acteur, operateur);
        return new Droit(cote, ecrit, ecrit ? Optional.empty()
                : Optional.of("Votre rôle permet de lire cette conversation, pas d'y écrire."), false, "RG-MSG-02");
    }
}
