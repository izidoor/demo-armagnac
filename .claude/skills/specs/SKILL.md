---
name: specs
description: Rédiger la spec d'un bounded context du projet BNIA (docs/specs/NN-contexte/spec.md) avec l'utilisateur, phase par phase, à partir du CDC et de la spec AGI, en itérant avec docs/contextes.md, puis rédiger les ADR qui en découlent. À utiliser quand l'utilisateur demande la spec d'un contexte (« /specs operateurs », « faisons les specs de cotisation »).
---

# Spec d'un contexte

Méthode éprouvée sur `docs/specs/01-referentiels/spec.md`, qui sert d'exemple abouti.
Argument : le nom du contexte, tel qu'il figure dans `docs/contextes.md` (`operateurs`, `cotisation`…).
Précédée de la skill `analyse`, dont le brouillon `docs/specs/NN-<contexte>/analyse.md` fait foi pour
la spec (voir « Conclusions de l'analyse »).

## Principes

- **L'utilisateur tranche tout.** Chaque question lui est soumise, y compris celles qui semblent relever
  du BNIA ou d'AGI (existant, reprise, sens d'un champ). Il connaît le terrain. L'option « inconnu :
  demander au BNIA » fait partie des alternatives ; ne jamais classer seul une question « pour le BNIA ».
- **Problématique d'abord.** Avant une question sur un sujet technique ou sur l'existant (vues AGI,
  champs InteGraal, notions proches), exposer le problème avec un exemple concret et des données
  (tableau, extrait de vue). Une question incomprise ne produit pas de décision.
- **Recommander en argumentant.** Une option recommandée porte « (Recommandé) » et une raison. Les
  recommandations peuvent être contestées : les reprendre sur le fond, sans s'y accrocher.
- **Ne rien inventer.** Chaque affirmation tirée des sources cite sa section et sa page (`§4.11.3 p.52`,
  `C.10.5.1`, `AGI 2.6.1 p.13`). Ce qui a été décidé en séance porte « décision du <date> ».
- **Itérer.** Spec → `contextes.md` → spec. Aucune des deux n'est figée avant l'autre.
- **Les questions vivent dans la spec du contexte** (§ Questions ouvertes), jamais dans un fichier global.
- **Toute hypothèse retenue est une question au BNIA.** Dès qu'une discussion retient une
  interprétation (source muette, sources contradictoires, écart au CDC), l'écrire aussitôt, sans
  attendre la rédaction : l'hypothèse `H-<CTX>-n` (§ Hypothèses, avec son pourquoi et ses sources), et
  sa question `Q-<CTX>-n` (§ Questions ouvertes) : « **Au BNIA** : confirmer que … (H-<CTX>-n) », avec
  dans « Bloque » ce qu'une autre réponse changerait. Les questions au BNIA de la spec forment ainsi la
  liste complète des points à valider avec lui.
- **Pas de code, pas de commit, pas de `git add`.**
- **Conclusions de l'analyse.** Ce que l'analyse a validé (cadrage, frontières, cas d'usage
  `UC-<CTX>-n`, objets et invariants, flux entre contextes, amendements au CDC, révisions de
  l'existant) est acquis : la spec le
  détaille, elle ne le remet pas en question. Ces points y citent « analyse du <date> » comme source. Un
  désaccord découvert en cours de spec (source mal lue, cas oublié) est soumis à l'utilisateur, puis
  reporté dans `analyse.md` avec sa date ; le doc publié n'est pas republié pour autant.
  Chaque révision de l'existant retenue (rôles, ADR, spec ou code d'un autre contexte) est planifiée
  dans la spec : révision de la spec concernée, nouvel ADR ou ADR révisé en phase H.
- **Un worktree par contexte.** Tout le travail du contexte (analyse, spec, ADR, puis implémentation) se
  fait dans le worktree `../bnia-worktrees/<contexte>` (chemin relatif à la racine du dépôt principal),
  sur la branche `contexte/<contexte>`, créés par la skill `analyse` (à défaut, en phase A). Le dépôt principal reste à l'utilisateur, qui y
  travaille en parallèle dans son IDE : n'y rien modifier, n'y changer ni de branche ni d'état. La
  branche n'est fusionnée dans `main` qu'à la fin de l'implémentation (skill `implementation`, phase F).

