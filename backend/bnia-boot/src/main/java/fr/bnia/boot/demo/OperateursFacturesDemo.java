package fr.bnia.boot.demo;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.util.List;

/**
 * Opérateurs de démonstration ajoutés pour donner du volume à la facturation (spec 08 §7.1, lot 5) : cinq
 * négociants dont toutes les DSM mensuelles de l'année civile en cours sont validées, jusqu'au mois dernier, sauf
 * Maison Saint-Puy, arrêtée au mois d'avant ({@link #EN_RETARD}) ; dix producteurs dont la DSM de la dernière année
 * déclarative close est validée ; toutes facturées et émises. Noms et identifiants fictifs.
 * Partagé par les chargeurs des opérateurs, des déclarations et de la facturation.
 *
 * <p>Un seul compte saisit leurs DSM, {@link #EMAIL_SAISIE}, AGENT de chacun ; le destinataire des factures est une
 * adresse saisie, sans compte.
 */
final class OperateursFacturesDemo {

    /**
     * Le négociant dont les DSM s'arrêtent deux mois avant le mois courant : la période du mois dernier est absente,
     * en retard dès le 6 du mois, seule ligne absente du listing du BNIA (RG-DEC-40, décision du 2026-10-21).
     */
    static final String EN_RETARD = "N320012";

    /** Le compte de saisie des DSM de ces opérateurs (décision du 2026-10-15). */
    static final String EMAIL_SAISIE = "saisie@demo.fr";

    /**
     * Un opérateur à facturer.
     *
     * @param sortiesFrance litres AP sortis en France, par mois (négociant) ou par an (producteur)
     * @param export        litres AP exportés, avec un DAE ; 0 : pas d'export
     */
    record Operateur(TypeOperateur type, String code, String raisonSociale, String codePostal, String ville,
            String region, Appellation appellation, int sortiesFrance, int export) {

        boolean negociant() {
            return type == TypeOperateur.NEGOCIANT;
        }
    }

    static final List<Operateur> OPERATEURS = List.of(
            new Operateur(TypeOperateur.NEGOCIANT, "N320010", "Caves de la Ténarèze", "32100", "Condom", "GERS",
                    Appellation.ARMAGNAC_TENAREZE, 420, 1350),
            new Operateur(TypeOperateur.NEGOCIANT, "N320011", "Comptoir des Armagnacs", "32800", "Eauze", "GERS",
                    Appellation.BAS_ARMAGNAC, 980, 2600),
            new Operateur(TypeOperateur.NEGOCIANT, "N320012", "Maison Saint-Puy", "32310", "Saint-Puy", "GERS",
                    Appellation.ARMAGNAC_TENAREZE, 260, 0),
            new Operateur(TypeOperateur.NEGOCIANT, "N400010", "Négoce du Bas-Armagnac", "40240",
                    "Labastide-d'Armagnac", "LANDES", Appellation.BAS_ARMAGNAC, 610, 1880),
            new Operateur(TypeOperateur.NEGOCIANT, "N470010", "Maison de l'Albret", "47600", "Nérac", "LOT-ET-GARONNE",
                    Appellation.ARMAGNAC_TENAREZE, 340, 720),
            new Operateur(TypeOperateur.PRODUCTEUR, "000020", "Domaine des Cinq Chênes", "32330", "Gondrin", "GERS",
                    Appellation.ARMAGNAC_TENAREZE, 640, 0),
            new Operateur(TypeOperateur.PRODUCTEUR, "000021", "Château de la Gélise", "40310", "Parleboscq", "LANDES",
                    Appellation.BAS_ARMAGNAC, 1250, 900),
            new Operateur(TypeOperateur.PRODUCTEUR, "000022", "Domaine du Pech", "32800", "Eauze", "GERS",
                    Appellation.BAS_ARMAGNAC, 380, 0),
            new Operateur(TypeOperateur.PRODUCTEUR, "000023", "Domaine de Lagraulet", "32330", "Lagraulet-du-Gers",
                    "GERS", Appellation.ARMAGNAC_TENAREZE, 820, 450),
            new Operateur(TypeOperateur.PRODUCTEUR, "000024", "Château des Sables Fauves", "40240",
                    "Mauvezin-d'Armagnac", "LANDES", Appellation.BAS_ARMAGNAC, 1560, 2100),
            new Operateur(TypeOperateur.PRODUCTEUR, "000025", "Domaine de la Baïse", "32190", "Vic-Fezensac", "GERS",
                    Appellation.ARMAGNAC_TENAREZE, 290, 0),
            new Operateur(TypeOperateur.PRODUCTEUR, "000026", "Domaine de Coulomé", "32110", "Nogaro", "GERS",
                    Appellation.BAS_ARMAGNAC, 710, 300),
            new Operateur(TypeOperateur.PRODUCTEUR, "000027", "Château de Montesquiou", "32320", "Montesquiou",
                    "GERS", Appellation.HAUT_ARMAGNAC, 180, 0),
            new Operateur(TypeOperateur.PRODUCTEUR, "000028", "Domaine des Landes d'Estang", "32240", "Estang",
                    "GERS", Appellation.BAS_ARMAGNAC, 960, 1200),
            new Operateur(TypeOperateur.PRODUCTEUR, "000029", "Domaine de la Losse", "47170", "Mézin",
                    "LOT-ET-GARONNE", Appellation.ARMAGNAC_TENAREZE, 430, 0));

