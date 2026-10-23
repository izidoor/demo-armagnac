package fr.bnia.declarations.domain.partage;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Une retiraison vient d'être validée par les deux parties d'un contrat d'achat Armagnac (spec 06 RG-CA-15,
 * ADR-013, ADR-017 amendé le 2026-10-23) : publié par {@code contratsarmagnac} dans sa transaction, reçu ici pour créer
 * les mouvements de contrat de l'acheteur et du vendeur et les inscrire dans leurs DSM (RG-CA-21, 22). Le contrat est
 * référencé sans clé étrangère (ADR-011 amendé le 2026-10-23).
 *
 * @param contrat        identifiant du contrat, opaque ici
 * @param numeroContrat  {@code CA-2026-0001}, copié
 * @param retiraison     numéro de la retiraison dans le contrat
 * @param dateEnlevement date de la retiraison (H-CA-3)
 * @param validePar      utilisateur qui a validé, pour le journal des DSM
 * @param lignes         une par ligne du contrat retirée
 */
public record RetiraisonValidee(long contrat, String numeroContrat, int retiraison, LocalDate dateEnlevement,
        long vendeur, long acheteur, long validePar, List<LigneRetiree> lignes) {

    public RetiraisonValidee {
        Objects.requireNonNull(numeroContrat, "numeroContrat");
        Objects.requireNonNull(dateEnlevement, "dateEnlevement");
        lignes = List.copyOf(lignes);
    }

    /**
     * Une ligne retirée, toujours sous bois (RG-CA-05) : {@code MB} et son millésime, ou {@code NB}, sa dénomination
     * telle que saisie et son compte d'âge à la date d'enlèvement (H-CA-11).
     *
     * @param compte pour {@code MB}, ignoré : il se calcule depuis le millésime
     */
    public record LigneRetiree(CleConditionnement cle, Integer millesime, String denomination, CompteDAgeDsm compte,
            VolumeAlcoolPur volumeAp) {
    }
}
