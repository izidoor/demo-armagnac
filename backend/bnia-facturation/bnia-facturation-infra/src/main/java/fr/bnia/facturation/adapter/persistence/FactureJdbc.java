package fr.bnia.facturation.adapter.persistence;

import static fr.bnia.facturation.adapter.persistence.Lignes.SCHEMA;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.facturation.adapter.persistence.Lignes.AdresseLigne;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.application.FactureRepository;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.facturation.domain.Auteur;
import fr.bnia.facturation.domain.CategorieAcheteur;
import fr.bnia.facturation.domain.EtatPiece;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.LigneFacture;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import fr.bnia.facturation.domain.LigneFacture.LigneInformative;
import fr.bnia.facturation.domain.SensPiece;
import fr.bnia.facturation.domain.Totaux;
import fr.bnia.facturation.domain.Vendeur;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * La pièce et ses lignes forment un agrégat Spring Data JDBC (A-FAC-4) : les lignes, groupes dépliés, en une
 * table, rangées par leur position ; une ligne d'un groupe désigne la position de sa ligne de groupe. Acheteur,
 * vendeur et origine en colonnes préfixées. Les totaux du domaine sont stockés pour l'historique.
 */
@Repository
class FactureJdbc implements FactureRepository {

    private static final String PDF = "application/pdf";

    private final JdbcAggregateTemplate template;
    private final JdbcClient jdbcClient;

