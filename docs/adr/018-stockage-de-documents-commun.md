# ADR-018 : Stockage de documents commun

- Statut : accepté
- Date : 2026-10-13
- Spec : `docs/specs/05-declarations/spec.md` (décision A-DEC-5)

## Contexte
Les DSM reçoivent des fichiers déposés : justificatifs (factures, DAE, avoirs, déclarations de
fabrication) et DRM des producteurs, au moins douze par producteur et par an (spec 05, RG-DEC-26 à 28).
D'autres contextes auront le même besoin : pièces des demandes de certificat et certificats PDF
(`exportation`), factures et avoirs PDF (`facturation`). Aucun stockage de fichiers n'existe dans
l'application.

Le domaine n'a pas à connaître le contenu d'un fichier : il lui faut une référence (clé, nom, type,
taille) et la garantie qu'un document référencé existe.

## Décision
- **Un port commun.** `StockageDocuments` (`deposer`, `lire`, `supprimer`) dans
  `fr.bnia.commun.application` (`bnia-commun-core`) ; l'objet-valeur `DocumentDepose` (clé, nom, type,
  taille) dans `fr.bnia.commun.domain` (ADR-009). Les contextes ne stockent que le `DocumentDepose`.
- **Adapter PostgreSQL.** Table `documents.document` (identifiant, nom, type, taille, contenu `bytea`,
  déposé le), dans `fr.bnia.commun.adapter.persistence` (`bnia-commun-infra`) ; schéma `documents` ajouté
  à `spring.flyway.schemas`. Le dépôt s'exécute dans la transaction de l'appelant : un document et sa
  référence sont écrits ou annulés ensemble.
- **Garde-fous dans l'adapter** : 10 Mo au plus par fichier ; un PDF attendu est vérifié par sa
  signature (`%PDF`), pas par son extension.
- Le téléchargement passe par le contexte propriétaire de la référence, qui vérifie les droits
  (ADR-016) avant de lire le contenu.

Options écartées :
- **stockage objet S3 (MinIO en dev et en test)** : la forme standard en production, mais un service de
  plus partout, et un stockage hors transaction qui laisse des fichiers orphelins à nettoyer ;
- **répertoire disque** : sauvegarde à part, volume partagé dès deux instances, sans gain sur
  PostgreSQL pour le prototype.

## Conséquences
- La base grossit avec les documents ; la sauvegarde de la base les couvre.
- Passer à S3 ne touche que l'adapter (et un nettoyage des orphelins) : ni le domaine ni les cas
  d'usage ne changent.
- La suppression d'une référence supprime le document dans la même transaction.
