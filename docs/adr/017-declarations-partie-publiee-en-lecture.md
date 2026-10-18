# ADR-017 : `declarations`, partie publiée en lecture

- Statut : accepté ; amendé le 2026-10-14 (factures, ADR-021)
- Date : 2026-10-13
- Étend : l'exception des ADR-008 et ADR-012 (partie publiée en lecture), réservée jusqu'ici à
  `referentiels` et `operateurs`
- Spec : `docs/specs/05-declarations/spec.md` (décisions A-DEC-1 et A-DEC-4)

## Contexte
ADR-002 interdit tout import Java d'un contexte à l'autre, sauf les parties publiées nommées de
`referentiels` (ADR-008) et de `operateurs` (ADR-012). L'ADR-013 place les types d'événements dans la
partie publiée du contexte dont l'autre dépend.

`declarations` devient à son tour un contexte dont plusieurs autres dépendent (analyse de la spec 05,
§4) :
- `facturation` lit la **cotisation d'une DSM** (détail par ligne de cotisation) pour créer la facture ;
- la fiche opérateur, composition de lecture, lit la **dernière DSM** et le **stock déclaré** d'un
  opérateur, et `statistiques` étendra ces lectures ;
- `contratsarmagnac` et `distillation` l'informent par événements (`ContratValide`, `ContratModifie`,
  `RetiraisonEffectuee`, `FinTravauxRecue`) qui alimentent le stock estimé, et le contrat y lira le
  bilan prévisionnel du vendeur ;
- `notifications` écoute ses événements (`DsmEnvoyee`, `ComplementsDemandes`, `DsmValidee`,
  `DeclarationEnRetard`).

Reste un besoin à rebours : l'écran et le listing des DSM affichent « à facturer » ou « facturée », avec
les montants de la facture, alors que `declarations` ne peut pas lire `facturation`, qui dépend de lui.
L'analyse proposait un port défini par `declarations` et implémenté par `facturation`, avec un adapter
par défaut.

## Décision
- **Partie publiée en lecture seule**, sur le modèle de l'ADR-012 :
  - `fr.bnia.declarations.domain.partage` : vues immuables (cotisation d'une DSM et son détail, dernière
    DSM, stock déclaré), `CompteDAgeDsm` (compte d'âge borné à `20+`) et **tous** les types d'événements
    échangés, émis comme reçus (`DsmEnvoyee`, `ComplementsDemandes`, `DsmValidee`,
    `DeclarationEnRetard` ; `ContratValide`, `ContratModifie`, `RetiraisonEffectuee`, `FinTravauxRecue`,
    `FactureEmise`, `AvoirEmis`) ;
  - `fr.bnia.declarations.application.partage` : l'interface `DeclarationsLecture`, implémentée dans
    `declarations`.
  Le reste du contexte est interdit aux autres (ArchUnit). Le contenu ne grandit qu'avec un consommateur
  réel : chaque spec consommatrice l'étend.
- **Facture d'une DSM : projection locale alimentée par événements.** `facturation` publie
  `FactureEmise` et `AvoirEmis` (DSM, numéro, date, HT, TVA, TTC) ; `declarations` les écoute dans la
  transaction de l'émetteur (ADR-013) et en tient une table locale. « À facturer » et « facturée » se
  lisent en SQL ; la TVA indicative d'une DSM non facturée se calcule avec les taux de `referentiels`.
  Tant que `facturation` n'existe pas, la table est vide : toute DSM validée est « à facturer ».
- **Exception nommée.** L'exception des ADR-008 et ADR-012 vaut désormais pour `referentiels`,
  `operateurs` et `declarations`. Les dépendances restent sans cycle : `contratsarmagnac`,
  `distillation`, `facturation`, `notifications` et `statistiques` → `declarations` → `operateurs` →
  `referentiels` → `commun`.

Options écartées :
- un **port défini par `declarations` et implémenté par `facturation`**, avec un adapter par défaut :
  forme nouvelle pour un seul besoin, et le listing devrait appeler le port ligne par ligne ou par page ;
- une **composition par le front** (l'écran appelle aussi l'API de `facturation`) : le filtre
  « à facturer » devient impossible côté serveur, et la règle sort du domaine ;
- des **ports de lecture côté consommateurs** (un port et un adapter de passe-plat dans chaque
  contexte lecteur) : écarté pour les mêmes raisons que dans l'ADR-008.

## Conséquences
- `CLAUDE.md` liste `fr.bnia.declarations.*.partage` parmi les exceptions à l'isolement.
- La règle ArchUnit des parties publiées s'étend à `declarations` (lot 1 de l'implémentation).
- Les montants d'une facture sont dupliqués dans `declarations` ; la facture fait foi, la projection
  ne se corrige que par un nouvel événement (avoir).
- Les types des événements reçus de `contratsarmagnac` et `distillation` sont fixés par la spec 05 (§5.2)
  et confirmés par les specs 06 et 09, qui peuvent les compléter.

## Amendement du 2026-10-14 (spec 08, ADR-021)
- **Sens inversé** : `declarations` dépend désormais de `facturation`, par son infra et le module
  `bnia-facturation-api`. `facturation` ne lit plus `declarations`.
- **Types retirés** de la partie publiée de `declarations` : `FactureEmise` et `AvoirEmis` (publiés
  par `facturation`, dans son API, avec l'`Origine` au lieu de la DSM, et `FactureAbandonnee` en plus) ;
  `CotisationDsmVue` (plus lue : `declarations` envoie une demande de facture et expose ses articles de
  cotisation par `CatalogueArticles`).
- **Projection des factures conservée**, alimentée par les événements de `facturation`, écoutés dans
  `adapter.evenement`. Elle est enrichie de « facturation demandée » et de « facture annulée » (spec 05,
  RG-DEC-34 révisée).
- Dépendances : `contratsarmagnac`, `distillation`, `notifications` et `statistiques` → `declarations` ;
  `declarations-infra` → `facturation-api` ; `facturation` → `operateurs` → `referentiels` → `commun`.
  Aucun cycle.
