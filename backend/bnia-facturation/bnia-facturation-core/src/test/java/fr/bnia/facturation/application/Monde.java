package fr.bnia.facturation.application;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.CatalogueArticles;
import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.Exemples;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.Numerotation;
import fr.bnia.facturation.domain.Vendeur;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Le contexte en mémoire, pour les tests de cas d'usage : repositories qui attribuent les identifiants, ports de
 * l'hôte simulés (un gestionnaire, un lecteur, un acteur sans droit), un catalogue « declarations » factice.
 */
final class Monde {

    static final Acteur GESTIONNAIRE = new Acteur(1);
    static final Acteur LECTEUR = new Acteur(2);
    static final Acteur ETRANGER = new Acteur(3);
    /** Rattaché à l'opérateur du client de l'exemple ({@link Exemples#CLIENT}, n° 42). */
    static final Acteur CLIENT = new Acteur(4);
    static final long OPERATEUR_CLIENT = 42;

    final Clock horloge = Clock.fixed(Instant.parse("2026-10-07T08:00:00Z"), ZoneId.of("Europe/Paris"));
    final Factures factures = new Factures();
    final Vendeurs vendeurs = new Vendeurs();
    final Numeros numeros = new Numeros();
    final Journal journal = new Journal();
    final Map<ClientRef, Acheteur> acheteurs = new HashMap<>(Map.of(Exemples.CLIENT, Exemples.acheteur()));
    final Map<String, Article> articles = new HashMap<>();
    final List<Object> evenements = new ArrayList<>();
    final Stockage stockage = new Stockage();

    final Clients clients = client -> Optional.ofNullable(acheteurs.get(client));
    final DroitsFacturation droits = new DroitsFacturation() {
        @Override
        public boolean gere(Acteur acteur) {
            return acteur.equals(GESTIONNAIRE);
        }

        @Override
        public boolean litTout(Acteur acteur) {
            return acteur.equals(GESTIONNAIRE) || acteur.equals(LECTEUR);
        }

        @Override
        public boolean litClient(Acteur acteur, long operateur) {
            return acteur.equals(GESTIONNAIRE) || acteur.equals(LECTEUR)
                    || acteur.equals(CLIENT) && operateur == OPERATEUR_CLIENT;
        }

        @Override
        public Optional<String> nom(long utilisateur) {
            return Optional.of("Utilisateur " + utilisateur);
        }
    };
    final CatalogueArticles catalogue = new CatalogueArticles() {
        @Override
        public String source() {
            return "declarations";
        }

        @Override
        public List<Article> articles(LocalDate date) {
            return List.copyOf(articles.values());
        }

        @Override
        public Optional<Article> article(String code, LocalDate date) {
            return Optional.ofNullable(articles.get(code));
        }
    };

    Monde() {
        for (String ligne : List.of("SORTIES_FRANCE", "SORTIES_EXPORT")) {
            ajouterArticle("CVO-" + ligne + "-S", "48.0000", true);
            ajouterArticle("CVO-" + ligne + "-E", "12.0000", true);
        }
    }

    void ajouterArticle(String code, String prix, boolean actif) {
        articles.put(code, new Article("declarations", code, "Cotisation " + code, "hlAP", new BigDecimal(prix),
                code.endsWith("-E") ? Exemples.EXONEREE : Exemples.TVA_20, NatureArticle.SERVICE, null, actif,
                "/bo/parametrage/taux-cvo"));
    }

    DemanderFactures demander() {
        return new DemanderFactures(factures, journal, clients, droits, List.of(catalogue), horloge);
    }

    EmettrePiece emettre() {
        return new EmettrePiece(factures, vendeurs, numeros, journal, clients, piece -> "%PDF-1.7".getBytes(), stockage,
                evenements::add, droits, horloge);
    }

    ConsulterFactures consulter() {
        return new ConsulterFactures(factures, filtre -> List.of(), vendeurs, journal, stockage,
                (piece, vendeur) -> ("%PDF-1.7 projet" + (vendeur.isPresent() ? " vendeur" : "")).getBytes(),
                droits, horloge);
    }

    ParametrerVendeur parametrer() {
        return new ParametrerVendeur(vendeurs, droits);
    }

    ModifierBrouillon modifier() {
        return new ModifierBrouillon(factures, vendeurs, journal, droits, List.of(catalogue), horloge);
    }

    AbandonnerBrouillon abandonner() {
        return new AbandonnerBrouillon(factures, evenements::add, droits);
    }

