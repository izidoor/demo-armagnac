<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 22-30 -->

<!-- p.22 -->
# 4.4 Déclarations des stocks et mouvements
Ce module gère les déclarations de stocks et mouvements suivies par le BNIA. Il doit permettre de retrouver une `DSM` , de la générer pour la bonne période, de la saisir, de la contrôler, de la compléter par ses justificatifs et de la facturer. 

La fiabilité des données déclarées repose sur les contrôles de cohérence décrits dans ce chapitre. Ces contrôles conditionnent la validation de la déclaration. 

## 4.4.1 Historique des DSM
L’historique des `DSM` est le point d’entrée du module. Il doit permettre de retrouver rapidement une déclaration existante et d’ouvrir sa consultation ou sa reprise. 

Les filtres attendus portent notamment sur : 

- le code / le nom de l’opérateur; 

- la période; 

- le type d’opérateur; 

- l’état de traitement. 

Le listing doit afficher au minimum l’opérateur, la période, le type de déclarant, l’état, le total de cotisation et les repères de traitement utiles. 

## 4.4.2 Génération des DSM
La génération d’une `DSM` doit partir de l’opérateur, de son historique déclaratif et de sa périodicité paramétrée. 

Lors de la création d’une nouvelle déclaration, le système doit : 

- proposer automatiquement la période suivante à partir de la dernière déclaration connue; 

- reprendre une période cohérente avec la périodicité paramétrée pour l’opérateur; 

- empêcher tout chevauchement de période entre deux déclarations d’un même opérateur; 

- empêcher la création d’une déclaration antérieure à la dernière période déjà enregistrée; 

- empêcher la création d’un trou de déclaration entre deux périodes successives; 

- signaler explicitement toute incohérence entre la période proposée et la périodicité attendue. 

Le cycle de traitement d’une `DSM` doit au moins distinguer les états suivants : 

- En attente de saisie; 

- À valider; 

- À facturer; 

- Facturée.

<!-- p.23 -->
## 4.4.3 Saisie d’une DSM
La `DSM` BNIA doit suivre les volumes en Litre Alcool Pur avec le niveau de détail suivant : 

- qualité ou dénomination; 

- clé de conditionnement, notamment `MB` , `MV` , `NB` , `NV` ; 

- compte d’âge; 

- millésime lorsque la ligne porte un millésime. 

Dans une déclaration, une ligne doit être unique. La clé d’unicité repose sur la dénomination ou le millésime, la clé et le compte d’âge. 

La saisie d’une `DSM` doit toujours permettre : 

- de retrouver l’opérateur concerné; 

- d’identifier la période travaillée; 

- de voir le total de cotisation HT / TTC calculé; 

- de connaître les dates de saisie, d’édition et de facturation; 

- d’identifier le compte ayant créé ou modifié la déclaration. 

Le stock de début de période doit être repris automatiquement à partir du stock de fin de la déclaration précédente lorsque cette donnée existe. 

### 4.4.3.1 Onglets et colonnes de la déclaration
La `DSM` repose sur un format de déclaration unique, commun aux producteurs et aux négociants. La numérotation des colonnes est unique et s’applique aux deux types d’opérateur. 

Les colonnes hors du périmètre déclaratif du producteur doivent être grisées et non saisissables lorsque la déclaration porte sur un opérateur de ce type. Ces colonnes restent visibles et conservent leur numéro, en saisie comme à l’édition. 

La déclaration doit être organisée sur `5` onglets : 

- `Récapitulatif` 

- `Mouvements en entrée` 

- `Mouvements en sortie` 

- `Ventes France` 

- `Ventes à l'exportation` 

L’onglet `Récapitulatif` doit permettre de gérer : 

- le stock début de période (1) : lecture seule, reprise du stock en fin de période précédente; 

- les entrées achats à la propriété (2); 

- les entrées achats négoce (3); 

- les autres entrées : lecture seule, calculé automatiquement par la somme des entrées de l’onglet `Mouvements en entrée` ;

<!-- p.24 -->
- le total des sorties : lecture seule, calculé automatiquement par la somme des sorties des onglets `Mouvements en sortie` , `Ventes France` et `Ventes à l'exportation` ; 

- le stock fin de période (26) : lecture seule, calculé automatiquement. 

L’onglet `Mouvements en entrée` doit permettre de saisir : 

- la distillation propre (4); 

