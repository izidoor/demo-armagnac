package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.Vendeur;
import java.util.Optional;

/** Le vendeur, unique (spec 08 §3.7, RG-FAC-16). */
public interface VendeurRepository {

    /** Vide tant qu'il n'est pas paramétré. */
    Optional<Vendeur> charger();

    void enregistrer(Vendeur vendeur);
}
