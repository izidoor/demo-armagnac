package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.CalendrierDeclaratif;
import fr.bnia.declarations.domain.CalendrierDeclaratif.Situation;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * La période attendue de chaque opérateur au statut {@code OUI} qui a un point de départ (RG-DEC-40), en deux
 * lectures quel que soit leur nombre : les opérateurs, leurs repères. Partagé par le listing du BNIA et la
 * détection des retards.
 */
final class PeriodesAttendues {

    private PeriodesAttendues() {
    }

    /** Un opérateur attendu et la situation de sa période attendue. */
    record Attendue(OperateurVue operateur, Situation situation) {
    }

    static List<Attendue> de(OperateursLecture operateurs, CalendriersLecture calendriers, LocalDate aujourdhui) {
        Map<Long, CalendriersLecture.Reperes> reperes = calendriers.reperes();
        return operateurs.declarantsAttendus().stream()
                .filter(o -> reperes.containsKey(o.id()))
                .map(o -> {
                    CalendriersLecture.Reperes r = reperes.get(o.id());
                    return new Attendue(o, CalendrierDeclaratif.prochaine(o.type(), o.statutDeclarationStock(),
                            Optional.of(r.pointDeDepart()), r.dsms(), aujourdhui));
                })
                .toList();
    }
}
