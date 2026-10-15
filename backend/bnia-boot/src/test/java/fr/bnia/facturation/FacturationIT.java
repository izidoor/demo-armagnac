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
import fr.bnia.facturation.api.LigneDemandee.ArticleDemande;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.application.Clients;
import fr.bnia.facturation.application.EmettrePiece;
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
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Lot 1 de la spec 08 : facturer une DSM, de bout en bout. La DSM validée d'un négociant aux volumes de la facture
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
     * La DSM du mois de l'opérateur, validée, aux volumes de la facture d'exemple : sorties France 839,11 L
     * (colonne 17), export 2 254,15 L (colonne 22, avec son DAE).
     */
    private long dsmValidee(long operateur) {
        String email = emailUnique("agent");
        membre(operateur, Role.AGENT, email);
        var agent = new fr.bnia.operateurs.domain.partage.Acteur(operateurs.utilisateurParEmail(email).orElseThrow());
        points.fixer(admin, operateur, LocalDate.now().withDayOfMonth(1), List.of(new SaisieLigneStock(
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
        String numero = (String) corps(client.post().uri(PIECES + "/{id}/emission", brouillon).exchange()
                .expectStatus().isOk()).get("numero");

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
    @DisplayName("RG-FAC-26 : l'historique, l'émission et le vendeur sont refusés à un membre d'un négociant ; le vendeur incomplet est refusé (RG-FAC-16)")
    void rg_fac_26_droits() {
        long operateur = negociant(true);
        String email = emailUnique("admin");
        membre(operateur, Role.ADMIN, email);
        RestTestClient negociant = connecte(email);

        negociant.get().uri(PIECES).exchange().expectStatus().isForbidden();
        negociant.post().uri(PIECES + "/1/emission").exchange().expectStatus().isForbidden();
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
}
