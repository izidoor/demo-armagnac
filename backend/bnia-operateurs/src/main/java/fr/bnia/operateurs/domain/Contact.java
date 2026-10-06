package fr.bnia.operateurs.domain;

import fr.bnia.operateurs.domain.partage.ModulePortail;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contact d'un opérateur (§4.3.4 p.20-21, C.1.9 p.104-105) : une personne à qui s'adresser, qui porte
 * ses droits au portail. Entité de l'agrégat {@code Operateur} (ADR-012), modifiée seulement à travers
 * lui. L'identifiant est attribué avant l'enregistrement, et reste stable (décision du 2026-10-06).
 */
public final class Contact {

    private final long id;
    private IdentiteContact identite;
    private DroitsPortail droits;
    private boolean emailVerrouille;
    private boolean actif;

    private Contact(long id, IdentiteContact identite, DroitsPortail droits, boolean emailVerrouille, boolean actif) {
        this.id = id;
        this.identite = identite;
        this.droits = droits;
        this.emailVerrouille = emailVerrouille;
        this.actif = actif;
    }

    /** RG-OPE-33 : actif à la création, sans accès au portail (RG-OPE-29). */
    static Contact creer(long id, IdentiteContact identite) {
        return new Contact(id, identite, DroitsPortail.aucun(), false, true);
    }

    public static Contact reconstituer(long id, IdentiteContact identite, DroitsPortail droits, boolean emailVerrouille,
            boolean actif) {
        return new Contact(id, identite, droits, emailVerrouille, actif);
    }

    void identifier(IdentiteContact nouvelle) {
        this.identite = nouvelle;
    }

    void attribuer(DroitsPortail nouveaux) {
        this.droits = nouveaux;
    }

    void verrouillerEmail() {
        this.emailVerrouille = true;
    }

    void desactiver() {
        this.actif = false;
    }

    void reactiver() {
        this.actif = true;
    }

    /** Administrateur actif : compte pour RG-OPE-30. */
    public boolean estAdministrateurActif() {
        return actif && droits.administrateur();
    }

    boolean aEmail(Email email) {
        return identite.email() != null && identite.email().cle().equals(email.cle());
    }

    /** RG-OPE-34 : les champs d'identité qui diffèrent, avec leurs valeurs avant et après. */
    static List<Changement> changements(IdentiteContact avant, IdentiteContact apres) {
        List<Changement> changements = new ArrayList<>();
        comparer(changements, ChampContact.TITRE, avant, apres, i -> texte(i.titre()));
        comparer(changements, ChampContact.NOM, avant, apres, IdentiteContact::nom);
        comparer(changements, ChampContact.PRENOM, avant, apres, IdentiteContact::prenom);
        comparer(changements, ChampContact.FONCTION, avant, apres, IdentiteContact::fonction);
        comparer(changements, ChampContact.CENTRES_INTERET, avant, apres, i -> liste(i.centresInteret()));
        comparer(changements, ChampContact.TELEPHONE_PROFESSIONNEL, avant, apres, IdentiteContact::telephoneProfessionnel);
        comparer(changements, ChampContact.TELEPHONE_SECONDAIRE, avant, apres, IdentiteContact::telephoneSecondaire);
        comparer(changements, ChampContact.EMAIL, avant, apres, i -> texte(i.email()));
        comparer(changements, ChampContact.ADRESSE, avant, apres, IdentiteContact::adresse);
        comparer(changements, ChampContact.CODE_POSTAL, avant, apres, IdentiteContact::codePostal);
        comparer(changements, ChampContact.VILLE, avant, apres, IdentiteContact::ville);
        comparer(changements, ChampContact.PAYS, avant, apres, i -> texte(i.pays()));
        comparer(changements, ChampContact.OBSERVATION, avant, apres, IdentiteContact::observation);
        return changements;
    }

    /** RG-OPE-34 : rôle et modules qui diffèrent. */
    static List<Changement> changements(DroitsPortail avant, DroitsPortail apres) {
        List<Changement> changements = new ArrayList<>();
        comparer(changements, ChampContact.ADMINISTRATEUR, avant, apres, d -> d.administrateur() ? "oui" : "non");
        comparer(changements, ChampContact.MODULES, avant, apres,
                d -> d.modules().stream().sorted().map(ModulePortail::libelle).collect(Collectors.joining(", ")));
        return changements;
    }

    private static <T> void comparer(List<Changement> changements, ChampContact champ, T avant, T apres,
            Function<T, String> valeur) {
        String a = vide(valeur.apply(avant));
        String b = vide(valeur.apply(apres));
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

    public long id() {
        return id;
    }

    public IdentiteContact identite() {
        return identite;
    }

    public DroitsPortail droits() {
        return droits;
    }

    /** RG-OPE-32 : l'e-mail est l'identifiant d'un compte portail. */
    public boolean emailVerrouille() {
        return emailVerrouille;
    }

    public boolean estActif() {
        return actif;
    }
}
