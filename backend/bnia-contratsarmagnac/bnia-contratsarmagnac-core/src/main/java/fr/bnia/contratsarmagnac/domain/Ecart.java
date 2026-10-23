package fr.bnia.contratsarmagnac.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * L'écart entre deux versions du contrat (RG-CA-09, A-CA-7), ou d'une retiraison (RG-CA-14), montré à la partie qui doit
 * approuver : les champs changés et, ligne par ligne (comparées par rang), les lignes ajoutées, retirées ou modifiées. Les valeurs sont
 * brutes (nombres en notation simple, énumérations par leur nom, dates ISO) : le front les met en forme.
 *
 * @param depuis  la dernière version approuvée par la partie (la version 1 si elle n'en a approuvé aucune)
 * @param jusqua  la version courante
 * @param auteurs les auteurs des versions de {@code depuis + 1} à {@code jusqua}
 */
public record Ecart(int depuis, int jusqua, List<Auteur> auteurs, List<Changement> champs, List<EcartLigne> lignes) {

    public Ecart {
        auteurs = List.copyOf(auteurs);
        champs = List.copyOf(champs);
        lignes = List.copyOf(lignes);
    }

    /** Un champ changé : son code ({@code volume}, {@code caracteristiques}…), sa valeur avant, après ; {@code null} : vide. */
    public record Changement(String champ, String avant, String apres) {
    }

    public enum Nature {
        AJOUTEE, RETIREE, MODIFIEE
    }

    /** @param rang à partir de 0 */
    public record EcartLigne(int rang, Nature nature, List<Changement> champs) {

        public EcartLigne {
            champs = List.copyOf(champs);
        }
    }

    public boolean vide() {
        return champs.isEmpty() && lignes.isEmpty();
    }

    public static Ecart entre(VersionContrat avant, VersionContrat apres, List<Auteur> auteurs) {
        ContenuContrat a = avant.contenu();
        ContenuContrat b = apres.contenu();
        List<Changement> champs = new ArrayList<>();
        compare(champs, "caracteristiques", a, b, ContenuContrat::caracteristiques);
        compare(champs, "conditionsRetiraison", a, b, ContenuContrat::conditionsRetiraison);
        compare(champs, "aRetirerAvantLe", a, b, ContenuContrat::aRetirerAvantLe);
        compare(champs, "solde", a, b, c -> c.paiement().solde());
        compare(champs, "acompte", a, b, c -> c.paiement().acompte());
        List<EcartLigne> lignes = new ArrayList<>();
        int n = Math.max(a.lignes().size(), b.lignes().size());
        for (int rang = 0; rang < n; rang++) {
            LigneContrat la = rang < a.lignes().size() ? a.lignes().get(rang) : null;
            LigneContrat lb = rang < b.lignes().size() ? b.lignes().get(rang) : null;
            List<Changement> changes = new ArrayList<>();
            compare(changes, "appellation", la, lb, LigneContrat::appellation);
            compare(changes, "millesime", la, lb, LigneContrat::millesime);
            compare(changes, "denomination", la, lb, LigneContrat::denomination);
            compare(changes, "compte", la, lb, LigneContrat::compte);
            compare(changes, "volume", la, lb, LigneContrat::volume);
            compare(changes, "degre", la, lb, LigneContrat::degre);
            compare(changes, "prix", la, lb, LigneContrat::prix);
            if (la == null) {
                lignes.add(new EcartLigne(rang, Nature.AJOUTEE, changes));
            } else if (lb == null) {
                lignes.add(new EcartLigne(rang, Nature.RETIREE, changes));
            } else if (!changes.isEmpty()) {
                lignes.add(new EcartLigne(rang, Nature.MODIFIEE, changes));
            }
        }
        return new Ecart(avant.numero(), apres.numero(), auteurs, champs, lignes);
    }

    /**
     * RG-CA-14 : l'écart entre deux versions d'une retiraison : date d'enlèvement, « dernière » et, par ligne du
     * contrat (rang), le volume retiré.
     */
    public static Ecart entre(VersionRetiraison avant, VersionRetiraison apres, List<Auteur> auteurs) {
        ContenuRetiraison a = avant.contenu();
        ContenuRetiraison b = apres.contenu();
        List<Changement> champs = new ArrayList<>();
        compare(champs, "dateEnlevement", a, b, ContenuRetiraison::dateEnlevement);
        compare(champs, "derniere", a, b, ContenuRetiraison::derniere);
        List<EcartLigne> lignes = new ArrayList<>();
        SortedSet<Integer> rangs = new TreeSet<>();
        a.lignes().forEach(l -> rangs.add(l.rang()));
        b.lignes().forEach(l -> rangs.add(l.rang()));
        for (int rang : rangs) {
            LigneRetiraison la = a.lignes().stream().filter(l -> l.rang() == rang).findFirst().orElse(null);
            LigneRetiraison lb = b.lignes().stream().filter(l -> l.rang() == rang).findFirst().orElse(null);
            List<Changement> changes = new ArrayList<>();
            compare(changes, "volume", la, lb, LigneRetiraison::volume);
            if (la == null) {
                lignes.add(new EcartLigne(rang, Nature.AJOUTEE, changes));
            } else if (lb == null) {
                lignes.add(new EcartLigne(rang, Nature.RETIREE, changes));
            } else if (!changes.isEmpty()) {
                lignes.add(new EcartLigne(rang, Nature.MODIFIEE, changes));
            }
        }
        return new Ecart(avant.numero(), apres.numero(), auteurs, champs, lignes);
    }

    private static <T> void compare(List<Changement> changes, String champ, T avant, T apres,
            Function<T, ?> valeur) {
        String va = avant == null ? null : brut(valeur.apply(avant));
        String vb = apres == null ? null : brut(valeur.apply(apres));
        if (!Objects.equals(va, vb)) {
            changes.add(new Changement(champ, va, vb));
        }
    }

    private static String brut(Object valeur) {
        if (valeur == null) {
            return null;
        }
        if (valeur instanceof BigDecimal nombre) {
            return nombre.toPlainString();
        }
        if (valeur instanceof Enum<?> e) {
            return e.name();
        }
        return valeur.toString();
    }
}
