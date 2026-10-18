package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import java.util.Optional;

/**
 * Une DSM décrite à {@code messagerie} (ADR-023, RG-MSG-04) : son opérateur et son libellé, sans contrôle de droits,
 * que la messagerie vérifie elle-même. Appelé par l'infra, qui implémente {@code ObjetsConversables}.
 */
@UseCase
public class DecrireDsm {

    private final DsmRepository dsms;

    public DecrireDsm(DsmRepository dsms) {
        this.dsms = dsms;
    }

    /** Une DSM et ce qui la désigne dans sa conversation. */
    public record DsmDecrite(long operateur, String libelle) {
    }

    /** Vide si la DSM n'existe pas. */
    public Optional<DsmDecrite> decrire(long id) {
        return dsms.parId(id).map(d -> new DsmDecrite(d.operateur(), "DSM " + d.periode().libelle()));
    }
}
