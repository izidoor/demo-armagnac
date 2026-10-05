<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 14-18 -->

<!-- p.14 -->
# 4.2 Gestion des accès
Le module de gestion des accès doit permettre au BNIA d’administrer les rôles, les comptes internes et les règles de sécurité du back-office. 

## 4.2.1 Rôles utilisateurs
La gestion des rôles doit permettre de modifier les rôles existants et d’en créer de nouveaux. Chaque rôle doit comporter : 

- un nom; 

- une description; 

- un caractère d’administration globale (donnant droit d’accès à l’ensemble des fonctionnalités du back-office); 

- un ensemble de droits associés. 

## 4.2.2 Utilisateurs BNIA
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

## 4.2.3 Sécurité d’accès
Le back-office BNIA doit reposer sur un socle d’authentification sécurisé et adapté à un usage interne professionnel.

<!-- p.15 -->
## 4.2.4 Journal des accès
Le module doit permettre de consulter les connexions et les événements sensibles liés aux accès : 

- création ou désactivation d’un compte; 

- affectation ou changement de rôle; 

- tentatives de connexion échouées; 

- blocages temporaires; 

- réinitialisations d’accès. 

## 4.2.5 Catalogue des droits
Le logiciel doit fournir un catalogue de droits administrable, structuré par module et par action. 

### 4.2.5.1 Modules métier
|**Module**|**Droits**|**Description**|
|---|---|---|
|Gestion des<br>opérateurs|`Lecture`,`Écriture`|Consultation, création et mise à jour des<br>fiches opérateur, des informations métier et<br>des données de rattachement utiles aux<br>autres modules.|
|DSM|`Lecture`,`Écriture`,<br>`Facturer`|Consultation des déclarations, saisie,<br>import, correction, validation et<br>déclenchement des traitements de<br>facturation liés aux déclarations éligibles.|
|Contrats achat<br>Armagnac|`Lecture`,`Écriture`|Consultation, traitement et validation des<br>contrats achat Armagnac.|
|Contrat pluri-annuel<br>Armagnac|`Lecture`,`Écriture`|Création, modification et suivi des contrats<br>pluri-annuels Armagnac.|
|Proposition|`Lecture`,`Écriture`|Création, modification et suivi des|
|d’engagement<br>Armagnac||propositions d’engagement Armagnac.|
|Suivi des sorties<br>export|`Lecture`,`Écriture`|Consultation, création, modification et suivi<br>des sorties export.|
|Certification export|`Lecture`,`Générer`|Consultation, génération, édition PDF et<br>mise à disposition des certificats export.|
|Boutique|`Lecture`,`Écriture`,<br>`Facturer`|Gestion des catégories et produits, gestion<br>des commandes et facturation.|

<!-- p.16 -->
|**Module**|**Droits**|**Description**|
|---|---|---|
|Facturation|`Lecture`,`Écriture`|Consultation des factures et avoirs, création<br>de factures de cotisations, factures<br>manuelles, factures provisionnelles et<br>avoirs.|
|Statistiques et<br>restitutions|`Lecture`|Accès à l’ensemble des statistiques,<br>consolidations et restitutions ouvertes aux<br>équipes internes BNIA. Si nécessaire,<br>prévoir une granularité de droit d’accès par<br>export.|

### 4.2.5.2 Flux ODG et objets liés
|**Module**|**Droits**|**Description**|
|---|---|---|
|Contrat vin|`Lecture`|Consultation des contrats vin issus de<br>l’ODG et rattachés aux opérateurs BNIA.|
|Contrat pluri-annuel<br>vin|`Lecture`,`Écriture`|Consultation, qualification interne et suivi<br>des contrats pluri-annuels vin dans le<br>périmètre BNIA.|
|Proposition<br>d’engagement vin|`Lecture`,`Écriture`|Consultation, qualification interne et suivi<br>des propositions d’engagement vin dans le<br>périmètre BNIA.|
|Déclarations de fin de<br>travaux de distillation|`Lecture`|Consultation des retombées ODG utiles aux<br>contrôles métier et à la facturation de la<br>cotisation associée.|

