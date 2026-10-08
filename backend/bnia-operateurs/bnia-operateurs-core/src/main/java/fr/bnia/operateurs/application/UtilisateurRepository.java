package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.Utilisateur;
import java.util.Optional;

/** Agrégat {@code Utilisateur}, avec sa demande de changement d'e-mail (A-OPE-6). */
public interface UtilisateurRepository {

    Optional<Utilisateur> parId(long id);

    /** RG-OPE-29 : à la casse près. */
    Optional<Utilisateur> parEmail(Email email);

    /** RG-OPE-48 : l'utilisateur dont la demande de changement d'e-mail porte cette empreinte de jeton. */
    Optional<Utilisateur> parEmpreinteChangementEmail(String empreinte);

    Utilisateur inserer(Utilisateur utilisateur);

    /** @throws fr.bnia.commun.domain.ModificationConcurrente (RG-OPE-51) */
    void mettreAJour(Utilisateur utilisateur);

    void supprimer(long id);
}
