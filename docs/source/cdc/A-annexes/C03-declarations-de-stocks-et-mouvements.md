<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 107-112 -->

<!-- p.107 -->
## C.2.3 Journal des accès et actions sensibles
### C.2.3.1 Historique d’audit
|**Champ**|**Type**|**Description**|
|---|---|---|
|Journal des accès|Table fille|Historique des connexions et des changements sensibles<br>liés aux accès.|
|Événement - date|Date|Date de la connexion ou de l’action tracée.|
|Événement - type|Référentiel|Nature de l’événement, par exemple connexion réussie,<br>échec, création de compte, changement de rôle,<br>désactivation.|
|Événement - utilisateur<br>concerné|Référentiel|Compte interne visé par l’événement.|
|Événement - auteur de l’action|Référentiel|Utilisateur ou administrateur à l’origine de l’action lorsque<br>cette information existe.|
|Événement - rôle concerné|Référentiel|Rôle ajouté, retiré ou modifié lorsqu’un changement<br>d’habilitation est tracé.|
|Événement - résultat|Référentiel|Résultat de l’action, par exemple`succès`,`échec`,`blocage`<br>`déclenché`.|
|Événement - détail|Texte long|Détail utile au support, au contrôle interne ou à l’audit.|

# C.3 Déclarations de stocks et mouvements
## C.3.1 Déclaration DSM
### C.3.1.1 Pilotage de la déclaration
|**Champ**|**Type**|**Description**|
|---|---|---|
|Déclarations DSM|Table fille|Ensemble des déclarations de stocks et mouvements<br>suivies par le BNIA.|
|Déclaration - opérateur|Référentiel|Référence de l’opérateur déclarant.|
|Déclaration - période|Texte court|Période de déclaration, mensuelle, annuelle ou regroupée<br>selon la périodicité autorisée.|
|Déclaration - type de déclarant|Référentiel|Profil déclaratif`producteur`ou`négociant`.|
|Déclaration - mode<br>d’alimentation|Référentiel|Saisie manuelle, import BNIA ou import depuis logiciel<br>opérateur.|
|Déclaration - état|Référentiel|État de traitement, avec au minimum`En attente de`<br>`saisie`,`À facturer`,`Facturée`,`Validée`.|
|Déclaration - observation<br>générale|Texte long|Commentaire porté sur l’ensemble de la déclaration.|
|Déclaration - total cotisation HT|Donnée<br>calculée|Total calculé à partir des flux soumis à cotisation.|
|Déclaration - date de saisie|Date|Date de création ou de saisie de la déclaration.|

<!-- p.108 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Déclaration - saisi par|Référentiel|Utilisateur BNIA ou opérateur à l’origine de la saisie ou de<br>l’import.|
|Déclaration - dernière<br>modification|Date|Dernière date de mise à jour de la déclaration.|
|Déclaration - date d’édition|Date|Dernière édition produite au format BNIA.|
|Déclaration - date de facturation|Date|Date reprise depuis la facture liée lorsque la DSM est<br>facturée.|
|Déclaration - pièce de<br>facturation liée|Référentiel|Référence de la facture ou du document émis à partir de la<br>DSM.|

## C.3.2 Ligne DSM
### C.3.2.1 Clé commune de ligne
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes DSM|Table fille|Lignes détaillées de stock, de mouvement, de vente ou de<br>fabrication rattachées à une déclaration.|
|Ligne - dénomination / qualité|Texte court|Qualité ou dénomination portée par la ligne.|
|Ligne - millésime|Texte court|Millésime lorsque la ligne porte un millésime.|
|Ligne - clé de conditionnement|Référentiel|Clé de type`MB`,`MV`,`NB`,`NV`.|
|Ligne - compte d’âge|Référentiel|Compte d’âge associé à la ligne.|
|Ligne - note de contrôle|Texte long|Note laissée par le BNIA sur la ligne lorsqu’un<br>commentaire de contrôle est nécessaire.|

