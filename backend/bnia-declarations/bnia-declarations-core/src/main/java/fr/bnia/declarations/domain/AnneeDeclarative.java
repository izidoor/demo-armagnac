package fr.bnia.declarations.domain;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Année déclarative (glossaire, RG-DEC-44) : du 1er avril au 31 mars, celle de la période d'un producteur
 * (RG-DEC-03). Le stock vieillit à son début (annexe B p.98) : ses stocks déclarés ne se cumulent jamais avec
 * ceux d'une autre année. À ne pas confondre avec la campagne de distillation (1er septembre – 31 mars).
 *
 * @param debutAnnee l'année civile de son 1er avril : 2026 pour l'année 2026-2027
 */
public record AnneeDeclarative(int debutAnnee) {

    /** L'année déclarative qui contient cette date. */
    public static AnneeDeclarative contenant(LocalDate date) {
        return new AnneeDeclarative(date.getMonthValue() >= Month.APRIL.getValue() ? date.getYear()
                : date.getYear() - 1);
    }

    public LocalDate debut() {
        return LocalDate.of(debutAnnee, Month.APRIL, 1);
    }

    public LocalDate fin() {
        return LocalDate.of(debutAnnee + 1, Month.MARCH, 31);
    }

    /** La période entière tient dans l'année. */
    public boolean contient(PeriodeDeclaration periode) {
        return !periode.debut().isBefore(debut()) && !periode.fin().isAfter(fin());
    }

    /**
     * RG-DEC-44 : les fins de mois de l'année écoulées à cette date (la fin du mois en cours n'en est pas), dans
     * l'ordre ; toutes pour une année passée, aucune pour une année à venir.
     */
    public List<LocalDate> finsDeMois(LocalDate aujourdhui) {
        List<LocalDate> fins = new ArrayList<>();
        for (LocalDate fin = debut().with(TemporalAdjusters.lastDayOfMonth()); !fin.isAfter(fin())
                && fin.isBefore(aujourdhui); fin = fin.plusDays(1).with(TemporalAdjusters.lastDayOfMonth())) {
            fins.add(fin);
        }
        return fins;
    }

    /** « 2026-2027 ». */
    public String libelle() {
        return debutAnnee + "-" + (debutAnnee + 1);
    }

    @Override
    public String toString() {
        return libelle();
    }
}
