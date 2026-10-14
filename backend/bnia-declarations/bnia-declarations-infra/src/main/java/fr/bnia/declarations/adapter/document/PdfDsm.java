package fr.bnia.declarations.adapter.document;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.application.CotisationDsm.Calculee;
import fr.bnia.declarations.application.CotisationDsm.Facturee;
import fr.bnia.declarations.application.CotisationDsm;
import fr.bnia.declarations.application.EditionsDsm;
import fr.bnia.declarations.application.FactureDsm;
import fr.bnia.declarations.domain.ColonneDsm;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.GrilleCotisation.TauxEnVigueur;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.LigneDsm;
import fr.bnia.declarations.domain.OngletDsm;
import fr.bnia.declarations.domain.PolitiqueCotisation.Detail;
import fr.bnia.declarations.domain.partage.EtatDsm;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Le PDF de la DSM (RG-DEC-35, ADR-020) : gabarit Thymeleaf {@code gabarits/declarations/dsm.html} rendu par
 * OpenHTMLtoPDF, A4 paysage. Le gabarit ne reçoit que des textes déjà mis en forme (cartes et listes) : volumes
 * au masque hl.l,cl, montants en euros, dates à la française.
 */
@Component
class PdfDsm implements EditionsDsm {

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");

    private final TemplateEngine moteur;

    PdfDsm() {
        ClassLoaderTemplateResolver resolveur = new ClassLoaderTemplateResolver();
        resolveur.setPrefix("gabarits/");
        resolveur.setSuffix(".html");
        resolveur.setTemplateMode(TemplateMode.HTML);
        resolveur.setCharacterEncoding("UTF-8");
        moteur = new TemplateEngine();
        moteur.setTemplateResolver(resolveur);
    }