    /** Transactions immédiates (ADR-022) : l'action s'exécute sans transaction. */
    EmettreLot emettreLot() {
        return new EmettreLot(emettre(), Supplier::get, droits);
    }

    ConsulterMesFactures mesFactures(PiecesLecture lecture) {
        return new ConsulterMesFactures(factures, lecture, journal, stockage, droits, horloge);
    }

    AnnoterPiece annoter() {
        return new AnnoterPiece(factures, journal, droits, horloge);
    }

    static final class Factures implements FactureRepository {

        final Map<Long, Facture.Donnees> parId = new HashMap<>();
        private long suivant = 1;

        @Override
        public Facture inserer(Facture facture) {
            Facture.Donnees d = facture.donnees();
            long id = suivant++;
            Facture.Donnees enregistree = new Facture.Donnees(id, d.sens(), d.etat(), d.origine(), d.dateOperation(),
                    d.libellePeriode(), d.condition(), d.bonDeCommande(), d.noteEntete(), d.notePied(), d.acheteur(),
                    d.vendeur(), d.lignes(), d.numero(), d.dateEmission(), d.echeance(), d.pdf(), d.creeeLe(),
                    d.creePar(), 0);
            parId.put(id, enregistree);
            return Facture.reconstituer(enregistree);
        }

        @Override
        public Facture mettreAJour(Facture facture) {
            Facture.Donnees d = facture.donnees();
            if (parId.get(d.id()).version() != d.version()) {
                throw new ModificationConcurrente(null, "modifiée");
            }
            Facture.Donnees enregistree = new Facture.Donnees(d.id(), d.sens(), d.etat(), d.origine(),
                    d.dateOperation(), d.libellePeriode(), d.condition(), d.bonDeCommande(), d.noteEntete(),
                    d.notePied(), d.acheteur(), d.vendeur(), d.lignes(), d.numero(), d.dateEmission(), d.echeance(),
                    d.pdf(), d.creeeLe(), d.creePar(), d.version() + 1);
            parId.put(d.id(), enregistree);
            return Facture.reconstituer(enregistree);
        }

        @Override
        public Optional<Facture> parId(long id) {
            return Optional.ofNullable(parId.get(id)).map(Facture::reconstituer);
        }

        @Override
        public void supprimer(long id) {
            parId.remove(id);
        }

        @Override
        public boolean origineVivante(String source, String identifiant) {
            return parId.values().stream().anyMatch(d -> d.origine() != null && d.origine().source().equals(source)
                    && d.origine().identifiant().equals(identifiant));
        }
    }

    static final class Vendeurs implements VendeurRepository {

        Vendeur vendeur;

        @Override
        public Optional<Vendeur> charger() {
            return Optional.ofNullable(vendeur);
        }

        @Override
        public void enregistrer(Vendeur vendeur) {
            this.vendeur = vendeur;
        }
    }

    static final class Numeros implements NumerotationRepository {

        final Map<Integer, Long> derniers = new HashMap<>();

        @Override
        public Numerotation verrouiller(int annee) {
            return new Numerotation(annee, derniers.getOrDefault(annee, 0L));
        }

        @Override
        public void enregistrer(Numerotation numerotation) {
            derniers.put(numerotation.annee(), numerotation.dernier());
        }
    }

    static final class Journal implements JournalFacturation {

        final List<EntreeJournal> entrees = new ArrayList<>();

        @Override
        public void ajouter(EntreeJournal entree) {
            entrees.add(entree);
        }

        @Override
        public List<EntreeJournal> deLaPiece(long piece) {
            return entrees.stream().filter(e -> e.piece() == piece).toList().reversed();
        }
    }

    static final class Stockage implements StockageDocuments {

        final Map<Long, DocumentLu> documents = new HashMap<>();
        private long suivant = 100;

        @Override
        public DocumentDepose deposerPdf(String nom, byte[] contenu) {
            DocumentDepose document = new DocumentDepose(suivant++, nom, "application/pdf", contenu.length);
            documents.put(document.cle(), new DocumentLu(document, contenu));
            return document;
        }

        @Override
        public DocumentLu lire(long cle) {
            DocumentLu document = documents.get(cle);
            if (document == null) {
                throw new ElementIntrouvable("Document introuvable");
            }
            return document;
        }

        @Override
        public void supprimer(long cle) {
            documents.remove(cle);
        }

        @Override
        public void supprimer(Collection<Long> cles) {
            cles.forEach(documents::remove);
        }
    }
}
