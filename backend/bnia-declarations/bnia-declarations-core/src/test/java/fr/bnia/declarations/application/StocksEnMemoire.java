package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.AnneeDeclarative;
import fr.bnia.declarations.domain.DeclarationStocksMouvements;
import fr.bnia.declarations.domain.TypeDeclarant;
import fr.bnia.declarations.domain.partage.EtatDsm;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Les stocks déclarés, tirés des DSM en mémoire, comme l'adapter : lignes au stock de fin non nul. */
final class StocksEnMemoire implements StocksLecture {

    private final DsmEnMemoire dsms;

    StocksEnMemoire(DsmEnMemoire dsms) {
        this.dsms = dsms;
    }

    @Override
    public List<LigneStock> stocksDeclares(Optional<Long> operateur, Optional<TypeDeclarant> type) {
        return List.of(Monde.NEGOCIANT, Monde.PRODUCTEUR, Monde.SOMMEIL).stream()
                .filter(o -> operateur.map(o::equals).orElse(true))
                .flatMap(o -> dsms.dsmsDe(o).stream().filter(d -> d.etat() == EtatDsm.VALIDEE)
                        .max(Comparator.comparing(d -> d.periode().fin())).stream())
                .map(d -> dsms.parId(d.id()).orElseThrow())
                .filter(d -> type.map(t -> t == d.type()).orElse(true))
                .flatMap(StocksEnMemoire::lignes)
                .toList();
    }

    @Override
    public List<LigneStock> stocksDeLAnnee(AnneeDeclarative annee, long operateur) {
        return validees(operateur)
                .filter(d -> annee.contient(d.periode()))
                .flatMap(d -> toutes(d))
                .toList();
    }

    @Override
    public Set<AnneeDeclarative> anneesDeclarees(long operateur) {
        return validees(operateur).map(d -> AnneeDeclarative.contenant(d.periode().fin()))
                .collect(Collectors.toSet());
    }

    private Stream<DeclarationStocksMouvements> validees(long operateur) {
        return dsms.dsmsDe(operateur).stream().filter(d -> d.etat() == EtatDsm.VALIDEE)
                .map(d -> dsms.parId(d.id()).orElseThrow());
    }

    private static Stream<LigneStock> lignes(DeclarationStocksMouvements d) {
        return toutes(d).filter(l -> !l.stock().estNul());
    }

    private static Stream<LigneStock> toutes(DeclarationStocksMouvements d) {
        return d.lignes().stream()
                .map(l -> new LigneStock(d.operateur(), d.type(), d.id(), d.periode(), d.valideeLe(), l.identite(),
                        l.stockFin()));
    }
}
