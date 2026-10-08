package fr.bnia.authentification.application;

/** D'où vient une demande, pour le journal des connexions (RG-AUT-12). */
public record Contexte(String adresseIp, String navigateur) {

    public static Contexte inconnu() {
        return new Contexte(null, null);
    }
}
