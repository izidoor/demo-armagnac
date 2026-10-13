package fr.bnia.declarations.application;

import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.declarations.domain.CalendrierDeclaratif.DsmPassee;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.declarations.domain.LigneDsm;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** DSM en mémoire : attribue les identifiants (DSM et lignes) et incrémente les versions. */
final class DsmEnMemoire implements DsmRepository, DsmLecture {

    private final AtomicLong sequence = new AtomicLong(100);
    private final Map<Long, DeclarationStocksMouvements> dsms = new LinkedHashMap<>();

    @Override
    public DeclarationStocksMouvements inserer(DeclarationStocksMouvements dsm) {
        if (nonValideeDe(dsm.operateur()).isPresent()) {
            throw new ConflitUnicite("RG-DEC-05", "Une DSM non validée existe déjà.");
        }
        return stocker(dsm, sequence.getAndIncrement(), 0);
    }

    @Override
    public DeclarationStocksMouvements mettreAJour(DeclarationStocksMouvements dsm) {
        if (dsms.get(dsm.id()).version() != dsm.version()) {
            throw new ModificationConcurrente(null, "modifiée");
        }
        return stocker(dsm, dsm.id(), dsm.version() + 1);
    }

    /** Pour les tests : place une DSM telle quelle, dans un état donné. */
    DeclarationStocksMouvements forcer(DeclarationStocksMouvements dsm, EtatDsm etat) {
        DeclarationStocksMouvements forcee = DeclarationStocksMouvements.reconstituer(dsm.id(), dsm.operateur(), dsm.type(),
                dsm.periode(), etat, dsm.dateDia(), dsm.lignes(), dsm.envoyeeLe(), dsm.valideeLe(), dsm.valideePar(),
                dsm.version());
        dsms.put(dsm.id(), forcee);
        return forcee;
    }

    private DeclarationStocksMouvements stocker(DeclarationStocksMouvements dsm, long id, long version) {
        List<LigneDsm> lignes = dsm.lignes().stream()
                .map(l -> new LigneDsm(l.id() != null ? l.id() : sequence.getAndIncrement(), l.identite(), l.origine(),
                        l.volumes()))
                .toList();
        dsms.put(id, DeclarationStocksMouvements.reconstituer(id, dsm.operateur(), dsm.type(), dsm.periode(), dsm.etat(),
                dsm.dateDia(), lignes, dsm.envoyeeLe(), dsm.valideeLe(), dsm.valideePar(), version));
        return parId(id).orElseThrow();
    }

    /** Une copie : l'agrégat chargé ne modifie pas l'état stocké. */
    @Override
    public Optional<DeclarationStocksMouvements> parId(long id) {
        return Optional.ofNullable(dsms.get(id)).map(d -> DeclarationStocksMouvements.reconstituer(d.id(), d.operateur(),
                d.type(), d.periode(), d.etat(), d.dateDia(), d.lignes(), d.envoyeeLe(), d.valideeLe(), d.valideePar(),
                d.version()));
    }

    @Override
    public void supprimer(long id) {
        dsms.remove(id);
    }

    @Override
    public List<DsmPassee> dsmsDe(long operateur) {
        return dsms.values().stream().filter(d -> d.operateur() == operateur)
                .map(d -> new DsmPassee(d.id(), d.periode(), d.etat())).toList();
    }

    @Override
    public Optional<DeclarationStocksMouvements> nonValideeDe(long operateur) {
        return dsms.values().stream().filter(d -> d.operateur() == operateur && d.etat() != EtatDsm.VALIDEE)
                .findFirst().flatMap(d -> parId(d.id()));
    }

    @Override
    public boolean existeValideeCommencantLe(long operateur, LocalDate debut) {
        return dsms.values().stream().anyMatch(d -> d.operateur() == operateur && d.etat() == EtatDsm.VALIDEE
                && d.periode().debut().equals(debut));
    }

    @Override
    public List<DsmLigne> deLOperateur(long operateur) {
        return dsms.values().stream().filter(d -> d.operateur() == operateur)
                .map(d -> new DsmLigne(d.id(), d.operateur(), d.type(), d.periode(), d.etat(), d.envoyeeLe(),
                        d.valideeLe(), null))
                .toList()
                .reversed();
    }

    @Override
    public List<DsmLigne> rechercher(FiltreDsm filtre) {
        return dsms.values().stream()
                .filter(d -> filtre.operateur().map(o -> o == d.operateur()).orElse(true))
                .filter(d -> filtre.type().map(t -> t == d.type()).orElse(true))
                .filter(d -> filtre.etats().isEmpty() || filtre.etats().contains(d.etat()))
                .filter(d -> filtre.mois().map(m -> !m.atDay(1).isBefore(d.periode().debut())
                        && !m.atDay(1).isAfter(d.periode().fin())).orElse(true))
                .map(d -> new DsmLigne(d.id(), d.operateur(), d.type(), d.periode(), d.etat(), d.envoyeeLe(),
                        d.valideeLe(), null))
                .toList();
    }

    @Override
    public List<String> denominations(long operateur) {
        return List.of();
    }
}
