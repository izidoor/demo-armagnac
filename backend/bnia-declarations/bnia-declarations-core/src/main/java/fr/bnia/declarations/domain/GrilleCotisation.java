package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * La grille des taux CVO (agrégat, spec 05 §3.5), administrée par le BNIA dans le menu Paramétrage. Une seule
 * grille, en chaîne continue : chaque taux court de son début à la veille du début du suivant, le dernier sans
 * fin ; ni trou ni chevauchement possibles (RG-DEC-30, H-DEC-20).
 */
public final class GrilleCotisation {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Un taux et sa fin calculée, absente pour le dernier (RG-DEC-30). */
    public record TauxEnVigueur(TauxCotisation taux, Optional<LocalDate> fin) {

        public boolean couvre(LocalDate date) {
            return !date.isBefore(taux.debut()) && fin.map(f -> !date.isAfter(f)).orElse(true);
        }
    }

    private final List<TauxCotisation> taux;

    private GrilleCotisation(Collection<TauxCotisation> taux) {
        this.taux = new ArrayList<>(taux);
    }

    public static GrilleCotisation de(Collection<TauxCotisation> taux) {
        GrilleCotisation grille = new GrilleCotisation(List.of());
        taux.forEach(grille::ajouter);
        return grille;
    }

    /** Les taux, du plus ancien au plus récent. */
    public List<TauxCotisation> taux() {
        return taux.stream().sorted(Comparator.comparing(TauxCotisation::debut)).toList();
    }

    /** RG-DEC-30 : chaque taux avec sa fin, veille du début du suivant, du plus ancien au plus récent. */
    public List<TauxEnVigueur> chaine() {
        List<TauxCotisation> tries = taux();
        List<TauxEnVigueur> chaine = new ArrayList<>();
        for (int i = 0; i < tries.size(); i++) {
            chaine.add(new TauxEnVigueur(tries.get(i), i + 1 < tries.size()
                    ? Optional.of(tries.get(i + 1).debut().minusDays(1)) : Optional.empty()));
        }
        return chaine;
    }

    /** RG-DEC-32 : le taux en vigueur à cette date, ou rien avant le premier. */
    public Optional<TauxEnVigueur> tauxAu(LocalDate date) {
        return chaine().stream().filter(t -> t.couvre(date)).findFirst();
    }

    /** RG-DEC-30 : la date de début est unique ; le nouveau taux clôt le précédent la veille de son début. */
    public void ajouter(TauxCotisation nouveau) {
        exigerDebutLibre(nouveau.debut(), taux);
        taux.add(nouveau);
    }

    /** RG-DEC-30 : corrige un taux, désigné par sa date de début actuelle ; la chaîne se recompose. */
    public void modifier(LocalDate debutActuel, TauxCotisation corrige) {
        TauxCotisation actuel = commencantLe(debutActuel);
        List<TauxCotisation> autres = new ArrayList<>(taux);
        autres.remove(actuel);
        exigerDebutLibre(corrige.debut(), autres);
        taux.set(taux.indexOf(actuel), corrige);
    }

    /** RG-DEC-30 : le précédent court alors jusqu'à la veille du suivant, ou sans fin. */
    public void supprimer(LocalDate debut) {
        taux.remove(commencantLe(debut));
    }

    private TauxCotisation commencantLe(LocalDate debut) {
        return taux.stream().filter(t -> t.debut().equals(debut)).findFirst()
                .orElseThrow(() -> new ElementIntrouvable("Aucun taux CVO ne commence le " + debut.format(DATE) + "."));
    }

    private static void exigerDebutLibre(LocalDate debut, List<TauxCotisation> existants) {
        if (existants.stream().anyMatch(t -> t.debut().equals(debut))) {
            throw new RegleEnfreinte("RG-DEC-30", "Un taux CVO commence déjà le " + debut.format(DATE) + ".");
        }
    }
}