## Phases

Après **chaque** phase : résumer ce qui a été écrit ou décidé, puis **s'arrêter** et attendre
l'utilisateur.

### A. Cadrage
0. Créer le worktree du contexte et y placer la session. `git worktree list` d'abord :
   - worktree déjà présent (reprise) : y entrer ;
   - sinon, depuis la racine du dépôt principal : `git worktree add ../bnia-worktrees/<contexte> -b
     contexte/<contexte> main` (sans `-b` si la branche existe déjà). Le worktree part du dernier commit
     de `main` : ce que l'utilisateur n'a pas commité n'y est pas, le signaler s'il y en a.
   - puis, à la racine du worktree (même en reprise : sans effet s'il est déjà configuré) :
     `./worktree-env.sh`, qui lui donne ses propres ports (back, front, base, Mailpit) dans
     `backend/.env` et `frontend/.env.local`, ignorés par git ; annoncer les ports à l'utilisateur.

   Entrer avec l'outil `EnterWorktree` (paramètre `path`, chemin absolu du worktree), puis vérifier
   `git branch --show-current`.
1. Lire `docs/specs/NN-<contexte>/analyse.md`. Absente ou non terminée : le signaler et proposer de
   lancer d'abord la skill `analyse`. Puis relire `CLAUDE.md`, `docs/contextes.md` (fiche du contexte,
   ordre, bordures, interrogations, zones partagées), `docs/glossaire.md`, `docs/adr/`, et les specs
   déjà écrites dont le contexte dépend.
2. Lire toutes les sources de la fiche du contexte (`docs/source/cdc/`, `docs/source/agi/`). Ne jamais
   lire les PDF.
3. **Recherche transverse des usages**, la phase la plus productive : chercher chaque concept du
   contexte dans tout le CDC et toute la spec AGI (grep sur les termes, synonymes et liaisons AGI). Lire
   les passages trouvés, et relever les pages (marqueurs `<!-- p.N -->`).
4. Restituer un tableau **concept : où il est utilisé : ce que cela implique**. Y signaler les
   contradictions (BO contre annexe C), les attributs implicites (un champ « texte » qui sert à un
   calcul), les objets décrits ailleurs (un « référentiel produits » en facturation), les manques de la
   spec AGI (liaison sans vue).
5. Contester la fiche de `contextes.md` : ce qui devrait en sortir, ce qui devrait y entrer. Partir du
   cadrage et des frontières validés par l'analyse ; ne relever que ce qu'elle n'a pas couvert.

### B. Périmètre
Questions sur ce qui entre ou sort du contexte, avec le contexte de destination envisagé, au-delà des
frontières déjà fixées par l'analyse. Puis créer la
spec à partir de `gabarit-spec.md` (même dossier), avec son sommaire en tête (`python3 tools/sommaire.py <spec>`), et remplir l'objet et le périmètre, y compris le
tableau « hors du contexte » avec ses motifs.

### C. Modèle et règles
Questions sur les attributs, les types, les invariants, les cas implicites (suppression, unicité,
valeurs par défaut, historique). Puis rédiger le modèle, les règles `RG-<CTX>-nn`, les cas d'usage,
les écrans et les données (démo, reprise AGI vue par vue). Le modèle part des objets et invariants de
l'analyse ; les cas d'usage reprennent ses `UC-<CTX>-n` (mêmes numéros), complétés des cas secondaires.

