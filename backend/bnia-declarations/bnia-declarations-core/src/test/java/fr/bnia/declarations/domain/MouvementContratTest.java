package fr.bnia.declarations.domain;

import static fr.bnia.declarations.domain.Exemples.AUJOURDHUI;
import static fr.bnia.declarations.domain.Exemples.MAINTENANT;
import static fr.bnia.declarations.domain.Exemples.PERIODE_JUIN;
import static fr.bnia.declarations.domain.Exemples.dsm;
import static fr.bnia.declarations.domain.Exemples.dsmDeJuin;
import static fr.bnia.declarations.domain.Exemples.l;
import static fr.bnia.declarations.domain.Exemples.sixLignes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import fr.bnia.declarations.domain.partage.RetiraisonValidee;
import fr.bnia.declarations.domain.partage.RetiraisonValidee.LigneRetiree;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Mouvements de contrat dans {@code declarations} (spec 06 RG-CA-21 à 24). */
class MouvementContratTest {

    private static final LocalDate LE_15_JUIN = LocalDate.of(2026, 6, 15);

    /** Retiraison n° 1 du contrat 3 : 7,05 hl AP de MB 2010 et 1,35 hl AP de Hors d'âge 20+, du vendeur 8 à l'acheteur 7. */
    private static RetiraisonValidee retiraison(LocalDate date) {
        return new RetiraisonValidee(3, "CA-2026-0001", 1, date, 8, 7, 10, List.of(
                new LigneRetiree(CleConditionnement.MB, 2010, null, null, l("705.00")),
                new LigneRetiree(CleConditionnement.NB, null, "Hors d'âge", CompteDAgeDsm.VINGT_ET_PLUS, l("135.00"))));
    }

    @Test
    @DisplayName("RG-CA-21 : un achat pour l'acheteur, une vente pour le vendeur ; achat en 2 d'un producteur, en 3 sinon ; vente en 20 en région délimitée, en 21 sinon")
    void rg_ca_21_colonnes() {
        List<MouvementContrat> duProducteur = MouvementContrat.de(retiraison(LE_15_JUIN), TypeOperateur.PRODUCTEUR, true);
        assertThat(duProducteur).extracting(MouvementContrat::operateur, MouvementContrat::sens, MouvementContrat::colonne)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(7L, SensMouvementContrat.ACHAT, ColonneDsm.C02),
                        org.assertj.core.groups.Tuple.tuple(8L, SensMouvementContrat.VENTE, ColonneDsm.C20),
                        org.assertj.core.groups.Tuple.tuple(7L, SensMouvementContrat.ACHAT, ColonneDsm.C02),
                        org.assertj.core.groups.Tuple.tuple(8L, SensMouvementContrat.VENTE, ColonneDsm.C20));
        assertThat(duProducteur.getFirst().identite().compte()).isEqualTo(CompteDAgeDsm.de("15"));
        assertThat(duProducteur.getFirst().autrePartie()).isEqualTo(8);
        assertThat(duProducteur.get(1).autrePartie()).isEqualTo(7);
        assertThat(duProducteur.getFirst().libelle()).isEqualTo("Contrat CA-2026-0001, retiraison 1");

