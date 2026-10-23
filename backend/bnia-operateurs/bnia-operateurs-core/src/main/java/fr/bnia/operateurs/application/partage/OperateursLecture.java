package fr.bnia.operateurs.application.partage;

import fr.bnia.operateurs.domain.partage.AcheteurVue;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.EtatConnexion;
import fr.bnia.operateurs.domain.partage.IdentiteLegaleVue;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.PersonneVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Lecture des opérateurs, de leurs personnes et des droits par les autres contextes (ADR-012, amendé par
 * ADR-016 ; spec 02 §5.2), seule porte d'entrée avec les types de {@code domain.partage}. Lecture seule ;
 * implémentée dans {@code operateurs}. Les codes, CVI, SIRET et identifiants ODG sont comparés sans
 * distinction de casse ni d'espaces.
 */
public interface OperateursLecture {

    /** Actif ou non, pour l'afficher là où il est référencé. */
    Optional<OperateurVue> operateur(long id);

    /** {@code facturation} : l'opérateur vu comme acheteur, avec ce qui manque pour le facturer (RG-OPE-02, 23). */
    Optional<AcheteurVue> acheteur(long id);

    /** {@code contratsarmagnac} : SIRET, CVI et téléphone, imprimés sur le contrat (spec 06 RG-CA-20). */
    Optional<IdentiteLegaleVue> identiteLegale(long id);

    /** Imports Excel, dont les fichiers portent des codes. */
    Optional<OperateurVue> operateurParCode(String code);

    /** Passerelle ODG : d'abord par l'identifiant tiers ODG, unique (RG-OPE-07). */
    Optional<OperateurVue> operateurParIdentifiantOdg(String identifiantOdg);

    /** Passerelle ODG, à défaut d'identifiant ODG ; plusieurs fiches possibles (RG-OPE-06). */
    List<OperateurVue> operateursParCvi(String cvi);

    /** Passerelle ODG, à défaut d'identifiant ODG ; plusieurs fiches possibles (RG-OPE-06). */
    List<OperateurVue> operateursParSiret(String siret);

    /** RG-OPE-11, 39 : opérateurs actifs au statut de déclaration {@code OUI}, triés par code ; jamais le BNIA. */
    List<OperateurVue> declarantsAttendus();

    /**
     * RG-OPE-37, 39 : opérateurs actifs, autres que le BNIA, dont le code, la raison sociale ou le nom
     * commercial contient le texte ; vingt au plus. Moins de trois caractères : aucun résultat.
     */
    List<ResultatRecherche> rechercher(String texte);

    /**
     * L'identité (code, raison sociale, nom commercial, ville) de ces opérateurs, en une seule lecture, pour
     * les listings d'autres contextes ; un identifiant inconnu est ignoré.
     */
    List<ResultatRecherche> identites(Collection<Long> ids);

    /**
     * RG-OPE-02 : ceux de ces opérateurs qui sont facturables, en une seule lecture, pour les listings d'autres
     * contextes ; un identifiant inconnu est ignoré.
     */
    Set<Long> facturables(Collection<Long> ids);

    /** RG-OPE-39 : l'identifiant de l'opérateur BNIA. */
    long operateurBnia();

    /**
     * RG-OPE-31, 32 : les droits d'un utilisateur sur un opérateur, évalués à l'appel, à vérifier dans chaque
     * cas d'usage (ADR-016).
     */
    Droits droits(long utilisateur, long operateur);

    /** Copie du nom de l'auteur d'une action, à l'instant de l'action (A-OPE-8) ; vide s'il n'existe plus. */
    Optional<String> nomUtilisateur(long utilisateur);

    /** {@code notifications} : contacts et membres d'un opérateur, triés par nom (C.14 p.136). */
    List<PersonneVue> personnes(long operateur);

    /** {@code authentification} (RG-AUT-04, 09) : l'utilisateur peut-il se connecter ? */
    EtatConnexion etatConnexion(long utilisateur);

    /** {@code authentification} (RG-AUT-01, 08) : l'utilisateur de cet e-mail, à la casse près. */
    Optional<Long> utilisateurParEmail(String email);

    /** {@code authentification} (RG-AUT-15) : l'utilisateur qui a accepté l'invitation de ce jeton il y a moins d'une heure. */
    Optional<Long> utilisateurDInvitationAcceptee(String jeton);
}
