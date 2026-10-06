package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.referentiels.application.ClasseTvaLigne;
import fr.bnia.referentiels.application.ClasseTvaRepository;
import fr.bnia.referentiels.application.ClassesTvaLecture;
import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.domain.ClasseTva;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.TauxTva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * La classe et ses taux forment un agrégat Spring Data JDBC : chargés ensemble, et à chaque mise à
 * jour les taux sont supprimés puis réinsérés.
 */
@Repository
class ClasseTvaJdbc extends ValeursJdbc<ClasseTva, ClasseTvaJdbc.Ligne> implements ClasseTvaRepository, ClassesTvaLecture {

    ClasseTvaJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "classe_tva");
    }

    @Override
    public List<ClasseTvaLigne> lister(FiltreValeurs filtre, LocalDate date) {
        Sql.Where where = Sql.where(filtre, "code", "libelle");
        var parametres = new HashMap<>(where.parametres());
        parametres.put("date", date);
        return jdbcClient.sql("select code, libelle, actif, (select t.taux from " + SCHEMA + ".taux_tva t"
                        + " where t.classe_tva_code = c.code and t.date_debut <= :date"
                        + " and (t.date_fin is null or t.date_fin >= :date)) as taux_en_vigueur"
                        + " from " + table + " c" + where.clause() + " order by libelle, code")
                .params(parametres)
                .query(ClasseTvaLigne.class)
                .list();
    }

    @Override
    protected Ligne versLigne(ClasseTva classe) {
        Set<TauxLigne> taux = classe.taux().stream()
                .map(t -> new TauxLigne(t.taux(), t.debut(), t.fin()))
                .collect(Collectors.toSet());
        return new Ligne(classe.code().valeur(), classe.libelle().valeur(), classe.estActif(), taux);
    }

    @Override
    protected ClasseTva versDomaine(Ligne ligne) {
        return ClasseTva.reconstituer(Code.de(ligne.code()), Libelle.de(ligne.libelle()), ligne.actif(),
                ligne.taux().stream().map(t -> new TauxTva(t.taux(), t.dateDebut(), t.dateFin())).toList());
    }

    @Table(schema = SCHEMA, name = "classe_tva")
    record Ligne(
            @Id String code,
            String libelle,
            boolean actif,
            @MappedCollection(idColumn = "classe_tva_code") Set<TauxLigne> taux) {
    }

    @Table(schema = SCHEMA, name = "taux_tva")
    record TauxLigne(BigDecimal taux, LocalDate dateDebut, LocalDate dateFin) {
    }
}
