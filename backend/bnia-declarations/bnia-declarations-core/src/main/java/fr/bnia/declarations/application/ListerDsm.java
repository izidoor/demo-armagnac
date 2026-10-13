package fr.bnia.declarations.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.declarations.domain.EtatDsm;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Listing de toutes les DSM, pour le BNIA (analyse UC-DEC-7, spec 05 §6.1) : filtré par opérateur, type, mois
 * et état ; la file d'attente est le filtre « envoyées ». RG-DEC-01. « À facturer » : lot 4 ; retards : lot 7.
 */
@UseCase
public class ListerDsm {

    private final DsmLecture lecture;
    private final OperateursLecture operateurs;
    private final Autorisations autorisations;

    public ListerDsm(DsmLecture lecture, OperateursLecture operateurs) {
        this.lecture = lecture;
        this.operateurs = operateurs;
        this.autorisations = new Autorisations(operateurs);
    }

    /**
     * Une DSM du listing, avec le code et la raison sociale de son opérateur, lus dans {@code operateurs}
     * pour tout le listing à la fois.
     *
     * @param prendreEnInstruction l'acteur peut la prendre en instruction depuis le listing (RG-DEC-01, 20)
     */
    public record DsmListee(DsmLigne dsm, String code, String raisonSociale, boolean prendreEnInstruction) {
    }

    /**
     * RG-DEC-01 : de la période la plus récente à la plus ancienne, puis par code d'opérateur. Trois lectures
     * en tout, quelle que soit la taille du listing : les droits, les DSM, l'identité de leurs opérateurs.
     */
    public List<DsmListee> lister(Acteur acteur, FiltreDsm filtre) {
        autorisations.exigerLectureBnia(acteur);
        boolean instruit = autorisations.droits(acteur, operateurs.operateurBnia()).bnia()
                .map(r -> r.gere(Module.DECLARATIONS)).orElse(false);
        List<DsmLigne> dsms = lecture.rechercher(filtre);
        // Une seule lecture des opérateurs pour tout le listing, jamais une par ligne.
        Map<Long, ResultatRecherche> identites = operateurs.identites(
                        dsms.stream().map(DsmLigne::operateur).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(ResultatRecherche::id, Function.identity()));
        return dsms.stream()
                .map(d -> {
                    ResultatRecherche identite = identites.get(d.operateur());
                    return new DsmListee(d, identite == null ? "?" : identite.code(),
                            identite == null ? "Opérateur n° " + d.operateur() : identite.raisonSociale(),
                            instruit && d.etat() == EtatDsm.ENVOYEE);
                })
                .sorted((a, b) -> {
                    int periode = b.dsm().periode().debut().compareTo(a.dsm().periode().debut());
                    return periode != 0 ? periode : a.code().compareTo(b.code());
                })
                .toList();
    }
}