        assertThat(MouvementContrat.de(retiraison(LE_15_JUIN), TypeOperateur.NEGOCIANT, false))
                .extracting(MouvementContrat::colonne)
                .containsExactly(ColonneDsm.C03, ColonneDsm.C21, ColonneDsm.C03, ColonneDsm.C21);
    }

    @Test
    @DisplayName("RG-CA-22 : le volume s'ajoute à la colonne, sur la ligne du grain ; une ligne absente est ajoutée (AJOUT, colonne 1 à 0)")
    void rg_ca_22_inscription() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        List<MouvementContrat> achats = MouvementContrat.de(retiraison(LE_15_JUIN), TypeOperateur.NEGOCIANT, true)
                .stream().filter(m -> m.sens() == SensMouvementContrat.ACHAT).toList();

        assertThat(achats).allMatch(dsm::inscrire);
        assertThat(dsm.inscrire(achats.getFirst())).isTrue();

        LigneDsm mb = dsm.lignes().get(3);
        assertThat(mb.identite().millesime().annee()).isEqualTo(2010);
        assertThat(mb.volume(ColonneDsm.C03)).isEqualTo(l("1410.00"));
        assertThat(mb.stockDebut()).isEqualTo(l("640.25"));
        LigneDsm ajoutee = dsm.lignes().getLast();
        assertThat(dsm.lignes()).hasSize(7);
        assertThat(ajoutee.origine()).isEqualTo(OrigineLigne.AJOUT);
        assertThat(ajoutee.id()).isNull();
        assertThat(ajoutee.identite().denomination().texte()).isEqualTo("Hors d'âge");
        assertThat(ajoutee.stockDebut().estNul()).isTrue();
        assertThat(ajoutee.volume(ColonneDsm.C03)).isEqualTo(l("135.00"));
    }

    @Test
    @DisplayName("RG-CA-22 : une colonne grisée pour le déclarant (achat d'un producteur) n'inscrit rien ; une DSM envoyée ne se modifie plus")
    void rg_ca_22_colonne_grisee_et_dsm_envoyee() {
        PeriodeDeclaration campagne = PeriodeDeclaration.commencant(TypeDeclarant.PRODUCTEUR, LocalDate.of(2026, 4, 1));
        DeclarationStocksMouvements producteur = dsm(TypeOperateur.PRODUCTEUR, campagne, sixLignes());
        MouvementContrat achat = MouvementContrat.de(retiraison(LE_15_JUIN), TypeOperateur.PRODUCTEUR, true).getFirst();
        assertThat(producteur.inscrire(achat)).isFalse();
        assertThat(producteur.lignes().get(3).volume(ColonneDsm.C02).estNul()).isTrue();

        DeclarationStocksMouvements envoyee = dsmDeJuin();
        envoyee.envoyer(MAINTENANT, AUJOURDHUI, null);
        assertThatThrownBy(() -> envoyee.inscrire(MouvementContrat.de(retiraison(LE_15_JUIN), TypeOperateur.NEGOCIANT,
                true).getFirst())).isInstanceOf(RegleEnfreinte.class).hasMessageContaining("ne se modifie plus");
    }

    @Test
    @DisplayName("RG-CA-23 : un mouvement de contrat inscrit justifie son type de mouvement")
    void rg_ca_23_justificatif_contrat() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        MouvementContrat achat = MouvementContrat.de(retiraison(LE_15_JUIN), TypeOperateur.NEGOCIANT, true).getFirst();
        dsm.inscrire(achat);
        assertThat(ControlePieces.manques(dsm, List.of(), List.of(), List.of()))
                .extracting(Manque::typeMouvement).containsExactly(TypeMouvement.ACHAT);
        assertThat(ControlePieces.manques(dsm, List.of(), List.of(), List.of(achat.inscritDans(50)))).isEmpty();
    }

    @Test
    @DisplayName("RG-CA-24 : une DSM ne s'envoie qu'à partir du lendemain de la fin de sa période")
    void rg_ca_24_envoi_apres_la_fin_de_periode() {
        DeclarationStocksMouvements dsm = dsmDeJuin();
        assertThat(dsm.envoiPossibleLe()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(dsm.envoyablePar(LocalDate.of(2026, 6, 30))).isFalse();
        assertThatThrownBy(() -> dsm.envoyer(MAINTENANT, LocalDate.of(2026, 6, 30), null))
                .isInstanceOf(RegleEnfreinte.class)
                .hasMessage("La DSM de juin 2026 s'envoie à partir du 01/07/2026, lendemain de la fin de sa période.")
                .extracting(e -> ((RegleEnfreinte) e).regle()).isEqualTo(java.util.Optional.of("RG-CA-24"));
        assertThat(dsm.envoyer(MAINTENANT, LocalDate.of(2026, 7, 1), null)).isFalse();
        assertThat(PERIODE_JUIN.fin()).isEqualTo(LocalDate.of(2026, 6, 30));
    }
}
