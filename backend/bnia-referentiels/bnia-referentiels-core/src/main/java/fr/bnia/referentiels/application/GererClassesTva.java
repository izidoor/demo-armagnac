package fr.bnia.referentiels.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.referentiels.domain.ClasseTva;
import fr.bnia.referentiels.domain.Code;
import fr.bnia.referentiels.domain.Libelle;
import fr.bnia.referentiels.domain.TauxTva;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Classes de TVA et l'historique de leurs taux (§4.11.1 p.52, C.10.1-2 p.127).
 */
@UseCase
public class GererClassesTva extends GererValeurs<ClasseTva> {

    private final ClassesTvaLecture lecture;
    private final Clock horloge;

    public GererClassesTva(ClasseTvaRepository repository, ClassesTvaLecture lecture, Clock horloge,
            ControleParametrage controle) {
        super(repository, "classes de TVA", controle);
        this.lecture = lecture;
        this.horloge = horloge;
    }

    /** Avec le taux en vigueur aujourd'hui. */
    public List<ClasseTvaLigne> lister(FiltreValeurs filtre) {
        return lecture.lister(filtre, aujourdhui());
    }

    public LocalDate aujourdhui() {
        return LocalDate.now(horloge);
    }

    /** RG-REF-01, RG-REF-03. Une classe naît sans taux. */
    public ClasseTva creer(long acteur, String code, String libelle) {
        exigerParametrage(acteur);
        return enregistrerNouvelle(ClasseTva.creer(Code.de(code), Libelle.de(libelle)));
    }

    /** RG-REF-01 à 03. */
    public ClasseTva modifier(long acteur, long id, String code, String libelle) {
        exigerParametrage(acteur);
        Code nouveauCode = Code.de(code);
        Libelle nouveau = Libelle.de(libelle);
        return modifierValeur(id, classe -> {
            classe.recoder(nouveauCode);
            classe.renommer(nouveau);
        });
    }

    /** RG-REF-11. */
    public ClasseTva ajouterTaux(long acteur, long id, SaisieTauxTva saisie) {
        exigerParametrage(acteur);
        TauxTva taux = taux(saisie);
        return modifierValeur(id, classe -> classe.ajouterTaux(taux));
    }

    /** RG-REF-11. Le taux est désigné par sa date de début actuelle. */
    public ClasseTva modifierTaux(long acteur, long id, LocalDate debutActuel, SaisieTauxTva saisie) {
        exigerParametrage(acteur);
        TauxTva taux = taux(saisie);
        return modifierValeur(id, classe -> classe.modifierTaux(debutActuel, taux));
    }

    /** Correction d'une erreur de saisie dans l'historique (spec 01, §5.1). */
    public ClasseTva retirerTaux(long acteur, long id, LocalDate debut) {
        exigerParametrage(acteur);
        return modifierValeur(id, classe -> classe.retirerTaux(debut));
    }

    private static TauxTva taux(SaisieTauxTva saisie) {
        return new TauxTva(saisie.taux(), saisie.debut(), saisie.fin());
    }
}
