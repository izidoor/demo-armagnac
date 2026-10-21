package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Acteur;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** Les ventes consolidées d'une période, au back-office, et leur PDF (UC-FAC-8). RG-FAC-26, 30. */
@UseCase
public class ConsulterVentes {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VentesLecture lecture;
    private final EditionVentes edition;
    private final Controles controles;
    private final Clock horloge;

    public ConsulterVentes(VentesLecture lecture, EditionVentes edition, DroitsFacturation droits, Clock horloge) {
        this.lecture = lecture;
        this.edition = edition;
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /**
     * RG-FAC-26, 30 : les lignes article des pièces émises du {@code du} au {@code au} inclus, celles d'un avoir en
     * négatif ; le récapitulatif de TVA de la période et ses totaux.
     */
    public Ventes ventes(Acteur acteur, LocalDate du, LocalDate au) {
        controles.exigerLectureBnia(acteur);
        exigerPeriode(du, au);
        return Ventes.de(du, au, lecture.ventes(du, au, Optional.empty(), Optional.empty()));
    }

    /** RG-FAC-30 : la période des ventes va d'une date à une date postérieure ou égale. */
    static void exigerPeriode(LocalDate du, LocalDate au) {
        if (du == null || au == null || au.isBefore(du)) {
            throw new RegleEnfreinte("RG-FAC-30", "La période des ventes va d'une date à une date postérieure ou égale"
                    + (du != null && au != null ? " (du " + du.format(DATE) + " au " + au.format(DATE) + ")." : "."));
        }
    }

    /** RG-FAC-26, 30 : le PDF des ventes de la période, édité à la date du jour. */
    public byte[] pdf(Acteur acteur, LocalDate du, LocalDate au) {
        return edition.pdf(ventes(acteur, du, au), LocalDate.now(horloge));
    }
}
