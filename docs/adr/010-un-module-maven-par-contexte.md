# ADR-010 : Un module Maven par contexte, couches en packages

- Statut : accepté
- Date : 2026-10-06
- Remplace : la structure en modules de l'ADR-002 (un module par couche)

## Contexte
ADR-002 a créé un module Maven par couche technique (`bnia-domain`, `bnia-application`,
`bnia-adapter-rest`, `-persistence`, `-integration`, `bnia-boot`), les contextes étant des packages.
Après le premier contexte métier, cette organisation disperse le code d'un contexte dans cinq modules,
et seul ArchUnit, au moment des tests, empêche un contexte d'en importer un autre.

Le projet est un prototype : la structure doit rester légère.

## Décision
- **Un module par bounded context** : `bnia-<contexte>` (`bnia-referentiels`, `bnia-operateurs`…),
  plus `bnia-commun` (noyau partagé, ADR-009 : concepts transverses, `@UseCase`, socle REST) et
  `bnia-boot` (assemblage). La sonde technique `systeme` a son module, `bnia-systeme`.
- **Les couches restent des packages** dans le module du contexte :
  `fr.bnia.<contexte>.{domain, application, adapter.rest, adapter.persistence, adapter.integration}`.
- **Pas de module d'intégration global** : l'adapter d'une intégration (Otentik, Zeendoc, ODG,
  e-mail…) vit dans le package `adapter.integration` du contexte qui en définit le port.
- **Dépendances entre contextes déclarées dans les poms.** Un contexte ne dépend que de
  `bnia-commun`, et de `bnia-referentiels` s'il en lit la partie publiée (ADR-008). `bnia-boot`
  dépend de tous les contextes ; aucun module ne dépend de `bnia-boot` (enforcer).
- **La migration Flyway unique** (ADR-005) vit dans `bnia-boot`, qui assemble les schémas de tous
  les contextes.
- **ArchUnit** (dans `bnia-boot`) garde les règles de couches : domaine en Java pur, application sans
  framework, adapters indépendants, contextes isolés, partie publiée de `referentiels`.

Options écartées :
- un module par couche (ADR-002) : code d'un contexte dispersé, isolement des contextes vérifié
  seulement par les tests ;
- un module par contexte avec un sous-module par couche : la pureté du domaine serait garantie à la
  compilation, mais au prix d'une soixantaine de poms, disproportionné pour un prototype.

## Conséquences
- Le code d'un contexte est dans un seul dossier. Un import entre contextes non déclaré dans le pom
  ne compile pas.
- La pureté du domaine et de l'application n'est plus garantie à la compilation : Spring est dans le
  classpath du module. ArchUnit la vérifie à chaque `./mvnw test`.
- Chaque nouveau contexte demande un pom (dépendances : `bnia-commun`, starters web et JDBC), une
  entrée dans les `<modules>` du pom racine et une dépendance dans `bnia-boot`.
- Extraire un contexte en service séparé reste possible, et plus simple qu'avec ADR-002.
