package fr.bnia.declarations.application;

import fr.bnia.operateurs.domain.partage.Acteur;
import java.util.ArrayList;
import java.util.List;

/** Le port vers {@code facturation} simulé : les demandes reçues, un brouillon numéroté par demande. */
final class FacturationCapturee implements Facturation {

    final List<DemandeFactureDsm> demandes = new ArrayList<>();
    private long suivant = 500;

    @Override
    public List<Long> demander(Acteur acteur, List<DemandeFactureDsm> lot) {
        demandes.addAll(lot);
        return lot.stream().map(d -> suivant++).toList();
    }
}
