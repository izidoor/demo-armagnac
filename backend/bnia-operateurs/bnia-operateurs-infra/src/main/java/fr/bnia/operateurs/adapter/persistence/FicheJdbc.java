package fr.bnia.operateurs.adapter.persistence;

import static fr.bnia.operateurs.adapter.persistence.OperateurJdbc.SCHEMA;

import fr.bnia.operateurs.application.FicheRepository;
import fr.bnia.operateurs.domain.CoordonneesProfessionnelles;
import fr.bnia.operateurs.domain.FicheProfessionnelle;
import fr.bnia.operateurs.domain.TypeFiche;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Repository;

/** Agrégat {@code FicheProfessionnelle} en Spring Data JDBC (A-OPE-6), versionné (RG-OPE-51). */
@Repository
class FicheJdbc implements FicheRepository {

    private final JdbcAggregateTemplate template;

    FicheJdbc(JdbcAggregateTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<FicheProfessionnelle> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(FicheJdbc::versDomaine);
    }

    @Override
    public FicheProfessionnelle inserer(FicheProfessionnelle fiche) {
        return versDomaine(template.insert(versLigne(fiche)));
    }

    @Override
    public void mettreAJour(FicheProfessionnelle fiche) {
        Versionnage.mettreAJour(template, versLigne(fiche), "La fiche professionnelle");
    }

    @Override
    public void supprimer(long id) {
        template.deleteById(id, Ligne.class);
    }

    private static Ligne versLigne(FicheProfessionnelle f) {
        CoordonneesProfessionnelles c = f.coordonnees();
        return new Ligne(f.id(), f.operateur(), f.type(), c.fonction(), c.telephoneProfessionnel(),
                c.telephoneSecondaire(), c.adresse(), c.codePostal(), c.ville(), c.pays(), c.observation(),
                c.centresInteret().stream().map(CentreInteretLigne::new).collect(Collectors.toSet()),
                f.modifieePar(), f.modifieeLe(), f.id() == null ? null : f.version());
    }

    private static FicheProfessionnelle versDomaine(Ligne l) {
        return FicheProfessionnelle.reconstituer(l.id(), l.operateurId(), l.type(), new CoordonneesProfessionnelles(
                        l.fonction(), l.telephoneProfessionnel(), l.telephoneSecondaire(), l.adresse(), l.codePostal(),
                        l.ville(), l.paysId(),
                        l.centresInteret().stream().map(CentreInteretLigne::centreInteretId).collect(Collectors.toSet()),
                        l.observation()),
                l.modifieePar(), l.modifieeLe(), l.version());
    }

    @Table(schema = SCHEMA, name = "fiche_professionnelle")
    record Ligne(
            @Id Long id,
            long operateurId,
            TypeFiche type,
            String fonction,
            String telephoneProfessionnel,
            String telephoneSecondaire,
            String adresse,
            String codePostal,
            String ville,
            Long paysId,
            String observation,
            @MappedCollection(idColumn = "fiche_id") Set<CentreInteretLigne> centresInteret,
            String modifieePar,
            Instant modifieeLe,
            @Version Long version) {
    }

    @Table(schema = SCHEMA, name = "fiche_centre_interet")
    record CentreInteretLigne(long centreInteretId) {
    }
}
