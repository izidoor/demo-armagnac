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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Partie publiée en lecture (ADR-008), en SQL direct (ADR-004).
 */
@Repository
class JdbcReferentielsLecture implements ReferentielsLecture {

    private static final String S = ValeursJdbc.SCHEMA + ".";
    private static final String VALEUR = "id, code, libelle, actif";
    private static final String PAYS = "id, code, code_iso, libelle, zone_commerciale_id, membre_ue,"
            + " langue_certificat, niveau_detail_certificat, age_minimum, actif";
    private static final String REGION = "id, code, libelle, en_region_delimitee, actif";
    private static final String CONTENANCE = "id, volume, libelle, actif";

    private final JdbcClient jdbcClient;

    JdbcReferentielsLecture(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    // Géographie

    @Override
    public Optional<ValeurVue> zoneCommerciale(long id) {
        return parId("zone_commerciale", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> zoneCommercialeParCode(String code) {
        return parCode("zone_commerciale", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> zonesCommercialesActives() {
        return actives("zone_commerciale", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<PaysVue> pays(long id) {
        return parId("pays", PAYS, id, JdbcReferentielsLecture::pays);
    }

    @Override
    public Optional<PaysVue> paysParCode(String code) {
        return parCode("pays", PAYS, code, JdbcReferentielsLecture::pays);
    }

    @Override
    public List<PaysVue> paysActifs() {
        return actives("pays", PAYS, "libelle, code", JdbcReferentielsLecture::pays);
    }

    @Override
    public Optional<RegionVue> region(long id) {
        return parId("region", REGION, id, JdbcReferentielsLecture::region);
    }

    @Override
    public Optional<RegionVue> regionParCode(String code) {
        return parCode("region", REGION, code, JdbcReferentielsLecture::region);
    }

    @Override
    public List<RegionVue> regionsActives() {
        return actives("region", REGION, "libelle, code", JdbcReferentielsLecture::region);
    }

    // Qualification

    @Override
    public Optional<ValeurVue> titre(long id) {
        return parId("titre", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> titreParCode(String code) {
        return parCode("titre", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> titresActifs() {
        return actives("titre", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> label(long id) {
        return parId("label", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> labelParCode(String code) {
        return parCode("label", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> labelsActifs() {
        return actives("label", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> typeVisite(long id) {
        return parId("type_visite", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> typeVisiteParCode(String code) {
        return parCode("type_visite", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> typesVisiteActifs() {
        return actives("type_visite", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> langue(long id) {
        return parId("langue", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> langueParCode(String code) {
        return parCode("langue", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> languesActives() {
        return actives("langue", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> centreInteret(long id) {
        return parId("centre_interet", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> centreInteretParCode(String code) {
        return parCode("centre_interet", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> centresInteretActifs() {
        return actives("centre_interet", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> membreBnia(long id) {
        return parId("membre_bnia", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> membreBniaParCode(String code) {
        return parCode("membre_bnia", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> membresBniaActifs() {
        return actives("membre_bnia", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    // Présentation et commercialisation

    @Override
    public Optional<ValeurVue> marque(long id) {
        return parId("marque", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> marqueParCode(String code) {
        return parCode("marque", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> marquesActives() {
        return actives("marque", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> circuitDistribution(long id) {
        return parId("circuit_distribution", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> circuitDistributionParCode(String code) {
        return parCode("circuit_distribution", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> circuitsDistributionActifs() {
        return actives("circuit_distribution", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> produitCommercialise(long id) {
        return parId("produit_commercialise", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> produitCommercialiseParCode(String code) {
        return parCode("produit_commercialise", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> produitsCommercialisesActifs() {
        return actives("produit_commercialise", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ContenanceVue> contenance(long id) {
        return parId("contenance", CONTENANCE, id, JdbcReferentielsLecture::contenance);
    }

    @Override
    public Optional<ContenanceVue> contenanceParVolume(BigDecimal volume) {
        return jdbcClient.sql("select " + CONTENANCE + " from " + S + "contenance where volume = :volume")
                .param("volume", volume)
                .query(JdbcReferentielsLecture::contenance)
                .optional();
    }

    @Override
    public List<ContenanceVue> contenancesActives() {
        return actives("contenance", CONTENANCE, "volume", JdbcReferentielsLecture::contenance);
    }

    // Fiscalité

    @Override
    public Optional<ValeurVue> classeTva(long id) {
        return parId("classe_tva", VALEUR, id, JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<ValeurVue> classeTvaParCode(String code) {
        return parCode("classe_tva", VALEUR, code, JdbcReferentielsLecture::valeur);
    }

    @Override
    public List<ValeurVue> classesTvaActives() {
        return actives("classe_tva", VALEUR, "libelle, code", JdbcReferentielsLecture::valeur);
    }

    @Override
    public Optional<TauxTvaVue> tauxTva(long classeTva, LocalDate date) {
        return jdbcClient.sql("select classe_tva_id, taux, date_debut, date_fin from " + S + "taux_tva"
                        + " where classe_tva_id = :classe and date_debut <= :date"
                        + " and (date_fin is null or date_fin >= :date)")
                .param("classe", classeTva)
                .param("date", date)
                .query((rs, i) -> new TauxTvaVue(rs.getLong("classe_tva_id"), rs.getBigDecimal("taux"),
                        rs.getObject("date_debut", LocalDate.class),
                        Optional.ofNullable(rs.getObject("date_fin", LocalDate.class))))
                .optional();
    }

    // Requêtes communes ; tables et colonnes sont des constantes, jamais une saisie utilisateur.

    private <V> Optional<V> parId(String table, String colonnes, long id, RowMapper<V> mapper) {
        return jdbcClient.sql("select " + colonnes + " from " + S + table + " where id = :id")
                .param("id", id)
                .query(mapper)
                .optional();
    }

    private <V> Optional<V> parCode(String table, String colonnes, String code, RowMapper<V> mapper) {
        return jdbcClient.sql("select " + colonnes + " from " + S + table + " where lower(code) = lower(:code)")
                .param("code", code)
                .query(mapper)
                .optional();
    }

    private <V> List<V> actives(String table, String colonnes, String ordre, RowMapper<V> mapper) {
        return jdbcClient.sql("select " + colonnes + " from " + S + table + " where actif order by " + ordre)
                .query(mapper)
                .list();
    }

    private static ValeurVue valeur(ResultSet rs, int ligne) throws SQLException {
        return new ValeurVue(rs.getLong("id"), rs.getString("code"), rs.getString("libelle"), rs.getBoolean("actif"));
    }

    private static PaysVue pays(ResultSet rs, int ligne) throws SQLException {
        return new PaysVue(rs.getLong("id"), rs.getString("code"), rs.getString("code_iso"), rs.getString("libelle"),
                rs.getLong("zone_commerciale_id"), rs.getBoolean("membre_ue"),
                LangueCertificat.valueOf(rs.getString("langue_certificat")),
                NiveauDetailCertificat.valueOf(rs.getString("niveau_detail_certificat")),
                Optional.ofNullable(rs.getString("age_minimum")).map(CompteDAge::de),
                rs.getBoolean("actif"));
    }

    private static RegionVue region(ResultSet rs, int ligne) throws SQLException {
        return new RegionVue(rs.getLong("id"), rs.getString("code"), rs.getString("libelle"),
                rs.getBoolean("en_region_delimitee"), rs.getBoolean("actif"));
    }

    private static ContenanceVue contenance(ResultSet rs, int ligne) throws SQLException {
        BigDecimal volume = rs.getBigDecimal("volume");
        String libelle = rs.getString("libelle");
        return new ContenanceVue(rs.getLong("id"), volume, libelle != null ? libelle : Volume.de(volume).libelle(),
                rs.getBoolean("actif"));
    }
}
