package fr.bnia.operateurs.application;

import fr.bnia.commun.domain.ElementUtilise;
import fr.bnia.operateurs.domain.CodeOperateur;
import fr.bnia.operateurs.domain.Cvi;
import fr.bnia.operateurs.domain.NumeroAccise;
import fr.bnia.operateurs.domain.Operateur;
import fr.bnia.operateurs.domain.SerieCode;
import fr.bnia.operateurs.domain.Siret;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;

/**
 * Opérateurs en mémoire, pour les tests de cas d'usage. Attribue les identifiants à l'insertion et
 * incrémente la version à chaque mise à jour, comme la base ; {@link #utilises} simule les clés
 * étrangères des autres contextes (RG-OPE-25) ; {@link #verrous} compte les verrous pris (RG-OPE-35).
 */
class OperateursEnMemoire implements OperateurRepository, ListingOperateurs, NumerotationLecture {

    final Map<Long, Operateur> operateurs = new LinkedHashMap<>();
    final Set<Long> utilises = new HashSet<>();
    final List<Long> verrous = new ArrayList<>();
    /** Appelé à la suppression d'un opérateur : la cascade de la base sur ses personnes. */
    java.util.function.LongConsumer cascade = id -> { };
    private long prochainId = 1;

    @Override
    public Optional<Operateur> parId(long id) {
        return Optional.ofNullable(operateurs.get(id));
    }

    @Override
    public Operateur inserer(Operateur o) {
        Operateur enregistre = copie(o, prochainId++, 0);
        operateurs.put(enregistre.id(), enregistre);
        return enregistre;
    }

    @Override
    public void mettreAJour(Operateur o) {
        operateurs.put(o.id(), copie(o, o.id(), o.version() + 1));
    }

    @Override
    public void supprimer(long id) {
        if (utilises.contains(id)) {
            throw new ElementUtilise("RG-OPE-25", "Opérateur utilisé.");
        }
        operateurs.remove(id);
        cascade.accept(id);
    }

    @Override
    public boolean existeCode(CodeOperateur code, Long sauf) {
        return autres(sauf).stream().anyMatch(o -> o.code().valeur().equalsIgnoreCase(code.valeur()));
    }

    @Override
    public boolean existeIdentifiantOdg(String identifiantOdg, Long sauf) {
        return autres(sauf).stream().anyMatch(o -> identifiantOdg.equalsIgnoreCase(o.identite().identifiantOdg()));
    }

    @Override
    public void verrouiller(long id) {
        verrous.add(id);
    }

    @Override
    public boolean estActif(long id) {
        return parId(id).map(Operateur::estActif).orElse(false);
    }

    @Override
    public long idBnia() {
        return operateurs.values().stream().filter(o -> o.type() == TypeOperateur.BNIA).findFirst().orElseThrow().id();
    }

    @Override
    public List<OperateurLigne> lister(FiltreOperateurs filtre) {
        return operateurs.values().stream().filter(o -> !o.estBnia())
                .map(o -> new OperateurLigne(o.id(), o.type(), o.code().valeur(), o.identite().raisonSociale(),
                        o.identite().nomCommercial(), o.identite().adresse().codePostal(), o.identite().adresse().ville(),
                        o.identite().telephone1(), null, o.estActif(), o.manquePourFacturer(true).isEmpty(),
                        o.manquePourCertifier().isEmpty()))
                .toList();
    }

    @Override
    public List<OperateurResume> duGroupe(String groupeNormalise, long sauf) {
        return autres(sauf).stream().filter(o -> groupeNormalise.equals(o.identite().groupeNormalise()))
                .map(OperateursEnMemoire::resume).toList();
    }

    @Override
    public List<String> groupes(String recherche) {
        return operateurs.values().stream().map(o -> o.identite().groupe()).filter(Objects::nonNull).distinct().toList();
    }

    @Override
    public List<Doublon> doublons(String raisonSocialeNormalisee, Cvi cvi, Siret siret, NumeroAccise accise, Long sauf) {
        List<Doublon> doublons = new ArrayList<>();
        ajouter(doublons, sauf, raisonSocialeNormalisee, o -> o.identite().raisonSocialeNormalisee(),
                Doublon.Champ.RAISON_SOCIALE, "RG-OPE-08");
        ajouter(doublons, sauf, cvi, o -> o.identite().cvi(), Doublon.Champ.CVI, "RG-OPE-06");
        ajouter(doublons, sauf, siret, o -> o.identite().siret(), Doublon.Champ.SIRET, "RG-OPE-06");
        ajouter(doublons, sauf, accise, o -> o.identite().accise(), Doublon.Champ.ACCISE, "RG-OPE-06");
        return doublons;
    }

    @Override
    public OptionalInt plusGrandNumero(SerieCode serie) {
        return operateurs.values().stream()
                .map(o -> o.code().valeur().toUpperCase(Locale.ROOT))
                .filter(code -> code.matches(serie.motif()))
                .mapToInt(code -> Integer.parseInt(code.substring(serie.prefixe().length())))
                .max();
    }

    private static Operateur copie(Operateur o, long id, long version) {
        return Operateur.reconstituer(id, o.type(), o.code(), o.identite(), o.declaratif(), o.production(),
                o.diffusion(), o.accesStatistiques(), o.estActif(), o.contactPrincipal().orElse(null),
                o.destinataireFactures().orElse(null), version);
    }

    private <V> void ajouter(List<Doublon> doublons, Long sauf, V valeur, Function<Operateur, V> lire,
            Doublon.Champ champ, String regle) {
        if (valeur != null) {
            autres(sauf).stream().filter(o -> Objects.equals(valeur, lire.apply(o)))
                    .forEach(o -> doublons.add(new Doublon(champ, regle, resume(o))));
        }
    }

    private List<Operateur> autres(Long sauf) {
        return operateurs.values().stream().filter(o -> !o.id().equals(sauf)).toList();
    }

    private static OperateurResume resume(Operateur o) {
        return new OperateurResume(o.id(), o.code().valeur(), o.identite().raisonSociale(),
                o.identite().adresse().ville(), o.estActif());
    }
}
