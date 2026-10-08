package fr.bnia.referentiels.domain;

import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Classe de TVA et l'historique de ses taux (C.10.1.1, C.10.2.1 p.127). Agrégat : les taux ne se
 * modifient qu'à travers leur classe, qui garantit qu'ils ne se chevauchent pas (RG-REF-11). Les trous
 * entre deux taux sont permis.
 */
public final class ClasseTva extends ValeurSimple {

    private final List<TauxTva> taux;

    private ClasseTva(Long id, Code code, Libelle libelle, boolean actif, Collection<TauxTva> taux) {
        super(id, code, libelle, actif);
        this.taux = new ArrayList<>(taux);
    }

    public static ClasseTva creer(Code code, Libelle libelle) {
        return new ClasseTva(null, code, libelle, true, List.of());
    }

    public static ClasseTva reconstituer(Long id, Code code, Libelle libelle, boolean actif,
            Collection<TauxTva> taux) {
        return new ClasseTva(id, code, libelle, actif, taux);
    }

    /** Les taux, du plus récent au plus ancien. */
    public List<TauxTva> taux() {
        return taux.stream().sorted(Comparator.comparing(TauxTva::debut).reversed()).toList();
    }

    /** RG-REF-11. */
    public void ajouterTaux(TauxTva nouveau) {
        verifierSansChevauchement(nouveau, taux);
        taux.add(nouveau);
    }

    /**
     * Corrige un taux, désigné par sa date de début actuelle (RG-REF-11).
     *
     * @throws ElementIntrouvable si aucun taux ne commence à cette date
     */
    public void modifierTaux(LocalDate debutActuel, TauxTva corrige) {
        TauxTva actuel = tauxCommencantLe(debutActuel);
        List<TauxTva> autres = new ArrayList<>(taux);
        autres.remove(actuel);
        verifierSansChevauchement(corrige, autres);
        taux.set(taux.indexOf(actuel), corrige);
    }

    /**
     * Retire un taux saisi par erreur. Les documents déjà calculés ont figé leur taux : ils n'en
     * dépendent pas (spec 01, §5.1).
     */
    public void retirerTaux(LocalDate debut) {
        taux.remove(tauxCommencantLe(debut));
    }

    /**
     * RG-REF-12 : le seul taux dont la période contient la date, ou rien ; jamais de valeur par défaut.
     * Vaut aussi pour une classe inactive (RG-REF-13).
     */
    public Optional<TauxTva> tauxApplicable(LocalDate date) {
        return taux.stream().filter(t -> t.couvre(date)).findFirst();
    }

    private TauxTva tauxCommencantLe(LocalDate debut) {
        return taux.stream().filter(t -> t.debut().equals(debut)).findFirst()
                .orElseThrow(() -> new ElementIntrouvable(
                        "Aucun taux ne commence le " + debut + " dans la classe de TVA « " + code() + " »."));
    }

    private static void verifierSansChevauchement(TauxTva candidat, List<TauxTva> existants) {
        existants.stream().filter(candidat::chevauche).findFirst().ifPresent(conflit -> {
            throw new RegleEnfreinte("RG-REF-11",
                    "Le taux " + candidat.description() + " chevauche le taux " + conflit.description() + ".");
        });
    }
}