### 4.2.5.3 Administration des accès et paramétrages sensibles
|**Périmètre**|**Droit**|**Description**|
|---|---|---|
|Gestion des<br>utilisateurs|`Gérer`|Administration des comptes utilisateurs du<br>back-office et affectation des rôles.|
|Gestion des rôles<br>utilisateurs|`Gérer`|Création, modification, activation,<br>désactivation et composition des rôles à<br>partir du catalogue de droits.|
|Taux de TVA|`Gérer`|Paramétrage des taux avec gestion des<br>périodes de validité sans chevauchement.|
|Taux de cotisation et<br>d’exonération|`Gérer`|Paramétrage des montants et pourcentages<br>impactant la facturation CVO et les fins de<br>travaux de distillation.|

<!-- p.17 -->
|**Périmètre**|**Droit**|**Description**|
|---|---|---|
|Référentiels métier|`Gérer`|Administration des pays, régions, types tiers,<br>titres, labels, visites, marques, circuits de<br>distribution, produits commercialisés,|
|||contenances, langues, produits et<br>destinataires.|

## 4.2.6 Règles de gestion
- les droits sont définis par module et par type d’action; 

- le BNIA peut créer librement ses rôles utilisateurs à partir du catalogue de droits; 

- le contenu affiché dans le back-office s’adapte aux droits effectivement attribués; 

- le droit de lecture sur un module couvre les consultations, éditions Excel/PDF et exports autorisés sur les données de ce module; 

- l’adresse électronique ou l’identifiant utilisé pour la connexion doit être unique; 

- les créations, modifications, désactivations, réactivations, changements de rôle et connexions doivent être historisés. 

Le back-office BNIA doit aussi couvrir : 

- une authentification par identifiant et mot de passe; 

- un mécanisme de mot de passe oublié avec réinitialisation sécurisée; 

- la double authentification pour les profils d’administration fonctionnelle et pour tout profil désigné par le BNIA; 

- un blocage temporaire du compte pendant `30 minutes` après `3` tentatives de connexion échouées; 

- une gestion claire des comptes inactifs, désactivés ou bloqués. 

Le module doit signaler clairement : 

- un compte bloqué temporairement; 

- un compte en attente d’activation; 

- une double authentification requise mais non activée; 

## 4.2.7 Gestion de la double authentification
La double authentification, lorsqu’elle est activée pour un utilisateur, doit permettre : 

- de recevoir un code par email 

- de paramétrer une application TOTP pour gérer le code par application 

A la connexion, l’utilisateur devra saisir le code reçu par email ou le code fournit par son application. Une option doit lui permettre de ne plus avoir à saisir la double authentification pendant 30 jours sur le navigateur actuellement utilisé.

<!-- p.18 -->
# 4.3 Gestion des opérateurs
Le module de gestion des opérateurs regroupe le référentiel des producteurs, négociants et autres structures suivies par le BNIA. Il doit permettre de retrouver rapidement un opérateur, de mettre à jour ses informations et d’accéder aux données qui lui sont liées. 

## 4.3.1 Listing des opérateurs
L’écran de listing des opérateurs doit permettre de rechercher une fiche existante, d’ouvrir sa consultation détaillée et d’accéder à la création d’un nouvel opérateur. 

Les filtres attendus portent notamment sur : 

- le type d’opérateur; 

- le code opérateur BNIA; 

- la raison sociale; 

- le nom commercial; 

- le code postal; 

- la ville; 

Le listing doit afficher les informations utiles à l’identification rapide de l’opérateur. Il doit au moins rendre visibles le type, le code, la raison sociale, le nom commercial et les coordonnées principales. Il doit aussi permettre l’ouverture directe de la fiche. 

## 4.3.2 Édition d’un opérateur
L’édition d’un opérateur doit regrouper les données générales de la structure et les informations métier, à répartir en onglets afin d’organiser de façon lisible l’ensemble des informations rattachées à la fiche. 

La saisie doit couvrir en particulier : 

- le type d’opérateur; 

- le code opérateur BNIA; 

- le groupe opérateur lorsqu’un même opérateur dispose de plusieurs fiches ou plusieurs chais; 

- ■ les identifiants de rattachement, notamment `CVI` , `SIRET` , numéro d’accise et identifiant tiers utile; 

- le titre, la raison sociale, le nom commercial et le contact principal; 

- les coordonnées postales, téléphoniques et électroniques; 

- les sites web et commerciaux; 

- les paramètres généraux de diffusion; 

- la périodicité déclarative `DSM` ; 

- les commentaires utiles au suivi interne. 

Le détail complet des champs est décrit dans l’annexe Référentiel des données et objets de gestion.