<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 137-140 -->

<!-- p.137 -->
# D Cartographie des interfaces et flux
Cette annexe présente la cartographie cible des échanges entre la plateforme BNIA, ses environnements utilisateurs et les outils tiers conservés dans le périmètre du cahier des charges. 

## D.1 Vue d’ensemble
*[Schéma supprimé (OCR illisible) : voir le PDF à cette page]*

_Figure D.1_ 

La plateforme BNIA s’articule autour de trois ensembles : 

- le `Back-office BNIA` , utilise par les utilisateurs BNIA pour la gestion, les controles et le suivi des traitements 

- les `Portails operateurs` , utilises par les producteurs et negociants pour la consultation, la saisie et le depot des elements attendus 

- l’ `API et flux d'interconnexion` , utilisee pour gerer les echanges avec les outils tiers de la plateforme 

Le flux `ODG` couvre notamment : 

- les contrats vin 

- les declarations de fin de travaux de distillation 

## D.2 Flux concernés

<!-- p.138 -->
|**Source**<br>Utilisateurs<br>`BNIA`|**Destination**<br>`Back-office`<br>`BNIA`|**Flux**<br>Consultation,<br>saisie et<br>contrôle|**Sens**<br>Bidirectionnel|**Fonction**<br>Permettre au<br>BNIA d’instruire<br>les dossiers, de<br>valider les<br>traitements et<br>de piloter les<br>modules métier.|
|---|---|---|---|---|
|Producteurs /<br>Négociants|`Portails`<br>`opérateurs`|Consultation,<br>saisie, dépôt et<br>suivi|Bidirectionnel|Permettre aux<br>opérateurs<br>d’accéder à<br>leurs services,<br>de déposer<br>leurs éléments<br>et de suivre<br>leurs<br>démarches.|
|`Logiciel`<br>`métier ODG`|`API et flux`<br>`d'interconnexion`|Remontée des<br>contrats vin|Entrant vers la<br>plateforme|Mettre à<br>disposition du<br>BNIA les<br>contrats vin<br>suivis côté`ODG`,<br>sans ressaisie<br>dans la<br>plateforme<br>BNIA.|
|`Logiciel`<br>`métier ODG`|`API et flux`<br>`d'interconnexion`|Remontée des<br>déclarations de<br>fin de travaux<br>de distillation|Entrant vers la<br>plateforme|Alimenter le<br>BNIA pour le<br>suivi de la<br>distillation et la<br>facturation de la<br>cotisation<br>associée.|
|`API et flux`<br>`d'interconnexion`|`Zeendoc`|Dépôt des<br>factures`PDF`|Sortant depuis<br>la plateforme|Transmettre les<br>factures vers<br>`Zeendoc`pour<br>diffusion,<br>traitement<br>comptable et<br>facturation<br>électronique.|
|`Zeendoc`|`API et flux`<br>`d'interconnexion`|Retour d’état de<br>paiement|Entrant vers la<br>plateforme|Restituer dans<br>la plateforme<br>l’état de<br>règlement<br>associé à une<br>facture.|

<!-- p.139 -->
> **⚠ ATTENTION**
Le flux `ODG` est à sens unique vers le BNIA. Les modalités techniques d’échange devront être arrêtées avec le prestataire au moment du remplacement d’ `Innovagro` par `TimeTonic` . 

> **⚠ ATTENTION**
La plateforme BNIA doit conserver ses propres documents et son propre historique. `Zeendoc` intervient comme outil tiers de diffusion et de retour d’état, sans devenir la source métier des factures.

<!-- p.140 -->
# E Exemples de fichiers
Cette annexe recense les exemples de fichiers et de documents joints au cahier des charges pour illustrer les formats actuellement utilisés ou attendus. 

Ces exemples sont fournis comme pièces jointes externes au dossier de consultation. Ils ne sont pas reproduits dans le présent document. 

Les pièces jointes prévues à ce stade sont les suivantes : 

|**Nom du fichier**|**Fonction**|
|---|---|
|`ENVOI_DSM_NEGOCIANT.xlsx`|Matrice`DSM`servant de base au format unique<br>de saisie et d’import.|
|`ENVOI_DSM_PRODUCTEUR.xlsx`|Même matrice appliquée à un opérateur de<br>type producteur, avec les colonnes hors<br>périmètre grisées.|
|`ENVOI_DSM_PRODUCTEUR_ANCIEN_FORMAT.xlsx`|Matrice`DSM`producteur actuelle, fournie à titre<br>de comparaison avec le format unique retenu.|
|`ENVOI_VENTES_EXPORT.xlsx`|Matrice proposée pour la saisie et l’import des<br>ventes export, construite sur le style de la<br>matrice`DSM`négociant.|
|`FACT CVO NORMALE.pdf`|Exemple de facture`CVO`mise à disposition de<br>l’opérateur.|
|`FACTURE DISTILLATION.pdf`|Exemple de facture de cotisation sur fin de<br>travaux de distillation.|
|`FACT MANUELLE PROVISION.pdf`|Exemple de facture manuelle ou<br>provisionnelle.|
|`CERTIF EXPORT.pdf`|Exemple de certificat export au format<br>standard.|
|`CERTIF EXPORT DETAILS.pdf`|Exemple de certificat export avec détail des<br>produits.|
|`CERTIF EXPORT DETAILS VRAC.pdf`|Exemple de certificat export avec détail<br>adapté aux flux vrac.|
|`EXEMPLE DAE 1.pdf`|Exemple de document justificatif de type`DAE`<br>utilisable en appui des contrôles export ou<br>`DSM`.|
|`EXEMPLE DAE 2.pdf`|Second exemple de document justificatif de<br>type`DAE`.|
|`EXEMPLE JUSTIFICATIF EXPORT 1.pdf`|Exemple de justificatif export transmis pour<br>une demande de certificat export.|
|`EXEMPLE JUSTIFICATIF EXPORT 2.pdf`|Second exemple de justificatif export transmis<br>pour une demande de certificat export.|