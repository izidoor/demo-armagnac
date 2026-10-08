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
 * Contenances : un volume unique (RG-REF-10), corrigeable (RG-REF-02).
 */
@UseCase
public class GererContenances {

    private final ContenanceRepository repository;
    private final ContenancesLecture lecture;
    private final ControleParametrage controle;

    public GererContenances(ContenanceRepository repository, ContenancesLecture lecture, ControleParametrage controle) {
        this.repository = repository;
        this.lecture = lecture;
        this.controle = controle;
    }

    public List<ContenanceLigne> lister(FiltreValeurs filtre) {
        return lecture.lister(filtre);
    }

    public Contenance consulter(long id) {
        return repository.parId(id).orElseThrow(() -> new ElementIntrouvable("Contenance " + id + " introuvable."));
    }

    /**
     * RG-REF-10 : volume strictement positif et unique.
     *
     * @param libelle facultatif : vide pour le libellé dérivé du volume
     */
    public Contenance creer(long acteur, BigDecimal volume, String libelle) {
        controle.exigerGestion(acteur);
        Volume nouveau = Volume.de(volume);
        verifierVolumeLibre(nouveau, null);
        return repository.inserer(Contenance.creer(nouveau, libelleFacultatif(libelle)));
    }

    /** Le volume et le libellé se corrigent (RG-REF-02, RG-REF-10). */
    public Contenance modifier(long acteur, long id, BigDecimal volume, String libelle) {
        controle.exigerGestion(acteur);
        Volume nouveau = Volume.de(volume);
        Libelle nouveauLibelle = libelleFacultatif(libelle);
        verifierVolumeLibre(nouveau, id);
        return modifierContenance(id, contenance -> contenance.decrire(nouveau, nouveauLibelle));
    }

    /** RG-REF-04. */
    public Contenance desactiver(long acteur, long id) {
        controle.exigerGestion(acteur);
        return modifierContenance(id, Contenance::desactiver);
    }

    public Contenance reactiver(long acteur, long id) {
        controle.exigerGestion(acteur);
        return modifierContenance(id, Contenance::reactiver);
    }

    /** RG-REF-04 : seulement si aucune autre donnée n'y fait référence. */
    public void supprimer(long acteur, long id) {
        controle.exigerGestion(acteur);
        consulter(id);
        repository.supprimer(id);
    }

    private Contenance modifierContenance(long id, Consumer<Contenance> modification) {
        Contenance contenance = consulter(id);
        modification.accept(contenance);
        repository.mettreAJour(contenance);
        return contenance;
    }

    private void verifierVolumeLibre(Volume volume, Long sauf) {
        if (repository.existeVolume(volume, sauf)) {
            throw new ConflitUnicite("RG-REF-10", "La contenance " + volume.libelle() + " existe déjà.");
        }
    }

    private static Libelle libelleFacultatif(String libelle) {
        return libelle == null || libelle.isBlank() ? null : Libelle.de(libelle);
    }
}
