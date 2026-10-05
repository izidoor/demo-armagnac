<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Priorisation, déploiement et conclusion · pp. 91-94 -->

<!-- p.91 -->
# 9 Priorisation, déploiement et conclusion
La cible fonctionnelle décrite dans le présent cahier des charges est unifiée, mais sa mise en œuvre doit être organisée de façon progressive. Le déploiement retenu doit d’abord sécuriser la continuité des traitements indispensables, la qualité des données et l’ouverture simultanée du back-office BNIA et du portail opérateur. 

Le présent chapitre fixe les principes de priorisation, le périmètre du premier temps de mise en service et la logique de déploiement des compléments ultérieurs. 

## 9.1 Principes de priorisation
La priorisation du projet doit respecter les principes suivants : 

- ouvrir en priorité les fonctions nécessaires à la continuité des traitements obligatoires, récurrents ou structurants pour le BNIA et les opérateurs; 

- traiter en premier les fonctions qui réduisent les ressaisies, fiabilisent les calculs et sécurisent les justificatifs; 

- mettre en service au même moment le Back-office BNIA et le portail opérateur sur leur socle fonctionnel indispensable; 

- distinguer les fonctions qui conditionnent l’ouverture de la plateforme de celles qui peuvent être déployées après la mise en service sans rupture métier; 

- prévoir, pendant la phase de démarrage, des modalités de contrôle ou de secours lorsque certaines automatisations ou interfaces décrites au chapitre Passerelles et interfaces ne sont pas encore pleinement industrialisées. 

## 9.2 Volets du déploiement
Le déploiement du projet s’organise autour de quatre volets complémentaires : 

|**Volet**|**Finalité dans le déploiement**|
|---|---|
|Back-office BNIA|Mettre à disposition l’environnement de<br>gestion, de contrôle et de suivi des équipes<br>internes|
|Portail opérateur|Ouvrir un point d’accès unique pour les<br>démarches, documents et suivis opérateur|
|Récupération des données`InteGraal`et<br>`pro.armagnac.fr`|Alimenter la mise en service avec les<br>référentiels, historiques et objets métier utiles|
|Récupération des contenus utiles issus de<br>`armagnac.fr`|Maintenir la continuité des informations et<br>ressources reprises dans le portail|

<!-- p.92 -->
Les deux volets de récupération ne constituent pas des phases indépendantes après l’ouverture de la plateforme. Ils alimentent directement la mise en service initiale et doivent être préparés comme des prérequis du lancement. 

## 9.3 Premier temps de mise en service
Le premier temps de mise en service doit permettre d’ouvrir la nouvelle plateforme sans rupture sur les fonctions métier vitales. Il couvre un socle commun `BNIA + portail opérateur` , alimenté par les reprises de données nécessaires au démarrage. 

### 9.3.1 Socle métier côté BNIA
Le socle initial du back-office BNIA doit couvrir : 

- la gestion des opérateurs; 

- la gestion des accès et le paramétrage nécessaire au fonctionnement courant; 

- les déclarations de stocks et mouvements; 

- les contrats d’achat à la propriété, pour les parcours Armagnac et vin déjà décrits dans le présent cahier des charges; 

- le bloc export, incluant le suivi des sorties export et les certificats export; 

- la facturation et les cotisations; 

- les contrôles, historiques et justificatifs nécessaires à l’instruction de ces dossiers. 

### 9.3.2 Socle métier côté portail opérateur
Le socle initial du portail opérateur doit couvrir : 

- l’accès, l’authentification et la gestion du compte; 

- la gestion des contacts liés au compte opérateur; 

- la consultation et la saisie des parcours métier rattachés aux déclarations, contrats, certificats et factures du socle initial; 

- la consultation des documents et statuts utiles au suivi de ces parcours; 

- les notifications et relances nécessaires au fonctionnement courant. 

### 9.3.3 Données et contenus nécessaires au lancement
Le premier temps de mise en service doit s’appuyer sur : 

- la récupération, au jour de lancement, des données `InteGraal` et `pro.armagnac.fr` néces- 

   - saires au fonctionnement du back-office et du portail; 

- la reprise des contenus et ressources `armagnac.fr` utiles dès l’ouverture du portail opérateur; 

- des contrôles de reprise permettant de vérifier les rattachements, les historiques, les calculs et les principaux parcours métier.

<!-- p.93 -->
> **⚠ ATTENTION**
La mise en service initiale ne doit pas dépendre de la reprise exhaustive de tous les contenus existants ni de l’automatisation complète de toutes les interfaces. Le démarrage doit rester possible dès lors que les processus du socle initial sont opérationnels, contrôlés et exploitables. 

