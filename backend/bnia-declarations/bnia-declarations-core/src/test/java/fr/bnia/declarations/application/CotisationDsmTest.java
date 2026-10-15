package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_BNIA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.CotisationDsm.Calculee;
import fr.bnia.declarations.application.CotisationDsm.Facturee;
import fr.bnia.declarations.application.CotisationDsm.NonCalculable;
import fr.bnia.declarations.application.GererGrilleCotisation.SaisieTauxCvo;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.SaisieDsm;
import fr.bnia.declarations.domain.SaisieLigneDsm;
import fr.bnia.declarations.domain.partage.LigneCotisation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Grille CVO, cotisation d'une DSM, projection des factures et lecture publiée (spec 05, lot 4). */
class CotisationDsmTest {

    private static final FiltreDsm TOUT = new FiltreDsm(Optional.empty(), Optional.empty(), Optional.empty(),
            Set.of());

    private final Monde monde = new Monde();
    private final GererDsm dsms = monde.gererDsm();
    private final GererGrilleCotisation grille = monde.gererGrille();
    private long id;

    private static SaisieTauxCvo taux(String montant, String pourcentage, LocalDate debut) {
        return new SaisieTauxCvo(new BigDecimal(montant), new BigDecimal(pourcentage), debut);
    }

    /** La DSM de juin du négociant : 839,11 L en France (17, 21) et 2 254,15 L à l'export (22, 23). */
    @BeforeEach
    void dsmDeJuin() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN, GererPointDeDepartTest.STOCK);
        VueDsm vue = dsms.initier(AGENT_NEGOCIANT, NEGOCIANT);
        id = vue.dsm().id();
        List<SaisieLigneDsm> lignes = new ArrayList<>();
        List<LigneDsm> existantes = vue.dsm().lignes();
        for (int i = 0; i < existantes.size(); i++) {
            Map<ColonneDsm, VolumeAlcoolPur> volumes = i == 0
                    ? Map.of(ColonneDsm.C17, VolumeAlcoolPur.litres("719.11"), ColonneDsm.C21,
                            VolumeAlcoolPur.litres("120.00"), ColonneDsm.C22, VolumeAlcoolPur.litres("954.15"),
                            ColonneDsm.C23, VolumeAlcoolPur.litres("1300.00"))
                    : Map.of();
            lignes.add(telle(existantes.get(i), volumes));
        }
        dsms.enregistrer(AGENT_NEGOCIANT, NEGOCIANT, id, new SaisieDsm(null, lignes), vue.dsm().version());
    }

    private static SaisieLigneDsm telle(LigneDsm l, Map<ColonneDsm, VolumeAlcoolPur> volumes) {
        return new SaisieLigneDsm(l.id(), l.identite().cle(),
                l.identite().millesime() == null ? null : l.identite().millesime().annee(),
                l.identite().denomination() == null ? null : l.identite().denomination().texte(),
                l.identite().compte().toString(), volumes);
    }

    private static void refuse(ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(AccesRefuse.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo("RG-OPE-31");
    }

    @Test
    @DisplayName("RG-DEC-30 : le BNIA gère la grille (Paramétrage) ; l'observateur du BNIA la lit ; l'opérateur ne la lit pas")
    void rg_dec_30_droits() {
        assertThat(grille.ajouter(AGENT_BNIA, taux("55", "20", LocalDate.of(2021, 1, 1)))).hasSize(1);
        assertThat(grille.ajouter(AGENT_BNIA, taux("60", "20", LocalDate.of(2024, 1, 1))))
                .extracting(t -> t.fin().orElse(null)).containsExactly(LocalDate.of(2023, 12, 31), null);
        assertThat(grille.consulter(OBSERVATEUR_BNIA)).hasSize(2);
        refuse(() -> grille.ajouter(OBSERVATEUR_BNIA, taux("65", "20", LocalDate.of(2027, 1, 1))));
        refuse(() -> grille.ajouter(AGENT_NEGOCIANT, taux("65", "20", LocalDate.of(2027, 1, 1))));
        refuse(() -> grille.consulter(AGENT_NEGOCIANT));
        refuse(() -> grille.modifier(OBSERVATEUR_BNIA, LocalDate.of(2024, 1, 1), taux("65", "20", LocalDate.of(2024, 1, 1))));
        refuse(() -> grille.supprimer(OBSERVATEUR_BNIA, LocalDate.of(2024, 1, 1)));

        assertThat(grille.modifier(AGENT_BNIA, LocalDate.of(2024, 1, 1), taux("62", "20", LocalDate.of(2024, 7, 1))))
                .extracting(t -> t.fin().orElse(null)).containsExactly(LocalDate.of(2024, 6, 30), null);
        assertThat(grille.supprimer(AGENT_BNIA, LocalDate.of(2021, 1, 1))).hasSize(1);
    }

    @Test
    @DisplayName("RG-DEC-32 : sans taux à la fin de la période, la cotisation est non calculable, sans total HT")
    void rg_dec_32_non_calculable() {
        grille.ajouter(AGENT_BNIA, taux("60", "20", LocalDate.of(2026, 7, 1)));

        CotisationDsm cotisation = dsms.consulter(AGENT_NEGOCIANT, NEGOCIANT, id).cotisation();

        assertThat(cotisation).isEqualTo(new NonCalculable(LocalDate.of(2026, 6, 30)));
        assertThat(dsms.lister(AGENT_NEGOCIANT, NEGOCIANT).getFirst().totalHt()).isEmpty();
    }

    @Test
    @DisplayName("RG-DEC-34 : calculée à la volée, TVA indicative sur la part assujettie, TTC ; total HT au listing")
    void rg_dec_34_calculee() {
        grille.ajouter(AGENT_BNIA, taux("60", "20", LocalDate.of(2024, 1, 1)));

        Calculee cotisation = (Calculee) dsms.consulter(AGENT_NEGOCIANT, NEGOCIANT, id).cotisation();

        assertThat(cotisation.detail().lignes()).extracting(l -> l.ligne())
                .containsExactly(LigneCotisation.SORTIES_FRANCE, LigneCotisation.SORTIES_EXPORT);
        assertThat(cotisation.detail().totalHt()).isEqualByComparingTo("1855.95");
        assertThat(cotisation.tva().orElseThrow().montant()).isEqualByComparingTo("296.95");
        assertThat(cotisation.ttc()).contains(new BigDecimal("2152.90"));
        assertThat(dsms.lister(AGENT_NEGOCIANT, NEGOCIANT).getFirst().totalHt()).contains(new BigDecimal("1855.95"));
        assertThat(monde.listerDsm().lister(AGENT_BNIA, TOUT).getFirst().totalHt())
                .contains(new BigDecimal("1855.95"));
    }

    @Test
    @DisplayName("RG-DEC-34 : sans taux pour la classe de TVA de la CVO, ni TVA ni TTC ; le HT reste")
    void rg_dec_34_tva_indisponible() {
        grille.ajouter(AGENT_BNIA, taux("60", "20", LocalDate.of(2024, 1, 1)));
        GererDsm sansTva = new GererDsm(monde.dsms, monde.points, monde.dsms, monde.journal, monde.commentaires,
                monde.pieces, monde.stockage, monde.classeurs, monde.grilles, monde.factures, monde.evenements, monde.operateurs,
                monde.referentiels, new ParametresDeclarations("INCONNUE"), Monde.HORLOGE);

        Calculee cotisation = (Calculee) sansTva.consulter(AGENT_NEGOCIANT, NEGOCIANT, id).cotisation();

        assertThat(cotisation.tva()).isEmpty();
        assertThat(cotisation.ttc()).isEmpty();
        assertThat(cotisation.detail().totalHt()).isEqualByComparingTo("1855.95");
    }

    @Test
    @DisplayName("RG-DEC-34 : facturée, la vue montre factures et avoirs ; un même numéro n'est reçu qu'une fois ; le listing la marque")
    void rg_dec_34_facturee() {
        grille.ajouter(AGENT_BNIA, taux("60", "20", LocalDate.of(2024, 1, 1)));
        EnregistrerFactures factures = monde.enregistrerFactures();
        monde.factures.inserer(FactureDsm.brouillon(id, 41));
        EnregistrerFactures.PieceRecue facture = new EnregistrerFactures.PieceRecue(id, 41, "2026000700",
                LocalDate.of(2026, 7, 15), new BigDecimal("1855.95"), new BigDecimal("296.95"),
                new BigDecimal("2152.90"), AGENT_BNIA);

        factures.factureEmise(facture);
        factures.factureEmise(facture);
        factures.avoirEmis(new EnregistrerFactures.PieceRecue(id, 42, "2026000712", LocalDate.of(2026, 8, 1),
                new BigDecimal("100.00"), new BigDecimal("20.00"), new BigDecimal("120.00"), AGENT_BNIA));

        Facturee cotisation = (Facturee) dsms.consulter(AGENT_NEGOCIANT, NEGOCIANT, id).cotisation();
        assertThat(cotisation.documents()).extracting(FactureDsm::numero).containsExactly("2026000700", "2026000712");
        assertThat(cotisation.totalHt()).isEqualByComparingTo("1755.95");
        ListerDsm.DsmListee listee = monde.listerDsm().lister(AGENT_BNIA, TOUT).getFirst();
        assertThat(listee.dsm().orElseThrow().facturee()).isTrue();
        assertThat(listee.dsm().orElseThrow().facturationDemandee()).as("brouillon remplacé par la facture").isFalse();
        assertThat(listee.totalHt()).contains(new BigDecimal("1755.95"));
        assertThat(monde.journal.deLaDsm(id)).filteredOn(e -> e.action() == ActionJournalDeclarations.FACTURE_EMISE)
                .singleElement().satisfies(e -> assertThat(e.detail()).isEqualTo("2026000700"));
    }

    @Test
    @DisplayName("RG-DEC-34 : le brouillon abandonné dans facturation quitte la projection ; la DSM redevient à facturer")
    void rg_dec_34_brouillon_abandonne() {
        grille.ajouter(AGENT_BNIA, taux("60", "20", LocalDate.of(2024, 1, 1)));
        monde.factures.inserer(FactureDsm.brouillon(id, 41));

        monde.enregistrerFactures().factureAbandonnee(id, 41, AGENT_BNIA);

        ListerDsm.DsmListee listee = monde.listerDsm().lister(AGENT_BNIA, TOUT).getFirst();
        assertThat(listee.dsm().orElseThrow().facturationDemandee()).isFalse();
        assertThat(listee.dsm().orElseThrow().facturee()).isFalse();
        assertThat(monde.journal.deLaDsm(id)).extracting(e -> e.action())
                .contains(ActionJournalDeclarations.FACTURATION_ABANDONNEE);
    }

    @Test
    @DisplayName("Spec 08 §3.13 : huit articles de cotisation, prix du taux CVO à la date, part assujettie à la TVA de la CVO, l'autre exonérée")
    void rg_dec_33_articles_de_cotisation() {
        grille.ajouter(AGENT_BNIA, taux("55", "20", LocalDate.of(2021, 1, 1)));
        grille.ajouter(AGENT_BNIA, taux("60", "20", LocalDate.of(2024, 1, 1)));
        ArticlesCotisation catalogue = monde.articlesCotisation();

        assertThat(catalogue.articles(LocalDate.of(2026, 7, 31))).hasSize(8).first().satisfies(a -> {
            assertThat(a.code()).isEqualTo("CVO-SORTIES_FRANCE-S");
            assertThat(a.prixUnitaire()).isEqualByComparingTo("48.0000");
            assertThat(a.tauxTva()).contains(new BigDecimal("20.00"));
            assertThat(a.actif()).isTrue();
        });
        assertThat(catalogue.article("CVO-SORTIES_EXPORT-E", LocalDate.of(2023, 6, 30)).orElseThrow())
                .satisfies(a -> {
                    assertThat(a.prixUnitaire()).isEqualByComparingTo("11.0000");
                    assertThat(a.tauxTva()).isEmpty();
                    assertThat(a.designation()).isEqualTo("Cotisation Sorties Export, part non assujettie");
                });
        assertThat(catalogue.article("CVO-SORTIES_FRANCE-S", LocalDate.of(2020, 6, 30)).orElseThrow().actif())
                .as("aucun taux CVO en 2020").isFalse();
        assertThat(catalogue.article("CVO-INCONNU", LocalDate.of(2026, 7, 31))).isEmpty();
    }
}
