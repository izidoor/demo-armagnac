package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Point de départ déclaratif d'un opérateur (agrégat, spec 05 §3.3, analyse UC-DEC-3) : début de sa
 * première période et stock initial, ligne par ligne, fixés par le BNIA. Le plus récent est en vigueur.
 * Il se modifie tant qu'aucune DSM qui commence à son début n'est validée (RG-DEC-08).
 */
public final class PointDeDepartDeclaratif {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Long id;
    private final long operateur;
    private LocalDate debut;
    private List<LigneStockInitial> lignes;
    private Auteur fixePar;
    private Instant fixeLe;
    private final long version;

    private PointDeDepartDeclaratif(Long id, long operateur, LocalDate debut, List<LigneStockInitial> lignes,
            Auteur fixePar, Instant fixeLe, long version) {
        this.id = id;
        this.operateur = operateur;
        this.debut = Objects.requireNonNull(debut, "debut");
        this.lignes = List.copyOf(lignes);
        this.fixePar = fixePar;
        this.fixeLe = fixeLe;
        this.version = version;
    }

    /** RG-DEC-06, 07, 10 à 12, 15 : début selon le type, lignes contrôlées ; une liste vide est permise. */
    public static PointDeDepartDeclaratif fixer(long operateur, TypeDeclarant type, LocalDate debut,
            List<SaisieLigneStock> lignes, Auteur auteur, Instant maintenant) {
        verifierDebut(type, debut);
        return new PointDeDepartDeclaratif(null, operateur, debut, lignes(lignes, debut), auteur, maintenant, 0);
    }

    /**
     * RG-DEC-09 : un nouveau point de départ, quand l'opérateur n'a aucune DSM en cours, à une date postérieure
     * à la fin de sa dernière DSM validée. Le précédent reste consultable.
     *
     * @param dsmEnCours la période de la DSM non validée de l'opérateur, s'il en a une
     * @param derniere   la période de sa dernière DSM validée
     */
    public static PointDeDepartDeclaratif fixerApres(long operateur, TypeDeclarant type, LocalDate debut,
            List<SaisieLigneStock> lignes, Optional<PeriodeDeclaration> dsmEnCours, PeriodeDeclaration derniere,
            Auteur auteur, Instant maintenant) {
        if (dsmEnCours.isPresent()) {
            throw new RegleEnfreinte("RG-DEC-09", "La DSM de " + dsmEnCours.get().libelle()
                    + " est en cours : un nouveau point de départ attend qu'elle soit validée ou supprimée.");
        }
        verifierDebut(type, debut);
        if (!debut.isAfter(derniere.fin())) {
            throw new RegleEnfreinte("RG-DEC-09", "Le nouveau point de départ commence après le "
                    + DATE.format(derniere.fin()) + ", fin de la dernière DSM validée (" + derniere.libelle() + ").");
        }
        return fixer(operateur, type, debut, lignes, auteur, maintenant);
    }

    public static PointDeDepartDeclaratif reconstituer(long id, long operateur, LocalDate debut,
            List<LigneStockInitial> lignes, Auteur fixePar, Instant fixeLe, long version) {
        return new PointDeDepartDeclaratif(id, operateur, debut, lignes, fixePar, fixeLe, version);
    }

    /**
     * RG-DEC-08 : se modifie tant qu'il n'est pas figé ; sa date de début ne change pas tant qu'une DSM est en
     * cours (décision du 2026-10-13 : supprimer d'abord le brouillon).
     *
     * @param fige        une DSM qui commence à son début est validée
     * @param dsmEnCours  la période de la DSM non validée de l'opérateur, s'il en a une
     * @param versionLue  {@code null} : pas de contrôle
     */
    public void modifier(TypeDeclarant type, LocalDate nouveauDebut, List<SaisieLigneStock> saisies, boolean fige,
            Optional<PeriodeDeclaration> dsmEnCours, Auteur auteur, Instant maintenant, Long versionLue) {
        Versions.verifier(versionLue, version, "Le point de départ");
        if (fige) {
            throw new RegleEnfreinte("RG-DEC-08", "Le point de départ du " + DATE.format(debut)
                    + " est figé : la première DSM qui en est issue est validée.");
        }
        if (!nouveauDebut.equals(debut) && dsmEnCours.isPresent()) {
            throw new RegleEnfreinte("RG-DEC-08", "La DSM de " + dsmEnCours.get().libelle()
                    + " est en cours : supprimez-la avant de changer la date de début.");
        }
        verifierDebut(type, nouveauDebut);
        this.lignes = lignes(saisies, nouveauDebut);
        this.debut = nouveauDebut;
        this.fixePar = auteur;
        this.fixeLe = maintenant;
    }

    private static void verifierDebut(TypeDeclarant type, LocalDate debut) {
        if (debut == null) {
            throw new RegleEnfreinte("RG-DEC-06", "La date de début du point de départ est obligatoire.");
        }
        if (!PeriodeDeclaration.debutPermis(type, debut)) {
            throw new RegleEnfreinte("RG-DEC-06", type == TypeDeclarant.PRODUCTEUR
                    ? "Le point de départ d'un producteur est un 1er avril (" + DATE.format(debut) + " saisi)."
                    : "Le point de départ d'un négociant est un premier du mois (" + DATE.format(debut) + " saisi).");
        }
    }

    private static List<LigneStockInitial> lignes(List<SaisieLigneStock> saisies, LocalDate debut) {
        List<LigneStockInitial> lignes = saisies.stream()
                .map(s -> new LigneStockInitial(
                        IdentiteLigne.saisie(s.cle(), s.millesime(), s.denomination(), s.compte(), debut), s.volume()))
                .toList();
        Unicite.verifier(lignes.stream().map(LigneStockInitial::identite).toList());
        return lignes;
    }

    /** La première période qui en est issue (RG-DEC-04). */
    public PeriodeDeclaration premierePeriode(TypeDeclarant type) {
        return PeriodeDeclaration.commencant(type, debut);
    }

    public Long id() {
        return id;
    }

    public long operateur() {
        return operateur;
    }

    public LocalDate debut() {
        return debut;
    }

    public List<LigneStockInitial> lignes() {
        return lignes;
    }

    public Auteur fixePar() {
        return fixePar;
    }

    public Instant fixeLe() {
        return fixeLe;
    }

    public long version() {
        return version;
    }
}
