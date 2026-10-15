package fr.bnia.facturation.application;

import static fr.bnia.facturation.application.DemanderFacturesTest.demandeDeLExemple;
import static fr.bnia.facturation.application.Monde.GESTIONNAIRE;
import static fr.bnia.facturation.application.Monde.LECTEUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.facturation.api.ClientRef;
import fr.bnia.facturation.application.EmettreLot.CompteRendu;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.facturation.domain.Exemples;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmettreLotTest {

    private final Monde monde = new Monde();
    private final EmettreLot cas = monde.emettreLot();

    @Test
    @DisplayName("RG-FAC-19 : chaque brouillon dans l'ordre de la sélection ; un refus nomme son motif et ne consomme pas de numéro")
    void rg_fac_19_lot() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        List<Long> ids = monde.demander().demander(GESTIONNAIRE,
                List.of(demandeDeLExemple("7"), demandeDeLExemple("8"), demandeDeLExemple("9")));
        // L'acheteur relu à l'émission perd son e-mail : seul le deuxième brouillon est concerné.
        ClientRef autre = ClientRef.operateur(43, "N320002");
        Acheteur a = Exemples.acheteur();
        monde.acheteurs.put(autre, new Acheteur(autre, a.categorie(), "Domaine sans e-mail", a.siren(), a.siret(),
                null, a.adresseFacturation(), null, null));
        monde.factures.parId.computeIfPresent(ids.get(1), (id, d) -> new fr.bnia.facturation.domain.Facture.Donnees(
                d.id(), d.sens(), d.etat(), d.origine(), d.dateOperation(), d.libellePeriode(), d.condition(),
                d.bonDeCommande(), d.noteEntete(), d.notePied(), monde.acheteurs.get(autre), d.vendeur(), d.lignes(),
                d.numero(), d.dateEmission(), d.echeance(), d.pdf(), d.creeeLe(), d.creePar(), d.version()));

        List<CompteRendu> comptesRendus = cas.emettre(GESTIONNAIRE, List.of(ids.get(2), ids.get(1), ids.get(0)));

        assertThat(comptesRendus).extracting(CompteRendu::id).containsExactly(ids.get(2), ids.get(1), ids.get(0));
        assertThat(comptesRendus).extracting(CompteRendu::numero).containsExactly("2026000001", null, "2026000002");
        assertThat(comptesRendus.get(1).motif()).contains("e-mail du destinataire des factures");
    }

    @Test
    @DisplayName("RG-FAC-19 : un brouillon déjà émis ou introuvable est rapporté, sans arrêter le lot")
    void rg_fac_19_deja_emis() {
        monde.vendeurs.vendeur = Exemples.vendeur();
        long brouillon = monde.demander().demander(GESTIONNAIRE, List.of(demandeDeLExemple("7"))).getFirst();

        List<CompteRendu> comptesRendus = cas.emettre(GESTIONNAIRE, List.of(brouillon, brouillon, 404L));

        assertThat(comptesRendus).hasSize(2);
        assertThat(comptesRendus.getFirst().emis()).isTrue();
        assertThat(comptesRendus.getLast().motif()).contains("introuvable");
    }

    @Test
    @DisplayName("RG-FAC-26 : émettre une sélection est réservé à qui gère la facturation")
    void rg_fac_26_refus() {
        assertThatThrownBy(() -> cas.emettre(LECTEUR, List.of(1L)))
                .isInstanceOfSatisfying(AccesRefuse.class, e -> assertThat(e.regle()).contains("RG-FAC-26"));
    }
}
