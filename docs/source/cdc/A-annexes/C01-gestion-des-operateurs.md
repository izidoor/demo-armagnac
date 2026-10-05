<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 100-105 -->

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

<!-- p.101 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|N°`CVI`|Texte court|Référence viticole de rattachement; utilisée pour les<br>contrôles et certains échanges.|
|`SIRET`|Texte court|Identifiant administratif de l’opérateur; utilisé dans les<br>contrats, justificatifs et rapprochements.|
|N° Accise|Texte court|Référence douanière de l’opérateur.|
|Bureau douanes|Référentiel|Information de rattachement administratif utile aux flux et<br>contrôles.|
|Identifiant tiers ODG|Identifiant|Clé de correspondance vers le système ODG ou autre<br>système tiers si distincte du code BNIA.|
|Titre|Référentiel|Civilité ou titre de présentation associé à l’opérateur.|
|Raison sociale / nom|Texte court|Libellé principal de la structure.|
|Nom commercial|Texte court|Nom d’usage ou de diffusion, distinct de la raison sociale si<br>nécessaire.|
|Contact principal|Texte court|Personne ou point d’entrée principal de la structure.|
|Adresse ligne 1|Texte court|Adresse postale de référence.|
|Adresse ligne 2|Texte court|Complément d’adresse.|
|Adresse ligne 3|Texte court|Complément d’adresse.|
|Code postal|Texte court|Code postal de l’opérateur.|
|Ville|Texte court|Commune ou ville de rattachement.|
|Pays|Référentiel|Pays de rattachement postal ou commercial.|
|Téléphone 1|Texte court|Numéro principal.|
|Téléphone 2|Texte court|Numéro complémentaire.|
|Téléphone portable|Texte court|Numéro mobile utile aux contacts opérationnels.|
|Fax|Texte court|Champ historique à reprendre uniquement si le besoin<br>métier est confirmé.|
|Mél 1|Texte court|Adresse électronique principale.|
|Mél 2|Texte court|Adresse électronique secondaire.|
|Mél 3|Texte court|Adresse électronique secondaire.|
|Site web|Texte court|URL institutionnelle ou de présentation.|
|Site commercial|Texte court|URL dédiée à la commercialisation si distincte.|
|Coordonnées GPS|Nombre|Latitude / longitude utiles à la localisation ou à certains<br>usages de diffusion.|
|Membre BNIA|Référentiel|Statut ou rôle interne au BNIA associé à l’opérateur.|
|Commentaire interne|Texte long|Observation de gestion non diffusable.|
|Accord de diffusion général|Booléen|Accord global de diffusion des informations opérateur sur<br>les supports autorisés.|
|Imprimé personnel|Booléen|Paramètre hérité de l’existant à confirmer selon les éditions<br>maintenues.|
|Périodicité DSM|Référentiel|Mensuelle, trimestrielle, semestrielle ou annuelle, avec<br>valeur par défaut selon le type d’opérateur.|
|Code d’accès Internet historique|Texte court|Champ technique hérité de l’existant; à reprendre,<br>neutraliser ou migrer selon la cible d’authentification.|

<!-- p.102 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Dernière déclaration créée|Donnée<br>calculée|Date (MM / AAAA) ou repère de suivi alimenté<br>automatiquement.|
|Dernière déclaration saisie|Donnée<br>calculée|Date (MM / AAAA) ou repère de suivi alimenté<br>automatiquement.|

#### C.1.1.2 Production et activité déclarative
|**Champ**|**Type**|**Description**|
|---|---|---|
|Région|Référentiel|Qualification géographique métier, avec distinction entre<br>région Armagnac et hors région.|
|Appellations|Choix multiple|Liste des appellations associées à l’opérateur parmis<br>`Armagnac`,`Bas Armagnac`,`Armagnac Ténarèze`et`Haut`<br>`Armagnac`.|
|Statut de déclaration de stock|Référentiel|Valeurs de type`oui`,`non`,`sommeil`,`clôturé`.|
|Détenteur alambic|Booléen|Indique la détention d’un ou plusieurs alambics.|

### C.1.2 Alambic
#### C.1.2.1 Agréments et capacités
|**Champ**|**Type**|**Description**|
|---|---|---|
|Liste des alambics|Table fille|Sous-ensemble de lignes décrivant les alambics rattachés<br>à l’opérateur.|
|Alambic - N° agrément|Texte court|Identifiant réglementaire de l’alambic.|
|Alambic - type|Référentiel|Type d’alambic, par exemple armagnacais ou double<br>chauffe.|
|Alambic - capacité|Nombre|Capacité exprimée selon l’unité propre au type d’alambic.|

### C.1.3 Diffusion des millésimes
#### C.1.3.1 Paramètres de diffusion
|**Champ**|**Type**|**Description**|
|---|---|---|
|Niveau d’accord de diffusion<br>des millésimes|Référentiel|Paramètre global`refus`,`accord partiel`ou`accord`<br>`total`.|
|Millésime diffusable|Table fille|Liste des millésimes autorisés à la diffusion lorsque<br>l’accord est partiel.|
|Millésime - case diffusion|Booléen|Indique si le millésime peut être repris dans les restitutions<br>autorisées.|

<!-- p.103 -->
### C.1.4 Marque opérateur
#### C.1.4.1 Marques France et Export
|**Champ**|**Type**|**Description**|
|---|---|---|
|Marques|Table fille|Liste structurée des marques exploitées sur le marché.|
|Marque - portée|Référentiel|`France`ou`Export`.|
|Marque - référence|Référentiel|Valeur choisie dans le référentiel des marques.|
|Marque - diffusion web|Booléen|Indique si la marque peut être diffusée sur le web.|

