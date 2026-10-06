# ADR-004 : Spring Data JDBC en écriture, SQL direct en lecture

- Statut : accepté
- Date : 2026-10-05

## Contexte
Le domaine doit rester pur (ADR-002). Les agrégats DDD se chargent et se sauvegardent d'un bloc.
Le CDC demande de nombreux listings filtrés et des consolidations statistiques (hlAP par qualité,
pays, opérateur…) qui n'ont rien à voir avec les agrégats.

## Décision
- Écriture : Spring Data JDBC, un repository par racine d'agrégat. Les classes de persistance
  (`*Entity` ou `*Row`) vivent dans la couche `adapter.persistence` du contexte (ADR-010) et sont mappées vers le domaine.
- Lecture : `JdbcClient` en SQL direct. Des ports de lecture, définis dans `application`,
  retournent des modèles de lecture dédiés, jamais des agrégats.

## Conséquences
- Pas de lazy loading ni de proxy : le chargement d'un agrégat est prévisible.
- Un mapping explicite domaine ↔ persistance à écrire et à tester.
- Les statistiques et listings sont optimisables en SQL sans toucher au modèle d'écriture.
- Séparation lecture / écriture légère (sans bus de commandes ni base distincte).
