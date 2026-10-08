package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
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
            MarquesProchesLecture prochesLecture, ControleParametrage controle) {
        super(repository, lecture, Marque::creer, "marques", controle);
        this.repository = repository;
        this.prochesLecture = prochesLecture;
    }

    /** RG-REF-01, RG-REF-03, RG-REF-15 : au paramétrage. */
    @Override
    public Marque creer(long acteur, String code, String libelle) {
        exigerParametrage(acteur);
        verifierLibelleLibre(Libelle.de(libelle), null);
        return nouvelle(code, libelle);
    }

    /**
     * RG-REF-14, 15 : depuis la fiche d'un opérateur, au back-office comme au portail, avec les mêmes
     * règles ; par qui gère les contacts de cet opérateur.
     */
    public Marque creerDepuisFiche(long acteur, long operateur, String code, String libelle) {
        controle().exigerGestionFiche(acteur, operateur);
        verifierLibelleLibre(Libelle.de(libelle), null);
        return nouvelle(code, libelle);
    }

    /** RG-REF-01 à 03, RG-REF-15. */
    @Override
    public Marque modifier(long acteur, long id, String code, String libelle) {
        exigerParametrage(acteur);
        verifierLibelleLibre(Libelle.de(libelle), id);
        return super.modifier(acteur, id, code, libelle);
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
     * @param marqueModifiee identifiant de la marque modifiée, qui peut garder son libellé ; {@code null}
     *                       à la création
     */
    private void verifierLibelleLibre(Libelle libelle, Long marqueModifiee) {
        repository.parLibelleNormalise(LibelleNormalise.de(libelle))
                .filter(existante -> !existante.id().equals(marqueModifiee))
                .ifPresent(existante -> {
                    throw new ConflitUnicite("RG-REF-15", "La marque « " + existante.libelle() + " » (code "
                            + existante.code() + ") existe déjà"
                            + (existante.estActif() ? "." : " ; elle est inactive : réactivez-la."));
                });
    }
}
