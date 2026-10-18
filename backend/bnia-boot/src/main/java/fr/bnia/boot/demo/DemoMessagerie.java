package fr.bnia.boot.demo;

import fr.bnia.declarations.application.EditerDsm;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.facturation.application.ConsulterMesFactures;
import fr.bnia.facturation.application.FiltrePieces;
import fr.bnia.facturation.application.PieceLigne;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.ConversationsDesObjets;
import fr.bnia.messagerie.application.Converser;
import fr.bnia.messagerie.application.FichierJoint;
import fr.bnia.messagerie.application.MessagerieLecture;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Données de démonstration de la messagerie (spec 15 §7.1), en profil {@code demo} seulement, après les déclarations
 * et la facturation, pour Armagnacs de Gascogne ({@code N320001}) :
 * <ul>
 * <li>la conversation de la DSM envoyée du mois courant : un message de Julie Darrigade et la réponse de Luc Dubos,
 * non lue pour Gascogne (lots 2 et 3) ;</li>
 * <li>la conversation libre « Import de la matrice refusé » de Julie Darrigade, l'Excel de sa DSM en pièce jointe,
 * avec la réponse du BNIA ; celle de Thomas Barrère, invisible de Julie et d'Henri Castex, l'ADMIN (lot 5) ;</li>
 * <li>la conversation de la dernière facture émise : la question d'Henri Castex, non lue pour le BNIA (lot 6).</li>
 * </ul>
 * Le motif des compléments de Maison Larressingle est déjà dans la conversation de sa DSM, déposé par
 * {@code declarations}.
 *
 * <p>Tout passe par les cas d'usage. Gascogne ayant déjà une conversation libre, rien n'est rechargé : le chargeur
 * est relançable.
 */
@Component
@Profile("demo")
@Order(6)
public class DemoMessagerie implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoMessagerie.class);

    private final Converser converser;
    private final ConversationsDesObjets objets;
    private final GererDsm dsms;
    private final EditerDsm editions;
    private final ConsulterMesFactures factures;
    private final MessagerieLecture messagerie;
    private final OperateursLecture lecture;

    public DemoMessagerie(Converser converser, ConversationsDesObjets objets, GererDsm dsms, EditerDsm editions,
            ConsulterMesFactures factures, MessagerieLecture messagerie, OperateursLecture lecture) {
        this.converser = converser;
        this.objets = objets;
        this.dsms = dsms;
        this.editions = editions;
        this.factures = factures;
        this.messagerie = messagerie;
        this.lecture = lecture;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        long gascogne = lecture.operateurParCode("N320001").orElseThrow().id();
        var libresDeGascogne = new MessagerieLecture.Portee(Cote.BNIA, Optional.of(gascogne), Optional.empty(), Set.of());
        if (!messagerie.conversations(libresDeGascogne, new MessagerieLecture.Filtre(Optional.empty(), Optional.empty(),
                true, false, Optional.empty())).isEmpty()) {
            LOG.info("Démo : messagerie déjà chargée.");
            return;
        }
        conversationDeLaDsmDeGascogne();
        conversationsLibres(gascogne);
        conversationDUneFacture(gascogne);
    }

    /** Lot 5 : une demande d'aide avec sa pièce jointe, et une conversation qu'Henri Castex, l'ADMIN, ne voit pas. */
    private void conversationsLibres(long gascogne) {
        Acteur julie = membre("agent@negociant.fr");
        var anne = new fr.bnia.operateurs.domain.partage.Acteur(DemoPersonnelBnia.anne(lecture));
        long dsm = dsms.lister(anne, gascogne).getFirst().dsm().id();
        var excel = editions.excel(new fr.bnia.operateurs.domain.partage.Acteur(julie.utilisateur()), gascogne, dsm);
        var vue = converser.ouvrirLibre(julie, gascogne, "Import de la matrice refusé", "Bonjour,\nl'import de ma"
                + " matrice du mois est refusé (« colonne grisée »). Je vous joins le fichier ; que dois-je corriger ?",
                List.of(new FichierJoint(excel.nom(), excel.contenu())));
        converser.ecrire(membre("agent@bnia.fr"), vue.conversation().orElseThrow().id(), "Bonjour, la colonne 15"
                + " (Floc de Gascogne) est grisée pour un négociant : laissez-la vide et réimportez le fichier.");
        converser.ouvrirLibre(membre("observateur@negociant.fr"), gascogne, "Accès aux statistiques du marché",
                "Bonjour, je n'arrive pas à consulter les statistiques globalisées : mon rôle le permet-il ?", List.of());
    }

    /** Lot 6 : la question du client sur sa dernière facture émise. */
    private void conversationDUneFacture(long gascogne) {
        Acteur henri = membre("admin@negociant.fr");
        List<PieceLigne> emises = factures.lister(new fr.bnia.facturation.api.Acteur(henri.utilisateur()), gascogne,
                FiltrePieces.aucun()).stream().filter(p -> p.etat() == EtatPiece.EMISE).toList();
        emises.stream().max(Comparator.comparing(PieceLigne::dateEmission)).ifPresent(p -> converser.ecrireSurObjet(
                henri, ObjetRef.de(TypeObjet.FACTURE, p.id()), "Bonjour, la facture reprend 839,11 L de sorties France"
                        + " ; notre comptabilité trouve 829,11 L. Pouvez-vous vérifier ?"));
    }

    private void conversationDeLaDsmDeGascogne() {
        long gascogne = lecture.operateurParCode("N320001").orElseThrow().id();
        var anne = new fr.bnia.operateurs.domain.partage.Acteur(DemoPersonnelBnia.anne(lecture));
        var envoyee = dsms.lister(anne, gascogne).stream().map(GererDsm.DsmDeLOperateur::dsm)
                .filter(d -> d.etat() == EtatDsm.ENVOYEE).findFirst();
        if (envoyee.isEmpty()) {
            return;
        }
        ObjetRef dsm = ObjetRef.de(TypeObjet.DSM, envoyee.get().id());
        if (objets.ouverte(dsm)) {
            return;
        }
        converser.ecrireSurObjet(membre("agent@negociant.fr"), dsm, "40 L de XO sont passés dans l'assemblage VSOP (colonnes 12 et 6) ;"
                + " les DAE de l'export partent par courrier cette semaine.");
        converser.ecrireSurObjet(membre("agent@bnia.fr"), dsm, "Bien noté. Nous rapprocherons les DAE des sorties"
                + " export à l'instruction ; merci de joindre aussi celui du 12.");
        LOG.info("Démo : conversation de la DSM de {} d'Armagnacs de Gascogne.", envoyee.get().periode().libelle());
    }

    private Acteur membre(String email) {
        return new Acteur(lecture.utilisateurParEmail(email).orElseThrow());
    }
}
