# ADR-023 : `messagerie`, seconde lib en aval

- Statut : accepté
- Date : 2026-10-17
- Amende : ADR-021 (la forme de lib en aval vaut au-delà de `facturation`)
- Spec : `docs/specs/15-messagerie/spec.md` (décisions A-MSG-1, A-MSG-2, A-MSG-3, A-MSG-4, A-MSG-9)

## Contexte
L'analyse de `messagerie` (2026-10-17, contexte ajouté pour la démonstration, absent du CDC) en fait une
**lib en aval** : elle porte les conversations entre le BNIA et un opérateur, sur un objet métier (DSM,
facture émise, puis contrat et demande de certificat) ou sur un sujet libre, et ne connaît aucun contexte
métier. Un contexte métier a deux besoins envers elle :
- **appeler** : déposer le motif d'une demande de compléments dans la conversation de la DSM, supprimer la
  conversation d'un objet supprimé, connaître ses messages non lus ;
- **décrire** ses objets : dire si un objet existe et se prête à une conversation, son opérateur et son
  libellé (« DSM octobre 2026 »).

`messagerie` dépend elle-même de l'hôte pour les droits : le côté d'un acteur (BNIA ou opérateur), la
lecture et la gestion du module de chaque type d'objet, le rattachement actif de l'auteur d'une
conversation libre.

Les commentaires de DSM existants (`Commentaire` de `declarations`) mêlaient échanges et notes internes du
BNIA ; l'analyse les sépare.

## Décision
- **Module d'API `bnia-messagerie-api`**, en Java pur, sur le modèle de `bnia-facturation-api` (ADR-021) :
  - `Conversations`, implémentée par le core de `messagerie`, appelée par les contextes métier ;
  - `ObjetsConversables`, implémentée par l'infra de chaque contexte métier (`type()`, `decrire(identifiant)`) ;
  - les types échangés : `TypeObjet` (liste fermée, un type par objet conversable), `ObjetRef`,
    `DescriptionObjet`, `Auteur`, `Acteur`.
- **Le core d'un contexte métier ne dépend pas de l'API** : il a son propre port (`Messagerie` pour
  `declarations`), implémenté dans son infra (`adapter.integration`) sur `Conversations`. Son infra
  implémente `ObjetsConversables` en appelant un cas d'usage de son core.
- **Descriptions injectées** : le core de `messagerie` reçoit la liste des `ObjetsConversables`, indexée par
  `type()` ; deux implémentations d'un même type font échouer le démarrage.
- **Port de l'hôte `DroitsMessagerie`**, défini dans le core, implémenté dans `bnia-messagerie-infra` sur la
  partie publiée d'`operateurs` (ADR-012). L'adapter seul sait quel module porte chaque type (DSM :
  Déclarations ; facture : Facturation) ; le core garde la règle d'écriture (gestion du module, ou sa
  lecture pour une facture).
- **Appels synchrones**, dans la transaction du contexte métier, au lieu d'événements : un écouteur de
  `ComplementsDemandes` ferait dépendre `messagerie` de `declarations`.
- **Notes internes dans chaque contexte métier** (`NoteInterne` de `declarations`), pas dans `messagerie` :
  une note n'est pas un échange et n'a qu'un côté.
- **Dépendances** : `declarations-infra`, `facturation-infra` → `messagerie-api` ← `messagerie-core` ←
  `messagerie-infra` → `operateurs-core`. Aucun cycle.

Options écartées :
- **le core du contexte métier appelle l'API**, sans port propre : moins de code, hexagonale moins stricte ;
- **des paquets `partage` dans le core de `messagerie`** : frontière gardée par ArchUnit seul ;
- **des ports côté contextes métier, implémentés par `messagerie`**, ou des droits rendus par
  `ObjetsConversables` : la lib dépendrait de chaque contexte, ou chacun réécrirait les règles de côté ;
- **les notes internes dans `messagerie`** (conversation côté BNIA seul), ou une lib commune de notes : écarté
  par l'analyse, puis un module d'API de plus pour une table et trois règles.

## Conséquences
- ADR-021 est amendé : la lib en aval est une forme générale, dont `messagerie` est le second cas.
- `CLAUDE.md` cite `bnia-messagerie-api` parmi les exceptions à l'isolement, importé par l'infra des
  contextes métier seulement.
- ArchUnit vérifie qu'aucun core n'importe `fr.bnia.messagerie.api`, hors `messagerie` elle-même, et que rien
  d'autre de `messagerie` n'est importé ailleurs.
- Chaque nouveau contexte conversable (`contratsarmagnac`, `exportation`) ajoute un `TypeObjet`, un adapter
  `ObjetsConversables` et, s'il appelle la messagerie, un port propre ; `DroitsMessagerie` mappe son module.
- `declarations` perd `Commentaire` et gagne `NoteInterne` (spec 05, révisée par les lots 1 et 2 de la
  spec 15).
