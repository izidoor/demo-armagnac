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

## Essayer en local avant de pousser

```bash
backend/mvnw -f pom.xml -DskipTests -Pfront clean package
SCALINGO_POSTGRESQL_URL=postgres://utilisateur:mot-de-passe@localhost:5432/base PORT=18080 \
  bash bin/demarrer-scalingo.sh
```

Viser une base jetable : le profil `demo` y charge ses données et la vide si V1 a changé.
