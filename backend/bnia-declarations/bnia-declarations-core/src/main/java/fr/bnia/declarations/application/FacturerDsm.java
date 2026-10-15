package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.application.Facturation.DemandeFactureDsm;
import fr.bnia.declarations.application.Facturation.Groupe;
import fr.bnia.declarations.application.Facturation.LigneArticle;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.GrilleCotisation;
import fr.bnia.declarations.domain.PartCotisation;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.PolitiqueCotisation;
import fr.bnia.declarations.domain.PolitiqueCotisation.Detail;
import fr.bnia.declarations.domain.PolitiqueCotisation.LigneDetail;
import fr.bnia.declarations.domain.TauxCotisation;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.AcheteurVue;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Facturer une DSM, ou une sélection de DSM (UC-DEC-10, révisé par la spec 08 §3.13) : une demande de facture
 * par DSM, envoyée à {@code facturation} par le port {@link Facturation}. RG-DEC-43.
 */
@UseCase
public class FacturerDsm {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DsmRepository dsms;
    private final FacturesDsm factures;
    private final GrilleCotisationRepository grilles;
    private final Facturation facturation;
    private final OperateursLecture operateurs;
    private final Autorisations autorisations;
    private final Journaliste journaliste;

    /** La facture demandée d'une DSM : l'identifiant de son brouillon chez {@code facturation}. */
    public record DsmFacturee(long dsm, long brouillon) {
    }

    public FacturerDsm(DsmRepository dsms, FacturesDsm factures, GrilleCotisationRepository grilles,
            Facturation facturation, JournalDeclarations journal, OperateursLecture operateurs, Clock horloge) {
        this.dsms = dsms;
        this.factures = factures;
        this.grilles = grilles;
        this.facturation = facturation;
        this.operateurs = operateurs;
        this.autorisations = new Autorisations(operateurs);
        this.journaliste = new Journaliste(journal, operateurs, horloge);
    }

    /** RG-DEC-43 : la DSM de l'opérateur, depuis son écran. */
    public DsmFacturee facturer(Acteur acteur, long operateur, long id) {
        autorisations.exigerFacturation(acteur);
        DeclarationStocksMouvements dsm = dsms.parId(id).filter(d -> d.operateur() == operateur)
                .orElseThrow(() -> new ElementIntrouvable("DSM introuvable : n° " + id + "."));
        return demander(acteur, List.of(dsm)).getFirst();
    }

    /**
     * RG-DEC-43 : les DSM sélectionnées, toutes ou aucune ; le refus nomme la DSM en cause. Chacune est validée,
     * sans facture vivante (ni brouillon demandé ni facture émise), d'un opérateur facturable (RG-OPE-02), et sa
     * cotisation est calculable et non nulle. Journal : « Facturation demandée ».
     */
    public List<DsmFacturee> facturer(Acteur acteur, List<Long> ids) {
        autorisations.exigerFacturation(acteur);
        if (ids.isEmpty()) {
            throw new RegleEnfreinte("RG-DEC-43", "Aucune DSM à facturer.");
        }
        List<DeclarationStocksMouvements> selection = new ArrayList<>();
        for (long id : new LinkedHashSet<>(ids)) {
            selection.add(dsms.parId(id).orElseThrow(() -> new ElementIntrouvable("DSM introuvable : n° " + id + ".")));
        }
        return demander(acteur, selection);
    }

    private List<DsmFacturee> demander(Acteur acteur, List<DeclarationStocksMouvements> selection) {
        GrilleCotisation grille = grilles.charger();
        List<DemandeFactureDsm> demandes = selection.stream().map(d -> demande(d, grille)).toList();
        List<Long> brouillons = facturation.demander(acteur, demandes);
        List<DsmFacturee> facturees = new ArrayList<>();
        for (int i = 0; i < selection.size(); i++) {
            DeclarationStocksMouvements dsm = selection.get(i);
            long brouillon = brouillons.get(i);
            factures.inserer(FactureDsm.brouillon(dsm.id(), brouillon));
            journaliste.noter(acteur, dsm, ActionJournalDeclarations.FACTURATION_DEMANDEE, null);
            facturees.add(new DsmFacturee(dsm.id(), brouillon));
        }
        return facturees;
    }

