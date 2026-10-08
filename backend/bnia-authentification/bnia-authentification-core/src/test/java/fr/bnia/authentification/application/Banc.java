package fr.bnia.authentification.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.bnia.authentification.domain.CompteConnexion;
import fr.bnia.authentification.domain.Politique;
import fr.bnia.authentification.domain.Secrets;
import fr.bnia.commun.domain.ConflitUnicite;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Le contexte en mémoire, pour les tests de cas d'usage : comptes, journal, e-mails et sessions fermées
 * retenus, hachage factice, horloge réglable ; {@code operateurs} simulé.
 */
final class Banc {

    static final Contexte ICI = new Contexte("192.0.2.1", "Firefox");
    static final String MOT_DE_PASSE = "demo-bnia-2026";

    final Horloge horloge = new Horloge(Instant.parse("2026-10-08T08:00:00Z"));
    final Comptes comptes = new Comptes();
    final Journal journal = new Journal();
    final List<Courriel> courriels = new ArrayList<>();
    final List<Long> sessionsFermees = new ArrayList<>();
    final OperateursLecture operateurs = mock(OperateursLecture.class);
    final HachageMotDePasse hachage = new HachageMotDePasse() {
        @Override
        public String hacher(String clair) {
            return "h:" + clair;
        }

        @Override
        public boolean correspond(String clair, String empreinte) {
            return empreinte.equals("h:" + clair);
        }
    };
    final ParametresAuthentification parametres = new ParametresAuthentification(new Politique(Duration.ofMinutes(10),
            Duration.ofHours(1), Duration.ofDays(30), 3, Duration.ofMinutes(30), 12, Optional.empty()),
            "https://bnia.example");

    SeConnecter seConnecter() {
        return new SeConnecter(comptes, hachage, operateurs, journal, courriels::add, parametres, horloge);
    }

    GererMotDePasse gererMotDePasse() {
        return new GererMotDePasse(comptes, hachage, operateurs, sessionsFermees::add, journal, courriels::add,
                parametres, horloge);
    }

    GererComptes gererComptes() {
        return new GererComptes(comptes, sessionsFermees::add);
    }

    ConsulterConnexions consulterConnexions() {
        return new ConsulterConnexions(journal, comptes, operateurs, horloge);
    }

    /** Un compte avec mot de passe, autorisé à se connecter par {@code operateurs}. */
    CompteConnexion compte(long utilisateur, String email) {
        CompteConnexion compte = CompteConnexion.creer(utilisateur, email);
        compte.definirMotDePasse(hachage.hacher(MOT_DE_PASSE), horloge.instant());
        when(operateurs.etatConnexion(utilisateur)).thenReturn(EtatConnexion.AUTORISE);
        return comptes.inserer(compte);
    }

    /** Le code du dernier e-mail envoyé. */
    String dernierCode() {
        String texte = courriels.getLast().texte();
        int debut = texte.indexOf(" : ") + 3;
        return texte.substring(debut, debut + 6);
    }

    /** Le jeton du dernier lien envoyé. */
    String dernierJeton() {
        String texte = courriels.getLast().texte();
        int debut = texte.indexOf("jeton=") + "jeton=".length();
        return java.net.URLDecoder.decode(texte.substring(debut, texte.indexOf('\n', debut)),
                java.nio.charset.StandardCharsets.UTF_8);
    }

    static final class Horloge extends Clock {

        private Instant maintenant;

        Horloge(Instant depart) {
            this.maintenant = depart;
        }

        void avancer(Duration duree) {
            maintenant = maintenant.plus(duree);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return maintenant;
        }
    }

    static final class Comptes implements CompteRepository {

        final Map<Long, CompteConnexion> parId = new LinkedHashMap<>();
        private long prochain = 1;

        @Override
        public Optional<CompteConnexion> parUtilisateur(long utilisateur) {
            return parId.values().stream().filter(c -> c.utilisateur() == utilisateur).findFirst();
        }

        @Override
        public Optional<CompteConnexion> parIdentifiant(String identifiant) {
            return parId.values().stream().filter(c -> c.identifiant().equals(identifiant)).findFirst();
        }

        @Override
        public Optional<CompteConnexion> parEmpreinteReinitialisation(String empreinte) {
            return parId.values().stream()
                    .filter(c -> c.reinitialisation().filter(r -> r.empreinte().equals(empreinte)).isPresent())
                    .findFirst();
        }

        @Override
        public CompteConnexion inserer(CompteConnexion c) {
            verifierUnicite(c, null);
            CompteConnexion enregistre = CompteConnexion.reconstituer(prochain++, c.utilisateur(), c.identifiant(),
                    c.motDePasse().orElse(null), c.echecs(), c.bloqueJusqua().orElse(null),
                    c.derniereConnexion().orElse(null), c.motDePasseChangeLe().orElse(null), c.code().orElse(null),
                    c.reinitialisation().orElse(null), c.navigateurs());
            parId.put(enregistre.id(), enregistre);
            return enregistre;
        }

        @Override
        public void mettreAJour(CompteConnexion compte) {
            verifierUnicite(compte, compte.id());
            parId.put(compte.id(), compte);
        }

        @Override
        public void supprimer(long id) {
            parId.remove(id);
        }

        private void verifierUnicite(CompteConnexion compte, Long sauf) {
            if (parId.values().stream().anyMatch(c -> !c.id().equals(sauf) && c.identifiant().equals(compte.identifiant()))) {
                throw new ConflitUnicite("RG-AUT-10", "Identifiant déjà pris.");
            }
        }
    }

    static final class Journal implements JournalConnexions {

        final List<LigneConnexion> lignes = new ArrayList<>();

        @Override
        public void ajouter(LigneConnexion ligne) {
            lignes.add(ligne);
        }

        @Override
        public List<LigneConnexion> lister(FiltreConnexions filtre) {
            return lignes.stream()
                    .filter(l -> filtre.utilisateur() == null || filtre.utilisateur().equals(l.utilisateur()))
                    .filter(l -> filtre.evenement() == null || filtre.evenement() == l.evenement())
                    .sorted(Comparator.comparing(LigneConnexion::date).reversed())
                    .toList();
        }
    }

    static String empreinte(String clair) {
        return Secrets.empreinte(clair);
    }
}
