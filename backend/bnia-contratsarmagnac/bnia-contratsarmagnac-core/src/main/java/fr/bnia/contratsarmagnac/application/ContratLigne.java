package fr.bnia.contratsarmagnac.application;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.contratsarmagnac.domain.EtatContrat;
import fr.bnia.contratsarmagnac.domain.Partie;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

/**
 * Un contrat dans une liste (RG-CA-25), lu sur sa version courante.
 *
 * @param attend      en projet, la partie dont l'approbation manque
 * @param retiraisonsAttendent conclu, les parties qu'attend au moins une retiraison (RG-CA-25)
 * @param produit     la première ligne : « 2015 » ou « Hors d'âge 20+ »
 * @param modifieLe   la date de la version courante
 */
public record ContratLigne(long id, String numero, EtatContrat etat, long acheteur, long vendeur, Partie ouvertPar,
        Optional<Partie> attend, Set<Partie> retiraisonsAttendent, Optional<LocalDate> dateContrat, Instant modifieLe, int versionCourante,
        int nombreLignes, String produit, VolumeAlcoolPur volumeAp, BigDecimal montantHt,
        Optional<LocalDate> aRetirerAvantLe) {
}
