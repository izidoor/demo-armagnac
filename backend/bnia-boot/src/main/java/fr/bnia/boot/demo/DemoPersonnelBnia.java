package fr.bnia.boot.demo;

import fr.bnia.authentification.application.GererMotDePasse;
import fr.bnia.operateurs.application.GererInvitations;
import fr.bnia.operateurs.application.RattachementRepository;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Personnel du BNIA de démonstration (spec 02 §7.1), en profil {@code demo}, avant les autres chargeurs qui
 * agissent en son nom : Anne Lamarque ({@code admin@bnia.fr}, ADMIN, invitée par l'application), Luc Dubos
 * ({@code agent@bnia.fr}, AGENT), Claire Sentex ({@code observateur@bnia.fr}, OBSERVATEUR). Comptes listés
 * dans {@code docs/demo.md}.
 */
@Component
@Profile("demo")
@Order(1)
public class DemoPersonnelBnia implements ApplicationRunner {

    static final String ANNE = "admin@bnia.fr";

    private static final Logger LOG = LoggerFactory.getLogger(DemoPersonnelBnia.class);

    private final ComptesDemo comptes;
    private final OperateursLecture lecture;

    public DemoPersonnelBnia(GererInvitations invitations, GererMotDePasse motsDePasse,
            RattachementRepository rattachements, OperateursLecture lecture) {
        this.comptes = new ComptesDemo(invitations, motsDePasse, rattachements, lecture);
        this.lecture = lecture;
    }

    /** L'ADMIN du BNIA au nom de qui les chargeurs de démonstration agissent. */
    static long anne(OperateursLecture lecture) {
        return lecture.utilisateurParEmail(ANNE)
                .orElseThrow(() -> new IllegalStateException("Le personnel du BNIA de démonstration manque."));
    }

    @Override
    public void run(ApplicationArguments arguments) {
        long bnia = lecture.operateurBnia();
        comptes.membre(null, bnia, Role.ADMIN, ANNE, "Lamarque", "Anne", "Directrice");
        Acteur anne = new Acteur(anne(lecture));
        comptes.membre(anne, bnia, Role.AGENT, "agent@bnia.fr", "Dubos", "Luc", "Chargé des opérateurs");
        comptes.membre(anne, bnia, Role.OBSERVATEUR, "observateur@bnia.fr", "Sentex", "Claire",
                "Chargée d'études");
        LOG.info("Démo : personnel du BNIA prêt (mot de passe {}, code 000000).", ComptesDemo.MOT_DE_PASSE);
    }
}
