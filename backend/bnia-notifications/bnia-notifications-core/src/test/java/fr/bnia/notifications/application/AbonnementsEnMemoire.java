package fr.bnia.notifications.application;

import fr.bnia.notifications.domain.Abonnement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Abonnements des tests, par adresse. */
final class AbonnementsEnMemoire implements AbonnementRepository {

    final Map<String, Abonnement> parAdresse = new LinkedHashMap<>();

    @Override
    public void enregistrer(Abonnement abonnement) {
        parAdresse.put(abonnement.adresse(), abonnement);
    }

    @Override
    public Optional<Abonnement> parAdresse(String adresse) {
        return Optional.ofNullable(parAdresse.get(adresse));
    }

    @Override
    public List<Abonnement> de(Collection<Long> utilisateurs) {
        return parAdresse.values().stream().filter(a -> utilisateurs.contains(a.utilisateur())).toList();
    }

    @Override
    public void supprimer(String adresse) {
        parAdresse.remove(adresse);
    }
}
