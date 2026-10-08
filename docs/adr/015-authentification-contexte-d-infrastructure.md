# ADR-015 : Authentification, contexte d'infrastructure

- Statut : accepté
- Date : 2026-10-08
- Remplace : le contexte `acces` prévu par `docs/contextes.md` (comptes, rôles BNIA, événements avec `operateurs`)
- Spec : `docs/specs/03-authentification/spec.md` (décisions A-AUT-1 à 3)

## Contexte
Le CDC décrit deux populations (utilisateurs BNIA du back-office, contacts des opérateurs au portail),
des rôles composés par le BNIA et des droits délégués par module (§4.2, §5.1, §5.13). La vision du
2026-10-08 les remplace par un modèle unique : le BNIA est un opérateur, une personne est contact ou
membre d'un opérateur, un membre a un rôle fixe (ADR-016). Tout ce métier est dans `operateurs`, pour
que les transitions contact ↔ membre se fassent dans une seule transaction.

Reste la preuve d'identité : mot de passe, double authentification, blocage après des échecs, mot de
passe oublié, sessions, journal des connexions. Elle doit pouvoir être remplacée (Keycloak, autre
fournisseur) sans toucher au métier, et ne porter ni nom ni droit.

Deux besoins vont en sens contraires : `authentification` doit demander à `operateurs` si un utilisateur
peut se connecter ; `operateurs` doit créer, renommer et supprimer le compte de connexion avec
l'utilisateur, dans sa propre transaction.

## Décision
- **Un contexte `authentification`**, modules `bnia-authentification-core` (Java pur) et
  `bnia-authentification-infra` (Spring Security, Spring Session, envoi des e-mails), schéma
  `authentification`. Il porte le compte de connexion (identifiant = e-mail de l'utilisateur, mot de
  passe haché, blocage temporaire), les codes de double authentification, les réinitialisations, les
  navigateurs de confiance et le journal des connexions. Ni nom, ni droit, ni opérateur.
- **`authentification` dépend de `operateurs`, jamais l'inverse.**
  - Il lit la partie publiée de `operateurs` (ADR-012) : l'utilisateur peut-il se connecter (non bloqué,
    un rattachement actif dans un opérateur actif), l'utilisateur d'un e-mail, l'utilisateur d'une
    invitation acceptée par son jeton.
  - Il écoute trois événements de `operateurs`, synchrones, dans la transaction de l'émetteur (ADR-013) :
    `UtilisateurCree` (identifiant, e-mail), `EmailUtilisateurModifie` (identifiant, nouvel e-mail),
    `UtilisateurSupprime` (identifiant). Leurs types sont dans `fr.bnia.operateurs.domain.partage`. Un
    échec de l'écouteur annule l'action de `operateurs`.
  - Le compte référence l'utilisateur par son identifiant `bigint` (ADR-011), **sans clé étrangère** :
    aucune clé ne traverse les schémas `authentification` et `operateurs`.
- **Le mot de passe ne voyage jamais dans un événement.** À l'acceptation d'une invitation, le compte
  est créé sans mot de passe. Le formulaire d'acceptation fait un second appel à `authentification`,
  avec le même jeton : `authentification` vérifie auprès de `operateurs` que l'invitation a été acceptée
  depuis moins d'une heure, et pose le mot de passe si le compte n'en a pas (RG-AUT-15). Un échec de ce
  second appel se rattrape par « mot de passe oublié ».
- **Session serveur en base** : Spring Session JDBC, tables dans le schéma `authentification`, cookie
  `HttpOnly` et `SameSite=Lax`, protection CSRF de Spring Security. La session ne porte que
  l'identifiant de l'utilisateur ; à chaque requête, `operateurs` est interrogé à nouveau, ce qui rend
  immédiats un blocage par le BNIA, une suspension ou un retrait d'accès.
- **Connexion unique** pour tous les utilisateurs, à une seule adresse ; l'opérateur actif décide
  ensuite de l'espace (back-office pour l'opérateur BNIA, portail sinon).
- **Spring Security seul.** Double authentification par code e-mail pour tous (`000000` en dev et en
  démo), sans TOTP ; e-mails par SMTP, Mailpit en dev.

Options écartées :
- un port publié par `operateurs` et implémenté par `authentification` : pas de compte sans mot de
  passe, mais le mot de passe traverserait `operateurs` ;
- la dépendance inverse (`operateurs` appelle `authentification`) : le métier dépendrait de
  l'infrastructure qu'on veut remplaçable ;
- un lien « définir votre mot de passe » envoyé par e-mail après l'acceptation : deux e-mails, deux
  étapes pour la personne invitée ;
- une session en mémoire : perdue à chaque redémarrage de devtools, et fermer les sessions d'un
  utilisateur (RG-AUT-08) demanderait un registre maison ;
- un jeton JWT : révocation et inactivité à construire ;
- Keycloak : infrastructure de plus et écrans aux couleurs de Keycloak, pour un prototype.

## Conséquences
- Le contexte `acces` de `contextes.md` disparaît ; les ADR et specs qui le citent se lisent
  « `authentification` » pour la connexion, « `operateurs` » pour les droits.
- `spring.flyway.schemas` gagne `authentification` ; `compose.yaml` gagne un Mailpit.
- Remplacer l'authentification revient à réécrire ce contexte : écouter les trois événements et
  interroger la partie publiée de `operateurs`.
- Entre l'acceptation et le premier mot de passe, un compte existe sans mot de passe : il ne permet
  aucune connexion.
- Écarts au CDC assumés : connexion unique au lieu de deux accès (§4.2.3, §5.1.1), double
  authentification par e-mail pour tous au lieu d'imposée aux seuls administrateurs (§4.2.6), pas de TOTP
  (§4.2.7).
