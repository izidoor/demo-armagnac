<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Passerelles et interfaces · pp. 80-84 -->

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

<!-- p.81 -->
### 6.1.3 Données échangées
Les échanges doivent couvrir les objets suivants : 

- les contrats vin suivis par l’ `ODG` ; 

- les déclarations de fin de travaux de distillation. 

Chaque déclaration de fin de travaux de distillation donne lieu à la création d’un seul objet dans la plateforme BNIA. Aucun contrat n’est généré en complément. 

Le détail fonctionnel des objets synchronisés est décrit dans les chapitres Contrats d’achat à la propriété, Contrats vin et Déclarations de fin de travaux de distillation, ainsi que dans l’annexe Référentiel des données et objets de gestion. 

### 6.1.4 Rattachement opérateur
La table de correspondance entre identifiants `ODG` et identifiants BNIA est géré côté `ODG` et est alimenté via un accès dédié par une ressource interne au BNIA. L’ `ODG` synchronise les données vers le BNIA une fois qu’elle a connaissance du/des codes opérateurs BNIA rattachés à l’élément à synchroniser. 

### 6.1.5 Contrôles, doublons et rejets
La passerelle doit intégrer des contrôles de cohérence sur : 

- la présence des identifiants de rattachement; 

- la complétude des références de dossier et des parties; 

- la cohérence des dates et des lignes produit utiles aux traitements BNIA; 

- la détection des resynchronisations d’un objet déjà reçu; 

- la distinction entre création, mise à jour et rejet. 

La gestion des doublons est un point de vigilance majeur sur les déclarations de fin de travaux de distillation. La plateforme doit empêcher la création de doublons lorsqu’une même déclaration est revalidée ou retransmise par l’outil `ODG` , tout en conservant la traçabilité des événements reçus. 

### 6.1.6 Supervision et journalisation
La passerelle doit permettre au BNIA de suivre : 

- la date et l’heure de chaque synchronisation; 

- le nombre d’objets reçus, intégrés, mis à jour et rejetés; 

- le motif des anomalies détectées; 

- l’historique des rejets, corrections de correspondance et relances de traitement.

<!-- p.82 -->
Les anomalies d’interface doivent être consultables dans un écran ou un journal dédié, avec les éléments utiles à la reprise du traitement. 

> **ℹ INFO**
Une prise de contact sera nécessaire avec `Timetonic` ou l’intégrateur de la solution pour déterminer les aspects techniques nécessaires à la mise en oeuvre de cette passerelle.

<!-- p.83 -->
## 6.2 Zeendoc
`Zeendoc` l’outil SaaS utilisé par le BNIA pour gérer le traitement de sa facturation. 

### 6.2.1 Finalité
La passerelle `Zeendoc` doit permettre : 

- de transmettre automatiquement les factures `PDF` émises par la plateforme BNIA, afin d’assurer le traitement par `Zeendoc` , la diffusion par email au client, la transmission comptable et la facturation électronique; 

- de restituer dans la plateforme BNIA l’état de traitement ou de règlement renvoyé par `Zeendoc` lorsqu’il est disponible; 

- de conserver côté BNIA les documents mis à disposition du portail opérateur et du back-office. 

### 6.2.2 Sens du flux et systèmes concernés
La passerelle repose sur deux échanges : 

- un flux sortant `BNIA` vers `Zeendoc` pour le dépôt des factures `PDF` ; 

- un flux retour `Zeendoc` vers `BNIA` ou intérogation par la plateforme `BNIA` de `Zeendoc` pour la restitution de l’état de traitement ou de règlement associé à une facture. 

> **ℹ INFO**
Une prise de contact sera nécessaire avec `Zeendoc` ou le revendeur de la solution pour disposer des éléments techniques nécessaires à la mise en oeuvre de cette passerelle. 

### 6.2.3 Contrôles, erreurs et supervision
La passerelle doit permettre : 

- de distinguer une facture en attente de dépôt, déposée, rejetée ou en anomalie; 

- d’identifier un échec de transmission du `PDF` ; 

- de rejouer un dépôt en cas d’échec sans créer de doublon documentaire; 

- d’historiser les retours de statut reçus depuis `Zeendoc` ; 

- de suivre les anomalies d’interface dans un journal exploitable par le BNIA. 

Les erreurs de dépôt ou de retour d’état ne doivent pas bloquer silencieusement la consultation de la facture dans la plateforme BNIA.

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