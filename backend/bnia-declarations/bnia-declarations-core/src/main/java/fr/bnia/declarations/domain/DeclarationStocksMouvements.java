package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Déclaration de stocks et mouvements d'un opérateur pour une période (agrégat, spec 05 §3.2) : au format
 * unique, 5 onglets et 26 colonnes. L'opérateur l'initie et la remplit ; elle ne porte ni montant de
 * cotisation ni état de facturation (analyse §3).
 */
public final class DeclarationStocksMouvements {

    private final Long id;
    private final long operateur;
    private final TypeDeclarant type;
    private final PeriodeDeclaration periode;
    private EtatDsm etat;
    private LocalDate dateDia;
    private List<LigneDsm> lignes;
    private Instant envoyeeLe;
    private Instant valideeLe;
    private Auteur valideePar;
    private final long version;

    private DeclarationStocksMouvements(Long id, long operateur, TypeDeclarant type, PeriodeDeclaration periode,
            EtatDsm etat, LocalDate dateDia, List<LigneDsm> lignes, Instant envoyeeLe, Instant valideeLe,
            Auteur valideePar, long version) {
        this.id = id;
        this.operateur = operateur;
        this.type = Objects.requireNonNull(type, "type");
        this.periode = Objects.requireNonNull(periode, "periode");
        this.etat = Objects.requireNonNull(etat, "etat");
        this.dateDia = dateDia;
        this.lignes = List.copyOf(lignes);
        this.envoyeeLe = envoyeeLe;
        this.valideeLe = valideeLe;
        this.valideePar = valideePar;
        this.version = version;
    }

    /**
     * RG-DEC-02, 13, 14 : un producteur ou un négociant au statut {@code OUI} initie sa DSM, en brouillon ;
     * le type de déclarant est lu maintenant et ne change plus. Ses lignes reprennent le stock de départ
     * (colonne 1, origine {@code REPRISE}) : le stock initial du point de départ, ou le stock de fin de la DSM
     * précédente, vieilli ({@link Vieillissement}).
     */
    public static DeclarationStocksMouvements initier(long operateur, TypeOperateur typeOperateur,
            StatutDeclarationStock statut, PeriodeDeclaration periode, List<LigneStockInitial> stockInitial) {
        TypeDeclarant type = TypeDeclarant.de(typeOperateur);
        if (statut != StatutDeclarationStock.OUI) {
            throw new RegleEnfreinte("RG-DEC-02", "Seul un opérateur au statut de déclaration « Oui » initie une DSM"
                    + " (statut « " + statut.libelle() + " »).");
        }
        List<LigneDsm> lignes = stockInitial.stream().map(l -> LigneDsm.reprise(l.identite(), l.volume())).toList();
        return new DeclarationStocksMouvements(null, operateur, type, periode, EtatDsm.BROUILLON, null, lignes, null,
                null, null, 0);
    }

    public static DeclarationStocksMouvements reconstituer(long id, long operateur, TypeDeclarant type,
            PeriodeDeclaration periode, EtatDsm etat, LocalDate dateDia, List<LigneDsm> lignes, Instant envoyeeLe,
            Instant valideeLe, Auteur valideePar, long version) {
        return new DeclarationStocksMouvements(id, operateur, type, periode, etat, dateDia, lignes, envoyeeLe,
                valideeLe, valideePar, version);
    }

    /**
     * RG-DEC-10 à 13, 15, 16, 19, 21 : remplace la saisie (lignes, volumes, date de DIA). Les anomalies
     * (RG-DEC-18) n'empêchent pas d'enregistrer.
     *
     * @param versionLue {@code null} : pas de contrôle
     */
    public void enregistrer(SaisieDsm saisie, Long versionLue) {
        exigerModifiable();
        Versions.verifier(versionLue, version, "La DSM de " + periode.libelle());
        this.lignes = appliquer(saisie.lignes());
        this.dateDia = saisie.dateDia();
    }

    /** Vue provisoire (décision du 2026-10-13) : la DSM telle qu'elle serait enregistrée, sans la modifier. */
    public DeclarationStocksMouvements apercu(SaisieDsm saisie) {
        exigerModifiable();
        return new DeclarationStocksMouvements(id, operateur, type, periode, etat, saisie.dateDia(),
                appliquer(saisie.lignes()), envoyeeLe, valideeLe, valideePar, version);
    }

