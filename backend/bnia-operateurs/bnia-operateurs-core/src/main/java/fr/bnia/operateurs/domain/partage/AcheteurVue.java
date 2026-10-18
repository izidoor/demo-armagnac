package fr.bnia.operateurs.domain.partage;

import java.util.List;
import java.util.Optional;

/**
 * Un opérateur vu comme acheteur d'une facture (spec 02 §5.2, révision de la spec 08 §3.13), que
 * {@code facturation} recopie sur ses pièces : identité légale, adresse, e-mail du destinataire des factures, et
 * ce qui manque pour qu'il soit facturable (RG-OPE-02).
 *
 * @param siret              le SIREN en est tiré (9 premiers chiffres)
 * @param numeroTva          n° de TVA intracommunautaire (RG-OPE-06)
 * @param emailFacturation   adresse du destinataire des factures (RG-OPE-23)
 * @param manquePourFacturer vide si l'opérateur est facturable (RG-OPE-02)
 */
public record AcheteurVue(
        long id,
        String code,
        String raisonSociale,
        Optional<String> siret,
        Optional<String> numeroTva,
        AdresseVue adresse,
        Optional<String> emailFacturation,
        List<String> manquePourFacturer,
        boolean actif) {

    public AcheteurVue {
        manquePourFacturer = List.copyOf(manquePourFacturer);
    }

    public boolean facturable() {
        return manquePourFacturer.isEmpty();
    }
}
