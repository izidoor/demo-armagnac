package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.commun.domain.CompteDAge;
import fr.bnia.referentiels.application.FiltreValeurs;
import fr.bnia.referentiels.application.PaysLecture;
import fr.bnia.referentiels.application.PaysLigne;
import fr.bnia.referentiels.application.PaysRepository;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.CodeIso;
import fr.bnia.referentiels.domain.DescriptionPays;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Pays;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class PaysJdbc extends ValeursJdbc<Pays, PaysJdbc.Ligne> implements PaysRepository, PaysLecture {

    PaysJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        super(template, jdbcClient, Ligne.class, "pays");
    }

    @Override
    public boolean existeCodeIso(CodeIso codeIso, Long sauf) {
        return jdbcClient.sql("select exists (select 1 from " + table
                        + " where code_iso = :codeIso and id is distinct from :sauf)")
                .param("codeIso", codeIso.valeur())
                .param("sauf", sauf, Types.BIGINT)
                .query(Boolean.class)
                .single();
    }

    @Override
    public List<PaysLigne> lister(FiltreValeurs filtre) {
        Sql.Where where = Sql.where(filtre, "code", "code_iso", "libelle");
        return jdbcClient.sql("select id, code, code_iso, libelle, zone_commerciale_id as zone_commerciale, membre_ue, langue_certificat,"
                        + " niveau_detail_certificat, age_minimum, actif from " + table + where.clause()
                        + " order by libelle, code")
                .params(where.parametres())
                .query(PaysLigne.class)
                .list();
    }

    @Override
    protected Ligne versLigne(Pays pays) {
        DescriptionPays d = pays.description();
        return new Ligne(pays.id(), pays.code().valeur(), d.codeIso().valeur(), pays.libelle().valeur(),
                d.zoneCommerciale(), d.membreUe(), d.langueCertificat(), d.niveauDetailCertificat(),
                d.ageMinimum().map(CompteDAge::toString).orElse(null), pays.estActif());
    }

    @Override
    protected Pays versDomaine(Ligne ligne) {
        var description = new DescriptionPays(CodeIso.de(ligne.codeIso()), ligne.zoneCommercialeId(),
                ligne.membreUe(), ligne.langueCertificat(), ligne.niveauDetailCertificat(),
                Optional.ofNullable(ligne.ageMinimum()).map(CompteDAge::de));
        return Pays.reconstituer(ligne.id(), Code.de(ligne.code()), Libelle.de(ligne.libelle()), description, ligne.actif());
    }

    @Table(schema = SCHEMA, name = "pays")
    record Ligne(
            @Id Long id,
            String code,
            String codeIso,
            String libelle,
            long zoneCommercialeId,
            boolean membreUe,
            LangueCertificat langueCertificat,
            NiveauDetailCertificat niveauDetailCertificat,
            String ageMinimum,
            boolean actif) {
    }
}
