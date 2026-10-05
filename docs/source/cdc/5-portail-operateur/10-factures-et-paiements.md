<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 74-75 -->

<!-- p.74 -->
# 5.10 Factures et paiements
Le portail doit permettre à l’opérateur de consulter ses factures, de télécharger les documents mis à disposition par le BNIA et de suivre l’état de règlement restitué par `Zeendoc` . 

## 5.10.1 Historique des factures
L’opérateur doit pouvoir retrouver l’ensemble de ses factures et avoirs. 

Le listing doit permettre au minimum : 

- la recherche par numéro; 

- la recherche par période; 

- la recherche par date; 

- la distinction entre facture de cotisation sur `DSM` , facture de cotisation sur fin de travaux de distillation, facture diverse, facture de commande boutique et avoir. 

## 5.10.2 Consultation d’une facture
Depuis le détail d’une facture, l’opérateur doit pouvoir : 

- consulter les informations principales du document : date, numéro, montant; 

- télécharger le `PDF` ; 

- identifier le type de facture et la période concernée; 

- retrouver, si nécessaire, le document lié à une `DSM` , à une déclaration de fin de travaux de distillation ou à une commande boutique. 

## 5.10.3 État de règlement
Lorsque l’information est transmise à la plateforme, le portail doit afficher l’état de règlement restitué par `Zeendoc` . 

Le portail n’a pas vocation à gérer l’encaissement. Il restitue une information de suivi utile à l’opérateur, afin d’indiqué si la facture est en attente de paiement ou payée.

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