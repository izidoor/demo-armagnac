<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 44-47 -->

<!-- p.44 -->
# 4.9 Facturation et cotisations
Le module de facturation doit permettre au BNIA d’émettre les factures de cotisations sur `DSM` , les factures de cotisations sur fin de travaux de distillation, les factures diverses, les factures issues des commandes boutique et les avoirs, puis d’en assurer la diffusion documentaire et le suivi de transmission vers `Zeendoc` . 

## 4.9.1 Historique des factures
L’historique des factures est le point d’entrée du module. Il doit permettre de retrouver rapidement une facture existante, d’ouvrir son détail et de consulter son `PDF` . 

Les filtres attendus portent notamment sur : 

- le numéro de facture; 

- le type de facture; 

- l’opérateur facturé; 

- la période facturée; 

- la date de facture. 

Le listing doit afficher au minimum le numéro de facture, sa date, le tiers facturé, le type de facture, les montants `HT` , `TVA` et `TTC` . 

## 4.9.2 Référentiel produits
La codification des produits permet d’identifier les produits utilisés à alimenter les factures manuelles. 

Ils sont caractérisés par : 

- un code; 

- une dénomination; 

- un taux de TVA. 

## 4.9.3 Facturation des cotisations sur** **`DSM`
La facturation des cotisations doit pouvoir être déclenchée à partir d’une `DSM` . 

Les volumes qui servent de base de calcul pour la cotisation sont : 

### ■ pour les DSM négociant : 

   - les sorties transfert chai Armagnac hors région délimitée (colonne 14 de la DSM); 

   - les ventes France hors chai Armagnac (colonnes 17, 18, 19 de la DSM); 

   - les ventes France négoce hors région délimitée (colonne 21 de la DSM); 

   - les ventes export (colonnes 22, 23, 24, 25 de la DSM). 

- pour les DSM producteur :

<!-- p.45 -->
- les sorties d’utilisation de l’Armagnac pour autres fabrications que l’AOC (colonne 16 de la DSM); 

- les ventes marché intérieur hors chai Armagnac (colonnes 17, 18 de la DSM); 

- les ventes négoce hors région délimitée (colonne 21 de la DSM); 

- les ventes export (colonnes 22, 23 de la DSM). 

Le module doit permettre : 

- de reprendre automatiquement le montant de cotisation calculé sur la déclaration; 

- de modifier le libellé de la période facturée lorsque cela est nécessaire; 

- d’appliquer la `TVA` en tenant compte du pourcentage d’exonération paramétré; 

- de conserver le lien entre la facture et la `DSM` d’origine. 

## 4.9.4 Facturation des cotisations sur fin de travaux de distillation
La facturation doit pouvoir être déclenchée à partir d’une déclaration de fin de travaux de distillation. 

Le module doit permettre : 

- de calculer la cotisation à partir du volume de vin distillé porté par la déclaration; 

- d’appliquer le taux de cotisation paramétré pour ce cas d’usage; 

- d’appliquer la `TVA` en tenant compte du pourcentage d’exonération paramétré; 

- de conserver le lien entre la facture et la déclaration d’origine. 

## 4.9.5 Facturation des commandes boutique
La facturation d’une commande boutique doit pouvoir être déclenchée à partir d’une commande validée dans le module Boutique et gestion des commandes. 

Le module doit permettre : 

- de reprendre automatiquement l’opérateur facturé, les lignes de commande, les quantités et les prix unitaires; 

- d’appliquer la `TVA` selon le paramétrage du ou des produits concernés; 

- de conserver le lien entre la facture et la commande boutique d’origine; 

- de mettre à jour l’état de la commande lorsqu’elle passe au statut `facturée` . 

## 4.9.6 Facturation manuelle
Le module doit permettre de créer des factures manuelles pour les besoins hors cotisations, notamment pour la PLV, les goodies ou d’autres ventes diverses du BNIA. 

La saisie doit permettre de choisir : 

- le tiers facturé; 

- le ou les produits facturés;

<!-- p.46 -->
- les quantités; 

- les prix unitaires; 

- le taux de `TVA` applicable selon le produit. 

## 4.9.7 Avoirs
Le module doit permettre de gérer : 

- les avoirs manuels; 

- les avoirs générés à partir d’une facture existante, sur la totalité ou une partie de la facture sélectionnée; 

Dans le cadre d’un avoir sur une facture de cotisation, le calcul de `TVA` d’un avoir doit reprendre la logique d’exonération ou de taux appliquée à la facture d’origine. 

## 4.9.8 Diffusion des factures
Chaque facture doit être éditée au format `PDF` . 

Le module doit permettre : 

- de conserver le `PDF` dans la plateforme BNIA; 

- de mettre la facture à disposition sur le portail opérateur concerné par la facture; 

- de transmettre automatiquement le `PDF` à `Zeendoc` ; 

- de récupérer depuis `Zeendoc` , sur la base du numéro de facture, l’état de traitement ou de paiement restitué par cet outil. 

Il n’y a pas d’export comptable à prévoir depuis la plateforme BNIA. Le traitement comptable et documentaire réalisé dans `Zeendoc` reste hors du périmètre de la plateforme BNIA. 

> **⚠ ATTENTION**
Le modèle de facture cible ne doit pas reprendre les modèles actuels de cotisation et de fin de travaux de distillation lorsqu’ils nuisent à la lisibilité ou à l’exploitation automatisée des documents. Un nouveau modèle `PDF` doit être prévu.

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