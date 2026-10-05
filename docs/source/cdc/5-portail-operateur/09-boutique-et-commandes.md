<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 73-74 -->

<!-- p.73 -->
# 5.9 Boutique et commandes
Le portail doit permettre aux opérateurs d’accéder à la boutique du BNIA, de passer des commandes de goodies et d’en suivre l’historique. 

## 5.9.1 Accès à la boutique
L’accès à la boutique doit permettre de consulter les produits disponibles classés par catégorie. 

Pour chaque produit, le portail doit afficher au minimum : 

- la catégorie; 

- la photo; 

- l’intitulé; 

- le prix unitaire `HT` . 

## 5.9.2 Passage de commande
Le portail doit permettre de saisir une quantité par produit et de valider une commande. 

La commande doit permettre de transmettre au BNIA : 

- le compte opérateur concerné; 

- les produits sélectionnés; 

- les quantités demandées; 

- la date de la commande. 

## 5.9.3 Historique des commandes
L’opérateur doit pouvoir retrouver l’historique de ses commandes. 

Le listing doit permettre au minimum : 

- la consultation du numéro de commande; 

- la consultation de la date de commande; 

- la consultation de l’état de la commande; 

- la consultation du détail des produits commandés et des quantités. 

Lorsqu’une commande a donné lieu à une facture, le portail doit permettre d’identifier cette facturation et de retrouver la facture correspondante dans le module Factures et paiements. 

## 5.9.4 Règles de gestion
La boutique ne gère pas de paiement en ligne ni de choix de mode de livraison dans le portail.

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