<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Récupération de données · pp. 84-89 -->

<!-- p.84 -->
# 7 Récupération de données
La récupération des données constitue un volet à part entière du projet. Elle doit permettre d’alimenter la future plateforme avec les historiques, référentiels et contenus utiles issus de l’existant, sans perte des rattachements nécessaires au fonctionnement métier. 

Le présent chapitre cadre les sources à reprendre dans le cahier des charges principal. Le détail des vues, des colonnes attendues et des modalités de mise à disposition des données AGI (logiciel InteGraal et extranet `pro.armagnac.fr` ) est traité dans le document autonome `project/reprise-donnees/` . 

|**Source**|**Périmètre de récupération**<br>**attendu**|**Finalité principale**|
|---|---|---|
|`InteGraal`|Référentiels, données de<br>gestion et paramétrages du<br>back-office BNIA|Alimenter le futur back-office<br>BNIA|
|`pro.armagnac.fr`|Objets métier saisis ou suivis<br>côté opérateur|Reprendre les historiques<br>utiles au portail opérateur et<br>au back-office|
|`armagnac.fr`|Contenus documentaires<br>utiles au futur portail<br>opérateur|Maintenir la continuité des<br>informations mises à<br>disposition des opérateurs|

> **⚠ ATTENTION**
La récupération des données ne couvre pas la reprise à l’identique de tous les outils existants. Le site public `armagnac.fr` et la gestion de la fiche annuaire opérateur restent hors du périmètre de la plateforme BNIA. 

## 7.1 Récupération des données InteGraal
`InteGraal` constitue la source historique principale pour les données de gestion du BNIA. La récupération doit s’appuyer sur des vues `SQL` permettant d’accéder aux données à jour, en particulier pour préparer la bascule vers la future solution. 

### 7.1.1 Référentiel des opérateurs
La récupération des données `InteGraal` couvre le référentiel des opérateurs et ses sousensembles utiles au futur dispositif : 

- informations générales; 

- agréments alambic; 

- accords de diffusion des millésimes; 

- dérogations Armagnac à `40 % vol` ;

<!-- p.85 -->
- marques France; 

- circuits de distribution France; 

- marques export; 

- circuits de distribution export; 

- visites; 

- labels touristiques; 

- langues parlées; 

- médailles et concours; 

- produits commercialisés; 

- contacts. 

### 7.1.2 Données de gestion
La récupération couvre également les objets métier structurants du back-office BNIA : 

- déclarations de stocks et mouvements; 

- factures; 

- contrats d’achat à la propriété; 

- suivis sorties export; 

- destinataires associés aux sorties export; 

- vendeurs. 

Pour les lignes de `DSM` , la structure reprise doit permettre de conserver la clé métier fondée sur la dénomination, l’année, le conditionnement `MB` / `MV` / `NB` / `NV` et le compte d’âge. 

Pour les factures, la récupération doit conserver les liaisons utiles avec les `DSM` et les contrats d’achat à la propriété, notamment lorsque ces liens servent de base aux cotisations. 

Les contrats `InteGraal` de type `Distillation à façon` décrivent la même opération que les déclarations de fin de travaux de l’extranet `pro.armagnac.fr` . Les deux sources doivent être reprises dans l’objet unique de la plateforme cible, la déclaration de fin de travaux de distillation, sans créer de doublon. 

### 7.1.3 Paramétrages à reprendre
La récupération des données `InteGraal` couvre les paramétrages suivants : 

- pays; 

- régions; 

- titres; 

- types de tiers; 

- labels; 

- visites; 

- cotisations; 

- contenances; 

- langues. 

La structure reprise des cotisations doit permettre d’identifier distinctement les règles applicables aux négociants et aux producteurs.

<!-- p.86 -->
### 7.1.4 Attendus de mise à disposition
Les données issues de `InteGraal` doivent être mises à disposition dans une forme documentée, exploitable et cohérente avec les objets fonctionnels décrits dans le présent cahier des charges. 

Le détail du découpage des vues et des colonnes attendues relève du document autonome dédié à la récupération des données. Celui-ci pourra être ajusté en fonction des aspects techniques propres à la solution AGI. 
> **ℹ INFO**
Une prise de contact sera nécessaire avec `AGI` pour définir les modalités d’accès aux vues SQL et ajuster leur paramétrage si besoin.

