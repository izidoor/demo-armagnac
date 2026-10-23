package fr.bnia.declarations.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.bnia.commun.domain.RegleEnfreinte;
import fr.bnia.commun.domain.VolumeAlcoolPur;
import fr.bnia.declarations.domain.partage.CleConditionnement;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

/** Données d'exemple des tests du domaine : le négociant de la démonstration (spec 05 §7.1). */
final class Exemples {

    static final long OPERATEUR = 7;
    static final LocalDate JUIN = LocalDate.of(2026, 6, 1);
    static final PeriodeDeclaration PERIODE_JUIN = PeriodeDeclaration.commencant(TypeDeclarant.NEGOCIANT, JUIN);
    static final Auteur BNIA = new Auteur(1, "Agnès Martin");
    static final Instant MAINTENANT = Instant.parse("2026-10-13T08:00:00Z");
    static final LocalDate AUJOURDHUI = LocalDate.of(2026, 10, 13);

    private Exemples() {
    }

    static VolumeAlcoolPur l(String litres) {
        return VolumeAlcoolPur.litres(litres);
    }

    static SaisieLigneStock stock(CleConditionnement cle, Object millesimeOuDenomination, String compte, String volume) {
        return millesimeOuDenomination instanceof Integer m
                ? new SaisieLigneStock(cle, m, null, compte, l(volume))
                : new SaisieLigneStock(cle, null, (String) millesimeOuDenomination, compte, l(volume));
    }

    /** Les six lignes de la démonstration. */
    static List<SaisieLigneStock> sixLignes() {
        return List.of(stock(CleConditionnement.NB, "XO", "12", "1500"),
                stock(CleConditionnement.NB, "VSOP", "5", "2200.50"),
                stock(CleConditionnement.NV, "VSOP", "5", "800"),
                stock(CleConditionnement.MB, 2010, null, "640.25"),
                stock(CleConditionnement.MV, 2015, "6", "300"),
                stock(CleConditionnement.NB, "Blanche", "00", "120"));
    }

    static PointDeDepartDeclaratif pointDeJuin(List<SaisieLigneStock> lignes) {
        return PointDeDepartDeclaratif.fixer(OPERATEUR, TypeDeclarant.NEGOCIANT, JUIN, lignes, BNIA, MAINTENANT);
    }

    static DeclarationStocksMouvements dsm(TypeOperateur type, PeriodeDeclaration periode, List<SaisieLigneStock> stock) {
        List<LigneStockInitial> lignes = stock.stream()
                .map(s -> new LigneStockInitial(IdentiteLigne.saisie(s.cle(), s.millesime(), s.denomination(), s.compte(),
                        periode.debut()), s.volume()))
                .toList();
        DeclarationStocksMouvements initiee = DeclarationStocksMouvements.initier(OPERATEUR, type,
                StatutDeclarationStock.OUI, periode, lignes);
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < initiee.lignes().size(); i++) {
            ids.add(100L + i);
        }
        return initiee.avecIds(50, ids);
    }

    /** La DSM de juin du négociant, initiée depuis les six lignes. */
    static DeclarationStocksMouvements dsmDeJuin() {
        return dsm(TypeOperateur.NEGOCIANT, PERIODE_JUIN, sixLignes());
    }

    /** La saisie qui reprend les lignes telles quelles, avec les volumes donnés par rang de ligne. */
    static SaisieDsm saisie(DeclarationStocksMouvements dsm, Map<Integer, Map<ColonneDsm, VolumeAlcoolPur>> volumes) {
        List<SaisieLigneDsm> lignes = new ArrayList<>();
        for (int i = 0; i < dsm.lignes().size(); i++) {
            lignes.add(telleQuelle(dsm.lignes().get(i), volumes.getOrDefault(i, Map.of())));
        }
        return new SaisieDsm(dsm.dateDia(), lignes);
    }

    static SaisieLigneDsm telleQuelle(LigneDsm ligne, Map<ColonneDsm, VolumeAlcoolPur> volumes) {
        IdentiteLigne id = ligne.identite();
        EnumMap<ColonneDsm, VolumeAlcoolPur> mouvements = new EnumMap<>(ColonneDsm.class);
        ligne.volumes().forEach((c, v) -> {
            if (c != ColonneDsm.C01) {
                mouvements.put(c, v);
            }
        });
        mouvements.putAll(volumes);
        return new SaisieLigneDsm(ligne.id(), id.cle(), id.millesime() == null ? null : id.millesime().annee(),
                id.denomination() == null ? null : id.denomination().texte(), id.compte().toString(), mouvements);
    }

    static void refuse(String regle, ThrowingCallable appel) {
        assertThatThrownBy(appel).isInstanceOf(RegleEnfreinte.class)
                .extracting(e -> ((RegleEnfreinte) e).regle().orElse(null)).isEqualTo(regle);
    }
}
