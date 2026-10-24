# Suivi de l'utilisation du prototype

Pour l'auteur seul : qui utilise le prototype (e-mail, appareil), quels écrans, combien de temps, quels fichiers
téléchargés ou importés. Rien n'en paraît dans l'application, pas même pour les ADMIN du BNIA. La lecture passe par
`./suivi.sh`, avec un jeton.

## Ce qui est enregistré

| Type | Relevé par | Signification | Colonne `lieu` |
|---|---|---|---|
| `ECRAN` | front | Arrivée sur un écran du back-office ou du portail (un changement de filtre sur le même écran ne compte pas) | Gabarit de la route : `/portail/declarations/$dsm` |
| `OFFRE` | front | Section de « Voir l'offre » affichée | `offre/<section>` : `offre/01-synthese` |
| `PRESENCE` | front | Toujours là : signal envoyé chaque minute tant que l'onglet est visible ; c'est lui qui mesure le temps passé | Écran ou section affiché à cet instant |
| `TELECHARGEMENT` | serveur | Fichier reçu : export Excel, PDF (DSM, facture, pièce), PDF de l'offre ; `detail` : nom du fichier | Méthode et adresse de l'API : `GET /api/declarations/stocks/excel` |
| `IMPORT` | serveur | Fichier envoyé (import Excel, pièce, pièce jointe), accepté ou refusé ; `detail` : nom du fichier, `statut` : réponse (200 accepté, 4xx refusé) | `POST /api/operateurs/{operateur}/dsm/{id}/import` |

Chaque événement porte aussi l'instant, l'e-mail (copié : les identifiants changent à chaque remise à zéro),
l'appareil (ordinateur, tablette, mobile) et une empreinte de la session, qui regroupe les visites.

Les fichiers sont relevés par le serveur parce que beaucoup passent par un lien direct (`<a download>`,
`window.open`) que le front ne voit pas.

## Où sont les données

Dans le schéma `suivi` (table `suivi.evenement`), créé par l'application au démarrage, hors de Flyway et de
`V1__schema.sql`. `flyway clean` ne vide que les schémas de `spring.flyway.schemas` : le suivi survit à une V1 modifiée
comme à `scalingo-reset.sh`. Il ne disparaît qu'avec l'addon PostgreSQL, en local avec `./dev-reset.sh`, qui
supprime le volume Docker, ou par une remise à zéro volontaire (ci-dessous).

## Remettre le suivi à zéro

```bash
./suivi.sh remise-a-zero           # instance hébergée (.env.suivi)
./suivi.sh --local remise-a-zero   # instance locale
```

Le script demande de taper « effacer », puis vide la table `suivi.evenement` et donne le nombre d'événements effacés.
C'est définitif : aucune archive n'est gardée. La remise à zéro exige le même jeton que l'export
(`POST /api/suivi/remise-a-zero`). Elle ne touche ni la démonstration (le rapport `connexions` la lit) ni les
cookies `?suivi=non` des navigateurs exclus.

## Ne pas se compter soi-même

Les comptes de démonstration sont partagés : l'e-mail ne distingue pas l'auteur des visiteurs. Ouvrir une fois, dans
chaque navigateur et sur chaque appareil :

```
https://bnia-demo.osc-fr1.scalingo.io/connexion?suivi=non
```

Ce navigateur n'est plus suivi, quel que soit le compte (`?suivi=oui` l'y remet). La consigne est gardée un an dans un
cookie (`bnia-suivi`), lu par le front pour les écrans et par le serveur pour les fichiers. À refaire après un
effacement des cookies.

## Mise en place sur Scalingo (une fois)

L'export exige un jeton, un mot de passe connu du serveur et du script seulement. Depuis la racine du dépôt
(`~/Projects/bnia`), dans un même terminal :

```bash
jeton=$(openssl rand -hex 32)                                     # 1. inventer le jeton
scalingo --app bnia-demo env-set BNIA_SUIVI_JETON="$jeton"        # 2. le donner au serveur (redémarre l'application)
printf 'BNIA_SUIVI_URL=https://bnia-demo.osc-fr1.scalingo.io\nBNIA_SUIVI_JETON=%s\n' "$jeton" > .env.suivi   # 3. au script
```

`.env.suivi` est ignoré par git. Fichier perdu : `scalingo --app bnia-demo env-get BNIA_SUIVI_JETON` redonne le jeton.
Sans `BNIA_SUIVI_JETON` sur le serveur, l'export n'existe pas (404).

En local, rien à faire : le profil `dev` a le jeton `dev`.

## Lire le suivi

`./suivi.sh` tourne sur le poste de l'auteur et interroge l'instance par Internet : rien à lancer sur Scalingo. Il
interroge Scalingo si `.env.suivi` existe, l'instance locale sinon ou avec `--local` (port de `backend/.env` s'il
existe, 8080 sinon). Sa première ligne dit laquelle.

