<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 12-14 -->

<!-- p.12 -->
# 4 Back-office BNIA
Le back-office BNIA constitue l’environnement de référence pour les équipes internes. Il doit regrouper les traitements de gestion, de contrôle, de validation et de suivi aujourd’hui répartis entre `InteGraal` , `pro.armagnac.fr` , `armagnac.fr` et des circuits manuels. 

Il couvre : 

- l’accueil utilisateur et le suivi des actions à traiter; 

- la gestion des habilitations et des opérateurs; 

- les traitements métier liés aux déclarations, contrats, certifications, facturations et restitutions; 

- les fonctions transverses de paramétrage, d’administration, de gestion des contenus diffusés sur le portail opérateur et de gestion de la boutique. 

## 4.1 Accueil / dashboard BNIA
L’accueil du back-office BNIA doit montrer immédiatement les éléments à traiter et donner accès aux écrans correspondants. 

Le tableau de bord doit rester simple. Les indicateurs affichés sont des accès directs vers les listings ou écrans de traitement correspondants. 

|**Bloc ou indicateur**|**Contenu affiché**|**Action accessible**|
|---|---|---|
|Opérateurs|Affichage du nombre<br>d’opérateurs, avec distinction<br>entre`producteurs`et<br>`négociants`.|Accès à un listing préfiltré ou<br>à la recherche opérateur du<br>moduleGestion des<br>opérateurs.|
|DSM en attente de saisie|Nombre de déclarations à<br>compléter.|Ouverture d’une liste des<br>éléments à saisir ou à<br>relancer.|
|DSM en attente de validation|Nombre de déclarations à<br>contrôler ou valider.|Accès direct à la liste des`DSM`<br>à traiter.|
|DSM en attente de<br>facturation|Nombre de déclarations<br>validées mais non encore<br>intégrées dans le circuit de<br>facturation.|Accès à une vue filtrée des<br>éléments à facturer.|
|Contrats Armagnac en<br>attente de validation|Nombre de contrats<br>nécessitant un contrôle ou<br>une validation BNIA.|Accès direct au listing des<br>contrats en attente.|
|Demande de certificat export|Nombre de demandes de<br>certificat export en attente de<br>traitement.|Accès direct au listing des<br>demandes en attente.|

<!-- p.13 -->
|**Bloc ou indicateur**|**Contenu affiché**|**Action accessible**|
|---|---|---|
|Commandes boutique en|Nombre de commandes en|Accès direct au listing des|
|<br>attente|attente de traitement ou de<br>facturation.|<br>commandes boutique à<br>traiter.|

> **ℹ INFO**
Le tableau de bord doit s’adapter aux droits de l’utilisateur connecté. Il ne doit afficher que les blocs, compteurs et accès rapides correspondant aux habilitations effectives de ce profil et ne doit exposer aucune donnée qu’il n’est pas autorisé à consulter.

<!-- p.14 -->
## 4.2 Gestion des accès
Le module de gestion des accès doit permettre au BNIA d’administrer les rôles, les comptes internes et les règles de sécurité du back-office. 

### 4.2.1 Rôles utilisateurs
La gestion des rôles doit permettre de modifier les rôles existants et d’en créer de nouveaux. Chaque rôle doit comporter : 

- un nom; 

- une description; 

- un caractère d’administration globale (donnant droit d’accès à l’ensemble des fonctionnalités du back-office); 

- un ensemble de droits associés. 

### 4.2.2 Utilisateurs BNIA
La gestion des utilisateurs doit permettre de rechercher un compte interne, d’en créer un nouveau et d’en suivre l’état. 

Les filtres attendus portent notamment sur : 

- le nom; 

- le prénom; 

- l’adresse électronique professionnelle; 

- le rôle; 

- le statut du compte (actif / inactif). 

La création ou la modification d’un utilisateur doit permettre de gérer : 

- le prénom et le nom; 

- l’adresse électronique professionnelle; 

- le rôle attribué; 

- le statut du compte; 

- l’obligation éventuelle de double authentification; 

- les informations de suivi utiles à l’administration. 

Le détail des champs est décrit dans l’annexe Référentiel des données et objets de gestion. 

### 4.2.3 Sécurité d’accès
Le back-office BNIA doit reposer sur un socle d’authentification sécurisé et adapté à un usage interne professionnel.