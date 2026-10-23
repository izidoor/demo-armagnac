package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.contratsarmagnac.application.EditionsContrats.EditionContrat;
import fr.bnia.contratsarmagnac.application.EditionsContrats.PartieEditee;
import fr.bnia.contratsarmagnac.domain.ActionContrat;
import fr.bnia.contratsarmagnac.domain.Auteur;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.Fait;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.AdresseVue;
import fr.bnia.operateurs.domain.partage.IdentiteLegaleVue;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Édition PDF d'un contrat au formulaire CA-05 allégé (spec 06 §6, RG-CA-20) : au portail par une partie, inscrite au
 * journal (RG-CA-19) ; au back-office par le personnel du BNIA, en lecture (RG-CA-01).
 */
@UseCase
public class EditerContrats {

    /** Un document produit, avec le nom de fichier proposé. */
    public record DocumentEdite(String nom, byte[] contenu) {
    }

    private final ContratRepository contrats;
    private final JournalContrats journal;
    private final EditionsContrats editions;
    private final OperateursLecture operateurs;
    private final Autorisations autorisations;
    private final Clock horloge;

    public EditerContrats(ContratRepository contrats, JournalContrats journal, EditionsContrats editions,
            OperateursLecture operateurs, Clock horloge) {
        this.contrats = contrats;
        this.journal = journal;
        this.editions = editions;
        this.operateurs = operateurs;
        this.autorisations = new Autorisations(operateurs);
        this.horloge = horloge;
    }

    /** RG-CA-01, 19, 20 : le PDF d'un contrat dont l'opérateur actif est partie, noté au journal. */
    public DocumentEdite auPortail(Acteur acteur, long operateur, long id) {
        autorisations.exigerLecture(acteur, operateur);
        ContratAchatArmagnac contrat = charger(id);
        Partie partie = Autorisations.partie(contrat, operateur);
        DocumentEdite document = editer(contrat);
        journal.ajouter(contrat.id(), new Fait(ActionContrat.EDITION_PDF, new Auteur(partie, acteur.utilisateur(),
                operateurs.nomUtilisateur(acteur.utilisateur()).orElse("?"), Instant.now(horloge)), null));
        return document;
    }

    /** RG-CA-01, 20 : le PDF d'un contrat, pour le personnel du BNIA. */
    public DocumentEdite auBackOffice(Acteur acteur, long id) {
        autorisations.exigerPersonnelBnia(acteur);
        return editer(charger(id));
    }

    private ContratAchatArmagnac charger(long id) {
        return contrats.parId(id).orElseThrow(() -> new ElementIntrouvable("Contrat introuvable."));
    }

    private DocumentEdite editer(ContratAchatArmagnac contrat) {
        byte[] pdf = editions.pdf(new EditionContrat(contrat, contrat.campagne(LocalDate.now(horloge)),
                partie(contrat.acheteur(), false), partie(contrat.vendeur(), true)));
        return new DocumentEdite("Contrat-" + contrat.numero() + ".pdf", pdf);
    }

    /** CA-05, partie 1 : identité, adresse, téléphone, n° BNIA, SIRET ; CVI du vendeur. */
    private PartieEditee partie(long id, boolean vendeur) {
        OperateurVue vue = operateurs.operateur(id).orElseThrow();
        Optional<IdentiteLegaleVue> legale = operateurs.identiteLegale(id);
        AdresseVue a = vue.adresse();
        List<String> adresse = new ArrayList<>();
        Stream.of(a.ligne1(), a.ligne2(), a.ligne3()).flatMap(Optional::stream).forEach(adresse::add);
        String ville = Stream.of(a.codePostal(), a.ville()).flatMap(Optional::stream).reduce((x, y) -> x + " " + y)
                .orElse("");
        if (!ville.isBlank()) {
            adresse.add(ville);
        }
        return new PartieEditee(vue.raisonSociale(), adresse, legale.flatMap(IdentiteLegaleVue::telephone),
                vue.code(), legale.flatMap(IdentiteLegaleVue::siret),
                vendeur ? legale.flatMap(IdentiteLegaleVue::cvi) : Optional.empty());
    }
}
