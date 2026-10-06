# ADR-012 : Opérateurs, partie publiée en lecture et agrégats

- Statut : accepté
- Date : 2026-10-06
- Étend : l'exception de l'ADR-008 (partie publiée en lecture), réservée jusqu'ici à `referentiels`
- Spec : `docs/specs/02-operateurs/spec.md` (décisions A-OPE-1 et A-OPE-3)

## Contexte
ADR-002 interdit tout import Java d'un contexte à l'autre : ils communiquent par ports ou par
événements. L'ADR-008 a fait une exception pour `referentiels`, lu par presque tous les contextes, afin
d'éviter des dizaines de ports et d'adapters de passe-plat.

`operateurs` est dans la même situation. Une dizaine de contextes lisent un opérateur ou ses contacts :
`declarations` (type, périodicité, statut déclaratif), `cotisation`, `facturation` (adresse,
destinataire des factures), `exportation` (opérateur par code, expéditeur du certificat), `distillation`
et `contratsvin` (rattachement des flux ODG par identifiant ODG, CVI ou SIRET), `acces` (contacts d'un
e-mail et leurs droits), `notifications`, `statistiques`, `contratsarmagnac` (recherche restreinte au
portail). Certains manipulent aussi ses types : `TypeOperateur` pour griser les colonnes de DSM,
`ModulePortail` pour les droits du portail.

Il faut aussi fixer les agrégats, car ils déterminent ce que la partie publiée expose. Plusieurs règles
traversent l'opérateur et ses contacts : le contact principal et le destinataire des factures sont des
contacts actifs de l'opérateur, l'e-mail d'un contact est unique dans son opérateur, un opérateur ne doit
jamais perdre son dernier administrateur depuis le portail (RG-OPE-22, 23, 28, 30). La présentation
(marques, visites, œnotourisme…) n'a, elle, aucune règle commune avec l'identité, et elle est surtout
modifiée au portail. Avec Spring Data JDBC (ADR-004), enregistrer un agrégat réécrit toutes ses tables
filles.

## Décision
- **Partie publiée en lecture seule.** `operateurs` publie deux packages, les seuls que les autres
  contextes peuvent importer :
  - `fr.bnia.operateurs.domain.partage` : vues immuables (`OperateurVue`, avec `AdresseVue`,
    `ContactVue`, `ResultatRecherche`), listes fermées (`TypeOperateur`, `PeriodiciteDsm`,
    `StatutDeclarationStock`, `NiveauDiffusion`, `ModulePortail`) et types des événements échangés
    (ADR-013). `AdresseVue` et `NiveauDiffusion` ont été ajoutés à l'implémentation (2026-10-06) : la
    facturation fige l'adresse, les statistiques lisent la diffusion des millésimes ;
  - `fr.bnia.operateurs.application.partage` : l'interface `OperateursLecture`, implémentée dans
    `operateurs` (opérateur par identifiant, par code, par identifiant ODG, par CVI ou SIRET ; déclarants
    attendus ; recherche restreinte ; contacts d'un e-mail ; contacts par centre d'intérêt).
- **Exception nommée.** L'exception de l'ADR-008 vaut désormais pour `referentiels` et `operateurs`, et
  pour eux seuls : des contextes lus par presque tous les autres, dont la partie publiée n'expose aucune
  écriture. Tout autre contexte communique par ports et événements.
- **Référence par identifiant technique**, avec clé étrangère vers `operateurs.operateur` (ADR-011).
- **Deux agrégats.**
  - `Operateur` : identité, déclaratif, production (alambics), diffusion des millésimes, et ses
    **contacts**, porteurs de leurs droits au portail. Les règles qui lient opérateur et contacts sont
    garanties par l'agrégat.
  - `PresentationOperateur` : marques, circuits, visites, labels, œnotourisme, médailles ; même
    identifiant que l'opérateur (1 pour 1), modifié sans réécrire la fiche.
  - Le journal des contacts est une table hors agrégat, en ajout seul, alimentée par les cas d'usage.

Options écartées :
- un port dans chaque consommateur : une dizaine de ports et d'adapters de traduction, qui exigeraient
  de toute façon une API publiée par `operateurs`, ou un SQL sur son schéma (couplage caché, écarté par
  l'ADR-008) ;
- une copie locale dans chaque consommateur, tenue à jour par événements : synchronisation, reprise et
  absence de clé étrangère, disproportionnées pour un monolithe prototype ;
- le contact en agrégat séparé : les règles communes seraient vérifiées par les cas d'usage, et non
  garanties par le modèle ;
- un agrégat unique, présentation comprise : une quinzaine de tables réécrites à chaque case cochée, et
  des conflits entre le BNIA et le portail sur la même fiche.

## Conséquences
- Un contexte qui lit les opérateurs déclare la dépendance vers `bnia-operateurs` et une clé étrangère
  vers `operateurs.operateur`.
- La règle ArchUnit des contextes ignore aussi `fr.bnia.operateurs.domain.partage..` et
  `fr.bnia.operateurs.application.partage..` ; une règle interdit aux autres contextes tout autre
  package de `operateurs`. À mettre en place au premier lot de l'implémentation.
- `operateurs` ne dépend que de `commun` et de `referentiels` ; `acces` dépend de `operateurs`, jamais
  l'inverse (ADR-013).
- Changer la partie publiée touche tous les contextes : elle doit rester petite et stable.
- Modifier un contact réenregistre l'agrégat `Operateur`, contacts compris : acceptable pour quelques
  contacts par opérateur.
- Une troisième exception devrait faire l'objet d'un nouvel ADR, avec le même critère.
