package fr.bnia.boot.demo;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.referentiels.application.GererCentresInteret;
import fr.bnia.referentiels.application.GererCircuitsDistribution;
import fr.bnia.referentiels.application.GererClassesTva;
import fr.bnia.referentiels.application.GererContenances;
import fr.bnia.referentiels.application.GererLabels;
import fr.bnia.referentiels.application.GererLangues;
import fr.bnia.referentiels.application.GererMarques;
import fr.bnia.referentiels.application.GererMembresBnia;
import fr.bnia.referentiels.application.GererPays;
import fr.bnia.referentiels.application.GererProduitsCommercialises;
import fr.bnia.referentiels.application.GererRegions;
import fr.bnia.referentiels.application.GererTitres;
import fr.bnia.referentiels.application.GererTypesVisite;
import fr.bnia.referentiels.application.GererValeursSimples;
import fr.bnia.referentiels.application.GererZonesCommerciales;
import fr.bnia.referentiels.application.SaisiePays;
import fr.bnia.referentiels.application.SaisieTauxTva;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.LangueCertificat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Données de démonstration des référentiels (spec 01, §7.1), en profil {@code demo} seulement :
 * jamais dans la migration (ADR-005). Valeurs plausibles, présentées comme telles ; les valeurs
 * réelles viendront de la reprise AGI.
 *
 * <p>Tout passe par les cas d'usage, donc par les règles de gestion. Relancé sur une base qui les
 * contient déjà, le chargeur ne crée que ce qui manque.
 */
