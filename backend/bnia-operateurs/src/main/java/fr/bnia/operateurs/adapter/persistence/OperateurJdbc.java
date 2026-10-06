package fr.bnia.operateurs.adapter.persistence;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.ElementUtilise;
import fr.bnia.commun.domain.Millesime;
import fr.bnia.operateurs.application.OperateurRepository;
import fr.bnia.operateurs.domain.Adresse;
import fr.bnia.operateurs.domain.Alambic;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.CoordonneesGps;
import fr.bnia.operateurs.domain.Cvi;
import fr.bnia.operateurs.domain.Declaratif;
import fr.bnia.operateurs.domain.DiffusionMillesimes;
import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.IdentiteOperateur;
import fr.bnia.operateurs.domain.NiveauDiffusion;
import fr.bnia.operateurs.domain.NumeroAccise;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.Production;
import fr.bnia.operateurs.domain.Siret;
import fr.bnia.operateurs.domain.SiteWeb;
import fr.bnia.operateurs.domain.TypeAlambic;
import fr.bnia.operateurs.domain.partage.PeriodiciteDsm;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Agrégat {@code Operateur} en Spring Data JDBC (ADR-004, ADR-012) : appellations, alambics et
 * millésimes diffusables sont réécrits à chaque enregistrement. Les formes normalisées de la raison
 * sociale et du groupe sont stockées pour les comparaisons en SQL (RG-OPE-08, 09).
 */
@Repository
class OperateurJdbc implements OperateurRepository {

