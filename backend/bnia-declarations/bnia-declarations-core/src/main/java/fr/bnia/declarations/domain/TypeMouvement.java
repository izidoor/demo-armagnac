package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Type de mouvement qui appelle un justificatif (spec 05 §3.1, H-DEC-13) : ses colonnes et les types de pièce
 * qui le justifient (RG-DEC-26). La colonne 15 (Floc de Gascogne) n'en appelle aucun (Q-DEC-1). Déclarés
 * dans l'ordre de leur première colonne, celui des manques.
 */
public enum TypeMouvement {

    ACHAT("Achats", List.of(ColonneDsm.C02, ColonneDsm.C03), Set.of(TypePiece.FACTURE, TypePiece.DAE)),
    REINTEGRATION("Réintégrations", List.of(ColonneDsm.C09), Set.of(TypePiece.AVOIR, TypePiece.DAE)),
    AUTRES_FABRICATIONS("Autres fabrications", List.of(ColonneDsm.C16),
            Set.of(TypePiece.DECLARATION_FABRICATION_DOUANES)),
    VENTE_NEGOCE("Ventes négoce", List.of(ColonneDsm.C20, ColonneDsm.C21), Set.of(TypePiece.FACTURE, TypePiece.DAE)),
    VENTE_EXPORT("Ventes à l'exportation", List.of(ColonneDsm.C22, ColonneDsm.C23, ColonneDsm.C24, ColonneDsm.C25),
            Set.of(TypePiece.FACTURE, TypePiece.DAE));

    private final String libelle;
    private final List<ColonneDsm> colonnes;
    private final Set<TypePiece> pieces;

    TypeMouvement(String libelle, List<ColonneDsm> colonnes, Set<TypePiece> pieces) {
        this.libelle = libelle;
        this.colonnes = colonnes;
        this.pieces = pieces;
    }

    public String libelle() {
        return libelle;
    }

    public List<ColonneDsm> colonnes() {
        return colonnes;
    }

    /** RG-DEC-26 : les types de pièce qui justifient ce mouvement. */
    public Set<TypePiece> pieces() {
        return pieces;
    }

    /** RG-DEC-26 : le type de pièce justifie ce mouvement. */
    public void exigerPiece(TypePiece piece) {
        if (!pieces.contains(piece)) {
            throw new RegleEnfreinte("RG-DEC-26", "Une pièce « " + piece.libelle() + " » ne justifie pas les "
                    + libelle.toLowerCase() + " : attendu " + pieces.stream().sorted().map(TypePiece::libelle)
                    .collect(Collectors.joining(" ou ")) + ".");
        }
    }
}