@Component
@Profile("demo")
@Order(1)
public class DemoReferentiels implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoReferentiels.class);

    private final GererZonesCommerciales zones;
    private final GererPays pays;
    private final GererRegions regions;
    private final GererTitres titres;
    private final GererLabels labels;
    private final GererTypesVisite typesVisite;
    private final GererLangues langues;
    private final GererCentresInteret centresInteret;
    private final GererMembresBnia membresBnia;
    private final GererMarques marques;
    private final GererCircuitsDistribution circuits;
    private final GererProduitsCommercialises produits;
    private final GererContenances contenances;
    private final GererClassesTva classesTva;
    private final ReferentielsLecture lecture;

    private int crees;

    public DemoReferentiels(GererZonesCommerciales zones, GererPays pays, GererRegions regions, GererTitres titres,
            GererLabels labels, GererTypesVisite typesVisite, GererLangues langues,
            GererCentresInteret centresInteret, GererMembresBnia membresBnia, GererMarques marques, GererCircuitsDistribution circuits,
            GererProduitsCommercialises produits, GererContenances contenances, GererClassesTva classesTva,
            ReferentielsLecture lecture) {
        this.zones = zones;
        this.pays = pays;
        this.regions = regions;
        this.titres = titres;
        this.labels = labels;
        this.typesVisite = typesVisite;
        this.langues = langues;
        this.centresInteret = centresInteret;
        this.membresBnia = membresBnia;
        this.marques = marques;
        this.circuits = circuits;
        this.produits = produits;
        this.contenances = contenances;
        this.classesTva = classesTva;
        this.lecture = lecture;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        crees = 0;
        charger();
        LOG.info("Démo : {} valeurs de référentiels créées.", crees);
    }

    void charger() {
        // Géographie : les continents comme zones commerciales (H-REF-5).
        valeurs(zones, "EUR", "Europe", "AMN", "Amérique du Nord", "AMS", "Amérique du Sud", "ASI", "Asie",
                "AFR", "Afrique", "OCE", "Océanie");
        paysExportateurs();
        creer(() -> regions.creer("GERS", "Gers", true));
        creer(() -> regions.creer("LANDES", "Landes", true));
        creer(() -> regions.creer("LOT-ET-GARONNE", "Lot-et-Garonne", true));
        creer(() -> regions.creer("GIRONDE", "Gironde", false));
        creer(() -> regions.creer("IDF", "Île-de-France", false));

        // Qualification.
        valeurs(titres, "M", "Monsieur", "MME", "Madame", "ME", "Maître", "DR", "Docteur");
        valeurs(labels, "AB", "Agriculture biologique", "HVE", "Haute valeur environnementale", "VD",
                "Vignobles & Découvertes");
        valeurs(typesVisite, "CHAI", "Visite de chai", "DEGUST", "Dégustation", "DISTIL", "Visite de la distillerie");
        valeurs(langues, "FR", "Français", "EN", "Anglais", "ES", "Espagnol", "DE", "Allemand", "IT", "Italien");
        valeurs(centresInteret, "COMPTA", "Comptabilité", "MARKETING", "Marketing", "PRODUCTION", "Production",
                "EXPORT", "Export");
        // Membres BNIA : valeurs fictives, le sens du champ est demandé au BNIA (Q-OPE-4).
        valeurs(membresBnia, "PROD", "Collège des producteurs", "NEGO", "Collège des négociants", "ASSOC",
                "Membre associé");

        // Présentation et commercialisation : marques fictives.
        valeurs(marques, "DEMO-1", "Domaine de la Démonstration", "DEMO-2", "Château Exemple", "DEMO-3",
                "Maison Fictive");
        valeurs(circuits, "DIRECT", "Vente directe", "CAVISTES", "Cavistes", "GMS", "Grande distribution", "CHR",
                "Cafés, hôtels, restaurants", "EXPORT", "Export");
        valeurs(produits, "ARMAGNAC", "Armagnac", "BLANCHE", "Blanche Armagnac", "FLOC", "Floc de Gascogne");
        for (String volume : List.of("0.05", "0.20", "0.35", "0.50", "0.70", "0.75", "1.00", "1.50", "3.00")) {
            creer(() -> contenances.creer(new BigDecimal(volume), null));
        }

        // Fiscalité : une classe « Normal » à 20 % depuis le 01/01/2014.
        if (creer(() -> classesTva.creer("NORMAL", "Normal"))) {
            long normal = lecture.classeTvaParCode("NORMAL").orElseThrow().id();
            classesTva.ajouterTaux(normal, new SaisieTauxTva(new BigDecimal("20"), LocalDate.of(2014, 1, 1), null));
        }
    }

    /**
     * Une trentaine de pays exportateurs, codés par leur code ISO, rattachés à leur zone par son code. Langue du certificat : espagnol pour
     * l'Espagne, italien pour l'Italie, anglais sinon (§7.1).
     */
    private void paysExportateurs() {
        Object[][] liste = {
                {"DE", "Allemagne", "EUR", true}, {"AT", "Autriche", "EUR", true}, {"BE", "Belgique", "EUR", true},
                {"DK", "Danemark", "EUR", true}, {"ES", "Espagne", "EUR", true}, {"FI", "Finlande", "EUR", true},
                {"IE", "Irlande", "EUR", true}, {"IT", "Italie", "EUR", true}, {"LU", "Luxembourg", "EUR", true},
                {"NL", "Pays-Bas", "EUR", true}, {"PL", "Pologne", "EUR", true}, {"PT", "Portugal", "EUR", true},
                {"CZ", "Tchéquie", "EUR", true}, {"SE", "Suède", "EUR", true}, {"NO", "Norvège", "EUR", false},
                {"GB", "Royaume-Uni", "EUR", false}, {"CH", "Suisse", "EUR", false},
                {"US", "États-Unis", "AMN", false}, {"CA", "Canada", "AMN", false}, {"MX", "Mexique", "AMN", false},
                {"BR", "Brésil", "AMS", false}, {"CN", "Chine", "ASI", false}, {"HK", "Hong Kong", "ASI", false},
                {"JP", "Japon", "ASI", false}, {"KR", "Corée du Sud", "ASI", false}, {"SG", "Singapour", "ASI", false},
                {"TW", "Taïwan", "ASI", false}, {"IN", "Inde", "ASI", false},
                {"AE", "Émirats arabes unis", "ASI", false}, {"ZA", "Afrique du Sud", "AFR", false},
                {"AU", "Australie", "OCE", false}, {"NZ", "Nouvelle-Zélande", "OCE", false}};
        for (Object[] p : liste) {
            String code = (String) p[0];
            LangueCertificat langue = switch (code) {
                case "ES" -> LangueCertificat.FR_ES;
                case "IT" -> LangueCertificat.FR_IT;
                default -> LangueCertificat.FR_EN;
            };
            long zone = lecture.zoneCommercialeParCode((String) p[2]).orElseThrow().id();
            creer(() -> pays.creer(new SaisiePays(code, (String) p[1], code, zone, (Boolean) p[3], langue, null,
                    null)));
        }
    }

    /** @param codesEtLibelles code, libellé, code, libellé… */
    private void valeurs(GererValeursSimples<?> liste, String... codesEtLibelles) {
        for (int i = 0; i < codesEtLibelles.length; i += 2) {
            String code = codesEtLibelles[i];
            String libelle = codesEtLibelles[i + 1];
            creer(() -> liste.creer(code, libelle));
        }
    }

    /** Crée la valeur si elle n'existe pas encore ; vrai si elle a été créée. */
    private boolean creer(Runnable creation) {
        try {
            creation.run();
            crees++;
            return true;
        } catch (ConflitUnicite dejaPresente) {
            return false;
        }
    }
}
