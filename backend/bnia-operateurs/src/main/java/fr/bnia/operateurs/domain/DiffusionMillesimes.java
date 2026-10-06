package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Diffusion des millésimes (§4.3.2.2 p.19, C.1.3.1 p.102), RG-OPE-14 : refus par défaut ; en accord
 * partiel, au moins un millésime diffusable, sans doublon ; en refus ou accord total, aucun.
 */
public record DiffusionMillesimes(NiveauDiffusion niveau, SortedSet<Millesime> millesimes) {

    public DiffusionMillesimes {
        if (niveau == null) {
            throw new RegleEnfreinte("RG-OPE-14", "Le niveau de diffusion des millésimes est obligatoire.");
        }
        millesimes = new TreeSet<>(millesimes == null ? Set.of() : millesimes);
        if (niveau == NiveauDiffusion.PARTIEL && millesimes.isEmpty()) {
            throw new RegleEnfreinte("RG-OPE-14", "Un accord partiel exige au moins un millésime diffusable.");
        }
        if (niveau != NiveauDiffusion.PARTIEL && !millesimes.isEmpty()) {
            throw new RegleEnfreinte("RG-OPE-14", "Les millésimes diffusables ne se choisissent qu'en accord partiel.");
        }
    }

    public static DiffusionMillesimes refus() {
        return new DiffusionMillesimes(NiveauDiffusion.REFUS, new TreeSet<>());
    }

    /**
     * Depuis une saisie : chaque année doit être un millésime valide, sans doublon. Hors accord partiel,
     * la liste saisie est vidée : l'écran a averti avant (RG-OPE-14).
     */
    public static DiffusionMillesimes saisie(NiveauDiffusion niveau, Collection<Integer> annees) {
        SortedSet<Millesime> millesimes = new TreeSet<>();
        if (niveau == NiveauDiffusion.PARTIEL && annees != null) {
            Set<Integer> vues = new HashSet<>();
            for (Integer annee : annees) {
                if (annee == null) {
                    continue;
                }
                if (!vues.add(annee)) {
                    throw new RegleEnfreinte("RG-OPE-14", "Le millésime " + annee + " est saisi deux fois.");
                }
                millesimes.add(millesime(annee));
            }
        }
        return new DiffusionMillesimes(niveau, millesimes);
    }

    private static Millesime millesime(int annee) {
        try {
            return Millesime.de(annee);
        } catch (RegleEnfreinte invalide) {
            throw new RegleEnfreinte("RG-OPE-14", invalide.getMessage());
        }
    }
}
