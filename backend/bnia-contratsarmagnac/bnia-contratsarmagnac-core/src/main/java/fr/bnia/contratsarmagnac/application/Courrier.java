package fr.bnia.contratsarmagnac.application;

import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.Fait;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.PersonneVue;
import fr.bnia.operateurs.domain.partage.Role;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rédige les e-mails et les notifications push à l'autre partie (RG-CA-18) et les confie à l'envoi, après la validation
 * de la transaction. Ils vont à ses membres {@code AGENT} et {@code ADMIN} : l'e-mail à ceux qui ont un e-mail, le push
 * à leurs appareils abonnés (spec 11) ; le lien vise le contrat au portail. Le push, lu sur l'écran verrouillé, n'a
 * qu'une phrase, sans montant ni volume.
 */
final class Courrier {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRANCE);

    private final EnvoiCourriels envoi;
    private final EnvoiNotifications notifications;
    private final OperateursLecture operateurs;
    private final ParametresContrats parametres;

    Courrier(EnvoiCourriels envoi, EnvoiNotifications notifications, OperateursLecture operateurs,
            ParametresContrats parametres) {
        this.envoi = envoi;
        this.notifications = notifications;
        this.operateurs = operateurs;
        this.parametres = parametres;
    }

    /** RG-CA-18 : l'e-mail et le push d'un fait qui attend l'autre partie ou la concerne ; rien pour les autres faits. */
    void notifier(ContratAchatArmagnac contrat, Fait fait) {
        Partie auteur = fait.auteur().partie();
        String parQui = fait.auteur().nom() + " (" + nom(contrat.operateur(auteur)) + ")";
        String chemin = "/portail/contrats/" + contrat.id();
        String lien = "\n\nPour consulter le contrat : " + parametres.urlPublique() + chemin + "\n";
        Optional<Message> message = switch (fait.action()) {
            case OUVERTURE -> Optional.of(new Message("Projet de contrat " + contrat.numero() + " à approuver",
                parQui + " vous propose un projet de contrat d'achat d'Armagnac, n° " + contrat.numero()
                        + ", dont vous êtes " + auteur.autre().libelle().toLowerCase(Locale.ROOT)
                        + ". Il attend votre approbation." + lien,
                parQui + " vous propose un projet de contrat ; il attend votre approbation."));
            case MODIFICATION -> Optional.of(new Message("Projet de contrat " + contrat.numero() + " modifié",
                parQui + " a modifié le projet de contrat " + contrat.numero() + " (version " + fait.detail()
                        + "). Voyez ce qui a changé, puis approuvez, modifiez ou refusez." + lien,
                parQui + " a modifié le projet. Voyez ce qui a changé, puis approuvez, modifiez ou refusez."));
            case CONCLUSION -> Optional.of(new Message("Contrat " + contrat.numero() + " conclu",
                parQui + " a approuvé le contrat " + contrat.numero() + " : il est conclu le "
                        + contrat.dateContrat().map(DATE::format).orElse("") + "." + lien,
                parQui + " a approuvé le contrat : il est conclu."));
            case REFUS -> Optional.of(new Message("Projet de contrat " + contrat.numero() + " refusé",
                parQui + " a refusé le projet de contrat " + contrat.numero() + "." + lien,
                parQui + " a refusé le projet de contrat."));
            case RETIRAISON_DECLAREE -> Optional.of(new Message("Retiraison à valider, contrat " + contrat.numero(),
                parQui + " a déclaré la retiraison n° " + fait.detail() + " du contrat " + contrat.numero()
                        + ", pour le " + enlevement(contrat, fait) + ". Elle attend votre validation." + lien,
                parQui + " a déclaré la retiraison n° " + fait.detail() + ", pour le " + enlevement(contrat, fait)
                        + " ; elle attend votre validation."));
            case RETIRAISON_MODIFIEE -> Optional.of(new Message("Retiraison modifiée, contrat " + contrat.numero(),
                parQui + " a modifié la retiraison n° " + fait.detail() + " du contrat " + contrat.numero()
                        + " (enlèvement le " + enlevement(contrat, fait) + "). Voyez ce qui a changé, puis validez"
                        + " ou modifiez." + lien,
                parQui + " a modifié la retiraison n° " + fait.detail() + " (enlèvement le " + enlevement(contrat, fait)
                        + "). Voyez ce qui a changé, puis validez ou modifiez."));
            case RETIRAISON_VALIDEE -> Optional.of(new Message("Retiraison validée, contrat " + contrat.numero(),
                parQui + " a validé la retiraison n° " + fait.detail() + " du contrat " + contrat.numero()
                        + " : elle est inscrite dans vos stocks estimés et dans votre DSM." + lien,
                parQui + " a validé la retiraison n° " + fait.detail() + " : elle est inscrite dans votre DSM."));
            case ANNULATION -> Optional.of(new Message("Contrat " + contrat.numero() + " annulé",
                parQui + " a annulé le contrat " + contrat.numero() + " ; ses retiraisons validées restent." + lien,
                parQui + " a annulé le contrat ; ses retiraisons validées restent."));
            case CLOTURE -> Optional.of(new Message("Contrat " + contrat.numero() + " exécuté",
                parQui + " a déclaré que le contrat " + contrat.numero() + " n'aura plus de retiraison : il est"
                        + " exécuté." + lien,
                parQui + " a déclaré que le contrat n'aura plus de retiraison : il est exécuté."));
            default -> Optional.empty();
        };
        message.ifPresent(m -> {
            List<PersonneVue> destinataires = operateurs.personnes(contrat.operateur(auteur.autre())).stream()
                    .filter(p -> p.role().filter(r -> r == Role.AGENT || r == Role.ADMIN).isPresent())
                    .toList();
            destinataires.stream()
                    .map(PersonneVue::email)
                    .flatMap(Optional::stream)
                    .distinct()
                    .forEach(email -> envoi.envoyer(new Courriel(email, m.sujet(), m.texte())));
            Set<Long> utilisateurs = destinataires.stream()
                    .map(PersonneVue::utilisateur)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());
            if (!utilisateurs.isEmpty()) {
                notifications.envoyer(new NotificationContrat(utilisateurs, m.sujet(), m.push(), chemin));
            }
        });
    }

    /** @param push la phrase de la notification push, sous le sujet qui lui sert de titre */
    private record Message(String sujet, String texte, String push) {
    }

    /** La date d'enlèvement de la retiraison du fait : « 30 octobre 2026 ». */
    private static String enlevement(ContratAchatArmagnac contrat, Fait fait) {
        return DATE.format(contrat.retiraison(fait.detail()).courante().contenu().dateEnlevement());
    }

    private String nom(long operateur) {
        return operateurs.operateur(operateur).map(OperateurVue::raisonSociale).orElse("opérateur n° " + operateur);
    }
}
