package fr.bnia.facturation.domain;

import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Une pièce, facture ou avoir (agrégat, spec 08 §3.1) : brouillon, puis émise, sans retour. Une pièce émise ne
 * se modifie plus (RG-FAC-14) : son numéro, son vendeur, son acheteur et son PDF sont figés.
 */
public final class Facture {

    private final Long id;
    private final SensPiece sens;
    private EtatPiece etat;
    private final Origine origine;
    private final LocalDate dateOperation;
    private final String libellePeriode;
    private final ConditionPaiement condition;
    private final String bonDeCommande;
    private final String noteEntete;
    private final String notePied;
    private Acheteur acheteur;
    private Vendeur vendeur;
    private final List<LigneFacture> lignes;
    private String numero;
    private LocalDate dateEmission;
    private LocalDate echeance;
    private DocumentDepose pdf;
    private final Instant creeeLe;
    private final Auteur creePar;
    private final long version;

    /** Tous les champs, tels qu'enregistrés : voir {@link #demandee} et {@link #reconstituer}. */
    public record Donnees(Long id, SensPiece sens, EtatPiece etat, Origine origine, LocalDate dateOperation,
            String libellePeriode, ConditionPaiement condition, String bonDeCommande, String noteEntete,
            String notePied, Acheteur acheteur, Vendeur vendeur, List<LigneFacture> lignes, String numero,
            LocalDate dateEmission, LocalDate echeance, DocumentDepose pdf, Instant creeeLe, Auteur creePar,
            long version) {
    }

    private Facture(Donnees d) {
        this.id = d.id();
        this.sens = d.sens();
        this.etat = d.etat();
        this.origine = d.origine();
        this.dateOperation = d.dateOperation();
        this.libellePeriode = Textes.facultatif(d.libellePeriode());
        this.condition = d.condition();
        this.bonDeCommande = Textes.facultatif(d.bonDeCommande());
        this.noteEntete = Textes.facultatif(d.noteEntete());
        this.notePied = Textes.facultatif(d.notePied());
        this.acheteur = d.acheteur();
        this.vendeur = d.vendeur();
        this.lignes = List.copyOf(d.lignes());
        this.numero = d.numero();
        this.dateEmission = d.dateEmission();
        this.echeance = d.echeance();
        this.pdf = d.pdf();
        this.creeeLe = d.creeeLe();
        this.creePar = d.creePar();
        this.version = d.version();
    }

    /**
     * RG-FAC-01 : le brouillon de facture d'une demande, avec son origine, son acheteur et ses lignes résolues.
     */
    public static Facture demandee(Origine origine, Acheteur acheteur, LocalDate dateOperation, String libellePeriode,
            ConditionPaiement condition, String bonDeCommande, String noteEntete, String notePied,
            List<LigneFacture> lignes, Auteur creePar, Instant creeeLe) {
        if (origine == null) {
            throw new RegleEnfreinte("RG-FAC-01", "Une facture a une origine.");
        }
        if (dateOperation == null) {
            throw new RegleEnfreinte("RG-FAC-01", "Une facture a une date de l'opération.");
        }
        if (condition == null) {
            throw new RegleEnfreinte("RG-FAC-13", "Une facture a une condition de paiement.");
        }
        if (acheteur == null) {
            throw new RegleEnfreinte("RG-FAC-10", "Une facture a un acheteur.");
        }
        return new Facture(new Donnees(null, SensPiece.FACTURE, EtatPiece.BROUILLON, origine, dateOperation,
                libellePeriode, condition, bonDeCommande, noteEntete, notePied, acheteur, null, lignes, null, null, null,
                null, creeeLe, creePar, 0));
    }

    public static Facture reconstituer(Donnees donnees) {
        return new Facture(donnees);
    }

    public Donnees donnees() {
        return new Donnees(id, sens, etat, origine, dateOperation, libellePeriode, condition, bonDeCommande, noteEntete,
                notePied, acheteur, vendeur, lignes, numero, dateEmission, echeance, pdf, creeeLe, creePar, version);
    }

