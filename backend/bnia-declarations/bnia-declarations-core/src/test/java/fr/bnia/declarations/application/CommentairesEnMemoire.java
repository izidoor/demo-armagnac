package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.Commentaire;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Commentaires en mémoire : attribue les identifiants ; rend des copies, que seul {@code mettreAJour} enregistre. */
final class CommentairesEnMemoire implements Commentaires {

    private final AtomicLong sequence = new AtomicLong(500);
    private final Map<Long, Commentaire> commentaires = new LinkedHashMap<>();

    @Override
    public Commentaire inserer(Commentaire c) {
        long id = sequence.getAndIncrement();
        commentaires.put(id, copie(c, id));
        return parId(id).orElseThrow();
    }

    @Override
    public void mettreAJour(Commentaire c) {
        commentaires.put(c.id(), copie(c, c.id()));
    }

    @Override
    public Optional<Commentaire> parId(long id) {
        return Optional.ofNullable(commentaires.get(id)).map(c -> copie(c, id));
    }

    @Override
    public List<Commentaire> deLaDsm(long dsm) {
        return commentaires.values().stream().filter(c -> c.dsm() == dsm).map(c -> copie(c, c.id())).toList();
    }

    @Override
    public void supprimer(long id) {
        commentaires.remove(id);
    }

    @Override
    public void supprimerDeLaDsm(long dsm) {
        commentaires.values().removeIf(c -> c.dsm() == dsm);
    }

    private static Commentaire copie(Commentaire c, long id) {
        return Commentaire.reconstituer(id, c.dsm(), c.ligne(), c.ligneSupprimee(), c.auteur(), c.cote(), c.ecritLe(),
                c.modifieLe(), c.texte(), c.interne(), c.motif());
    }
}
