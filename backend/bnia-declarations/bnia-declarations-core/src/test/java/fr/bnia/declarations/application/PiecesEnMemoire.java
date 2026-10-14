package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.Drm;
import fr.bnia.declarations.domain.Justificatif;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Pièces en mémoire : attribue les identifiants ; rend des copies, que seul {@code mettreAJour} enregistre. */
final class PiecesEnMemoire implements Pieces {

    private final AtomicLong sequence = new AtomicLong(700);
    private final Map<Long, Justificatif> justificatifs = new LinkedHashMap<>();
    private final Map<Long, Drm> drms = new LinkedHashMap<>();

    @Override
    public Justificatif inserer(Justificatif j) {
        long id = sequence.getAndIncrement();
        justificatifs.put(id, copie(j, id));
        return justificatif(id).orElseThrow();
    }

    @Override
    public void mettreAJour(Justificatif j) {
        justificatifs.put(j.id(), copie(j, j.id()));
    }

    @Override
    public Optional<Justificatif> justificatif(long id) {
        return Optional.ofNullable(justificatifs.get(id)).map(j -> copie(j, id));
    }

    @Override
    public List<Justificatif> justificatifsDe(long dsm) {
        return justificatifs.values().stream().filter(j -> j.dsm() == dsm).map(j -> copie(j, j.id())).toList();
    }

    @Override
    public void supprimerJustificatif(long id) {
        justificatifs.remove(id);
    }

    @Override
    public Drm inserer(Drm drm) {
        long id = sequence.getAndIncrement();
        Drm inseree = new Drm(id, drm.dsm(), drm.mois(), drm.document(), drm.deposePar(), drm.deposeLe());
        drms.put(id, inseree);
        return inseree;
    }

    @Override
    public Optional<Drm> drm(long dsm, YearMonth mois) {
        return drms.values().stream().filter(d -> d.dsm() == dsm && d.mois().equals(mois)).findFirst();
    }

    @Override
    public List<Drm> drmsDe(long dsm) {
        return drms.values().stream().filter(d -> d.dsm() == dsm).sorted(Comparator.comparing(Drm::mois)).toList();
    }

    @Override
    public void supprimerDrm(long id) {
        drms.remove(id);
    }

    private static Justificatif copie(Justificatif j, long id) {
        return Justificatif.reconstituer(id, j.dsm(), j.ligne(), j.ligneSupprimee(), j.typeMouvement(), j.typePiece(),
                j.document(), j.reference(), j.deposePar(), j.deposeLe());
    }
}
