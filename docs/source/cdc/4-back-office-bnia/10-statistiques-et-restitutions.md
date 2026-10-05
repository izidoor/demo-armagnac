<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 47-52 -->

<!-- p.47 -->
# 4.10 Statistiques et restitutions
Le module de statistiques doit permettre au BNIA d’extraire ses principaux indicateurs métier, de les consolider sur une période donnée et de les exporter sous forme de tableaux `Excel` . Il doit servir au pilotage interne, à la préparation des supports de communication et à la production de restitutions fiables à partir des données validées de la plateforme. 

> **ℹ INFO**
L’accès détaillé aux statistiques est réservé au BNIA. Seul le back-office doit donner accès aux restitutions nominatives, notamment les données à l’opérateur et la distinction entre `producteurs` et `négociants` . 

- Afin de répondre aux exigences de la répression des fraudes, les opérateurs n’ont accès, depuis le portail opérateur, qu’à des statistiques globalisées ou à des statistiques portant sur leur propre structure juridique. 

## 4.10.1 Lancement des restitutions
Le module doit proposer un point d’entrée unique pour lancer les restitutions statistiques attendues. 

Toutes les statistiques doivent pouvoir être extraites pour une plage de dates donnée. Les états de stock doivent en plus pouvoir être calculés à une date de référence. 

Le lancement d’une restitution doit permettre au minimum : 

- de choisir la famille statistique attendue; 

- de renseigner la période ou la date de référence utile; 

- de sélectionner les axes de consolidation utiles selon la restitution; 

- d’exporter le résultat sous forme de tableau `Excel` . 

## 4.10.2 Consolidation marché Armagnac
Le module doit permettre de produire une consolidation du marché Armagnac à partir des ventes France et export. 

Les indicateurs attendus sont : 

- les ventes France en `hlAP` ; 

- les ventes export en `hlAP` . 

Les dimensions attendues sont : 

- la qualité, notamment millésime et compte d’âge; 

- le pays; 

- le continent, en distinguant l’Union européenne du reste de l’Europe; 

- l’opérateur.

<!-- p.48 -->
## 4.10.3 Suivi des opérateurs** **`DSM`
Le module doit permettre au BNIA de suivre les volumes déclarés dans les `DSM` et les cotisations associées. 

Les indicateurs attendus sont : 

- les volumes de sorties en vente France; 

- les volumes de sorties en vente export; 

- les volumes de sorties en fabrication; 

- le montant de `CVO` facturé; 

- le montant de `CVO` facturé au titre des fins de travaux de distillation. 

Les dimensions attendues sont : 

- le type d’opérateur, notamment `Producteur` ou `Négociant` ; 

- l’opérateur. 

La restitution doit mettre en évidence les principaux opérateurs sur ce périmètre. 

## 4.10.4 Consolidation des achats à la propriété
Le module doit permettre de consolider les achats à la propriété sur la base : 

- des mouvements d’achat et de vente déclarés dans les `DSM` , pour les volumes; 

- des contrats d’achat Armagnac, pour la valeur. 

Les indicateurs attendus sont : 

- le nombre de contrats; 

- le volume en `hlAP` ; 

- le prix minimum d’achat en `€ / hlAP` ; 

- le prix moyen d’achat en `€ / hlAP` ; 

- le prix maximum d’achat en `€ / hlAP` . 

Les dimensions attendues sont : 

- le millésime; 

- le compte d’âge.

<!-- p.49 -->
## 4.10.5 Etat de stock des millésimes et comptes d’âge
Le module doit permettre d’établir l’état des stocks à partir des `DSM` . 

L’indicateur attendu est : 

- le stock en `hlAP` chez les opérateurs à une date donnée. 

Les dimensions attendues sont : 

- le millésime; 

- le compte d’âge. 

Cette restitution doit tenir compte du paramétrage de diffusion des millésimes défini sur les fiches opérateurs. 

## 4.10.6 Extractions filière Armagnac
Le module doit permettre de produire des extractions de synthèse sur la filière Armagnac. 

### 4.10.6.1 Production Armagnac
Cette restitution doit s’appuyer sur les déclarations de fin de travaux de distillation. 

Les indicateurs attendus sont : 

- le volume de vin distillé en `hl` ; 

- le `TAV` moyen en `% vol` ; 

- le volume d’Armagnac produit en `hlAP` . 

Les dimensions attendues sont : 

- la campagne de distillation; 

- le département; 

- l’appellation. 

### 4.10.6.2 Achat vin
Cette restitution doit s’appuyer sur les contrats vin. 

Les indicateurs attendus sont : 

- le prix au degré `hlAP` ; 

- le chiffre d’affaires global des ventes.

<!-- p.50 -->
La dimension attendue est : 

> ■ l’année. 

### 4.10.6.3 Sorties totales
Cette restitution doit s’appuyer sur les `DSM` . 

L’indicateur attendu est : 

- les sorties totales en `hlAP` . 

Les dimensions attendues sont : 

- la distinction `France` / `Export` ; 

- la distinction `Bouteille` , `Vrac` et par utilisation, notamment `Floc de Gascogne` , `Fabrications` et `Brandies` ; 

- le compte d’âge; 

- le millésime; 

- le pays; 

- la zone d’export. 

La restitution doit distinguer les sorties en bouteille et en vrac lorsqu’elle consolide les comptes d’âge et les millésimes. 

### 4.10.6.4 Répartition des ventes en bouteilles
Cette restitution doit s’appuyer sur les `DSM` . 

Les indicateurs attendus sont : 

- le stock total en `hlAP` ; 

- les sorties totales en `hlAP` . 

Les dimensions attendues sont : 

- les petits producteurs; 

- les gros producteurs; 

- les caves coopératives; 

- le négoce coopératif; 

- les négociants.

<!-- p.51 -->
## 4.10.7 Export du listing opérateurs
Le module doit permettre d’exporter : 

- la liste des négociants qui achètent ou vendent plus de `1 hl` à l’année; 

- la liste des producteurs qui achètent ou vendent plus de `1 hl` à l’année.

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