package fr.bnia.facturation.adapter.integration;

import fr.bnia.facturation.api.Acteur;
import fr.bnia.facturation.application.DroitsFacturation;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.Module;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Port de l'hôte (A-FAC-3) sur la partie publiée d'{@code operateurs} : les droits sur l'opérateur BNIA, dont
 * les rôles valent pour tous (RG-OPE-31, 32), et le module « Facturation » (spec 08 §3.12).
 */
@Component
class DroitsFacturationOperateurs implements DroitsFacturation {

    private final OperateursLecture operateurs;

    DroitsFacturationOperateurs(OperateursLecture operateurs) {
        this.operateurs = operateurs;
    }

    @Override
    public boolean gere(Acteur acteur) {
        return droits(acteur).gere(Module.FACTURATION);
    }

    @Override
    public boolean litTout(Acteur acteur) {
        return droits(acteur).personnelBnia();
    }

    @Override
    public Optional<String> nom(long utilisateur) {
        return operateurs.nomUtilisateur(utilisateur);
    }

    private Droits droits(Acteur acteur) {
        return operateurs.droits(acteur.utilisateur(), operateurs.operateurBnia());
    }
}
