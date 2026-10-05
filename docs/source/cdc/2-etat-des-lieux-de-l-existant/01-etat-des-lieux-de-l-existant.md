<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · État des lieux de l'existant · pp. 7-10 -->

<!-- p.7 -->
# 2 État des lieux de l’existant
Le fonctionnement actuel du BNIA repose sur plusieurs outils distincts reliés par des synchronisations, des imports, des exports et des contrôles manuels. Les principaux composants de cet existant sont les suivants. 

## 2.1 Vue d’ensemble de l’écosystème actuel
|**Composant**|**Rôle principal dans**<br>**l’existant**|**Utilisateurs**<br>**principaux**|**Observations**|
|---|---|---|---|
|`InteGraal`|Logiciel métier<br>central du BNIA|Équipe BNIA|Application client /<br>serveur Windows,<br>adossée à une base<br>Oracle|
|`pro.armagnac.fr`|Extranet opérateur<br>orienté contrats et<br>consultations|Opérateurs, BNIA|Couvre<br>principalement les<br>contrats d’achat<br>Armagnac, les<br>contrats vin et<br>déclarations de fin<br>de travaux<br>synchronisés avec<br>l’ODG, ainsi qu’une<br>partie des<br>statistiques|
|`armagnac.fr`et son<br>espace<br>professionnel|Site grand public et<br>espace<br>documentaire<br>réservé|Grand public,<br>opérateurs|Porte l’annuaire<br>opérateur, les<br>contenus<br>d’information et<br>certaines ressources<br>documentaires|
|Outils ODG|Remontée de<br>données tierces vers<br>l’écosystème BNIA|ODG, BNIA,<br>opérateurs|Utilisés notamment<br>pour les contrats vin<br>et les déclarations<br>de fin de travaux|
|`Zeendoc`|Diffusion<br>documentaire et<br>transmission de<br>factures|BNIA, opérateurs,<br>comptabilité|Utilisé pour le dépôt<br>des factures,<br>transmission aux<br>clients. Pas<br>d’interconnexion<br>automatisée avec les<br>systèmes actuels.|

<!-- p.8 -->
|**Composant**|**Rôle principal dans**<br>**l’existant**|**Utilisateurs**<br>**principaux**|**Observations**|
|---|---|---|---|
|Outils satellites|Comptabilité<br>InteGraal, BI interne,<br>ancien gestionnaire<br>de chais|BNIA, partenaires|Présents dans<br>l’écosystème actuel<br>comme composants<br>périphériques. À ne<br>pas prendre en<br>compte dans le<br>périmètre du cahier<br>des charges les<br>modules étant<br>abandonnés ou<br>remplacés par des<br>outils tiers hors<br>périmètre.|

## 2.2 Logiciel métier BNIA actuel :** **`InteGraal`
`InteGraal` constitue aujourd’hui le point d’entrée principal du travail interne du BNIA. 

Le périmètre actuellement géré dans `InteGraal` comprend notamment : 

- la gestion des fiches opérateur, des contacts et de plusieurs informations de diffusion, d’identification et d’activité; 

- la saisie et l’historisation des déclarations de stocks et mouvements; 

- la reprise et l’exploitation des contrats d’achat à la propriété issus de l’extranet; 

- la saisie des suivis sorties export; 

- la génération des certificats export; 

- la facturation des cotisations et la facturation manuelle; 

- les éditions statistiques et les consolidations; 

- plusieurs référentiels et paramètres métier utiles au fonctionnement quotidien. 

Le logiciel centralise une part importante de la donnée BNIA. Il ne couvre pas à lui seul l’ensemble des usages. 

Une même opération de distillation à façon est aujourd’hui portée par deux objets distincts : un contrat `Distillation à façon` dans `InteGraal` et une déclaration de fin de travaux dans l’extranet `pro.armagnac.fr` . Ces deux objets décrivent la même opération. Le dispositif cible doit les unifier en une seule déclaration de fin de travaux de distillation. 

