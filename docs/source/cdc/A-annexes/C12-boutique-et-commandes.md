<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 133-135 -->

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

<!-- p.134 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant|Identifiant|Référence interne unique de la catégorie.|
|Intitulé|Texte court|Libellé affiché dans le back-office et le portail opérateur.|
|Position|Nombre|Position d’apparition de la catégorie dans le portail.|

## C.12.2 Produit boutique
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant|Identifiant|Référence interne unique du produit.|
|Catégorie|Référentiel|Catégorie de rattachement du produit.|
|Intitulé|Texte court|Libellé du produit affiché dans la boutique.|
|Photo|Texte court|Référence de l’image utilisée pour illustrer le produit.|
|Prix unitaire`HT`|Nombre|Prix hors taxes du produit.|

## C.12.3 Commande boutique
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant|Identifiant|Référence interne unique de la commande.|
|Numéro|Identifiant|Numéro visible dans les écrans de suivi.|
|Opérateur|Référentiel|Opérateur ayant passé la commande.|
|Contact saisissant|Référentiel|Contact portail à l’origine de la commande lorsque cette<br>information est conservée.|
|Date de commande|Date|Date de validation de la commande.|
|État|Référentiel|Statut de suivi de la commande, avec au minimum`en`<br>`attente`,`traitée`,`facturée`.|
|Montant`HT`|Nombre|Montant total hors taxes de la commande.|
|Montant`TVA`|Nombre|Montant total de`TVA`de la commande lorsqu’il est calculé<br>dans l’application.|
|Montant`TTC`|Nombre|Montant total toutes taxes comprises de la commande<br>lorsqu’il est calculé dans l’application.|

### C.12.3.1 Commande boutique > Lignes de commande
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant commande|Référentiel|Identifiant de la commande liée.|
|Produit|Référentiel|Produit boutique rattaché à la ligne.|
|Quantité|Nombre|Quantité commandée pour le produit.|
|Prix unitaire`HT`|Nombre|Prix unitaire hors taxes repris au moment de la commande.|

<!-- p.135 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Montant total`HT`|Donnée<br>calculée|Montant hors taxes calculé sur la ligne.|

# C.13 Comptes et accès opérateur
## C.13.1 Compte utilisateur opérateur
### C.13.1.1 Identité et sécurité du compte
|**Champ**|**Type**|**Description**|
|---|---|---|
|Adresse électronique de<br>connexion|Identifiant|Identifiant unique de connexion de l’utilisateur opérateur.|
|Mot de passe|Texte court|Secret d’authentification du compte, géré selon les règles<br>de sécurité retenues.|
|Double authentification activée|Booléen|Indique si le compte utilise un second facteur<br>d’authentification.|
|Mode de double authentification|Référentiel|Mode retenu, par exemple`courriel`ou`application`<br>`TOTP`.|
|État du compte|Référentiel|Statut du compte : actif, désactivé, bloqué ou en attente<br>d’activation.|
|Date de dernière connexion|Date|Dernière connexion constatée sur le compte.|
|Nombre d’échecs récents|Donnée<br>calculée|Compteur utilisé pour le blocage temporaire après<br>tentatives échouées.|

## C.13.2 Accès opérateur rattaché
### C.13.2.1 Comptes opérateurs accessibles
|**Champ**|**Type**|**Description**|
|---|---|---|
|Compte opérateur accessible|Table fille|Liste des opérateurs auxquels le compte utilisateur peut<br>accéder.|
|Code opérateur|Référentiel|Référence du compte opérateur rattaché.|
|Rôle sur le compte opérateur|Référentiel|Niveau d’administration ou d’usage du contact sur ce<br>compte.|
|Modules autorisés|Choix multiple|Liste des modules du portail ouverts pour ce rattachement,<br>y compris la boutique lorsque le contact y est habilité.|
|Compte opérateur actif|Booléen|Indique le compte actuellement sélectionné dans la<br>session.|