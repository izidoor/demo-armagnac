package fr.bnia.facturation.application;

import fr.bnia.commun.application.UseCase;
import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.domain.Vendeur;
import java.util.Optional;

/** Paramétrer le vendeur (spec 08 §3.7, §6.1 Paramétrage > Facturation). RG-FAC-16, 26. */
@UseCase
public class ParametrerVendeur {

    private final VendeurRepository vendeurs;
    private final DroitsFacturation droits;
    private final Controles controles;

    public ParametrerVendeur(VendeurRepository vendeurs, DroitsFacturation droits) {
        this.vendeurs = vendeurs;
        this.droits = droits;
        this.controles = new Controles(droits);
    }

    /** RG-FAC-26 : lu par le personnel du BNIA ; vide tant qu'il n'est pas paramétré. */
    public Optional<Vendeur> consulter(Acteur acteur) {
        controles.exigerLectureBnia(acteur);
        return vendeurs.charger();
    }

    /** RG-FAC-26 : l'acteur peut le modifier. */
    public boolean modifiable(Acteur acteur) {
        return droits.gere(acteur);
    }

    /** RG-FAC-16 : complet à l'enregistrement ; les pièces émises gardent le vendeur recopié à leur émission. */
    public Vendeur enregistrer(Acteur acteur, Vendeur vendeur) {
        controles.exigerGestion(acteur);
        vendeurs.enregistrer(vendeur);
        return vendeur;
    }
}
