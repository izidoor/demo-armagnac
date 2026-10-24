package fr.bnia.contratsarmagnac.application;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.bnia.commun.domain.Appellation;
import fr.bnia.contratsarmagnac.application.SaisieContenu.SaisieLigne;
import fr.bnia.contratsarmagnac.domain.Solde;
import fr.bnia.contratsarmagnac.domain.StocksExemples;
import fr.bnia.declarations.application.partage.DeclarationsLecture;
import fr.bnia.declarations.domain.partage.StockEstimeVue;
import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.AdresseVue;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.IdentiteLegaleVue;
import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.PersonneVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Le monde des tests de cas d'usage : le négociant d'Henri, le producteur de Jeanne, un opérateur inactif, le BNIA ;
 * {@link OperateursLecture} et {@link DeclarationsLecture} simulés (Mockito).
 */
final class Monde {

    static final long BNIA = 1;
    static final long NEGOCIANT = 7;
    static final long PRODUCTEUR = 8;
    static final long INACTIF = 9;
    static final long TIERS = 10;

    static final Acteur HENRI = new Acteur(70);
    static final Acteur JEANNE = new Acteur(80);
    static final Acteur OBSERVATEUR_PRODUCTEUR = new Acteur(81);
    static final Acteur AGENT_BNIA = new Acteur(20);
    static final Acteur INCONNU = new Acteur(30);

    /** Le 23 octobre 2026. */
    static final Clock HORLOGE = Clock.fixed(Instant.parse("2026-10-23T08:00:00Z"), ZoneId.of("Europe/Paris"));

    final OperateursLecture operateurs = mock(OperateursLecture.class);
    final DeclarationsLecture declarations = mock(DeclarationsLecture.class);
    final ContratsEnMemoire contrats = new ContratsEnMemoire();
    final JournalEnMemoire journal = new JournalEnMemoire();
    final CourrielsCaptures courriels = new CourrielsCaptures();
    final NotificationsCaptures notifications = new NotificationsCaptures();
    final List<Object> evenements = new java.util.ArrayList<>();
    final List<EditionsContrats.EditionContrat> editions = new java.util.ArrayList<>();
    private int rang;
    private int numero;

    private final Map<Long, OperateurVue> vues = Map.of(
            BNIA, vue(BNIA, TypeOperateur.BNIA, "BNIA", true),
            NEGOCIANT, vue(NEGOCIANT, TypeOperateur.NEGOCIANT, "Armagnacs de Gascogne", true),
            PRODUCTEUR, vue(PRODUCTEUR, TypeOperateur.PRODUCTEUR, "Maison Durand", true),
            INACTIF, vue(INACTIF, TypeOperateur.PRODUCTEUR, "Domaine fermé", false),
            TIERS, vue(TIERS, TypeOperateur.NEGOCIANT, "Négoce des Landes", true));

