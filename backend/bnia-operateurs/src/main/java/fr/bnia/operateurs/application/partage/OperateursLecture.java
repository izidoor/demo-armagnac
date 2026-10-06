package fr.bnia.operateurs.application.partage;

import fr.bnia.operateurs.domain.partage.ContactVue;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import java.util.List;
import java.util.Optional;

/**
 * Lecture des opérateurs par les autres contextes (ADR-012, spec 02 §5.2), seule porte d'entrée avec les
 * types de {@code domain.partage}. Lecture seule ; implémentée dans {@code operateurs}. Les codes, CVI,
 * SIRET et identifiants ODG sont comparés sans distinction de casse ni d'espaces.
 */
public interface OperateursLecture {

    /** Actif ou non, pour l'afficher là où il est référencé. */
    Optional<OperateurVue> operateur(long id);

    /** Imports Excel et reprise, dont les fichiers portent des codes. */
    Optional<OperateurVue> operateurParCode(String code);

    /** Passerelle ODG : d'abord par l'identifiant tiers ODG, unique (RG-OPE-07). */
    Optional<OperateurVue> operateurParIdentifiantOdg(String identifiantOdg);

    /** Passerelle ODG, à défaut d'identifiant ODG ; plusieurs fiches possibles (RG-OPE-06). */
    List<OperateurVue> operateursParCvi(String cvi);

    /** Passerelle ODG, à défaut d'identifiant ODG ; plusieurs fiches possibles (RG-OPE-06). */
    List<OperateurVue> operateursParSiret(String siret);

    /** RG-OPE-11 : opérateurs actifs au statut de déclaration {@code OUI}, triés par code. */
    List<OperateurVue> declarantsAttendus();

    /**
     * {@code acces} : contacts actifs, avec accès, de cet e-mail, dans des opérateurs actifs ; un par
     * opérateur (RG-OPE-28).
     */
    List<ContactVue> contactsAvecAccesParEmail(String email);

    /** Contacts d'un opérateur, actifs ou non, triés par nom. */
    List<ContactVue> contacts(long operateur);

    /** {@code notifications} : contacts actifs d'un opérateur qui ont ce centre d'intérêt (C.14). */
    List<ContactVue> contactsParCentreInteret(long operateur, long centreInteret);

    /**
     * RG-OPE-37 : opérateurs actifs dont le code, la raison sociale ou le nom commercial contient le texte ;
     * vingt au plus. Moins de trois caractères : aucun résultat.
     */
    List<ResultatRecherche> rechercher(String texte);
}
