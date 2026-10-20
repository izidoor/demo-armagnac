package fr.bnia.authentification.adapter.rest;

import fr.bnia.authentification.application.ConsulterConnexions;
import fr.bnia.authentification.application.Contexte;
import fr.bnia.authentification.application.EtatCompte;
import fr.bnia.authentification.application.EtatDoubleAuthentification;
import fr.bnia.authentification.application.FiltreConnexions;
import fr.bnia.authentification.application.GererDoubleAuthentification;
import fr.bnia.authentification.application.GererMotDePasse;
import fr.bnia.authentification.application.LigneConnexion;
import fr.bnia.authentification.application.ParametresAuthentification;
import fr.bnia.authentification.application.Resultat;
import fr.bnia.authentification.application.SeConnecter;
import fr.bnia.authentification.domain.EvenementConnexion;
import fr.bnia.commun.adapter.rest.Connecte;
import fr.bnia.commun.adapter.rest.SessionUtilisateur;
import fr.bnia.commun.domain.RegleEnfreinte;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Connexion unique de tous les utilisateurs (RG-AUT-01), mots de passe, journal des connexions (spec 03
 * §5). La session ne porte que l'identifiant de l'utilisateur (RG-AUT-09) ; entre le mot de passe et le
 * code, elle retient l'utilisateur en attente, sans l'authentifier.
 */
@RestController
@RequestMapping("/api/authentification")
@Tag(name = "authentification", description = "Connexion, double authentification, mots de passe, journal des connexions")
class AuthentificationController {

    /** Attribut de session : l'utilisateur dont le mot de passe est vérifié, en attente du code. */
    static final String EN_ATTENTE = "bnia.authentification.en-attente";
    /** Cookie d'un navigateur de confiance (RG-AUT-06). */
    static final String COOKIE_CONFIANCE = "BNIA_CONFIANCE";

    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");

    private final SeConnecter seConnecter;
    private final GererMotDePasse motDePasse;
    private final ConsulterConnexions connexions;
    private final GererDoubleAuthentification doubleAuthentification;
    private final SessionUtilisateur session;
    private final SecurityContextRepository depot;
    private final ParametresAuthentification parametres;

    AuthentificationController(SeConnecter seConnecter, GererMotDePasse motDePasse, ConsulterConnexions connexions,
            GererDoubleAuthentification doubleAuthentification, SessionUtilisateur session,
            SecurityContextRepository depot, ParametresAuthentification parametres) {
        this.seConnecter = seConnecter;
        this.motDePasse = motDePasse;
        this.connexions = connexions;
        this.doubleAuthentification = doubleAuthentification;
        this.session = session;
        this.depot = depot;
        this.parametres = parametres;
    }

