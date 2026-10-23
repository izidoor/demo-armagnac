package fr.bnia.declarations.domain;

import fr.bnia.commun.domain.Millesime;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.declarations.domain.partage.CompteDAgeDsm;
import fr.bnia.declarations.domain.partage.RetiraisonValidee;
import fr.bnia.declarations.domain.partage.RetiraisonValidee.LigneRetiree;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mouvement de contrat (spec 06 §3.7) : la copie, pour un opérateur, d'une ligne d'une retiraison validée, achat
 * pour l'acheteur, vente pour le vendeur. Il entre dans son stock estimé (RG-CA-11) et s'inscrit dans la DSM de sa
 * période (RG-CA-22), qu'il justifie (RG-CA-23). Contrat, retiraison et autre partie sont copiés, sans clé étrangère
 * (A-CA-4).
 *
 * @param identite  la ligne de DSM visée : compte {@code MB} calculé à la date d'enlèvement
 * @param colonne   RG-CA-21 : 2 ou 3 pour un achat, 20 ou 21 pour une vente
 * @param dsm       la DSM où il est inscrit ; vide tant qu'il ne l'est pas
 */
public record MouvementContrat(Long id, long operateur, SensMouvementContrat sens, LocalDate dateEnlevement,
        IdentiteLigne identite, VolumeAlcoolPur volumeAp, ColonneDsm colonne, long contrat, String numeroContrat,
        int retiraison, long autrePartie, Optional<Long> dsm) {

    public MouvementContrat {
        Objects.requireNonNull(sens, "sens");
        Objects.requireNonNull(dateEnlevement, "dateEnlevement");
        Objects.requireNonNull(identite, "identite");
        Objects.requireNonNull(volumeAp, "volumeAp");
        Objects.requireNonNull(colonne, "colonne");
        Objects.requireNonNull(numeroContrat, "numeroContrat");
        Objects.requireNonNull(dsm, "dsm");
    }

    /**
     * RG-CA-21 : par ligne de la retiraison, un achat pour l'acheteur et une vente pour le vendeur. L'achat va en
     * colonne 2 si le vendeur est un producteur, en 3 sinon ; la vente en 20 si l'acheteur est en région délimitée, en
     * 21 sinon.
     */
    public static List<MouvementContrat> de(RetiraisonValidee retiraison, TypeOperateur typeVendeur,
            boolean acheteurEnRegionDelimitee) {
        ColonneDsm achat = typeVendeur == TypeOperateur.PRODUCTEUR ? ColonneDsm.C02 : ColonneDsm.C03;
        ColonneDsm vente = acheteurEnRegionDelimitee ? ColonneDsm.C20 : ColonneDsm.C21;
        List<MouvementContrat> mouvements = new ArrayList<>();
        for (LigneRetiree ligne : retiraison.lignes()) {
            IdentiteLigne identite = identite(ligne, retiraison.dateEnlevement());
            mouvements.add(new MouvementContrat(null, retiraison.acheteur(), SensMouvementContrat.ACHAT,
                    retiraison.dateEnlevement(), identite, ligne.volumeAp(), achat, retiraison.contrat(),
                    retiraison.numeroContrat(), retiraison.retiraison(), retiraison.vendeur(), Optional.empty()));
            mouvements.add(new MouvementContrat(null, retiraison.vendeur(), SensMouvementContrat.VENTE,
                    retiraison.dateEnlevement(), identite, ligne.volumeAp(), vente, retiraison.contrat(),
                    retiraison.numeroContrat(), retiraison.retiraison(), retiraison.acheteur(), Optional.empty()));
        }
        return List.copyOf(mouvements);
    }

    private static IdentiteLigne identite(LigneRetiree ligne, LocalDate date) {
        if (ligne.cle() == CleConditionnement.MB) {
            Millesime millesime = new Millesime(ligne.millesime());
            return new IdentiteLigne(CleConditionnement.MB, millesime, null, CompteDAgeDsm.calcule(millesime, date));
        }
        return new IdentiteLigne(CleConditionnement.NB, null, Denomination.de(ligne.denomination()), ligne.compte());
    }

    /** RG-CA-22 : la ligne visée dans une DSM qui commence à {@code debut} (compte {@code MB} calculé à cette date). */
    public IdentiteLigne identiteAu(LocalDate debut) {
        return identite.cle() == CleConditionnement.MB
                ? new IdentiteLigne(CleConditionnement.MB, identite.millesime(), null,
                        CompteDAgeDsm.calcule(identite.millesime(), debut))
                : identite;
    }

    /** Pour l'écran : « Contrat CA-2026-0001, retiraison 1 ». */
    public String libelle() {
        return "Contrat " + numeroContrat + ", retiraison " + retiraison;
    }

    public MouvementContrat inscritDans(long nouvelleDsm) {
        return new MouvementContrat(id, operateur, sens, dateEnlevement, identite, volumeAp, colonne, contrat,
                numeroContrat, retiraison, autrePartie, Optional.of(nouvelleDsm));
    }
}
