package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.CodeIso;
import fr.bnia.referentiels.domain.DescriptionPays;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.Pays;
import java.util.List;

@UseCase
public class GererPays extends GererValeurs<Pays> {

    private final PaysRepository repository;
    private final PaysLecture lecture;
    private final ZoneCommercialeRepository zones;

    public GererPays(PaysRepository repository, PaysLecture lecture, ZoneCommercialeRepository zones,
            ControleParametrage controle) {
        super(repository, "pays", controle);
        this.repository = repository;
        this.lecture = lecture;
        this.zones = zones;
    }

    public List<PaysLigne> lister(FiltreValeurs filtre) {
        return lecture.lister(filtre);
    }

    /** RG-REF-01, 03, 05 à 09. */
    public Pays creer(long acteur, SaisiePays saisie) {
        exigerParametrage(acteur);
        Code code = Code.de(saisie.code());
        Libelle libelle = Libelle.de(saisie.libelle());
        DescriptionPays description = description(saisie);
        verifierZoneActive(description.zoneCommerciale());
        verifierCodeIsoLibre(description.codeIso(), null);
        return enregistrerNouvelle(Pays.creer(code, libelle, description));
    }

    /**
     * Tout se corrige, code compris (RG-REF-02). La zone n'est contrôlée que si elle change : un pays
     * garde sa zone même si elle a été désactivée depuis (RG-REF-05).
     */
    public Pays modifier(long acteur, long id, SaisiePays saisie) {
        exigerParametrage(acteur);
        Code code = Code.de(saisie.code());
        Libelle libelle = Libelle.de(saisie.libelle());
        DescriptionPays description = description(saisie);
        return modifierValeur(id, pays -> {
            if (pays.description().zoneCommerciale() != description.zoneCommerciale()) {
                verifierZoneActive(description.zoneCommerciale());
            }
            verifierCodeIsoLibre(description.codeIso(), id);
            pays.decrire(code, libelle, description);
        });
    }

    private static DescriptionPays description(SaisiePays saisie) {
        if (saisie.zoneCommerciale() == null) {
            throw new RegleEnfreinte("RG-REF-05", "La zone commerciale est obligatoire.");
        }
        return new DescriptionPays(
                CodeIso.de(saisie.codeIso()),
                saisie.zoneCommerciale(),
                saisie.membreUe(),
                saisie.langueCertificat(),
                saisie.niveauDetailCertificat(),
                DescriptionPays.ageMinimum(saisie.ageMinimum()));
    }

    /** RG-REF-05 : la zone existe et est active au moment de la saisie. */
    private void verifierZoneActive(long zone) {
        var trouvee = zones.parId(zone).orElseThrow(() -> new RegleEnfreinte(
                "RG-REF-05", "Zone commerciale inconnue : " + zone + "."));
        if (!trouvee.estActif()) {
            throw new RegleEnfreinte("RG-REF-05", "La zone commerciale « " + trouvee.libelle() + " » est inactive.");
        }
    }

    /** RG-REF-06. */
    private void verifierCodeIsoLibre(CodeIso codeIso, Long sauf) {
        if (repository.existeCodeIso(codeIso, sauf)) {
            throw new ConflitUnicite("RG-REF-06", "Le code ISO « " + codeIso + " » est déjà attribué à un autre pays.");
        }
    }
}
