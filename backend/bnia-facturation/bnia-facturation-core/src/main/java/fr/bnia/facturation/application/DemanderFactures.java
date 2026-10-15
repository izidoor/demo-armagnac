package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.api.CatalogueArticles;
import fr.bnia.facturation.api.DemandeFacture;
import fr.bnia.facturation.api.DemandeFactures;
import fr.bnia.facturation.api.LigneDemandee;
import fr.bnia.facturation.api.LigneDemandee.ArticleDemande;
import fr.bnia.facturation.api.LigneDemandee.GroupeDemande;
import fr.bnia.facturation.api.LigneDemandee.TexteDemande;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.facturation.domain.Auteur;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.LigneFacture;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import fr.bnia.facturation.domain.LigneFacture.LigneInformative;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Demander une ou plusieurs factures (UC-FAC-1), appelé par les sources par l'API. RG-FAC-01 à 08, 11, 20, 26.
 */
@UseCase
public class DemanderFactures implements DemandeFactures {

    private final FactureRepository factures;
    private final JournalFacturation journal;
    private final Clients clients;
    private final Catalogues catalogues;
    private final Controles controles;
    private final Clock horloge;

    public DemanderFactures(FactureRepository factures, JournalFacturation journal, Clients clients,
            DroitsFacturation droits, List<CatalogueArticles> catalogues, Clock horloge) {
        this.factures = factures;
        this.journal = journal;
        this.clients = clients;
        this.catalogues = new Catalogues(catalogues);
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /**
     * RG-FAC-01, 26 : un brouillon par demande, journalisé {@code DEMANDEE} côté vendeur ; tout ou rien, la
     * transaction du cas d'usage étant annulée au premier refus, qui nomme la demande en cause.
     */
    @Override
    public List<Long> demander(Acteur acteur, List<DemandeFacture> demandes) {
        controles.exigerGestion(acteur);
        Auteur auteur = controles.auteur(acteur);
        Instant maintenant = horloge.instant();
        Set<String> origines = new HashSet<>();
        List<Long> ids = new ArrayList<>();
        for (DemandeFacture demande : demandes) {
            try {
                ids.add(creer(demande, auteur, maintenant, origines));
            } catch (RegleEnfreinte refus) {
                throw demandes.size() == 1 ? refus : nommer(demande, refus);
            }
        }
        return ids;
    }

    private long creer(DemandeFacture demande, Auteur auteur, Instant maintenant, Set<String> origines) {
        Origine origine = demande.origine();
        // RG-FAC-02 : une origine n'a qu'une facture vivante, y compris parmi les demandes d'un même lot.
        if (!origines.add(origine.source() + "/" + origine.identifiant())
                || factures.origineVivante(origine.source(), origine.identifiant())) {
            throw new ConflitUnicite("RG-FAC-02", origine.reference() + " a déjà une facture.");
        }
        // RG-FAC-11 : l'acheteur d'un client se lit chez l'hôte.
        if (demande.client() == null) {
            throw new RegleEnfreinte("RG-FAC-11", "Une demande de facture désigne son client.");
        }
        Acheteur acheteur = clients.acheteur(demande.client()).orElseThrow(() -> new RegleEnfreinte("RG-FAC-11",
                "Client introuvable : " + demande.client().code() + "."));
        LocalDate date = demande.dateOperation();
        if (date == null) {
            throw new RegleEnfreinte("RG-FAC-01", "Une demande de facture a une date de l'opération.");
        }
        List<LigneFacture> lignes = demande.lignes().stream().map(l -> ligne(l, date)).toList();
        Facture brouillon = factures.inserer(Facture.demandee(origine, acheteur, date, demande.libellePeriode(),
                demande.condition(), demande.bonDeCommande(), demande.noteEntete(), demande.notePied(), lignes, auteur,
                maintenant));
        journal.ajouter(EntreeJournal.par(auteur, brouillon.id(), maintenant, EvenementJournal.DEMANDEE,
                CoteJournal.VENDEUR, origine.reference()));
        return brouillon.id();
    }

    /** RG-FAC-03, 04 : les lignes demandées, articles résolus à la date de l'opération. */
    private LigneFacture ligne(LigneDemandee ligne, LocalDate date) {
        return switch (ligne) {
            case ArticleDemande a -> catalogues.resoudre(a, date);
            case TexteDemande t -> new LigneInformative(t.texte());
            case GroupeDemande g -> new LigneGroupe(g.titre(), g.lignes().stream().map(l -> {
                if (l instanceof GroupeDemande) {
                    throw new RegleEnfreinte("RG-FAC-04", "Le groupe « " + g.titre() + " » ne contient pas d'autre groupe.");
                }
                return ligne(l, date);
            }).toList());
        };
    }

    /** RG-FAC-01 : dans un lot, l'erreur nomme la demande en cause. */
    private static RegleEnfreinte nommer(DemandeFacture demande, RegleEnfreinte refus) {
        String message = demande.origine().reference() + " : " + refus.getMessage();
        String regle = refus.regle().orElse(null);
        return refus instanceof ConflitUnicite ? new ConflitUnicite(regle, message) : new RegleEnfreinte(regle, message);
    }
}
