package fr.bnia.contratsarmagnac.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

/**
 * Le contenu d'une version de retiraison (spec 06 §3.4) : la date d'enlèvement (H-CA-3), « dernière » ou non, et les
 * lignes du contrat retirées.
 *
 * @param derniere sa validation clôt le contrat, {@code EXECUTE} (RG-CA-15)
 */
public record ContenuRetiraison(LocalDate dateEnlevement, boolean derniere, List<LigneRetiraison> lignes) {

    /** RG-CA-13 : une date, au moins une ligne, chaque ligne du contrat une fois au plus. */
    public ContenuRetiraison {
        if (dateEnlevement == null) {
            throw new RegleEnfreinte("RG-CA-13", "Une retiraison porte sa date d'enlèvement.");
        }
        if (lignes == null || lignes.isEmpty()) {
            throw new RegleEnfreinte("RG-CA-13", "Une retiraison porte au moins une ligne du contrat.");
        }
        lignes = List.copyOf(lignes);
        if (!lignes.stream().map(LigneRetiraison::rang).allMatch(new HashSet<>()::add)) {
            throw new RegleEnfreinte("RG-CA-13", "Une ligne du contrat ne figure qu'une fois dans une retiraison.");
        }
    }
}
