package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.domain.ModificationConcurrente;
import fr.bnia.contratsarmagnac.domain.ContratAchatArmagnac;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.LigneContrat;
import fr.bnia.contratsarmagnac.domain.Mercuriale.LigneConclue;
import fr.bnia.contratsarmagnac.domain.Retiraison;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Les contrats en mémoire, qui attribuent les identifiants et les versions comme la base ; listes comprises. */
final class ContratsEnMemoire implements ContratRepository, ContratsLecture {

    private final Map<Long, ContratAchatArmagnac> contrats = new LinkedHashMap<>();
    private long suivant = 1;

    @Override
    public ContratAchatArmagnac inserer(ContratAchatArmagnac contrat) {
        return garder(copie(contrat, suivant++, 0));
    }

    @Override
    public ContratAchatArmagnac mettreAJour(ContratAchatArmagnac contrat) {
        if (!contrats.get(contrat.id()).version().equals(contrat.version())) {
            throw new ModificationConcurrente(null, "modifié entre-temps");
        }
        return garder(copie(contrat, contrat.id(), contrat.version() + 1));
    }

    @Override
    public Optional<ContratAchatArmagnac> parId(long id) {
        return Optional.ofNullable(contrats.get(id)).map(c -> copie(c, c.id(), c.version()));
    }

    @Override
    public List<ContratLigne> lister(Optional<Long> operateur, Optional<String> numero) {
        return contrats.values().stream()
                .filter(c -> operateur.map(o -> c.acheteur() == o || c.vendeur() == o).orElse(true))
                .filter(c -> numero.map(n -> c.numero().toLowerCase().contains(n.toLowerCase())).orElse(true))
                .sorted(Comparator.comparing((ContratAchatArmagnac c) -> c.courante().auteur().le()).reversed())
                .map(c -> {
                    var contenu = c.courante().contenu();
                    LigneContrat premiere = contenu.lignes().getFirst();
                    return new ContratLigne(c.id(), c.numero(), c.etat(), c.acheteur(), c.vendeur(), c.ouvertPar(),
                            c.partieQuiDoitApprouver(), c.retiraisons().stream()
                                    .flatMap(r -> r.partieQuiDoitValider().stream()).collect(Collectors.toSet()),
                            c.dateContrat(), c.courante().auteur().le(),
                            c.courante().numero(), contenu.lignes().size(),
                            premiere.millesime() != null ? premiere.millesime().toString()
                                    : premiere.denomination() + " " + premiere.compte(),
                            contenu.volumeAp(), contenu.montantHt(), Optional.ofNullable(contenu.aRetirerAvantLe()));
                })
                .toList();
    }

    @Override
    public List<LigneConclue> lignesConclues(LocalDate du, LocalDate au) {
        return contrats.values().stream()
                .filter(c -> c.etat() == EtatContrat.CONCLU || c.etat() == EtatContrat.EXECUTE)
                .filter(c -> c.dateContrat().filter(d -> !d.isBefore(du) && !d.isAfter(au)).isPresent())
                .flatMap(c -> c.courante().contenu().lignes().stream().map(l -> new LigneConclue(c.id(),
                        l.millesime() == null ? null : l.millesime().annee(), l.denomination(), l.compte(),
                        l.volumeAp(), l.prix())))
                .toList();
    }

    private ContratAchatArmagnac garder(ContratAchatArmagnac contrat) {
        contrats.put(contrat.id(), contrat);
        return contrat;
    }

    private static ContratAchatArmagnac copie(ContratAchatArmagnac c, long id, long version) {
        return ContratAchatArmagnac.reconstituer(id, c.numero(), c.acheteur(), c.vendeur(), c.ouvertPar(), c.etat(),
                c.dateContrat().orElse(null), c.versions(), c.approbations(), c.retiraisons().stream()
                        .map(r -> Retiraison.reconstituer(r.numero(), r.etat(), r.versions(), r.approbations()))
                        .toList(), c.retiraisonsNumerotees(), version);
    }
}
