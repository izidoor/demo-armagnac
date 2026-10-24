# ADR-024 : `notifications`, lib en aval, canal push par Web Push

- Statut : accepté
- Date : 2026-10-24
- Suit : ADR-021 et ADR-023 (forme de lib en aval)
- Spec : `docs/specs/11-notifications/spec.md` (décisions A-NOT-1 à A-NOT-4)

## Contexte
Pour la soutenance du 2026-10-27, le fil des contrats d'achat Armagnac doit prévenir l'autre partie sur son
téléphone, aussitôt, même application fermée : un projet à approuver, une modification, une conclusion, une
retiraison à valider. RG-CA-18 envoie déjà un e-mail pour ces faits. L'application est installable (PWA) : Web
Push (Push API ; RFC 8030, 8291, 8292) la fait notifier sur Android, iPhone et iPad (iOS 16.4 et plus, application
ajoutée à l'écran d'accueil) et sur ordinateur, sans application du store ni compte chez Google, Mozilla ou Apple.

Il faut garder les abonnements des appareils (un par appareil et par utilisateur) et chiffrer chaque message pour
son appareil. `docs/contextes.md` prévoit un contexte `notifications` (fiche 11), distinct de `messagerie` : une
notification vient du système et vise des destinataires.

## Décision
- **Ouvrir `notifications`, réduit au canal push, en lib en aval**, sur le modèle de `facturation` et de
  `messagerie` :
  - module d'API `bnia-notifications-api`, Java pur, sans dépendance : `Notifications.envoyer(Notification)`,
    `Notification(utilisateurs, titre, texte, lien)` ;
  - le core d'un contexte métier a son propre port (`EnvoiNotifications` pour `contratsarmagnac`), implémenté dans
    son infra (`adapter.integration`) sur l'API ;
  - le contexte métier décide des destinataires (identifiants d'utilisateurs, lus dans la partie publiée
    d'`operateurs`, qui publie `PersonneVue.utilisateur`) et du texte ; `notifications` ne connaît aucun métier.
- **Web Push écrit avec le JDK**, dans `bnia-notifications-infra` : chiffrement `aes128gcm` (RFC 8291 : ECDH P-256,
  HKDF, AES-128-GCM), jeton VAPID ES256 (RFC 8292), `java.net.http.HttpClient`. Vérifié sur l'exemple de
  l'annexe A de RFC 8291. Envoi après la validation de la transaction de l'appelant, sur un thread virtuel ; un
  abonnement expiré (404, 410) est retiré ; tout autre échec est journalisé et n'annule rien.
- **Clés VAPID par configuration** (`bnia.notifications.vapid.*`, variables `BNIA_NOTIFICATIONS_VAPID_*`) : une
  paire de dev dans `application-dev.yml`, une paire propre à chaque installation hébergée ; sans clés, pas de push.
- **Abonnements** dans le schéma `notifications`, avec clé étrangère vers `operateurs.utilisateur` et suppression en
  cascade.
- **Dépendances** : `contratsarmagnac-infra` → `notifications-api` ← `notifications-core` ← `notifications-infra`.
  Aucun cycle ; `notifications` ne lit aucun autre contexte.

Options écartées :
- **abonnements et envoi dans `contratsarmagnac`** : plus rapide, mais l'abonnement d'un appareil n'est pas une
  notion du contrat ; il faudrait le déplacer dès qu'une DSM ou une facture notifierait ;
- **dans `messagerie`** : une notification n'est pas un message (`contextes.md`, fiche 11) ;
- **la bibliothèque `nl.martijndwars:web-push`** : peu maintenue, elle tire BouncyCastle et un client HTTP de plus,
  pour moins de deux cents lignes de JDK ;
- **`notifications` à l'écoute d'événements des contrats** : il faudrait une partie publiée de `contratsarmagnac`
  et la règle des destinataires (`AGENT` et `ADMIN` de l'autre partie) dans `notifications`.

## Conséquences
- `CLAUDE.md` cite `bnia-notifications-api` parmi les exceptions à l'isolement, importé par l'infra des contextes
  métier seulement.
- ArchUnit vérifie qu'aucun core n'importe `fr.bnia.notifications.api`, hors `notifications` elle-même, que rien
  d'autre de `notifications` n'est importé ailleurs, et que l'API ne dépend que d'elle-même.
- La démonstration hébergée reçoit sa paire VAPID par variables d'environnement
  (`docs/deploiement-scalingo.md`) ; changer de paire invalide les abonnements.
- Le centre de notifications, les relances et les règles de destinataires du CDC (§5.12, C.14) compléteront ce
  contexte ; d'autres contextes (DSM, factures, messages) ajouteront leur port propre, comme `contratsarmagnac`.
