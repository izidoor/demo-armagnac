<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 118-119 -->

<!-- p.118 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Statut validation vendeur|Référentiel|État de validation porté par le vendeur.|
|Statut validation BNIA|Référentiel|État de traitement ou de validation BNIA.|
|Acheteur - code opérateur BNIA|Référentiel|Rattachement de l’acheteur.|
|Vendeur - code opérateur BNIA|Référentiel|Rattachement du vendeur.|
|Type de produit|Référentiel|Qualification du produit concerné.|
|Volume|Nombre|Volume proposé.|
|Cépage|Texte court|Cépage porté par la proposition.|
|Prix départ HT|Nombre|Prix de base hors taxes.|
|Conditions et calendrier de<br>retiraison|Texte long|Bloc libre décrivant les retraits prévus.|
|Acompte activé|Booléen|Indique si un acompte est prévu.|
|Paiement comptant|Booléen|Indique un règlement comptant.|
|Paiement à`45`jours|Booléen|Indique l’usage de ce délai lorsqu’il est proposé.|
|Paiement à`60`jours|Booléen|Indique l’usage de ce délai lorsqu’il est proposé.|
|Échéancier|Texte long|Calendrier libre de paiement si nécessaire.|
|Distillation - lieu|Référentiel|Lieu de distillation associé au dossier.|
|Distillation - méthode|Référentiel|Méthode de distillation associée au dossier.|
|Distillation - adresse|Texte long|Adresse ou précision logistique associée à la distillation.|

# C.5 Déclarations de fin de travaux de distillation
## C.5.1 Déclaration de fin de travaux de distillation
### C.5.1.1 Socle de la déclaration
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant déclaration fin de<br>travaux BNIA|Identifiant|Référence interne attribuée à la déclaration intégrée.|
|Numéro déclaration ODG|Identifiant|Référence portée par l’ODG sur la déclaration source.|
|Date de la déclaration|Date|Date de formalisation de la déclaration.|
|Propriétaire - code opérateur<br>BNIA|Référentiel|Rattachement du propriétaire au référentiel BNIA.|
|Propriétaire -`CVI`|Identifiant|Référence viticole utile au rapprochement.|
|Propriétaire -`SIRET`|Identifiant|Identifiant administratif du propriétaire.|
|Distillateur - code opérateur<br>BNIA|Référentiel|Rattachement du distillateur au référentiel BNIA.|
|Distillateur -`CVI`|Identifiant|Référence viticole utile au rapprochement.|
|Distillateur -`SIRET`|Identifiant|Identifiant administratif du distillateur.|
|Lieu de distillation|Référentiel|Valeur de type`À domicile`ou`Chez un bouilleur`.|

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