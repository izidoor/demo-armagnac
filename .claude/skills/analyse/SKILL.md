---
name: analyse
description: Mener avec l'utilisateur l'analyse d'un bounded context du projet BNIA avant sa spec : cadrage et frontières, principaux cas d'usage, objets du domaine, flux entre contextes, sans présupposé : tiré du CDC seul, puis confronté à ce qui est déjà implémenté (correspondance ou non), en prenant du recul sur le CDC (le corriger, l'amender, le compléter). Brouillon itératif en Markdown (docs/specs/NN-contexte/analyse.md), paragraphe par paragraphe, puis publication en doc lisible en 5 minutes, illustrée de schémas. À lancer avant la skill specs (« /analyse cotisation », « analysons contratsarmagnac »).
---

# Analyse d'un contexte

Première étape d'un contexte, avant la skill `specs`, qui en reprend les conclusions.
Argument : le nom du contexte (`cotisation`, `declarations`…) ou le thème du CDC à analyser. Le
découpage de `docs/contextes.md` n'est qu'une première lecture : l'analyse peut le redessiner.

## Principes

- **Sans présupposé.** L'analyse part du CDC, pas de ce qui existe. Rôles, acteurs, objets,
  frontières et flux sont tirés des sources, dans les termes des sources. L'existant (ADR, specs,
  code, glossaire, `contextes.md`) n'est **pas un cadre** : c'est l'état actuel, que le nouveau
  contexte peut confirmer, compléter, remplacer ou remettre en cause (ex. : des rôles nouveaux ou
  différents de ceux d'ADR-016, un objet d'`operateurs` à redécouper, un ADR à réviser).
- **Confronter ensuite.** Une fois le CDC compris, vérifier, pour chaque élément de l'étape, si les
  passages du CDC correspondent **ou non** à l'implémentation faite jusqu'ici. Chaque divergence est
  exposée avec ses alternatives (adapter le contexte à l'existant, réviser l'existant, amender le CDC)
  et une recommandation argumentée ; l'utilisateur tranche.
- **Vision avant détail.** L'analyse établit ce qu'est le contexte, ses frontières et la valeur qu'il
  porte. Les attributs, les règles fines, les écrans et la reprise relèvent de la spec.
- **Les 20 % qui font 80 %.** Seuls les **principaux** cas d'usage, ceux qui portent l'essentiel de la
  valeur métier. Le reste est cité d'une ligne en « hors analyse », sans plus.
- **Recul sur le CDC.** Le CDC n'est pas une vérité inaliénable : relever ce qui est contraire au bon
  sens, contradictoire, superflu ou manquant, et proposer de le corriger, l'amender ou le compléter.
  Chaque écart est **proposé**, jamais tranché seul ; l'utilisateur le valide par ses remarques.
- **Ne rien inventer.** Ce qui vient des sources cite sa section et sa page (`§<section> p.<page>`,
  `C.<n>`, `AGI <section> p.<page>`) ; ce qui s'en écarte porte « amendement proposé » puis, validé, « décision du <date> ».
- **Schémas d'abord.** Tout ce qui se dessine se dessine (Mermaid dans le brouillon). Le texte
  accompagne le schéma, il ne le répète pas.
- **Lisible en 5 minutes.** Le document final se relit en 5 minutes au plus : environ 900 mots hors
  schémas, toutes étapes confondues. Tenir ce budget à chaque paragraphe, pas seulement à la fin.
- **Vocabulaire des sources.** Les termes du CDC d'abord ; un écart avec `docs/glossaire.md` (même
  mot, autre sens ; même notion, autre mot) est une divergence à confronter, pas une correction
  silencieuse. Un terme nouveau est signalé comme tel.
- **Pas de code, pas de commit, pas de `git add`.**

## Préparation (sans validation)

0. **Worktree du contexte.** Tout le travail du contexte (analyse, spec, ADR, implémentation) se fait
   dans `../bnia-worktrees/<contexte>` (chemin relatif à la racine du dépôt principal), sur la branche
   `contexte/<contexte>`. `git worktree list` d'abord :
   - worktree déjà présent (reprise) : y entrer ;
   - sinon, depuis la racine du dépôt principal : `git worktree add ../bnia-worktrees/<contexte> -b
     contexte/<contexte> main` (sans `-b` si la branche existe déjà). Le worktree part du dernier commit
     de `main` : ce que l'utilisateur n'a pas commité n'y est pas, le signaler s'il y en a.

   Entrer avec l'outil `EnterWorktree` (paramètre `path`, chemin absolu du worktree), puis vérifier
   `git branch --show-current`. Le dépôt principal reste à l'utilisateur : n'y rien modifier.
1. **Le CDC d'abord.** Lire les sources du thème (`docs/source/cdc/`, `docs/source/agi/`), jamais les
   PDF ; chercher ses concepts dans tout le CDC (grep sur les termes et synonymes) et relever les pages
   (`<!-- p.N -->`). Se faire une compréhension propre : qui agit, sur quoi, pour quelle valeur.
2. **L'existant ensuite**, pour la confrontation seulement : `CLAUDE.md`, `docs/contextes.md`,
   `docs/glossaire.md`, `docs/adr/`, les specs écrites et le code des contextes implémentés
   (`backend/`, `frontend/`) là où le thème les touche (personnes, rôles et droits, référentiels…).
3. Créer `docs/specs/NN-<contexte>/analyse.md` (NN : numéro suivant dans `docs/specs/`, ou celui du
   dossier existant), avec l'en-tête ci-dessous, puis annoncer l'étape 1.

## Le brouillon

