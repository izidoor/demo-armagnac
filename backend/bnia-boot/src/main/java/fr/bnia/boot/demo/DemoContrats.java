package fr.bnia.boot.demo;

import fr.bnia.commun.application.Transactions;
import fr.bnia.commun.domain.Appellation;
import fr.bnia.commun.domain.Millesime;
import fr.bnia.contratsarmagnac.application.ConsulterContrats;
import fr.bnia.contratsarmagnac.application.ContratRepository;
import fr.bnia.contratsarmagnac.application.ExecuterContrats;
import fr.bnia.contratsarmagnac.application.JournalContrats;
import fr.bnia.contratsarmagnac.application.NegocierContrats;
import fr.bnia.contratsarmagnac.application.NegocierContrats.SaisieContrat;
import fr.bnia.contratsarmagnac.application.Numerotation;
import fr.bnia.contratsarmagnac.application.ParametresContrats;
import fr.bnia.contratsarmagnac.application.PublicationEvenements;
import fr.bnia.contratsarmagnac.application.SaisieContenu;
import fr.bnia.contratsarmagnac.application.SaisieContenu.SaisieLigne;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.contratsarmagnac.domain.Solde;
import fr.bnia.declarations.application.partage.DeclarationsLecture;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import fr.bnia.declarations.domain.partage.StockEstimeVue.LigneStockEstimeVue;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Données de démonstration des contrats (spec 06 §7.1), en profil {@code demo} seulement, après les déclarations et
 * la facturation :
 * <ul>
 * <li>lot 3, l'historique : vingt contrats exécutés sur les douze derniers mois entre les opérateurs hors scénario
 * (acheteurs : Maison Larressingle, Négoce des Landes, les négociants du volume de la facturation ; vendeurs : Domaine
 * de Laubade, les producteurs du volume), pour remplir les mercuriales et les listes. Chacun porte une ligne du stock
 * estimé sous bois de son vendeur, au prix d'un barème par compte d'âge (le CA-05 : 1 750 € / hl AP au compte 16,
 * 1 300 € au compte 7). Ouverts par l'acheteur, approuvés par le vendeur, puis clos sans retiraison : aucun mouvement
 * de contrat, les stocks restent intacts. Ils sont datés dans le passé par des instances des cas d'usage propres à la
 * démonstration, réglées sur une horloge qu'elles avancent, sans e-mail ;</li>
 * <li>lot 1 : un projet ouvert par Henri Castex ({@code admin@negociant.fr}, Armagnacs de Gascogne), 3 hl de Hors
 * d'âge {@code 20+} à 45 % et 2 400 € / hl AP, qui attend Jeanne Durand ({@code admin@producteur.fr}, Maison Durand,
 * Condom) : un filet si la saisie en direct traîne. Le fil de la démonstration se joue sur ces deux comptes.</li>
 * </ul>
 *
 * <p>Gascogne ayant déjà un contrat, rien n'est rechargé : le chargeur est relançable.
 */
