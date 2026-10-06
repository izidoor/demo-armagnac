package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.ElementIntrouvable;
import fr.bnia.referentiels.domain.Contenance;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Volume;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

/**
 * Contenances : identifiées par leur volume, qui ne change jamais (RG-REF-02, RG-REF-10).
 */
@UseCase
public class GererContenances {

    private final ContenanceRepository repository;
    private final ContenancesLecture lecture;

    public GererContenances(ContenanceRepository repository, ContenancesLecture lecture) {
        this.repository = repository;
        this.lecture = lecture;
    }

    public List<ContenanceLigne> lister(FiltreValeurs filtre) {
        return lecture.lister(filtre);
    }

    public Contenance consulter(BigDecimal volume) {
        Volume cle = Volume.de(volume);
        return repository.parVolume(cle).orElseThrow(() -> new ElementIntrouvable(
                "Contenance introuvable : " + cle.libelle() + "."));
    }

    /**
     * RG-REF-10 : volume strictement positif et unique.
     *
     * @param libelle facultatif : vide pour le libellé dérivé du volume
     */
    public Contenance creer(BigDecimal volume, String libelle) {
        Volume cle = Volume.de(volume);
        if (repository.existeVolume(cle)) {
            throw new ConflitUnicite("RG-REF-10", "La contenance " + cle.libelle() + " existe déjà.");
        }
        Contenance contenance = Contenance.creer(cle, libelleFacultatif(libelle));
        repository.inserer(contenance);
        return contenance;
    }

    /** Seul le libellé change (RG-REF-02). */
    public Contenance modifier(BigDecimal volume, String libelle) {
        Libelle nouveau = libelleFacultatif(libelle);
        return modifierContenance(volume, contenance -> contenance.renommer(nouveau));
    }

    /** RG-REF-04. */
    public Contenance desactiver(BigDecimal volume) {
        return modifierContenance(volume, Contenance::desactiver);
    }

    public Contenance reactiver(BigDecimal volume) {
        return modifierContenance(volume, Contenance::reactiver);
    }

    private Contenance modifierContenance(BigDecimal volume, Consumer<Contenance> modification) {
        Contenance contenance = consulter(volume);
        modification.accept(contenance);
        repository.mettreAJour(contenance);
        return contenance;
    }

    private static Libelle libelleFacultatif(String libelle) {
        return libelle == null || libelle.isBlank() ? null : Libelle.de(libelle);
    }
}
