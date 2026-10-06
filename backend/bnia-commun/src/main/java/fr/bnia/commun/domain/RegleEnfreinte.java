package fr.bnia.commun.domain;

import java.util.Optional;

/**
 * Une règle de gestion n'est pas respectée. Porte l'identifiant de la règle ({@code RG-REF-11}) quand
 * elle en a un ; le message s'adresse à l'utilisateur.
 */
public class RegleEnfreinte extends RuntimeException {

    private final String regle;

    public RegleEnfreinte(String regle, String message) {
        super(message);
        this.regle = regle;
    }

    public Optional<String> regle() {
        return Optional.ofNullable(regle);
    }
}
