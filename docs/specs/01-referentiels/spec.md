# 01 : référentiels

> Contexte `referentiels` (étape 1 de `docs/contextes.md`). Statut : **terminée** (validée le 2026-10-06), **implémentée** (rouverte le 2026-10-06 pour `MembreBnia`, spec 02 ; implémentée le même jour).
> Références : `p.N` = page du CDC v1.1, `AGI p.N` = page de la spec de reprise AGI.
> `H-REF-n` : hypothèse retenue, révisable. `Q-REF-n` : question ouverte, suivie dans cette spec (§10).

## Sommaire

- [1. Objet](#1-objet)
- [2. Périmètre](#2-périmètre)
  - [2.1 Dans le contexte](#21-dans-le-contexte)
  - [2.2 Hors du contexte (décisions du 2026-10-06)](#22-hors-du-contexte-décisions-du-2026-10-06)
- [3. Modèle](#3-modèle)
  - [3.1 Liste de valeurs simple](#31-liste-de-valeurs-simple)
  - [3.2 Pays](#32-pays)
  - [3.3 Région](#33-région)
  - [3.4 Contenance](#34-contenance)
  - [3.5 Classe de TVA (agrégat)](#35-classe-de-tva-agrégat)
- [4. Règles de gestion](#4-règles-de-gestion)
- [5. Cas d'usage](#5-cas-dusage)
  - [5.1 Écriture](#51-écriture)
  - [5.2 Lecture par les autres contextes : la partie publiée](#52-lecture-par-les-autres-contextes--la-partie-publiée)
- [6. Écrans](#6-écrans)
  - [6.1 Back-office](#61-back-office)
  - [6.2 Portail](#62-portail)
- [7. Données](#7-données)
  - [7.1 Démonstration (profil `demo`)](#71-démonstration-profil-demo)
  - [7.2 Reprise AGI](#72-reprise-agi)
- [8. Décisions d'architecture](#8-décisions-darchitecture)
- [9. Hypothèses](#9-hypothèses)
- [10. Questions ouvertes](#10-questions-ouvertes)
- [11. Tests attendus](#11-tests-attendus)
- [Journal](#journal)

## 1. Objet

Données de référence stables, administrées par le BNIA et lues par les autres contextes : géographie,
qualification des opérateurs et des contacts, présentation commerciale, classes et taux de TVA.

Le contexte ne contient aucun processus métier. Sa valeur tient dans trois choses :
- des valeurs **fiables** : codes uniques et corrigeables derrière un identifiant technique stable ;
  suppression d'une valeur inutilisée, désactivation sinon ;
- des **règles propres** : pas de chevauchement des taux de TVA, attributs du pays pour le certificat
  export, contenance exprimée en volume ;
- une **lecture simple** pour tous les autres contextes.

Sources : §4.11.1, 4.11.3 à 4.11.5 (p.52-53) · C.10.1-2 et C.10.4 à C.10.9 (p.127-131) ·
§4.2.5.3 (droits, p.16-17) · AGI 2.6 (p.13-15).
Usages relevés ailleurs : §4.3.2.3 (marques, p.19) · C.1.1.2 (région, p.102) · §4.7.5 (contenance, p.40) ·
§4.8.4-4.8.5 et C.7.2.1 (certificat, p.42 et 122-123) · §4.10 (UE, p.47) · AGI 2.1 (vues professionnel).

## 2. Périmètre

### 2.1 Dans le contexte

| Famille | Référentiel | Code (ADR-001) | Source |
|---|---|---|---|
| Géographie | Continents et zones commerciales | `ZoneCommerciale` | §4.11.3 p.52, C.10.4 p.128 |
| | Pays | `Pays` | §4.11.3 p.52, C.10.5 p.128 |
| | Régions | `Region` | §4.11.3 p.52, C.10.6 p.129 |
| Qualification | Titres | `Titre` | §4.11.4 p.53, C.10.8.1 p.129 |
| | Labels | `Label` | §4.11.4 p.53, C.10.8.3 p.130 |
| | Types de visite | `TypeVisite` | §4.11.4 p.53, C.10.8.4 p.130 |
| | Langues | `Langue` | §4.11.4 p.53, C.10.8.5 p.130 |
| | Centres d'intérêt | `CentreInteret` | §4.11.4 p.53, C.10.8.6 p.130 |
| | Membres BNIA | `MembreBnia` | C.1.1.1 p.101, décision du 2026-10-06 (spec 02) |
| Présentation et commercialisation | Marques | `Marque` | §4.11.5 p.53, C.10.9.1 p.131 |
| | Circuits de distribution | `CircuitDistribution` | §4.11.5 p.53, C.10.9.2 p.131 |
| | Produits commercialisés | `ProduitCommercialise` | §4.11.5 p.53, C.10.9.3 p.131 |
| | Contenances | `Contenance` | §4.11.5 p.53, C.10.9.4 p.131 |
| Fiscalité | Classes de TVA et leurs taux | `ClasseTva`, `TauxTva` | §4.11.1 p.52, C.10.1-2 p.127 |

### 2.2 Hors du contexte (décisions du 2026-10-06)

| Élément décrit dans le paramétrage du CDC | Contexte retenu | Motif |
|---|---|---|
| Taux de cotisation et d'exonération (§4.11.2, C.10.3) | `cotisation` | Paramètres de calcul, pas des valeurs de référence. Déjà prévu dans `contextes.md`. |
| Destinataires export (C.10.7) | `exportation` | Tiers avec adresse et pays, seul l'export s'en sert ; un objet avec un cycle de vie, pas une liste de valeurs. |
| Types tiers (§4.11.4, C.10.8.2) | `exportation` | Dans le CDC cible, la liste ne qualifie que le destinataire export (C.10.7.1). Dans InteGraal, c'était le type de la fiche professionnel (AGI 2.1.1) : voir Q-REF-1. |
| Produits pour la facturation manuelle (§4.11.5) | `boutique`, **à raffiner** | Le CDC les décrit pourtant en facturation : « Référentiel produits » (§4.9.2 p.44) et « Produit de facturation » (C.8.2.1 : référence, libellé, taux de TVA, actif), distinct du produit boutique (C.12.2). Suivi dans `contextes.md` §5. |
| Type d'opérateur | `operateurs` | Liste fermée qui pilote des règles : forme du code, périodicité, colonnes de DSM, assiette de cotisation (C.1.1.1 p.101). Elle ne peut pas être administrable. |
| Devises, flacons (AGI 2.5.1-2.5.2 p.12) | aucun pour l'instant | Présents dans la vue AGI des contrats export, absents du CDC cible. Voir Q-REF-4. |

## 3. Modèle

### 3.1 Liste de valeurs simple

Dix référentiels sur quatorze ont la même forme : `Titre`, `Label`, `TypeVisite`, `Langue`, `CentreInteret`,
`Marque`, `CircuitDistribution`, `ProduitCommercialise`, `ZoneCommerciale` et `MembreBnia` (ajouté le
2026-10-06 : C.1.1.1 type le champ « Référentiel » sans qu'aucune liste n'existe en C.10 ; spec 02). Chacun est une classe à
part entière, avec son repository, sa table et son endpoint (A-REF-3) : chaque liste pourra diverger
sans refonte.

| Champ | Type | Règle |
|---|---|---|
| code | texte | Obligatoire, unique dans le référentiel, modifiable (RG-REF-01, RG-REF-02) |
| libellé | texte | Obligatoire, modifiable |
| actif | booléen | Vrai à la création ; voir RG-REF-04 |

Chaque valeur, dans tous les référentiels, a aussi un identifiant technique attribué par la base : c'est
lui que les autres contextes référencent (A-REF-1, ADR-011).

Le CDC ne prévoit l'indicateur actif que sur les centres d'intérêt (C.10.8.6) et les classes de TVA
(C.10.1.1). On le généralise à tous les référentiels (RG-REF-04, décision du 2026-10-06).
La marque ajoute une règle propre : un libellé unique (RG-REF-15), car les opérateurs en créent depuis le
portail (RG-REF-14).

### 3.2 Pays

| Champ | Type | Règle | Source |
|---|---|---|---|
| code pays | texte | Code métier du BNIA, unique, modifiable | C.10.5.1 |
| code ISO | texte | ISO 3166-1 alpha-2, unique (H-REF-1) | C.10.5.1 |
| libellé | texte | Obligatoire | C.10.5.1 |
| zone commerciale | référence `ZoneCommerciale` | Obligatoire, active à la saisie ; continent géographique (H-REF-5) | C.10.5.1 |
| membre de l'UE | booléen | Obligatoire, faux par défaut (H-REF-5) | §4.10 p.47, §4.9 p.45 |
| langue du certificat | `FR_EN`, `FR_ES`, `FR_IT` | Obligatoire, `FR_EN` par défaut (H-REF-2) | §4.8.4 p.42, C.7.2.1 p.122 |
| niveau de détail du certificat | `CUMUL_DEGRE`, `DETAIL_COMPTE_AGE`, `DETAIL_MILLESIME` | Obligatoire, `CUMUL_DEGRE` par défaut (H-REF-2) | §4.8.5 p.42, C.7.2.1 p.123 |
| âge minimum à l'export | `CompteDAge` (`commun.domain`) | Facultatif (H-REF-3, A-REF-4) | §4.11.3 p.52, C.10.5.1 |
| actif | booléen | | RG-REF-04 |

Le « modèle de certificat associé » du §4.11.3 est donc décomposé en deux attributs : la langue et le
niveau de détail (décision du 2026-10-06). Le niveau de détail suit les trois valeurs de C.7.2.1, plus
précises que les deux du §4.8.5 : `CUMUL_DEGRE` correspond au certificat simplifié (une ligne par degré),
`DETAIL_COMPTE_AGE` et `DETAIL_MILLESIME` au certificat détaillé, par compte d'âge ou par millésime
(décision du 2026-10-06). Le sens des champs CA à CA5 d'InteGraal reste à confirmer (Q-REF-2).

### 3.3 Région

| Champ | Type | Règle | Source |
|---|---|---|---|
| code | texte | Unique, modifiable | C.10.6.1 |
| libellé | texte | Obligatoire | C.10.6.1 |
| en région délimitée | booléen | Obligatoire | C.1.1.2 p.102, décision du 2026-10-06 |
| actif | booléen | | RG-REF-04 |

C.1.1.2 demande de distinguer « région Armagnac et hors région » sur la fiche opérateur. La DSM
distingue aussi les flux « en » et « hors région délimitée » (colonnes 7, 8, 13, 14, 20 et 21). Cette
distinction devient un attribut de la région.

### 3.4 Contenance

| Champ | Type | Règle | Source |
|---|---|---|---|
| volume | décimal, en litres | Strictement positif, unique, modifiable, 3 décimales au plus | C.10.9.4 p.131, §4.7.5 p.40 |
| libellé | texte | Facultatif ; sinon dérivé du volume (« 0,70 L ») | |
| actif | booléen | | RG-REF-04 |

Le CDC la décrit comme un « texte court ». Mais §4.7.5 calcule le volume d'alcool pur des ventes export
« à partir des quantités, contenances et degrés » : la contenance est donc un volume (décision du
2026-10-06). Elle est stockée en `BigDecimal`, jamais en flottant, comme le volume d'alcool pur
(glossaire).

### 3.5 Classe de TVA (agrégat)

| Champ | Type | Règle | Source |
|---|---|---|---|
| code | texte | Unique, modifiable | C.10.1.1 p.127 |
| libellé | texte | Obligatoire | C.10.1.1 |
| active | booléen | | C.10.1.1 |
| taux | liste de `TauxTva` | Voir ci-dessous | C.10.2.1 p.127 |

`TauxTva` est un objet-valeur de l'agrégat :

| Champ | Type | Règle |
|---|---|---|
| taux | pourcentage décimal | Entre 0 et 100 inclus, 2 décimales au plus |
| date de début | date | Obligatoire |
| date de fin | date | Facultative (taux en vigueur sans terme) ; si présente, ≥ date de début |

Les taux d'une classe ne se chevauchent pas (RG-REF-11). Les trous sont permis : le CDC ne les
interdit pas, et une date sans taux applicable reçoit une réponse explicite (RG-REF-12).

## 4. Règles de gestion

| Id | Règle | Source |
|---|---|---|
| RG-REF-01 | Le code d'une valeur est obligatoire et unique dans son référentiel, sans distinction de casse ni d'espaces en tête et en fin, à la création comme à la modification. Exception : la contenance n'a pas de code, son volume est unique (RG-REF-10). | C.10 (« Identifiant ») |
| RG-REF-02 | Les autres contextes référencent une valeur par son identifiant technique (A-REF-1). Le code (le volume pour une contenance) se corrige comme le libellé, et reste unique (RG-REF-01, RG-REF-10). | Décision du 2026-10-06 (ADR-011), remplace H-REF-4 |
| RG-REF-03 | Le libellé est obligatoire. | C.10 |
| RG-REF-04 | Une valeur que rien ne référence peut être supprimée. Une valeur utilisée ailleurs ne l'est pas : elle est désactivée, et peut être réactivée. Une valeur inactive n'est plus proposée à la saisie, mais reste lisible et affichée là où elle est déjà utilisée. | Décisions du 2026-10-06 (ADR-011) ; C.10.8.6, C.10.1.1 |
| RG-REF-05 | Un pays est rattaché à une zone commerciale active au moment de la saisie. Désactiver une zone ne touche pas les pays qui y sont rattachés. | C.10.5.1 |
| RG-REF-06 | Le code ISO d'un pays est unique et respecte le format alpha-2 (deux lettres majuscules). | H-REF-1 |
| RG-REF-07 | Un pays porte une langue de certificat (`FR_EN`, `FR_ES`, `FR_IT`) et un niveau de détail (`CUMUL_DEGRE`, `DETAIL_COMPTE_AGE`, `DETAIL_MILLESIME`). | §4.8.4, §4.8.5 p.42, C.7.2.1 p.122-123 |
| RG-REF-08 | Un pays porte un indicateur « membre de l'UE », indépendant de sa zone commerciale. Les statistiques distinguent l'UE du reste de l'Europe en combinant les deux. | §4.10 p.47, H-REF-5 |
| RG-REF-09 | L'âge minimum à l'export, s'il est renseigné, est un `CompteDAge` valide (`00`, `0`, `1`, `2`…) ; la validation appartient à `CompteDAge` (`commun.domain`). | H-REF-3, A-REF-4 |
| RG-REF-10 | Le volume d'une contenance est strictement positif et unique dans le référentiel. | §4.7.5 p.40 |
| RG-REF-11 | Deux taux d'une même classe de TVA ne peuvent pas avoir de période commune. Un taux sans date de fin couvre toutes les dates postérieures à son début. | §4.11.1 p.52 |
| RG-REF-12 | Le taux applicable d'une classe à une date est le seul taux dont la période contient cette date. S'il n'y en a aucun, la réponse est « aucun taux applicable », jamais une valeur par défaut. | §4.11.1 p.52 |
| RG-REF-13 | Une classe de TVA inactive n'est plus proposée à la saisie, mais ses taux restent consultables, y compris à une date donnée (factures et avoirs anciens). | C.10.1.1, RG-REF-04 |
| RG-REF-14 | Une marque peut être créée depuis la fiche opérateur, au back-office **comme au portail** par l'opérateur, avec les mêmes règles que depuis le paramétrage : code saisi (RG-REF-01), libellé unique (RG-REF-15). | §4.3.2.3 p.19, décision du 2026-10-06 (Q-REF-7) |
| RG-REF-15 | Deux marques ne peuvent pas avoir le même libellé à la casse, aux accents et aux espaces près (« Château X » = « CHATEAU  X »). Une marque inactive bloque aussi son libellé : on la réactive au lieu d'en recréer une. Pendant la saisie, le formulaire propose les marques au libellé proche. | Décision du 2026-10-06 |

## 5. Cas d'usage

### 5.1 Écriture

| Cas d'usage | Référentiels | RG |
|---|---|---|
| Lister les valeurs d'un référentiel, avec un filtre actif / inactif et une recherche sur le code ou le libellé (back-office) | tous | |
| Créer une valeur (back-office) | tous | 01, 03, 05 à 10, 15 |
| Modifier une valeur, code compris (back-office) | tous | 01, 02, 03, 05 à 10, 15 |
| Désactiver, réactiver une valeur (back-office) | tous | 04, 13 |
| Supprimer une valeur que rien n'utilise (back-office) | tous | 04 |
| Ajouter, modifier ou retirer un taux d'une classe de TVA (back-office) | `ClasseTva` | 11 |
| Créer une marque depuis la fiche opérateur (back-office et portail) | `Marque` | 01, 14, 15 |

Retirer un taux de TVA n'est pas une suppression de valeur de référentiel. C'est la correction d'une
erreur de saisie dans l'historique d'une classe. Les contextes qui calculent un montant (`cotisation`,
`facturation`) **figent** le taux appliqué sur leur propre document ; ils ne dépendent donc pas de la
stabilité de cet historique (C.8.3 « taux de TVA appliqué », §4.9.7 p.46).

### 5.2 Lecture par les autres contextes : la partie publiée

`referentiels` est le seul contexte que les autres lisent directement, sans port ni adapter de leur côté
(A-REF-2). Ils n'en voient qu'une **partie publiée, en lecture seule** :
- `fr.bnia.referentiels.domain.partage` : les types de valeur que les autres domaines
  manipulent, comme `LangueCertificat`, `NiveauDetailCertificat` et les vues immuables (`PaysVue`,
  `RegionVue`, `ContenanceVue`, `ValeurVue`, `TauxTvaVue`) ;
- `fr.bnia.referentiels.application.partage` : l'interface `ReferentielsLecture`,
  implémentée dans `referentiels`.

Pour chaque liste, `ReferentielsLecture` lit une valeur par son identifiant (celui que les autres
contextes stockent), par son code (imports et reprise, dont les fichiers portent des codes) et la liste
des valeurs actives (ADR-011).

Tout le reste du contexte (agrégats, cas d'usage d'écriture, adapters) est interdit aux autres contextes,
ce que vérifie ArchUnit. La création d'une marque depuis la fiche opérateur (RG-REF-14) passe par l'écran,
qui appelle le cas d'usage de `referentiels` ; elle n'a pas besoin d'être publiée.

| Besoin | Consommateur probable |
|---|---|
| Valeurs actives d'un référentiel, pour une liste de choix | tous les écrans de saisie |
| Une valeur par son code, active ou non, pour l'affichage | tous |
| Pays : zone commerciale, membre de l'UE, langue et niveau de détail du certificat, âge minimum | `exportation`, `statistiques` |
| Région : indicateur de région délimitée | `operateurs` |
| Membre BNIA | `operateurs` |
| Contenance : volume en litres | `exportation` |
| Taux applicable d'une classe de TVA à une date (RG-REF-12) | `cotisation`, `facturation`, `boutique` |

## 6. Écrans

### 6.1 Back-office
- **Menu « Paramétrage »**, avec trois groupes : Géographie, Qualification, Présentation et
  commercialisation, plus une entrée « TVA ».
- **Une page par référentiel** : tableau (code, libellé, attributs propres, état), recherche, filtre
  actif / inactif, création et modification dans une fenêtre, actions désactiver / réactiver et
  supprimer. Le code se corrige comme le libellé. Une suppression est refusée si la valeur est utilisée
  ailleurs : on la désactive alors.
- **Page Pays** : colonnes zone, membre de l'UE, code ISO, langue et niveau de détail du certificat,
  âge minimum.
- **Page Marques** : pendant la saisie du libellé, les marques au libellé proche sont proposées
  (RG-REF-15). Même composant dans la fiche opérateur, au back-office et au portail.
- **Page Classes de TVA** : liste des classes ; le détail d'une classe montre ses taux par date de
  début décroissante, avec le taux en vigueur mis en évidence. Un chevauchement est refusé avec un
  message qui nomme le taux en conflit.
- **Droits** (§4.2.5.3 p.16-17) : « Référentiels métier : Gérer », « Taux de TVA : Gérer ». Ils sont
  appliqués quand le contexte `acces` existera (étape 3). D'ici là, toute personne connectée au
  back-office administre.

### 6.2 Portail
Aucun écran propre. Le portail ne voit les référentiels qu'à travers les listes de choix des autres
contextes, en lecture, et seulement les valeurs actives. Seule exception : l'opérateur peut créer une
marque depuis sa fiche (RG-REF-14, RG-REF-15), en saisissant son code et son libellé.

## 7. Données

### 7.1 Démonstration (profil `demo`)
Des valeurs plausibles, présentées comme telles :
- les zones commerciales : les continents (Europe, Amérique du Nord, Amérique du Sud, Asie, Afrique,
  Océanie) ;
- une trentaine de pays exportateurs réalistes, avec une langue de certificat cohérente (Espagne en
  `FR_ES`, Italie en `FR_IT`, les autres en `FR_EN`) et l'indicateur UE à jour ;
- quelques régions, avec et sans l'indicateur de région délimitée ;
- les contenances usuelles : 0,05, 0,20, 0,35, 0,50, 0,70, 0,75, 1,00, 1,50 et 3,00 L ;
- une classe de TVA « Normal » à 20 % depuis le 01/01/2014.
- quelques membres BNIA plausibles (valeurs réelles inconnues : Q-OPE-4 de la spec 02).

Les valeurs réelles viendront de la reprise.

### 7.2 Reprise AGI

| Référentiel | Vue AGI | Correspondance |
|---|---|---|
| Pays | 2.6.1 (p.13) | code pays, code ISO, libellé, continent → zone ; « Âge » → âge minimum ; indicateur UE à fixer à la reprise ; CA, CA1 à CA5 : Q-REF-2 |
| Régions | 2.6.2 (p.13) | code, libellé ; indicateur de région délimitée à fixer à la main (Q-REF-3) |
| Titres | 2.6.3 (p.13) | direct |
| Labels | 2.6.5 (p.14) | direct |
| Types de visite | 2.6.6 (p.14) | direct |
| Contenances | 2.6.8 (p.14) | « valeur de contenance » → volume en litres : format source inconnu (Q-REF-5) |
| Langues | 2.6.9 (p.14) | direct |
| Marques, circuits de distribution, types de produits commercialisés | **aucune vue** : les vues professionnel 2.1.5, 2.1.6 et 2.1.11 ne portent que l'identifiant de la table InteGraal | demander à AGI une vue de paramétrage par table (identifiant, code, libellé) : Q-REF-6. Produits commercialisés : on suit le CDC, une liste de types (Armagnac, Blanche, Floc…) ; les libellés libres par opérateur d'InteGraal (2.1.11) ne sont pas repris (Q-REF-9) |
| Centres d'intérêt, zones commerciales, classes de TVA | aucune vue | saisie initiale par le BNIA |
| Membres BNIA | **aucune vue** : la fiche professionnel (AGI 2.1.1 p.4) la lie à « paramétrage - membres BNIA » | vue demandée à AGI (Q-REF-6) ; sens du champ demandé au BNIA (Q-OPE-4, spec 02) |

Chaque code AGI est traduit en identifiant à la reprise, par la lecture par code (ADR-011).

Hors du contexte : types tiers (2.6.4) et destinataires (2.6.10) vont à `exportation`, cotisations (2.6.7)
à `cotisation`.

## 8. Décisions d'architecture

Décidées le 2026-10-06, actées par ADR-008 et ADR-009 ; A-REF-1 révisée le même jour par ADR-011.

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-REF-1 | Un contexte référence une valeur de `referentiels` par son **identifiant technique** (entier généré par la base), avec une clé étrangère. Le code, et le volume d'une contenance, se corrigent (RG-REF-02). | Référence par code métier immuable, première version de cette décision : erreurs incorrigeables, aucune suppression possible, démonstration rigide. UUID : illisible, sans gain pour une seule base. | 011 (remplace 008 sur ce point) |
| A-REF-2 | `referentiels` publie une **partie en lecture seule** (`..referentiels.domain.partage`, `..referentiels.application.partage`), que tous les contextes peuvent importer directement. Exception réservée à ce contexte générique ; les autres ne communiquent que par ports et événements. | Port et adapter dans chaque consommateur (passe-plat pour 13 listes) ; SQL direct sur les tables d'un autre contexte (couplage caché) ; interfaces dans `commun` (le noyau perdrait sa neutralité) ; shared kernel complet (exposerait les écritures et la structure des agrégats). | 008 |
| A-REF-3 | **Une classe par liste** simple, avec son repository, sa table et son endpoint. | Modèle générique typé par une énumération : moins de code, mais des listes qui ne peuvent pas diverger. | 008 |
| A-REF-4 | `CompteDAge` vit dans **`commun.domain`**, avec les autres concepts transverses du glossaire qui sont des règles et non des données administrées (`Millesime`, `Campagne`, `MentionCommerciale`, `VolumeAlcoolPur`, au fil des besoins). | Le placer dans `referentiels`, qui ne contient que des données administrées par le BNIA ; une version par contexte (règle dupliquée). | 009 |

Deux zones partagées, de nature différente :

| | `commun.domain` | `referentiels` (partie publiée) |
|---|---|---|
| Contenu | Concepts et règles : `CompteDAge`, `Millesime`, `VolumeAlcoolPur`… | Données administrées : pays, contenances, taux de TVA… |
| Évolution | Par le code, rarement | Par le BNIA, à l'écran |
| Accès | Import libre | Lecture seule, par la partie publiée |

## 9. Hypothèses

Statut au 2026-10-06 : toutes validées, sauf H-REF-4, abandonnée. Elles restent révisables si le BNIA dit
autre chose.

| Id | Hypothèse | Pourquoi |
|---|---|---|
| H-REF-1 | Le code ISO est l'alpha-2 de l'ISO 3166-1. | Le CDC dit « code normalisé » sans préciser. L'alpha-2 est le plus courant (douanes, Otentik probable). |
| H-REF-2 | Valeurs par défaut du pays : `FR_EN` et `CUMUL_DEGRE`. | Le CDC cite le français associé à l'anglais, l'espagnol ou l'italien (C.7.2.1) ; l'anglais est le cas général. |
| H-REF-3 | L'âge minimum est un compte d'âge (`00`, `0`, `1`…), pas un entier. | C.10.5.1 dit « Nombre », mais il se compare aux comptes d'âge des ventes export, où `00` et `0` sont distincts (glossaire). |
| ~~H-REF-4~~ | ~~Le code est immuable.~~ **Abandonnée le 2026-10-06** : le code se corrige, les références passent par l'identifiant technique (ADR-011). | L'erreur de saisie est courante ; il faut pouvoir revenir sur ce que l'on fait. |
| H-REF-5 | La zone commerciale du pays est son continent géographique ; l'appartenance à l'UE est un indicateur distinct sur le pays. | Les statistiques distinguent l'UE du reste de l'Europe (§4.10 p.47), mais le pays n'a qu'une zone (C.10.5.1). Deux zones « UE » et « Europe hors UE » auraient rendu le continent non géographique. |

## 10. Questions ouvertes

Toutes passées en revue le 2026-10-06. Celles qui restent ouvertes partent au BNIA ou à AGI ; aucune ne
bloque le prototype, qui s'appuie sur les décisions et hypothèses ci-dessus.

| Id | Question | Bloque |
|---|---|---|
| Q-REF-1 | **Au BNIA** : la liste « types tiers » sert-elle à autre chose qu'à qualifier les destinataires export ? Ses valeurs actuelles sont-elles les types d'opérateur d'InteGraal (AGI 2.1.1) ? | La reprise du type d'opérateur |
| Q-REF-2 | **Au BNIA** : que contiennent les champs CA et CA1 à CA5 de la vue Pays (AGI 2.6.1) ? Probablement les modèles de certificat autorisés : la vue contrat export porte des booléens « Certificat C.O Fr/A », « C.A Fr/A », « C.O Fr/Es », « C.C Fr/It », « Type 5 » (AGI p.12). Le niveau de détail, lui, est tranché (§3.2). | La reprise des pays |
| Q-REF-3 | **Au BNIA** : quelles sont les régions actuelles, et lesquelles sont « en région délimitée » ? | La reprise des régions |
| Q-REF-4 | **Au BNIA** : les devises et les flacons d'InteGraal (AGI p.12) sont-ils abandonnés dans la cible ? Le CDC n'en parle pas. | La reprise des contrats export |
| Q-REF-5 | **Au BNIA** : sous quelle forme la contenance est-elle stockée dans InteGraal (texte, centilitres, litres) ? | La reprise des contenances |
| Q-REF-6 | **Demande à AGI** : cinq vues de paramétrage manquent à la spec AGI (identifiant, code, libellé) : Marques, Circuits de distribution, Types de produits commercialisés, Médailles et concours, Membres BNIA. Les vues professionnel 2.1.1, 2.1.5, 2.1.6, 2.1.10 et 2.1.11 n'en donnent que l'identifiant. | La reprise des onglets Marques et Œnotourisme, et du membre BNIA |
| Q-REF-9 | **Au BNIA** : la cible suit le CDC (produits commercialisés = liste de types à cocher). Les libellés libres saisis par opérateur dans InteGraal (AGI 2.1.11) ne seront pas repris. Est-ce acceptable ? | La reprise |
| ~~Q-REF-10~~ | InteGraal gère des médailles et concours rattachés aux fiches (AGI 2.1.10). **Corrigée le 2026-10-06** : elles figurent bien dans le CDC cible (§4.3.2.5 p.20, C.1.7.1 p.104). **Tranché le 2026-10-06 (spec 02)** : lignes semi-structurées dans `operateurs` (concours, année, distinction, produit), sans référentiel. | |
| ~~Q-REF-7~~ | Un opérateur peut-il créer une marque depuis le portail ? **Tranché le 2026-10-06 : oui** (RG-REF-14, RG-REF-15). À confirmer par le BNIA. | |
| ~~Q-REF-8~~ | L'âge minimum d'un pays bloque-t-il la vente export ou le certificat ? **Tranché le 2026-10-06 : alerte non bloquante** à la saisie, à l'import et à la génération du certificat, mise en œuvre dans `exportation`. À confirmer par le BNIA. | |

## 11. Tests attendus

Chaque RG-REF-nn a au moins un test qui porte son identifiant (CLAUDE.md).
- **Tests de domaine**, sans base : règles portées par les objets (RG-REF-02, 03, 04, 06 pour le format,
  07, 08, 09, 10 pour la positivité, 11, 12, 13).
- **Tests de cas d'usage**, avec repository simulé, puis IT : règles d'unicité ou qui consultent d'autres
  valeurs (RG-REF-01, 05, 06 et 10 pour l'unicité, 14, 15).

## Journal

| Date | Changement |
|---|---|
| 2026-10-06 | Première rédaction. Décisions : TVA dans `referentiels` ; destinataires et types tiers dans `exportation` ; produits de facturation manuelle dans `boutique` (à raffiner) ; désactivation au lieu de suppression ; pays avec langue et niveau de détail du certificat ; contenance en litres ; région avec indicateur de région délimitée. |
| 2026-10-06 | Décisions d'architecture A-REF-1 à 4 (§8) : référence par code métier, partie publiée en lecture seule, une classe par liste, `CompteDAge` dans `commun.domain`. |
| 2026-10-06 | Hypothèses validées. Pays : indicateur « membre de l'UE », zone = continent. Marque créable par l'opérateur au portail, code saisi, libellé unique normalisé (RG-REF-15). Âge minimum : alerte non bloquante (dans `exportation`). |
| 2026-10-06 | Questions passées en revue avec l'utilisateur. Niveau de détail du certificat : trois valeurs (C.7.2.1). Q-REF-1, 2, 3, 4, 5 : au BNIA. Q-REF-6 : quatre vues demandées à AGI (manque de la spec AGI). Produits commercialisés : on suit le CDC (Q-REF-9 au BNIA). Médailles et concours : au BNIA (Q-REF-10). |
| 2026-10-06 | Relecture : RG renumérotées (01 à 15, sans 07a) ; contenance identifiée par son volume ; indicateur UE reporté dans la lecture et les écrans ; cas d'usage, tests et reprise mis en cohérence. |
| 2026-10-06 | Sommaire ajouté en tête. |
| 2026-10-06 | Spec validée, statut « terminée ». ADR-008 (partie publiée, référence par code, une classe par liste) et ADR-009 (noyau commun du domaine) rédigés. |
| 2026-10-06 | Implémentation (lots 1 à 7). Choix faits pendant l'implémentation : listes simples sur une classe abstraite par couche (`ValeurSimple`, `GererValeursSimples`…), une sous-classe par liste (A-REF-3) ; `Pays`, `Region` et `ClasseTva` étendent `ValeurSimple` ; la contenance a son propre modèle, identifiée par son `Volume` (`numeric(10,3)`) ; libellé normalisé des marques stocké avec un index unique (RG-REF-15) ; marques proches : recherche « contient » sur le libellé normalisé ; non-chevauchement des taux de TVA garanti par l'agrégat seul ; `ReferentielsLecture` à méthodes typées, classes de TVA comprises ; un schéma PostgreSQL `referentiels`. Chaque RG-REF-01 à 15 a au moins un test qui porte son identifiant. |
| 2026-10-06 | Révision (ADR-011) : référence par identifiant technique au lieu du code métier (A-REF-1) ; code et volume modifiables, toujours uniques (RG-REF-01, RG-REF-02) ; suppression d'une valeur inutilisée, désactivation sinon (RG-REF-04) ; H-REF-4 abandonnée ; `ReferentielsLecture` par identifiant et par code. Motif : l'erreur de saisie est courante, et la démonstration exige de pouvoir corriger. |
| 2026-10-06 | Rouverte par la spec 02 : nouvelle liste simple `MembreBnia` (§2.1, §3.1, §5.2, §7.2), cinquième vue demandée à AGI (Q-REF-6), Q-REF-10 corrigée (les médailles sont dans le CDC) et tranchée. À implémenter. |
| 2026-10-06 | `MembreBnia` implémenté (lot 1 de l'implémentation de `operateurs`) : liste simple sur le patron commun (A-REF-3), table `referentiels.membre_bnia`, `ReferentielsLecture` (par identifiant, par code, actifs), page « Membres BNIA » du groupe Qualification, trois valeurs fictives en démonstration (Q-OPE-4). |
