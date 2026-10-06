package fr.bnia.referentiels.domain;

import java.util.Objects;
import java.util.Optional;

/**
 * Contenance (C.10.9.4) : un volume en litres, qui l'identifie et ne change jamais (RG-REF-02,
 * RG-REF-10). Le CDC la décrit comme un texte, mais §4.7.5 calcule un volume d'alcool pur à partir
 * des contenances (décision du 2026-10-06).
 */
public final class Contenance {

    private final Volume volume;
    private Libelle libelleSaisi;
    private boolean actif;

    private Contenance(Volume volume, Libelle libelleSaisi, boolean actif) {
        this.volume = Objects.requireNonNull(volume);
        this.libelleSaisi = libelleSaisi;
        this.actif = actif;
    }

    /** @param libelleSaisi facultatif : {@code null} pour le libellé dérivé du volume */
    public static Contenance creer(Volume volume, Libelle libelleSaisi) {
        return new Contenance(volume, libelleSaisi, true);
    }

    public static Contenance reconstituer(Volume volume, Libelle libelleSaisi, boolean actif) {
        return new Contenance(volume, libelleSaisi, actif);
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

    public void renommer(Libelle libelleSaisi) {
        this.libelleSaisi = libelleSaisi;
    }

    public void desactiver() {
        actif = false;
    }

    public void reactiver() {
        actif = true;
    }
}
