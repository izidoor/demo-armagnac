# ADR-005 : Migration Flyway unique pendant le prototype

- Statut : accepté (temporaire)
- Date : 2026-10-05

## Contexte
Le schéma évolue à chaque incrément pendant les deux semaines de prototype. Empiler des
migrations V2, V3… alourdirait l'historique sans valeur : aucune donnée à conserver.

## Décision
Un seul fichier `V1__schema.sql` dans `bnia-adapter-persistence`, modifié sur place. Flyway
reste le seul mécanisme de création du schéma. Les données de démonstration en sont exclues
(profil `demo`, chargeur dédié).

## Conséquences
- Flyway refuse de démarrer si V1 change après application (checksum). En dev, la base est
  recréée par `./dev-reset.sh` (`docker compose down -v`, puis `up -d`).
- Les `*IT` repartent d'une base vierge : pas d'impact.
- Valable tant qu'aucune base ne doit survivre à une modification de schéma.
- Sortie : dès la première base à conserver (démo hébergée durable, premier déploiement client),
  V1 est figée et les évolutions passent en V2, V3… Un nouvel ADR actera ce passage.
