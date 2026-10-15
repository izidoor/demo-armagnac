package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.operateurs.application.GererInvitations;
import fr.bnia.operateurs.application.Habilitations;
import fr.bnia.operateurs.application.OperateurRepository;
import fr.bnia.operateurs.application.UtilisateurRepository;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.Adresse;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentiteOperateur;
import fr.bnia.operateurs.domain.NumeroTva;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Siret;
import fr.bnia.operateurs.domain.Textes;
import fr.bnia.operateurs.domain.partage.AcheteurVue;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.AdresseVue;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.PersonneVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import fr.bnia.operateurs.domain.partage.Role;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Partie publiée en lecture (ADR-012, amendé par ADR-016). Les opérateurs sont trouvés en SQL, puis
 * chargés comme agrégats et traduits en vues ; les droits et l'état de connexion viennent du cas d'usage
 * {@link Habilitations}, le seul à les calculer.
 */
@Repository
class JdbcOperateursLecture implements OperateursLecture {

    /** RG-OPE-37. */
    static final int LONGUEUR_MINIMALE = 3;
    static final int RESULTATS_MAXIMUM = 20;

    private static final String S = OperateurJdbc.SCHEMA + ".";
    private static final String TABLE = S + "operateur";

    private final JdbcClient jdbcClient;
    private final OperateurRepository repository;
    private final UtilisateurRepository utilisateurs;
    private final Habilitations habilitations;
    private final GererInvitations invitations;

    JdbcOperateursLecture(JdbcClient jdbcClient, OperateurRepository repository, UtilisateurRepository utilisateurs,
            Habilitations habilitations, GererInvitations invitations) {
        this.jdbcClient = jdbcClient;
        this.repository = repository;
        this.utilisateurs = utilisateurs;
        this.habilitations = habilitations;
        this.invitations = invitations;
    }

    @Override
    public Optional<OperateurVue> operateur(long id) {
        return repository.parId(id).map(this::vue);
    }

    @Override
    public Optional<AcheteurVue> acheteur(long id) {
        return repository.parId(id).map(o -> {
            IdentiteOperateur i = o.identite();
            Optional<String> email = emailFacturation(o);
            return new AcheteurVue(o.id(), o.code().valeur(), i.raisonSociale(),
                    Optional.ofNullable(i.siret()).map(Siret::valeur),
                    Optional.ofNullable(i.numeroTva()).map(NumeroTva::valeur), adresse(i.adresse()), email,
                    o.manquePourFacturer(email.isPresent()), o.estActif());
        });
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
        return vues("actif and type <> 'BNIA' and statut_declaration_stock = 'OUI'", Map.of());
    }

    @Override
    public List<ResultatRecherche> rechercher(String texte) {
        String recherche = Textes.facultatif(texte);
        if (recherche == null || recherche.length() < LONGUEUR_MINIMALE) {
            return List.of();
        }
        return jdbcClient.sql("select id, code, raison_sociale, nom_commercial, ville from " + TABLE
                        + " where actif and type <> 'BNIA' and (code ilike :motif escape '\\'"
                        + " or raison_sociale ilike :motif escape '\\' or nom_commercial ilike :motif escape '\\')"
                        + " order by raison_sociale, code limit :limite")
                .param("motif", Sql.motifContient(recherche))
                .param("limite", RESULTATS_MAXIMUM)
                .query((rs, i) -> new ResultatRecherche(rs.getLong("id"), rs.getString("code"),
                        rs.getString("raison_sociale"), Optional.ofNullable(rs.getString("nom_commercial")),
                        Optional.ofNullable(rs.getString("ville"))))
                .list();
    }

    @Override
    public List<ResultatRecherche> identites(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("select id, code, raison_sociale, nom_commercial, ville from " + TABLE
                        + " where id in (:ids)")
                .param("ids", List.copyOf(ids))
                .query((rs, i) -> new ResultatRecherche(rs.getLong("id"), rs.getString("code"),
                        rs.getString("raison_sociale"), Optional.ofNullable(rs.getString("nom_commercial")),
                        Optional.ofNullable(rs.getString("ville"))))
                .list();
    }

    @Override
    public Set<Long> facturables(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(jdbcClient.sql("select o.id from " + TABLE + " o where o.id in (:ids) and " + Sql.FACTURABLE)
                .param("ids", List.copyOf(ids))
                .query(Long.class)
                .list());
    }

    @Override
    public long operateurBnia() {
        return repository.idBnia();
    }

    @Override
    public Droits droits(long utilisateur, long operateur) {
        return habilitations.droits(new Acteur(utilisateur), operateur);
    }

