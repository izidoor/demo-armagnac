package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.api.Article;
import fr.bnia.facturation.api.ArticleRef;
import fr.bnia.facturation.api.CatalogueArticles;
import fr.bnia.facturation.api.ConditionPaiement;
import fr.bnia.facturation.api.LigneDemandee.ArticleDemande;
import fr.bnia.facturation.domain.CoteJournal;
import fr.bnia.facturation.domain.EntreeJournal;
import fr.bnia.facturation.domain.EvenementJournal;
import fr.bnia.facturation.domain.Facture;
import fr.bnia.facturation.domain.LigneFacture;
import fr.bnia.facturation.domain.LigneFacture.LigneArticle;
import fr.bnia.facturation.domain.LigneFacture.LigneGroupe;
import fr.bnia.facturation.domain.LigneFacture.LigneInformative;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Modifier un brouillon (UC-FAC-2) : en-tête et lignes, enregistrés d'un coup. RG-FAC-03 à 08, 14, 17, 26. La date
 * de l'opération et l'acheteur ne se modifient pas (RG-FAC-11, 17 révisées le 2026-10-15).
 */
@UseCase
public class ModifierBrouillon {

    /**
     * Le brouillon tel que saisi.
     *
     * @param version la version de la pièce sur laquelle la saisie a été faite
     */
    public record Saisie(String libellePeriode, ConditionPaiement condition, String bonDeCommande, String noteEntete,
            String notePied, List<LigneSaisie> lignes, long version) {

        public Saisie {
            lignes = List.copyOf(lignes);
        }
    }

    /** Une ligne saisie, dans l'ordre d'impression ; {@code numero} vide pour une ligne nouvelle. */
    public sealed interface LigneSaisie {

        Integer numero();

        /**
         * Une ligne article : existante, elle garde son article et sa TVA, et une ligne reçue ne change que de
         * désignation ; nouvelle, elle se résout dans le catalogue de sa source (RG-FAC-03).
         *
         * @param prixUnitaire facultatif pour une ligne nouvelle : sinon, celui de l'article
         * @param designation  facultative pour une ligne nouvelle : sinon, celle de l'article
         */
        record ArticleSaisi(Integer numero, ArticleRef article, String designation, String unite, BigDecimal quantite,
                BigDecimal prixUnitaire) implements LigneSaisie {
        }

        record TexteSaisi(Integer numero, String texte) implements LigneSaisie {
        }

        record GroupeSaisi(Integer numero, String titre, List<LigneSaisie> lignes) implements LigneSaisie {

            public GroupeSaisi {
                lignes = List.copyOf(lignes);
            }
        }
    }

    private final FactureRepository factures;
    private final VendeurRepository vendeurs;
    private final JournalFacturation journal;
    private final Catalogues catalogues;
    private final Controles controles;
    private final Clock horloge;

    public ModifierBrouillon(FactureRepository factures, VendeurRepository vendeurs, JournalFacturation journal,
            DroitsFacturation droits, List<CatalogueArticles> catalogues, Clock horloge) {
        this.factures = factures;
        this.vendeurs = vendeurs;
        this.journal = journal;
        this.catalogues = new Catalogues(catalogues);
        this.controles = new Controles(droits);
        this.horloge = horloge;
    }

    /**
     * RG-FAC-17 : enregistre l'en-tête et les lignes du brouillon, journalisé {@code MODIFIEE} côté vendeur.
     *
     * @return le brouillon enregistré et ce qui empêche encore son émission (RG-FAC-09)
     */
    public VuePiece modifier(Acteur acteur, long id, Saisie saisie) {
        controles.exigerGestion(acteur);
        Facture piece = piece(id);
        piece.exigerBrouillon();
        List<LigneFacture> lignes = saisie.lignes().stream().map(l -> ligne(piece, l)).toList();
        piece.modifier(new Facture.EnTete(saisie.libellePeriode(), saisie.condition(), saisie.bonDeCommande(),
                saisie.noteEntete(), saisie.notePied()), lignes, saisie.version());
        Facture enregistree = factures.mettreAJour(piece);
        journal.ajouter(EntreeJournal.par(controles.auteur(acteur), id, horloge.instant(), EvenementJournal.MODIFIEE,
                CoteJournal.VENDEUR, null));
        return new VuePiece(enregistree, enregistree.empechements(vendeurs.charger()), true);
    }

    /**
     * RG-FAC-03, 26 : les articles actifs de tous les catalogues, à la date de l'opération du brouillon, proposés à
     * l'ajout d'une ligne.
     */
    public List<Article> articlesProposes(Acteur acteur, long id) {
        controles.exigerGestion(acteur);
        Facture piece = piece(id);
        piece.exigerBrouillon();
        return catalogues.actifs(piece.dateOperation());
    }

    private LigneFacture ligne(Facture piece, LigneSaisie saisie) {
        LocalDate date = piece.dateOperation();
        return switch (saisie) {
            case LigneSaisie.ArticleSaisi a when a.numero() == null -> {
                if (a.article() == null || a.article().source() == null || a.article().code() == null) {
                    throw new RegleEnfreinte("RG-FAC-03", "Une ligne article ajoutée désigne son article.");
                }
                yield catalogues.resoudre(new ArticleDemande(a.article(), a.quantite(), a.prixUnitaire(),
                        a.designation()), date);
            }
            case LigneSaisie.ArticleSaisi a -> {
                if (!(existante(piece, a.numero()) instanceof LigneArticle ligne)) {
                    throw inconnue(a.numero());
                }
                yield ligne.saisie(a.designation(), a.unite(), a.quantite(), a.prixUnitaire());
            }
            case LigneSaisie.TexteSaisi t -> new LigneInformative(t.texte(), numero(t), false);
            case LigneSaisie.GroupeSaisi g -> new LigneGroupe(g.titre(), g.lignes().stream().map(l -> {
                if (l instanceof LigneSaisie.GroupeSaisi) {
                    throw new RegleEnfreinte("RG-FAC-04", "Le groupe « " + g.titre() + " » ne contient pas d'autre groupe.");
                }
                return ligne(piece, l);
            }).toList(), numero(g), false);
        };
    }

    private static int numero(LigneSaisie saisie) {
        return saisie.numero() == null ? 0 : saisie.numero();
    }

    private static LigneFacture existante(Facture piece, int numero) {
        return piece.ligne(numero).orElseThrow(() -> inconnue(numero));
    }

    private static RegleEnfreinte inconnue(int numero) {
        return new RegleEnfreinte("RG-FAC-17", "La ligne n° " + numero + " n'est pas une ligne de ce brouillon.");
    }

    private Facture piece(long id) {
        return factures.parId(id).orElseThrow(() -> new ElementIntrouvable("Pièce introuvable : n° " + id + "."));
    }
}