    static final String SCHEMA = "operateurs";

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    OperateurJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Operateur> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(OperateurJdbc::versDomaine);
    }

    @Override
    public Operateur inserer(Operateur operateur) {
        return versDomaine(template.insert(versLigne(operateur)));
    }

    @Override
    public void mettreAJour(Operateur operateur) {
        template.update(versLigne(operateur));
    }

    @Override
    public void supprimer(long id) {
        try {
            template.deleteById(id, Ligne.class);
        } catch (DataIntegrityViolationException utilise) {
            throw new ElementUtilise("RG-OPE-25",
                    "Cet opérateur est utilisé par d'autres données : désactivez-le plutôt que de le supprimer.");
        }
    }

    @Override
    public boolean existeCode(CodeOperateur code, Long sauf) {
        return existe("code", code.valeur(), sauf);
    }

    @Override
    public boolean existeIdentifiantOdg(String identifiantOdg, Long sauf) {
        return existe("identifiant_odg", identifiantOdg, sauf);
    }

    /** @param colonne constante, jamais une saisie utilisateur */
    private boolean existe(String colonne, String valeur, Long sauf) {
        return jdbcClient.sql("select exists (select 1 from " + SCHEMA + ".operateur"
                        + " where lower(" + colonne + ") = lower(:valeur) and id is distinct from :sauf)")
                .param("valeur", valeur)
                .param("sauf", sauf, Types.BIGINT)
                .query(Boolean.class)
                .single();
    }

    private static Ligne versLigne(Operateur o) {
        IdentiteOperateur i = o.identite();
        Adresse a = i.adresse();
        Production p = o.production();
        return new Ligne(o.id(), o.type(), o.code().valeur(), i.groupe(), i.groupeNormalise(),
                texte(i.cvi(), Cvi::valeur), texte(i.siret(), Siret::valeur), texte(i.accise(), NumeroAccise::valeur),
                i.bureauDouanes(), i.identifiantOdg(), i.titre(), i.raisonSociale(), i.raisonSocialeNormalisee(),
                i.nomCommercial(), a.ligne1(), a.ligne2(), a.ligne3(), a.codePostal(), a.ville(), a.pays(),
                i.telephone1(), i.telephone2(), i.portable(), texte(i.emailGeneral(), Email::valeur),
                texte(i.siteWeb(), SiteWeb::valeur), texte(i.siteCommercial(), SiteWeb::valeur),
                i.gps() == null ? null : i.gps().latitude(), i.gps() == null ? null : i.gps().longitude(),
                i.membreBnia(), i.accordDiffusionGeneral(), i.commentaireInterne(),
                o.declaratif().periodicite(), o.declaratif().statut(), p.region(),
                p.appellations().stream().map(AppellationLigne::new).collect(Collectors.toSet()),
                p.alambics().stream().map(al -> new AlambicLigne(al.numeroAgrement(), al.type(), al.capacite())).toList(),
                o.diffusion().niveau(),
                o.diffusion().millesimes().stream().map(m -> new MillesimeLigne(m.annee())).collect(Collectors.toSet()),
                o.accesStatistiques(), o.estActif());
    }

    private static Operateur versDomaine(Ligne l) {
        IdentiteOperateur identite = new IdentiteOperateur(
                l.groupe(),
                l.cvi() == null ? null : new Cvi(l.cvi()),
                l.siret() == null ? null : new Siret(l.siret()),
                l.accise() == null ? null : new NumeroAccise(l.accise()),
                l.bureauDouanes(),
                l.identifiantOdg(),
                l.titreId(),
                l.raisonSociale(),
                l.nomCommercial(),
                new Adresse(l.adresseLigne1(), l.adresseLigne2(), l.adresseLigne3(), l.codePostal(), l.ville(), l.paysId()),
                l.telephone1(),
                l.telephone2(),
                l.portable(),
                l.emailGeneral() == null ? null : new Email(l.emailGeneral()),
                l.siteWeb() == null ? null : new SiteWeb(l.siteWeb()),
                l.siteCommercial() == null ? null : new SiteWeb(l.siteCommercial()),
                CoordonneesGps.facultatives(l.latitude(), l.longitude()),
                l.membreBniaId(),
                l.accordDiffusionGeneral(),
                l.commentaireInterne());
        Production production = new Production(l.regionId(),
                l.appellations().stream().map(AppellationLigne::appellation).collect(Collectors.toSet()),
                l.alambics().stream().map(a -> new Alambic(a.numeroAgrement(), a.type(), a.capacite())).toList());
        DiffusionMillesimes diffusion = new DiffusionMillesimes(l.niveauDiffusion(), l.millesimesDiffusables().stream()
                .map(m -> Millesime.de(m.millesime())).collect(Collectors.toCollection(TreeSet::new)));
        return Operateur.reconstituer(l.id(), l.type(), CodeOperateur.de(l.code()), identite,
                new Declaratif(l.periodiciteDsm(), l.statutDeclarationStock()), production, diffusion,
                l.accesStatistiques(), l.actif());
    }

    private static <T> String texte(T valeur, Function<T, String> lire) {
        return valeur == null ? null : lire.apply(valeur);
    }

    @Table(schema = SCHEMA, name = "operateur")
    record Ligne(
            @Id Long id,
            TypeOperateur type,
            String code,
            String groupe,
            String groupeNormalise,
            String cvi,
            String siret,
            String accise,
            String bureauDouanes,
            String identifiantOdg,
            Long titreId,
            String raisonSociale,
            String raisonSocialeNormalisee,
            String nomCommercial,
            String adresseLigne1,
            String adresseLigne2,
            String adresseLigne3,
            String codePostal,
            String ville,
            Long paysId,
            String telephone1,
            String telephone2,
            String portable,
            String emailGeneral,
            String siteWeb,
            String siteCommercial,
            BigDecimal latitude,
            BigDecimal longitude,
            Long membreBniaId,
            boolean accordDiffusionGeneral,
            String commentaireInterne,
            PeriodiciteDsm periodiciteDsm,
            StatutDeclarationStock statutDeclarationStock,
            Long regionId,
            @MappedCollection(idColumn = "operateur_id") Set<AppellationLigne> appellations,
            @MappedCollection(idColumn = "operateur_id", keyColumn = "rang") List<AlambicLigne> alambics,
            NiveauDiffusion niveauDiffusion,
            @MappedCollection(idColumn = "operateur_id") Set<MillesimeLigne> millesimesDiffusables,
            boolean accesStatistiques,
            boolean actif) {
    }

    @Table(schema = SCHEMA, name = "operateur_appellation")
    record AppellationLigne(Appellation appellation) {
    }

    @Table(schema = SCHEMA, name = "alambic")
    record AlambicLigne(String numeroAgrement, TypeAlambic type, BigDecimal capacite) {
    }

    @Table(schema = SCHEMA, name = "millesime_diffusable")
    record MillesimeLigne(int millesime) {
    }
}
