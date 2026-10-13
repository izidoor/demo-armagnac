package fr.bnia.operateurs.application;

import fr.bnia.operateurs.domain.Contact;
import fr.bnia.operateurs.domain.Email;
import java.util.Optional;

/** Agrégat {@code Contact} (A-OPE-6). */
public interface ContactRepository {

    Optional<Contact> parId(long id);

    /** RG-OPE-30 : le contact qui porte cette fiche. */
    Optional<Contact> parFiche(long fiche);

    /** RG-OPE-29 et 28 : le contact de l'opérateur qui a cet e-mail, à la casse près (au plus un). */
    Optional<Contact> parEmail(long operateur, Email email);

    Contact inserer(Contact contact);

    /** @throws fr.bnia.commun.domain.ModificationConcurrente (RG-OPE-51) */
    void mettreAJour(Contact contact);

    void supprimer(long id);

    /** RG-OPE-28 : parmi les contacts de l'opérateur, à la casse près ; {@code sauf} exclut le contact modifié. */
    boolean existeEmail(long operateur, Email email, Long sauf);
}
