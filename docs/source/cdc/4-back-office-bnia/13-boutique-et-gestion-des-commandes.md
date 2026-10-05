<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 57-59 -->

<!-- p.57 -->
## 4.13 Boutique et gestion des commandes
Le module boutique doit permettre au BNIA d’administrer une offre simple de produits destinés aux opérateurs, de suivre les commandes passées depuis le portail et d’en assurer le traitement jusqu’à la facturation. 

### 4.13.1 Catégories de produits
Le BNIA doit pouvoir gérer les catégories utilisées pour classer les produits visibles dans la boutique. 

Le module doit permettre : 

- de consulter la liste des catégories; 

- d’ajouter une catégorie; 

- de modifier une catégorie; 

- de supprimer une catégorie. 

Chaque catégorie doit comporter un intitulé. 

### 4.13.2 Produits boutique
Le module doit permettre de gérer les produits proposés à la commande. 

Le BNIA doit pouvoir : 

- consulter le listing des produits; 

- ajouter un produit; 

- modifier un produit; 

- supprimer un produit; 

- rattacher un produit à une catégorie. 

Chaque produit doit comporter : 

- une photo; 

- un intitulé; 

- un prix unitaire `HT` . 

### 4.13.3 Commandes boutique
Le listing des commandes doit permettre de retrouver les commandes passées par les opérateurs et de suivre leur état. 

Les filtres attendus portent notamment sur : 

- le numéro de commande;

<!-- p.58 -->
- l’opérateur; 

- la date de commande; 

- l’état de la commande. 

Le listing doit afficher au minimum le numéro de commande, l’opérateur, la date de commande, l’état et le montant. 

### 4.13.4 Traitement d’une commande
Le détail d’une commande doit permettre de consulter les produits commandés, les quantités et l’avancement du traitement. 

Le BNIA doit pouvoir : 

- consulter les lignes de commande; 

- faire évoluer l’état de la commande; 

- distinguer au minimum les états `en attente` , `traitée` et `facturée` ; 

- déclencher la facturation de la commande pour les profils habilités. 

### 4.13.5 Règles de gestion
La boutique reste dans un périmètre simple. 

Elle ne gère pas : 

- le choix d’un mode de livraison; 

- le paiement en ligne; 

- un parcours e-commerce complexe. 

Le détail des champs est décrit dans l’annexe Référentiel des données et objets de gestion.

<!-- p.59 -->
# 5 Portail opérateur
Le portail opérateur est l’espace privé des producteurs et négociants. Il regroupe les usages aujourd’hui répartis entre `pro.armagnac.fr` et l’espace professionnel documentaire de `armagnac.fr` , tout en restant distinct de la fiche annuaire gérée sur le site public. 

Le portail doit permettre à chaque opérateur de consulter ses informations, de réaliser ses démarches, de suivre ses documents, de passer ses commandes boutique et de retrouver les contenus mis à disposition par le BNIA dans un environnement plus simple et plus lisible. L’ergonomie du portail sera un élément majeur dans son adhésion par les opérateurs. 

## 5.1 Accès, authentification et gestion du compte
Ce module permet aux contacts opérateurs d’accéder au portail et de sécuriser leur compte. Il couvre la connexion, la récupération d’accès, la double authentification et la gestion du compte utilisateur. 

### 5.1.1 Connexion au portail
L’accès au portail doit se faire par identifiant et mot de passe. 

Le portail doit permettre : 

- l’authentification d’un contact habilité; 

- l’accès à un ou plusieurs comptes opérateurs rattachés à ce contact; 

- l’affichage d’un tableau de bord adapté aux droits du compte connecté; 

- la conservation d’un historique des connexions par compte. 

Après trois tentatives de connexion échouées, le compte doit être bloqué pendant trente minutes. 

### 5.1.2 Mot de passe oublié et première connexion
Le portail doit permettre à un utilisateur : 

- de demander la réinitialisation de son mot de passe; 

- de définir un mot de passe lors de sa première connexion; 

- de changer son mot de passe depuis son compte; 

- de recevoir les messages nécessaires par email. 

Lorsqu’un accès est créé pour un nouveau contact, un email automatique doit lui permettre d’activer son compte. Le mot de passe initial doit être changé lors de la première connexion.