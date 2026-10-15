package fr.bnia.facturation.api;

import java.util.List;

/** Le cas d'usage qu'une source appelle pour faire facturer (spec 08 §3.11, UC-FAC-1). */
public interface DemandeFactures {

    /**
     * RG-FAC-01 : un brouillon par demande, tout ou rien ; l'erreur nomme la demande en cause.
     *
     * @return les identifiants des brouillons, dans l'ordre des demandes
     */
    List<Long> demander(Acteur acteur, List<DemandeFacture> demandes);
}
