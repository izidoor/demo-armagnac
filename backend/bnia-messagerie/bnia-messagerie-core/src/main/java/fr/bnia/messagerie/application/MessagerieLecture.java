package fr.bnia.messagerie.application;

import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.domain.Cote;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Port de lecture de la messagerie (spec 15 §5.2) : nouveaux messages et conversations d'une portée, lus en SQL
 * direct (ADR-004).
 */
public interface MessagerieLecture {

    /**
     * Ce qu'un acteur peut lire (RG-MSG-01, 02, 11) : son côté ; au portail, son opérateur et lui, auteur des seules
     * conversations libres qu'il voit ; au back-office, tous les opérateurs et toutes les conversations libres.
     *
     * @param operateur l'opérateur, au portail ; vide au back-office
     * @param auteur    l'utilisateur, au portail ; vide au back-office
     * @param types     les types d'objet dont il lit le module
     */
    record Portee(Cote cote, Optional<Long> operateur, Optional<Long> auteur, Set<TypeObjet> types) {
    }

    /** Filtre de « Mes conversations » (RG-MSG-15, 16). */
    record Filtre(Optional<Long> operateur, Optional<TypeObjet> type, boolean libres, boolean nonLuesSeulement,
            Optional<String> recherche) {

        public static Filtre aucun() {
            return new Filtre(Optional.empty(), Optional.empty(), false, false, Optional.empty());
        }
    }

    /** RG-MSG-11 : les messages non lus de ce côté, du plus récent au plus ancien. */
    List<NouveauMessage> nouveaux(Portee portee);

    /** RG-MSG-15, 16 : les conversations, de la plus récemment écrite à la plus ancienne. */
    List<ConversationListee> conversations(Portee portee, Filtre filtre);

    /** Un message non lu, tel que le liste le tableau de bord (titre, opérateur, début du texte). */
    record NouveauMessage(long message, long conversation, Optional<TypeObjet> type, Optional<String> identifiant,
            String intitule, long operateur, String auteur, Instant ecritLe, String texte) {
    }

    /**
     * Une conversation de la liste.
     *
     * @param extrait le message le plus récent qui correspond à la recherche, ou le dernier message
     */
    record ConversationListee(long id, Optional<TypeObjet> type, Optional<String> identifiant, String intitule,
            long operateur, Optional<String> auteurLibre, Instant dernierMessage, int nonLus, int messages,
            String extrait) {
    }
}