## 2.3 Espaces numériques actuels à destination des opérateurs
### 2.3.1 Extranet** **`pro.armagnac.fr`
L’extranet `pro.armagnac.fr` constitue aujourd’hui le principal espace transactionnel mis à disposition des opérateurs en dehors du logiciel interne BNIA.

<!-- p.9 -->
Il permet notamment : 

- la saisie et la validation des contrats d’achat Armagnac; 

- la saisie des contrats pluriannuels Armagnac; 

- la saisie des propositions d’engagement Armagnac; 

- la consultation des contrats vin et des déclarations de fin de travaux remontés depuis l’ODG; 

- l’accès à un sous-ensemble de statistiques à destination des opérateurs; 

Cet extranet joue un rôle central dans la relation opérationnelle avec les producteurs et négociants. Il ne couvre qu’une partie des besoins documentaires et ne concentre pas l’ensemble des données utiles à un opérateur. 

### 2.3.2 Site** **`armagnac.fr` et espace professionnel documentaire
Le site `armagnac.fr` relève d’abord d’un usage grand public. 

Son écosystème comprend également un espace professionnel réservé, utilisé pour : 

- la mise à disposition de ressources documentaires; 

- la diffusion d’actualités et d’informations pratiques; 

- l’accès à une photothèque ou à des contenus de communication; 

- la gestion de certaines informations liées à la fiche annuaire opérateur. 

## 2.4 Dépendances externes et flux associés
Le fonctionnement actuel dépend aussi d’interfaces et de flux externes qui structurent plusieurs processus métier : 

- les outils ODG alimentent l’écosystème BNIA pour les contrats vin et les déclarations de fin de travaux; 

- des justificatifs et données issues des douanes, dont les DAE, interviennent dans les traitements liés aux mouvements et à l’export; 

- certaines déclarations, restitutions et pièces justificatives circulent encore hors application, par exemple au moyen de fichiers Excel, d’impressions papier, d’envois par courriel ou de dépôts documentaires; 

- `Zeendoc` intervient pour la diffusion des factures et leur transmission vers la comptabilité;

<!-- p.10 -->
# 3 Vision cible et principes directeurs
La nouvelle plateforme BNIA doit remplacer une organisation aujourd’hui répartie entre `InteGraal` , `pro.armagnac.fr` , un espace documentaire séparé et plusieurs échanges manuels. Elle doit donner au BNIA et aux opérateurs un outil plus simple à utiliser, plus fiable et plus lisible au quotidien. 

## 3.1 Objectifs fonctionnels
La nouvelle plateforme BNIA doit couvrir dans un même ensemble : 

- le back-office BNIA pour la gestion des opérateurs, des `DSM` , des contrats, de l’export, de la facturation, des statistiques, du paramétrage; 

- le portail opérateur pour les démarches métier, la consultation des documents, le suivi des 

   - dossiers, l’accès à la boutique et les notifications; 

- les interfaces utiles avec l’ `ODG` , `Zeendoc` , les justificatifs douaniers et les imports ou exports nécessaires; 

- la reprise des données indispensables au démarrage. 

## 3.2 Objectifs opérationnels
La nouvelle plateforme BNIA doit faire gagner du temps aux équipes du BNIA et aux opérateurs. Elle doit en priorité : 

- réduire les saisies et ressaisies manuelles, en particulier pour les `DSM` et les suivis sorties export (anciennement Contrat Export dans `InteGraal` ); 

- centraliser dans un même outil les informations, les statuts, les justificatifs et les actions à traiter; 

- éviter les recherches dans plusieurs applications, fichiers `Excel` , courriels ou dépôts documentaires pour instruire un dossier; 

- rendre immédiatement visibles les déclarations à saisir, à contrôler, à valider ou à facturer; 

- permettre aux opérateurs de réaliser plus directement leurs démarches dans le portail, avec une saisie lisible et un meilleur suivi de leurs demandes; 

- apporter de nouveaux outils aux opérateurs. 

## 3.3 Objectifs de fiabilité et de traçabilité
La nouvelle plateforme BNIA doit améliorer la fiabilité de la donnée utilisée dans les traitements et les statistiques. 

Elle doit :