package fr.bnia.operateurs.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.operateurs.domain.partage.Droits;

/**
 * RG-OPE-35 : un opérateur peut n'avoir aucun ADMIN, mais dès qu'il en a un, aucun de ses membres ne
 * retire, ne suspend ni ne rétrograde le dernier. Un ADMIN du BNIA le peut, sauf pour l'opérateur BNIA,
 * où la règle est absolue. Le cas d'usage compte les ADMIN actifs sous le verrou de l'opérateur (A-OPE-6).
 */
public final class DernierAdmin {

    private DernierAdmin() {
    }

    /**
     * @param cible          le rattachement qui perdrait son rôle d'ADMIN actif
     * @param adminsActifs   nombre de rattachements {@code ACTIF} de rôle {@code ADMIN} de l'opérateur, cible comprise
     * @param acteur         droits de l'acteur sur l'opérateur
     * @param operateurBnia  vrai si l'opérateur est le BNIA
     * @param nomCible       « Jeanne Durand », pour le message
     */
    public static void verifier(Rattachement cible, long adminsActifs, Droits acteur, boolean operateurBnia,
            String nomCible) {
        if (!cible.estAdminActif() || adminsActifs > 1) {
            return;
        }
        if (operateurBnia) {
            throw new RegleEnfreinte("RG-OPE-35", nomCible + " est le dernier ADMIN du BNIA : invitez d'abord un autre"
                    + " ADMIN.");
        }
        if (!acteur.adminBnia()) {
            throw new RegleEnfreinte("RG-OPE-35", nomCible + " est le dernier ADMIN de l'opérateur : adressez-vous au"
                    + " BNIA pour en désigner un autre.");
        }
    }
}
