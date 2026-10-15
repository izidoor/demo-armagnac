package fr.bnia.declarations.adapter.integration;

import fr.bnia.declarations.application.Facturation;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.DemandeFacture;
import fr.bnia.facturation.api.DemandeFactures;
import fr.bnia.facturation.api.LigneDemandee;
import fr.bnia.facturation.api.LigneDemandee.ArticleDemande;
import fr.bnia.facturation.api.LigneDemandee.GroupeDemande;
import fr.bnia.facturation.api.Origine;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Le port {@link Facturation} sur l'API de {@code facturation} (ADR-021) : une DSM devient une demande de facture
 * d'origine {@code declarations}/{@code DSM}, au client de l'opérateur, aux articles du catalogue de la cotisation
 * ({@link CatalogueCotisation}).
 */
@Component
class FacturationAdapter implements Facturation {

    static final String SOURCE = "declarations";
    private static final String NATURE = "DSM";
    private static final String LIBELLE_NATURE = "Cotisation interprofessionnelle Armagnac";

    private final DemandeFactures demandeFactures;

    FacturationAdapter(DemandeFactures demandeFactures) {
        this.demandeFactures = demandeFactures;
    }

    @Override
    public List<Long> demander(Acteur acteur, List<DemandeFactureDsm> demandes) {
        return demandeFactures.demander(new fr.bnia.facturation.api.Acteur(acteur.utilisateur()),
                demandes.stream().map(FacturationAdapter::demande).toList());
    }

    private static DemandeFacture demande(DemandeFactureDsm d) {
        List<LigneDemandee> groupes = d.groupes().stream().<LigneDemandee>map(g -> new GroupeDemande(g.titre(),
                g.lignes().stream().<LigneDemandee>map(l -> new ArticleDemande(new ArticleRef(SOURCE, l.code()),
                        l.quantite(), null, null)).toList())).toList();
        return new DemandeFacture(new Origine(SOURCE, NATURE, LIBELLE_NATURE, Long.toString(d.dsm()), d.reference()),
                ClientRef.operateur(d.operateur(), d.codeOperateur()), d.dateOperation(), d.libellePeriode(),
                new ConditionPaiement(d.joursPaiement(), d.finDeMois()), null, null, d.notePied(), groupes);
    }
}
