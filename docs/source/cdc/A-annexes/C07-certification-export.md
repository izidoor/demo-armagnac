<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 121-123 -->

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

<!-- p.122 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Certificat export créé|Référentiel|Certificat généré à partir de la demande lorsqu’il existe.|
|Compte BNIA traitant|Référentiel|Compte interne ayant repris ou traité la demande.|
|Date de traitement|Date|Date de prise en charge ou de finalisation du traitement.|
|Commentaire de traitement|Texte long|Observation interne associée à la demande.|

### C.7.1.2 Contenu de la demande et pièces transmises
|**Champ**|**Type**|**Description**|
|---|---|---|
|Destinataire export demandé|Référentiel|Destinataire communiqué par l’opérateur pour l’instruction<br>du certificat.|
|Destinataire - nom transmis|Texte court|Libellé du destinataire porté dans la demande lorsqu’il doit<br>être contrôlé.|
|Justificatifs transmis|Table fille|Ensemble des justificatifs joints à la demande.|
|Justificatif transmis - type|Référentiel|Nature de la pièce jointe, par exemple facture ou`DAE`.|
|Justificatif transmis - document|Texte court|Référence du fichier transmis avec la demande.|
|Lignes produit demandées|Table fille|Produits exportés ou regroupements transmis pour<br>l’instruction.|
|Ligne demandée - désignation<br>produit|Texte court|Désignation produit utile à l’instruction de la demande.|
|Ligne demandée - compte d’âge|Référentiel|Compte d’âge communiqué pour le produit concerné.|
|Ligne demandée - millésime|Texte court|Millésime communiqué lorsqu’il existe.|
|Ligne demandée - volume|Nombre|Volume communiqué pour le produit exporté.|

## C.7.2 Certificat export
### C.7.2.1 Socle du certificat
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant certificat export|Identifiant|Référence interne unique du certificat généré.|
|Numéro du certificat|Identifiant|Numérotation du certificat selon la séquence retenue par le<br>BNIA.|
|Date du certificat|Date|Date d’édition du certificat.|
|Suivi sortie export de référence|Référentiel|Suivi sortie export à partir duquel le certificat est généré.|
|Opérateur exportateur - code<br>BNIA|Référentiel|Rattachement du certificat à l’opérateur expéditeur.|
|Destinataire export|Référentiel|Rattachement du destinataire au référentiel dédié.|
|Pays d’origine|Référentiel|Pays d’origine affiché sur le certificat.|
|Pays de destination|Référentiel|Pays de destination affiché sur le certificat.|
|Modèle linguistique du certificat|Référentiel|Valeur de type`FR + EN`,`FR + ES`ou`FR + IT`selon le pays.|

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