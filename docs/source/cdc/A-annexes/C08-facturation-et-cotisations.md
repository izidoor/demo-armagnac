<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 123-126 -->

<!-- p.123 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Niveau de détail du certificat|Référentiel|Valeur de type`Cumul par degré`,`Détail comptes d'âge`,<br>`Affichage millésime`.|
|Document`PDF`du certificat|Texte court|Référence de l’édition générée et mise à disposition.|
|Date de mise à disposition<br>portail|Date|Date à laquelle le certificat est rendu accessible à<br>l’opérateur.|

### C.7.2.2 Détail produit du certificat
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes ou regroupements du<br>certificat|Table fille|Ensemble des lignes ou regroupements repris dans le<br>certificat.|
|Ligne - degré d’alcool|Nombre|Degré alcoolique restitué sur le certificat.|
|Ligne - compte d’âge|Référentiel|Compte d’âge restitué, y compris au-delà de`20+`.|
|Ligne - millésime|Texte court|Millésime restitué lorsque ce niveau de détail est retenu.|
|Ligne - volume|Nombre|Volume restitué dans le certificat.|
|Ligne - volume alcool pur|Nombre|Volume en alcool pur utilisé dans les restitutions du<br>certificat.|

# C.8 Facturation et cotisations
## C.8.1 Tiers de facturation
### C.8.1.1 Client ou opérateur facturé
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant tiers de facturation|Identifiant|Référence interne du tiers utilisé en facturation.|
|Type de tiers facturé|Référentiel|Valeur de type`Opérateur BNIA`ou`Client`.|
|Code opérateur BNIA|Référentiel|Rattachement à la fiche opérateur lorsqu’il s’agit d’un<br>opérateur.|
|Raison sociale / nom du tiers|Texte court|Libellé du tiers facturé.|
|Adresse ligne 1|Texte court|Adresse de facturation.|
|Adresse ligne 2|Texte court|Complément d’adresse.|
|Code postal|Texte court|Code postal de facturation.|
|Ville|Texte court|Ville de facturation.|
|Pays|Référentiel|Pays de facturation.|
|Adresse électronique de<br>facturation|Texte court|Mél de contact utile à la diffusion documentaire.|

<!-- p.124 -->
## C.8.2 Produit de facturation
### C.8.2.1 Référentiel produit
|**Champ**|**Type**|**Description**|
|---|---|---|
|Référence produit|Identifiant|Référence unique du produit de facturation.|
|Libellé produit|Texte court|Libellé du produit utilisé dans les lignes de facture diverse.|
|Taux de`TVA`associé|Référentiel|Taux de`TVA`applicable au produit.|
|Produit actif|Booléen|Indique si le produit est utilisable dans les nouvelles<br>factures.|

## C.8.3 Facture
### C.8.3.1 Socle de la facture
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant facture BNIA|Identifiant|Référence interne unique de la facture.|
|Numéro de facture|Identifiant|Numérotation officielle de la facture.|
|Type de facture|Référentiel|Valeur de type`Cotisation DSM`,`Cotisation fin de`<br>`travaux de distillation`,`Facture diverse`ou<br>`Commande boutique`.|
|Date de facture<br>i|Date|Date d’émission de la facture.|
|Tiers facturé - identifiant|Référentiel|Référence du tiers facturé.|
|Libellé de la période facturée|Texte court|Période affichée sur la facture lorsqu’elle doit être adaptée<br>au dossier.|
|`DSM`liée|Référentiel|Référence de la déclaration source lorsqu’il s’agit d’une<br>cotisation`DSM`.|
|Déclaration de fin de travaux de<br>distillation liée|Référentiel|Référence de la déclaration source lorsqu’il s’agit d’une<br>cotisation sur fin de travaux de distillation.|
|Commande boutique liée|Référentiel|Référence de la commande source lorsqu’il s’agit d’une<br>facture issue de la boutique.|
|Taux de cotisation appliqué|Nombre|Taux utilisé dans le calcul du dossier lorsqu’il s’agit d’une<br>cotisation.|
|Pourcentage d’exonération de<br>`TVA`appliqué|Nombre|Pourcentage retenu dans le calcul de`TVA`sur le dossier.|
|Montant`HT`|Nombre|Montant total hors taxes de la facture.|
|Montant`TVA`|Nombre|Montant total de`TVA`.|
|Montant`TTC`|Nombre|Montant total toutes taxes comprises.|
|Document`PDF`de la facture|Texte court|Référence de l’édition générée.|
|Date de dépôt dans`Zeendoc`|Date|Date de transmission du`PDF`vers`Zeendoc`.|
|État de traitement`Zeendoc`|Référentiel|État documentaire ou de paiement restitué par`Zeendoc`.|

<!-- p.125 -->
### C.8.3.2 Lignes de facture
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes de facture|Table fille|Ensemble des lignes composant la facture.|
|Ligne - produit|Référentiel|Produit ou nature de facturation rattaché à la ligne.|
|Ligne - quantité|Nombre|Quantité facturée sur la ligne.|
|Ligne - unité métier|Texte court|Unité de calcul ou de présentation de la ligne.|
|Ligne - prix unitaire`HT`|Nombre|Prix unitaire hors taxes appliqué.|
|Ligne - montant`HT`|Donnée<br>calculée|Montant hors taxes de la ligne.|
|Ligne - taux de`TVA`|Référentiel|Taux de`TVA`appliqué à la ligne.|
|Ligne - montant`TVA`|Donnée<br>calculée|Montant de`TVA`calculé sur la ligne.|
|Ligne - montant`TTC`|Donnée<br>calculée|Montant toutes taxes comprises de la ligne.|

## C.8.4 Avoir
### C.8.4.1 Socle de l’avoir
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant avoir BNIA|Identifiant|Référence interne unique de l’avoir.|
|Numéro d’avoir|Identifiant|Numérotation officielle de l’avoir.|
|Facture d’origine|Référentiel|Référence de la facture annulée ou corrigée.|
|Type d’avoir|Référentiel|Valeur de type`Total`ou`Partiel`.|
|Date de l’avoir|Date|Date d’émission de l’avoir.|
|Motif de l’avoir|Texte long|Motif de correction ou d’annulation.|
|Montant`HT`de l’avoir|Nombre|Montant total hors taxes de l’avoir.|
|Montant`TVA`de l’avoir|Nombre|Montant total de`TVA`de l’avoir.|
|Montant`TTC`de l’avoir|Nombre|Montant total toutes taxes comprises de l’avoir.|
|Document`PDF`de l’avoir|Texte court|Référence de l’édition générée.|

### C.8.4.2 Lignes de l’avoir
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes d’avoir|Table fille|Ensemble des lignes reprises dans l’avoir, notamment<br>lorsqu’il est partiel.|
|Ligne - produit|Référentiel|Produit ou nature de facturation repris dans la ligne d’avoir.|
|Ligne - quantité|Nombre|Quantité annulée ou corrigée sur la ligne d’avoir.|

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