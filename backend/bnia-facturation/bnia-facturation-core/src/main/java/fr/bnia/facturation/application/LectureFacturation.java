package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.facturation.api.FacturationLecture;
import fr.bnia.facturation.api.LigneVendue;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * La lecture publiée (RG-FAC-31), pour {@code statistiques} : les ventes d'une période, filtrables par nature
 * d'origine. Sans acteur : le contexte appelant contrôle ses propres droits.
 */
@UseCase
public class LectureFacturation implements FacturationLecture {

    private final VentesLecture lecture;

    public LectureFacturation(VentesLecture lecture) {
        this.lecture = lecture;
    }

    @Override
    public List<LigneVendue> ventes(LocalDate du, LocalDate au, Optional<String> nature) {
        return lecture.ventes(du, au, nature.map(String::strip).filter(n -> !n.isEmpty()), Optional.empty());
    }
}
