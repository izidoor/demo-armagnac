# Plateforme BNIA : prototype

Ce dépôt contient un **prototype** de plateforme métier pour le BNIA (Bureau National Interprofessionnel de
l'Armagnac). Il accompagne une réponse à l'appel d'offres du BNIA pour remplacer Integraal et pro.armagnac.fr.

La plateforme a deux espaces, avec une seule connexion :

- le **back-office**, pour le personnel du BNIA ;
- le **portail**, pour les opérateurs de la filière (producteurs et négociants), utilisable aussi sur
  téléphone et tablette.

## Ce qu'on peut y faire

- **Opérateurs** : le BNIA tient la fiche de chaque opérateur ; chaque opérateur gère lui-même les
  utilisateurs de son entreprise et leurs droits.
- **DSM** : l'opérateur saisit sa déclaration à l'écran ou l'importe depuis le fichier Excel du BNIA, joint
  ses justificatifs et l'envoie ; le BNIA l'instruit, demande des compléments ou la valide.
- **Facturation** : la cotisation est calculée, le BNIA émet les factures, que l'opérateur retrouve sur le
  portail.
- **Contrats** : producteurs et négociants concluent leurs contrats d'achat d'armagnac sur la plateforme.
- **Messagerie** : le BNIA et les opérateurs échangent directement, à propos d'une DSM, d'une facture ou de
  tout autre sujet.

## Essayer la démonstration

- Adresse : **[bnia-demo.osc-fr1.scalingo.io](https://bnia-demo.osc-fr1.scalingo.io)**

Les données sont fictives : tout peut être essayé sans crainte. Aucun e-mail n'est réellement envoyé.

## Des données qui appartiennent au BNIA

Le code est ouvert, et toutes les données de la plateforme se téléchargent à tout moment depuis le
back-office (Paramètres, « Données »).

La documentation détaillée, module par module, est dans [docs/specs](docs/specs).
