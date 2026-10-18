package fr.bnia.facturation.domain;

import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.NatureArticle;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import fr.bnia.facturation.domain.LigneFacture.LigneInformative;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private String libellePeriode;
    private ConditionPaiement condition;
    private String bonDeCommande;
    private String noteEntete;
    private String notePied;
    private Acheteur acheteur;
    private Vendeur vendeur;
    private List<LigneFacture> lignes;
    private String numero;
    private LocalDate dateEmission;
    private LocalDate echeance;
    private DocumentDepose pdf;
    private SuiviZeendoc zeendoc;
    private final Instant creeeLe;
    private final Auteur creePar;
    private final long version;

    /** Tous les champs, tels qu'enregistrés : voir {@link #demandee} et {@link #reconstituer}. */
    public record Donnees(Long id, SensPiece sens, EtatPiece etat, Origine origine, LocalDate dateOperation,
            String libellePeriode, ConditionPaiement condition, String bonDeCommande, String noteEntete,
            String notePied, Acheteur acheteur, Vendeur vendeur, List<LigneFacture> lignes, String numero,
            LocalDate dateEmission, LocalDate echeance, DocumentDepose pdf, SuiviZeendoc zeendoc, Instant creeeLe,
            Auteur creePar, long version) {
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
        this.zeendoc = d.zeendoc();
        this.creeeLe = d.creeeLe();
        this.creePar = d.creePar();
        this.version = d.version();
    }

    /**
     * RG-FAC-01 : le brouillon de facture d'une demande, avec son origine, son acheteur et ses lignes résolues,
     * numérotées et marquées reçues de la source (RG-FAC-17).
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
                libellePeriode, condition, bonDeCommande, noteEntete, notePied, acheteur, null,
                ranger(lignes, 1, true), null, null, null, null, null, creeeLe, creePar, 0));
    }

    public static Facture reconstituer(Donnees donnees) {
        return new Facture(donnees);
    }

    public Donnees donnees() {
        return new Donnees(id, sens, etat, origine, dateOperation, libellePeriode, condition, bonDeCommande, noteEntete,
                notePied, acheteur, vendeur, lignes, numero, dateEmission, echeance, pdf, zeendoc, creeeLe, creePar,
                version);
    }

    /** L'en-tête modifiable d'un brouillon (RG-FAC-17) ; la date de l'opération et l'acheteur n'en sont pas. */
    public record EnTete(String libellePeriode, ConditionPaiement condition, String bonDeCommande, String noteEntete,
            String notePied) {
    }

    /**
     * RG-FAC-17 : l'en-tête et les lignes d'un brouillon, tels que saisis. Une ligne existante est désignée par
     * son numéro ; une ligne nouvelle a le numéro {@code 0} et reçoit le suivant. Une ligne reçue de la source
     * reste présente, dans son groupe, avec son article, sa quantité, son unité et son prix : seules sa
     * désignation et sa position changent. Les autres lignes sont libres.
     *
     * @param versionConnue la version sur laquelle la saisie a été faite
     * @throws ModificationConcurrente si le brouillon a changé depuis
     */
    public void modifier(EnTete enTete, List<LigneFacture> saisies, long versionConnue) {
        exigerBrouillon();
        if (versionConnue != version) {
            throw new ModificationConcurrente(null, designation() + " a été modifié entre-temps par quelqu'un "
                    + "d'autre : rechargez l'écran, puis recommencez.");
        }
        if (enTete.condition() == null) {
            throw new RegleEnfreinte("RG-FAC-13", "Une facture a une condition de paiement.");
        }
        Map<Integer, Placee> avant = placer(lignes);
        Map<Integer, Placee> apres = new HashMap<>();
        for (Placee p : placees(saisies)) {
            if (p.ligne().numero() == 0) {
                continue;
            }
            Placee ancienne = avant.get(p.ligne().numero());
            if (ancienne == null || ancienne.ligne().getClass() != p.ligne().getClass()) {
                throw new RegleEnfreinte("RG-FAC-17", "La ligne n° " + p.ligne().numero() + " n'est pas une ligne "
                        + "de ce brouillon.");
            }
            if (apres.put(p.ligne().numero(), p) != null) {
                throw new RegleEnfreinte("RG-FAC-17", "La ligne « " + p.ligne().designation() + " » figure deux fois.");
            }
            if (ancienne.ligne() instanceof LigneArticle a && p.ligne() instanceof LigneArticle b
                    && !(a.article().equals(b.article()) && a.tva().equals(b.tva()) && a.nature() == b.nature()
                            && Objects.equals(a.codeComptable(), b.codeComptable()))) {
                throw new RegleEnfreinte("RG-FAC-17", "La ligne « " + a.designation() + " » garde son article et sa "
                        + "TVA : pour en changer, retirez-la et ajoutez l'autre article.");
            }
        }
        for (Placee ancienne : avant.values()) {
            if (ancienne.ligne().recue()) {
                verrouillee(ancienne, apres.get(ancienne.ligne().numero()));
            }
        }
        int suivant = avant.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        List<LigneFacture> rangees = new ArrayList<>();
        for (LigneFacture saisie : saisies) {
            LigneFacture rangee = ranger(saisie, avant, suivant);
            suivant += nouvelles(saisie);
            rangees.add(rangee);
        }
        libellePeriode = Textes.facultatif(enTete.libellePeriode());
        condition = enTete.condition();
        bonDeCommande = Textes.facultatif(enTete.bonDeCommande());
        noteEntete = Textes.facultatif(enTete.noteEntete());
        notePied = Textes.facultatif(enTete.notePied());
        lignes = List.copyOf(rangees);
    }

    /** RG-FAC-17 : une ligne reçue reste, dans son groupe, avec son article, sa quantité, son unité et son prix. */
    private static void verrouillee(Placee ancienne, Placee nouvelle) {
        String nom = "La ligne « " + ancienne.ligne().designation() + " », reçue de la source,";
        if (nouvelle == null) {
            throw new RegleEnfreinte("RG-FAC-17", nom + " ne se retire pas : pour la retirer, corrigez la source, "
                    + "puis abandonnez le brouillon et refacturez.");
        }
        if (ancienne.groupe() != nouvelle.groupe()) {
            throw new RegleEnfreinte("RG-FAC-17", nom + " reste dans son groupe.");
        }
        if (ancienne.ligne() instanceof LigneArticle a && nouvelle.ligne() instanceof LigneArticle b
                && !(a.quantite().compareTo(b.quantite()) == 0 && a.prixUnitaire().compareTo(b.prixUnitaire()) == 0
                        && Objects.equals(a.unite(), b.unite()))) {
            throw new RegleEnfreinte("RG-FAC-17", nom + " garde sa quantité, son unité et son prix : seule sa "
                    + "désignation se modifie.");
        }
    }

    /** Une ligne et le numéro de son groupe ({@code 0} hors groupe). */
    private record Placee(LigneFacture ligne, int groupe) {
    }

    private static List<Placee> placees(List<LigneFacture> lignes) {
        List<Placee> placees = new ArrayList<>();
        for (LigneFacture ligne : lignes) {
            placees.add(new Placee(ligne, 0));
            if (ligne instanceof LigneGroupe g) {
                g.lignes().forEach(l -> placees.add(new Placee(l, g.numero())));
            }
        }
        return placees;
    }

    private static Map<Integer, Placee> placer(List<LigneFacture> lignes) {
        Map<Integer, Placee> parNumero = new HashMap<>();
        placees(lignes).forEach(p -> parNumero.put(p.ligne().numero(), p));
        return parNumero;
    }

    /** Une ligne saisie : existante, elle garde son numéro et sa provenance ; nouvelle, elle prend le suivant. */
    private static LigneFacture ranger(LigneFacture saisie, Map<Integer, Placee> avant, int suivant) {
        int[] numero = {suivant};
        LigneFacture rangee = rangerUne(saisie, avant, numero);
        if (rangee instanceof LigneGroupe g) {
            return g.avecLignes(g.lignes().stream().map(l -> rangerUne(l, avant, numero)).toList());
        }
        return rangee;
    }

    private static LigneFacture rangerUne(LigneFacture saisie, Map<Integer, Placee> avant, int[] suivant) {
        if (saisie.numero() == 0) {
            return saisie.rangee(suivant[0]++, false);
        }
        return saisie.rangee(saisie.numero(), avant.get(saisie.numero()).ligne().recue());
    }

    /** Le nombre de lignes nouvelles de cette saisie, groupe compris. */
    private static int nouvelles(LigneFacture saisie) {
        int n = saisie.numero() == 0 ? 1 : 0;
        if (saisie instanceof LigneGroupe g) {
            n += (int) g.lignes().stream().filter(l -> l.numero() == 0).count();
        }
        return n;
    }

    /** Les lignes d'une demande, numérotées dans l'ordre d'impression à partir de {@code premier}. */
    private static List<LigneFacture> ranger(List<LigneFacture> lignes, int premier, boolean recues) {
        int[] numero = {premier};
        return lignes.stream().map(l -> {
            LigneFacture rangee = l.rangee(numero[0]++, recues);
            if (rangee instanceof LigneGroupe g) {
                return (LigneFacture) g.avecLignes(g.lignes().stream()
                        .map(e -> e.rangee(numero[0]++, recues)).toList());
            }
            return rangee;
        }).toList();
    }

    /** La ligne de ce numéro, groupes dépliés. */
    public Optional<LigneFacture> ligne(int numero) {
        return placees(lignes).stream().map(Placee::ligne).filter(l -> l.numero() == numero).findFirst();
    }

    /** RG-FAC-14, 18 : seul un brouillon s'abandonne ; une pièce émise se corrige par avoir. */
    public void exigerAbandonnable() {
        exigerBrouillon();
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

    /**
     * RG-FAC-32 : seule une pièce émise se transmet à Zeendoc, et une seule fois : un rejeu ne crée jamais de second
     * document.
     */
    public void transmettre(Instant le) {
        if (etat != EtatPiece.EMISE) {
            throw new RegleEnfreinte("RG-FAC-32", designation() + " n'est pas émise : elle ne se transmet pas à "
                    + "Zeendoc.");
        }
        if (zeendoc != null) {
            throw new RegleEnfreinte("RG-FAC-32", designation() + " est déjà transmise à Zeendoc.");
        }
        zeendoc = SuiviZeendoc.transmise(le);
    }

    /** RG-FAC-33 : le règlement relevé dans Zeendoc, d'une pièce transmise et non encore payée. */
    public void enregistrerReglement(LocalDate le) {
        if (zeendoc == null) {
            throw new RegleEnfreinte("RG-FAC-33", designation() + " n'est pas transmise à Zeendoc : son règlement "
                    + "ne s'y relève pas.");
        }
        if (zeendoc.etat() == EtatZeendoc.PAYEE) {
            throw new RegleEnfreinte("RG-FAC-33", designation() + " est déjà payée.");
        }
        zeendoc = zeendoc.payee(le);
    }

    /** Les lignes article, groupes dépliés, dans l'ordre d'impression. */
    public List<LigneArticle> lignesArticle() {
        List<LigneArticle> articles = new ArrayList<>();
        for (LigneFacture ligne : lignes) {
            switch (ligne) {
                case LigneArticle a -> articles.add(a);
                case LigneGroupe g -> g.lignes().stream().filter(LigneArticle.class::isInstance)
                        .map(LigneArticle.class::cast).forEach(articles::add);
                case LigneInformative i -> {
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
        return LigneTva.recapituler(
                lignesArticle().stream().map(a -> new LigneTva.Montant(a.tva(), a.montantHt())).toList());
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

    /** Vide tant que la pièce n'est pas transmise (RG-FAC-32). */
    public Optional<SuiviZeendoc> zeendoc() {
        return Optional.ofNullable(zeendoc);
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
