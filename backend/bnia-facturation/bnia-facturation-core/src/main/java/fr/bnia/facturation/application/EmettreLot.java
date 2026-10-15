package fr.bnia.facturation.application;

import fr.bnia.commun.application.Transactions;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.facturation.api.Acteur;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Émettre une sélection de brouillons (UC-FAC-2, A-FAC-6, ADR-022). RG-FAC-19, et celles de l'émission. */
@UseCase
public class EmettreLot {

    /**
     * Le compte rendu d'un brouillon de la sélection : son numéro s'il a été émis, sinon le motif du refus.
     *
     * @param numero vide si le brouillon a été refusé
     * @param motif  vide s'il a été émis
     */
    public record CompteRendu(long id, String numero, String motif) {

        public boolean emis() {
            return numero != null;
        }
    }

    private final EmettrePiece emettre;
    private final Transactions transactions;
    private final Controles controles;

    public EmettreLot(EmettrePiece emettre, Transactions transactions, DroitsFacturation droits) {
        this.emettre = emettre;
        this.transactions = transactions;
        this.controles = new Controles(droits);
    }

    /**
     * RG-FAC-19 : chaque brouillon est émis dans sa propre transaction, dans l'ordre de la sélection ; un refus
     * n'annule pas les autres et ne consomme pas de numéro. Un brouillon sélectionné deux fois ne compte qu'une.
     */
    public List<CompteRendu> emettre(Acteur acteur, List<Long> ids) {
        controles.exigerGestion(acteur);
        List<CompteRendu> comptesRendus = new ArrayList<>();
        for (long id : new LinkedHashSet<>(ids)) {
            try {
                String numero = transactions.dansUneNouvelle(() -> emettre.emettre(acteur, id)).numero().orElseThrow();
                comptesRendus.add(new CompteRendu(id, numero, null));
            } catch (RegleEnfreinte | ElementIntrouvable refus) {
                comptesRendus.add(new CompteRendu(id, null, refus.getMessage()));
            }
        }
        return comptesRendus;
    }
}
