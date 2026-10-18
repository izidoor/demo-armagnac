package fr.bnia.declarations.adapter.integration;

import fr.bnia.declarations.application.DecrireDsm;
import fr.bnia.messagerie.api.DescriptionObjet;
import fr.bnia.messagerie.api.ObjetsConversables;
import fr.bnia.messagerie.api.TypeObjet;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Les DSM, objets conversables de {@code messagerie} (ADR-023, RG-MSG-04) : toute DSM, dans tout état. */
@Component
class DsmConversables implements ObjetsConversables {

    private final DecrireDsm decrire;

    DsmConversables(DecrireDsm decrire) {
        this.decrire = decrire;
    }

    @Override
    public TypeObjet type() {
        return TypeObjet.DSM;
    }

    @Override
    public Optional<DescriptionObjet> decrire(String identifiant) {
        long id;
        try {
            id = Long.parseLong(identifiant);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        return decrire.decrire(id).map(d -> new DescriptionObjet(d.operateur(), d.libelle()));
    }
}