- le passage sous bois / sous-verre (5); 

- les assemblages / coupes (6); 

- les transferts chai Armagnac en région délimitée (7); 

- les transferts chai Armagnac hors région délimitée (8); 

- les réintégrations (9). 

L’onglet `Mouvements en sortie` doit permettre de saisir : 

- les pertes sur inventaire (10); 

- le passage sous bois / sous-verre (11); 

- les assemblages / coupes (12); 

- les transferts chai Armagnac en région délimitée (13); 

- les transferts chai Armagnac hors région délimitée (14); 

- les fabrications Floc de Gascogne (15); 

- l’utilisation de l’Armagnac pour autres fabrications que l’AOC (16). 

L’onglet `Ventes France` doit permettre de saisir : 

- le marché intérieur hors chai en bouteilles (17); 

- le marché intérieur hors chai en vrac destiné à la mise en bouteille (18); 

- le marché intérieur hors chai en brandies (19); 

- le négoce chai Armagnac en région délimitée (20); 

- le négoce chai Armagnac hors région délimitée (21). 

L’onglet `Ventes à l'exportation` doit permettre de saisir : 

- les bouteilles (22); 

- le vrac destiné à la mise en bouteille (23); 

- les `brandies` (24); 

- les vins vinés et fabrications autres (25). 

### 4.4.3.2 Colonnes grisées pour les producteurs
Les colonnes suivantes sont hors du périmètre déclaratif du producteur. Elles doivent être grisées et non saisissables sur une déclaration de type producteur :

<!-- p.25 -->
|**Colonne**|**Onglet**|**Libellé**|
|---|---|---|
|2|`Récapitulatif`|entrées achats à la propriété|
|3|`Récapitulatif`|entrées achats négoce|
|5|`Mouvements en entrée`|passage sous bois / sous-verre|
|9|`Mouvements en entrée`|réintégrations|
|11|`Mouvements en sortie`|passage sous bois / sous-verre|
|19|`Ventes France`|marché intérieur hors chai en brandies|
|24|`Ventes à l'exportation`|brandies|
|25|`Ventes à l'exportation`|vins vinés et fabrications autres|

Les `18` autres colonnes sont communes aux deux types d’opérateur. 

Une colonne grisée ne doit être ni saisissable, ni alimentée par un import, ni prise en compte dans les totaux, les contrôles de cohérence et le calcul de la cotisation de la déclaration. 

## 4.4.4 Edition d’une DSM
Une `DSM` doit pouvoir être éditée au format `Excel` ou `PDF` , selon le format unique de déclaration. Les colonnes grisées pour le type d’opérateur concerné doivent apparaître comme telles dans l’édition. 

Lorsque la `DSM` a été validée et signée électroniquement depuis le portail opérateur, le `PDF` signé doit être archivé et rester consultable depuis la déclaration dans le back-office BNIA, y compris sur les périodes clôturées. 

## 4.4.5 Justificatifs
Le module doit permettre d’identifier, de déposer, de consulter et de tracer les justificatifs liés à une déclaration ou à une ligne de déclaration. 

|**Type de mouvement**|**Justificatif attendu**|
|---|---|
|achat négoce ou propriété ou producteur|contrat achat Armagnac (rattachement au<br>contrat présent dans le système), facture ou<br>`DAE`selon le cas (dépôt de fichier PDF)|
|fabrication`Floc de Gascogne`|rapprochement avec les données ODG -<br>simple pointage|
|utilisation de l’Armagnac pour autres<br>fabrications que l’AOC|déclaration de fabrication douanes (dépôt de<br>fichier PDF)|
|vente négoce région ou hors région|contrat achat Armagnac (rattachement au<br>contrat présent dans le système), facture ou<br>`DAE`selon le cas (dépôt de fichier PDF)|

<!-- p.26 -->
|**Type de mouvement**|**Justificatif attendu**|
|---|---|
|vente export|facture ou`DAE`(dépôt de fichier PDF)|
|réintégrations|avoir ou`DAE`(dépôt de fichier PDF)|

Le système doit tracer : 

- le compte utilisateur ayant déposé le justificatif; 

- la date de dépôt; 

- le rattachement à la déclaration ou à la ligne concernée. 

Le système doit aussi permettre d’ajouter une note sur une ligne de déclaration lorsqu’un commentaire de contrôle est nécessaire. 

