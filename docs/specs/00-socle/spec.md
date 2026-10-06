# 00 : socle technique

> Étape 0 de `docs/contextes.md`. Aucune règle de gestion : ce socle porte l'architecture
> (ADR-001 à 007) et une sonde technique qui traverse toute la chaîne.

## Périmètre
- Reactor Maven `backend/` : `bnia-domain`, `bnia-application`, `bnia-adapter-rest`,
  `bnia-adapter-persistence`, `bnia-adapter-integration`, `bnia-boot`. Remplacé le 2026-10-06 par
  un module par contexte (`bnia-commun`, `bnia-systeme`, `bnia-<contexte>`, `bnia-boot`, ADR-010).
- PostgreSQL 18 : `backend/compose.yaml` en dev, Testcontainers dans les `*IT`.
- Front `frontend/` : une SPA (base ShadcnAdmin épurée), deux espaces : `/bo` (back-office BNIA)
  et `/portail` (portail opérateur). La connexion est factice jusqu'au contexte `acces`.
- Contrat OpenAPI généré (ADR-007) et client TypeScript généré.

## Conventions
- Packages : `fr.bnia.<contexte>.domain`, `fr.bnia.<contexte>.application`,
  `fr.bnia.<contexte>.adapter.<rest|persistence|integration>`.
- `fr.bnia.commun` : noyau partagé, importable par tous les contextes, sans dépendance vers eux.
- `fr.bnia.boot` : câblage (dans `bnia-boot`).
- Un cas d'usage est une classe `@UseCase` de `application`, sans annotation Spring.
  `UseCaseConfiguration` en fait un bean et exécute ses méthodes publiques dans une transaction.
- Routes REST : préfixe `/api/<contexte>`. `operationId` explicite sur chaque opération.

## Garde-fous
| Règle | Vérifiée par |
|---|---|
| `domain` et `application` sans Spring, Jakarta, Jackson, Hibernate | enforcer (`domaine-pur`, `application-sans-framework`) et ArchUnit |
| `domain` sans dépendance interne, `application` ne dépend que de `domain` | enforcer et ArchUnit |
| Aucun adapter ne dépend d'un autre ni de `bnia-boot` | enforcer (`adapters-independants`) et ArchUnit |
| Aucun contexte ne dépend d'un autre ; pas de cycle | ArchUnit (`slices`) |
| `commun` ne dépend d'aucun contexte | ArchUnit |
| `@UseCase` uniquement dans `application` | ArchUnit |
| Une seule `@SpringBootApplication` | ArchUnit |

## Sonde `systeme`
`GET /api/systeme/info` renvoie le nom et la version de l'application, et la version de PostgreSQL.
Elle reste en place comme modèle des contextes suivants et comme contrôle de bon fonctionnement.

| Couche | Classe |
|---|---|
| application | `ConsulterInfoSysteme` (`@UseCase`), port `InfoBaseReader`, modèle `InfoSysteme` |
| persistance | `JdbcInfoBaseReader` (`JdbcClient`) |
| REST | `SystemeController`, `InfoSystemeResponse` |
| front | `InfoSystemeCard`, sur l'accueil des deux espaces |

## Tests
- `ConsulterInfoSystemeTest` (unitaire).
- `ArchitectureTest` (ArchUnit).
- `SystemeIT` (chaîne complète jusqu'à PostgreSQL), `UseCaseConfigurationIT` (cas d'usage
  transactionnel), `OpenApiContratIT` (export du contrat).
