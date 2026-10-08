package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.ChampJournal;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Écrit le journal des personnes et des accès (RG-OPE-34) : date, acteur et cible copiés, références
 * remplacées par leurs libellés. Partagé par les cas d'usage du contexte.
 */
final class Journaliste {

    /** Auteur des actions de l'application elle-même (amorçage, expiration des invitations). */
    static final String APPLICATION = "Application";

    private final JournalActions journal;
    private final UtilisateurRepository utilisateurs;
    private final ReferentielsLecture referentiels;
    private final Clock horloge;

    Journaliste(JournalActions journal, UtilisateurRepository utilisateurs, ReferentielsLecture referentiels,
            Clock horloge) {
        this.journal = journal;
        this.utilisateurs = utilisateurs;
        this.referentiels = referentiels;
        this.horloge = horloge;
    }

    /**
     * @param acteur    {@code null} : l'application
     * @param operateur {@code null} : action sur un utilisateur
     */
    void noter(Acteur acteur, Long operateur, ActionJournal action, TypeCible cible, long idCible, String nomCible,
            List<Changement> changements) {
        journal.ajouter(new LigneJournal(maintenant(), acteur == null ? null : acteur.utilisateur(), nomDe(acteur),
                operateur, action, cible, idCible, nomCible, changements.stream().map(this::lisible).toList()));
    }

    void noter(Acteur acteur, Long operateur, ActionJournal action, TypeCible cible, long idCible, String nomCible) {
        noter(acteur, operateur, action, cible, idCible, nomCible, List.of());
    }

    /** Copie du nom de l'acteur, à l'instant de l'action (A-OPE-8). */
    String nomDe(Acteur acteur) {
        if (acteur == null) {
            return APPLICATION;
        }
        return utilisateurs.parId(acteur.utilisateur()).map(u -> u.identite().nomComplet())
                .orElse("Utilisateur n° " + acteur.utilisateur());
    }

    Instant maintenant() {
        return horloge.instant();
    }

    Clock horloge() {
        return horloge;
    }

    /** Les identifiants de référentiels remplacés par leurs libellés. */
    private Changement lisible(Changement changement) {
        if (!changement.champ().reference()) {
            return changement;
        }
        return new Changement(changement.champ(), libelles(changement.champ(), changement.avant()),
                libelles(changement.champ(), changement.apres()));
    }

    private String libelles(ChampJournal champ, String ids) {
        if (ids == null) {
            return null;
        }
        return Arrays.stream(ids.split(",")).map(Long::parseLong)
                .map(id -> libelle(champ, id).orElse("n° " + id))
                .collect(Collectors.joining(", "));
    }

    private Optional<String> libelle(ChampJournal champ, long id) {
        return switch (champ) {
            case TITRE -> referentiels.titre(id).map(ValeurVue::libelle);
            case CENTRES_INTERET -> referentiels.centreInteret(id).map(ValeurVue::libelle);
            case PAYS -> referentiels.pays(id).map(p -> p.libelle());
            default -> Optional.empty();
        };
    }
}
