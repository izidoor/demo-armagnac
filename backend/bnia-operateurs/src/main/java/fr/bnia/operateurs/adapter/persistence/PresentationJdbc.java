package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.operateurs.application.PresentationRepository;
import fr.bnia.operateurs.domain.CarteBancaire;
import fr.bnia.operateurs.domain.CircuitPresente;
import fr.bnia.operateurs.domain.Distinction;
import fr.bnia.operateurs.domain.LabelObtenu;
import fr.bnia.operateurs.domain.MarquePresentee;
import fr.bnia.operateurs.domain.MedailleConcours;
import fr.bnia.operateurs.domain.ModePresentation;
import fr.bnia.operateurs.domain.Oenotourisme;
import fr.bnia.operateurs.domain.Portee;
import fr.bnia.operateurs.domain.PresentationOperateur;
import fr.bnia.operateurs.domain.VisiteProposee;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Repository;

/**
 * Agrégat {@code PresentationOperateur} en Spring Data JDBC (ADR-004, ADR-012) : même identifiant que
 * l'opérateur, inséré au premier enregistrement ; ses listes sont réécrites à chaque enregistrement,
 * sans toucher la fiche.
 */
@Repository
class PresentationJdbc implements PresentationRepository {

    private static final String SCHEMA = OperateurJdbc.SCHEMA;
    private static final String ID = "presentation_id";

    private final JdbcAggregateTemplate template;

    PresentationJdbc(JdbcAggregateTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<PresentationOperateur> parId(long operateur) {
        return Optional.ofNullable(template.findById(operateur, Ligne.class)).map(PresentationJdbc::versDomaine);
    }

    @Override
    public void enregistrer(PresentationOperateur presentation) {
        Ligne ligne = versLigne(presentation);
        if (template.existsById(presentation.id(), Ligne.class)) {
            template.update(ligne);
        } else {
            template.insert(ligne);
        }
    }

    private static Ligne versLigne(PresentationOperateur p) {
        Oenotourisme o = p.oenotourisme();
        return new Ligne(p.id(),
                p.marques().stream().map(m -> new MarqueLigne(m.portee(), m.marque(), m.diffusableWeb())).toList(),
                p.circuits().stream().map(c -> new CircuitLigne(c.portee(), c.circuit())).toList(),
                p.visites().stream().map(v -> new VisiteLigne(v.typeVisite(), v.commentaire())).toList(),
                p.labels().stream().map(l -> new LabelLigne(l.label(), l.commentaire())).toList(),
                o.paiementCarte(), o.cartes().stream().map(CarteLigne::new).collect(Collectors.toSet()),
                o.degustationGratuite(), o.lieuDeVente(), o.accueilGroupes(), o.adhesionOenotourisme(),
                o.langues().stream().map(LangueLigne::new).collect(Collectors.toSet()),
                o.produitsCommercialises().stream().map(ProduitLigne::new).collect(Collectors.toSet()),
                o.modePresentation(), o.presentationFr(), o.presentationEn(),
                p.medailles().stream().map(m -> new MedailleLigne(m.concours(),
                        m.annee() == null ? null : m.annee().annee(), m.distinction(), m.produit())).toList());
    }

    private static PresentationOperateur versDomaine(Ligne l) {
        return PresentationOperateur.reconstituer(l.id(),
                l.marques().stream().map(m -> new MarquePresentee(m.portee(), m.marqueId(), m.diffusableWeb())).toList(),
                l.circuits().stream().map(c -> new CircuitPresente(c.portee(), c.circuitDistributionId())).toList(),
                l.visites().stream().map(v -> new VisiteProposee(v.typeVisiteId(), v.commentaire())).toList(),
                l.labels().stream().map(x -> new LabelObtenu(x.labelId(), x.commentaire())).toList(),
                new Oenotourisme(l.paiementCarte(), l.cartes().stream().map(CarteLigne::carte).collect(Collectors.toSet()),
                        l.degustationGratuite(), l.lieuDeVente(), l.accueilGroupes(), l.adhesionOenotourisme(),
                        l.langues().stream().map(LangueLigne::langueId).collect(Collectors.toSet()),
                        l.produits().stream().map(ProduitLigne::produitCommercialiseId).collect(Collectors.toSet()),
                        l.modePresentation(), l.presentationFr(), l.presentationEn()),
                l.medailles().stream().map(m -> new MedailleConcours(m.concours(),
                        m.annee() == null ? null : Millesime.de(m.annee()), m.distinction(), m.produit())).toList());
    }

    @Table(schema = SCHEMA, name = "presentation_operateur")
    record Ligne(
            @Id Long id,
            @MappedCollection(idColumn = ID, keyColumn = "rang") List<MarqueLigne> marques,
            @MappedCollection(idColumn = ID, keyColumn = "rang") List<CircuitLigne> circuits,
            @MappedCollection(idColumn = ID, keyColumn = "rang") List<VisiteLigne> visites,
            @MappedCollection(idColumn = ID, keyColumn = "rang") List<LabelLigne> labels,
            boolean paiementCarte,
            @MappedCollection(idColumn = ID) Set<CarteLigne> cartes,
            boolean degustationGratuite,
            boolean lieuDeVente,
            boolean accueilGroupes,
            boolean adhesionOenotourisme,
            @MappedCollection(idColumn = ID) Set<LangueLigne> langues,
            @MappedCollection(idColumn = ID) Set<ProduitLigne> produits,
            ModePresentation modePresentation,
            String presentationFr,
            String presentationEn,
            @MappedCollection(idColumn = ID, keyColumn = "rang") List<MedailleLigne> medailles) {
    }

    @Table(schema = SCHEMA, name = "presentation_marque")
    record MarqueLigne(Portee portee, long marqueId, boolean diffusableWeb) {
    }

    @Table(schema = SCHEMA, name = "presentation_circuit")
    record CircuitLigne(Portee portee, long circuitDistributionId) {
    }

    @Table(schema = SCHEMA, name = "presentation_visite")
    record VisiteLigne(long typeVisiteId, String commentaire) {
    }

    @Table(schema = SCHEMA, name = "presentation_label")
    record LabelLigne(long labelId, String commentaire) {
    }

    @Table(schema = SCHEMA, name = "presentation_carte")
    record CarteLigne(CarteBancaire carte) {
    }

    @Table(schema = SCHEMA, name = "presentation_langue")
    record LangueLigne(long langueId) {
    }

    @Table(schema = SCHEMA, name = "presentation_produit")
    record ProduitLigne(long produitCommercialiseId) {
    }

    @Table(schema = SCHEMA, name = "medaille_concours")
    record MedailleLigne(String concours, Integer annee, Distinction distinction, String produit) {
    }
}
