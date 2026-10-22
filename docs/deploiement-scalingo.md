# Déploiement du prototype sur Scalingo

Une seule application Scalingo : le jar de `bnia-boot` embarque le front (profil Maven `front`) et le sert
(`FrontConfiguration`), sous les profils Spring `prod,demo`. Données de démonstration, double authentification désactivée
au départ et code de connexion `000000` une fois activée, comme en local (`docs/demo.md`) ; base recréée au démarrage si `V1__schema.sql` a changé
(`MigrationDevConfiguration`), ce qui remet la démonstration à zéro.

## Fichiers en jeu

| Fichier | Rôle |
|---|---|
| `pom.xml` (racine) | Agrégateur de `backend/` : le buildpack Java veut un `pom.xml` à la racine du dépôt |
| `system.properties` | Version de Java (`25`) |
| `Procfile` | Lance `bin/demarrer-scalingo.sh` |
| `bin/demarrer-scalingo.sh` | Convertit `SCALINGO_POSTGRESQL_URL` en `BNIA_DB_URL`, `BNIA_DB_USER`, `BNIA_DB_PASSWORD`, puis lance le jar |
| `backend/bnia-boot/pom.xml`, profil `front` | Télécharge Node et pnpm, construit `frontend/`, copie `dist/` dans `static/` |
| `scalingo-reset.sh` | Remise à zéro volontaire de la démonstration hébergée (voir plus bas) |

## Première mise en place

Une fois, avec la CLI Scalingo (`curl -O https://cli-dl.scalingo.com/install && bash install`, puis
`scalingo login`). Remplacer `bnia-demo` par le nom retenu (unique sur toute la plateforme).

```bash
scalingo create bnia-demo --region osc-fr1   # ou depuis le tableau de bord
git remote add scalingo git@ssh.osc-fr1.scalingo.com:bnia-demo.git   # déjà fait dans ce clone
scalingo --app bnia-demo addons-plans postgresql  # vérifier le nom du plan gratuit
scalingo --app bnia-demo addons-add postgresql postgresql-sandbox

scalingo --app bnia-demo env-set \
  MAVEN_CUSTOM_OPTS="-DskipTests=true -Pfront" \
  BNIA_DB_POOL=5 \
  JAVA_OPTS="-Xmx256m -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=64m -Xss512k -XX:+UseSerialGC" \
  BNIA_URL_PUBLIQUE=https://bnia-demo.osc-fr1.scalingo.io
```

- `BNIA_DB_POOL=5` : la base Sandbox n'accepte qu'une dizaine de connexions.
- `JAVA_OPTS` : tient dans un conteneur M (512 Mo) ; mesuré à environ 300 Mo après démarrage et
  sollicitation de toutes les routes de lecture.
- `BNIA_URL_PUBLIQUE` : adresse mise dans les liens des e-mails.
- Sans serveur SMTP (`spring.mail.host` absent), les e-mails ne partent pas : ils sont écrits dans le journal
  (`scalingo logs`), liens d'invitation compris.

## Déployer

Seul ce qui est commité part : Scalingo construit le dépôt poussé.

```bash
git push scalingo main
scalingo --app bnia-demo scale web:1:M   # premier déploiement : conteneur M
scalingo --app bnia-demo logs -f
```

Puis https://bnia-demo.osc-fr1.scalingo.io/connexion, avec les comptes de `docs/demo.md`.

Chaque `git push scalingo main` reconstruit et redéploie. L'ancien conteneur reste en service jusqu'à ce que
le nouveau ait démarré ; si le build ou le démarrage échoue, l'ancienne version reste en ligne.

## Ce que devient la base à chaque démarrage

Au démarrage, l'application compare `V1__schema.sql` à celle qui a créé la base (somme de contrôle de Flyway) :

| Situation | Effet sur la base |
|---|---|
| V1 inchangée (cas courant : code ou front modifiés) | Conservée : tout ce qui a été saisi en ligne reste. Les chargeurs du profil `demo` voient leurs données déjà là et ne les rechargent pas ; des données de démonstration modifiées n'apparaissent donc qu'après une remise à zéro |
| V1 modifiée | Vidée, recréée, puis rechargée avec la démonstration : tout ce qui a été saisi en ligne est perdu |
| Remise à zéro demandée (`scalingo-reset.sh`) | Vidée, recréée, rechargée, même si V1 n'a pas changé |

