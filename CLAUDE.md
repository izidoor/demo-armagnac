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

## Architecture (Maven multi-modules, vérifiée par enforcer et ArchUnit)
- `bnia-domain` : Java pur. Aucun framework ni annotation de persistance.
- `bnia-application` : cas d'usage (`@UseCase` maison) et ports. Dépend de `domain` uniquement.
- `bnia-adapter-rest` | `-persistence` | `-integration` : dépendent de `application`, jamais entre eux.
- `bnia-boot` : seul module avec `@SpringBootApplication`, `application*.yml`, câblage,
  transactions, **tous les `*IT`** et les tests ArchUnit.
- Un package par bounded context : `fr.bnia.<contexte>.<couche>`. Pas d'import direct entre contextes :
  passer par des ports ou des événements. Deux exceptions : `fr.bnia.commun.domain`, noyau de concepts
  transverses (ADR-009), et la partie publiée en lecture de `referentiels`
  (`fr.bnia.referentiels.*.partage`, ADR-008). Référence entre contextes par code métier.

## Persistance
- Écriture : Spring Data JDBC, un repository par racine d'agrégat. Les classes de persistance
  vivent dans `bnia-adapter-persistence` et sont mappées vers le domaine (jamais d'annotation dans `domain`).
- Lecture (listings, statistiques, consolidations) : `JdbcClient` en SQL direct, via des ports de
  lecture définis dans `application` qui retournent des modèles de lecture, pas des agrégats.
- **Une seule migration Flyway** : `V1__schema.sql`, modifiée sur place à chaque incrément.
  Ne jamais créer `V2`. Après modification : `./dev-reset.sh` (recrée la base de dev).
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
- Dev : `./mvnw -pl bnia-boot spring-boot:run` (profil `dev`, démarre `compose.yaml` au besoin)

Front, depuis `frontend/` :
- Client d'API, après `./mvnw verify` : `pnpm gen:api`
- Contrôles : `pnpm lint`, `pnpm typecheck`, `pnpm format:check`, `pnpm build`
- Dev : `pnpm dev` (proxy `/api` vers `localhost:8080`)

Racine :
- Reset de la base de dev : `./dev-reset.sh`
