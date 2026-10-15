package fr.bnia.facturation.api;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.LocalDate;

/**
 * Condition de paiement d'une pièce (spec 08 §3.6, C. com. L441-10) : un nombre de jours, puis, au besoin, la
 * fin du mois.
 *
 * @param jours     de 0 (à réception) à 60 ; 45 au plus avec fin de mois (RG-FAC-13)
 * @param finDeMois l'échéance est reportée au dernier jour du mois
 */
public record ConditionPaiement(int jours, boolean finDeMois) {

    /** H-FAC-3 : la condition de la cotisation interprofessionnelle. */
    public static final ConditionPaiement TRENTE_JOURS_FIN_DE_MOIS = new ConditionPaiement(30, true);

    public ConditionPaiement {
        if (jours < 0 || jours > 60) {
            throw new RegleEnfreinte("RG-FAC-13", "Le délai de paiement va de 0 à 60 jours.");
        }
        if (finDeMois && jours > 45) {
            throw new RegleEnfreinte("RG-FAC-13", "Avec fin de mois, le délai de paiement est de 45 jours au plus.");
        }
    }

    /** RG-FAC-13 : date d'émission + jours, puis le dernier jour de ce mois si la condition le dit. */
    public LocalDate echeance(LocalDate emission) {
        LocalDate date = emission.plusDays(jours);
        return finDeMois ? date.withDayOfMonth(date.lengthOfMonth()) : date;
    }

    /** « À réception », « 30 jours », « 30 jours fin de mois ». */
    public String libelle() {
        if (jours == 0 && !finDeMois) {
            return "À réception";
        }
        return jours + " jours" + (finDeMois ? " fin de mois" : "");
    }
}