<!-- p.87 -->
## 7.2 Récupération des données Extranet pro.armagnac.fr
L’extranet `pro.armagnac.fr` porte une partie des objets métier saisis ou suivis par les opérateurs. Leur récupération est nécessaire pour préserver les historiques utiles au futur portail opérateur et au back-office BNIA. 

### 7.2.1 Objets à reprendre
La récupération des données de `pro.armagnac.fr` couvre : 

- les contrats Armagnac, avec leurs informations générales et leurs lignes produits; 

- les contrats pluri-annuels Armagnac; 

- les propositions d’engagement Armagnac; 

- les contrats vin, avec leurs informations générales et leurs lignes produits; 

- les contrats pluri-annuels vin; 

- les propositions d’engagement vin, avec leurs informations générales et leurs lignes produits; 

- les déclarations de fin de travaux de distillation, avec leurs informations générales et leurs lignes produits; 

- les utilisateurs de l’extranet. 

### 7.2.2 Attendus de récupération
La récupération doit conserver la structure métier de ces objets ainsi que leur rattachement aux opérateurs et aux utilisateurs concernés. 

Elle doit permettre de retrouver les historiques utiles dans la future plateforme, sans rompre les liens entre les données saisies côté opérateur et les traitements instruits par le BNIA. 

Le détail des vues, des colonnes attendues et des modalités d’extraction est traité dans le document autonome de récupération des données. 

> **ℹ INFO**
Une prise de contact sera nécessaire avec `AGI` pour définir les modalités d’accès aux vues SQL et ajuster leur paramétrage si besoin.

<!-- p.88 -->
## 7.3 Récupération des données armagnac.fr
Le site `armagnac.fr` reste en place pour les contenus publics. Dans le cadre du présent projet, la récupération attendue concerne uniquement les contenus utiles au futur portail opérateur. 

### 7.3.1 Contenus à reprendre
La récupération des données de `armagnac.fr` couvre les contenus documentaires, pratiques et de boutique destinés aux opérateurs, notamment : 

- les actualités (catégories / articles / photos articles); 

- les informations pratiques; 

- les ressources documentaires (catégories / documents); 

- la photothèque (catégories / photos). 

- les données de la boutique (catégories / produits / photos). 

### 7.3.2 Hors périmètre
La récupération ne couvre pas la reprise fonctionnelle du site `armagnac.fr` . 

La gestion de la fiche annuaire opérateur reste rattachée à `armagnac.fr` et demeure hors du périmètre de la plateforme BNIA. 

> **⚠ ATTENTION**
La récupération des contenus utiles au portail opérateur dépend d’un échange avec l’agence en charge de `armagnac.fr` , afin d’identifier les données à reprendre et les modules à masquer ou à maintenir hors du futur portail BNIA, et établir avec eux un calendrier pour la désactivation des fonctionnalités qui ne feront plus partie du champ fonctionnel du site `armagnac.fr` .

<!-- p.89 -->
# 8 Technologies préconisées
La future plateforme doit reposer sur un socle web moderne, maintenable et exploitable dans la durée. Les choix techniques retenus doivent aussi faciliter la reprise du projet par un autre prestataire si nécessaire. 

Le présent chapitre fixe les orientations technologiques préconisées pour les interfaces, l’API applicative et l’hébergement. 

## 8.1 Plateforme web cible
Le futur dispositif doit être conçu comme une plateforme web unique, couvrant à la fois le backoffice BNIA et le portail opérateur. 

Cette orientation implique : 

- des interfaces accessibles via navigateur; 

- un socle applicatif commun pour les deux environnements; 

- une architecture compatible avec les échanges de données et interfaces décrits au chapitre Passerelles et interfaces. 

## 8.2 Interfaces utilisateur
Les interfaces utilisateur doivent être développées avec des technologies web modernes de type `React` / `Next.js` . 

Ce socle doit permettre : 

- une navigation fluide; 

- des interfaces réactives pour les usages de saisie, de consultation et de contrôle; 

- une base technique cohérente entre le back-office BNIA et le portail opérateur. 

## 8.3 API applicative
La plateforme doit s’appuyer sur une API pour l’interconnexion avec la base de données et les autres composants du dispositif. 

Cette API doit être développée sur un framework back moderne, largement diffusé et maintenable, de type `Symfony` , `Laravel` ou équivalent. 

Le choix retenu doit faciliter : 

- la lisibilité de l’architecture applicative; 

- la maintenabilité du projet; 

- l’optimisation des coûts de développement pour les briques communes entre le back-office et le portail opérateurs;