package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Canal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.ChampContact;
import fr.bnia.operateurs.domain.EntreeJournal;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enregistre un opérateur, puis relève ses faits : entrées du journal des contacts, datées et signées
 * (RG-OPE-34), et événements publiés (ADR-013). Partagé par les cas d'usage du contexte.
 */
final class Enregistreur {

    private final OperateurRepository repository;
    private final JournalContacts journal;
    private final PublicationEvenements publication;
    private final ReferentielsLecture referentiels;
    private final Clock horloge;

    Enregistreur(OperateurRepository repository, JournalContacts journal, PublicationEvenements publication,
            ReferentielsLecture referentiels, Clock horloge) {
        this.repository = repository;
        this.journal = journal;
        this.publication = publication;
        this.referentiels = referentiels;
        this.horloge = horloge;
    }

    void enregistrer(Operateur operateur, Canal canal) {
        repository.mettreAJour(operateur);
        Operateur.Faits faits = operateur.releverFaits();
        Instant maintenant = horloge.instant();
        for (EntreeJournal entree : faits.journal()) {
            journal.ajouter(operateur.id(), new LigneJournal(maintenant, canal.auteur(), entree.contact(),
                    entree.nomContact(), entree.action(), entree.changements().stream().map(this::lisible).toList()));
        }
        faits.evenements().forEach(publication::publier);
    }

    /** Les identifiants de référentiels remplacés par leurs libellés. */
    private Changement lisible(Changement changement) {
        if (!changement.champ().reference()) {
            return changement;
        }
        return new Changement(changement.champ(), libelles(changement.champ(), changement.avant()),
                libelles(changement.champ(), changement.apres()));
    }

    private String libelles(ChampContact champ, String ids) {
        if (ids == null) {
            return null;
        }
        return Arrays.stream(ids.split(",")).map(Long::parseLong)
                .map(id -> libelle(champ, id).orElse("n° " + id))
                .collect(Collectors.joining(", "));
    }

    private Optional<String> libelle(ChampContact champ, long id) {
        return switch (champ) {
            case TITRE -> referentiels.titre(id).map(ValeurVue::libelle);
            case CENTRES_INTERET -> referentiels.centreInteret(id).map(ValeurVue::libelle);
            case PAYS -> referentiels.pays(id).map(p -> p.libelle());
            default -> Optional.empty();
        };
    }

    List<LigneJournal> journal(long operateur, long contact) {
        return journal.duContact(operateur, contact);
    }
}
