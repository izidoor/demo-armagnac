<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 59-61 -->

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

<!-- p.60 -->
### 5.1.3 Double authentification
Le portail doit proposer une double authentification pour renforcer la sécurité des accès. 

Cette double authentification doit pouvoir s’appuyer sur : 

- un code transmis par courrier électronique; 

- ou une application de type `TOTP` . 

### 5.1.4 Comptes accessibles et multi-chais
Un même compte utilisateur doit pouvoir être rattaché à plusieurs comptes opérateurs. Ce besoin concerne notamment les structures disposant de plusieurs chais ou entrepôts. 

Le portail doit permettre : 

- d’identifier clairement le compte opérateur actif; 

- de basculer d’un compte opérateur à un autre lorsque le contact y est habilité; 

- d’appliquer les droits du contact pour chaque compte rattaché; 

- d’éviter la création de plusieurs comptes utilisateur pour une même adresse électronique. 

### 5.1.5 Mon compte
Chaque utilisateur doit disposer d’un espace personnel permettant au minimum : 

- de consulter ses informations de compte; 

- de modifier son mot de passe; 

- de gérer l’activation de la double authentification; 

- de visualiser les comptes opérateurs auxquels il a accès. 

### 5.1.6 Règles de gestion
Le module doit respecter les règles suivantes : 

- une adresse email ne doit correspondre qu’à un seul compte utilisateur; 

- un compte désactivé ne doit plus pouvoir ouvrir de session; 

- les règles de blocage de compte doivent être tracées; 

- les connexions et actions majeures doivent être historisées avec horodatage et identifiant du compte.

<!-- p.61 -->
## 5.2 Tableau de bord portail
Le tableau de bord est l’écran d’entrée du portail opérateur. Il doit permettre à un contact habilité d’identifier immédiatement ses démarches en attente, ses derniers documents et les informations utiles à son activité. 

### 5.2.1 Contenu du tableau de bord
Le tableau de bord doit s’adapter aux droits de l’utilisateur et au compte opérateur actif. 

Les blocs attendus sont les suivants. 

|**Bloc ou**<br>**indicateur**|**Contenu affiché**|**Action accessible**|
|---|---|---|
|Dernière`DSM`<br>reçue|Période, état et repère de dernière<br>déclaration traitée.|Accès direct à l’historique du<br>moduleDéclarations de stocks et<br>mouvements.|
|Alertes`DSM`|Déclarations en attente, justificatifs<br>attendus ou action demandée par<br>le BNIA.|Ouverture d’une vue filtrée sur les<br>déclarations concernées.|
|Contrats à suivre|Contrats Armagnac à valider,<br>contrats saisis récemment.|Accès au moduleContrats<br>Armagnac.|
|Dernières<br>factures|Factures récemment mises à<br>disposition et état de règlement<br>restitué par`Zeendoc`.|Accès au moduleFactures et<br>paiements.|
|Derniers<br>certificats|Certificats récemment délivrés par<br>le BNIA.|Accès au moduleCertificats export.|
|Dernières|Actualités publiées pour les|Accès au moduleContenus portail|
|actualités|opérateurs.|et ressources.|

### 5.2.2 Navigation rapide
Depuis le tableau de bord, l’utilisateur doit pouvoir : 

- ouvrir le bon module sans refaire de recherche; 

- retrouver les démarches qui demandent une action; 

- visualiser les derniers documents mis à disposition; 

- accéder rapidement aux actualités et ressources utiles; 

- accéder facilement aux informations de contact du BNIA si besoin d’assistance.