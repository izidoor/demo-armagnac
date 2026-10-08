package fr.bnia.referentiels.application;

import fr.bnia.commun.domain.AccesRefuse;

/** Droits sur le Paramétrage, pour les tests : l'acteur {@link #ACTEUR} gère, tout autre est refusé. */
final class Parametrage implements ControleParametrage {

    /** Un AGENT ou un ADMIN du BNIA. */
    static final long ACTEUR = 1;
    /** Un membre d'un opérateur, sans Paramétrage, qui gère les contacts de l'opérateur {@link #OPERATEUR}. */
    static final long MEMBRE = 2;
    static final long OPERATEUR = 50;

    static final Parametrage AUTORISE = new Parametrage();

    private Parametrage() {
    }

    @Override
    public void exigerGestion(long utilisateur) {
        if (utilisateur != ACTEUR) {
            throw new AccesRefuse("RG-OPE-31", "Le Paramétrage est réservé au BNIA.");
        }
    }

    @Override
    public void exigerGestionFiche(long utilisateur, long operateur) {
        if (utilisateur != ACTEUR && !(utilisateur == MEMBRE && operateur == OPERATEUR)) {
            throw new AccesRefuse("RG-OPE-31", "Vous ne gérez pas les contacts de cet opérateur.");
        }
    }
}
