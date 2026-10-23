package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.VolumeAlcoolPur;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contrôle des pièces d'une DSM (RG-DEC-29, H-DEC-13), sans état propre : les manques qui bloquent la
 * validation.
 */
public final class ControlePieces {

    private static final DateTimeFormatter MOIS = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);

    private ControlePieces() {
    }

    /**
     * RG-DEC-29 : un type de mouvement dont le total est non nul sans aucune pièce de ce type, rattachée à la
     * DSM ou à une de ses lignes, ni mouvement de contrat inscrit (spec 06 RG-CA-23, H-CA-2) ; pour un producteur, un
     * mois de la période sans DRM. Dans l'ordre des colonnes, puis des mois.
     *
     * @param contrats les mouvements de contrat inscrits dans la DSM
     */
    public static List<Manque> manques(DeclarationStocksMouvements dsm, List<Justificatif> justificatifs,
            List<Drm> drms, List<MouvementContrat> contrats) {
        List<Manque> manques = new ArrayList<>();
        Set<TypeMouvement> justifies = EnumSet.noneOf(TypeMouvement.class);
        justificatifs.forEach(j -> justifies.add(j.typeMouvement()));
        contrats.forEach(m -> TypeMouvement.de(m.colonne()).ifPresent(justifies::add));
        for (TypeMouvement type : TypeMouvement.values()) {
            VolumeAlcoolPur total = type.colonnes().stream().map(dsm::total)
                    .reduce(VolumeAlcoolPur.ZERO, VolumeAlcoolPur::plus);
            if (!total.estNul() && !justifies.contains(type)) {
                manques.add(new Manque(Manque.TypeManque.JUSTIFICATIF, type, null, total,
                        "Aucun justificatif pour les " + type.libelle().toLowerCase() + " ("
                                + total.toString().replace('.', ',') + " L, colonnes "
                                + type.colonnes().stream().map(c -> String.valueOf(c.numero()))
                                .collect(Collectors.joining(", ")) + ")."));
            }
        }
        Set<YearMonth> deposees = drms.stream().map(Drm::mois).collect(Collectors.toSet());
        for (YearMonth mois : dsm.moisAttendus()) {
            if (!deposees.contains(mois)) {
                manques.add(new Manque(Manque.TypeManque.DRM, null, mois, null,
                        "DRM de " + mois.format(MOIS) + " non déposée."));
            }
        }
        return List.copyOf(manques);
    }
}
