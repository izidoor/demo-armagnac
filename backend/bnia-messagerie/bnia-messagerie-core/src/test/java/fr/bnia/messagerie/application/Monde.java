package fr.bnia.messagerie.application;

import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.DescriptionObjet;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.TypeObjet;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Le monde des tests de cas d'usage : l'opérateur 7 (un négociant) et ses membres, le personnel du BNIA, la DSM 42 et
 * la facture 9 du négociant.
 */
final class Monde {

    static final long NEGOCIANT = 7;
    static final long AUTRE = 8;
    static final Acteur AGENT = new Acteur(10);
    static final Acteur OBSERVATEUR = new Acteur(11);
    static final Acteur ADMIN = new Acteur(12);
    static final Acteur AGENT_BNIA = new Acteur(20);
    static final Acteur OBSERVATEUR_BNIA = new Acteur(21);
    static final Acteur INCONNU = new Acteur(30);

    static final ObjetRef DSM = ObjetRef.de(TypeObjet.DSM, 42);
    static final ObjetRef FACTURE = ObjetRef.de(TypeObjet.FACTURE, 9);

    static final Clock HORLOGE = Clock.fixed(Instant.parse("2026-10-14T08:00:00Z"), ZoneId.of("Europe/Paris"));

    final Faux.Messages messages = new Faux.Messages();
    final Faux.Conversations conversations = new Faux.Conversations(messages);
    final Faux.Droits droits = new Faux.Droits();
    final Faux.Objets dsms = new Faux.Objets(TypeObjet.DSM);
    final Faux.Objets factures = new Faux.Objets(TypeObjet.FACTURE);
    final Faux.Stockage stockage = new Faux.Stockage();

    Monde() {
        droits.membre(AGENT.utilisateur(), NEGOCIANT, TypeObjet.DSM);
        droits.membre(OBSERVATEUR.utilisateur(), NEGOCIANT);
        droits.membre(ADMIN.utilisateur(), NEGOCIANT, TypeObjet.DSM);
        droits.bnia(AGENT_BNIA.utilisateur(), TypeObjet.DSM, TypeObjet.FACTURE);
        droits.bnia(OBSERVATEUR_BNIA.utilisateur());
        dsms.descriptions.put("42", new DescriptionObjet(NEGOCIANT, "DSM octobre 2026"));
        factures.descriptions.put("9", new DescriptionObjet(NEGOCIANT, "Facture 2026000003"));
    }

    Converser converser() {
        return new Converser(conversations, messages, droits, Faux.liste(dsms, factures), stockage, HORLOGE);
    }

    ConsulterMessagerie consulterMessagerie(MessagerieLecture lecture) {
        return new ConsulterMessagerie(lecture, droits);
    }

    ConversationsDesObjets conversationsDesObjets() {
        return new ConversationsDesObjets(conversations, messages, droits, Faux.liste(dsms, factures), HORLOGE);
    }
}