    /** RG-FAC-14 : seul un brouillon se modifie ou s'émet. */
    public void exigerBrouillon() {
        if (etat != EtatPiece.BROUILLON) {
            throw new RegleEnfreinte("RG-FAC-14", "La pièce " + numero + " est émise : elle ne se modifie plus, "
                    + "elle se corrige par avoir.");
        }
    }

    /**
     * RG-FAC-11 : l'acheteur d'un client, relu par le port {@code Clients} juste avant l'émission. Un acheteur
     * libre ne se relit pas.
     */
    public void relireAcheteur(Acheteur relu) {
        exigerBrouillon();
        if (acheteur.client() != null && relu != null && acheteur.client().equals(relu.client())) {
            acheteur = relu;
        }
    }

    /**
     * RG-FAC-09 : ce qui empêche l'émission, dans l'ordre : une ligne article ; un acheteur complet (RG-FAC-10) ;
     * un vendeur paramétré ; un total TTC positif ou nul (RG-FAC-06) ; une ligne hors champ seule de sa catégorie
     * (RG-FAC-07). Vide si le brouillon peut être émis.
     */
    public List<String> empechements(Optional<Vendeur> vendeurParametre) {
        List<String> empechements = new ArrayList<>();
        List<LigneArticle> articles = lignesArticle();
        if (articles.isEmpty()) {
            empechements.add("aucune ligne article");
        }
        List<String> manques = acheteur.manques();
        if (!manques.isEmpty()) {
            empechements.add("acheteur incomplet (" + String.join(", ", manques) + ")");
        }
        if (vendeurParametre.isEmpty()) {
            empechements.add("vendeur non paramétré (Paramétrage > Facturation)");
        }
        if (totaux().ttc().signum() < 0) {
            empechements.add("total TTC négatif");
        }
        boolean horsChamp = articles.stream().anyMatch(a -> a.tva().code() == CategorieTva.Code.O);
        if (horsChamp && articles.stream().anyMatch(a -> a.tva().code() != CategorieTva.Code.O)) {
            empechements.add("une ligne hors champ de la TVA ne se mêle à aucune autre catégorie");
        }
        return empechements;
    }

    /** RG-FAC-09 : refuse l'émission en nommant ce qui manque. */
    public void exigerEmettable(Optional<Vendeur> vendeurParametre) {
        exigerBrouillon();
        List<String> empechements = empechements(vendeurParametre);
        if (!empechements.isEmpty()) {
            throw new RegleEnfreinte("RG-FAC-09", "Le brouillon ne peut pas être émis : "
                    + String.join(" ; ", empechements) + ".");
        }
    }

    /**
     * RG-FAC-09, 12, 13 : le numéro, la date d'émission, l'échéance ; le vendeur recopié. Le PDF suit
     * ({@link #joindrePdf}).
     */
    public void emettre(Numerotation numerotation, LocalDate date, Vendeur vendeurParametre) {
        exigerEmettable(Optional.ofNullable(vendeurParametre));
        if (numerotation.annee() != date.getYear()) {
            throw new IllegalArgumentException("Numéro de " + numerotation.annee() + " pour une émission en "
                    + date.getYear());
        }
        numero = numerotation.numero();
        dateEmission = date;
        echeance = condition.echeance(date);
        vendeur = vendeurParametre;
        etat = EtatPiece.EMISE;
    }

    /** RG-FAC-15 : le PDF, produit une fois à l'émission, puis conservé. */
    public void joindrePdf(DocumentDepose document) {
        if (etat != EtatPiece.EMISE || pdf != null) {
            throw new IllegalStateException("Le PDF se joint une fois, à l'émission.");
        }
        pdf = document;
    }

    /** Les lignes article, groupes dépliés, dans l'ordre d'impression. */
    public List<LigneArticle> lignesArticle() {
        List<LigneArticle> articles = new ArrayList<>();
        for (LigneFacture ligne : lignes) {
            switch (ligne) {
                case LigneArticle a -> articles.add(a);
                case LigneGroupe g -> g.lignes().stream().filter(LigneArticle.class::isInstance)
                        .map(LigneArticle.class::cast).forEach(articles::add);
                case LigneFacture.LigneInformative i -> {
                }
            }
        }
        return articles;
    }

