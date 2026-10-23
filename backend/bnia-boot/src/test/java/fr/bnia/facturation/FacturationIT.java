package fr.bnia.facturation;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.FichierDepose;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.application.GererPieces;
import fr.bnia.declarations.application.GererPointDeDepart;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.SaisieLigneStock;
import fr.bnia.declarations.domain.TypeMouvement;
import fr.bnia.declarations.domain.TypePiece;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.CatalogueArticles;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.DemandeFacture;
import fr.bnia.facturation.api.DemandeFactures;
import fr.bnia.facturation.api.FacturationLecture;
import fr.bnia.facturation.api.LigneVendue;
import fr.bnia.facturation.api.LigneDemandee.ArticleDemande;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.application.Clients;
import fr.bnia.facturation.application.EmettrePiece;
import fr.bnia.facturation.application.EnregistrerReglement;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.PaysVue;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import fr.bnia.support.AbstractIT;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Lots 1, 2, 3, 5 et 6 de la spec 08 : facturer une DSM, de bout en bout, puis travailler son brouillon. La DSM validée d'un négociant aux volumes de la facture
 * d'exemple, facturée depuis {@code declarations}, émise par {@code facturation}, puis « facturée » dans la DSM.
 * La grille CVO, la classe de TVA de la CVO et le vendeur sont communs à la base des IT : on les assure.
 */
@SuppressWarnings("unchecked")
class FacturationIT extends AbstractIT {

    private static final String PIECES = "/api/facturation/pieces";
    private static final String VENDEUR = "/api/facturation/vendeur";
    private static final String SIRET = "77699995500029";

    @Autowired ReferentielsLecture referentiels;
    @Autowired GererPointDeDepart points;
    @Autowired GererDsm dsms;
    @Autowired GererPieces pieces;
    @Autowired List<CatalogueArticles> catalogues;
    @Autowired Clients clients;
    @Autowired DemandeFactures demandeFactures;
    @Autowired EmettrePiece emettrePiece;
    @Autowired FacturationLecture facturationLecture;
    @Autowired EnregistrerReglement reglement;

    private fr.bnia.operateurs.domain.partage.Acteur admin;
    private long france;

    @BeforeEach
    void parametrage() {
        admin = new fr.bnia.operateurs.domain.partage.Acteur(operateurs.utilisateurParEmail(ADMIN_BNIA).orElseThrow());
        long normal = referentiels.classeTvaParCode("NORMAL").map(ValeurVue::id).orElseGet(() -> creer(
                "/api/referentiels/classes-tva", Map.of("code", "NORMAL", "libelle", "Normal")));
        if (referentiels.tauxTva(normal, LocalDate.now()).isEmpty()) {
            client.post().uri("/api/referentiels/classes-tva/{id}/taux", normal)
                    .body(Map.of("taux", new BigDecimal("20"), "dateDebut", "2014-01-01")).exchange()
                    .expectStatus().isOk();
        }
        List<?> grille = client.get().uri("/api/declarations/taux-cvo").exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();
        for (String debut : List.of("2021-01-01", "2024-01-01")) {
            if (grille.stream().noneMatch(t -> ((Map<?, ?>) t).get("debut").equals(debut))) {
                client.post().uri("/api/declarations/taux-cvo").body(Map.of("montant",
                        new BigDecimal(debut.startsWith("2021") ? "55.00" : "60.00"), "pourcentageExonere",
                        new BigDecimal("20.00"), "debut", debut)).exchange().expectStatus().isOk();
            }
        }
        france = referentiels.paysActifs().stream().filter(p -> p.codeIso().equals("FR")).map(PaysVue::id).findFirst()
                .orElseGet(() -> {
                    long zone = creer("/api/referentiels/zones-commerciales", Map.of("code", "Z" + unique(),
                            "libelle", "Europe"));
                    return creer("/api/referentiels/pays", Map.of("code", "FR-" + unique(), "libelle", "France",
                            "codeIso", "FR", "zoneCommerciale", zone, "membreUe", true));
                });
        client.put().uri(VENDEUR).body(vendeur()).exchange().expectStatus().isOk();
    }

    private static Map<String, Object> vendeur() {
        Map<String, Object> v = new HashMap<>();
        v.put("nom", "Bureau National Interprofessionnel de l'Armagnac");
        v.put("siege", Map.of("ligne1", "11 place de la Liberté", "codePostal", "32800", "ville", "Eauze",
                "pays", "France", "codePays", "FR"));
        v.put("siret", SIRET);
        v.put("numeroTva", "FR55776999955");
        v.put("iban", "FR7610278022000002025240143");
        v.put("bic", "CMCIFR2A");
        v.put("telephone", "05 62 08 11 00");
        v.put("email", "compta@armagnac.fr");
        v.put("tvaSurDebits", false);
        return v;
    }

    /** Un négociant, facturable s'il a un SIRET (RG-OPE-02) ; un destinataire des factures. */
    private long negociant(boolean facturable) {
        Map<String, Object> saisie = new HashMap<>();
        saisie.put("type", "NEGOCIANT");
        saisie.put("code", "T" + unique());
        saisie.put("raisonSociale", "Négoce " + unique());
        saisie.put("adresseLigne1", "12 rue des Chais");
        saisie.put("codePostal", "32100");
        saisie.put("ville", "Condom");
        saisie.put("pays", france);
        saisie.put("accordDiffusionGeneral", false);
        if (facturable) {
            saisie.put("siret", SIRET);
            saisie.put("numeroTva", "FR55776999955");
        }
        long id = creer("/api/operateurs", saisie);
        client.put().uri("/api/operateurs/{id}/designations", id)
                .body(Map.of("destinataireFacturesEmail", "compta-" + id + "@negoce.example"))
                .exchange().expectStatus().isOk();
        return id;
    }

