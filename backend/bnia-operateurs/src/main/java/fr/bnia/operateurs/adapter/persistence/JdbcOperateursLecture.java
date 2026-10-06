package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.operateurs.application.OperateurRepository;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.Adresse;
import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentiteOperateur;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Textes;
import fr.bnia.operateurs.domain.partage.AdresseVue;
import fr.bnia.operateurs.domain.partage.ContactVue;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Partie publiée en lecture (ADR-012). Les opérateurs sont trouvés en SQL, puis chargés comme agrégats
 * et traduits en vues : une seule traduction, celle du domaine.
 */
@Repository
class JdbcOperateursLecture implements OperateursLecture {

    /** RG-OPE-37. */
    static final int LONGUEUR_MINIMALE = 3;
    static final int RESULTATS_MAXIMUM = 20;

    private static final String TABLE = OperateurJdbc.SCHEMA + ".operateur";

    private final JdbcClient jdbcClient;
    private final OperateurRepository repository;

    JdbcOperateursLecture(JdbcClient jdbcClient, OperateurRepository repository) {
        this.jdbcClient = jdbcClient;
        this.repository = repository;
    }

    @Override
    public Optional<OperateurVue> operateur(long id) {
        return repository.parId(id).map(JdbcOperateursLecture::vue);
    }

    @Override
    public Optional<OperateurVue> operateurParCode(String code) {
        return premier("lower(code) = lower(:valeur)", Textes.facultatif(code));
    }

    @Override
    public Optional<OperateurVue> operateurParIdentifiantOdg(String identifiantOdg) {
        return premier("lower(identifiant_odg) = lower(:valeur)", Textes.facultatif(identifiantOdg));
    }

    @Override
    public List<OperateurVue> operateursParCvi(String cvi) {
        return vues("cvi = :valeur", compact(cvi));
    }

    @Override
    public List<OperateurVue> operateursParSiret(String siret) {
        return vues("siret = :valeur", compact(siret));
    }

    @Override
    public List<OperateurVue> declarantsAttendus() {
        return vues("actif and statut_declaration_stock = 'OUI'", Map.of());
    }

    @Override
    public List<ContactVue> contactsAvecAccesParEmail(String email) {
        String adresse = Textes.facultatif(email);
        if (adresse == null) {
            return List.of();
        }
        Email cle = new Email(adresse);
        return repository.avecEmailDeContact(cle).stream()
                .filter(Operateur::estActif)
                .flatMap(o -> o.contacts().stream()
                        .filter(c -> c.estActif() && c.droits().acces() && c.identite().email() != null
                                && c.identite().email().cle().equals(cle.cle()))
                        .map(c -> vue(o.id(), c)))
                .toList();
    }

    @Override
    public List<ContactVue> contacts(long operateur) {
        return repository.parId(operateur).map(o -> o.contacts().stream().map(c -> vue(o.id(), c)).toList())
                .orElse(List.of());
    }

    @Override
    public List<ContactVue> contactsParCentreInteret(long operateur, long centreInteret) {
        return contacts(operateur).stream().filter(c -> c.actif() && c.centresInteret().contains(centreInteret)).toList();
    }

    @Override
    public List<ResultatRecherche> rechercher(String texte) {
        String recherche = Textes.facultatif(texte);
        if (recherche == null || recherche.length() < LONGUEUR_MINIMALE) {
            return List.of();
        }
        return jdbcClient.sql("select id, code, raison_sociale, nom_commercial, ville from " + TABLE
                        + " where actif and (code ilike :motif escape '\\' or raison_sociale ilike :motif escape '\\'"
                        + " or nom_commercial ilike :motif escape '\\') order by raison_sociale, code limit :limite")
                .param("motif", Sql.motifContient(recherche))
                .param("limite", RESULTATS_MAXIMUM)
                .query((rs, i) -> new ResultatRecherche(rs.getLong("id"), rs.getString("code"),
                        rs.getString("raison_sociale"), Optional.ofNullable(rs.getString("nom_commercial")),
                        Optional.ofNullable(rs.getString("ville"))))
                .list();
    }

    /** @param condition constante, jamais une saisie utilisateur ; rien si la valeur est absente */
    private Optional<OperateurVue> premier(String condition, String valeur) {
        return vues(condition, valeur).stream().findFirst();
    }

    /** @param condition constante, jamais une saisie utilisateur ; rien si la valeur est absente */
    private List<OperateurVue> vues(String condition, String valeur) {
        return valeur == null ? List.of() : vues(condition, Map.of("valeur", valeur));
    }

    private List<OperateurVue> vues(String condition, Map<String, ?> parametres) {
        return jdbcClient.sql("select id from " + TABLE + " where " + condition + " order by code")
                .params(parametres)
                .query(Long.class)
                .list().stream()
                .map(repository::parId)
                .flatMap(Optional::stream)
                .map(JdbcOperateursLecture::vue)
                .toList();
    }

    private static String compact(String texte) {
        String valeur = Textes.facultatif(texte);
        return valeur == null ? null : valeur.replaceAll("\\s+", "").toUpperCase(java.util.Locale.ROOT);
    }

    static OperateurVue vue(Operateur o) {
        IdentiteOperateur i = o.identite();
        Adresse a = i.adresse();
        return new OperateurVue(o.id(), o.code().valeur(), o.type(), i.raisonSociale(),
                Optional.ofNullable(i.nomCommercial()),
                new AdresseVue(Optional.ofNullable(a.ligne1()), Optional.ofNullable(a.ligne2()),
                        Optional.ofNullable(a.ligne3()), Optional.ofNullable(a.codePostal()),
                        Optional.ofNullable(a.ville()), Optional.ofNullable(a.pays())),
                Optional.ofNullable(o.declaratif().periodicite()), o.declaratif().statut(),
                Optional.ofNullable(o.production().region()), o.production().appellations(),
                o.diffusion().niveau(), o.diffusion().millesimes(), o.accesStatistiques(),
                o.emailFacturation().map(Email::valeur), o.estActif());
    }

    private static ContactVue vue(long operateur, Contact c) {
        return new ContactVue(c.id(), operateur, c.identite().nomComplet(), Optional.ofNullable(c.identite().fonction()),
                Optional.ofNullable(c.identite().email()).map(Email::valeur), c.identite().centresInteret(),
                c.droits().acces(), c.droits().administrateur(), c.droits().modules(), c.estActif());
    }
}
