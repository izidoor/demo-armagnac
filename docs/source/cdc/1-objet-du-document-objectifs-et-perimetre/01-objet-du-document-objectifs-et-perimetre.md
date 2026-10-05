<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Objet du document, objectifs et périmètre · pp. 5-7 -->

<!-- p.5 -->
# 1 Objet du document, objectifs et périmètre
## 1.1 Objet du document
Le présent cahier des charges définit les attentes du BNIA pour la nouvelle plateforme BNIA, couvrant le back-office BNIA, le portail opérateur, les interfaces avec les systèmes tiers et la reprise des données historiques. 

Il sert de base de consultation pour : 

- décrire les besoins fonctionnels à couvrir; 

- fixer le périmètre de chaque environnement utilisateur; 

- décrire les données, contrôles, restitutions et interfaces attendus; 

- cadrer la consultation des prestataires, le chiffrage, la mise en oeuvre et la recette. 

## 1.2 Le BNIA et son cadre d’intervention
Le BNIA, Bureau National Interprofessionnel de l’Armagnac, intervient au service de la filière Armagnac. Il coordonne des missions utiles au fonctionnement de la filière, à la défense de l’appellation et à la promotion de l’Armagnac. 

À ce titre, il doit disposer d’outils lui permettant de suivre les opérateurs, de gérer les flux métier de son périmètre, de produire les documents attendus et d’assurer la facturation des cotisations concernées. Il doit aussi fiabiliser les données, tracer les traitements réalisés et conserver les justificatifs utiles, notamment pour répondre à ses obligations de contrôle et de conformité. 

## 1.3 Périmètre couvert
Le périmètre du présent cahier des charges couvre : 

- le Back-office BNIA, dans le périmètre décrit au chapitre Back-office BNIA ; 

- le portail opérateur, dans le périmètre décrit au chapitre Portail opérateur ; 

- les passerelles et interfaces avec les systèmes tiers, dans le périmètre décrit au chapitre Passerelles et interfaces ; 

- la reprise des données nécessaires à la mise en service de la nouvelle plateforme BNIA, dans le périmètre décrit au chapitre Récupération de données et dans le document autonome dédié. 

## 1.4 Limites et exclusions de périmètre
Le présent cahier des charges n’inclut pas : 

- l’évolution détaillée du site public `armagnac.fr` ;

<!-- p.6 -->
- la gestion de la fiche opérateur alimentant l’annuaire public; 

- le détail technique de la reprise de données, traité dans un document complémentaire dédié au chiffrage et à la préparation des extractions.

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