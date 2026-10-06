# ADR-005 : Migration Flyway unique pendant le prototype

- Statut : accepté (temporaire)
- Date : 2026-10-05

## Contexte
Le schéma évolue à chaque incrément pendant les deux semaines de prototype. Empiler des
migrations V2, V3… alourdirait l'historique sans valeur : aucune donnée à conserver.

## Décision
Un seul fichier `V1__schema.sql` dans `bnia-boot` (ADR-010), modifié sur place. Flyway
reste le seul mécanisme de création du schéma. Les données de démonstration en sont exclues
(profil `demo`, chargeur dédié).

## Conséquences
- Flyway refuse de démarrer si V1 change après application (checksum). En profil `dev`, la base
  est donc recréée automatiquement au démarrage quand V1 a changé (`MigrationDevConfiguration` :
  validation, vidage, migration ; vidage autorisé dans `application-dev.yml` seulement). Sinon
  les données de dev sont conservées. `./dev-reset.sh` reste disponible pour une remise à zéro
  volontaire (`docker compose down -v`, puis `up -d`).
- Un schéma PostgreSQL par contexte, déclaré dans `spring.flyway.schemas` et créé par Flyway, pour
  que le vidage les couvre tous.
- Les `*IT` repartent d'une base vierge : pas d'impact.
- Valable tant qu'aucune base ne doit survivre à une modification de schéma.
- Sortie : dès la première base à conserver (démo hébergée durable, premier déploiement client),
  V1 est figée et les évolutions passent en V2, V3… Un nouvel ADR actera ce passage.
