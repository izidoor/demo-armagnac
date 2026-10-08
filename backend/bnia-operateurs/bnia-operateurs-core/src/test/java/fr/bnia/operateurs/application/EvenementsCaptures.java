package fr.bnia.operateurs.application;

import java.util.ArrayList;
import java.util.List;

/** Événements publiés, retenus pour les tests (ADR-013 : les tests de l'émetteur vérifient la publication). */
class EvenementsCaptures implements PublicationEvenements {

    final List<Object> publies = new ArrayList<>();

    @Override
    public void publier(Object evenement) {
        publies.add(evenement);
    }
}
