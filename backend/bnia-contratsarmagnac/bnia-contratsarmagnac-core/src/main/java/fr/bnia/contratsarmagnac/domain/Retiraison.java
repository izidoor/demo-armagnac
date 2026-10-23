package fr.bnia.contratsarmagnac.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Une retiraison du contrat (spec 06 §3.4), entité de l'agrégat : déclarée par l'acheteur, modifiée par la partie qui
 * doit la valider, validée quand les deux parties ont approuvé sa version courante (RG-CA-13 à 15). Ses versions et
 * ses approbations sont en ajout seul.
 *
 * @param numero rang dans le contrat, jamais réattribué
 */
public final class Retiraison {

    private final int numero;
    private EtatRetiraison etat;
    private final List<VersionRetiraison> versions;
    private final List<Approbation> approbations;

    Retiraison(int numero, EtatRetiraison etat, List<VersionRetiraison> versions, List<Approbation> approbations) {
        this.numero = numero;
        this.etat = Objects.requireNonNull(etat, "etat");
        this.versions = new ArrayList<>(versions);
        this.approbations = new ArrayList<>(approbations);
        if (this.versions.isEmpty()) {
            throw new IllegalArgumentException("Une retiraison a au moins une version.");
        }
    }

    public static Retiraison reconstituer(int numero, EtatRetiraison etat, List<VersionRetiraison> versions,
            List<Approbation> approbations) {
        return new Retiraison(numero, etat, versions, approbations);
    }

    /** RG-CA-13 : la version 1, approuvée par l'acheteur qui la déclare ; elle attend le vendeur. */
    static Retiraison declarer(int numero, Auteur auteur, ContenuRetiraison contenu) {
        return new Retiraison(numero, EtatRetiraison.A_VALIDER_VENDEUR, List.of(new VersionRetiraison(1, auteur,
                contenu)), List.of(new Approbation(1, auteur)));
    }

    /** RG-CA-14 : une nouvelle version, approuvée par son auteur seul ; elle attend l'autre partie. */
    void modifier(Auteur auteur, ContenuRetiraison contenu) {
        int suivante = courante().numero() + 1;
        versions.add(new VersionRetiraison(suivante, auteur, contenu));
        approbations.add(new Approbation(suivante, auteur));
        etat = EtatRetiraison.aValiderPar(auteur.partie().autre());
    }

    /** RG-CA-15 : la partie attendue approuve la version courante : la retiraison est validée. */
    void valider(Auteur auteur) {
        approbations.add(new Approbation(courante().numero(), auteur));
        etat = EtatRetiraison.VALIDEE;
    }

    /**
     * RG-CA-14 : l'écart entre la dernière version approuvée par la partie et la courante (date, dernière, volumes
     * des lignes comparées par rang de ligne du contrat), avec les auteurs des versions intermédiaires.
     */
    public Ecart ecartPour(Partie partie) {
        int depuis = approbations.stream().filter(a -> a.partie() == partie).mapToInt(Approbation::version).max()
                .orElse(1);
        VersionRetiraison avant = versions.stream().filter(v -> v.numero() == depuis).findFirst().orElseThrow();
        List<Auteur> auteurs = versions.stream().filter(v -> v.numero() > depuis).map(VersionRetiraison::auteur)
                .toList();
        return Ecart.entre(avant, courante(), auteurs);
    }

    public Optional<Partie> partieQuiDoitValider() {
        return etat.attend();
    }

    public boolean validee() {
        return etat == EtatRetiraison.VALIDEE;
    }

    public VersionRetiraison courante() {
        return versions.getLast();
    }

    public int numero() {
        return numero;
    }

    public EtatRetiraison etat() {
        return etat;
    }

    public List<VersionRetiraison> versions() {
        return List.copyOf(versions);
    }

    public List<Approbation> approbations() {
        return List.copyOf(approbations);
    }
}
