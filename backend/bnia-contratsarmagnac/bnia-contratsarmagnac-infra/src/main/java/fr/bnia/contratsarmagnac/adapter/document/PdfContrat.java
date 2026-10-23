package fr.bnia.contratsarmagnac.adapter.document;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import fr.bnia.commun.domain.Appellation;
import fr.bnia.contratsarmagnac.application.EditionsContrats;
import fr.bnia.contratsarmagnac.domain.Approbation;
import fr.bnia.contratsarmagnac.domain.ContenuContrat;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.LigneContrat;
import fr.bnia.contratsarmagnac.domain.Partie;
import fr.bnia.contratsarmagnac.domain.Solde;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Le PDF du contrat au formulaire CA-05 allégé (RG-CA-20, A-CA-8, ADR-020) : gabarit Thymeleaf
 * {@code gabarits/contratsarmagnac/contrat.html} rendu par OpenHTMLtoPDF, A4 portrait. Sans visa, échéancier,
 * rattachement, préavis, courtier ni mentions finales (vision). Le gabarit ne reçoit que des textes mis en forme.
 */
@Component
class PdfContrat implements EditionsContrats {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    /** L'ordre des cases du CA-05. */
    private static final List<Appellation> CASES = List.of(Appellation.ARMAGNAC, Appellation.BAS_ARMAGNAC,
            Appellation.ARMAGNAC_TENAREZE, Appellation.HAUT_ARMAGNAC, Appellation.BLANCHE_ARMAGNAC);

    private final TemplateEngine moteur;

    PdfContrat() {
        ClassLoaderTemplateResolver resolveur = new ClassLoaderTemplateResolver();
        resolveur.setPrefix("gabarits/");
        resolveur.setSuffix(".html");
        resolveur.setTemplateMode(TemplateMode.HTML);
        resolveur.setCharacterEncoding("UTF-8");
        moteur = new TemplateEngine();
        moteur.setTemplateResolver(resolveur);
    }

    @Override
    public byte[] pdf(EditionContrat edition) {
        ContratAchatArmagnac c = edition.contrat();
        ContenuContrat contenu = c.courante().contenu();
        Context contexte = new Context(Locale.FRANCE);
        contexte.setVariable("numero", c.numero());
        contexte.setVariable("campagne", edition.campagne().replace("-", " - "));
        contexte.setVariable("projet", c.etat() == EtatContrat.PROJET);
        contexte.setVariable("etat", c.etat() == EtatContrat.PROJET || c.etat() == EtatContrat.CONCLU ? null
                : c.etat().libelle());
        contexte.setVariable("parties", List.of(partie(edition.acheteur()), partie(edition.vendeur())));
        contexte.setVariable("lignes", contenu.lignes().stream().map(PdfContrat::ligne).toList());
        contexte.setVariable("totalAp", nombre(hl(contenu.volumeAp().litres())));
        contexte.setVariable("totalHt", euros(contenu.montantHt()));
        Set<Appellation> cochees = contenu.lignes().stream().map(LigneContrat::appellation).collect(Collectors.toSet());
        contexte.setVariable("appellations", CASES.stream().map(a -> Map.of("libelle", a.libelle(),
                "cochee", cochees.contains(a))).toList());
        contexte.setVariable("caracteristiques", contenu.caracteristiques());
        contexte.setVariable("conditions", contenu.conditionsRetiraison());
        contexte.setVariable("avant", contenu.aRetirerAvantLe() == null ? null : DATE.format(contenu.aRetirerAvantLe()));
        contexte.setVariable("acompte", contenu.paiement().acompte() == null ? null
                : nombre(contenu.paiement().acompte()) + " %");
        contexte.setVariable("comptant", contenu.paiement().solde() == Solde.COMPTANT);
        contexte.setVariable("etabli", c.dateContrat().map(DATE::format).orElse(null));
        contexte.setVariable("signatureVendeur", signature(c, Partie.VENDEUR));
        contexte.setVariable("signatureAcheteur", signature(c, Partie.ACHETEUR));
        String html = moteur.process("contratsarmagnac/contrat", contexte);
        try (ByteArrayOutputStream sortie = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(sortie);
            builder.run();
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Object> partie(PartieEditee p) {
        Map<String, Object> carte = new LinkedHashMap<>();
        carte.put("nom", p.raisonSociale());
        carte.put("adresse", p.adresse());
        carte.put("telephone", p.telephone().orElse(""));
        carte.put("numero", p.numero());
        carte.put("siret", p.siret().orElse(""));
        carte.put("cvi", p.cvi().orElse(null));
        return carte;
    }

    private static Map<String, String> ligne(LigneContrat l) {
        Map<String, String> carte = new LinkedHashMap<>();
        carte.put("appellation", l.appellation().libelle());
        carte.put("compte", l.compte().toString());
        carte.put("produit", l.millesime() != null ? l.millesime().toString() : l.denomination());
        carte.put("volume", nombre(l.volume()));
        carte.put("degre", nombre(l.degre()));
        carte.put("volumeAp", nombre(hl(l.volumeAp().litres())));
        carte.put("prix", nombre(l.prix()));
        carte.put("prixLitre", nombre(l.prixLitreVolume()));
        carte.put("montant", euros(l.montantHt()));
        return carte;
    }

    /** L'approbation de la version courante par la partie : son auteur et sa date ; vide si elle manque. */
    private static String signature(ContratAchatArmagnac c, Partie partie) {
        return c.approbationCourante(partie).map(Approbation::auteur)
                .map(a -> "Approuvé par " + a.nom() + " le " + DATE_HEURE.format(a.le().atZone(PARIS)))
                .orElse(null);
    }

    private static BigDecimal hl(BigDecimal litres) {
        return litres.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private static String euros(BigDecimal montant) {
        return nombre(montant) + " €";
    }

    private static String nombre(BigDecimal valeur) {
        DecimalFormatSymbols symboles = DecimalFormatSymbols.getInstance(Locale.FRANCE);
        // Espace simple comme séparateur des milliers : la police de base du PDF n'a pas l'espace fine insécable.
        symboles.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0.00", symboles).format(valeur);
    }
}
