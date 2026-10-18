package fr.bnia.messagerie.application;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.messagerie.api.DescriptionObjet;
import fr.bnia.messagerie.api.ObjetRef;
import fr.bnia.messagerie.api.ObjetsConversables;
import fr.bnia.messagerie.api.TypeObjet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Les {@link ObjetsConversables} de tous les contextes métier, indexés par type (A-MSG-2) : deux implémentations
 * d'un même type font échouer le démarrage.
 */
final class Objets {

    private final Map<TypeObjet, ObjetsConversables> parType = new EnumMap<>(TypeObjet.class);

    Objets(List<ObjetsConversables> conversables) {
        for (ObjetsConversables c : conversables) {
            if (parType.putIfAbsent(c.type(), c) != null) {
                throw new IllegalStateException("Deux descriptions d'objets conversables pour le type " + c.type()
                        + " : " + parType.get(c.type()).getClass().getName() + " et " + c.getClass().getName() + ".");
            }
        }
    }

    /** RG-MSG-04 : l'objet tel que le décrit son contexte ; introuvable s'il n'existe pas ou n'est pas conversable. */
    DescriptionObjet decrire(ObjetRef objet) {
        ObjetsConversables conversables = parType.get(objet.type());
        return (conversables == null ? java.util.Optional.<DescriptionObjet>empty()
                : conversables.decrire(objet.identifiant()))
                .orElseThrow(() -> new ElementIntrouvable(objet.type().libelle() + " introuvable : n° "
                        + objet.identifiant() + "."));
    }
}
