package fr.bnia.contratsarmagnac.application;

import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import java.util.List;
import java.util.Optional;

/** Port d'édition du contrat au formulaire CA-05 allégé (RG-CA-20, A-CA-8). */
public interface EditionsContrats {

    /**
     * Une partie, telle qu'imprimée (CA-05, partie 1).
     *
     * @param adresse lignes de l'adresse, code postal et ville compris
     * @param numero  n° BNIA : le code de l'opérateur
     * @param cvi     imprimé pour le vendeur seulement
     */
    record PartieEditee(String raisonSociale, List<String> adresse, Optional<String> telephone, String numero,
            Optional<String> siret, Optional<String> cvi) {
    }

    record EditionContrat(ContratAchatArmagnac contrat, String campagne, PartieEditee acheteur, PartieEditee vendeur) {
    }

    byte[] pdf(EditionContrat edition);
}