    /**
     * La DSM du mois précédent de l'opérateur, validée (une DSM ne s'envoie qu'après la fin de sa période, spec 06
     * RG-CA-24), aux volumes de la facture d'exemple : sorties France 839,11 L (colonne 17), export 2 254,15 L
     * (colonne 22, avec son DAE).
     */
    private long dsmValidee(long operateur) {
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        var agent = new fr.bnia.operateurs.domain.partage.Acteur(operateurs.utilisateurParEmail(email).orElseThrow());
        points.fixer(admin, operateur, LocalDate.now().withDayOfMonth(1).minusMonths(1), List.of(new SaisieLigneStock(
                CleConditionnement.NB, null, "XO", "12", VolumeAlcoolPur.litres("4000"))));
        DeclarationStocksMouvements dsm = dsms.initier(agent, operateur).dsm();
        LigneDsm ligne = dsm.lignes().getFirst();
        dsms.enregistrer(agent, operateur, dsm.id(), new SaisieDsm(null, List.of(new SaisieLigneDsm(ligne.id(),
                ligne.identite().cle(), null, "XO", "12", Map.of(ColonneDsm.C17, VolumeAlcoolPur.litres("839.11"),
                        ColonneDsm.C22, VolumeAlcoolPur.litres("2254.15"))))), null);
        pieces.deposerJustificatif(agent, operateur, dsm.id(), ligne.id(), TypeMouvement.VENTE_EXPORT, TypePiece.DAE,
                "26FRA0000000123456", new FichierDepose("dae.pdf", pdfMinimal()));
        dsms.envoyer(agent, operateur, dsm.id(), null);
        dsms.prendreEnInstruction(admin, operateur, dsm.id(), null);
        dsms.valider(admin, operateur, dsm.id(), null);
        return dsm.id();
    }

    private static byte[] pdfMinimal() {
        try (var flux = FacturationIT.class.getResourceAsStream("/demo/piece-demonstration.pdf")) {
            return flux.readAllBytes();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private static Map<String, Object> corps(RestTestClient.ResponseSpec reponse) {
        return reponse.expectBody(Map.class).returnResult().getResponseBody();
    }

    @Test
    @DisplayName("RG-FAC-01, 05, 09, 13, 25 et RG-DEC-43 : la DSM facturée, son brouillon émis au total de la facture d'exemple ; la DSM devient facturée dans la transaction")
    void rg_fac_09_chaine() {
        long operateur = negociant(true);
        long id = dsmValidee(operateur);
        String dsm = "/api/operateurs/" + operateur + "/dsm/" + id;
        assertThat((List<String>) corps(client.get().uri(dsm).exchange().expectStatus().isOk()).get("actions"))
                .contains("FACTURER");

        Map<String, Object> demandee = corps(client.post().uri(dsm + "/facturation").exchange().expectStatus().isOk());
        long brouillon = ((Number) demandee.get("brouillonFacture")).longValue();
        assertThat((List<String>) demandee.get("actions")).doesNotContain("FACTURER");
        client.get().uri("/api/declarations/dsm?operateur=" + operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].facturationDemandee").isEqualTo(true)
                .jsonPath("$[0].facturee").isEqualTo(false);

        Map<String, Object> piece = corps(client.get().uri(PIECES + "/{id}", brouillon).exchange().expectStatus().isOk());
        assertThat(piece.get("etat")).isEqualTo("BROUILLON");
        assertThat(piece.get("titre")).isEqualTo("Cotisation interprofessionnelle Armagnac");
        assertThat(piece.get("totalHt")).isEqualTo(1855.95);
        assertThat(piece.get("totalTva")).isEqualTo(296.95);
        assertThat(piece.get("totalTtc")).isEqualTo(2152.9);
        assertThat(piece.get("empechements")).isEqualTo(List.of());
        assertThat(piece.get("emettre")).isEqualTo(true);
        List<Map<String, Object>> lignes = (List<Map<String, Object>>) piece.get("lignes");
        assertThat(lignes).singleElement().satisfies(g -> {
            assertThat(g.get("sorte")).isEqualTo("GROUPE");
            assertThat(g.get("texte")).isEqualTo("Commerce de l'Armagnac");
            assertThat((List<Map<String, Object>>) g.get("lignes")).extracting(l -> l.get("montantHt"))
                    .containsExactly(402.77, 100.69, 1081.99, 270.5);
        });

        Map<String, Object> emise = corps(client.post().uri(PIECES + "/{id}/emission", brouillon).exchange()
                .expectStatus().isOk());
        String numero = (String) emise.get("numero");
        assertThat(numero).matches(LocalDate.now().getYear() + "[0-9]{6}");
        assertThat(emise.get("echeance")).isEqualTo(ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS
                .echeance(LocalDate.now()).toString());
        assertThat(((Map<String, Object>) emise.get("vendeur")).get("siret")).isEqualTo(SIRET);

        Map<String, Object> cotisation = (Map<String, Object>) corps(client.get().uri(dsm).exchange().expectStatus()
                .isOk()).get("cotisation");
        assertThat(cotisation.get("statut")).isEqualTo("FACTUREE");
        assertThat((List<Map<String, Object>>) cotisation.get("factures")).singleElement().satisfies(f -> {
            assertThat(f.get("numero")).isEqualTo(numero);
            assertThat(((Number) f.get("piece")).longValue()).isEqualTo(brouillon);
            assertThat(f.get("ttc")).isEqualTo(2152.9);
        });
        client.get().uri("/api/declarations/dsm?operateur=" + operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].facturationDemandee").isEqualTo(false)
                .jsonPath("$[0].facturee").isEqualTo(true);
        client.get().uri(dsm + "/journal").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].action").isEqualTo("FACTURE_EMISE")
                .jsonPath("$[1].action").isEqualTo("FACTURATION_DEMANDEE");
        client.get().uri(PIECES + "/{id}/journal", brouillon).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].evenement").isEqualTo(List.of("EMISE", "DEMANDEE"));
        client.post().uri(PIECES + "/{id}/emission", brouillon).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-FAC-14");
    }