    @Override
    public byte[] pdf(EditionDsm edition) {
        Context contexte = new Context(Locale.FRANCE);
        contexte.setVariable("entete", entete(edition));
        contexte.setVariable("provisoire", edition.dsm().etat() != EtatDsm.VALIDEE);
        contexte.setVariable("onglets", Arrays.stream(OngletDsm.values()).map(o -> onglet(edition.dsm(), o)).toList());
        contexte.setVariable("cotisation", cotisation(edition.cotisation(), edition.dsm().periode().fin()));
        String html = moteur.process("declarations/dsm", contexte);
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

    private static Map<String, Object> entete(EditionDsm edition) {
        DeclarationStocksMouvements dsm = edition.dsm();
        Map<String, Object> entete = new LinkedHashMap<>();
        entete.put("operateur", edition.operateur().code() + " – " + edition.operateur().raisonSociale());
        entete.put("type", dsm.type().libelle());
        entete.put("periode", dsm.periode().libelle() + " (du " + dsm.periode().debut().format(DATE) + " au "
                + dsm.periode().fin().format(DATE) + ")");
        entete.put("dateDia", dsm.dateDia() == null ? "—" : dsm.dateDia().format(DATE));
        entete.put("echeance", dsm.periode().echeance(dsm.type()).format(DATE));
        entete.put("etat", dsm.etat().libelle(edition.cote()));
        entete.put("validation", dsm.valideeLe() == null ? "—" : "le " + dsm.valideeLe().atZone(PARIS).format(DATE_HEURE)
                + (dsm.valideePar() == null ? "" : " par " + dsm.valideePar().nom()));
        return entete;
    }

    /** Un onglet du format unique : ses colonnes, une rangée par ligne, la rangée des totaux. */
    private static Map<String, Object> onglet(DeclarationStocksMouvements dsm, OngletDsm onglet) {
        List<ColonneDsm> colonnes = Arrays.stream(ColonneDsm.values()).filter(c -> c.onglet() == onglet).toList();
        boolean recapitulatif = onglet == OngletDsm.RECAPITULATIF;
        List<Map<String, Object>> entetes = new ArrayList<>();
        for (ColonneDsm c : colonnes) {
            if (recapitulatif && c == ColonneDsm.C26) {
                entetes.add(colonne("", "Total des entrées", false));
                entetes.add(colonne("", "Total des sorties", false));
            }
            entetes.add(colonne(String.valueOf(c.numero()), c.libelle(), c.griseePour(dsm.type())));
        }
        List<Map<String, Object>> rangees = new ArrayList<>();
        for (LigneDsm ligne : dsm.lignes()) {
            List<Map<String, Object>> cellules = new ArrayList<>();
            for (ColonneDsm c : colonnes) {
                if (recapitulatif && c == ColonneDsm.C26) {
                    cellules.add(cellule(ligne.totalEntrees(), false, true));
                    cellules.add(cellule(ligne.totalSorties(), false, true));
                }
                cellules.add(cellule(ligne.volume(c), c.griseePour(dsm.type()), c == ColonneDsm.C26));
            }
            IdentiteLigne identite = ligne.identite();
            rangees.add(Map.of("cle", identite.cle().name(),
                    "designation", identite.millesime() != null ? String.valueOf(identite.millesime().annee())
                            : identite.denomination().texte(),
                    "compte", identite.compte().toString(), "cellules", cellules));
        }
        List<Map<String, Object>> totaux = new ArrayList<>();
        for (ColonneDsm c : colonnes) {
            if (recapitulatif && c == ColonneDsm.C26) {
                totaux.add(cellule(somme(dsm, LigneDsm::totalEntrees), false, true));
                totaux.add(cellule(somme(dsm, LigneDsm::totalSorties), false, true));
            }
            totaux.add(cellule(dsm.total(c), c.griseePour(dsm.type()), true));
        }
        return Map.of("titre", onglet.libelle(), "colonnes", entetes, "rangees", rangees, "totaux", totaux);
    }

    private static VolumeAlcoolPur somme(DeclarationStocksMouvements dsm,
            Function<LigneDsm, VolumeAlcoolPur> volume) {
        return dsm.lignes().stream().map(volume).reduce(VolumeAlcoolPur.ZERO, VolumeAlcoolPur::plus);
    }

    private static Map<String, Object> colonne(String numero, String libelle, boolean grisee) {
        return Map.of("numero", numero, "libelle", libelle, "grisee", grisee);
    }

    /** Une cellule : vide si grisée (RG-DEC-16), ou nulle hors totaux, pour la lisibilité. */
    private static Map<String, Object> cellule(VolumeAlcoolPur volume, boolean grisee, boolean toujours) {
        String texte = grisee || (volume.estNul() && !toujours) ? "" : MasqueVolume.format(volume.litres());
        return Map.of("texte", texte, "grisee", grisee, "negatif", volume.estNegatif());
    }

    /** La page « Cotisation » (RG-DEC-31 à 34). */
    private static Map<String, Object> cotisation(CotisationDsm cotisation, LocalDate finPeriode) {
        Map<String, Object> page = new LinkedHashMap<>();
        switch (cotisation) {
            case Calculee c -> {
                Detail d = c.detail();
                TauxEnVigueur taux = d.taux();
                page.put("statut", "CALCULEE");
                page.put("taux", euros(taux.taux().montant()) + "/hlAP, dont " + pourcentage(taux.taux().pourcentageExonere())
                        + " exonérés de TVA, en vigueur " + taux.fin()
                        .map(f -> "du " + taux.taux().debut().format(DATE) + " au " + f.format(DATE))
                        .orElse("depuis le " + taux.taux().debut().format(DATE)));
                page.put("lignes", d.lignes().stream().map(l -> Map.of("libelle", l.ligne().libelle(),
                        "volume", MasqueVolume.format(l.volume().litres()),
                        "hectolitres", l.hectolitres().stripTrailingZeros().toPlainString().replace('.', ','),
                        "nonAssujettie", euros(l.nonAssujettie()), "assujettie", euros(l.assujettie()),
                        "total", euros(l.total()))).toList());
                page.put("totalNonAssujettie", euros(d.totalNonAssujettie()));
                page.put("totalAssujettie", euros(d.totalAssujettie()));
                page.put("totalHt", euros(d.totalHt()));
                page.put("tva", c.tva().map(t -> "TVA indicative (" + pourcentage(t.taux()) + " de la part assujettie) : "
                        + euros(t.montant())).orElse("TVA indicative non disponible"));
                page.put("ttc", c.ttc().map(PdfDsm::euros).orElse(null));
            }
            case CotisationDsm.NonCalculable n -> {
                page.put("statut", "NON_CALCULABLE");
                page.put("message", "Non calculable : aucun taux CVO n'est en vigueur au " + finPeriode.format(DATE)
                        + ", fin de la période.");
            }
            case Facturee f -> {
                page.put("statut", "FACTUREE");
                page.put("factures", f.documents().stream().map(doc -> Map.of(
                        "type", doc.type() == FactureDsm.Type.FACTURE ? "Facture" : "Avoir",
                        "numero", doc.numero(), "date", doc.date().format(DATE), "ht", euros(doc.ht()),
                        "tva", euros(doc.tva()), "ttc", euros(doc.ttc()))).toList());
                page.put("totalHt", euros(f.totalHt()));
            }
        }
        return page;
    }

    private static String euros(BigDecimal montant) {
        return nombre(montant) + " €";
    }

    private static String pourcentage(BigDecimal taux) {
        return nombre(taux) + " %";
    }

    private static String nombre(BigDecimal valeur) {
        DecimalFormatSymbols symboles = DecimalFormatSymbols.getInstance(Locale.FRANCE);
        // Espace simple comme séparateur des milliers : la police de base du PDF n'a pas l'espace fine insécable.
        symboles.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0.00", symboles).format(valeur);
    }
}
