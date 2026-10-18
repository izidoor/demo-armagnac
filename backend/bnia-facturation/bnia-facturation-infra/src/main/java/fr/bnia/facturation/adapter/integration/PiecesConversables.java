package fr.bnia.facturation.adapter.integration;

import fr.bnia.facturation.application.DecrirePiece;
import fr.bnia.messagerie.api.DescriptionObjet;
import fr.bnia.messagerie.api.ObjetsConversables;
import fr.bnia.messagerie.api.TypeObjet;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Les pièces émises, objets conversables de {@code messagerie} (spec 15, ADR-023, RG-MSG-17). */
@Component
class PiecesConversables implements ObjetsConversables {

    private final DecrirePiece decrire;

    PiecesConversables(DecrirePiece decrire) {
        this.decrire = decrire;
    }

    @Override
    public TypeObjet type() {
        return TypeObjet.FACTURE;
    }

    @Override
    public Optional<DescriptionObjet> decrire(String identifiant) {
        long id;
        try {
            id = Long.parseLong(identifiant);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return decrire.decrire(id).map(p -> new DescriptionObjet(p.operateur(), p.libelle()));
    }
}
