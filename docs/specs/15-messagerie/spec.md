# 15 : messagerie

> Contexte `messagerie` (étape 15 de `docs/contextes.md`, ajouté pour la démonstration au BNIA, absent du CDC).
> Statut : **implémentée** (2026-10-17 : lots 1 à 6 ; spec terminée le même jour).
> Références : `p.N` = page du CDC v1.1, `AGI p.N` = page de la spec de reprise AGI.
> `H-MSG-n` : hypothèse retenue, révisable. `Q-MSG-n` : question ouverte, suivie dans cette spec (§10).
> Précédée de l'analyse du contexte ([analyse.md](analyse.md), terminée le 2026-10-17, citée « analyse »),
> dont les conclusions (cadrage, cas d'usage `UC-MSG-n`, objets, flux, amendements au CDC, révisions de
> l'existant) sont acquises.

## Sommaire

- [1. Objet](#1-objet)
- [2. Périmètre](#2-périmètre)
  - [2.1 Dans le contexte](#21-dans-le-contexte)
  - [2.2 Hors du contexte](#22-hors-du-contexte)
- [3. Modèle](#3-modèle)
  - [3.1 Conversation (agrégat `Conversation`, lot 2)](#31-conversation-agrégat-conversation-lot-2)
  - [3.2 Objet métier (`ObjetMetier`, valeur, et `TypeObjet`, liste fermée, lots 2 et 6)](#32-objet-métier-objetmetier-valeur-et-typeobjet-liste-fermée-lots-2-et-6)
  - [3.3 Sujet libre (`SujetLibre`, valeur, lot 5)](#33-sujet-libre-sujetlibre-valeur-lot-5)
  - [3.4 Message (agrégat `Message`, lots 2 à 5)](#34-message-agrégat-message-lots-2-à-5)
  - [3.5 Partie publiée : `bnia-messagerie-api` (lot 2)](#35-partie-publiée--bnia-messagerie-api-lot-2)
  - [3.6 Droits : le module « Messagerie » (révision de la spec 02 §3.2.7, lot 2)](#36-droits--le-module--messagerie--révision-de-la-spec-02-327-lot-2)
  - [3.7 Révisions de l'existant livrées par cette spec](#37-révisions-de-lexistant-livrées-par-cette-spec)
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
- [12. Lots d'implémentation](#12-lots-dimplémentation)
- [Journal](#journal)

## 1. Objet

Le CDC n'a pas de messagerie : des champs « commentaire » épars sur la DSM (C.3 p.107), le contrat
(C.4 p.113) ou la demande de certificat (C.7 p.122), et un accès au BNIA « si besoin d'assistance »
(§5.2.2 p.61). `messagerie` porte **tous les échanges écrits entre le BNIA et un opérateur** (analyse §1,
amendement au CDC) :

- la **conversation d'un objet métier**, partagée par tous les utilisateurs de l'opérateur, écrite depuis
  la page de l'objet ou depuis la messagerie (analyse UC-MSG-2, 5) ;
- la **conversation libre**, ouverte par un utilisateur du portail, que lui seul voit dans son opérateur,
  avec pièces jointes (analyse UC-MSG-1, 3) ;
- l'état **lu / non lu** de chaque message, par côté (l'opérateur, le BNIA), le compteur « x nouveaux
  messages » des deux tableaux de bord et la liste qu'il ouvre (analyse UC-MSG-6 à 8) ;
- l'**écran unifié** des conversations, avec recherche dans le texte des messages (analyse UC-MSG-9, 10).

Il n'y a pas de destinataire nommé : un message s'adresse à l'autre côté. `messagerie` est une **lib en
aval** (analyse §4, ADR-021) : elle ne connaît aucun contexte métier, qui l'appelle et lui décrit ses
objets. Elle ne porte ni les notifications du système, ni les notes internes du BNIA, ni l'objet métier
lui-même.

Sa valeur : un seul fil d'échange par objet, au lieu de champs de texte épars, et un seul endroit où le
BNIA comme l'opérateur voient ce qui attend une réponse.

Sources : aucune section dédiée (contexte ajouté pour la démonstration) · analyse du 2026-10-17 · besoin
exprimé par l'utilisateur le 2026-10-17.
Usages relevés ailleurs : §4.1 (tableau de bord du BNIA, droits de l'utilisateur, p.12-13) · §4.4.5
(note de contrôle d'une ligne, p.26) · §4.5 (commentaire du contrat, p.31-32) · §5.2 (tableau de bord du
portail, contact du BNIA, p.61-62) · §5.12 (notifications, lu ou non lu, p.77) · C.3.1.1 (observation
générale, p.107) · C.3.2.1 (note de contrôle, p.108) · C.3.2.5 (note de transfert, p.110) · C.3.3.1
(commentaire de contrôle d'un justificatif, p.111) · C.3 et C.4 (commentaire de suivi d'une alerte,
p.112) · C.4.1.1 (commentaire du contrat, p.113) · C.4.2 (observations BNIA, p.116-117) · C.5.1 (commentaire
d'une fin de travaux, p.119) · C.6 (commentaire, commentaire de dossier, p.119-120) · C.7.1.1 (commentaire
de traitement, p.122) · C.14.1.1 (notification : objet concerné, lien d'action, statut de lecture, p.136).

## 2. Périmètre

### 2.1 Dans le contexte

| Famille | Élément | Code (ADR-001) | Source |
|---|---|---|---|
| Conversation | Conversation d'un objet métier : au plus une par objet, née au premier message, supprimée avec l'objet | `Conversation`, `ObjetMetier` | analyse §3, UC-MSG-2 |
| Conversation | Conversation libre ouverte par un utilisateur du portail : titre, auteur, vue de lui seul dans son opérateur | `Conversation`, `SujetLibre` | analyse §2-3, UC-MSG-1, 3 |
| Message | Texte, auteur, côté, date ; protégé (motif des compléments) ; pièces jointes en conversation libre | `Message`, `DocumentDepose` | analyse §3, UC-MSG-3, 5 |
| Lecture | Lu / non lu par message et par côté ; « marquer non lu » | — | analyse §2, UC-MSG-7, 8 |
| Lecture | Nouveaux messages de mon côté : compteur des tableaux de bord ; dans la liste des conversations, celles qui en ont ressortent en gras (titre, première ligne, objet ou sujet) | — | analyse UC-MSG-6 ; §4.1 p.12, §5.2.2 p.61 |
| Lecture | Écran unifié : mes conversations (BNIA : par opérateur, type d'objet, sujet libre), recherche dans le texte | — | analyse UC-MSG-9, 10 |
| Types d'objet | Liste fermée des objets conversables : **DSM** ; **facture émise** (décision du 2026-10-17) ; contrat d'achat et demande de certificat avec leur contexte | `TypeObjet` | analyse UC-MSG-2 ; décision du 2026-10-17 |
| Droits | Module « Messagerie » (conversations libres), géré par les trois rôles ; droits d'une conversation d'objet tirés du module de l'objet | `Module.MESSAGERIE` | analyse §2 et §4 |
| Partie publiée | Module `bnia-messagerie-api` : `Conversations` (appelée), `ObjetsConversables` (implémentée par chaque contexte métier) | — | analyse §4, ADR-021 |

**Facture émise** (décision du 2026-10-17, ajout à l'analyse). Une facture ou un avoir **émis** est
conversable ; un brouillon, jamais vu du portail, ne l'est pas. L'opérateur de la conversation est le
client de la pièce. Exception à la règle de droits de l'analyse (« lire l'objet pour lire la conversation,
le gérer pour y écrire ») : le module « Facturation » n'est géré que par le BNIA (RG-OPE-31) ; sur une
facture, sa **lecture suffit pour écrire**, sans quoi le client ne pourrait poser aucune question sur sa
facture. `facturation` implémente `ObjetsConversables` dans son infra (révision de la spec 08). Détails
(libellé, lien, avoir) en phase C.

### 2.2 Hors du contexte

| Élément décrit dans le CDC | Contexte retenu | Motif |
|---|---|---|
| L'objet métier (DSM, facture, contrat, demande de certificat) : existence, opérateur, libellé, droits | son contexte, par `ObjetsConversables` | Lib en aval : `messagerie` n'en connaît que type, identifiant et libellé (analyse §3-4) |
| Note de contrôle d'une ligne (§4.4.5 p.26, C.3.2.1 p.108), commentaire de contrôle d'un justificatif (C.3.3.1 p.111), commentaire de suivi d'une alerte (C.3 p.112) | `declarations` : carnet de notes internes de la DSM et de ses lignes, distinct à l'écran ; révision de la spec 05 planifiée dans nos lots | Note interne du BNIA, pas un échange (analyse §3) |
| Observations BNIA (C.4.2 p.117), commentaire de suivi d'une alerte (C.4 p.112) | `contratsarmagnac` : notes internes | Même principe (analyse §3) |
| Commentaire du contrat d'achat (C.4.1.1 p.113, §4.5 p.31-32) | `contratsarmagnac` (spec 06) : champ du contrat ou conversation | Listé parmi les champs saisis du contrat : peut-être un terme convenu entre les parties, à trancher par la spec 06 |
| Commentaire de traitement d'une demande de certificat (C.7.1.1 p.122) | `exportation` : note interne | « Observation interne » (analyse §3) |
| Commentaire et commentaire de dossier d'une sortie export (C.6 p.119-120) | `exportation` (spec 07) : conversable ou note interne | Objet absent de l'analyse, à décider avec son contexte |
| Commentaire d'une fin de travaux (C.5.1 p.119) | `distillation` | Donnée reçue de l'ODG, pas un échange |
| Commentaire d'une entrée d'historique (C.4 p.113, C.6, C.7 p.121) | contexte du journal | Annotation d'une action, pas un message |
| Note de transfert en région (C.3.2.5 p.110) | supprimée | Absente des deux matrices Excel (analyse, amendement au CDC) |
| Notifications et relances, leur lu / non lu, règles de destinataires (§5.12 p.77, C.14 p.136) | `notifications` | Émises par le système vers un destinataire ; le message est écrit par une personne, sans destinataire (analyse §1) |
| E-mail d'alerte d'un nouveau message | hors analyse (`notifications` le moment venu) | Analyse §2, hors analyse |
| Conversation libre ouverte par le BNIA ; clore une conversation ; pièces jointes sur un objet | hors prototype | Analyse §2, hors analyse |
| Tableaux de bord (§4.1 p.12, §5.2 p.61) | composition de l'écran ; `messagerie` fournit le compteur et la liste | Éléments transverses (`contextes.md` §3) |
| Reprise des commentaires d'InteGraal (AGI 2.1, 2.5, 3) | hors périmètre du prototype | Reprise hors périmètre |

## 3. Modèle

Deux agrégats (analyse §3) : `Conversation` et `Message`, racine à part pour qu'écrire ou lire ne verrouille
pas la conversation. Pas de journal : `messagerie` n'en tient pas (décision du 2026-10-17, RG-MSG-06).

### 3.1 Conversation (agrégat `Conversation`, lot 2)

| Champ | Type | Règle | Source |
|---|---|---|---|
| identifiant | `bigint` | Technique | ADR-011 |
| opérateur | référence `Operateur`, avec clé étrangère (A-MSG-5) | Obligatoire ; pour un objet, celui que rend `ObjetsConversables` ; pour un sujet libre, l'opérateur actif de l'auteur à l'ouverture (RG-MSG-04, 12) | analyse §3 |
| sujet | `ObjetMetier` ou `SujetLibre` (§3.2, §3.3) | Exactement un | analyse §3 |
| ouverte le | date-heure | Au premier message (RG-MSG-04) | analyse §3 |

Pas de date du dernier message ni de compteur : le modèle de lecture les calcule (§5.2).

### 3.2 Objet métier (`ObjetMetier`, valeur, et `TypeObjet`, liste fermée, lots 2 et 6)

| Champ | Type | Règle | Source |
|---|---|---|---|
| type | `TypeObjet` | Liste fermée ci-dessous | analyse §3 |
| identifiant | texte | Identifiant de l'objet chez son contexte, opaque, sans clé étrangère ; unique avec le type (RG-MSG-04) | analyse §3, comme `Origine` (ADR-011 amendé) |
| libellé | texte | Rendu par `ObjetsConversables` à l'ouverture, copié (« DSM octobre 2026 », « Facture 2026000003 ») | analyse §3 |

| `TypeObjet` | Libellé | Contexte | Module des droits | Écrire exige | Conversable | Lot |
|---|---|---|---|---|---|---|
| `DSM` | DSM | `declarations` | Déclarations | la gestion | Toute DSM, dans tout état | 2 |
| `FACTURE` | Facture | `facturation` | Facturation | la **lecture** (exception, §2.1) | Pièce émise (facture ou avoir) dont le client est un opérateur | 6 |

Le contrat d'achat et la demande de certificat s'ajouteront avec `contratsarmagnac` et `exportation`. Le lien
vers l'objet n'est pas stocké : l'écran le construit du type et de l'identifiant, selon l'espace
(back-office ou portail).

### 3.3 Sujet libre (`SujetLibre`, valeur, lot 5)

| Champ | Type | Règle | Source |
|---|---|---|---|
| titre | texte | Non vide, 120 caractères au plus (RG-MSG-12) | analyse §3 |
| auteur | identifiant de l'utilisateur, sans clé étrangère, et copie de son nom | Utilisateur du portail qui l'ouvre ; seul à la voir côté opérateur (RG-MSG-02) | analyse §2, ADR-011 (auteur d'une action) |

### 3.4 Message (agrégat `Message`, lots 2 à 5)

| Champ | Type | Règle | Source |
|---|---|---|---|
| identifiant | `bigint` | Technique | ADR-011 |
| conversation | référence `Conversation` | Obligatoire | analyse §3 |
| auteur | identifiant de l'utilisateur, sans clé étrangère, et copie de son nom | Celui qui écrit, ou au nom duquel le motif est déposé (RG-MSG-08) | ADR-011 (auteur d'une action) |
| côté | `OPERATEUR`, `BNIA` | Déduit de l'acteur (RG-MSG-03) | analyse §1 |
| écrit le, modifié le | date-heure | « modifié » affiché si renseigné (RG-MSG-06) | spec 05 RG-DEC-25, décision du 2026-10-17 |
| texte | texte | Non vide, 4 000 caractères au plus (RG-MSG-05) | analyse §3 |
| protégé | booléen | Vrai pour le motif d'une demande de compléments ; ne se supprime pas (RG-MSG-06, 08) | analyse UC-MSG-5 |
| lu par l'opérateur, lu par le BNIA | booléens | Vrai pour le côté de l'auteur à l'écriture, faux pour l'autre (RG-MSG-09, 10) | analyse §2 |
| pièces jointes | liste de `DocumentDepose` (`commun.domain`) | Conversation libre seulement ; 5 au plus ; PDF, PNG, JPEG, `.xlsx` (RG-MSG-14) | analyse §3, décision du 2026-10-17 |

Écarts avec le `Commentaire` actuel de `declarations` (spec 05 §3.2.4) : plus de **ligne** (le message porte
sur l'objet entier), plus d'**interne** (note interne, §3.7), `motif` devient `protégé` ; s'ajoutent les deux
états de lecture et les pièces jointes (analyse §3).

### 3.5 Partie publiée : `bnia-messagerie-api` (lot 2)

Module d'API au sens d'ADR-021, Java pur ; seule l'infra d'un contexte métier en dépend.

| Élément | Nature | Contenu |
|---|---|---|
| `Conversations` | interface appelée par les contextes métier, implémentée par le core de `messagerie` | `deposerMotif(objet, auteur, texte)` : message protégé côté BNIA (RG-MSG-08) ; `supprimer(objet)` : la conversation et ses messages (RG-MSG-07) ; `nonLus(objet, côté)` : nombre, pour une pastille sur la page de l'objet |
| `ObjetsConversables` | interface implémentée par l'infra de chaque contexte métier | `type()` ; `decrire(identifiant)` : vide si l'objet n'existe pas ou n'est pas conversable, sinon opérateur et libellé (RG-MSG-04, 17) |
| `TypeObjet`, `ObjetRef` (type, identifiant), `DescriptionObjet` (opérateur, libellé), `Auteur` | types échangés | — |

Le core de `messagerie` reçoit la liste des `ObjetsConversables`, indexée par `type()` ; deux implémentations
du même type font échouer le démarrage (comme `CatalogueArticles`, ADR-021).

### 3.6 Droits : le module « Messagerie » (révision de la spec 02 §3.2.7, lot 2)

| Rôle | Messagerie (conversations libres ; spec 15 §3.6) |
|---|---|
| `OBSERVATEUR` | lecture (gestion jusqu'au 2026-10-21) |
| `AGENT` | gestion |
| `ADMIN` | gestion |

Le module ne couvre que les conversations libres. Une conversation d'objet suit les droits du module de
l'objet (§3.2, RG-MSG-01). Exception à RG-OPE-31 (« tout rattachement `ACTIF` lit tout son opérateur ») : une
conversation libre n'est lue, côté opérateur, que par son auteur (RG-MSG-02). `OBSERVATEUR` la lit sans la
gérer, comme tous les autres modules (révision du 2026-10-21, qui retire l'exception de l'analyse §2) : il
n'ouvre pas de conversation libre et n'y écrit pas, au portail comme au BNIA.

### 3.7 Révisions de l'existant livrées par cette spec

| Concerné | Révision | Lot |
|---|---|---|
| Spec 05, code `declarations` : carnet de notes internes | `NoteInterne` (entité, à part de la DSM comme le commentaire) : DSM, ligne facultative (libellé gardé si la ligne est supprimée), auteur (identifiant, copie du nom), écrite le, modifiée le, texte non vide. Écrite par qui gère les Déclarations dans l'opérateur BNIA, lue par tout le personnel du BNIA, jamais par l'opérateur ; l'auteur la modifie ou la supprime ; supprimée avec la DSM (RG-DEC-22). RG-DEC-24 réécrite. Remplace la note de contrôle d'une ligne (§4.4.5 p.26, C.3.2.1 p.108), le commentaire de contrôle d'un justificatif (C.3.3.1 p.111, note sur la DSM) et le commentaire interne (décision du 2026-10-17) | 1 |
| Spec 05, code `declarations` : conversation de la DSM | `Commentaire`, `CommenterDsm`, table `declarations.commentaire` retirés ; la section *Commentaires* de la DSM devient *Messages* (§6). Port `Messagerie` du core (déposer le motif, supprimer la conversation), implémenté dans l'infra sur `Conversations` ; `ObjetsConversables` du type `DSM` implémenté dans l'infra. `GererDsm` dépose le motif des compléments par ce port (RG-DEC-20) et supprime la conversation avec le brouillon (RG-DEC-22). RG-DEC-25 réécrite ; RG-DEC-23 sans « commentaire supprimé » ; H-DEC-12 et 19 révisées (observation générale : message ; note de transfert : supprimée) | 2 |
| Spec 08, code `facturation` | `ObjetsConversables` du type `FACTURE` dans l'infra ; section *Messages* sur la pièce, au back-office et au portail | 6 |
| Spec 02, code `operateurs` | `Module.MESSAGERIE` (§3.6) ; exception à RG-OPE-31 ; libellé du module Déclarations (« commentaires » → « notes internes ») | 2 |
| `bnia-commun-infra` (ADR-018) | Le stockage accepte, sur demande de l'appelant, PDF, PNG, JPEG et `.xlsx`, reconnus par leur signature ; les justificatifs et DRM restent en PDF seul | 5 |
| ADR-011, ADR-018, ADR-021, `CLAUDE.md` | ADR-023 (lib en aval `messagerie`) ; `ObjetMetier` opaque sans clé étrangère (ADR-011) ; formats de documents (ADR-018) ; second module d'API (ADR-021) | phase H (écrits le 2026-10-17) |

## 4. Règles de gestion

| Id | Règle | Source | Lot |
|---|---|---|---|
| RG-MSG-01 | Une conversation d'objet se lit par qui lit le module de son type (§3.2) sur l'opérateur de l'objet ; on y écrit avec la gestion de ce module, sauf sur une facture, où la lecture suffit. Un rôle tenu dans l'opérateur BNIA vaut pour tous les opérateurs (RG-OPE-32). | analyse §2 ; décision du 2026-10-17 (facture) | 2 |
| RG-MSG-02 | Une conversation libre se lit, côté opérateur, par son seul auteur, tant qu'il a un rattachement `ACTIF` à l'opérateur de la conversation (aucun autre membre, `ADMIN` compris) ; côté BNIA, par tout son personnel. On y écrit avec la gestion du module Messagerie (`AGENT`, `ADMIN`) ; un `OBSERVATEUR` la lit seulement (révision du 2026-10-21). Exception à RG-OPE-31. | analyse §2, H-MSG-2 ; décision du 2026-10-21 | 5 |
| RG-MSG-03 | Le côté d'un acteur est `BNIA` si son opérateur actif est le BNIA, `OPERATEUR` sinon ; un message porte le côté de son auteur. | analyse §1 | 2 |
| RG-MSG-04 | Un objet a au plus une conversation. Elle naît à son premier message, écrit ou déposé : son opérateur et son libellé sont ceux que rend `ObjetsConversables` ; un objet inconnu ou non conversable est introuvable. | analyse §3 | 2 |
| RG-MSG-05 | Le texte d'un message est non vide (espaces exclus) et compte 4 000 caractères au plus. Un message porte sur l'objet entier, jamais sur une de ses parties. | analyse §3 ; convention du 2026-10-17 (longueur) | 2 |
| RG-MSG-06 | L'auteur modifie son message (« modifié » affiché) ou le supprime, sans trace. Un message protégé se modifie mais ne se supprime pas. Une modification ne change pas son état de lecture. Une conversation dont le dernier message est supprimé est supprimée. | spec 05 RG-DEC-25 ; décision du 2026-10-17 | 2 |
| RG-MSG-07 | La conversation d'un objet est supprimée avec lui, avec ses messages, dans la transaction du contexte de l'objet (DSM en brouillon, RG-DEC-22). | analyse §3 | 2 |
| RG-MSG-08 | Le motif d'une demande de compléments est déposé par `declarations` dans la conversation de la DSM, au nom de l'agent du BNIA, côté BNIA, protégé. | analyse UC-MSG-5, RG-DEC-20 | 2 |
| RG-MSG-09 | Un message est lu ou non lu par côté, sans dire qui l'a lu : lu par un utilisateur, il l'est pour tout son côté. Il est lu pour le côté de son auteur dès qu'il est écrit, non lu pour l'autre. | analyse §2 | 3 |
| RG-MSG-10 | Afficher une conversation (liste des nouveaux messages, écran unifié, page de l'objet) marque lus tous ses messages pour le côté de l'acteur. « Marquer non lu » remet un message non lu pour ce côté jusqu'au prochain affichage de sa conversation. Lire n'exige que le droit de lire la conversation. | décision du 2026-10-17 | 3 |
| RG-MSG-11 | Les nouveaux messages d'un acteur sont les messages non lus de son côté dans les conversations qu'il peut lire (RG-MSG-01, 02) : au portail, celles de son opérateur actif ; au back-office, celles de tous les opérateurs. Le tableau de bord en montre le nombre (au portail, avec les deux plus récents : libellé de l'objet ou titre du sujet libre, début du texte) et ouvre la messagerie ; dans « Mes conversations », celles qui en ont ressortent en gras, avec leur nombre (RG-MSG-15). | analyse UC-MSG-6 ; §4.1 p.13 (aucune donnée non autorisée) | 3 |
| RG-MSG-12 | Seul un utilisateur du portail qui gère le module Messagerie (`AGENT`, `ADMIN` ; révision du 2026-10-21) ouvre une conversation libre, avec un titre (non vide, 120 caractères au plus) et un premier message ; son opérateur est l'opérateur actif de l'auteur. | analyse UC-MSG-1 | 5 |
| RG-MSG-13 | Une conversation libre dont l'auteur n'a plus de rattachement `ACTIF` à son opérateur (retiré, suspendu, supprimé) passe en lecture seule pour le BNIA, marquée « auteur parti » ; elle redevient active si l'auteur est réactivé. | décision du 2026-10-17 | 5 |
| RG-MSG-14 | Un message d'une conversation libre porte au plus 5 pièces jointes, de 10 Mo au plus chacune, PDF, PNG, JPEG ou `.xlsx`, reconnues par leur signature. Elles se lisent avec la conversation et sont supprimées avec leur message. Une conversation d'objet n'a pas de pièces jointes. | analyse §3 ; décision du 2026-10-17 | 5 |
| RG-MSG-15 | « Mes conversations » liste les conversations que l'acteur peut lire, de la plus récemment écrite à la plus ancienne, avec leur nombre de messages non lus de son côté, comme une boîte de réception : une conversation qui en a ressort en gras ; au back-office, filtrées par opérateur, type d'objet ou sujet libre ; « non lues seulement » aux deux espaces. Ouverte, une conversation se montre sur son premier message non lu, sinon sur le dernier. | analyse UC-MSG-9 | 4 |
| RG-MSG-16 | La recherche trouve, sans tenir compte de la casse ni des accents, les conversations lisibles dont un message, le titre ou le libellé contient le texte cherché ; elle se combine aux filtres et montre, par conversation, l'extrait du message le plus récent qui correspond. | analyse UC-MSG-10 ; décision du 2026-10-17 | 4 |
| RG-MSG-17 | Une facture ou un avoir est conversable une fois émis, si son client est un opérateur ; l'opérateur de la conversation est ce client ; son libellé est le sens et le numéro (« Facture 2026000003 »). | décision du 2026-10-17 | 6 |

## 5. Cas d'usage

### 5.1 Écriture

Chaque cas d'usage obtient d'abord les droits de l'acteur (RG-MSG-01, 02) et a son test de refus. Les numéros
`UC-MSG-n` sont ceux de l'analyse ; 11 et 12 sont ajoutés.

| Cas d'usage | Objets | RG | Lot |
|---|---|---|---|
| Ouvrir une conversation libre, avec pièces jointes (UC-MSG-1, portail) | `Conversation`, `Message` | 02, 03, 05, 12, 14 | 5 |
| Écrire dans la conversation d'un objet, depuis sa page ou la messagerie (UC-MSG-2) | `Conversation`, `Message` | 01, 03, 04, 05, 09, 17 | 2, 6 |
| Répondre dans une conversation libre, avec pièces jointes (UC-MSG-3) | `Message` | 02, 03, 05, 13, 14 | 5 |
| Déposer le motif d'une demande de compléments (UC-MSG-5, `declarations`) | `Conversation`, `Message` | 04, 08, 09 | 2 |
| Marquer lus en affichant ; marquer un message non lu (UC-MSG-7, 8) | `Message` | 10 | 3 |
| Modifier ou supprimer son message (UC-MSG-11) | `Message`, `Conversation` | 06 | 2 |
| Supprimer la conversation avec son objet (UC-MSG-12, `declarations`) | `Conversation`, `Message` | 07 | 2 |

### 5.2 Lecture

| Besoin | Consommateur | Lot |
|---|---|---|
| Fil d'une conversation (messages, auteurs, côtés, « modifié », pièces jointes, protégé, lien vers l'objet) | écran unifié, pages de la DSM et de la facture | 2 |
| Nouveaux messages : nombre, et les plus récents pour la carte du portail (RG-MSG-11, UC-MSG-6) | tableaux de bord | 3 |
| Mes conversations, filtres et recherche (RG-MSG-15, 16, UC-MSG-9, 10) | écran unifié | 4 |
| Non lus d'un objet pour un côté (`Conversations.nonLus`) | pastille sur la page de l'objet | 3 |

Lectures en `JdbcClient` (ADR-004) : le nombre de non lus et la date du dernier message se calculent sur
`message`, avec un index par conversation et par état de lecture.

## 6. Écrans

Un seul écran par espace, « Messagerie », dans le menu ; une carte au tableau de bord ; une section *Messages*
sur la page de chaque objet. Mêmes composants au back-office et au portail.

### 6.1 Back-office

- **Tableau de bord** : carte « x nouveaux messages » (icône de bulle), qui ouvre la messagerie, sans
  filtre : les conversations non lues y ressortent en gras. « Aucun nouveau message » à zéro (RG-MSG-11, lot 3).
- **Messagerie**, menu du back-office, à deux panneaux :
  - à gauche, **une seule liste**, comme une boîte de réception (lots 3 et 4) : champ de recherche, filtres
    (opérateur par la recherche de `operateurs`, type : DSM, Facture, Sujet libre ; « non lues seulement »),
    une ligne par conversation (libellé ou titre, date du dernier message, type, opérateur, auteur d'un sujet
    libre, extrait) ; une conversation qui a des non lus ressort en gras, avec un point et son nombre de non
    lus (RG-MSG-11, 15) ; un clic l'ouvre à droite, sur son premier message non lu (RG-MSG-10) ;
  - à droite, **vue d'une conversation**, qui défile seule, la zone de réponse en bas : en-tête (libellé ou
    titre, opérateur, lien *Ouvrir la DSM* ou *Ouvrir la facture* ; pour un sujet libre, son auteur), fil en
    bulles du plus ancien au plus récent (mon côté à droite), séparé par jour, messages d'un même auteur
    regroupés, séparateur « Nouveaux messages » avant le premier non lu, côté marqué (« BNIA », ou le nom de
    l'opérateur), « modifié », mention « motif des compléments » sur un protégé ; menu d'un message, au
    survol : *Marquer non lu*, *Modifier* et *Supprimer* pour son auteur ; zone de réponse (pièces jointes en
    sujet libre) ; « auteur parti » et zone de réponse retirée (RG-MSG-13) ; sans droit d'écrire, zone de
    réponse retirée avec son motif.
- **Page d'une DSM** (spec 05 §6.3) : la section *Commentaires* devient *Messages* (lot 2), même vue de
  conversation, sans rattachement à une ligne ; à côté, section **Notes internes** (lot 1), fond distinct,
  icône de cadenas, mention « visible du BNIA seulement », notes de la DSM et de ses lignes, au gabarit des
  messages (avatar, menu au survol), la ligne visée en pastille ; pastille sur la ligne porteuse de notes.
- **Page d'une facture** (spec 08 §6.1) : section *Messages* sous les lignes (lot 6).

### 6.2 Portail

- **Tableau de bord** : carte « Nouveaux messages » de la section *Activités*, comme au back-office (lot 3 ;
  révision du 2026-10-21).
- **Messagerie**, menu du portail : même liste unique et même vue, sans filtre d'opérateur ;
  bouton **Nouvelle conversation** (lot 5), pour qui gère le module Messagerie : titre, message, pièces jointes ; mention « Seuls vous et le
  BNIA verrez cette conversation ».
- **Page d'une DSM** : section *Messages*, sans notes internes ni actions du BNIA (lot 2).
- **Page d'une facture** (spec 08 §6.2) : section *Messages* (lot 6).

Un `OBSERVATEUR` lit les conversations d'objet sans y écrire (sauf sur une facture) ; il n'ouvre pas de
conversation libre (§3.6, révision du 2026-10-21).

## 7. Données

### 7.1 Démonstration (profil `demo`)

Par les cas d'usage, relançable (`DemoMessagerie`, après `DemoDeclarations` et `DemoFacturation`). Comptes :
`docs/demo.md`.

| Opérateur | Données | Démontre | Lot |
|---|---|---|---|
| BNIA | Note interne d'Anne Lamarque sur la DSM d'octobre de Gascogne (reprise de l'actuel commentaire interne) et une note sur sa première ligne | Carnet distinct, invisible du portail | 1 |
| Armagnacs de Gascogne (`N320001`) | Conversation de la DSM d'octobre, en compléments demandés (décision du 2026-10-21) : motif « DAE manquants pour l'export », protégé, déposé par `declarations` ; puis message de Julie Darrigade, réponse de Luc Dubos, non lue pour Gascogne | Motif, conversation d'objet, page de la DSM, nouveaux messages des deux côtés | 2, 3 |
| Armagnacs de Gascogne | Conversation libre « Import de la matrice refusé » de Julie Darrigade, avec l'Excel de sa DSM en pièce jointe, réponse du BNIA, invisible d'Henri Castex (`ADMIN`) ; conversation libre d'Henri Castex, invisible de Julie (l'observateur Thomas Barrère n'en ouvre plus depuis le 2026-10-21) | Conversation libre, pièces jointes, confidentialité | 5 |
| Armagnacs de Gascogne | Conversation d'une facture émise : question d'Henri Castex sur le volume facturé, non lue pour le BNIA | Facture conversable, écriture par le client | 6 |

L'« auteur parti » (RG-MSG-13) n'est pas dans la démonstration : il est testé en IT.

### 7.2 Reprise AGI

Hors périmètre du prototype. Les champs « Commentaire » d'InteGraal (AGI 2.1, 2.5, 3) n'ont pas de vue de
conversation.

## 8. Décisions d'architecture

Principe repris de la spec 08 : architecture hexagonale stricte et modularisation Maven (ADR-021).
Décisions prises le 2026-10-17, en enchaînant les phases à la demande de l'utilisateur : l'option retenue est
chaque fois celle qui reproduit la forme de `facturation`, à relire.

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-MSG-1 | **Module d'API** `bnia-messagerie-api` (Java pur) : `Conversations` (appelée), `ObjetsConversables` (implémentée), `TypeObjet`, `ObjetRef`, `DescriptionObjet`, `Auteur`, `Acteur`. Le core d'un contexte métier n'en dépend pas : il a son propre port (`Messagerie` pour `declarations` : déposer le motif, supprimer la conversation d'une DSM), implémenté dans son infra (`adapter.integration`) sur `Conversations` ; son infra implémente aussi `ObjetsConversables` en appelant un cas d'usage de son core. Dépendances : `declarations-infra`, `facturation-infra` → `messagerie-api` ← `messagerie-core` ← `messagerie-infra` → `operateurs-core` ; aucun cycle. | Core du contexte métier appelant l'API (hexagonale moins stricte) ; paquets `partage` dans le core (frontière par ArchUnit seul) ; ports côté contextes métier implémentés par `messagerie` (la lib dépendrait d'eux) | ADR-023, ADR-021 amendé |
| A-MSG-2 | **Descriptions injectées** : le core reçoit la liste des `ObjetsConversables`, indexée par `type()` ; deux implémentations du même type font échouer le démarrage. | Registre dans `bnia-boot` | ADR-023 |
| A-MSG-3 | **Port de l'hôte** `DroitsMessagerie`, défini dans le core, implémenté dans `bnia-messagerie-infra` (`adapter.integration`) sur la partie publiée d'`operateurs` : côté de l'acteur, lecture et gestion d'un `TypeObjet` sur un opérateur (le module de chaque type n'est connu que de l'adapter), rattachement actif d'un utilisateur, noms des opérateurs et des utilisateurs. La règle d'écriture (gestion, ou lecture pour une facture, RG-MSG-01) reste dans le core. | Module de chaque type dans le core (dépendance à `operateurs`) ; droits rendus par `ObjetsConversables` (chaque contexte réécrirait les règles de côté et de BNIA) | ADR-023 |
| A-MSG-4 | **Appels synchrones** : `declarations` dépose le motif et supprime la conversation par `Conversations`, dans sa transaction (`@UseCase`, `REQUIRED`). | Écoute de `ComplementsDemandes` par `messagerie` (elle dépendrait de `declarations`) | ADR-023 |
| A-MSG-5 | **Références** : `ObjetMetier` (type, identifiant en texte) **sans clé étrangère**, index unique (type, identifiant) ; opérateur de la conversation **avec** clé étrangère vers `operateurs.operateur` (un opérateur qui a des conversations ne se supprime pas, comme avec ses DSM) ; auteurs (identifiant et copie du nom) sans clé étrangère. Les noms d'opérateur des listes se lisent par `DroitsMessagerie`, sans jointure entre schémas. | Clé étrangère par type d'objet ; opérateur opaque comme `ClientRef` (la conversation devrait recopier le nom) | ADR-011 amendé |
| A-MSG-6 | **Lecture par colonnes** : `lu_operateur` et `lu_bnia` sur `messagerie.message`, avec un index partiel par côté sur les non lus ; « afficher » est une mise à jour par conversation. | Table `lecture` (message, côté) : une ligne de plus par message lu, sans gain à deux côtés | — (ADR-004) |
| A-MSG-7 | **Formats de documents** : `StockageDocuments.deposer(nom, contenu, formats admis)`, `FormatDocument` (`PDF`, `PNG`, `JPEG`, `XLSX`) dans `bnia-commun-core`, signature vérifiée par l'adapter (`%PDF`, en-tête PNG, en-tête JPEG, archive ZIP) ; `deposerPdf` demeure pour les justificatifs et DRM. Les pièces jointes sont une table fille de `message`, avec clé étrangère vers `documents.document`. | Stockage propre à `messagerie` ; tout type, contrôlé sur la taille seule | ADR-018 amendé |
| A-MSG-8 | **Recherche** : extension `unaccent` créée dans `V1__schema.sql`, comparaison `unaccent(lower(...)) like unaccent(lower(:motif))` sur le texte, le titre et le libellé, sans index (volume du prototype). | Plein texte `tsvector` (numéros mal trouvés) ; `ilike` seul (accents) | — |
| A-MSG-9 | **Notes internes dans chaque contexte métier** : `NoteInterne` de `declarations`, entité à part de la DSM (table `declarations.note_interne`), reprise du code des commentaires sans côté ni motif. Pas de lib commune des notes. | Notes dans `messagerie` (côté BNIA seul : l'analyse l'a écarté) ; lib commune de notes (un troisième module d'API pour une table et trois règles) | ADR-023 |

## 9. Hypothèses

| Id | Hypothèse | Pourquoi |
|---|---|---|
| H-MSG-1 | Le BNIA veut une messagerie : une conversation par objet métier, partagée par l'opérateur, et des conversations libres ouvertes par le portail, sans destinataire nommé, avec lu / non lu par côté et un compteur aux tableaux de bord. | Absente du CDC, ajoutée pour la démonstration (analyse §1) ; seuls des champs « commentaire » épars (C.3 p.107, C.4 p.113, C.7 p.122) et le contact du BNIA (§5.2.2 p.61) l'annoncent. |
| H-MSG-2 | Une conversation libre n'est vue, côté opérateur, que de son auteur, même d'un `ADMIN` ; après son départ, le BNIA seul la voit, en lecture seule. | Besoin exprimé le 2026-10-17 : une demande d'aide peut être personnelle ; écart à RG-OPE-31. |
| H-MSG-3 | Les notes de contrôle d'une ligne, le commentaire de contrôle d'un justificatif et les commentaires internes deviennent des notes internes signées et datées, tenues par le contexte de l'objet, distinctes des messages. | §4.4.5 p.26, C.3.2.1 p.108, C.3.3.1 p.111 : champs uniques non signés ; plusieurs agents instruisent une même DSM (décision du 2026-10-17). |
| H-MSG-4 | L'observation générale de la DSM est un message de sa conversation ; la note de transfert en région est supprimée. | C.3.1.1 p.107, C.3.2.5 p.110 ; la note est absente des deux matrices Excel (analyse). |
| H-MSG-5 | Le client d'une facture émise peut écrire dans sa conversation, bien qu'il ne gère pas le module Facturation. | Sans quoi il ne pourrait poser aucune question sur sa facture (décision du 2026-10-17). |

## 10. Questions ouvertes

| Id | Question | Bloque |
|---|---|---|
| Q-MSG-1 | **Au BNIA** : confirmer l'intérêt et la forme de la messagerie (H-MSG-1). | Le contexte entier ; à défaut, retour aux commentaires de la DSM |
| Q-MSG-2 | **Au BNIA** : confirmer la confidentialité des conversations libres, `ADMIN` compris, et leur lecture seule après le départ de l'auteur (H-MSG-2). | Droits des conversations libres (RG-MSG-02, 13) |
| Q-MSG-3 | **Au BNIA** : confirmer les notes internes signées, en carnet distinct des messages, à la place des notes et commentaires de contrôle (H-MSG-3). | Carnet de `declarations` (lot 1), puis contrats et certificats |
| Q-MSG-4 | **Au BNIA** : confirmer l'observation générale portée par un message et la suppression de la note de transfert (H-MSG-4). | Champs de la DSM (spec 05, H-DEC-12) |
| Q-MSG-5 | **Au BNIA** : confirmer qu'un client écrit dans la conversation de sa facture (H-MSG-5). | Lot 6 |

## 11. Tests attendus

Chaque RG-MSG-nn a au moins un test qui porte son identifiant (CLAUDE.md).
- **Tests de domaine**, sans base : RG-MSG-05 (texte), 06 (modification, protégé, conversation vide), 09
  (lu à l'écriture), 10 (marquer non lu), 12 (titre), 14 (nombre de pièces).
- **Tests de cas d'usage**, avec repositories simulés, puis IT : RG-MSG-01 (refus par type, écriture par
  lecture sur une facture), 02 (`ADMIN` qui ne voit pas la conversation libre d'un autre), 03, 04 (unicité,
  objet inconnu), 07 (brouillon supprimé), 08 (motif déposé à la demande de compléments), 10 (affichage depuis
  la page de l'objet), 11 (compteur par espace, sans conversation non autorisée), 13 (auteur retiré), 14
  (signatures, 10 Mo), 15, 16 (« debit » trouve « débit »), 17 (brouillon de facture non conversable).
- `declarations` : RG-DEC-24 et 25 réécrites (note interne invisible du portail ; motif protégé).

## 12. Lots d'implémentation

| Lot | Incrément | Contenu (cas d'usage, écrans, RG) | Démontrable à la fin du lot |
|---|---|---|---|
| 1 | **Carnet de notes internes de la DSM** (`declarations`) | `NoteInterne` (DSM ou ligne), cas d'usage, table, REST ; section *Notes internes* et pastille de ligne au back-office ; les commentaires internes deviennent des notes ; démonstration. RG-DEC-24 réécrite (spec 05) | Un agent du BNIA annote une ligne de DSM ; l'opérateur ne voit rien |
| 2 | **Conversation de la DSM** | Modules `bnia-messagerie` (`-api`, `-core`, `-infra`), schéma `messagerie`, `Module.MESSAGERIE` (spec 02) ; `Conversations`, `ObjetsConversables` (type `DSM`, dans `declarations-infra`) ; écrire, modifier, supprimer ; motif déposé ; suppression avec le brouillon ; `Commentaire` retiré de `declarations` ; section *Messages* de la DSM aux deux espaces ; démonstration. RG-MSG-01, 03 à 08 ; RG-DEC-20, 22, 23, 25 révisées | L'opérateur et le BNIA échangent sur une DSM ; le motif des compléments y figure, protégé |
| 3 | **Nouveaux messages** | Lu / non lu par côté, affichage qui lit, « marquer non lu » ; carte des deux tableaux de bord ; écran Messagerie, onglet *Nouveaux messages* et vue de conversation ; pastille de la page de l'objet. RG-MSG-09 à 11 | Le BNIA voit « 1 nouveau message », l'ouvre, le compteur tombe ; il le remet non lu, le compteur remonte |
| 4 | **Conversations et recherche** | Onglet *Conversations* : liste, filtres, non lus par conversation, recherche sans casse ni accents (`unaccent`). RG-MSG-15, 16 | Le BNIA retrouve toutes les conversations d'un opérateur et cherche « DAE » |
| 5 | **Conversations libres** | Ouvrir, répondre ; confidentialité ; auteur parti ; pièces jointes (stockage étendu à PNG, JPEG, `.xlsx`) ; *Nouvelle conversation* au portail ; démonstration. RG-MSG-02, 12 à 14 | Un agent du portail demande de l'aide avec sa matrice jointe ; son `ADMIN` ne voit pas la conversation |
| 6 | **Conversation d'une facture** | `ObjetsConversables` du type `FACTURE` (`facturation-infra`), écriture par la lecture ; section *Messages* de la pièce aux deux espaces ; démonstration ; révision de la spec 08. RG-MSG-01 (facture), 17 | Le client interroge le BNIA sur sa facture depuis le portail |

## Journal

| Date | Changement |
|---|---|
| 2026-10-17 | Phases A et B : recherche transverse des usages ; spec créée (objet, périmètre). Décision : la **facture émise** est conversable, en plus de la DSM ; sur une facture, la lecture du module Facturation suffit pour écrire (ajout à l'analyse, reporté dans `analyse.md`). Notes internes, commentaire du contrat, sorties export et fins de travaux renvoyés à leur contexte. |
| 2026-10-17 | Phase C : décisions en séance. L'auteur modifie (« modifié ») ou supprime son message, sauf le motif protégé, sans trace ; une conversation vide est supprimée. Afficher une conversation la lit pour tout le côté ; « marquer non lu » jusqu'au prochain affichage. Conversation libre en lecture seule pour le BNIA après le départ de son auteur. Pièces jointes PDF, PNG, JPEG, `.xlsx`, 10 Mo, 5 par message. Facture : pièce émise d'un client opérateur, libellé sens et numéro, lien construit par l'écran. Recherche « contient », sans casse ni accents, sur messages, titres et libellés. Carnet de notes internes signées et datées dans `declarations`, sur la DSM ou une ligne. Modèle (§3), RG-MSG-01 à 17, cas d'usage, écrans, démonstration, H-MSG-1 à 5 et Q-MSG-1 à 5, six lots. Conventions : 4 000 caractères par message, 120 par titre. |
| 2026-10-17 | Phases D à H, enchaînées à la demande de l'utilisateur. D : `contextes.md` (ordre, bordure Q-MSG, fiche 15, fiches 2, 5, 6, 7, 8, 11, éléments transverses, context map, journal). E : décisions A-MSG-1 à 9, chaque fois la forme de `facturation`, à relire. F : H-MSG-1 à 5 ont chacune leur question au BNIA ; aucune question « à trancher ». G : relecture (RG continues, sources, cas d'usage, tests, lots) ; glossaire (Message, Conversation, Conversation libre, Note interne, Nouveaux messages ; Commentaire, Instruction, Module, Rôle révisés). H : spec terminée ; ADR-023 ; ADR-011, 018 et 021 amendés ; `CLAUDE.md`. Révisions des specs 02, 05 et 08 livrées par les lots 1, 2 et 6. |
| 2026-10-17 | Implémentation, lots 1 à 6, enchaînés à la demande de l'utilisateur. Écarts au plan des lots : les lots 1 et 2 sont livrés ensemble (le carnet de notes et le retrait de `Commentaire` touchent les mêmes fichiers de `declarations`) ; `Conversations.nonLus` n'est pas publié (la page d'un objet affiche sa conversation, avec ses non lus, par la messagerie elle-même). Choix d'implémentation : persistance en `JdbcClient` (conversations, messages, pièces jointes en table fille) ; afficher une conversation est un `GET` qui la lit pour le côté de l'acteur, comme la consultation journalisée d'une facture (RG-FAC-28) ; répondre sans afficher ne lit pas (le BNIA qui répond sans ouvrir garde le message de l'opérateur non lu) ; côté d'un acteur : membre actif de l'opérateur, sinon personnel du BNIA ; « marquer non lu » proposé sur les messages de l'autre côté ; réponse et ouverture en `multipart/form-data` (`texte`, `titre`, `pieces`), PDF et images ouverts dans le navigateur, Excel téléchargé ; `FormatDocument` dans `bnia-commun-core`, `deposerPdf` devenu une méthode par défaut de `deposer` ; recherche `unaccent(lower(...)) like`, motif échappé ; listes sans pagination ; schéma OpenAPI « Cote » partagé avec `declarations` (même ordre de valeurs). Écrans : menu « Messagerie » sous « Accueil » aux deux espaces, adresse `?onglet=&conversation=&message=` ; la carte « Nouveaux messages » du back-office remplace la carte de démonstration ; au portail, une carte « Messagerie » sur l'accueil. Démonstration : `DemoMessagerie` (ordre 6), relançable dès qu'une conversation libre de Gascogne existe ; l'« auteur parti » n'est vérifié qu'en IT. |
| 2026-10-18 | Écrans (§6), à la demande de l'utilisateur : la messagerie n'a plus d'onglets ; *Nouveaux messages* et *Conversations* fusionnent en une liste unique de conversations, comme une boîte de réception, non lues en gras (RG-MSG-11 et 15 révisées, §5.2) ; les cartes des tableaux de bord ouvrent la liste sans filtre (adresse `?nonLues=&conversation=&message=`, « non lues seulement » retenu dans l'adresse) ; une conversation s'ouvre sur son premier non lu. Fil en bulles, regroupé par jour et par auteur, défilant dans son panneau ; notes internes au même gabarit. Pièces jointes : le sélecteur filtre par type MIME, le format est vérifié au choix (RG-MSG-14). |
| 2026-10-21 | Droits (demande de l'utilisateur) : l'`OBSERVATEUR` ne gère plus le module Messagerie, il le lit, comme tous les autres modules (§3.6) ; au portail, il n'a plus « Nouvelle conversation » ; au BNIA comme au portail, il lit les conversations libres sans y écrire (« Votre rôle permet de lire cette conversation, pas d'y écrire. »). RG-MSG-02 et 12 révisées. Choix d'implémentation : `Role.gere(MESSAGERIE)` faux pour `OBSERVATEUR` (spec 02 §3.2.7) ; la lecture d'une conversation libre ne dépend plus que du côté de l'acteur, l'écriture de `gereMessagerie` (`Acces.surLibre`), refus sous RG-MSG-02 ; le niveau `messagerie` s'ajoute aux droits de l'API (`Droits`), que le bouton consulte. Démonstration : la seconde conversation libre de Gascogne est ouverte par Henri Castex, et non plus par Thomas Barrère (§7.1). Tableau de bord du portail : la carte « Messagerie » devient la carte « Nouveaux messages » de la section *Activités* (§6.2). |
