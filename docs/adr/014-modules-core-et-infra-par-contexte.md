# ADR-014 : Un module parent par contexte, sous-modules -core et -infra

- Statut : accepté ; amendé le 2026-10-14 (sous-module `-api`, ADR-021)
- Date : 2026-10-08
- Remplace : la structure en modules de l'ADR-010 (un module par contexte, couches en packages)

## Contexte
ADR-010 met tout le code d'un contexte dans un module `bnia-<contexte>`. Spring est donc dans le
classpath de `domain` et de `application`, et seul ArchUnit, au moment des tests, garantit que ces
couches restent en Java pur.

Le code de `domain` et `application` n'importe déjà rien de Spring : séparer le cœur des adapters ne
demande que des déplacements de fichiers.

## Décision
- **Un module parent par bounded context** : `bnia-<contexte>` (packaging `pom`), qui contient deux
  sous-modules, dans son dossier :
  - `bnia-<contexte>-core` : `fr.bnia.<contexte>.{domain, application}`, **Java pur** : aucune
    dépendance Spring, ni en compilation ni en test (JUnit, AssertJ, Mockito seulement) ;
  - `bnia-<contexte>-infra` : `fr.bnia.<contexte>.adapter.{rest, persistence, integration, evenement}`.
    Dépend de `bnia-<contexte>-core`, de `bnia-commun-infra` et des starters Spring utiles.
- **Même découpe pour le noyau partagé** : `bnia-commun-core` (`commun.domain`, ADR-009, et
  `commun.application` : `@UseCase`) et `bnia-commun-infra` (socle REST : erreurs, OpenAPI,
  `@Ressource`). Idem pour la sonde technique `bnia-systeme`.
- **Dépendances entre contextes** : de `-core` à `-core` (par exemple `bnia-operateurs-core` vers
  `bnia-referentiels-core` pour sa partie publiée, ADR-008). Un `-infra` ne dépend d'aucun autre `-infra`
  que `bnia-commun-infra`.
- **`bnia-boot`** dépend du `-infra` de chaque contexte, qui apporte son `-core`. Le reste d'ADR-010 est
  inchangé : migration Flyway, `*IT` et ArchUnit dans `bnia-boot`, intégrations externes dans le
  contexte qui en définit le port.

Option écartée : un sous-module par couche (domain, application, chaque adapter), écarté par ADR-010
pour son nombre de poms ; deux sous-modules suffisent à sortir Spring du cœur.

## Conséquences
- La pureté de `domain` et `application` est garantie à la compilation : un import Spring dans un
  `-core` ne compile pas. ArchUnit garde les règles internes (domaine sans application, adapters
  indépendants, contextes isolés, parties publiées).
- Chaque nouveau contexte demande trois poms (parent, `-core`, `-infra`), une entrée dans les
  `<modules>` du pom racine, une dépendance de `bnia-boot` vers son `-infra` et un schéma dans
  `spring.flyway.schemas`.

## Amendement du 2026-10-14 (spec 08, ADR-021)
- **Sous-module `-api`** : un contexte que d'autres appellent ou dont ils implémentent un port (au-delà
  d'une lecture) publie un troisième sous-module, `bnia-<contexte>-api`, en Java pur : interfaces,
  types échangés, événements. Son `-core` en dépend. Premier cas : `bnia-facturation-api`.
- **Dépendances** : l'**infra** d'un contexte peut dépendre du `-api` d'un autre, son core jamais
  (hexagonale stricte). Un `-infra` ne dépend toujours d'aucun autre `-infra` que `bnia-commun-infra`.
- Les dépendances de `-core` à `-core` restent celles des parties publiées en lecture (ADR-008, 012,
  017).
