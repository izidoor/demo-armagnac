<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 77-78 -->

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

<!-- p.78 -->
# 5.13 Gestion des contacts
Ce module permet à l’opérateur de gérer son profil dans le portail et d’administrer les accès de ses collaborateurs. Il repose sur un compte administrateur par défaut, habilité à gérer les autres accès du compte opérateur. 

## 5.13.1 Profil opérateur
Le portail doit permettre de consulter et de mettre à jour, pour les champs autorisés dans le portail, les informations du profil opérateur utiles à la relation avec le BNIA. 

## 5.13.2 Contacts et habilitations locales
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