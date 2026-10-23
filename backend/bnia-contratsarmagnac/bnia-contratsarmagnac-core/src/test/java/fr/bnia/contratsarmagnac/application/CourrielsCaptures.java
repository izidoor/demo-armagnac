package fr.bnia.contratsarmagnac.application;

import java.util.ArrayList;
import java.util.List;

final class CourrielsCaptures implements EnvoiCourriels {

    final List<Courriel> envoyes = new ArrayList<>();

    @Override
    public void envoyer(Courriel courriel) {
        envoyes.add(courriel);
    }

    List<Courriel> a(String destinataire) {
        return envoyes.stream().filter(c -> c.destinataire().equals(destinataire)).toList();
    }
}
