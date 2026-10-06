# ADR-008 : Référentiels, partie publiée en lecture et référence par code

- Statut : accepté
- Date : 2026-10-06
- Spec : `docs/specs/01-referentiels/spec.md` (décisions A-REF-1 à 3)

## Contexte
ADR-002 interdit tout import Java d'un contexte à l'autre : ils communiquent par ports ou par
événements. Le contexte `referentiels` fait exception par nature. Il ne contient que des données
administrées par le BNIA (pays, régions, contenances, taux de TVA, neuf listes de valeurs), sans
processus métier, et presque tous les autres contextes les lisent. Avec un port et un adapter par
contexte consommateur, on écrirait des dizaines de classes de passe-plat sans valeur.

Il faut aussi décider comment un contexte pointe vers une valeur de référence, et quelle forme
prennent les listes dans le code.

## Décision
- **Partie publiée en lecture seule.** `referentiels` publie deux packages, et ce sont les seuls que
  les autres contextes peuvent importer :
  - `fr.bnia.referentiels.domain.partage` (`bnia-domain`) : les types de valeur que d'autres domaines
    manipulent (`LangueCertificat`, `NiveauDetailCertificat`) et des vues immuables (`PaysVue`,
    `RegionVue`, `ContenanceVue`, `ValeurVue`, `TauxTvaVue`) ;
  - `fr.bnia.referentiels.application.partage` (`bnia-application`) : l'interface
    `ReferentielsLecture`, implémentée dans `referentiels`.

  Les agrégats, les cas d'usage d'écriture et les adapters restent internes. Une écriture venue
  d'ailleurs, comme la création d'une marque depuis la fiche opérateur, passe par l'écran, qui
  appelle le cas d'usage de `referentiels`.
- **Exception limitée à `referentiels`.** Les autres contextes ne communiquent toujours que par ports et
  événements (ADR-002).
- **Référence par code métier.** Un contexte qui pointe vers une valeur de référence stocke son code
  (`pays_code = 'US'`), immuable par règle de gestion. La contenance n'a pas de code : on stocke son
  volume. Pas d'identifiant technique entre contextes.
- **Une classe par liste.** Chaque liste de valeurs (`Titre`, `Label`, `Marque`…) a sa classe, son
  repository, sa table et son endpoint, même si elles ont aujourd'hui la même forme.

Options écartées :
- un port et un adapter dans chaque consommateur : du passe-plat pour treize référentiels ;
- un adapter consommateur en SQL direct sur les tables de `referentiels` : couplage caché au schéma
  d'un autre contexte, invisible à la compilation ;
- les interfaces de lecture dans `commun` : le noyau commun se chargerait du vocabulaire d'un contexte
  fournisseur ;
- un shared kernel complet : exposerait les écritures et la structure interne des agrégats ;
- un identifiant technique : illisible en base, et la reprise devrait traduire chaque code AGI ;
- un modèle générique de liste typé par une énumération : moins de code, mais des listes qui ne
  peuvent plus diverger.

## Conséquences
- La règle ArchUnit des contextes (`slices` de `fr.bnia.(*)..`) ignore les dépendances vers
  `fr.bnia.referentiels.domain.partage..` et `fr.bnia.referentiels.application.partage..`. Une
  nouvelle règle interdit aux autres contextes tout autre package de `referentiels`.
- Changer la partie publiée touche tous les contextes : elle doit rester petite et stable.
- Le code étant immuable, une erreur de code se corrige en désactivant la valeur et en en créant une
  autre. Aucune valeur n'est supprimée.
- Un consommateur vérifie lui-même, par `ReferentielsLecture`, qu'un code existe et qu'il est actif au
  moment de la saisie.
- Treize classes, tables et endpoints au lieu d'un seul modèle générique : plus de code, en partie
  répétitif, accepté pour la lisibilité et l'évolution indépendante des listes.
