<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 52-54 -->

<!-- p.52 -->
# 4.11 Paramétrage et administration générale
Le module de paramétrage doit permettre au BNIA d’administrer les taux et les référentiels utilisés par le back-office, les contrôles métier, les éditions et certains contenus diffusés. Ces paramètres ont un impact direct sur la fiabilité des calculs, des documents et des restitutions. 

## 4.11.1 Taux de** **`TVA`
Le module doit permettre de paramétrer une classe de TVA avec le taux associé. 

Pour chaque classe de taxe, des lignes de taux sont gérés avec : 

- le taux en vigueur; 

- la date de début de validité; 

- la date de fin de validité. 

Aucun chevauchement de dates ne doit être possible entre les taux d’une même classe de taxe; 

## 4.11.2 Taux de cotisation
Le module doit permettre de gérer les paramètres de calcul utilisés pour la facturation des cotisations. 

Il doit couvrir : 

- le montant de cotisation `CVO` , exprimé en euro par `hlAP` ; 

- le pourcentage du montant exonéré de `TVA` pour cette cotisation; 

- le montant de cotisation appliqué aux fins de travaux de distillation, exprimé en euro par hectolitre de vin distillé; 

- le pourcentage du montant exonéré de `TVA` pour cette cotisation. 

Ces paramètres doivent pouvoir évoluer dans le temps sans remettre en cause l’historique des calculs déjà produits. 

## 4.11.3 Référentiels géographiques et réglementaires
Le module doit permettre d’administrer les référentiels utilisés dans les fiches opérateurs, les suivis sortie export et les certificats. 

Il doit couvrir : 

- les continents, avec code et libellé de la région ou zone commerciale; 

- les pays, avec code pays, code `ISO` , libellé, continent de rattachement, modèle de certificat associé et âge minimum d’Armagnac autorisé à l’export vers le pays; 

- les régions, avec code et libellé de la région.

<!-- p.53 -->
## 4.11.4 Référentiels de qualification
Le module doit permettre de gérer les listes de valeurs partagées par plusieurs écrans du backoffice. 

Il doit couvrir : 

- les titres; 

- les types tiers; 

- les labels; 

- les visites; 

- les langues; 

- les centres d’intérêt des contacts opérateurs. 

Ces référentiels doivent pouvoir être utilisés dans les fiches opérateurs, les contacts, les destinataires et les écrans de diffusion. 

## 4.11.5 Référentiels de présentation et de commercialisation
Le module doit aussi permettre de gérer les référentiels utilisés dans la présentation des opérateurs, les suivis sortie export et les contenus liés à l’offre. 

Il doit couvrir : 

- les marques; 

- les circuits de distribution; 

- les produits commercialisés; 

- les produits pour la facturation manuelle; 

- les contenances reconnues dans l’application. 

Le détail des champs est décrit dans l’annexe Référentiel des données et objets de gestion.

<!-- p.54 -->
# 4.12 Gestion des contenus portail
Le module de gestion des contenus portail doit permettre au BNIA d’alimenter, organiser et mettre à jour les contenus documentaires diffusés aux opérateurs dans leur espace dédié. Il couvre les actualités, les informations pratiques, les ressources et la photothèque. 

## 4.12.1 Catégories d’actualités
Le module doit permettre de gérer les catégories utilisées pour classer les actualités visibles dans le portail opérateur. 

Le BNIA doit pouvoir : 

- consulter la liste des catégories; 

- créer / modifier / supprimer une catégorie; 

- définir l’ordre d’apparition des catégories. 

Chaque catégorie doit comporter un libellé et une photo associée pour l’illustrer. 

## 4.12.2 Articles d’actualité
Le module doit permettre de gérer les articles publiés dans le portail opérateur. 

Chaque article doit permettre de renseigner : 

- la date de publication; 

- la catégorie associée; 

- une photo; 

- le titre; 

- un contenu mis en forme. 

L’éditeur de contenu doit permettre au BNIA d’appliquer les mises en forme utiles, notamment le gras, l’italique, le soulignement, les listes à puces, les couleurs, la taille de texte et les liens vers des ressources internes ou des `URL` externes. 

## 4.12.3 Interlocuteurs BNIA
Le module doit permettre de gérer la liste des interlocuteurs affichés dans les informations pratiques du portail. 

Le BNIA doit pouvoir : 

- consulter les interlocuteurs existants; 

- ajouter un interlocuteur; 

- modifier un interlocuteur; 

- supprimer un interlocuteur; 

- définir l’ordre d’apparition.