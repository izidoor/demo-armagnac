# ADR-009 : Noyau commun du domaine

- Statut : accepté
- Date : 2026-10-06
- Spec : `docs/specs/01-referentiels/spec.md` (décision A-REF-4)

## Contexte
Certains concepts du glossaire sont des **règles** partagées par plusieurs contextes, et non des
données. Le compte d'âge en est le premier exemple : il se déduit d'un millésime et d'une date, ses
valeurs `00` et `0` sont distinctes et ordonnées (`00` < `0` < `1`), et il sert à l'âge minimum
d'un pays (`referentiels`), aux lignes de DSM (`declarations`) et aux ventes export
(`exportation`). Le socle (étape 0) a créé `fr.bnia.commun`, sans en fixer le contenu.

## Décision
- `fr.bnia.commun.domain` accueille les objets-valeurs transverses du glossaire qui remplissent trois
  critères :
  - c'est une **règle ou un concept**, pas une donnée administrée par le BNIA (celles-ci vont dans
    `referentiels`, ADR-008) ;
  - au moins **deux contextes** en ont besoin ;
  - la règle est **stable** et définie par le métier, pas par un contexte.
- Premier occupant : `CompteDAge`, avec sa validation, son ordre et sa mention commerciale.
  Candidats, ajoutés au fil des besoins : `Millesime`, `Campagne`, `MentionCommerciale`,
  `VolumeAlcoolPur`.
- Java pur, comme tout package `domain`. Module `bnia-commun` (ADR-010). `commun` ne dépend d'aucun contexte (règle ArchUnit existante).

Options écartées :
- placer ces concepts dans `referentiels` : ce contexte deviendrait propriétaire de règles qu'il
  n'administre pas, et mêlerait données et calculs ;
- une version par contexte : la règle `00` < `0` < `1` et ses bornes seraient dupliquées et
  divergeraient.

## Conséquences
- `commun.domain` accueille aussi les exceptions métier communes (`RegleEnfreinte`, avec l'identifiant
  de la règle, `ConflitUnicite`, `ElementIntrouvable`), traduites en `ProblemDetail` par
  `commun.adapter.rest` (ajout du 2026-10-06, implémentation de `referentiels`). Elles ne sont pas des
  concepts du glossaire : ce sont les seules classes techniques admises dans ce package.
- Deux zones partagées, de nature différente :

  | | `commun.domain` | `referentiels` (partie publiée) |
  |---|---|---|
  | Contenu | Concepts et règles | Données administrées |
  | Évolution | Par le code, rarement | Par le BNIA, à l'écran |
  | Accès | Import libre | Lecture seule (ADR-008) |
- Toute modification de `commun.domain` touche plusieurs contextes. Ses objets sont couverts par
  leurs propres tests, qui portent l'identifiant de la règle d'origine (spec et glossaire).
- Un concept n'entre dans `commun` que si un deuxième contexte en a besoin de façon avérée ou certaine
  (spec écrite, CDC explicite), pas par anticipation.
