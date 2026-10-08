package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.Module;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.support.AbstractIT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Partie publiée en lecture (ADR-012) : ce que les autres contextes lisent d'un opérateur. */
class OperateursLectureIT extends AbstractIT {

    private static final String URL = "/api/operateurs";

    @Autowired
    OperateursLecture lecture;

    @Test
    void lire_un_operateur_par_identifiant_code_identifiant_odg_cvi_et_siret() {
        String code = "T" + suffixe();
        String odg = "ODG-" + suffixe();
        var saisie = saisie(code, "Domaine " + suffixe());
        saisie.put("identifiantOdg", odg);
        saisie.put("cvi", "32032" + suffixe().substring(0, 5).replaceAll("[^A-Z0-9]", "0"));
        saisie.put("siret", "35243987300014");
        saisie.put("codePostal", "32100");
        long id = creer(URL, saisie);

        OperateurVue vue = lecture.operateur(id).orElseThrow();
        assertThat(vue.code()).isEqualTo(code);
        assertThat(vue.statutDeclarationStock()).isEqualTo(StatutDeclarationStock.OUI);
        assertThat(vue.adresse().codePostal()).contains("32100");
        assertThat(lecture.operateurParCode(code.toLowerCase())).map(OperateurVue::id).contains(id);
        assertThat(lecture.operateurParIdentifiantOdg(odg.toLowerCase())).map(OperateurVue::id).contains(id);
        assertThat(lecture.operateursParCvi(" " + saisie.get("cvi").toString().toLowerCase())).extracting(OperateurVue::id).contains(id);
        assertThat(lecture.operateursParSiret("352 439 873 00014")).extracting(OperateurVue::id).contains(id);
        assertThat(lecture.declarantsAttendus()).extracting(OperateurVue::id).contains(id);
        assertThat(lecture.operateur(-1)).isEmpty();
    }

    @Test
    @DisplayName("RG-OPE-11 et RG-OPE-26 : un opérateur inactif ou en sommeil n'est pas un déclarant attendu")
    void rg_ope_11_26_declarants_attendus() {
        long sommeil = creer(URL, saisie("T" + suffixe(), "Domaine " + suffixe()));
        client.put().uri(URL + "/{id}/production", sommeil).body(Map.of("periodicite", "ANNUELLE",
                "statutDeclarationStock", "SOMMEIL", "appellations", List.of(), "alambics", List.of())).exchange().expectStatus().isOk();
        long inactif = creer(URL, saisie("T" + suffixe(), "Domaine " + suffixe()));
        client.post().uri(URL + "/{id}/desactivation", inactif).exchange().expectStatus().isOk();

        assertThat(lecture.declarantsAttendus()).extracting(OperateurVue::id).doesNotContain(sommeil, inactif);
        assertThat(lecture.operateur(inactif)).map(OperateurVue::actif).contains(false);
    }

    @Test
    @DisplayName("RG-OPE-31 et 32 : droits d'un membre sur son opérateur ; le rôle tenu au BNIA vaut partout")
    void rg_ope_31_32_droits() {
        long condom = creer(URL, saisie("T" + suffixe(), "Chai de Condom"));
        long eauze = creer(URL, saisie("T" + suffixe(), "Chai d'Eauze"));
        long jeanne = membre(condom, Role.AGENT, emailUnique("jeanne"));
        long luc = membre(lecture.operateurBnia(), Role.OBSERVATEUR, emailUnique("luc"));

        Droits droits = lecture.droits(jeanne, condom);
        assertThat(droits.local()).contains(Role.AGENT);
        assertThat(droits.gere(Module.CONTACTS)).isTrue();
        assertThat(droits.gere(Module.ACCES)).isFalse();
        assertThat(droits.lit(Module.PARAMETRAGE)).isFalse();
        assertThat(lecture.droits(jeanne, eauze).aucunAcces()).isTrue();
        assertThat(lecture.droits(luc, eauze).bnia()).contains(Role.OBSERVATEUR);
        assertThat(lecture.droits(luc, eauze).lit(Module.PARAMETRAGE)).isTrue();
        assertThat(lecture.droits(luc, eauze).gere(Module.CONTACTS)).isFalse();
    }

