# Spécification fonctionnelle : gestion des opérateurs, contacts et accès

Oct 8, 2026 · @Pierre-Yves

## 1. Cas d'utilisation

L'application permet à une interprofession agricole de tenir l'annuaire de ses opérateurs adhérents et à ces opérateurs de gérer eux-mêmes leurs personnes et leurs accès. L'interprofession (BNIA) est elle-même un opérateur : son personnel est géré avec les mêmes écrans et les mêmes règles que celui des autres opérateurs, avec en plus des droits qui valent sur tous les opérateurs.

**Vocabulaire retenu**

- **Opérateur** : une entité de l'interprofession. L'interprofession elle-même est un opérateur de type BNIA. Les opérateurs adhérents sont de type PRODUCTEUR ou NEGOCIANT.
- **Contact** : une personne connue d'un opérateur, sans accès à l'application.
- **Utilisateur** : une personne qui possède un compte (nom, prénom, email de connexion). Elle en est propriétaire.
- **Rattachement** : l'accès d'un utilisateur à un opérateur, avec un rôle propre à cet opérateur.
- **Fiche professionnelle** : les informations de la personne dans un opérateur (fonction, téléphone professionnel). Elle appartient à l'opérateur.
- **ADMIN** : rôle d'un rattachement qui permet de gérer les contacts et les accès de l'opérateur.
- **Droit transversal** : un droit qui vaut sur tous les opérateurs. Il ne peut être attribué que dans l'opérateur BNIA.
- **Module** : un ensemble de fonctions de l'application, par exemple les contacts ou les accès. Gérer un module permet d'y tout faire.
- **Rôle** : un nom donné à l'ensemble des modules qu'un rattachement peut gérer : OBSERVATEUR, AGENT ou ADMIN.

| Réf. | Acteur | Cas d'utilisation | Résumé |
| --- | --- | --- | --- |
| UC-01 | Personnel BNIA | Créer un opérateur | Saisit la raison sociale, le type (PRODUCTEUR ou NEGOCIANT) et l'identité du premier ADMIN (nom, prénom, email). Une invitation est envoyée à cet ADMIN. |
| UC-02 | Personnel BNIA | Ajouter un ADMIN à un opérateur | Invite un ADMIN supplémentaire, par exemple si le premier n'a pas répondu. |
| UC-03 | Personnel BNIA | Consulter l'annuaire d'un opérateur | Voit les contacts et les membres avec accès, pour connaître l'opérateur. |
| UC-05 | Personnel BNIA | Bloquer ou débloquer un utilisateur | Bloque un compte sur tous les opérateurs pour une raison de sécurité, ou le rétablit. |
| UC-10 | AGENT ou ADMIN | Déclarer un contact | Saisit nom, prénom, fonction, téléphone professionnel et, si connu, email. |
| UC-11 | AGENT ou ADMIN | Modifier ou supprimer un contact | Modifie l'identité et la fiche du contact, ou le supprime. |
| UC-12 | ADMIN | Inviter une personne à accéder à l'application | Part d'un contact ou d'une nouvelle personne. Saisit l'email et le rôle, puis envoie un lien d'activation. |
| UC-13 | ADMIN | Gérer les invitations en attente | Relance ou annule une invitation. |
| UC-14 | ADMIN | Modifier le rôle d'un membre | Change le rôle d'un rattachement. Un opérateur garde toujours au moins un ADMIN. |
| UC-15 | ADMIN | Suspendre ou réactiver un accès | Bloque ou rétablit l'accès d'un membre à l'opérateur. |
| UC-16 | ADMIN | Retirer un accès | Supprime le rattachement. Une case permet de conserver la personne comme contact. |
| UC-17 | Tout membre | Consulter l'annuaire de l'opérateur | Liste unifiée des contacts et des membres avec accès. |
| UC-18 | AGENT ou ADMIN | Modifier la fiche d'une personne de l'opérateur | Modifie fonction et téléphone d'un contact ou d'un membre. |
| UC-20 | Personne invitée | Accepter une invitation | Prouve son email par le lien. Crée son compte, ou rattache son compte existant. |
| UC-21 | Utilisateur | Se connecter et choisir son opérateur | Authentification, puis ouverture directe dans son opérateur actif, ou choix de l'opérateur de travail à défaut. |
| UC-22 | Utilisateur | Consulter ses opérateurs | Liste ses rattachements et ses rôles dans chacun, et permet de changer d'opérateur actif. |
| UC-23 | Utilisateur | Modifier son profil | Modifie son nom et son prénom. Le changement est visible dans tous ses opérateurs. |
| UC-24 | Utilisateur | Changer son email | Saisit le nouvel email et le confirme par un lien. L'email sert aussi de login. |
| UC-25 | Utilisateur | Modifier sa propre fiche | Modifie sa fonction et son téléphone dans un opérateur donné. |

