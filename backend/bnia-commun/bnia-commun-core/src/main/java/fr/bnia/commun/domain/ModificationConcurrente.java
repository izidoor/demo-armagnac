package fr.bnia.commun.domain;

/**
 * L'objet a été modifié par quelqu'un d'autre depuis qu'il a été lu : la seconde modification est
 * refusée, l'écran se recharge (RG-OPE-51).
 */
public class ModificationConcurrente extends RegleEnfreinte {

    public ModificationConcurrente(String regle, String message) {
        super(regle, message);
    }
}
