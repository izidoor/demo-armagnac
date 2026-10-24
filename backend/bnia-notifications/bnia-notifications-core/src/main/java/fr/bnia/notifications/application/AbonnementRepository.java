package fr.bnia.notifications.application;

import fr.bnia.notifications.domain.Abonnement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Port d'écriture des abonnements (spec 11 §3.1). */
public interface AbonnementRepository {

    /** RG-NOT-02 : ajoute l'abonnement, ou remplace celui de la même adresse, quel qu'en soit l'utilisateur. */
    void enregistrer(Abonnement abonnement);

    Optional<Abonnement> parAdresse(String adresse);

    /** RG-NOT-03 : les abonnements de ces utilisateurs, tous appareils confondus. */
    List<Abonnement> de(Collection<Long> utilisateurs);

    /** Rien si l'adresse est inconnue. */
    void supprimer(String adresse);
}