    private OperateursFacturesDemo() {
    }

    /**
     * Le nombre de DSM validées de l'opérateur : un négociant, les mois de l'année écoulés jusqu'au mois dernier
     * (jusqu'à l'avant-dernier pour {@link #EN_RETARD}) ; un producteur, une.
     */
    static int dsmValidees(Operateur operateur, LocalDate aujourdhui) {
        if (!operateur.negociant()) {
            return 1;
        }
        return Math.max(0, aujourdhui.getMonthValue() - (EN_RETARD.equals(operateur.code()) ? 2 : 1));
    }

    /** Le rang de l'opérateur dans la liste : il fait varier ses volumes et ses identifiants. */
    static int rang(Operateur operateur) {
        return OPERATEURS.indexOf(operateur);
    }

    /** SIRET (clés de Luhn du SIREN et du SIRET justes) et n° de TVA (clé juste) fictifs, tirés du rang. */
    static String[] identifiantsLegaux(Operateur operateur) {
        String siren = completerLuhn(String.format("8%07d", 1300 + 137 * rang(operateur)));
        String siret = completerLuhn(siren + "0001");
        int cle = (int) ((12 + 3 * (Long.parseLong(siren) % 97)) % 97);
        return new String[] {siret, "FR" + String.format("%02d", cle) + siren};
    }

    /** Le volume d'un mois (négociant) ou de l'année (producteur), qui varie d'un mois et d'un opérateur à l'autre. */
    static String litres(int base, int rang, int mois) {
        int variation = 70 + (rang * 17 + mois * 23) % 61;
        return String.format("%d.%02d", base * variation / 100, (rang * 7 + mois * 11) % 100);
    }

    private static String completerLuhn(String debut) {
        for (int chiffre = 0; chiffre <= 9; chiffre++) {
            if (luhn(debut + chiffre)) {
                return debut + chiffre;
            }
        }
        throw new IllegalStateException("Aucune clé de Luhn pour " + debut);
    }

    private static boolean luhn(String chiffres) {
        int somme = 0;
        for (int i = 0; i < chiffres.length(); i++) {
            int c = chiffres.charAt(chiffres.length() - 1 - i) - '0';
            if (i % 2 == 1) {
                c *= 2;
                if (c > 9) {
                    c -= 9;
                }
            }
            somme += c;
        }
        return somme % 10 == 0;
    }
}
