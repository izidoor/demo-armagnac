package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.ActionJournalDeclarations;
import java.util.ArrayList;
import java.util.List;

/** Journal des déclarations en mémoire. */
final class JournalEnMemoire implements JournalDeclarations {

    private final List<EntreeJournal> entrees = new ArrayList<>();

    @Override
    public void ajouter(EntreeJournal entree) {
        entrees.add(entree);
    }

    @Override
    public List<EntreeJournal> deLaDsm(long dsm) {
        return entrees.reversed().stream().filter(e -> e.dsm() != null && e.dsm() == dsm).toList();
    }

    @Override
    public List<EntreeJournal> deLOperateur(long operateur) {
        return entrees.reversed().stream().filter(e -> e.operateur() == operateur).toList();
    }

    @Override
    public void supprimerDeLaDsm(long dsm) {
        entrees.removeIf(e -> e.dsm() != null && e.dsm() == dsm);
    }

    /** Les actions, de la plus ancienne à la plus récente. */
    List<ActionJournalDeclarations> actions() {
        return entrees.stream().map(EntreeJournal::action).toList();
    }
}