    @Test
    @DisplayName("RG-FAC-15, 20 : le PDF reprend vendeur, acheteur, numéro, lignes, récapitulatif, totaux et mentions ; son téléchargement est journalisé")
    void rg_fac_15_pdf() throws IOException {
        long operateur = negociant(true);
        long id = dsmValidee(operateur);
        long brouillon = ((Number) corps(client.post().uri("/api/operateurs/" + operateur + "/dsm/" + id + "/facturation")
                .exchange().expectStatus().isOk()).get("brouillonFacture")).longValue();
        client.get().uri(PIECES + "/{id}/pdf", brouillon).exchange().expectStatus().isNotFound();
        byte[] apercu = client.get().uri(PIECES + "/{id}/apercu", brouillon).exchange().expectStatus().isOk()
                .expectHeader().contentType("application/pdf")
                .expectBody(byte[].class).returnResult().getResponseBody();
        try (PDDocument document = Loader.loadPDF(apercu)) {
            assertThat(new PDFTextStripper().getText(document)).contains("BROUILLON", "PROJET DE FACTURE",
                    "Document sans valeur comptable", "Facture n° attribué à l'émission", "Commerce de l'Armagnac",
                    "2 152,90 €", "calculée à l'émission");
        }
        client.get().uri(PIECES + "/{id}/journal", brouillon).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(1);
        String numero = (String) corps(client.post().uri(PIECES + "/{id}/emission", brouillon).exchange()
                .expectStatus().isOk()).get("numero");
        client.get().uri(PIECES + "/{id}/apercu", brouillon).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-FAC-14");

        byte[] pdf = client.get().uri(PIECES + "/{id}/pdf", brouillon).exchange().expectStatus().isOk()
                .expectHeader().contentType("application/pdf")
                .expectBody(byte[].class).returnResult().getResponseBody();

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String texte = new PDFTextStripper().getText(document);
            assertThat(texte).contains("Cotisation interprofessionnelle Armagnac", "Facture n° " + numero,
                    "Bureau National Interprofessionnel", "SIRET " + SIRET, "Commerce de l'Armagnac",
                    "8,3911 hlAP", "402,77 €", "1 484,76 €", "296,95 €", "2 152,90 €", "30 jours fin de mois",
                    "Prestation de services", "DSM", "Indemnité forfaitaire", "60,00 €/hlAP",
                    "Part de la cotisation interprofessionnelle non assujettie à la TVA");
        }
        client.get().uri(PIECES + "/{id}/journal", brouillon).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].evenement").isEqualTo("CONSULTEE")
                .jsonPath("$[0].detail").isEqualTo("PDF");
    }

    @Test
    @DisplayName("RG-FAC-12 : des émissions simultanées prennent des numéros distincts et consécutifs")
    void rg_fac_12_concurrence() throws Exception {
        long operateur = negociant(true);
        String code = operateurs.operateur(operateur).orElseThrow().code();
        Acteur acteur = new Acteur(admin.utilisateur());
        List<DemandeFacture> demandes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            demandes.add(new DemandeFacture(new Origine("declarations", "TEST", "Test de numérotation", "IT-" + unique(), "IT"),
                    ClientRef.operateur(operateur, code), LocalDate.of(2026, 7, 31), null,
                    ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null, null, null, List.of(new ArticleDemande(
                            new ArticleRef("declarations", "CVO-SORTIES_FRANCE-S"), BigDecimal.ONE, null, null))));
        }
        List<Long> brouillons = demandeFactures.demander(acteur, demandes);
        ExecutorService executeur = Executors.newFixedThreadPool(brouillons.size());
        try {
            List<Callable<String>> emissions = brouillons.stream()
                    .<Callable<String>>map(b -> () -> emettrePiece.emettre(acteur, b).numero().orElseThrow()).toList();
            List<Long> numeros = new ArrayList<>();
            for (Future<String> numero : executeur.invokeAll(emissions)) {
                numeros.add(Long.parseLong(numero.get()));
            }
            assertThat(numeros).doesNotHaveDuplicates();
            assertThat(numeros.stream().mapToLong(Long::longValue).max().orElseThrow()
                    - numeros.stream().mapToLong(Long::longValue).min().orElseThrow()).isEqualTo(brouillons.size() - 1);
        } finally {
            executeur.shutdown();
        }
    }

    @Test
    @DisplayName("RG-DEC-43 : la DSM d'un opérateur non facturable est refusée, et le refus nomme ce qui manque")
    void rg_dec_43_non_facturable() {
        long operateur = negociant(false);
        long id = dsmValidee(operateur);

        client.post().uri("/api/declarations/facturation").body(Map.of("dsms", List.of(id))).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-DEC-43")
                .jsonPath("$.detail").value(m -> assertThat((String) m).contains("n'est pas facturable (SIRET manquant)"));
    }

    @Test
    @DisplayName("RG-FAC-26 : l'historique, l'émission, la modification, l'abandon, les notes et le vendeur sont refusés à un membre d'un négociant ; le vendeur incomplet est refusé (RG-FAC-16)")
    void rg_fac_26_droits() {
        long operateur = negociant(true);
        String email = emailUnique("admin");
        membre(operateur, Role.ADMIN, email);
        RestTestClient negociant = connecte(email);

        negociant.get().uri(PIECES).exchange().expectStatus().isForbidden();
        negociant.post().uri(PIECES + "/1/emission").exchange().expectStatus().isForbidden();
        negociant.post().uri(PIECES + "/emission").body(Map.of("ids", List.of(1))).exchange().expectStatus()
                .isForbidden();
        negociant.put().uri(PIECES + "/1").body(Map.of("condition", Map.of("jours", 0, "finDeMois", false),
                "lignes", List.of(), "version", 0)).exchange().expectStatus().isForbidden();
        negociant.delete().uri(PIECES + "/1").exchange().expectStatus().isForbidden();
        negociant.post().uri(PIECES + "/1/notes").body(Map.of("texte", "Note")).exchange().expectStatus().isForbidden();
        negociant.get().uri(PIECES + "/1/articles").exchange().expectStatus().isForbidden();
        negociant.put().uri(VENDEUR).body(vendeur()).exchange().expectStatus().isForbidden();
        negociant.post().uri("/api/declarations/facturation").body(Map.of("dsms", List.of(1))).exchange()
                .expectStatus().isForbidden();
        Map<String, Object> incomplet = vendeur();
        incomplet.put("iban", "FR76");
        client.put().uri(VENDEUR).body(incomplet).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-FAC-16");
        client.get().uri(VENDEUR).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.vendeur.siren").isEqualTo("776999955")
                .jsonPath("$.modifiable").isEqualTo(true);
    }

    @Test
    @DisplayName("RG-FAC-27 : l'historique filtre par client, état, numéro et nature d'origine")
    void rg_fac_27_historique() {
        long operateur = negociant(true);
        long id = dsmValidee(operateur);
        long brouillon = ((Number) corps(client.post().uri("/api/operateurs/" + operateur + "/dsm/" + id + "/facturation")
                .exchange().expectStatus().isOk()).get("brouillonFacture")).longValue();
        String client_ = Long.toString(operateur);

        client.get().uri(u -> u.path(PIECES).queryParam("client", client_).queryParam("etat", "BROUILLON").build())
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].id").isEqualTo(List.of((int) brouillon))
                .jsonPath("$[0].reference").value(r -> assertThat((String) r).startsWith("DSM "))
                .jsonPath("$[0].ttc").isEqualTo(2152.9);
        String numero = (String) corps(client.post().uri(PIECES + "/{id}/emission", brouillon).exchange()
                .expectStatus().isOk()).get("numero");
        client.get().uri(u -> u.path(PIECES).queryParam("numero", numero).queryParam("nature", "DSM").build())
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].id").isEqualTo(List.of((int) brouillon))
                .jsonPath("$[0].etat").isEqualTo("EMISE")
                .jsonPath("$[0].clientCode").isEqualTo(operateurs.operateur(operateur).orElseThrow().code());
        client.get().uri(u -> u.path(PIECES).queryParam("client", client_).queryParam("etat", "BROUILLON").build())
                .exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("A-FAC-2, RG-FAC-03 : le catalogue de declarations donne les huit articles de la cotisation à une date")
    void rg_fac_03_catalogue_de_declarations() {
        CatalogueArticles catalogue = catalogues.stream().filter(c -> c.source().equals("declarations")).findFirst()
                .orElseThrow();

        List<Article> articles = catalogue.articles(LocalDate.of(2026, 7, 31));

        assertThat(articles).hasSize(8).first().satisfies(a -> {
            assertThat(a.code()).isEqualTo("CVO-SORTIES_FRANCE-S");
            assertThat(a.prixUnitaire()).isEqualByComparingTo("48");
            assertThat(a.tva().code()).isEqualTo(CategorieTva.Code.S);
            assertThat(a.unite()).isEqualTo("hlAP");
            assertThat(a.lienAdministration()).isEqualTo("/bo/parametrage/taux-cvo");
        });
        assertThat(catalogue.article("CVO-SORTIES_EXPORT-E", LocalDate.of(2023, 12, 31)).orElseThrow())
                .satisfies(a -> {
                    assertThat(a.prixUnitaire()).isEqualByComparingTo("11");
                    assertThat(a.tva().code()).isEqualTo(CategorieTva.Code.E);
                });
    }

    /** Le brouillon de la DSM validée d'un nouveau négociant facturable. */
    private long brouillonDeDsm(long operateur) {
        long id = dsmValidee(operateur);
        return ((Number) corps(client.post().uri("/api/operateurs/" + operateur + "/dsm/" + id + "/facturation")
                .exchange().expectStatus().isOk()).get("brouillonFacture")).longValue();
    }

    /** Une ligne de la réponse, telle qu'elle se renvoie à l'enregistrement (RG-FAC-17). */
    private static Map<String, Object> saisieDe(Map<String, Object> ligne) {
        Map<String, Object> saisie = new HashMap<>();
        for (String champ : List.of("numero", "sorte", "texte", "unite", "quantite", "prixUnitaire")) {
            saisie.put(champ, ligne.get(champ));
        }
        saisie.put("lignes", ((List<Map<String, Object>>) ligne.get("lignes")).stream().map(FacturationIT::saisieDe)
                .toList());
        return saisie;
    }

    private static Map<String, Object> modification(Map<String, Object> piece, String periode,
            List<Map<String, Object>> lignes) {
        Map<String, Object> corps = new HashMap<>();
        corps.put("libellePeriode", periode);
        corps.put("condition", Map.of("jours", 30, "finDeMois", true));
        corps.put("notePied", piece.get("notePied"));
        corps.put("lignes", lignes);
        corps.put("version", piece.get("version"));
        return corps;
    }

    @Test
    @DisplayName("RG-FAC-17 : période corrigée, texte ajouté en tête, journal « modifiée » ; ligne reçue verrouillée ; version périmée refusée")
    void rg_fac_17_modifier() {
        long brouillon = brouillonDeDsm(negociant(true));
        Map<String, Object> piece = corps(client.get().uri(PIECES + "/{id}", brouillon).exchange().expectStatus().isOk());
        Map<String, Object> groupe = saisieDe(((List<Map<String, Object>>) piece.get("lignes")).getFirst());
        Map<String, Object> texte = new HashMap<>(Map.of("sorte", "TEXTE",
                "texte", "Régularisation suite au contrôle du 02/10"));

        Map<String, Object> modifiee = corps(client.put().uri(PIECES + "/{id}", brouillon)
                .body(modification(piece, "DSM juillet 2026, rectifiée", List.of(texte, groupe))).exchange()
                .expectStatus().isOk());

        assertThat(modifiee.get("libellePeriode")).isEqualTo("DSM juillet 2026, rectifiée");
        List<Map<String, Object>> lignes = (List<Map<String, Object>>) modifiee.get("lignes");
        assertThat(lignes.getFirst()).containsEntry("sorte", "TEXTE").containsEntry("recue", false);
        assertThat(lignes.getLast()).containsEntry("recue", true);
        assertThat(modifiee.get("totalTtc")).isEqualTo(2152.9);
        client.get().uri(PIECES + "/{id}/journal", brouillon).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].evenement").isEqualTo("MODIFIEE");
        // Saisie faite sur la version d'avant : quelqu'un a enregistré entre-temps.
        client.put().uri(PIECES + "/{id}", brouillon).body(modification(piece, "Autre", List.of(groupe))).exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
        Map<String, Object> ampute = new HashMap<>(groupe);
        ampute.put("lignes", ((List<?>) groupe.get("lignes")).subList(1, 4));
        client.put().uri(PIECES + "/{id}", brouillon).body(modification(modifiee, null, List.of(ampute))).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-FAC-17");
        client.get().uri(PIECES + "/{id}/articles", brouillon).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].code").value(codes ->
                        assertThat((List<String>) codes).contains("CVO-SORTIES_FRANCE-S", "CVO-SORTIES_FRANCE-E"));
    }

    @Test
    @DisplayName("RG-FAC-18, 25 : le brouillon abandonné disparaît avec son journal ; la DSM redevient à facturer et se refacture ; une pièce émise ne s'abandonne pas")
    void rg_fac_18_abandonner() {
        long operateur = negociant(true);
        long id = dsmValidee(operateur);
        String dsm = "/api/operateurs/" + operateur + "/dsm/" + id;
        long brouillon = ((Number) corps(client.post().uri(dsm + "/facturation").exchange().expectStatus().isOk())
                .get("brouillonFacture")).longValue();

        client.delete().uri(PIECES + "/{id}", brouillon).exchange().expectStatus().isNoContent();

        client.get().uri(PIECES + "/{id}", brouillon).exchange().expectStatus().isNotFound();
        assertThat((List<String>) corps(client.get().uri(dsm).exchange().expectStatus().isOk()).get("actions"))
                .contains("FACTURER");
        client.get().uri("/api/declarations/dsm?operateur=" + operateur).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].facturationDemandee").isEqualTo(false)
                .jsonPath("$[0].facturee").isEqualTo(false);
        client.get().uri(dsm + "/journal").exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].action").isEqualTo("FACTURATION_ABANDONNEE");
        long refacturee = ((Number) corps(client.post().uri(dsm + "/facturation").exchange().expectStatus().isOk())
                .get("brouillonFacture")).longValue();
        client.post().uri(PIECES + "/{id}/emission", refacturee).exchange().expectStatus().isOk();
        client.delete().uri(PIECES + "/{id}", refacturee).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-FAC-14");
    }

    @Test
    @DisplayName("RG-FAC-19 : une sélection émise brouillon par brouillon, dans l'ordre ; un refus est rapporté et ne consomme pas de numéro")
    void rg_fac_19_lot() {
        long dejaEmis = brouillonDeDsm(negociant(true));
        long aEmettre = brouillonDeDsm(negociant(true));
        long numero = Long.parseLong((String) corps(client.post().uri(PIECES + "/{id}/emission", dejaEmis).exchange()
                .expectStatus().isOk()).get("numero"));

        List<Map<String, Object>> comptesRendus = client.post().uri(PIECES + "/emission")
                .body(Map.of("ids", List.of(dejaEmis, aEmettre))).exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();

        assertThat(comptesRendus).extracting(c -> ((Number) c.get("id")).longValue()).containsExactly(dejaEmis, aEmettre);
        assertThat((String) comptesRendus.getFirst().get("motif")).contains("est émise");
        assertThat(comptesRendus.getFirst().get("numero")).isNull();
        assertThat(Long.parseLong((String) comptesRendus.getLast().get("numero"))).isEqualTo(numero + 1);
        client.get().uri(PIECES + "/{id}", aEmettre).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.etat").isEqualTo("EMISE");
    }

    @Test
    @DisplayName("RG-FAC-21, 26 : une note interne au carnet de la pièce, sur un brouillon comme sur une pièce émise, hors du journal ; modifiée et supprimée par son auteur ; une note vide est refusée ; refusée à un membre d'un négociant")
    void rg_fac_21_note() {
        long operateur = negociant(true);
        long brouillon = brouillonDeDsm(operateur);
        String notes = PIECES + "/" + brouillon + "/notes";

        long note = ((Number) corps(client.post().uri(notes).body(Map.of("texte", "Relancer le comptable")).exchange()
                .expectStatus().isCreated()).get("id")).longValue();
        client.post().uri(PIECES + "/{id}/emission", brouillon).exchange().expectStatus().isOk();
        client.post().uri(notes).body(Map.of("texte", "Envoyée par courrier")).exchange().expectStatus().isCreated();
        client.post().uri(notes).body(Map.of("texte", " ")).exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-FAC-21");
        client.put().uri(notes + "/{id}", note).body(Map.of("texte", "Comptable relancé")).exchange()
                .expectStatus().isOk().expectBody().jsonPath("$.modifieeLe").isNotEmpty();

        client.get().uri(notes).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].texte").isEqualTo("Comptable relancé")
                .jsonPath("$[0].deLUtilisateur").isEqualTo(true)
                .jsonPath("$[1].texte").isEqualTo("Envoyée par courrier");
        client.get().uri(PIECES + "/{id}/journal", brouillon).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].detail").value(d -> assertThat((List<String>) d)
                        .doesNotContain("Envoyée par courrier", "Comptable relancé"));
        client.delete().uri(notes + "/{id}", note).exchange().expectStatus().isNoContent();
        client.get().uri(notes).exchange().expectStatus().isOk().expectBody().jsonPath("$.length()").isEqualTo(1);

        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);
        RestTestClient observateur = connecte(email);
        observateur.get().uri(notes).exchange().expectStatus().isForbidden();
        observateur.post().uri(notes).body(Map.of("texte", "Note")).exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-FAC-32, 33 : une sélection transmise à Zeendoc, état au listing, au détail, au journal et au portail ; ni brouillon ni retransmission ; refusé à un membre d'un négociant")
    void rg_fac_32_zeendoc() {
        long operateur = negociant(true);
        long emise = brouillonDeDsm(operateur);
        client.post().uri(PIECES + "/{id}/emission", emise).exchange().expectStatus().isOk();
        long brouillon = brouillonDeDsm(negociant(true));

        List<Map<String, Object>> comptesRendus = client.post().uri(PIECES + "/zeendoc")
                .body(Map.of("ids", List.of(emise, brouillon))).exchange().expectStatus().isOk()
                .expectBody(List.class).returnResult().getResponseBody();

        assertThat(comptesRendus.getFirst().get("motif")).isNull();
        assertThat((String) comptesRendus.getLast().get("motif")).contains("n'est pas émise");
        client.get().uri(PIECES + "?client={client}", String.valueOf(operateur)).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].zeendoc").isEqualTo("TRANSMISE")
                .jsonPath("$[0].transmiseLe").exists();
        client.get().uri(PIECES + "/{id}/journal", emise).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].evenement").isEqualTo("TRANSMISE")
                .jsonPath("$[0].cote").isEqualTo("VENDEUR");
        client.post().uri(PIECES + "/zeendoc").body(Map.of("ids", List.of(emise))).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].motif").value(m -> assertThat((String) m).contains("déjà transmise"));

        reglement.enregistrer(emise, LocalDate.of(2026, 11, 30));
        client.get().uri(PIECES + "/{id}", emise).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.zeendoc").isEqualTo("PAYEE")
                .jsonPath("$.payeeLe").isEqualTo("2026-11-30");
        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);
        RestTestClient observateur = connecte(email);
        String factures = "/api/operateurs/" + operateur + "/factures";
        observateur.get().uri(factures).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].zeendoc").isEqualTo("PAYEE");
        observateur.get().uri(factures + "/{id}", emise).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.zeendoc").isEqualTo("PAYEE");
        observateur.post().uri(PIECES + "/zeendoc").body(Map.of("ids", List.of(emise))).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-FAC-26, 28 : au portail, l'opérateur voit ses pièces émises, jamais ses brouillons ; détail et PDF journalisés côté client")
    void rg_fac_28_portail() throws IOException {
        long operateur = negociant(true);
        long emise = brouillonDeDsm(operateur);
        String numero = (String) corps(client.post().uri(PIECES + "/{id}/emission", emise).exchange()
                .expectStatus().isOk()).get("numero");
        long brouillon = demandeFactures.demander(new Acteur(admin.utilisateur()), List.of(new DemandeFacture(
                new Origine("declarations", "TEST", "Test du portail", "IT-" + unique(), "IT"),
                ClientRef.operateur(operateur, operateurs.operateur(operateur).orElseThrow().code()),
                LocalDate.of(2026, 7, 31), null, ConditionPaiement.TRENTE_JOURS_FIN_DE_MOIS, null, null, null,
                List.of(new ArticleDemande(new ArticleRef("declarations", "CVO-SORTIES_FRANCE-S"), BigDecimal.ONE,
                        null, null))))).getFirst();
        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);
        RestTestClient observateur = connecte(email);
        String factures = "/api/operateurs/" + operateur + "/factures";

        observateur.get().uri(factures).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].id").isEqualTo(List.of((int) emise))
                .jsonPath("$[0].numero").isEqualTo(numero);
        observateur.get().uri(factures + "/{id}", emise).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.numero").isEqualTo(numero)
                .jsonPath("$.origine.reference").value(r -> assertThat((String) r).startsWith("DSM "))
                .jsonPath("$.emettre").isEqualTo(false);
        byte[] pdf = observateur.get().uri(factures + "/{id}/pdf", emise).exchange().expectStatus().isOk()
                .expectBody(byte[].class).returnResult().getResponseBody();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(new PDFTextStripper().getText(document)).contains("Facture n° " + numero);
        }
        observateur.get().uri(factures + "/{id}", brouillon).exchange().expectStatus().isNotFound();
        observateur.get().uri(factures + "/{id}/journal", emise).exchange().expectStatus().isNotFound();

        client.get().uri(PIECES + "/{id}/journal", emise).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[0].evenement").isEqualTo("CONSULTEE")
                .jsonPath("$[0].cote").isEqualTo("CLIENT")
                .jsonPath("$[0].detail").isEqualTo("PDF")
                .jsonPath("$[1].cote").isEqualTo("CLIENT");

        // RG-FAC-26 : un membre d'un autre opérateur n'y a pas accès, ni à l'historique du BNIA.
        String autre = emailUnique("observateur");
        membre(negociant(true), Role.OBSERVATEUR, autre);
        RestTestClient etranger = connecte(autre);
        etranger.get().uri(factures).exchange().expectStatus().isForbidden();
        etranger.get().uri(factures + "/{id}", emise).exchange().expectStatus().isForbidden();
        observateur.get().uri(PIECES + "/{id}", emise).exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-FAC-26, 34 : au portail, l'opérateur lit les lignes de ses pièces émises de la période ; un autre opérateur est refusé")
    void rg_fac_34_lignes_portail() {
        long operateur = negociant(true);
        long emise = brouillonDeDsm(operateur);
        client.post().uri(PIECES + "/{id}/emission", emise).exchange().expectStatus().isOk();
        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);
        RestTestClient observateur = connecte(email);
        String jour = LocalDate.now().toString();
        String hier = LocalDate.now().minusDays(1).toString();
        String lignes = "/api/operateurs/" + operateur + "/factures/lignes";

        observateur.get().uri(u -> u.path(lignes).queryParam("du", jour).queryParam("au", jour)
                        .queryParam("nature", "DSM").build()).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$[*].piece").isEqualTo(List.of((int) emise, (int) emise, (int) emise,
                        (int) emise))
                .jsonPath("$[*].montantHt").isEqualTo(List.of(402.77, 100.69, 1081.99, 270.5));
        observateur.get().uri(u -> u.path(lignes).queryParam("du", hier).queryParam("au", hier).build())
                .exchange().expectStatus().isOk().expectBody().jsonPath("$").isEmpty();
        observateur.get().uri(u -> u.path(lignes).queryParam("du", jour).queryParam("au", hier).build())
                .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

        String autre = emailUnique("observateur");
        membre(negociant(true), Role.OBSERVATEUR, autre);
        connecte(autre).get().uri(u -> u.path(lignes).queryParam("du", jour).queryParam("au", jour).build())
                .exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-FAC-10, 11 : le port Clients lit l'acheteur d'un opérateur, pays compris ; un client inconnu est vide")
    void rg_fac_11_clients() {
        long operateur = negociant(true);
        String code = operateurs.operateur(operateur).orElseThrow().code();

        Acheteur acheteur = clients.acheteur(ClientRef.operateur(operateur, code)).orElseThrow();

        assertThat(acheteur.siren()).isEqualTo("776999955");
        assertThat(acheteur.adresseFacturation().codePays()).isEqualTo("FR");
        assertThat(acheteur.email()).isEqualTo("compta-" + operateur + "@negoce.example");
        assertThat(acheteur.manques()).isEmpty();
        assertThat(clients.acheteur(ClientRef.operateur(-1, "X"))).isEmpty();
    }

    @Test
    @DisplayName("RG-FAC-29 : le tableau des articles réunit les huit articles de la cotisation, avec le lien vers la grille")
    void rg_fac_29_articles() {
        List<Map<String, Object>> articles = client.get().uri("/api/facturation/articles").exchange().expectStatus()
                .isOk().expectBody(List.class).returnResult().getResponseBody();

        assertThat(articles).filteredOn(a -> a.get("source").equals("declarations")).hasSize(8)
                .allSatisfy(a -> assertThat(a.get("lienAdministration")).isEqualTo("/bo/parametrage/taux-cvo"))
                .extracting(a -> a.get("code")).contains("CVO-SORTIES_FRANCE-S", "CVO-SORTIES_FRANCE-E");
    }

    @Test
    @DisplayName("RG-FAC-30 : les ventes du jour reprennent les lignes de la facture émise ; une période antérieure ne les a pas ; PDF avec récapitulatif de TVA et totaux")
    void rg_fac_30_ventes() throws IOException {
        long operateur = negociant(true);
        long brouillon = brouillonDeDsm(operateur);
        String jour = LocalDate.now().toString();
        String hier = LocalDate.now().minusDays(1).toString();
        client.post().uri(PIECES + "/{id}/emission", brouillon).exchange().expectStatus().isOk();

        Map<String, Object> ventes = corps(client.get().uri(u -> u.path("/api/facturation/ventes")
                .queryParam("du", jour).queryParam("au", jour).build()).exchange().expectStatus().isOk());

        List<Map<String, Object>> lignes = ((List<Map<String, Object>>) ventes.get("lignes")).stream()
                .filter(l -> ((Number) l.get("piece")).longValue() == brouillon).toList();
        assertThat(lignes).extracting(l -> l.get("montantHt")).containsExactly(402.77, 100.69, 1081.99, 270.5);
        assertThat(lignes).allSatisfy(l -> {
            assertThat(l.get("avoir")).isEqualTo(false);
            assertThat(l.get("clientIdentifiant")).isEqualTo(Long.toString(operateur));
            assertThat(((Map<String, Object>) l.get("origine")).get("nature")).isEqualTo("DSM");
        });
        client.get().uri(u -> u.path("/api/facturation/ventes").queryParam("du", hier).queryParam("au", hier).build())
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.lignes[?(@.piece == " + brouillon + ")]").isEmpty();
        client.get().uri(u -> u.path("/api/facturation/ventes").queryParam("du", jour).queryParam("au", hier).build())
                .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.regle").isEqualTo("RG-FAC-30");

        byte[] pdf = client.get().uri(u -> u.path("/api/facturation/ventes/pdf").queryParam("du", jour)
                .queryParam("au", jour).build()).exchange().expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF).expectBody(byte[].class).returnResult()
                .getResponseBody();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String texte = new PDFTextStripper().getText(document);
            assertThat(texte).contains("Ventes du", "CVO-SORTIES_FRANCE-S", "Récapitulatif de TVA", "Total TTC");
        }
    }

    @Test
    @DisplayName("RG-FAC-31 : la lecture publiée rend les lignes vendues de la période, filtrées par nature d'origine")
    void rg_fac_31_lecture_publiee() {
        long operateur = negociant(true);
        long brouillon = brouillonDeDsm(operateur);
        client.post().uri(PIECES + "/{id}/emission", brouillon).exchange().expectStatus().isOk();
        LocalDate jour = LocalDate.now();

        assertThat(facturationLecture.ventes(jour, jour, Optional.of("DSM")))
                .filteredOn(l -> l.piece() == brouillon).hasSize(4)
                .extracting(LigneVendue::montantHt).containsExactly(new BigDecimal("402.77"),
                        new BigDecimal("100.69"), new BigDecimal("1081.99"), new BigDecimal("270.50"));
        assertThat(facturationLecture.ventes(jour, jour, Optional.of("FIN_DE_TRAVAUX")))
                .noneMatch(l -> l.piece() == brouillon);
    }

    @Test
    @DisplayName("RG-FAC-26 : les articles et les ventes sont refusés à un membre d'un négociant")
    void rg_fac_26_ventes_et_articles() {
        long operateur = negociant(true);
        String email = emailUnique("admin");
        membre(operateur, Role.ADMIN, email);
        RestTestClient negociant = connecte(email);
        String jour = LocalDate.now().toString();

        negociant.get().uri("/api/facturation/articles").exchange().expectStatus().isForbidden();
        negociant.get().uri(u -> u.path("/api/facturation/ventes").queryParam("du", jour).queryParam("au", jour)
                .build()).exchange().expectStatus().isForbidden();
        negociant.get().uri(u -> u.path("/api/facturation/ventes/pdf").queryParam("du", jour).queryParam("au", jour)
                .build()).exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("RG-MSG-01, 17 (spec 15) : une pièce émise d'un client est conversable ; le client, en lecture seule, y écrit")
    void rg_msg_17_conversation_d_une_facture() {
        long operateur = negociant(true);
        long brouillon = brouillonDeDsm(operateur);
        String conversation = "/api/messagerie/objets/FACTURE/" + brouillon;
        client.get().uri(conversation).exchange().expectStatus().isNotFound();
        String numero = (String) corps(client.post().uri(PIECES + "/{id}/emission", brouillon).exchange()
                .expectStatus().isOk()).get("numero");

        String email = emailUnique("observateur");
        membre(operateur, Role.OBSERVATEUR, email);
        RestTestClient observateur = connecte(email);
        observateur.get().uri(conversation).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.objet.libelle").isEqualTo("Facture " + numero)
                .jsonPath("$.peutEcrire").isEqualTo(true);
        observateur.post().uri(conversation + "/messages").body(Map.of("texte", "Le volume facturé est-il juste ?"))
                .exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.cote").isEqualTo("OPERATEUR");
        client.get().uri(conversation).exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.messages[0].nonLu").isEqualTo(true)
                .jsonPath("$.operateur.id").isEqualTo(operateur);
        String autre = emailUnique("autre");
        membre(negociant(true), Role.ADMIN, autre);
        connecte(autre).get().uri(conversation).exchange().expectStatus().isForbidden();
    }
}
