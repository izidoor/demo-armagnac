# 08 : facturation

> Contexte `facturation` (étape 8 de `docs/contextes.md`). Statut : **implémentée** (2026-10-17 ; spec terminée le
> 2026-10-14). Lot 4 (avoirs) hors prototype ; lot 6 (Zeendoc) réduit à une simulation (§12).
> Références : `p.N` = page du CDC v1.1, `AGI p.N` = page de la spec de reprise AGI.
> `H-FAC-n` : hypothèse retenue, révisable. `Q-FAC-n` : question ouverte, suivie dans cette spec (§10).
> Précédée de l'analyse du contexte ([analyse.md](analyse.md), terminée le 2026-10-14, citée « analyse »),
> dont les conclusions (cadrage, cas d'usage `UC-FAC-n`, objets, flux, amendements au CDC, révisions de
> l'existant) sont acquises.

## Sommaire

- [1. Objet](#1-objet)
- [2. Périmètre](#2-périmètre)
  - [2.1 Dans le contexte](#21-dans-le-contexte)
  - [2.2 Hors du contexte](#22-hors-du-contexte)
- [3. Modèle](#3-modèle)
  - [3.1 Pièce (agrégat `Facture`, lot 1)](#31-pièce-agrégat-facture-lot-1)
  - [3.2 Lignes (`LigneFacture`, entités de la pièce, lot 1)](#32-lignes-lignefacture-entités-de-la-pièce-lot-1)
  - [3.3 Catégorie de TVA (`CategorieTva`, valeur publiée, lot 1)](#33-catégorie-de-tva-categorietva-valeur-publiée-lot-1)
  - [3.4 Origine, rattachement (valeurs, lots 1 et 4)](#34-origine-rattachement-valeurs-lots-1-et-4)
  - [3.5 Acheteur (`Acheteur`, valeur, lot 1)](#35-acheteur-acheteur-valeur-lot-1)
  - [3.6 Condition de paiement (`ConditionPaiement`, valeur publiée, lot 1)](#36-condition-de-paiement-conditionpaiement-valeur-publiée-lot-1)
  - [3.7 Vendeur (`Vendeur`, agrégat de paramétrage, lot 1)](#37-vendeur-vendeur-agrégat-de-paramétrage-lot-1)
  - [3.8 Numérotation (`Numerotation`, agrégat, lot 1)](#38-numérotation-numerotation-agrégat-lot-1)
  - [3.9 Journal (`Journal`, hors agrégat de la pièce, en ajout seul, lot 1)](#39-journal-journal-hors-agrégat-de-la-pièce-en-ajout-seul-lot-1)
  - [3.10 Suivi Zeendoc (`SuiviZeendoc`, valeur de la pièce, lot 6, simulé)](#310-suivi-zeendoc-suivizeendoc-valeur-de-la-pièce-lot-6-simulé)
  - [3.11 Partie publiée et ports de l'hôte](#311-partie-publiée-et-ports-de-lhôte)
  - [3.12 Droits : le module « Facturation » (révision de la spec 02 §3.2.7)](#312-droits--le-module--facturation--révision-de-la-spec-02-327)
  - [3.13 Révisions de l'existant livrées par cette spec](#313-révisions-de-lexistant-livrées-par-cette-spec)
- [4. Règles de gestion](#4-règles-de-gestion)
- [5. Cas d'usage](#5-cas-dusage)
  - [5.1 Écriture](#51-écriture)
  - [5.2 Lecture](#52-lecture)
- [6. Écrans](#6-écrans)
  - [6.1 Back-office (opérateur actif : BNIA)](#61-back-office-opérateur-actif--bnia)
  - [6.2 Portail (opérateur actif : producteur ou négociant)](#62-portail-opérateur-actif--producteur-ou-négociant)
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

Le BNIA vit de la cotisation interprofessionnelle (`CVO`) et vend quelques articles aux opérateurs.
`facturation` est une **lib en aval** (analyse §1) : les contextes métier lui demandent des factures,
elle en fait des pièces légales.

- Elle reçoit des **demandes de facture** (origine, acheteur, lignes d'article), qui deviennent des
  **brouillons** que le BNIA relit, complète, abandonne ou émet, seuls ou en lot (analyse UC-FAC-1, 2).
- Elle garantit les invariants de la pièce : numérotation continue, commune aux factures et aux avoirs ;
  facture émise jamais modifiée ni supprimée, corrigée par **avoir** ; mentions obligatoires ; une
  catégorie de TVA par ligne (analyse §3).
- Elle édite le **PDF** selon un nouveau modèle, le met à disposition au back-office et au portail, le
  dépose dans **Zeendoc** et en restitue l'état de règlement (§4.9.8 p.46, §5.10 p.74, §6.2 p.83).
- Elle tient le **journal** de chaque pièce et consolide les **ventes** (analyse UC-FAC-4, 8).

Elle ne connaît ni DSM, ni cotisation, ni commande : une ligne lui arrive avec un article, que la source
résout par le port `CatalogueArticles` que publie la lib. Sa valeur tient dans des pièces **légalement
sûres**, émises **sans ressaisie**, et dans un seul endroit où retrouver toutes les factures, quelle que
soit leur origine.

Sources : §4.9 (p.44-46) · §5.10 (p.74) · §6.2 (p.83) · C.8 (p.123-126) · annexe D (p.138-139) · annexe E
(p.140) · AGI 2.3 (p.9-10) · factures d'exemple `docs/source/fichiers-example/FACTURE-CVO-Armagnac_2026000700.pdf`,
`FACTURE-CVO_DISTILLATION_2026000249.pdf`, `FACTURE_CVO-PROVISION_202603-00002.pdf` · récapitulatif des
cotisations transmis par le BNIA (2026-10-14).
Usages relevés ailleurs : §3 (vision, p.10-12) · §4.1 (DSM en attente de facturation, p.12) · §4.2.5.1
(modules DSM, Boutique, Facturation, p.15-16) · §4.3.3 (historique des factures de la fiche opérateur,
p.20) · §4.4.9 et §4.4.10 (facturation et historique de la DSM, p.28-29) · §4.6.2 (fin de travaux,
p.36) · §4.10.3 (CVO facturée, p.48) · §4.11.1 et §4.11.2 (TVA et cotisations, p.52) · §4.13 (boutique,
p.57-58) · §5.9.3 (facture d'une commande, p.73) · §5.12 (avis de nouvelle facture, droit « accès aux
factures », p.77-78) · §9.3.1 et §9.4 (socle initial, Zeendoc enrichi plus tard, p.92-93) · C.3.1.1 et
C.3.3.1 (DSM, p.107-111) · C.5.1.3 (code payeur, état de facturation, p.119) · C.10.1 à C.10.3
(p.127-128) · C.12 (p.133-134) · C.14 (p.136).

## 2. Périmètre

### 2.1 Dans le contexte

| Famille | Élément | Code (ADR-001) | Source |
|---|---|---|---|
| Pièce | Facture et avoir en un seul agrégat, qualifiés par leur sens (codes 380, 381, 262) ; brouillon puis émise | `Facture`, `SensPiece` | §4.9 p.44-46, C.8.3 et C.8.4 p.124-126, analyse §3 |
| Pièce | Lignes ordonnées : article, texte informatif, groupe avec sous-total (un niveau) | `LigneArticle`, `LigneInformative`, `LigneGroupe` | C.8.3.2 p.125, analyse §3 (XP Z12-012) |
| Pièce | Origine d'une facture, fournie par la source : source, nature et son libellé, identifiant opaque, référence affichable ; remplace le type de facture | `Origine` | C.8.3.1 p.124, analyse §3 |
| Pièce | Rattachement d'un avoir : une ou plusieurs factures émises, ou un contrat et une période ; motif obligatoire | `Rattachement` | §4.9.7 p.46, C.8.4.1 p.125, analyse §3 |
| Pièce | Acheteur recopié (catégorie, identifiants légaux, adresses, e-mail), avec un client opaque de l'hôte facultatif | `Acheteur`, `ClientRef` | C.8.1.1 p.123, analyse §3 |
| Pièce | Vendeur (le BNIA), paramétré dans la lib une fois, recopié à l'émission ; mentions d'indemnité, de pénalités et d'escompte générées | `Vendeur` | analyse §3 ; paramètre de la lib : décision du 2026-10-14 |
| Pièce | Numérotation unique des factures et des avoirs, par année, sans trou ni doublon | `Numerotation` | C.8.3.1 p.124, analyse §3 |
| Pièce | PDF au nouveau modèle, conservé dans la plateforme (ADR-018, ADR-020) | — | §4.9.8 p.46 |
| Suivi | Journal de la pièce, en ajout seul, côté vendeur ou client ; carnet de notes internes du BNIA, à part (décision du 2026-10-21) | `Journal`, `EntreeJournal`, `NoteInterne` | analyse §3 |
| Zeendoc | Transmission d'une sélection de pièces émises, une fois chacune, puis état de règlement ; simulé dans le prototype (décision du 2026-10-17 ; dépôt planifié, rejeu, historique des retours et anomalies hors prototype) | `SuiviZeendoc` | §4.9.8 p.46, §6.2 p.83, annexe D p.139 |
| Lecture | Historique des factures et avoirs du BNIA (filtres : numéro, nature d'origine, acheteur ou client, période, date) | — | §4.9.1 p.44, §4.3.3 p.20, analyse UC-FAC-4 |
| Lecture | Factures et avoirs de l'opérateur au portail, état de règlement | — | §5.10 p.74, analyse UC-FAC-5 |
| Lecture | Tableau de tous les articles de tous les catalogues | — | §4.9.2 p.44, analyse UC-FAC-6 |
| Lecture | Ventes consolidées d'une période, avoirs déduits ; export Excel et PDF | `LigneVendue` | analyse UC-FAC-8 |
| API (`bnia-facturation-api`, A-FAC-1) | Cas d'usage appelé par les sources et sa commande | `DemandeFactures`, `DemandeFacture` | analyse §4 |
| API (A-FAC-1) | Port implémenté par chaque source pour ses articles | `CatalogueArticles`, `Article` | analyse §1 et §4 |
| API (A-FAC-1) | Événements émis | `FactureEmise`, `FactureAbandonnee`, `AvoirEmis` | analyse §4, ADR-013 |
| API (A-FAC-1) | Lecture des ventes pour `statistiques` (CVO facturée sur DSM et sur fin de travaux) | `LigneVendue` | §4.10.3 p.48 ; publiée dès cette spec : décision du 2026-10-14 |
| Droits | Module « Facturation » du tableau rôles × modules | `Module.FACTURATION` | §4.2.5.1 p.15-16, analyse §2 et §4, A-FAC-9 |
| Ports de l'hôte | Droits et clients, définis dans le core, implémentés dans l'infra sur la partie publiée d'`operateurs` | `DroitsFacturation`, `Clients` | analyse §4, A-FAC-3 ; décision du 2026-10-14 |
| Révision | `declarations` : demande de facture d'une DSM ou d'un lot (UC-DEC-10), deux articles par ligne de cotisation, `CatalogueArticles`, état « facturation demandée », événements de `facturation`, retrait de `CotisationDsmVue` ; spec 05 et code livrés dans les lots de cette spec | — | analyse, révisions de l'existant ; décision du 2026-10-14 |
| Révision | `operateurs` : n° de TVA, fiche facturable et certifiable, acheteur publié, module Facturation ; spec 02 et code livrés dans le lot 1 | — | analyse, révisions de l'existant ; décision du 2026-10-14 |

### 2.2 Hors du contexte

| Élément décrit dans le CDC | Contexte retenu | Motif |
|---|---|---|
| Déclenchement de la facture d'une DSM, une par une ou en lot (§4.4.9 p.28, §4.9.3 p.44-45) | `declarations` (UC-DEC-10), révisé dans les lots de cette spec | La source demande, la lib facture (analyse §1 ; révision ici : décision du 2026-10-14) |
| Calcul de la cotisation, taux `CVO` et pourcentage exonéré (§4.9.3 p.45, §4.11.2 p.52) | `declarations` | La source résout ses articles, prix et TVA compris (analyse §1) |
| Facture de fin de travaux, calcul et taux (§4.6.2 p.36, §4.9.4 p.45) | `distillation` | Appellera `DemandeFactures` et implémentera `CatalogueArticles` dans sa spec ; seule `declarations` est source ici (décision du 2026-10-14) |
| Facture de commande boutique, état `facturée` de la commande (§4.9.5 p.45, §4.13 p.57-58, §5.9.3 p.73) | `boutique` | Idem ; la commande suit `FactureEmise` |
| Facture manuelle : PLV, goodies, ventes diverses (§4.9.6 p.45-46) | `boutique`, vente directe (amendement) | Une facture a toujours une origine (analyse §1) |
| Référentiel produits de la facturation (§4.9.2 p.44, C.8.2.1 p.124) | chaque source, par `CatalogueArticles` | Chaque article vit avec sa règle (analyse §1, amendement) |
| Tiers de facturation réutilisable, type `Client` (C.8.1.1 p.123) | `boutique` (répertoire des clients libres) | Seul `boutique` vend à des clients libres ; la lib recopie l'acheteur reçu (décision du 2026-10-14) |
| Type de facture en liste fermée (C.8.3.1 p.124, §5.10.1 p.74) | remplacé par `Origine` | Nature et libellé fournis par la source (analyse §3, amendement) |
| Taux de cotisation et pourcentage d'exonération sur la facture (C.8.3.1 p.124) | abandonné | Prix et catégorie de TVA portés par chaque ligne article (analyse §3) |
| Classes et taux de TVA (§4.11.1 p.52, C.10.1-C.10.2 p.127) | `referentiels`, lus par les sources | `facturation` ne lit plus `referentiels` (analyse §4) |
| Fiche « facturable » de l'opérateur (acheteur légalement complet, numéro de TVA) | `operateurs` (révision de RG-OPE-02) | La lib exige un acheteur complet ; la fiche dit si elle peut le fournir (analyse §3) |
| Avis de nouvelle facture (§5.12 p.77) | `notifications` | Écoute `FactureEmise` (analyse §4) |
| Historique des factures sur la fiche opérateur (§4.3.3 p.20) | composition de lecture | Onglet *Factures* de la fiche, fourni par le front de `facturation` (décision du 2026-10-15 ; auparavant un lien vers l'historique filtré) ; `operateurs` est en amont et ne lit pas `facturation` |
| Indicateurs de CVO facturée (§4.10.3 p.48) | `statistiques` | Lit la partie publiée de `facturation` |
| DSM en attente de facturation (§4.1 p.12) | `declarations` | Projection locale des factures (A-DEC-4 révisé) |
| Facture provisionnelle (§4.2.5.1 p.16, annexe E p.140) | en attente du BNIA | Jamais décrite (Q-FAC-3) |
| Encaissement (§5.10.3 p.74), export comptable (§4.9.8 p.46) | aucun | Exclus par le CDC ; Zeendoc s'en charge |
| Reprise des factures InteGraal (AGI 2.3 p.9-10, §7 p.85) | hors périmètre du prototype | Reprise hors périmètre (décision du 2026-10-08) ; la vue AGI ne sert qu'à relever des attributs |

## 3. Modèle

Le modèle part des objets et invariants de l'analyse (§3), précisés en phase C le 2026-10-14. Quatre
écarts à l'analyse, décidés en séance et reportés dans `analyse.md` :
- la TVA se calcule **par catégorie et taux**, et non plus par ligne (§3.2, RG-FAC-05) ;
- l'acheteur d'un opérateur se lit par un port `Clients` de l'hôte, et il est relu à l'émission (§3.5) ;
- un brouillon abandonné est **supprimé** avec son journal (RG-FAC-18) ;
- l'avoir d'**annulation** est distinct de l'avoir de correction (§3.1, RG-FAC-23).

### 3.1 Pièce (agrégat `Facture`, lot 1)

Facture et avoir partagent un même agrégat, distingués par leur sens (analyse §3).

| Champ | Type | Règle | Source |
|---|---|---|---|
| identifiant | `bigint` | Technique | — |
| sens | `FACTURE`, `AVOIR` | Fixé à la création | analyse §3 (codes 380, 381) |
| état | `BROUILLON`, `EMISE` | `BROUILLON` → `EMISE`, sans retour ; un brouillon abandonné est supprimé (RG-FAC-18) | analyse §3 ; décision du 2026-10-14 |
| origine | `Origine` (§3.4) | Obligatoire pour une facture, absente pour un avoir | C.8.3.1 p.124, analyse §3 |
| rattachement | `Rattachement` (§3.4) | Obligatoire pour un avoir, absent pour une facture | §4.9.7 p.46, C.8.4.1 p.125 |
| annulation | booléen | Avoir seulement : vrai pour un avoir d'annulation (RG-FAC-23) | C.8.4.1 p.125 (« Total ») ; décision du 2026-10-14 |
| motif | texte long | Obligatoire pour un avoir | C.8.4.1 p.125 |
| numéro | texte, `AAAA` + 6 chiffres | Attribué à l'émission (RG-FAC-12) ; vide en brouillon | C.8.3.1 p.124, factures d'exemple |
| date d'émission | date | Date du jour à l'émission | C.8.3.1 p.124 |
| date de l'opération | date | Fournie par la source ; date de résolution des articles (H-FAC-2) ; non modifiable en brouillon (RG-FAC-17) | analyse §3 ; décision du 2026-10-15 |
| libellé de la période | texte court, facultatif | « DSM de juillet 2026 », « Campagne 2025/2026 » ; modifiable en brouillon | §4.9.3 p.45, C.8.3.1 p.124 |
| n° de bon de commande | texte court, facultatif | Imprimé s'il est donné | mention obligatoire (analyse §3) |
| condition de paiement | `ConditionPaiement` (§3.6) | Obligatoire ; fournie par la source, modifiable en brouillon | analyse §3 ; décision du 2026-10-14 |
| échéance | date | Calculée à l'émission (RG-FAC-13) | factures d'exemple |
| note d'en-tête, note de pied | texte long, facultatifs | Imprimées au-dessus des lignes et sous les totaux ; fournies par la source, modifiables en brouillon (RG-FAC-17) | AGI 2.3.1 p.10 ; décision du 2026-10-14 |
| acheteur | `Acheteur` (§3.5) | Obligatoire ; complet à l'émission (RG-FAC-10) | C.8.1.1 p.123 |
| vendeur | `Vendeur` (§3.7) | Recopié du paramétrage à l'émission | analyse §3 |
| lignes | liste ordonnée de `LigneFacture` (§3.2) | Au moins une ligne article à l'émission | C.8.3.2 p.125 |
| nature de l'opération | `BIENS`, `SERVICES`, `LES_DEUX` | Déduite des lignes (RG-FAC-08) | mention obligatoire de 2026 (analyse §3) |
| récapitulatif de TVA | liste de (catégorie, taux, motif, base HT, TVA) | Calculé (RG-FAC-05) | EN 16931 BR-CO-17 ; décision du 2026-10-14 |
| total HT, total TVA, total TTC | décimal, 2 décimales | Calculés (RG-FAC-05), TTC ≥ 0 (RG-FAC-06) | C.8.3.1 p.124 |
| PDF | `DocumentDepose` (ADR-018) | Produit et figé à l'émission (RG-FAC-15) | §4.9.8 p.46 |
| créée le, par | date-heure ; acteur (identifiant sans clé étrangère, copie du nom) | Auteur de la demande (ADR-011 amendé) | — |

**Écarts au CDC** : plus de type de facture, remplacé par l'origine ; plus de taux de cotisation ni de
pourcentage d'exonération sur la facture, puisque chaque ligne porte son prix et sa catégorie de TVA
(analyse §3). La date de transmission et l'état Zeendoc forment le suivi Zeendoc de la pièce (§3.10). L'identifiant BNIA
et le numéro restent distincts.

### 3.2 Lignes (`LigneFacture`, entités de la pièce, lot 1)

Une succession ordonnée de trois sortes de lignes (analyse §3, sous-types DETAIL, INFORMATION et GROUP
d'EXTENDED-CTC-FR).

| Sorte | Champ | Type | Règle |
|---|---|---|---|
| toutes | position | entier | Ordre d'impression ; réordonnable en brouillon |
| toutes | numéro | entier | Identité stable de la ligne dans la pièce, attribuée à sa création ; survit aux déplacements |
| toutes | reçue | booléen | Reçue de la source avec la demande : verrouillée (RG-FAC-17) |
| toutes | groupe | référence d'une `LigneGroupe` de la pièce, facultatif | Un seul niveau (RG-FAC-04) |
| `LigneArticle` | article | `ArticleRef` (source, code) | Résolu par `CatalogueArticles` (RG-FAC-03) |
| `LigneArticle` | désignation, unité | texte court | Recopiés de l'article ; la désignation se modifie en brouillon, l'unité seulement sur une ligne ajoutée (RG-FAC-17) |
| `LigneArticle` | nature | `BIEN`, `SERVICE` | Recopiée de l'article |
| `LigneArticle` | code comptable | texte court, facultatif | Recopié de l'article |
| `LigneArticle` | quantité | décimal, 4 décimales, signé | Non nulle (RG-FAC-06) |
| `LigneArticle` | prix unitaire HT | décimal, 4 décimales, signé | Celui de la demande, sinon celui de l'article |
| `LigneArticle` | catégorie de TVA | `CategorieTva` (§3.3) | Recopiée de l'article |
| `LigneArticle` | montant HT | décimal, 2 décimales | Calculé (RG-FAC-05) |
| `LigneInformative` | texte | texte long | Imprimé tel quel, sans montant |
| `LigneGroupe` | titre | texte court | Obligatoire |
| `LigneGroupe` | sous-total HT | décimal, 2 décimales | Calculé : somme des HT de ses lignes article |

**Écart à l'analyse** (décision du 2026-10-14) : une ligne ne porte ni montant de TVA ni TTC. La TVA ne
se calcule que dans le récapitulatif (RG-FAC-05), comme le veut EN 16931.

### 3.3 Catégorie de TVA (`CategorieTva`, valeur publiée, lot 1)

| Champ | Type | Règle | Source |
|---|---|---|---|
| code | `S` (taux normal ou réduit), `Z` (taux zéro), `E` (exonérée), `O` (hors champ) | Sous-ensemble de la liste UNCL5305 retenu pour le prototype | EN 16931 ; analyse §3 |
| taux | décimal, 2 décimales | Strictement positif pour `S`, nul pour `Z` et `E`, absent pour `O` | — |
| motif d'exonération | texte court | Obligatoire pour `E` et `O`, absent sinon ; imprimé dans le récapitulatif | EN 16931 (BT-120) |

### 3.4 Origine, rattachement (valeurs, lots 1 et 4)

| Objet | Champ | Type | Règle |
|---|---|---|---|
| `Origine` | source | texte court | Nom de la source : `declarations`, plus tard `distillation`, `boutique` |
| `Origine` | nature, libellé de la nature | code ; texte court | `DSM`, « Cotisation interprofessionnelle Armagnac » : le libellé titre le PDF et sert de filtre ; la nature est stable, lue par `statistiques` |
| `Origine` | identifiant | texte court, opaque | Identifiant de l'objet chez la source (identifiant de la DSM), sans clé étrangère (analyse §3) ; rendu dans les événements |
| `Origine` | référence | texte court | Affichée : « DSM juillet 2026 » |
| `Rattachement` | factures | liste de (identifiant, numéro, date d'émission) de factures émises | Une au moins, toutes du même acheteur (RG-FAC-22) ; une seule pour une annulation |
| `Rattachement` | ou contrat et période | référence de contrat : le n° BNIA du client de l'avoir (code de son `ClientRef`) ; date de début et date de fin, par défaut du 1er janvier au 31 décembre de l'année en cours | Remise globale (code 262), sans facture visée (RG-FAC-22) ; décision du 2026-10-14 |

### 3.5 Acheteur (`Acheteur`, valeur, lot 1)

| Champ | Type | Règle | Source |
|---|---|---|---|
| client | `ClientRef` (identifiant opaque, code affichable : n° BNIA), facultatif | Présent pour un client de l'hôte (un opérateur), absent pour un client libre | analyse §3 |
| catégorie | `PROFESSIONNEL`, `PARTICULIER` | Toujours `PROFESSIONNEL` pour un opérateur | analyse §3 |
| nom | texte court | Obligatoire (raison sociale) | C.8.1.1 p.123 |
| SIREN | 9 chiffres | Obligatoire pour un professionnel français (RG-FAC-10) | mention obligatoire de 2026 |
| SIRET | 14 chiffres, facultatif | Le SIREN en est tiré s'il est donné | C.1.1.1 |
| n° de TVA intracommunautaire | texte court, facultatif | Format contrôlé (RG-FAC-10) | mention obligatoire |
| adresse de facturation | `Adresse` (lignes 1 et 2, code postal, ville, pays) | Obligatoire, ligne 1, code postal, ville et pays renseignés | C.8.1.1 p.123 |
| adresse de livraison | `Adresse`, facultative | Si elle diffère | mention obligatoire de 2026 |
| e-mail | texte court | Obligatoire : destinataire des factures | C.8.1.1 p.123, RG-OPE-23 |

Un acheteur **avec client** se lit par le port `Clients` (§3.11) : à la demande, puis de nouveau à
l'émission (décision du 2026-10-14). Il ne se modifie pas dans le brouillon, et l'on n'y change pas de
client : on le corrige sur la fiche de l'opérateur (décision du 2026-10-15, RG-FAC-11). La saisie d'un
acheteur **sans client** se définira avec la première source qui en envoie (`boutique`).

### 3.6 Condition de paiement (`ConditionPaiement`, valeur publiée, lot 1)

| Champ | Type | Règle | Source |
|---|---|---|---|
| jours | entier, de 0 à 60 | 0 : à réception | C. com. L441-10 |
| fin de mois | booléen | Avec fin de mois, 45 jours au plus | C. com. L441-10 |

Libellé imprimé : « À réception », « 30 jours », « 30 jours fin de mois ». Pour la `CVO`, `declarations`
envoie « 30 jours fin de mois » (H-FAC-3), qui retrouve les échéances des deux factures d'exemple.

### 3.7 Vendeur (`Vendeur`, agrégat de paramétrage, lot 1)

Le vendeur est unique : le BNIA. Il est paramétré dans la lib (décision du 2026-10-14) et recopié sur
chaque pièce à son émission.

| Champ | Type | Règle | Source |
|---|---|---|---|
| nom | texte court | Obligatoire | factures d'exemple |
| adresse du siège | `Adresse` | Obligatoire | mention obligatoire |
| adresse de facturation | `Adresse`, facultative | Si elle diffère du siège | mention obligatoire de 2026 |
| SIREN, SIRET | 9 et 14 chiffres | Obligatoires, clé de Luhn | factures d'exemple |
| n° de TVA intracommunautaire | texte court | Obligatoire | factures d'exemple |
| IBAN, BIC | texte court | Obligatoires, format contrôlé | factures d'exemple |
| téléphone, e-mail | texte court | Obligatoires | factures d'exemple (`compta@armagnac.fr`) |
| option pour la TVA d'après les débits | booléen | Imprimée si vraie | CGI art. 242 nonies A |
| escompte | texte court | Défaut : « Pas d'escompte pour paiement anticipé » | C. com. L441-9 |
| pénalités de retard | texte court | Défaut : « trois fois le taux d'intérêt légal » | factures d'exemple, C. com. L441-10 |

Sur toute pièce, les mentions de pénalités (PMT), d'indemnité forfaitaire de 40 € (PMD, pour un acheteur
professionnel seulement) et d'escompte (AAB) sont générées (BR-FR-05, analyse §3).

### 3.8 Numérotation (`Numerotation`, agrégat, lot 1)

Il y a une ligne par année : (année, dernier numéro). Un seul compteur sert aux factures et aux avoirs
(analyse §3).

### 3.9 Journal (`Journal`, hors agrégat de la pièce, en ajout seul, lot 1)

| Champ | Type | Règle |
|---|---|---|
| pièce | référence `Facture` | Supprimée avec un brouillon abandonné (RG-FAC-18) |
| date | date-heure | — |
| événement | `DEMANDEE` (facture), `CREEE` (avoir), `MODIFIEE`, `EMISE`, `CONSULTEE`, `ANNULEE`, `AVOIR_EMIS`, `TRANSMISE` (« Transmise à Zeendoc »), `PAYEE` | RG-FAC-20 |
| acteur | `UTILISATEUR` (identifiant sans clé étrangère, copie du nom) ou `TRAITEMENT` (nom du traitement) | ADR-011 amendé |
| côté | `VENDEUR`, `CLIENT` | Fixé par le cas d'usage (analyse §3) |
| détail | texte long, facultatif | Motif d'un rejet, numéro de l'avoir |

**Notes internes** (`NoteInterne`, hors agrégat de la pièce et hors journal, lot 2, révisé le 2026-10-21) :
le carnet du BNIA sur une pièce, comme celui d'une DSM (spec 15 §3.7). L'écrire ne change pas la version
de la pièce.

| Champ | Type | Règle |
|---|---|---|
| pièce | référence `Facture` | Supprimée avec un brouillon abandonné (RG-FAC-18) |
| auteur | identifiant de l'utilisateur, sans clé étrangère, copie du nom | ADR-011 amendé |
| écrite le, modifiée le | date-heure ; la seconde facultative (« modifiée » affiché) | RG-FAC-21 |
| texte | texte long, non vide | RG-FAC-21 |

### 3.10 Suivi Zeendoc (`SuiviZeendoc`, valeur de la pièce, lot 6, simulé)

Réduit pour le prototype (décision du 2026-10-17) : une démonstration simple, sans agrégat `Transmission`,
sans traitement planifié ni historique des retours. Le suivi est porté par la pièce émise ; vide tant
qu'elle n'est pas transmise.

| Champ | Type | Règle | Source |
|---|---|---|---|
| état | `TRANSMISE` (« Transmis »), `PAYEE` (« Payé ») | RG-FAC-32, 33 | §6.2.3 p.83, §5.10.3 p.74 |
| transmise le | date-heure | RG-FAC-32 | C.8.3.1 p.124 |
| payée le | date | RG-FAC-33 | §5.10.3 p.74 |

Hors prototype : les états de dépôt `EN_ATTENTE`, `REJETEE` et `EN_ANOMALIE`, le règlement `EN_ATTENTE`,
l'historique des retours de Zeendoc.

### 3.11 Partie publiée et ports de l'hôte

Les éléments publiés vivent dans le module `bnia-facturation-api` (A-FAC-1) ; les ports de l'hôte dans
le core de `facturation`, implémentés dans son infra (A-FAC-3).

| Élément | Nature | Contenu | Lot |
|---|---|---|---|
| `DemandeFactures` | cas d'usage appelé par les sources | `demander(acteur, demandes)` : crée un brouillon par demande, rend leurs identifiants ; tout ou rien (RG-FAC-01) | 1 |
| `DemandeFacture` | commande | Origine ; client (`ClientRef`) ou acheteur libre ; date de l'opération ; libellé de la période ; condition de paiement ; n° de bon de commande ; notes d'en-tête et de pied ; lignes ordonnées : article (source, code, quantité, prix facultatif, désignation facultative), texte, groupe (titre, lignes) | 1 |
| `CatalogueArticles` | port implémenté par chaque source | `source()`, `articles(date)`, `article(code, date)` | 1 |
| `Article` | valeur | Source, code, désignation, unité, prix unitaire HT à la date, `CategorieTva` à la date, nature, code comptable facultatif, actif, lien d'administration | 1 |
| `FactureEmise` | événement | Pièce, origine, client, numéro, date d'émission, HT, TVA, TTC | 1 |
| `FactureAbandonnee` | événement | Pièce, origine, client, acteur qui l'a abandonné (pour le journal de la source, comme `FactureEmise`) | 2 |
| `AvoirEmis` | événement | Pièce, numéro, date, annulation, factures rattachées (pièce, numéro, origine), HT, TVA, TTC | 4 (hors prototype) |
| Lecture des ventes | partie publiée en lecture | `LigneVendue` d'une période, filtrée par nature d'origine (RG-FAC-31) | 5 |
| `DroitsFacturation` | port de la lib, implémenté dans son infra sur `operateurs` | L'acteur gère-t-il la facturation ? Quels clients lit-il ? (RG-FAC-26) | 1 |
| `Clients` | port de la lib, implémenté dans son infra sur `operateurs` | `acheteur(client)` : l'acheteur et ce qui lui manque pour être complet ; `rechercher(texte)` | 1 |
| `Zeendoc` | port de la lib, adapter simulé dans son infra (bordure) | `deposer(numero, pdf)` : le numéro sert de clé d'idempotence (RG-FAC-32) | 6 |

### 3.12 Droits : le module « Facturation » (révision de la spec 02 §3.2.7)

| Rôle | Facturation |
|---|---|
| `OBSERVATEUR` | lecture |
| `AGENT` | lecture ; gestion s'il est tenu dans l'opérateur BNIA |
| `ADMIN` | lecture ; gestion s'il est tenu dans l'opérateur BNIA |

Comme le Paramétrage, la gestion est réservée au personnel du BNIA : un ADMIN d'un négociant ne
facture pas. La lecture est celle de son opérateur : les pièces émises dont il est le client (analyse
§2, RG-FAC-26).

### 3.13 Révisions de l'existant livrées par cette spec

**`declarations`** (spec 05 et code, lots 1, 2 et 4 ; décision du 2026-10-14) :

| Élément | Aujourd'hui | Après |
|---|---|---|
| UC-DEC-10 | Porté par `facturation` | Dans `declarations` : facturer une DSM ou une sélection de DSM validées, sans facture vivante, d'un opérateur facturable ; une `DemandeFacture` par DSM, envoyée par le port `Facturation` de `declarations`, implémenté dans son infra sur l'API (A-FAC-1, §5.1) |
| Articles de cotisation | Une `LigneCotisation` à deux parts (RG-DEC-33) | `declarations` implémente `CatalogueArticles` : deux articles par ligne de cotisation, `CVO-<ligne>-S` (part assujettie, classe de TVA de la `CVO` à la date, H-DEC-22) et `CVO-<ligne>-E` (part non assujettie, exonérée, H-FAC-1) ; prix = montant × (1 − pourcentage), ou montant × pourcentage, du taux en vigueur à la date ; unité hlAP ; nature `SERVICE` ; lien vers la grille de cotisation |
| Demande d'une DSM | — | Origine (`declarations`, `DSM`, « Cotisation interprofessionnelle Armagnac », identifiant de la DSM, « DSM juillet 2026 ») ; client = opérateur ; date de l'opération = fin de période ; condition « 30 jours fin de mois » ; un groupe par famille de lignes de cotisation (commerce de l'Armagnac ; EDV pour un autre produit), deux lignes article par ligne de cotisation de volume non nul ; note de pied CVO (texte de la facture d'exemple) |
| Projection `FactureDsm` (RG-DEC-34) | Facture émise, avoirs | En plus : brouillon demandé (retour de `demander`), effacé par `FactureAbandonnee` ; facture annulée par un avoir d'annulation. « À facturer » = validée sans facture vivante ; montants nets des avoirs |
| Partie publiée | `FactureEmise`, `AvoirEmis`, `CotisationDsmVue` | Retirés : les deux événements viennent de la partie publiée de `facturation`, et `CotisationDsmVue` n'est plus lue |
| Journal de la DSM | — | Facturation demandée, facture émise, abandonnée, annulée (§4.4.10 p.29) |

**`operateurs`** (spec 02 et code, lot 1 ; décision du 2026-10-14) :

| Élément | Aujourd'hui | Après |
|---|---|---|
| Fiche | SIRET facultatif ; pas de n° de TVA | N° de TVA intracommunautaire facultatif : `FR`, une clé de 2 chiffres, le SIREN ; clé contrôlée ; avertissement s'il ne concorde pas avec le SIRET |
| RG-OPE-02 | Fiche « incomplète » sans adresse ligne 1, code postal, ville, pays ou région | Deux indicateurs, chacun nommant ce qui manque. **Facturable** : SIRET, adresse ligne 1, code postal, ville, pays, destinataire des factures résolu. **Certifiable** : adresse ligne 1, code postal, ville, pays, région |
| Partie publiée | Adresse, pays, e-mail du destinataire des factures | Acheteur d'un opérateur (identité, SIREN, n° de TVA, adresse, e-mail, ce qui manque) ; recherche d'opérateurs pour le port `Clients` |
| §3.2.7 | Quatre modules | Cinquième module : Facturation (§3.12) |

## 4. Règles de gestion

| Id | Règle | Source | Lot |
|---|---|---|---|
| RG-FAC-01 | Une demande de facture exige que l'acteur gère la facturation (RG-FAC-26). Chaque demande crée un brouillon de sens `FACTURE`, journalisé `DEMANDEE` côté vendeur. Une demande en lot réussit ou échoue tout entière, et l'erreur nomme la demande en cause. | analyse UC-FAC-1 ; décision du 2026-10-14 | 1 |
| RG-FAC-02 | Une origine (source, identifiant) n'a jamais plus d'une facture **vivante** : un brouillon, ou une facture émise qui n'est pas annulée. Une demande pour une origine qui en a déjà une est refusée. | analyse §3 ; décision du 2026-10-14 | 1 |
| RG-FAC-03 | Une ligne article se résout par le `CatalogueArticles` de sa source, à la date de l'opération (H-FAC-2). Désignation, unité, nature, code comptable et catégorie de TVA sont recopiés ; le prix est celui de la demande s'il est donné, sinon celui de l'article. Un article inconnu, ou inactif, est refusé. | §4.9.5 p.45, analyse §3 | 1 |
| RG-FAC-04 | Les lignes d'une pièce sont ordonnées. Un groupe a un titre et contient des lignes article ou informatives, jamais un autre groupe ; une ligne appartient au plus à un groupe. Le sous-total d'un groupe est la somme des HT de ses lignes article. | XP Z12-012, analyse §3 | 1 |
| RG-FAC-05 | HT d'une ligne = quantité × prix unitaire, arrondi au centime, demi supérieur. Le récapitulatif de TVA regroupe les lignes article par (catégorie, taux) ; sa base est la somme des HT, et sa TVA = base × taux, arrondie au centime, demi supérieur. Total HT = somme des HT des lignes article ; total TVA = somme des TVA du récapitulatif ; TTC = HT + TVA. | EN 16931 BR-CO-17 ; décision du 2026-10-14 (écart à l'analyse et à RG-DEC-33) | 1 |
| RG-FAC-06 | La quantité d'une ligne article n'est jamais nulle. Quantité et prix peuvent être négatifs (remise), mais le total TTC d'une pièce est positif ou nul. | décision du 2026-10-14 | 1 |
| RG-FAC-07 | Une catégorie de TVA a un taux strictement positif pour `S`, nul pour `Z` et `E`, aucun pour `O` ; `E` et `O` ont un motif. Une pièce qui a une ligne `O` n'a que des lignes `O`. | EN 16931 (BR-O), analyse §3 | 1 |
| RG-FAC-08 | La nature de l'opération vaut `BIENS` si toutes les lignes article sont des biens, `SERVICES` si toutes sont des services, `LES_DEUX` sinon. | mention obligatoire de 2026 (analyse §3) | 1 |
| RG-FAC-09 | Émettre un brouillon, c'est vérifier dans l'ordre : au moins une ligne article ; un acheteur complet (RG-FAC-10, relu selon RG-FAC-11) ; un vendeur complet (§3.7) ; RG-FAC-06 et 07. Un refus nomme ce qui manque. L'émission attribue ensuite le numéro (RG-FAC-12), la date d'émission (le jour même) et l'échéance (RG-FAC-13) ; elle recopie le vendeur, produit le PDF (RG-FAC-15), journalise `EMISE` et publie `FactureEmise`, ou `AvoirEmis` pour un avoir, dans la même transaction. | §4.9.8 p.46, analyse §3, ADR-013 | 1 |
| RG-FAC-10 | Un acheteur est complet s'il a un nom, une adresse de facturation (ligne 1, code postal, ville, pays) et un e-mail. Un professionnel dont le pays est la France a en outre un SIREN (9 chiffres, clé de Luhn). Le n° de TVA, s'il est donné, a le format `FR` + clé + SIREN, avec une clé juste ; une adresse de livraison, si elle est donnée, est complète. | C.8.1.1 p.123, mentions obligatoires (analyse §3) | 1 |
| RG-FAC-11 | Un acheteur avec client se lit par le port `Clients` à la demande et de nouveau à l'émission ; il ne se modifie pas dans le brouillon, et l'on n'y change pas de client : il se corrige sur la fiche de l'opérateur. La saisie d'un acheteur sans client se définira avec la première source qui en envoie (`boutique`). | décisions du 2026-10-14 et du 2026-10-15 | 1 |
| RG-FAC-12 | Le numéro est l'année d'émission suivie de 6 chiffres. Les factures et les avoirs suivent une seule séquence par année, continue, sans trou ni doublon ; le numéro est pris dans la transaction d'émission. | BOI-TVA-DECLA-30-20-20-10, factures d'exemple, analyse §3 | 1 |
| RG-FAC-13 | Échéance = date d'émission + nombre de jours, puis, si la condition le dit, le dernier jour de ce mois. Le nombre de jours va de 0 à 60, et de 0 à 45 avec fin de mois. | C. com. L441-10, factures d'exemple ; décision du 2026-10-14 | 1 |
| RG-FAC-14 | Une pièce émise ne se modifie ni ne se supprime. Elle ne reçoit plus que des entrées de journal (consultations, retours de Zeendoc) et des notes internes, et se corrige par avoir. | §4.9.7 p.46, CGI art. 289 (analyse §3) | 1 |
| RG-FAC-15 | Le PDF d'une pièce reprend : le vendeur et ses mentions générées (§3.7) ; l'acheteur ; le titre (libellé de la nature d'origine, ou « Avoir ») ; le numéro ; la date d'émission ; la date de l'opération ; la période ; l'échéance et la condition de paiement ; la référence de l'origine, ou pour un avoir les factures rattachées (numéro et date) ou le contrat et la période, avec le motif ; le n° de bon de commande ; la nature de l'opération ; la note d'en-tête ; les lignes (désignation, quantité, unité, prix unitaire HT, HT, taux) ; les titres et sous-totaux des groupes ; le récapitulatif de TVA, avec les motifs d'exonération ; les totaux ; la note de pied. Il est produit une fois, à l'émission, puis conservé (ADR-018, ADR-020). Un brouillon a un **aperçu** de même mise en page, recalculé à chaque demande, ni conservé ni journalisé : bandeau « BROUILLON · PROJET DE FACTURE » (ou « D'AVOIR »), filigrane, « n° attribué à l'émission », date d'émission et échéance « à l'émission », vendeur paramétré à cet instant (ou « Vendeur non paramétré ») ; une pièce émise n'a pas d'aperçu, elle a son PDF (RG-FAC-14). | §4.9.8 p.46, mentions obligatoires (analyse §3) | 1 |
| RG-FAC-16 | Le vendeur se paramètre une fois. Le modifier ne change aucune pièce émise. | analyse §3 ; décision du 2026-10-14 | 1 |
| RG-FAC-17 | Dans un brouillon se modifient le libellé de la période, la condition de paiement, le n° de bon de commande et les notes. La date de l'opération et l'acheteur ne se modifient pas (RG-FAC-11) : pour les corriger, on abandonne le brouillon et l'on refait la demande depuis la source. Une ligne **reçue** de la source garde son article, sa quantité, son unité, son prix et son groupe, et ne se retire pas ; seules sa désignation (ou le titre du groupe) et sa position changent. Les lignes **ajoutées** (article de n'importe quel catalogue, résolu à la date de l'opération ; texte ; groupe) se modifient, se retirent et se réordonnent librement. L'en-tête et les lignes s'enregistrent d'un coup, sur la version affichée ; chaque enregistrement est journalisé `MODIFIEE`. | §4.9.3 p.45 ; analyse UC-FAC-2 ; décisions du 2026-10-15 | 2 |
| RG-FAC-18 | Abandonner un brouillon le supprime avec son journal et ses notes internes, et publie `FactureAbandonnee` avec son origine ; la source peut refaire la demande. Un brouillon d'avoir s'abandonne de même, sans événement. Une pièce émise ne s'abandonne pas (RG-FAC-14). | analyse UC-FAC-2 ; décision du 2026-10-14 | 2 |
| RG-FAC-19 | Une émission en lot émet chaque brouillon dans sa propre transaction, dans l'ordre de la sélection. Elle rend un compte rendu par brouillon : son numéro, ou le motif de son refus. Un brouillon refusé ne consomme pas de numéro. | décision du 2026-10-14 | 2 |
| RG-FAC-20 | Le journal d'une pièce est en ajout seul. Il enregistre la demande d'une facture ou la création d'un avoir, chaque modification, l'émission, chaque consultation (côté client au portail, côté vendeur au back-office), l'annulation (avec le numéro de l'avoir), les avoirs émis et les retours de Zeendoc. Il est lu par le BNIA seulement. | CGI art. 289 VII, analyse §3 | 1 |
| RG-FAC-21 | Une note interne est un texte non vide, signé et daté, au carnet de la pièce, à part de son journal. Elle s'écrit par un acteur qui gère la facturation, quel que soit l'état de la pièce, et se lit par le personnel du BNIA qui lit la facturation. Seul son auteur la modifie (« modifiée » affiché) ou la supprime, sans trace. Elle n'est jamais imprimée, transmise ni montrée au client. | analyse §3 ; décision du 2026-10-21 (carnet comme celui d'une DSM) | 2 |
| RG-FAC-22 | Un avoir de correction a un rattachement et un motif. Le rattachement désigne soit une ou plusieurs factures émises du même acheteur (même client, ou même SIREN), non annulées, soit un contrat et une période : le contrat est le n° BNIA du client de l'avoir, qui doit donc avoir un client, et la période vaut par défaut l'année civile en cours, modifiable (décision du 2026-10-14). L'acheteur de l'avoir est celui des factures visées, ou le client du contrat ; avec un client, il se lit selon RG-FAC-11. Ses lignes sont libres, préremplies au besoin depuis les factures visées. Si la somme des avoirs émis sur les factures visées dépasse leur total TTC, un avertissement s'affiche, sans blocage. | §4.9.7 p.46, BOFiP BOI-TVA-DECLA-30-20-20-20 §220 et §260 (analyse §3) | 4 (hors prototype) |
| RG-FAC-23 | Annuler une facture émise crée un avoir d'annulation qui la vise seule, avec ses lignes reprises à l'identique et non modifiables, et un motif. Une facture déjà annulée, ou qui a déjà des avoirs de correction, ne s'annule pas : le reste se corrige par avoir. Une fois l'avoir émis, la facture est annulée (journal `ANNULEE`) et son origine est libérée (RG-FAC-02). | C.8.4.1 p.125 (« Total ») ; décision du 2026-10-14 | 4 (hors prototype) |
| RG-FAC-24 | Un avoir suit le cycle d'une facture (RG-FAC-09 à 17) : même séquence de numéros, PDF titré « Avoir ». | analyse §3 | 4 (hors prototype) |
| RG-FAC-25 | Les événements sont publiés dans la transaction qui les cause : `FactureEmise` et `AvoirEmis` à l'émission, `FactureAbandonnee` à l'abandon. Ils ne portent que l'origine opaque, jamais l'objet de la source. | ADR-013, analyse §4 | 1 |
| RG-FAC-26 | Seul un acteur qui gère la facturation (rôle `AGENT` ou `ADMIN` tenu dans l'opérateur BNIA, §3.12) demande, modifie, émet, abandonne, annule, crée un avoir, ajoute une note et paramètre le vendeur. Le personnel du BNIA lit toutes les pièces. Un rattachement actif lit les pièces émises dont son opérateur est le client, jamais les brouillons. | §4.2.5.1 p.15-16, ADR-016, analyse §2 | 1 |
| RG-FAC-27 | L'historique du back-office filtre par numéro, sens, état, nature d'origine, client ou nom d'acheteur, libellé de la période, date d'émission (de… à). Il affiche le numéro, la date, l'acheteur, la nature et la référence d'origine, le HT, la TVA, le TTC et l'état, puis, à partir du lot 6, l'état de règlement. Un lien de la fiche opérateur l'ouvre filtré sur ce client. | §4.9.1 p.44, §4.3.3 p.20 | 1 |
| RG-FAC-28 | Au portail, l'opérateur retrouve ses factures et ses avoirs émis par numéro, période et date ; il les distingue par nature d'origine et par sens ; il ouvre le détail, télécharge le PDF et voit la référence de l'origine. Chaque ouverture et chaque téléchargement est journalisé `CONSULTEE`, côté client. | §5.10.1-5.10.2 p.74 | 3 |
| RG-FAC-29 | Le tableau des articles réunit, à la date du jour, les articles de tous les `CatalogueArticles`, actifs ou non : source, code, désignation, unité, prix, catégorie et taux de TVA, actif. Chaque ligne ouvre l'article chez sa source par son lien d'administration. | §4.9.2 p.44, analyse UC-FAC-6 | 5 |
| RG-FAC-30 | Les ventes consolidées d'une période (date d'émission, du… au…, bornes comprises, la seconde postérieure ou égale à la première) sont faites des lignes article des pièces émises, à leur date d'émission ; celles d'un avoir comptent en négatif (quantité et HT). Elles se présentent ligne par ligne, dans une table triable, filtrable et regroupable, avec le total HT des lignes affichées. Les quantités ne se somment que par article : un regroupement qui mêle plusieurs articles n'a pas de quantité. La TVA ne se présente que par catégorie et taux, dans le récapitulatif du PDF : la somme des récapitulatifs des pièces, chaque TVA arrondie sur sa pièce (RG-FAC-05), puis les totaux HT, TVA et TTC. Export : un PDF de la période (synthèse par article, quantité dans son unité et HT ; récapitulatif de TVA ; totaux). Plus de tableau croisé dynamique ni d'export Excel (décision du 2026-10-16). | analyse UC-FAC-8 ; décisions du 2026-10-14, du 2026-10-15 et du 2026-10-16 | 5 |
| RG-FAC-31 | La partie publiée rend les `LigneVendue` d'une période, filtrables par nature d'origine, pour `statistiques` (CVO facturée sur DSM, sur fin de travaux). | §4.10.3 p.48 ; décision du 2026-10-14 | 5 |
| RG-FAC-32 | Qui gère la facturation transmet à Zeendoc une sélection de pièces émises, chacune dans sa transaction, son numéro comme clé d'idempotence : la pièce devient « Transmis » (journal `TRANSMISE`, côté vendeur). Une pièce ne se transmet qu'une fois, et un brouillon jamais : le refus est rapporté sans arrêter les autres. Simulé dans le prototype, sans traitement planifié ni rejet (décision du 2026-10-17). | §4.9.8 p.46, §6.2.3 p.83 | 6 |
| RG-FAC-33 | Le règlement relevé dans Zeendoc rend « Payé » une pièce transmise, une fois, à sa date (journal `PAYEE`, au nom de « Zeendoc »). L'état (« Transmis », « Payé ») s'affiche au back-office et au portail. Simulé dans le prototype : sans relevé planifié, seule la démonstration enregistre des règlements (décision du 2026-10-17). | §4.9.8 p.46, §5.10.3 p.74, §6.2.2 p.83 | 6 |
| RG-FAC-34 | Au portail, l'opérateur suit la facturation de ses cotisations : les lignes article de ses pièces émises dans une période (date d'émission, du… au…, bornes comprises, la seconde postérieure ou égale à la première, l'année civile en cours par défaut), filtrées par nature d'origine (`DSM`), celles d'un avoir en négatif. Elles se présentent comme les ventes du back-office (RG-FAC-30), sans l'acheteur, avec le total HT des lignes affichées ; une ligne ouvre sa facture. Pas d'export. Lues par qui lit les factures de l'opérateur (RG-FAC-26). | demande de l'utilisateur du 2026-10-21 | 3 |

## 5. Cas d'usage

### 5.1 Écriture

| Cas d'usage | Objets | RG | Lot |
|---|---|---|---|
| UC-FAC-1 Demander une ou plusieurs factures (appelé par une source) | `Facture`, `Journal` | 01, 02, 03, 04, 05, 06, 07, 08, 11, 20, 26 | 1 |
| UC-DEC-10 (dans `declarations`) Facturer une DSM ou une sélection de DSM : DSM validées, sans facture vivante, opérateur facturable ; une demande par DSM | DSM, projection `FactureDsm` | 01, 02 ; RG-DEC-34 révisée | 1 |
| UC-FAC-2 Émettre un brouillon | `Facture`, `Numerotation`, `Journal` | 05, 09, 10, 11, 12, 13, 14, 15, 25, 26 | 1 |
| Paramétrer le vendeur | `Vendeur` | 16, 26 | 1 |
| UC-FAC-2 Modifier un brouillon (en-tête, lignes, groupes, ordre) | `Facture`, `Journal` | 03, 04, 05, 06, 07, 08, 11, 14, 17, 26 | 2 |
| UC-FAC-2 Abandonner un brouillon | `Facture`, `Journal` | 14, 18, 25, 26 | 2 |
| UC-FAC-2 Émettre une sélection de brouillons | `Facture` | 19, et celles de l'émission | 2 |
| Écrire, modifier ou supprimer une note interne | `NoteInterne` | 21, 26 | 2 |
| UC-FAC-3 Annuler une facture | `Facture` (avoir), `Journal` | 23, 24, 25, 26 | 4 (hors prototype) |
| UC-FAC-3 Créer un avoir de correction (brouillon), puis l'émettre | `Facture` (avoir), `Journal` | 04, 05, 06, 11, 20, 22, 24, 26 | 4 (hors prototype) |
| UC-FAC-7 Transmettre une sélection de pièces émises à Zeendoc (simulé) | `Facture`, `Journal` | 26, 32 | 6 |
| UC-FAC-7 Enregistrer un règlement relevé dans Zeendoc (simulé, démonstration seulement) | `Facture`, `Journal` | 33 | 6 |

### 5.2 Lecture

| Cas d'usage | RG | Lot |
|---|---|---|
| UC-FAC-4 Historique du back-office, détail d'une pièce, PDF, journal (consultation journalisée côté vendeur) | 20, 26, 27 | 1 |
| Brouillons de l'acteur, avec ce qui manque pour les émettre | 09, 26 | 1 |
| Notes internes d'une pièce, au personnel du BNIA | 21, 26 | 2 |
| UC-FAC-5 Factures et avoirs de l'opérateur au portail, détail, PDF | 26, 28 | 3 |
| Suivi des cotisations de l'opérateur au portail : ses lignes de cotisation d'une période | 26, 34 | 3 (révision du 2026-10-21) |
| UC-FAC-6 Tableau de tous les articles | 29 | 5 |
| UC-FAC-8 Ventes consolidées, export Excel et PDF | 30 | 5 |
| Ventes publiées pour `statistiques` | 31 | 5 |
| État de transmission et de règlement (back-office, portail) | 32, 33 | 6 |

**Ce qu'elle lit** : rien dans les autres contextes. Ses ports (`Clients`, droits) sont implémentés dans
son infra sur la partie publiée d'`operateurs` (analyse §4). Les articles lui viennent des
`CatalogueArticles` que les sources implémentent.

**Ce qu'elle publie** : §3.11.

## 6. Écrans

### 6.1 Back-office (opérateur actif : BNIA)

- **Menu Facturation** :
  - *Factures* : historique (RG-FAC-27), avec un onglet *Brouillons* où l'on sélectionne des brouillons à émettre ensemble (compte rendu, RG-FAC-19) ;
  - *Articles* (RG-FAC-29) : table triable et filtrable par colonne ; une ligne ouvre l'écran de la source (la grille CVO) ;
  - *Ventes* (RG-FAC-30), sans onglets : en tête, deux graphiques de démonstration côte à côte (données fictives, indépendantes de la période, badge « Démonstration » : chiffre d'affaires total en histogramme ; chiffre d'affaires par secteur en courbes, cotisations totales ou détaillées, courbes à choisir ; vues 5 dernières années, 12 mois glissants, année en cours (mois terminés), vue par défaut, comparée par défaut à l'année précédente, « Comparer avec année précédente ») ; puis, juste au-dessus de la table, la période (raccourcis : trimestre en cours, trimestre précédent, année en cours, année précédente ; par défaut l'année civile en cours) et *Exporter en PDF* ; enfin les lignes vendues (table triable et filtrable, total HT des lignes affichées, une ligne ouvre sa pièce). Plus d'onglet *Analyse* (décision du 2026-10-16).
- **Pièce** :
  - l'en-tête (origine ou rattachement, acheteur, dates, période, condition, échéance, notes) ;
  - les lignes, groupées et ordonnées ; le récapitulatif de TVA ; les totaux ;
  - l'onglet *Journal*, avec l'ajout d'une note ;
  - le PDF (après l'émission) ; l'état Zeendoc (lot 6) : badge « Transmis » ou « Payé » à côté de l'état, avec ses dates ;
  - les actions selon l'état :
    - brouillon : *Aperçu PDF* (RG-FAC-15 ; masqué tant qu'une carte est en saisie), *Émettre*, *Abandonner* ; *Modifier* est porté par chaque carte (ci-dessous) ;
    - facture émise : *Ajouter une note* (*Annuler* et *Créer un avoir* : lot 4, hors prototype).

  Un brouillon affiche ce qui l'empêche d'être émis.
- **Édition d'un brouillon** (lot 2) : la pièce se présente en quatre cartes, *Acheteur*, *Facture*, *Contenu* et *Note de pied*. En haut à droite de chacune, *Modifier* si elle se modifie, sinon un cadenas dont l'infobulle dit pourquoi (pièce émise, acheteur, consultation sans gestion). *Modifier* passe la seule carte en saisie, une carte à la fois ; *Enregistrer* enregistre le brouillon (une entrée `MODIFIEE`), *Annuler* n'écrit rien (décisions du 2026-10-15) :
  - *Facture* : période, condition de paiement (jours, fin de mois), bon de commande, note d'en-tête ; la date de l'opération en lecture ;
  - *Acheteur* : jamais modifiable, il se corrige sur la fiche de l'opérateur (RG-FAC-11) ;
  - *Contenu* : les lignes (ajout d'un article, recherche dans tous les catalogues, d'un texte ou d'un groupe ; ordre par glisser-déposer, au pointeur ou au clavier, une ligne restant dans sa liste (racine ou groupe) ; désignation modifiable ; quantité, unité, prix et suppression pour les seules lignes ajoutées, ceux des lignes reçues affichés en texte avec un cadenas, RG-FAC-17). Dessous, le récapitulatif de TVA et les totaux, recalculés à chaque saisie avec les arrondis de RG-FAC-05 (le serveur recalcule à l'enregistrement et fait foi) ;
  - *Note de pied* : sa propre carte, sous les totaux ; vide, elle ne s'affiche que sur un brouillon que l'on peut modifier.
- **Abandonner** un brouillon (lot 2) : confirmation qui dit que l'origine redevient à facturer (« DSM juillet 2026 redeviendra à facturer ») ; retour à l'onglet *Brouillons*.
- **Journal** (lot 2) : sans onglets : bouton *Historique* (à gauche de *PDF* ou d'*Aperçu PDF*) qui ouvre le journal dans un tiroir à droite, pour un brouillon comme pour une pièce émise ; les notes n'y sont plus (décision du 2026-10-21).
- **Notes internes** (lot 2, révisé le 2026-10-21) : le cadre du carnet de la DSM, en bas de la page, avant la zone danger, quel que soit l'état de la pièce : notes signées et datées, du plus ancien au plus récent, « Visible du BNIA seulement » ; *Ajouter la note* pour qui gère la facturation ; *Modifier* et *Supprimer* (confirmé) pour l'auteur (RG-FAC-21).
- **Brouillons** (lot 2) : cases à cocher et *Émettre la sélection (n)*, pour qui gère la facturation ; une confirmation, puis un compte rendu par brouillon : son numéro (lien vers la pièce), ou le motif de son écart (RG-FAC-19).
- **Émises** et **Payées** (lot 6, simulé) : l'historique a trois onglets, *Brouillons*, *Émises* (pièces émises non payées, transmises comprises ; leur nombre en pastille, comme les brouillons) et *Payées* (pièces payées, sans cases à cocher) ; colonne *Zeendoc* (badge « Transmis » ou « Payé », dates en infobulle), aussi dans l'onglet *Factures* de la fiche opérateur ; pour qui gère la facturation, cases à cocher (une pièce déjà transmise ne se coche pas) et *Transmettre à Zeendoc (n)*, sans confirmation : un loader « Transmission… » d'au moins 1,5 s, puis les badges « Transmis » et un message ; un refus éventuel est nommé (RG-FAC-32).
- **Avoir** (lot 4, hors prototype) : choix des factures visées (même acheteur) ou du contrat du client (son n° BNIA) et d'une période, par défaut l'année civile en cours ; motif ; lignes préremplies depuis les factures ; avertissement de dépassement (RG-FAC-22).
- **Paramètres de l'application > BNIA > Facturation** : le vendeur (§3.7), dans un cadre en lecture, éditable, sous la fiche et les contacts du BNIA (spec 02 §6.1).
- **Écrans d'autres contextes** :
  - DSM : action *Facturer* sur la DSM, et sur une sélection de l'onglet *Validées* du listing ; lien vers la facture, ou vers le brouillon ;
  - fiche opérateur : indicateurs « facturable » et « certifiable », n° de TVA, onglet *Factures*, dernier de la barre « Suivi des activités » (back-office) : un cadre « Factures et avoirs » (pièces émises du client, sans colonne acheteur), puis un cadre « Brouillons » s'il en a ; une ligne ouvre la pièce ; plus de bouton *Factures* en tête de fiche (décision du 2026-10-15).

### 6.2 Portail (opérateur actif : producteur ou négociant)

**Menu Factures** (lot 3) : la liste des factures et avoirs émis (RG-FAC-28 ; filtres numéro, pièces : toutes, factures ou avoirs, période, dates d'émission ; colonne « Objet » : libellé de la nature d'origine et référence), puis le détail d'une pièce, sans journal :
- en-tête, lignes et totaux ;
- PDF ;
- référence de l'origine ;
- état Zeendoc (lot 6) : badge « Transmis » ou « Payé », en colonne de la liste et dans le détail, avec ses dates.

**Menu Cotisation** (révision du 2026-10-21, RG-FAC-34) : le suivi de la facturation des cotisations, pendant
de la page *Ventes* du back-office, sans onglets :
- en tête, deux graphiques de démonstration côte à côte (données fictives, celles de la filière ramenées à la
  part d'un opérateur moyen de son type, badge « Démonstration ») : cotisation totale en histogramme ; cotisation
  par ligne (sorties France, sorties export, EDV France, EDV export) en courbes à choisir ; vues 5 dernières
  années, 12 mois glissants, année en cours (par défaut, comparée à l'année précédente) ;
- la période (raccourcis : trimestre en cours, trimestre précédent, année en cours, année précédente ; par défaut
  l'année civile en cours) ;
- ses lignes de cotisation (émise le, n°, objet, article, TVA, quantité, prix unitaire HT, montant HT ; code
  article en colonne masquée), table triable et filtrable, total HT des lignes affichées ; une ligne ouvre sa
  facture.

Le module Facturation est lu par tout rattachement actif (§3.12).

## 7. Données

### 7.1 Démonstration (profil `demo`)

- **Vendeur** : le BNIA, avec les données imprimées sur les factures d'exemple (adresse à Eauze, SIRET
  77699995500029, n° de TVA FR55776999955, IBAN et BIC, `compta@armagnac.fr`) ; pas d'option sur les
  débits.
- **Fiches opérateurs** : SIRET et n° de TVA sur les opérateurs de démonstration qui ont des DSM
  validées ; une fiche laissée non facturable (sans SIRET), pour montrer le refus.
- **Pièces**, à partir des DSM validées de la démonstration :
  - une facture émise par opérateur à DSM validée, aux montants de la facture d'exemple ;
  - un brouillon en attente ;
  - dix DSM validées d'Armagnacs de Gascogne à facturer en lot (onze mois validés, le plus ancien facturé), une note interne sur la facture émise (lot 2) ; révisé au lot 5, puis le 2026-10-23 : ses onze DSM validées sont facturées et émises, chacune à la fin du mois qui suit sa période ; aucun brouillon au démarrage, aucune DSM de Gascogne à facturer ; la DSM de la Maison Durand est émise de même, à la fin du mois qui suit sa campagne : les ventes s'étalent ainsi sur plusieurs mois et deux années civiles ; quinze opérateurs fictifs ajoutés pour le volume, facturables (destinataire des factures en adresse saisie, sans compte ; DSM saisies par un seul compte `saisie@demo.fr`, AGENT de chacun) : cinq négociants, toutes leurs DSM mensuelles de l'année validées jusqu'au mois dernier (jusqu'à l'avant-dernier pour Maison Saint-Puy, en retard, décision du 2026-10-21), et dix producteurs, leur DSM de la dernière année déclarative close validée ; chaque DSM facturée et émise de même ;
  - Zeendoc (lot 6, simulé ; décision du 2026-10-17, révisée le 2026-10-23) : toute facture émise avant le mois courant est transmise le lendemain de son émission, puis payée à son échéance, au plus tard la veille, sauf celle de Gascogne émise il y a deux mois, transmise et impayée, échéance passée (cartes « Factures impayées » du back-office et du portail) ; celles du mois courant restent à transmettre ;
  - pas d'avoir (lot 4 hors prototype).

### 7.2 Reprise AGI

Hors périmètre du prototype (décision du 2026-10-08). La vue AGI 2.3 (p.9-10) n'a servi qu'à relever
des attributs : notes d'en-tête et de pied.

## 8. Décisions d'architecture

Principe (décision du 2026-10-14) : architecture hexagonale stricte et modularisation Maven. Schémas des
alternatives : [architecture.md](architecture.md).

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-FAC-1 | **Module d'API** `bnia-facturation-api` : `DemandeFactures`, `CatalogueArticles`, `FacturationLecture`, leurs types et les événements ; seul module de `facturation` dont dépendent les autres. Le core d'une source n'en dépend pas : il a son propre port (`Facturation` pour `declarations`), implémenté dans son infra par un adapter sur l'API ; l'infra de la source implémente aussi `CatalogueArticles` et écoute les événements. Le core de `facturation` implémente `DemandeFactures` et `FacturationLecture`. Dépendances : `declarations-infra` → `facturation-api` ← `facturation-core` ; aucun cycle. | Core de la source appelant l'API directement (hexagonale moins stricte) ; paquets `partage` dans le core (ADR-012, 017 : frontière par ArchUnit seul) ; ports côté sources implémentés par `facturation` (la lib dépendrait de ses sources) | ADR-021 ; amendements d'ADR-014 et d'ADR-017 |
| A-FAC-2 | **Catalogues injectés** : chaque source déclare un adapter qui implémente `CatalogueArticles` ; le core de `facturation` reçoit la liste de tous, indexée par `source()` ; deux catalogues de même source font échouer le démarrage. | Registre explicite dans `bnia-boot` ; catalogue composite dans `bnia-boot` | ADR-021 |
| A-FAC-3 | **Ports de l'hôte** `DroitsFacturation` et `Clients`, définis dans le core de `facturation`, implémentés dans `bnia-facturation-infra` (`adapter.integration`) sur la partie publiée d'`operateurs`. Le nom `Habilitations` est déjà pris par `operateurs`. | Adapters dans `bnia-boot` ; module hôte dédié | ADR-021 |
| A-FAC-4 | **Lignes en une table** `facturation.ligne_facture` : colonne `sorte` (`ARTICLE`, `TEXTE`, `GROUPE`), `position`, `groupe` (référence à une ligne de la même pièce), colonnes de l'article facultatives, contrôlées par des contraintes `CHECK` selon la sorte ; mappées vers les trois classes du domaine dans `adapter.persistence`. | Une table par sorte ; lignes en `jsonb` | — (ADR-004) |
| A-FAC-5 | **Numérotation par compteur verrouillé** : table `facturation.numerotation (annee, dernier)`, lue `FOR UPDATE` et incrémentée dans la transaction d'émission ; un rollback ne consomme pas le numéro. | Séquence PostgreSQL (trou au rollback) ; max + 1 sous contrainte d'unicité (collisions) | — |
| A-FAC-6 | **Émission en lot** par un cas d'usage `EmettreLot` qui appelle `EmettrePiece` pour chaque brouillon à travers un port technique `Transactions` (« exécuter dans une nouvelle transaction »), défini dans `bnia-commun-core` et implémenté dans `bnia-commun-infra` (`TransactionTemplate`, `REQUIRES_NEW`). Le compte rendu (RG-FAC-19) est produit par le cas d'usage. | Boucle dans le controller (règle dans l'adapter REST) ; `@UseCase` non transactionnel | ADR-022 |
| A-FAC-7 | **Zeendoc** : port `Zeendoc` (déposer un PDF avec son numéro, lire l'état d'une pièce) dans le core ; adapter simulé dans `adapter.integration` tant que l'API est inconnue (bordure) ; dépôt et relevé par deux traitements planifiés (`adapter.planification`), idempotents en base. Dans le prototype (2026-10-17) : port `Zeendoc.deposer` seul, adapter simulé, transmission à la demande, sans traitement planifié. | — | — (ADR-019) |
| A-FAC-8 | **Références opaques** : `Origine` (source, nature, identifiant) et `ClientRef` (identifiant, code) stockés en texte, **sans clé étrangère** ; index sur (source, identifiant) pour RG-FAC-02 et sur le client pour l'historique et le portail. | Clé étrangère par source (la lib connaîtrait ses sources) | ADR-011 amendé le 2026-10-14 |
| A-FAC-9 | **Module `FACTURATION`** dans `operateurs.domain.partage.Module` : lu par tout rattachement actif (ses pièces), géré par le seul rôle tenu dans l'opérateur BNIA, comme `PARAMETRAGE` pour la gestion (`Droits.gere`). | — | — (ADR-016) |
| A-FAC-10 | **PDF** au gabarit Thymeleaf et OpenHTMLtoPDF dans `adapter.document`, stocké par `StockageDocuments` (`DocumentDepose`) à l'émission ; ventes en Excel par `fastexcel`. | — | — (ADR-018, ADR-020) |

## 9. Hypothèses

| Id | Hypothèse | Pourquoi |
|---|---|---|
| H-FAC-1 | La part non assujettie d'une cotisation est **exonérée** (catégorie E, avec motif), sur sa propre ligne, à côté de la part assujettie (S) : une seule facture par objet facturé. | La facture d'exemple montre deux parts sur une ligne, sans qualifier la part non assujettie. Hors champ (O), elle ne pourrait se mêler à aucune autre catégorie sur une facture EN 16931 : deux factures par DSM (analyse §1 et §3, décision du 2026-10-14). |
| H-FAC-2 | Le prix et la TVA d'un article se résolvent à la **date de l'opération** (fin de la période de la DSM, campagne), pas à la date d'émission. | Le CDC ne le dit pas ; la cotisation se calcule au taux en vigueur en fin de période (RG-DEC-32) ; l'émission peut suivre de plusieurs semaines (analyse §1, décision du 2026-10-14). |
| H-FAC-3 | La `CVO` se paie à « 30 jours fin de mois ». | Seule condition qui retrouve les échéances des deux factures d'exemple (07/10 → 30/11, 31/03 → 30/04) ; le CDC ne la donne pas (décision du 2026-10-14). |
| H-FAC-4 | Factures et avoirs partagent une seule séquence de numéros par année (`AAAA` + 6 chiffres). | Les exemples ne montrent que des factures ; la facture provisionnelle a sa propre forme (`202603-00002`). Une séquence unique est la plus simple à tenir sans trou (analyse §3). |
| H-FAC-5 | Sur une facture de DSM, les lignes de cotisation de volume nul ne sont pas imprimées. | La facture d'exemple les imprime à 0,00 € ; avec deux lignes article par cotisation, elles doubleraient sans rien dire (nouveau modèle, §4.9.8 p.46). |
| H-FAC-6 | La note de pied d'une facture de DSM reprend le texte de la facture d'exemple (part assujettie, accords interprofessionnels, arrêté du 11 janvier 2024, 60 €/hlAP), adapté au taux appliqué. | Seule source du texte (facture `FACTURE-CVO-Armagnac_2026000700.pdf`). |

## 10. Questions ouvertes

Revue le 2026-10-14 (phase F) : toutes les hypothèses sont maintenues pour le prototype ; toutes les
questions sont adressées au BNIA (et à son comptable pour Q-FAC-1, 2 et 6). La facture provisionnelle et
le forfait restent hors du prototype jusqu'à sa réponse.

| Id | Question | Bloque | Lot |
|---|---|---|---|
| Q-FAC-1 | **Au BNIA et à son comptable** : confirmer que la part non assujettie de la cotisation est exonérée (E) et non hors champ (O), et donner le motif à imprimer (H-FAC-1). | Hors champ : deux factures par DSM, et la demande de facture change de forme | 1 |
| Q-FAC-2 | **Au BNIA et à son comptable** : confirmer que le prix et la TVA se prennent à la date de l'opération (H-FAC-2). | Date passée à `CatalogueArticles` ; facture émise après un changement de taux | 1 |
| Q-FAC-3 | **Au BNIA** : qu'est-ce qu'une facture provisionnelle (citée p.16, exemple en annexe E p.140, jamais décrite) ? Qui la déclenche, sur quelle base, et comment se solde-t-elle ? | Une source de plus, ou un avoir de solde | — |
| Q-FAC-4 | **Au BNIA** : le forfait de 1 500 € des opérateurs satellites et des metteurs en bouteilles à façon (récapitulatif du 2026-10-14) : qui y est soumis, à quelle fréquence, avec quelle part assujettie ? | Une source et un article de plus, dans un contexte à désigner | — |
| Q-FAC-5 | **Au BNIA** : confirmer « 30 jours fin de mois » pour la `CVO` (H-FAC-3). | Condition envoyée par `declarations` | 1 |
| Q-FAC-6 | **Au BNIA et à son comptable** : confirmer une séquence commune aux factures et aux avoirs (H-FAC-4). | Numérotation des avoirs | 4 |
| Q-FAC-7 | **Au BNIA** : confirmer qu'on n'imprime pas les lignes de cotisation de volume nul (H-FAC-5). | Demande de facture d'une DSM | 1 |
| Q-FAC-8 | **Au BNIA** : confirmer le texte de la note de pied d'une facture de cotisation (H-FAC-6). | PDF des factures de DSM | 1 |

Questions de l'analyse reportées dans la spec de leur contexte (révision de la spec 05, dans les lots de
cette spec) : la cotisation Floc de Gascogne sur la colonne 15 ; la facturation de l'acheteur qui
relève du BNIA. Pour `distillation` : le code payeur d'une fin de travaux (C.5.1.3 p.119).

## 11. Tests attendus

Chaque RG-FAC-nn a au moins un test qui porte son identifiant (CLAUDE.md).
- **Tests de domaine**, sans base : 04, 05, 06, 07, 08, 10, 12, 13, 14, 22 (calcul de l'avertissement),
  23 (conditions de l'annulation).
- **Tests de cas d'usage**, avec repositories et ports simulés (un `CatalogueArticles` factice) : 01, 02,
  03, 09, 11, 16, 17, 18, 19, 20, 21, 24, 25, 26 (un test de refus par cas d'usage), 30, 32, 33, 34.
- **Tests d'intégration** : 12 (numéros sous concurrence), 15 (PDF), 25 (`declarations` reçoit les
  événements dans la transaction), 21, 27, 28, 29, 31, 34 ; `CatalogueArticles` de `declarations` et port
  `Clients` sur `operateurs`.
- **Révisions** : les RG-DEC et RG-OPE révisées gardent leur identifiant et leurs tests, mis à jour.

## 12. Lots d'implémentation

| Lot | Incrément | Contenu (cas d'usage, écrans, RG) | Démontrable à la fin du lot |
|---|---|---|---|
| 1 | **Facturer une DSM** | Contexte, schéma et module `Facturation` ; partie publiée (`DemandeFactures`, `CatalogueArticles`, `FactureEmise`) ; ports `Clients` et droits ; vendeur (Paramétrage) ; brouillon créé par la demande, consulté, émis seul ; numérotation, échéance, PDF, journal ; historique et détail. Révisions : `operateurs` (n° de TVA, facturable et certifiable, acheteur publié, module) ; `declarations` (UC-DEC-10 pour une DSM et une sélection, `CatalogueArticles`, demande, projection, journal, retrait des types). RG-FAC-01 à 16, 20, 25, 26, 27. | Le BNIA facture la DSM de juillet d'un négociant, ouvre le brouillon, l'émet : facture 2026000001 en PDF, total identique à la facture d'exemple ; la DSM passe à « facturée ». Une DSM d'un opérateur sans SIRET est refusée, et le refus nomme ce qui manque. |
| 2 | **Brouillons** | Modifier un brouillon (en-tête, changement de client, lignes, groupes, ordre), abandonner, émettre une sélection avec compte rendu, notes du journal. RG-FAC-17, 18, 19, 21. | Le BNIA facture dix DSM, corrige un libellé de période, ajoute une ligne de texte, abandonne un brouillon (la DSM redevient « à facturer »), émet les neuf autres d'un coup ; une note s'affiche au journal. |
| 3 | **Portail** | Factures et avoirs de l'opérateur, détail, PDF, consultation journalisée côté client. RG-FAC-28. | Un négociant voit sa facture, télécharge le PDF ; le BNIA voit « consultée par » au journal. |
| 5 | **Articles et ventes** | Tableau des articles ; ventes consolidées, regroupements, exports Excel et PDF ; lecture publiée pour `statistiques`. RG-FAC-29, 30, 31. | Le BNIA voit les huit articles de cotisation et ouvre la grille ; il consulte les ventes du trimestre par article et les exporte. |
| 4 | **Avoirs** (hors prototype, décision du 2026-10-17) | Annuler une facture ; avoir de correction (factures visées ou contrat et période, motif, lignes) ; `AvoirEmis` ; montants nets et réouverture dans `declarations`. RG-FAC-22, 23, 24. | Le BNIA annule la facture d'une DSM, qui redevient « à facturer », puis la refacture ; il accorde un avoir partiel sur une autre : la DSM montre le net. |
| 6 | **Zeendoc** (réduit à une simulation, décision du 2026-10-17) | Transmission d'une sélection de pièces émises au back-office, une fois chacune, par un adapter simulé ; règlements enregistrés par la démonstration ; état au back-office et au portail. Sans traitement planifié, rejet, anomalie ni rejeu. RG-FAC-32, 33. | Le BNIA sélectionne deux factures récentes, *Transmettre à Zeendoc* : après le loader, elles sont « Transmis » ; les anciennes sont « Payé », au portail aussi. |

## Journal

| Date | Changement |
|---|---|
| 2026-10-14 | Spec créée (phases A et B) : objet et périmètre. Décisions de périmètre : révision de `declarations` (spec 05 et code) menée dans les lots de cette spec ; clients libres dans `boutique` ; vendeur paramétré dans la lib ; lecture des ventes publiée dès maintenant pour `statistiques` ; `declarations` seule source réelle, `distillation` et `boutique` implémenteront la partie publiée dans leurs specs. H-FAC-1, 2 et Q-FAC-1 à 4 reprises de l'analyse. |
| 2026-10-14 | Phase C : modèle (§3), RG-FAC-01 à 33, cas d'usage, écrans, données, six lots. Décisions : révision d'`operateurs` dans le lot 1 (n° de TVA, facturable et certifiable) ; condition de paiement en jours et fin de mois ; notes d'en-tête et de pied ; avoir d'annulation distinct, qui libère l'origine ; TVA calculée par catégorie et taux (BR-CO-17, écart à l'analyse) ; acheteur d'un client lu par le port `Clients` et relu à l'émission ; émission en lot brouillon par brouillon ; brouillon abandonné supprimé ; lignes négatives admises, total positif ou nul. H-FAC-3 à 6, Q-FAC-5 à 8. |
| 2026-10-14 | Rattachement d'un avoir à un contrat (code 262) : le contrat est le n° BNIA du client, la période vaut par défaut l'année civile en cours (§3.4, RG-FAC-22, §6.1). |
| 2026-10-14 | Phase D : `contextes.md` mis à jour (ordre et dépendances de 5, 8 et 9 ; bordures ; fiches 1, 2, 5, 8, 9, 11, 12, 13 ; éléments transverses ; context map ; interrogations ; journal). |
| 2026-10-14 | Phase E : décisions A-FAC-1 à 10 (§8), schémas dans [architecture.md](architecture.md). Principe : hexagonale stricte et modularisation Maven. Module `bnia-facturation-api`, port propre à chaque source ; catalogues injectés ; ports `DroitsFacturation` et `Clients` dans l'infra ; lignes en une table ; compteur verrouillé ; port `Transactions` pour l'émission en lot. |
| 2026-10-14 | Phase F : hypothèses H-FAC-1 à 6 maintenues ; Q-FAC-1 à 8 adressées au BNIA ; facture provisionnelle et forfait de 1 500 € hors prototype en attendant sa réponse. |
| 2026-10-14 | Phase G, relecture : pages corrigées (§3 p.10-12, C.3.1.1 p.107, C.12 p.133-134) ; §2.1 aligné sur A-FAC-1 et A-FAC-3 (API, ports de l'hôte, révision d'`operateurs`) ; `distillation` appelle `DemandeFactures` (§2.2) ; renvois du §3 ; journal `CREEE` pour un avoir ; acheteur de l'avoir (RG-FAC-22) ; RG-FAC-14 rattachée aux cas d'usage ; UC-DEC-10 par le port propre de `declarations`. |
| 2026-10-14 | Phase G, glossaire : « Facture », « Avoir », « État de règlement », « Ligne de cotisation » corrigés ; « Fiche incomplète » remplacée par fiche facturable et certifiable ; seize termes ajoutés (brouillon, facture vivante, demande de facture, source, origine, article et catalogue, lignes, catégorie et récapitulatif de TVA, acheteur, vendeur, condition de paiement, journal de la pièce, transmission, vente directe, ligne vendue). |
| 2026-10-14 | Phase H : spec validée par l'utilisateur, statut « terminée ». ADR écrits : ADR-021, ADR-022, amendements d'ADR-011, 014 et 017 ; `CLAUDE.md` mis à jour. |
| 2026-10-14 | Implémentation, cadrage (décisions en séance) : lot 1 scindé en **1a** (révision d'`operateurs`) et **1b** (`facturation` et révision de `declarations`) ; au listing des DSM, une DSM dont la facture est demandée mais pas émise va dans *Facturées*, avec un badge « Brouillon » ; après « Facturer » sur une DSM, on reste sur la DSM (« Facturation demandée », lien vers le brouillon). |
| 2026-10-14 | Lot 1a implémenté : spec 02 révisée (n° de TVA, RG-OPE-06 ; facturable et certifiable, RG-OPE-02 ; module Facturation, RG-OPE-31 ; `AcheteurVue` publiée, `OperateursLecture.acheteur`). Choix d'implémentation : « destinataire des factures » au nombre de ce qui manque pour facturer quand il ne donne pas d'e-mail ; filtre unique du listing (non facturables, non certifiables, les deux) ; en démonstration, le Domaine de Laubade, qui a une DSM validée, reste sans SIRET. |
| 2026-10-14 | Lot 1b implémenté : contexte `facturation` (modules `-api`, `-core`, `-infra`, schéma `facturation`), RG-FAC-01 à 16, 20, 25 à 27 ; révision de `declarations` (spec 05 : RG-DEC-34 révisée, RG-DEC-43 nouvelle, port `Facturation`, catalogue de la cotisation, projection avec le brouillon demandé). Choix d'implémentation : l'API dépend du noyau commun (`RegleEnfreinte`), et ses valeurs (`CategorieTva`, `ConditionPaiement`, `Origine`, `ClientRef`) se valident elles-mêmes ; le domaine de `facturation` emploie ces valeurs (ArchUnit l'admet, et seulement pour lui) ; `FactureEmise` porte en plus l'acteur qui a émis, pour le journal de la source ; prix unitaire à 4 décimales, quantité en hlAP à 4 décimales ; motif d'exonération « Part de la cotisation interprofessionnelle non assujettie à la TVA » (Q-FAC-1) ; note de pied composée d'après H-FAC-6 (Q-FAC-8) ; vendeur en une ligne, émission refusée tant qu'il n'est pas paramétré ; totaux stockés pour l'historique ; acheteur, vendeur et origine en colonnes préfixées ; l'ouverture d'une pièce émise et son PDF sont journalisés « consultée » au back-office, pas celle d'un brouillon ; historique en deux onglets (*Émises*, *Brouillons*), filtres numéro, acheteur, période, dates, client (lien de la fiche opérateur) ; SIRET de démonstration corrigés (clé de Luhn) ; Armagnacs de Gascogne a deux DSM validées en démonstration (spec 05 §7.1). |
| 2026-10-15 | Écrans (demande de l'utilisateur) : l'historique des factures d'un opérateur devient l'onglet *Factures* de sa fiche, dernier de la barre « Suivi des activités », au back-office (pièces émises, puis brouillons s'il y en a) ; le bouton *Factures* en tête de fiche est retiré ; l'historique reste filtrable par client par l'URL. |
| 2026-10-15 | Écrans (demande de l'utilisateur) : le vendeur passe de la section *Facturation* du Paramétrage à la sous-section *Facturation* de la section *BNIA* des paramètres de l'application, dans un cadre « Vendeur » en lecture, « Éditer » le passant en saisie. |
| 2026-10-15 | Implémentation, lot 2, décisions en séance : édition d'un brouillon **d'un seul enregistrement** (un journal `MODIFIEE` par enregistrement) ; **date de l'opération non modifiable** et **acheteur non modifiable**, sans *Changer de client* ni saisie libre (écarts à RG-FAC-11, 17 et §6.1 : on abandonne et l'on refacture ; la saisie d'un acheteur libre se définira avec `boutique`) ; **lignes reçues verrouillées** (article, quantité, unité, prix, groupe ; ni retrait), seules leur désignation et leur position changent ; lignes ajoutées libres. RG-FAC-11, 17, 18 révisées, §3.1, §3.2, §3.5, §6.1. |
| 2026-10-15 | Lot 2 implémenté : RG-FAC-17, 18, 19, 21. Choix d'implémentation : chaque ligne a un numéro stable et un indicateur « reçue » (colonnes `numero`, `recue` de `ligne_facture`) ; le `PUT` du brouillon renvoie le numéro des lignes existantes, dont le serveur reprend article, nature, code comptable et TVA tels qu'enregistrés (la TVA ne vient jamais du navigateur) ; seule une ligne article nouvelle se résout dans son catalogue ; une ligne reçue reste dans son groupe reçu ; version de la pièce renvoyée, 409 si elle est périmée ; `FactureAbandonnee` porte la pièce, le client et l'acteur, pour le journal de la DSM (« Facturation abandonnée », spec 05) ; émission en lot par `EmettreLot` et le port `Transactions` (ADR-022), toute erreur métier devenant un motif du compte rendu, un brouillon sélectionné deux fois ne comptant qu'une ; endpoints `PUT`, `DELETE /pieces/{id}`, `POST /pieces/emission`, `POST /pieces/{id}/notes`, `GET /pieces/{id}/articles` (articles actifs de tous les catalogues à la date de l'opération) ; monter et descendre au lieu du glisser-déposer ; en démonstration, Armagnacs de Gascogne a onze DSM validées (une facturée, dix à facturer) et la facture émise porte une note. |
| 2026-10-15 | Lot 3 implémenté (portail) : RG-FAC-28, RG-FAC-26 pour le portail. Choix d'implémentation : endpoints rattachés à l'opérateur, comme les DSM du portail (`/api/operateurs/{operateur}/factures`, détail, PDF) ; droit de lecture du module Facturation sur l'opérateur (port `DroitsFacturation.litClient`) ; un brouillon, ou la pièce d'un autre client, est introuvable (404) sans dire qu'elle existe ; consultation journalisée côté client, côté vendeur si c'est le personnel du BNIA ; le journal n'est pas exposé au portail (RG-FAC-20) ; filtre par sens (toutes, factures, avoirs), la nature d'origine en colonne ; l'acheteur n'y renvoie pas à la fiche opérateur. Démonstration : aucune donnée nouvelle, la facture émise d'Armagnacs de Gascogne suffit. |
| 2026-10-15 | Ordre des lots (demande de l'utilisateur) : le lot 5 (articles et ventes) passe avant le lot 4 (avoirs) ; les numéros ne changent pas. Les ventes consolidées comptent déjà les avoirs en négatif (RG-FAC-30), vérifié de bout en bout au lot 4. Les ventes reprendront le tableau croisé dynamique né sur les stocks déclarés (spec 05, RG-DEC-44). |
| 2026-10-15 | Lot 5, décisions en séance : PDF des ventes produit par le serveur, de forme fixe (synthèse par article, récapitulatif de TVA, totaux), l'Excel étant l'export du tableau croisé affiché ; TVA dans une carte « Récapitulatif de TVA de la période », hors du tableau croisé ; mesure « Quantité » qui rend l'article non cumulable (extension générique du composant : une mesure déclare les champs qu'elle ne cumule pas) ; période du… au… sur la date d'émission, avec raccourcis, l'année civile en cours par défaut. RG-FAC-29, 30 précisées, §6.1. |
| 2026-10-15 | Lot 5 implémenté : RG-FAC-29, 30, 31. Choix d'implémentation : `FacturationLecture` et `LigneVendue` dans `bnia-facturation-api`, implémentées dans le core (`LectureFacturation`), sans acteur ; une ligne vendue porte son signe (`avoir`) et ses montants signés ; le récapitulatif de la période se calcule dans le core, pièce par pièce, par la fonction du domaine (`LigneTva.recapituler`, puis `cumuler`), ce qui retrouve le total de TVA stocké ; une seule requête (`JdbcVentesLecture`), sans changement de schéma ; endpoints `GET /api/facturation/articles`, `GET /api/facturation/ventes?du&au`, `GET /api/facturation/ventes/pdf?du&au` (gabarit `ventes.html`), lus par le seul personnel du BNIA ; page *Ventes* en deux onglets comme les stocks déclarés ; aucune donnée de démonstration nouvelle. Point pour le lot 4 : un avoir n'a pas d'origine, le filtre par nature de la lecture publiée l'écarte donc ; il devra prendre la nature des factures visées pour que `statistiques` compte la CVO nette. |
| 2026-10-15 | Démonstration enrichie pour les ventes (demande de l'utilisateur, lot 5) : factures émises à des dates passées, la fin du mois qui suit la période de chaque DSM (au plus tard la veille), dans l'ordre des dates pour que les numéros les suivent ; huit pour Armagnacs de Gascogne, une pour la Maison Durand ; un brouillon et deux DSM à facturer restent pour les lots 1 et 2 (§7.1). La date d'émission vient d'une instance d'`EmettrePiece` propre au chargeur de démonstration, sur une horloge réglable ; le cas d'usage ne change pas et l'application émet toujours au jour même (RG-FAC-09). |
| 2026-10-15 | Démonstration, volume (demande de l'utilisateur) : cinq négociants et dix producteurs fictifs, facturables, toutes leurs DSM validées, facturées et émises à des dates passées (§7.1). Décision en séance : un seul compte de saisie de leurs DSM, `saisie@demo.fr`, AGENT de chacun (la DSM se remplit par un membre de l'opérateur, RG-DEC-01) ; destinataire des factures en adresse saisie, sans compte. |
| 2026-10-16 | Page *Ventes* (demande de l'utilisateur) : deux graphiques de démonstration, aux données fictives calculées dans le front, au-dessus des lignes vendues ; l'onglet *Analyse* (tableau croisé dynamique, son export Excel, récapitulatif de TVA à l'écran) est retiré, la page n'a plus d'onglets. Le PDF de la période garde sa synthèse et son récapitulatif de TVA. RG-FAC-30 révisée (§6.1). Code retiré : le tableau croisé des ventes (front) ; le récapitulatif de TVA et les totaux de la réponse de `GET /api/facturation/ventes`, qui ne rend plus que les lignes (le PDF calcule toujours les siens). |
| 2026-10-17 | Lots restants (décisions en séance) : **lot 4 (avoirs) hors prototype**, pour ne pas charger les écrans ; l'essentiel est de facturer des DSM. RG-FAC-22 à 24, UC-FAC-3 et `AvoirEmis` restent décrits, marqués « hors prototype ». **Lot 6 réduit à une simulation de démonstration** : au back-office, onglet *Émises*, le BNIA sélectionne des factures émises et clique *Transmettre à Zeendoc* ; un loader, puis le badge « Transmis » ; des factures déjà « Payé » en démonstration (toute facture émise il y a plus de deux mois) ; état affiché au listing, au détail, dans l'onglet *Factures* de la fiche opérateur et au portail ; entrée au journal ; pas de retransmission ; ni `EN_ATTENTE`, `REJETEE`, `EN_ANOMALIE`, ni traitement planifié, ni historique des retours. §2.1, §3.9, §3.10, §3.11, RG-FAC-32 et 33 révisées, §5.1, §6.1, §6.2, §7.1, §12. |
| 2026-10-17 | Lot 6 implémenté (simulé) : RG-FAC-32, 33 ; statut « implémentée ». Choix d'implémentation : pas d'agrégat `Transmission`, le suivi Zeendoc est une valeur de la pièce (`SuiviZeendoc` : état `EtatZeendoc`, transmise le, payée le ; colonnes `zeendoc`, `zeendoc_transmise_le`, `zeendoc_payee_le` de `facture`) ; `Facture.transmettre` et `Facture.enregistrerReglement` ; port `Zeendoc.deposer(numero, pdf)` et adapter `ZeendocSimule`, qui ne fait que tracer le dépôt ; cas d'usage `TransmettreAZeendoc` sur le modèle d'`EmettreLot` (une transaction par pièce, compte rendu par pièce) et `EnregistrerReglement`, sans acteur, appelé par la seule démonstration (pas d'endpoint) ; événement de journal `DEPOSEE` renommé `TRANSMISE`, `REJETEE` et `EN_ANOMALIE` retirés ; endpoint `POST /api/facturation/pieces/zeendoc` ; `zeendoc`, `transmiseLe`, `payeeLe` dans `PieceListee` et `Piece` (back-office et portail) ; délai de 1,5 s simulé dans le front, le serveur répond aussitôt ; pas de confirmation ; en démonstration, transmission le lendemain de l'émission et règlement à l'échéance, au plus tard la veille, sur l'horloge réglable du chargeur. |
| 2026-10-17 | Écrans (demande de l'utilisateur) : troisième onglet *Payées* dans l'historique du back-office, qui reçoit les pièces payées ; *Émises* garde les pièces transmises et non transmises. Partage du listing des pièces émises dans le front, selon l'état Zeendoc, sans changement d'API (§6.1). |
| 2026-10-17 | Écrans (demande de l'utilisateur) : l'onglet *Émises* compte ses pièces (émises non payées) dans une pastille, comme *Brouillons*, sans les filtres de saisie (seul le client de l'URL compte) (§6.1). |
| 2026-10-17 | Révision par la spec 15 (`messagerie`, lot 6) : une pièce émise dont le client est un opérateur est conversable (`ObjetsConversables` du type `FACTURE` dans l'infra, cas d'usage `DecrirePiece`) ; section *Messages* sur la pièce émise, au back-office (§6.1) et au portail (§6.2) ; la lecture du module Facturation suffit au client pour y écrire (spec 15 RG-MSG-01, 17). |
| 2026-10-21 | Portail (demande de l'utilisateur) : menu *Cotisation*, suivi de la facturation des cotisations de l'opérateur, bâti comme la page *Ventes* (§6.2) ; RG-FAC-34. Choix d'implémentation : port `VentesLecture` filtré aussi par client (identifiant chez l'hôte) ; cas d'usage `ConsulterMesFactures.lignes`, droit de lecture du client (RG-FAC-26), même contrôle de période que RG-FAC-30 ; endpoint `GET /api/operateurs/{operateur}/factures/lignes?du&au&nature`, qui rend des `LigneVendue` ; le front demande la nature `DSM`, `facturation` ignore ce qu'est une cotisation ; graphiques de démonstration des ventes, ramenés à la part de l'opérateur ; pas d'export. Le chevron qui finit chaque ligne cliquable s'ajoute aux tables *Ventes* et *Articles* (composant commun). |
| 2026-10-21 | Démonstration (§7.1) : Maison Saint-Puy (`N320012`) a une DSM validée et facturée de moins, celle du mois dernier n'étant pas initiée, pour montrer une période absente en retard (spec 05, RG-DEC-40). |
| 2026-10-21 | Écran d'une pièce (demande de l'utilisateur) : « Abandonner » (brouillon) quitte l'en-tête pour le cadre « Zone danger » en bas de page, avec ce qu'il entraîne (convention de la spec 00). |
| 2026-10-21 | Notes internes d'une pièce (demande de l'utilisateur) : elles quittent le journal (*Historique* › *Note interne*, événement `NOTE` retiré) pour un carnet comme celui d'une DSM, en bas de la page de la pièce, quel que soit son état ; l'auteur modifie ou supprime la sienne ; supprimées avec un brouillon abandonné. `NoteInterne` (§3.9), RG-FAC-14, 18, 20, 21 révisées, §5, §6.1, §7.1, §11. Choix d'implémentation : le cadre est un composant commun au carnet de la DSM (`CarnetNotesInternes`) ; endpoints `/api/facturation/pieces/{piece}/notes`. |
| 2026-10-23 | Écrans (demande de l'utilisateur, §6.1), révision du 2026-10-17 : les pastilles de *Brouillons* et d'*Émises* suivent les filtres, comme les onglets de la page DSM : chacune compte les lignes que son onglet montre. L'acheteur et la période valent pour les deux ; le numéro et les dates d'émission pour les pièces émises seulement ; le client de l'URL, toujours. Elles ne comptaient que selon le client : un filtre changeait le tableau, pas la pastille. *Payées* reste sans pastille. |
| 2026-10-23 | Démonstration (§7.1, demande de l'utilisateur) : les onze DSM validées d'Armagnacs de Gascogne sont toutes facturées et émises ; plus de brouillon au démarrage ; seule la DSM du Domaine de Laubade, non facturable, reste à facturer. Zeendoc : transmises, les factures émises avant le mois courant (payées, sauf celle de Gascogne émise il y a deux mois, impayée) ; à transmettre, celles du mois courant. `DemoFacturationIT` le vérifie. Démonstration des opérateurs : Hélène Lassus devient Hélène Larrieu. |
| 2026-10-23 | Tableau de bord du back-office (demande de l'utilisateur) : carte « Factures impayées », après « Factures à valider », pour qui lit la facturation : les factures **échues et impayées**, transmises à Zeendoc, non payées, échéance passée (RG-FAC-13, 33) ; affichée seulement s'il y en a, liseré rouge, comme « DSM à relancer » ; elle ouvre l'onglet *Émises*, où ces lignes (et leurs cartes) sont teintées de rouge pâle, comme dans l'onglet *Factures* de la fiche opérateur et dans la liste *Factures* du portail. La liste des pièces porte désormais l'échéance (`PieceListee.echeance`). |
| 2026-10-23 | Tableau de bord du portail (demande de l'utilisateur) : « Factures non payées » devient « Factures impayées », alignée sur le back-office : ses factures échues et impayées seulement (RG-FAC-13, 33), affichée s'il y en a, liseré rouge ; elle ouvre ses factures, où ces lignes sont teintées. |
| 2026-10-25 | Tableau de bord du back-office (demande de l'utilisateur) : ligne « Facturation » des activités rangées par thème : « Factures à valider », « Factures impayées », « Synchronisation Zeendoc » (état, démonstration) ; « Factures impayées » est désormais toujours affichée, en alerte (liseré rouge) seulement s'il y en a, pour garder la ligne entière. « DSM à facturer » est dans la ligne des DSM. |
| 2026-10-25 | Tableau de bord du portail (décision de l'utilisateur) : « Factures impayées » devient « Factures à régler », en tête à côté des nouveaux messages : ses factures émises pas encore payées (RG-FAC-33), toujours affichée ; en alerte (liseré rouge, triangle) si l'une est échue et impayée (RG-FAC-13). Elle ouvre ses factures, où ces lignes sont teintées. |