    /**
     * RG-FAC-05 : les lignes article regroupées par catégorie et taux, dans l'ordre de leur première apparition ;
     * TVA = base × taux, au centime.
     */
    public List<LigneTva> recapitulatifTva() {
        Map<String, List<LigneArticle>> parCategorie = new LinkedHashMap<>();
        for (LigneArticle a : lignesArticle()) {
            parCategorie.computeIfAbsent(a.tva().code() + "/" + a.tva().taux(), k -> new ArrayList<>()).add(a);
        }
        return parCategorie.values().stream().map(groupe -> {
            CategorieTva categorie = groupe.getFirst().tva();
            BigDecimal base = groupe.stream().map(LigneArticle::montantHt).reduce(Montants.ZERO, BigDecimal::add);
            return new LigneTva(categorie, base, Montants.pourcentage(base, categorie.tauxApplique()));
        }).toList();
    }

    /** RG-FAC-05 : HT des lignes article, TVA du récapitulatif, TTC. */
    public Totaux totaux() {
        BigDecimal ht = lignesArticle().stream().map(LigneArticle::montantHt).reduce(Montants.ZERO, BigDecimal::add);
        BigDecimal tva = recapitulatifTva().stream().map(LigneTva::tva).reduce(Montants.ZERO, BigDecimal::add);
        return new Totaux(ht, tva, ht.add(tva));
    }

    /** RG-FAC-08 : vide sans ligne article. */
    public Optional<NatureOperation> natureOperation() {
        List<LigneArticle> articles = lignesArticle();
        if (articles.isEmpty()) {
            return Optional.empty();
        }
        boolean biens = articles.stream().anyMatch(a -> a.nature() == NatureArticle.BIEN);
        boolean services = articles.stream().anyMatch(a -> a.nature() == NatureArticle.SERVICE);
        return Optional.of(biens && services ? NatureOperation.LES_DEUX
                : biens ? NatureOperation.BIENS : NatureOperation.SERVICES);
    }

    /** RG-FAC-15 : titre du PDF, libellé de la nature d'origine, ou « Avoir ». */
    public String titre() {
        return sens == SensPiece.AVOIR || origine == null ? sens.libelle() : origine.libelleNature();
    }

    /** « La facture 2026000001 », « Le brouillon de facture n° 12 » : pour les messages. */
    public String designation() {
        return numero != null ? "La pièce " + numero : "Le brouillon n° " + id;
    }

    public Long id() {
        return id;
    }

    public SensPiece sens() {
        return sens;
    }

    public EtatPiece etat() {
        return etat;
    }

    public Origine origine() {
        return origine;
    }

    public LocalDate dateOperation() {
        return dateOperation;
    }

    public String libellePeriode() {
        return libellePeriode;
    }

    public ConditionPaiement condition() {
        return condition;
    }

    public String bonDeCommande() {
        return bonDeCommande;
    }

    public String noteEntete() {
        return noteEntete;
    }

    public String notePied() {
        return notePied;
    }

    public Acheteur acheteur() {
        return acheteur;
    }

    /** Recopié à l'émission ; vide en brouillon. */
    public Optional<Vendeur> vendeur() {
        return Optional.ofNullable(vendeur);
    }

    public List<LigneFacture> lignes() {
        return lignes;
    }

    public Optional<String> numero() {
        return Optional.ofNullable(numero);
    }

    public Optional<LocalDate> dateEmission() {
        return Optional.ofNullable(dateEmission);
    }

    public Optional<LocalDate> echeance() {
        return Optional.ofNullable(echeance);
    }

    public Optional<DocumentDepose> pdf() {
        return Optional.ofNullable(pdf);
    }

    public Instant creeeLe() {
        return creeeLe;
    }

    public Auteur creePar() {
        return creePar;
    }

    public long version() {
        return version;
    }
}
