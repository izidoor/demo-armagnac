package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.CompteConnexion;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;

/**
 * Le compte de connexion suit l'utilisateur de {@code operateurs}, à ses événements, dans sa transaction
 * (A-AUT-1, RG-AUT-10, 11) : un échec ici annule l'action de {@code operateurs}.
 */
@UseCase
public class GererComptes {

    private final CompteRepository comptes;
    private final SessionsOuvertes sessions;

    public GererComptes(CompteRepository comptes, SessionsOuvertes sessions) {
        this.comptes = comptes;
        this.sessions = sessions;
    }

    /** RG-AUT-11 : sans mot de passe ; aucune connexion avant le premier (RG-AUT-15). */
    public CompteConnexion creer(long utilisateur, String email) {
        if (comptes.parIdentifiant(CompteConnexion.identifiant(email)).isPresent()) {
            throw new ConflitUnicite("RG-AUT-10", "« " + email + " » est déjà l'identifiant d'un compte de connexion.");
        }
        return comptes.inserer(CompteConnexion.creer(utilisateur, email));
    }

    /** RG-AUT-10 : l'identifiant de connexion suit l'e-mail confirmé. */
    public void changerIdentifiant(long utilisateur, String email) {
        comptes.parUtilisateur(utilisateur).ifPresent(compte -> {
            comptes.parIdentifiant(CompteConnexion.identifiant(email)).filter(autre -> autre.utilisateur() != utilisateur)
                    .ifPresent(autre -> {
                        throw new ConflitUnicite("RG-AUT-10", "« " + email + " » est déjà l'identifiant d'un autre"
                                + " compte de connexion.");
                    });
            compte.changerIdentifiant(email);
            comptes.mettreAJour(compte);
        });
    }

    /** RG-AUT-11 : supprimé avec l'utilisateur ; ses sessions sont fermées. Le journal reste (RG-AUT-12). */
    public void supprimer(long utilisateur) {
        comptes.parUtilisateur(utilisateur).ifPresent(compte -> comptes.supprimer(compte.id()));
        sessions.fermer(utilisateur);
    }
}
