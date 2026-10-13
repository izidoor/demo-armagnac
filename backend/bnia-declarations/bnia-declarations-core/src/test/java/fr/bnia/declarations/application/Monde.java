package fr.bnia.declarations.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.bnia.operateurs.application.partage.OperateursLecture;
import fr.bnia.operateurs.domain.partage.Acteur;
import fr.bnia.operateurs.domain.partage.AdresseVue;
import fr.bnia.operateurs.domain.partage.Droits;
import fr.bnia.operateurs.domain.partage.NiveauDiffusion;
import fr.bnia.operateurs.domain.partage.OperateurVue;
import fr.bnia.operateurs.domain.partage.ResultatRecherche;
import fr.bnia.operateurs.domain.partage.Role;
import fr.bnia.operateurs.domain.partage.StatutDeclarationStock;
import fr.bnia.operateurs.domain.partage.TypeOperateur;
import fr.bnia.referentiels.application.partage.ReferentielsLecture;
import fr.bnia.referentiels.domain.partage.TauxTvaVue;
import fr.bnia.referentiels.domain.partage.ValeurVue;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Le monde des tests de cas d'usage : un négociant, un producteur, le BNIA, et leurs membres ; {@link
 * OperateursLecture} simulé (Mockito), l'interface étant trop large pour un faux écrit à la main.
 */
final class Monde {

    static final long BNIA = 1;
    static final long NEGOCIANT = 7;
    static final long PRODUCTEUR = 8;

    static final Acteur AGENT_NEGOCIANT = new Acteur(10);
    static final Acteur OBSERVATEUR_NEGOCIANT = new Acteur(11);
    static final Acteur AGENT_BNIA = new Acteur(20);
    static final Acteur OBSERVATEUR_BNIA = new Acteur(21);
    static final Acteur INCONNU = new Acteur(30);

    /** Le 13 octobre 2026. */
    static final ParametresDeclarations PARAMETRES = new ParametresDeclarations("NORMAL");

    static final Clock HORLOGE = Clock.fixed(Instant.parse("2026-10-13T08:00:00Z"), ZoneId.of("Europe/Paris"));

    final OperateursLecture operateurs = mock(OperateursLecture.class);
    final FacturesEnMemoire factures = new FacturesEnMemoire();
    final DsmEnMemoire dsms = new DsmEnMemoire(factures);
    final GrilleEnMemoire grilles = new GrilleEnMemoire();
    final ReferentielsLecture referentiels = mock(ReferentielsLecture.class);
    final PointsEnMemoire points = new PointsEnMemoire();
    final JournalEnMemoire journal = new JournalEnMemoire();
    final CommentairesEnMemoire commentaires = new CommentairesEnMemoire();
    final PiecesEnMemoire pieces = new PiecesEnMemoire();
    final StockageEnMemoire stockage = new StockageEnMemoire();
    final ClasseursEnMemoire classeurs = new ClasseursEnMemoire();
    final EvenementsCaptures evenements = new EvenementsCaptures();
    private final Map<Long, OperateurVue> vues = new HashMap<>();

    Monde() {
        operateur(BNIA, TypeOperateur.BNIA, StatutDeclarationStock.NON);
        operateur(NEGOCIANT, TypeOperateur.NEGOCIANT, StatutDeclarationStock.OUI);
        operateur(PRODUCTEUR, TypeOperateur.PRODUCTEUR, StatutDeclarationStock.OUI);
        when(operateurs.operateur(anyLong())).thenAnswer(i -> Optional.ofNullable(vues.get(i.<Long>getArgument(0))));
        when(operateurs.nomUtilisateur(anyLong())).thenAnswer(i -> Optional.of("Utilisateur " + i.getArgument(0)));
        when(operateurs.operateurBnia()).thenReturn(BNIA);
        when(operateurs.identites(anyCollection())).thenAnswer(i -> i.<Collection<Long>>getArgument(0).stream()
                .map(vues::get).filter(Objects::nonNull)
                .map(v -> new ResultatRecherche(v.id(), v.code(), v.raisonSociale(), Optional.empty(), Optional.empty()))
                .toList());
        when(operateurs.droits(anyLong(), anyLong())).thenAnswer(i -> droits(i.getArgument(0), i.getArgument(1)));
        // La classe de TVA de la CVO : 20 % depuis 2014.
        when(referentiels.classeTvaParCode("NORMAL")).thenReturn(Optional.of(new ValeurVue(5, "NORMAL", "Normal", true)));
        when(referentiels.tauxTva(anyLong(), any())).thenAnswer(i -> i.<Long>getArgument(0) == 5
                ? Optional.of(new TauxTvaVue(5, new BigDecimal("20.00"), LocalDate.of(2014, 1, 1), Optional.empty()))
                : Optional.empty());
    }

    void operateur(long id, TypeOperateur type, StatutDeclarationStock statut) {
        vues.put(id, new OperateurVue(id, "N" + id, type, "Opérateur " + id, Optional.empty(),
                new AdresseVue(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty()), Optional.empty(), statut, Optional.empty(), Set.of(),
                NiveauDiffusion.REFUS, new TreeSet<>(), false, Optional.empty(), true));
    }

    private static Droits droits(long utilisateur, long operateur) {
        Optional<Role> bnia = switch ((int) utilisateur) {
            case 20 -> Optional.of(Role.AGENT);
            case 21 -> Optional.of(Role.OBSERVATEUR);
            default -> Optional.empty();
        };
        Optional<Role> local = switch ((int) utilisateur) {
            case 10 -> operateur == NEGOCIANT || operateur == PRODUCTEUR ? Optional.of(Role.AGENT) : Optional.empty();
            case 11 -> operateur == NEGOCIANT ? Optional.of(Role.OBSERVATEUR) : Optional.empty();
            default -> operateur == BNIA ? bnia : Optional.empty();
        };
        return new Droits(operateur, local, bnia);
    }

    GererPointDeDepart gererPointDeDepart() {
        return new GererPointDeDepart(points, dsms, classeurs, journal, operateurs, HORLOGE);
    }

    GererDsm gererDsm() {
        return new GererDsm(dsms, points, dsms, journal, commentaires, pieces, stockage, classeurs, grilles, factures,
                evenements, operateurs, referentiels, PARAMETRES, HORLOGE);
    }

    CommenterDsm commenterDsm() {
        return new CommenterDsm(dsms, commentaires, journal, operateurs, HORLOGE);
    }

    GererPieces gererPieces() {
        return new GererPieces(dsms, pieces, stockage, journal, operateurs, HORLOGE);
    }

    ListerDsm listerDsm() {
        return new ListerDsm(dsms, grilles, operateurs);
    }

    GererGrilleCotisation gererGrille() {
        return new GererGrilleCotisation(grilles, operateurs);
    }

    EnregistrerFactures enregistrerFactures() {
        return new EnregistrerFactures(factures);
    }

    CotisationsPubliees cotisationsPubliees() {
        return new CotisationsPubliees(dsms, grilles, factures);
    }
}
