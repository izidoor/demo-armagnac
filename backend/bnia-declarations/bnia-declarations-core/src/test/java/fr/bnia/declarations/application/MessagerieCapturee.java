package fr.bnia.declarations.application;

import fr.bnia.operateurs.domain.partage.Acteur;
import java.util.ArrayList;
import java.util.List;

/** Le port vers la messagerie, qui retient les motifs déposés et les conversations supprimées. */
final class MessagerieCapturee implements Messagerie {

    record Motif(long dsm, long auteur, String texte) {
    }

    final List<Motif> motifs = new ArrayList<>();
    final List<Long> supprimees = new ArrayList<>();

    @Override
    public void deposerMotif(long dsm, Acteur auteur, String motif) {
        motifs.add(new Motif(dsm, auteur.utilisateur(), motif));
    }

    @Override
    public void supprimerConversation(long dsm) {
        supprimees.add(dsm);
    }
}
