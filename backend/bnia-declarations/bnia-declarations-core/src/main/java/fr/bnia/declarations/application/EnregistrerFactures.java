package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.domain.ActionJournalDeclarations;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Projection locale des factures et avoirs des DSM (A-DEC-4, RG-DEC-34), alimentée par les événements de
 * {@code facturation}, dans sa transaction (ADR-013, RG-FAC-25). Sans contrôle de droits : {@code facturation}
 * a vérifié ceux de l'acteur qui a émis.
 */
@UseCase
public class EnregistrerFactures {

    private final FacturesDsm factures;
    private final DsmRepository dsms;
    private final Journaliste journaliste;

    /**
     * Une pièce émise pour une DSM, reçue de {@code facturation}.
     *
     * @param piece    identifiant de la pièce chez {@code facturation}
     * @param emisePar l'utilisateur qui l'a émise, pour le journal de la DSM
     */
    public record PieceRecue(long dsm, long piece, String numero, LocalDate date, BigDecimal ht, BigDecimal tva,
            BigDecimal ttc, Acteur emisePar) {
    }

    public EnregistrerFactures(FacturesDsm factures, DsmRepository dsms, JournalDeclarations journal,
            OperateursLecture operateurs, Clock horloge) {
        this.factures = factures;
        this.dsms = dsms;
        this.journaliste = new Journaliste(journal, operateurs, horloge);
    }

    /**
     * RG-DEC-34 : la DSM devient facturée, son brouillon est remplacé ; journal « Facture émise ». Un même
     * numéro reçu deux fois n'est enregistré qu'une fois.
     */
    public void factureEmise(PieceRecue facture) {
        factures.retirerBrouillon(facture.piece());
        boolean nouvelle = factures.inserer(new FactureDsm(facture.dsm(), facture.piece(), FactureDsm.Type.FACTURE,
                facture.numero(), facture.date(), facture.ht(), facture.tva(), facture.ttc()));
        if (nouvelle) {
            dsms.parId(facture.dsm()).ifPresent(dsm -> journaliste.noter(facture.emisePar(), dsm,
                    ActionJournalDeclarations.FACTURE_EMISE, facture.numero()));
        }
    }

    /** RG-DEC-34 : l'avoir se retranche du total facturé de la DSM (publié par {@code facturation} au lot 4). */
    public void avoirEmis(PieceRecue avoir) {
        factures.inserer(new FactureDsm(avoir.dsm(), avoir.piece(), FactureDsm.Type.AVOIR, avoir.numero(),
                avoir.date(), avoir.ht(), avoir.tva(), avoir.ttc()));
    }
}
