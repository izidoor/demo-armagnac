package fr.bnia.operateurs.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Un champ changé, avec ses valeurs avant et après (RG-OPE-34). Les références vers {@code referentiels}
 * sont notées par leurs identifiants, séparés par des virgules ; la couche application les remplace par
 * leurs libellés.
 *
 * @param avant {@code null} si le champ était vide
 * @param apres {@code null} si le champ est vidé
 */
public record Changement(ChampJournal champ, String avant, String apres) {

    /** Titre, nom et prénom qui diffèrent. */
    static List<Changement> identite(IdentitePersonne avant, IdentitePersonne apres) {
        List<Changement> changements = new ArrayList<>();
        comparer(changements, ChampJournal.TITRE, texte(avant.titre()), texte(apres.titre()));
        comparer(changements, ChampJournal.NOM, avant.nom(), apres.nom());
        comparer(changements, ChampJournal.PRENOM, avant.prenom(), apres.prenom());
        return changements;
    }

    /** Champs d'une fiche professionnelle qui diffèrent. */
    static List<Changement> coordonnees(CoordonneesProfessionnelles avant, CoordonneesProfessionnelles apres) {
        List<Changement> changements = new ArrayList<>();
        comparer(changements, ChampJournal.FONCTION, avant.fonction(), apres.fonction());
        comparer(changements, ChampJournal.TELEPHONE_PROFESSIONNEL, avant.telephoneProfessionnel(),
                apres.telephoneProfessionnel());
        comparer(changements, ChampJournal.TELEPHONE_SECONDAIRE, avant.telephoneSecondaire(),
                apres.telephoneSecondaire());
        comparer(changements, ChampJournal.ADRESSE, avant.adresse(), apres.adresse());
        comparer(changements, ChampJournal.CODE_POSTAL, avant.codePostal(), apres.codePostal());
        comparer(changements, ChampJournal.VILLE, avant.ville(), apres.ville());
        comparer(changements, ChampJournal.PAYS, texte(avant.pays()), texte(apres.pays()));
        comparer(changements, ChampJournal.CENTRES_INTERET, liste(avant.centresInteret()), liste(apres.centresInteret()));
        comparer(changements, ChampJournal.OBSERVATION, avant.observation(), apres.observation());
        return changements;
    }

    static void comparer(List<Changement> changements, ChampJournal champ, String avant, String apres) {
        String a = vide(avant);
        String b = vide(apres);
        if (!Objects.equals(a, b)) {
            changements.add(new Changement(champ, a, b));
        }
    }

    private static String vide(String texte) {
        return texte == null || texte.isEmpty() ? null : texte;
    }

    private static String texte(Object valeur) {
        return valeur == null ? null : valeur.toString();
    }

    private static String liste(Collection<Long> ids) {
        return ids.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
    }
}
