package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.NoteInterne;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

final class NotesEnMemoire implements NotesInternes {

    private final AtomicLong sequence = new AtomicLong(500);
    private final Map<Long, NoteInterne> notes = new LinkedHashMap<>();

    @Override
    public NoteInterne inserer(NoteInterne n) {
        long id = sequence.getAndIncrement();
        notes.put(id, copie(n, id));
        return parId(id).orElseThrow();
    }

    @Override
    public void mettreAJour(NoteInterne n) {
        notes.put(n.id(), copie(n, n.id()));
    }

    @Override
    public Optional<NoteInterne> parId(long id) {
        return Optional.ofNullable(notes.get(id)).map(n -> copie(n, id));
    }

    @Override
    public List<NoteInterne> deLaDsm(long dsm) {
        return notes.values().stream().filter(n -> n.dsm() == dsm).map(n -> copie(n, n.id())).toList();
    }

    @Override
    public void supprimer(long id) {
        notes.remove(id);
    }

    @Override
    public void supprimerDeLaDsm(long dsm) {
        notes.values().removeIf(n -> n.dsm() == dsm);
    }

    private static NoteInterne copie(NoteInterne n, long id) {
        return NoteInterne.reconstituer(id, n.dsm(), n.ligne(), n.ligneSupprimee(), n.auteur(), n.ecriteLe(),
                n.modifieeLe(), n.texte());
    }
}