## 2. Vision retenue

Chaque information a un seul propriétaire et n'est stockée qu'à un seul endroit. Cette règle rend impossible qu'une même donnée diverge entre deux écrans ou deux opérateurs.

### 2.1 Principes directeurs et contextes

**Qui possède quoi**

| Information | Propriétaire | Stockée dans | Qui peut la modifier |
| --- | --- | --- | --- |
| Nom, prénom, email d'un utilisateur | La personne | `utilisateur` | La personne seule |
| Nom, prénom, email d'un contact (sans compte) | L'opérateur | `contact` | Les membres habilités de l'opérateur |
| Fonction, téléphone professionnel | L'opérateur | `fiche_professionnelle` | La personne pour sa propre fiche ; tout membre habilité pour les autres |
| Rôle et statut d'accès | L'opérateur | `rattachement` | Les ADMIN de l'opérateur |
| Mot de passe, login technique, sessions | Contexte Authentification | Tables d'authentification | Le contexte Authentification |
| Opérateur actif de l'utilisateur | La personne | utilisateur | La personne seule |

**Principes**

1. Dans un opérateur donné, une personne est soit un contact, soit un membre avec accès (rattachement), jamais les deux. Dans un autre opérateur, elle peut être dans l'autre état. Tout membre avec accès figure dans l'annuaire de son opérateur.
2. L'interprofession (BNIA) est un opérateur comme les autres. Son personnel est géré par les mêmes cas d'utilisation et les mêmes règles : il n'existe pas de population distincte pour le back-office.
3. Un utilisateur n'existe qu'une fois son email prouvé, c'est-à-dire à l'acceptation d'une invitation. Aucun compte n'est créé à partir de la seule saisie d'un ADMIN.
4. Pour un membre avec accès, nom, prénom et email sont toujours lus dans `utilisateur`. Aucune copie n'en est faite dans l'opérateur tant que l'accès existe.
5. La fiche professionnelle reste la même ligne quand la personne passe de contact à membre, ou l'inverse. Elle n'est jamais recopiée.
6. Les identifiants internes sont des UUID. L'email n'est jamais un identifiant.
7. Le jeton d'authentification identifie la personne et ne porte aucun droit. Les droits sont évalués à chaque requête par le contexte Opérateurs.

**Contextes**

| Contexte | Nature | Responsabilité |
| --- | --- | --- |
| Opérateurs | Métier | Opérateurs, contacts, utilisateurs, rattachements, fiches professionnelles, invitations et droits. Les transitions entre états se font dans une seule transaction. |
| Authentification | Infrastructure | Prouver l'identité (mot de passe, fournisseur externe) et gérer les sessions ou jetons. Ce contexte est remplaçable : session serveur ou JWT, Spring Security seul ou Keycloak. Il ne contient ni nom, ni prénom, ni droits. |

L'architecture cible est un monolithe modulaire avec un module par contexte. Aucune clé étrangère ne traverse les schémas Authentification et Opérateurs. Elles sont autorisées à l'intérieur du schéma Opérateurs.

&#91;embedded content: contextes · Opérateurs (BNIA incluse) et Authentification\]

