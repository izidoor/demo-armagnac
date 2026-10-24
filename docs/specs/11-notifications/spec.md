# 11 : notifications (canal push)

> Contexte `notifications` (étape 11 de `docs/contextes.md`), ouvert le 2026-10-24 pour la démonstration au BNIA
> (soutenance du 2026-10-27), réduit pour l'instant au **canal push** : les notifications du système sur les
> appareils des utilisateurs (téléphone, tablette, ordinateur), par l'application installée (PWA).
> Statut : **implémentée** (2026-10-24, un lot). Le centre de notifications, les relances et les règles de
> destinataires du CDC (§5.12 p.77, C.14 p.136) restent à spécifier : ils compléteront cette spec.
> Références : `p.N` = page du CDC v1.1. `H-NOT-n` : hypothèse retenue, révisable.
> Pas d'analyse préalable (skill `analyse`) : le canal push est une demande de démonstration, décidée en séance le
> 2026-10-24 (placement et périmètre ci-dessous).

## Sommaire

1. [Objet](#1-objet)
2. [Périmètre](#2-périmètre)
3. [Modèle](#3-modèle)
4. [Règles de gestion](#4-règles-de-gestion)
5. [Cas d'usage](#5-cas-dusage)
6. [Écrans](#6-écrans)
7. [Données](#7-données)
8. [Décisions d'architecture](#8-décisions-darchitecture)
9. [Hypothèses](#9-hypothèses)
10. [Tests attendus](#10-tests-attendus)
11. [Lots d'implémentation](#11-lots-dimplémentation)

## 1. Objet

Prévenir aussitôt un utilisateur, sur ses appareils, d'un fait qui l'attend ou le concerne, même quand
l'application est fermée : d'abord le fil des contrats d'achat Armagnac (RG-CA-18 : un projet à approuver, une
modification, une conclusion, une retiraison à valider…). Le CDC demande des notifications au portail (§5.12.2
p.77 : « un contrat en attente de validation par l'opérateur ») ; le push en est la forme la plus directe sur un
téléphone, sans application du store.

Une notification vient du système et vise des destinataires ; un message de `messagerie` (spec 15) est écrit par
une personne, sans destinataire (`docs/contextes.md`, fiche 11). Le contexte métier décide **qui** prévenir et
**quoi** dire ; `notifications` ne connaît aucun métier : il garde les abonnements des appareils et y envoie les
messages.

Technique : Web Push, standard du W3C et de l'IETF (Push API ; RFC 8030, 8291, 8292), commun à Chrome, Edge,
Firefox, Samsung Internet et Safari. Aucun compte chez Google, Mozilla ni Apple : le serveur s'identifie par sa
paire de clés VAPID.

| Appareil | Push |
|---|---|
| Android (Chrome, Edge, Firefox, Samsung Internet) | Oui, application installée ou dans le navigateur |
| iPhone, iPad | iOS et iPadOS 16.4 et plus, **application ajoutée à l'écran d'accueil** seulement |
| Ordinateur (Chrome, Edge, Firefox ; Safari sur macOS 13 et plus) | Oui, tant que le navigateur tourne |

## 2. Périmètre

### 2.1 Dans le contexte
- Abonnement d'un appareil, par l'utilisateur connecté, depuis Mon compte ; désabonnement ; essai.
- Envoi d'une notification aux appareils abonnés de ses destinataires, pour un contexte métier qui l'appelle
  (`bnia-notifications-api`).
- Retrait des abonnements expirés.

### 2.2 Hors du contexte (pour l'instant)
- Centre de notifications du portail (liste, date, type, lien, lu ou non lu ; §5.12.1 p.77), relances (§5.12.3),
  règles de destinataires par centre d'intérêt (C.14.1.2 p.136) : à spécifier dans cette spec.
- Les e-mails : chaque contexte envoie encore les siens (`contratsarmagnac`, `operateurs`, `authentification`).
- Le choix des faits notifiés et de leur texte : au contexte métier (spec 06 RG-CA-18 pour les contrats).
- Les notifications des DSM, des factures et des messages : quand leur contexte appellera `Notifications`.

## 3. Modèle

### 3.1 Abonnement (`Abonnement`)
L'abonnement push d'un appareil, que son navigateur a reçu de son service push.

| Champ | Contenu |
|---|---|
| `utilisateur` | identifiant de l'utilisateur dans `operateurs`, avec clé étrangère : ses abonnements sont supprimés avec lui |
| `adresse` | URL HTTPS du service push, propre à l'appareil (`endpoint`) ; unique |
| `cleP256dh` | clé publique de l'appareil, point P-256 non compressé de 65 octets, en base64url (`keys.p256dh`) |
| `secretAuth` | secret d'authentification de 16 octets, en base64url (`keys.auth`) |
| `creeLe` | instant du dernier abonnement |

Un utilisateur a un abonnement par appareil (et par navigateur) où il a activé les notifications ; plusieurs
utilisateurs connectés tour à tour sur un même appareil n'en ont qu'un, au dernier abonné (RG-NOT-02).

### 3.2 Message push (`MessagePush`)
Ce qu'un appareil affiche : un **titre** (une ligne), un **texte** (une phrase), un **lien** (chemin de
l'application ouvert par un appui), qui sert aussi d'étiquette : une notification remplace la précédente du même
lien. Lu sur l'écran verrouillé, il ne porte ni montant ni volume (spec 06 RG-CA-18).

### 3.3 Partie publiée : `bnia-notifications-api`
- `Notifications.envoyer(Notification)`, implémentée par le core, appelée par l'infra d'un contexte métier dans
  sa transaction ;
- `Notification(utilisateurs, titre, texte, lien)`.

### 3.4 Droits
Aucun module : chacun gère les notifications de ses propres appareils, comme son profil (RG-OPE-33). Un
désabonnement ne touche que les appareils de l'acteur (RG-NOT-05).

## 4. Règles de gestion

| Id | Règle | Source | Lot |
|---|---|---|---|
| RG-NOT-01 | Un abonnement a une adresse HTTPS, une clé P-256 de 65 octets et un secret de 16 octets, en base64url ; un utilisateur abonne chacun de ses appareils. | RFC 8030, 8291 ; décision du 2026-10-24 | 1 |
| RG-NOT-02 | S'abonner avec une adresse déjà connue la donne à l'utilisateur qui s'abonne, avec ses nouvelles clés : l'appareil d'un utilisateur qui ne s'est pas déconnecté passe au suivant. Ouvrir Mon compte réenregistre l'abonnement de l'appareil. | décision du 2026-10-24 | 1 |
| RG-NOT-03 | Une notification part vers tous les appareils abonnés de tous ses destinataires, après la validation de la transaction de l'appelant ; un échec d'envoi est journalisé et n'annule rien. Sans appareil abonné, rien ne part. | décision du 2026-10-24 | 1 |
| RG-NOT-04 | Un abonnement que son service push dit expiré (HTTP 404 ou 410 : notifications coupées dans les réglages, application désinstallée) est supprimé. | RFC 8030 §7.3 | 1 |
| RG-NOT-05 | L'utilisateur désabonne son appareil (« Désactiver », et à la déconnexion, avant la fermeture de la session) ; désabonner l'adresse d'un autre utilisateur, ou une adresse inconnue, ne change rien. | décision du 2026-10-24 | 1 |
| RG-NOT-06 | L'utilisateur s'envoie une notification d'essai sur tous ses appareils abonnés ; sans appareil abonné, l'essai est refusé. | décision du 2026-10-24 | 1 |
| RG-NOT-07 | Sans paire de clés VAPID configurée, l'installation n'envoie pas de notification push : pas de clé publique, abonnement refusé. | RFC 8292 ; décision du 2026-10-24 | 1 |

## 5. Cas d'usage

### 5.1 Envoi
- `EnvoyerNotifications` (implémente `Notifications`) : lit les abonnements des destinataires, les confie au
  service push (RG-NOT-03).
- `RetirerAbonnementsExpires` : appelé par l'adapter Web Push sur un 404 ou un 410 (RG-NOT-04).

### 5.2 Mes appareils (`GererAbonnements`)
`clePublique` (RG-NOT-07), `abonner` (RG-NOT-01, 02, 07), `desabonner` (RG-NOT-05), `essayer` (RG-NOT-06).

## 6. Écrans

Mon compte, onglet **Profil**, section « Notifications sur cet appareil », au back-office comme au portail :

| État de l'appareil | Affiché |
|---|---|
| Installation sans clés, ou navigateur sans push | « Cette installation n'envoie pas de notifications. », ou « Ce navigateur ne reçoit pas de notifications. » |
| iPhone ou iPad, dans Safari | la marche à suivre : Partager, « Sur l'écran d'accueil », puis ouvrir BNIA depuis son icône |
| Refusées | comment les rétablir (iPhone : Réglages, Notifications, BNIA ; Android : appui long sur l'icône, Infos sur l'appli, Notifications ; ordinateur : le cadenas de l'adresse) |
| Non activées | « Activer les notifications » : l'autorisation est demandée sur cet appui (iOS l'exige) |
| Activées | « Activées sur cet appareil », « Envoyer une notification d'essai », « Désactiver » |

La notification affiche le titre, le texte et l'icône de BNIA ; un appui ouvre son lien dans une fenêtre de
l'application déjà ouverte, sinon dans une nouvelle (le lien d'une retiraison ouvre la page des retiraisons du
contrat sur un téléphone, sa carte dans le contrat sur ordinateur, spec 06 §6.2). Se déconnecter désabonne d'abord l'appareil (RG-NOT-05).

## 7. Données

- Démonstration : aucun abonnement chargé ; chacun active les notifications sur son appareil. La remise à zéro
  de la démonstration (et toute modification de `V1__schema.sql`) les efface : il faut les réactiver.
- Reprise : hors périmètre du prototype.

## 8. Décisions d'architecture

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-NOT-1 | **Lib en aval** : module d'API `bnia-notifications-api` (Java pur, sans dépendance), `Notifications` et `Notification` ; le core d'un contexte métier a son propre port (`EnvoiNotifications` pour `contratsarmagnac`), implémenté dans son infra sur l'API. Le contexte métier choisit les destinataires (identifiants d'utilisateurs, lus dans la partie publiée d'`operateurs` : `PersonneVue.utilisateur`) et le texte. Dépendances : `contratsarmagnac-infra` → `notifications-api` ← `notifications-core` ← `notifications-infra`. | Abonnements et envoi dans `contratsarmagnac` (à déplacer au premier autre contexte) ; dans `messagerie` (une notification n'est pas un message, `contextes.md` fiche 11) | ADR-024 |
| A-NOT-2 | **Web Push écrit avec le JDK** (`adapter.integration.WebPushHttp`) : chiffrement `aes128gcm` de RFC 8291 (ECDH P-256, HKDF, AES-128-GCM), jeton VAPID ES256 de RFC 8292, `java.net.http.HttpClient` ; vérifié sur l'exemple de l'annexe A de RFC 8291. Envoi après la validation de la transaction, sur un thread virtuel. | Bibliothèque `nl.martijndwars:web-push` (peu maintenue, tire BouncyCastle) | ADR-024 |
| A-NOT-3 | **Clés VAPID par configuration** : `bnia.notifications.vapid.cle-publique`, `cle-privee` et `sujet` (variables `BNIA_NOTIFICATIONS_VAPID_*`) ; une paire de dev dans `application-dev.yml`, une paire propre à la démonstration hébergée dans ses variables d'environnement. | Clés en base ; clés générées au démarrage (les abonnements tomberaient à chaque redémarrage) | ADR-024 |
| A-NOT-4 | **Persistance** en `JdbcClient` : table `notifications.abonnement`, adresse unique, clé étrangère vers `operateurs.utilisateur` avec suppression en cascade. | Abonnement sans clé étrangère, nettoyé par l'événement `UtilisateurSupprime` | ADR-011 |

## 9. Hypothèses

| Id | Hypothèse |
|---|---|
| H-NOT-1 | Le push s'ajoute aux e-mails, il ne les remplace pas : un utilisateur sans appareil abonné reste prévenu par e-mail. |
| H-NOT-2 | Le lien d'une notification ouvre l'objet dans l'opérateur actif, comme celui d'un e-mail : une personne de plusieurs opérateurs peut devoir changer d'opérateur actif. |

## 10. Tests attendus

- Domaine : `AbonnementTest` (RG-NOT-01).
- Cas d'usage : `NotificationsTest` (RG-NOT-01 à 07, dont le refus de RG-NOT-05).
- Adapter : `ChiffrementWebPushTest` (exemple de RFC 8291, jeton VAPID vérifié, charge utile JSON).
- Intégration : `NotificationsIT` (clé publique, deux appareils et essai, reprise et désabonnement, suppression
  avec l'utilisateur) ; service push remplacé par `PushCaptures`.
- Contrats : spec 06 RG-CA-18 (`NegocierContratsTest`, `ExecuterContratsTest`).

## 11. Lots d'implémentation

| Lot | Contenu |
|---|---|
| 1 | Contexte `notifications` (api, core, infra, schéma), branchement de `contratsarmagnac` (RG-CA-18), section de Mon compte, service worker, déconnexion. |

## Journal

| Date | Changement |
|---|---|
| 2026-10-24 | Spec créée et implémentée (lot 1), pour la soutenance du 2026-10-27. Décisions en séance : placement en contexte `notifications`, lib en aval (A-NOT-1) ; les neuf faits de RG-CA-18 sont notifiés par push, comme par e-mail. ADR-024. Révisions : spec 06 RG-CA-18, spec 02 RG-OPE-33, `PersonneVue.utilisateur` (partie publiée d'`operateurs`). |
