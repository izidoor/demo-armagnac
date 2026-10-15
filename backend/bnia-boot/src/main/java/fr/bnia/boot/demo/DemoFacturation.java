package fr.bnia.boot.demo;

import fr.bnia.declarations.application.FacturerDsm;
import fr.bnia.declarations.application.GererDsm;
import fr.bnia.declarations.application.DsmLigne;
import fr.bnia.declarations.domain.partage.EtatDsm;
import fr.bnia.facturation.application.AnnoterPiece;
import fr.bnia.facturation.application.EmettrePiece;
import fr.bnia.facturation.application.ParametrerVendeur;
import fr.bnia.facturation.domain.Adresse;
import fr.bnia.facturation.domain.Vendeur;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Données de démonstration de la facturation (spec 08 §7.1, lots 1 et 2), en profil {@code demo} seulement, après les
 * déclarations :
 * <ul>
 * <li>le vendeur, le BNIA, tel qu'imprimé sur les factures d'exemple ;</li>
 * <li>Armagnacs de Gascogne ({@code N320001}) : sa plus ancienne DSM validée est facturée et la facture émise,
 * au total de la facture d'exemple (1 855,95 € HT, 2 152,90 € TTC), avec une note au journal ; les dix suivantes
 * restent à facturer, en lot (lot 2) ;</li>
 * <li>Maison Durand, Condom ({@code 000001}) : la facture de sa DSM validée est demandée, un brouillon en
 * attente ;</li>
 * <li>le Domaine de Laubade ({@code 000003}), sans SIRET : sa DSM validée reste à facturer, et le refus nomme ce
 * qui manque.</li>
 * </ul>
 *
 * <p>Tout passe par les cas d'usage, au nom d'Anne Lamarque, ADMIN du BNIA. Le vendeur déjà paramétré, rien
 * n'est rechargé : le chargeur est relançable.
 */
@Component
@Profile("demo")
@Order(5)
public class DemoFacturation implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoFacturation.class);

    private final ParametrerVendeur vendeur;
    private final FacturerDsm facturer;
    private final EmettrePiece emettre;
    private final AnnoterPiece annoter;
    private final GererDsm dsms;
    private final OperateursLecture lecture;

    public DemoFacturation(ParametrerVendeur vendeur, FacturerDsm facturer, EmettrePiece emettre, AnnoterPiece annoter,
            GererDsm dsms, OperateursLecture lecture) {
        this.vendeur = vendeur;
        this.facturer = facturer;
        this.emettre = emettre;
        this.annoter = annoter;
        this.dsms = dsms;
        this.lecture = lecture;
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

        List<Long> gascogne = validees(anne, "N320001");
        long piece = facturer.facturer(anne, List.of(gascogne.getFirst())).getFirst().brouillon();
        String numero = emettre.emettre(emettrice, piece).numero().orElseThrow();
        annoter.annoter(emettrice, piece, "Facture envoyée aussi par courrier, à la demande du service comptable.");
        LOG.info("Démo : facture {} d'Armagnacs de Gascogne émise.", numero);

        facturer.facturer(anne, validees(anne, "000001"));
        LOG.info("Démo : facture de la Maison Durand, Condom, demandée (brouillon en attente).");
    }

    /** Les DSM validées de l'opérateur, de la plus ancienne à la plus récente. */
    private List<Long> validees(Acteur anne, String code) {
        long operateur = lecture.operateurParCode(code).orElseThrow().id();
        return dsms.lister(anne, operateur).stream().map(GererDsm.DsmDeLOperateur::dsm)
                .filter(d -> d.etat() == EtatDsm.VALIDEE)
                .sorted(Comparator.comparing(d -> d.periode().debut()))
                .map(DsmLigne::id).toList();
    }
}
