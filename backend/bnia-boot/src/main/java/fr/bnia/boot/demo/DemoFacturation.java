package fr.bnia.boot.demo;

import fr.bnia.commun.application.StockageDocuments;
import fr.bnia.commun.application.Transactions;
import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.application.FacturerDsm;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.facturation.application.AnnoterPiece;
import fr.bnia.facturation.application.Clients;
import fr.bnia.facturation.application.DroitsFacturation;
import fr.bnia.facturation.application.EditionPiece;
import fr.bnia.facturation.application.EmettrePiece;
import fr.bnia.facturation.application.FactureRepository;
import fr.bnia.facturation.application.JournalFacturation;
import fr.bnia.facturation.application.NumerotationRepository;
import fr.bnia.facturation.application.ParametrerVendeur;
import fr.bnia.facturation.application.PublicationEvenements;
import fr.bnia.facturation.application.VendeurRepository;
import fr.bnia.facturation.domain.Adresse;
import fr.bnia.facturation.domain.Vendeur;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Données de démonstration de la facturation (spec 08 §7.1, lots 1, 2 et 5), en profil {@code demo} seulement,
 * après les déclarations :
 * <ul>
 * <li>le vendeur, le BNIA, tel qu'imprimé sur les factures d'exemple ;</li>
 * <li>Armagnacs de Gascogne ({@code N320001}), onze DSM validées : les huit plus anciennes sont facturées et
 * émises, chacune à la fin du mois qui suit sa période (les ventes s'étalent ainsi sur l'année, lot 5), au total
 * de la facture d'exemple (2 152,90 € TTC), une note au journal de la première ; la neuvième est demandée, un
 * brouillon en attente ; les deux dernières restent à facturer, en lot (lot 2) ;</li>
 * <li>Maison Durand, Condom ({@code 000001}) : sa DSM validée est facturée et émise à la fin du mois qui suit sa
 * campagne ;</li>
 * <li>les cinq négociants et dix producteurs ajoutés pour le volume ({@link OperateursFacturesDemo}) : chaque DSM
 * validée est facturée et émise à la fin du mois qui suit sa période, au plus tard la veille ;</li>
 * <li>le Domaine de Laubade ({@code 000003}), sans SIRET : sa DSM validée reste à facturer, et le refus nomme ce
 * qui manque.</li>
 * </ul>
 *
 * <p>Tout passe par les cas d'usage, au nom d'Anne Lamarque, ADMIN du BNIA. L'émission datée du passé passe par
 * une instance d'{@link EmettrePiece} propre à la démonstration, réglée sur une horloge qu'elle avance de date en
 * date, dans l'ordre chronologique (numéros continus), chaque émission dans sa transaction. Le vendeur déjà
 * paramétré, rien n'est rechargé : le chargeur est relançable.
 */