```bash
./suivi.sh tableau               # page de graphiques, ouverte dans le navigateur
./suivi.sh                       # tout, en texte : personnes, visites, écrans, offre, fichiers
./suivi.sh personnes             # une ligne par e-mail : appareils, visites, minutes, fichiers, première et dernière venue
./suivi.sh visites               # une ligne par visite : début, fin, durée, parcours
./suivi.sh ecrans                # écrans, du plus regardé au moins regardé
./suivi.sh offre                 # sections de l'offre lues
./suivi.sh fichiers              # téléchargements et imports : qui, quand, quel fichier, accepté ou refusé
./suivi.sh evenements            # événements bruts
./suivi.sh connexions            # dernière connexion de chaque compte, lue dans la base courante
./suivi.sh --local tableau       # l'instance locale
./suivi.sh visites 2026-10-21    # depuis une date (tous les rapports)
```

Le parcours d'une visite se lit ainsi : `/portail/declarations/$dsm (2.8) [téléchargement DSM-000001-2026-04.xlsx] >
/portail/factures (0.0)`. Chaque étape donne l'écran et ses minutes, avec entre crochets les fichiers téléchargés ou
importés depuis cet écran.

### Le tableau

`./suivi.sh tableau` récupère les événements bruts, les insère dans le modèle `tools/suivi-tableau.html`, écrit la page
dans `out/suivi-tableau.html` (ignoré par git) et l'ouvre dans le navigateur. Relancer la commande pour rafraîchir. La
page fonctionne sans réseau. Elle montre :
- les chiffres clés ;
- les personnes (un clic filtre la page) ;
- les minutes par jour ;
- les visites avec leur parcours ;
- les écrans ;
- une grille sections de l'offre × personnes ;
- les fichiers.

Filtres par période et par personne, et e-mails à exclure, gardés d'une fois sur l'autre.

## Règles de calcul

- Une **visite** regroupe les événements du front d'une session ; elle s'arrête après 30 minutes sans événement.
- Le **temps passé** sur un événement court jusqu'au suivant de la session, 90 secondes au plus (le front signale sa
  présence chaque minute). Le dernier compte pour rien.
- La **durée d'une visite** va du premier au dernier événement. Les **minutes d'une personne** additionnent le temps
  écran par écran. Les deux chiffres peuvent donc différer légèrement.
- Les **fichiers** ne comptent pas dans le temps : ils se rangent dans l'étape du parcours en cours.
- `connexions` ne lit pas le suivi mais `authentification.compte` : il couvre aussi la période d'avant le suivi, et
  s'efface avec la démonstration. Le chargement de la démonstration connecte chaque compte : une date égale à celle du
  chargement n'est pas une vraie connexion.

## Accès direct à la base Scalingo

Pour une requête que les rapports n'offrent pas, en lecture seule : c'est la base que consulte le BNIA.

```bash
scalingo --app bnia-demo pgsql-console                       # psql directement
scalingo --app bnia-demo db-tunnel SCALINGO_POSTGRESQL_URL   # tunnel sur localhost:10000, pour IntelliJ ou DBeaver
scalingo --app bnia-demo env-get SCALINGO_POSTGRESQL_URL     # identifiants (remplacer l'hôte par localhost:10000)
```

Les requêtes des rapports sont dans `RapportSuivi.java` (remplacer `:depuis` par une date).

## Le code

| Fichier | Rôle |
|---|---|
| `frontend/src/features/suivi/suivi.ts` | Écrans, sections de l'offre, présence ; appareil ; cookie `?suivi=non` |
| `frontend/src/main.tsx`, `features/offre/offre.tsx` | Branchements : changement de route, section affichée |
| `backend/bnia-boot/src/main/java/fr/bnia/boot/suivi/SuiviConfiguration.java` | Schéma `suivi`, intercepteur, chaîne de sécurité de l'export |
| `SuiviController.java` | `POST /api/suivi/evenements` (sous session), `GET /api/suivi/export/{rapport}` et `POST /api/suivi/remise-a-zero` (jeton) ; hors du contrat OpenAPI |
| `SuiviFichiers.java` | Relève téléchargements et imports, côté serveur |
| `JournalSuivi.java` | Écriture des événements |
| `RapportSuivi.java` | Les rapports, en SQL |
| `JetonSuivi.java` | Contrôle du jeton (`bnia.suivi.jeton`, `BNIA_SUIVI_JETON`) |
| `suivi.sh` | Lecture des rapports, génération du tableau, remise à zéro |
| `tools/suivi-tableau.html` | Modèle du tableau : mise en forme, calculs et graphiques en JavaScript |

Tests : `SuiviIT`, `JetonSuiviTest`, `SuiviFichiersTest`, et `MigrationDevConfigurationIT` pour la survie aux remises
à zéro.
