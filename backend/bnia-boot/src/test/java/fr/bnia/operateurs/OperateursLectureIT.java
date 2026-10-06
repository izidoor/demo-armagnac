package fr.bnia.operateurs;

import static org.assertj.core.api.Assertions.assertThat;

import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.ContactVue;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
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
    @DisplayName("RG-OPE-28 et RG-OPE-29 : contacts avec accès d'un e-mail, un par opérateur actif")
    void rg_ope_28_29_contacts_d_un_email() {
        String email = "jeanne." + suffixe().toLowerCase() + "@exemple.fr";
        long condom = creer(URL, saisie("T" + suffixe(), "Chai de Condom"));
        long eauze = creer(URL, saisie("T" + suffixe(), "Chai d'Eauze"));
        long aCondom = creer(URL + "/" + condom + "/contacts", Map.of("nom", "Durand", "email", email));
        long aEauze = creer(URL + "/" + eauze + "/contacts", Map.of("nom", "Durand", "email", email.toUpperCase()));
        client.put().uri(URL + "/{o}/contacts/{c}/portail", condom, aCondom).body(Map.of("acces", true, "administrateur", true))
                .exchange().expectStatus().isOk();

        assertThat(lecture.contactsAvecAccesParEmail(email)).extracting(ContactVue::id).containsExactly(aCondom);
        client.put().uri(URL + "/{o}/contacts/{c}/portail", eauze, aEauze)
                .body(Map.of("acces", true, "administrateur", false, "modules", List.of("DSM"))).exchange().expectStatus().isOk();
        assertThat(lecture.contactsAvecAccesParEmail(email.toUpperCase())).extracting(ContactVue::operateur)
                .containsExactlyInAnyOrder(condom, eauze);
        assertThat(lecture.contacts(eauze)).singleElement().satisfies(c -> assertThat(c.modules()).hasSize(1));
    }

    @Test
    @DisplayName("Notifications (C.14) : contacts actifs d'un opérateur par centre d'intérêt")
    void contacts_par_centre_interet() {
        String code = "CI" + suffixe();
        long compta = creer("/api/referentiels/centres-interet", Map.of("code", code, "libelle", "Comptabilité " + code));
        long operateur = creer(URL, saisie("T" + suffixe(), "Domaine " + suffixe()));
        long durand = creer(URL + "/" + operateur + "/contacts", Map.of("nom", "Durand", "centresInteret", List.of(compta)));
        creer(URL + "/" + operateur + "/contacts", Map.of("nom", "Petit"));

        assertThat(lecture.contactsParCentreInteret(operateur, compta)).extracting(ContactVue::id).containsExactly(durand);
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
