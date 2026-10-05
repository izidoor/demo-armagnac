<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 69-71 -->

<!-- p.69 -->
# 5.6 Contrats vin
Le portail doit regrouper les informations vin utiles aux opérateurs. Il couvre la consultation des contrats vin issus de l’ `ODG` , la saisie des pluri-annuels vin, la saisie des propositions d’engagement vin et la consultation des déclarations de fin de travaux de distillation. 

## 5.6.1 Contrat vin
Les contrats vin issus de l’ `ODG` sont en consultation seule dans le portail. 

L’opérateur doit pouvoir : 

- lister les contrats qui le concernent; 

- rechercher un contrat par date, numéro ou acheteur/vendeur; 

- ouvrir le détail d’un contrat; 

- éditer ou télécharger le document lorsqu’il est disponible. 

Le portail ne doit pas permettre de modifier les données synchronisées depuis l’ `ODG` . 

## 5.6.2 Pluri-annuel vin
Le portail doit permettre la consultation et la création des contrats pluri-annuels vin. 

> **⚠ ATTENTION**
Ce module fait actuellement l’objet d’une discussion au sein du BNIA. Son maintien dans le périmètre du projet sera arbitré prochainement. 

La saisie doit permettre de renseigner : 

- les parties au contrat; 

- l’objet du contrat; 

- les volumes annuels; 

- les caractéristiques produit; 

- les conditions de retiraison; 

- les modalités de prix et de paiement. 

Les champs sont les mêmes que pour la saisie côté back-office BNIA. Un opérateur qui saisi un pluri-annuel doit obligatoirement s’identifier comme acheteur ou vendeur. 

## 5.6.3 Proposition d’engagement vin
Le portail doit permettre la consultation et la création des propositions d’engagement vin lorsqu’elles relèvent du périmètre opérateur. 

Les champs sont les mêmes que pour la saisie côté back-office BNIA. Un opérateur qui saisi une proposition d’engagement doit obligatoirement s’identifier comme acheteur ou vendeur.

<!-- p.70 -->
## 5.6.4 Déclarations de fin de travaux de distillation
Les déclarations de fin de travaux de distillation issues de l’ `ODG` doivent être consultables dans le portail lorsqu’elles concernent l’opérateur. 

Le portail doit permettre : 

- de retrouver une déclaration de fin de travaux de distillation; 

- d’en consulter le détail; 

- d’identifier les informations utiles au suivi de la distillation et de la cotisation associée. 

Ces déclarations sont en consultation seule dans le portail. 

## 5.6.5 Règles de gestion
Le module doit respecter les règles suivantes : 

- les flux `ODG` en consultation ne sont pas modifiables dans le portail opérateur; 

- l’opérateur ne doit voir que les contrats / pluri-annuels / propositions d’engagement sur lesquels il est identifié comme acheteur ou vendeur; 

- les saisies opérateur validées ou transmises ne doivent plus être modifiables par l’opérateur; 

- les actions de création et de validation doivent être historisées.

<!-- p.71 -->
# 5.7 Ventes export
Le portail doit permettre à l’opérateur de transmettre au BNIA le détail de ses ventes à l’export. Ces données alimentent le suivi sortie export et les statistiques export. 

## 5.7.1 Saisie et import
Le portail doit permettre : 

- la saisie directe des lignes de vente export; 

- le téléchargement du modèle `Excel` BNIA; 

- l’import du fichier `Excel` complété; 

- la consultation des lignes déjà transmises et de leur état. 

Le modèle `Excel` et les colonnes attendues sont décrits dans le module Suivi des sorties export du back-office BNIA. 

## 5.7.2 Contrôles et transmission
Le portail doit appliquer les mêmes contrôles que le back-office BNIA, en saisie manuelle comme après import d’un fichier. 

Les anomalies doivent être restituées avec le motif et la ligne concernée. L’opérateur doit pouvoir corriger sa saisie ou réimporter un fichier corrigé. 

La transmission n’est possible qu’une fois les anomalies levées. Les lignes transmises sont ensuite mises à disposition du BNIA, qui conserve la validation définitive du suivi sortie export.