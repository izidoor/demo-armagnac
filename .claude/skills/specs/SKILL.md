---
name: specs
description: Rédiger la spec d'un bounded context du projet BNIA (docs/specs/NN-contexte/spec.md) avec l'utilisateur, phase par phase, à partir du CDC et de la spec AGI, en itérant avec docs/contextes.md, puis rédiger les ADR qui en découlent. À utiliser quand l'utilisateur demande la spec d'un contexte (« /specs operateurs », « faisons les specs de cotisation »).
---

# Spec d'un contexte

Méthode éprouvée sur `docs/specs/01-referentiels/spec.md`, qui sert d'exemple abouti.
Argument : le nom du contexte, tel qu'il figure dans `docs/contextes.md` (`operateurs`, `cotisation`…).

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
- **Pas de code, pas de commit, pas de `git add`.**

## Phases

Après **chaque** phase : résumer ce qui a été écrit ou décidé, puis **s'arrêter** et attendre
l'utilisateur.

### A. Cadrage
1. Relire `CLAUDE.md`, `docs/contextes.md` (fiche du contexte, ordre, bordures, interrogations, zones
   partagées), `docs/glossaire.md`, `docs/adr/`, et les specs déjà écrites dont le contexte dépend.
2. Lire toutes les sources de la fiche du contexte (`docs/source/cdc/`, `docs/source/agi/`). Ne jamais
   lire les PDF.
3. **Recherche transverse des usages**, la phase la plus productive : chercher chaque concept du
   contexte dans tout le CDC et toute la spec AGI (grep sur les termes, synonymes et liaisons AGI). Lire
   les passages trouvés, et relever les pages (marqueurs `<!-- p.N -->`).
4. Restituer un tableau **concept : où il est utilisé : ce que cela implique**. Y signaler les
   contradictions (BO contre annexe C), les attributs implicites (un champ « texte » qui sert à un
   calcul), les objets décrits ailleurs (un « référentiel produits » en facturation), les manques de la
   spec AGI (liaison sans vue).
5. Contester la fiche de `contextes.md` : ce qui devrait en sortir, ce qui devrait y entrer.

### B. Périmètre
Questions sur ce qui entre ou sort du contexte, avec le contexte de destination envisagé. Puis créer la
spec à partir de `gabarit-spec.md` (même dossier), avec son sommaire en tête (`python3 tools/sommaire.py <spec>`), et remplir l'objet et le périmètre, y compris le
tableau « hors du contexte » avec ses motifs.

### C. Modèle et règles
Questions sur les attributs, les types, les invariants, les cas implicites (suppression, unicité,
valeurs par défaut, historique). Puis rédiger le modèle, les règles `RG-<CTX>-nn`, les cas d'usage,
les écrans et les données (démo, reprise AGI vue par vue).

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
3. Proposer un message de commit. Ne pas commiter.

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
- [ ] Hypothèses et questions : statut à jour, aucune « à trancher » oubliée.
- [ ] Aucun renvoi vers `docs/questions-ouvertes.md`.
- [ ] Vocabulaire conforme au glossaire (ADR-001) ; nouveaux termes listés pour la phase G.
- [ ] `contextes.md` cohérent avec la spec (fiche, autres fiches, interrogations, journal).
- [ ] Sommaire régénéré : `python3 tools/sommaire.py docs/specs/NN-<contexte>/spec.md` (idempotent).
- [ ] Journal de la spec à jour.
