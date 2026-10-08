package fr.bnia.operateurs.application;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongFunction;

/**
 * RG-OPE-21 : une valeur de référentiel choisie doit exister et être active ; celle que l'objet porte déjà
 * reste acceptée, même devenue inactive. Partagé par les cas d'usage du contexte.
 */
final class References {

    private final ReferentielsLecture referentiels;

    References(ReferentielsLecture referentiels) {
        this.referentiels = referentiels;
    }

    void titre(Long choisi, Long actuel) {
        verifier("titre", choisi, actuel, id -> referentiels.titre(id).map(v -> new Valeur(v.libelle(), v.actif())));
    }

    void pays(Long choisi, Long actuel) {
        verifier("pays", choisi, actuel, id -> referentiels.pays(id).map(v -> new Valeur(v.libelle(), v.actif())));
    }

    void membreBnia(Long choisi, Long actuel) {
        verifier("membre BNIA", choisi, actuel,
                id -> referentiels.membreBnia(id).map(v -> new Valeur(v.libelle(), v.actif())));
    }

    void region(Long choisi, Long actuel) {
        verifier("région", choisi, actuel, id -> referentiels.region(id).map(v -> new Valeur(v.libelle(), v.actif())));
    }

    void centresInteret(Collection<Long> choisis, Set<Long> dejaPortes) {
        for (Long centre : choisis) {
            if (!dejaPortes.contains(centre)) {
                verifier("centre d'intérêt", centre, null,
                        id -> referentiels.centreInteret(id).map(v -> new Valeur(v.libelle(), v.actif())));
            }
        }
    }

    private static void verifier(String nom, Long choisi, Long actuel, LongFunction<Optional<Valeur>> lire) {
        if (choisi == null || Objects.equals(choisi, actuel)) {
            return;
        }
        Valeur valeur = lire.apply(choisi).orElseThrow(() -> new RegleEnfreinte("RG-OPE-21",
                "Valeur inconnue (" + nom + ") : " + choisi + "."));
        if (!valeur.actif()) {
            throw new RegleEnfreinte("RG-OPE-21",
                    "Valeur inactive (" + nom + ") : « " + valeur.libelle() + " ». Choisissez une valeur active.");
        }
    }

    private record Valeur(String libelle, boolean actif) {
    }
}