## 4.4.6 DRM des producteurs
Une déclaration de type producteur doit être accompagnée des `DRM` transmises aux douanes sur la période déclarée. Cette obligation ne s’applique pas aux négociants. 

Le module doit attendre une `DRM` au format `PDF` par mois compris dans la période de déclaration. Une `DSM` producteur annuelle appelle donc `12 DRM` . 

Le module doit permettre : 

- d’afficher les mois attendus sur la période de déclaration et l’état de dépôt de chacun; 

- de déposer une `DRM` manquante depuis le back-office BNIA; 

- de consulter et de télécharger chaque `DRM` déposée depuis la déclaration; 

- de remplacer une `DRM` erronée en conservant la trace du remplacement. 

Chaque `DRM` doit être archivée et rattachée à la déclaration et au mois concerné. Elle doit rester consultable par le BNIA depuis la déclaration, y compris sur les périodes clôturées, en cas de contrôle. 

Le système doit tracer le compte ayant déposé la `DRM` , la date de dépôt et le mois couvert. 

## 4.4.7 Contrôles de cohérence
Le module doit exécuter un jeu de contrôles de cohérence à la saisie, à l’import d’un fichier et avant toute validation d’une `DSM` . 

Les contrôles portent sur : 

- la cohérence entre stock d’ouverture, mouvements d’entrée, mouvements de sortie, ventes, fabrications et stock de clôture; 

- la reprise exacte du stock de fin de période de la déclaration précédente; 

- les colonnes croisées entre mouvements d’entrée et mouvements de sortie;

<!-- p.27 -->
- l’unicité des lignes de déclaration sur la dénomination ou le millésime, la clé de conditionnement et le compte d’âge; 

- la continuité des périodes déclarées, sans chevauchement ni trou; 

- la présence des justificatifs attendus sur les mouvements soumis à justificatif; 

- la présence de toutes les `DRM` attendues sur la période, pour les déclarations de type producteur; 

- le rattachement des lignes d’achat et de vente à un `Contrat achat Armagnac` exécuté. 

Un `Contrat achat Armagnac` est exécuté lorsqu’il est validé par les parties et par le BNIA et que les volumes mentionnés dans le contrat couvrent les volumes déclarés dans la `DSM` . 

La validation d’une `DSM` doit être bloquée tant que : 

- une incohérence de stock, de total ou de reprise du stock de période précédente subsiste; 

- un justificatif attendu est manquant; 

- une `DRM` attendue sur la période est manquante, pour une déclaration de type producteur; 

- une ligne d’achat ou de vente reste rattachée à un `Contrat achat Armagnac` non exécuté. 

Le blocage doit s’appliquer à la validation dans le back-office BNIA comme à la validation depuis le portail opérateur. Le système doit afficher le motif du blocage et le niveau exact concerné : déclaration, onglet ou ligne. 

Le module doit permettre d’ouvrir directement la ligne à corriger depuis le motif de blocage. 

## 4.4.8 Relances
Le module doit permettre d’identifier les `DSM` en retard ou absentes sur une période attendue. Il doit aussi faire remonter : 

- les justificatifs manquants au niveau de la déclaration; 

- les justificatifs manquants au niveau d’une ligne; 

- les `DRM` manquantes sur la période, mois par mois, pour les producteurs; 

- les incohérences de stock ou de total qui bloquent la validation; 

- les écarts à contrôler entre la `DSM` et les pièces de rapprochement utiles. 

Ces alertes doivent permettre d’ouvrir la déclaration concernée et d’identifier immédiatement le niveau exact du problème. 

## 4.4.9 Facturation
Le module doit calculer automatiquement la cotisation à partir des colonnes soumises, selon un paramétrage par type d’opérateur et par colonne de déclaration. 

Les flux pris en compte dans l’assiette de cotisation sont les suivants : 

- les ventes France hors chai Armagnac; 

- les ventes Bouteilles;

<!-- p.28 -->
- les ventes Négoce Hors Région Délimitée; 

- les ventes Brandies; 

- les ventes export; 

- l’utilisation de l’Armagnac pour autres fabrications que l’AOC entrant dans l’assiette retenue par le BNIA. 

Le total de cotisation hors taxes doit être visible dans la déclaration et exploitable par le module de facturation. 

Le module doit permettre : 

- d’identifier les `DSM` éligibles à la facturation; 