@Component
@Profile("demo")
@Order(7)
public class DemoContrats implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoContrats.class);

    /** Nombre de contrats de l'historique (spec 06 §7.1). */
    static final int HISTORIQUE = 20;

    private final NegocierContrats negocier;
    private final ConsulterContrats consulter;
    private final OperateursLecture lecture;
    private final DeclarationsLecture declarations;
    private final Transactions transactions;
    private final DemoFacturation.HorlogeReglable horloge;
    private final NegocierContrats negocierLe;
    private final ExecuterContrats executerLe;

    public DemoContrats(NegocierContrats negocier, ConsulterContrats consulter, OperateursLecture lecture,
            DeclarationsLecture declarations, ContratRepository contrats, Numerotation numerotation,
            JournalContrats journal, PublicationEvenements evenements, Transactions transactions, Clock horloge) {
        this.negocier = negocier;
        this.consulter = consulter;
        this.lecture = lecture;
        this.declarations = declarations;
        this.transactions = transactions;
        this.horloge = new DemoFacturation.HorlogeReglable(horloge);
        ParametresContrats parametres = new ParametresContrats("");
        this.negocierLe = new NegocierContrats(contrats, numerotation, journal, courriel -> {
        }, lecture, declarations, parametres, this.horloge);
        this.executerLe = new ExecuterContrats(contrats, journal, courriel -> {
        }, evenements, lecture, declarations, parametres, this.horloge);
    }

    @Override
    public void run(ApplicationArguments arguments) {
        long gascogne = lecture.operateurParCode("N320001").orElseThrow().id();
        long durand = lecture.operateurParCode("000001").orElseThrow().id();
        Acteur henri = membre("admin@negociant.fr");
        if (!consulter.duPortail(henri, gascogne).isEmpty()) {
            LOG.info("Démo : contrats déjà chargés.");
            return;
        }
        historique();
        ContratAchatArmagnac projet = negocier.ouvrir(henri, gascogne, new SaisieContrat(Partie.ACHETEUR, durand,
                new SaisieContenu(List.of(new SaisieLigne(Appellation.ARMAGNAC, null, "Hors d'âge", "20+",
                        new BigDecimal("3"), new BigDecimal("45"), new BigDecimal("2400"))), null, null, null,
                        Solde.TRENTE_JOURS_FIN_DE_MOIS, null)));
        LOG.info("Démo : projet de contrat {} d'Armagnacs de Gascogne, à approuver par la Maison Durand.",
                projet.numero());
    }

    /** Une partie de l'historique et le membre qui agit pour elle. */
    private record Partenaire(long operateur, Acteur membre) {
    }

    /**
     * Vingt contrats, du plus ancien (il y a dix mois et demi environ) au plus récent (il y a cinq semaines), tous
     * datés avant le mois courant : ouverture, approbation trois jours après, clôture dix jours plus tard.
     */
    private void historique() {
        Acteur saisie = membre(OperateursFacturesDemo.EMAIL_SAISIE);
        List<Partenaire> acheteurs = new ArrayList<>(List.of(partenaire("N320002", membre("admin@larressingle.fr")),
                partenaire("N400001", membre("admin@landes.fr"))));
        List<Partenaire> vendeurs = new ArrayList<>(List.of(partenaire("000003", membre("admin@laubade.fr"))));
        for (OperateursFacturesDemo.Operateur o : OperateursFacturesDemo.OPERATEURS) {
            (o.negociant() ? acheteurs : vendeurs).add(partenaire(o.code(), saisie));
        }
        LocalDate aujourdhui = LocalDate.now(horloge.reelle());
        int charges = 0;
        try {
            for (int i = HISTORIQUE - 1; i >= 0; i--) {
                Partenaire acheteur = acheteurs.get(i % acheteurs.size());
                Partenaire vendeur = vendeurs.get(i % vendeurs.size());
                LocalDate conclusion = aujourdhui.minusDays(35 + 15L * i);
                if (contrat(i, acheteur, vendeur, conclusion)) {
                    charges++;
                }
            }
        } finally {
            horloge.relacher();
        }
        LOG.info("Démo : {} contrats exécutés sur les douze derniers mois (mercuriales).", charges);
    }

    /**
     * Un contrat de l'historique, dans sa transaction : une ligne du stock sous bois du vendeur, au plus 30 % de son
     * stock estimé.
     *
     * @return {@code false} si le vendeur n'a aucun stock sous bois
     */
    private boolean contrat(int rang, Partenaire acheteur, Partenaire vendeur, LocalDate conclusion) {
        List<LigneStockEstimeVue> stock = declarations.stockEstime(vendeur.operateur()).lignes().stream()
                .filter(l -> l.cle() == CleConditionnement.MB || l.cle() == CleConditionnement.NB)
                .filter(l -> !l.estime().estNul() && !l.estime().estNegatif())
                .toList();
        if (stock.isEmpty()) {
            return false;
        }
        LigneStockEstimeVue ligne = stock.get(rang % stock.size());
        BigDecimal degre = BigDecimal.valueOf(40 + rang % 9);
        BigDecimal plafond = ligne.estime().litres().multiply(new BigDecimal("0.30"))
                .divide(degre, 0, RoundingMode.DOWN);
        BigDecimal volume = BigDecimal.valueOf(2 + rang % 6L).min(plafond).max(BigDecimal.ONE);
        OperateurVue vue = lecture.operateur(vendeur.operateur()).orElseThrow();
        Appellation appellation = vue.appellations().stream().findFirst().orElse(Appellation.BAS_ARMAGNAC);
        SaisieLigne saisie = ligne.cle() == CleConditionnement.MB
                ? new SaisieLigne(appellation, ligne.millesime(), null, null, volume, degre,
                        prix(CompteDAgeDsm.calcule(new Millesime(ligne.millesime()), conclusion),
                                rang))
                : new SaisieLigne(appellation, null, ligne.denomination(), ligne.compte().toString(), volume, degre,
                        prix(ligne.compte(), rang));
        SaisieContenu contenu = new SaisieContenu(List.of(saisie), null, null, null,
                rang % 3 == 0 ? Solde.COMPTANT : Solde.TRENTE_JOURS_FIN_DE_MOIS, null);
        return transactions.dansUneNouvelle(() -> {
            horloge.regler(conclusion.minusDays(3));
            long id = negocierLe.ouvrir(acheteur.membre(), acheteur.operateur(),
                    new SaisieContrat(Partie.ACHETEUR, vendeur.operateur(), contenu)).id();
            horloge.regler(conclusion);
            negocierLe.approuver(vendeur.membre(), vendeur.operateur(), id, 1);
            horloge.regler(conclusion.plusDays(10));
            executerLe.cloturer(acheteur.membre(), acheteur.operateur(), id);
            return true;
        });
    }

    /**
     * Le barème de l'historique, en € HT / hl AP : 1 300 € au compte 7, 50 € de plus par année de compte (1 750 € au
     * compte 16, comme le CA-05), 900 € au moins ; un écart de -25 à +50 € d'un contrat à l'autre.
     */
    private static BigDecimal prix(CompteDAgeDsm compte, int rang) {
        int annees = compte.compareTo(CompteDAgeDsm.de(0)) <= 0 ? 0 : Integer.parseInt(compte.toString()
                .replace("+", ""));
        int base = Math.max(900, 1300 + 50 * (annees - 7));
        return BigDecimal.valueOf(base - 25 + 25L * (rang % 4));
    }

    private Partenaire partenaire(String code, Acteur membre) {
        return new Partenaire(lecture.operateurParCode(code).orElseThrow().id(), membre);
    }

    private Acteur membre(String email) {
        return new Acteur(lecture.utilisateurParEmail(email).orElseThrow());
    }
}
