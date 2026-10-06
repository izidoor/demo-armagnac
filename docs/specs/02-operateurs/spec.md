# 02 : opérateurs

> Contexte `operateurs` (étape 2 de `docs/contextes.md`). Statut : **terminée** (validée le 2026-10-06), **implémentée** (lots 1 à 9).
> Références : `p.N` = page du CDC v1.1, `AGI p.N` = page de la spec de reprise AGI.
> `H-OPE-n` : hypothèse retenue, révisable. `Q-OPE-n` : question ouverte, suivie dans cette spec (§10).

## Sommaire

- [1. Objet](#1-objet)
- [2. Périmètre](#2-périmètre)
  - [2.1 Dans le contexte](#21-dans-le-contexte)
  - [2.2 Hors du contexte](#22-hors-du-contexte)
- [3. Modèle](#3-modèle)
  - [3.1 Opérateur (agrégat)](#31-opérateur-agrégat)
  - [3.2 Contact (dans l'agrégat `Operateur`)](#32-contact-dans-lagrégat-operateur)
- [4. Règles de gestion](#4-règles-de-gestion)
- [5. Cas d'usage](#5-cas-dusage)
  - [5.1 Écriture](#51-écriture)
  - [5.2 Lecture](#52-lecture)
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

Référentiel des structures suivies par le BNIA (producteurs, négociants, autres structures) et des
personnes à qui s'adresser dans chacune : identité, codification, identifiants administratifs,
paramètres déclaratifs, production, diffusion des millésimes, présentation commerciale et
œnotouristique, autorisation statistique, contacts et leurs droits au portail.

Le contexte ne déclare rien, ne calcule aucune cotisation, et ne gère ni mot de passe ni connexion.
Sa valeur tient dans trois choses :
- une **fiche fiable**, que tous les autres contextes référencent : code attribué automatiquement et
  unique, type d'opérateur qui pilote les DSM et la cotisation, périodicité et statut déclaratif ;
- des **contacts** qui portent leurs droits au portail, rattachés à un compte unique par e-mail ;
- une **lecture** pour les autres contextes et une **recherche restreinte** pour le portail.

Sources : §4.3 (p.18-21) · §5.13 (p.78-79) · C.1 (p.100-105) · AGI 2.1 (p.4-8).
Usages relevés ailleurs : §4.4.2, 4.4.3, 4.4.6, 4.4.8 à 4.4.10 (DSM, p.22-28) · §4.5 et §5.5.2 (contrats,
p.30-34 et 67) · §4.7 (vendeur export, p.38-40) · §4.8.4 (expéditeur du certificat, p.42) · C.8.1.1 (tiers
facturé, p.123) · §4.10, §5.11 (statistiques, p.47-51 et 75) · §5.1 et C.13 (compte portail, p.59-60 et
135) · C.14 (destinataires des notifications, p.136) · §6.1.4 (ODG, p.81) · §7.1.1 (reprise, p.84-85) ·
§1.4 (annuaire hors périmètre, p.6) · AGI 3.8 (utilisateurs de l'extranet, p.22).

## 2. Périmètre

### 2.1 Dans le contexte

| Famille | Élément | Code (ADR-001) | Source |
|---|---|---|---|
| Identité | Opérateur : type, code, groupe, CVI, SIRET, accise, bureau des douanes (texte), identifiant tiers ODG, titre, raison sociale, nom commercial, contact principal, adresse, pays, téléphones, e-mail général, destinataire des factures, sites, coordonnées GPS, membre BNIA, accord de diffusion général, commentaire interne, état actif | `Operateur` | §4.3.2 p.18, C.1.1.1 p.100-101, C.8.1.1 p.123 |
| | Type d'opérateur, liste fermée | `TypeOperateur` | C.1.1.1 p.100, spec 01 §2.2 |
| | Code opérateur BNIA, attribué automatiquement | `CodeOperateur` | §4.3.5 p.21 |
| Déclaratif | Périodicité DSM | `PeriodiciteDsm` | C.1.1.1 p.101, §4.4.10 p.28 |
| | Statut de déclaration de stock | `StatutDeclarationStock` | C.1.1.2 p.102 |
| Production | Région, appellations, détenteur alambic | | §4.3.2.1 p.19, C.1.1.2 p.102 |
| | Alambics (agrément, type, capacité) | `Alambic` | C.1.2 p.102, AGI 2.1.2 p.5 |
| Diffusion | Niveau d'accord de diffusion des millésimes, millésimes diffusables | `DiffusionMillesimes` | §4.3.2.2 p.19, C.1.3 p.102 |
| | Accès aux statistiques du portail | | §4.3.2.6 p.20, C.1.8 p.104 |
| Présentation | Marques France et Export (diffusion web), circuits de distribution par portée | `PresentationOperateur` | §4.3.2.3 p.19, C.1.4 p.103 |
| | Visites et labels, avec commentaire | | §4.3.2.4 p.19, C.1.5-1.6 p.103 |
| | Œnotourisme : paiement et cartes, dégustation, lieu de vente, groupes, langues, mode de présentation, produits commercialisés, adhésion, présentations FR et EN | | §4.3.2.5 p.20, C.1.7 p.104 |
| | Médailles et concours, semi-structurés (concours, année, distinction, produit) | `MedailleConcours` | §4.3.2.5 p.20, C.1.7.1 p.104, décision du 2026-10-06 |
| Contacts | Contact : identité, fonction, centres d'intérêt, coordonnées, observation, état | `Contact` | §4.3.4 p.20-21, C.1.9.1 p.104-105 |
| | Droits au portail du contact : accès activé, administrateur, modules | `ModulePortail` | C.1.9.2 p.105, §5.13.2 p.78, décision du 2026-10-06 |
| | Journal des modifications d'un contact | | §5.13.3 p.79 |
| Échanges | Événement « accès portail accordé », écouté par `acces` | | §5.13.3 p.79, décision du 2026-10-06 |
| | Recherche restreinte d'opérateurs au portail (autre partie d'un contrat) | | §5.5.2 p.67, décision du 2026-10-06 |
| | Lecture d'un opérateur par les autres contextes (forme : phase E) | | C.3 à C.8, C.13 |
| | Export Excel du listing filtré | | §4.2.6 p.17, décision du 2026-10-06 |

**Décisions de périmètre du 2026-10-06.**
- **Contact et compte portail.** Une personne = un e-mail = un compte portail ; le compte est
  rattaché à plusieurs opérateurs (§5.1.4 p.60, §5.13.2 p.78), et la personne a **un contact dans
  chaque opérateur**. Les droits (accès activé, administrateur, modules) sont portés par le contact,
  dans `operateurs` (C.1.9.2). `acces` porte le compte (e-mail, mot de passe, double authentification,
  blocage) et trouve les opérateurs accessibles par les contacts actifs, à accès activé, de même e-mail.
  Le C.13.2, qui place rôle et modules sur le rattachement du compte, n'est donc pas suivi.
- **Données de présentation publique** (onglets Marques, Visites et labels, Œnotourisme ; GPS, sites,
  accord de diffusion) : saisies et consultables, **sans flux** vers armagnac.fr, dont la fiche annuaire
  est hors périmètre (§1.4 p.6). Voir Q-OPE-1.
- **Bureau des douanes** : texte libre, contrairement au typage « Référentiel » de C.1.1.1. Aucune liste
  n'existe au paramétrage (§4.11, C.10) et InteGraal le stocke sans liaison (AGI 2.1.1 p.5).
- **Membre BNIA** : référence vers une nouvelle liste simple `MembreBnia` de `referentiels` (spec 01
  rouverte, phase D). Sens à préciser : Q-OPE-4.
- **Médailles et concours** : lignes semi-structurées dans `operateurs`, pas de référentiel.
- **Historique** : un journal par contact, avec le détail des champs changés. La fiche opérateur n'est
  pas historisée (le CDC ne le demande pas).
- **Activation** : `operateurs` publie un événement quand un contact reçoit l'accès ; `acces` crée le
  compte s'il n'existe pas et envoie l'e-mail d'activation (§5.1.2 p.59).
- **Recherche au portail** : opérateurs actifs seulement, trois caractères au moins, vingt résultats au
  plus ; code, raison sociale, nom commercial et ville, rien d'autre. Voir Q-OPE-5.
- **ODG** : le code opérateur BNIA n'est pas une clé partagée avec l'ODG. Il reste modifiable, comme
  les codes des référentiels (ADR-011).

### 2.2 Hors du contexte

| Élément décrit dans le CDC | Contexte retenu | Motif |
|---|---|---|
| Compte portail : e-mail de connexion, mot de passe, double authentification, blocage, journal des connexions (§5.1 p.59-60, C.13.1 p.135) | `acces` | Sécurité et authentification. Les droits restent sur le contact (décision du 2026-10-06). |
| Création du compte et e-mail d'activation (§5.13.3 p.79, §5.1.2 p.59) | `acces` | Déclenchés par l'événement « accès portail accordé » de `operateurs`. |
| Dernière déclaration créée et saisie (C.1.1.1 p.102), état de la dernière déclaration et historique des DSM, factures, contrats (§4.3.3 p.20) | `declarations`, `facturation`, `contratsarmagnac` | Données de ces contextes. L'écran de la fiche les assemble au fil des étapes 5, 6 et 8, comme les tableaux de bord (`contextes.md` §3). Les deux colonnes AGI ne sont pas reprises. |
| Export des opérateurs qui achètent ou vendent plus d'1 hl par an (§4.10.7 p.51) | `statistiques` | Le critère est un volume tiré des DSM ou des contrats. |
| Alimentation de l'annuaire public armagnac.fr | aucun | Hors périmètre (§1.4 p.6, §7 p.84, glossaire p.98). Q-OPE-1. |
| Dérogations Armagnac -40 % vol (AGI 2.1.4 p.6, reprise au §7.1.1 p.84) | **en attente** | Absentes du §4.3 et de C.1, usage inconnu : rien n'est construit avant la réponse du BNIA (Q-OPE-2). |
| Fax, imprimé personnel, code d'accès Internet historique (C.1.1.1 p.101), chai agréé, déclaration en mode Excel (AGI 2.1.1 p.5) | aucun | Abandonnés : ni affichés ni repris (décision du 2026-10-06, Q-OPE-3). Le compte portail remplace le code d'accès. |
| Valeurs référencées par la fiche : titres, pays, régions, marques, circuits, types de visite, labels, langues, produits commercialisés, centres d'intérêt, membres BNIA | `referentiels` | Partie publiée (ADR-008, ADR-011). Une marque se crée depuis la fiche (RG-REF-14). |
| Types tiers (C.10.8.2) | `exportation` | Déjà décidé (spec 01). Probablement l'ancien type d'opérateur d'InteGraal : Q-REF-1. |
| Rattachement des flux ODG à une fiche (§6.1.4 p.81) | passerelle ODG (`distillation`) | La passerelle cherche la fiche par l'identifiant tiers ODG, puis par CVI ou SIRET, que `operateurs` publie (RG-OPE-07, §5.2). |

## 3. Modèle

Deux agrégats de même identifiant technique (ADR-011), décidés en A-OPE-3 : **`Operateur`**
(identité, déclaratif, production, diffusion, et ses **contacts**, porteurs de leurs droits au portail)
et **`PresentationOperateur`** (marques, circuits, visites, labels, œnotourisme, médailles). Le journal des
contacts est une table hors agrégat, en ajout seul. `Millesime` et `Appellation` viennent de
`commun.domain` (A-OPE-4).

### 3.1 Opérateur (agrégat)

**Identité et coordonnées** (C.1.1.1 p.100-101)

| Champ | Type | Règle | Source |
|---|---|---|---|
| type | `PRODUCTEUR`, `NEGOCIANT`, `AUTRE` | Obligatoire (RG-OPE-01) | C.1.1.1, §4.3 p.18, décision du 2026-10-06 |
| code | texte | Obligatoire, unique, proposé à la création, modifiable (RG-OPE-03 à 05) | §4.3.5 p.21 |
| groupe | texte | Facultatif, comparé normalisé (RG-OPE-09, H-OPE-1) | C.1.1.1 |
| CVI, SIRET, n° d'accise | texte | Facultatifs, format contrôlé (RG-OPE-06) | C.1.1.1 |
| bureau des douanes | texte | Facultatif, libre | C.1.1.1, décision du 2026-10-06 |
| identifiant tiers ODG | texte | Facultatif, unique (RG-OPE-07) | C.1.1.1, décision du 2026-10-06 |
| titre | référence `Titre` | Facultatif (RG-OPE-21) | C.1.1.1 |
| raison sociale | texte | Obligatoire (RG-OPE-02, 08) | C.1.1.1 |
| nom commercial | texte | Facultatif | C.1.1.1 |
| contact principal | référence `Contact` de l'opérateur | Facultatif (RG-OPE-22) | C.1.1.1, décision du 2026-10-06 |
| adresse lignes 1 à 3, code postal, ville | texte | Facultatifs ; complétude signalée (RG-OPE-02) | C.1.1.1 |
| pays | référence `Pays` | France par défaut (RG-OPE-02) | C.1.1.1 |
| téléphone 1, téléphone 2, portable | texte | Facultatifs, libres (RG-OPE-20) | C.1.1.1 |
| e-mail général | texte | Facultatif, format e-mail (RG-OPE-24) | C.1.1.1 (Mél 1), décision du 2026-10-06 |
| destinataire des factures | contact de l'opérateur **ou** adresse saisie | Facultatif (RG-OPE-23) | C.8.1.1 p.123, décision du 2026-10-06 |
| site web, site commercial | texte | Facultatifs, format URL (RG-OPE-20) | C.1.1.1 |
| coordonnées GPS | latitude, longitude décimales | Facultatives, ensemble (RG-OPE-20) | C.1.1.1 |
| membre BNIA | référence `MembreBnia` | Facultatif (RG-OPE-21, Q-OPE-4) | C.1.1.1, décision du 2026-10-06 |
| accord de diffusion général | booléen | Faux par défaut | C.1.1.1 |
| commentaire interne | texte long | Facultatif, masqué au portail (RG-OPE-36) | C.1.1.1 |
| actif | booléen | Vrai à la création (RG-OPE-25, 26) | décision du 2026-10-06 |

**Déclaratif et production** (C.1.1.1 p.101, C.1.1.2 p.102)

| Champ | Type | Règle | Source |
|---|---|---|---|
| périodicité DSM | `MENSUELLE`, `TRIMESTRIELLE`, `SEMESTRIELLE`, `ANNUELLE` | Valeur courante ; défaut selon le type ; absente pour `AUTRE` (RG-OPE-10) | C.1.1.1, §4.4.10 p.28 |
| statut de déclaration de stock | `OUI`, `NON`, `SOMMEIL`, `CLOTURE` | Défaut selon le type (RG-OPE-11, H-OPE-2) | C.1.1.2 |
| région | référence `Region` | Facultative ; « en région délimitée » lu dans `referentiels` (RG-OPE-21) | C.1.1.2 |
| appellations | ensemble de `ARMAGNAC`, `BAS_ARMAGNAC`, `ARMAGNAC_TENAREZE`, `HAUT_ARMAGNAC`, `BLANCHE_ARMAGNAC` | Choix multiple (RG-OPE-12) | C.1.1.2, AGI 2.1.1 p.5, décision du 2026-10-06 |
| alambics | liste d'`Alambic` | Voir ci-dessous (RG-OPE-13) | C.1.2.1 p.102 |
| détenteur alambic | booléen **calculé** | Vrai si la liste n'est pas vide (RG-OPE-13, H-OPE-3) | C.1.1.2 |

`Alambic` : n° d'agrément (texte facultatif), type (`ARMAGNACAIS`, `DOUBLE_CHAUFFE`, obligatoire),
capacité (décimal strictement positif, facultatif ; unité affichée selon le type, H-OPE-3).

**Diffusion et statistiques** (C.1.3 p.102, C.1.8 p.104)

| Champ | Type | Règle | Source |
|---|---|---|---|
| niveau de diffusion des millésimes | `REFUS`, `PARTIEL`, `TOTAL` | `REFUS` par défaut (RG-OPE-14) | §4.3.2.2 p.19, décision du 2026-10-06 |
| millésimes diffusables | ensemble de `Millesime` | Non vide si `PARTIEL`, vide sinon (RG-OPE-14) | C.1.3.1 |
| accès aux statistiques du portail | booléen | Faux par défaut (RG-OPE-15, H-OPE-4) | §4.3.2.6 p.20 |

La granularité de C.1.8 (trois niveaux, familles, périmètre visible) n'est pas retenue, comme C.1.8 le
permet (« si cette granularité est retenue ») : le portail n'a que deux restitutions (§5.11.2-5.11.3).

**Présentation** (C.1.4 à C.1.7 p.103-104), agrégat `PresentationOperateur`

| Élément | Forme | Règle |
|---|---|---|
| marques | lignes (portée `FRANCE` ou `EXPORT`, référence `Marque`, diffusable web) | RG-OPE-16 |
| circuits de distribution | lignes (portée, référence `CircuitDistribution`) | RG-OPE-16 |
| visites | lignes (référence `TypeVisite`, commentaire) | RG-OPE-17 |
| labels | lignes (référence `Label`, commentaire) | RG-OPE-17 |
| paiement par carte, cartes acceptées (`VISA`, `MASTERCARD`, `AMERICAN_EXPRESS`) | booléen, ensemble | RG-OPE-18 |
| dégustation gratuite, lieu de vente aménagé, accueil de groupes, adhésion œnotourisme BNIA | booléens | Faux par défaut (RG-OPE-18) |
| langues parlées, produits commercialisés | ensembles de `Langue`, de `ProduitCommercialise` | RG-OPE-21 |
| mode de présentation des produits | `ARMAGNAC_EXCLUSIVEMENT`, `ARMAGNAC_ET_AUTRES_PRODUITS` | Facultatif (RG-OPE-18) |
| présentation du domaine, en français et en anglais | textes longs | Facultatifs |
| médailles et concours | lignes `MedailleConcours` (concours, année, distinction `OR`, `ARGENT`, `BRONZE`, `AUTRE`, produit) | RG-OPE-19 |

**Écarts au CDC, décidés le 2026-10-06 :** fax, imprimé personnel, code d'accès Internet, chai agréé,
mode Excel abandonnés ; Mél 2 et 3 supprimés (Mél 1 devient l'e-mail général, la note de C.1 p.100 envoie
les personnes vers les contacts) ; bureau des douanes en texte et non en référentiel ; dernières
déclarations non stockées (§2.2) ; « Blanche Armagnac » ajoutée aux appellations.

### 3.2 Contact (dans l'agrégat `Operateur`)

| Champ | Type | Règle | Source |
|---|---|---|---|
| opérateur | référence `Operateur` | Obligatoire, fixe | C.1.9 |
| titre | référence `Titre` | Facultatif | C.10.8.1 p.129 |
| nom | texte | Obligatoire (RG-OPE-27) | C.1.9.1 |
| prénom, fonction | texte | Facultatifs | C.1.9.1 |
| centres d'intérêt | ensemble de `CentreInteret` | Facultatif (RG-OPE-21) | C.1.9.1, §4.3.4 p.21 |
| téléphone professionnel, téléphone secondaire | texte | Facultatifs | C.1.9.1 |
| e-mail | texte | Format e-mail ; unique dans l'opérateur ; requis pour l'accès ; verrouillé s'il est l'identifiant d'un compte (RG-OPE-28, 29, 32) | C.1.9.1, §5.13.3 p.79 |
| adresse, code postal, ville, pays | texte, référence `Pays` | Facultatifs, si distincte de l'opérateur | C.1.9.1 |
| observation | texte long | Facultatif | C.1.9.1 |
| actif | booléen | Vrai à la création (RG-OPE-33) | §4.3.4 p.20 |
| accès portail | booléen | Faux par défaut (RG-OPE-29) | C.1.9.2 p.105 |
| administrateur | booléen | Faux par défaut ; requiert l'accès (RG-OPE-30) | C.1.9.2 |
| modules | ensemble de `ModulePortail` | Requiert l'accès ; tous si administrateur (RG-OPE-31) | C.1.9.2, §5.13.2 p.78-79 |
| e-mail verrouillé | booléen, alimenté par `acces` | Vrai quand l'e-mail est l'identifiant d'un compte (RG-OPE-32) | décision du 2026-10-06 |
| journal | lignes (date, auteur, action, champs avant / après) | RG-OPE-34 | §5.13.3 p.79 |

`ModulePortail`, liste fermée (§5.13.2, décision du 2026-10-06) : `DSM`, `CONTRATS_ARMAGNAC`,
`PLURIANNUELS_ARMAGNAC`, `PROPOSITIONS_ARMAGNAC`, `CONTRATS_VIN`, `PLURIANNUELS_VIN`,
`PROPOSITIONS_VIN`, `FINS_DE_TRAVAUX`, `BOUTIQUE`, `FACTURES`, `CERTIFICATS_EXPORT` (qui couvre aussi les
ventes export du §5.7), `STATISTIQUES`. Le droit « gestion des contacts » du §5.13.2 n'est pas un
module : c'est le rôle d'administrateur. Les quatre modules en arbitrage n'ouvrent rien tant que leur
contexte n'existe pas.

**Une personne = un e-mail = un compte.** Mme Durand, comptable de deux chais, a un contact dans chacun,
avec le même e-mail ; `acces` lui ouvre les deux opérateurs avec les droits de chaque contact. Le
C.13.2 (rôle et modules sur le rattachement du compte) n'est pas suivi : les droits sont sur le contact.

## 4. Règles de gestion

| Id | Règle | Source |
|---|---|---|
| RG-OPE-01 | Le type d'opérateur est obligatoire : `PRODUCTEUR`, `NEGOCIANT` ou `AUTRE`. Une fiche `AUTRE` ne déclare pas : pas de périodicité, statut de déclaration `NON`. | §4.3 p.18, C.1.1.1 p.100, décision du 2026-10-06 |
| RG-OPE-02 | Type, code et raison sociale sont obligatoires ; le pays vaut France par défaut. Une fiche à laquelle manque l'adresse ligne 1, le code postal, la ville, le pays ou la région est « incomplète » : elle s'enregistre, et l'écran nomme ce qui manque pour facturer ou certifier. | Décision du 2026-10-06 ; C.8.1.1 p.123, §4.8.4 p.42 |
| RG-OPE-03 | À la création, un code est proposé. Producteur et `AUTRE` : le plus grand numéro existant à 6 chiffres, plus un (séquence commune). Négociant : `N`, le département tiré du code postal français (deux premiers chiffres ; `2A` de 20000 à 20199 et `2B` de 20200 à 20699 ; trois chiffres pour 971 à 976), puis le plus grand numéro sur 4 chiffres de ce département, plus un. Sans code postal français, aucun code n'est proposé : l'agent le saisit. | §4.3.5 p.21, décision du 2026-10-06 |
| RG-OPE-04 | Le code est unique, sans distinction de casse ni d'espaces en tête et en fin, à la création comme à la modification. Il peut toujours être saisi ou corrigé à la main. Les autres contextes référencent l'opérateur par son identifiant technique. | §4.3.5 p.21, ADR-011, décision du 2026-10-06 (code non partagé avec l'ODG) |
| RG-OPE-05 | Changer le type ou le code postal ne change pas le code. Si le code n'a plus la forme du type (ou du département pour un négociant), la fiche le signale et propose d'attribuer un nouveau code selon RG-OPE-03. | Décision du 2026-10-06 |
| RG-OPE-06 | CVI, SIRET et n° d'accise sont facultatifs. Renseignés, leur format est contrôlé et bloquant : SIRET sur 14 chiffres avec clé de Luhn, CVI sur 10 caractères alphanumériques, accise `FR` suivi de 11 caractères alphanumériques. Une valeur déjà portée par une autre fiche donne un avertissement non bloquant qui nomme cette fiche. | C.1.1.1 p.101, décision du 2026-10-06 |
| RG-OPE-07 | L'identifiant tiers ODG est facultatif et, s'il est renseigné, unique (bloquant), pour que la passerelle ODG rattache un flux sans ambiguïté. | C.1.1.1 p.101, §6.1.4 p.81, décision du 2026-10-06 |
| RG-OPE-08 | Une raison sociale déjà portée par une autre fiche (à la casse, aux accents et aux espaces près) donne un avertissement non bloquant qui nomme cette fiche. | Décision du 2026-10-06 |
| RG-OPE-09 | Deux fiches sont du même groupe si leurs groupes sont égaux à la casse, aux accents et aux espaces près. À la saisie, les groupes existants sont proposés. La fiche liste les autres fiches de son groupe. | §4.3.2 p.18, §4.3.3 p.20, H-OPE-1 |
| RG-OPE-10 | La périodicité est mensuelle, trimestrielle, semestrielle ou annuelle. À la création, elle vaut annuelle pour un producteur, mensuelle pour un négociant, rien pour `AUTRE` ; elle est obligatoire pour un producteur ou un négociant. Elle se modifie à tout moment ; changer le type ne la modifie pas, l'écran suggère la valeur par défaut du nouveau type. Seule exception, ce que RG-OPE-01 et 11 imposent : passer en `AUTRE` retire la périodicité et change `OUI` en `NON` ; quitter `AUTRE` donne la périodicité et le statut par défaut du nouveau type. Seule la valeur courante est conservée. | C.1.1.1 p.101, §4.4.10 p.28, décision du 2026-10-06 (exception : implémentation, lot 3) |
| RG-OPE-11 | Le statut de déclaration de stock vaut `OUI`, `NON`, `SOMMEIL` ou `CLOTURE` ; `OUI` par défaut pour un producteur ou un négociant, `NON` pour `AUTRE`, qui ne peut pas être à `OUI`. Seul `OUI` rend une DSM attendue. Toutes les transitions sont permises. | C.1.1.2 p.102, §4.4.8 p.27, H-OPE-2 |
| RG-OPE-12 | Les appellations sont un choix multiple parmi cinq valeurs, dont Blanche Armagnac. | C.1.1.2 p.102, AGI 2.1.1 p.5, décision du 2026-10-06 |
| RG-OPE-13 | Un alambic a un type (`ARMAGNACAIS` ou `DOUBLE_CHAUFFE`) et, si elle est renseignée, une capacité strictement positive. L'opérateur est « détenteur d'alambic » si et seulement s'il a au moins un alambic. | C.1.1.2, C.1.2.1 p.102, H-OPE-3 |
| RG-OPE-14 | La diffusion des millésimes vaut `REFUS` à la création. En `PARTIEL`, au moins un millésime diffusable, sans doublon, chacun un `Millesime` valide ; en `REFUS` ou `TOTAL`, aucun. Passer de `PARTIEL` à un autre niveau vide la liste, après avertissement. | §4.3.2.2 p.19, C.1.3.1 p.102, décision du 2026-10-06 |
| RG-OPE-15 | L'accès aux statistiques du portail est un booléen, faux à la création, fixé par le BNIA. Il s'ajoute au module `STATISTIQUES` du contact (§5.11.1). Les données d'un opérateur n'entrent dans les statistiques diffusées que selon sa diffusion des millésimes. | §4.3.2.6 p.20, §5.11 p.75, H-OPE-4 |
| RG-OPE-16 | Une marque figure au plus une fois par portée (France, Export) ; la même marque peut figurer dans les deux, et sur plusieurs fiches. « Diffusable web » est faux par défaut. Un circuit figure au plus une fois par portée. | §4.3.2.3 p.19, C.1.4 p.103, décision du 2026-10-06 |
| RG-OPE-17 | Un type de visite, ou un label, figure au plus une fois sur une fiche ; le commentaire est facultatif. | C.1.5-1.6 p.103, décision du 2026-10-06 |
| RG-OPE-18 | Les cartes acceptées ne se saisissent que si le paiement par carte est accepté ; le décocher les vide. Dégustation, lieu de vente, accueil de groupes et adhésion sont faux par défaut. Le mode de présentation est facultatif. | §4.3.2.5 p.20, C.1.7 p.104, décision du 2026-10-06 |
| RG-OPE-19 | Une médaille porte un concours (obligatoire), et facultativement une année (un `Millesime` valide), une distinction (`OR`, `ARGENT`, `BRONZE`, `AUTRE`) et un produit. | §4.3.2.5 p.20, C.1.7.1 p.104, décision du 2026-10-06 |
| RG-OPE-20 | Latitude et longitude se renseignent ensemble, dans les bornes −90 à 90 et −180 à 180. Les sites sont des URL `http` ou `https`. Les téléphones sont libres (numéros étrangers). | C.1.1.1 p.101, décision du 2026-10-06 |
| RG-OPE-21 | Une valeur de référentiel (titre, pays, région, membre BNIA, marque, circuit, type de visite, label, langue, produit commercialisé, centre d'intérêt) doit être active quand on la choisit. Devenue inactive, elle reste affichée là où elle est déjà utilisée. | RG-REF-04, ADR-011 |
| RG-OPE-22 | Le contact principal est un contact actif de l'opérateur. Il cesse de l'être si ce contact est désactivé ou supprimé, après un avertissement qui le nomme. | C.1.1.1 p.101, décision du 2026-10-06 |
| RG-OPE-23 | Le destinataire des factures est soit un contact actif de l'opérateur qui a un e-mail, soit une adresse e-mail saisie. `facturation` lit l'adresse qui en résulte et la fige sur la facture. Un contact destinataire désactivé ou supprimé cesse de l'être, après avertissement. | C.8.1.1 p.123, décision du 2026-10-06 |
| RG-OPE-24 | L'e-mail général et l'adresse saisie du destinataire des factures respectent le format d'une adresse e-mail. | Décision du 2026-10-06 |
| RG-OPE-25 | Un opérateur que rien ne référence se supprime, avec ses contacts, ses sous-ensembles et sa présentation. Sinon la suppression est refusée, et on le désactive ; il se réactive. | Décision du 2026-10-06, ADR-011 |
| RG-OPE-26 | Un opérateur inactif reste consultable avec son historique, mais n'est plus proposé dans les recherches ni les listes de choix, et ses contacts perdent l'accès au portail pour cet opérateur (événement « accès portail retiré »). Le réactiver ne rend pas l'accès d'office : il se réaccorde contact par contact. | Décision du 2026-10-06 |
| RG-OPE-27 | Le nom du contact est obligatoire ; les autres champs sont facultatifs. | C.1.9.1 p.104, décision du 2026-10-06 |
| RG-OPE-28 | L'e-mail d'un contact respecte le format d'une adresse e-mail et est unique parmi les contacts de son opérateur, à la casse près. Deux contacts d'opérateurs différents peuvent avoir le même e-mail : c'est la même personne. | §5.13.3 p.79, §5.1.4 p.60, décision du 2026-10-06 |
| RG-OPE-29 | L'accès portail exige un e-mail. Accorder l'accès publie l'événement « accès portail accordé » (e-mail, opérateur, contact) ; le retirer, désactiver ou supprimer le contact publie « accès portail retiré ». `acces` crée le compte s'il n'existe pas et envoie l'e-mail d'activation. | §5.13.3 p.79, §5.1.2 p.59, décision du 2026-10-06 |
| RG-OPE-30 | Un administrateur a l'accès portail et tous les modules. Un opérateur peut avoir plusieurs administrateurs ; le premier est désigné par le BNIA. Au portail, aucune action ne peut laisser un opérateur sans administrateur actif (retrait du rôle, de l'accès, désactivation, suppression) : le message renvoie vers le BNIA, qui le peut au back-office. | §5.13 p.78-79, C.1.9.2 p.105, décision du 2026-10-06 |
| RG-OPE-31 | Les modules d'un contact sont pris dans la liste fermée `ModulePortail` et n'existent que si l'accès est accordé ; retirer l'accès les efface. | §5.13.2 p.78-79, décision du 2026-10-06 |
| RG-OPE-32 | Tant que l'e-mail d'un contact n'est l'identifiant d'aucun compte, le BNIA et l'administrateur le corrigent librement. Dès qu'il l'est (événement de `acces`), il n'est plus modifiable, ni au back-office ni au portail : seule la personne le change, dans « Mon compte ». `acces` publie alors le changement, et `operateurs` reporte la nouvelle adresse sur tous les contacts qui portaient l'ancienne. | §5.1.5 p.60, décision du 2026-10-06 |
| RG-OPE-33 | Un contact se désactive, se réactive ou se supprime. La désactivation et la suppression retirent son accès (RG-OPE-29) et font tomber ses désignations (RG-OPE-22, 23) après avertissement. Un contact réactivé n'a pas d'accès : il se réaccorde. | §4.3.4 p.20, §5.13.2 p.78, décision du 2026-10-06 |
| RG-OPE-34 | Chaque création, modification, désactivation, réactivation, suppression, accord ou retrait d'accès et changement de droits d'un contact est journalisé : date, auteur, action, et pour une modification chaque champ changé avec ses valeurs avant et après. Le journal survit à la suppression du contact et garde son nom. | §5.13.3 p.79, décision du 2026-10-06 |
| RG-OPE-35 | Au portail, seul un administrateur de l'opérateur actif modifie son profil, crée, modifie, désactive ou supprime ses contacts, et accorde ou retire l'accès, les modules et le rôle d'administrateur. Les autres contacts consultent le profil. | §5.13 p.78-79, décision du 2026-10-06 |
| RG-OPE-36 | Au portail, sont modifiables : titre, nom commercial, adresse et pays, téléphones, e-mail général, sites, GPS, contact principal, destinataire des factures, marques et circuits, visites et labels, œnotourisme, médailles, diffusion des millésimes, accord de diffusion général. Sont en lecture : type, code, raison sociale, groupe, CVI, SIRET, accise, bureau des douanes, identifiant ODG, membre BNIA, périodicité, statut de déclaration, région, appellations, alambics, accès aux statistiques. Le commentaire interne est masqué. | §5.13.1 p.78, C.1.1.1 p.101, §5.11.1 p.75, décision du 2026-10-06 |
| RG-OPE-37 | Au portail, la recherche d'un autre opérateur ne porte que sur les opérateurs actifs, exige au moins trois caractères (code, raison sociale ou nom commercial), renvoie au plus vingt résultats, et n'affiche que le code, la raison sociale, le nom commercial et la ville. | §5.5.2 p.67, décision du 2026-10-06 |
| RG-OPE-38 | Au back-office, le listing filtré s'exporte au format Excel, avec ses colonnes affichées. | §4.2.6 p.17, décision du 2026-10-06 |

## 5. Cas d'usage

### 5.1 Écriture

| Cas d'usage | Objets | RG |
|---|---|---|
| Lister et rechercher les opérateurs : filtres type, code, raison sociale, nom commercial, code postal, ville (§4.3.1 p.18), actif, incomplet ; export Excel (back-office) | `Operateur` | 02, 38 |
| Proposer un code pour un type et un code postal (back-office) | `Operateur` | 03 |
| Créer un opérateur (back-office) | `Operateur` | 01 à 12, 14, 15, 20, 21, 24 |
| Modifier l'identité, le déclaratif, la production (back-office) | `Operateur` | 01 à 13, 20, 21, 24 |
| Attribuer un nouveau code à un opérateur (back-office) | `Operateur` | 03, 04, 05 |
| Modifier la diffusion des millésimes et l'accès aux statistiques (back-office ; diffusion seule au portail) | `Operateur` | 14, 15, 35, 36 |
| Modifier la présentation : marques, circuits, visites, labels, œnotourisme, médailles (back-office et portail) | `PresentationOperateur` | 16 à 19, 21, 35, 36 ; RG-REF-14 pour créer une marque |
| Désigner le contact principal et le destinataire des factures (back-office et portail) | `Operateur` | 22, 23, 24, 35, 36 |
| Modifier le profil au portail | `Operateur` | 35, 36 |
| Désactiver, réactiver, supprimer un opérateur (back-office) | `Operateur` | 25, 26 |
| Créer, modifier un contact (back-office ; portail pour un administrateur) | `Contact` | 21, 27, 28, 32, 34, 35 |
| Accorder ou retirer l'accès, fixer les modules, nommer ou retirer un administrateur (back-office ; portail pour un administrateur) | `Contact` | 29, 30, 31, 34, 35 |
| Désactiver, réactiver, supprimer un contact (back-office ; portail pour un administrateur) | `Contact` | 22, 23, 29, 30, 33, 34, 35 |
| Consulter le journal d'un contact (back-office ; portail pour un administrateur) | `Contact` | 34 |
| Verrouiller l'e-mail des contacts d'un compte (à réception de l'événement « compte créé » de `acces`) | `Contact` | 32 |
| Reporter un changement d'identifiant de compte (à réception de l'événement de `acces`) | `Contact` | 28, 32, 34 |
| Rechercher un autre opérateur (portail) | `Operateur` | 37 |

### 5.2 Lecture

Presque tous les contextes lisent un opérateur. Ils le font par une **partie publiée en lecture seule**
(A-OPE-1), sur le modèle de `referentiels` (ADR-008) : `fr.bnia.operateurs.domain.partage` (vues
immuables `OperateurVue`, `ContactVue`, `ResultatRecherche` ; listes fermées `TypeOperateur`,
`PeriodiciteDsm`, `StatutDeclarationStock`, `ModulePortail` ; types d'événements) et
`fr.bnia.operateurs.application.partage.OperateursLecture`. Le reste du contexte leur est interdit
(ArchUnit). Un consommateur stocke l'identifiant de l'opérateur avec une clé étrangère (ADR-011).
Besoins relevés :

| Besoin | Consommateur probable |
|---|---|
| Un opérateur par identifiant : code, raison sociale, nom commercial, type, actif | tous |
| Un opérateur par code (imports Excel, reprise) | `declarations`, `exportation`, reprise |
| Un opérateur par identifiant ODG, puis par CVI ou SIRET | `distillation`, `contratsvin` (passerelle ODG) |
| Type, périodicité, statut de déclaration ; opérateurs à `OUI` | `declarations` (génération, relances, colonnes grisées) |
| Type | `cotisation` (assiette), `statistiques` |
| Adresse, pays, e-mail résolu du destinataire des factures | `facturation` (figés sur la facture), `boutique` |
| Raison sociale et adresse de l'expéditeur | `exportation` (certificat, figées) |
| Région (et « en région délimitée » via `referentiels`), appellations, diffusion des millésimes, accès aux statistiques | `statistiques` |
| Contacts actifs, avec accès, d'un e-mail ; leurs opérateurs actifs, administrateur et modules | `acces` (connexion, droits du portail) |
| Contacts d'un opérateur par centre d'intérêt, et contacts avec accès | `notifications` (C.14 p.136) |
| Recherche restreinte (RG-OPE-37) | `contratsarmagnac` (écran du portail) |

Événements (A-OPE-2), tous définis dans `operateurs.domain.partage` : publiés, `AccesPortailAccorde` et
`AccesPortailRetire` (RG-OPE-29) ; écoutés, venus de `acces`, `CompteCree` et
`IdentifiantCompteModifie` (RG-OPE-32). Tant que `acces` n'existe pas, rien ne les écoute ni ne les
émet : aucun e-mail n'est verrouillé.

## 6. Écrans

### 6.1 Back-office

- **Menu « Opérateurs »**, droit « Gestion des opérateurs : Lecture, Écriture » (§4.2.5.1 p.15).
- **Listing** : filtres du §4.3.1 (type, code, raison sociale, nom commercial, code postal, ville), plus
  actif / inactif et « fiche incomplète » ; colonnes type, code, raison sociale, nom commercial, code
  postal, ville, téléphone 1, e-mail général ; ouverture directe de la fiche ; bouton de création ;
  export Excel (RG-OPE-38).
- **Création** : type d'abord, puis code postal ; le code proposé se remplit (RG-OPE-03) et reste
  modifiable ; avertissements de doublon (raison sociale, CVI, SIRET, accise) affichés sans bloquer.
- **Fiche opérateur**, consultation et édition par onglets :
  - *Général* : identité (dont titre), coordonnées, e-mail général, destinataire des factures (bascule
    « un contact » / « une adresse »), contact principal, membre BNIA, accord de diffusion général,
    commentaire interne ; indicateur d'incomplétude ;
    alerte de code non conforme avec « attribuer un nouveau code » (RG-OPE-05) ; autres fiches du
    groupe (RG-OPE-09) ;
  - *Production* : périodicité, statut de déclaration, région, appellations, alambics (détenteur
    affiché, calculé) ;
  - *Diffusion* : niveau, millésimes diffusables en accord partiel ;
  - *Marques* : sections France et Export ; marques avec « diffusable web », circuits ; création d'une
    marque avec le composant de `referentiels` (RG-REF-14, RG-REF-15) ;
  - *Visites et labels* ; *Œnotourisme* (dont médailles en grille) ; *Statistiques* (accès au portail) ;
  - *Contacts* : liste (nom, fonction, e-mail, téléphone, accès, administrateur, actif), création,
    ouverture ;
  - blocs ajoutés par les étapes suivantes : dernière DSM et son état, historique des DSM, factures,
    contrats (§4.3.3, §2.2).
- **Actions** : désactiver, réactiver, supprimer (refusée si l'opérateur est utilisé : le message
  propose de désactiver).
- **Fiche contact** : identité, coordonnées, centres d'intérêt, observation ; bloc « Portail » : accès,
  administrateur, modules (cases, toutes cochées et grisées pour un administrateur) ; e-mail grisé
  quand il est l'identifiant d'un compte, avec l'explication (RG-OPE-32) ; journal ; actions
  désactiver, réactiver, supprimer, avec la liste des effets (désignations, accès) avant confirmation.

### 6.2 Portail

- **« Mon entreprise »** (profil de l'opérateur actif) : lecture pour tout contact ayant accès ;
  modification par un administrateur, des seuls champs de RG-OPE-36, dans les mêmes onglets que le
  back-office, sans le commentaire interne.
- **« Contacts »** (administrateur seulement, RG-OPE-35) : liste, création, modification, accès,
  modules, administration, désactivation, suppression, journal. Les actions qui laisseraient
  l'opérateur sans administrateur sont refusées avec un renvoi vers le BNIA (RG-OPE-30).
- **Composant de recherche d'opérateur** (RG-OPE-37), utilisé par les écrans de contrats du portail.

Droits : au back-office, « Gestion des opérateurs » (§4.2.5.1) ; au portail, le rôle d'administrateur.
Ils sont appliqués quand `acces` existera (étape 3). D'ici là, toute personne connectée au back-office
administre, et l'utilisateur fictif du portail est administrateur de l'opérateur choisi.

## 7. Données

### 7.1 Démonstration (profil `demo`)

Des données plausibles, présentées comme telles, sur les référentiels de démonstration de la spec 01 :
- une quinzaine d'opérateurs du Gers, des Landes et du Lot-et-Garonne : producteurs (`000001`…),
  négociants (`N320001`…, `N400001`), deux `AUTRE` (un distillateur, un courtier) ;
- un groupe « Maison Durand » de deux chais (Condom et Eauze) ;
- Mme Durand, administratrice à Condom et contact DSM et factures à Eauze, avec le même e-mail ;
  M. Petit, maître de chai sans accès ;
- des alambics des deux types, une diffusion partielle (quelques millésimes), des marques France et
  Export, des visites, labels et médailles ;
- une fiche incomplète (sans adresse) et une fiche inactive.

### 7.2 Reprise AGI

| Objet | Vue AGI | Correspondance |
|---|---|---|
| Opérateur | 2.1.1 (p.4-5) | Code professionnel → code ; type de tiers → type, d'après les valeurs réelles (Q-REF-1) ; groupe, CVI, SIRET, accise, bureau des douanes, raison sociale, raison commerciale, adresse, téléphones, sites, latitude et longitude, commentaire : directs (CVI, SIRET, accise : format contrôlé, anomalies listées) ; titre, pays, région → identifiants par lecture par code (ADR-011) ; membre BNIA → `MembreBnia` (Q-OPE-4) ; Mél 1 → e-mail général, Mél 2 et 3 listés pour le BNIA (destinataire des factures ou contact) ; contact principal (texte) → rapproché d'un contact au nom identique, sinon contact créé avec ce seul nom ; accord de diffusion : direct ; déclaration stock → statut ; appellations : les cinq booléens ; statistiques : `refus` → faux, `total` → vrai, `partiel` → vrai et listé pour le BNIA (H-OPE-4) ; diffusion millésimes → niveau. **Non repris** : dernières déclarations, fax, imprimé personnel, code d'accès Internet, chai agréé, déclaration mode Excel ; « détenteur alambic » recalculé, incohérences listées. |
| Alambics | 2.1.2 (p.5) | Agrément, capacité directs ; type texte rapproché des deux valeurs, sinon listé (H-OPE-3) |
| Millésimes diffusables | 2.1.3 (p.5) | Lignes « Diffusion = vrai » seulement |
| Dérogations -40 % vol | 2.1.4 (p.6) | **En attente** (Q-OPE-2) |
| Marques, circuits | 2.1.5, 2.1.6 (p.6) | Portée directe ; marque et circuit : identifiant InteGraal à traduire, vues de paramétrage demandées (Q-REF-6) |
| Visites, labels, langues | 2.1.7 à 2.1.9 (p.6-7) | Directs, par lecture par code |
| Médailles et concours | 2.1.10 (p.7) | Libellé de la table InteGraal (vue demandée, Q-REF-6) → concours ; année, distinction, produit vides |
| Produits commercialisés | 2.1.11 (p.7) | Type → `ProduitCommercialise` ; libellés libres non repris (Q-REF-9) |
| Contacts | 2.1.12 (p.7-8) | « Nom contact » → nom, prénom vide (les noms à séparer sont listés) ; téléphones, e-mail, adresse, pays, observation directs ; code contact : clé de reprise seulement. Deux contacts du même opérateur au même e-mail : listés (RG-OPE-28) |
| Utilisateurs extranet | 3.8.1, 3.8.2 (p.22) | Fusionnés par e-mail avec les contacts du même opérateur (prénom et nom complétés), sinon nouveau contact ; actif → accès accordé ; profils → droits, lecture large (H-OPE-5) |

## 8. Décisions d'architecture

Décidées le 2026-10-06, actées par ADR-012, ADR-013 et l'amendement d'ADR-009.

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-OPE-1 | `operateurs` publie une **partie en lecture seule**, comme `referentiels` : `fr.bnia.operateurs.domain.partage` (vues immuables, listes fermées, types d'événements) et `fr.bnia.operateurs.application.partage.OperateursLecture`. L'exception de l'ADR-008 est étendue, nommément, à `operateurs`, lu par presque tous les contextes. Les consommateurs déclarent la dépendance Maven et une clé étrangère vers `operateurs.operateur`. | Un port par consommateur : une dizaine de ports et d'adapters de traduction, qui exigeraient de toute façon une API publiée (ou un SQL sur le schéma d'un autre contexte, écarté par l'ADR-008). Copie locale par événements : synchronisation et reprise disproportionnées pour un monolithe prototype, et plus de clé étrangère. | 012 (étend 008) |
| A-OPE-2 | Événements entre contextes : **Spring `ApplicationEvent`, synchrones**, l'écouteur appelant son cas d'usage dans la transaction de l'émetteur (tout ou rien). L'application publie par un port `PublicationEvenements` ; l'adapter Spring et les écouteurs (`@EventListener`) vivent dans un nouveau package d'adapter `adapter.evenement`. Les **types d'événements** échangés avec `acces` sont des records de `operateurs.domain.partage`, ceux qu'il émet comme ceux qu'il accepte : `acces` dépend de `operateurs`, jamais l'inverse. Un e-mail ne part qu'après validation de la transaction. | Écoute après validation (`AFTER_COMMIT`) : un échec de `acces` laisserait un accès sans compte, sans reprise. Types dans `commun` : le noyau commun se chargerait du vocabulaire de deux contextes (ADR-009). | 013 |
| A-OPE-3 | Deux agrégats : **`Operateur`**, qui contient ses **contacts** (les règles RG-OPE-22, 23, 28 et 30 deviennent locales), et **`PresentationOperateur`**, 1 pour 1 de même identifiant, modifié sans réécrire la fiche. Le journal des contacts est une table hors agrégat, en ajout seul. | Contact en agrégat séparé : invariants répartis dans les cas d'usage. Un seul agrégat : une quinzaine de tables réécrites à chaque case cochée (Spring Data JDBC), conflits entre le BNIA et le portail. | 012 |
| A-OPE-4 | **`Millesime`** (année valide) et **`Appellation`** (cinq AOC) entrent dans **`commun.domain`**, à côté de `CompteDAge` : concepts métier stables, d'usage certain dans plusieurs contextes (DSM, export, contrats, statistiques). | `Appellation` dans la partie publiée de `operateurs` : les contrats en dépendraient pour typer leur propre appellation. Les garder dans `operateurs` jusqu'au deuxième usage écrit : déplacement de code certain à l'étape 5 ou 6. | 009 (amendé) |
| A-OPE-5 | Le numéro suivant d'un code se calcule par **le plus grand numéro existant, plus un**, lu par un port de lecture (`JdbcClient`) pour chaque série (6 chiffres ; `N` + département). L'index unique sur le code tranche une création simultanée : 409, et l'écran repropose un code. | Table de compteurs ou séquences PostgreSQL : sûres en concurrence, mais en conflit avec les codes saisis à la main, et des séquences par département seraient lourdes dans la migration unique (ADR-005). | aucun (choix local) |

Conséquences : CLAUDE.md mis à jour (exception `operateurs.*.partage`, package `adapter.evenement`,
règle des événements) ; règle ArchUnit à étendre aux packages `partage` de `operateurs` au premier lot
de l'implémentation.

## 9. Hypothèses

Revue du 2026-10-06 : toutes retenues, provisoirement pour H-OPE-1 à 4, dont le sens est demandé au BNIA
(Q-OPE-6 à 9) ; H-OPE-5 retenue, à confirmer (Q-OPE-14). Elles restent révisables.

| Id | Hypothèse | Pourquoi |
|---|---|---|
| H-OPE-1 | Le groupe est un texte, comparé à la casse, aux accents et aux espaces près, avec suggestion des groupes existants. Il est distinct de la « structure juridique » des statistiques du portail, qui ne portent que sur la fiche active. | C.1.1.1 le type « texte court ». Le sens du groupe et de la « structure juridique » est demandé au BNIA (Q-OPE-6). |
| H-OPE-2 | Statut de déclaration : `OUI` déclare ; `NON` n'est pas concerné ; `SOMMEIL` suspend, reprise possible ; `CLOTURE` cesse. Seul `OUI` rend une DSM attendue. | Le CDC ne définit aucune valeur (Q-OPE-7). |
| H-OPE-3 | Les alambics sont de deux types, `ARMAGNACAIS` et `DOUBLE_CHAUFFE`. La capacité a une unité propre au type, affichée à l'écran. Le détenteur se calcule. | C.1.2.1 cite ces deux types « par exemple » ; unités non données (Q-OPE-8). |
| H-OPE-4 | L'accès aux statistiques du portail est un booléen. L'utilisation des données d'un opérateur suit sa diffusion des millésimes. | §4.3.2.6 dit « activer ou refuser » ; le « partiel » d'InteGraal reste inexpliqué (Q-OPE-9). |
| H-OPE-5 | Profils de l'extranet → droits : « Admin Professionnel » → administrateur ; « Statistiques » → `STATISTIQUES` ; « Contrats » → tout ce que l'extranet couvre aujourd'hui : `CONTRATS_ARMAGNAC`, `PLURIANNUELS_ARMAGNAC`, `PROPOSITIONS_ARMAGNAC`, `CONTRATS_VIN`, `PLURIANNUELS_VIN`, `PROPOSITIONS_VIN`, `FINS_DE_TRAVAUX` (décision du 2026-10-06). Les autres modules ne sont donnés qu'aux administrateurs, qui les délèguent. | AGI 3.8.2 p.22 ne donne que ces trois libellés ; l'extranet couvre contrats, pluri-annuels, propositions et fins de travaux (§2.1 p.7, AGI 3.1-3.7). Personne ne perd ce qu'il a (Q-OPE-14). |

## 10. Questions ouvertes

Revue du 2026-10-06 : aucune question ne reste à trancher par nous. Le prototype se construit sur les
décisions et hypothèses ; seules la reprise et `declarations` (Q-OPE-7) dépendent des réponses.

**À envoyer au BNIA** : Q-OPE-1, 2, 4, 6, 7, 8, 9 (questions) ; Q-OPE-3, 5, 10, 11, 12, 13, 14, 15
(décisions à confirmer).
**À envoyer à AGI** : Q-OPE-4 (vue « membres BNIA »), à joindre à Q-REF-6.

| Id | Question | Bloque |
|---|---|---|
| Q-OPE-1 | **Au BNIA** : le site armagnac.fr doit-il lire les données de présentation saisies dans la plateforme (marques diffusables, visites, labels, œnotourisme, GPS, présentations) ? Quelle base fait foi pour ces données publiques, la plateforme ou le site ? | Tout flux vers l'annuaire |
| Q-OPE-2 | **Au BNIA** : à quoi servent les dérogations Armagnac -40 % vol (AGI 2.1.4) ? Qui les accorde, faut-il en saisir de nouvelles, un contexte doit-il les vérifier (DSM, vente export, certificat) ? | La reprise de la vue AGI 2.1.4 et la place des dérogations dans la cible |
| ~~Q-OPE-3~~ | Les champs fax, imprimé personnel, code d'accès Internet historique, chai agréé et déclaration en mode Excel sont-ils encore utiles ? **Tranché le 2026-10-06 : abandonnés.** À confirmer par le BNIA. | |
| Q-OPE-4 | **Au BNIA** : que contient le champ « membre BNIA » (adhésion, collège, rôle dans les instances) ? **Demande à AGI** : une vue de la table « paramétrage - membres BNIA » (identifiant, code, libellé), à joindre à la demande Q-REF-6. | Les valeurs initiales de `MembreBnia` et la reprise du champ |
| ~~Q-OPE-5~~ | Que voit un opérateur des autres opérateurs au portail, pour désigner l'autre partie d'un contrat ? **Tranché le 2026-10-06 : recherche restreinte** (RG-OPE-37). À confirmer par le BNIA, avec la façon de faire actuelle sur pro.armagnac.fr. | |
| Q-OPE-6 | **Au BNIA** : à quoi sert le groupe opérateur aujourd'hui ? La « propre structure juridique » des statistiques du portail (§4.10 p.47, §5.11 p.75) est-elle le groupe, le SIREN, ou la fiche ? | H-OPE-1 ; le périmètre des statistiques du portail |
| Q-OPE-7 | **Au BNIA** : que signifient « oui », « non », « sommeil » et « clôturé » du statut de déclaration de stock ? Lesquels rendent une DSM attendue ? | H-OPE-2 ; la génération et les relances de `declarations` |
| Q-OPE-8 | **Au BNIA** : quels types d'alambic et quelles unités de capacité sont en usage ? | H-OPE-3 ; la reprise des alambics |
| Q-OPE-9 | **Au BNIA** : que signifie l'accord statistique « partiel » d'InteGraal ? Une autorisation d'utilisation des données, distincte de la diffusion des millésimes, est-elle nécessaire (§5.11 p.75) ? | H-OPE-4 ; la reprise du champ Statistiques |
| ~~Q-OPE-10~~ | Les droits portail sont-ils portés par le contact (C.1.9.2) ou par le rattachement du compte (C.13.2) ? **Tranché le 2026-10-06 : par le contact** ; le droit « gestion des contacts » devient le rôle d'administrateur. À confirmer par le BNIA. | |
| ~~Q-OPE-11~~ | La Blanche Armagnac fait-elle partie des appellations de la fiche (absente de C.1.1.2, présente dans AGI 2.1.1) ? **Tranché le 2026-10-06 : oui.** À confirmer par le BNIA. | |
| ~~Q-OPE-12~~ | Quelle diffusion des millésimes par défaut pour une nouvelle fiche ? **Tranché le 2026-10-06 : refus.** À confirmer par le BNIA. | |
| ~~Q-OPE-13~~ | Le code négociant prend-il le département du code postal, avec un incrément par département ? **Tranché le 2026-10-06 : oui** (RG-OPE-03). À confirmer par le BNIA, avec le cas des négociants étrangers. | |
| ~~Q-OPE-14~~ | Quels droits donnent les profils de l'extranet (« Admin Professionnel », « Statistiques », « Contrats ») ? **Tranché le 2026-10-06 : lecture large** (H-OPE-5). À confirmer par le BNIA. | |
| ~~Q-OPE-15~~ | Le bureau des douanes, typé « Référentiel » en C.1.1.1, peut-il rester un texte libre ? **Tranché le 2026-10-06 : texte libre** (aucune liste au paramétrage, texte dans InteGraal). À confirmer par le BNIA. | |

## 11. Tests attendus

Chaque RG-OPE-nn a au moins un test qui porte son identifiant (CLAUDE.md).
- **Tests de domaine**, sans base : RG-OPE-01, 02, 03 (calcul du code à partir d'un numéro courant et
  d'un code postal), 05, 06 (formats), 09 (normalisation), 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
  22, 23, 24, 27, 28 (format), 30 (dernier administrateur), 31, 32 (verrou), 33 (effets), 34, 36.
- **Tests de cas d'usage**, avec repositories simulés, puis IT : RG-OPE-03 (numéro suivant), 04, 06 et
  08 (doublons), 07, 21, 25 (suppression refusée par clé étrangère), 26, 28 (unicité dans l'opérateur),
  29 et 32 (événements), 35 (autorisation au portail), 37, 38.

## Journal

| Date | Changement |
|---|---|
| 2026-10-06 | Phases A et B : cadrage, puis périmètre. Décisions : contact par opérateur, droits portail sur le contact, compte unique par e-mail dans `acces` ; données de présentation saisies sans flux ; bureau des douanes en texte ; membre BNIA en liste de `referentiels` ; médailles semi-structurées ; journal des contacts ; événement d'accès vers `acces` ; recherche restreinte au portail ; historique de la fiche assemblé par l'écran ; export « plus d'1 hl » dans `statistiques` ; champs hérités abandonnés ; dérogations en attente du BNIA ; code BNIA non partagé avec l'ODG. |
| 2026-10-06 | Phase C : modèle, RG-OPE-01 à 38, cas d'usage, écrans, données. Décisions : type `AUTRE` ; code négociant par département du code postal, recodage proposé et non imposé ; CVI, SIRET, accise au format contrôlé, doublons signalés ; identifiant ODG unique ; contact principal désigné ; e-mail général et destinataire des factures (contact ou adresse) ; e-mail d'un compte modifiable dans « Mon compte » seulement ; administrateurs désignés par le BNIA, plusieurs possibles ; cinq appellations ; opérateur et contact supprimables s'ils sont inutilisés, désactivables sinon ; périodicité courante ; répartition des champs au portail ; administrateur seul au portail ; douze modules ; diffusion des millésimes à refus par défaut ; champs obligatoires minimaux. Hypothèses H-OPE-1 à 5 (groupe, statut déclaratif, alambics, statistiques, profils extranet). |
| 2026-10-06 | Phase D : `contextes.md` mis à jour (fiches `operateurs`, `acces` et sept autres, bordures, context map, interrogations, journal) ; spec 01 rouverte pour `MembreBnia`, Q-REF-6 étendue, Q-REF-10 corrigée. |
| 2026-10-06 | Phase E : décisions A-OPE-1 à 5 (§8) : partie publiée en lecture seule ; événements Spring synchrones, types dans `operateurs.domain.partage` ; agrégats `Operateur` (avec contacts) et `PresentationOperateur` ; `Millesime` et `Appellation` dans `commun.domain` ; numéro suivant par maximum + 1. §3 et §5.2 mis à jour. |
| 2026-10-06 | Phase F : revue des hypothèses et questions. H-OPE-5 élargie (« Contrats » → sept modules). Q-OPE-14 et 15 ajoutées (à confirmer). Liste d'envoi au BNIA et à AGI en tête du §10. |
| 2026-10-06 | Phase G, relecture : RG-OPE-36 complétée (titre, pays et accord de diffusion général modifiables au portail, auparavant absents des deux listes) ; présentation rattachée à `PresentationOperateur` (§2.1, cas d'usage, RG-OPE-25) ; onglet Général complété. Numéros de page vérifiés. |
| 2026-10-06 | Phase G, glossaire : type d'opérateur, code, groupe, contact, compte portail, millésime et introduction des référentiels corrigés ; quinze termes ajoutés (administrateur, module portail, périodicité DSM, statut de déclaration de stock, appellation, alambic, diffusion des millésimes, identifiant tiers ODG, contact principal, destinataire des factures, fiche incomplète, présentation de l'opérateur, médaille et concours, membre BNIA, fiche annuaire opérateur). |
| 2026-10-06 | Spec validée, statut « terminée ». |
| 2026-10-06 | ADR-012 (partie publiée et agrégats) et ADR-013 (événements entre contextes) rédigés ; ADR-008 et ADR-009 amendés ; CLAUDE.md mis à jour. |
| 2026-10-06 | Implémentation, lot 3 : RG-OPE-10 précisée. Changer le type ne touche au déclaratif que si RG-OPE-01 et 11 l'imposent (vers `AUTRE` : plus de périodicité, `OUI` devient `NON` ; depuis `AUTRE` : valeurs par défaut du nouveau type). Contradiction relevée à l'implémentation, tranchée par l'utilisateur (« ajustement minimal »). |
| 2026-10-06 | Implémentation (lots 1 à 9). Choix faits pendant l'implémentation, soumis à l'utilisateur quand ils touchaient la spec : **portail** sans `acces` : opérateur fixe de démonstration (`bnia.portail.operateur-demo`, profil `demo`), API du portail désignant l'opérateur dans le chemin ; RG-OPE-35 réduite pour l'instant à « opérateur actif » (le rôle d'administrateur sera contrôlé avec `acces`) ; **export Excel** côté serveur (fastexcel) ; **identifiant du contact** tiré de la séquence avant l'enregistrement et conservé quand l'agrégat est réécrit, clés étrangères du contact principal et du destinataire des factures vérifiées en fin de transaction ; **doublons** signalés pendant la saisie (endpoint dédié) ; **changement de type** : ajustement minimal du déclaratif (RG-OPE-10 précisée, voir plus haut). Sans question, faute de règle : un e-mail changé sur un contact qui a l'accès publie « accès retiré » (ancienne adresse) puis « accès accordé » (nouvelle) ; effacer l'e-mail du destinataire des factures est refusé (RG-OPE-23) ; un contact inactif ne reçoit pas l'accès ; le verrouillage d'un e-mail n'est pas journalisé, le report d'un identifiant l'est (auteur « Mon compte ») ; auteur du journal « BNIA », « Portail » ou « Mon compte » ; unité de capacité des alambics non affichée (Q-OPE-8) ; doublons de marques signalés par portée sans nommer la marque ; présentation enregistrée d'un bloc. Partie publiée : vues `OperateurVue` (avec `AdresseVue`), `ContactVue`, `ResultatRecherche`, listes `TypeOperateur`, `PeriodiciteDsm`, `StatutDeclarationStock`, `NiveauDiffusion`, `ModulePortail`, quatre événements (ADR-012 complété). Démonstration : quinze fiches (§7.1), la France ajoutée aux pays de démonstration de la spec 01. Chaque RG-OPE-01 à 38 a au moins un test qui porte son identifiant. |
