# 02 : opérateurs

> Contexte `operateurs` (étape 2 de `docs/contextes.md`). Statut : terminée (validée le 2026-10-06), implémentée (lots 1 à 9) ; **révisée et validée le 2026-10-08, réimplémentée le 2026-10-08** pour personnes et accès (utilisateurs, rattachements, rôles, invitations, fiches professionnelles), avec la spec 03 (`authentification`), d'après la vision `Spécification fonctionnelle gestion des opérateurs, contacts et accès.md` (même dossier, citée « vision »). Les sections révisées sont écrites dans leur état cible.
> Références : `p.N` = page du CDC v1.1, `AGI p.N` = page de la spec de reprise AGI.
> `H-OPE-n` : hypothèse retenue, révisable. `Q-OPE-n` : question ouverte, suivie dans cette spec (§10).

## Sommaire

- [1. Objet](#1-objet)
- [2. Périmètre](#2-périmètre)
  - [2.1 Dans le contexte](#21-dans-le-contexte)
  - [2.2 Hors du contexte](#22-hors-du-contexte)
- [3. Modèle](#3-modèle)
  - [3.1 Opérateur (agrégat)](#31-opérateur-agrégat)
  - [3.2 Personnes et accès (révision du 2026-10-08)](#32-personnes-et-accès-révision-du-2026-10-08)
- [4. Règles de gestion](#4-règles-de-gestion)
- [5. Cas d'usage](#5-cas-dusage)
  - [5.1 Écriture](#51-écriture)
  - [5.2 Lecture](#52-lecture)
- [6. Écrans](#6-écrans)
  - [6.1 Back-office (opérateur actif : BNIA)](#61-back-office-opérateur-actif--bnia)
  - [6.2 Portail (opérateur actif : autre que BNIA)](#62-portail-opérateur-actif--autre-que-bnia)
  - [6.3 Communs aux deux espaces](#63-communs-aux-deux-espaces)
- [7. Données](#7-données)
  - [7.1 Démonstration (profil `demo`)](#71-démonstration-profil-demo)
  - [7.2 Reprise AGI](#72-reprise-agi)
- [8. Décisions d'architecture](#8-décisions-darchitecture)
- [9. Hypothèses](#9-hypothèses)
- [10. Questions ouvertes](#10-questions-ouvertes)
- [11. Tests attendus](#11-tests-attendus)
- [Journal](#journal)

## 1. Objet

Référentiel des structures suivies par le BNIA (producteurs, négociants, autres structures, et le BNIA
lui-même) et des personnes de chacune : identité, codification, identifiants administratifs,
paramètres déclaratifs, production, diffusion des millésimes, présentation commerciale et
œnotouristique, autorisation statistique ; contacts, utilisateurs, rattachements et leurs rôles,
invitations, fiches professionnelles ; droits, évalués à chaque requête.

Le contexte ne déclare rien, ne calcule aucune cotisation, et ne gère ni mot de passe ni connexion
(`authentification`, spec 03). Sa valeur tient dans quatre choses :
- une **fiche fiable**, que tous les autres contextes référencent : code attribué automatiquement et
  unique, type d'opérateur qui pilote les DSM et la cotisation, périodicité et statut déclaratif ;
- des **personnes sans doublon** : chaque information a un seul propriétaire et n'est stockée qu'à un
  endroit (vision §2) ; une personne est, dans un opérateur, soit un contact (sans compte), soit un
  membre (utilisateur rattaché, avec un rôle), jamais les deux ;
- des **droits simples** : trois rôles fixes, des modules, le BNIA opérateur comme les autres dont les
  rôles valent pour tous les opérateurs ;
- une **lecture** pour les autres contextes et une **recherche restreinte** pour le portail.

Sources : §4.3 (p.18-21) · §5.13 (p.78-79) · C.1 (p.100-105) · AGI 2.1 (p.4-8) · §4.2 (p.14-17) · §5.1
(p.59-60) · C.2 (p.105-107) · C.13 (p.135) · vision (2026-10-08).
Usages relevés ailleurs : §4.4.2, 4.4.3, 4.4.6, 4.4.8 à 4.4.10 (DSM, p.22-28) · §4.5 et §5.5.2 (contrats,
p.30-34 et 67) · §4.7 (vendeur export, p.38-40) · §4.8.4 (expéditeur du certificat, p.42) · C.8.1.1 (tiers
facturé, p.123) · §4.10, §5.11 (statistiques, p.47-51 et 75) · §5.1 et C.13 (compte utilisateur, p.59-60 et
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
| Personnes | Contact : personne sans compte, connue d'un opérateur (titre, nom, prénom, e-mail) | `Contact` | §4.3.4 p.20-21, C.1.9.1 p.104-105, vision §2.2 |
| | Utilisateur : personne avec un compte (titre, nom, prénom, e-mail de connexion, statut, opérateur actif) | `Utilisateur` | §4.2.2 p.14, §5.1 p.59-60, C.2.1.1 p.105, C.13.1 p.135, vision §2.2 |
| | Fiche professionnelle : informations de la personne dans un opérateur (fonction, téléphones, adresse, centres d'intérêt, observation) | `FicheProfessionnelle` | C.1.9.1 p.104-105, vision §2.2, décision du 2026-10-08 |
| Accès | Rattachement d'un utilisateur à un opérateur, avec un rôle et un statut | `Rattachement` | §5.1.4 p.60, C.13.2 p.135, vision §2.2 |
| | Rôles fixes et modules : `OBSERVATEUR`, `AGENT`, `ADMIN` ; Contacts, Accès, Paramétrage | `Role`, `Module` | §4.2.5 p.15-17, §5.13.2 p.78, vision §2.5, décision du 2026-10-08 |
| | Invitation : lien d'activation qui prouve l'e-mail et crée le rattachement | `Invitation` | §5.1.2 p.59, §5.13.3 p.79, vision UC-12, 13, 20 |
| | Changement d'e-mail d'un utilisateur, confirmé par lien | `ChangementEmail` | §5.1.5 p.60, vision UC-24 |
| | Blocage d'un utilisateur par le BNIA | | §4.2.6 p.17, vision UC-05 |
| | Opérateur actif d'un utilisateur, choix et bascule | | §5.1.4 p.60, vision UC-21, 22 |
| | Droits d'un utilisateur sur un opérateur, évalués à chaque requête | | §4.2.6 p.17, §5.1.4 p.60, vision principe 7 |
| | Journal des actions sur l'opérateur, ses personnes et ses accès | | §4.2.4 p.15, §5.13.3 p.79, C.2.3 p.107, décision du 2026-10-08 |
| Échanges | Annuaire d'un opérateur : contacts, membres et invitations en une liste | | §5.13.2 p.78, vision UC-03, 17 |
| | Recherche restreinte d'opérateurs au portail (autre partie d'un contrat) | | §5.5.2 p.67, décision du 2026-10-06 |
| | « L'utilisateur peut-il se connecter ? », pour `authentification` | | vision UC-21, spec 03 RG-AUT-04 |
| | Lecture d'un opérateur par les autres contextes (forme : phase E) | | C.3 à C.8, C.13 |
| | Export Excel du listing filtré | | §4.2.6 p.17, décision du 2026-10-06 |

**Décisions de périmètre du 2026-10-08** (révision, vision et spec 03 §2) ; elles remplacent les
décisions « contact et compte portail », « activation » et « historique » du 2026-10-06.
- **Personnes et accès dans `operateurs`.** Utilisateurs, rattachements, rôles, invitations, fiches
  professionnelles et droits sont ici, pour que les transitions contact ↔ membre se fassent dans une
  seule transaction ; `authentification` ne garde que la preuve d'identité (spec 03). Une personne = un
  e-mail = un utilisateur ; un utilisateur a un rattachement par opérateur, avec un rôle (C.13.2 suivi,
  à rebours de Q-OPE-10). Un contact n'a pas de compte.
- **Le BNIA est un opérateur**, de type `BNIA`. Son personnel est rattaché à l'opérateur BNIA ; un rôle
  qui y est tenu vaut pour tous les opérateurs. Pas de population « utilisateurs BNIA » distincte (écart à
  §4.2.2 et C.2.1).
- **Rôles fixes** `OBSERVATEUR`, `AGENT`, `ADMIN`, un par rattachement, et trois modules : Contacts (avec
  la fiche de l'opérateur), Accès, Paramétrage (BNIA seulement). Écarts assumés : pas de rôles composés
  par le BNIA (§4.2.1, §4.2.6), pas de droits délégués par module (§5.13.2, C.1.9.2) ; tout membre actif
  lit tout son opérateur.
- **Historique** : un journal de toutes les actions du contexte sur les personnes et les accès (un journal
  par contexte ; les connexions sont au journal de `authentification`). La fiche opérateur n'est toujours
  pas historisée.
- **Espaces** : l'opérateur actif décide de l'espace (BNIA : back-office ; autre : portail).

**Décisions de périmètre du 2026-10-06** (toujours valables).
- **Données de présentation publique** (onglets Marques, Visites et labels, Œnotourisme ; GPS, sites,
  accord de diffusion) : saisies et consultables, **sans flux** vers armagnac.fr, dont la fiche annuaire
  est hors périmètre (§1.4 p.6). Voir Q-OPE-1.
- **Bureau des douanes** : texte libre, contrairement au typage « Référentiel » de C.1.1.1. Aucune liste
  n'existe au paramétrage (§4.11, C.10) et InteGraal le stocke sans liaison (AGI 2.1.1 p.5).
- **Membre BNIA** : référence vers une nouvelle liste simple `MembreBnia` de `referentiels` (spec 01
  rouverte, phase D). Sens à préciser : Q-OPE-4.
- **Médailles et concours** : lignes semi-structurées dans `operateurs`, pas de référentiel.
- **Recherche au portail** : opérateurs actifs seulement, trois caractères au moins, vingt résultats au
  plus ; code, raison sociale, nom commercial et ville, rien d'autre. Voir Q-OPE-5.
- **ODG** : le code opérateur BNIA n'est pas une clé partagée avec l'ODG. Il reste modifiable, comme
  les codes des référentiels (ADR-011).

### 2.2 Hors du contexte

| Élément décrit dans le CDC | Contexte retenu | Motif |
|---|---|---|
| Compte de connexion : mot de passe, double authentification, blocage temporaire, mot de passe oublié, session, journal des connexions (§4.2.6-4.2.7 p.17, §5.1 p.59-60, C.13.1 p.135) | `authentification` | Infrastructure remplaçable, sans métier (spec 03, décision du 2026-10-08). Créé, renommé et supprimé avec l'utilisateur. |
| Rôles composables, catalogue de droits module × action, administrateur global (§4.2.1 p.14, §4.2.5 p.15-17, C.2.2 p.106) ; droits délégables par module au portail (§5.13.2 p.78-79, C.1.9.2 p.105) | aucun | Écart assumé : rôles fixes et modules (RG-OPE-31), décision du 2026-10-08. |
| Reprise des utilisateurs de l'extranet (AGI 3.8 p.22) | aucun | Reprise des données hors périmètre du prototype (décision du 2026-10-08). |
| Dernière déclaration créée et saisie (C.1.1.1 p.102), état de la dernière déclaration et historique des DSM, factures, contrats (§4.3.3 p.20) | `declarations`, `facturation`, `contratsarmagnac` | Données de ces contextes. L'écran de la fiche les assemble au fil des étapes 5, 6 et 8, comme les tableaux de bord (`contextes.md` §3). Les deux colonnes AGI ne sont pas reprises. |
| Export des opérateurs qui achètent ou vendent plus d'1 hl par an (§4.10.7 p.51) | `statistiques` | Le critère est un volume tiré des DSM ou des contrats. |
| Alimentation de l'annuaire public armagnac.fr | aucun | Hors périmètre (§1.4 p.6, §7 p.84, glossaire p.98). Q-OPE-1. |
| Dérogations Armagnac -40 % vol (AGI 2.1.4 p.6, reprise au §7.1.1 p.84) | **en attente** | Absentes du §4.3 et de C.1, usage inconnu : rien n'est construit avant la réponse du BNIA (Q-OPE-2). |
| Fax, imprimé personnel, code d'accès Internet historique (C.1.1.1 p.101), chai agréé, déclaration en mode Excel (AGI 2.1.1 p.5) | aucun | Abandonnés : ni affichés ni repris (décision du 2026-10-06, Q-OPE-3). Le compte de connexion (spec 03) remplace le code d'accès. |
| Valeurs référencées par la fiche : titres, pays, régions, marques, circuits, types de visite, labels, langues, produits commercialisés, centres d'intérêt, membres BNIA | `referentiels` | Partie publiée (ADR-008, ADR-011). Une marque se crée depuis la fiche (RG-REF-14). |
| Types tiers (C.10.8.2) | `exportation` | Déjà décidé (spec 01). Probablement l'ancien type d'opérateur d'InteGraal : Q-REF-1. |
| Rattachement des flux ODG à une fiche (§6.1.4 p.81) | passerelle ODG (`distillation`) | La passerelle cherche la fiche par l'identifiant tiers ODG, puis par CVI ou SIRET, que `operateurs` publie (RG-OPE-07, §5.2). |

## 3. Modèle

L'opérateur (§3.1) et sa présentation sont inchangés, sauf le type `BNIA` et les désignations, qui
visent désormais une fiche professionnelle. Les personnes et les accès (§3.2) remplacent l'ancien contact
porteur de droits. Chaque objet de §3.2 est son propre agrégat (A-OPE-6) : `Operateur` ne contient plus de contacts.
Identifiants `bigint` (A-OPE-9, ADR-011 maintenu). `Millesime` et `Appellation` viennent
de `commun.domain` (A-OPE-4).

### 3.1 Opérateur (agrégat)

**Identité et coordonnées** (C.1.1.1 p.100-101)

| Champ | Type | Règle | Source |
|---|---|---|---|
| type | `BNIA`, `PRODUCTEUR`, `NEGOCIANT`, `AUTRE` | Obligatoire (RG-OPE-01, 39) | C.1.1.1, §4.3 p.18, décisions du 2026-10-06 et du 2026-10-08 |
| code | texte | Obligatoire, unique, proposé à la création, modifiable (RG-OPE-03 à 05) | §4.3.5 p.21 |
| groupe | texte | Facultatif, comparé normalisé (RG-OPE-09, H-OPE-1) | C.1.1.1 |
| CVI, SIRET, n° d'accise | texte | Facultatifs, format contrôlé (RG-OPE-06) | C.1.1.1 |
| bureau des douanes | texte | Facultatif, libre | C.1.1.1, décision du 2026-10-06 |
| identifiant tiers ODG | texte | Facultatif, unique (RG-OPE-07) | C.1.1.1, décision du 2026-10-06 |
| titre | référence `Titre` | Facultatif (RG-OPE-21) | C.1.1.1 |
| raison sociale | texte | Obligatoire (RG-OPE-02, 08) | C.1.1.1 |
| nom commercial | texte | Facultatif | C.1.1.1 |
| contact principal | référence `FicheProfessionnelle` de l'opérateur (contact ou membre) | Facultatif (RG-OPE-22) | C.1.1.1, décisions du 2026-10-06 et du 2026-10-08 |
| adresse lignes 1 à 3, code postal, ville | texte | Facultatifs ; complétude signalée (RG-OPE-02) | C.1.1.1 |
| pays | référence `Pays` | France par défaut (RG-OPE-02) | C.1.1.1 |
| téléphone 1, téléphone 2, portable | texte | Facultatifs, libres (RG-OPE-20) | C.1.1.1 |
| e-mail général | texte | Facultatif, format e-mail (RG-OPE-24) | C.1.1.1 (Mél 1), décision du 2026-10-06 |
| destinataire des factures | fiche professionnelle de l'opérateur **ou** adresse saisie | Facultatif (RG-OPE-23) | C.8.1.1 p.123, décisions du 2026-10-06 et du 2026-10-08 |
| site web, site commercial | texte | Facultatifs, format URL (RG-OPE-20) | C.1.1.1 |
| coordonnées GPS | latitude, longitude décimales | Facultatives, ensemble (RG-OPE-20) | C.1.1.1 |
| membre BNIA | référence `MembreBnia` | Facultatif (RG-OPE-21, Q-OPE-4) | C.1.1.1, décision du 2026-10-06 |
| accord de diffusion général | booléen | Faux par défaut | C.1.1.1 |
| commentaire interne | texte long | Facultatif, visible de l'opérateur BNIA seulement (RG-OPE-36) | C.1.1.1 |
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

### 3.2 Personnes et accès (révision du 2026-10-08)

**Qui possède quoi** (vision §2.1) : l'identité d'un utilisateur appartient à la personne, qui seule la
modifie ; l'identité d'un contact et toutes les fiches professionnelles appartiennent à l'opérateur ;
rôle et statut d'un rattachement appartiennent aux ADMIN de l'opérateur ; mot de passe et sessions sont à
`authentification`. Une personne est portée, dans un opérateur, par un contact **ou** par un
rattachement (RG-OPE-29) ; ses informations professionnelles, par une fiche dans les deux cas.

```
           contact (sans compte)          utilisateur (avec compte)
             titre, nom, prénom,            titre, nom, prénom, e-mail,
             e-mail (facultatif)            statut, opérateur actif
                    │ 1                               │ 1
                    │                         rattachement (1 par opérateur)
                    │                           rôle, statut
                    │ 1                               │ 1
                    └──────── fiche_professionnelle ──┘
                                (à l'opérateur ; même ligne quand
                                 la personne change d'état)
```

**3.2.1 Contact** (personne sans compte, connue d'un opérateur)

| Champ | Type | Règle | Source |
|---|---|---|---|
| opérateur | référence `Operateur` | Obligatoire, fixe | C.1.9 p.104 |
| titre | référence `Titre` | Facultatif (RG-OPE-21) | C.1.9.1, C.10.8.1 p.129 |
| nom | texte | Obligatoire (RG-OPE-27) | C.1.9.1 |
| prénom | texte | Facultatif | C.1.9.1 |
| e-mail | texte | Facultatif ; format, unique parmi les contacts de l'opérateur (RG-OPE-28) | C.1.9.1, vision UC-10 |
| fiche | référence `FicheProfessionnelle` de type `CONTACT` | Exactement une (RG-OPE-30) | vision §2.2 |
| version | entier | Contrôle de concurrence (RG-OPE-51) | vision §2.2 règle 5 |

**3.2.2 Utilisateur** (personne avec un compte ; n'existe qu'une fois son e-mail prouvé)

| Champ | Type | Règle | Source |
|---|---|---|---|
| titre | référence `Titre` | Facultatif (RG-OPE-27) | décision du 2026-10-08 |
| nom, prénom | texte | Obligatoires (RG-OPE-27) ; modifiés par la personne seule (RG-OPE-47) | C.2.1.1 p.105, vision UC-23 |
| e-mail | texte | Obligatoire, unique sans distinction de casse (RG-OPE-29) ; identifiant de connexion (spec 03) ; changé par confirmation (RG-OPE-48) | §5.1.6 p.60, vision UC-24 |
| statut | `ACTIF`, `BLOQUE` | `ACTIF` à la création ; bloqué par un ADMIN BNIA (RG-OPE-50) | C.2.1.1, vision UC-05 |
| opérateur actif | référence `Operateur` | Facultatif (RG-OPE-49) | §5.1.4 p.60, C.13.2.1 p.135, vision UC-21 |
| version | entier | RG-OPE-51 | vision §2.2 règle 5 |

**3.2.3 Fiche professionnelle** (informations de la personne dans un opérateur, propriété de l'opérateur)

| Champ | Type | Règle | Source |
|---|---|---|---|
| opérateur | référence `Operateur` | Obligatoire, fixe | vision §2.2 |
| type | `CONTACT`, `RATTACHEMENT` | Selon son porteur (RG-OPE-30) | vision §2.2 |
| fonction | texte | Facultatif | C.1.9.1 p.105 |
| téléphone professionnel, téléphone secondaire | texte | Facultatifs, libres | C.1.9.1 |
| adresse, code postal, ville, pays | texte, référence `Pays` | Facultatifs, si distincte de l'opérateur | C.1.9.1 |
| centres d'intérêt | ensemble de `CentreInteret` | Facultatif (RG-OPE-21) ; ciblage des notifications (C.14) | C.1.9.1, §4.3.4 p.21 |
| observation | texte long | Facultatif | C.1.9.1 |
| modifiée par, modifiée le | copie du nom de l'auteur, date-heure | RG-OPE-46 | vision UC-18 |
| version | entier | RG-OPE-51 | vision §2.2 règle 5 |

**3.2.4 Rattachement** (accès d'un utilisateur à un opérateur)

| Champ | Type | Règle | Source |
|---|---|---|---|
| opérateur, utilisateur | références | Obligatoires, fixes ; au plus un par couple (RG-OPE-29) | C.13.2.1 p.135, vision §2.2 |
| rôle | `OBSERVATEUR`, `AGENT`, `ADMIN` | Obligatoire (RG-OPE-31, 43) | C.13.2.1, vision §2.5 |
| statut | `ACTIF`, `SUSPENDU` | `ACTIF` à la création (RG-OPE-43) | vision UC-15 |
| fiche | référence `FicheProfessionnelle` de type `RATTACHEMENT` | Exactement une (RG-OPE-30) | vision §2.2 |
| créé le | date-heure | | |

**3.2.5 Invitation**

| Champ | Type | Règle | Source |
|---|---|---|---|
| opérateur | référence `Operateur` | Obligatoire | vision §2.2 |
| e-mail | texte | Obligatoire, format | §5.13.3 p.79 |
| rôle | `OBSERVATEUR`, `AGENT`, `ADMIN` | Obligatoire | vision UC-12 |
| statut | `EN_ATTENTE`, `ACCEPTEE`, `ANNULEE`, `EXPIREE` | RG-OPE-40 à 42 | vision §2.2 |
| expire le | date-heure | Envoi + 7 jours (RG-OPE-40) | décision du 2026-10-08 |
| jeton | empreinte d'un jeton aléatoire | Usage unique ; jamais conservé en clair (RG-OPE-40) | vision §2.4 règle 2 |
| fiche d'un contact | référence `FicheProfessionnelle` | Si l'invitation part d'un contact ; sinon les champs suivants | vision UC-12 |
| titre, nom, prénom, fonction, téléphone professionnel | textes | Personne nouvelle : nom et prénom obligatoires | vision UC-12 |
| envoyée par, envoyée le | copie du nom de l'auteur, date-heure | | |

**3.2.6 Changement d'e-mail**

| Champ | Type | Règle | Source |
|---|---|---|---|
| utilisateur | référence `Utilisateur` | Une seule demande en cours (RG-OPE-48) | vision §2.2 |
| nouvel e-mail | texte | Format ; libre au moment de la demande et de la confirmation | vision UC-24 |
| jeton, expire le | empreinte, date-heure | Demande + 24 heures | décision du 2026-10-08 |

**3.2.7 Rôles et modules** (liste fermée, définie par l'application, vision §2.5)

| Rôle | Contacts (contacts, fiches professionnelles, fiche et présentation de l'opérateur) | Accès (invitations, rôles, suspensions, retraits) | Paramétrage (référentiels ; opérateur BNIA seulement) |
|---|---|---|---|
| `OBSERVATEUR` | lecture | lecture | lecture |
| `AGENT` | gestion | lecture | gestion |
| `ADMIN` | gestion | gestion | gestion |

Gérer un module permet d'y tout faire (créer, modifier, supprimer). Un rôle tenu dans l'opérateur BNIA
vaut pour tous les opérateurs (RG-OPE-32). Un nouveau module (DSM, contrats…) ajoute une colonne.

**3.2.8 Journal** (hors agrégat, en ajout seul ; remplace le journal des contacts)

| Champ | Type | Règle |
|---|---|---|
| date | date-heure | |
| acteur | identifiant de l'utilisateur, sans clé étrangère, et copie de son nom | RG-OPE-34 |
| opérateur | référence `Operateur` | Vide pour une action sur un utilisateur (profil, e-mail, blocage) |
| action | liste fermée (voir RG-OPE-34) | |
| cible | type (contact, membre, invitation, utilisateur), identifiant, copie du nom | Survit à la suppression de la cible |
| changements | lignes champ, avant, après | Pour une modification |

**Écarts au CDC** (décidés le 2026-10-08) : plus de booléens « accès portail » et « administrateur » ni de
« droits par module » sur le contact (C.1.9.2) ; plus d'état actif du contact (§4.3.4 : on le supprime) ;
rôles fixes au lieu de rôles composés (§4.2.1, C.2.2) ; le contact principal et le destinataire des
factures désignent une fiche professionnelle.

## 4. Règles de gestion

| Id | Règle | Source |
|---|---|---|
| RG-OPE-01 | Le type d'opérateur est obligatoire : `BNIA`, `PRODUCTEUR`, `NEGOCIANT` ou `AUTRE`. Une fiche `AUTRE` ou `BNIA` ne déclare pas : pas de périodicité, statut de déclaration `NON`. Le type `BNIA` ne se choisit pas (RG-OPE-39). | §4.3 p.18, C.1.1.1 p.100, décisions du 2026-10-06 et du 2026-10-08 |
| RG-OPE-02 | Type, code et raison sociale sont obligatoires ; le pays vaut France par défaut. Une fiche à laquelle manque l'adresse ligne 1, le code postal, la ville, le pays ou la région est « incomplète » : elle s'enregistre, et l'écran nomme ce qui manque pour facturer ou certifier. | Décision du 2026-10-06 ; C.8.1.1 p.123, §4.8.4 p.42 |
| RG-OPE-03 | À la création, un code est proposé. Producteur et `AUTRE` : le plus grand numéro existant à 6 chiffres, plus un (séquence commune). Négociant : `N`, le département tiré du code postal français (deux premiers chiffres ; `2A` de 20000 à 20199 et `2B` de 20200 à 20699 ; trois chiffres pour 971 à 976), puis le plus grand numéro sur 4 chiffres de ce département, plus un. Sans code postal français, aucun code n'est proposé : l'agent le saisit. Aucun code n'est proposé pour l'opérateur BNIA (RG-OPE-39). | §4.3.5 p.21, décision du 2026-10-06 |
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
| RG-OPE-15 | L'accès aux statistiques du portail est un booléen, faux à la création, fixé par le BNIA. Vrai, il ouvre les statistiques du portail à tous les membres de l'opérateur (§5.11.1 ; plus de module par contact depuis le 2026-10-08). Les données d'un opérateur n'entrent dans les statistiques diffusées que selon sa diffusion des millésimes. | §4.3.2.6 p.20, §5.11 p.75, H-OPE-4 |
| RG-OPE-16 | Une marque figure au plus une fois par portée (France, Export) ; la même marque peut figurer dans les deux, et sur plusieurs fiches. « Diffusable web » est faux par défaut. Un circuit figure au plus une fois par portée. | §4.3.2.3 p.19, C.1.4 p.103, décision du 2026-10-06 |
| RG-OPE-17 | Un type de visite, ou un label, figure au plus une fois sur une fiche ; le commentaire est facultatif. | C.1.5-1.6 p.103, décision du 2026-10-06 |
| RG-OPE-18 | Les cartes acceptées ne se saisissent que si le paiement par carte est accepté ; le décocher les vide. Dégustation, lieu de vente, accueil de groupes et adhésion sont faux par défaut. Le mode de présentation est facultatif. | §4.3.2.5 p.20, C.1.7 p.104, décision du 2026-10-06 |
| RG-OPE-19 | Une médaille porte un concours (obligatoire), et facultativement une année (un `Millesime` valide), une distinction (`OR`, `ARGENT`, `BRONZE`, `AUTRE`) et un produit. | §4.3.2.5 p.20, C.1.7.1 p.104, décision du 2026-10-06 |
| RG-OPE-20 | Latitude et longitude se renseignent ensemble, dans les bornes −90 à 90 et −180 à 180. Les sites sont des URL `http` ou `https`. Les téléphones sont libres (numéros étrangers). | C.1.1.1 p.101, décision du 2026-10-06 |
| RG-OPE-21 | Une valeur de référentiel (titre, pays, région, membre BNIA, marque, circuit, type de visite, label, langue, produit commercialisé, centre d'intérêt) doit être active quand on la choisit. Devenue inactive, elle reste affichée là où elle est déjà utilisée. | RG-REF-04, ADR-011 |
| RG-OPE-22 | Le contact principal est une fiche professionnelle de l'opérateur, d'un contact ou d'un membre. La désignation survit aux passages de contact à membre et inverse (même fiche) ; elle tombe quand la fiche est supprimée, après un avertissement qui nomme la personne. | C.1.1.1 p.101, décisions du 2026-10-06 et du 2026-10-08 |
| RG-OPE-23 | Le destinataire des factures est soit une fiche professionnelle de l'opérateur dont la personne a un e-mail (celui du contact, ou celui de l'utilisateur pour un membre, qui suit donc ses changements), soit une adresse e-mail saisie. `facturation` lit l'adresse qui en résulte et la fige sur la facture. La désignation tombe quand la fiche est supprimée, après avertissement. | C.8.1.1 p.123, décisions du 2026-10-06 et du 2026-10-08 |
| RG-OPE-24 | L'e-mail général et l'adresse saisie du destinataire des factures respectent le format d'une adresse e-mail. | Décision du 2026-10-06 |
| RG-OPE-25 | Un opérateur que rien ne référence se supprime, avec sa présentation, ses contacts, ses fiches professionnelles, ses rattachements et ses invitations ; un utilisateur qui y perd son dernier rattachement est supprimé avec son compte de connexion (RG-OPE-44). Sinon la suppression est refusée, et on le désactive. Réservé à un ADMIN de l'opérateur BNIA ; l'opérateur BNIA ne se supprime pas. | Décisions du 2026-10-06 et du 2026-10-08, ADR-011 |
| RG-OPE-26 | Un opérateur inactif reste consultable, mais n'est plus proposé dans les recherches ni les listes de choix. Ses rattachements sont gelés : ils restent, mais ne donnent plus accès (un utilisateur qui n'a plus d'autre rattachement utilisable ne peut plus se connecter) ; ses invitations en attente sont annulées. Le réactiver rend les rattachements tels quels. Réservé à un ADMIN de l'opérateur BNIA ; l'opérateur BNIA ne se désactive pas. | Décisions du 2026-10-06 et du 2026-10-08 |
| RG-OPE-27 | Le nom d'un contact est obligatoire ; titre, prénom et e-mail sont facultatifs. Le nom et le prénom d'un utilisateur sont obligatoires, son titre facultatif. Tous les champs d'une fiche professionnelle sont facultatifs. | C.1.9.1 p.104, vision UC-20, décisions du 2026-10-06 et du 2026-10-08 |
| RG-OPE-28 | L'e-mail d'un contact respecte le format d'une adresse e-mail et est unique parmi les contacts de son opérateur, à la casse près. S'il est celui d'un membre de l'opérateur ou d'une invitation en attente de l'opérateur, l'écran avertit sans bloquer. Aucune recherche n'est faite dans les autres opérateurs : un même e-mail déclaré comme contact dans deux opérateurs donne deux contacts indépendants. | §5.13.3 p.79, vision UC-10 et §2.4, décision du 2026-10-08 |
| RG-OPE-29 | Un e-mail correspond à au plus un utilisateur, à la casse près. Dans un opérateur, une personne est un contact ou un membre (rattachement), jamais les deux ; un utilisateur a au plus un rattachement par opérateur. | §5.1.4 et 5.1.6 p.60, §5.13.3 p.79, vision §2.1 principe 1 et §2.2 |
| RG-OPE-30 | Chaque contact et chaque rattachement a exactement une fiche professionnelle, du même opérateur, de type `CONTACT` ou `RATTACHEMENT` selon son porteur. Quand la personne passe de contact à membre (RG-OPE-42) ou inverse (RG-OPE-44), la fiche reste la même ligne : seul son type change ; elle n'est jamais recopiée. | Vision §2.1 principe 5, §2.2 |
| RG-OPE-31 | Les droits sont ceux du rôle d'un rattachement `ACTIF` dans un opérateur actif (tableau §3.2.7), évalués à chaque requête, jamais portés par la session. Tout rattachement `ACTIF` lit tout ce qui concerne son opérateur, et seulement lui. Gérer un module permet d'y créer, modifier et supprimer. Le module Contacts couvre les contacts, les fiches professionnelles et la fiche et la présentation de l'opérateur (dans la limite de RG-OPE-36) ; Accès, les invitations, les rôles, les suspensions et les retraits ; Paramétrage, les référentiels (spec 01), dans l'opérateur BNIA seulement. | §4.2.6 p.17, §5.1.4 p.60, vision §2.5, décision du 2026-10-08 |
| RG-OPE-32 | Un rôle tenu dans l'opérateur BNIA vaut aussi pour tous les autres opérateurs. Sont de plus réservés à l'opérateur BNIA : lister et exporter tous les opérateurs, modifier les champs réservés de RG-OPE-36 et voir le commentaire interne (Contacts, lecture ou gestion selon le cas), créer un opérateur (Contacts, gestion), inviter un premier ADMIN à la création, désactiver, réactiver ou supprimer un opérateur, bloquer ou débloquer un utilisateur (ADMIN), consulter les utilisateurs et le journal des connexions (Accès, lecture). | Vision §2.5 point 7, UC-01 à 05, décision du 2026-10-08 |
| RG-OPE-33 | Sans rôle particulier, tout utilisateur qui a un rattachement `ACTIF` gère son profil (RG-OPE-47), son e-mail (RG-OPE-48), son mot de passe (spec 03) et son opérateur actif (RG-OPE-49), et modifie sa propre fiche professionnelle dans un opérateur où son rattachement est `ACTIF` (RG-OPE-46). Il ne peut pas quitter un opérateur de lui-même (RG-OPE-44). | Vision §2.4 règles 1 et 7, §2.5 point 6 |
| RG-OPE-34 | Chaque action du contexte sur les personnes et les accès est journalisée : création, modification et suppression d'un contact ou d'une fiche ; création, relance, annulation, expiration et acceptation d'une invitation ; changement de rôle, suspension, réactivation et retrait d'un rattachement ; changement de profil et d'e-mail, blocage, déblocage et suppression d'un utilisateur. Chaque ligne porte la date, l'acteur (identifiant et copie de son nom), l'opérateur, l'action, la cible (copie de son nom) et, pour une modification, chaque champ changé avec ses valeurs avant et après. Le journal survit à la suppression de l'acteur et de la cible. | §4.2.4 p.15, §4.2.6 p.17, §5.13.3 p.79, C.2.3 p.107, vision §2.4 règle 5, décision du 2026-10-08 |
| RG-OPE-35 | Un opérateur peut n'avoir aucun ADMIN (créé sans invitation, ou invitation non acceptée). Dès qu'il a un rattachement `ACTIF` de rôle `ADMIN`, aucun de ses membres ne peut retirer, suspendre ou rétrograder le dernier ; un ADMIN de l'opérateur BNIA le peut, après un avertissement « l'opérateur n'aura plus d'ADMIN », puis invite un remplaçant. Pour l'opérateur BNIA, la règle est absolue. | Vision §2.2 règle 4, §2.5 point 5, décision du 2026-10-08 |
| RG-OPE-36 | Par le module Contacts, un rôle d'un opérateur autre que BNIA modifie dans la fiche : titre, nom commercial, adresse et pays, téléphones, e-mail général, sites, GPS, contact principal, destinataire des factures, marques et circuits, visites et labels, œnotourisme, médailles, diffusion des millésimes, accord de diffusion général. Les autres champs (type, code, raison sociale, groupe, CVI, SIRET, accise, bureau des douanes, identifiant ODG, membre BNIA, périodicité, statut de déclaration, région, appellations, alambics, accès aux statistiques) ne se modifient que par un rôle de l'opérateur BNIA. Le commentaire interne n'est visible que de l'opérateur BNIA. | §5.13.1 p.78, C.1.1.1 p.101, §5.11.1 p.75, décisions du 2026-10-06 et du 2026-10-08 |
| RG-OPE-37 | Au portail, la recherche d'un autre opérateur ne porte que sur les opérateurs actifs, exige au moins trois caractères (code, raison sociale ou nom commercial), renvoie au plus vingt résultats, et n'affiche que le code, la raison sociale, le nom commercial et la ville. | §5.5.2 p.67, décision du 2026-10-06 |
| RG-OPE-38 | Au back-office, le listing filtré s'exporte au format Excel, avec ses colonnes affichées. | §4.2.6 p.17, décision du 2026-10-06 |
| RG-OPE-39 | Il existe un seul opérateur de type `BNIA`, l'interprofession, créé par l'application (migration ou démonstration), jamais par la création d'un opérateur ; aucun type ne devient ni ne cesse d'être `BNIA`. Il n'a ni déclaratif (RG-OPE-01) ni code proposé ; il n'apparaît ni dans le listing des opérateurs, ni dans la recherche du portail, ni dans les lectures destinées aux DSM et aux statistiques. Il ne se désactive ni ne se supprime. Sa fiche se réduit à l'identité et aux coordonnées (menu « BNIA »). | Vision §1 et §2.2 règle 7, décision du 2026-10-08 |
| RG-OPE-40 | Inviter (module Accès, gestion) part d'un contact de l'opérateur ou d'une personne nouvelle (titre, nom, prénom, fonction, téléphone professionnel), avec un e-mail obligatoire et un rôle. L'invitation est refusée si une invitation en attente existe déjà pour ce contact ou pour cet e-mail dans l'opérateur (on la relance), ou si l'e-mail est celui d'un membre de l'opérateur. Partant d'un contact, son e-mail est mis à jour s'il diffère. Aucun utilisateur, rattachement ni contact n'est créé. Le jeton est aléatoire, conservé haché, à usage unique, valable 7 jours et pour cet e-mail seulement. L'e-mail part après la validation de la transaction ; un échec d'envoi n'annule rien et se reprend par une relance. À la création d'un opérateur, un ADMIN BNIA peut inviter un premier ADMIN dans le même formulaire. | §5.1.2 p.59, §5.13.3 p.79, vision UC-01, 02, 12 et §2.4 règles 2 et 3, décisions du 2026-10-08 |
| RG-OPE-41 | Relancer une invitation en attente remplace son jeton (l'ancien devient invalide) et repousse son expiration de 7 jours ; un nouvel e-mail part. L'annuler la passe à `ANNULEE`. Une invitation dont l'expiration est dépassée passe à `EXPIREE`. Dans tous les cas, le contact dont elle partait reste inchangé. | Vision UC-13 |
| RG-OPE-42 | Accepter une invitation : le jeton doit correspondre à une invitation `EN_ATTENTE` non expirée. Si son e-mail est celui d'un utilisateur, la personne se connecte (spec 03) ; sinon elle confirme nom et prénom (préremplis) et définit son mot de passe. Puis, en une transaction : trouver ou créer l'utilisateur (`authentification` crée alors son compte, sans mot de passe, que la personne pose aussitôt : spec 03 RG-AUT-11 et 15) ; refuser s'il a déjà un rattachement dans l'opérateur ; si l'invitation part d'un contact, supprimer ce contact et passer sa fiche au type `RATTACHEMENT`, sinon créer une fiche d'après l'invitation ; créer le rattachement `ACTIF` avec le rôle de l'invitation ; passer l'invitation à `ACCEPTEE`. Un utilisateur existant garde ses nom et prénom. S'il n'avait pas d'opérateur actif, celui de l'invitation le devient. Tout échec annule tout. Une invitation adressée à un e-mail qu'un utilisateur a quitté depuis (RG-OPE-48) ne peut plus être acceptée. | §5.1.2 p.59, vision UC-20 et §2.4 |
| RG-OPE-43 | Changer le rôle d'un membre, le suspendre (`SUSPENDU`) ou le réactiver (`ACTIF`) relève du module Accès (gestion), sous réserve de RG-OPE-35. L'effet est immédiat et ne vaut que pour cet opérateur ; un utilisateur qui n'a plus aucun rattachement utilisable ne peut plus se connecter. | Vision UC-14, 15 |
| RG-OPE-44 | Retirer l'accès d'un membre (module Accès, gestion ; RG-OPE-35) supprime son rattachement. Avec la case « conserver cette personne dans les contacts », un contact est créé avec une copie de ses titre, nom, prénom et e-mail à cet instant, et la fiche repasse au type `CONTACT` ; sans, la fiche est supprimée (RG-OPE-22, 23). S'il s'agissait de son dernier rattachement, l'utilisateur est supprimé avec son compte de connexion. Un utilisateur ne quitte pas un opérateur de lui-même ; un ADMIN peut se retirer s'il n'est pas le dernier. | Vision UC-16, §2.2 règle 6, §2.4 règles 6 et 7 |
| RG-OPE-45 | Supprimer un contact (module Contacts, gestion) annule les invitations en attente qui partent de lui, puis supprime le contact et sa fiche, après un avertissement qui nomme ses désignations (RG-OPE-22, 23). | §4.3.4 p.20, vision UC-11, décision du 2026-10-08 |
| RG-OPE-46 | Une fiche professionnelle se modifie par son titulaire (son propre rattachement `ACTIF`) ou par un rôle qui gère le module Contacts de l'opérateur ; l'auteur et la date sont enregistrés. Pour un membre, nom, prénom et e-mail sont affichés en lecture seule ; ceux d'un contact se modifient avec le contact. | Vision UC-11, 18, 25 |
| RG-OPE-47 | Seul l'utilisateur modifie ses titre, nom et prénom ; le changement se voit aussitôt dans tous ses opérateurs. | Vision UC-23, §2.1 |
| RG-OPE-48 | Changer son e-mail : refusé s'il est celui d'un autre utilisateur ; une seule demande en cours ; un lien valable 24 heures est envoyé au nouvel e-mail. À la confirmation, en une transaction : l'e-mail de l'utilisateur et son identifiant de connexion (spec 03) changent, la demande est close ; refusé si l'e-mail a été pris entre-temps. Une notification part à l'ancien e-mail. Les invitations déjà envoyées à l'ancien e-mail ne sont pas modifiées. | §5.1.5 p.60, vision UC-24, décision du 2026-10-08 |
| RG-OPE-49 | À la connexion, si l'opérateur actif de l'utilisateur est actif et que son rattachement y est `ACTIF`, l'application s'y ouvre directement ; sinon l'utilisateur choisit parmi ses rattachements utilisables. Le choix devient son opérateur actif ; il en change à tout moment. L'opérateur actif décide de l'espace : opérateur BNIA, back-office ; autre, portail. Chaque appel désigne l'opérateur concerné, et chaque cas d'usage y vérifie les droits de l'acteur. | §5.1.4 p.60, C.13.2.1 p.135, vision UC-21, 22, décision du 2026-10-08 |
| RG-OPE-50 | Un ADMIN de l'opérateur BNIA bloque ou débloque un utilisateur (`BLOQUE`, `ACTIF`). Bloquer exige un motif, conservé au journal. L'effet est immédiat dans tous les opérateurs : ses connexions sont refusées et sa session coupée (spec 03). Rattachements, opérateur actif et données ne changent pas ; débloquer rétablit la situation. Au blocage comme au déblocage, un e-mail part à l'utilisateur et aux ADMIN de chacun de ses opérateurs, sans le motif. L'annuaire affiche « bloqué par le BNIA » sur ce membre. | §4.2.6 p.17, vision UC-05 et §2.4, décision du 2026-10-08 (Q-OPE-17) |
| RG-OPE-51 | Opérateur, contact, fiche professionnelle et utilisateur portent un numéro de version : de deux modifications concurrentes, la seconde est refusée par un conflit (409) et l'écran se recharge. | Vision §2.2 règle 5, §2.4 règle 4 |
| RG-OPE-52 | L'annuaire d'un opérateur liste en une fois ses contacts, ses membres (nom et e-mail lus dans l'utilisateur, rôle, mentions « accès suspendu », « bloqué par le BNIA ») et les personnes nouvelles d'une invitation en attente (mention « invitation envoyée », aussi portée par un contact invité). Tout rattachement `ACTIF` le consulte ; les rôles de l'opérateur BNIA, pour tout opérateur. | §5.13.2 p.78, vision UC-03, 17 et §2.2 |
| RG-OPE-53 | Les rôles de l'opérateur BNIA (Accès, lecture) consultent tous les utilisateurs : nom, e-mail, statut, rattachements avec rôle et statut, blocage temporaire en cours et dernière connexion (lus dans `authentification`). | §4.2.2 p.14, §4.2.6 p.17, vision UC-05 |

## 5. Cas d'usage

### 5.1 Écriture

Chaque cas d'usage vérifie d'abord les droits de l'acteur sur l'opérateur concerné (RG-OPE-31 à 33),
journalise (RG-OPE-34) et envoie ses e-mails après la validation de la transaction (RG-OPE-40).

| Cas d'usage | Objets | RG |
|---|---|---|
| Lister et rechercher les opérateurs : type ; une seule recherche dans le code, la raison sociale, le nom commercial, le code postal et la ville (§4.3.1 p.18, décision du 2026-10-12) ; actif, incomplet ; export Excel (opérateur BNIA) | `Operateur` | 02, 32, 38, 39 |
| Proposer un code pour un type et un code postal | `Operateur` | 03 |
| Créer un opérateur, en invitant éventuellement un premier ADMIN (opérateur BNIA) | `Operateur`, `Invitation` | 01 à 12, 14, 15, 20, 21, 24, 32, 39, 40 |
| Modifier l'identité, le déclaratif, la production | `Operateur` | 01 à 13, 20, 21, 24, 36, 51 |
| Attribuer un nouveau code à un opérateur (opérateur BNIA) | `Operateur` | 03, 04, 05 |
| Modifier la diffusion des millésimes et l'accès aux statistiques | `Operateur` | 14, 15, 36 |
| Modifier la présentation : marques, circuits, visites, labels, œnotourisme, médailles | `PresentationOperateur` | 16 à 19, 21, 36 ; RG-REF-14 pour créer une marque |
| Désigner le contact principal et le destinataire des factures | `Operateur` | 22, 23, 24, 36 |
| Désactiver, réactiver, supprimer un opérateur (ADMIN BNIA) | `Operateur` et ses personnes | 25, 26, 39, 44 |
| Déclarer un contact (vision UC-10) | `Contact`, `FicheProfessionnelle` | 21, 27, 28, 30, 34 |
| Modifier un contact (UC-11) | `Contact` | 21, 27, 28, 34, 51 |
| Supprimer un contact (UC-11) | `Contact`, `FicheProfessionnelle`, `Invitation` | 22, 23, 34, 45 |
| Modifier une fiche professionnelle, la sienne ou celle d'un autre (UC-18, 25) | `FicheProfessionnelle` | 21, 30, 33, 34, 46, 51 |
| Inviter une personne : depuis un contact ou une personne nouvelle ; ajouter un ADMIN (UC-02, 12) | `Invitation`, `Contact` | 28, 29, 34, 40 |
| Relancer, annuler une invitation ; expirer les invitations échues (UC-13) | `Invitation` | 34, 41 |
| Accepter une invitation (UC-20) | `Utilisateur`, `Contact`, `FicheProfessionnelle`, `Rattachement`, `Invitation` ; compte de connexion (spec 03) | 29, 30, 34, 42, 49 |
| Changer le rôle d'un membre, le suspendre, le réactiver (UC-14, 15) | `Rattachement` | 34, 35, 43 |
| Retirer l'accès d'un membre, en le gardant ou non comme contact (UC-16) | `Rattachement`, `FicheProfessionnelle`, `Contact`, `Utilisateur` | 22, 23, 30, 34, 35, 44 |
| Modifier son profil (UC-23) | `Utilisateur` | 27, 34, 47, 51 |
| Demander puis confirmer un changement d'e-mail (UC-24) | `ChangementEmail`, `Utilisateur` ; identifiant de connexion (spec 03) | 29, 34, 48 |
| Choisir son opérateur actif (UC-21, 22) | `Utilisateur` | 49 |
| Bloquer, débloquer un utilisateur (UC-05, ADMIN BNIA) | `Utilisateur` | 34, 50 |
| Rechercher un autre opérateur (portail) | `Operateur` | 37, 39 |
| *Lecture* : calculer les droits d'un acteur sur un opérateur (tous les cas d'usage, « mes droits » du front, autres contextes) | `Rattachement`, `Utilisateur` | 31, 32, 33 |
| *Lecture* : consulter l'annuaire d'un opérateur (UC-03, 17) | `Contact`, `Rattachement`, `Invitation`, `Utilisateur` | 31, 52 |
| *Lecture* : consulter les utilisateurs (back-office) | `Utilisateur`, `Rattachement` | 32, 53 |
| *Lecture* : consulter le journal d'un opérateur ou d'une personne ; « Mes opérateurs » (UC-22) | journal, `Rattachement` | 31, 34, 49 |

### 5.2 Lecture

Presque tous les contextes lisent un opérateur. Ils le font par une **partie publiée en lecture seule**
(A-OPE-1), sur le modèle de `referentiels` (ADR-008) : `fr.bnia.operateurs.domain.partage` et
`fr.bnia.operateurs.application.partage.OperateursLecture`. Le reste du contexte leur est interdit
(ArchUnit). Le contenu exact de la partie publiée (vues, listes fermées, types `Droits` et `Acteur`, événements de la spec 03 A-AUT-1) est fixé à l'implémentation : `ContactVue`,
`ModulePortail` et les quatre événements échangés avec `acces` disparaissent. Besoins relevés :

| Besoin | Consommateur probable |
|---|---|
| Un opérateur par identifiant : code, raison sociale, nom commercial, type, actif | tous |
| Un opérateur par code (imports Excel) | `declarations`, `exportation` |
| Un opérateur par identifiant ODG, puis par CVI ou SIRET | `distillation`, `contratsvin` (passerelle ODG) |
| Type, périodicité, statut de déclaration ; opérateurs à `OUI` (sans l'opérateur BNIA, RG-OPE-39) | `declarations` (génération, relances, colonnes grisées) |
| Type | `cotisation` (assiette), `statistiques` |
| Adresse, pays, e-mail résolu du destinataire des factures | `facturation` (figés sur la facture), `boutique` |
| Raison sociale et adresse de l'expéditeur | `exportation` (certificat, figées) |
| Région (et « en région délimitée » via `referentiels`), appellations, diffusion des millésimes, accès aux statistiques | `statistiques` |
| L'utilisateur peut-il se connecter (non bloqué, un rattachement utilisable) ; utilisateur d'un e-mail | `authentification` (RG-AUT-04, 09) |
| Droits d'un utilisateur sur un opérateur (rôle effectif par module, y compris par l'opérateur BNIA) | tous les contextes, à chaque requête (RG-OPE-31, 32) |
| Nom d'un utilisateur, pour la copie de l'auteur d'une action | tous les contextes (C.3, C.4, C.6, C.7) |
| Personnes d'un opérateur (contacts et membres) par centre d'intérêt, par rôle | `notifications` (C.14 p.136) |
| Recherche restreinte (RG-OPE-37) | `contratsarmagnac` (écran du portail) |

**Auteur d'une action dans un autre contexte** (décision du 2026-10-08) : un utilisateur est supprimé
quand il perd son dernier rattachement (RG-OPE-44). Un contexte qui enregistre un auteur (DSM « saisie
par », contrat « validé par »…) stocke donc l'identifiant de l'utilisateur **sans clé étrangère**, avec
une copie de son nom à l'instant de l'action. Exception nommée à ADR-011 (A-OPE-8).

## 6. Écrans

L'opérateur actif décide de l'espace (RG-OPE-49) ; un sélecteur d'opérateur actif est dans l'en-tête des
deux espaces. Les entrées de menu et les actions s'affichent selon les droits (§4.2.6 p.17).

### 6.1 Back-office (opérateur actif : BNIA)

- **Menu « Opérateurs »** (tous rôles BNIA) : listing de tous les opérateurs sauf l'opérateur BNIA
  (RG-OPE-39) ; filtre type et un seul champ de recherche, trouvé dans le code, la raison sociale, le nom
  commercial, le code postal ou la ville (les cinq filtres texte du §4.3.1 p.18 réunis, décision du
  2026-10-12), plus actif / inactif et « fiche incomplète » ; colonnes type, code, raison sociale, nom commercial, code
  postal, ville, téléphone 1, e-mail général ; ouverture directe de la fiche ; création (Contacts,
  gestion) ; export Excel (RG-OPE-38).
- **Création** : type d'abord, puis code postal ; le code proposé se remplit (RG-OPE-03) et reste
  modifiable ; avertissements de doublon affichés sans bloquer ; bloc facultatif « inviter un premier
  ADMIN » (nom, prénom, e-mail), pour un ADMIN BNIA (RG-OPE-40).
- **Fiche opérateur**, consultation et édition par onglets, ouverte sans changer d'opérateur actif :
  - *Général* : identité (dont titre), coordonnées, e-mail général, destinataire des factures (bascule
    « une personne de l'annuaire » / « une adresse »), contact principal (une personne de l'annuaire),
    membre BNIA, accord de diffusion général, commentaire interne ; indicateur d'incomplétude ; alerte de
    code non conforme avec « attribuer un nouveau code » (RG-OPE-05) ; autres fiches du groupe
    (RG-OPE-09) ;
  - *Production*, *Diffusion*, *Marques*, *Visites et labels*, *Œnotourisme* (dont médailles),
    *Statistiques* : inchangés ;
  - *Annuaire* : la liste de RG-OPE-52 (nom, fonction, e-mail, téléphone, état : contact, rôle,
    « accès suspendu », « invitation envoyée ») ; déclarer un contact ; inviter ; ouvrir une personne ;
    invitations en attente (relancer, annuler) ;
  - *Journal* : actions sur les personnes et les accès de l'opérateur (RG-OPE-34) ;
  - blocs ajoutés par les étapes suivantes : dernière DSM et son état, historique des DSM, factures,
    contrats (§4.3.3, §2.2).
- **Actions sur l'opérateur** (ADMIN BNIA) : désactiver, réactiver, supprimer (refusée si l'opérateur est
  utilisé : le message propose de désactiver), avec la liste des effets sur les accès (RG-OPE-25, 26).
- **Menu « BNIA »** : l'opérateur BNIA, en deux onglets : *Fiche* réduite (identité et coordonnées, sans
  déclaratif ni présentation, RG-OPE-39), dont les coordonnées servent aussi à l'assistance du portail
  (§5.2.2 p.61) ; *Annuaire* (personnel, invitations), avec les mêmes écrans que l'annuaire d'un
  opérateur.
- **Menu « Accès »** : *Utilisateurs* (RG-OPE-53 : liste, filtres nom, e-mail, statut, opérateur ;
  blocage avec motif et déblocage pour un ADMIN BNIA, RG-OPE-50) ; *Journal des connexions* (spec 03).
- **Menu « Paramétrage »** : écrans de la spec 01, selon le module Paramétrage.

### 6.2 Portail (opérateur actif : autre que BNIA)

- **« Mon entreprise »** : la fiche de l'opérateur actif, dans les mêmes onglets que le back-office ;
  lecture pour tout membre ; modification des seuls champs de RG-OPE-36 par le module Contacts ;
  commentaire interne masqué.
- **« Annuaire »** : la liste de RG-OPE-52 ; contacts et fiches selon le module Contacts ; invitations,
  rôles, suspensions et retraits selon le module Accès ; journal. Les actions qui laisseraient
  l'opérateur sans ADMIN sont refusées avec un renvoi vers le BNIA (RG-OPE-35).
- **Composant de recherche d'opérateur** (RG-OPE-37), utilisé par les écrans de contrats du portail.

### 6.3 Communs aux deux espaces

- **Fiche d'une personne** : identité (en lecture seule pour un membre), fiche professionnelle, et pour
  un membre son rôle et son statut ; actions selon les droits : modifier, supprimer (contact), inviter
  (contact), changer le rôle, suspendre, réactiver, retirer l'accès (case « conserver dans les
  contacts »), avec la liste des effets avant confirmation (désignations, dernier ADMIN).
- **Accepter une invitation** (lien de l'e-mail) : présentation de l'opérateur et du rôle ; connexion si
  l'e-mail a déjà un compte, sinon nom, prénom (préremplis) et mot de passe (spec 03) ; messages des refus
  de RG-OPE-42.
- **Confirmer un changement d'e-mail** (lien de l'e-mail).
- **Choix de l'opérateur actif** (après connexion, si nécessaire) et sélecteur en en-tête.
- **« Mon compte »** : *Profil* (titre, nom, prénom), *E-mail* (changement), *Mes opérateurs*
  (rattachements, rôle, statut, opérateur actif), *Ma fiche* dans chaque opérateur ; et les sections
  *Mot de passe* et *Mes connexions* de la spec 03.

## 7. Données

### 7.1 Démonstration (profil `demo`)

Des données plausibles, présentées comme telles, sur les référentiels de démonstration de la spec 01 :
- l'opérateur BNIA (type `BNIA`) et une quinzaine d'opérateurs du Gers, des Landes et du Lot-et-Garonne :
  producteurs (`000001`…), négociants (`N320001`…, `N400001`), deux `AUTRE` (la Distillerie ambulante
  Lafitte, un courtier) ;
- un groupe « Maison Durand » de deux chais (Condom et Eauze) ;
- des alambics des deux types, une diffusion partielle (quelques millésimes), des marques France et
  Export, des visites, labels et médailles ;
- une fiche incomplète (sans adresse) et une fiche inactive ;
- des personnes et des accès (décision du 2026-10-08), mot de passe `demo-armagnac` et code `000000`
  (spec 03), listés dans `docs/demo.md` :

| Opérateur | Personne | Identifiant | État |
|---|---|---|---|
| BNIA | Anne Lamarque | `admin@bnia.fr` | membre `ADMIN` |
| BNIA | Luc Dubos | `agent@bnia.fr` | membre `AGENT` |
| BNIA | Claire Sentex | `observateur@bnia.fr` | membre `OBSERVATEUR` |
| Maison Durand, Condom | Jeanne Durand | `admin@producteur.fr` | membre `ADMIN` (opérateur actif) |
| Maison Durand, Eauze | Jeanne Durand | `admin@producteur.fr` | membre `ADMIN` |
| Maison Durand, Condom | Sophie Martin | `agent@producteur.fr` | membre `AGENT` |
| Maison Durand, Condom | Pierre Roy | `observateur@producteur.fr` | membre `OBSERVATEUR` |
| Maison Durand, Eauze | Pierre Roy | `observateur@producteur.fr` | membre `OBSERVATEUR`, `SUSPENDU` |
| Armagnacs de Gascogne (`N320001`) | Henri Castex | `admin@negociant.fr` | membre `ADMIN` |
| Armagnacs de Gascogne | Julie Darrigade | `agent@negociant.fr` | membre `AGENT` |
| Armagnacs de Gascogne | Thomas Barrère | `observateur@negociant.fr` | membre `OBSERVATEUR` |
| Maison Durand, Condom | Paul Petit | | contact (maître de chai), désigné contact principal |
| Distillerie ambulante Lafitte | Marc Lafitte | | invitation `EN_ATTENTE`, rôle `ADMIN` |
| tous les autres | | | aucun membre |

### 7.2 Reprise AGI

Reprise des données hors périmètre du prototype (décision du 2026-10-08). Le tableau ci-dessous
reste comme note ; sa ligne « Utilisateurs extranet » est caduque (ni contact porteur de droits, ni
modules).

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

**Révision du 2026-10-08** : les événements de A-OPE-2 échangés avec `acces` n'ont plus d'objet (le
mécanisme reste, pour `authentification` : spec 03 A-AUT-1) ; A-OPE-3 est remplacée par A-OPE-6 (les
contacts sortent de `Operateur`). Décisions A-OPE-6 à 9 ajoutées, actées par ADR-016 et les amendements d'ADR-011, 012 et 013.

Décidées le 2026-10-06, actées par ADR-012, ADR-013 et l'amendement d'ADR-009.

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-OPE-1 | `operateurs` publie une **partie en lecture seule**, comme `referentiels` : `fr.bnia.operateurs.domain.partage` (vues immuables, listes fermées, types d'événements) et `fr.bnia.operateurs.application.partage.OperateursLecture`. L'exception de l'ADR-008 est étendue, nommément, à `operateurs`, lu par presque tous les contextes. Les consommateurs déclarent la dépendance Maven et une clé étrangère vers `operateurs.operateur`. | Un port par consommateur : une dizaine de ports et d'adapters de traduction, qui exigeraient de toute façon une API publiée (ou un SQL sur le schéma d'un autre contexte, écarté par l'ADR-008). Copie locale par événements : synchronisation et reprise disproportionnées pour un monolithe prototype, et plus de clé étrangère. | 012 (étend 008) |
| A-OPE-2 | Événements entre contextes : **Spring `ApplicationEvent`, synchrones**, l'écouteur appelant son cas d'usage dans la transaction de l'émetteur (tout ou rien). L'application publie par un port `PublicationEvenements` ; l'adapter Spring et les écouteurs (`@EventListener`) vivent dans un nouveau package d'adapter `adapter.evenement`. Les **types d'événements** échangés avec `acces` sont des records de `operateurs.domain.partage`, ceux qu'il émet comme ceux qu'il accepte : `acces` dépend de `operateurs`, jamais l'inverse. Un e-mail ne part qu'après validation de la transaction. | Écoute après validation (`AFTER_COMMIT`) : un échec de `acces` laisserait un accès sans compte, sans reprise. Types dans `commun` : le noyau commun se chargerait du vocabulaire de deux contextes (ADR-009). | 013 |
| A-OPE-3 | Deux agrégats : **`Operateur`**, qui contient ses **contacts** (les règles RG-OPE-22, 23, 28 et 30 deviennent locales), et **`PresentationOperateur`**, 1 pour 1 de même identifiant, modifié sans réécrire la fiche. Le journal des contacts est une table hors agrégat, en ajout seul. | Contact en agrégat séparé : invariants répartis dans les cas d'usage. Un seul agrégat : une quinzaine de tables réécrites à chaque case cochée (Spring Data JDBC), conflits entre le BNIA et le portail. | 012 |
| A-OPE-4 | **`Millesime`** (année valide) et **`Appellation`** (cinq AOC) entrent dans **`commun.domain`**, à côté de `CompteDAge` : concepts métier stables, d'usage certain dans plusieurs contextes (DSM, export, contrats, statistiques). | `Appellation` dans la partie publiée de `operateurs` : les contrats en dépendraient pour typer leur propre appellation. Les garder dans `operateurs` jusqu'au deuxième usage écrit : déplacement de code certain à l'étape 5 ou 6. | 009 (amendé) |
| A-OPE-5 | Le numéro suivant d'un code se calcule par **le plus grand numéro existant, plus un**, lu par un port de lecture (`JdbcClient`) pour chaque série (6 chiffres ; `N` + département). L'index unique sur le code tranche une création simultanée : 409, et l'écran repropose un code. | Table de compteurs ou séquences PostgreSQL : sûres en concurrence, mais en conflit avec les codes saisis à la main, et des séquences par département seraient lourdes dans la migration unique (ADR-005). | aucun (choix local) |
| A-OPE-6 | **Un agrégat par objet** pour les personnes et les accès : `Contact`, `FicheProfessionnelle`, `Rattachement`, `Invitation`, `Utilisateur` (avec sa demande de changement d'e-mail), chacun son repository ; `Operateur` ne contient plus ses contacts (remplace A-OPE-3 sur ce point). Les règles collectives vivent dans les cas d'usage, appuyées par la base : index uniques (e-mail parmi les contacts d'un opérateur ; un rattachement par opérateur et utilisateur ; une invitation `EN_ATTENTE` par e-mail et opérateur, index partiel ; une fiche par porteur), et **verrou sur la ligne de l'opérateur** (`select … for update`) dans tout cas d'usage qui change les ADMIN (RG-OPE-35). Une version par objet (RG-OPE-51). Plus d'identifiant tiré de la séquence avant l'insertion pour ces objets. | Agrégat `Annuaire` par opérateur (règles locales, mais une version pour toutes les personnes : conflit entre deux fiches modifiées en même temps). Tout dans `Operateur` (chaque modification de la fiche réécrit les personnes, conflits sans rapport). | 016 ; 012 amendé |
| A-OPE-7 | **Droits vérifiés dans les cas d'usage seuls.** Le controller passe l'`Acteur` (utilisateur connecté) ; chaque cas d'usage demande ses `Droits` sur l'opérateur concerné (rôle effectif par module, y compris par l'opérateur BNIA) et vérifie tout, règles simples (module, niveau) et règles liées aux données (sa propre fiche, champs réservés RG-OPE-36, dernier ADMIN sauf BNIA). Les droits sont publiés aux autres contextes par la partie publiée ; le front lit « mes droits » pour ses menus. | Annotation sur chaque endpoint vérifiée par un intercepteur, avec ou sans vérification complémentaire dans les cas d'usage. | 016 |
| A-OPE-8 | **Auteur d'une action sans clé étrangère** : un autre contexte stocke l'identifiant de l'utilisateur et une copie de son nom à l'instant de l'action, l'utilisateur pouvant être supprimé (RG-OPE-44). Exception nommée à ADR-011. | Utilisateur jamais supprimé (écart à la vision, comptes dormants) ; suppression bloquée s'il est auteur (deux comportements). | 011 amendé |
| A-OPE-9 | **Identifiants `bigint`** pour les nouveaux objets, comme partout (ADR-011 maintenu) ; le principe 6 de la vision (UUID) n'est pas suivi, l'e-mail n'étant jamais un identifiant. | UUID partout ; UUID pour les seuls nouveaux objets. | 011 amendé |

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
| ~~H-OPE-5~~ | Profils de l'extranet → droits : « Admin Professionnel » → administrateur ; « Statistiques » → `STATISTIQUES` ; « Contrats » → tout ce que l'extranet couvre aujourd'hui : `CONTRATS_ARMAGNAC`, `PLURIANNUELS_ARMAGNAC`, `PROPOSITIONS_ARMAGNAC`, `CONTRATS_VIN`, `PLURIANNUELS_VIN`, `PROPOSITIONS_VIN`, `FINS_DE_TRAVAUX` (décision du 2026-10-06). Les autres modules ne sont donnés qu'aux administrateurs, qui les délèguent. | AGI 3.8.2 p.22 ne donne que ces trois libellés ; l'extranet couvre contrats, pluri-annuels, propositions et fins de travaux (§2.1 p.7, AGI 3.1-3.7). Personne ne perd ce qu'il a (Q-OPE-14). **Caduque le 2026-10-08** : plus de modules par personne, et reprise hors périmètre du prototype. |

## 10. Questions ouvertes

Revue du 2026-10-06 : aucune question ne reste à trancher par nous. Le prototype se construit sur les
décisions et hypothèses ; seules la reprise et `declarations` (Q-OPE-7) dépendent des réponses.

**À envoyer au BNIA** : Q-OPE-1, 2, 4, 6, 7, 8, 9 (questions) ; Q-OPE-3, 5, 10, 11, 12, 13, 15, 16, 17
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
| ~~Q-OPE-10~~ | Les droits portail sont-ils portés par le contact (C.1.9.2) ou par le rattachement du compte (C.13.2) ? **Tranché le 2026-10-06 : par le contact** ; le droit « gestion des contacts » devient le rôle d'administrateur. À confirmer par le BNIA. **Révisé le 2026-10-08 : par le rattachement**, avec un rôle fixe (C.13.2 suivi, vision). |  |
| ~~Q-OPE-11~~ | La Blanche Armagnac fait-elle partie des appellations de la fiche (absente de C.1.1.2, présente dans AGI 2.1.1) ? **Tranché le 2026-10-06 : oui.** À confirmer par le BNIA. | |
| ~~Q-OPE-12~~ | Quelle diffusion des millésimes par défaut pour une nouvelle fiche ? **Tranché le 2026-10-06 : refus.** À confirmer par le BNIA. | |
| ~~Q-OPE-13~~ | Le code négociant prend-il le département du code postal, avec un incrément par département ? **Tranché le 2026-10-06 : oui** (RG-OPE-03). À confirmer par le BNIA, avec le cas des négociants étrangers. | |
| ~~Q-OPE-14~~ | Quels droits donnent les profils de l'extranet (« Admin Professionnel », « Statistiques », « Contrats ») ? **Tranché le 2026-10-06 : lecture large** (H-OPE-5). À confirmer par le BNIA. **Caduque le 2026-10-08** (reprise hors périmètre). |  |
| ~~Q-OPE-15~~ | Le bureau des douanes, typé « Référentiel » en C.1.1.1, peut-il rester un texte libre ? **Tranché le 2026-10-06 : texte libre** (aucune liste au paramétrage, texte dans InteGraal). À confirmer par le BNIA. | |
| ~~Q-OPE-16~~ | Les droits seront-ils affinés après le prototype (plusieurs rôles par personne, droits plus fins par module) ? (vision §2.4, point à valider 1) **Tranché le 2026-10-08 : non, les trois rôles fixes sont la cible**, pas seulement le prototype. | |
| ~~Q-OPE-17~~ | Un utilisateur bloqué par le BNIA est-il signalé aux ADMIN de ses opérateurs ? Le BNIA doit-il saisir un motif ? (vision §2.4, point à valider 2) **Tranché le 2026-10-08 : motif obligatoire, au journal ; e-mail à l'utilisateur et aux ADMIN de ses opérateurs, sans le motif, au blocage et au déblocage** (RG-OPE-50). | |

## 11. Tests attendus

Chaque RG-OPE-nn a au moins un test qui porte son identifiant (CLAUDE.md).
- **Tests de domaine**, sans base : RG-OPE-01, 02, 03 (calcul du code à partir d'un numéro courant et
  d'un code postal), 05, 06 (formats), 09 (normalisation), 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
  22, 23, 24, 27, 28 (format), 29, 30, 31 et 32 (évaluation des droits), 33, 35 (dernier ADMIN), 36, 39,
  41 (états de l'invitation), 43, 44 (conversion de la fiche), 46, 47, 50.
- **Tests de cas d'usage**, avec repositories simulés, puis IT : RG-OPE-03 (numéro suivant), 04, 06 et
  08 (doublons), 07, 21, 25 (suppression refusée par clé étrangère, cascade des personnes), 26 (gel des
  rattachements), 28 (unicité dans l'opérateur, avertissements), 29, 34 (journal), 37, 38, 40, 41, 42
  (transaction complète, refus), 44 (dernier rattachement : utilisateur et compte supprimés), 45, 48, 49,
  51 (conflit de version), 52, 53.

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
| 2026-10-08 | Révision, phases B et C avec la spec 03 (`authentification`), d'après la vision du 2026-10-08 : personnes et accès dans ce contexte (contact, utilisateur, fiche professionnelle, rattachement, invitation, changement d'e-mail, journal) ; BNIA opérateur de type `BNIA` (RG-OPE-01, 03, 39), ses rôles valant pour tous les opérateurs ; rôles fixes et modules Contacts (avec la fiche), Accès, Paramétrage ; contact sans état actif ; champs professionnels dans la fiche ; désignations vers la fiche (RG-OPE-22, 23) ; RG-OPE-36 conservée ; fin d'un opérateur par un ADMIN BNIA, rattachements gelés (RG-OPE-25, 26) ; dernier ADMIN protégé sauf par le BNIA (RG-OPE-35) ; création d'opérateur sans ADMIN obligatoire ; utilisateur supprimé avec son dernier rattachement, auteur copié ailleurs ; invitation 7 jours, changement d'e-mail 24 heures ; démonstration de six comptes. RG-OPE-27 à 35 réécrites, 39 à 53 ajoutées ; RG-OPE-15, 22, 23, 25, 26, 36 révisées ; H-OPE-5 et Q-OPE-14 caduques, Q-OPE-10 révisée ; reprise hors périmètre du prototype. |
| 2026-10-08 | Révision, phase E (avec la spec 03) : A-OPE-6 (un agrégat par objet, verrou sur l'opérateur pour le dernier ADMIN, index uniques), A-OPE-7 (droits vérifiés dans les cas d'usage), A-OPE-8 (auteur sans clé étrangère), A-OPE-9 (`bigint`) ; A-OPE-3 remplacée sur les contacts ; RG-OPE-42 précisée (compte créé par `authentification`, premier mot de passe). |
| 2026-10-08 | Révision, phase F : Q-OPE-16 tranchée (rôles fixes, cible définitive) ; Q-OPE-17 tranchée (motif de blocage obligatoire, e-mail à l'utilisateur et aux ADMIN au blocage et au déblocage : RG-OPE-50, 52 révisées) ; menu « BNIA » avec fiche réduite et annuaire (RG-OPE-39 complétée) ; nom et prénom obligatoires à l'invitation d'une personne nouvelle et titre facultatif de l'utilisateur confirmés. |
| 2026-10-08 | Phase G, relecture : cas d'usage de lecture ajoutés (droits, annuaire, utilisateurs, journal) pour citer RG-OPE-31, 52, 53 ; commentaire interne « visible de l'opérateur BNIA seulement ». Glossaire : BNIA, rôle, opérateur, type d'opérateur, contact, contact principal, destinataire des factures corrigés ; administrateur, module portail, compte portail, compte opérateur actif remplacés ; personnel BNIA, module, utilisateur, membre, rattachement, fiche professionnelle, invitation, annuaire, suspension, blocage par le BNIA, opérateur actif ajoutés. |
| 2026-10-08 | Révision validée. À réimplémenter avec la spec 03 : personnes et accès, type `BNIA`, désignations vers la fiche professionnelle, droits. |
| 2026-10-08 | Phase H : ADR-016 (personnes et accès dans `operateurs`) rédigé ; ADR-011, 012 et 013 amendés ; `CLAUDE.md` mis à jour. |
| 2026-10-08 | Révision implémentée, avec la spec 03. Choix faits pendant l'implémentation, sans question : **endpoints unifiés** `/api/operateurs/{id}/…` pour les deux espaces, les droits vérifiés dans les cas d'usage (ADR-016) ; l'identité complète (`PUT …/identite`), la production et l'accès aux statistiques réservés au personnel du BNIA, le reste de la fiche par `PUT …/profil` (RG-OPE-36) ; **numéro de version** porté par chaque saisie de modification, `null` pour un appel interne (démonstration), écart refusé en 409 (`ModificationConcurrente`, RG-OPE-51) ; **verrou** `for update` sur l'opérateur pour le dernier ADMIN (RG-OPE-35) ; **opérateur BNIA** inséré par `V1__schema.sql` (structurel), son premier ADMIN amorcé à l'installation par `bnia.amorce.email`, `nom`, `prenom` (invitation, si le BNIA n'a ni ADMIN ni invitation en attente) ; **contrôle du Paramétrage** par un port `ControleParametrage` de `referentiels`, implémenté dans `bnia-boot` avec `OperateursLecture` (`referentiels` ne peut dépendre de `operateurs`) ; **invitation d'un compte existant** : l'invitation retient l'utilisateur visé (`compteVise`), refusée s'il a changé d'e-mail entre-temps (RG-OPE-42) ; **expiration** des invitations à la lecture et par une tâche planifiée horaire ; **e-mails** en texte simple, envoyés après la validation de la transaction (Mailpit en dev, ports `BNIA_SMTP_PORT` et `BNIA_MAILPIT_PORT`) ; liens `/invitation?jeton=` et `/changement-email?jeton=` ; **front** : un seul jeu d'écrans, l'espace ne décidant que des liens, les actions affichées selon les droits renvoyés par l'API (`mes-droits`, `droits` de la fiche) ; fiche réduite pour l'opérateur BNIA (menu « BNIA »), annuaire et journal en onglets au back-office, menu « Annuaire » au portail ; sélecteur d'opérateur actif sous le titre de l'espace. Démonstration conforme au §7.1, comptes dans `docs/demo.md`. Chaque RG-OPE-01 à 53 a au moins un test qui porte son identifiant. |
| 2026-10-08 | Démonstration (demande de l'utilisateur) : identifiants de connexion lisibles, un par rôle et par type d'opérateur (`admin@`, `agent@`, `observateur@` × `bnia.fr`, `producteur.fr`, `negociant.fr`) ; trois membres ajoutés au négociant Armagnacs de Gascogne ; Pierre Roy aussi observateur actif à Condom, pour que son compte se connecte (sa suspension à Eauze reste montrée). §7.1 et `docs/demo.md` mis à jour. Mot de passe de démonstration `demo-armagnac` (13 caractères, RG-AUT-07 respectée), à la place de `demo-bnia-2026`. |
| 2026-10-12 | Listing du back-office (demande de l'utilisateur) : les filtres code, raison sociale, nom commercial, code postal et ville (§4.3.1 p.18) réunis en un seul champ de recherche (paramètre `recherche`, « contient », sans distinction de casse, une colonne suffit), pour l'écran comme pour l'export Excel. Le code postal se cherche désormais n'importe où dans la valeur, et plus seulement par son début. |
