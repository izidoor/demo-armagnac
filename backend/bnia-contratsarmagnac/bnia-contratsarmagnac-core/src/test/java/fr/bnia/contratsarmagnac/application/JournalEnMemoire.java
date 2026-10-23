package fr.bnia.contratsarmagnac.application;

import fr.bnia.contratsarmagnac.domain.Fait;
import java.util.ArrayList;
import java.util.List;

final class JournalEnMemoire implements JournalContrats {

    private record Entree(long contrat, EntreeJournal entree) {
    }

    private final List<Entree> entrees = new ArrayList<>();

    @Override
    public void ajouter(long contrat, Fait fait) {
        entrees.add(new Entree(contrat, new EntreeJournal(fait.auteur().le(), fait.auteur().partie(),
                fait.auteur().utilisateur(), fait.auteur().nom(), fait.action(), fait.detail())));
    }

    @Override
    public List<EntreeJournal> duContrat(long contrat) {
        return entrees.stream().filter(e -> e.contrat() == contrat).map(Entree::entree).toList();
    }
}
