package fr.bnia.declarations.application;

import fr.bnia.declarations.domain.MouvementContrat;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import java.util.List;

/** Port d'écriture et de lecture des mouvements de contrat (spec 06 §3.7, RG-CA-21 à 23). */
public interface MouvementsContrat {

    /** @return les mouvements, avec leur identifiant, dans le même ordre */
    List<MouvementContrat> inserer(List<MouvementContrat> mouvements);

    /** RG-CA-22 : le mouvement est inscrit dans cette DSM. */
    void inscrire(long mouvement, long dsm);

    /** RG-CA-11 : tous les mouvements de l'opérateur, par date d'enlèvement. */
    List<MouvementContrat> deLOperateur(long operateur);

    /** RG-CA-22 : ceux de l'opérateur datés dans la période et pas encore inscrits, par date d'enlèvement. */
    List<MouvementContrat> enAttente(long operateur, PeriodeDeclaration periode);

    /** RG-CA-23 : ceux inscrits dans la DSM, par contrat et retiraison. */
    List<MouvementContrat> inscritsDans(long dsm);
}