Les colonnes de déclaration sont communes aux producteurs et aux négociants. La colonne `Producteur` indique si la colonne est incluse dans le périmètre déclaratif du producteur ou grisée pour ce type d’opérateur. 

### C.3.2.2 Onglet Récapitulatif
|**Champ**|**Type**|**Producteur**|**Description**|
|---|---|---|---|
|Ligne - stock début de période<br>(1)|Donnée<br>calculée|Incluse|Reprise automatique du stock de fin de la<br>déclaration précédente.|
|Ligne - entrées achats à la<br>propriété (2)|Nombre|Grisée|Achats réalisés auprès de la propriété, des<br>producteurs ou des caves coopératives.|
|Ligne - entrées achats négoce<br>(3)|Nombre|Grisée|Achats réalisés auprès du négoce.|
|Ligne - autres entrées|Donnée<br>calculée|Incluse|Cumul des entrées issues de l’onglet<br>`Mouvements en entrée`.|
|Ligne - total des sorties|Donnée<br>calculée|Incluse|Cumul des sorties des onglets`Mouvements`<br>`en sortie`,`Ventes France`et`Ventes à`<br>`l'exportation`.|

<!-- p.109 -->
|**Champ**|**Type**|**Producteur**|**Description**|
|---|---|---|---|
|Ligne - stock fin de période|Donnée|Incluse|Stock final calculé à partir des achats,|
|(26)|calculée||entrées, sorties et ventes.|

### C.3.2.3 Onglet Mouvements en entrée
|**Champ**|**Type**|**Producteur**|**Description**|
|---|---|---|---|
|Ligne - distillation propre (4)|Nombre|Incluse|Entrée issue d’une distillation propre.|
|Ligne - passage sous bois /<br>sous-verre (5)|Nombre|Grisée|Mouvement d’entrée lié à un changement de<br>conditionnement ou d’élevage.|
|Ligne - assemblages / coupes<br>(6)|Nombre|Incluse|Mouvement d’entrée lié à des assemblages<br>ou coupes.|
|Ligne - transferts chai<br>Armagnac en région délimitée<br>(7)|Nombre|Incluse|Entrée par transfert en région délimitée.|
|Ligne - transferts chai<br>Armagnac hors région<br>délimitée (8)|Nombre|Incluse|Entrée par transfert hors région délimitée.|
|Ligne - réintégrations (9)|Nombre|Grisée|Réintégrations après annulation, retour ou<br>régularisation.|

### C.3.2.4 Contrôles croisés visibles
|**Champ**|**Type**|**Producteur**|**Description**|
|---|---|---|---|
|Ligne - contrôle croisé<br>assemblages / coupes en<br>sortie|Donnée<br>calculée|Incluse|Valeur de contrôle visible pour rapprocher les<br>mouvements symétriques.|
|Ligne - contrôle croisé|Donnée|Grisée|Valeur de contrôle visible pour rapprocher les|
|passage sous bois /|calculée||mouvements symétriques.|
|sous-verre en sortie||||

### C.3.2.5 Onglet Mouvements en sortie
|**Champ**|**Type**|**Producteur**|**Description**|
|---|---|---|---|
|Ligne - pertes sur inventaire<br>(10)|Nombre|Incluse|Pertes d’inventaire ou ajustements liés à la<br>`DAI`.|
|Ligne - passage sous bois /<br>sous-verre (11)|Nombre|Grisée|Mouvement de sortie lié à un changement de<br>conditionnement ou d’élevage.|
|Ligne - assemblages / coupes<br>(12)|Nombre|Incluse|Mouvement de sortie lié à des assemblages<br>ou coupes.|
|Ligne - transferts chai<br>Armagnac en région délimitée<br>(13)|Nombre|Incluse|Sortie par transfert en région délimitée.|

