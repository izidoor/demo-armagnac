# ADR-002 : Structure backend Maven multi-modules, hexagonale

- Statut : accepté
- Date : 2026-10-05

## Contexte
Le CDC (§8) demande une architecture lisible et reprenable par une autre agence. Le projet
suit l'architecture hexagonale et le DDD tactique.

## Décision
Un module Maven par couche : `bnia-domain`, `bnia-application`, `bnia-adapter-rest`,
`bnia-adapter-persistence`, `bnia-adapter-integration`, et `bnia-boot` (point d'entrée, câblage,
configuration, tous les tests d'intégration et ArchUnit). Les bounded contexts sont des packages
`fr.bnia.<contexte>.<couche>`, non des modules.

- Maven enforcer interdit Spring, Jakarta Persistence et Jackson dans `domain` et `application`.
- ArchUnit (dans `bnia-boot`) interdit les dépendances entre contextes et entre adapters.
- Les cas d'usage portent un `@UseCase` maison. Les transactions sont posées dans `bnia-boot`.

## Conséquences
- Le sens des dépendances est garanti à la compilation, pas par convention.
- Un module par couche et par contexte aurait donné 15 à 20 poms : écarté pour le prototype.
- Extraire un contexte en service séparé reste possible.
