package fr.bnia.facturation.adapter.document;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import fr.bnia.facturation.api.CategorieTva;
import fr.bnia.facturation.api.Origine;
import fr.bnia.facturation.application.EditionPiece;
import fr.bnia.facturation.domain.Acheteur;
import fr.bnia.facturation.domain.Adresse;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.LigneFacture;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import fr.bnia.facturation.domain.LigneFacture.LigneInformative;
import fr.bnia.facturation.domain.LigneTva;
import fr.bnia.facturation.domain.NatureOperation;
import fr.bnia.facturation.domain.Totaux;
import fr.bnia.facturation.domain.Vendeur;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Le PDF d'une pièce émise (RG-FAC-15, A-FAC-10) : gabarit Thymeleaf {@code gabarits/facturation/piece.html}
 * rendu par OpenHTMLtoPDF, A4 portrait, d'après la facture d'exemple. Le gabarit ne reçoit que des textes déjà
 * mis en forme : montants en euros, quantités, dates à la française.
 */
@Component
class PdfPiece implements EditionPiece {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TemplateEngine moteur;

    PdfPiece() {
        ClassLoaderTemplateResolver resolveur = new ClassLoaderTemplateResolver();
        resolveur.setPrefix("gabarits/");
        resolveur.setSuffix(".html");
        resolveur.setTemplateMode(TemplateMode.HTML);
        resolveur.setCharacterEncoding("UTF-8");
        moteur = new TemplateEngine();
        moteur.setTemplateResolver(resolveur);
    }

    @Override
    public byte[] pdf(Facture piece) {
        Vendeur vendeur = piece.vendeur().orElseThrow(() -> new IllegalStateException("Pièce non émise"));
        Context contexte = new Context(Locale.FRANCE);
        contexte.setVariable("titre", piece.titre());
        contexte.setVariable("sens", piece.sens().libelle());
        contexte.setVariable("numero", piece.numero().orElseThrow());
        contexte.setVariable("vendeur", vendeur(vendeur));
        contexte.setVariable("acheteur", acheteur(piece.acheteur()));
        contexte.setVariable("informations", informations(piece));
        contexte.setVariable("noteEntete", piece.noteEntete());
        contexte.setVariable("lignes", lignes(piece.lignes()));
        contexte.setVariable("recapitulatif", piece.recapitulatifTva().stream().map(PdfPiece::tva).toList());
        Totaux totaux = piece.totaux();
        contexte.setVariable("totaux", Map.of("ht", euros(totaux.ht()), "tva", euros(totaux.tva()),
                "ttc", euros(totaux.ttc())));
        contexte.setVariable("notePied", piece.notePied());
        contexte.setVariable("mentions", vendeur.mentions(piece.acheteur().categorie()));
        contexte.setVariable("reglement", "IBAN " + vendeur.iban() + " · BIC " + vendeur.bic());
        String html = moteur.process("facturation/piece", contexte);
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

    private static Map<String, Object> vendeur(Vendeur v) {
        List<String> lignes = new ArrayList<>(adresse(v.siege()));
        if (v.adresseFacturation() != null) {
            lignes.add("Adresse de facturation : " + String.join(", ", adresse(v.adresseFacturation())));
        }
        lignes.add("Tél. " + v.telephone() + " · " + v.email());
        lignes.add("SIRET " + v.siret() + " · TVA " + v.numeroTva());
        return Map.of("nom", v.nom(), "lignes", lignes);
    }

    private static Map<String, Object> acheteur(Acheteur a) {
        List<String> lignes = new ArrayList<>(adresse(a.adresseFacturation()));
        if (a.client() != null) {
            lignes.add("N° client : " + a.client().code());
        }
        if (a.siren() != null) {
            lignes.add("SIREN " + a.siren() + (a.numeroTva() == null ? "" : " · TVA " + a.numeroTva()));
        } else if (a.numeroTva() != null) {
            lignes.add("TVA " + a.numeroTva());
        }
        if (a.adresseLivraison() != null) {
            lignes.add("Livraison : " + String.join(", ", adresse(a.adresseLivraison())));
        }
        return Map.of("nom", a.nom(), "lignes", lignes);
    }

    private static List<String> adresse(Adresse a) {
        return Stream.of(a.ligne1(), a.ligne2(), joindre(a.codePostal(), a.ville()), a.pays())
                .filter(l -> l != null && !l.isBlank()).toList();
    }

    private static String joindre(String codePostal, String ville) {
        return Stream.of(codePostal, ville).filter(t -> t != null).reduce((x, y) -> x + " " + y).orElse(null);
    }

    /** RG-FAC-15 : dates, période, échéance et condition, origine, bon de commande, nature de l'opération. */
    private static Map<String, String> informations(Facture piece) {
        Map<String, String> infos = new LinkedHashMap<>();
        infos.put("Date d'émission", piece.dateEmission().orElseThrow().format(DATE));
        infos.put("Date de l'opération", piece.dateOperation().format(DATE));
        if (piece.libellePeriode() != null) {
            infos.put("Période", piece.libellePeriode());
        }
        infos.put("Échéance", piece.echeance().orElseThrow().format(DATE) + " (" + piece.condition().libelle() + ")");
        Origine origine = piece.origine();
        if (origine != null) {
            infos.put("Référence", origine.reference());
        }
        if (piece.bonDeCommande() != null) {
            infos.put("Bon de commande", piece.bonDeCommande());
        }
        infos.put("Nature de l'opération", piece.natureOperation().map(NatureOperation::libelle).orElse("—"));
        return infos;
    }

    /** Les rangées du tableau : articles, textes, titres et sous-totaux de groupe. */
    private static List<Map<String, Object>> lignes(List<LigneFacture> lignes) {
        List<Map<String, Object>> rangees = new ArrayList<>();
        for (LigneFacture ligne : lignes) {
            switch (ligne) {
                case LigneGroupe g -> {
                    rangees.add(Map.of("sorte", "groupe", "texte", g.titre()));
                    g.lignes().forEach(l -> rangees.add(rangee(l, true)));
                    rangees.add(Map.of("sorte", "sous-total", "texte", "Sous-total " + g.titre(),
                            "ht", euros(g.sousTotalHt())));
                }
                default -> rangees.add(rangee(ligne, false));
            }
        }
        return rangees;
    }

    private static Map<String, Object> rangee(LigneFacture ligne, boolean dansGroupe) {
        return switch (ligne) {
            case LigneArticle a -> Map.of("sorte", "article", "texte", a.designation(), "retrait", dansGroupe,
                    "quantite", nombre(a.quantite(), "#,##0.00##") + (a.unite() == null ? "" : " " + a.unite()),
                    "prix", nombre(a.prixUnitaire(), "#,##0.00##") + " €", "ht", euros(a.montantHt()),
                    "tva", a.tva().libelle());
            case LigneInformative i -> Map.of("sorte", "texte", "texte", i.texte(), "retrait", dansGroupe);
            case LigneGroupe g -> throw new IllegalArgumentException("Groupe imbriqué");
        };
    }

    private static Map<String, String> tva(LigneTva t) {
        CategorieTva c = t.categorie();
        return Map.of("categorie", c.libelle() + (c.motif() == null ? "" : " : " + c.motif()), "base", euros(t.base()),
                "tva", euros(t.tva()));
    }

    private static String euros(BigDecimal montant) {
        return nombre(montant, "#,##0.00") + " €";
    }

    private static String nombre(BigDecimal valeur, String masque) {
        DecimalFormatSymbols symboles = DecimalFormatSymbols.getInstance(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        return new DecimalFormat(masque, symboles).format(valeur);
    }
}
