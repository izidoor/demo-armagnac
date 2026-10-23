package fr.bnia.contratsarmagnac.application;

import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.Fait;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.PersonneVue;
import fr.bnia.operateurs.domain.partage.Role;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Rédige les e-mails à l'autre partie (RG-CA-18) et les confie à l'envoi, après la validation de la transaction. Ils
 * vont à ses membres {@code AGENT} et {@code ADMIN} qui ont un e-mail ; le lien vise le contrat au portail.
 */
final class Courrier {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRANCE);

    private final EnvoiCourriels envoi;
    private final OperateursLecture operateurs;
    private final ParametresContrats parametres;

    Courrier(EnvoiCourriels envoi, OperateursLecture operateurs, ParametresContrats parametres) {
        this.envoi = envoi;
        this.operateurs = operateurs;
        this.parametres = parametres;
    }

    /** RG-CA-18 : l'e-mail d'un fait qui attend l'autre partie ou la concerne ; rien pour les autres faits. */
    void notifier(ContratAchatArmagnac contrat, Fait fait) {
        Partie auteur = fait.auteur().partie();
        String parQui = fait.auteur().nom() + " (" + nom(contrat.operateur(auteur)) + ")";
        String lien = "\n\nPour consulter le contrat : " + parametres.urlPublique() + "/portail/contrats/" + contrat.id()
                + "\n";
        Optional<Message> message = switch (fait.action()) {
            case OUVERTURE -> Optional.of(new Message("Projet de contrat " + contrat.numero() + " à approuver",
                parQui + " vous propose un projet de contrat d'achat d'Armagnac, n° " + contrat.numero()
                        + ", dont vous êtes " + auteur.autre().libelle().toLowerCase(Locale.ROOT)
                        + ". Il attend votre approbation." + lien));
            case MODIFICATION -> Optional.of(new Message("Projet de contrat " + contrat.numero() + " modifié",
                parQui + " a modifié le projet de contrat " + contrat.numero() + " (version " + fait.detail()
                        + "). Voyez ce qui a changé, puis approuvez, modifiez ou refusez." + lien));
            case CONCLUSION -> Optional.of(new Message("Contrat " + contrat.numero() + " conclu",
                parQui + " a approuvé le contrat " + contrat.numero() + " : il est conclu le "
                        + contrat.dateContrat().map(DATE::format).orElse("") + "." + lien));
            case REFUS -> Optional.of(new Message("Projet de contrat " + contrat.numero() + " refusé",
                parQui + " a refusé le projet de contrat " + contrat.numero() + "." + lien));
            case RETIRAISON_DECLAREE -> Optional.of(new Message("Retiraison à valider, contrat " + contrat.numero(),
                parQui + " a déclaré la retiraison n° " + fait.detail() + " du contrat " + contrat.numero()
                        + ", pour le " + enlevement(contrat, fait) + ". Elle attend votre validation." + lien));
            case RETIRAISON_MODIFIEE -> Optional.of(new Message("Retiraison modifiée, contrat " + contrat.numero(),
                parQui + " a modifié la retiraison n° " + fait.detail() + " du contrat " + contrat.numero()
                        + " (enlèvement le " + enlevement(contrat, fait) + "). Voyez ce qui a changé, puis validez"
                        + " ou modifiez." + lien));
            case RETIRAISON_VALIDEE -> Optional.of(new Message("Retiraison validée, contrat " + contrat.numero(),
                parQui + " a validé la retiraison n° " + fait.detail() + " du contrat " + contrat.numero()
                        + " : elle est inscrite dans vos stocks estimés et dans votre DSM." + lien));
            case ANNULATION -> Optional.of(new Message("Contrat " + contrat.numero() + " annulé",
                parQui + " a annulé le contrat " + contrat.numero() + " ; ses retiraisons validées restent." + lien));
            case CLOTURE -> Optional.of(new Message("Contrat " + contrat.numero() + " exécuté",
                parQui + " a déclaré que le contrat " + contrat.numero() + " n'aura plus de retiraison : il est"
                        + " exécuté." + lien));
            default -> Optional.empty();
        };
        message.ifPresent(m -> operateurs.personnes(contrat.operateur(auteur.autre())).stream()
                .filter(p -> p.role().filter(r -> r == Role.AGENT || r == Role.ADMIN).isPresent())
                .map(PersonneVue::email)
                .flatMap(Optional::stream)
                .distinct()
                .forEach(email -> envoi.envoyer(new Courriel(email, m.sujet(), m.texte()))));
    }

    private record Message(String sujet, String texte) {
    }

    /** La date d'enlèvement de la retiraison du fait : « 30 octobre 2026 ». */
    private static String enlevement(ContratAchatArmagnac contrat, Fait fait) {
        return DATE.format(contrat.retiraison(fait.detail()).courante().contenu().dateEnlevement());
    }

    private String nom(long operateur) {
        return operateurs.operateur(operateur).map(OperateurVue::raisonSociale).orElse("opérateur n° " + operateur);
    }
}