## 9.4 Déploiement des compléments ultérieurs
Les fonctions qui n’empêchent pas l’ouverture de la plateforme peuvent être déployées après la mise en service initiale, dans une logique de montée en puissance progressive. 

Les sujets relevant d’un temps ultérieur comprennent notamment : 

- les imports informatiques avancés pour les `DSM` , au-delà des parcours nécessaires au démarrage; 

- les statistiques et restitutions complémentaires; 

- les enrichissements de contenus et de services portail au-delà du socle initial; 

- les synchronisations plus riches avec `Zeendoc` et les autres outils tiers; 

- les reprises complémentaires de contenus `armagnac.fr` qui ne conditionnent pas les usages métier d’ouverture; 

- les évolutions complémentaires du bloc export et les autres enrichissements fonctionnels ultérieurs. 

## 9.5 Déploiement de la bascule
Le déploiement de la bascule doit être conduit selon l’enchaînement suivant : 

1. préparer le paramétrage cible, les contrôles de reprise, les jeux de données à migrer et les modalités transitoires nécessaires; 

2. réaliser la récupération des données et contenus utiles au lancement; 

3. ouvrir simultanément le back-office BNIA et le portail opérateur sur le socle initial; 

4. contrôler les principaux parcours métier, les calculs, les justificatifs et les statuts après bascule; 

5. fermer progressivement les usages de l’existant une fois la continuité opérationnelle confirmée; 

6. déployer ensuite les fonctions complémentaires selon les priorités validées par le BNIA. 

## 9.6 Conclusion de cadrage
La plateforme cible doit être livrée comme un dispositif cohérent, mais sa mise en œuvre doit rester progressive. Le premier temps de mise en service doit couvrir simultanément le back-office BNIA, le portail opérateur et les reprises de données utiles au lancement. 

Le déploiement retenu doit donc concentrer l’effort initial sur les fonctions indispensables, la qualité des données et la sécurité de la bascule. Les enrichissements complémentaires peuvent être déployés après ouverture sans remettre en cause la cible fonctionnelle du projet.

<!-- p.94 -->
# A Glossaire et sigles
Cette annexe regroupe les principaux termes métier, noms d’outils et sigles utilisés dans le cahier des charges. Elle vise à faciliter la lecture du document et à limiter les ambiguïtés de vocabulaire. 

## A.1 Termes métier et outils
|**Terme**|**Définition**|
|---|---|
|`armagnac.fr`|Site du BNIA comprenant le site public et un espace professionnel<br>documentaire. Une partie de ses contenus utiles doit être reprise<br>dans le futur portail opérateur.|
|Assemblage|Mélange de plusieurs Armagnacs. Le compte d’âge retenu est celui<br>de l’eau-de-vie la plus jeune utilisée.|
|Back-office BNIA|Environnement de travail interne du BNIA. Il regroupe les fonctions de<br>gestion, de contrôle, de validation, de facturation et de suivi.|
|Campagne de<br>distillation|Période pendant laquelle la distillation peut être réalisée. Elle<br>commence le 1er septembre d’une année et se termine le 31 mars de<br>l’année suivante.|
|Certificat d’âge export|Document établi à partir d’un suivi sortie export, valant attestation<br>d’origine pour certains envois à l’export. Un certificat correspond à un<br>pays de destination.|
|Compte d’âge|Indication de vieillissement d’un Armagnac, liée au temps passé sous<br>bois et utilisée dans les suivis, contrats et certificats. La règle de<br>calcul est précisée dans l’annexe @sec-annexe-comptes-age.|
|Contrat pluri-annuel|Contrat engageant les parties sur plusieurs campagnes ou années,<br>avec des règles propres de durée et de rattachement éventuel à des<br>contrats annuels.|
|Déclaration de fin de<br>travaux de distillation|Donnée issue de l’ODG documentant une opération de distillation à<br>façon. Elle est l’objet unique de suivi, de contrôle et de facturation de<br>la cotisation associée.|
|Distillation à façon|Opération de distillation réalisée pour le compte d’un opérateur. Elle<br>est documentée par une déclaration de fin de travaux de distillation et<br>ne donne pas lieu à un contrat distinct.|
|Fiche annuaire<br>opérateur|Fiche de présentation d’un opérateur sur`armagnac.fr`. Sa gestion<br>reste hors du périmètre direct de la plateforme cible.|
|`InteGraal`|Logiciel métier actuel du BNIA. Il couvre notamment la gestion des<br>opérateurs, les`DSM`, les sorties export, les certificats export et une<br>partie de la facturation.|