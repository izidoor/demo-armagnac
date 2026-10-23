package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.Appellation;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Données des tests du domaine : l'exemple du CA-05 (annexe de la vision). */
final class Exemples {

    static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 23);
    static final Instant MAINTENANT = Instant.parse("2026-10-23T08:00:00Z");
    static final long ACHETEUR = 7;
    static final long VENDEUR = 8;

    private Exemples() {
    }

    static Auteur jeanne() {
        return new Auteur(Partie.VENDEUR, 80, "Jeanne Durand", MAINTENANT);
    }

    static Auteur henri() {
        return new Auteur(Partie.ACHETEUR, 70, "Henri Castex", MAINTENANT);
    }

    static LigneContrat millesime(int annee, String volume, String degre, String prix) {
        return LigneContrat.saisie(Appellation.BAS_ARMAGNAC, annee, null, null, new BigDecimal(volume),
                new BigDecimal(degre), new BigDecimal(prix), AUJOURDHUI);
    }

    static LigneContrat assemblage(String denomination, String compte, String volume, String degre, String prix) {
        return LigneContrat.saisie(Appellation.ARMAGNAC, null, denomination, compte, new BigDecimal(volume),
                new BigDecimal(degre), new BigDecimal(prix), AUJOURDHUI);
    }

    static ContenuContrat contenu(LigneContrat... lignes) {
        return new ContenuContrat(List.of(lignes), null, null, null, new Paiement(Solde.COMPTANT, null));
    }

    /** Le projet de Jeanne : 15 hl de 2015 à 47 %, 1 750 € / hl AP. */
    static ContratAchatArmagnac projetDeJeanne() {
        return ContratAchatArmagnac.ouvrir("CA-2026-0001", ACHETEUR, VENDEUR, jeanne(),
                contenu(millesime(2015, "15", "47", "1750")));
    }
}
