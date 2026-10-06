package fr.bnia.referentiels.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * Contenance (C.10.9.4) : un volume en litres, unique dans le référentiel (RG-REF-10). Le CDC la
 * décrit comme un texte, mais §4.7.5 calcule un volume d'alcool pur à partir des contenances
 * (décision du 2026-10-06). Référencée par son identifiant technique ; le volume se corrige
 * (RG-REF-02, ADR-011).
 */
public final class Contenance {

    private final Long id;
    private Volume volume;
    private Libelle libelleSaisi;
    private boolean actif;

    private Contenance(Long id, Volume volume, Libelle libelleSaisi, boolean actif) {
        this.id = id;
        this.volume = Objects.requireNonNull(volume);
        this.libelleSaisi = libelleSaisi;
        this.actif = actif;
    }

    /** @param libelleSaisi facultatif : {@code null} pour le libellé dérivé du volume */
    public static Contenance creer(Volume volume, Libelle libelleSaisi) {
        return new Contenance(null, volume, libelleSaisi, true);
    }

    public static Contenance reconstituer(Long id, Volume volume, Libelle libelleSaisi, boolean actif) {
        return new Contenance(id, volume, libelleSaisi, actif);
    }

    /** Identifiant technique ; {@code null} tant que la contenance n'est pas enregistrée. */
    public Long id() {
        return id;
    }

    public Volume volume() {
        return volume;
    }

    public Optional<Libelle> libelleSaisi() {
        return Optional.ofNullable(libelleSaisi);
    }

    /** Le libellé saisi, sinon celui dérivé du volume (« 0,70 L »). */
    public String libelle() {
        return libelleSaisi != null ? libelleSaisi.valeur() : volume.libelle();
    }

    public boolean estActif() {
        return actif;
    }

    /** @param libelleSaisi {@code null} pour revenir au libellé dérivé du volume */
    public void decrire(Volume volume, Libelle libelleSaisi) {
        this.volume = Objects.requireNonNull(volume);
        this.libelleSaisi = libelleSaisi;
    }

    public void desactiver() {
        actif = false;
    }

    public void reactiver() {
        actif = true;
    }
}
