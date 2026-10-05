<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 78-80 -->

<!-- p.78 -->
## 5.13 Gestion des contacts
Ce module permet à l’opérateur de gérer son profil dans le portail et d’administrer les accès de ses collaborateurs. Il repose sur un compte administrateur par défaut, habilité à gérer les autres accès du compte opérateur. 

### 5.13.1 Profil opérateur
Le portail doit permettre de consulter et de mettre à jour, pour les champs autorisés dans le portail, les informations du profil opérateur utiles à la relation avec le BNIA. 

### 5.13.2 Contacts et habilitations locales
Le contact administrateur doit pouvoir gérer les accès des collaborateurs rattachés à l’opérateur. 

Le module doit permettre : 

- de lister les contacts existants; 

- de créer un contact; 

- de modifier un contact; 

- de désactiver un contact; 

- d’affecter les droits d’accès par module; 

- de rattacher un même contact à plusieurs comptes opérateurs lorsque le besoin existe. 

Chaque contact doit permettre de renseigner au minimum : 

- l’adresse électronique; 

- le nom; 

- le prénom; 

- le téléphone; 

- la fonction; 

- l’état d’activation de l’accès portail; 

- le niveau d’administration ou les droits attribués. 

Les droits pouvant être attribués : 

- gestion des contacts; 

- gestion des DSM; 

- gestion des contrats Armagnac; 

- gestion des pluri-annuels Armagnac; 

- gestion des propositions d’engagement Armagnac; 

- consultation des contrats Vin; 

- gestion des pluri-annuels Vin; 

- gestion des propositions d’engagement Vin; 

- consultation des déclarations de fin de travaux de distillation; 

- accès à la boutique et aux commandes; 

- accès aux factures;

<!-- p.79 -->
- gestion des demandes de certificats Export. 

Le contenu portail (actualités, ressources, photothèque, informations pratiques) est accessible à l’ensemble des comptes, sans droit d’accès particulier. 

Le contact administrateur créé par défaut dispose de l’accès à l’ensemble des modules du portail. Il doit pouvoir déléguer des droits plus restreints à d’autres contacts. 

### 5.13.3 Règles de gestion
Le module doit respecter les règles suivantes : 

- l’adresse électronique est obligatoire pour activer un accès portail; 

- une même adresse électronique ne doit correspondre qu’à un seul compte utilisateur; 

- la création d’un accès doit déclencher l’envoi d’un message d’activation; 

- les créations, modifications, désactivations et changements de droits doivent être historisés.

<!-- p.80 -->
# 6 Passerelles et interfaces
La plateforme cible doit intégrer les échanges tiers qui restent nécessaires au fonctionnement métier du BNIA. Dans le périmètre du présent cahier des charges, les passerelles externes explicitement attendues concernent le flux `ODG` et l’interconnexion avec `Zeendoc` . 

Le dispositif doit garantir la traçabilité des échanges, la lisibilité des anomalies et la capacité de contrôle par le BNIA. Le détail de la reprise des données historiques est traité dans le chapitre Récupération des données. 

## 6.1 ODG
La passerelle `ODG` couvre les données métier suivies dans l’outil `ODG` puis exploitées par le BNIA. Elle alimente le back-office BNIA et le portail opérateur sans ouvrir de ressaisie dans la plateforme BNIA sur les objets synchronisés. 

### 6.1.1 Finalité
La passerelle `ODG` doit permettre : 

- de synchroniser vers la plateforme BNIA les contrats vin; 

- de synchroniser vers la plateforme BNIA les déclarations de fin de travaux de distillation; 

- de rattacher chaque flux à la bonne fiche opérateur BNIA; 

- de mettre les données synchronisées à disposition du back-office BNIA et du portail opérateur; 

- de déclencher les traitements BNIA qui dépendent de ces objets, notamment les contrôles métier et la facturation des cotisations sur fin de travaux de distillation. 

### 6.1.2 Sens du flux et systèmes concernés
Le flux est descendant : 

- système source : outil métier `ODG` ; 

- système cible : plateforme BNIA; 

- sens du flux : `ODG` vers `BNIA` ; 

- usage côté BNIA : consultation, rapprochement, contrôle et exploitation des traitements liés. 

Le flux actuel repose sur `Innovagro` . Le dispositif cible doit rester compatible avec l’outil appelé à le remplacer.