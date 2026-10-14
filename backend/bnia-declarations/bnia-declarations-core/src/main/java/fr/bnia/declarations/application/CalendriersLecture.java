package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.CalendrierDeclaratif.DsmPassee;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Port de lecture du calendrier de tous les opérateurs à la fois (listing du BNIA, détection des retards,
 * RG-DEC-40, 41), en une lecture.
 */
public interface CalendriersLecture {

    /**
     * Les repères de chaque opérateur qui a un point de départ, par identifiant d'opérateur.
     *
     * @param pointDeDepart le début du point de départ en vigueur
     * @param dsms          ce que le calendrier en lit : la DSM non validée, s'il en a une, et la dernière DSM
     *                      validée qui commence au début du point de départ ou après
     */
    record Reperes(LocalDate pointDeDepart, List<DsmPassee> dsms) {

        public Reperes {
            dsms = List.copyOf(dsms);
        }
    }

    Map<Long, Reperes> reperes();
}