Le contexte Opérateurs porte tout le métier. Authentification prouve l'identité des personnes et peut être remplacée sans toucher au métier. Le personnel de l'interprofession est géré dans le contexte Opérateurs, comme celui de n'importe quel opérateur.

### 2.2 Modèle de données

Le schéma Opérateurs compte sept tables. L'identité d'une personne est portée par `contact` ou par `utilisateur`, jamais par les deux. Ses informations professionnelles sont portées par `fiche_professionnelle` dans tous les cas.

**Schéma Opérateurs**

| Table | Colonnes utiles au flux | À retenir |
| --- | --- | --- |
| `operateur` | raison sociale, type (BNIA, PRODUCTEUR ou NEGOCIANT), statut | Racine : ses contacts, ses membres et ses invitations en dépendent. Un seul opérateur est de type BNIA : l'interprofession. |
| `utilisateur` | nom, prénom, email, statut (ACTIF ou BLOQUE), opérateur actif (facultatif) | Un email correspond à un seul utilisateur. Créé uniquement à l'acceptation d'une invitation. L'opérateur actif est celui où la personne est connectée directement, sans choix. Un utilisateur BLOQUE par la BNIA ne peut se connecter à aucun opérateur. |
| `fiche_professionnelle` | opérateur, type (CONTACT ou RATTACHEMENT), fonction, tél. pro | Une fiche n'a qu'un seul porteur : un contact ou un rattachement du même opérateur. |
| `contact` | opérateur, nom, prénom, email (facultatif), fiche | Identité d'une personne sans compte. Une seule fiche. |
| `rattachement` | opérateur, utilisateur, rôle (OBSERVATEUR, AGENT ou ADMIN), statut (ACTIF ou SUSPENDU), fiche | Au plus un par opérateur et par utilisateur. Une seule fiche. |
| `invitation` | email, rôle, statut (EN\_ATTENTE, ACCEPTEE, ANNULEE ou EXPIREE), date d'expiration, et soit la fiche d'un contact existant, soit le nom, le prénom, la fonction et le tél. pro d'une nouvelle personne | Au plus une invitation en attente par contact. Le jeton n'est conservé que sous forme hachée. |
| `changement_email` | utilisateur, nouvel email, date d'expiration | Une seule demande en cours par utilisateur. |

**Autres schémas**

| Schéma | Contenu | Remarque |
| --- | --- | --- |
| Authentification | Identifiants de connexion (mot de passe, comptes externes) et sessions ou jetons | Aucun nom, aucun droit. Ce qui est propre à une technique disparaît avec elle. |
| Transverse | Journal d'audit | Qui a fait quoi, dans quel opérateur et quand. |

**Règles garanties par le modèle**

1. Un email correspond à au plus un utilisateur.
2. Une personne a au plus un rattachement par opérateur.
3. Chaque contact et chaque rattachement a exactement une fiche. Une fiche n'a qu'un seul porteur, du même opérateur et du type correspondant.
4. Un opérateur garde toujours au moins un rattachement actif avec le rôle ADMIN. Cette règle porte sur plusieurs lignes, elle est donc vérifiée par l'application dans la transaction concernée.
5. Une modification concurrente d'une fiche, d'un utilisateur ou d'un opérateur est détectée par un numéro de version. Le second enregistrement est refusé avec un conflit.
6. Un utilisateur est supprimé quand il perd son dernier rattachement, avec son compte de connexion.
7. Il n'existe qu'un seul opérateur de type BNIA : l'interprofession.

**Annuaire d'un opérateur (vue de lecture)**

L'écran Annuaire affiche les contacts et les membres avec accès dans une seule liste. Il s'appuie sur une vue SQL, sans copie de données, qui lit le nom d'un membre dans `utilisateur` et celui d'un contact dans `contact`.

Une personne qui a une invitation en attente est signalée comme telle dans la liste. Si elle n'est pas déjà contact, elle y figure d'après les informations de son invitation.