    Monde() {
        when(operateurs.operateurBnia()).thenReturn(BNIA);
        when(operateurs.operateur(anyLong())).thenAnswer(i -> Optional.ofNullable(vues.get(i.<Long>getArgument(0))));
        when(operateurs.identites(anyCollection())).thenAnswer(i -> i.<Collection<Long>>getArgument(0).stream()
                .map(vues::get)
                .map(v -> new ResultatRecherche(v.id(), v.code(), v.raisonSociale(), Optional.empty(), Optional.empty()))
                .toList());
        when(operateurs.droits(anyLong(), anyLong())).thenAnswer(i -> droits(i.getArgument(0), i.getArgument(1)));
        when(operateurs.nomUtilisateur(anyLong())).thenAnswer(i -> Optional.of(switch (i.<Long>getArgument(0)
                .intValue()) {
            case 70 -> "Henri Castex";
            case 80 -> "Jeanne Durand";
            default -> "Utilisateur " + i.getArgument(0);
        }));
        when(operateurs.personnes(NEGOCIANT)).thenReturn(List.of(
                personne(NEGOCIANT, 70L, "Henri Castex", "admin@negociant.fr", Role.ADMIN),
                personne(NEGOCIANT, 71L, "Paul Lecteur", "lecteur@negociant.fr", Role.OBSERVATEUR),
                personne(NEGOCIANT, null, "Contact sans compte", "contact@negociant.fr", null)));
        when(operateurs.personnes(PRODUCTEUR)).thenReturn(List.of(
                personne(PRODUCTEUR, 80L, "Jeanne Durand", "admin@producteur.fr", Role.ADMIN)));
        when(operateurs.identiteLegale(anyLong())).thenAnswer(i -> Optional.of(new IdentiteLegaleVue(
                i.getArgument(0), Optional.of("77699995500029"), Optional.of("3205600001"),
                Optional.of("05 62 28 00 00"))));
        when(declarations.stockEstime(anyLong()))
                .thenAnswer(i -> new StockEstimeVue(i.getArgument(0), Optional.empty(), List.of()));
        when(declarations.stockEstime(PRODUCTEUR)).thenReturn(StocksExemples.durand());
    }

    private static Droits droits(long utilisateur, long operateur) {
        Optional<Role> bnia = utilisateur == 20 ? Optional.of(Role.ADMIN) : Optional.empty();
        Optional<Role> local = switch ((int) utilisateur) {
            case 70 -> operateur == NEGOCIANT ? Optional.of(Role.AGENT) : Optional.empty();
            case 80 -> operateur == PRODUCTEUR ? Optional.of(Role.ADMIN) : Optional.empty();
            case 81 -> operateur == PRODUCTEUR ? Optional.of(Role.OBSERVATEUR) : Optional.empty();
            case 20 -> operateur == BNIA ? Optional.of(Role.ADMIN) : Optional.empty();
            default -> Optional.empty();
        };
        return new Droits(operateur, local, bnia);
    }

    private static OperateurVue vue(long id, TypeOperateur type, String raisonSociale, boolean actif) {
        return new OperateurVue(id, "C" + id, type, raisonSociale, Optional.empty(),
                new AdresseVue(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.of("Condom"), Optional.empty()),
                Optional.empty(), StatutDeclarationStock.OUI, Optional.empty(), Set.of(Appellation.BAS_ARMAGNAC),
                NiveauDiffusion.REFUS, new TreeSet<>(), false, Optional.empty(), actif);
    }

    private PersonneVue personne(long operateur, Long utilisateur, String nom, String email, Role role) {
        return new PersonneVue(operateur, ++rang, nom, Optional.empty(), Optional.of(email), Set.of(),
                Optional.ofNullable(role), Optional.ofNullable(utilisateur));
    }

    NegocierContrats negocier() {
        return new NegocierContrats(contrats, annee -> ++numero, journal, courriels, notifications, operateurs,
                declarations, new ParametresContrats("https://bnia.example"), HORLOGE);
    }

    ExecuterContrats executer() {
        return new ExecuterContrats(contrats, journal, courriels, notifications, evenements::add, operateurs,
                declarations, new ParametresContrats("https://bnia.example"), HORLOGE);
    }

    EditerContrats editer() {
        return new EditerContrats(contrats, journal, edition -> {
            editions.add(edition);
            return new byte[] {'%', 'P', 'D', 'F'};
        }, operateurs, HORLOGE);
    }

    ConsulterContrats consulter() {
        return new ConsulterContrats(contrats, contrats, journal, operateurs, declarations, HORLOGE);
    }

    static SaisieContenu millesime2015(String volume, String prix) {
        return new SaisieContenu(List.of(new SaisieLigne(Appellation.BAS_ARMAGNAC, 2015, null, null,
                new BigDecimal(volume), new BigDecimal("47"), new BigDecimal(prix))), null, null, null,
                Solde.COMPTANT, null);
    }
}
