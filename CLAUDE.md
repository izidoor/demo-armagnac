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

## Architecture (un module Maven par contexte, couches vérifiées par ArchUnit, ADR-010)
- `bnia-<contexte>` : un module par bounded context. Couches en packages,
  `fr.bnia.<contexte>.{domain, application, adapter.rest, adapter.persistence, adapter.integration, adapter.evenement}` :
  - `domain` : Java pur. Aucun framework ni annotation de persistance.
  - `application` : cas d'usage (`@UseCase` maison) et ports. Dépend de `domain` uniquement.
  - `adapter.*` : dépendent de `application`, jamais entre eux. Une intégration externe vit dans le
    contexte qui en définit le port.
- `bnia-commun` : noyau partagé (concepts transverses, `@UseCase`, socle REST).
- `bnia-boot` : seul module avec `@SpringBootApplication`, `application*.yml`, câblage,
  transactions, migration Flyway, **tous les `*IT`** et les tests ArchUnit. Dépend de tous les contextes.
- Nouveau contexte : un pom, une entrée dans `<modules>` du pom racine, une dépendance dans `bnia-boot`,
  un schéma dans `spring.flyway.schemas`.
- Pas d'import direct entre contextes :
  passer par des ports ou des événements. Trois exceptions : `fr.bnia.commun.domain`, noyau de concepts
  transverses (ADR-009), et les parties publiées en lecture de `referentiels`
  (`fr.bnia.referentiels.*.partage`, ADR-008) et de `operateurs` (`fr.bnia.operateurs.*.partage`,
  ADR-012). Référence entre contextes par identifiant technique, avec clé étrangère (ADR-011).
- Événements entre contextes : Spring, synchrones, traités dans la transaction de l'émetteur ; publiés
  par un port, écoutés dans `adapter.evenement` ; types dans la partie publiée du contexte dont l'autre
  dépend, jamais de cycle (ADR-013).

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
- Dev : `./mvnw -pl bnia-boot spring-boot:run` (profil `dev`, démarre `compose.yaml` au besoin) ;
  avec les données de démonstration : `-Dspring-boot.run.profiles=dev,demo`

Front, depuis `frontend/` :
- Client d'API, après `./mvnw verify` : `pnpm gen:api`
- Contrôles : `pnpm lint`, `pnpm typecheck`, `pnpm format:check`, `pnpm build`
- Dev : `pnpm dev` (proxy `/api` vers `localhost:8080`)

Racine :
- Remise à zéro volontaire de la base de dev : `./dev-reset.sh` (automatique si V1 a changé)