```markdown
# Analyse : <contexte>

Statut : en cours (étape N) · Sources : <sections et pages lues> · Doc publié : —

## 1. Cadrage
<paragraphe proposé>

**Confrontation avec l'existant**

| CDC (page) | Existant (ADR, spec, code) | Correspond ? | Proposition |
|---|---|---|---|

<!-- remarques:debut -->
**Remarques :**

<!-- remarques:fin -->

## 2. Cas d'usage
…
```

Une zone de remarques suit **le seul paragraphe en cours**. Les paragraphes validés n'en ont plus.
Chaque étape se termine par sa table de confrontation : seulement les points où le CDC touche
l'existant, « oui » sans commentaire quand tout correspond, une proposition (avec ses alternatives
dans la conversation) quand ce n'est pas le cas. Une étape qui ne touche rien de l'existant l'écrit
d'une ligne.

## Étapes

Quatre étapes, dans l'ordre, **une à la fois**. On ne passe à la suivante qu'après accord explicite de
l'utilisateur (« étape suivante », « go étape 3 »).

### Itération (autant que nécessaire dans chaque étape)

1. Écrire (ou réécrire) le paragraphe de l'étape dans `analyse.md`, suivi d'une zone de remarques
   vide. Dire en deux lignes ce qui a été écrit, ce qui est proposé contre le CDC et ce qui diverge de
   l'existant, puis **s'arrêter**.
2. L'utilisateur écrit ses remarques dans la zone, puis le signale. Relire **tout** le fichier : il a
   pu retoucher le paragraphe lui-même.
3. Prendre en compte chaque remarque ; une remarque qui ouvre une question ou un désaccord se discute
   dans la conversation avant réécriture (une question à la fois, contexte complet, alternatives,
   option recommandée argumentée). Réécrire le paragraphe, **effacer les remarques**, laisser une zone
   vide, retour en 1.
4. Sur validation (« validé », « ok pour ce paragraphe ») : retirer la zone de remarques, mettre à jour
   le statut de l'en-tête, rappeler l'étape suivante et attendre l'accord pour la commencer.

### 1. Cadrage

10 à 15 lignes **au plus** : la raison d'être du contexte, qui s'en sert, ce qu'il contient, ce qu'il
ne contient pas (et où cela va), ses principales dépendances, tels que le CDC les fait apparaître. Puis
un **schéma de contexte** (Mermaid `flowchart`) : le contexte au centre, ses voisins et les tiers
(Otentik, ODG, Zeendoc…), les flèches nommées par ce qui circule. Confrontation : le découpage de
`contextes.md` et les frontières des contextes déjà implémentés.

### 2. Cas d'usage

Les principaux cas d'usage, numérotés `UC-<CTX>-n`, **une phrase chacun** :

> **UC-<CTX>-1** En tant que <rôle>, étant donné que <précondition>, je peux <action>, afin de
> <valeur métier>. (§<section> p.<page>)

Rôles : ceux que le CDC fait agir, nommés comme lui (un service du BNIA, un négociant, un distillateur,
un acteur non connecté, un traitement planifié…), **pas** les rôles techniques existants. Le « afin
de » est gardé quand il dit la valeur métier. Terminer par une ligne « Hors analyse : … » listant les
cas secondaires écartés. Confrontation : les rôles et droits actuels (ADR-016, spec 02 §3.2.7) suffisent-
ils à porter ces acteurs, faut-il les étendre, les remplacer, ou en ajouter ?

### 3. Objets du domaine

Pour les cas d'usage de l'étape 2 : un **diagramme de classes** (Mermaid `classDiagram`) des agrégats,
entités et objets-valeurs tirés du CDC, les racines d'agrégat marquées `<<agrégat>>`, les objets d'autres
contextes en pointillé. Puis un tableau court :

| Objet | Nature | Rôle (UC servis) | Invariants |
|---|---|---|---|

Seulement les attributs qui portent un invariant ou une décision ; la liste exhaustive des champs relève
de la spec. Confrontation : les objets déjà modélisés ailleurs (même notion, autre découpage ; attribut
manquant ; invariant contredit) et les règles de référence entre contextes (ADR-011).

### 4. Flux entre contextes

Pour chaque cas d'usage de l'étape 2 qui traverse une frontière : un **diagramme de séquence** (Mermaid
`sequenceDiagram`) entre contextes et tiers, avec ce qui est demandé ou notifié à chaque appel.
Regrouper les cas d'usage qui partagent un même flux. Confrontation : les mécanismes en place (partie
publiée en lecture, ADR-008 et 012 ; événements synchrones, ADR-013 ; ports vers les tiers) conviennent-
ils, et les dépendances restent-elles sans cycle ? Une divergence retenue appellera un nouvel ADR ou la
révision d'un ADR dans la spec.

## Publication

Après validation de l'étape 4 :

1. Relire `analyse.md` d'un bloc : budget de 5 minutes tenu, chaque écart au CDC et chaque divergence
   avec l'existant marqués. Ajouter en fin de document deux tableaux récapitulatifs des décisions des
   étapes 1 à 4 (s'il n'y a rien, l'écrire) :
   - **Amendements au CDC** : CDC dit, on retient, motif, page ;
   - **Révisions de l'existant** : ADR, spec ou code concerné, ce qui change, motif.
2. Publier en **doc** : charger la skill `anthropic-skills:docs` et suivre ses instructions (à défaut,
   `Artifact` avec `action: "quickstart"` et `intent: "document"`). Titre « Analyse : <contexte> ».
   Les schémas Mermaid deviennent des diagrammes du doc (voir son guide `topic.diagram`) ; aucun
   schéma n'est remplacé par du texte.
3. Reporter le lien du doc dans l'en-tête de `analyse.md`, statut « terminée, <date> ». Le brouillon
   reste la source que lit la skill `specs`.
4. Proposer un message de commit. Ne pas commiter. Rester dans le worktree : la spec suit (`/specs
   <contexte>`).