- de déclencher la facturation depuis la déclaration pour les profils habilités; 

- de reprendre la date de facture; 

- de créer le lien avec la pièce de facturation émise; 

- d’historiser l’opération. 

## 4.4.10 Règles de gestion
La périodicité déclarative reste pilotée par la fiche opérateur. 

Le système doit couvrir : 

- une périodicité annuelle par défaut pour les producteurs; 

- une périodicité mensuelle par défaut pour les négociants; 

- la possibilité d’appliquer une autre périodicité lorsqu’elle est paramétrée sur la fiche opérateur : prévoir un champ sur la fiche opérateur pour pouvoir paramétrer une DSM au mois, au trimestre, au semestre, à l’année. 

Le BNIA doit pouvoir traiter une `DSM` selon plusieurs modes d’alimentation, sans créer plusieurs versions concurrentes d’une même déclaration : 

- saisie manuelle dans le back-office BNIA; 

- import d’un fichier au format normalisé BNIA (fichiers Excel au même format que l’édition en sortie); 

- import d’un fichier issu d’un logiciel opérateur compatible. 

La matrice Excel existante `Négociant` doit servir de base au format d’import normalisé BNIA. Ce format intègre le grisage des colonnes selon le type d’opérateur. 

Un format de fichier “informatique” (type `XML` ou `JSON` ) devra être proposé pour être soumis aux éditeurs de logiciels opérateurs afin de leur permettre de faire une sortie mensuelle de ce format et ainsi de faciliter la transmission par les opérateurs de leur DSM au BNIA. Les logiciels les plus utilisés par les opérateurs et donc à traiter prioritairement sont : 

- `GrandCru` ; 

- `Isavigne` ; 

- `ACCISES` .

<!-- p.29 -->
Quel que soit le mode d’alimentation retenu, la déclaration doit passer les contrôles de cohérence décrits dans la section Contrôles de cohérence. Un import ne doit jamais créer une déclaration validée sans que ces contrôles aient été joués. 

Les actions majeures sur la déclaration doivent être historisées, notamment : 

- la création et la modification d’une `DSM` ; 

- le dépôt d’un justificatif; 

- le dépôt ou le remplacement d’une `DRM` producteur; 

- la validation d’une `DSM` et la signature électronique associée; 

- la facturation issue d’une `DSM` ; 

- la validation de la facture associée.

<!-- p.30 -->
# 4.5 Contrats d’achat à la propriété
Le module `Contrats d'achat à la propriété` regroupe les contrats et écritures contractuelles suivis par le back-office BNIA. Il doit permettre de retrouver rapidement un contrat, de le consulter ou de le traiter selon sa nature, et d’assurer les liens utiles avec les opérateurs, le portail, la `DSM` et la facturation. 

Le module regroupe deux types de contrats d’achat à la propriété : 

- `Contrat achat Armagnac` : contrat saisi par le BNIA ou par un opérateur, avec validation des parties et du BNIA; 

- `Contrat achat de vin de distillation` : contrat saisi dans l’outil ODG puis synchronisé dans le back-office BNIA en consultation. 

Il couvre également les objets contractuels rattachés à ces contrats : 

- les pluri-annuels Armagnac; 

- les propositions d’engagement Armagnac; 

- les pluri-annuels vin; 

- les propositions d’engagement vin. 

## 4.5.1 Contrat achat Armagnac
Le `Contrat achat Armagnac` formalise un achat ou une vente d’Armagnac entre opérateurs. Il peut être saisi par un opérateur depuis le portail. Le contrat doit ensuite être validé par les parties et par le BNIA avant d’être exploité dans les autres modules. 

### 4.5.1.1 Listing des contrats
Le module doit permettre : 

- de lister les contrats Armagnac; 

- de rechercher un contrat par date, numéro, acheteur, vendeur ou statut; 

- de créer un contrat et de le compléter sans ressaisie des données opérateur déjà connues; 

- de consulter, modifier et éditer le contrat dans le cadre des droits ouverts. 

### 4.5.1.2 Fiche contrat
Le contrat doit au minimum gérer : 

- la date du contrat; 

- l’identification de l’acheteur et du vendeur : liaison avec le référentiel d’opérateurs; 

- les lignes produit, avec compte d’âge, millésime, appellation, dénomination, volume, degré, volume en alcool pur et prix; 

- les conditions et le calendrier de retiraison; 

- les conditions de paiement;