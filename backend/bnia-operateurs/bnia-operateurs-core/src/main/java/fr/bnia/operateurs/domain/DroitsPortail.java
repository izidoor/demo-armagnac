package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.ModulePortail;
import java.util.EnumSet;
import java.util.Set;

/**
 * Droits d'un contact au portail (C.1.9.2 p.105, §5.13.2 p.78-79) : accès, administrateur, modules.
 * RG-OPE-30 : un administrateur a l'accès et tous les modules. RG-OPE-31 : sans accès, aucun module.
 */
public record DroitsPortail(boolean acces, boolean administrateur, Set<ModulePortail> modules) {

    public DroitsPortail {
        if (administrateur && !acces) {
            throw new RegleEnfreinte("RG-OPE-30", "Un administrateur doit avoir l'accès au portail.");
        }
        if (!acces) {
            modules = Set.of();
        } else if (administrateur) {
            modules = Set.copyOf(EnumSet.allOf(ModulePortail.class));
        } else {
            modules = modules == null || modules.isEmpty() ? Set.of() : Set.copyOf(EnumSet.copyOf(modules));
        }
    }

    public static DroitsPortail aucun() {
        return new DroitsPortail(false, false, Set.of());
    }
}