<!-- p.110 -->
|**Champ**|**Type**|**Producteur**|**Description**|
|---|---|---|---|
|Ligne - transferts chai<br>Armagnac en région délimitée -<br>note|Texte long|Incluse|Note associée à la sortie par transfert en<br>région délimitée.|
|Ligne - transferts chai<br>Armagnac hors région<br>délimitée (14)|Nombre|Incluse|Sortie par transfert hors région délimitée.|
|Ligne - fabrications Floc de<br>Gascogne (15)|Nombre|Incluse|Volume affecté aux fabrications`Floc de`<br>`Gascogne`.|
|Ligne - utilisation de<br>l’Armagnac pour autres<br>fabrications que l’AOC (16)|Nombre|Incluse|Volume d’Armagnac utilisé pour d’autres<br>fabrications que l’`AOC`.|
|Ligne - utilisation de<br>l’Armagnac pour autres<br>fabrications que l’AOC -<br>justificatif|Texte court|Incluse|Lien document justificatif fabrications.|

### C.3.2.6 Onglet Ventes France
|**Champ**|**Type**|**Producteur**|**Description**|
|---|---|---|---|
|Ligne - marché intérieur hors<br>chai en bouteilles (17)|Nombre|Incluse|Ventes France en bouteilles hors chai<br>Armagnac.|
|Ligne - marché intérieur hors<br>chai en vrac destiné à la mise<br>en bouteille (18)|Nombre|Incluse|Ventes France en vrac pour mise en bouteille<br>Armagnac.|
|Ligne - marché intérieur hors<br>chai en brandies (19)|Nombre|Grisée|Ventes France de`brandies`.|
|Ligne - négoce chai Armagnac<br>en région délimitée (20)|Nombre|Incluse|Ventes ou sorties vers le négoce en région<br>délimitée.|
|Ligne - négoce chai Armagnac<br>hors région délimitée (21)|Nombre|Incluse|Ventes ou sorties vers le négoce hors région<br>délimitée.|
|Ligne - négoce chai Armagnac<br>hors région délimitée -<br>justificatif|Texte court|Incluse|Lien document justificatif ventes hors région<br>délimitée.|

### C.3.2.7 Onglet Ventes à l’exportation
|**Champ**|**Type**|**Producteur**|**Description**|
|---|---|---|---|
|Ligne - bouteilles (22)|Nombre|Incluse|Volumes exportés en bouteilles.|
|Ligne - vrac destiné à la mise<br>en bouteille (23)|Nombre|Incluse|Volumes exportés en vrac pour mise en<br>bouteille Armagnac.|
|Ligne - brandies (24)|Nombre|Grisée|Volumes exportés en`brandies`.|
|Ligne - vins vinés et<br>fabrications autres (25)|Nombre|Grisée|Volumes affectés aux vins vinés et<br>fabrications associées.|

<!-- p.111 -->
## C.3.3 Justificatif DSM
### C.3.3.1 Pièce et pointage de contrôle
|**Champ**|**Type**|**Description**|
|---|---|---|
|Justificatifs DSM|Table fille|Pièces rattachées à une déclaration ou à une ligne DSM.|
|Justificatif - type de mouvement|Référentiel|Mouvement ou traitement auquel la pièce se rattache.|
|Justificatif - type de pièce|Référentiel|Nature de la pièce, par exemple contrat Armagnac, facture,<br>`DAE`, déclaration douanes.|
|Justificatif - document déposé|Texte court|Référence du fichier ou du document transmis.|
|Justificatif - déclaration liée|Référentiel|Déclaration DSM à laquelle la pièce est rattachée.|
|Justificatif - ligne liée|Référentiel|Ligne DSM concernée lorsque le rattachement est fin.|
|Justificatif - contrat Armagnac<br>lié|Référentiel|Contrat achat Armagnac utilisé comme pièce de<br>rapprochement lorsqu’il existe dans la plateforme.|
|Justificatif - facture liée|Référentiel|Facture liée lorsqu’une pièce de facturation interne sert de<br>rapprochement.|
|Justificatif - avoir lié|Référentiel|Avoir utilisé comme pièce de régularisation lorsqu’il existe.|
|Justificatif - référence`DAE`/`DAI`|Texte court|Référence portée par le document douanier lorsqu’elle doit<br>être tracée.|
|Justificatif - date de dépôt|Date|Date de dépôt ou d’ajout de la pièce.|
|Justificatif - déposé par|Référentiel|Utilisateur ou opérateur ayant déposé la pièce.|
|Justificatif - résultat du contrôle|Référentiel|Résultat du pointage BNIA sur la pièce ou le<br>rapprochement associé.|
|Justificatif - commentaire de<br>contrôle|Texte long|Commentaire interne sur le résultat du contrôle ou le<br>rapprochement documentaire.|