    /** Spec 08 §3.13 : la demande d'une DSM, après les contrôles de RG-DEC-43. */
    private DemandeFactureDsm demande(DeclarationStocksMouvements dsm, GrilleCotisation grille) {
        AcheteurVue acheteur = operateurs.acheteur(dsm.operateur())
                .orElseThrow(() -> new ElementIntrouvable("Opérateur introuvable : n° " + dsm.operateur() + "."));
        String designation = acheteur.raisonSociale() + ", DSM " + dsm.periode().libelle();
        if (dsm.etat() != EtatDsm.VALIDEE) {
            throw refus(designation, "elle n'est pas validée.");
        }
        List<FactureDsm> documents = factures.deLaDsm(dsm.id());
        if (documents.stream().anyMatch(f -> f.type() == FactureDsm.Type.BROUILLON)) {
            throw refus(designation, "sa facturation est déjà demandée.");
        }
        if (documents.stream().anyMatch(f -> f.type() == FactureDsm.Type.FACTURE)) {
            throw refus(designation, "elle est déjà facturée.");
        }
        if (!acheteur.facturable()) {
            throw refus(designation, acheteur.raisonSociale() + " n'est pas facturable ("
                    + String.join(", ", acheteur.manquePourFacturer()) + " manquant).");
        }
        Detail detail = PolitiqueCotisation.calculer(dsm.type(), dsm.periode().fin(), dsm::total, grille)
                .orElseThrow(() -> refus(designation, "aucun taux CVO n'est en vigueur au "
                        + dsm.periode().fin().format(DATE) + "."));
        if (detail.lignes().isEmpty()) {
            throw refus(designation, "sa cotisation est nulle : rien à facturer.");
        }
        PeriodeDeclaration periode = dsm.periode();
        boolean mensuelle = periode.debut().getMonth() == periode.fin().getMonth()
                && periode.debut().getYear() == periode.fin().getYear();
        String libelle = periode.libelle();
        String de = !mensuelle ? "DSM " : "aeiou".indexOf(libelle.charAt(0)) >= 0 ? "DSM d'" : "DSM de ";
        return new DemandeFactureDsm(dsm.id(), dsm.operateur(), acheteur.code(), "DSM " + libelle, de + libelle,
                periode.fin(), 30, true, groupes(detail), notePied(detail.taux().taux()));
    }

    /** Un groupe par famille de lignes de cotisation, deux lignes article par ligne non nulle (H-FAC-1, 5). */
    private static List<Groupe> groupes(Detail detail) {
        Map<String, List<LigneArticle>> parFamille = new LinkedHashMap<>();
        for (LigneDetail ligne : detail.lignes()) {
            List<LigneArticle> lignes = parFamille.computeIfAbsent(ligne.ligne().famille(), f -> new ArrayList<>());
            for (PartCotisation part : PartCotisation.values()) {
                lignes.add(new LigneArticle(part.code(ligne.ligne()), ligne.hectolitres()));
            }
        }
        return parFamille.entrySet().stream().map(e -> new Groupe(e.getKey(), e.getValue())).toList();
    }

    /** H-FAC-6 : la note de pied de la facture d'exemple, au taux appliqué. */
    private static String notePied(TauxCotisation taux) {
        DecimalFormat format = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.FRANCE));
        BigDecimal assujettie = BigDecimal.valueOf(100).subtract(taux.pourcentageExonere());
        return "Cotisation interprofessionnelle de " + format.format(taux.montant()) + " €/hlAP, due en application"
                + " des accords interprofessionnels étendus par l'arrêté du 11 janvier 2024. " + format.format(assujettie)
                + " % de son montant sont assujettis à la TVA ; " + format.format(taux.pourcentageExonere())
                + " % ne le sont pas.";
    }

    private static RegleEnfreinte refus(String designation, String motif) {
        return new RegleEnfreinte("RG-DEC-43", designation + " : " + motif);
    }
}
