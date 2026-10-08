# 03 : authentification

> Contexte `authentification` (étape 3 de `docs/contextes.md`, ex-`acces`). Statut : **terminée** (validée le 2026-10-08).
> Références : `p.N` = page du CDC v1.1. Vision : `docs/specs/02-operateurs/Spécification fonctionnelle
> gestion des opérateurs, contacts et accès.md` (2026-10-08), citée « vision §n ».
> `H-AUT-n` : hypothèse retenue, révisable. `Q-AUT-n` : question ouverte, suivie dans cette spec (§10).

## Sommaire

- [1. Objet](#1-objet)
- [2. Périmètre](#2-périmètre)
  - [2.1 Dans le contexte](#21-dans-le-contexte)
  - [2.2 Hors du contexte](#22-hors-du-contexte)
- [3. Modèle](#3-modèle)
  - [3.1 Compte de connexion](#31-compte-de-connexion)
  - [3.2 Code de vérification](#32-code-de-vérification)
  - [3.3 Réinitialisation du mot de passe](#33-réinitialisation-du-mot-de-passe)
  - [3.4 Navigateur de confiance](#34-navigateur-de-confiance)
  - [3.5 Session](#35-session)
  - [3.6 Journal des connexions (hors agrégat, en ajout seul)](#36-journal-des-connexions-hors-agrégat-en-ajout-seul)
- [4. Règles de gestion](#4-règles-de-gestion)
- [5. Cas d'usage](#5-cas-dusage)
  - [5.1 Écriture](#51-écriture)
  - [5.2 Lecture](#52-lecture)
- [6. Écrans](#6-écrans)
  - [6.1 Communs à tous les utilisateurs](#61-communs-à-tous-les-utilisateurs)
  - [6.2 Back-office](#62-back-office)
- [7. Données](#7-données)
  - [7.1 Démonstration (profil `demo`)](#71-démonstration-profil-demo)
  - [7.2 Reprise AGI](#72-reprise-agi)
- [8. Décisions d'architecture](#8-décisions-darchitecture)
- [9. Hypothèses](#9-hypothèses)
- [10. Questions ouvertes](#10-questions-ouvertes)
- [11. Tests attendus](#11-tests-attendus)
- [Journal](#journal)

## 1. Objet

Prouver l'identité d'une personne et tenir sa session : mot de passe, double authentification par
e-mail, blocage après des échecs répétés, mot de passe oublié, journal des connexions.

Le contexte est une **infrastructure remplaçable** (vision §2.1) : il ne porte ni nom, ni prénom, ni
droit, ni opérateur. Tout le métier des personnes et des accès (utilisateurs, rattachements, rôles,
invitations, contacts, fiches professionnelles) est dans `operateurs` (spec 02, révisée en conséquence).
Pour accepter une connexion, il demande à `operateurs` si l'utilisateur peut se connecter (non bloqué,
au moins un rattachement actif, vision UC-21). La session ne porte que l'identifiant de l'utilisateur,
jamais ses droits (vision §2.1 principe 7).

Sa valeur : une connexion sûre et conforme au CDC (blocage, mot de passe oublié, double
authentification, traçabilité) que l'on peut remplacer (Keycloak, autre fournisseur) sans toucher au
métier.

Sources : §4.2.3, 4.2.4, 4.2.6, 4.2.7 (p.14-17) · §5.1 (p.59-60) · C.2.1 (p.105-106) · C.2.3 (p.107) ·
C.13.1 (p.135) · vision §2.1, §2.3 (UC-20, 21, 24, 05), §2.4.
Usages relevés ailleurs : §4.1 et §5.2 (tableaux de bord adaptés aux droits, p.13 et 61-62) · §9 (accès
et authentification au premier lot, p.92) · C.1.1.1 (code d'accès Internet historique, p.101).

## 2. Périmètre

### 2.1 Dans le contexte

| Famille | Élément | Code (ADR-001) | Source |
|---|---|---|---|
| Compte | Compte de connexion d'un utilisateur de `operateurs` : identifiant de connexion (son e-mail), mot de passe haché, état | `CompteConnexion` | C.2.1.1 p.105, C.13.1.1 p.135, vision UC-20 et §2.2 |
| | Définition du mot de passe à l'acceptation d'une invitation | | §5.1.2 p.59, vision UC-20 |
| | Changement du mot de passe depuis son compte | | §5.1.2 p.59, §5.1.5 p.60 |
| | Mot de passe oublié : demande, lien, réinitialisation | | §4.2.6 p.17, §5.1.2 p.59 |
| | Report d'un changement d'e-mail sur l'identifiant de connexion | | vision UC-24 |
| Connexion | Connexion, déconnexion, session | | §4.2.6 p.17, §5.1.1 p.59 |
| | Double authentification par code envoyé par e-mail, pour **tous** les utilisateurs | | §4.2.7 p.17, §5.1.3 p.60, décision du 2026-10-08 |
| | Blocage temporaire de 30 minutes après 3 échecs de connexion | | §4.2.6 p.17, §5.1.1 p.59 |
| | Refus de connexion et coupure des sessions d'un utilisateur bloqué ou sans rattachement actif | | §5.1.6 p.60, vision UC-05, UC-15, UC-21 |
| Traçabilité | Journal des connexions : réussies, échouées, blocages, double authentification, réinitialisations | `JournalConnexion` | §4.2.4 p.15, C.2.3 p.107, §5.1.6 p.60 |

**Décisions de périmètre du 2026-10-08.**
- **Découpage.** Le contexte 3, prévu sous le nom `acces` pour les comptes, les rôles BNIA et des
  événements avec `operateurs`, devient `authentification`, sans métier. Utilisateurs, rattachements,
  rôles, invitations et droits vont dans `operateurs`, pour que les transitions contact ↔ membre se
  fassent dans une seule transaction (vision §2.1). Écarté : garder le nom `acces` (il laisserait croire
  qu'il porte les droits) ; garder le métier des accès dans `acces` (deux propriétaires pour une même
  personne, événements entre contextes).
- **Socle construit.** Mot de passe, mot de passe oublié, blocage après 3 échecs, journal des
  connexions, et double authentification **par e-mail seulement, pour tous les utilisateurs** (écart
  assumé : le CDC l'impose aux seuls profils d'administration BNIA et la propose au portail). En dev et
  en démo, le code vaut toujours `000000`. **Spring Security seul**, pas de Keycloak.
- **BNIA.** Le BNIA est un opérateur, de type `BNIA` (vision §1, principe 2). Son personnel se connecte
  comme les autres ; un rôle tenu dans l'opérateur BNIA vaut pour tous les opérateurs (spec 02 révisée).
- **Espaces.** Une seule connexion ; l'opérateur actif décide de l'espace : opérateur BNIA, back-office ;
  autre opérateur, portail (vision UC-21, 22).
- **Journal.** Un journal par contexte : `authentification` trace les connexions, `operateurs` les
  actions sur les personnes et les accès.
- **Reprise des données** : hors périmètre du prototype.

### 2.2 Hors du contexte

| Élément décrit dans le CDC | Contexte retenu | Motif |
|---|---|---|
| Utilisateur : nom, prénom, e-mail, statut (actif, bloqué par le BNIA), opérateur actif (§4.2.2 p.14, C.2.1.1 p.105, §5.1.5 p.60) | `operateurs` | Métier : la personne est propriétaire de son identité (vision §2.1). Créé à l'acceptation d'une invitation seulement (vision principe 3). |
| Rattachement d'un utilisateur à un opérateur, rôle, opérateur actif, bascule (§5.1.4 p.60, C.13.2 p.135) | `operateurs` | Vision §2.2. Les droits sont évalués à chaque requête par `operateurs` (vision principe 7). |
| Rôles composables par le BNIA, catalogue de droits module × action, « administrateur global » (§4.2.1 p.14, §4.2.5 p.15-17, §4.2.6 p.17, C.2.2 p.106) | **aucun** | Écart assumé : rôles fixes `OBSERVATEUR`, `AGENT`, `ADMIN`, définis par l'application (vision §2.5) ; un nouveau module ajoute une colonne au tableau rôles × modules. |
| Population distincte des utilisateurs BNIA (§4.2.2 p.14, C.2.1 p.105-106) | `operateurs` | Le personnel BNIA est rattaché à l'opérateur `BNIA` ; son rôle y vaut pour tous les opérateurs (décision du 2026-10-08). |
| Droits délégables par module au portail (12 droits) et modules autorisés du rattachement (§5.13.2 p.78-79, C.1.9.2 p.105, C.13.2 p.135) | **aucun** | Écart assumé : un rôle par rattachement ; tout rattachement actif lit tout son opérateur (vision §2.5). |
| Invitation et lien d'activation (§5.1.2 p.59, §5.13.3 p.79) | `operateurs` | Vision UC-12, 13, 20 : l'invitation prouve l'e-mail et crée l'utilisateur. |
| Changement d'e-mail confirmé par lien (§5.1.5 p.60) | `operateurs` | Vision UC-24 ; `authentification` reporte la nouvelle adresse sur l'identifiant de connexion. |
| Blocage d'un utilisateur par le BNIA (§4.2.6 p.17 « comptes désactivés ») | `operateurs` | Vision UC-05 : statut de l'utilisateur ; `authentification` refuse la connexion et coupe les sessions. |
| Journal des actions sur les accès : créations, changements de rôle, désactivations (§4.2.4 p.15, C.2.3 p.107, §5.13.3 p.79) | `operateurs` | Un journal par contexte (décision du 2026-10-08) ; celui de `operateurs` étend son journal des contacts. |
| Double authentification par application TOTP (§4.2.7 p.17, §5.1.3 p.60, C.13.1.1 p.135) | **aucun** | Écart assumé : code par e-mail seulement (décision du 2026-10-08). |
| Code d'accès Internet historique (C.1.1.1 p.101) | **aucun** | Déjà abandonné (spec 02, Q-OPE-3). |
| Utilisateurs de l'extranet (AGI 3.8 p.22) | **aucun** | Reprise des données hors périmètre du prototype (décision du 2026-10-08). |
| Tableaux de bord adaptés aux droits (§4.1 p.13, §5.2 p.61-62) | écrans | Compositions de lecture (`contextes.md` §3), sur les droits lus dans `operateurs`. |

## 3. Modèle

Aucun nom, aucun prénom, aucun droit, aucun opérateur (vision §2.2 « Autres schémas »). Le compte
référence l'utilisateur de `operateurs` par son identifiant `bigint`, **sans clé étrangère** : rien ne
traverse les schémas `authentification` et `operateurs` (vision §2.1, A-AUT-4). Le compte suit
l'utilisateur par les événements de `operateurs` (A-AUT-1).

### 3.1 Compte de connexion

| Champ | Type | Règle | Source |
|---|---|---|---|
| utilisateur | identifiant de l'utilisateur de `operateurs` | Obligatoire, unique, fixe | vision UC-20 |
| identifiant de connexion | texte, e-mail en minuscules | Obligatoire, unique ; copie de l'e-mail de l'utilisateur (RG-AUT-10) | §4.2.6 p.17, §5.1.6 p.60, C.13.1.1 p.135 |
| mot de passe | empreinte (hachage lent) | Jamais en clair (RG-AUT-07) ; vide entre l'acceptation de l'invitation et le premier mot de passe (RG-AUT-15) | C.13.1.1, A-AUT-2 |
| échecs consécutifs | entier | Remis à zéro par une connexion réussie ou une réinitialisation (RG-AUT-03, 08) | C.2.1.1 p.106 |
| bloqué jusqu'à | date-heure | Vide hors blocage (RG-AUT-03) | C.2.1.1 |
| dernière connexion réussie | date-heure | | C.2.1.1, C.13.1.1 |
| mot de passe changé le | date-heure | Invalide les navigateurs de confiance antérieurs (RG-AUT-06) | décision du 2026-10-08 |

**Écarts à C.2.1.1 et C.13.1.1** : prénom, nom, statut, rôle et commentaire d'administration sont dans
`operateurs` (utilisateur, rattachement). « Double authentification requise / activée / mode »
disparaît : code par e-mail pour tous. « Initialisation du mot de passe en attente » et l'état « en
attente d'activation » sont remplacés par l'invitation en attente de `operateurs`, et par un mot de passe
vide pendant les instants qui séparent l'acceptation du premier mot de passe (A-AUT-2).

### 3.2 Code de vérification

| Champ | Type | Règle |
|---|---|---|
| compte | référence au compte | Un seul code en cours par compte (RG-AUT-05) |
| code | empreinte d'un code à 6 chiffres | `000000` en profils `dev` et `demo` (RG-AUT-01) |
| expire le | date-heure | Émission + 10 minutes |

### 3.3 Réinitialisation du mot de passe

| Champ | Type | Règle |
|---|---|---|
| compte | référence au compte | Une seule demande en cours par compte (RG-AUT-08) |
| jeton | empreinte d'un jeton aléatoire | Usage unique |
| expire le | date-heure | Demande + 1 heure |

### 3.4 Navigateur de confiance

| Champ | Type | Règle |
|---|---|---|
| compte | référence au compte | Un par navigateur, plusieurs par compte |
| jeton | empreinte du jeton porté par un cookie | RG-AUT-06 |
| créé le, expire le | date-heure | Création + 30 jours |

### 3.5 Session

Session serveur en base, Spring Session JDBC (A-AUT-3) : l'identifiant de l'utilisateur et l'étape
franchie (mot de passe vérifié, code vérifié). Ni rôle, ni droit, ni opérateur (vision principe 7).
L'opérateur actif est un attribut de l'utilisateur, dans `operateurs` (vision §2.2).

### 3.6 Journal des connexions (hors agrégat, en ajout seul)

| Champ | Type | Règle | Source |
|---|---|---|---|
| date | date-heure | | C.2.3.1 p.107 |
| identifiant saisi | texte | Tel que saisi, même s'il ne correspond à aucun compte | C.2.3.1 |
| utilisateur | identifiant, sans clé étrangère | Vide si l'identifiant est inconnu ; survit à la suppression du compte | C.2.3.1 |
| événement | `CONNEXION_REUSSIE`, `ECHEC_MOT_DE_PASSE`, `ECHEC_CODE`, `BLOCAGE_DECLENCHE`, `CONNEXION_REFUSEE`, `DECONNEXION`, `MOT_DE_PASSE_DEFINI`, `MOT_DE_PASSE_CHANGE`, `REINITIALISATION_DEMANDEE`, `MOT_DE_PASSE_REINITIALISE`, `NAVIGATEUR_DE_CONFIANCE` | RG-AUT-12 | §4.2.4 p.15, C.2.3.1 |
| détail | texte | Motif d'un refus (`BLOQUE_TEMPORAIREMENT`, `UTILISATEUR_BLOQUE`, `AUCUN_ACCES_ACTIF`) ; « navigateur de confiance » pour une connexion sans code | C.2.3.1 |
| adresse IP, navigateur | texte | | décision du 2026-10-08 |

Les actions sur les personnes et les accès (création, rôle, suspension, blocage par le BNIA) vont au
journal de `operateurs` (décision du 2026-10-08 : un journal par contexte).

## 4. Règles de gestion

| Id | Règle | Source |
|---|---|---|
| RG-AUT-01 | **Un seul écran de connexion, à une seule adresse, pour tous les utilisateurs** : personnel BNIA et membres des opérateurs ; pas d'entrée distincte pour le back-office. Après connexion, l'opérateur actif décide de l'espace (spec 02 RG-OPE-49). Se connecter demande l'e-mail (sans distinction de casse) et le mot de passe, puis un code à 6 chiffres envoyé à cet e-mail, **pour tout utilisateur**. En profils `dev` et `demo`, le code vaut toujours `000000`. | §4.2.3, 4.2.6-4.2.7 p.14-17, §5.1.1 et 5.1.3 p.59-60, décision du 2026-10-08 (écart : le CDC décrit deux accès) |
| RG-AUT-02 | Les messages de connexion sont explicites : « aucun compte pour cet e-mail » si l'e-mail est inconnu, « mot de passe incorrect » sinon. | Décision du 2026-10-08 (H-AUT-1 écartée) |
| RG-AUT-03 | Trois échecs consécutifs (mot de passe ou code faux) bloquent le compte 30 minutes : toute tentative est refusée, même juste, avec « compte bloqué jusqu'à HH:MM » et le lien « mot de passe oublié ». Une connexion réussie remet le compteur à zéro. | §4.2.6 p.17, §5.1.1 p.59, H-AUT-2 validée |
| RG-AUT-04 | Une fois le mot de passe vérifié, la connexion n'est acceptée que si `operateurs` répond que l'utilisateur peut se connecter : non bloqué par le BNIA, au moins un rattachement actif dans un opérateur actif. Sinon : « compte bloqué, contactez le BNIA » ou « aucun accès actif », et aucun code n'est envoyé. | §5.1.6 p.60, vision UC-05 et UC-21, spec 02 |
| RG-AUT-05 | Le code est valable 10 minutes, une seule fois. Un nouveau code (renvoi) invalide le précédent. | §4.2.7 p.17, décision du 2026-10-08 |
| RG-AUT-06 | À la saisie du code, la case « faire confiance à ce navigateur pendant 30 jours » dispense ensuite de code sur ce navigateur, pour ce compte. Un changement ou une réinitialisation du mot de passe invalide tous les navigateurs de confiance du compte. | §4.2.7 p.17, décision du 2026-10-08 |
| RG-AUT-07 | Le mot de passe compte 12 caractères au moins, sans autre contrainte. Il est stocké haché, jamais affiché ni journalisé. Un nouvel utilisateur le définit en acceptant son invitation ; il se change dans « Mon compte », mot de passe actuel exigé. | §5.1.2 p.59, recommandation CNIL, décision du 2026-10-08 |
| RG-AUT-08 | « Mot de passe oublié » : un e-mail inconnu est signalé (« aucun compte pour cet e-mail ») ; sinon, le lien envoyé est valable 1 heure, une seule fois ; une nouvelle demande invalide la précédente. Réinitialiser lève le blocage temporaire, remet le compteur à zéro, invalide les navigateurs de confiance et ferme les autres sessions du compte. | §4.2.6 p.17, §5.1.2 p.59, décision du 2026-10-08 |
| RG-AUT-09 | La session porte l'identifiant de l'utilisateur, jamais ses droits. Elle se ferme à la déconnexion ou après 2 heures d'inactivité. À chaque requête, RG-AUT-04 est vérifiée à nouveau : un blocage par le BNIA, une suspension ou un retrait d'accès prennent effet immédiatement. | Vision principe 7, UC-05, UC-15, décision du 2026-10-08 |
| RG-AUT-10 | L'identifiant de connexion est l'e-mail de l'utilisateur. Un changement d'e-mail confirmé dans `operateurs` le reporte dans la même transaction (événement `EmailUtilisateurModifie`). | Vision UC-24, A-AUT-1 |
| RG-AUT-11 | Le compte est créé, sans mot de passe, quand `operateurs` crée un utilisateur (acceptation d'une invitation, événement `UtilisateurCree`), et supprimé avec lui (dernier rattachement retiré, opérateur supprimé : `UtilisateurSupprime`), dans la transaction de `operateurs`. Un compte sans mot de passe ne permet aucune connexion. | Vision UC-20, §2.2 règle 6, A-AUT-1 |
| RG-AUT-12 | Sont journalisés : connexion réussie, échec, blocage, refus, déconnexion, premier mot de passe, changement, demande et réinitialisation du mot de passe, mise en confiance d'un navigateur ; avec la date, l'identifiant saisi, l'utilisateur, l'événement, le détail, l'adresse IP et le navigateur. Jamais de mot de passe ni de code. Le journal est en ajout seul et survit à la suppression du compte. | §4.2.4 p.15, §4.2.6 p.17, C.2.3 p.107, §5.1.6 p.60 |
| RG-AUT-13 | Un rôle de l'opérateur BNIA (module Accès, en lecture au moins) consulte tout le journal, filtré par utilisateur, événement et période. Chaque utilisateur consulte ses propres connexions dans « Mon compte ». | §4.2.4 p.15, §5.1.1 p.59, décision du 2026-10-08 |
| RG-AUT-14 | Durées et seuils (validité du code, du lien de réinitialisation, de la confiance d'un navigateur ; inactivité de la session ; nombre d'échecs et durée du blocage ; longueur du mot de passe) sont des valeurs de configuration, non modifiables à l'écran. | Décision du 2026-10-08 |
| RG-AUT-15 | Premier mot de passe : avec le jeton de l'invitation qu'il vient d'accepter et un mot de passe conforme à RG-AUT-07, le nouvel utilisateur pose son mot de passe, si l'invitation a été acceptée depuis moins d'une heure et que son compte n'en a pas ; la session s'ouvre alors, sans code (l'e-mail vient d'être prouvé par le lien). Sinon, il passe par « mot de passe oublié ». | A-AUT-2, décision du 2026-10-08 |

## 5. Cas d'usage

### 5.1 Écriture

| Cas d'usage | Objets | RG |
|---|---|---|
| Se connecter : vérifier e-mail et mot de passe, interroger `operateurs`, envoyer le code (sauf navigateur de confiance) | compte, code | 01 à 04, 06, 12, 14 |
| Vérifier le code ; faire confiance au navigateur | code, navigateur de confiance | 03, 05, 06, 12, 14 |
| Renvoyer le code | code | 05 |
| Se déconnecter | session | 09, 12 |
| Changer son mot de passe (« Mon compte ») | compte | 06, 07, 12 |
| Demander une réinitialisation ; réinitialiser | réinitialisation, compte | 03, 06, 07, 08, 12 |
| Créer le compte, sans mot de passe (à l'événement `UtilisateurCree`) | compte | 11 |
| Changer l'identifiant de connexion (à l'événement `EmailUtilisateurModifie`) | compte | 10 |
| Supprimer le compte (à l'événement `UtilisateurSupprime`) | compte | 11 |
| Poser le premier mot de passe (après l'acceptation d'une invitation) | compte | 07, 12, 15 |
| *Lecture* : consulter le journal des connexions (back-office) ; ses propres connexions (« Mon compte ») | journal | 12, 13 |

### 5.2 Lecture

| Besoin | Consommateur |
|---|---|
| Journal des connexions filtré (back-office) ; ses propres connexions (« Mon compte ») | écrans (RG-AUT-13) |
| Blocage temporaire en cours d'un utilisateur (jusqu'à quand), dernière connexion | écran « Utilisateurs » du back-office (`operateurs`) : « signaler clairement un compte bloqué » (§4.2.6 p.17) |
| Utilisateur connecté (identifiant) | tous les contextes, par le socle REST |

Ce que `authentification` lit dans la partie publiée de `operateurs` (A-AUT-1) : « cet utilisateur
peut-il se connecter ? » (RG-AUT-04, 09), l'utilisateur d'un e-mail (RG-AUT-01, 08), l'utilisateur d'une
invitation acceptée par son jeton (RG-AUT-15). Ce qu'il écoute : `UtilisateurCree`,
`EmailUtilisateurModifie`, `UtilisateurSupprime`.

## 6. Écrans

Les écrans de ce contexte sont **les mêmes pour tous** (RG-AUT-01) : il n'existe ni connexion du
back-office ni connexion du portail, mais une connexion unique, après laquelle l'opérateur actif décide
de l'espace (spec 02 RG-OPE-49). Le gabarit distingue back-office et portail ; ici, §6.1 regroupe les
écrans communs et §6.2 ce qui est propre au back-office.

### 6.1 Communs à tous les utilisateurs

- **Connexion** (une seule adresse, `/connexion`) : e-mail, mot de passe, lien « mot de passe oublié » ;
  messages de RG-AUT-02 à 04.
- **Code** : saisie du code, « renvoyer le code », case « faire confiance à ce navigateur pendant 30
  jours ».
- **Mot de passe oublié**, puis **nouveau mot de passe** (depuis le lien).
- **Définir son mot de passe**, intégré à l'écran d'acceptation d'une invitation de `operateurs`, pour
  un nouvel utilisateur.
- **Mon compte**, sections de ce contexte : « Mot de passe » (changement) et « Mes connexions ». Les
  sections « Profil », « E-mail » et « Mes opérateurs » sont de `operateurs`.
- **Bandeau** des deux espaces : utilisateur connecté, déconnexion.

### 6.2 Back-office

- **Journal des connexions** (menu Accès, rôles de l'opérateur BNIA) : liste filtrable par utilisateur,
  événement et période ; colonnes date, identifiant saisi, utilisateur, événement, détail, adresse IP.
- **Utilisateurs** (écran de `operateurs`) : affiche aussi le blocage temporaire en cours et la dernière
  connexion, lus ici.

## 7. Données

### 7.1 Démonstration (profil `demo`)

Les utilisateurs et rattachements de démonstration sont décrits dans la spec 02 (§7.1). Chacun reçoit un
compte de mot de passe `demo-bnia-2026` ; le code vaut `000000` (RG-AUT-01). Les comptes sont listés dans
`docs/demo.md`, avec leur opérateur et leur rôle. Le journal des connexions part vide.

### 7.2 Reprise AGI

Hors périmètre du prototype (décision du 2026-10-08).

## 8. Décisions d'architecture

Décidées le 2026-10-08, actées par ADR-015 et les amendements d'ADR-011 et ADR-013.

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-AUT-1 | **`authentification` dépend de `operateurs`, jamais l'inverse.** Il lit la partie publiée de `operateurs` (« l'utilisateur peut-il se connecter ? », utilisateur d'un e-mail, utilisateur d'une invitation acceptée) et **écoute ses événements**, Spring, synchrones, dans la transaction de l'émetteur (ADR-013) : `UtilisateurCree` (identifiant, e-mail), `EmailUtilisateurModifie` (identifiant, nouvel e-mail), `UtilisateurSupprime` (identifiant), types de `operateurs.domain.partage`. Un échec de l'écouteur (identifiant de connexion déjà pris) annule la transaction de `operateurs`. Le mot de passe ne voyage jamais dans un événement. | Port publié par `operateurs` et implémenté par `authentification` (le mot de passe traverserait `operateurs`). Dépendance inverse : le métier dépendrait de l'infrastructure qu'on veut remplaçable. | 015 ; 013 amendé |
| A-AUT-2 | **Premier mot de passe en deux appels, un seul formulaire** : le front envoie l'acceptation à `operateurs` (qui publie `UtilisateurCree` : compte créé sans mot de passe), puis le mot de passe à `authentification` avec le même jeton d'invitation ; `authentification` demande à `operateurs` l'utilisateur de l'invitation acceptée de ce jeton (acceptée depuis moins d'une heure) et ne pose le mot de passe que si le compte n'en a pas (RG-AUT-15). Un échec du second appel se rattrape par « mot de passe oublié ». | Lien « définir votre mot de passe » envoyé par e-mail après l'acceptation : deux e-mails, deux étapes. | 015 |
| A-AUT-3 | **Session serveur en base** : Spring Session JDBC, tables dans le schéma `authentification`, cookie `HttpOnly` et `SameSite=Lax`, protection CSRF de Spring Security. Survit aux redémarrages de devtools ; les sessions d'un utilisateur se retrouvent et se ferment (RG-AUT-08). | Session en mémoire (perdue à chaque redémarrage, registre maison pour fermer les sessions). JWT (révocation et inactivité à construire). | 015 |
| A-AUT-4 | **Identifiants `bigint`** (ADR-011 maintenu, à rebours du principe 6 de la vision : l'e-mail n'est jamais un identifiant, ce qui suffit). Le compte de connexion référence l'utilisateur par son identifiant **sans clé étrangère** : aucune clé ne traverse les schémas `authentification` et `operateurs` (vision §2.1), la cohérence passe par les événements. | UUID partout (refonte des référentiels et de `operateurs`) ; UUID pour les seuls nouveaux objets (deux formes d'identifiant côte à côte). | 011 amendé |

## 9. Hypothèses

| Id | Hypothèse | Pourquoi |
|---|---|---|
| ~~H-AUT-1~~ | Les messages de connexion et de mot de passe oublié ne révèlent pas qu'un e-mail a un compte. | **Écartée le 2026-10-08** : messages explicites (RG-AUT-02, 08), plus clairs pour l'utilisateur ; le compte d'une personne n'est pas une information sensible ici. |
| H-AUT-2 | Un code faux compte comme un échec de connexion pour le blocage (RG-AUT-03). | Proposé avec les durées (décision du 2026-10-08). **Validée le 2026-10-08.** |
| H-AUT-3 | Les e-mails (code, réinitialisation) partent par SMTP ; en dev, vers un Mailpit ajouté à `compose.yaml`. | Le fournisseur d'e-mails n'est pas précisé (bordure de `contextes.md`). **Validée le 2026-10-08** : SMTP (Spring Mail), Mailpit dans `compose.yaml` en dev et démo (http://localhost:8025). |

## 10. Questions ouvertes

Revue du 2026-10-08 : aucune question ouverte. Hypothèses H-AUT-2 et 3 validées, H-AUT-1 écartée.

## 11. Tests attendus

Chaque RG-AUT-nn a au moins un test qui porte son identifiant (CLAUDE.md).
- **Tests de domaine**, sans base : RG-AUT-03 (compteur et échéance du blocage), 05, 06, 07 (longueur),
  08 (effets d'une réinitialisation).
- **Tests de cas d'usage**, avec repositories simulés, puis IT (Spring Security, session) : RG-AUT-01,
  02, 03, 04 (refus selon `operateurs`), 08, 09 (effet immédiat d'un blocage ou d'une suspension), 10, 11
  (événements, annulation de la transaction de `operateurs`), 12, 13, 14, 15.

## Journal

| Date | Changement |
|---|---|
| 2026-10-08 | Phases A et B : cadrage à partir de la vision du 2026-10-08, puis périmètre. Décisions : contexte `acces` remplacé par `authentification`, sans métier, le métier des accès allant dans `operateurs` (spec 02 révisée) ; socle mot de passe, mot de passe oublié, blocage, journal des connexions ; double authentification par e-mail pour tous (code `000000` en dev et démo) ; Spring Security seul ; BNIA opérateur de type `BNIA`, son rôle valant pour tous les opérateurs ; fiche de l'opérateur gérée avec les contacts ; un journal par contexte ; types `BNIA`, `PRODUCTEUR`, `NEGOCIANT`, `AUTRE` ; l'opérateur actif décide de l'espace (back-office ou portail) ; reprise hors périmètre. |
| 2026-10-08 | Phase C : modèle (compte de connexion, code, réinitialisation, navigateur de confiance, session, journal des connexions), RG-AUT-01 à 14, cas d'usage, écrans, démonstration. Décisions : navigateur de confiance 30 jours ; durées et seuils fixes en configuration (code 10 min, lien 1 h, session 2 h, mot de passe 12 caractères) ; le lien « mot de passe oublié » lève le blocage, pas de déblocage par le BNIA ; journal des connexions consulté par l'opérateur BNIA, et par chacun pour les siennes ; démonstration de six comptes (`demo-bnia-2026`, code `000000`). Hypothèses H-AUT-1 à 3. Le métier des personnes et des accès est rédigé dans la spec 02 révisée. |
| 2026-10-08 | Phase E : décisions A-AUT-1 à 4 (§8) : `authentification` dépend de `operateurs` et écoute ses événements synchrones (`UtilisateurCree`, `EmailUtilisateurModifie`, `UtilisateurSupprime`) ; premier mot de passe en deux appels d'un même formulaire (RG-AUT-15 ajoutée, RG-AUT-10 et 11 révisées, compte sans mot de passe entre les deux) ; session serveur en base (Spring Session JDBC) ; identifiants `bigint`, ADR-011 maintenu. |
| 2026-10-08 | Phase F : H-AUT-1 écartée (messages explicites : RG-AUT-02 et 08 révisées) ; H-AUT-2 (code faux = échec) et H-AUT-3 (SMTP, Mailpit) validées. Aucune question ouverte. |
| 2026-10-08 | Phase G, relecture : premier mot de passe journalisé (`MOT_DE_PASSE_DEFINI`, RG-AUT-12) ; lecture du journal citée par un cas d'usage (RG-AUT-13) ; RG-AUT-14 citée. Glossaire : compte de connexion, double authentification, navigateur de confiance, blocage temporaire ajoutés ; compte portail remplacé. Pages vérifiées par recherche dans le CDC converti. |
| 2026-10-08 | Connexion unique rendue explicite : RG-AUT-01 (un seul écran, une seule adresse, pour le personnel BNIA comme pour les opérateurs ; écart au CDC qui décrit deux accès) ; §6 réorganisé (écrans communs, puis back-office). |
| 2026-10-08 | Spec validée, statut « terminée ». Prochaine étape : ADR, puis implémentation avec la révision de la spec 02. |
| 2026-10-08 | Phase H : ADR-015 (authentification, contexte d'infrastructure) rédigé ; ADR-011 (auteur d'une action, compte de connexion sans clé étrangère, `bigint` confirmé) et ADR-013 (événements) amendés ; `CLAUDE.md` mis à jour. |