    /** RG-DEC-21 : les lignes, les volumes et la date de DIA ne se modifient que quand la DSM est chez l'opérateur. */
    private void exigerModifiable() {
        if (!etat.modifiable()) {
            throw new RegleEnfreinte("RG-DEC-21", "La DSM de " + periode.libelle() + " est « "
                    + etat.libelle().toLowerCase() + " » : elle ne se modifie plus.");
        }
    }

    /** RG-DEC-22 : seul un brouillon se supprime. */
    public void exigerSupprimable() {
        if (etat != EtatDsm.BROUILLON) {
            throw new RegleEnfreinte("RG-DEC-22", "La DSM de " + periode.libelle() + " est « "
                    + etat.libelle().toLowerCase() + " » : seul un brouillon se supprime.");
        }
    }

    /**
     * RG-DEC-20 : l'opérateur envoie sa DSM en brouillon ({@code ENVOYEE}), ou ses compléments
     * ({@code COMPLEMENTS_RECUS}), si elle n'a aucune anomalie de calcul (RG-DEC-18).
     *
     * @return {@code true} pour un renvoi après compléments
     */
    public boolean envoyer(Instant le, Long versionLue) {
        exigerEtat(EtatDsm.BROUILLON, EtatDsm.COMPLEMENTS_DEMANDES, "envoyée");
        Versions.verifier(versionLue, version, "La DSM de " + periode.libelle());
        int nombre = anomalies().size();
        if (nombre > 0) {
            throw new RegleEnfreinte("RG-DEC-20", "La DSM de " + periode.libelle() + " a " + nombre
                    + (nombre == 1 ? " anomalie" : " anomalies") + " : corrigez-la avant de l'envoyer.");
        }
        boolean renvoi = etat == EtatDsm.COMPLEMENTS_DEMANDES;
        this.etat = renvoi ? EtatDsm.COMPLEMENTS_RECUS : EtatDsm.ENVOYEE;
        this.envoyeeLe = Objects.requireNonNull(le, "le");
        return renvoi;
    }

    /** RG-DEC-20 : le BNIA prend en instruction une DSM envoyée. */
    public void prendreEnInstruction(Long versionLue) {
        exigerEtat(EtatDsm.ENVOYEE, null, "prise en instruction");
        Versions.verifier(versionLue, version, "La DSM de " + periode.libelle());
        this.etat = EtatDsm.EN_INSTRUCTION;
    }

    /**
     * RG-DEC-20 : le BNIA demande des compléments sur une DSM en instruction, ou de nouveau après des
     * compléments reçus ; le motif est un commentaire.
     */
    public void demanderComplements(Long versionLue) {
        exigerEtat(EtatDsm.EN_INSTRUCTION, EtatDsm.COMPLEMENTS_RECUS, "renvoyée pour compléments");
        Versions.verifier(versionLue, version, "La DSM de " + periode.libelle());
        this.etat = EtatDsm.COMPLEMENTS_DEMANDES;
    }

    /**
     * RG-DEC-20 : le BNIA valide une DSM en instruction, ou dès ses compléments reçus, sans la reprendre en
     * instruction ; elle est alors figée. RG-DEC-29 : pas tant qu'il manque une pièce.
     *
     * @param manques les pièces manquantes ({@link ControlePieces#manques})
     */
    public void valider(Auteur par, Instant le, Long versionLue, List<Manque> manques) {
        exigerEtat(EtatDsm.EN_INSTRUCTION, EtatDsm.COMPLEMENTS_RECUS, "validée");
        Versions.verifier(versionLue, version, "La DSM de " + periode.libelle());
        if (!manques.isEmpty()) {
            int nombre = manques.size();
            throw new RegleEnfreinte("RG-DEC-29", "La DSM de " + periode.libelle() + " ne peut pas être validée : "
                    + (nombre == 1 ? "une pièce manque. " : nombre + " pièces manquent, dont : ")
                    + manques.getFirst().message());
        }
        this.etat = EtatDsm.VALIDEE;
        this.valideeLe = Objects.requireNonNull(le, "le");
        this.valideePar = Objects.requireNonNull(par, "par");
    }

    /** RG-DEC-27 : une DSM producteur attend une DRM par mois de sa période ; une DSM négociant, aucune. */
    public List<YearMonth> moisAttendus() {
        if (type != TypeDeclarant.PRODUCTEUR) {
            return List.of();
        }
        List<YearMonth> mois = new ArrayList<>();
        for (YearMonth m = YearMonth.from(periode.debut()); !m.isAfter(YearMonth.from(periode.fin())); m = m.plusMonths(1)) {
            mois.add(m);
        }
        return List.copyOf(mois);
    }

