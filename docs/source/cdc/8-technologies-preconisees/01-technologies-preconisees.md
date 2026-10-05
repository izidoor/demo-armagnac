<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Technologies préconisées · pp. 89-91 -->

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

<!-- p.90 -->
- la reprise du projet par une autre agence si nécessaire. 

## 8.4 Hébergement
La solution doit reposer sur un hébergement adapté au fonctionnement d’une plateforme web métier et d’un portail opérateur. 

L’hébergement retenu doit être cohérent avec : 

- l’exploitation du back-office BNIA; 

- l’ouverture sécurisée du portail opérateur; 

- le fonctionnement des interfaces et échanges de données attendus.

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