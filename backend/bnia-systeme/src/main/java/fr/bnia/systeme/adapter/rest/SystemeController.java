package fr.bnia.systeme.adapter.rest;

import fr.bnia.systeme.application.ConsulterInfoSysteme;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/systeme")
@Tag(name = "systeme", description = "Sonde technique")
class SystemeController {

    private final ConsulterInfoSysteme consulterInfoSysteme;

    SystemeController(ConsulterInfoSysteme consulterInfoSysteme) {
        this.consulterInfoSysteme = consulterInfoSysteme;
    }

    @GetMapping("/info")
    @Operation(operationId = "consulterInfoSysteme", summary = "Version de l'application et de la base")
    InfoSystemeResponse info() {
        return InfoSystemeResponse.depuis(consulterInfoSysteme.executer());
    }
}
