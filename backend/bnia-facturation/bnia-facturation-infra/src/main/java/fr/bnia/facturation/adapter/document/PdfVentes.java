package fr.bnia.facturation.adapter.document;

import static fr.bnia.facturation.adapter.document.PdfPiece.euros;
import static fr.bnia.facturation.adapter.document.PdfPiece.nombre;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.application.EditionVentes;
import fr.bnia.facturation.application.Ventes;
import fr.bnia.facturation.domain.Totaux;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Le PDF des ventes d'une période (RG-FAC-30, A-FAC-10) : gabarit Thymeleaf {@code gabarits/facturation/ventes.html}
 * rendu par OpenHTMLtoPDF, A4 portrait ; synthèse par article (les quantités ne se somment que par article),
 * récapitulatif de TVA, totaux. Le gabarit ne reçoit que des textes déjà mis en forme.
 */
@Component
class PdfVentes implements EditionVentes {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TemplateEngine moteur;

    PdfVentes() {
        ClassLoaderTemplateResolver resolveur = new ClassLoaderTemplateResolver();
        resolveur.setPrefix("gabarits/");
        resolveur.setSuffix(".html");
        resolveur.setTemplateMode(TemplateMode.HTML);
        resolveur.setCharacterEncoding("UTF-8");
        moteur = new TemplateEngine();
        moteur.setTemplateResolver(resolveur);
    }

    @Override
    public byte[] pdf(Ventes ventes, LocalDate editeLe) {
        Context contexte = new Context(Locale.FRANCE);
        contexte.setVariable("periode", "du " + ventes.du().format(DATE) + " au " + ventes.au().format(DATE));
        contexte.setVariable("editeLe", editeLe.format(DATE));
        contexte.setVariable("pieces", ventes.lignes().stream().map(l -> l.piece()).distinct().count());
        contexte.setVariable("articles", ventes.parArticle().stream().map(a -> {
            Map<String, String> rangee = new HashMap<>();
            rangee.put("code", a.code());
            rangee.put("designation", a.designation());
            rangee.put("quantite", nombre(a.quantite(), "#,##0.00##") + (a.unite() == null ? "" : " " + a.unite()));
            rangee.put("ht", euros(a.montantHt()));
            return rangee;
        }).toList());
        contexte.setVariable("recapitulatif", ventes.recapitulatifTva().stream().map(t -> {
            CategorieTva c = t.categorie();
            return Map.of("categorie", c.libelle() + (c.motif() == null ? "" : " : " + c.motif()),
                    "base", euros(t.base()), "tva", euros(t.tva()));
        }).toList());
        Totaux totaux = ventes.totaux();
        contexte.setVariable("totaux", Map.of("ht", euros(totaux.ht()), "tva", euros(totaux.tva()),
                "ttc", euros(totaux.ttc())));
        String html = moteur.process("facturation/ventes", contexte);
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
}
