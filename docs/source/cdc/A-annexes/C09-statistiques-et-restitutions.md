<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 126-127 -->

<!-- p.126 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Ligne - unité métier|Texte court|Unité de calcul ou de présentation de la ligne d’avoir.|
|Ligne - prix unitaire`HT`|Nombre|Prix unitaire hors taxes retenu pour la ligne d’avoir.|
|Ligne - montant`HT`|Nombre|Montant hors taxes de la ligne d’avoir.|
|Ligne - taux de`TVA`|Référentiel|Taux de`TVA`appliqué à la ligne d’avoir.|
|Ligne - montant`TVA`|Nombre|Montant de`TVA`de la ligne d’avoir.|
|Ligne - montant`TTC`|Nombre|Montant toutes taxes comprises de la ligne d’avoir.|

# C.9 Statistiques et restitutions
## C.9.1 Demande de restitution statistique
### C.9.1.1 Paramètres d’extraction
|**Champ**|**Type**|**Description**|
|---|---|---|
|Type de restitution|Référentiel|Famille statistique demandée, par exemple marché<br>Armagnac, suivi`DSM`, achats à la propriété ou extraction<br>filière.|
|Date de début|Date|Début de la plage d’extraction lorsque la restitution porte<br>sur une période.|
|Date de fin|Date|Fin de la plage d’extraction lorsque la restitution porte sur<br>une période.|
|Date de référence|Date|Date de calcul utilisée pour les états de stock.|
|Type d’opérateur|Référentiel|Filtre ou axe de consolidation par type d’opérateur.|
|Opérateur|Référentiel|Filtre ou axe de consolidation par opérateur.|
|Qualité|Référentiel|Axe de consolidation par qualité, notamment millésime ou<br>compte d’âge selon la restitution.|
|Pays ou zone d’export|Référentiel|Filtre ou axe de consolidation par pays, continent, Union<br>européenne ou zone d’export.|
|Département|Référentiel|Axe de consolidation utilisé dans les restitutions de<br>production Armagnac.|
|Appellation|Référentiel|Axe de consolidation utilisé dans les restitutions de<br>production Armagnac.|
|Type de conditionnement ou<br>d’utilisation|Référentiel|Distinction par bouteille, vrac ou usage métier lorsque la<br>restitution l’exige.|
|Format d’export|Référentiel|Format de sortie attendu, notamment tableau`Excel`.|

## C.9.2 Restitution statistique
### C.9.2.1 Ligne de restitution

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