### 2.3 Flux de données par cas d'utilisation

Chaque flux indique les étapes, la transaction et les tables touchées. Sauf mention contraire, un cas d'utilisation de modification vérifie d'abord le droit de l'acteur, écrit une ligne dans le journal d'audit, et envoie les emails après la validation de la transaction.

&#91;embedded content: cycle de vie d'une personne dans un opérateur · 4 états\]

Une personne devient membre à l'acceptation d'une invitation (UC-20) et redevient contact ou disparaît au retrait de son accès (UC-16). Sa fiche professionnelle la suit dans tous les cas, sans copie. Seul UC-10 crée un contact. Une personne invitée directement (UC-01, UC-02, UC-12) devient membre sans passer par l'état de contact.

**UC-01 et UC-02 : créer un opérateur, ajouter un ADMIN (personnel BNIA)**

1. Le personnel BNIA saisit la raison sociale et le type, PRODUCTEUR ou NEGOCIANT (UC-01 seulement), puis le nom, le prénom et l'email de l'ADMIN.
2. Une transaction unique crée l'`operateur` (UC-01 seulement) et une `invitation` EN\_ATTENTE portant le rôle ADMIN, le nom, le prénom et l'email saisis. Aucun contact n'est créé.
3. Après la validation, le lien d'activation est envoyé par email.
4. L'opérateur n'a aucun membre avec accès tant que l'invitation n'est pas acceptée (UC-20).

Tables : `operateur`, `invitation`.

**UC-10 : déclarer un contact (AGENT ou ADMIN)**

1. L'acteur saisit nom, prénom, fonction, téléphone professionnel et, facultativement, l'email.
2. Une transaction crée la `fiche_professionnelle` de type CONTACT, puis le `contact`.
3. Si l'email saisi est celui d'un membre du même opérateur, l'acteur en est averti. Aucune recherche n'est faite dans les autres opérateurs.

Tables : `fiche_professionnelle`, `contact`.

**UC-11 : modifier ou supprimer un contact (AGENT ou ADMIN)**

1. Modification : mise à jour de l'identité dans `contact`, avec contrôle de version. La fiche se modifie par UC-18.
2. Suppression : une transaction annule les invitations EN\_ATTENTE de la fiche, puis supprime le `contact` et sa `fiche_professionnelle`.

Tables : `contact`, `fiche_professionnelle`, `invitation`.

**UC-12 : inviter une personne à accéder à l'application (ADMIN)**

