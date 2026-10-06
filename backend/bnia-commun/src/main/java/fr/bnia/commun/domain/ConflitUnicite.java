package fr.bnia.commun.domain;

/**
 * Une valeur qui doit être unique existe déjà (code, libellé…).
 */
public class ConflitUnicite extends RegleEnfreinte {

    public ConflitUnicite(String regle, String message) {
        super(regle, message);
    }
}
