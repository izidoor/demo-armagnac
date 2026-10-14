package fr.bnia.declarations.application;

import static fr.bnia.declarations.application.Monde.AGENT_BNIA;
import static fr.bnia.declarations.application.Monde.AGENT_NEGOCIANT;
import static fr.bnia.declarations.application.Monde.INCONNU;
import static fr.bnia.declarations.application.Monde.NEGOCIANT;
import static fr.bnia.declarations.application.Monde.OBSERVATEUR_NEGOCIANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.declarations.application.EditerDsm.DocumentEdite;
import fr.bnia.declarations.application.EditionsDsm.EditionDsm;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.declarations.domain.partage.Cote;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Édition PDF de la DSM (spec 05, lot 5). */
class EditerDsmTest {

    private final Monde monde = new Monde();
    private final List<EditionDsm> editees = new ArrayList<>();
    private final EditerDsm cas = new EditerDsm(monde.dsms, edition -> {
        editees.add(edition);
        return "%PDF-1.4".getBytes();
    }, monde.classeurs, monde.journal, monde.grilles, monde.factures, monde.operateurs, monde.referentiels, Monde.PARAMETRES,
            Monde.HORLOGE);
    private long id;

    @BeforeEach
    void dsmDeJuin() {
        monde.gererPointDeDepart().fixer(AGENT_BNIA, NEGOCIANT, GererPointDeDepartTest.JUIN, GererPointDeDepartTest.STOCK);
        id = monde.gererDsm().initier(AGENT_NEGOCIANT, NEGOCIANT).dsm().id();
    }

    @Test
    @DisplayName("RG-DEC-35, 23 : tout lecteur édite le PDF, observateur compris ; l'édition est au journal")
    void rg_dec_35_pdf() {
        DocumentEdite pdf = cas.pdf(OBSERVATEUR_NEGOCIANT, NEGOCIANT, id);

        assertThat(pdf.nom()).isEqualTo("DSM-N7-2026-06.pdf");
        assertThat(pdf.contenu()).startsWith("%PDF".getBytes());
        assertThat(editees).singleElement().satisfies(e -> {
            assertThat(e.dsm().id()).isEqualTo(id);
            assertThat(e.cote()).isEqualTo(Cote.OPERATEUR);
            assertThat(e.cotisation()).isInstanceOf(CotisationDsm.NonCalculable.class);
        });
        assertThat(monde.journal.deLaDsm(id).getFirst()).satisfies(e -> {
            assertThat(e.action()).isEqualTo(ActionJournalDeclarations.DSM_EDITEE);
            assertThat(e.detail()).isEqualTo("PDF");
        });
        assertThat(cas.pdf(AGENT_BNIA, NEGOCIANT, id).nom()).isEqualTo(pdf.nom());
    }

    @Test
    @DisplayName("RG-DEC-01 : sans lecture des déclarations de l'opérateur, pas d'édition ; DSM d'un autre opérateur introuvable")
    void rg_dec_01_refus() {
        assertThatThrownBy(() -> cas.pdf(INCONNU, NEGOCIANT, id)).isInstanceOf(AccesRefuse.class);
        assertThatThrownBy(() -> cas.pdf(AGENT_NEGOCIANT, Monde.PRODUCTEUR, id)).isInstanceOf(ElementIntrouvable.class);
        assertThat(editees).isEmpty();
    }
}
