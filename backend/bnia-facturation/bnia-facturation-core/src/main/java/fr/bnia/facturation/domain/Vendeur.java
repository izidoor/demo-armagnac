package fr.bnia.facturation.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.ArrayList;
import java.util.List;

/**
 * Le vendeur, le BNIA (spec 08 §3.7) : paramétré une fois dans la lib, recopié sur chaque pièce à son
 * émission ; le modifier ne change aucune pièce émise (RG-FAC-16). Complet dès sa construction.
 *
 * @param adresseFacturation facultative ({@code null}), si elle diffère du siège
 * @param tvaSurDebits       option pour la TVA d'après les débits (CGI art. 242 nonies A), imprimée si vraie
 * @param escompte           défaut : « Pas d'escompte pour paiement anticipé »
 * @param penalites          défaut : « trois fois le taux d'intérêt légal »
 */
public record Vendeur(String nom, Adresse siege, Adresse adresseFacturation, String siren, String siret,
        String numeroTva, String iban, String bic, String telephone, String email, boolean tvaSurDebits,
        String escompte, String penalites) {

    public static final String ESCOMPTE = "Pas d'escompte pour paiement anticipé";
    public static final String PENALITES = "trois fois le taux d'intérêt légal";

    public Vendeur {
        nom = Textes.facultatif(nom);
        siren = Textes.compact(siren);
        siret = Textes.compact(siret);
        numeroTva = Textes.compact(numeroTva);
        iban = Textes.compact(iban);
        bic = Textes.compact(bic);
        telephone = Textes.facultatif(telephone);
        email = Textes.facultatif(email);
        escompte = Textes.facultatif(escompte) == null ? ESCOMPTE : escompte.strip();
        penalites = Textes.facultatif(penalites) == null ? PENALITES : penalites.strip();
        if (siren == null && siret != null && siret.length() >= 9) {
            siren = siret.substring(0, 9);
        }
        List<String> erreurs = new ArrayList<>();
        if (nom == null) {
            erreurs.add("le nom");
        }
        if (siege == null || !siege.manques().isEmpty()) {
            erreurs.add("l'adresse du siège complète");
        }
        if (adresseFacturation != null && !adresseFacturation.manques().isEmpty()) {
            erreurs.add("une adresse de facturation complète");
        }
        if (!IdentifiantsLegaux.sirenValide(siren)) {
            erreurs.add("un SIREN valide");
        }
        if (!IdentifiantsLegaux.siretValide(siret) || !siret.startsWith(siren == null ? "" : siren)) {
            erreurs.add("un SIRET valide, qui commence par le SIREN");
        }
        if (!IdentifiantsLegaux.tvaValide(numeroTva)) {
            erreurs.add("un n° de TVA intracommunautaire valide");
        }
        if (iban == null || !iban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}")) {
            erreurs.add("un IBAN valide");
        }
        if (bic == null || !bic.matches("[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?")) {
            erreurs.add("un BIC valide (8 ou 11 caractères)");
        }
        if (telephone == null) {
            erreurs.add("le téléphone");
        }
        if (email == null || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            erreurs.add("un e-mail valide");
        }
        if (!erreurs.isEmpty()) {
            throw new RegleEnfreinte("RG-FAC-16", "Le vendeur doit avoir " + String.join(", ", erreurs) + ".");
        }
    }

    /**
     * BR-FR-05 : mentions générées sur toute pièce : pénalités de retard (PMT), indemnité forfaitaire de 40 € pour
     * un acheteur professionnel (PMD), escompte (AAB), option sur les débits si elle est prise.
     */
    public List<String> mentions(CategorieAcheteur acheteur) {
        List<String> mentions = new ArrayList<>();
        mentions.add("Pénalités de retard : " + penalites + ".");
        if (acheteur == CategorieAcheteur.PROFESSIONNEL) {
            mentions.add("Indemnité forfaitaire pour frais de recouvrement en cas de retard de paiement : 40 €.");
        }
        mentions.add(escompte + ".");
        if (tvaSurDebits) {
            mentions.add("Option pour le paiement de la TVA d'après les débits.");
        }
        return mentions;
    }
}
