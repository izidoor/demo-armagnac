package fr.bnia.operateurs.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.operateurs.domain.ActionJournal;
import fr.bnia.operateurs.domain.Changement;
import fr.bnia.operateurs.domain.CoordonneesProfessionnelles;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.Rattachement;
import fr.bnia.operateurs.domain.TypeCible;
import fr.bnia.operateurs.domain.TypeFiche;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.time.Clock;
import java.util.List;

/**
 * Fiches professionnelles (vision UC-18, 25 ; spec 02 §3.2.3) : modifiées par leur titulaire ou par un
 * rôle qui gère le module Contacts de l'opérateur (RG-OPE-46).
 */
@UseCase
public class GererFiches {

    private final ContactRepository contacts;
    private final FicheRepository fiches;
    private final RattachementRepository rattachements;
    private final Habilitations habilitations;
    private final References references;
    private final Journaliste journaliste;
    private final Personnes personnes;

    public GererFiches(OperateurRepository operateurs, ContactRepository contacts, FicheRepository fiches,
            RattachementRepository rattachements, UtilisateurRepository utilisateurs, InvitationRepository invitations,
            JournalActions journal, PublicationEvenements publication, Habilitations habilitations,
            ReferentielsLecture referentiels, Clock horloge) {
        this.contacts = contacts;
        this.fiches = fiches;
        this.rattachements = rattachements;
        this.habilitations = habilitations;
        this.references = new References(referentiels);
        this.journaliste = new Journaliste(journal, utilisateurs, referentiels, horloge);
        this.personnes = new Personnes(operateurs, contacts, fiches, rattachements, utilisateurs, invitations,
                publication, journaliste);
    }

    /** RG-OPE-31, 33 : tout membre lit les fiches de son opérateur. */
    public FicheProfessionnelle consulter(Acteur acteur, long operateur, long id) {
        habilitations.exigerLecture(acteur, operateur, Module.CONTACTS);
        return personnes.fiche(operateur, id);
    }

    /** RG-OPE-21, 33, 34, 46, 51 : sa propre fiche (rattachement {@code ACTIF}), ou le module Contacts géré. */
    public FicheProfessionnelle modifier(Acteur acteur, long operateur, long id, SaisieFiche saisie) {
        FicheProfessionnelle fiche = personnes.fiche(operateur, id);
        if (!estTitulaire(acteur, fiche) && !habilitations.droits(acteur, operateur).gere(Module.CONTACTS)) {
            throw new AccesRefuse("RG-OPE-46", "Vous ne pouvez modifier que votre propre fiche dans cet opérateur.");
        }
        CoordonneesProfessionnelles nouvelles = new CoordonneesProfessionnelles(saisie.fonction(),
                saisie.telephoneProfessionnel(), saisie.telephoneSecondaire(), saisie.adresse(), saisie.codePostal(),
                saisie.ville(), saisie.pays(), saisie.centresInteret(), saisie.observation());
        references.pays(nouvelles.pays(), fiche.coordonnees().pays());
        references.centresInteret(nouvelles.centresInteret(), fiche.coordonnees().centresInteret());
        String nom = personnes.nomDeFiche(id);
        List<Changement> changements = fiche.modifier(nouvelles, saisie.version(), nom, journaliste.nomDe(acteur),
                journaliste.maintenant());
        fiches.mettreAJour(fiche);
        if (!changements.isEmpty()) {
            journaliste.noter(acteur, operateur, ActionJournal.FICHE_MODIFIEE, cible(fiche), idCible(fiche), nom,
                    changements);
        }
        return fiche;
    }

    /** RG-OPE-33 : la fiche de son propre rattachement {@code ACTIF}. */
    private boolean estTitulaire(Acteur acteur, FicheProfessionnelle fiche) {
        return fiche.type() == TypeFiche.RATTACHEMENT && rattachements.parFiche(fiche.id())
                .filter(r -> r.utilisateur() == acteur.utilisateur() && r.estActif()).isPresent();
    }

    private static TypeCible cible(FicheProfessionnelle fiche) {
        return fiche.type() == TypeFiche.CONTACT ? TypeCible.CONTACT : TypeCible.MEMBRE;
    }

    private long idCible(FicheProfessionnelle fiche) {
        return fiche.type() == TypeFiche.CONTACT
                ? contacts.parFiche(fiche.id()).map(c -> c.id()).orElse(fiche.id())
                : rattachements.parFiche(fiche.id()).map(Rattachement::id).orElse(fiche.id());
    }
}
