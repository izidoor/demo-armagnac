package fr.bnia.contratsarmagnac.adapter.persistence;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.contratsarmagnac.application.ContratLigne;
import fr.bnia.contratsarmagnac.application.ContratRepository;
import fr.bnia.contratsarmagnac.application.ContratsLecture;
import fr.bnia.contratsarmagnac.domain.Approbation;
import fr.bnia.contratsarmagnac.domain.Auteur;
import fr.bnia.contratsarmagnac.domain.ContenuContrat;
import fr.bnia.contratsarmagnac.domain.ContenuRetiraison;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.EtatRetiraison;
import fr.bnia.contratsarmagnac.domain.LigneContrat;
import fr.bnia.contratsarmagnac.domain.LigneRetiraison;
import fr.bnia.contratsarmagnac.domain.Mercuriale.LigneConclue;
import fr.bnia.contratsarmagnac.domain.Paiement;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.contratsarmagnac.domain.Retiraison;
import fr.bnia.contratsarmagnac.domain.Solde;
import fr.bnia.contratsarmagnac.domain.VersionContrat;
import fr.bnia.contratsarmagnac.domain.VersionRetiraison;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Le contrat et ses approbations forment un agrégat Spring Data JDBC, avec verrou optimiste ; ses versions, en ajout
 * seul (A-CA-2), s'insèrent à part, une fois pour toutes, et se relisent avec lui ; ses retiraisons sont réécrites à
 * chaque enregistrement. Version courante et partie attendue
 * sont recopiées sur le contrat pour les listes.
 */
@Repository
class ContratJdbc implements ContratRepository, ContratsLecture {

    static final String SCHEMA = "contratsarmagnac";

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    ContratJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public ContratAchatArmagnac inserer(ContratAchatArmagnac contrat) {
        Ligne ligne = template.insert(versLigne(contrat));
        insererVersions(ligne.id(), contrat.versions(), 0);
        reecrireRetiraisons(ligne.id(), contrat.retiraisons());
        return parId(ligne.id()).orElseThrow();
    }

    @Override
    public ContratAchatArmagnac mettreAJour(ContratAchatArmagnac contrat) {
        int dejaEnregistrees = jdbcClient.sql("select coalesce(max(numero), 0) from " + SCHEMA
                + ".version_contrat where contrat_id = :contrat").param("contrat", contrat.id()).query(Integer.class)
                .single();
        try {
            template.update(versLigne(contrat));
        } catch (OptimisticLockingFailureException concurrente) {
            throw new ModificationConcurrente(null, "Le contrat " + contrat.numero() + " a été modifié entre-temps"
                    + " par l'autre partie : rechargez l'écran pour voir ce qui a changé.");
        }
        insererVersions(contrat.id(), contrat.versions(), dejaEnregistrees);
        reecrireRetiraisons(contrat.id(), contrat.retiraisons());
        return parId(contrat.id()).orElseThrow();
    }