    FactureJdbc(JdbcAggregateTemplate template, JdbcClient jdbcClient) {
        this.template = template;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Facture inserer(Facture facture) {
        try {
            return versDomaine(template.insert(versLigne(facture)));
        } catch (DuplicateKeyException doublon) {
            // RG-FAC-02 sous concurrence : l'index unique de l'origine vivante a tranché.
            throw new ConflitUnicite("RG-FAC-02", facture.origine().reference() + " a déjà une facture.");
        }
    }

    @Override
    public Facture mettreAJour(Facture facture) {
        try {
            return versDomaine(template.update(versLigne(facture)));
        } catch (OptimisticLockingFailureException concurrente) {
            throw new ModificationConcurrente(null, facture.designation() + " a été modifiée entre-temps par quelqu'un"
                    + " d'autre : rechargez l'écran, puis recommencez.");
        }
    }

    @Override
    public Optional<Facture> parId(long id) {
        return Optional.ofNullable(template.findById(id, Ligne.class)).map(FactureJdbc::versDomaine);
    }

    @Override
    public void supprimer(long id) {
        // Le journal suit par la clé étrangère (on delete cascade), les lignes avec l'agrégat (RG-FAC-18).
        template.deleteById(id, Ligne.class);
    }

    @Override
    public boolean origineVivante(String source, String identifiant) {
        return jdbcClient.sql("select exists (select 1 from " + SCHEMA + ".facture where sens = 'FACTURE'"
                        + " and origine_source = :source and origine_identifiant = :identifiant)")
                .param("source", source)
                .param("identifiant", identifiant)
                .query(Boolean.class)
                .single();
    }

    private static Ligne versLigne(Facture facture) {
        Facture.Donnees d = facture.donnees();
        Totaux totaux = facture.totaux();
        Origine o = d.origine();
        DocumentDepose pdf = d.pdf();
        return new Ligne(d.id(), d.sens(), d.etat(),
                o == null ? null : new OrigineLigne(o.source(), o.nature(), o.libelleNature(), o.identifiant(),
                        o.reference()),
                d.dateOperation(), d.libellePeriode(), d.condition().jours(), d.condition().finDeMois(),
                d.bonDeCommande(), d.noteEntete(), d.notePied(), AcheteurLigne.de(d.acheteur()),
                d.vendeur() == null ? null : VendeurLigne.de(d.vendeur()), d.numero(), d.dateEmission(), d.echeance(),
                pdf == null ? null : pdf.cle(), pdf == null ? null : pdf.nom(), pdf == null ? null : pdf.taille(),
                totaux.ht(), totaux.tva(), totaux.ttc(), d.creeeLe(), d.creePar().utilisateur(), d.creePar().nom(),
                aplatir(d.lignes()), d.id() == null ? null : d.version());
    }

    /** A-FAC-4 : les lignes dans l'ordre d'impression ; celles d'un groupe suivent sa ligne et désignent sa position. */
    private static List<LigneFactureLigne> aplatir(List<LigneFacture> lignes) {
        List<LigneFactureLigne> plates = new ArrayList<>();
        for (LigneFacture ligne : lignes) {
            if (ligne instanceof LigneGroupe g) {
                int position = plates.size();
                plates.add(LigneFactureLigne.texte("GROUPE", null, g.titre(), g.numero(), g.recue()));
                g.lignes().forEach(l -> plates.add(LigneFactureLigne.de(l, position)));
            } else {
                plates.add(LigneFactureLigne.de(ligne, null));
            }
        }
        return plates;
    }

    private static Facture versDomaine(Ligne l) {
        OrigineLigne o = l.origine();
        return Facture.reconstituer(new Facture.Donnees(l.id(), l.sens(), l.etat(),
                o == null ? null : new Origine(o.source(), o.nature(), o.libelleNature(), o.identifiant(), o.reference()),
                l.dateOperation(), l.libellePeriode(), new ConditionPaiement(l.conditionJours(), l.conditionFinDeMois()),
                l.bonDeCommande(), l.noteEntete(), l.notePied(), l.acheteur().versDomaine(),
                vendeur(l.vendeur()), regrouper(l.lignes()), l.numero(),
                l.dateEmission(), l.echeance(),
                l.pdfDocumentId() == null ? null : new DocumentDepose(l.pdfDocumentId(), l.pdfNom(), PDF, l.pdfTaille()),
                l.creeeLe(), new Auteur(l.creeeParId(), l.nomCreeePar()), l.version()));
    }

    /** Le vendeur recopié à l'émission ; aucun en brouillon (colonnes vides). */
    private static Vendeur vendeur(VendeurLigne ligne) {
        return ligne == null || ligne.nom() == null ? null : ligne.versDomaine();
    }

    /** Les lignes relues, regroupées sous leur ligne de groupe. */
    private static List<LigneFacture> regrouper(List<LigneFactureLigne> plates) {
        Map<Integer, Object> parPosition = new LinkedHashMap<>();
        Map<Integer, List<LigneFacture>> enfants = new LinkedHashMap<>();
        for (int position = 0; position < plates.size(); position++) {
            LigneFactureLigne p = plates.get(position);
            if (p.groupe() != null) {
                enfants.computeIfAbsent(p.groupe(), g -> new ArrayList<>()).add(p.versDomaine());
            } else {
                parPosition.put(position, p.sorte().equals("GROUPE") ? p : p.versDomaine());
            }
        }
        List<LigneFacture> lignes = new ArrayList<>();
        parPosition.forEach((position, ligne) -> lignes.add(ligne instanceof LigneFactureLigne g
                ? new LigneGroupe(g.texte(), enfants.getOrDefault(position, List.of()), g.numero(), g.recue())
                : (LigneFacture) ligne));
        return lignes;
    }

    @Table(schema = SCHEMA, name = "facture")
    record Ligne(
            @Id Long id,
            SensPiece sens,
            EtatPiece etat,
            @Embedded.Nullable(prefix = "origine_") OrigineLigne origine,
            LocalDate dateOperation,
            String libellePeriode,
            int conditionJours,
            boolean conditionFinDeMois,
            String bonDeCommande,
            String noteEntete,
            String notePied,
            @Embedded.Empty(prefix = "acheteur_") AcheteurLigne acheteur,
            @Embedded.Nullable(prefix = "vendeur_") VendeurLigne vendeur,
            String numero,
            LocalDate dateEmission,
            LocalDate echeance,
            Long pdfDocumentId,
            String pdfNom,
            Long pdfTaille,
            BigDecimal totalHt,
            BigDecimal totalTva,
            BigDecimal totalTtc,
            Instant creeeLe,
            long creeeParId,
            String nomCreeePar,
            @MappedCollection(idColumn = "facture_id", keyColumn = "position") List<LigneFactureLigne> lignes,
            @Version Long version) {
    }

    record OrigineLigne(String source, String nature, String libelleNature, String identifiant, String reference) {
    }

    record ClientLigne(String identifiant, String code) {
    }

    record AcheteurLigne(@Embedded.Nullable(prefix = "client_") ClientLigne client, CategorieAcheteur categorie,
            String nom, String siren, String siret, String numeroTva,
            @Embedded.Empty(prefix = "facturation_") AdresseLigne facturation,
            @Embedded.Nullable(prefix = "livraison_") AdresseLigne livraison, String email) {

        static AcheteurLigne de(Acheteur a) {
            return new AcheteurLigne(a.client() == null ? null : new ClientLigne(a.client().identifiant(),
                    a.client().code()), a.categorie(), a.nom(), a.siren(), a.siret(), a.numeroTva(),
                    AdresseLigne.de(a.adresseFacturation()), AdresseLigne.de(a.adresseLivraison()), a.email());
        }

        Acheteur versDomaine() {
            return new Acheteur(client == null ? null : new ClientRef(client.identifiant(), client.code()), categorie,
                    nom, siren, siret, numeroTva, facturation.versDomaine(), Lignes.adresse(livraison), email);
        }
    }

    record VendeurLigne(String nom, @Embedded.Nullable(prefix = "siege_") AdresseLigne siege,
            @Embedded.Nullable(prefix = "facturation_") AdresseLigne facturation, String siren, String siret,
            String numeroTva, String iban, String bic, String telephone, String email, Boolean tvaSurDebits,
            String escompte, String penalites) {

        static VendeurLigne de(Vendeur v) {
            return new VendeurLigne(v.nom(), AdresseLigne.de(v.siege()), AdresseLigne.de(v.adresseFacturation()),
                    v.siren(), v.siret(), v.numeroTva(), v.iban(), v.bic(), v.telephone(), v.email(), v.tvaSurDebits(),
                    v.escompte(), v.penalites());
        }

        Vendeur versDomaine() {
            return new Vendeur(nom, siege.versDomaine(), Lignes.adresse(facturation), siren, siret, numeroTva, iban,
                    bic, telephone, email, Boolean.TRUE.equals(tvaSurDebits), escompte, penalites);
        }
    }

    /** Une ligne de la pièce, quelle que soit sa sorte (A-FAC-4) ; colonnes de l'article vides hors article. */
    @Table(schema = SCHEMA, name = "ligne_facture")
    record LigneFactureLigne(int numero, boolean recue, String sorte, Integer groupe, String texte,
            String articleSource, String articleCode, String unite, NatureArticle nature, String codeComptable,
            BigDecimal quantite, BigDecimal prixUnitaire, CategorieTva.Code tvaCode, BigDecimal tvaTaux,
            String tvaMotif, BigDecimal montantHt) {

        static LigneFactureLigne texte(String sorte, Integer groupe, String texte, int numero, boolean recue) {
            return new LigneFactureLigne(numero, recue, sorte, groupe, texte, null, null, null, null, null, null, null,
                    null, null, null, null);
        }

        static LigneFactureLigne de(LigneFacture ligne, Integer groupe) {
            return switch (ligne) {
                case LigneArticle a -> new LigneFactureLigne(a.numero(), a.recue(), "ARTICLE", groupe, a.designation(),
                        a.article().source(), a.article().code(), a.unite(), a.nature(), a.codeComptable(),
                        a.quantite(), a.prixUnitaire(), a.tva().code(), a.tva().taux(), a.tva().motif(),
                        a.montantHt());
                case LigneInformative i -> texte("TEXTE", groupe, i.texte(), i.numero(), i.recue());
                case LigneGroupe g -> throw new IllegalArgumentException("Un groupe ne se range pas dans un groupe.");
            };
        }

        LigneFacture versDomaine() {
            return switch (sorte) {
                case "ARTICLE" -> new LigneArticle(new ArticleRef(articleSource, articleCode), texte, unite, nature,
                        codeComptable, quantite, prixUnitaire, new CategorieTva(tvaCode, tvaTaux, tvaMotif), numero,
                        recue);
                case "TEXTE" -> new LigneInformative(texte, numero, recue);
                default -> throw new IllegalStateException("Ligne de groupe inattendue à cette place.");
            };
        }
    }
}
