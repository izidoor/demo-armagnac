package fr.bnia.authentification.application;

import fr.bnia.authentification.domain.CompteConnexion;
import fr.bnia.commun.application.UseCase;
import fr.bnia.commun.domain.AccesRefuse;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.Module;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Lectures du contexte : le journal des connexions (RG-AUT-13), l'état des comptes pour le back-office
 * (RG-OPE-53), et la validité d'une session à chaque requête (RG-AUT-09). Les droits sont ceux que
 * {@code operateurs} publie (ADR-016) ; ce contexte n'en porte aucun.
 */
@UseCase
public class ConsulterConnexions {

    private final JournalConnexions journal;
    private final CompteRepository comptes;
    private final OperateursLecture operateurs;
    private final Clock horloge;

    public ConsulterConnexions(JournalConnexions journal, CompteRepository comptes, OperateursLecture operateurs,
            Clock horloge) {
        this.journal = journal;
        this.comptes = comptes;
        this.operateurs = operateurs;
        this.horloge = horloge;
    }

    /** RG-AUT-13 : tout le journal, pour un rôle de l'opérateur BNIA (module Accès, lecture). */
    public List<LigneConnexion> journal(long acteur, FiltreConnexions filtre) {
        exigerBnia(acteur);
        return journal.lister(filtre);
    }

    /** RG-AUT-13 : chacun consulte ses propres connexions. */
    public List<LigneConnexion> mesConnexions(long utilisateur) {
        return journal.lister(new FiltreConnexions(utilisateur, null, null, null));
    }

    /** RG-OPE-53 : blocage temporaire et dernière connexion des utilisateurs, pour le BNIA. */
    public List<EtatCompte> etats(long acteur, List<Long> utilisateurs) {
        exigerBnia(acteur);
        return utilisateurs.stream().map(comptes::parUtilisateur).flatMap(Optional::stream)
                .map(c -> new EtatCompte(c.utilisateur(), c.bloqueJusqua().filter(b -> b.isAfter(horloge.instant())),
                        c.derniereConnexion(), c.aUnMotDePasse()))
                .toList();
    }

    /**
     * RG-AUT-09 : à chaque requête, la session ne vaut que si le compte existe encore et que
     * {@code operateurs} autorise la connexion : un blocage, une suspension ou un retrait prend effet aussitôt.
     */
    public boolean sessionValide(long utilisateur) {
        return comptes.parUtilisateur(utilisateur).filter(CompteConnexion::aUnMotDePasse).isPresent()
                && operateurs.etatConnexion(utilisateur) == EtatConnexion.AUTORISE;
    }

    private void exigerBnia(long acteur) {
        if (!operateurs.droits(acteur, operateurs.operateurBnia()).lit(Module.ACCES)) {
            throw new AccesRefuse("RG-AUT-13", "Le journal des connexions est réservé au personnel du BNIA.");
        }
    }
}