    @Override
    public Optional<ContratAchatArmagnac> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class))
                .map(l -> versDomaine(l, versions(id), retiraisons(id)));
    }

    private void insererVersions(long contrat, List<VersionContrat> versions, int dejaEnregistrees) {
        for (VersionContrat v : versions) {
            if (v.numero() <= dejaEnregistrees) {
                continue;
            }
            ContenuContrat c = v.contenu();
            jdbcClient.sql("insert into " + SCHEMA + ".version_contrat (contrat_id, numero, partie, auteur_id,"
                    + " nom_auteur, le, caracteristiques, conditions_retiraison,"
                    + " a_retirer_avant_le, solde, acompte) values (:contrat, :numero, :partie, :auteur, :nom, :le,"
                    + " :caracteristiques, :conditions, :avant, :solde, :acompte)")
                    .param("contrat", contrat)
                    .param("numero", v.numero())
                    .param("partie", v.auteur().partie().name())
                    .param("auteur", v.auteur().utilisateur())
                    .param("nom", v.auteur().nom())
                    .param("le", Timestamp.from(v.auteur().le()))
                    .param("caracteristiques", c.caracteristiques())
                    .param("conditions", c.conditionsRetiraison())
                    .param("avant", c.aRetirerAvantLe())
                    .param("solde", c.paiement().solde().name())
                    .param("acompte", c.paiement().acompte())
                    .update();
            for (int rang = 0; rang < c.lignes().size(); rang++) {
                LigneContrat l = c.lignes().get(rang);
                jdbcClient.sql("insert into " + SCHEMA + ".ligne_contrat (contrat_id, version_numero, rang, appellation,"
                        + " millesime, denomination, compte, volume, degre, prix) values (:contrat, :version, :rang,"
                        + " :appellation, :millesime, :denomination, :compte, :volume, :degre, :prix)")
                        .param("contrat", contrat)
                        .param("version", v.numero())
                        .param("rang", rang)
                        .param("appellation", l.appellation().name())
                        .param("millesime", l.millesime() == null ? null : l.millesime().annee(), Types.INTEGER)
                        .param("denomination", l.denomination())
                        .param("compte", l.compte().toString())
                        .param("volume", l.volume())
                        .param("degre", l.degre())
                        .param("prix", l.prix())
                        .update();
            }
        }
    }

    /**
     * Les retiraisons sont peu nombreuses et une retiraison retirée disparaît (RG-CA-16) : elles sont supprimées puis
     * réinsérées, versions, lignes et approbations comprises.
     */
    private void reecrireRetiraisons(long contrat, List<Retiraison> retiraisons) {
        jdbcClient.sql("delete from " + SCHEMA + ".retiraison where contrat_id = :contrat").param("contrat", contrat)
                .update();
        for (Retiraison r : retiraisons) {
            jdbcClient.sql("insert into " + SCHEMA + ".retiraison (contrat_id, numero, etat)"
                            + " values (:contrat, :numero, :etat)")
                    .param("contrat", contrat).param("numero", r.numero()).param("etat", r.etat().name()).update();
            for (VersionRetiraison v : r.versions()) {
                jdbcClient.sql("insert into " + SCHEMA + ".version_retiraison (contrat_id, retiraison_numero, numero,"
                                + " partie, auteur_id, nom_auteur, le, date_enlevement, derniere) values (:contrat,"
                                + " :retiraison, :numero, :partie, :auteur, :nom, :le, :date, :derniere)")
                        .param("contrat", contrat)
                        .param("retiraison", r.numero())
                        .param("numero", v.numero())
                        .param("partie", v.auteur().partie().name())
                        .param("auteur", v.auteur().utilisateur())
                        .param("nom", v.auteur().nom())
                        .param("le", Timestamp.from(v.auteur().le()))
                        .param("date", v.contenu().dateEnlevement())
                        .param("derniere", v.contenu().derniere())
                        .update();
                for (LigneRetiraison l : v.contenu().lignes()) {
                    jdbcClient.sql("insert into " + SCHEMA + ".ligne_retiraison (contrat_id, retiraison_numero,"
                                    + " version_numero, rang_ligne, volume) values (:contrat, :retiraison, :version,"
                                    + " :rang, :volume)")
                            .param("contrat", contrat).param("retiraison", r.numero()).param("version", v.numero())
                            .param("rang", l.rang()).param("volume", l.volume()).update();
                }
            }
            List<Approbation> approbations = r.approbations();
            for (int rang = 0; rang < approbations.size(); rang++) {
                Approbation a = approbations.get(rang);
                jdbcClient.sql("insert into " + SCHEMA + ".approbation_retiraison (contrat_id, retiraison_numero, rang,"
                                + " partie, version_numero, auteur_id, nom_auteur, le) values (:contrat, :retiraison,"
                                + " :rang, :partie, :version, :auteur, :nom, :le)")
                        .param("contrat", contrat)
                        .param("retiraison", r.numero())
                        .param("rang", rang)
                        .param("partie", a.partie().name())
                        .param("version", a.version())
                        .param("auteur", a.auteur().utilisateur())
                        .param("nom", a.auteur().nom())
                        .param("le", Timestamp.from(a.auteur().le()))
                        .update();
            }
        }
    }

    private List<Retiraison> retiraisons(long contrat) {
        Map<String, List<LigneRetiraison>> lignes = new HashMap<>();
        jdbcClient.sql("select * from " + SCHEMA + ".ligne_retiraison where contrat_id = :contrat"
                        + " order by retiraison_numero, version_numero, rang_ligne").param("contrat", contrat)
                .query((rs, n) -> lignes.computeIfAbsent(rs.getInt("retiraison_numero") + "/" + rs.getInt("version_numero"),
                        k -> new ArrayList<>()).add(new LigneRetiraison(rs.getInt("rang_ligne"),
                        rs.getBigDecimal("volume"))))
                .list();
        Map<Integer, List<VersionRetiraison>> versions = new HashMap<>();
        jdbcClient.sql("select * from " + SCHEMA + ".version_retiraison where contrat_id = :contrat"
                        + " order by retiraison_numero, numero").param("contrat", contrat)
                .query((rs, n) -> {
                    int retiraison = rs.getInt("retiraison_numero");
                    int numero = rs.getInt("numero");
                    return versions.computeIfAbsent(retiraison, k -> new ArrayList<>()).add(new VersionRetiraison(numero,
                            auteur(rs), new ContenuRetiraison(rs.getObject("date_enlevement", LocalDate.class),
                                    rs.getBoolean("derniere"), lignes.getOrDefault(retiraison + "/" + numero, List.of()))));
                })
                .list();
        Map<Integer, List<Approbation>> approbations = new HashMap<>();
        jdbcClient.sql("select * from " + SCHEMA + ".approbation_retiraison where contrat_id = :contrat"
                        + " order by retiraison_numero, rang").param("contrat", contrat)
                .query((rs, n) -> approbations.computeIfAbsent(rs.getInt("retiraison_numero"), k -> new ArrayList<>())
                        .add(new Approbation(rs.getInt("version_numero"), auteur(rs))))
                .list();
        return jdbcClient.sql("select numero, etat from " + SCHEMA + ".retiraison where contrat_id = :contrat"
                        + " order by numero").param("contrat", contrat)
                .query((rs, n) -> {
                    int numero = rs.getInt("numero");
                    return Retiraison.reconstituer(numero, EtatRetiraison.valueOf(rs.getString("etat")),
                            versions.getOrDefault(numero, List.of()), approbations.getOrDefault(numero, List.of()));
                })
                .list();
    }

    private static Auteur auteur(ResultSet rs) throws SQLException {
        return new Auteur(Partie.valueOf(rs.getString("partie")), rs.getLong("auteur_id"), rs.getString("nom_auteur"),
                rs.getTimestamp("le").toInstant());
    }

    private List<VersionContrat> versions(long contrat) {
        Map<Integer, List<LigneContrat>> lignes = new HashMap<>();
        jdbcClient.sql("select * from " + SCHEMA + ".ligne_contrat where contrat_id = :contrat"
                + " order by version_numero, rang").param("contrat", contrat)
                .query((rs, n) -> {
                    Integer millesime = (Integer) rs.getObject("millesime");
                    lignes.computeIfAbsent(rs.getInt("version_numero"), k -> new ArrayList<>()).add(new LigneContrat(
                            Appellation.valueOf(rs.getString("appellation")),
                            millesime == null ? null : new Millesime(millesime), rs.getString("denomination"),
                            CompteDAgeDsm.de(rs.getString("compte")), rs.getBigDecimal("volume"),
                            rs.getBigDecimal("degre"), rs.getBigDecimal("prix")));
                    return null;
                }).list();
        return jdbcClient.sql("select * from " + SCHEMA + ".version_contrat where contrat_id = :contrat order by numero")
                .param("contrat", contrat)
                .query((rs, n) -> {
                    int numero = rs.getInt("numero");
                    return new VersionContrat(numero, new Auteur(Partie.valueOf(rs.getString("partie")),
                            rs.getLong("auteur_id"), rs.getString("nom_auteur"), rs.getTimestamp("le").toInstant()),
                            new ContenuContrat(lignes.getOrDefault(numero, List.of()),
                                    rs.getString("caracteristiques"),
                                    rs.getString("conditions_retiraison"),
                                    rs.getObject("a_retirer_avant_le", LocalDate.class),
                                    new Paiement(Solde.valueOf(rs.getString("solde")), rs.getBigDecimal("acompte"))));
                })
                .list();
    }

    /**
     * RG-CA-25 : les contrats sur leur version courante, totaux calculés en base comme dans le domaine (RG-CA-04 :
     * volume AP = volume × degré ; montant = volume × degré × prix / 100, arrondi au demi supérieur).
     */
    @Override
    public List<ContratLigne> lister(Optional<Long> operateur, Optional<String> numero) {
        Map<String, Object> parametres = new HashMap<>();
        StringBuilder sql = new StringBuilder("select c.id, c.numero, c.etat, c.acheteur_id, c.vendeur_id, c.ouvert_par,"
                + " c.attend, c.date_contrat, c.version_courante, v.le as modifie_le, v.a_retirer_avant_le,"
                + " t.nombre, t.volume_ap, t.montant, p.produit,"
                + " exists (select 1 from " + SCHEMA + ".retiraison r where r.contrat_id = c.id"
                + " and r.etat = 'A_VALIDER_ACHETEUR') as retiraison_attend_acheteur,"
                + " exists (select 1 from " + SCHEMA + ".retiraison r where r.contrat_id = c.id"
                + " and r.etat = 'A_VALIDER_VENDEUR') as retiraison_attend_vendeur"
                + " from " + SCHEMA + ".contrat c"
                + " join " + SCHEMA + ".version_contrat v on v.contrat_id = c.id and v.numero = c.version_courante"
                + " left join lateral (select count(*) as nombre, coalesce(sum(l.volume * l.degre), 0) as volume_ap,"
                + " coalesce(sum(round(l.volume * l.degre * l.prix / 100, 2)), 0) as montant"
                + " from " + SCHEMA + ".ligne_contrat l where l.contrat_id = c.id"
                + " and l.version_numero = c.version_courante) t on true"
                + " left join lateral (select coalesce(l.millesime::text, l.denomination || ' ' || l.compte) as produit"
                + " from " + SCHEMA + ".ligne_contrat l where l.contrat_id = c.id"
                + " and l.version_numero = c.version_courante order by l.rang limit 1) p on true"
                + " where true");
        operateur.ifPresent(o -> {
            sql.append(" and (c.acheteur_id = :operateur or c.vendeur_id = :operateur)");
            parametres.put("operateur", o);
        });
        numero.ifPresent(n -> {
            sql.append(" and c.numero ilike :numero");
            parametres.put("numero", "%" + n.strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        });
        sql.append(" order by v.le desc, c.id desc");
        return jdbcClient.sql(sql.toString()).params(parametres).query((rs, n) -> ligneListee(rs)).list();
    }

    /** RG-CA-26 : les lignes de la version courante des contrats conclus ou exécutés, datés dans la période. */
    @Override
    public List<LigneConclue> lignesConclues(LocalDate du, LocalDate au) {
        return jdbcClient.sql("select c.id, l.millesime, l.denomination, l.compte, l.volume, l.degre, l.prix"
                        + " from " + SCHEMA + ".contrat c join " + SCHEMA + ".ligne_contrat l on l.contrat_id = c.id"
                        + " and l.version_numero = c.version_courante"
                        + " where c.etat in ('CONCLU', 'EXECUTE') and c.date_contrat between :du and :au"
                        + " order by c.date_contrat, c.id, l.rang")
                .param("du", du)
                .param("au", au)
                .query((rs, n) -> new LigneConclue(rs.getLong("id"), (Integer) rs.getObject("millesime"),
                        rs.getString("denomination"), CompteDAgeDsm.de(rs.getString("compte")),
                        VolumeAlcoolPur.litres(rs.getBigDecimal("volume").multiply(rs.getBigDecimal("degre"))),
                        rs.getBigDecimal("prix")))
                .list();
    }

    private static ContratLigne ligneListee(ResultSet rs) throws SQLException {
        String attend = rs.getString("attend");
        Set<Partie> retiraisonsAttendent = EnumSet.noneOf(Partie.class);
        if (rs.getBoolean("retiraison_attend_acheteur")) {
            retiraisonsAttendent.add(Partie.ACHETEUR);
        }
        if (rs.getBoolean("retiraison_attend_vendeur")) {
            retiraisonsAttendent.add(Partie.VENDEUR);
        }
        return new ContratLigne(rs.getLong("id"), rs.getString("numero"), EtatContrat.valueOf(rs.getString("etat")),
                rs.getLong("acheteur_id"), rs.getLong("vendeur_id"), Partie.valueOf(rs.getString("ouvert_par")),
                Optional.ofNullable(attend).map(Partie::valueOf), retiraisonsAttendent,
                Optional.ofNullable(rs.getObject("date_contrat", LocalDate.class)),
                rs.getTimestamp("modifie_le").toInstant(), rs.getInt("version_courante"), rs.getInt("nombre"),
                rs.getString("produit"), VolumeAlcoolPur.litres(rs.getBigDecimal("volume_ap")),
                rs.getBigDecimal("montant"), Optional.ofNullable(rs.getObject("a_retirer_avant_le", LocalDate.class)));
    }

    private static Ligne versLigne(ContratAchatArmagnac c) {
        return new Ligne(c.id(), c.numero(), c.acheteur(), c.vendeur(), c.ouvertPar().name(), c.etat().name(),
                c.dateContrat().orElse(null), c.courante().numero(),
                c.partieQuiDoitApprouver().map(Enum::name).orElse(null), c.retiraisonsNumerotees(),
                c.approbations().stream().map(a -> new ApprobationLigne(a.partie().name(), a.version(),
                        a.auteur().utilisateur(), a.auteur().nom(), a.auteur().le())).toList(),
                c.version());
    }

    private static ContratAchatArmagnac versDomaine(Ligne l, List<VersionContrat> versions,
            List<Retiraison> retiraisons) {
        return ContratAchatArmagnac.reconstituer(l.id(), l.numero(), l.acheteur(), l.vendeur(),
                Partie.valueOf(l.ouvertPar()), EtatContrat.valueOf(l.etat()), l.dateContrat(), versions,
                l.approbations().stream().map(a -> new Approbation(a.versionNumero(), new Auteur(
                        Partie.valueOf(a.partie()), a.auteurId(), a.nomAuteur(), a.le()))).collect(Collectors.toList()),
                retiraisons, l.retiraisonsNumerotees(), l.version());
    }

    @Table(schema = SCHEMA, name = "contrat")
    record Ligne(
            @Id Long id,
            String numero,
            @Column("acheteur_id") long acheteur,
            @Column("vendeur_id") long vendeur,
            String ouvertPar,
            String etat,
            LocalDate dateContrat,
            int versionCourante,
            String attend,
            int retiraisonsNumerotees,
            @MappedCollection(idColumn = "contrat_id", keyColumn = "rang") List<ApprobationLigne> approbations,
            @Version Long version) {
    }

    @Table(schema = SCHEMA, name = "approbation")
    record ApprobationLigne(String partie, int versionNumero, long auteurId, String nomAuteur, Instant le) {
    }
}
