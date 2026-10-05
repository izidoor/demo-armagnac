<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 131-133 -->

<!-- p.131 -->
## C.10.9 Référentiels de présentation et de commercialisation
### C.10.9.1 Marques
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code marque|Identifiant|Code unique de la marque.|
|Libellé de la marque|Texte court|Libellé de la marque utilisé dans les fiches opérateurs.|

### C.10.9.2 Circuits de distribution
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code circuit|Identifiant|Code unique du circuit de distribution.|
|Libellé du circuit|Texte court|Libellé du circuit de distribution.|

### C.10.9.3 Produits commercialisés
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code produit commercialisé|Identifiant|Code du produit commercialisé.|
|Libellé du produit commercialisé|Texte court|Libellé du produit utilisé dans les fiches opérateurs et<br>contenus liés à l’offre.|

### C.10.9.4 Contenances
|**Champ**|**Type**|**Description**|
|---|---|---|
|Contenance reconnue|Texte court|Valeur de contenance autorisée dans l’application,<br>notamment pour les suivis sortie export.|

# C.11 Gestion des contenus
## C.11.1 Catégorie d’actualité
### C.11.1.1 Classement des actualités
|**Champ**|**Type**|**Description**|
|---|---|---|
|Libellé de la catégorie|Texte court|Libellé de la catégorie d’actualité.<br>fi|
|Ordre d’apparition|Nombre|Position d’affichage de la catégorie dans le portail.|
|Photo de catégorie|Texte court|Référence du visuel associé à la catégorie.|

<!-- p.132 -->
## C.11.2 Article d’actualité
### C.11.2.1 Article
|**Champ**|**Type**|**Description**|
|---|---|---|
|Date de publication|Date|Date affichée pour l’actualité.|
|Catégorie associée|Référentiel|Catégorie de rattachement de l’article.|
|Photo de l’article|Texte court|Référence du visuel associé à l’article.|
|Titre de l’article|Texte court|Titre de l’actualité.|
|Contenu mis en forme|Texte long|Corps de l’article avec mise en forme et liens intégrés.|

## C.11.3 Interlocuteur BNIA
### C.11.3.1 Contact pratique
|**Champ**|**Type**|**Description**|
|---|---|---|
|Rôle|Texte court|Fonction affichée pour l’interlocuteur.|
|Nom|Texte court|Nom de famille de l’interlocuteur.|
|Prénom|Texte court|Prénom de l’interlocuteur.|
|Adresse électronique|Texte court|Mél de contact affiché dans le portail.|
|Téléphone|Texte court|Numéro de téléphone affiché dans le portail.|
|Ordre d’apparition|Nombre|Position d’affichage dans la liste des interlocuteurs.|

## C.11.4 Informations horaires BNIA
### C.11.4.1 Bloc horaire
|**Champ**|**Type**|**Description**|
|---|---|---|
|Contenu des horaires et|Texte long|Bloc de texte mis en forme affiché dans les informations|
|informations pratiques||pratiques du portail.|

## C.11.5 Catégorie de ressource
### C.11.5.1 Classement documentaire
|**Champ**|**Type**|**Description**|
|---|---|---|
|Libellé de la catégorie|Texte court|Libellé de la catégorie documentaire.|
|Catégorie parente|Référentiel|Rattachement à une catégorie parente lorsque le niveau<br>enfant est utilisé.|

<!-- p.133 -->
|**Champ**<br>Ordre d’apparition|**Type**<br>Nombre|**Description**<br>Position d’affichage de la catégorie ou sous-catégorie.|
|---|---|---|

## C.11.6 Document ressource
### C.11.6.1 Document téléchargeable
|**Champ**|**Type**|**Description**|
|---|---|---|
|Titre du document|Texte court|Titre affiché pour la ressource.|
|Catégorie ou sous-catégorie de<br>rattachement|Référentiel|Classement documentaire du fichier.|
|Fichier déposé|Texte court|Référence du document mis à disposition.|
|Format du fichier|Référentiel|Format de fichier autorisé pour la ressource.|

## C.11.7 Catégorie de photothèque
### C.11.7.1 Classement des photos
|**Champ**|**Type**|**Description**|
|---|---|---|
|Libellé de la catégorie|Texte court|Libellé de la catégorie de photothèque.|
|Ordre d’apparition|Nombre|Position d’affichage de la catégorie dans le portail.|

## C.11.8 Photo de photothèque
### C.11.8.1 Photo
|**Champ**|**Type**|**Description**|
|---|---|---|
|Catégorie de rattachement|Référentiel|Catégorie de photothèque à laquelle la photo appartient.|
|Fichier image|Texte court|Référence du fichier image diffusé dans le portail.|
|Format image|Référentiel|Format de la photo, limité aux formats d’image retenus.|

# C.12 Boutique et commandes
## C.12.1 Catégorie boutique