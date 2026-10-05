<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 94-98 -->

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

<!-- p.95 -->
|**Terme**|**Définition**|
|---|---|
|Justificatif|Pièce fournie, déposée ou rapprochée pour prouver un mouvement,<br>une déclaration ou une opération métier. Il peut s’agir par exemple<br>d’un contrat, d’une facture, d’une`DAE`ou d’un document douanier.|
|Millésime|Année de la campagne de distillation servant à identifier un produit. Il<br>intervient dans les règles de suivi et de restitution.|
|Opérateur|Producteur, négociant ou autre acteur métier disposant d’une fiche<br>dans le référentiel BNIA et pouvant interagir avec le portail opérateur.|
|`Otentik`|Plateforme douanière d’authentification des documents. Elle permet<br>de vérifier un certificat export`CEV`par lecture de son`QR code`.|
|Portail opérateur|Espace privé destiné aux opérateurs. Il regroupe les parcours de<br>consultation, de saisie, de dépôt de justificatifs et d’accès aux<br>informations mises à disposition par le BNIA.|
|`pro.armagnac.fr`|Extranet opérateur actuel. Il doit être remplacé par le futur portail<br>opérateur.|
|Proposition<br>d’engagement|Objet préparatoire ou intermédiaire utilisé dans certains flux<br>contractuels avant transformation ou validation en contrat.|
|Reprise de données|Opération consistant à récupérer, transformer et réintégrer dans la<br>nouvelle plateforme les données utiles issues de`InteGraal`, de<br>`pro.armagnac.fr`et de`armagnac.fr`.|
|Synchronisation ODG|Flux descendant par lequel certaines données gérées par l’ODG sont<br>transmises vers la plateforme BNIA. Le BNIA ne remonte pas de<br>données vers l’ODG dans ce périmètre.|
|Vue`SQL`|Vue de données mise à disposition par le système existant pour<br>extraire des informations à jour et sécuriser la reprise de données ou<br>certains contrôles.|
|`Zeendoc`|Outil tiers utilisé pour la diffusion des factures, le suivi de leur<br>traitement et leur transmission vers la comptabilité.|

## A.2 Sigles et abréviations
|**Sigle**|**Définition**|
|---|---|
|`AGI`|Éditeur actuel du logiciel`InteGraal`.|
|`AOC`|Appellation d’origine contrôlée.|
|`API`|Interface d’échange automatisé entre applications.|
|`BI`|Business intelligence. Le terme désigne ici les outils décisionnels et<br>statistiques internes, hors périmètre de reprise fonctionnelle.|
|`BNIA`|Bureau National Interprofessionnel de l’Armagnac.|

<!-- p.96 -->
|**Sigle**|**Définition**|
|---|---|
|`CEV`|Certificat export authentifiable, rattaché à la plateforme douanière<br>`Otentik`. Il porte un`QR code`permettant de contrôler l’authenticité du<br>document présenté.|
|`CVE`|Cotisation volontaire obligatoire facturée par le BNIA sur certains flux,<br>notamment les sorties d’Armagnac, certaines fabrications et les volumes<br>de vin distillés.|
|`CVI`|Casier viticole informatisé. Référence administrative utilisée pour<br>caractériser un opérateur viticole.|
|`CVO`|Cotisation volontaire obligatoire. Le terme est utilisé dans le périmètre de<br>facturation BNIA pour certaines cotisations calculées à partir des flux<br>métier.|
|`DAE`|Document d’accompagnement électronique. Document douanier utilisé<br>pour justifier certains mouvements de produits alcooliques, notamment à<br>l’export.|
|`DAI`|Document annuel d’inventaire transmis aux douanes.|
|`DGCCRF`|Direction générale de la concurrence, de la consommation et de la<br>répression des fraudes.|
|`DRM`|Déclaration récapitulative mensuelle transmise aux douanes. Elle est<br>attendue en pièce jointe des`DSM`producteurs, à raison d’une par mois de<br>la période déclarée.|
|`DSM`|Déclaration de stocks et de mouvements transmise au BNIA par les<br>opérateurs.|
|`EDI`|Échange de données informatisé entre systèmes.|
|`hl AP`|Hectolitre alcool pur. Unité de volume utilisée dans plusieurs calculs et<br>restitutions métier.|
|`hl.l,cl`|Hectolitre, litre et centilitre. Unités de volume utilisées dans plusieurs<br>calculs et restitutions métier. Il s’agit de présenter un volume sous ce<br>masque de présentation. Par exemple, pour un volume de 129550,3 litres,<br>on présentera 1 295.50,30 hl.l,cl.|
|`MB`|Millésime sous bois.|
|`MV`|Millésime sous verre.|
|`NB`|Assemblage sous bois.|
|`NV`|Assemblage sous verre.|
|`ODG`|Organisme de défense et de gestion. Dans le présent document, il s’agit<br>de l’organisme en charge des flux métier externes relatifs notamment aux<br>contrats vin et aux déclarations de fin de travaux.|
|`PDF`|Portable Document Format. Format utilisé pour certaines éditions,<br>factures, certificats et justificatifs.|

<!-- p.97 -->
|**Sigle**|**Définition**|
|---|---|
|`SQL`|Langage de requête et de structuration de bases de données, utilisé ici<br>notamment pour les vues de reprise.|
|`TOTP`|Code temporaire à usage unique utilisé comme second facteur<br>d’authentification.|
|`TAV`|Titre Alcoométrique Volumique. Il correspond au pourcentage d’alcool<br>éthylique contenu dans un produit, exprimé en % vol.|
|`TVA`|Taxe sur la valeur ajoutée.|

<!-- p.98 -->
# B Comptes d’âge et mentions de vieillissement
Cette annexe fixe la règle de calcul du compte d’âge utilisée dans le cahier des charges, notamment pour les `DSM` , les contrats, les certificats export et les restitutions statistiques. 

## B.1 Principe général
Le compte d’âge évolue chaque année au `1er avril` . 

Pour un millésime `N` : 

- avant le `1er avril` de l’année `N+1` , le produit est au compte d’âge `00` ; 

- du `1er avril N+1` au `31 mars N+2` , le produit est au compte d’âge `0` ; 

- du `1er avril N+2` au `31 mars N+3` , le produit est au compte d’âge `1` ; 

- le compte d’âge augmente ensuite d’une unité à chaque `1er avril` . 

A partir du `1er avril N+1` , la règle de calcul est la suivante : 

```
annéed'observation-millésime-1
```

## B.2 Grille de calcul générique
|**Date d’observation**|**Compte d’âge**|
|---|---|
|Avant le`1er avril N+1`|`00`|
|Du`1er avril N+1`au`31 mars N+2`|`0`|
|Du`1er avril N+2`au`31 mars N+3`|`1`|
|Du`1er avril N+3`au`31 mars N+4`|`2`|
|Puis à chaque nouveau`1er avril`|Incrément de`+1`|

## B.3 Exemple pour un millésime 2025
Pour un millésime `2025` , la campagne de distillation 2025/2026 court du `1er septembre 2025` au `31 mars 2026` . L’Armagnac produit au cours de cette campagne portera le compte d’âge comme décrit dans le tableau ci-dessous. 

|**Période**|**Compte d’âge**|
|---|---|
|Du`01/09/2025`au`31/03/2026`|`00`|
|A partir du`01/04/2026`|`0`|