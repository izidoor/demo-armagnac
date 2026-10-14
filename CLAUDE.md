# BNIA : plateforme métier (back-office + portail opérateur)

Prototype pour appel d'offres de l'interprofession de l'armagnac.

## Sources de vérité
- Exigences : `docs/source/cdc/` (cahier des charges converti en Markdown). Ne jamais lire les PDF.
- Reprise de données : `docs/source/agi/`.
- Les marqueurs `<!-- p.N -->` renvoient aux pages du PDF : les citer dans les specs.
- Specs de travail : `docs/specs/NN-nom/spec.md`. Vocabulaire : `docs/glossaire.md`.
- Décisions d'architecture : `docs/adr/`. Les respecter ; en cas de besoin de s'en écarter, proposer un nouvel ADR.
- Contradiction ou ambiguïté dans le CDC : ne pas trancher seul. La soumettre avec des alternatives, puis
  la consigner dans la spec du contexte concerné (hypothèses `H-xxx-n`, questions `Q-xxx-n`).

## Langue
- Vocabulaire du domaine en français, sans accents dans les identifiants
  (`Operateur`, `DeclarationStocksMouvements`, `CompteDAge`, `Cotisation`).
- Termes techniques en anglais (`Repository`, `Adapter`, `UseCase`, `Controller`).
- Les termes du glossaire ne sont jamais traduits ni renommés.

## Stack
- Backend : Java 25, Spring Boot 4, Maven, configuration en `application*.yml`.
- Base : PostgreSQL. Dev : Docker Compose. Tests d'intégration : Testcontainers.
- Frontend : React, TanStack (Router/Query), Tailwind, shadcn/ui (base ShadcnAdmin).
- Contrat d'API : généré depuis les controllers (springdoc), exporté vers `docs/api/openapi.json`
  par `OpenApiContratIT` ; client TypeScript généré depuis ce fichier (ADR-007).

## Architecture (un module parent par contexte, sous-modules -core et -infra, ADR-014 ; hexagonale stricte)
- `bnia-<contexte>` : module parent (packaging `pom`) d'un bounded context, avec deux sous-modules :
  - `bnia-<contexte>-core` : `fr.bnia.<contexte>.{domain, application}`, **Java pur** (aucune dépendance Spring) :
    - `domain` : aucun framework ni annotation de persistance.
    - `application` : cas d'usage (`@UseCase` maison) et ports. Dépend de `domain` uniquement.
  - `bnia-<contexte>-infra` : `fr.bnia.<contexte>.adapter.{rest, persistence, integration, evenement,
    planification, document}`. Dépend de son `-core` et de `bnia-commun-infra`. Les adapters dépendent de
    `application`, jamais entre eux. Une intégration externe vit dans le contexte qui en définit le port.
    `planification` : composants `@Scheduled` qui n'appellent qu'un cas d'usage, horloge injectée,
    idempotence en base (ADR-019). `document` : PDF (OpenHTMLtoPDF, gabarits Thymeleaf) et Excel
    (`fastexcel`), en écriture comme en lecture (ADR-020).
