<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 75-77 -->

<!-- p.75 -->
# 5.11 Statistiques
Le portail doit permettre aux opérateurs de consulter les statistiques lorsque l’accès leur est autorisé par le BNIA sur le paramétrage de leur fiche. Le module statistiques couvre uniquement les restitutions diffusables dans l’espace opérateur et reste distinct des consolidations internes du BNIA. 

Les statistiques portail doivent être calculées uniquement avec les données des opérateurs ayant donné leur autorisation d’utilisation statistique, notamment sur la diffusion des millésimes. 

Les différentes statistiques sont consultables sous forme de tableaux à l’écran et exportables au format Excel. 
> **ℹ INFO**
Afin de répondre aux exigences de la répression des fraudes, les opérateurs n’ont accès qu’à des statistiques globalisées ou à des statistiques portant sur leur propre structure juridique. 

L’accès détaillé aux statistiques, notamment les données à l’opérateur et la distinction entre `producteurs` et `négociants` , est réservé au BNIA et n’est accessible que depuis le back-office. 

## 5.11.1 Consultation des statistiques autorisées
L’accès au module doit être conditionné : 

- aux droits attribués au contact portail; 

- à l’autorisation statistique définie par le BNIA sur la fiche opérateur. 

Le portail doit afficher uniquement les restitutions autorisées pour le compte opérateur actif. 

## 5.11.2 Classement des ventes France en bouteille
Le portail doit proposer une restitution des ventes en bouteille à partir des `DSM` . 

L’indicateur attendu est : 

- le total des ventes en bouteille en `hlAP` . 

Les dimensions attendues sont : 

- l’année; 

- le compte d’âge; 

- le millésime.

<!-- p.76 -->
## 5.11.3 Exportations par compte d’âge
Le portail doit proposer une restitution des exportations à partir des suivis sortie export. 

L’indicateur attendu est : 

- le volume exporté en `hlAP` . 

La dimension attendue est : 

- le pays de destination.

<!-- p.77 -->
# 5.12 Notifications et relances
Le portail doit permettre aux opérateurs de recevoir et consulter les notifications liées à leurs démarches. Il couvre à la fois les alertes visibles dans l’interface et les messages adressés aux contacts définis pour le compte opérateur. 

## 5.12.1 Centre de notifications
Le portail doit proposer un espace de consultation des notifications, avec au minimum : 

- la liste des notifications reçues; 

- la date d’émission; 

- le type de notification; 

- le lien vers l’élément concerné (contrat, DSM, etc.); 

- l’état lu ou non lu. 

## 5.12.2 Notifications métier attendues
Les notifications attendues pour les opérateurs portent notamment sur : 

- une déclaration en attente; 

- une relance sur `DSM` ou justificatif; 

- la mise à disposition d’une nouvelle facture; 

- la mise à disposition d’un nouveau certificat; 

- un contrat en attente de validation par l’opérateur; 

- la validation d’un contrat Armagnac par le BNIA. 

## 5.12.3 Relances
Le système doit permettre au BNIA d’émettre des relances lorsque l’opérateur n’a pas réalisé l’action attendue dans les délais prévus. 

Les relances concernent : 

- les `DSM` en attente de saisie ou validation; 

- les justificatifs manquants sur les DSM; 

- les `DRM` manquantes sur la période déclarée, pour les producteurs. 

## 5.12.4 Destinataires
Le portail doit permettre d’identifier quels contacts reçoivent les différents types de notifications. 

La définition de ces destinataires dépend du module Gestion des contacts.