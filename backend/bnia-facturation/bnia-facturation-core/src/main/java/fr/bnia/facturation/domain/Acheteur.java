package fr.bnia.facturation.domain;

import fr.bnia.facturation.api.ClientRef;
import java.util.ArrayList;
import java.util.List;

/**
 * L'acheteur d'une pièce (spec 08 §3.5), recopié sur la pièce. Celui d'un client se lit par le port
 * {@code Clients}, à la demande et de nouveau à l'émission (RG-FAC-11).
 *
 * @param client            {@code null} pour un client libre
 * @param siren             9 chiffres ; tiré du SIRET s'il n'est pas donné
 * @param siret             facultatif
 * @param numeroTva         facultatif
 * @param adresseLivraison  facultative ({@code null}), si elle diffère
 * @param email             destinataire des factures
 */
public record Acheteur(ClientRef client, CategorieAcheteur categorie, String nom, String siren, String siret,
        String numeroTva, Adresse adresseFacturation, Adresse adresseLivraison, String email) {

    public Acheteur {
        categorie = categorie == null ? CategorieAcheteur.PROFESSIONNEL : categorie;
        nom = Textes.facultatif(nom);
        siret = Textes.compact(siret);
        siren = Textes.compact(siren);
        if (siren == null && siret != null && siret.length() >= 9) {
            siren = siret.substring(0, 9);
        }
        numeroTva = Textes.compact(numeroTva);
        adresseFacturation = adresseFacturation == null ? new Adresse(null, null, null, null, null, null)
                : adresseFacturation;
        email = Textes.facultatif(email);
    }

    /**
     * RG-FAC-10 : ce qui manque ou est faux pour que l'acheteur soit complet, chaque élément nommé ; vide s'il
     * est complet.
     */
    public List<String> manques() {
        List<String> manques = new ArrayList<>();
        if (nom == null) {
            manques.add("nom");
        }
        manques.addAll(adresseFacturation.manques());
        if (email == null) {
            manques.add("e-mail du destinataire des factures");
        }
        boolean professionnelFrancais = categorie == CategorieAcheteur.PROFESSIONNEL && adresseFacturation.enFrance();
        if (siren == null) {
            if (professionnelFrancais) {
                manques.add("SIREN");
            }
        } else if (!IdentifiantsLegaux.sirenValide(siren)) {
            manques.add("SIREN valide (" + siren + ")");
        }
        if (numeroTva != null && !IdentifiantsLegaux.tvaValide(numeroTva)) {
            manques.add("n° de TVA valide (" + numeroTva + ")");
        }
        if (adresseLivraison != null && !adresseLivraison.manques().isEmpty()) {
            manques.add("adresse de livraison complète");
        }
        return manques;
    }

    public boolean complet() {
        return manques().isEmpty();
    }
}
