---
name: implementation
description: Implémenter un bounded context du projet BNIA à partir de sa spec terminée (docs/specs/NN-contexte/spec.md), par lots livrés un à un (domaine, cas d'usage, persistance, REST, partie publiée, front, démo), chacun vérifié et soumis à l'utilisateur avant le suivant. À utiliser quand l'utilisateur demande d'implémenter un contexte (« /implementation operateurs », « passons à l'implémentation de cotisation », « go lot 2 »).
---

# Implémentation d'un contexte

Méthode éprouvée sur `referentiels` (spec 01, lots 1 à 7, puis révision ADR-011), qui sert d'exemple
abouti : `backend/bnia-referentiels/` (`-core` et `-infra`), `frontend/src/features/referentiels/`.
Argument : le nom du contexte, tel qu'il figure dans `docs/contextes.md`.
Les patrons de code à reprendre sont dans `patrons.md` (même dossier).

## Principes

- **La spec fait foi.** Chaque RG, cas d'usage, écran et donnée de démo vient de la spec terminée. Un
  écart nécessaire (la spec est muette, fausse ou contredite par l'existant) est soumis à l'utilisateur
  avant d'être codé, puis consigné au journal de la spec. Un écart d'architecture passe par un ADR.
- **Les règles restrictives se montrent à l'écran avant d'être codées.** Immuabilité, suppression
  interdite, champ figé : les formuler par leur effet visible (« le code ne pourra plus être modifié »)
  et les faire confirmer. Par défaut, dans le prototype : identifiant technique, tout se corrige, une
  valeur inutilisée se supprime (ADR-011).
- **Par lots.** Chaque lot se termine build vert, par un compte rendu, un message de commit proposé et
  un **arrêt**. Le lot suivant attend l'accord (« go lot N »).
- **Ni commit, ni `git add`, ni `git rm`** sans demande explicite pour cette commande précise.
- **Travailler dans le worktree du contexte**, `../bnia-worktrees/<contexte>` (branche
  `contexte/<contexte>`), créé par la skill `analyse` (ou `specs`). Le dépôt principal reste à l'utilisateur, qui y
  travaille en parallèle : n'y rien modifier. Seule la phase F fusionne la branche dans `main`, après
  validation explicite.
- **Ne rien affirmer d'invérifié.** Ce qui n'a pas été exécuté (écran dans un navigateur, démarrage sur
  la base de dev de l'utilisateur) est dit tel quel dans le compte rendu.
- **Ne pas toucher l'environnement de l'utilisateur** : ni son dépôt principal, ni sa base de dev
  (projet Compose `backend`, port 5432), ni les ports 8080 et 5173, ni `.idea/`. Lancer l'application
  n'est permis que depuis le worktree, en instance isolée (voir « Lancer l'application »), arrêtée et
  sa base supprimée une fois la vérification faite.

## Phases

### A. Cadrage et plan (mode plan)
0. Entrer dans le worktree du contexte : `git worktree list`, puis l'outil `EnterWorktree` (paramètre
   `path`, chemin absolu de `../bnia-worktrees/<contexte>`) ; vérifier `git branch --show-current`
   (`contexte/<contexte>`). Worktree absent : s'arrêter et demander à l'utilisateur (le créer comme la
   skill `specs`, phase A, ou travailler ailleurs). Ne jamais implémenter dans le dépôt principal.
   Premier passage : `pnpm install` dans `frontend/` du worktree.
1. Relire `CLAUDE.md`, la spec du contexte, les ADR, `docs/contextes.md` (fiche, dépendances), les
   specs et le code des contextes dont il dépend (`ReferentielsLecture` notamment), et `patrons.md`.
2. Relever ce que la spec laisse ouvert côté implémentation (forme d'une saisie, identifiant d'un
   objet sans code, factorisation, recherche…). Le soumettre en un lot de questions (outil de
   questions, 4 au plus, option recommandée en premier avec sa raison, aperçus de code si utile).
3. Écrire le plan : contexte, décisions prises en séance, conventions retenues sans question
   (modifiables à la relecture), **lots**, fichiers critiques, vérification. Puis le faire approuver.

Les lots sont ceux de la spec (§ Lots d'implémentation) : des incréments verticaux, chacun livré de
bout en bout (domaine, cas d'usage, persistance, REST, IT, **écrans**, données de démonstration) et
démontrable à la fin du lot. Le plan peut les affiner (scinder un lot trop gros), pas les remettre en
couches. Le lot 1 crée aussi le module Maven et le schéma PostgreSQL du contexte ; le dernier, la
clôture. Une spec sans lots (antérieure à cette règle) : les proposer dans le plan sur ce modèle.

### B. Un lot
Ordre de CLAUDE.md : tests de domaine, cas d'usage, adapters, front.
1. **Domaine** (Java pur) : objets-valeurs qui valident à la construction et lèvent `RegleEnfreinte`
   avec l'identifiant de la règle ; agrégats ; tests nommés `rg_xxx_nn_…` avec `@DisplayName("RG-XXX-nn : …")`.
2. **Application** : ports (écriture par agrégat, lecture par listing), cas d'usage `@UseCase` à
   **un seul constructeur public** (Spring l'instancie sans annotation) ; tests avec repositories en
   mémoire qui attribuent les identifiants.
3. **Persistance** : section du contexte dans `V1__schema.sql` (jamais de V2) ; lignes Spring Data
   JDBC en records imbriqués ; listings en `JdbcClient`.
4. **REST** : controllers, DTO `@Schema` avec `requiredMode`, `operationId` explicites ou `@Ressource`.
5. **IT** dans `bnia-boot` : chaîne complète, données uniques par test (base partagée), statuts 409 et
   422 avec la propriété `regle`.
6. **Front** (dans chaque lot) : `pnpm gen:api`, pages sous `src/features/<contexte>/`, routes fines
   sous `src/routes/bo/...`, menu dans `components/layout/data/espaces.ts`.
7. **Démonstration** : les données du profil `demo` qui rendent l'incrément du lot démontrable.

### C. Vérification d'un lot
- `cd backend && ./mvnw -q verify` : tests, IT, ArchUnit, enforcer, réexport de `docs/api/openapi.json`.
  Lire `bnia-boot/target/failsafe-reports/failsafe-summary.xml` (nombre d'IT, échecs).
- Contrat : operationId uniques, sans suffixe `_1` (script Python sur `docs/api/openapi.json`).
- Front, dès qu'il est touché : `pnpm gen:api && pnpm build && pnpm lint && pnpm format:check`
  (`build` inclut `tsc -b` et régénère `routeTree.gen.ts`). `pnpm knip` : ne traiter que ce qui
  concerne le contexte.
- Couverture des RG : `grep -rhoE "RG-XXX-[0-9]+" backend/*/src/test | sort -u` doit lister toutes
  les RG livrées par le lot.

### Lancer l'application (instance isolée)
Seulement depuis le worktree, quand une vérification l'exige (démarrage, démo, écran). Variables
lues par `backend/compose.yaml` et `frontend/vite.config.ts` ; sans elles, valeurs de l'utilisateur.
- Backend, depuis `backend/` du worktree, en arrière-plan :
  `BNIA_COMPOSE_PROJECT=bnia-<contexte> BNIA_DB_PORT=55432 BNIA_SMTP_PORT=11025 BNIA_MAILPIT_PORT=18025 SERVER_PORT=8181 ./mvnw -pl bnia-boot spring-boot:run`
  (base et Mailpit propres au contexte, détectés par Spring Boot ; prêt quand `localhost:8181/v3/api-docs` répond 200).
- Front, depuis `frontend/` du worktree : `BNIA_API_URL=http://localhost:8181 pnpm dev --port 5174 --strictPort`.
- Port occupé : prendre le suivant, sans arrêter le processus qui l'occupe.
- Arrêt : tuer le processus par son PID (jamais `pkill -f` sur un motif, qui atteint aussi les
  processus de l'utilisateur), puis `BNIA_COMPOSE_PROJECT=bnia-<contexte> docker compose -f
  compose.yaml down -v`.

### D. Compte rendu d'un lot
- Une phrase de résultat : ce qui est livré, build vert (nombre d'IT), rien d'indexé.
- Ce qui est en place, par couche, avec les RG appliquées.
- **Écarts avec le plan**, et pourquoi.
- **Fichiers non suivis** à inclure au commit (`git status --short | grep '^??'`) : un fichier neuf
  oublié casse le commit (incident du lot 2 : `V1__schema.sql` non commité).
- Ce qui n'a pas été vérifié ; ce que l'utilisateur doit faire (`./dev-reset.sh` inutile : la base de
  dev se recrée seule si V1 a changé ; recharger Maven dans IntelliJ si un pom a changé et que le worktree y est ouvert).
- Message de commit proposé, puis le lot suivant en une ligne.

### E. Clôture du contexte (dernier lot)
1. Chargeur de démonstration (`fr.bnia.boot.demo.Demo<Contexte>`, profil `demo`) : par les cas
   d'usage, relançable, avec son IT.
2. Spec : statut « implémentée », ligne au journal qui consigne les **choix d'implémentation**.
3. `docs/contextes.md` : fiche (« implémentée »), journal, étape suivante. ADR et `CLAUDE.md` si une
   convention a changé. Glossaire si un terme est apparu.
4. Bilan : à faire par l'utilisateur (tour d'écran en profil `dev,demo`), questions encore ouvertes
   (au BNIA, à AGI), ce qui reste en suspens et pourquoi, ce qui est à retenir.

### F. Fusion dans `main` (ultime étape)
La fusion se fait dans le dépôt principal (`git -C <dépôt principal>`), où `main` est extrait : un
worktree ne peut pas extraire une branche déjà extraite ailleurs.
1. Une fois le dernier lot commité par l'utilisateur, vérifier : worktree propre (`git status`),
   branche courante `contexte/<contexte>`, liste des commits à fusionner
   (`git log --oneline main..contexte/<contexte>`) ; dans le dépôt principal, branche `main` et arbre
   propre (`git -C <dépôt principal> status`). Sinon : s'arrêter et le signaler, sans changer de branche.
2. **Demander la validation** de la fusion (outil de questions), en montrant cette liste et la
   commande prévue. Sans « oui » explicite, s'arrêter là.
3. Après accord seulement : `git -C <dépôt principal> merge --no-ff contexte/<contexte>`. En cas de
   conflit : `git -C <dépôt principal> merge --abort`, puis le signaler sans rien résoudre seul.
4. Sortir du worktree (outil `ExitWorktree`, `action: "keep"`). Ne pas pousser, ne supprimer ni la
   branche ni le worktree : proposer `git worktree remove ../bnia-worktrees/<contexte>` et
   `git branch -d contexte/<contexte>`, sans le faire.

## Liste de contrôle d'un lot

- [ ] Chaque RG du lot a au moins un test qui porte son identifiant.
- [ ] Les messages d'erreur sont en français, destinés à l'utilisateur, et nomment l'objet en cause.
- [ ] Une valeur référencée ailleurs a une clé étrangère ; sa suppression renvoie 409 (`ElementUtilise`).
- [ ] Unicité vérifiée à la création **et** à la modification (en excluant l'objet modifié).
- [ ] Le domaine ne dépend que de `java..` et de `commun.domain` ; ArchUnit vert.
- [ ] `V1__schema.sql` modifié sur place ; schéma du contexte déclaré dans `spring.flyway.schemas`.
- [ ] Contrat OpenAPI réexporté et client régénéré si l'API a changé.
- [ ] Aucun fichier généré ou d'IDE ajouté au dépôt (`out/`, `target/`, `dist/`).
- [ ] Compte rendu : écarts, fichiers non suivis, non-vérifié, message de commit.
