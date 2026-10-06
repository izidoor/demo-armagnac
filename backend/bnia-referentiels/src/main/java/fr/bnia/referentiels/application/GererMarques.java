package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.LibelleNormalise;
import fr.bnia.referentiels.domain.Marque;
import java.util.List;

/**
 * Marques : au paramétrage, et depuis la fiche opérateur au back-office comme au portail, avec les
 * mêmes règles (RG-REF-14).
 */
@UseCase
public class GererMarques extends GererValeursSimples<Marque> {

    static final int NOMBRE_DE_PROCHES = 10;

    private final MarqueRepository repository;
    private final MarquesProchesLecture prochesLecture;

    public GererMarques(MarqueRepository repository, ValeursSimplesLecture<Marque> lecture,
            MarquesProchesLecture prochesLecture) {
        super(repository, lecture, Marque::creer, "marques");
        this.repository = repository;
        this.prochesLecture = prochesLecture;
    }

    /** RG-REF-01, RG-REF-03, RG-REF-14, RG-REF-15. */
    @Override
    public Marque creer(String code, String libelle) {
        verifierLibelleLibre(Libelle.de(libelle), null);
        return super.creer(code, libelle);
    }

    /** RG-REF-02, RG-REF-03, RG-REF-15. */
    @Override
    public Marque modifier(String code, String libelle) {
        verifierLibelleLibre(Libelle.de(libelle), Code.de(code));
        return super.modifier(code, libelle);
    }

    /** Marques au libellé proche du texte saisi, pour éviter un doublon (RG-REF-15). */
    public List<ValeurSimpleLigne> proches(String texte) {
        if (texte == null || texte.isBlank()) {
            return List.of();
        }
        return prochesLecture.proches(LibelleNormalise.de(texte), NOMBRE_DE_PROCHES);
    }

    /**
     * RG-REF-15 : aucune autre marque, même inactive, n'a ce libellé à la casse, aux accents et aux
     * espaces près.
     *
     * @param marqueModifiee code de la marque modifiée, qui peut garder son libellé ; {@code null} à la création
     */
    private void verifierLibelleLibre(Libelle libelle, Code marqueModifiee) {
        repository.parLibelleNormalise(LibelleNormalise.de(libelle))
                .filter(existante -> !existante.code().equals(marqueModifiee))
                .ifPresent(existante -> {
                    throw new ConflitUnicite("RG-REF-15", "La marque « " + existante.libelle() + " » (code "
                            + existante.code() + ") existe déjà"
                            + (existante.estActif() ? "." : " ; elle est inactive : réactivez-la."));
                });
    }
}
