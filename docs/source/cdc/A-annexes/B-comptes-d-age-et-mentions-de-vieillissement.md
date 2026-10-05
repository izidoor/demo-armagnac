<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 98-100 -->

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

<!-- p.99 -->
|**Période**|**Compte d’âge**|
|---|---|
|A partir du`01/04/2027`|`1`|
|A partir du`01/04/2028`|`2`|
|A partir du`01/04/2029`|`3`|
|A partir du`01/04/2030`|`4`|
|A partir du`01/04/2035`|`9`|
|A partir du`01/04/2036`|`10`|

## B.4 Mentions commerciales associées
|**Compte d’âge**|**Mention commerciale possible**|
|---|---|
|`00`|Pas de mention de vieillissement|
|`0`|Pas de mention de vieillissement|
|`1`à`3`|`VS`ou`***`|
|`4`à`9`|`VSOP`|
|`10`et plus|`Hors d'Âge`,`Extra`,`Réserve`|

<!-- p.100 -->
# C Référentiel des données et objets de gestion
Cette annexe a vocation à regrouper, par module, entité et sous-entité, les référentiels de champs utiles au futur dispositif. Elle doit permettre de centraliser, dans un format homogène, les entités gérées par la plateforme BNIA et les champs associés, sans distinction par environnement d’usage. Les champs décrits dans cette annexe constituent un cadre de travail à date. Ils peuvent évoluer selon les choix retenus, les contraintes techniques de mise en œuvre et la teneur des données qui seront récupérées sur les systèmes existants. 

Convention de typage : 

- `Identifiant` : valeur propre à l’objet décrit, numéro métier autonome, clé technique ou repère unique de suivi. 

- `Référentiel` : valeur sélectionnée dans une autre table métier, un autre objet de la plateforme, une table de paramétrage ou une liste fermée. 

- `Texte court` : libellé, nom ou valeur alphanumérique courte. 

- `Texte long` : commentaire ou description multiligne. 

- `Booléen` : oui / non. 

- `Date` : date simple. 

- `Nombre` : valeur numérique entière ou décimale avec unité métier. 

- `Choix multiple` : sélection de plusieurs valeurs dans un référentiel. 

- `Table fille` : sous-ensemble de lignes rattachées à une entité principale. 

- `Donnée calculée` : valeur dérivée ou alimentée automatiquement. 

## C.1 Gestion des opérateurs
> **ℹ INFO**
Certaines données sont reprises de l’ancien logiciel en guise de restitution, mais pourront être réorganisées pour répondre à la structure de la nouvelle plateforme. Notamment, les adresses emails associées à la fiche opérateur auront vocation à être saisies sur les fiches contacts dans la plateforme cible. 

### C.1.1 Opérateur
#### C.1.1.1 Socle d’identification et de coordonnées
|**Champ**|**Type**|**Description**|
|---|---|---|
|Type d’opérateur|Référentiel|Catégorie de rattachement de la fiche, utilisée pour la<br>numérotation, les contrôles et les droits associés.|
|Code opérateur BNIA|Identifiant|Identifiant principal de la fiche; unique, recherché, repris et<br>utilisé dans les rapprochements.|
|Groupe opérateur|Texte court|Permet de regrouper plusieurs fiches d’un même opérateur<br>ou de plusieurs chais rattachés.|