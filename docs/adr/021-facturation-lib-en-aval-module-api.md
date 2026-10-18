# ADR-021 : `facturation`, lib en aval : module d'API et hexagonale stricte

- Statut : accepté ; amendé le 2026-10-17 (second cas : `messagerie`, ADR-023)
- Date : 2026-10-14
- Amende : ADR-014 (troisième sous-module `-api`), ADR-017 (types de facture retirés de `declarations`)
- Spec : `docs/specs/08-facturation/spec.md` (décisions A-FAC-1, A-FAC-2, A-FAC-3) ; schémas :
  `docs/specs/08-facturation/architecture.md`

## Contexte
L'analyse de `facturation` (2026-10-14) en fait une **lib en aval** : les contextes métier
(`declarations`, puis `distillation` et `boutique`), appelés **sources**, lui demandent des factures,
et elle ne connaît aucun d'eux. Une source a donc trois besoins envers `facturation` :
- **appeler** un cas d'usage : demander des factures (`DemandeFactures`) ;
- **implémenter** un port : exposer ses articles, avec leur prix et leur TVA à une date
  (`CatalogueArticles`) ;
- **écouter** des événements : `FactureEmise`, `FactureAbandonnee`, `AvoirEmis`.

`statistiques` lit en plus les ventes. Les parties publiées existantes (ADR-008, 012, 017) ne se lisent
qu'en lecture. Ce sont des paquets `partage` dans le core, que le core des autres contextes importe ;
seul ArchUnit en garde la frontière.

`facturation` dépend elle-même de l'hôte pour deux choses : les droits d'un acteur et l'identité d'un
client (un opérateur).

Principe retenu le 2026-10-14 pour ce contexte : **architecture hexagonale stricte et modularisation
Maven**.

## Décision
- **Module d'API `bnia-facturation-api`**, troisième sous-module de `bnia-facturation`, en Java pur. Il
  contient :
  - les interfaces `DemandeFactures` (appelée), `CatalogueArticles` (implémentée par les sources) et
    `FacturationLecture` (lue) ;
  - les types échangés (`DemandeFacture`, `Origine`, `ClientRef`, `ConditionPaiement`, `CategorieTva`,
    `Article`, `LigneVendue`…) ;
  - les événements émis.

  C'est le seul module de `facturation` dont dépendent les autres contextes.
  `bnia-facturation-core` en dépend et implémente `DemandeFactures` et `FacturationLecture`.
- **Le core d'une source ne dépend pas de l'API.** Il définit son propre port, dans ses propres termes
  (`Facturation` pour `declarations`), que son infra implémente par un adapter sur l'API
  (`adapter.integration`). L'infra de la source implémente aussi `CatalogueArticles`, en appelant un cas
  d'usage de son core, et écoute les événements (`adapter.evenement`).
- **Catalogues injectés** : le core de `facturation` reçoit la liste de tous les `CatalogueArticles`,
  indexée par `source()`. Deux catalogues de même source font échouer le démarrage. Une nouvelle source
  n'a qu'à déclarer son adapter.
- **Ports de l'hôte** : `DroitsFacturation` et `Clients` sont définis dans le core de `facturation` et
  implémentés dans `bnia-facturation-infra` (`adapter.integration`) sur la partie publiée d'`operateurs`.
  L'intégration vit dans le contexte qui définit le port (ADR-010). Le nom `Habilitations` est écarté :
  il désigne déjà un cas d'usage d'`operateurs`.
- **Dépendances** : `declarations-infra` → `facturation-api` ← `facturation-core` ← `facturation-infra`
  → `operateurs-core`. `facturation` ne connaît aucune source. Il n'y a aucun cycle, `declarations-core`
  ne dépendant de rien de `facturation`.

Options écartées :
- **Le core de la source appelle l'API directement**, sans port propre : moins de code, mais le core
  dépend d'un autre contexte, et l'hexagonale n'est plus stricte ;
- **des paquets `partage` dans le core** (forme d'ADR-012 et 017) : pas de module de plus, mais tout le
  core de `facturation` serait sur le classpath des sources, et seul ArchUnit en garderait la frontière ;
- **des ports côté sources, implémentés par `facturation`** : la lib dépendrait de chacune de ses
  sources, ce qui contredit l'analyse ;
- pour les catalogues, **un registre ou un composite dans `bnia-boot`** : une ligne de câblage par
  source, ou une logique de répartition hors du contexte ;
- pour les ports de l'hôte, **des adapters dans `bnia-boot` ou dans un module hôte dédié** : la lib
  serait pure, mais `bnia-boot` porterait de l'intégration, ou il faudrait un module de plus.

## Conséquences
- ADR-014 est amendé : un contexte qui publie une API a un sous-module `-api`, et l'infra d'un contexte
  peut dépendre du `-api` d'un autre.
- ADR-017 est amendé : `FactureEmise`, `AvoirEmis` et `CotisationDsmVue` sortent de la partie publiée de
  `declarations`.
- `CLAUDE.md` cite le sous-module `-api` et ajoute `bnia-facturation-api` aux exceptions à l'isolement :
  importé par l'infra des sources seulement.
- ArchUnit vérifie qu'aucun core n'importe `fr.bnia.facturation.api`, hors `facturation` elle-même, et
  que rien d'autre de `facturation` n'est importé ailleurs.
- `distillation` et `boutique` suivront la même forme : un port propre, un adapter sur l'API, un
  `CatalogueArticles`.
- Les parties publiées de `referentiels`, `operateurs` et `declarations` ne suivent pas ce principe.
  Les aligner reste une interrogation ouverte (`contextes.md` §5).

## Amendement du 2026-10-17 (spec 15, ADR-023)
- La lib en aval devient une forme générale : `messagerie` publie `bnia-messagerie-api` (`Conversations`
  appelée, `ObjetsConversables` implémentée par les contextes métier), sur le même modèle : port propre dans
  le core de l'appelant, adapter dans son infra, implémentations injectées et indexées, ports de l'hôte
  implémentés dans l'infra de la lib.
