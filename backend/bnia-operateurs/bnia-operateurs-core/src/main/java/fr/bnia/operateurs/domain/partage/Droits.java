package fr.bnia.operateurs.domain.partage;

import java.util.Optional;

/**
 * Droits d'un utilisateur sur un opérateur, évalués à chaque requête (RG-OPE-31, 32) : le rôle de son
 * rattachement {@code ACTIF} dans cet opérateur, s'il est actif, et le rôle de son rattachement
 * {@code ACTIF} dans l'opérateur BNIA, qui vaut pour tous les opérateurs. Sans l'un ni l'autre : aucun
 * droit.
 *
 * @param operateur identifiant de l'opérateur concerné
 * @param local     rôle tenu dans cet opérateur
 * @param bnia      rôle tenu dans l'opérateur BNIA (le même que {@code local} pour l'opérateur BNIA)
 */
public record Droits(long operateur, Optional<Role> local, Optional<Role> bnia) {

    public static Droits aucun(long operateur) {
        return new Droits(operateur, Optional.empty(), Optional.empty());
    }

    /** RG-OPE-31 : tout rattachement {@code ACTIF} lit tout son opérateur ; le Paramétrage est au BNIA. */
    public boolean lit(Module module) {
        return module == Module.PARAMETRAGE ? bnia.isPresent() : !aucunAcces();
    }

    /**
     * RG-OPE-31, 32 : gérer un module, par le rôle local ou par le rôle tenu au BNIA ; le Paramétrage et la
     * Facturation, par le rôle tenu au BNIA seulement (spec 08 §3.12) ; les Contrats, par le rôle local seulement
     * (RG-CA-01 : le BNIA les lit, seules les parties agissent).
     */
    public boolean gere(Module module) {
        boolean parBnia = bnia.map(r -> r.gere(module)).orElse(false);
        boolean parLocal = local.map(r -> r.gere(module)).orElse(false);
        return switch (module) {
            case PARAMETRAGE, FACTURATION -> parBnia;
            case CONTRATS -> parLocal;
            default -> parBnia || parLocal;
        };
    }

    /** RG-OPE-32 : l'acteur appartient au personnel du BNIA (rattachement actif dans l'opérateur BNIA). */
    public boolean personnelBnia() {
        return bnia.isPresent();
    }

    /** RG-OPE-25, 26, 32, 35, 50 : actions réservées à un ADMIN de l'opérateur BNIA. */
    public boolean adminBnia() {
        return bnia.filter(r -> r == Role.ADMIN).isPresent();
    }

    /** Aucun rôle sur cet opérateur. */
    public boolean aucunAcces() {
        return local.isEmpty() && bnia.isEmpty();
    }
}
