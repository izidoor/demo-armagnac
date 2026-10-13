package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.Exemples.BNIA;
import static fr.bnia.declarations.domain.Exemples.MAINTENANT;
import static fr.bnia.declarations.domain.Exemples.dsmDeJuin;
import static fr.bnia.declarations.domain.Exemples.l;
import static fr.bnia.declarations.domain.Exemples.saisie;
import static fr.bnia.declarations.domain.Exemples.stock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.DocumentDepose;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Justificatifs, DRM et manques (spec 05 §3.2.2, §3.2.3, lot 3). */
class PiecesTest {

    private static final DocumentDepose PDF = new DocumentDepose(1, "piece.pdf", "application/pdf", 1200);

    private static void enfreint(String regle, ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElseThrow()).isEqualTo(regle);
    }

    private static Justificatif piece(TypeMouvement mouvement, TypePiece type, Long ligne) {
        return Justificatif.deposer(50, ligne, mouvement, type, " DAE-2026-001 ", PDF, BNIA, MAINTENANT);
    }

    private static DeclarationStocksMouvements producteur() {
        return Exemples.dsm(TypeOperateur.PRODUCTEUR,
                PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR, LocalDate.of(2025, 4, 1)),
                List.of(stock(CleConditionnement.MB, 2010, null, "640.25")));
    }

    private static List<Drm> drms(DeclarationStocksMouvements dsm, YearMonth... sauf) {
        List<Drm> drms = new ArrayList<>();
        for (YearMonth mois : dsm.moisAttendus()) {
            if (!List.of(sauf).contains(mois)) {
                drms.add(new Drm(null, 50, mois, PDF, BNIA, MAINTENANT));
            }
        }
        return drms;
    }

    @Test
    @DisplayName("RG-DEC-26 : facture ou DAE pour achats et ventes, avoir ou DAE pour réintégrations, déclaration douanes pour autres fabrications")
    void rg_dec_26_types_compatibles() {
        for (TypeMouvement mouvement : List.of(TypeMouvement.ACHAT, TypeMouvement.VENTE_NEGOCE, TypeMouvement.VENTE_EXPORT)) {
            assertThat(mouvement.pieces()).containsExactlyInAnyOrder(TypePiece.FACTURE, TypePiece.DAE);
        }
        assertThat(TypeMouvement.REINTEGRATION.pieces()).containsExactlyInAnyOrder(TypePiece.AVOIR, TypePiece.DAE);
        assertThat(TypeMouvement.AUTRES_FABRICATIONS.pieces())
                .containsExactly(TypePiece.DECLARATION_FABRICATION_DOUANES);
        enfreint("RG-DEC-26", () -> piece(TypeMouvement.VENTE_EXPORT, TypePiece.AVOIR, null));
        enfreint("RG-DEC-26", () -> piece(TypeMouvement.AUTRES_FABRICATIONS, TypePiece.FACTURE, null));
    }

    @Test
    @DisplayName("RG-DEC-26 : rattaché à la DSM ou à une ligne, référence facultative ; détaché quand sa ligne est supprimée")
    void rg_dec_26_rattachement() {
        Justificatif surDsm = Justificatif.deposer(50, null, TypeMouvement.ACHAT, TypePiece.FACTURE, " ", PDF, BNIA,
                MAINTENANT);
        assertThat(surDsm.ligne()).isNull();
        assertThat(surDsm.reference()).isNull();
        Justificatif surLigne = piece(TypeMouvement.VENTE_EXPORT, TypePiece.DAE, 100L);
        assertThat(surLigne.ligne()).isEqualTo(100L);
        assertThat(surLigne.reference()).isEqualTo("DAE-2026-001");
        surLigne.detacher("NB « XO » 12");
        assertThat(surLigne.ligne()).isNull();
        assertThat(surLigne.ligneSupprimee()).isEqualTo("NB « XO » 12");
    }

    @Test
    @DisplayName("RG-DEC-27 : une DRM par mois de la période d'un producteur, aucune pour un négociant")
    void rg_dec_27_mois_attendus() {
        DeclarationStocksMouvements producteur = producteur();
        assertThat(producteur.moisAttendus()).hasSize(12)
                .startsWith(YearMonth.of(2025, 4)).endsWith(YearMonth.of(2026, 3));
        producteur.exigerMoisDrm(YearMonth.of(2025, 9));
        enfreint("RG-DEC-27", () -> producteur.exigerMoisDrm(YearMonth.of(2026, 4)));
        DeclarationStocksMouvements negociant = dsmDeJuin();
        assertThat(negociant.moisAttendus()).isEmpty();
        enfreint("RG-DEC-27", () -> negociant.exigerMoisDrm(YearMonth.of(2026, 6)));
    }

    @Test
    @DisplayName("RG-DEC-28 : l'opérateur dépose quand la DSM est chez lui ; le BNIA, dans tout état sauf validée")
    void rg_dec_28_etats() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        dsm.exigerPiecesModifiables(Cote.OPERATEUR);
        dsm.exigerPiecesModifiables(Cote.BNIA);
        dsm.envoyer(MAINTENANT, null);
        enfreint("RG-DEC-28", () -> dsm.exigerPiecesModifiables(Cote.OPERATEUR));
        dsm.exigerPiecesModifiables(Cote.BNIA);
        dsm.prendreEnInstruction(null);
        enfreint("RG-DEC-28", () -> dsm.exigerPiecesModifiables(Cote.OPERATEUR));
        dsm.exigerPiecesModifiables(Cote.BNIA);
        dsm.demanderComplements(null);
        dsm.exigerPiecesModifiables(Cote.OPERATEUR);
        dsm.exigerPiecesModifiables(Cote.BNIA);
        dsm.envoyer(MAINTENANT, null);
        enfreint("RG-DEC-28", () -> dsm.exigerPiecesModifiables(Cote.OPERATEUR));
        dsm.exigerPiecesModifiables(Cote.BNIA);
        dsm.valider(BNIA, MAINTENANT, null, List.of());
        enfreint("RG-DEC-28", () -> dsm.exigerPiecesModifiables(Cote.OPERATEUR));
        enfreint("RG-DEC-28", () -> dsm.exigerPiecesModifiables(Cote.BNIA));
    }

    @Test
    @DisplayName("RG-DEC-29 : un type de mouvement non nul sans pièce manque ; la colonne 15 n'appelle rien ; une pièce de ligne couvre son type")
    void rg_dec_29_justificatifs() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        dsm.enregistrer(saisie(dsm, Map.of(
                0, Map.of(ColonneDsm.C15, l("10"), ColonneDsm.C21, l("120")),
                1, Map.of(ColonneDsm.C22, l("40"), ColonneDsm.C23, l("60.50")))), null);

        List<Manque> manques = ControlePieces.manques(dsm, List.of(), List.of());

        assertThat(manques).extracting(Manque::typeMouvement)
                .containsExactly(TypeMouvement.VENTE_NEGOCE, TypeMouvement.VENTE_EXPORT);
        assertThat(manques.get(1).total()).isEqualTo(l("100.50"));
        assertThat(manques.get(1).niveau()).isEqualTo(NiveauAnomalie.DSM);
        assertThat(manques.get(1).message()).isEqualTo("Aucun justificatif pour les ventes à l'exportation (100,50 L,"
                + " colonnes 22, 23, 24, 25).");
        assertThat(ControlePieces.manques(dsm, List.of(piece(TypeMouvement.VENTE_EXPORT, TypePiece.DAE, 101L),
                piece(TypeMouvement.VENTE_NEGOCE, TypePiece.FACTURE, null)), List.of())).isEmpty();
        assertThat(EnumSet.allOf(TypeMouvement.class).stream().flatMap(t -> t.colonnes().stream()))
                .doesNotContain(ColonneDsm.C15);
    }

    @Test
    @DisplayName("RG-DEC-29 : un mois sans DRM manque ; la validation est refusée tant qu'il reste un manque")
    void rg_dec_29_drm_et_validation() {
        DeclarationStocksMouvements dsm = producteur();
        List<Manque> manques = ControlePieces.manques(dsm, List.of(), drms(dsm, YearMonth.of(2025, 11)));
        assertThat(manques).singleElement().satisfies(m -> {
            assertThat(m.type()).isEqualTo(Manque.TypeManque.DRM);
            assertThat(m.mois()).isEqualTo(YearMonth.of(2025, 11));
            assertThat(m.message()).isEqualTo("DRM de novembre 2025 non déposée.");
        });
        dsm.envoyer(MAINTENANT, null);
        dsm.prendreEnInstruction(null);
        enfreint("RG-DEC-29", () -> dsm.valider(BNIA, MAINTENANT, null, manques));
        assertThat(dsm.etat()).isEqualTo(EtatDsm.EN_INSTRUCTION);
        dsm.valider(BNIA, MAINTENANT, null, ControlePieces.manques(dsm, List.of(), drms(dsm)));
        assertThat(dsm.etat()).isEqualTo(EtatDsm.VALIDEE);
    }
}
