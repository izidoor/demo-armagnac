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

    public GererPays(PaysRepository repository, PaysLecture lecture, ZoneCommercialeRepository zones) {
        super(repository, "pays");
        this.repository = repository;
        this.lecture = lecture;
        this.zones = zones;
    }

    public List<PaysLigne> lister(FiltreValeurs filtre) {
        return lecture.lister(filtre);
    }

    /** RG-REF-01, 03, 05 à 09. */
    public Pays creer(String code, SaisiePays saisie) {
        Code cle = Code.de(code);
        Libelle libelle = Libelle.de(saisie.libelle());
        DescriptionPays description = description(saisie);
        verifierZoneActive(description.zoneCommerciale());
        verifierCodeIsoLibre(description.codeIso(), null);
        return enregistrerNouvelle(Pays.creer(cle, libelle, description));
    }

    /**
     * Tout sauf le code (RG-REF-02). La zone n'est contrôlée que si elle change : un pays garde sa
     * zone même si elle a été désactivée depuis (RG-REF-05).
     */
    public Pays modifier(String code, SaisiePays saisie) {
        Libelle libelle = Libelle.de(saisie.libelle());
        DescriptionPays description = description(saisie);
        return modifierValeur(code, pays -> {
            if (!pays.description().zoneCommerciale().equals(description.zoneCommerciale())) {
                verifierZoneActive(description.zoneCommerciale());
            }
            verifierCodeIsoLibre(description.codeIso(), pays.code());
            pays.decrire(libelle, description);
        });
    }

    private static DescriptionPays description(SaisiePays saisie) {
        if (saisie.zoneCommerciale() == null || saisie.zoneCommerciale().isBlank()) {
            throw new RegleEnfreinte("RG-REF-05", "La zone commerciale est obligatoire.");
        }
        return new DescriptionPays(
                CodeIso.de(saisie.codeIso()),
                Code.de(saisie.zoneCommerciale()),
                saisie.membreUe(),
                saisie.langueCertificat(),
                saisie.niveauDetailCertificat(),
                DescriptionPays.ageMinimum(saisie.ageMinimum()));
    }

    /** RG-REF-05 : la zone existe et est active au moment de la saisie. */
    private void verifierZoneActive(Code zone) {
        boolean active = zones.parCode(zone).map(z -> z.estActif()).orElseThrow(() -> new RegleEnfreinte(
                "RG-REF-05", "Zone commerciale inconnue : « " + zone + " »."));
        if (!active) {
            throw new RegleEnfreinte("RG-REF-05", "La zone commerciale « " + zone + " » est inactive.");
        }
    }

    /** RG-REF-06. */
    private void verifierCodeIsoLibre(CodeIso codeIso, Code sauf) {
        if (repository.existeCodeIso(codeIso, sauf)) {
            throw new ConflitUnicite("RG-REF-06", "Le code ISO « " + codeIso + " » est déjà attribué à un autre pays.");
        }
    }
}