1. L'ADMIN part d'un contact existant, ou saisit directement une nouvelle personne (nom, prénom, fonction, téléphone professionnel). Dans le second cas, aucun contact n'est créé : ces informations sont conservées dans l'invitation.
2. Il saisit l'email, obligatoire, et le rôle à accorder.
3. Une transaction met à jour l'email du contact si nécessaire et crée l'`invitation` EN\_ATTENTE (jeton haché, date d'expiration). Pour une nouvelle personne, l'invitation porte elle-même les informations saisies.
4. Après la validation, le lien est envoyé à l'email saisi.
5. Aucun `utilisateur`, `rattachement` ni contact n'est créé à cette étape. La personne apparaît dans l'annuaire avec la mention « invitation envoyée ».
6. L'invitation est refusée si une invitation EN\_ATTENTE existe déjà pour ce contact ou pour cet email dans l'opérateur (l'ADMIN peut la relancer), ou si l'email est celui d'un membre déjà rattaché à cet opérateur.

Tables : `invitation`, et `contact` si l'invitation part d'un contact.

**UC-13 : relancer ou annuler une invitation (ADMIN)**

1. Relance : un nouveau jeton remplace l'ancien, qui devient invalide, avec une nouvelle date d'expiration. Un nouvel email est envoyé.
2. Annulation : l'invitation passe à ANNULEE. Si elle partait d'un contact, il reste inchangé.
3. Une invitation dont la date est dépassée passe à EXPIREE. Si elle partait d'un contact, il reste inchangé.

Table : `invitation`.

**UC-20 : accepter une invitation (personne invitée)**

C'est le flux central du modèle : c'est lui qui donne un accès à la personne invitée, en reprenant son contact et sa fiche s'il y en a un.

1. La personne ouvre le lien. Le système vérifie que le jeton (comparé sous forme hachée) correspond à une invitation EN\_ATTENTE non expirée.
2. Si l'email de l'invitation correspond à un utilisateur existant, la personne s'authentifie. Sinon, elle choisit son mode de connexion et confirme son nom et son prénom, préremplis avec ceux saisis par l'ADMIN.
3. Une transaction unique effectue les opérations suivantes, dans cet ordre :
   1. trouver ou créer l'`utilisateur` ; en cas de création, créer aussi son compte de connexion ;
   2. refuser l'acceptation si cet utilisateur a déjà un `rattachement` dans l'opérateur ;
   3. si l'invitation partait d'un contact, supprimer ce `contact` ;
   4. si l'invitation partait d'un contact, passer le `type` de sa `fiche_professionnelle` à RATTACHEMENT ; sinon, créer une `fiche_professionnelle` de type RATTACHEMENT d'après les informations de l'invitation ;
   5. créer le `rattachement` (statut ACTIF) avec cette fiche et le rôle de l'invitation ;
   6. passer l'`invitation` à ACCEPTEE.
4. Si l'utilisateur existait déjà, ses nom et prénom priment sur ceux saisis par l'ADMIN. Ces derniers sont abandonnés. Si l'utilisateur n'avait pas d'opérateur actif, l'opérateur de l'invitation le devient.
5. En cas d'échec à n'importe quelle étape, tout est annulé : l'invitation et le contact éventuel restent comme avant.

Tables : `utilisateur`, `contact`, `fiche_professionnelle`, `rattachement`, `invitation`, et le schéma Authentification.

**UC-14 et UC-15 : modifier le rôle, suspendre ou réactiver un accès (ADMIN)**

1. L'ADMIN choisit un membre et modifie son rôle, ou passe son statut de ACTIF à SUSPENDU, ou l'inverse.
2. Le système vérifie que l'opérateur garde au moins un rattachement ACTIF avec le rôle ADMIN après la modification. Sinon, l'opération est refusée.
3. Mise à jour de `rattachement``  (rôle ou statut) `.
4. L'effet est immédiat pour cet opérateur seulement, puisque les droits sont lus à chaque requête. Les autres rattachements de l'utilisateur ne changent pas. Si l'utilisateur n'a plus aucun rattachement ACTIF, ses connexions sont refusées.

Table : `rattachement`.

**UC-16 : retirer un accès (ADMIN)**

1. L'ADMIN choisit un membre. Le formulaire propose une case « conserver cette personne dans mes contacts ».
2. Le système vérifie qu'au moins un ADMIN ACTIF reste dans l'opérateur. Un ADMIN peut se retirer lui-même s'il n'est pas le dernier.
3. Une transaction supprime le `rattachement`, puis :
   - case cochée : la `fiche_professionnelle` repasse au type CONTACT et un `contact` est créé avec une copie du nom, du prénom et de l'email de l'utilisateur à cet instant ;
   - case non cochée : la `fiche_professionnelle` est supprimée. Il ne reste aucune trace de la personne dans l'annuaire.
4. L'`utilisateur` et ses rattachements dans les autres opérateurs ne sont pas modifiés. S'il s'agissait de son dernier rattachement, l'utilisateur est supprimé avec son compte de connexion. La copie de ces informations dans le contact appartient désormais à l'opérateur et ne suit plus le profil de la personne.

Tables : `rattachement`, `fiche_professionnelle`, `contact`.

**UC-18 et UC-25 : modifier une fiche professionnelle**

1. L'acteur ouvre la fiche d'une personne de l'opérateur, qui est un contact ou un membre. L'acteur peut être la personne elle-même.
2. Le système autorise l'opération dans deux cas : la fiche est celle du propre rattachement ACTIF de l'acteur, ou l'acteur gère le module Contacts dans l'opérateur de la fiche.
3. Mise à jour de la fonction et du téléphone, avec l'auteur, la date et un contrôle de version. Si une autre modification a eu lieu entre-temps, l'opération est refusée avec un conflit et l'écran se recharge.
4. Pour un membre avec accès, nom, prénom et email sont affichés en lecture seule sur cet écran. Pour un contact, l'identité se modifie par UC-11.

Table : `fiche_professionnelle`.

**UC-23 : modifier son profil (utilisateur)**

1. L'utilisateur modifie son nom et son prénom.
2. Mise à jour de `utilisateur`, avec contrôle de version.
3. Aucune synchronisation n'est nécessaire : tous les écrans lisent directement `utilisateur`. Le changement est visible dans tous ses opérateurs.

Table : `utilisateur`.

**UC-24 : changer son email (utilisateur)**

1. L'utilisateur saisit le nouvel email. Si cet email est déjà utilisé par un autre compte, la demande est refusée et le message l'indique.
2. Une demande de changement est enregistrée dans `changement_email` (jeton haché, expiration) et un lien est envoyé au nouvel email.
3. Au clic sur le lien, une transaction met à jour l'email de l'utilisateur et son login de connexion, puis clôt la demande. Si l'email a été pris entre-temps, l'opération est refusée.
4. Une notification est envoyée à l'ancien email. Les invitations déjà envoyées à l'ancien email ne sont pas modifiées.

Tables : `changement_email`, `utilisateur`, et le schéma Authentification.

**UC-21 et UC-22 : se connecter, choisir son opérateur**

1. L'utilisateur s'authentifie auprès du contexte Authentification. La technique (session ou jeton, Spring Security ou Keycloak) n'a pas d'incidence sur le métier.
2. Le contexte Authentification interroge le contexte Opérateurs. La connexion est acceptée si l'utilisateur n'est pas bloqué et a au moins un rattachement ACTIF.
3. La session ou le jeton ne contient que l'identifiant de l'utilisateur, jamais ses rôles.
4. Si l'utilisateur a un opérateur actif où son rattachement est ACTIF, l'application s'ouvre directement dans cet opérateur, sans choix. Sinon, l'application affiche ses rattachements ACTIFS (UC-22) et l'utilisateur choisit un opérateur. Ce choix, à la connexion ou plus tard, devient son nouvel opérateur actif, et il peut en changer à tout moment.
5. L'opérateur est explicite dans chaque appel (par exemple `/operateurs/{id}/contacts`). Chaque cas d'utilisation vérifie le droit de l'acteur pour cet opérateur.

Le personnel de l'interprofession se connecte de la même façon. Son opérateur actif est l'opérateur BNIA.

Tables en lecture : `utilisateur`, `rattachement`.

**UC-03 et UC-17 : consulter l'annuaire**

1. Tout membre consulte l'annuaire de son opérateur. Le personnel BNIA, grâce à un droit transversal, peut consulter celui de n'importe quel opérateur.
2. Le système lit la vue `annuaire` filtrée sur l'opérateur. Les contacts et les membres s'affichent dans une seule liste, avec les mentions « invitation envoyée » et « accès suspendu ».

Lecture seule, aucune table modifiée.

**UC-05 : bloquer ou débloquer un utilisateur (personnel BNIA)**

1. Le personnel BNIA recherche l'utilisateur et choisit de le bloquer ou de le débloquer.
2. Mise à jour du statut de l'`utilisateur` (ACTIF ou BLOQUE) et écriture dans le journal d'audit.
3. L'effet est immédiat et vaut pour tous les opérateurs : les connexions de l'utilisateur sont refusées et sa session en cours est coupée.
4. Ses rattachements, son opérateur actif et ses données ne sont pas modifiés. Le déblocage rétablit la situation précédente.

Table : `utilisateur`.

### 2.4 Règles transverses et points à valider

**Règles transverses**

1. **Droits.** Les droits sont portés par les rôles du rattachement et évalués à chaque requête. Leurs principes sont décrits en section 2.5. Modifier sa propre fiche ne demande aucun droit, mais un rattachement ACTIF.
2. **Invitations.** Le jeton est aléatoire, à usage unique, stocké sous forme hachée et limité dans le temps. Il n'est valable que pour l'email de l'invitation.
3. **Emails.** Ils sont envoyés après la validation de la transaction. Un échec d'envoi n'annule pas l'opération et peut être repris par une relance.
4. **Concurrence.** Les modifications d'une fiche, d'un utilisateur ou d'un opérateur sont protégées par un contrôle de version. Le second enregistrement concurrent est refusé.
5. **Audit.** Chaque modification est journalisée avec l'acteur, l'opérateur, l'action, la cible et la date. Le journal est découplé : il ne dépend d'aucune autre table et reste lisible après la suppression d'un utilisateur.
6. **Suppression.** Un utilisateur est supprimé quand il perd son dernier rattachement, avec son compte de connexion. Les contacts et les fiches peuvent l'être aussi.
7. **Départ.** Un utilisateur ne peut pas quitter lui-même un opérateur. Seul un ADMIN retire un accès (UC-16).

**Cas limites à traiter**

- Une personne qui a déjà un rattachement dans l'opérateur ne peut pas accepter une seconde invitation de cet opérateur.
- Le dernier ADMIN actif d'un opérateur ne peut être ni retiré, ni suspendu, ni privé de son rôle.
- Un même email déclaré comme contact dans deux opérateurs donne deux contacts indépendants. Ils ne sont jamais rapprochés avant l'acceptation d'une invitation.
- Si l'utilisateur change d'email alors qu'une invitation est en attente sur l'ancien, l'invitation ne peut plus être acceptée. L'ADMIN doit la renvoyer.
- Une invitation expirée ou annulée laisse intact le contact dont elle partait, s'il y en avait un.

**Points à valider avec le métier**

1. Les droits seront affinés après le prototype (plusieurs rôles par personne, droits plus fins par module) ?
2. Un utilisateur bloqué par la BNIA est-il signalé aux ADMIN de ses opérateurs ? La BNIA doit-elle saisir un motif ?

### 2.5 Principes des rôles et des droits

Le prototype retient un système de droits volontairement simple.

1. **Lecture.** Tout rattachement ACTIF consulte tout ce qui concerne son opérateur, et seulement lui.
2. **Gestion par module.** L'écriture se décide module par module, avec un seul niveau : qui gère un module peut tout y faire (créer, modifier, supprimer). Deux modules existent à ce stade : les contacts, fiches professionnelles comprises, et les accès (invitations, rôle, suspension, retrait).
3. **Rôles.** Un rôle est un nom donné à un ensemble de modules gérés. Un rattachement porte un seul rôle, parmi ceux du tableau ci-dessous. Ils sont définis par l'application, pas par les opérateurs.
4. **Évolution.** Un nouveau module s'ajoute comme une colonne du tableau, sans changer le mécanisme.
5. **Attribution.** Seul un ADMIN attribue ou retire un rôle dans son opérateur. Un opérateur garde toujours au moins un ADMIN actif, y compris la BNIA.
6. **Sans rôle.** Certaines actions ne dépendent d'aucun rôle : gérer son profil, son email, son opérateur actif et sa propre fiche, tant que le rattachement est ACTIF.
7. **BNIA.** Les droits de la BNIA sur les autres opérateurs sont des droits transversaux. Ils ne peuvent être attribués que dans l'opérateur BNIA.
8. **Évaluation.** Les droits sont évalués à chaque requête. Ils ne figurent jamais dans le jeton d'authentification.

**Rôles et modules**

| Rôle | Contacts | Accès |
| --- | --- | --- |
| OBSERVATEUR | lecture | lecture |
| AGENT | gestion | lecture |
| ADMIN | gestion | gestion |
