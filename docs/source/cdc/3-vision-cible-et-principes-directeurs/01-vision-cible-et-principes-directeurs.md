<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Vision cible et principes directeurs · pp. 10-12 -->

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

<!-- p.11 -->
- réutiliser une même donnée dans les écrans, les contrôles, les exports, les justificatifs et les restitutions; 

- limiter les écarts entre ce qui est saisi, ce qui est contrôlé et ce qui est restitué dans les statistiques; 

- intégrer les contrôles directement dans les parcours métier, au lieu de les laisser reposer sur des vérifications manuelles dispersées; 

- tracer les actions majeures, notamment la saisie, la correction, la validation et la mise à jour des dossiers; 

- conserver un historique suffisant pour comprendre l’origine d’une donnée, d’un statut, d’une 

   - facture, d’un contrat ou d’un certificat; 

- mettre un contrôle de données sur les saisies, afin de valider à la fois la conformité et l’exactitude des informations saisies par rapport aux règles métiers du BNIA. 

Cette exigence de fiabilité répond directement aux besoins de suivi du BNIA, à la qualité et la cohérence attendue des données statistiques et aux obligations de traçabilité liées aux échanges avec les douanes et la `DGCCRF` .

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