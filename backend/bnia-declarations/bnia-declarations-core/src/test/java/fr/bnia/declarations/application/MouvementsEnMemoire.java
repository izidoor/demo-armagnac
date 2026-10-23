package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.MouvementContrat;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Mouvements de contrat en mémoire ; identifiants attribués à l'insertion. */
final class MouvementsEnMemoire implements MouvementsContrat {

    private final List<MouvementContrat> mouvements = new ArrayList<>();
    private long sequence = 500;

    @Override
    public List<MouvementContrat> inserer(List<MouvementContrat> nouveaux) {
        List<MouvementContrat> inseres = nouveaux.stream().map(m -> new MouvementContrat(++sequence, m.operateur(),
                m.sens(), m.dateEnlevement(), m.identite(), m.volumeAp(), m.colonne(), m.contrat(), m.numeroContrat(),
                m.retiraison(), m.autrePartie(), m.dsm())).toList();
        mouvements.addAll(inseres);
        return inseres;
    }

    @Override
    public void inscrire(long mouvement, long dsm) {
        mouvements.replaceAll(m -> m.id() == mouvement ? m.inscritDans(dsm) : m);
    }

    @Override
    public List<MouvementContrat> deLOperateur(long operateur) {
        return mouvements.stream().filter(m -> m.operateur() == operateur)
                .sorted(Comparator.comparing(MouvementContrat::dateEnlevement)).toList();
    }

    @Override
    public List<MouvementContrat> enAttente(long operateur, PeriodeDeclaration periode) {
        return deLOperateur(operateur).stream().filter(m -> m.dsm().isEmpty())
                .filter(m -> !m.dateEnlevement().isBefore(periode.debut()) && !m.dateEnlevement().isAfter(periode.fin()))
                .toList();
    }

    @Override
    public List<MouvementContrat> inscritsDans(long dsm) {
        return mouvements.stream().filter(m -> m.dsm().equals(Optional.of(dsm))).toList();
    }
}
