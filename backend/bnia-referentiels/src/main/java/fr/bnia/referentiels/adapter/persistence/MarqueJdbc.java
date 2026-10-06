package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.MarqueRepository;
import fr.bnia.referentiels.application.MarquesProchesLecture;
import fr.bnia.referentiels.application.ValeurSimpleLigne;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.LibelleNormalise;
import fr.bnia.referentiels.domain.Marque;
import java.util.List;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Le libellé normalisé est stocké (colonne {@code libelle_normalise}, index unique) : la base garantit
 * aussi RG-REF-15, et la recherche des marques proches n'a rien à recalculer.
 */
@Repository
class MarqueJdbc extends ValeursSimplesJdbc<Marque, MarqueJdbc.Ligne>
        implements MarqueRepository, MarquesProchesLecture {

    MarqueJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "marque");
    }

    @Override
    public Optional<Marque> parLibelleNormalise(LibelleNormalise libelle) {
        return jdbcClient.sql("select code from " + table + " where libelle_normalise = :libelle")
                .param("libelle", libelle.valeur())
                .query(String.class)
                .optional()
                .flatMap(code -> parCode(Code.de(code)));
    }

    @Override
    public List<ValeurSimpleLigne> proches(LibelleNormalise texte, int nombreMaximum) {
        return jdbcClient.sql("select code, libelle, actif from " + table
                        + " where libelle_normalise like :motif escape '\\' order by libelle, code limit :limite")
                .param("motif", Sql.motifContient(texte.valeur()))
                .param("limite", nombreMaximum)
                .query(ValeurSimpleLigne.class)
                .list();
    }

    @Override
    protected Ligne versLigne(Marque marque) {
        return new Ligne(marque.code().valeur(), marque.libelle().valeur(), marque.libelleNormalise().valeur(),
                marque.estActif());
    }

    @Override
    protected Marque versDomaine(Ligne ligne) {
        return Marque.reconstituer(Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif());
    }

    @Table(schema = SCHEMA, name = "marque")
    record Ligne(@Id String code, String libelle, String libelleNormalise, boolean actif) {
    }
}
