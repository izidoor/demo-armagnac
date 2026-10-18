package fr.bnia.facturation.adapter.rest;

import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.facturation.application.ParametrerVendeur;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Paramétrage > Facturation : le vendeur (spec 08 §3.7, §6.1). */
@RestController
@Tag(name = "facturation")
class VendeurController {

    private static final String CHEMIN = "/api/facturation/vendeur";

    private final ParametrerVendeur parametrer;

    VendeurController(ParametrerVendeur parametrer) {
        this.parametrer = parametrer;
    }

    @GetMapping(CHEMIN)
    @Operation(operationId = "consulterVendeur", summary = "Le vendeur, s'il est paramétré (RG-FAC-16, 26)")
    ParametrageFacturationResponse consulter(Connecte connecte) {
        return new ParametrageFacturationResponse(parametrer.consulter(Acteurs.de(connecte)).map(VendeurDto::de)
                .orElse(null), parametrer.modifiable(Acteurs.de(connecte)));
    }

    @PutMapping(CHEMIN)
    @Operation(operationId = "parametrerVendeur",
            summary = "Paramètre le vendeur ; les pièces émises gardent celui de leur émission (RG-FAC-16, 26)")
    VendeurDto parametrer(Connecte connecte, @RequestBody VendeurDto saisie) {
        return VendeurDto.de(parametrer.enregistrer(Acteurs.de(connecte), saisie.versDomaine()));
    }
}