Le vidage n'est possible qu'en profil `dev` ou `demo` (`spring.flyway.clean-disabled: false`, posé dans
`application-dev.yml` et `application-demo.yml`). Une installation en `prod` seul n'est jamais vidée.

## Remettre la démonstration à zéro

Pour retrouver la démonstration d'origine (après des essais, avant de montrer le prototype), depuis la racine
du dépôt :

```bash
./scalingo-reset.sh              # application bnia-demo
./scalingo-reset.sh autre-app    # une autre application
```

Le script demande de retaper le nom de l'application, puis :

1. **Lance un conteneur ponctuel** (`scalingo run`), à côté de l'application en ligne. Il démarre le même jar,
   avec les mêmes variables, et deux arguments en plus :
   - `--bnia.demo.reinitialisation=true` : `MigrationDevConfiguration` vide la base (`flyway clean`) et la
     recrée depuis V1, sans regarder si V1 a changé ;
   - `--server.port=0` : port quelconque, ce conteneur ne reçoit aucune requête.

   Les chargeurs du profil `demo` remplissent ensuite la base. Une fois tout chargé, `ReinitialisationDemo`
   arrête l'application et le conteneur disparaît. Compter une minute environ ; son journal s'affiche dans le
   terminal.
2. **Redémarre l'application en ligne** (`scalingo restart`) pour qu'elle reparte proprement sur la base
   neuve. Les sessions étaient en base : chacun doit se reconnecter.

Pendant l'opération, l'application en ligne reste accessible, mais ses données disparaissent puis
réapparaissent : à éviter pendant une présentation. Si le conteneur ponctuel échoue (journal en erreur),
l'étape 2 n'est pas lancée ; relancer le script une fois la cause corrigée.

Sans le script, à la main :

```bash
scalingo --app bnia-demo run \
  "bash bin/demarrer-scalingo.sh --bnia.demo.reinitialisation=true --server.port=0"
scalingo --app bnia-demo restart
```

Ne jamais poser `bnia.demo.reinitialisation` en variable d'environnement de l'application : chaque
redémarrage viderait la base, puis arrêterait l'application.

## Suivi de l'utilisation

Pour l'auteur seul : qui utilise le prototype (e-mail, appareil), quels écrans, combien de temps. Rien n'en paraît
dans l'application.

- **Enregistrement** : le front (`frontend/src/features/suivi/suivi.ts`) signale chaque écran du back-office et du
  portail (gabarit de la route), chaque section de « Voir l'offre », et la présence sur la page chaque minute, onglet
  visible. Le serveur (`fr.bnia.boot.suivi`) y joint l'e-mail et une empreinte de la session.
- **Fichiers** : le serveur (`SuiviFichiers`) relève lui-même chaque téléchargement (exports Excel, PDF, pièces, offre)
  et chaque import (envoi de fichier, même refusé), avec le nom du fichier : beaucoup passent par un lien direct que le
  front ne voit pas. Ils apparaissent entre crochets dans le parcours de la visite.
- **Survie aux remises à zéro** : le schéma `suivi` est créé par l'application au démarrage, hors de Flyway et de
  `V1__schema.sql`. `flyway clean` ne vide que les schémas de `spring.flyway.schemas` : le suivi survit à une V1
  modifiée comme à `scalingo-reset.sh`. Il ne disparaît qu'avec l'addon PostgreSQL (ou `./dev-reset.sh` en local, qui
  supprime le volume).
- **Ses propres visites** : `?suivi=non` au bout de n'importe quelle adresse du prototype retire ce navigateur du
  suivi ; `?suivi=oui` l'y remet. La consigne est gardée dans un cookie (`bnia-suivi`), lu par le front pour les écrans
  et par le serveur pour les fichiers. Les comptes de démonstration étant partagés, c'est la seule façon de ne pas se
  compter soi-même.

