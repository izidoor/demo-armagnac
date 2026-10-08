package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Region;
import java.util.List;

@UseCase
public class GererRegions extends GererValeurs<Region> {

    private final RegionsLecture lecture;

    public GererRegions(RegionRepository repository, RegionsLecture lecture) {
        super(repository, "régions");
        this.lecture = lecture;
    }

    public List<RegionLigne> lister(FiltreValeurs filtre) {
        return lecture.lister(filtre);
    }

    /** RG-REF-01, RG-REF-03. */
    public Region creer(String code, String libelle, boolean enRegionDelimitee) {
        return enregistrerNouvelle(Region.creer(Code.de(code), Libelle.de(libelle), enRegionDelimitee));
    }

    /** RG-REF-01 à 03. */
    public Region modifier(long id, String code, String libelle, boolean enRegionDelimitee) {
        Code nouveauCode = Code.de(code);
        Libelle nouveau = Libelle.de(libelle);
        return modifierValeur(id, region -> region.decrire(nouveauCode, nouveau, enRegionDelimitee));
    }
}