    /** RG-DEC-27 : une DRM porte sur un mois de la période d'une DSM producteur. */
    public void exigerMoisDrm(YearMonth mois) {
        if (type != TypeDeclarant.PRODUCTEUR) {
            throw new RegleEnfreinte("RG-DEC-27", "Une DSM de négociant n'a pas de DRM.");
        }
        if (!moisAttendus().contains(mois)) {
            throw new RegleEnfreinte("RG-DEC-27", "Le mois " + mois + " n'est pas dans la période de la DSM ("
                    + periode.libelle() + ").");
        }
    }

    /**
     * RG-DEC-28 : l'opérateur dépose et supprime pièces et DRM quand la DSM est chez lui ({@code BROUILLON},
     * {@code COMPLEMENTS_DEMANDES}) ; le BNIA dans tout état sauf {@code VALIDEE}.
     */
    public void exigerPiecesModifiables(Cote cote) {
        boolean permis = cote == Cote.BNIA ? etat != EtatDsm.VALIDEE : etat.modifiable();
        if (!permis) {
            throw new RegleEnfreinte("RG-DEC-28", "La DSM de " + periode.libelle() + " est « "
                    + etat.libelle(cote).toLowerCase() + " » : ses pièces ne se déposent ni ne se suppriment plus.");
        }
    }

    /** RG-DEC-20 : la transition part de l'un de ces états ; aucune ne sort de {@code VALIDEE}. */
    private void exigerEtat(EtatDsm attendu, EtatDsm autre, String participe) {
        if (etat != attendu && etat != autre) {
            throw new RegleEnfreinte("RG-DEC-20", "La DSM de " + periode.libelle() + " est « "
                    + etat.libelle().toLowerCase() + " » : elle ne peut pas être " + participe + ".");
        }
    }

    /**
     * RG-DEC-08 : la DSM issue d'un point de départ modifié suit son stock initial. Colonne 1 recalculée ;
     * ligne ajoutée au stock initial ajoutée à la DSM (ou reprise, si l'opérateur l'avait déjà ajoutée) ;
     * ligne retirée gardée comme ligne {@code AJOUT} à 0 en colonne 1.
     */
    public void suivrePointDeDepart(List<LigneStockInitial> stockInitial) {
        Map<IdentiteLigne, VolumeAlcoolPur> stock = stockInitial.stream()
                .collect(Collectors.toMap(LigneStockInitial::identite, LigneStockInitial::volume, (a, b) -> a,
                        LinkedHashMap::new));
        List<LigneDsm> suivies = new ArrayList<>();
        for (LigneDsm ligne : lignes) {
            VolumeAlcoolPur volume = stock.remove(ligne.identite());
            if (volume != null) {
                suivies.add(ligne.avecStockDebut(OrigineLigne.REPRISE, volume));
            } else if (ligne.origine() == OrigineLigne.REPRISE) {
                suivies.add(ligne.avecStockDebut(OrigineLigne.AJOUT, VolumeAlcoolPur.ZERO));
            } else {
                suivies.add(ligne);
            }
        }
        stock.forEach((identite, volume) -> suivies.add(LigneDsm.reprise(identite, volume)));
        this.lignes = List.copyOf(suivies);
    }

