package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.application.PeriodesAttendues.Attendue;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.partage.DeclarationEnRetard;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Traitement planifié quotidien (A-DEC-7, ADR-019) : publie {@link DeclarationEnRetard} une seule fois par
 * période en retard, le lendemain de l'échéance (RG-DEC-39 à 41). Sans acteur ni droits : seul le planificateur
 * l'appelle. L'unicité est tenue en base ({@link RetardsSignales}) : une double exécution est inoffensive.
 */
@UseCase
public class DetecterRetards {

    private final OperateursLecture operateurs;
    private final CalendriersLecture calendriers;
    private final RetardsSignales retards;
    private final PublicationEvenements evenements;
    private final Clock horloge;

    public DetecterRetards(OperateursLecture operateurs, CalendriersLecture calendriers, RetardsSignales retards,
            PublicationEvenements evenements, Clock horloge) {
        this.operateurs = operateurs;
        this.calendriers = calendriers;
        this.retards = retards;
        this.evenements = evenements;
        this.horloge = horloge;
    }

    /** @return le nombre de retards signalés par ce passage */
    public int detecter() {
        int signales = 0;
        for (Attendue attendue : PeriodesAttendues.de(operateurs, calendriers, LocalDate.now(horloge))) {
            if (!attendue.situation().enRetard()) {
                continue;
            }
            long operateur = attendue.operateur().id();
            PeriodeDeclaration periode = attendue.situation().periode().orElseThrow();
            if (retards.signaler(operateur, periode.debut(), horloge.instant())) {
                evenements.publier(new DeclarationEnRetard(operateur, periode.debut(), periode.fin(),
                        attendue.situation().echeance().orElseThrow(),
                        attendue.situation().dsmEnCours().orElse(null)));
                signales++;
            }
        }
        return signales;
    }
}