Proposer enfin les **lots d'implémentation** (§ Lots) : des incréments au sens agile, chacun une
tranche verticale (domaine, cas d'usage, persistance, REST, écrans, démo) livrable, démontrable et
testable seule, qui ne s'appuie que sur les lots précédents. Par exemple : saisie manuelle, puis
édition PDF, puis import et export Excel. Pour chaque lot : l'incrément, son contenu, ses RG, et ce
qu'on peut démontrer à la fin. Chaque RG porte le numéro du lot qui la livre (colonne « Lot » du § Règles).

### D. Impact sur `contextes.md`
Lister ce que la spec change : fiche du contexte, fiches des autres contextes (objets déplacés,
dépendances), ordre d'implémentation, bordures, context map, interrogations (tranchées ou nouvelles),
journal. **Proposer la liste, l'appliquer après accord.**

### E. Architecture
Relever les décisions structurantes, `A-<CTX>-n` : relations entre contextes, forme du code, place des
concepts partagés, exceptions aux ADR. Pour chacune, 2 à 4 alternatives **illustrées** (aperçu de code,
de packages ou de tables). Reporter les décisions dans la spec (tableau Décision / Écarté / ADR) et dans
`contextes.md` (§ éléments transverses) si elles valent au-delà du contexte.

### F. Questions ouvertes
Passer en revue **toutes** les hypothèses `H-<CTX>-n` et questions `Q-<CTX>-n`, par lots. Chaque
réponse devient une décision (reportée dans le modèle ou les RG), une hypothèse validée, ou une question
**adressée** (« Au BNIA : », « Demande à AGI : ») avec ce qu'elle bloque. Une question tranchée reste
visible, barrée, avec sa réponse et « à confirmer par le BNIA » si besoin.

### G. Relecture, puis glossaire
Relire toute la spec avec la liste de contrôle ci-dessous, corriger, puis mettre à jour
`docs/glossaire.md` : nouveaux termes avec leur code, leur définition et leurs pages ; termes existants
devenus faux. Signaler les numéros de page non vérifiés.

### H. Clôture
1. L'utilisateur valide : statut « terminée » dans l'en-tête, ligne au journal de la spec et de
   `contextes.md`.
2. Rédiger les ADR issus des décisions `A-<CTX>-n` (un ADR peut en regrouper plusieurs), au format des
   ADR existants : titre `ADR-NNN : …`, statut, date, Contexte, Décision, Conséquences, avec les options
   écartées et leur motif. Numéro suivant dans `docs/adr/`. Proposer, puis écrire après accord.
   Mettre à jour la colonne ADR de la spec et `CLAUDE.md` si une règle d'architecture change.
3. Proposer un message de commit. Ne pas commiter. Rester dans le worktree du contexte : il
   accueille ensuite l'implémentation.

## Règles de questionnement

- Outil de questions, au plus 4 questions par lot, de 2 à 4 options chacune.
- Le libellé cite la source : « Régions (C.10.6) : … ».
- Option recommandée en premier, suffixée « (Recommandé) », avec sa raison dans la description.
- « Inconnu : demander au BNIA » quand l'existant ou le métier réel est en jeu.
- Aperçus (`preview`) pour comparer des alternatives d'architecture ou de modèle.
- Si l'utilisateur veut préciser au lieu de répondre : lui demander ce qu'il veut préciser, expliquer,
  puis reposer les questions restantes.

## Liste de contrôle de relecture

- [ ] Numérotation continue des RG, sans suffixe ni trou ; tous les renvois à jour.
- [ ] Chaque RG a une source (section et page) ou « décision du <date> ».
- [ ] Chaque attribut ajouté au modèle apparaît dans la lecture par les autres contextes, les écrans, la
      démo et la reprise.
- [ ] Chaque cas d'usage cite les RG qu'il applique ; chaque RG est appliquée par un cas d'usage.
- [ ] Les tests attendus répartissent les RG entre domaine et cas d'usage / IT.
- [ ] Chaque RG a un lot ; chaque lot est un incrément vertical démontrable, sans dépendance vers un lot suivant.
- [ ] Hypothèses et questions : statut à jour, aucune « à trancher » oubliée.
- [ ] Chaque hypothèse `H-<CTX>-n` a sa question « Au BNIA : confirmer… » dans § Questions ouvertes.
- [ ] Aucun renvoi vers `docs/questions-ouvertes.md`.
- [ ] Vocabulaire conforme au glossaire (ADR-001) ; nouveaux termes listés pour la phase G.
- [ ] `contextes.md` cohérent avec la spec (fiche, autres fiches, interrogations, journal).
- [ ] Sommaire régénéré : `python3 tools/sommaire.py docs/specs/NN-<contexte>/spec.md` (idempotent).
- [ ] Journal de la spec à jour.