    @Override
    public Optional<String> nomUtilisateur(long utilisateur) {
        return utilisateurs.parId(utilisateur).map(u -> u.identite().nomComplet());
    }

    @Override
    public List<PersonneVue> personnes(long operateur) {
        Map<Long, Set<Long>> centres = new HashMap<>();
        jdbcClient.sql("select c.fiche_id, c.centre_interet_id from " + S + "fiche_centre_interet c join " + S
                        + "fiche_professionnelle f on f.id = c.fiche_id where f.operateur_id = :operateur")
                .param("operateur", operateur)
                .query(rs -> {
                    centres.computeIfAbsent(rs.getLong("fiche_id"), f -> new HashSet<>()).add(rs.getLong("centre_interet_id"));
                });
        return jdbcClient.sql("""
                        select c.fiche_id, c.nom, c.prenom, c.email, f.fonction, null as role
                          from %1$scontact c join %1$sfiche_professionnelle f on f.id = c.fiche_id
                         where c.operateur_id = :operateur
                        union all
                        select r.fiche_id, u.nom, u.prenom, u.email, f.fonction, r.role
                          from %1$srattachement r join %1$sutilisateur u on u.id = r.utilisateur_id
                          join %1$sfiche_professionnelle f on f.id = r.fiche_id
                         where r.operateur_id = :operateur
                         order by nom, prenom nulls first
                        """.formatted(S))
                .param("operateur", operateur)
                .query((rs, i) -> personne(operateur, rs, centres))
                .list();
    }

    @Override
    public EtatConnexion etatConnexion(long utilisateur) {
        return habilitations.etatConnexion(utilisateur);
    }

    @Override
    public Optional<Long> utilisateurParEmail(String email) {
        String adresse = Textes.facultatif(email);
        return adresse == null ? Optional.empty() : utilisateurs.parEmail(new Email(adresse)).map(u -> u.id());
    }

    @Override
    public Optional<Long> utilisateurDInvitationAcceptee(String jeton) {
        return invitations.utilisateurDInvitationAcceptee(jeton);
    }

    private static PersonneVue personne(long operateur, ResultSet rs, Map<Long, Set<Long>> centres) throws SQLException {
        long fiche = rs.getLong("fiche_id");
        String prenom = rs.getString("prenom");
        String nom = rs.getString("nom");
        String role = rs.getString("role");
        return new PersonneVue(operateur, fiche, prenom == null ? nom : prenom + " " + nom,
                Optional.ofNullable(rs.getString("fonction")), Optional.ofNullable(rs.getString("email")),
                Set.copyOf(centres.getOrDefault(fiche, Set.of())), Optional.ofNullable(role).map(Role::valueOf));
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
                .map(this::vue)
                .toList();
    }

    private static String compact(String texte) {
        String valeur = Textes.facultatif(texte);
        return valeur == null ? null : valeur.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private OperateurVue vue(Operateur o) {
        IdentiteOperateur i = o.identite();
        return new OperateurVue(o.id(), o.code().valeur(), o.type(), i.raisonSociale(),
                Optional.ofNullable(i.nomCommercial()), adresse(i.adresse()),
                Optional.ofNullable(o.declaratif().periodicite()), o.declaratif().statut(),
                Optional.ofNullable(o.production().region()), o.production().appellations(),
                o.diffusion().niveau(), o.diffusion().millesimes(), o.accesStatistiques(), emailFacturation(o),
                o.estActif());
    }

    /** RG-OPE-23 : l'adresse saisie, ou l'e-mail de la personne désignée. */
    private Optional<String> emailFacturation(Operateur o) {
        return o.destinataireFactures().flatMap(d -> d.adresse() != null
                ? Optional.of(d.adresse().valeur()) : emailDeFiche(d.fiche()));
    }

    private static AdresseVue adresse(Adresse a) {
        return new AdresseVue(Optional.ofNullable(a.ligne1()), Optional.ofNullable(a.ligne2()),
                Optional.ofNullable(a.ligne3()), Optional.ofNullable(a.codePostal()), Optional.ofNullable(a.ville()),
                Optional.ofNullable(a.pays()));
    }

    private Optional<String> emailDeFiche(long fiche) {
        return jdbcClient.sql("select coalesce(c.email, u.email) from " + S + "fiche_professionnelle f"
                        + " left join " + S + "contact c on c.fiche_id = f.id"
                        + " left join " + S + "rattachement r on r.fiche_id = f.id"
                        + " left join " + S + "utilisateur u on u.id = r.utilisateur_id where f.id = :fiche")
                .param("fiche", fiche)
                .query(String.class)
                .optional();
    }
}