#### C.1.4.2 Circuits de distribution
|**Champ**|**Type**|**Description**|
|---|---|---|
|Circuit de distribution|Table fille|Liste des circuits de distribution des marques.|
|Circuit de distribution - portée|Référentiel|`France`ou`Export`.|
|Circuit de distribution - circuits<br>de distribution|Référentiel|Valeur choisie dans le référentiel des circuits de distribution.|

### C.1.5 Visite opérateur
#### C.1.5.1 Visites proposées
|**Champ**|**Type**|**Description**|
|---|---|---|
|Types de visites proposées|Table fille|Liste des visites ou prestations d’accueil.|
|Visite - type|Référentiel|Type de visite issu du référentiel paramétré.|
|Visite - commentaire|Texte long|Commentaire pratique, horaire ou précision libre.|
|**C.1.6 Label opérateur**|||

#### C.1.6.1 Labels touristiques
|**Champ**|**Type**|**Description**|
|---|---|---|
|Labels touristiques|Table fille|Liste des labels ou distinctions rattachés à l’opérateur.|
|Label - type|Référentiel|Valeur du référentiel des labels.|
|Label - commentaire|Texte long|Précision associée au label.|

<!-- p.104 -->
### C.1.7 Paramètres œnotourisme
#### C.1.7.1 Œnotourisme
|**Champ**|**Type**|**Description**|
|---|---|---|
|Paiement CB accepté|Booléen|Indique l’acceptation du paiement par carte bancaire.|
|Types de cartes acceptées|Choix multiple|Liste des cartes acceptées si paiement CB activé.|
|Dégustation gratuite|Booléen|Indique si des dégustations gratuites sont proposées.|
|Lieu de vente aménagé|Booléen|Indique l’existence d’un espace de vente aménagé.|
|Accueil groupe|Booléen|Indique la capacité d’accueil de groupes.|
|Langues parlées|Choix multiple|Liste des langues disponibles pour l’accueil.|
|Médailles et concours|Table fille|Liste libre ou semi-structurée des distinctions à valoriser.|
|Mode de présentation des<br>produits|Référentiel|Distinction du type`Accueil Armagnac et autres`<br>`produits`ou`Accueil 100 % Armagnac`.|
|Produits commercialisés|Choix multiple|Produits proposés par l’opérateur à partir d’un référentiel<br>dédié.|
|Adhésion œnotourisme BNIA|Booléen|Indique l’adhésion au dispositif BNIA concerné.|
|Présentation du domaine|Texte long|Texte de présentation en français.|
|Présentation du domaine en<br>anglais|Texte long|Texte de présentation en anglais.|

### C.1.8 Paramètres statistiques opérateur
#### C.1.8.1 Visibilité statistique
|**Champ**|**Type**|**Description**|
|---|---|---|
|Famille statistique autorisée|Table fille|Liste des blocs statistiques potentiellement exposables à<br>l’opérateur.|
|Niveau d’accord statistique|Référentiel|Paramètre de type`refus`,`accord partiel`,`accord total`<br>si cette granularité est retenue.|
|Périmètre statistique visible|Choix multiple|Liste des états ou indicateurs effectivement accessibles à<br>l’opérateur.|

### C.1.9 Contact opérateur
#### C.1.9.1 Coordonnées et rattachement
|**Champ**|**Type**|**Description**|
|---|---|---|
|Contacts opérateur|Table fille|Ensemble des personnes rattachées à la structure.|
|Contact - nom|Texte court|Nom du contact.|
|Contact - prénom|Texte court|Prénom du contact.|

<!-- p.105 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Contact - fonction|Texte court|Fonction ou rôle dans la structure.|
|Contact - centres d’intérêt|Choix multiple|Un ou plusieurs centres d’intérêt issus du référentiel,<br>utilisés pour cibler les communications adressées au<br>contact.|
|Contact - téléphone<br>professionnel|Texte court|Coordonnée téléphonique principale.|
|Contact - téléphone personnel /<br>secondaire|Texte court|Coordonnée complémentaire si conservée.|
|Contact - mél|Texte court|Adresse électronique du contact; obligatoire pour l’accès<br>portail.|
|Contact - adresse|Texte court|Adresse postale du contact si distincte de celle de<br>l’opérateur.|
|Contact - code postal|Texte court|Code postal du contact.|
|Contact - ville|Texte court|Ville du contact.|
|Contact - pays|Référentiel|Pays du contact.|
|Contact - observation|Texte long|Commentaire interne ou précision de gestion.|

#### C.1.9.2 Accès portail et habilitations locales
|**Champ**|**Type**|**Description**|
|---|---|---|
|Contact - accès portail activé|Booléen|Active ou non un accès pour ce contact.|
|Contact - administrateur du|Booléen|Indique si le contact peut administrer les autres accès|
|compte||portail.|
|Contact - droits par module|Choix multiple|Liste des modules du portail autorisés pour ce contact.|

## C.2 Gestion des accès et habilitations
### C.2.1 Compte utilisateur interne BNIA
#### C.2.1.1 Identité, statut et sécurité d’accès
|**Champ**|**Type**|**Description**|
|---|---|---|
|Comptes utilisateurs BNIA|Table fille|Ensemble des accès internes de la plateforme pour les<br>utilisateurs BNIA.|
|Compte - prénom|Texte court|Prénom de l’utilisateur interne.|
|Compte - nom|Texte court|Nom de l’utilisateur interne.|
|Compte - adresse électronique<br>professionnelle|Identifiant|Adresse de référence pour la connexion, les notifications<br>d’accès et la recherche d’un compte.|
|Compte - statut|Référentiel|État du compte, avec au minimum`actif`,`désactivé`,<br>`bloqué temporairement`,`en attente d'activation`.|