    @Test
    @DisplayName("RG-AUT-04 : l'état de connexion suit le blocage, les suspensions et la désactivation des opérateurs")
    void etat_connexion() {
        long operateur = creer(URL, saisie("T" + suffixe(), "Domaine " + suffixe()));
        long jeanne = membre(operateur, Role.AGENT, emailUnique("jeanne"));
        assertThat(lecture.etatConnexion(jeanne)).isEqualTo(EtatConnexion.AUTORISE);

        client.post().uri(URL + "/{id}/desactivation", operateur).exchange().expectStatus().isOk();
        assertThat(lecture.etatConnexion(jeanne)).isEqualTo(EtatConnexion.AUCUN_ACCES_ACTIF);
        client.post().uri(URL + "/{id}/reactivation", operateur).exchange().expectStatus().isOk();
        client.post().uri("/api/utilisateurs/{id}/blocage", jeanne).body(Map.of("motif", "Essai")).exchange()
                .expectStatus().isOk();
        assertThat(lecture.etatConnexion(jeanne)).isEqualTo(EtatConnexion.UTILISATEUR_BLOQUE);
        assertThat(lecture.etatConnexion(-1)).isEqualTo(EtatConnexion.INCONNU);
        assertThat(lecture.nomUtilisateur(jeanne)).hasValueSatisfying(nom -> assertThat(nom).startsWith("Test Jeanne"));
    }

    @Test
    @DisplayName("Notifications (C.14) : contacts et membres d'un opérateur, triés par nom, avec centres d'intérêt et rôle")
    void personnes() {
        String code = "CI" + suffixe();
        long compta = creer("/api/referentiels/centres-interet", Map.of("code", code, "libelle", "Comptabilité " + code));
        long operateur = creer(URL, saisie("T" + suffixe(), "Domaine " + suffixe()));
        creer(URL + "/" + operateur + "/contacts", Map.of("nom", "Petit", "prenom", "Paul", "centresInteret", List.of(compta)));
        String email = emailUnique("durand");
        membre(operateur, Role.ADMIN, email);

        assertThat(lecture.personnes(operateur)).hasSize(2).anySatisfy(p -> {
            assertThat(p.nomComplet()).isEqualTo("Paul Petit");
            assertThat(p.centresInteret()).containsExactly(compta);
            assertThat(p.role()).isEmpty();
        }).anySatisfy(p -> {
            assertThat(p.email()).contains(email);
            assertThat(p.role()).contains(Role.ADMIN);
        });
    }

    @Test
    @DisplayName("RG-OPE-37 : recherche restreinte, actifs seulement, trois caractères, vingt résultats au plus")
    void rg_ope_37_recherche_restreinte() {
        String racine = "Rech" + suffixe();
        long actif = creer(URL, saisie("T" + suffixe(), racine + " actif"));
        long inactif = creer(URL, saisie("T" + suffixe(), racine + " inactif"));
        client.post().uri(URL + "/{id}/desactivation", inactif).exchange().expectStatus().isOk();
        for (int i = 0; i < 21; i++) {
            creer(URL, saisie("T" + suffixe(), racine + " série " + i));
        }

        List<ResultatRecherche> resultats = lecture.rechercher(racine.toLowerCase());
        assertThat(resultats).hasSize(20).extracting(ResultatRecherche::id).doesNotContain(inactif);
        assertThat(lecture.rechercher(racine + " actif")).extracting(ResultatRecherche::id).containsExactly(actif);
        assertThat(lecture.rechercher("Re")).isEmpty();
    }

    private static Map<String, Object> saisie(String code, String raisonSociale) {
        var saisie = new HashMap<String, Object>();
        saisie.put("type", "PRODUCTEUR");
        saisie.put("code", code);
        saisie.put("raisonSociale", raisonSociale);
        saisie.put("ville", "Condom");
        saisie.put("accordDiffusionGeneral", false);
        return saisie;
    }

    private static String suffixe() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