- `bnia-<contexte>-api` (ADR-021, amendement d'ADR-014) : pour un contexte que d'autres appellent ou dont
  ils implémentent un port (premier cas : `bnia-facturation-api`). Java pur : interfaces, types échangés,
  événements ; son `-core` en dépend. Seule l'**infra** d'un autre contexte en dépend, jamais son core :
  le core a son propre port, implémenté par un adapter sur l'API.
- `bnia-commun` : noyau partagé. `bnia-commun-core` : concepts transverses (ADR-009, dont
  `VolumeAlcoolPur` et `DocumentDepose`), `@UseCase`, ports `StockageDocuments` et `Transactions` (nouvelle
  transaction, pour les traitements par lot, ADR-022) ; `bnia-commun-infra` : socle
  REST (erreurs, OpenAPI, `@Ressource`) et stockage des documents en base (schéma `documents`, ADR-018).
- `bnia-boot` : seul module avec `@SpringBootApplication`, `application*.yml`, câblage,
  transactions, migration Flyway, **tous les `*IT`** et les tests ArchUnit. Dépend du `-infra` de chaque contexte.
- Nouveau contexte : trois poms (parent, `-core`, `-infra`), une entrée dans `<modules>` du pom racine,
  une dépendance de `bnia-boot` vers le `-infra`, un schéma dans `spring.flyway.schemas`.
- Pas d'import direct entre contextes :
  passer par des ports ou des événements. Quatre exceptions : `fr.bnia.commun.domain`, noyau de concepts
  transverses (ADR-009), et les parties publiées en lecture de `referentiels`
  (`fr.bnia.referentiels.*.partage`, ADR-008), de `operateurs` (`fr.bnia.operateurs.*.partage`,
  ADR-012) et de `declarations` (`fr.bnia.declarations.*.partage`, ADR-017). S'y ajoute le module
  `bnia-facturation-api` (ADR-021), importé par l'infra des sources seulement. Référence entre contextes par identifiant technique `bigint`, avec clé étrangère (ADR-011).
  Exceptions sans clé étrangère : l'auteur d'une action (identifiant de l'utilisateur et copie de son nom,
  l'utilisateur pouvant être supprimé), le compte de connexion de `authentification` (ADR-011 amendé),
  l'origine et le client d'une facture (`Origine`, `ClientRef`, opaques ; ADR-011 amendé le 2026-10-14).
- Événements entre contextes : Spring, synchrones, traités dans la transaction de l'émetteur ; publiés
  par un port, écoutés dans `adapter.evenement` ; types dans la partie publiée (ou le module `-api`) du
  contexte dont l'autre dépend, jamais de cycle (ADR-013). Jamais de secret (mot de passe) dans un événement.
- Personnes et droits (ADR-016) : dans `operateurs`. Rôles fixes `OBSERVATEUR`, `AGENT`, `ADMIN`, un par
  rattachement ; modules lus ou gérés par rôle ; le BNIA est un opérateur (type `BNIA`) dont les rôles
  valent pour tous les opérateurs. **Les droits se vérifient dans les cas d'usage** : le controller passe
  l'`Acteur`, le cas d'usage obtient ses `Droits` sur l'opérateur concerné, et chaque cas d'usage a un
  test de refus. **Chaque nouveau contexte déclare son module** dans le tableau rôles × modules (spec 02
  §3.2.7).
- `authentification` (ADR-015) : preuve d'identité, sans métier ni droit ; dépend de `operateurs` (partie
  publiée, événements `UtilisateurCree`, `EmailUtilisateurModifie`, `UtilisateurSupprime`). Session
  serveur Spring Session JDBC ; une seule connexion pour tous, l'opérateur actif décide de l'espace
  (back-office pour l'opérateur BNIA, portail sinon).

## Persistance
- Écriture : Spring Data JDBC, un repository par racine d'agrégat. Les classes de persistance
  vivent dans `adapter.persistence` du contexte et sont mappées vers le domaine (jamais d'annotation dans `domain`).
- Lecture (listings, statistiques, consolidations) : `JdbcClient` en SQL direct, via des ports de
  lecture définis dans `application` qui retournent des modèles de lecture, pas des agrégats.
- **Une seule migration Flyway** : `V1__schema.sql`, modifiée sur place à chaque incrément.
  Ne jamais créer `V2`. En profil `dev`, la base est recréée au démarrage si V1 a changé.
  Un schéma PostgreSQL par contexte, à ajouter à `spring.flyway.schemas` (`application.yml`).
- Les données de démonstration ne sont pas dans la migration (profil `demo`).

## Méthode de travail
- Une feature par session. Lire la spec, proposer un plan (mode plan), puis coder.
- Ordre : tests de domaine, cas d'usage, adapters, front.
- Chaque règle de gestion de la spec (`RG-xxx-nn`) a au moins un test portant son identifiant.
- Définition de « terminé » : build vert, tests, ArchUnit, lint, typecheck front, spec à jour.

## Commandes
Backend, depuis `backend/` :
- Tests rapides : `./mvnw -q test`
- Tout, ITs inclus (Docker requis) : `./mvnw -q verify` (réexporte aussi `docs/api/openapi.json`)
- Dev : `./mvnw -pl bnia-boot spring-boot:run` (profil `dev`, démarre `compose.yaml` au besoin :
  PostgreSQL et Mailpit, boîte des e-mails sur http://localhost:8025) ; avec les données de
  démonstration : `-Dspring-boot.run.profiles=dev,demo` (comptes et code `000000` : `docs/demo.md`)

Front, depuis `frontend/` :
- Client d'API, après `./mvnw verify` : `pnpm gen:api`
- Contrôles : `pnpm lint`, `pnpm typecheck`, `pnpm format:check`, `pnpm build`
- Dev : `pnpm dev` (proxy `/api` vers `localhost:8080`)

Worktree (`../bnia-worktrees/<contexte>`), à côté de l'instance du dépôt principal : `./worktree-env.sh`
à sa racine, une fois, lui donne ses propres ports (back, front, base, Mailpit) dans `backend/.env` et
`frontend/.env.local`, ignorés par git et lus par Docker Compose, Spring Boot (profil `dev`) et Vite.
Ensuite, lancement habituel (IntelliJ ou `pnpm dev`), sans variable à passer.

Racine :
- Remise à zéro volontaire de la base de dev : `./dev-reset.sh` (automatique si V1 a changé)
- Ports propres à un worktree : `./worktree-env.sh`
