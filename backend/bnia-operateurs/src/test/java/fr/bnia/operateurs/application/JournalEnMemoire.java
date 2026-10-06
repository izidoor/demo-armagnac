package fr.bnia.operateurs.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Journal des contacts en mémoire, pour les tests de cas d'usage. */
class JournalEnMemoire implements JournalContacts {

    final List<LigneJournal> lignes = new ArrayList<>();

    @Override
    public void ajouter(long operateur, LigneJournal ligne) {
        lignes.add(ligne);
    }

    @Override
    public List<LigneJournal> duContact(long operateur, long contact) {
        return lignes.stream().filter(l -> l.contact() == contact)
                .sorted(Comparator.comparing(LigneJournal::date).reversed()).toList();
    }
}