Mise en place, une fois (le changement de variable redémarre l'application) :

```bash
jeton=$(openssl rand -hex 32)
scalingo --app bnia-demo env-set BNIA_SUIVI_JETON="$jeton"
printf 'BNIA_SUIVI_URL=https://bnia-demo.osc-fr1.scalingo.io\nBNIA_SUIVI_JETON=%s\n' "$jeton" > .env.suivi
```

Sans `BNIA_SUIVI_JETON`, l'export n'existe pas (404). En profil `dev`, le jeton vaut `dev` : sans `.env.suivi`, ou
avec `--local` (`./suivi.sh --local ecrans`), le script interroge l'instance locale, au port de `backend/.env` s'il
existe. Lecture, depuis la racine du dépôt :

```bash
./suivi.sh                       # tout : personnes, visites, écrans, offre, fichiers
./suivi.sh personnes             # une ligne par e-mail : appareils, visites, minutes, première et dernière venue
./suivi.sh visites               # une ligne par visite : début, fin, durée, parcours (écrans dans l'ordre, minutes)
./suivi.sh ecrans                # écrans, du plus regardé au moins regardé
./suivi.sh offre                 # sections de l'offre lues
./suivi.sh fichiers              # téléchargements, exports et imports : qui, quand, quel fichier, accepté ou refusé
./suivi.sh evenements 2026-10-22 # événements bruts, depuis une date
./suivi.sh connexions            # dernière connexion de chaque compte, lue dans la base courante
./suivi.sh tableau               # page de graphiques, ouverte dans le navigateur
```

`./suivi.sh tableau [depuis]` récupère les événements bruts et les insère dans le modèle `tools/suivi-tableau.html`. La
page est écrite dans `out/suivi-tableau.html` (ignoré par git), puis ouverte dans le navigateur ; on relance la commande
pour la rafraîchir. Elle recalcule visites, parcours et temps avec les mêmes règles que les rapports. Elle montre : les
personnes, les minutes par jour, les visites et leur parcours, les écrans, une grille sections de l'offre × personnes,
et les fichiers. On peut y filtrer par période et par personne, et exclure des e-mails. Elle fonctionne sans réseau.

Colonne `type` des rapports `evenements` et `fichiers` :

| Type | Relevé par | Signification | Colonne `lieu` |
|---|---|---|---|
| `ECRAN` | front | Arrivée sur un écran du back-office ou du portail (un changement de filtre sur le même écran ne compte pas) | Gabarit de la route : `/portail/declarations/$dsm` |
| `OFFRE` | front | Section de « Voir l'offre » affichée | `offre/<section>` : `offre/01-synthese` |
| `PRESENCE` | front | Toujours là : signal envoyé chaque minute tant que l'onglet est visible ; c'est lui qui mesure le temps passé | Écran ou section affiché à cet instant |
| `TELECHARGEMENT` | serveur | Fichier reçu : export Excel, PDF (DSM, facture, pièce), PDF de l'offre ; colonne `detail` : nom du fichier | Méthode et adresse de l'API : `GET /api/declarations/stocks/excel` |
| `IMPORT` | serveur | Fichier envoyé (import Excel, pièce, pièce jointe), accepté ou refusé ; `detail` : nom du fichier, `statut` : réponse (200 accepté, 4xx refusé) | `POST /api/operateurs/{operateur}/dsm/{id}/import` |

Une visite s'arrête après 30 minutes sans événement. Le temps passé sur un écran court jusqu'à l'événement suivant,
90 secondes au plus. `connexions` ne lit pas le suivi mais `authentification.compte` : elle couvre aussi la période
d'avant le suivi, et s'efface avec la démonstration. Le chargement de la démonstration connecte chaque compte : une date
égale à celle du chargement n'est pas une vraie connexion.

## Essayer en local avant de pousser

```bash
backend/mvnw -f pom.xml -DskipTests -Pfront clean package
SCALINGO_POSTGRESQL_URL=postgres://utilisateur:mot-de-passe@localhost:5432/base PORT=18080 \
  bash bin/demarrer-scalingo.sh
```

Viser une base jetable : le profil `demo` y charge ses données et la vide si V1 a changé.
