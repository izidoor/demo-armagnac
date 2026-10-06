package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Cvi;
import fr.bnia.operateurs.domain.NumeroAccise;
import fr.bnia.operateurs.domain.Siret;
import java.util.List;

/** Lectures du back-office (ADR-004) : listing, groupes, doublons. */
public interface ListingOperateurs {

    /** Trié par raison sociale, puis par code. */
    List<OperateurLigne> lister(FiltreOperateurs filtre);

    /** RG-OPE-09 : les autres fiches du groupe, actives ou non, triées par code. */
    List<OperateurResume> duGroupe(String groupeNormalise, long sauf);

    /** RG-OPE-09 : les groupes existants, un par forme normalisée, qui contiennent le texte. */
    List<String> groupes(String recherche);

    /**
     * RG-OPE-06, 08 : les fiches qui portent déjà l'une des valeurs ; un critère {@code null} est ignoré.
     *
     * @param sauf l'opérateur modifié, ou {@code null} à la création
     */
    List<Doublon> doublons(String raisonSocialeNormalisee, Cvi cvi, Siret siret, NumeroAccise accise, Long sauf);
}