## C.3.4 DRM producteur
### C.3.4.1 Pièce mensuelle rattachée à la déclaration
|**Champ**|**Type**|**Description**|
|---|---|---|
|`DRM`producteur|Table fille|Déclarations récapitulatives mensuelles douanes<br>attendues avec une`DSM`de type producteur.|
|`DRM`- déclaration liée|Référentiel|Déclaration`DSM`à laquelle la pièce est rattachée.|
|`DRM`- mois couvert|Texte court|Mois de la période de déclaration couvert par la pièce<br>(`MMAAAA`).|
|`DRM`- document déposé|Texte court|Référence du fichier`PDF`transmis et archivé.|
|`DRM`- date de dépôt|Date|Date de dépôt de la pièce.|
|`DRM`- déposé par|Référentiel|Utilisateur BNIA ou opérateur ayant déposé la pièce.|

## C.3.5 Alerte et relance DSM

<!-- p.112 -->
### C.3.5.1 Suivi des alertes et relances
|**Champ**|**Type**|**Description**|
|---|---|---|
|Alertes et relances DSM|Table fille|Ensemble des alertes et relances émises sur les<br>déclarations ou justificatifs attendus.|
|Alerte / relance - type d’alerte|Référentiel|Nature du point signalé, par exemple déclaration absente,<br>justificatif manquant ou incohérence bloquante.|
|Alerte / relance - niveau de<br>rattachement|Référentiel|Niveau concerné : déclaration entière ou ligne de<br>déclaration.|
|Alerte / relance - déclaration liée|Référentiel|Déclaration`DSM`concernée par l’alerte ou la relance.|
|Alerte / relance - ligne liée|Référentiel|Ligne de déclaration concernée lorsque le rattachement est<br>fin.|
|Alerte / relance - opérateur<br>concerné|Référentiel|Opérateur visé par l’action de suivi.|
|Alerte / relance - contact<br>destinataire|Référentiel|Contact opérateur destinataire lorsque la relance est<br>adressée nominativement.|
|Alerte / relance - date<br>d’émission|Date|Date d’émission ou de mise à disposition de l’alerte.|
|Alerte / relance - échéance<br>d’action|Date|Date limite attendue pour la régularisation.|
|Alerte / relance - statut de<br>traitement|Référentiel|État de suivi de l’alerte ou de la relance.|
|Alerte / relance - commentaire<br>de suivi|Texte long|Observation portée par le BNIA sur le traitement du point.|

# C.4 Contrats d’achat à la propriété
## C.4.1 Contrats Armagnac
### C.4.1.1 Contrat achat Armagnac
### C.4.1.1.1 Socle du contrat
|**Champ**|**Type**|**Description**|
|---|---|---|
|Type de contrat|Référentiel|Valeur de type`Achat Armagnac`, utilisée pour distinguer le<br>parcours de traitement.|
|Identifiant contrat BNIA|Identifiant|Référence interne unique du contrat.|
|Numéro de contrat BNIA|Identifiant|Numéro visible dans les listings et éditions du contrat.|
|Date du contrat|Date|Date de signature ou de formalisation du contrat.|
|Numéro`VISA`|Identifiant|Référence de suivi utilisée dans les consultations, éditions<br>et rapprochements.|
|Code payeur|Identifiant|Référence utile aux traitements de facturation lorsqu’elle<br>s’applique.|
|Acheteur - code opérateur BNIA|Référentiel|Rattachement de l’acheteur à la fiche opérateur BNIA.|
|Vendeur - code opérateur BNIA|Référentiel|Rattachement du vendeur à la fiche opérateur BNIA.|