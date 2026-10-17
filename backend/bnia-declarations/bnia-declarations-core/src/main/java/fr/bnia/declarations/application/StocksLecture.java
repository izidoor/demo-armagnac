package fr.bnia.declarations.application;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.AnneeDeclarative;
import fr.bnia.declarations.domain.IdentiteLigne;
import fr.bnia.declarations.domain.PeriodeDeclaration;
import fr.bnia.declarations.domain.TypeDeclarant;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Port de lecture des stocks déclarés (RG-DEC-42, UC-DEC-1, spec 05 §3.6). */
public interface StocksLecture {

    /**
     * Une ligne du stock déclaré d'un opérateur.
     *
     * @param dsm     sa dernière DSM validée, dont la ligne est tirée
     * @param periode la période de cette DSM : le stock est daté de sa fin
     * @param stock   le stock de fin de la ligne (colonne 26, RG-DEC-17), jamais nul
     */
    record LigneStock(long operateur, TypeDeclarant type, long dsm, PeriodeDeclaration periode, Instant valideeLe,
            IdentiteLigne identite, VolumeAlcoolPur stock) {
    }

    /**
     * RG-DEC-42 : les lignes au stock de fin non nul de la dernière DSM validée de chaque opérateur, filtrées par
     * opérateur et par type, dans l'ordre des lignes de chaque DSM.
     */
    List<LigneStock> stocksDeclares(Optional<Long> operateur, Optional<TypeDeclarant> type);

    /**
     * RG-DEC-44 : toutes les lignes, stock nul compris, des DSM validées de l'opérateur dont la période tient dans
     * l'année, dans l'ordre des lignes de chaque DSM.
     */
    List<LigneStock> stocksDeLAnnee(AnneeDeclarative annee, long operateur);

    /** RG-DEC-44 : les années déclaratives où l'opérateur a au moins une DSM validée. */
    Set<AnneeDeclarative> anneesDeclarees(long operateur);
}
