<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 18-22 -->

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

<!-- p.19 -->
### 4.3.2.1 Onglet Production
Cet onglet doit permettre de qualifier les données de production et d’activité déclarative utiles aux autres traitements. 

Il doit couvrir notamment : 

- la région; 

- les appellations; 

- le statut de déclaration de stock; 

- l’information `détenteur alambic` ; 

- la liste des alambics rattachés à l’opérateur. 

### 4.3.2.2 Onglet Diffusion
Cet onglet doit permettre de gérer les règles de diffusion des millésimes. 

Il doit permettre : 

- le niveau d’accord de diffusion des millésimes dans les statistiques BNIA : 

   - Refus : les stocks de millésimes de l’opérateur ne sont pas utilisés dans les diffusions statistiques du BNIA; 

   - Accord total; 

   - Accord partiel : seuls certains millésimes sont diffusés; 

- la sélection des millésimes diffusables en cas d’accord partiel. 

### 4.3.2.3 Onglet Marques
Cet onglet permet d’identifier les marques commercialisées par l’opérateur, et doit distinguer les sections `Marques France` et `Marques Export` . 

Pour chaque section, il doit permettre : 

- de sélectionner une marque dans le référentiel ou d’ajouter une nouvelle marque dans le référentiel si nécessaire; 

- pour chaque marque d’indiquer si l’information est diffusable sur le Web (case à cocher); 

- de choisir les circuits de distribution applicables. 

### 4.3.2.4 Onglet Visites et labels
Cet onglet doit permettre de gérer : 

- les types de visites proposées (sélection dans la table de référentiel des visites); 

- les commentaires associés à chaque visite; 

- les labels touristiques (sélection dans la table de référentiel des labels); 

- les commentaires associés à chaque label.

<!-- p.20 -->
### 4.3.2.5 Onglet Œnotourisme
Cet onglet doit permettre de gérer : 

- l’acceptation des paiements par carte bancaire (oui / non) et les types de cartes acceptées (choix multiples parmis VISA / MASTERCARD / AMERICAN EXPRESS); 

- la dégustation gratuite (case à cocher); 

- la présence d’un lieu de vente aménagé (case à cocher); 

- la possibilité d’un accueil de groupes (case à cocher); 

- les langues parlées (selection multiple); 

- les médailles et concours (saisie multiple); 

- le mode de présentation des produits (choix entre Accueil Armagnac exclusivement ou Accueil Armagnac et autres produits); 

- la sélection des produits commercialisés (choix multiple parmis la table de référenciel des produits); 

- l’adhésion œnotourisme BNIA (case à cocher); 

- les textes de présentation du domaine (champs textes français / anglais). 

### 4.3.2.6 Onglet Statistiques
Cet onglet doit permettre d’activer ou de refuser l’accès de l’opérateur au module statistiques disponible dans le portail. 

## 4.3.3 Fiche opérateur
La fiche opérateur doit servir de vue de consultation et de suivi. Elle doit reprendre les informations générales de l’opérateur et donner accès aux données liées utiles au BNIA. 

Elle doit notamment permettre : 

- de consulter les données générales et métier sans repasser par l’édition; 

- de voir l’état de la dernière déclaration; 

- d’accéder facilement à l’historique des différentes données liées à l’opérateur : DSM, factures, Contrats, etc.; 

- d’identifier les autres fiches rattachées au même groupe; 

- d’ouvrir les contacts rattachés à la structure. 

## 4.3.4 Contacts opérateurs
Le module doit permettre de gérer plusieurs contacts pour une même structure opérateur. 

Le listing des contacts doit permettre : 

- de retrouver un contact existant; 

- d’ouvrir sa fiche; 

- de créer un nouveau contact; 

- de désactiver un contact quand il ne doit plus être utilisé.

<!-- p.21 -->
La fiche contact doit permettre de gérer : 

- l’identité de la personne : nom, prénom; 

- sa fonction; 

- ses centres d’intérêt; 

- son adresse électronique; 

- son numéro de téléphone; 

- un champ d’observations; 

- son statut d’accès au portail opérateur et droits d’accès associés. 

Les centres d’intérêt qualifient le domaine d’activité du contact dans la structure, par exemple `Comptabilité` , `Marketing` ou `Production` . Ils doivent permettre de cibler les communications et les diffusions d’informations adressées aux contacts opérateurs. 

## 4.3.5 Codification des opérateurs
Le système doit codifier automatiquement les opérateurs (code opérateur BNIA) : 

- les codes des négociants sont toujours préfixés par la lettre N suivi du numéro de département de l’opérateur suivi d’un numéro d’incrément codifié sur 4 chiffres; 

- les codes producteurs sont codifiés avec un numéro d’incrément codifié sur 6 chiffres; 

- le système affecte automatiquement le numéro suivant à la création d’une fiche; 

- un contrôle d’unicité du code doit être fait en cas de saisie manuelle du code;

<!-- p.22 -->
# 4.4 Déclarations des stocks et mouvements
Ce module gère les déclarations de stocks et mouvements suivies par le BNIA. Il doit permettre de retrouver une `DSM` , de la générer pour la bonne période, de la saisir, de la contrôler, de la compléter par ses justificatifs et de la facturer. 

La fiabilité des données déclarées repose sur les contrôles de cohérence décrits dans ce chapitre. Ces contrôles conditionnent la validation de la déclaration. 

## 4.4.1 Historique des DSM
L’historique des `DSM` est le point d’entrée du module. Il doit permettre de retrouver rapidement une déclaration existante et d’ouvrir sa consultation ou sa reprise. 

Les filtres attendus portent notamment sur : 

- le code / le nom de l’opérateur; 

- la période; 

- le type d’opérateur; 

- l’état de traitement. 

Le listing doit afficher au minimum l’opérateur, la période, le type de déclarant, l’état, le total de cotisation et les repères de traitement utiles. 

## 4.4.2 Génération des DSM
La génération d’une `DSM` doit partir de l’opérateur, de son historique déclaratif et de sa périodicité paramétrée. 

Lors de la création d’une nouvelle déclaration, le système doit : 

- proposer automatiquement la période suivante à partir de la dernière déclaration connue; 

- reprendre une période cohérente avec la périodicité paramétrée pour l’opérateur; 

- empêcher tout chevauchement de période entre deux déclarations d’un même opérateur; 

- empêcher la création d’une déclaration antérieure à la dernière période déjà enregistrée; 

- empêcher la création d’un trou de déclaration entre deux périodes successives; 

- signaler explicitement toute incohérence entre la période proposée et la périodicité attendue. 

Le cycle de traitement d’une `DSM` doit au moins distinguer les états suivants : 

- En attente de saisie; 

- À valider; 

- À facturer; 

- Facturée.