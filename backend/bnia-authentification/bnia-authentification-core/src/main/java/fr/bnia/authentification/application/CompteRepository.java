package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.CompteConnexion;
import java.util.Optional;

/** Agrégat {@code CompteConnexion}, avec ses secrets en cours (spec 03 §3.1 à 3.4). */
public interface CompteRepository {

    Optional<CompteConnexion> parUtilisateur(long utilisateur);

    /** RG-AUT-01 : l'identifiant de connexion, sans distinction de casse. */
    Optional<CompteConnexion> parIdentifiant(String identifiant);

    /** RG-AUT-08 : le compte dont la réinitialisation en cours porte cette empreinte de jeton. */
    Optional<CompteConnexion> parEmpreinteReinitialisation(String empreinte);

    /** @throws fr.bnia.commun.domain.ConflitUnicite si l'identifiant est déjà celui d'un autre compte */
    CompteConnexion inserer(CompteConnexion compte);

    /** @throws fr.bnia.commun.domain.ConflitUnicite si l'identifiant est déjà celui d'un autre compte */
    void mettreAJour(CompteConnexion compte);

    void supprimer(long id);
}
