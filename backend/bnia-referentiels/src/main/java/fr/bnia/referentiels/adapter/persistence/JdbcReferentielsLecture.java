package fr.bnia.referentiels.adapter.persistence;

import fr.bnia.commun.domain.CompteDAge;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.Volume;
import fr.bnia.referentiels.domain.partage.ContenanceVue;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import fr.bnia.referentiels.domain.partage.NiveauDetailCertificat;
import fr.bnia.referentiels.domain.partage.PaysVue;
import fr.bnia.referentiels.domain.partage.RegionVue;
import fr.bnia.referentiels.domain.partage.TauxTvaVue;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Partie publiée en lecture (ADR-008), en SQL direct (ADR-004).
 */
@Repository
class JdbcReferentielsLecture implements ReferentielsLecture {

    private static final String S = ValeursJdbc.SCHEMA + ".";
    private static final String COLONNES_PAYS = "code, code_iso, libelle, zone_commerciale, membre_ue,"
            + " langue_certificat, niveau_detail_certificat, age_minimum, actif";

    private final JdbcClient jdbcClient;

    JdbcReferentielsLecture(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Géographie

    @Override
    public Optional<ValeurVue> zoneCommerciale(String code) {
        return valeur("zone_commerciale", code);
    }

    @Override
    public List<ValeurVue> zonesCommercialesActives() {
        return actives("zone_commerciale");
    }

    @Override
    public Optional<PaysVue> pays(String code) {
        return jdbcClient.sql("select " + COLONNES_PAYS + " from " + S + "pays where code = :code")
                .param("code", code)
                .query(JdbcReferentielsLecture::pays)
                .optional();
    }

    @Override
    public List<PaysVue> paysActifs() {
        return jdbcClient.sql("select " + COLONNES_PAYS + " from " + S + "pays where actif order by libelle, code")
                .query(JdbcReferentielsLecture::pays)
                .list();
    }

    @Override
    public Optional<RegionVue> region(String code) {
        return jdbcClient.sql("select code, libelle, en_region_delimitee, actif from " + S + "region where code = :code")
                .param("code", code)
                .query(RegionVue.class)
                .optional();
    }

    @Override
    public List<RegionVue> regionsActives() {
        return jdbcClient.sql("select code, libelle, en_region_delimitee, actif from " + S + "region"
                        + " where actif order by libelle, code")
                .query(RegionVue.class)
                .list();
    }

    // Qualification

    @Override
    public Optional<ValeurVue> titre(String code) {
        return valeur("titre", code);
    }

    @Override
    public List<ValeurVue> titresActifs() {
        return actives("titre");
    }

    @Override
    public Optional<ValeurVue> label(String code) {
        return valeur("label", code);
    }

    @Override
    public List<ValeurVue> labelsActifs() {
        return actives("label");
    }

    @Override
    public Optional<ValeurVue> typeVisite(String code) {
        return valeur("type_visite", code);
    }

    @Override
    public List<ValeurVue> typesVisiteActifs() {
        return actives("type_visite");
    }

    @Override
    public Optional<ValeurVue> langue(String code) {
        return valeur("langue", code);
    }

    @Override
    public List<ValeurVue> languesActives() {
        return actives("langue");
    }

    @Override
    public Optional<ValeurVue> centreInteret(String code) {
        return valeur("centre_interet", code);
    }

    @Override
    public List<ValeurVue> centresInteretActifs() {
        return actives("centre_interet");
    }

    // Présentation et commercialisation

    @Override
    public Optional<ValeurVue> marque(String code) {
        return valeur("marque", code);
    }

    @Override
    public List<ValeurVue> marquesActives() {
        return actives("marque");
    }

    @Override
    public Optional<ValeurVue> circuitDistribution(String code) {
        return valeur("circuit_distribution", code);
    }

    @Override
    public List<ValeurVue> circuitsDistributionActifs() {
        return actives("circuit_distribution");
    }

    @Override
    public Optional<ValeurVue> produitCommercialise(String code) {
        return valeur("produit_commercialise", code);
    }

    @Override
    public List<ValeurVue> produitsCommercialisesActifs() {
        return actives("produit_commercialise");
    }

    @Override
    public Optional<ContenanceVue> contenance(BigDecimal volume) {
        return jdbcClient.sql("select volume, libelle, actif from " + S + "contenance where volume = :volume")
                .param("volume", volume)
                .query(JdbcReferentielsLecture::contenance)
                .optional();
    }

    @Override
    public List<ContenanceVue> contenancesActives() {
        return jdbcClient.sql("select volume, libelle, actif from " + S + "contenance where actif order by volume")
                .query(JdbcReferentielsLecture::contenance)
                .list();
    }

    // Fiscalité

    @Override
    public Optional<ValeurVue> classeTva(String code) {
        return valeur("classe_tva", code);
    }

    @Override
    public List<ValeurVue> classesTvaActives() {
        return actives("classe_tva");
    }

    @Override
    public Optional<TauxTvaVue> tauxTva(String classeTva, LocalDate date) {
        return jdbcClient.sql("select classe_tva_code, taux, date_debut, date_fin from " + S + "taux_tva"
                        + " where classe_tva_code = :classe and date_debut <= :date"
                        + " and (date_fin is null or date_fin >= :date)")
                .param("classe", classeTva)
                .param("date", date)
                .query((rs, i) -> new TauxTvaVue(rs.getString("classe_tva_code"), rs.getBigDecimal("taux"),
                        rs.getObject("date_debut", LocalDate.class),
                        Optional.ofNullable(rs.getObject("date_fin", LocalDate.class))))
                .optional();
    }

    // Listes simples : même forme de table (code, libelle, actif)

    /** @param table nom de table du schéma, jamais une saisie utilisateur */
    private Optional<ValeurVue> valeur(String table, String code) {
        return jdbcClient.sql("select code, libelle, actif from " + S + table + " where code = :code")
                .param("code", code)
                .query(ValeurVue.class)
                .optional();
    }

    private List<ValeurVue> actives(String table) {
        return jdbcClient.sql("select code, libelle, actif from " + S + table + " where actif order by libelle, code")
                .query(ValeurVue.class)
                .list();
    }

    private static PaysVue pays(ResultSet rs, int ligne) throws SQLException {
        return new PaysVue(rs.getString("code"), rs.getString("code_iso"), rs.getString("libelle"),
                rs.getString("zone_commerciale"), rs.getBoolean("membre_ue"),
                LangueCertificat.valueOf(rs.getString("langue_certificat")),
                NiveauDetailCertificat.valueOf(rs.getString("niveau_detail_certificat")),
                Optional.ofNullable(rs.getString("age_minimum")).map(CompteDAge::de),
                rs.getBoolean("actif"));
    }

    private static ContenanceVue contenance(ResultSet rs, int ligne) throws SQLException {
        BigDecimal volume = rs.getBigDecimal("volume");
        String libelle = rs.getString("libelle");
        return new ContenanceVue(volume, libelle != null ? libelle : Volume.de(volume).libelle(), rs.getBoolean("actif"));
    }
}
