# ADR-007 : Contrat OpenAPI généré depuis les controllers

- Statut : accepté
- Date : 2026-10-06

## Contexte
Le front consomme l'API par un client TypeScript généré. Deux approches : écrire le contrat
OpenAPI à la main puis générer le code serveur (« contrat d'abord »), ou générer le contrat depuis
les controllers. Le prototype avance vite et seul notre front consomme l'API : un contrat écrit à la
main doublerait chaque évolution et finirait par diverger du code.

## Décision
- Le contrat est généré depuis les controllers (`adapter.rest` de chaque contexte, ADR-010) par springdoc-openapi.
  Les DTO portent `@Schema` (nom public, champs requis, exemples), les opérations `@Operation`
  avec un `operationId` explicite, qui donne le nom des fonctions du client.
- `OpenApiContratIT` (dans `bnia-boot`) exporte le contrat vers `docs/api/openapi.json` à chaque
  `./mvnw verify`. Sortie stable (clés triées, serveur fixé à `/`) : le diff git du fichier montre toute
  évolution de l'API, et le fichier est versionné.
- Le front génère son client depuis ce fichier (`pnpm gen:api`, `@hey-api/openapi-ts` avec le plugin
  TanStack Query). Le code généré, dans `frontend/src/api/generated/`, est versionné.

## Conséquences
- Une seule source de vérité : le code Java. Pas de dérive entre contrat et implémentation.
- La chaîne est manuelle : `./mvnw verify`, puis `pnpm gen:api`. Le typecheck front révèle une
  rupture de contrat après régénération.
- Le contrat dépend de la qualité des annotations : un controller mal décrit donne un contrat pauvre.
- Si un tiers doit un jour consommer l'API, le fichier exporté sert de contrat publié ; passer au
  contrat d'abord se ferait alors par un nouvel ADR.
