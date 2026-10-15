package fr.bnia.facturation.application;

import fr.bnia.facturation.domain.Facture;
import java.util.Optional;

/** Les pièces (agrégat {@code Facture}, spec 08 §3.1), lignes comprises. */
public interface FactureRepository {

    /** @return la pièce enregistrée, avec son identifiant */
    Facture inserer(Facture facture);

    /** @throws fr.bnia.commun.domain.ModificationConcurrente si elle a changé depuis son chargement */
    Facture mettreAJour(Facture facture);

    Optional<Facture> parId(long id);

    /** RG-FAC-02 : cette origine a une facture vivante (un brouillon, ou une facture émise non annulée). */
    boolean origineVivante(String source, String identifiant);
}
