# 05 : declarations

> Contexte `declarations` (étape 5 de `docs/contextes.md`). Statut : **terminée** (2026-10-13).
> Références : `p.N` = page du CDC v1.1, `AGI p.N` = page de la spec de reprise AGI.
> `H-DEC-n` : hypothèse retenue, révisable. `Q-DEC-n` : question ouverte, suivie dans cette spec (§10).
> Précédée de l'analyse du contexte ([analyse.md](analyse.md), terminée le 2026-10-13, citée « analyse »),
> dont les conclusions (cadrage, cas d'usage `UC-DEC-n`, objets, flux, amendements au CDC) sont acquises.

## Sommaire

- [1. Objet](#1-objet)
- [2. Périmètre](#2-périmètre)
  - [2.1 Dans le contexte](#21-dans-le-contexte)
  - [2.2 Hors du contexte](#22-hors-du-contexte)
- [3. Modèle](#3-modèle)
  - [3.1 Les 26 colonnes du format unique](#31-les-26-colonnes-du-format-unique)
  - [3.2 Déclaration de stocks et mouvements (agrégat `DeclarationStocksMouvements`, lot 1)](#32-déclaration-de-stocks-et-mouvements-agrégat-declarationstocksmouvements-lot-1)
  - [3.3 Point de départ déclaratif (agrégat `PointDeDepartDeclaratif`, lot 1)](#33-point-de-départ-déclaratif-agrégat-pointdedepartdeclaratif-lot-1)
  - [3.4 Journal (`JournalDeclarations`, hors agrégat, en ajout seul, lot 1)](#34-journal-journaldeclarations-hors-agrégat-en-ajout-seul-lot-1)
  - [3.5 Grille de cotisation (agrégat `GrilleCotisation`, lot 4)](#35-grille-de-cotisation-agrégat-grillecotisation-lot-4)
  - [3.6 Politiques et modèles de lecture (sans état propre)](#36-politiques-et-modèles-de-lecture-sans-état-propre)
  - [3.7 Droits : le module « Déclarations » (révision de la spec 02 §3.2.7)](#37-droits--le-module--déclarations--révision-de-la-spec-02-327)
- [4. Règles de gestion](#4-règles-de-gestion)
- [5. Cas d'usage](#5-cas-dusage)
  - [5.1 Écriture](#51-écriture)
  - [5.2 Lecture](#52-lecture)
- [6. Écrans](#6-écrans)
  - [6.1 Back-office (opérateur actif : BNIA)](#61-back-office-opérateur-actif--bnia)
  - [6.2 Portail (opérateur actif : producteur ou négociant)](#62-portail-opérateur-actif--producteur-ou-négociant)
  - [6.3 Écran d'une DSM (commun)](#63-écran-dune-dsm-commun)
- [7. Données](#7-données)
  - [7.1 Démonstration (profil `demo`)](#71-démonstration-profil-demo)
  - [7.2 Reprise AGI](#72-reprise-agi)
- [8. Décisions d'architecture](#8-décisions-darchitecture)
- [9. Hypothèses](#9-hypothèses)
- [10. Questions ouvertes](#10-questions-ouvertes)
- [11. Tests attendus](#11-tests-attendus)
- [12. Lots d'implémentation](#12-lots-dimplémentation)
- [Journal](#journal)

## 1. Objet

Les douanes ont confié au BNIA le suivi des stocks d'Armagnac de chaque opérateur, et peuvent lui en
demander à tout moment un état des lieux (dire de l'utilisateur du 2026-10-12, analyse §1). Le contexte
porte ce suivi :

- la **déclaration de stocks et mouvements** (DSM) : l'opérateur l'initie pour la période qui suit sa
  dernière DSM validée (ou son point de départ fixé par le BNIA), la remplit au portail par saisie ou
  import Excel, y joint justificatifs et, s'il est producteur, ses DRM, puis l'envoie ; le BNIA
  l'instruit (commentaires, demande de compléments) et la valide, ce qui la fige (analyse §2) ;
- la **cotisation** de la DSM (`CVO`) : assiette selon le type de déclarant, taux daté administré par
  le BNIA, détail transmis à `facturation`, qui émet la facture ;
- les **stocks** : le stock déclaré (fin de la dernière DSM validée), livré maintenant ; le stock
  estimé, les engagements, le stock disponible et le bilan prévisionnel d'un contrat, spécifiés ici
  comme contrat d'interface et livrés avec `contratsarmagnac` et `distillation` (décision du
  2026-10-13).

Le contexte ne gère ni le contrat et ses retiraisons, ni la fin de travaux, ni la facture et sa TVA, ni
l'envoi des notifications, ni les statistiques. Sa valeur tient dans trois choses : des déclarations
**cohérentes** (contrôles bloquants à l'envoi, pièces exigées à la validation), un **workflow
d'instruction** lisible des deux côtés, et un **état des stocks** opposable aux douanes.

Sources : §4.4 (p.22-29) · §5.4 (p.64-66) · C.3 (p.107-112) · annexe B (p.98-99) · annexe E (p.140) et
matrices `docs/source/fichiers-example/20261005_DI_ModeleDSM-*.xlsx` · §4.11.2 (p.52) · C.10.3 (p.128) ·
§4.9.3 (p.44-45) · facture `FACTURE-CVO-Armagnac_2026000700.pdf` · AGI 2.2 (p.8-9).
Usages relevés ailleurs : §4.1 (tableau de bord BNIA, p.12) · §4.2.5.1 (droits du module DSM, p.15) ·
§4.3.2 et §4.3.3 (périodicité, statut de déclaration, diffusion des millésimes, fiche opérateur, p.18-20) ·
§4.5.1 (contrat achat Armagnac, p.30) · §4.7 (borne `20+` des comptes d'âge, p.39-40) · §4.9.3 (assiette
par colonne, p.44-45) · §4.10.3 à §4.10.6 (statistiques sur les DSM, p.48-50) · §5.2 (tableau de bord
portail, p.61) · §5.12 (notifications et relances, p.77-78) · §7 (reprise des lignes de DSM, p.85) · C.1.1 (périodicité,
statut de déclaration, p.101-102) · C.8.3.1 (DSM liée à la facture, p.124) · C.14 (notifications, p.136).

## 2. Périmètre

### 2.1 Dans le contexte

| Famille | Élément | Code (ADR-001) | Source |
|---|---|---|---|
| Déclaration | DSM d'un opérateur pour une période, au format unique (5 onglets, 26 colonnes) | `DeclarationStocksMouvements` | §4.4.3 p.23-25, C.3.1-3.2 p.107-110 |
| Déclaration | Ligne de DSM (dénomination ou millésime, clé de conditionnement, compte d'âge, volumes en LAP) | `LigneDsm` | §4.4.3 p.23, C.3.2.1 p.108 |
| Déclaration | Période déduite de la dernière DSM validée ou du point de départ : année du 1er avril au 31 mars (producteur), mois civil (négociant) | `PeriodeDeclaration` | §4.4.2 p.22, analyse §3, H-DEC-5 |
| Déclaration | États d'instruction (`BROUILLON`, `ENVOYEE`, `EN_INSTRUCTION`, `COMPLEMENTS_DEMANDES`, `COMPLEMENTS_RECUS`, `VALIDEE`) | `EtatDsm` | analyse §2 et §3 |
| Déclaration | Point de départ déclaratif (début de la première période, stock initial), fixé par le BNIA | `PointDeDepartDeclaratif` | analyse UC-DEC-3 |
| Saisie | Saisie au portail, colonnes grisées pour un producteur | — | §4.4.3.2 p.24-25, §5.4.3 p.64 |
| Saisie | Import du modèle Excel du BNIA | — | §4.4.10 p.28, §5.4.4 p.64 |
| Saisie | Édition Excel et PDF au format unique (décision du 2026-10-13) | — | §4.4.4 p.25, §5.4.1 p.64 |
| Contrôles | Contrôles de cohérence (calculs, reprise, unicité, continuité), bloquants à l'envoi ; pièces, bloquantes à la validation | — | §4.4.7 p.26-27, §5.4.7 p.65-66, analyse §2 |
| Pièces | Justificatif : fichier déposé, type de pièce, référence DAE/DAI facultative, rattaché à la DSM ou à une ligne (décision du 2026-10-13) | `Justificatif` | §4.4.5 p.25-26, C.3.3.1 p.111 |
| Pièces | DRM d'un producteur, une par mois de la période ; supprimée puis redéposée tant que la DSM est modifiable (décision du 2026-10-13) | `Drm` | §4.4.6 p.26, §5.4.6 p.65, C.3.4.1 p.111 |
| Instruction | Commentaire d'instruction (DSM ou ligne), demande de compléments, validation | `Commentaire` | §4.4.5 p.26, C.3.3.1 p.111, analyse §2 |
| Instruction | Journal des actions de la DSM, en ajout seul, sans valeurs avant et après (décision du 2026-10-13) | `JournalDeclarations` | §4.4.10 p.29 |
| Suivi | DSM en retard ou absentes, détectées par un traitement planifié ; événements pour `notifications` | — | §4.4.8 p.27, analyse UC-DEC-11 |
| Cotisation | Grille des taux `CVO` datés, administrée par le BNIA dans le menu Paramétrage (décision du 2026-10-13) | `GrilleCotisation`, `TauxCotisation` | §4.11.2 p.52, C.10.3 p.128 |
| Cotisation | Calcul : assiette selon le type de déclarant, taux en vigueur en fin de période, détail par ligne de facture | `PolitiqueCotisation` | §4.4.9 p.27-28, §4.9.3 p.44-45, analyse §3 |
| Stocks | Stock déclaré : fin de la dernière DSM validée, par ligne, livré maintenant | — | analyse UC-DEC-1, §4.10.5 p.49 |
| Stocks | Stock estimé, engagements, stock disponible, bilan prévisionnel : contrat d'interface, livré avec 6 et 9 (décision du 2026-10-13) | — | analyse UC-DEC-2 et §4.3 |
| Lecture publiée | Cotisation d'une DSM (`facturation`), stock déclaré et dernière DSM d'un opérateur (fiche opérateur) ; étendue par les specs consommatrices (décision du 2026-10-13) | `DeclarationsLecture` | analyse §4.2, ADR-012, A-DEC-1 |
| Lecture publiée | Types des événements émis et reçus (`contratsarmagnac`, `distillation`, `facturation`) | `DsmEnvoyee`, `ContratValide`, `FactureEmise`… | analyse §4.3, ADR-013, A-DEC-1 |
| Projection | Factures et avoirs des DSM (numéro, date, montants), reçus de `facturation` par événements (A-DEC-4) | `FactureDsm` | analyse §4.2, A-DEC-4 |
| Droits | Module « Déclarations » du tableau rôles × modules | `Module.DECLARATIONS` | ADR-016, analyse §2 |

### 2.2 Hors du contexte

| Élément décrit dans le CDC | Contexte retenu | Motif |
|---|---|---|
| Génération de la DSM par le BNIA (§4.4.2 p.22) ; saisie et correction par le BNIA (§5.4.3 p.64, C.3.3 p.111) ; validation par l'opérateur (§5.4.8 p.66) | — (amendement) | L'opérateur initie, remplit et envoie ; le BNIA instruit et valide (analyse §2, décision du 2026-10-13) |
| Signature électronique de la DSM, PDF signé archivé (§4.4.4 p.25, §5.4.8-5.4.9 p.66) | hors périmètre du prototype | Fournisseur, coût et signataire inconnus (décision du 2026-10-12) |
| Import d'un fichier issu d'un logiciel opérateur, format `XML`/`JSON` (§4.4.10 p.28, §5.4.4 p.64) | différé | §9.4 ; seul le modèle Excel du BNIA est importé |
| Contrat achat Armagnac, retiraisons, refus d'un contrat non couvert | `contratsarmagnac` | Il lit le bilan prévisionnel et informe `declarations` par événements (analyse §4.3) |
| Rattachement d'une ligne d'achat ou de vente aux retiraisons de la période (§4.4.7 p.27) ; rattachement d'un justificatif à un contrat (C.3.3.1 p.111) | `declarations`, livré avec `contratsarmagnac` | Dépend des retiraisons ; la spec 06 révisera cette spec (décisions du 2026-10-13) |
| Lien d'un justificatif vers une facture ou un avoir interne (C.3.3.1 p.111) | abandonné | Le PDF déposé suffit ; pas de dépendance vers `facturation` (décision du 2026-10-13) |
| Pointage des fabrications Floc avec les données ODG (§4.4.5 p.25) | en attente du BNIA | Aucune source ODG pour le Floc (Q-DEC-1, décision du 2026-10-13) |
| Remplacement d'une DRM avec trace (§4.4.6 p.26) | remplacé | Suppression et nouveau dépôt tant que la DSM est modifiable ; la trace est au journal (décision du 2026-10-13) |
| Fin de travaux de distillation, volume produit pour le propriétaire, taux de fin de travaux (§4.11.2 p.52) | `distillation` | Informe `declarations` par événement (analyse §4.3) ; le taux fin de travaux lui appartient (`contextes.md`, 2026-10-12) |
| Création de la facture, unicité par DSM, TVA, date et pièce de facturation (§4.4.9 p.28, C.3.1.1 p.108, §4.9.3 p.44-45) | `facturation` | UC-DEC-10 porté et livré par `facturation` (analyse §4.2) |
| Envoi des relances et des notifications ; alertes et relances (C.3.5.1 p.111-112) | `notifications` | `declarations` détecte et publie, `notifications` envoie (analyse UC-DEC-11) |
| Statistiques sur les DSM et état des stocks (§4.10.3 à §4.10.6 p.48-50) | `statistiques` | Lectures ajoutées à la partie publiée par la spec `statistiques` (décision du 2026-10-13) |
| Indicateurs DSM des tableaux de bord (§4.1 p.12, §5.2 p.61), dernière DSM de la fiche opérateur (§4.3.3 p.20) | compositions de lecture | `contextes.md` §3 ; `declarations` fournit le listing filtré et la dernière DSM |
| Type d'opérateur, statut de déclaration de stock (C.1.1 p.101-102) | `operateurs` | Lus dans sa partie publiée (ADR-012) |
| Périodicités trimestrielle et semestrielle, périodicité réglée par opérateur et changement de périodicité (§4.4.10 p.28, C.1.1 p.101-102) | hors périmètre du prototype | Trop d'inconnues (alignement, période de transition, DRM attendues). La périodicité découle du type : annuelle pour le producteur, mensuelle pour le négociant ; la périodicité de la fiche opérateur n'est pas lue (décision du 2026-10-13) |
| Reprise des DSM d'InteGraal (AGI 2.2 p.8-9, §7) | hors périmètre du prototype | Reprise des données hors périmètre |

## 3. Modèle

Le modèle part des objets et invariants de l'analyse (§3), précisés par les hypothèses H-DEC-1 à 23.
Trois agrégats : la DSM (§3.2), le point de départ (§3.3), la grille de cotisation (§3.5). Identifiants
`bigint` ; l'opérateur est référencé par son identifiant, avec clé étrangère (ADR-011) ; l'auteur d'une
action par l'identifiant de l'utilisateur, sans clé étrangère, et une copie de son nom (ADR-011 amendé).
`Millesime`, `CompteDAge` et `VolumeAlcoolPur` viennent de `commun.domain` (ADR-009, A-DEC-8). Chaque objet porte le lot qui le livre
(§12).

### 3.1 Les 26 colonnes du format unique

Référentiel fixe, défini par l'application (`ColonneDsm`). Il porte, pour chaque colonne, tout ce que les
règles en tirent : sens dans le calcul du stock, grisage producteur, assiette, ligne de cotisation,
justificatif attendu.

| N° | Onglet | Libellé | Sens | Grisée producteur | Assiette | Ligne de cotisation | Justificatif (type de mouvement) |
|---|---|---|---|---|---|---|---|
| 1 | Récapitulatif | stock début de période | stock de début, calculé | non | | | |
| 2 | Récapitulatif | entrées achats à la propriété | entrée | **oui** | | | achat |
| 3 | Récapitulatif | entrées achats négoce | entrée | **oui** | | | achat |
| 4 | Mouvements en entrée | distillation propre | entrée | non | | | |
| 5 | Mouvements en entrée | passage sous bois / sous-verre | entrée | **oui** | | | |
| 6 | Mouvements en entrée | assemblages / coupes | entrée | non | | | |
| 7 | Mouvements en entrée | transferts chai Armagnac en région délimitée | entrée | non | | | |
| 8 | Mouvements en entrée | transferts chai Armagnac hors région délimitée | entrée | non | | | |
| 9 | Mouvements en entrée | réintégrations | entrée | **oui** | | | réintégration |
| 10 | Mouvements en sortie | pertes sur inventaire | sortie | non | | | |
| 11 | Mouvements en sortie | passage sous bois / sous-verre | sortie | **oui** | | | |
| 12 | Mouvements en sortie | assemblages / coupes | sortie | non | | | |
| 13 | Mouvements en sortie | transferts chai Armagnac en région délimitée | sortie | non | | | |
| 14 | Mouvements en sortie | transferts chai Armagnac hors région délimitée | sortie | non | oui | Sorties France | |
| 15 | Mouvements en sortie | fabrications Floc de Gascogne | sortie | non | | | (Q-DEC-1 : rien de bloquant) |
| 16 | Mouvements en sortie | utilisation de l'Armagnac pour autres fabrications que l'AOC | sortie | non | oui | EDV autre produit, marché intérieur | autres fabrications |
| 17 | Ventes France | marché intérieur hors chai en bouteilles | sortie | non | oui | Sorties France | |
| 18 | Ventes France | marché intérieur hors chai en vrac destiné à la mise en bouteille | sortie | non | oui | Sorties France | |
| 19 | Ventes France | marché intérieur hors chai en brandies | sortie | **oui** | oui | Sorties France | |
| 20 | Ventes France | négoce chai Armagnac en région délimitée | sortie | non | | | vente négoce |
| 21 | Ventes France | négoce chai Armagnac hors région délimitée | sortie | non | oui | Sorties France | vente négoce |
| 22 | Ventes à l'exportation | bouteilles | sortie | non | oui | Sorties Export | vente export |
| 23 | Ventes à l'exportation | vrac destiné à la mise en bouteille | sortie | non | oui | Sorties Export | vente export |
| 24 | Ventes à l'exportation | brandies | sortie | **oui** | oui | Sorties Export | vente export |
| 25 | Ventes à l'exportation | vins vinés et fabrications autres | sortie | **oui** | oui | EDV autre produit, export | vente export |
| 26 | Récapitulatif | stock fin de période | stock de fin, calculé | non | | | |

Sources : libellés et onglets §4.4.3.1 p.23-24 ; grisage §4.4.3.2 p.25 ; assiette H-DEC-1 et H-DEC-2 ;
lignes de cotisation H-DEC-3 ; justificatifs §4.4.5 p.25-26 et H-DEC-13. Colonnes croisées (H-DEC-7) :
6 avec 12, 5 avec 11.

### 3.2 Déclaration de stocks et mouvements (agrégat `DeclarationStocksMouvements`, lot 1)

| Champ | Type | Règle | Source |
|---|---|---|---|
| opérateur | référence `Operateur` (`operateurs`) | Obligatoire ; producteur ou négociant au statut `OUI` à l'initiation (RG-DEC-02) | C.3.1.1 p.107 |
| type de déclarant | `PRODUCTEUR`, `NEGOCIANT` | Lu sur l'opérateur à l'initiation, figé (RG-DEC-02) | C.3.1.1 p.107, analyse §3 |
| période | `PeriodeDeclaration` : début, fin (dates) | Calculée à l'initiation (RG-DEC-03, 04) | §4.4.2 p.22, H-DEC-5 |
| état | `EtatDsm` : `BROUILLON`, `ENVOYEE`, `EN_INSTRUCTION`, `COMPLEMENTS_DEMANDES`, `COMPLEMENTS_RECUS`, `VALIDEE` ; libellé selon le côté : `ENVOYEE` « Envoyée » pour l'opérateur, « À prendre en charge » pour le BNIA ; `COMPLEMENTS_RECUS` « Compléments envoyés », « Compléments reçus » | `BROUILLON` à l'initiation ; transitions RG-DEC-20 | analyse §2 |
| date de la DIA | date | Facultative (RG-DEC-19) | modèles Excel, H-DEC-12 |
| lignes | liste de `LigneDsm` (§3.2.1) | Uniques (RG-DEC-11) | §4.4.3 p.23 |
| justificatifs | liste de `Justificatif` (§3.2.2) | Lot 3 (RG-DEC-26, 28) | §4.4.5 p.25-26, C.3.3.1 p.111 |
| DRM | liste de `Drm` (§3.2.3) | Producteur seulement ; lot 3 (RG-DEC-27, 28) | §4.4.6 p.26, C.3.4.1 p.111 |
| commentaires | liste de `Commentaire` (§3.2.4) | Lot 2 (RG-DEC-24, 25) | analyse §3, C.3.3.1 p.111 |
| envoyée le | date-heure | Dernier envoi (RG-DEC-20) ; sert au listing et au retard | analyse §2 |
| validée le, validée par | date-heure ; identifiant et copie du nom | À la validation (RG-DEC-20) ; daté du stock déclaré | analyse §2 |
| version | entier | Verrou optimiste (`ModificationConcurrente`) | `commun.domain` |

La DSM ne porte **ni montant de cotisation ni état de facturation** (analyse §3) : la cotisation se
calcule (§3.6), « à facturer » et « facturée » se lisent dans la projection locale des factures, alimentée
par les événements de `facturation` (§5.2, A-DEC-4).

**Écarts au CDC.** Plus de « mode d'alimentation », de « date de saisie », « saisi par », « dernière
modification », « date d'édition » : le journal les porte (§3.4, H-DEC-12). Plus d'« observation
générale » ni de « note » de transfert ou de contrôle : ce sont des commentaires (H-DEC-12, analyse).
Plus de « pièce de facturation liée » ni de « date de facturation » : la facture référence la DSM
(analyse §3). Plus de signature (§2.2).

#### 3.2.1 Ligne de DSM (entité `LigneDsm`, lot 1)

| Champ | Type | Règle | Source |
|---|---|---|---|
| clé de conditionnement | `CleConditionnement` : `MB`, `MV`, `NB`, `NV` | Obligatoire, figée pour une ligne reprise (RG-DEC-10, 13) | §4.4.3 p.23, C.3.2.1 p.108 |
| millésime | `Millesime` | Obligatoire si la clé commence par `M`, absent sinon (RG-DEC-10) | H-DEC-8 |
| dénomination | texte court | Obligatoire, non vide, si la clé commence par `N`, absente sinon ; comparée normalisée (RG-DEC-10, 11) | H-DEC-8 |
| compte d'âge | `CompteDAgeDsm` : `00`, `0`, `1` … `19`, `20+` | `MB` : calculé ; autres : saisi (RG-DEC-12) | §4.7 p.39-40, annexe B p.98-99, H-DEC-9 |
| origine | `REPRISE`, `AJOUT` | `REPRISE` pour une ligne venue du stock initial ou de la DSM précédente (RG-DEC-13, 14) | H-DEC-11 |
| volumes | `VolumeAlcoolPur` pour chacune des colonnes 1 à 25 | 0 par défaut ; colonne 1 calculée, non saisie ; colonnes grisées vides pour un producteur (RG-DEC-15 à 17) | §4.4.3.1 p.23-24 |
| stock de fin (colonne 26) | `VolumeAlcoolPur` **calculé** | Non stocké (RG-DEC-17) | §4.4.3.1 p.24 |

`CompteDAgeDsm` : objet-valeur de `declarations`, un `CompteDAge` borné ; tout compte de 20 ou plus vaut
`20+` (H-DEC-9), dans la partie publiée (A-DEC-8). `VolumeAlcoolPur` (`commun.domain`, A-DEC-8) :
litres d'alcool pur, `BigDecimal` à 2 décimales, signé ; un volume saisi est positif ou nul (RG-DEC-15),
le stock de fin calculé peut être négatif (anomalie, RG-DEC-18).

#### 3.2.2 Justificatif (entité `Justificatif`, lot 3)

| Champ | Type | Règle | Source |
|---|---|---|---|
| ligne | référence `LigneDsm` | Facultative : vide, la pièce est rattachée à la DSM (RG-DEC-26) | C.3.3.1 p.111 |
| type de mouvement | `ACHAT`, `AUTRES_FABRICATIONS`, `VENTE_NEGOCE`, `VENTE_EXPORT`, `REINTEGRATION` | Obligatoire (§3.1, RG-DEC-26) | §4.4.5 p.25-26 |
| type de pièce | `FACTURE`, `DAE`, `AVOIR`, `DECLARATION_FABRICATION_DOUANES` | Obligatoire, compatible avec le type de mouvement (RG-DEC-26) ; `CONTRAT` avec la spec 06 | §4.4.5 p.25-26, C.3.3.1 p.111 |
| document | `DocumentDepose` : clé, nom, type, taille | PDF (RG-DEC-26) ; stockage commun (A-DEC-5) | analyse §3 |
| référence DAE/DAI | texte court | Facultative | C.3.3.1 p.111 |
| déposé par, déposé le | identifiant et copie du nom ; date-heure | Automatiques | §4.4.5 p.26 |

**Écarts au CDC** : plus de « contrat lié » (avec la spec 06), de « facture liée » ni d'« avoir lié »
(décision du 2026-10-13), de « résultat » ni de « commentaire de contrôle » (commentaires, analyse).

#### 3.2.3 DRM (entité `Drm`, lot 3)

| Champ | Type | Règle | Source |
|---|---|---|---|
| mois couvert | année-mois | Dans la période ; un seul dépôt par mois (RG-DEC-27) | C.3.4.1 p.111 |
| document | `DocumentDepose` | PDF (RG-DEC-27) | §4.4.6 p.26 |
| déposé par, déposé le | identifiant et copie du nom ; date-heure | Automatiques | §4.4.6 p.26 |

#### 3.2.4 Commentaire (entité `Commentaire`, lot 2)

| Champ | Type | Règle | Source |
|---|---|---|---|
| ligne | référence `LigneDsm` | Facultative : vide, le commentaire porte sur la DSM (RG-DEC-24) | analyse §3 |
| auteur | identifiant et copie du nom ; côté `BNIA` ou `OPERATEUR` | Automatique | analyse §3 |
| écrit le, modifié le | date-heure | « modifié » affiché si renseigné (RG-DEC-25) | H-DEC-19 |
| texte | texte long | Obligatoire, non vide | C.3.3.1 p.111 |
| interne | booléen | Faux par défaut ; vrai seulement côté BNIA, invisible de l'opérateur (RG-DEC-24) | C.3.3.1 p.111, H-DEC-19 |
| motif de compléments | booléen | Vrai pour le motif d'une demande de compléments ; non supprimable (RG-DEC-20, 25) | H-DEC-19 |

### 3.3 Point de départ déclaratif (agrégat `PointDeDepartDeclaratif`, lot 1)

| Champ | Type | Règle | Source |
|---|---|---|---|
| opérateur | référence `Operateur` | Obligatoire ; producteur ou négociant | analyse UC-DEC-3 |
| début | date | 1er avril (producteur), premier du mois (négociant) (RG-DEC-06) | H-DEC-5 |
| lignes de stock initial | liste de `LigneStockInitial` : clé, millésime ou dénomination, compte d'âge, volume (`VolumeAlcoolPur`) | Mêmes règles que les lignes de DSM ; liste vide permise (RG-DEC-07) | analyse §3, H-DEC-14 |
| fixé par, fixé le | identifiant et copie du nom ; date-heure | Automatiques | |
| version | entier | Verrou optimiste | |

Plusieurs points de départ par opérateur : le plus récent est **en vigueur** (RG-DEC-09, lot 7). Il est
**figé** dès qu'une DSM qui commence à son début est validée (RG-DEC-08).

**Écart à l'analyse** : « un par opérateur » devient « un en vigueur », pour la reprise après une
interruption (H-DEC-10, H-DEC-14, décision du 2026-10-13).

### 3.4 Journal (`JournalDeclarations`, hors agrégat, en ajout seul, lot 1)

| Champ | Type | Règle |
|---|---|---|
| date | date-heure | |
| acteur | identifiant de l'utilisateur, sans clé étrangère, et copie de son nom | |
| opérateur | référence `Operateur` | Obligatoire |
| DSM | identifiant, sans clé étrangère, et copie de la période | Vide pour une action sur le point de départ |
| action | liste fermée (RG-DEC-23) | |
| détail | texte court | Nom de fichier, mois de DRM, type de pièce, format d'édition… ; jamais de valeurs avant et après |

L'historique d'une DSM (panneau latéral) affiche ses entrées ; l'onglet « Déclarations » de la fiche
opérateur, toutes les siennes (décision du 2026-10-13).

### 3.5 Grille de cotisation (agrégat `GrilleCotisation`, lot 4)

Une seule grille, liste de `TauxCotisation` (objets-valeurs), même forme que `ClasseTva` (spec 01).

| Champ | Type | Règle | Source |
|---|---|---|---|
| montant | décimal, €/hlAP, 2 décimales | Strictement positif (RG-DEC-30) | §4.11.2 p.52, C.10.3.1 p.128 |
| pourcentage exonéré de TVA | décimal, 2 décimales | De 0 à 100 (RG-DEC-30) | §4.11.2 p.52, C.10.3.1 p.128 |
| début de validité | date | Obligatoire, unique dans la grille | C.10.3.1 p.128 |
| fin de validité | date **calculée** | Veille du début du taux suivant ; absente pour le dernier (RG-DEC-30) | H-DEC-20 |

**Écarts au CDC** : ni « type de cotisation » ni « unité de calcul » (C.10.3.1) : la grille ne porte
que la `CVO`, toujours en €/hlAP ; le taux de fin de travaux appartient à `distillation` (§2.2).

### 3.6 Politiques et modèles de lecture (sans état propre)

| Objet | Rôle | Lot |
|---|---|---|
| `CalendrierDeclaratif` | Période suivante, échéance, période attendue, retard (RG-DEC-03 à 05, 39 à 41) ; retards déjà signalés tenus dans `declarations.retard_signale` (A-DEC-7) | 1, 7 |
| `Vieillissement` | Compte d'âge au début d'une période, fusion des lignes devenues identiques (RG-DEC-12, 14) | 1, 2 |
| `ControleCoherence` | Anomalies de calcul, bloquantes à l'envoi ; pièces manquantes, bloquantes à la validation, avec le niveau exact (RG-DEC-18, 29) | 1, 3 |
| `PolitiqueCotisation` | Détail de la cotisation : une ligne par ligne de cotisation (§3.1), volume en hlAP, part non assujettie, part assujettie, total HT (RG-DEC-31 à 34) | 4 |
| Projection `FactureDsm` | Factures et avoirs des DSM reçus de `facturation` : numéro, date, HT, TVA, TTC (RG-DEC-34, A-DEC-4) | 4 |
| Vue de la DSM | DSM, anomalies, cotisation, « à facturer » ou « facturée » lus dans la projection des factures, TVA indicative (RG-DEC-34, A-DEC-4) | 1, 4 |
| Listing des DSM | Filtres : opérateur, type, période, état, en retard, absente (RG-DEC-40) | 2, 7 |
| Stock déclaré | Colonne 26 de la dernière DSM validée de chaque opérateur, par ligne, datée de sa fin de période (RG-DEC-42) | 8 |

Le stock estimé, les engagements, le stock disponible et le bilan prévisionnel (analyse UC-DEC-2, §4.3)
sont livrés avec `contratsarmagnac` et `distillation` ; leur contrat d'interface est décrit en §5.2.

### 3.7 Droits : le module « Déclarations » (révision de la spec 02 §3.2.7)

| Rôle | Déclarations (DSM, pièces, commentaires, point de départ, stocks déclarés) |
|---|---|
| `OBSERVATEUR` | lecture |
| `AGENT` | gestion |
| `ADMIN` | gestion |

Un rôle de l'opérateur BNIA vaut pour tous les opérateurs (RG-OPE-32). Les actions du BNIA (point de
départ, instruction, validation, commentaire interne, dépôt pendant l'instruction, stocks de tous les
opérateurs) exigent la gestion du module **dans l'opérateur BNIA** (RG-DEC-01). La grille relève du
module Paramétrage (décision du 2026-10-13).

## 4. Règles de gestion

| Id | Règle | Source | Lot |
|---|---|---|---|
| **Droits et déclarants** | | | |
| RG-DEC-01 | Lire les DSM, le point de départ et les stocks d'un opérateur exige la lecture du module « Déclarations » sur cet opérateur ; agir exige sa gestion. Réservés à l'opérateur BNIA (gestion) : fixer ou modifier un point de départ, prendre en instruction, demander des compléments, valider, écrire un commentaire interne, déposer une pièce pendant l'instruction, lire les stocks déclarés de tous les opérateurs. L'opérateur ne voit jamais les commentaires internes. | ADR-016, analyse §2, RG-OPE-32 | 1 |
| RG-DEC-02 | Seul un opérateur `PRODUCTEUR` ou `NEGOCIANT` au statut de déclaration `OUI` peut initier une DSM. Le type de déclarant est celui de l'opérateur à l'initiation, et ne change plus. Une DSM en cours quand le statut change va à son terme. | RG-OPE-01, RG-OPE-11, analyse §3, H-DEC-10 | 1 |
| **Calendrier** | | | |
| RG-DEC-03 | La période d'un producteur va du 1er avril au 31 mars ; celle d'un négociant est un mois civil. La périodicité de la fiche opérateur n'est pas lue. | §4.4.10 p.28, H-DEC-5, décision du 2026-10-13 (§2.2) | 1 |
| RG-DEC-04 | La période d'une nouvelle DSM commence le lendemain de la fin de la dernière DSM validée de l'opérateur, ou, si aucune DSM validée ne commence à son début ou après, au début du point de départ en vigueur. Ni chevauchement ni trou, sauf par un nouveau point de départ (RG-DEC-09). | §4.4.2 p.22, §4.4.7 p.27, analyse §3 | 1 |
| RG-DEC-05 | Un opérateur a au plus une DSM non validée. L'initiation est refusée sans point de départ, tant qu'une DSM n'est pas validée, ou avant le premier jour de la période. | analyse §3, H-DEC-14, H-DEC-23 | 1 |
| **Point de départ** | | | |
| RG-DEC-06 | Le début d'un point de départ est un 1er avril pour un producteur, un premier du mois pour un négociant. | H-DEC-5 | 1 |
| RG-DEC-07 | Les lignes de stock initial suivent RG-DEC-10 à 12 et RG-DEC-15 (compte d'âge `MB` calculé au début du point de départ) ; une liste vide est permise. | analyse UC-DEC-3, H-DEC-14 | 1 |
| RG-DEC-08 | Un point de départ se modifie tant qu'aucune DSM qui commence à son début n'est validée. La DSM en cours suit : colonne 1 recalculée, ligne ajoutée au stock initial ajoutée à la DSM, ligne retirée gardée comme ligne `AJOUT` à 0 en colonne 1. Sa date de début ne change pas tant qu'une DSM est en cours : supprimer d'abord le brouillon. | H-DEC-14, décision du 2026-10-13 (implémentation) | 1 |
| RG-DEC-09 | Le BNIA fixe un nouveau point de départ quand l'opérateur n'a aucune DSM en cours, à une date postérieure à la fin de sa dernière DSM validée. Le stock initial proposé est le stock de fin de celle-ci, vieilli (RG-DEC-14), modifiable. Le précédent reste consultable ; le journal trace le trou éventuel. | H-DEC-10, H-DEC-14 | 7 |
| **Lignes** | | | |
| RG-DEC-10 | Une ligne `MB` ou `MV` porte un millésime et pas de dénomination ; une ligne `NB` ou `NV` porte une dénomination non vide et pas de millésime. | §4.4.3 p.23, C.3.2.1 p.108, H-DEC-8 | 1 |
| RG-DEC-11 | Deux lignes d'une DSM (ou d'un stock initial) ne peuvent avoir même millésime ou même dénomination, même clé et même compte d'âge. Les dénominations se comparent sans tenir compte de la casse, des accents ni des espaces. | §4.4.3 p.23, §4.4.7 p.27, H-DEC-8 | 1 |
| RG-DEC-12 | Le compte d'âge d'une ligne va de `00` à `20+`. `MB` : calculé selon l'annexe B au début de la période, borné à `20+`, non saisi. `MV` : saisi, au plus égal au compte calculé de son millésime (sinon anomalie, RG-DEC-18). `NB`, `NV` : saisis. | §4.7 p.39-40, annexe B p.98-99, H-DEC-9 | 1 |
| RG-DEC-13 | Les lignes de la première DSM issue d'un point de départ reprennent son stock initial (colonne 1, origine `REPRISE`). L'opérateur ajoute des lignes (origine `AJOUT`, colonne 1 à 0) et supprime une ligne `AJOUT` ; une ligne `REPRISE` ne se supprime pas et son identité (clé, millésime ou dénomination, compte d'âge saisi) ne se modifie pas. | §4.4.3 p.23, H-DEC-11 | 1 |
| RG-DEC-14 | À l'initiation, chaque ligne de la DSM précédente dont le stock de fin est strictement positif devient une ligne `REPRISE`, colonne 1 = ce stock. Si la période commence un 1er avril, les lignes vieillissent : `MB` recalculé, `NB` +1, bornés à `20+` ; `MV` et `NV` inchangés. Les lignes devenues identiques fusionnent, volumes additionnés. | §4.4.3 p.23, annexe B p.98, analyse §3, H-DEC-9, H-DEC-11 | 2 |
| **Volumes et calculs** | | | |
| RG-DEC-15 | Un volume est en litres d'alcool pur, positif ou nul, à 2 décimales ; il se saisit en litres décimaux. | §4.4.3 p.23, H-DEC-4 | 1 |
| RG-DEC-16 | Pour un producteur, les colonnes 2, 3, 5, 9, 11, 19, 24 et 25 restent visibles, avec leur numéro, mais vides et non saisissables ; elles n'entrent dans aucun total, contrôle ni calcul de cotisation. | §4.4.3.1 p.23, §4.4.3.2 p.25 | 1 |
| RG-DEC-17 | Par ligne : stock de fin (26) = 1 + 2 + 3 + Σ(4 à 9) − Σ(10 à 25). La DSM affiche aussi, par ligne, le total des entrées et des sorties, et, par colonne, le total de la DSM. | §4.4.3.1 p.23-24, modèles Excel | 1 |
| RG-DEC-18 | Sont des **anomalies de calcul** : un stock de fin négatif sur une ligne ; Σ6 ≠ Σ12 ; pour un négociant, Σ5 ≠ Σ11 (égalités exactes au centilitre) ; un compte `MV` supérieur au compte calculé de son millésime. Chaque anomalie est montrée dès la saisie, avec son niveau (DSM, onglet, ligne) et son écart. Elle n'empêche pas d'enregistrer le brouillon ; elle bloque l'envoi (RG-DEC-20). | §4.4.7 p.26-27, C.3.2.4 p.109, H-DEC-7, H-DEC-9 | 1 |
| RG-DEC-19 | La DSM porte une date de DIA facultative. | modèles Excel, H-DEC-12 | 1 |
| **Cycle de la DSM** | | | |
| RG-DEC-20 | Transitions : l'opérateur envoie une DSM `BROUILLON` (`ENVOYEE`) ou la renvoie depuis `COMPLEMENTS_DEMANDES` (`COMPLEMENTS_RECUS`), si elle n'a aucune anomalie de calcul ; le BNIA prend en instruction une DSM `ENVOYEE` (`EN_INSTRUCTION`) ; depuis `EN_INSTRUCTION` ou `COMPLEMENTS_RECUS`, sans nouvelle prise en instruction, il demande des compléments avec un motif obligatoire (`COMPLEMENTS_DEMANDES`) ou la valide (`VALIDEE`) si aucune pièce ne manque (RG-DEC-29). Aucune transition ne sort de `VALIDEE`. Publiés à chaque transition : `DsmEnvoyee`, `ComplementsDemandes` (avec le motif), `DsmValidee`. | analyse §2 et §4.1, H-DEC-17, H-DEC-19 | 2 |
| RG-DEC-21 | Les lignes, les volumes et la date de DIA ne se modifient, et l'import ne s'applique, qu'en `BROUILLON` ou `COMPLEMENTS_DEMANDES`, par l'opérateur. | analyse §2, H-DEC-15 | 2 |
| RG-DEC-22 | Seule une DSM `BROUILLON` se supprime, par l'opérateur ou par le BNIA, avec ses lignes, pièces, commentaires et entrées de journal ; une entrée « brouillon supprimé » reste au journal de l'opérateur. | H-DEC-16 | 1 |
| RG-DEC-23 | Le journal s'écrit en ajout seul, sans valeurs avant et après. Actions : DSM initiée, enregistrée, importée (nom du fichier), envoyée, prise en instruction, compléments demandés, validée, éditée (format), brouillon supprimé ; justificatif déposé ou supprimé (type, nom) ; DRM déposée ou supprimée (mois) ; commentaire supprimé ; point de départ fixé, modifié, importé. | §4.4.10 p.29, décision du 2026-10-13, H-DEC-12 | 1 |
| **Instruction** | | | |
| RG-DEC-24 | L'opérateur et le BNIA commentent la DSM ou une ligne, dans tout état, y compris validée. Un commentaire interne ne s'écrit que côté BNIA et n'est jamais montré à l'opérateur. | analyse §3, C.3.3.1 p.111, H-DEC-19 | 2 |
| RG-DEC-25 | L'auteur modifie son commentaire (« modifié » affiché) ou le supprime (trace au journal). Le motif d'une demande de compléments ne se supprime pas. | H-DEC-19 | 2 |
| **Pièces** | | | |
| RG-DEC-26 | Un justificatif est un PDF, rattaché à la DSM ou à une de ses lignes, avec un type de mouvement et un type de pièce compatibles : achat, vente négoce, vente export : facture ou DAE ; réintégration : avoir ou DAE ; autres fabrications : déclaration de fabrication douanes. Plusieurs par type de mouvement. | §4.4.5 p.25-26, C.3.3.1 p.111, H-DEC-13 | 3 |
| RG-DEC-27 | Une DSM producteur attend une DRM par mois de sa période ; une DRM est un PDF, pour un mois de la période ; un second dépôt pour le même mois est refusé tant que le premier n'est pas supprimé. Une DSM négociant n'a pas de DRM. | §4.4.6 p.26, C.3.4.1 p.111, H-DEC-18 | 3 |
| RG-DEC-28 | L'opérateur dépose et supprime pièces et DRM en `BROUILLON` ou `COMPLEMENTS_DEMANDES` ; le BNIA dans tout état sauf `VALIDEE`. Le remplacement est une suppression suivie d'un dépôt. | §4.4.6 p.26, H-DEC-18, décision du 2026-10-13 | 3 |
| RG-DEC-29 | La validation est bloquée tant que, pour un type de mouvement dont le total est non nul (§3.1), aucune pièce de ce type n'est déposée, ou, pour un producteur, tant qu'un mois de la période n'a pas sa DRM. La colonne 15 n'appelle rien. Chaque manque est montré avec son niveau. | §4.4.7 p.27, §4.4.8 p.27, H-DEC-13, Q-DEC-1 | 3 |
| **Cotisation** | | | |
| RG-DEC-30 | Un taux CVO a un montant strictement positif en €/hlAP et un pourcentage exonéré de TVA de 0 à 100, à 2 décimales chacun, et une date de début unique dans la grille. Sa fin est la veille du début du taux suivant ; le dernier n'a pas de fin. Ajouter, modifier ou supprimer un taux recompose la chaîne, sans trou ni chevauchement. | §4.11.2 p.52, C.10.3.1 p.128, H-DEC-20 | 4 |
| RG-DEC-31 | L'assiette d'une DSM est le volume des colonnes 14, 16 à 19 et 21 à 25 (§3.1), colonnes grisées exclues : de fait 14, 16, 17, 18, 21, 22 et 23 pour un producteur. | §4.4.9 p.27-28, §4.9.3 p.44-45, H-DEC-1, H-DEC-2 | 4 |
| RG-DEC-32 | Le taux appliqué est celui en vigueur à la fin de la période. Sans taux à cette date, la cotisation est « non calculable » et le signale. | analyse §3 | 4 |
| RG-DEC-33 | Le détail de la cotisation a une ligne par ligne de cotisation (§3.1) : volume en hlAP = litres ÷ 100 ; part non assujettie = volume × montant × pourcentage ; part assujettie = volume × montant × (1 − pourcentage) ; chaque part arrondie au centime, demi supérieur. Total HT = somme des parts arrondies. | facture `FACTURE-CVO-Armagnac_2026000700.pdf`, H-DEC-3, H-DEC-4, H-DEC-20 | 4 |
| RG-DEC-34 | La cotisation se calcule à la volée tant que la DSM n'est pas facturée ; facturée, la vue montre les montants de la facture. « À facturer » = validée et sans facture. La facture d'une DSM est lue dans la projection locale alimentée par `FactureEmise` et `AvoirEmis` (A-DEC-4). La TVA d'une DSM non facturée est indicative : part assujettie × taux de la classe de TVA de la `CVO` (paramètre de l'application, lue dans `referentiels`) en vigueur à la date du jour ; TTC = HT + TVA. | analyse §3, A-DEC-4, H-DEC-22, facture `FACTURE-CVO-Armagnac_2026000700.pdf` | 4 |
| **Éditions et import** | | | |
| RG-DEC-35 | Le PDF d'une DSM présente le format unique, un onglet par page, avec l'en-tête (opérateur, période, échéance, date de DIA), les totaux, le détail de la cotisation, l'état et la date de validation ; colonnes grisées visibles comme telles. Avant validation : « Document provisoire, DSM non validée ». Sans commentaires, pièces ni journal. | §4.4.4 p.25, H-DEC-21 | 5 |
| RG-DEC-36 | L'édition Excel reprend la matrice négociant : 5 onglets, 26 colonnes numérotées, cellules en litres au format `#0\.00.00`, ligne « base cotisation », colonnes grisées grisées et vides pour un producteur, en-tête de RG-DEC-35, et un 6e onglet « Cotisation ». Celle d'un brouillon sert de modèle pré-rempli. | §4.4.4 p.25, §4.4.10 p.28, H-DEC-21 | 6 |
| RG-DEC-37 | L'import Excel est tout ou rien : clé inconnue, volume négatif ou non numérique, ligne en double, valeur en colonne grisée, mois ou année du fichier différent de la période refusent le fichier entier, avec la liste des erreurs (onglet, ligne, colonne). | §4.4.3.2 p.25, §4.4.10 p.28-29, H-DEC-15 | 6 |
| RG-DEC-38 | Un import remplace les colonnes 2 à 25. Les lignes se rapprochent par leur identité : ligne du fichier absente de la DSM ajoutée (`AJOUT`), ligne `REPRISE` absente du fichier gardée à 0, ligne `AJOUT` absente supprimée. Colonnes 1 et 26 et compte `MB` du fichier ignorés, leurs écarts signalés en avertissement. L'onglet « Cotisation » est ignoré. Le même import (colonne 26) alimente un stock initial. | §5.4.4 p.64, H-DEC-14, H-DEC-15 | 6 |
| **Retard** | | | |
| RG-DEC-39 | L'échéance d'une période est le 5 du mois qui suit sa fin (négociant), le 30 avril qui suit (producteur), jour compris. | modèles Excel, H-DEC-6 | 7 |
| RG-DEC-40 | La période attendue d'un opérateur au statut `OUI` est celle de sa DSM non validée s'il en a une, sinon celle qui suit sa dernière DSM validée (ou commence à son point de départ). Elle est **absente** si aucune DSM n'est initiée pour elle. Elle est **en retard** si son échéance est passée et que sa DSM est absente, `BROUILLON` ou `COMPLEMENTS_DEMANDES` ; une DSM `ENVOYEE`, `EN_INSTRUCTION` ou `COMPLEMENTS_RECUS` attend le BNIA, l'opérateur n'est pas en retard. | §4.4.8 p.27, H-DEC-6, H-DEC-10 | 7 |
| RG-DEC-41 | Un traitement planifié quotidien publie `DeclarationEnRetard` une seule fois par période en retard, le lendemain de l'échéance. | §4.4.8 p.27, analyse §4.1, H-DEC-6 | 7 |
| **Stocks** | | | |
| RG-DEC-42 | Le stock déclaré d'un opérateur est le stock de fin (colonne 26) de sa dernière DSM validée, ligne par ligne, daté de la fin de sa période ; sans DSM validée, il n'en a pas. | analyse UC-DEC-1, §4.10.5 p.49 | 8 |

## 5. Cas d'usage

### 5.1 Écriture

Chaque cas d'usage obtient d'abord les `Droits` de l'acteur sur l'opérateur concerné (RG-DEC-01,
ADR-016) et a son test de refus ; il écrit au journal (RG-DEC-23). Les numéros `UC-DEC-n` sont ceux de
l'analyse ; les autres lignes sont des cas secondaires.

| Cas d'usage | Objets | RG | Lot |
|---|---|---|---|
| Fixer le point de départ d'un opérateur, saisi à l'écran (UC-DEC-3, BNIA) | `PointDeDepartDeclaratif` | 01, 06, 07, 10 à 12, 15, 23 | 1 |
| Modifier le point de départ tant qu'il n'est pas figé ; la DSM en cours suit (BNIA) | `PointDeDepartDeclaratif`, `DeclarationStocksMouvements` | 01, 07, 08, 23 | 1 |
| Initier la DSM de la période attendue (UC-DEC-4) | `DeclarationStocksMouvements`, `PointDeDepartDeclaratif` | 01 à 05, 12, 13, 23 | 1 |
| Enregistrer la saisie : ajouter, modifier, supprimer des lignes, saisir les volumes et la date de DIA (UC-DEC-4) | `DeclarationStocksMouvements` | 01, 10 à 13, 15 à 19, 21, 23 | 1 |
| Supprimer un brouillon (opérateur ou BNIA) | `DeclarationStocksMouvements` | 01, 22, 23 | 1 |
| Initier la DSM suivante, avec reprise et vieillissement (UC-DEC-4) | `DeclarationStocksMouvements` | 01 à 05, 12, 14, 23 | 2 |
| Envoyer, renvoyer après compléments (UC-DEC-6) | `DeclarationStocksMouvements` | 01, 18, 20, 23 | 2 |
| Prendre en instruction (UC-DEC-8, BNIA) | `DeclarationStocksMouvements` | 01, 20, 23 | 2 |
| Demander des compléments, avec motif (UC-DEC-8, BNIA) | `DeclarationStocksMouvements`, `Commentaire` | 01, 20, 24, 23 | 2 |
| Valider (UC-DEC-9, BNIA) ; contrôle des pièces au lot 3 | `DeclarationStocksMouvements` | 01, 20, 23 ; 29 | 2 ; 3 |
| Commenter, modifier ou supprimer son commentaire | `Commentaire` | 01, 24, 25, 23 | 2 |
| Déposer, supprimer un justificatif | `Justificatif` | 01, 26, 28, 23 | 3 |
| Déposer, supprimer une DRM | `Drm` | 01, 27, 28, 23 | 3 |
| Ajouter, modifier, supprimer un taux CVO (module Paramétrage) | `GrilleCotisation` | 30 | 4 |
| Enregistrer une facture ou un avoir reçu de `facturation` (écouteur de `FactureEmise`, `AvoirEmis`) | projection `FactureDsm` | 34 | 4 |
| Éditer une DSM en PDF | `DeclarationStocksMouvements` | 01, 23, 35 | 5 |
| Éditer une DSM en Excel | `DeclarationStocksMouvements` | 01, 23, 36 | 6 |
| Importer le modèle Excel dans une DSM (UC-DEC-5) | `DeclarationStocksMouvements` | 01, 21, 23, 37, 38 | 6 |
| Importer un stock initial depuis le modèle Excel (BNIA) | `PointDeDepartDeclaratif` | 01, 08, 23, 37, 38 | 6 |
| Détecter les retards : traitement planifié quotidien | `CalendrierDeclaratif` | 39 à 41 | 7 |
| Fixer un nouveau point de départ après une interruption (BNIA) | `PointDeDepartDeclaratif` | 01, 09, 23 | 7 |
| *Lecture* : consulter une DSM, ses anomalies, son historique ; télécharger une pièce | vue de la DSM, journal | 01, 16 à 18, 23 ; 29 (lot 3) ; 31 à 34 (lot 4) | 1 |
| *Lecture* : les DSM d'un opérateur (portail, fiche opérateur) ; son point de départ et son journal | listing, journal | 01, 23 | 1 |
| *Lecture* : le listing du BNIA, filtré par état (UC-DEC-7) ; puis « à facturer », « facturée » (lot 4) ; « en retard », « absente » (lot 7) | listing des DSM | 01, 34, 40 | 2, 4, 7 |
| *Lecture* : le stock déclaré d'un opérateur ou de tous, export Excel (UC-DEC-1, BNIA) | stock déclaré | 01, 42 | 8 |

Hors des lots de ce contexte : UC-DEC-2 (stock estimé, avec `contratsarmagnac` et `distillation`),
UC-DEC-10 (facturation, porté par `facturation`), UC-DEC-11 (notifications, envoyées par
`notifications` à partir des événements publiés ici).

### 5.2 Lecture

`declarations` ne dépend que de `operateurs` et de `commun`. Les autres contextes dépendent de lui
(analyse §4) : il publie une **partie en lecture**, des **événements**, et les types des événements qu'il
reçoit (ADR-012, ADR-013), dans `fr.bnia.declarations.*.partage` (A-DEC-1).

**Ce qu'il lit** : dans `operateurs.partage`, l'opérateur (type, statut de déclaration, code, raison
sociale), les opérateurs à `OUI` (`declarantsAttendus()`), les droits et le nom d'un utilisateur ; dans
`referentiels.partage`, le taux de la classe de TVA de la `CVO` à une date (RG-DEC-34, H-DEC-22).

**Partie publiée** (décision du 2026-10-13 : seulement le nécessaire)

| Besoin | Consommateur | Lot |
|---|---|---|
| Cotisation d'une DSM : opérateur, état, fin de période, détail par ligne de cotisation (catégorie, volume en hlAP, part non assujettie, part assujettie), total HT | `facturation` | 4 |
| Dernière DSM d'un opérateur (période, état) et son stock déclaré | fiche opérateur (composition, `contextes.md` §3) | 8 |

**Factures des DSM** (A-DEC-4, écart à l'analyse §4.2 qui proposait un port implémenté par
`facturation`) : `facturation` publie `FactureEmise` et `AvoirEmis` (DSM, numéro, date, HT, TVA, TTC),
dont les types sont dans la partie publiée de `declarations` ; `declarations` en tient une projection
locale. Tant que `facturation` n'existe pas, elle est vide (lot 4).

**Événements publiés** (ADR-013 ; sans écouteur tant que `notifications` n'existe pas)

| Événement | Champs minimaux | Émis par | Lot |
|---|---|---|---|
| `DsmEnvoyee` | DSM, opérateur, période, premier envoi ou renvoi | envoi, renvoi (RG-DEC-20) | 2 |
| `ComplementsDemandes` | DSM, opérateur, période, motif | demande de compléments (RG-DEC-20) | 2 |
| `DsmValidee` | DSM, opérateur, période | validation (RG-DEC-20) | 2 |
| `DeclarationEnRetard` | opérateur, période attendue, échéance | traitement planifié (RG-DEC-41) | 7 |

**Contrat d'interface du stock estimé** (décision du 2026-10-13 ; livré avec les specs 06 et 09, qui
le confirmeront). Définitions de l'analyse (§1, UC-DEC-2) :

- **Stock estimé** à une date : le stock déclaré vieilli à cette date, plus les entrées connues depuis la
  fin de sa période (achats retirés des contrats, volume produit par les fins de travaux du
  propriétaire), moins les ventes retirées des contrats.
- **Engagements** : les volumes des contrats de vente validés, pas encore retirés.
- **Stock disponible** : le stock estimé moins les engagements.
- **Bilan prévisionnel** : pour un vendeur et les lignes d'un contrat, le stock disponible au grain du
  contrat, comparé aux volumes demandés ; lu par `contratsarmagnac` avant validation.
- **Grain** : millésime pour les lignes `M` ; dénomination (« assemblage ») et compte d'âge pour les
  lignes `N` ; fût et bouteille confondus. Le vieillissement (RG-DEC-14) s'applique au stock déclaré
  avant l'agrégation.
- **Événements reçus**, types dans la partie publiée de `declarations`, champs à confirmer par la spec
  06 ou 09 (grain et bilan : Q-DEC-26) : `ContratValide` et `ContratModifie` (contrat, vendeur, acheteur, lignes : millésime ou
  assemblage, compte d'âge, volume en litres d'alcool pur) ; `RetiraisonEffectuee` (contrat, date,
  volumes retirés par ligne) ; `FinTravauxRecue` (propriétaire, date, millésime, volume produit).

## 6. Écrans

Les deux espaces partagent l'**écran d'une DSM** (§6.3) ; les actions s'y affichent selon l'état, le côté
(BNIA ou opérateur) et les droits du module « Déclarations » (RG-DEC-01). Chaque élément porte son lot.

### 6.1 Back-office (opérateur actif : BNIA)

- **Menu « Déclarations »**, nouveau groupe :
  - *DSM* (UC-DEC-7) : listing de toutes les DSM, en trois onglets : *En cours* (brouillon, envoyée, en
    instruction, compléments demandés ; filtre d'état, « tous » par défaut), *Validées*, *Facturées*
    (rempli au lot 4) ; filtres opérateur (recherche par code ou raison sociale), type de déclarant,
    période ; colonnes opérateur, type, période, état, puis, en cours, modifiée le (dernière action au
    journal) et « Prendre en charge » sur une DSM à prendre en charge, qui la passe en instruction puis l'ouvre, ailleurs envoyée le et validée
    le ; ouverture de la DSM, dont le retour ramène au listing (lot 2). Puis « à facturer » dans les validées, total HT (lot 4) ; échéance, « en retard »,
    « absente » (lot 7) : une période absente apparaît comme une ligne sans DSM.
  - *Stocks déclarés* (UC-DEC-1) : par opérateur et par ligne (clé, millésime ou dénomination, compte
    d'âge, stock, fin de période), filtres opérateur et type, total ; export Excel pour les douanes
    (lot 8).
- **Fiche opérateur, nouvel onglet « Déclarations »** (producteur ou négociant ; remplace les blocs
  « dernière DSM » et « historique des DSM » annoncés par la spec 02 §6.1) :
  - *Point de départ* : début et lignes de stock initial ; fixer, modifier tant qu'il n'est pas figé
    (lot 1), importer depuis le modèle Excel (lot 6) ; points de départ précédents en lecture et « fixer
    un nouveau point de départ » (lot 7) ;
  - *DSM* : les DSM de l'opérateur, du plus récent au plus ancien, avec état ; ouverture (lot 1) ;
  - *Journal* : toutes les entrées de l'opérateur, brouillons supprimés compris (lot 1) ;
  - *Stock déclaré* : celui de l'opérateur (lot 8).
- **Menu « Paramétrage »**, nouvelle entrée *Cotisation > Taux CVO* : liste des taux (début, fin
  calculée, montant, pourcentage exonéré), ajout, modification, suppression ; module Paramétrage (lot 4).

### 6.2 Portail (opérateur actif : producteur ou négociant)

- **Menu « Déclarations »**, entrée *Mes DSM* : les DSM de l'opérateur actif (période, état, envoyée le,
  validée le ; total HT au lot 4) ; bouton « Initier la DSM de <période> », ou la raison qui l'empêche
  (pas de point de départ, DSM précédente non validée, période pas commencée, statut de déclaration) ;
  le point de départ en lecture ; bandeau de l'échéance et du retard (lot 7).
- **Écran d'une DSM** (§6.3), sans commentaires internes ni actions du BNIA.

### 6.3 Écran d'une DSM (commun)

- **En-tête** : opérateur, type de déclarant, période, état, envoyée le, validée le ; nombre
  d'anomalies et de manques ; échéance (lot 7) ; total HT (lot 4) ; actions.
- **Onglets du format unique** (lot 1) : *Récapitulatif*, *Mouvements en entrée*, *Mouvements en
  sortie*, *Ventes France*, *Ventes à l'exportation*. Chacun est une grille : une rangée par ligne
  (millésime ou dénomination, clé, compte d'âge), une colonne par colonne de l'onglet, numérotée ; une
  rangée de totaux. Colonnes 1 et 26 en lecture seule ; colonnes grisées visibles, grisées et vides
  pour un producteur (RG-DEC-16) ; volumes en litres décimaux (RG-DEC-15). Le récapitulatif montre aussi
  les totaux d'entrées et de sorties par ligne ; les contrôles des colonnes croisées (RG-DEC-18) ne se
  montrent qu'en anomalie, dans le panneau des anomalies (totaux d'entrée et de sortie, écart). Ajouter
  une ligne (clé, puis millésime ou dénomination, compte d'âge sauf `MB`, avec suggestion des
  dénominations déjà employées) ; supprimer une ligne `AJOUT`. Date de DIA en tête de l'onglet
  *Mouvements en sortie*, celui de la colonne 10 dont elle date les pertes (H-DEC-12). Bouton
  « Enregistrer ».
- **Panneau des anomalies et des manques** : chaque anomalie (lot 1) et chaque pièce manquante (lot 3)
  avec son niveau et son écart ; un clic ouvre l'onglet et la ligne concernés, ou l'onglet *Pièces*
  pour un manque (§4.4.7 p.27). Les manques sont recalculés avec la vue provisoire.
- **Onglet *Pièces*** (lot 3), après les cinq du format unique, avec le nombre de manques : justificatifs,
  groupés par type de mouvement (avec ses colonnes et ses types de pièce, « pièce attendue » s'il en
  manque une), avec leur rattachement (DSM, ligne, ou « ligne supprimée » : comme un commentaire, la
  pièce d'une ligne ajoutée puis supprimée passe sur la DSM avec le libellé de la ligne) ; dépôt (type
  de mouvement, type de pièce filtré selon lui, ligne facultative, référence DAE/DAI, PDF), ouverture
  du PDF dans un nouvel onglet, suppression confirmée ; pour un producteur, les mois de la période avec
  l'état de leur DRM (déposée ou attendue), dépôt, ouverture, suppression. Une pièce déposée ne change
  pas la version de la DSM.
- **Section *Commentaires*** (lot 2), sous le classeur et hors des onglets (elle ne fait pas partie de
  la saisie, décision du 2026-10-13) : le fil, du plus ancien au plus récent ; un commentaire peut viser
  une ligne (lien vers elle) ; « interne » à cocher côté BNIA, signalé par un badge ; « modifié »
  affiché ; modifier ou supprimer les siens. Une ligne de la grille porteuse de commentaires a une
  pastille qui y mène. Les commentaires d'une ligne ajoutée puis supprimée passent sur la DSM, avec le
  libellé de la ligne.
- **Onglet *Cotisation*** (lot 4) : le détail par ligne de cotisation (volume en hlAP, parts non
  assujettie et assujettie, total), le taux et le pourcentage appliqués ; « facturée » ou « à
  facturer », TVA et TTC indicatifs, puis ceux de la facture.
- **Historique** (lot 1) : le journal de la DSM, hors des onglets (ce n'est pas une partie de la
  saisie), dans un panneau latéral ouvert depuis l'en-tête.
- **Actions**, selon l'état et le côté :

| État | Opérateur (gestion) | BNIA (gestion) |
|---|---|---|
| `BROUILLON` | enregistrer, importer (lot 6), déposer des pièces (lot 3), envoyer (lot 2), supprimer | supprimer |
| `ENVOYEE` | — | prendre en charge (la DSM passe en instruction) |
| `EN_INSTRUCTION` | — | demander des compléments (motif obligatoire), valider |
| `COMPLEMENTS_DEMANDES` | enregistrer, importer, déposer des pièces, renvoyer | — |
| `COMPLEMENTS_RECUS` | — | demander des compléments, valider |
| tous | commenter, éditer en PDF (lot 5) et Excel (lot 6) | commenter, éditer, déposer des pièces sauf `VALIDEE` (lot 3) |

  L'envoi et la validation désactivés montrent leur motif (anomalies, manques). La confirmation d'envoi
  signale les manques, qui ne bloquent pas l'envoi (H-DEC-13). Les lecteurs
  (`OBSERVATEUR`) voient tout sans action, sauf éditer.

## 7. Données

### 7.1 Démonstration (profil `demo`)

Données plausibles, présentées comme telles, sur les opérateurs et les comptes de démonstration de la
spec 02 (`docs/demo.md`). Les dates sont **calculées depuis la date du jour** au chargement, pour que
échéances et retards restent parlants ; les exemples ci-dessous supposent le 13 octobre 2026. Chaque
lot ajoute ses données et son scénario à `docs/demo.md`.

| Opérateur | Données | Montre | Lot |
|---|---|---|---|
| Armagnacs de Gascogne (`N320001`, négociant) | Point de départ au 1er du mois courant (1er octobre 2026), stock fixé par le BNIA au dernier jour du mois précédent, six lignes (`NB` « XO » 12, `NB` « VSOP » 5, `NV` « VSOP » 5, `MB` 2010, `MV` 2015 compte 6, `NB` « Blanche » `00`) ; DSM d'octobre envoyée, qui reproduit la facture d'exemple (sorties France 839,11 L, export 2 254,15 L) ; un commentaire de l'opérateur, un commentaire interne du BNIA | Saisie, colonnes ; cycle, file d'attente du BNIA ; puis cotisation de l'exemple (lot 4) | 1, 2 |
| Maison Durand, Condom (producteur) | Point de départ au 1er avril 2025 ; DSM 2025-2026 validée, avec `NB` « Hors d'âge » 19 et `20+`, `MB` 2015 ; DSM 2026-2027 à initier | Reprise, vieillissement au 1er avril, fusion à `20+`, colonnes grisées du producteur | 2 |
| Maison Durand, Condom | Douze DRM et des justificatifs (PDF de démonstration) sur la DSM validée | Pièces, mois attendus | 3 |
| Maison Larressingle (`N320002`, négociant) | Point de départ au 1er du mois courant ; DSM d'octobre en compléments demandés, motif « DAE manquants pour l'export » | Compléments ; retard (lot 7) | 2 |
| Négoce des Landes (`N400001`, négociant) | Point de départ au 1er du mois courant ; DSM d'octobre en brouillon, Σ6 ≠ Σ12 | Anomalie qui bloque l'envoi ; modification du point de départ ; retard (lot 7) | 2 |
| Maison Durand, Eauze (producteur) | Aucun point de départ | Le BNIA fixe un point de départ | 1 |
| Grille | 55,00 €/hlAP et 20 % depuis le 1er janvier 2021, 60,00 €/hlAP et 20 % depuis le 1er janvier 2024 | Chaîne des taux | 4 |
| Un producteur au statut `SOMMEIL` (spec 02, données à compléter) | DSM 2024-2025 validée | Nouveau point de départ après interruption | 7 |

Les PDF déposés sont un même document de démonstration.

Les négociants partent du mois courant (décision du 2026-10-13) : leur première DSM est celle du mois en
cours, comme pour un opérateur qui démarre sur la plateforme. La reprise d'un négociant (DSM suivante)
ne se montre donc qu'à partir du 1er du mois suivant (RG-DEC-05) ; elle se montre dès le chargement sur
le producteur.

### 7.2 Reprise AGI

Hors périmètre du prototype.

## 8. Décisions d'architecture

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-DEC-1 | Partie publiée sur le modèle de `operateurs` : types partagés et **tous** les types d'événements, émis (`DsmEnvoyee`, `ComplementsDemandes`, `DsmValidee`, `DeclarationEnRetard`) et reçus (`ContratValide`, `ContratModifie`, `RetiraisonEffectuee`, `FinTravauxRecue`, `FactureEmise`, `AvoirEmis`), dans `fr.bnia.declarations.domain.partage` ; lecture `DeclarationsLecture` dans `application.partage`. Troisième partie publiée, exception nommée. | Ports de lecture de passe-plat dans chaque contexte lecteur (ADR-008) | ADR-017 (étend ADR-008 et 012), ADR-013 |
| A-DEC-2 | `Module.DECLARATIONS` ajouté à `operateurs.domain.partage.Module` ; droits vérifiés dans chaque cas d'usage, un test de refus par cas. | — (conforme) | ADR-016 |
| A-DEC-3 | Journal propre au contexte (`declarations.journal`), en ajout seul, écrit par un port, comme `JournalActions` de `operateurs`. | — (conforme) | — |
| A-DEC-4 | `declarations` tient une **projection locale** des factures de DSM (numéro, date, HT, TVA, TTC), alimentée par `FactureEmise` et `AvoirEmis` que publie `facturation` (types dans la partie publiée de `declarations`). « À facturer » et « facturée » se lisent en SQL local ; la TVA indicative se calcule depuis les taux de `referentiels` (partie publiée). Tant que `facturation` n'existe pas, la table est vide. | Port défini par `declarations` et implémenté par `facturation` (forme nouvelle, ADR à écrire, listing appelant le port) ; composition par le front (filtre « à facturer » impossible côté serveur, RG-DEC-34 hors du domaine) | ADR-017, ADR-013 |
| A-DEC-5 | Stockage de documents **commun à tous les contextes** : port `StockageDocuments` (déposer, lire, supprimer) dans `bnia-commun-core`, objet-valeur `DocumentDepose` (clé, nom, type, taille) dans `commun.domain` ; adapter PostgreSQL (`bytea`, schéma `documents`) dans `bnia-commun-infra`, dans la transaction de l'appelant ; 10 Mo au plus, PDF vérifié par sa signature `%PDF`. | Stockage objet S3/MinIO (service de plus, fichiers orphelins sans transaction) ; répertoire disque (sauvegarde à part, volume partagé) ; tous deux restent possibles derrière le port | ADR-018 |
| A-DEC-6 | Volumes d'une ligne en **table large** : une colonne `numeric(12,2)` par colonne de la DSM (`c01` à `c25`) dans `declarations.ligne_dsm`, mappée vers une `EnumMap<ColonneDsm, VolumeAlcoolPur>` du domaine ; la colonne 26 n'est pas stockée. Stock initial du point de départ : table à part, un volume par ligne. | Table étroite, une ligne par volume (pivot à chaque lecture, sommes difficiles) ; JSONB (typage perdu, sommes par extraction) | ADR-004 |
| A-DEC-7 | Premier traitement planifié : cas d'usage `DetecterRetards` avec horloge injectée (`Clock`), appelé par un `@Scheduled` (cron configurable) dans un nouveau package d'adapter `adapter.planification`, qui n'appelle qu'un cas d'usage ; émission unique garantie par la table `declarations.retard_signale` (contrainte d'unicité, événement publié seulement si l'insertion réussit). `@EnableScheduling` dans `bnia-boot`, coupé dans les IT. | ShedLock (dépendance superflue, l'unicité en base protège l'émission) ; détection à la lecture (émission dépendante du passage d'un utilisateur) | ADR-019 ; CLAUDE.md |
| A-DEC-8 | `VolumeAlcoolPur` dans `commun.domain` (premier besoin réel ; contrats, export, distillation, statistiques suivront) : litres en `BigDecimal` à 2 décimales, arrondi au demi supérieur, signé, avec `enHectolitres()`, `plus`, `moins` ; la positivité d'un volume saisi est une règle de la DSM (RG-DEC-15), un stock de fin calculé pouvant être négatif (anomalie, RG-DEC-18). Le masque hl.l,cl reste dans les adapters d'édition et le front. `CompteDAgeDsm` (borne `20+`) dans `fr.bnia.declarations.domain.partage`, présent dans les vues publiées. | `VolumeAlcoolPur` dans `declarations` puis déplacé au deuxième besoin (déplacement certain, étapes 6 et 7) ; simple `BigDecimal` (règles répétées) ; `CompteDAgeDsm` dans `commun` (contraire à ADR-009) | ADR-009 (amendé le 2026-10-13) |
| A-DEC-9 | Production de documents : Excel avec `fastexcel` et `fastexcel-reader` (déjà dans la pile), matrice reconstruite par le code ; PDF avec OpenHTMLtoPDF et des gabarits Thymeleaf (moteur seul). Gabarits et rendu dans un nouveau package d'adapter `adapter.document` du contexte qui produit le document. | Apache POI sur une copie du modèle du BNIA (dépendance lourde, modèle à maintenir, grisage par type) ; OpenPDF (gabarits en code de placement) ; JasperReports (disproportionné) | ADR-020 ; CLAUDE.md |

## 9. Hypothèses

Chaque hypothèse est retenue pour le prototype (décision du 2026-10-13) et reste **à confirmer par le
BNIA**.

| Id | Hypothèse | Pourquoi |
|---|---|---|
| H-DEC-1 | Assiette négociant : colonnes 14, 16, 17, 18, 19, 21, 22, 23, 24, 25. | Trois sources divergent. §4.9.3 p.44 omet la 16 ; la matrice négociant (`20261005_DI_ModeleDSM-Negciants.xlsx`, ligne « base cotisation ») omet la 25. La facture d'exemple (`FACTURE-CVO-Armagnac_2026000700.pdf`) a deux lignes « EDV destinée à un produit autre que l'Armagnac » (marché intérieur, export) que seules les colonnes 16 et 25 alimentent. |
| H-DEC-2 | Assiette producteur : l'assiette négociant moins les colonnes grisées pour le producteur (§4.4.3.2 p.25 : 2, 3, 5, 9, 11, 19, 24, 25), soit 14, 16, 17, 18, 21, 22, 23. Une seule règle, par colonne, sans liste par type. | Le CDC présente la matrice producteur comme « la même matrice », colonnes hors périmètre grisées (annexe E), et une colonne grisée n'entre pas dans la cotisation (§4.4.3.2 p.25). Écart : §4.9.3 p.45 exclut la 14 pour le producteur. Le modèle producteur à 14 colonnes (`20261005_DI_ModeleDSM-Producteurs.xlsx`), distinct du format unique, confond transferts et coupes dans une colonne hors assiette : il ne tranche pas. |
| H-DEC-3 | Lignes du détail de la cotisation : « Sorties France » = 14, 17, 18, 19, 21 ; « Sorties Export » = 22, 23, 24 ; « EDV autre produit, marché intérieur » = 16 ; « EDV autre produit, export » = 25. | Lignes de la facture d'exemple. Incertains : la 14 (transfert, pas une vente) rangée en France ; les brandies (19, 24) rangées dans le commerce de l'Armagnac, à côté des bouteilles et du vrac, et non dans « EDV autre produit ». Le taux étant le même sur toutes les lignes (60 €/hlAP, facture d'exemple), seule la présentation en dépend. |
| H-DEC-4 | Volumes en litres d'alcool pur, 2 décimales (centilitre), jamais négatifs dans une colonne ; saisis et affichés en litres décimaux, présentés au masque hl.l,cl dans les éditions Excel et PDF. Cotisation : litres ÷ 100 en hlAP, montant arrondi au centime (demi supérieur) par ligne et par part (non assujettie, assujettie) ; sous-totaux = somme des montants arrondis. | LAP : §4.4.3 p.23. Masque hl.l,cl : annexe A p.96, fichiers du BNIA « en Hl.l,cl AP ». Arrondi recalculé sur la facture d'exemple (8,3911 hlAP × 12 € = 100,69 € ; 22,5415 × 12 = 270,50 €). Le masque hl.l,cl à la saisie (point entre hl et l, virgule entre l et cl) exposerait à des erreurs d'un facteur 100. |
| H-DEC-5 | Aucune période ne contient le 1er avril ailleurs qu'à son début : la période producteur (annuelle) va du 1er avril au 31 mars ; la période négociant (mensuelle) est un mois civil. Le point de départ est un 1er avril pour un producteur, un premier du mois pour un négociant. | Le stock vieillit au 1er avril (annexe B p.98) : une période à cheval obligerait à deux comptes d'âge par ligne, et le stock au 31 mars n'apparaîtrait dans aucune DSM. Le modèle producteur couvre « du 1er avril 2026 au 31 mars 2027 » ; le modèle négociant, un mois civil. |
| H-DEC-6 | Échéance : le 5 du mois qui suit la fin de période (négociant), le 30 avril qui suit (producteur), jour compris. Seule la période attendue (celle de la DSM non validée s'il y en a une, sinon celle qui suit la dernière DSM validée) peut être en retard : échéance passée et DSM absente, en brouillon ou en compléments demandés. L'opérateur n'est pas en retard tant que sa DSM attend le BNIA (envoyée, en instruction, compléments reçus). `DeclarationEnRetard` est publié une fois par période, le lendemain de l'échéance. | §4.4.8 p.27 ne définit pas le retard. Modèle négociant : « A retourner au plus tard le 5 du mois suivant la déclaration » ; modèle producteur : « A retourner avant le 30 avril 2027 », lu « au plus tard » par cohérence. Une seule DSM non validée à la fois : la lenteur de l'instruction ne doit pas compter contre l'opérateur. |
| H-DEC-7 | Les colonnes croisées se contrôlent sur les totaux de la DSM, à égalité exacte au centilitre : Σ6 = Σ12 (assemblages / coupes) et, pour le négociant, Σ5 = Σ11 (passage sous bois / sous-verre). Ces égalités et un stock de fin positif ou nul sur chaque ligne bloquent l'envoi ; le brouillon reste enregistrable. | §4.4.7 p.26-27 cite les « colonnes croisées » sans dire quelle égalité. C.3.2.4 p.109 : « valeur de contrôle visible pour rapprocher les mouvements symétriques » ; le modèle négociant recopie les totaux 12 et 11 dans l'onglet des entrées. Une coupe prend sur plusieurs lignes pour en alimenter une autre : l'égalité ne vaut pas ligne par ligne. Une perte se déclare en colonne 10. |
| H-DEC-8 | La première lettre de la clé décide de ce que porte la ligne : clé `M` (`MB`, `MV`), un millésime obligatoire (`Millesime`) et pas de dénomination ; clé `N` (`NB`, `NV`), une dénomination obligatoire et pas de millésime. La dénomination est un texte libre ; l'unicité de la ligne la compare sans tenir compte de la casse, des espaces ni des accents ; l'écran suggère les dénominations déjà employées par l'opérateur. | §4.4.3 p.23 et C.3.2.1 p.108 : « qualité ou dénomination » et « millésime lorsque la ligne porte un millésime », texte court tous deux ; les modèles Excel n'ont qu'une colonne « Millésime ou Dénomination », et leur légende dit « M : Millésime - N : Non Millésime ». Une liste fermée demanderait un référentiel inconnu et exclurait les marques propres à un négociant. |
| H-DEC-9 | Le compte d'âge d'une ligne de DSM va de `00` à `20+`. `MB` : calculé depuis le millésime (annexe B), non saisi. `MV` : saisi, figé, au plus égal au compte calculé du millésime (sinon l'envoi est bloqué). `NB`, `NV` : saisis. Au 1er avril, `MB` est recalculé et `NB` prend +1, bornés à `20+` ; les lignes devenues identiques sont fusionnées dans le stock de début, volumes additionnés. | Borne : §4.7 p.39-40 (« la borne définie sur les DSM de `20+` »). Règle de calcul : annexe B p.98-99. Seul le fût vieillit (analyse). Une ligne `MV` ne peut être plus vieille que son millésime en fût. Deux lignes `NB` de compte 19 et `20+` deviennent identiques au 1er avril. |
| H-DEC-10 | Seul un opérateur au statut de déclaration `OUI` peut initier une DSM et peut être en retard ; une DSM en cours quand le statut change va à son terme. Après un statut autre que `OUI`, la reprise passe par un nouveau point de départ fixé par le BNIA ; le stock initial proposé est le stock de fin de la dernière DSM validée, vieilli, modifiable ; le trou est tracé au journal. | Sens des statuts : H-OPE-2 (spec 02), non confirmé (Q-OPE-7) ; RG-OPE-11 : seul `OUI` rend une DSM attendue, toutes les transitions permises. Sans mouvement pendant le sommeil, le rattrapage période par période (une DSM non validée à la fois) imposerait autant de cycles d'envoi et de validation vides, en retard dès la reprise. |
| H-DEC-11 | À l'initiation, seules les lignes de la dernière DSM validée dont le stock de fin est strictement positif sont reprises (colonne 1 = ancien stock de fin, compte d'âge vieilli). Une ligne ajoutée commence à 0 et peut être supprimée tant que la DSM est modifiable ; une ligne reprise ne se supprime pas, et son identité (clé, millésime ou dénomination) ne se modifie pas : un changement passe par les colonnes 12 et 6. | §4.4.3 p.23 et §4.4.3.1 p.23 : stock de début repris, en lecture seule ; le CDC ne dit rien des lignes à reprendre, ni de l'ajout ou de la suppression. Reprendre les lignes à 0 allongerait la DSM sans fin ; supprimer une ligne reprise ferait disparaître du volume. |
| H-DEC-12 | La DSM porte une date de DIA facultative, reprise dans les éditions en tête de la colonne 10. La note de transfert en région (colonne 13) et l'observation générale ne sont pas des champs : ce sont des commentaires, sur la ligne ou sur la DSM. Le mode d'alimentation n'est pas un attribut : le journal trace chaque saisie et chaque import, avec le nom du fichier. | Modèles Excel : « Pertes inventaire DIA du …/…/… » ; le CDC n'a pas de champ de date (C.3.2.5 p.109 dit « DAI »). Note de transfert : C.3.2.5 p.110 ; observation générale et mode d'alimentation : C.3.1.1 p.107. Un seul fil de commentaires (analyse). Une DSM importée puis corrigée à la main n'a pas un mode unique. |
| H-DEC-13 | Justificatifs attendus par type de mouvement : achats (2, 3), autres fabrications (16), ventes négoce (20, 21), ventes export (22 à 25), réintégrations (9) ; le Floc (15) n'appelle rien de bloquant (Q-DEC-1). Le contrôle se fait sur la DSM : dès que le total d'un type de mouvement est non nul, au moins une pièce de ce type, rattachée à la DSM ou à l'une de ses lignes. Il bloque la validation, pas l'envoi. | §4.4.5 p.25-26 ; rattachement à la DSM ou à une ligne : C.3.3.1 p.111, §4.4.8 p.27. Une pièce (DAE, facture) couvre souvent plusieurs lignes ; un contrôle par ligne alourdirait la saisie sans lire le PDF ; le BNIA vérifie le contenu à l'instruction. |
| H-DEC-14 | Point de départ : date de début et lignes de stock initial (règles des lignes de DSM, aucune ligne permise), saisies à l'écran ou importées du modèle Excel du BNIA (colonne 26). Modifiable tant que la première DSM qui en est issue n'est pas validée : la colonne 1 de la DSM en cours suit, une ligne retirée y reste comme ligne ajoutée à 0. Un nouveau point de départ est possible quand l'opérateur n'a aucune DSM en cours, à une date postérieure à sa dernière DSM validée, avec pour stock proposé le stock de fin de celle-ci, vieilli ; le précédent reste consultable, le trou est tracé au journal. | Analyse UC-DEC-3 (amendement). `operateurs` ne garde pas l'historique des statuts : la reprise après sommeil (H-DEC-10) ne peut pas s'en déduire. Corriger le stock initial pendant l'instruction de la première DSM sans perdre les mouvements saisis. |
| H-DEC-15 | Import du modèle Excel (format de la matrice négociant, 5 onglets, cellules en litres affichées au masque `#0\.00.00`) sur une DSM modifiable seulement, tout ou rien : clé inconnue, volume négatif ou non numérique, ligne en double, valeur en colonne grisée ou mois différent de la période refusent le fichier, erreurs situées (onglet, ligne, colonne). L'import remplace les colonnes 2 à 25 ; les lignes se rapprochent par leur identité ; une ligne reprise absente du fichier reste à 0, une ligne ajoutée absente est supprimée. Colonnes 1, 26 et compte `MB` ignorés, écarts signalés en avertissement. Le journal trace l'import et le nom du fichier ; le fichier n'est pas conservé. | §4.4.10 p.28-29 : la matrice négociant, base du format d'import, intègre le grisage ; contrôles quel que soit le mode. §4.4.3.2 p.25 : colonne grisée non alimentée par un import. §5.4.4 p.64 : préremplissage. Formats numériques du modèle négociant (`xl/styles.xml`). Un import partiel laisserait l'opérateur ignorer ce qui est passé. |
| H-DEC-16 | Seule une DSM en brouillon se supprime, par l'opérateur ou par le BNIA (module Déclarations en gestion), avec ses lignes, pièces, commentaires et journal ; une trace de la suppression subsiste dans l'historique des DSM de l'opérateur. Une DSM envoyée ou validée ne se supprime pas. | Le CDC ne parle pas de suppression. Un brouillon n'a jamais été transmis ; le BNIA doit pouvoir lever un brouillon abandonné avant un nouveau point de départ (H-DEC-14). Une DSM envoyée se corrige par les compléments demandés. |
| H-DEC-17 | Une DSM validée ne se rouvre pas. Une erreur de stock se régularise dans la DSM suivante (colonne 10 pour un stock trop haut, colonne 9 pour un stock trop bas, négociant seulement), justifiée par commentaire, ou par un nouveau point de départ (H-DEC-14) ; une erreur de ventilation reste dans la DSM, documentée par un commentaire du BNIA ; la cotisation facturée se corrige par un avoir (`facturation`). | Analyse : la validation fige la DSM. Le CDC ne prévoit pas de DSM rectificative ; colonne 9 « réintégrations après annulation, retour ou régularisation » (C.3.2.4 p.109), colonne 10 « pertes d'inventaire ou ajustements » (C.3.2.5 p.109). La dévalidation exigerait un événement de plus pour `facturation` et les statistiques. |
| H-DEC-18 | DRM et justificatifs : l'opérateur dépose et supprime quand la DSM est chez lui (brouillon, compléments demandés) ; le BNIA, dans tout état non validé ; rien après validation. Exactement un PDF par mois de DRM ; plusieurs pièces par type de mouvement. | §4.4.6 p.26 : le BNIA peut « déposer une DRM manquante depuis le back-office » ; §5.4.6 p.65 ; C.3.4.1 p.111 : un « document déposé » par mois. Pièces reçues par courrier ou e-mail : sans aller-retour de compléments. |
| H-DEC-19 | Instruction : le BNIA prend explicitement une DSM envoyée en instruction ; la demande de compléments exige un motif (commentaire sur la DSM, non supprimable) ; au renvoi, la DSM passe à « compléments reçus » (« compléments envoyés » pour l'opérateur), que le BNIA valide ou renvoie sans la reprendre en instruction (revue du 2026-10-13). Les deux parties commentent dans tous les états, y compris après validation ; le BNIA peut marquer un commentaire « interne », invisible de l'opérateur ; l'auteur modifie (mention « modifié ») ou supprime (trace au journal) son commentaire. | Cycle et `Commentaire` : analyse. C.3.3.1 p.111 : « commentaire interne ». Le geste explicite sépare la file d'attente du travail en cours. |
| H-DEC-20 | Grille CVO en chaîne continue : un nouveau taux clôt le précédent la veille de son début ; seul le dernier est sans fin ; ni trou ni chevauchement. Montant en €/hlAP et pourcentage non assujetti, 2 décimales chacun. La cotisation se calcule à la volée tant que la DSM n'est pas facturée ; la facture la fige (analyse §3) ; un taux se modifie ou se supprime librement, seules les DSM non facturées en dépendent. Part non assujettie = taux × pourcentage, part assujettie = le reste ; chaque montant se calcule et s'arrondit séparément. | §4.11.2 p.52 : « sans remettre en cause l'historique des calculs déjà produits ». C.10.3.1 p.128. Pas de chevauchement : par analogie avec la TVA (§4.11.1 p.52). Un trou bloquerait des DSM sans que l'opérateur y puisse rien. Facture d'exemple : 402,77 € et non 402,78 €. |
| H-DEC-21 | L'édition Excel reprend la matrice négociant (5 onglets, 26 colonnes, format de cellule, ligne « base cotisation », colonnes grisées pour le producteur), avec l'en-tête (opérateur, période, échéance, date de DIA) et un 6e onglet « Cotisation » ignoré à l'import ; elle se réimporte, et celle d'un brouillon sert de modèle pré-rempli. Le PDF présente le même contenu, un onglet par page, avec l'état et la date de validation, sans commentaires, pièces ni journal. Édition possible dans tous les états, « Document provisoire, DSM non validée » avant validation ; chaque édition est inscrite au journal. | §4.4.4 p.25 : Excel ou PDF, format unique, colonnes grisées visibles. §4.4.10 p.28 : import au même format que l'édition, matrice négociant pour base. C.3.1.1 p.108 : date d'édition. |
| H-DEC-22 | La TVA indicative d'une DSM non facturée vaut la part assujettie × le taux de la classe de TVA de la `CVO`, paramètre de l'application, lu dans `referentiels` à la date du jour ; TTC = HT + TVA. | Le CDC ne dit pas quelle classe de TVA s'applique à la cotisation. Facture d'exemple : TVA à 20 % sur la seule part assujettie (402,77 € × 20 % = 80,55 €). Projection des factures : A-DEC-4. |
| H-DEC-23 | Une DSM ne s'initie pas avant le premier jour de sa période (RG-DEC-05). | Le CDC ne le dit pas. Les volumes d'une période ne sont connus qu'en cours ou en fin de période. |

## 10. Questions ouvertes

La colonne « Lot » dit quand la réponse devient nécessaire : c'est l'ordre de relance du BNIA. Les
hypothèses permettent d'avancer sans attendre. Question liée de la spec 02 : Q-OPE-7 (sens des statuts
de déclaration).

| Id | Question | Bloque | Lot |
|---|---|---|---|
| Q-DEC-1 | **Au BNIA** : quelles données Floc de Gascogne l'ODG fournit-il, et sous quelle forme, pour pointer la colonne 15 (§4.4.5 p.25) ? D'ici la réponse, la colonne 15 n'appelle aucun justificatif bloquant ; le BNIA peut commenter la ligne. | Pointage automatique de la colonne 15 | 3 |
| Q-DEC-2 | **Au BNIA** : confirmer que l'assiette négociant comprend les colonnes 16 et 25 en plus de celles de §4.9.3 p.44 (H-DEC-1). | Montant de la cotisation négociant | 4 |
| Q-DEC-3 | **Au BNIA** : confirmer que la colonne 14 (transferts hors région) entre dans l'assiette producteur, contrairement à §4.9.3 p.45 (H-DEC-2). | Montant de la cotisation producteur ; une liste par type d'opérateur si la réponse est non | 4 |
| Q-DEC-4 | **Au BNIA** : confirmer la ligne de facture de chaque colonne de l'assiette, notamment la 14 en « Sorties France » et les brandies (19, 24) dans le commerce de l'Armagnac (H-DEC-3). | Présentation du détail de la cotisation et de la facture ; statistiques par ligne | 4 |
| Q-DEC-5 | **Au BNIA** : confirmer l'unité (litres d'alcool pur au centilitre), la saisie en litres décimaux et l'arrondi au centime par ligne et par part (H-DEC-4). | Écran de saisie ; écarts d'un centime avec les factures actuelles | 1 |
| Q-DEC-6 | **Au BNIA** : confirmer qu'aucune période ne chevauche le 1er avril (producteur : 1er avril - 31 mars ; négociant : mois civil) (H-DEC-5). | Calcul des périodes et du vieillissement ; choix du point de départ | 1 |
| Q-DEC-7 | **Au BNIA** : confirmer les échéances (5 du mois suivant pour le négociant, 30 avril pour le producteur, jour compris) et la définition du retard, qui exclut l'attente de l'instruction (H-DEC-6). | Traitement planifié des retards, listing, notifications | 7 |
| Q-DEC-8 | **Au BNIA** : confirmer que les contrôles croisés sont Σ6 = Σ12 et Σ5 = Σ11 sur les totaux, à égalité exacte, et qu'ils bloquent l'envoi comme un stock de fin négatif (H-DEC-7). | Contrôles à l'envoi ; tolérance ou simple avertissement si la réponse est non | 1 |
| Q-DEC-9 | **Au BNIA** : confirmer qu'une ligne `M` porte un millésime et une ligne `N` une dénomination en texte libre, sans liste de dénominations (H-DEC-8). | Saisie et import des lignes ; un référentiel de dénominations si la réponse est non | 1 |
| Q-DEC-10 | **Au BNIA** : confirmer la borne `20+` des DSM, le compte `MB` calculé depuis le millésime, le contrôle du compte `MV` et la fusion des lignes `NB` qui atteignent `20+` (H-DEC-9). | Saisie des lignes, reprise du stock de début | 1 |
| Q-DEC-11 | **Au BNIA** : confirmer qu'à la reprise après un sommeil ou une clôture, l'opérateur repart d'un nouveau point de départ, sans déclarer les périodes d'inactivité (H-DEC-10 ; dépend de Q-OPE-7). | Initiation, retard et point de départ ; rattrapage période par période si la réponse est non | 7 |
| Q-DEC-12 | **Au BNIA** : confirmer que les lignes finies à 0 ne sont pas reprises et qu'une ligne reprise garde son identité, un changement passant par les colonnes 12 et 6 (H-DEC-11). | Initiation de la DSM, saisie des lignes | 1 |
| Q-DEC-13 | **Au BNIA** : confirmer la date de DIA facultative, la note de transfert et l'observation générale portées par des commentaires, et l'absence de mode d'alimentation, remplacé par le journal (H-DEC-12). | Champs de la DSM et des éditions | 1 |
| Q-DEC-14 | **Au BNIA** : confirmer la correspondance entre types de mouvement et colonnes, et que la présence des justificatifs se contrôle par type de mouvement sur la DSM, non ligne par ligne (H-DEC-13). | Contrôle avant validation ; rattachement d'une pièce à plusieurs lignes si la réponse est non | 3 |
| Q-DEC-15 | **Au BNIA** : confirmer le point de départ fixé par le BNIA, modifiable jusqu'à la validation de la première DSM, et renouvelable après une interruption (H-DEC-14). | Démarrage d'un opérateur, reprise après sommeil | 1 |
| Q-DEC-16 | **Au BNIA** : confirmer le format d'import (matrice négociant, cellules en litres) et son comportement : tout ou rien, refus d'une valeur en colonne grisée, colonnes calculées ignorées avec avertissement (H-DEC-15). | Import Excel au portail et au back-office | 6 |
| Q-DEC-17 | **Au BNIA** : confirmer que seul un brouillon se supprime, par l'opérateur ou par le BNIA (H-DEC-16). | Suppression ; rejet d'une DSM envoyée si la réponse est non | 1 |
| Q-DEC-18 | **Au BNIA** : confirmer qu'une DSM validée n'est jamais rouverte et que les erreurs se régularisent dans la DSM suivante, par un nouveau point de départ ou par un avoir (H-DEC-17). | Correction après validation ; dévalidation et événement associé si la réponse est non | 2 |
| Q-DEC-19 | **Au BNIA** : confirmer que le BNIA dépose DRM et justificatifs pendant l'instruction, que l'opérateur ne le fait que quand la DSM est chez lui, et qu'une DRM tient en un seul PDF par mois (H-DEC-18). | Dépôt des pièces ; plusieurs fichiers par mois si la réponse est non | 3 |
| Q-DEC-20 | **Au BNIA** : confirmer la prise en instruction explicite, le motif obligatoire des compléments et les commentaires internes au BNIA (H-DEC-19). | Cycle de la DSM, écran d'instruction | 2 |
| Q-DEC-21 | **Au BNIA** : confirmer la grille CVO sans trou, la cotisation figée par la facture, et le calcul séparé des deux parts (H-DEC-20). | Grille de cotisation, calcul et historique des montants | 4 |
| Q-DEC-22 | **Au BNIA** : confirmer le contenu des éditions Excel (matrice négociant réimportable, onglet « Cotisation ») et PDF, et leur disponibilité dans tous les états avec mention provisoire (H-DEC-21). | Éditions et modèle d'import | 5 |
| Q-DEC-23 | **Au BNIA** : quelle classe de TVA s'applique à la `CVO` ? Confirmer la TVA indicative sur la seule part assujettie (H-DEC-22). | TVA et TTC indicatifs avant facturation | 4 |
| Q-DEC-24 | **Au BNIA** : un opérateur doit-il pouvoir ouvrir sa DSM avant le premier jour de la période (H-DEC-23) ? | Initiation de la DSM | 1 |
| Q-DEC-25 | **Au BNIA** : combien d'opérateurs déclarent au trimestre ou au semestre, et à quelle fréquence change-t-on de périodicité (§2.2) ? | Retour de ces périodicités, hors périmètre du prototype | hors lots |
| Q-DEC-26 | **Au BNIA** : confirmer le grain du stock estimé (millésime, ou assemblage et compte d'âge, fût et bouteille confondus) et l'usage du bilan prévisionnel à la validation d'un contrat (§5.2, analyse §3). | Stock estimé et bilan prévisionnel, specs 06 et 09 | hors lots |

## 11. Tests attendus

Chaque RG-DEC-nn a au moins un test qui porte son identifiant (CLAUDE.md), livré avec son lot (§4).
Chaque cas d'usage a son test de refus de droits (ADR-016).
- **Tests de domaine**, sans base : RG-DEC-02 (type figé), 03, 04 et 05 (calendrier), 06, 07, 10, 11
  (normalisation des dénominations), 12 (compte `MB` calculé, borne `20+`, contrôle `MV`), 13, 14
  (reprise, vieillissement, fusion), 15, 16, 17, 18 (chaque anomalie et son niveau), 19, 20 (toutes les
  transitions permises et refusées, aucune depuis `VALIDEE`), 21, 22, 24, 25, 26 (compatibilité des
  types), 27, 29 (manques), 30 (chaîne des taux), 31, 32, 33 (exemple de la facture : 100,69 €,
  402,77 €, 270,50 €, 1 081,99 €), 39, 40 (absente, en retard, attente du BNIA).
- **Tests de cas d'usage**, avec repositories simulés, puis IT : RG-DEC-01 (refus par cas d'usage ;
  commentaire interne invisible de l'opérateur), 02 (statut autre que `OUI`), 05 (une seule DSM non
  validée), 08 (propagation à la DSM en cours), 09, 20 (événements publiés), 22 (suppression en
  cascade, trace au journal), 23 (journal de chaque action), 28 (dépôt selon l'état et le côté), 34
  (projection des factures, TVA indicative), 35 (PDF produit, mentions provisoire et colonnes grisées), 36
  (Excel produit puis réimporté à l'identique), 37 (chaque cause de refus, rien d'importé), 38 (lignes
  rapprochées, avertissements, stock initial), 41 (événement émis une seule fois), 42 (stock déclaré
  de tous les opérateurs).

## 12. Lots d'implémentation

Chaque lot est un **incrément** : une tranche verticale (domaine, cas d'usage, persistance, REST, écrans,
données de démonstration) livrable, démontrable et testable seule, build vert. Un lot ne livre que les
RG qui portent son numéro (§4) ; il s'appuie sur les lots précédents, jamais sur les suivants.

| Lot | Incrément | Contenu | Démontrable à la fin du lot |
|---|---|---|---|
| 1 | **Saisie manuelle d'une DSM** | Modules Maven, schéma `declarations`, `Module.DECLARATIONS` (spec 02), règles ArchUnit de la partie publiée (ADR-017). Point de départ saisi à l'écran par le BNIA depuis la fiche opérateur. Initiation de la première DSM, lignes (identité, compte d'âge, ajout, suppression), saisie des 26 colonnes, colonnes grisées, stock de fin, totaux, anomalies montrées en direct, date de DIA. Suppression du brouillon. Journal et historique de la DSM. Écrans : portail (mes DSM, saisie), back-office (point de départ, consultation). RG-DEC-01 à 08, 10 à 13, 15 à 19, 22, 23. | Le BNIA fixe le point de départ d'un négociant ; l'opérateur initie sa DSM, la remplit, voit ses anomalies et l'enregistre en brouillon. |
| 2 | **Envoi, instruction, validation** | Cycle complet, commentaires (internes compris), listing du BNIA filtré par état, DSM suivante avec reprise et vieillissement au 1er avril. Événements `DsmEnvoyee`, `ComplementsDemandes`, `DsmValidee` (sans écouteur, ADR-013). RG-DEC-14, 20, 21, 24, 25. | Un négociant envoie mai ; le BNIA demande un complément, l'opérateur renvoie, le BNIA valide ; juin s'initie avec le stock de fin de mai. |
| 3 | **Justificatifs et DRM** | Stockage de documents commun (A-DEC-5), dépôt, consultation, suppression ; mois attendus des DRM ; manques bloquant la validation. RG-DEC-26 à 29. | Un producteur dépose ses 12 DRM et ses pièces export ; la validation est refusée tant qu'un mois manque. |
| 4 | **Cotisation** | Grille CVO dans le menu Paramétrage ; assiette, détail par ligne de cotisation, total HT et TVA indicative sur la DSM et le listing ; projection des factures (`FactureEmise`, `AvoirEmis`) ; lecture publiée de la cotisation pour `facturation`. RG-DEC-30 à 34. | Le BNIA saisit 60 €/hlAP et 20 % ; la DSM de l'exemple affiche 100,69 € + 402,77 € en sorties France. |
| 5 | **Édition PDF** | PDF de la DSM, dans tous les états. RG-DEC-35. | L'opérateur et le BNIA téléchargent le PDF d'une DSM provisoire puis validée. |
| 6 | **Excel : édition et import** | Édition Excel réimportable ; import tout ou rien sur une DSM et sur un stock initial. RG-DEC-36 à 38. | L'opérateur télécharge son brouillon en Excel, le remplit, le réimporte ; un fichier erroné est refusé avec ses erreurs. |
| 7 | **Échéances et retards** | Échéances, période attendue, DSM absentes et en retard dans le listing ; traitement planifié et `DeclarationEnRetard` ; nouveau point de départ après une interruption. RG-DEC-09, 39 à 41. | Le listing montre les opérateurs en retard ; l'événement part le lendemain de l'échéance ; un opérateur sorti de sommeil repart d'un nouveau point de départ. |
| 8 | **Stocks déclarés** | Stock déclaré d'un opérateur et de tous (UC-DEC-1) ; lecture publiée de la dernière DSM et du stock déclaré (fiche opérateur). RG-DEC-42. | Le BNIA exporte l'état des stocks déclarés pour les douanes. |

Hors lots de ce contexte : stock estimé et bilan prévisionnel (avec `contratsarmagnac` et
`distillation`), facturation (UC-DEC-10, avec `facturation`), envoi des notifications (avec
`notifications`), import XML/JSON (différé).

## Journal

| Date | Changement |
|---|---|
| 2026-10-13 | Spec créée (phases A et B) : objet et périmètre. Décisions de périmètre : grille `CVO` administrée dans le menu Paramétrage ; édition Excel et PDF ; DRM supprimée puis redéposée, sans historique ; journal des actions sans valeurs avant et après ; stock estimé et bilan prévisionnel en contrat d'interface, livrés avec 6 et 9 ; partie publiée réduite aux lecteurs réels ; justificatif = pièce déposée typée, sans lien vers facture ou avoir ; pointage Floc soumis au BNIA (Q-DEC-1). |
| 2026-10-13 | Phase C, premier temps : 19 sujets tranchés, consignés en hypothèses H-DEC-1 à 21, chacune avec sa question au BNIA (Q-DEC-2 à 22) ; périodicités autres qu'annuelle et mensuelle hors périmètre (§2.2). Modèle (§3 : 26 colonnes, DSM, point de départ, journal, grille, politiques, module « Déclarations »), règles RG-DEC-01 à 42 avec leur lot, lots d'implémentation (§12). Écarts à l'analyse reportés dans `analyse.md`. |
| 2026-10-13 | Phase C, second temps : cas d'usage avec leurs RG et leur lot (§5.1) ; partie publiée, port facture, événements publiés et contrat d'interface du stock estimé (§5.2) ; écrans du back-office, du portail et de la DSM (§6) ; données de démonstration par lot (§7.1) ; tests attendus (§11). |
| 2026-10-13 | Phase D : `contextes.md` mis à jour (ordre et bordures, fiches 2, 5, 6, 8, 9, 11, 12, éléments transverses, context map, interrogations, journal). |
| 2026-10-13 | Phase E : décisions A-DEC-1 à 9 (§8). Factures en projection locale (écart à l'analyse, reporté) ; RG-DEC-34, §2.1, §3.6, §5.1, §5.2 et lot 4 alignés ; `VolumeAlcoolPur` signé, dans `commun.domain`. `contextes.md` mis à jour. |
| 2026-10-13 | Phase F : H-DEC-6 réalignée sur RG-DEC-40 ; H-DEC-22 et 23, Q-DEC-23 à 26 ajoutées ; colonne « Lot » des questions (ordre de relance du BNIA). |
| 2026-10-13 | Phase G : relecture (renvois « phase E » remplacés par A-DEC-n, pages de §5.12 et §7, §4.4.3 pour le LAP, projection `FactureDsm` et `retard_signale` dans le modèle, lecture de `referentiels`) ; code `ControleCoherence` du glossaire repris. Glossaire mis à jour. |
| 2026-10-13 | Spec validée par l'utilisateur : statut « terminée ». |
| 2026-10-13 | Phase H : ADR-017 (partie publiée de `declarations`), ADR-018 (stockage de documents), ADR-019 (traitements planifiés), ADR-020 (production de documents), amendement d'ADR-009 ; colonne ADR du §8 à jour ; CLAUDE.md mis à jour ; spec 02 révisée. Règles ArchUnit à adapter au lot 1. |
| 2026-10-13 | Implémentation, lot 1 : décisions en séance. Anomalies montrées dès la saisie par une **vue provisoire** calculée par le serveur (`POST …/dsm/{id}/apercu`, sans enregistrement ni journal, appelée 300 ms après la dernière frappe) : les règles restent dans le domaine, le front ne fait que les sommes d'affichage. La date de début d'un point de départ ne change pas tant qu'une DSM est en cours (RG-DEC-08 complétée). Choix d'implémentation : une seule API pour les deux espaces (`/api/operateurs/{operateur}/…`, les droits font la différence) ; « Enregistrer » envoie toute la saisie (lignes, volumes, date de DIA, version) ; volumes en litres, nombres JSON à 2 décimales, tableaux de 26 valeurs ; identifiant de ligne de DSM stable d'un enregistrement à l'autre ; un seul point de départ fixé par opérateur au lot 1 (un second est refusé, 409 : le nouveau point de départ vient au lot 7) ; anomalie des colonnes croisées au niveau de l'onglet des entrées. |
| 2026-10-13 | Implémentation, lot 1, revue de l'écran de la DSM par l'utilisateur : les cinq onglets du format unique en classeur (le cadre de la saisie et ses languettes), chacun titré et situé par une phrase ; l'historique sort des onglets pour un panneau latéral ; les contrôles des colonnes croisées ne se montrent qu'en anomalie, dans le panneau du haut, et plus en permanence au récapitulatif ; la date de DIA passe du récapitulatif à l'onglet *Mouvements en sortie*, celui des pertes sur inventaire (§6.3). |
| 2026-10-13 | Implémentation, lot 2 : décisions en séance. Commentaires dans une **section sous le classeur**, pas dans un onglet (§6.3) ; ceux d'une ligne ajoutée puis supprimée **passent sur la DSM** avec le libellé de la ligne ; **« Envoyer » enregistre puis envoie**, après confirmation, désactivé avec son motif tant qu'il reste une anomalie ; démonstration des négociants au **mois courant** (§7.1). Choix d'implémentation : commentaires enregistrés à part de la DSM (les écrire ne change pas sa version) ; actions permises calculées par le serveur selon l'état et le côté, montrées telles quelles par l'écran ; chaque transition envoie la version lue (409 sinon) ; listing du BNIA en onglets *En cours*, *Validées*, *Facturées* (revue de l'utilisateur, au lieu du filtre par défaut « envoyées »), filtré par opérateur (recherche de `operateurs`), type, mois contenu dans la période et états ; un compte ADMIN de démonstration pour Maison Larressingle et Négoce des Landes. |
| 2026-10-13 | Implémentation, lot 2, revue de l'utilisateur : nouvel état `COMPLEMENTS_RECUS` au renvoi après compléments (auparavant `ENVOYEE`), d'où le BNIA demande de nouveau des compléments ou valide, sans reprise en instruction ; libellés d'état selon le côté, fournis par le serveur : `ENVOYEE` « Envoyée » / « À prendre en charge », `COMPLEMENTS_RECUS` « Compléments envoyés » / « Compléments reçus » (RG-DEC-20, 40, H-DEC-6, 19, §3.2, §6.3). |
| 2026-10-13 | Implémentation, lot 2, revue de l'utilisateur : dans l'onglet *En cours* du listing du BNIA, « modifiée le » (date de la dernière entrée du journal de la DSM, aucune colonne ajoutée) remplace « envoyée le », et « Prendre en instruction » remplace « validée le », sur les DSM à prendre en charge et pour qui gère le module au BNIA (§6.1). |
| 2026-10-13 | Implémentation, lot 3 : choix d'implémentation. Pièces (justificatifs, DRM) enregistrées **à part de la DSM**, comme les commentaires : un dépôt ne change pas sa version. Le `DocumentDepose` (clé, nom, type, taille) est copié dans `justificatif` et `drm`, avec clé étrangère vers `documents.document` (ADR-011, 018) ; le stockage vérifie 10 Mo et la signature `%PDF` (`DocumentRefuse`, 422), `spring.servlet.multipart` est réglé à 10 Mo. Justificatif d'une ligne ajoutée puis supprimée : passe sur la DSM avec le libellé de la ligne. Dépôt en `multipart/form-data` ; PDF servi `inline`, ouvert dans un nouvel onglet, lisible par tout lecteur de la DSM, observateur compris. Manques calculés dans la vue de la DSM (deux lectures, jamais une par ligne) ; « Valider » désactivé avec son motif ; la confirmation d'envoi les signale. Démonstration : douze DRM et un DAE sur la DSM validée de la Maison Durand, Condom ; aucune pièce sur Gascogne et Larressingle, dont les manques bloquent la validation (§6.3, §7.1). |
| 2026-10-13 | Revue de l'utilisateur : le bouton « Prendre en instruction » devient « Prendre en charge », au listing du BNIA comme sur l'écran de la DSM, en écho à l'état « À prendre en charge » ; l'état atteint reste « En instruction ». Depuis le listing, il ouvre ensuite la DSM prise en charge (§6.1, §6.3). |
