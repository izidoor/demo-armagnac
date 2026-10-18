package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.facturation.domain.EtatPiece;
import java.util.Optional;

/**
 * Une pièce décrite à {@code messagerie} (spec 15, RG-MSG-17) : seule une facture ou un avoir émis dont le client
 * est un opérateur est conversable ; son libellé est le sens et le numéro. Sans contrôle de droits, que la messagerie
 * vérifie elle-même. Appelé par l'infra, qui implémente {@code ObjetsConversables}.
 */
@UseCase
public class DecrirePiece {

    private final FactureRepository factures;

    public DecrirePiece(FactureRepository factures) {
        this.factures = factures;
    }

    /** Une pièce et ce qui la désigne dans sa conversation. */
    public record PieceDecrite(long operateur, String libelle) {
    }

    /** Vide si la pièce n'existe pas, n'est pas émise ou n'a pas d'opérateur pour client. */
    public Optional<PieceDecrite> decrire(long id) {
        return factures.parId(id)
                .filter(f -> f.etat() == EtatPiece.EMISE && f.acheteur().client() != null && f.numero().isPresent())
                .map(f -> new PieceDecrite(Long.parseLong(f.acheteur().client().identifiant()),
                        f.sens().libelle() + " " + f.numero().orElseThrow()));
    }
}
