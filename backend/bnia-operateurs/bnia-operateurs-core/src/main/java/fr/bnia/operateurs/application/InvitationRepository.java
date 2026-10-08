package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Email;
import fr.bnia.operateurs.domain.Invitation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Agrégat {@code Invitation} (A-OPE-6). Au plus une en attente par e-mail et opérateur (RG-OPE-40). */
public interface InvitationRepository {

    Optional<Invitation> parId(long id);

    /** RG-OPE-42 : l'invitation dont le jeton a cette empreinte. */
    Optional<Invitation> parEmpreinte(String empreinte);

    /** RG-OPE-40 : en attente pour cet e-mail dans l'opérateur, à la casse près. */
    Optional<Invitation> enAttentePourEmail(long operateur, Email email);

    /** RG-OPE-40, 45 : en attente, partie de ce contact (sa fiche). */
    Optional<Invitation> enAttentePourFiche(long fiche);

    /** RG-OPE-26 : en attente dans l'opérateur. */
    List<Invitation> enAttente(long operateur);

    /** RG-OPE-41 : en attente dont l'expiration est passée. */
    List<Invitation> echues(Instant maintenant);

    Invitation inserer(Invitation invitation);

    void mettreAJour(Invitation invitation);
}
