package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.contratsarmagnac.domain.ContenuContrat;
import fr.bnia.contratsarmagnac.domain.LigneContrat;
import fr.bnia.contratsarmagnac.domain.Paiement;
import fr.bnia.contratsarmagnac.domain.Solde;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Le contenu d'un contrat tel que saisi (spec 06 §6.2), contrôlé par le domaine (RG-CA-03 à 05). */
public record SaisieContenu(List<SaisieLigne> lignes, String caracteristiques, String conditionsRetiraison,
        LocalDate aRetirerAvantLe, Solde solde, BigDecimal acompte) {

    /** Une ligne : millésime, ou dénomination et compte d'âge ; volume en hl, degré en % vol., prix en € HT / hl AP. */
    public record SaisieLigne(Appellation appellation, Integer millesime, String denomination, String compte,
            BigDecimal volume, BigDecimal degre, BigDecimal prix) {
    }

    /** @param date la date de la version, pour le compte d'âge d'un millésime (RG-CA-04) */
    ContenuContrat versContenu(LocalDate date) {
        return new ContenuContrat(lignes == null ? List.of() : lignes.stream()
                .map(l -> LigneContrat.saisie(l.appellation(), l.millesime(), l.denomination(), l.compte(),
                        l.volume(), l.degre(), l.prix(), date))
                .toList(), caracteristiques, conditionsRetiraison, aRetirerAvantLe,
                new Paiement(solde, acompte));
    }
}
