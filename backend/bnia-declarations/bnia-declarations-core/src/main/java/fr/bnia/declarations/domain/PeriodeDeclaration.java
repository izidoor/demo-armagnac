package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Période d'une DSM (glossaire, RG-DEC-03, H-DEC-5) : du 1er avril au 31 mars pour un producteur, un mois
 * civil pour un négociant. Aucune période ne contient un 1er avril ailleurs qu'à son début.
 */
public record PeriodeDeclaration(LocalDate debut, LocalDate fin) {

    private static final DateTimeFormatter MOIS = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

    public PeriodeDeclaration {
        Objects.requireNonNull(debut, "debut");
        Objects.requireNonNull(fin, "fin");
    }

    /** RG-DEC-03, 06 : un 1er avril pour un producteur, un premier du mois pour un négociant. */
    public static boolean debutPermis(TypeDeclarant type, LocalDate debut) {
        return debut.getDayOfMonth() == 1 && (type == TypeDeclarant.NEGOCIANT || debut.getMonth() == Month.APRIL);
    }

    /** RG-DEC-03 : la période de ce type qui commence à cette date. */
    public static PeriodeDeclaration commencant(TypeDeclarant type, LocalDate debut) {
        if (!debutPermis(type, debut)) {
            throw new RegleEnfreinte("RG-DEC-03", type == TypeDeclarant.PRODUCTEUR
                    ? "La période d'un producteur commence un 1er avril."
                    : "La période d'un négociant commence le premier du mois.");
        }
        LocalDate fin = type == TypeDeclarant.PRODUCTEUR ? debut.plusYears(1).minusDays(1)
                : debut.plusMonths(1).minusDays(1);
        return new PeriodeDeclaration(debut, fin);
    }

    /** RG-DEC-04 : la période qui commence le lendemain de celle-ci, ni chevauchement ni trou. */
    public PeriodeDeclaration suivante(TypeDeclarant type) {
        return commencant(type, fin.plusDays(1));
    }

    /** RG-DEC-05 : la période a commencé à cette date. */
    public boolean commencee(LocalDate aujourdhui) {
        return !aujourdhui.isBefore(debut);
    }

    /** « juin 2026 » pour un mois, « 2026-2027 » pour une année du 1er avril au 31 mars. */
    public String libelle() {
        if (debut.getYear() == fin.getYear() && debut.getMonth() == fin.getMonth()) {
            return MOIS.format(debut);
        }
        return debut.getYear() + "-" + fin.getYear();
    }

    @Override
    public String toString() {
        return libelle();
    }
}
