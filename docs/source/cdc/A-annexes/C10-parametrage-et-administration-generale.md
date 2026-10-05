<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 127-131 -->

<!-- p.127 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Famille statistique|Référentiel|Regroupe la ligne dans une famille de restitution BNIA.|
|Indicateur|Référentiel|Indicateur restitué, par exemple volume, nombre de<br>contrats, montant de`CVO`ou prix moyen.|
|Valeur agrégée|Nombre|Valeur calculée de la ligne statistique.|
|Unité de mesure|Texte court|Unité utilisée pour la lecture de la valeur, par exemple`hl`,<br>`hlAP`,`% vol`ou`€ / hlAP`.|
|Axe principal|Texte court|Première dimension de consolidation affichée sur la ligne.|
|Axe secondaire|Texte court|Seconde dimension de consolidation affichée lorsqu’elle<br>existe.|
|Diffusable|Booléen|Indique si la ligne peut être reprise dans une restitution<br>diffusable hors usage interne BNIA.|

# C.10 Paramétrage et administration générale
## C.10.1 Classe de** **`TVA`
### C.10.1.1 Définition de la classe
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code classe de`TVA`|Identifiant|Code de la classe de taxe utilisée dans le paramétrage<br>BNIA.|
|Libellé de la classe de`TVA`|Texte court|Libellé métier de la classe de taxe.|
|Classe de`TVA`active|Booléen|Indique si la classe peut encore être utilisée dans les<br>paramétrages courants.|

## C.10.2 Taux de** **`TVA`
### C.10.2.1 Historique des taux
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes de taux de`TVA`|Table fille|Historique des taux applicables dans la plateforme.|
|Ligne - classe de`TVA`|Référentiel|Classe de taxe à laquelle se rattache la ligne de taux.|
|Ligne - taux de`TVA`|Nombre|Valeur du taux de`TVA`applicable.|
|Ligne - date de début de validité|Date|Date à partir de laquelle le taux devient applicable.|
|Ligne - date de fin de validité|Date|Date de fin d’application du taux lorsqu’elle existe.|

<!-- p.128 -->
## C.10.3 Paramètre de cotisation
### C.10.3.1 Grille des cotisations
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes de cotisation|Table fille|Historique des paramètres de cotisation utilisés dans les<br>calculs BNIA.|
|Ligne - type de cotisation|Référentiel|Valeur de type`CVO`ou`Fin de travaux de distillation`.|
|Ligne - montant unitaire|Nombre|Montant de cotisation applicable.|
|Ligne - unité de calcul|Texte court|Unité métier du calcul, par exemple`€ / hlAP`ou`€ / hl`<br>`de vin distillé`.|
|Ligne - pourcentage<br>d’exonération de`TVA`|Nombre|Pourcentage de la base exonéré de`TVA`.|
|Ligne - date de début de validité|Date|Début d’application du paramètre.|
|Ligne - date de fin de validité|Date|Fin d’application du paramètre lorsqu’elle existe.|

## C.10.4 Référentiel des continents
### C.10.4.1 Continent ou zone commerciale
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code région / zone|Identifiant|Code du continent ou de la zone commerciale.|
|Libellé du continent / de la zone|Texte court|Libellé utilisé dans les rattachements métier.|

## C.10.5 Référentiel des pays
### C.10.5.1 Pays
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code pays|Identifiant|Code métier du pays utilisé dans les fiches opérateurs,<br>suivis sortie export et certificats.|
|Code`ISO`|Identifiant|Code normalisé du pays.|
|Libellé du pays|Texte court|Libellé du pays.|
|Continent de rattachement|Référentiel|Continent ou zone commerciale associé.|
|Modèle de certificat associé|Référentiel|Modèle de certificat utilisé pour ce pays.|
|Âge minimum d’Armagnac<br>autorisé|Nombre|Seuil réglementaire ou commercial utilisé dans les<br>contrôles export.|

<!-- p.129 -->
## C.10.6 Référentiel des régions
### C.10.6.1 Région de rattachement
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code région|Identifiant|Code de la région métier de rattachement.|
|Libellé de la région|Texte court|Libellé de la région métier.|

## C.10.7 Référentiel des destinataires export
### C.10.7.1 Destinataire export
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant destinataire export|Identifiant|Référence interne du destinataire utilisé dans les certificats<br>export.|
|Type tiers|Référentiel|Catégorie d’acteur du destinataire.|
|Titre|Référentiel|Civilité ou titre de présentation lorsque cette information est<br>utilisée.|
|Raison sociale / nom du<br>destinataire|Texte court|Libellé principal utilisé dans les certificats export.|
|Interlocuteur destinataire|Texte court|Nom ou personne de contact lorsque l’information est<br>portée.|
|Adresse ligne 1|Texte court|Adresse principale du destinataire.|
|Adresse ligne 2|Texte court|Complément d’adresse du destinataire.|
|Code postal|Texte court|Code postal du destinataire.|
|Ville|Texte court|Ville du destinataire.|
|Pays|Référentiel|Pays de rattachement du destinataire.|
|Adresse électronique|Texte court|Adresse électronique de contact lorsqu’elle est connue.|
|Téléphone|Texte court|Coordonnée téléphonique utile au dossier export.|

## C.10.8 Référentiels de qualification
### C.10.8.1 Titres
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code titre|Identifiant|Code de civilité ou de forme d’appel.|
|Libellé du titre|Texte court|Libellé utilisé dans les fiches, contacts et destinataires.|

<!-- p.130 -->
### C.10.8.2 Types tiers
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code type tiers|Identifiant|Code de la catégorie d’acteur.|
|Libellé du type tiers|Texte court|Libellé de la catégorie d’acteur, par exemple producteur ou<br>négociant.|

### C.10.8.3 Labels
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code label|Identifiant|Code du label ou de la distinction.|
|Libellé du label|Texte court|Libellé du label.|

### C.10.8.4 Visites
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code type de visite|Identifiant|Code du type de visite proposé.|
|Libellé du type de visite|Texte court|Libellé du type de visite ou de prestation d’accueil.|

### C.10.8.5 Langues
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code langue|Identifiant|Code de la langue.|
|Libellé de la langue|Texte court|Libellé de la langue utilisée dans les fiches opérateurs.|

### C.10.8.6 Centres d’intérêt
|**Champ**|**Type**|**Description**|
|---|---|---|
|Code centre d’intérêt|Identifiant|Code du centre d’intérêt.|
|Libellé du centre d’intérêt|Texte court|Libellé du domaine d’activité du contact, par exemple<br>comptabilité, marketing ou production.|
|Actif|Booléen|Indique si la valeur reste proposée à la saisie sur les<br>contacts opérateurs.|

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