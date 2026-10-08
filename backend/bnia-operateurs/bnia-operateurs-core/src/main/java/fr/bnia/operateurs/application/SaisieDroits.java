package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.partage.ModulePortail;
import java.util.Set;

/** Droits d'un contact au portail (C.1.9.2 p.105) : tous les modules pour un administrateur (RG-OPE-30). */
public record SaisieDroits(boolean acces, boolean administrateur, Set<ModulePortail> modules) {
}