    private List<LigneDsm> appliquer(List<SaisieLigneDsm> saisies) {
        Map<Long, LigneDsm> existantes = lignes.stream().filter(l -> l.id() != null)
                .collect(Collectors.toMap(LigneDsm::id, Function.identity()));
        Set<Long> gardees = new HashSet<>();
        List<LigneDsm> nouvelles = new ArrayList<>();
        for (SaisieLigneDsm saisie : saisies) {
            IdentiteLigne identite = IdentiteLigne.saisie(saisie.cle(), saisie.millesime(), saisie.denomination(),
                    saisie.compte(), periode.debut());
            Map<ColonneDsm, VolumeAlcoolPur> volumes = mouvements(identite, saisie.volumes());
            if (saisie.id() == null) {
                nouvelles.add(new LigneDsm(null, identite, OrigineLigne.AJOUT, volumes));
                continue;
            }
            LigneDsm existante = existantes.get(saisie.id());
            if (existante == null || !gardees.add(saisie.id())) {
                throw new RegleEnfreinte(null, "La ligne n° " + saisie.id() + " n'est pas une ligne de la DSM de "
                        + periode.libelle() + " : rechargez l'écran.");
            }
            if (existante.origine() == OrigineLigne.REPRISE && !identite.equals(existante.identite())) {
                throw new RegleEnfreinte("RG-DEC-13", "La ligne reprise " + existante.identite().libelle()
                        + " garde son identité : un changement passe par les colonnes 12 et 6.");
            }
            IdentiteLigne gardee = existante.origine() == OrigineLigne.REPRISE ? existante.identite() : identite;
            volumes.put(ColonneDsm.C01, existante.stockDebut());
            nouvelles.add(new LigneDsm(existante.id(), gardee, existante.origine(), volumes));
        }
        lignes.stream()
                .filter(l -> l.origine() == OrigineLigne.REPRISE && !gardees.contains(l.id()))
                .findFirst()
                .ifPresent(l -> {
                    throw new RegleEnfreinte("RG-DEC-13", "La ligne reprise " + l.identite().libelle()
                            + " ne se supprime pas.");
                });
        Unicite.verifier(nouvelles.stream().map(LigneDsm::identite).toList());
        return List.copyOf(nouvelles);
    }

    /** RG-DEC-15, 16, 17 : colonnes 2 à 25, positives ou nulles, vides si grisées. */
    private EnumMap<ColonneDsm, VolumeAlcoolPur> mouvements(IdentiteLigne identite,
            Map<ColonneDsm, VolumeAlcoolPur> saisis) {
        EnumMap<ColonneDsm, VolumeAlcoolPur> volumes = new EnumMap<>(ColonneDsm.class);
        saisis.forEach((colonne, volume) -> {
            if (volume == null || volume.estNul()) {
                return;
            }
            if (colonne.sens() == SensColonne.STOCK_DEBUT || colonne.sens() == SensColonne.STOCK_FIN) {
                throw new RegleEnfreinte("RG-DEC-17", "La " + colonne + " (" + colonne.libelle().toLowerCase()
                        + ") est calculée : elle ne se saisit pas.");
            }
            if (colonne.griseePour(type)) {
                throw new RegleEnfreinte("RG-DEC-16", "La " + colonne + " est grisée pour un producteur : elle reste vide"
                        + " (ligne " + identite.libelle() + ").");
            }
            if (volume.estNegatif()) {
                throw new RegleEnfreinte("RG-DEC-15", "Ligne " + identite.libelle() + ", " + colonne
                        + " : un volume est positif ou nul.");
            }
            volumes.put(colonne, volume);
        });
        return volumes;
    }

    /** RG-DEC-17 : total d'une colonne sur toutes les lignes ; la colonne 26 totalise les stocks de fin. */
    public VolumeAlcoolPur total(ColonneDsm colonne) {
        return lignes.stream().map(l -> l.volume(colonne)).reduce(VolumeAlcoolPur.ZERO, VolumeAlcoolPur::plus);
    }

    /** RG-DEC-18. */
    public List<Anomalie> anomalies() {
        return ControleCoherence.anomalies(this);
    }

    /** Après l'insertion : les identifiants attribués par la persistance, dans l'ordre des lignes. */
    public DeclarationStocksMouvements avecIds(long nouvelId, List<Long> idsLignes) {
        List<LigneDsm> numerotees = new ArrayList<>();
        for (int i = 0; i < lignes.size(); i++) {
            numerotees.add(lignes.get(i).avecId(idsLignes.get(i)));
        }
        return new DeclarationStocksMouvements(nouvelId, operateur, type, periode, etat, dateDia, numerotees,
                envoyeeLe, valideeLe, valideePar, version);
    }

    public Long id() {
        return id;
    }

    public long operateur() {
        return operateur;
    }

    public TypeDeclarant type() {
        return type;
    }

    public PeriodeDeclaration periode() {
        return periode;
    }

    public EtatDsm etat() {
        return etat;
    }

    public LocalDate dateDia() {
        return dateDia;
    }

    public List<LigneDsm> lignes() {
        return lignes;
    }

    public Instant envoyeeLe() {
        return envoyeeLe;
    }

    public Instant valideeLe() {
        return valideeLe;
    }

    public Auteur valideePar() {
        return valideePar;
    }

    public long version() {
        return version;
    }
}
