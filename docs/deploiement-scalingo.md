# Déploiement du prototype sur Scalingo

Une seule application Scalingo : le jar de `bnia-boot` embarque le front (profil Maven `front`) et le sert
(`FrontConfiguration`), sous les profils Spring `prod,demo`. Données de démonstration et code de connexion
`000000` comme en local (`docs/demo.md`) ; base recréée au démarrage si `V1__schema.sql` a changé
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

## Essayer en local avant de pousser

```bash
backend/mvnw -f pom.xml -DskipTests -Pfront clean package
SCALINGO_POSTGRESQL_URL=postgres://utilisateur:mot-de-passe@localhost:5432/base PORT=18080 \
  bash bin/demarrer-scalingo.sh
```

Viser une base jetable : le profil `demo` y charge ses données et la vide si V1 a changé.