@Component
@Profile("demo")
@Order(5)
public class DemoFacturation implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoFacturation.class);
    /** Les DSM d'Armagnacs de Gascogne émises, des plus anciennes ; la suivante reste en brouillon. */
    static final int EMISES_GASCOGNE = 8;

    private final ParametrerVendeur vendeur;
    private final FacturerDsm facturer;
    private final AnnoterPiece annoter;
    private final GererDsm dsms;
    private final OperateursLecture lecture;
    private final Transactions transactions;
    private final HorlogeReglable horloge;
    private final EmettrePiece emettreLe;

    public DemoFacturation(ParametrerVendeur vendeur, FacturerDsm facturer, AnnoterPiece annoter, GererDsm dsms,
            OperateursLecture lecture, Transactions transactions, Clock horloge, FactureRepository factures,
            VendeurRepository vendeurs, NumerotationRepository numerotations, JournalFacturation journal,
            Clients clients, EditionPiece edition, StockageDocuments stockage, PublicationEvenements evenements,
            DroitsFacturation droits) {
        this.vendeur = vendeur;
        this.facturer = facturer;
        this.annoter = annoter;
        this.dsms = dsms;
        this.lecture = lecture;
        this.transactions = transactions;
        this.horloge = new HorlogeReglable(horloge);
        this.emettreLe = new EmettrePiece(factures, vendeurs, numerotations, journal, clients, edition, stockage,
                evenements, droits, this.horloge);
    }

    @Override
    public void run(ApplicationArguments arguments) {
        Acteur anne = new Acteur(DemoPersonnelBnia.anne(lecture));
        fr.bnia.facturation.api.Acteur emettrice = new fr.bnia.facturation.api.Acteur(anne.utilisateur());
        if (vendeur.consulter(emettrice).isPresent()) {
            LOG.info("Démo : facturation déjà chargée.");
            return;
        }
        vendeur.enregistrer(emettrice, new Vendeur("Bureau National Interprofessionnel de l'Armagnac",
                new Adresse("11 place de la Liberté", "BP 3", "32800", "Eauze", "France", "FR"), null, null,
                "77699995500029", "FR55776999955", "FR7610278022000002025240143", "CMCIFR2A", "05 62 08 11 00",
                "compta@armagnac.fr", false, null, null));

        List<DsmLigne> gascogne = validees(anne, "N320001");
        List<DsmLigne> condom = validees(anne, "000001");
        List<DsmLigne> aEmettre = new ArrayList<>(gascogne.subList(0, EMISES_GASCOGNE));
        aEmettre.addAll(condom);
        for (OperateursFacturesDemo.Operateur o : OperateursFacturesDemo.OPERATEURS) {
            aEmettre.addAll(validees(anne, o.code()));
        }
        Map<Long, Long> brouillons = facturer.facturer(anne, aEmettre.stream().map(DsmLigne::id).toList()).stream()
                .collect(Collectors.toMap(FacturerDsm.DsmFacturee::dsm, FacturerDsm.DsmFacturee::brouillon));
        facturer.facturer(anne, List.of(gascogne.get(EMISES_GASCOGNE).id()));
        LOG.info("Démo : brouillon de la DSM de {} d'Armagnacs de Gascogne en attente.",
                gascogne.get(EMISES_GASCOGNE).periode().fin());

        // Dans l'ordre chronologique des émissions, pour que les numéros suivent les dates (RG-FAC-12).
        record Emission(long piece, LocalDate le) {
        }
        List<Emission> emissions = new ArrayList<>();
        for (DsmLigne dsm : aEmettre) {
            emissions.add(new Emission(brouillons.get(dsm.id()), emissionDe(dsm)));
        }
        emissions.sort(Comparator.comparing(Emission::le));
        for (Emission e : emissions) {
            horloge.regler(e.le());
            String numero = transactions.dansUneNouvelle(() -> emettreLe.emettre(emettrice, e.piece()))
                    .numero().orElseThrow();
            LOG.info("Démo : facture {} émise le {}.", numero, e.le());
        }
        horloge.relacher();
        annoter.annoter(emettrice, brouillons.get(gascogne.getFirst().id()),
                "Facture envoyée aussi par courrier, à la demande du service comptable.");
    }

    /** La fin du mois qui suit la période de la DSM, au plus tard la veille d'aujourd'hui. */
    private LocalDate emissionDe(DsmLigne dsm) {
        LocalDate fin = dsm.periode().fin().plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate veille = LocalDate.now(horloge.reelle()).minusDays(1);
        return fin.isAfter(veille) ? veille : fin;
    }

    /** Les DSM validées de l'opérateur, de la plus ancienne à la plus récente. */
    private List<DsmLigne> validees(Acteur anne, String code) {
        long operateur = lecture.operateurParCode(code).orElseThrow().id();
        return dsms.lister(anne, operateur).stream().map(GererDsm.DsmDeLOperateur::dsm)
                .filter(d -> d.etat() == EtatDsm.VALIDEE)
                .sorted(Comparator.comparing(d -> d.periode().debut()))
                .toList();
    }

    /** L'horloge de l'émission datée : réglée sur un jour passé (à 9 h), sinon l'horloge de l'application. */
    static final class HorlogeReglable extends Clock {

        private final Clock reelle;
        private volatile Instant reglee;

        HorlogeReglable(Clock reelle) {
            this.reelle = reelle;
        }

        void regler(LocalDate jour) {
            reglee = jour.atTime(LocalTime.of(9, 0)).atZone(reelle.getZone()).toInstant();
        }

        void relacher() {
            reglee = null;
        }

        Clock reelle() {
            return reelle;
        }

        @Override
        public ZoneId getZone() {
            return reelle.getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return reelle.withZone(zone);
        }

        @Override
        public Instant instant() {
            Instant r = reglee;
            return r == null ? reelle.instant() : r;
        }
    }
}
