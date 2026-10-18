package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.api.FactureAbandonnee;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.SensPiece;

/** Abandonner un brouillon (UC-FAC-2). RG-FAC-14, 18, 25, 26. */
@UseCase
public class AbandonnerBrouillon {

    private final FactureRepository factures;
    private final PublicationEvenements evenements;
    private final Controles controles;

    public AbandonnerBrouillon(FactureRepository factures, PublicationEvenements evenements,
            DroitsFacturation droits) {
        this.factures = factures;
        this.evenements = evenements;
        this.controles = new Controles(droits);
    }

    /**
     * RG-FAC-18 : le brouillon est supprimé avec son journal ; celui d'une facture publie {@link FactureAbandonnee}
     * avec son origine, dans la même transaction (RG-FAC-25). Une pièce émise ne s'abandonne pas (RG-FAC-14).
     */
    public void abandonner(Acteur acteur, long id) {
        controles.exigerGestion(acteur);
        Facture piece = factures.parId(id).orElseThrow(() -> new ElementIntrouvable("Pièce introuvable : n° " + id + "."));
        piece.exigerAbandonnable();
        factures.supprimer(id);
        if (piece.sens() == SensPiece.FACTURE) {
            evenements.publier(new FactureAbandonnee(id, piece.origine(), piece.acheteur().client(), acteur));
        }
    }
}