    @Schema(name = "EtatSession")
    record EtatSessionResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean connecte,
            @Schema(description = "L'utilisateur connecté") Long utilisateur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Mot de passe vérifié, code attendu")
            boolean codeAttendu) {
    }

    @Schema(name = "EtapeConnexion")
    record EtapeResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Etape etape) {
    }

    /** Où en est la connexion. */
    @Schema(name = "Etape")
    enum Etape {
        /** RG-AUT-01, 16 : un code est parti par e-mail. */
        CODE_REQUIS,
        /** La session est ouverte. */
        CONNECTE
    }

    @Schema(name = "SaisieConnexion")
    record ConnexionRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "admin@bnia.fr") String email,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String motDePasse) {
    }

    @Schema(name = "SaisieCode")
    record CodeRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "000000") String code,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Faire confiance à ce navigateur 30 jours (RG-AUT-06)")
            boolean faireConfiance) {
    }

    @Schema(name = "SaisieEmail")
    record EmailRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) String email) {
    }

    @Schema(name = "SaisieNouveauMotDePasse", description = "Un nouveau mot de passe, avec le jeton du lien reçu")
    record NouveauMotDePasseRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String jeton,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Douze caractères au moins (RG-AUT-07)")
            String motDePasse) {
    }

    @Schema(name = "SaisieChangementMotDePasse")
    record ChangementMotDePasseRequest(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String actuel,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Douze caractères au moins (RG-AUT-07)")
            String nouveau) {
    }

    @Schema(name = "LigneConnexion", description = "Entrée du journal des connexions (RG-AUT-12)")
    record LigneConnexionResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant date,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String identifiantSaisi,
            Long utilisateur,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EvenementConnexion evenement,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String libelle,
            String detail,
            String adresseIp,
            String navigateur) {

        static LigneConnexionResponse depuis(LigneConnexion l) {
            return new LigneConnexionResponse(l.date(), l.identifiantSaisi(), l.utilisateur(), l.evenement(),
                    l.evenement().libelle(), l.detail(), l.adresseIp(), l.navigateur());
        }
    }

    @Schema(name = "EtatCompte", description = "Blocage temporaire en cours et dernière connexion (RG-OPE-53)")
    record EtatCompteResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long utilisateur,
            Instant bloqueJusqua,
            Instant derniereConnexion,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean motDePasseDefini) {

        static EtatCompteResponse depuis(EtatCompte e) {
            return new EtatCompteResponse(e.utilisateur(), e.bloqueJusqua().orElse(null),
                    e.derniereConnexion().orElse(null), e.motDePasseDefini());
        }
    }

    @Schema(name = "DoubleAuthentification", description = "Réglage de la double authentification (RG-AUT-16)")
    record DoubleAuthentificationResponse(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean active,
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "L'utilisateur est ADMIN du BNIA")
            boolean modifiable) {

        static DoubleAuthentificationResponse depuis(EtatDoubleAuthentification e) {
            return new DoubleAuthentificationResponse(e.active(), e.modifiable());
        }
    }

    @Schema(name = "SaisieDoubleAuthentification")
    record DoubleAuthentificationRequest(@Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean active) {
    }

    @GetMapping("/session")
    @Operation(operationId = "consulterSession",
            summary = "État de la session ; pose aussi le cookie du jeton CSRF, à appeler au chargement du front")
    EtatSessionResponse etat(HttpServletRequest requete) {
        Optional<Long> utilisateur = session.utilisateurConnecte();
        HttpSession courante = requete.getSession(false);
        boolean attendu = courante != null && courante.getAttribute(EN_ATTENTE) != null;
        return new EtatSessionResponse(utilisateur.isPresent(), utilisateur.orElse(null), utilisateur.isEmpty() && attendu);
    }

    @PostMapping("/connexion")
    @Operation(operationId = "seConnecter",
            summary = "E-mail et mot de passe ; un code part par e-mail, sauf sur un navigateur de confiance (RG-AUT-01 à 04, 06)")
    EtapeResponse connexion(@RequestBody ConnexionRequest saisie,
            @CookieValue(name = COOKIE_CONFIANCE, required = false) String confiance, HttpServletRequest requete,
            HttpServletResponse reponse) {
        return suite(seConnecter.verifierMotDePasse(saisie.email(), saisie.motDePasse(), confiance, contexte(requete)),
                requete, reponse);
    }

    @PostMapping("/code")
    @Operation(operationId = "verifierCode",
            summary = "Le code reçu par e-mail ; ouvre la session, et met le navigateur en confiance si demandé (RG-AUT-03, 05, 06)")
    EtapeResponse code(@RequestBody CodeRequest saisie, HttpServletRequest requete, HttpServletResponse reponse) {
        return suite(seConnecter.verifierCode(enAttente(requete), saisie.code(), saisie.faireConfiance(),
                contexte(requete)), requete, reponse);
    }

    @PostMapping("/code/renvoi")
    @Operation(operationId = "renvoyerCode", summary = "Un nouveau code, qui invalide le précédent (RG-AUT-05)")
    EtapeResponse renvoyerCode(HttpServletRequest requete, HttpServletResponse reponse) {
        return suite(seConnecter.renvoyerCode(enAttente(requete), contexte(requete)), requete, reponse);
    }

    @PostMapping("/deconnexion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "seDeconnecter", summary = "Fermer la session (RG-AUT-09)")
    void deconnexion(Connecte connecte, HttpServletRequest requete) {
        seConnecter.seDeconnecter(connecte.utilisateur(), contexte(requete));
        fermer(requete);
    }

    @PostMapping("/premier-mot-de-passe")
    @Operation(operationId = "definirPremierMotDePasse",
            summary = "Après l'acceptation d'une invitation : le mot de passe, avec le même jeton ; la session s'ouvre (RG-AUT-15)")
    EtapeResponse premierMotDePasse(@RequestBody NouveauMotDePasseRequest saisie, HttpServletRequest requete,
            HttpServletResponse reponse) {
        long utilisateur = motDePasse.definirPremierMotDePasse(saisie.jeton(), saisie.motDePasse(), contexte(requete));
        ouvrir(utilisateur, requete, reponse);
        return new EtapeResponse(Etape.CONNECTE);
    }

    @PutMapping("/mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "changerMotDePasse", summary = "Changer son mot de passe, l'actuel exigé (RG-AUT-06, 07)")
    void changerMotDePasse(Connecte connecte, @RequestBody ChangementMotDePasseRequest saisie,
            HttpServletRequest requete) {
        motDePasse.changer(connecte.utilisateur(), saisie.actuel(), saisie.nouveau(), contexte(requete));
    }

    @PostMapping("/mot-de-passe-oublie")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "demanderReinitialisation",
            summary = "Mot de passe oublié : un lien valable une heure part par e-mail (RG-AUT-08)")
    void motDePasseOublie(@RequestBody EmailRequest saisie, HttpServletRequest requete) {
        motDePasse.demanderReinitialisation(saisie.email(), contexte(requete));
    }

    @PostMapping("/reinitialisation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "reinitialiserMotDePasse",
            summary = "Un nouveau mot de passe, avec le lien reçu ; lève le blocage, ferme les sessions (RG-AUT-06 à 08)")
    void reinitialiser(@RequestBody NouveauMotDePasseRequest saisie, HttpServletRequest requete) {
        motDePasse.reinitialiser(saisie.jeton(), saisie.motDePasse(), contexte(requete));
    }

    @GetMapping("/mes-connexions")
    @Operation(operationId = "listerMesConnexions", summary = "Ses propres connexions, « Mon compte » (RG-AUT-13)")
    List<LigneConnexionResponse> mesConnexions(Connecte connecte) {
        return connexions.mesConnexions(connecte.utilisateur()).stream().map(LigneConnexionResponse::depuis).toList();
    }

    @GetMapping("/journal")
    @Operation(operationId = "listerConnexions",
            summary = "Journal des connexions, filtré ; personnel du BNIA (module Accès, lecture) (RG-AUT-12, 13)")
    List<LigneConnexionResponse> journal(Connecte connecte,
            @RequestParam(required = false) Long utilisateur,
            @RequestParam(required = false) EvenementConnexion evenement,
            @Parameter(description = "Inclus") @RequestParam(required = false) LocalDate du,
            @Parameter(description = "Inclus") @RequestParam(required = false) LocalDate au) {
        var filtre = new FiltreConnexions(utilisateur, evenement,
                du == null ? null : du.atStartOfDay(PARIS).toInstant(),
                au == null ? null : au.plusDays(1).atStartOfDay(PARIS).toInstant());
        return connexions.journal(connecte.utilisateur(), filtre).stream().map(LigneConnexionResponse::depuis).toList();
    }

    @GetMapping("/double-authentification")
    @Operation(operationId = "consulterDoubleAuthentification",
            summary = "Double authentification activée ou non, pour toute l'application ; personnel du BNIA (RG-AUT-16)")
    DoubleAuthentificationResponse consulterDoubleAuthentification(Connecte connecte) {
        return DoubleAuthentificationResponse.depuis(doubleAuthentification.consulter(connecte.utilisateur()));
    }

    @PutMapping("/double-authentification")
    @Operation(operationId = "definirDoubleAuthentification",
            summary = "Activer ou désactiver la double authentification pour toute l'application ; ADMIN du BNIA (RG-AUT-12, 16)")
    DoubleAuthentificationResponse definirDoubleAuthentification(Connecte connecte,
            @RequestBody DoubleAuthentificationRequest saisie, HttpServletRequest requete) {
        return DoubleAuthentificationResponse.depuis(
                doubleAuthentification.definir(connecte.utilisateur(), saisie.active(), contexte(requete)));
    }

    @GetMapping("/comptes")
    @Operation(operationId = "listerEtatsComptes",
            summary = "Blocage temporaire et dernière connexion de ces utilisateurs ; personnel du BNIA (RG-OPE-53)")
    List<EtatCompteResponse> etatsComptes(Connecte connecte, @RequestParam List<Long> utilisateurs) {
        return connexions.etats(connecte.utilisateur(), utilisateurs).stream().map(EtatCompteResponse::depuis).toList();
    }

    private EtapeResponse suite(Resultat resultat, HttpServletRequest requete, HttpServletResponse reponse) {
        return switch (resultat) {
            case Resultat.CodeEnvoye envoye -> {
                requete.getSession(true).setAttribute(EN_ATTENTE, envoye.utilisateur());
                yield new EtapeResponse(Etape.CODE_REQUIS);
            }
            case Resultat.Connecte connecte -> {
                ouvrir(connecte.utilisateur(), requete, reponse);
                connecte.jetonConfiance().ifPresent(jeton -> poserCookieConfiance(jeton, requete, reponse));
                yield new EtapeResponse(Etape.CONNECTE);
            }
            case Resultat.Refuse refus -> throw new RegleEnfreinte(refus.regle(), refus.message());
        };
    }

    /** La session s'ouvre : nouvel identifiant de session, principal = identifiant de l'utilisateur (A-AUT-3). */
    private void ouvrir(long utilisateur, HttpServletRequest requete, HttpServletResponse reponse) {
        HttpSession courante = requete.getSession(true);
        courante.removeAttribute(EN_ATTENTE);
        requete.changeSessionId();
        SecurityContext contexte = SecurityContextHolder.createEmptyContext();
        contexte.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(String.valueOf(utilisateur), null,
                List.of()));
        SecurityContextHolder.setContext(contexte);
        depot.saveContext(contexte, requete, reponse);
    }

    private static void fermer(HttpServletRequest requete) {
        SecurityContextHolder.clearContext();
        HttpSession courante = requete.getSession(false);
        if (courante != null) {
            courante.invalidate();
        }
    }

    private void poserCookieConfiance(String jeton, HttpServletRequest requete, HttpServletResponse reponse) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_CONFIANCE, jeton)
                .httpOnly(true)
                .secure(requete.isSecure())
                .sameSite("Lax")
                .path("/api/authentification")
                .maxAge(parametres.politique().validiteConfiance())
                .build();
        reponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private static long enAttente(HttpServletRequest requete) {
        HttpSession courante = requete.getSession(false);
        Object utilisateur = courante == null ? null : courante.getAttribute(EN_ATTENTE);
        if (!(utilisateur instanceof Long id)) {
            throw new RegleEnfreinte("RG-AUT-01", "Saisissez d'abord votre e-mail et votre mot de passe.");
        }
        return id;
    }

    private static Contexte contexte(HttpServletRequest requete) {
        return new Contexte(requete.getRemoteAddr(), requete.getHeader(HttpHeaders.USER_AGENT));
    }
}
