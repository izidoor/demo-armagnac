<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 119-121 -->

<!-- p.119 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Méthode de distillation|Référentiel|Valeur de type`Double chauffe`ou`Continue`.|
|Cépage|Référentiel|Valeur de type`Monocépage`ou`Assemblage`lorsque cette<br>distinction est portée.|
|Destination|Référentiel|Destination de l’alcool issu de la distillation.|
|Commentaire|Texte long|Observation associée à la déclaration.|
|Montant des cotisations|Nombre|Montant calculé sur le dossier de distillation.|
|Document`PDF`de la déclaration|Texte court|Référence à l’édition ou au document transmis.|

### C.5.1.2 Lignes de la déclaration
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes de déclaration de fin de<br>travaux|Table fille|Ensemble des lignes produit rattachées à la déclaration.|
|Ligne - appellation|Référentiel|Appellation portée par la ligne.|
|Ligne - commune|Texte court|Commune portée par la ligne.|
|Ligne - cépage|Texte court|Cépage porté par la ligne.|
|Ligne - n° de cuve|Texte court|Référence de cuve lorsqu’elle existe.|
|Ligne - degré|Nombre|Degré alcoolique associé à la ligne.|
|Ligne - volume|Nombre|Volume porté par la ligne.|
|Ligne - volume alcool pur|Nombre|Volume en alcool pur porté par la ligne.|
|Ligne - montant de cotisation|Nombre|Montant de cotisation rattaché à la ligne lorsqu’il est<br>détaillé.|

### C.5.1.3 Traitement BNIA et facturation
|**Champ**|**Type**|**Description**|
|---|---|---|
|Date d’intégration|Date|Date de création ou de mise à jour de la déclaration dans le<br>système BNIA.|
|Numéro`VISA`|Identifiant|Référence de suivi de la retombée`ODG`.|
|Code payeur|Identifiant|Référence utile à la facturation du dossier.|
|État de facturation|Référentiel|État de la déclaration dans le cycle de facturation.|
|Facture liée|Référentiel|Référence de la facture issue de la déclaration.|

# C.6 Suivi des sorties export
## C.6.1 Suivi sortie export

<!-- p.120 -->
### C.6.1.1 Socle du suivi sortie export
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant suivi sortie export<br>BNIA|Identifiant|Référence interne unique du suivi sortie export.|
|Date du suivi sortie export|Date|Date de formalisation du dossier.|
|Vendeur - code opérateur BNIA|Référentiel|Rattachement du vendeur à la fiche opérateur BNIA.|
|Pays de destination|Référentiel|Pays de destination du suivi sortie export.|
|Type de justificatif source|Référentiel|Valeur de type`DAE`,`DAI`ou`Facture`.|
|Document justificatif source|Texte court|Référence du fichier rattaché au dossier.|
|Mode d’alimentation|Référentiel|Valeur de type`Saisie manuelle`ou`Import Excel`.|
|Commentaire de dossier|Texte long|Observation interne utile au suivi.|

### C.6.1.2 Lignes de vente export
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes de vente export|Table fille|Ensemble des lignes produit rattachées au suivi sortie<br>export.|
|Ligne - date de vente|Date|Date de la vente export portée par la ligne.|
|Ligne - pays de destination|Référentiel|Pays de destination porté par la ligne, utilisé au<br>rattachement lors d’un import.|
|Ligne - marque|Référentiel|Marque portée par la ligne lorsqu’elle existe.|
|Ligne - dénomination|Texte court|Dénomination commerciale ou qualité produit.|
|Ligne - millésime|Texte court|Millésime porté par la ligne lorsqu’il existe.|
|Ligne - compte d’âge|Référentiel|Compte d’âge du produit exporté, y compris au-delà de<br>`20+`.|
|Ligne - type de conditionnement|Référentiel|Valeur de type`Bouteille`ou`Vrac`.|
|Ligne - nombre de bouteilles|Nombre|Nombre de bouteilles lorsque la ligne est conditionnée.|
|Ligne - quantité vrac|Nombre|Quantité en vrac lorsque la ligne n’est pas conditionnée.|
|Ligne - contenance|Référentiel|Contenance de référence utilisée pour le calcul et les<br>restitutions.|
|Ligne - degré d’alcool|Nombre|Degré alcoolique de la ligne.|
|Ligne - volume alcool pur|Nombre|Volume en alcool pur exprimé en`Hl.l,cl`, calculé en<br>saisie directe et déclaré à l’import.|
|Ligne - valeur|Nombre|Valeur économique portée par la ligne lorsqu’elle est utile<br>au dossier.|
|Ligne - origine|Référentiel|Valeur de type`Saisie manuelle`ou`Import Excel`,<br>conservée au niveau de la ligne.|

### C.6.1.3 Historique et suivi

<!-- p.121 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Date de création du suivi sortie<br>export|Date|Date de création du dossier dans la plateforme BNIA.|
|Suivi sortie export créé par|Référentiel|Utilisateur BNIA à l’origine de la création du dossier.|
|Date de dernière modification<br>du suivi sortie export|Date|Dernière date de mise à jour du dossier.|
|Suivi sortie export modifié par|Référentiel|Utilisateur BNIA à l’origine de la dernière modification.|
|Historique des actions suivi<br>sortie export|Table fille|Historique des créations, modifications, imports et<br>corrections du suivi sortie export.|
|Action historique - date|Date|Date de l’action tracée sur le suivi sortie export.|
|Action historique - type|Référentiel|Nature de l’action tracée sur le suivi sortie export.|
|Action historique - auteur|Référentiel|Utilisateur BNIA à l’origine de l’action.|
|Action historique - commentaire|Texte long|Observation utile pour expliciter l’action ou la correction<br>apportée.|

### C.6.1.4 Documents et certificats liés
|**Champ**|**Type**|**Description**|
|---|---|---|
|Justificatif source conservé|Texte court|Référence du justificatif source conservé dans le dossier<br>export.|
|Date de dépôt du justificatif|Date|Date de rattachement du justificatif source au suivi sortie|
|source||export.|
|Certificats export générés|Table fille|Certificats générés à partir du suivi sortie export.|
|Certificat export lié|Référentiel|Certificat export généré à partir du suivi sortie export.|

# C.7 Certification export
## C.7.1 Demande de certificat export
### C.7.1.1 Suivi de la demande
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant demande de certificat|Identifiant|Référence unique de la demande transmise par l’opérateur.|
|Date de la demande|Date|Date de création de la demande.|
|Opérateur demandeur - code<br>BNIA|Référentiel|Rattachement de la demande à l’opérateur concerné.|
|Pays de destination demandé|Référentiel|Pays de destination pour lequel le certificat est demandé.|
|Suivi sortie export lié|Référentiel|Référence du suivi sortie export utilisé comme base de<br>traitement lorsqu’il est déjà connu.|
|État de traitement de la<br>demande|Référentiel|Valeur de suivi de type`À traiter`,`En cours`,`Complétée`,<br>`Certificat généré`.|