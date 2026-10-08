package fr.bnia.authentification.domain;

import fr.bnia.commun.domain.RegleEnfreinte;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Compte de connexion (spec 03 §3.1) : la preuve d'identité d'un utilisateur de {@code operateurs}, sans
 * nom ni droit. Racine de son agrégat, avec le code de vérification en cours, la réinitialisation en
 * cours et les navigateurs de confiance. Le mot de passe est une empreinte (hachage lent, RG-AUT-07),
 * calculée hors du domaine.
 */
public final class CompteConnexion {

    private final Long id;
    private final long utilisateur;
    private String identifiant;
    private String motDePasse;
    private int echecs;
    private Instant bloqueJusqua;
    private Instant derniereConnexion;
    private Instant motDePasseChangeLe;
    private Echeance code;
    private Echeance reinitialisation;
    private final List<NavigateurConfiance> navigateurs;

    private CompteConnexion(Long id, long utilisateur, String identifiant, String motDePasse, int echecs,
            Instant bloqueJusqua, Instant derniereConnexion, Instant motDePasseChangeLe, Echeance code,
            Echeance reinitialisation, List<NavigateurConfiance> navigateurs) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.identifiant = identifiant(identifiant);
        this.motDePasse = motDePasse;
        this.echecs = echecs;
        this.bloqueJusqua = bloqueJusqua;
        this.derniereConnexion = derniereConnexion;
        this.motDePasseChangeLe = motDePasseChangeLe;
        this.code = code;
        this.reinitialisation = reinitialisation;
        this.navigateurs = new ArrayList<>(navigateurs);
    }

    /** RG-AUT-11 : créé sans mot de passe quand {@code operateurs} crée l'utilisateur ; aucune connexion avant. */
    public static CompteConnexion creer(long utilisateur, String email) {
        return new CompteConnexion(null, utilisateur, email, null, 0, null, null, null, null, null, List.of());
    }

    public static CompteConnexion reconstituer(Long id, long utilisateur, String identifiant, String motDePasse,
            int echecs, Instant bloqueJusqua, Instant derniereConnexion, Instant motDePasseChangeLe, Echeance code,
            Echeance reinitialisation, List<NavigateurConfiance> navigateurs) {
        return new CompteConnexion(id, utilisateur, identifiant, motDePasse, echecs, bloqueJusqua, derniereConnexion,
                motDePasseChangeLe, code, reinitialisation, navigateurs);
    }

    /** RG-AUT-01, 10 : l'e-mail de l'utilisateur, sans distinction de casse. */
    public static String identifiant(String email) {
        if (email == null || email.isBlank()) {
            throw new RegleEnfreinte("RG-AUT-10", "L'identifiant de connexion est l'e-mail de l'utilisateur : il est obligatoire.");
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    /** RG-AUT-07 : douze caractères au moins, sans autre contrainte. */
    public static void verifierMotDePasse(String clair, Politique politique) {
        if (clair == null || clair.length() < politique.longueurMotDePasse()) {
            throw new RegleEnfreinte("RG-AUT-07", "Le mot de passe doit compter au moins "
                    + politique.longueurMotDePasse() + " caractères.");
        }
    }

    /** RG-AUT-03 : bloqué jusqu'à une date, après trop d'échecs. */
    public boolean estBloque(Instant maintenant) {
        return bloqueJusqua != null && maintenant.isBefore(bloqueJusqua);
    }

    /** RG-AUT-11, 15 : sans mot de passe, aucune connexion. */
    public boolean aUnMotDePasse() {
        return motDePasse != null;
    }

    /**
     * RG-AUT-03 : un échec (mot de passe ou code faux, H-AUT-2) ; au troisième consécutif, le compte est
     * bloqué pour la durée donnée et le compteur repart de zéro.
     *
     * @return vrai si cet échec déclenche le blocage
     */
    public boolean enregistrerEchec(Instant maintenant, Politique politique) {
        echecs++;
        if (echecs >= politique.echecsAvantBlocage()) {
            echecs = 0;
            bloqueJusqua = maintenant.plus(politique.dureeBlocage());
            code = null;
            return true;
        }
        return false;
    }

    /** RG-AUT-03 : une connexion réussie remet le compteur à zéro. */
    public void reussir(Instant maintenant) {
        echecs = 0;
        bloqueJusqua = null;
        code = null;
        derniereConnexion = maintenant;
    }

    /**
     * RG-AUT-01, 05 : un code à 6 chiffres, valable le temps donné, une seule fois ; il remplace le
     * précédent.
     *
     * @return le code en clair, à envoyer par e-mail
     */
    public String emettreCode(Instant maintenant, Politique politique) {
        String clair = politique.codeFixe().orElseGet(Secrets::code);
        code = new Echeance(Secrets.empreinte(clair), maintenant.plus(politique.validiteCode()));
        return clair;
    }

    /** RG-AUT-05 : vrai si le code est celui en cours et n'a pas expiré ; il ne resservira pas. */
    public boolean verifierCode(String saisi, Instant maintenant) {
        if (code == null || !code.accepte(saisi == null ? null : saisi.strip(), maintenant)) {
            return false;
        }
        code = null;
        return true;
    }

    /**
     * RG-AUT-06 : ce navigateur est dispensé de code jusqu'à l'échéance ; renvoie le jeton de son cookie.
     */
    public String faireConfiance(Instant maintenant, Politique politique) {
        String jeton = Secrets.jeton();
        navigateurs.removeIf(n -> !maintenant.isBefore(n.expireLe()));
        navigateurs.add(new NavigateurConfiance(Secrets.empreinte(jeton), maintenant,
                maintenant.plus(politique.validiteConfiance())));
        return jeton;
    }

    /** RG-AUT-06 : le jeton du cookie désigne un navigateur de confiance de ce compte, encore valable. */
    public boolean estDeConfiance(String jeton, Instant maintenant) {
        if (jeton == null || jeton.isBlank()) {
            return false;
        }
        String empreinte = Secrets.empreinte(jeton);
        return navigateurs.stream().anyMatch(n -> Secrets.egales(n.empreinte(), empreinte)
                && maintenant.isBefore(n.expireLe()));
    }

    /**
     * RG-AUT-06, 07, 15 : un nouveau mot de passe (empreinte) ; les navigateurs de confiance sont oubliés.
     */
    public void definirMotDePasse(String empreinte, Instant maintenant) {
        this.motDePasse = empreinte;
        this.motDePasseChangeLe = maintenant;
        this.navigateurs.clear();
    }

    /**
     * RG-AUT-08 : un lien valable le temps donné, une seule fois ; une nouvelle demande remplace la
     * précédente.
     *
     * @return le jeton en clair, à mettre dans le lien
     */
    public String demanderReinitialisation(Instant maintenant, Politique politique) {
        String jeton = Secrets.jeton();
        reinitialisation = new Echeance(Secrets.empreinte(jeton), maintenant.plus(politique.validiteLien()));
        return jeton;
    }

    /**
     * RG-AUT-06, 08 : réinitialiser lève le blocage temporaire, remet le compteur à zéro, oublie les
     * navigateurs de confiance ; le lien ne resservira pas.
     */
    public void reinitialiser(String jeton, String empreinteMotDePasse, Instant maintenant) {
        if (reinitialisation == null || !reinitialisation.accepte(jeton, maintenant)) {
            throw new RegleEnfreinte("RG-AUT-08", "Ce lien de réinitialisation a expiré ou a déjà servi : refaites"
                    + " la demande « mot de passe oublié ».");
        }
        reinitialisation = null;
        echecs = 0;
        bloqueJusqua = null;
        code = null;
        definirMotDePasse(empreinteMotDePasse, maintenant);
    }

    /** RG-AUT-10 : l'utilisateur a confirmé un nouvel e-mail dans {@code operateurs}. */
    public void changerIdentifiant(String email) {
        this.identifiant = identifiant(email);
    }

    public Long id() {
        return id;
    }

    public long utilisateur() {
        return utilisateur;
    }

    public String identifiant() {
        return identifiant;
    }

    /** L'empreinte du mot de passe ; vide avant le premier (RG-AUT-15). */
    public Optional<String> motDePasse() {
        return Optional.ofNullable(motDePasse);
    }

    public int echecs() {
        return echecs;
    }

    public Optional<Instant> bloqueJusqua() {
        return Optional.ofNullable(bloqueJusqua);
    }

    public Optional<Instant> derniereConnexion() {
        return Optional.ofNullable(derniereConnexion);
    }

    public Optional<Instant> motDePasseChangeLe() {
        return Optional.ofNullable(motDePasseChangeLe);
    }

    public Optional<Echeance> code() {
        return Optional.ofNullable(code);
    }

    public Optional<Echeance> reinitialisation() {
        return Optional.ofNullable(reinitialisation);
    }

    public List<NavigateurConfiance> navigateurs() {
        return List.copyOf(navigateurs);
    }
}
