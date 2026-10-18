package fr.bnia.messagerie.adapter.integration;

import fr.bnia.messagerie.api.Acteur;
import fr.bnia.messagerie.api.TypeObjet;
import fr.bnia.messagerie.application.DroitsMessagerie;
import fr.bnia.messagerie.domain.Cote;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Port de l'hôte (A-MSG-3) sur la partie publiée d'{@code operateurs} : seul cet adapter sait quel module porte
 * chaque type d'objet (spec 15 §3.2). Un rôle tenu dans l'opérateur BNIA vaut pour tous (RG-OPE-32).
 */
@Component
class DroitsMessagerieOperateurs implements DroitsMessagerie {

    private final OperateursLecture operateurs;

    DroitsMessagerieOperateurs(OperateursLecture operateurs) {
        this.operateurs = operateurs;
    }

    /** Le module des droits de chaque type d'objet (spec 15 §3.2). */
    static Module module(TypeObjet type) {
        return switch (type) {
            case DSM -> Module.DECLARATIONS;
            case FACTURE -> Module.FACTURATION;
        };
    }

    @Override
    public Optional<Cote> cote(Acteur acteur, long operateur) {
        Droits droits = droits(acteur, operateur);
        if (droits.local().isPresent()) {
            return Optional.of(Cote.OPERATEUR);
        }
        return droits.personnelBnia() ? Optional.of(Cote.BNIA) : Optional.empty();
    }

    @Override
    public boolean lit(Acteur acteur, TypeObjet type, long operateur) {
        return droits(acteur, operateur).lit(module(type));
    }

    @Override
    public boolean gere(Acteur acteur, TypeObjet type, long operateur) {
        return droits(acteur, operateur).gere(module(type));
    }

    @Override
    public boolean gereMessagerie(Acteur acteur, long operateur) {
        return droits(acteur, operateur).gere(Module.MESSAGERIE);
    }

    @Override
    public boolean personnelBnia(Acteur acteur) {
        return droits(acteur, operateurs.operateurBnia()).personnelBnia();
    }

    @Override
    public Set<TypeObjet> typesLusParLeBnia(Acteur acteur) {
        Droits droits = droits(acteur, operateurs.operateurBnia());
        if (!droits.personnelBnia()) {
            return Set.of();
        }
        return types(droits);
    }

    @Override
    public Set<TypeObjet> typesLus(Acteur acteur, long operateur) {
        Droits droits = droits(acteur, operateur);
        return droits.local().isEmpty() ? Set.of() : types(droits);
    }

    @Override
    public boolean membreActif(long utilisateur, long operateur) {
        return operateurs.droits(utilisateur, operateur).local().isPresent();
    }

    @Override
    public Optional<String> nom(long utilisateur) {
        return operateurs.nomUtilisateur(utilisateur);
    }

    @Override
    public Map<Long, OperateurNomme> operateurs(Collection<Long> ids) {
        return operateurs.identites(ids).stream()
                .map(r -> new OperateurNomme(r.id(), r.code(), r.raisonSociale()))
                .collect(Collectors.toMap(OperateurNomme::id, Function.identity(), (a, b) -> a));
    }

    private static Set<TypeObjet> types(Droits droits) {
        Set<TypeObjet> types = EnumSet.noneOf(TypeObjet.class);
        Arrays.stream(TypeObjet.values()).filter(t -> droits.lit(module(t))).forEach(types::add);
        return types;
    }

    private Droits droits(Acteur acteur, long operateur) {
        return operateurs.droits(acteur.utilisateur(), operateur);
    }
}
