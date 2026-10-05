<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 112-118 -->

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

<!-- p.113 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Courtier|Texte court|Intermédiaire commercial s’il existe dans le dossier.|
|Courtier à|Texte court|Partie ou modalité de courtage associée.|
|Conditions de retiraison|Texte long|Calendrier ou règle de retrait du produit.|
|Conditions de paiement|Texte long|Modalités de règlement du contrat.|
|Proposition d’engagement<br>rattachée|Référentiel|Lien vers la proposition d’engagement lorsqu’il existe.|
|Pluri-annuel rattaché|Référentiel|Lien vers le pluri-annuel Armagnac lorsqu’il existe.|
|Statut validation acheteur|Référentiel|État de validation porté par l’acheteur.|
|Statut validation vendeur|Référentiel|État de validation porté par le vendeur.|
|Statut validation BNIA|Référentiel|État de validation ou d’invalidation porté par le BNIA.|
|Commentaire|Texte long|Observation associée au contrat.|

### C.4.1.1.2 Lignes du contrat
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes du contrat Armagnac|Table fille|Ensemble des lignes produit rattachées au contrat.|
|Ligne - appellation|Référentiel|Appellation portée par la ligne.|
|Ligne - compte d’âge|Référentiel|Compte d’âge de l’Armagnac concerné.|
|Ligne - millésime|Texte court|Millésime lorsque la ligne le porte.|
|Ligne - dénomination|Texte court|Dénomination ou qualité produit.|
|Ligne - volume|Nombre|Volume commercialisé.|
|Ligne - degré|Nombre|Degré alcoolique associé à la ligne.|
|Ligne - volume alcool pur|Nombre|Volume en alcool pur exploité dans les contrôles et<br>statistiques.|
|Ligne - prix €/`hlAP`|Nombre|Prix ramené à l’hectolitre d’alcool pur.|
|Ligne - prix €/litre vol|Nombre|Prix ramené au litre volumique lorsque cette unité est<br>utilisée.|
|Ligne - montant total HT|Nombre|Montant hors taxes de la ligne.|

### C.4.1.1.3 Historique et validations
|**Champ**|**Type**|**Description**|
|---|---|---|
|Date de validation acheteur|Date|Date à laquelle l’acheteur a validé le contrat.|
|Compte / contact de validation<br>acheteur|Référentiel|Compte ou contact ayant réalisé la validation côté acheteur.|
|Date de validation vendeur|Date|Date à laquelle le vendeur a validé le contrat.|
|Compte / contact de validation<br>vendeur|Référentiel|Compte ou contact ayant réalisé la validation côté vendeur.|
|Date de validation BNIA|Date|Date de validation finale portée par le BNIA.|

<!-- p.114 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Compte BNIA validateur|Référentiel|Utilisateur BNIA ayant validé le contrat.|
|Historique des actions contrat|Table fille|Historique des créations, modifications, validations et<br>invalidations du contrat.|
|Action historique - date|Date|Date de l’action tracée sur le contrat.|
|Action historique - type|Référentiel|Nature de l’action tracée sur le contrat.|
|Action historique - auteur|Référentiel|Compte ou contact à l’origine de l’action.|
|Action historique - commentaire<br>**C.4.1.1.4 Documents liés**|Texte long|Observation utile pour expliciter l’action ou sa correction.|
|**Champ**|**Type**|**Description**|
|Document`PDF`du contrat|Texte court|Référence de l’édition`PDF`du contrat mise à disposition ou<br>conservée par le BNIA.|
|Dernière date d’édition du<br>contrat|Date|Date de la dernière génération du document contractuel.|

### C.4.1.1.4 Documents liés
### C.4.1.2 Pluri-annuel Armagnac
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant pluri-annuel<br>Armagnac|Identifiant|Référence interne unique du dossier.|
|Vendeur - code opérateur BNIA|Référentiel|Rattachement du vendeur.|
|Vendeur - représenté par|Texte court|Personne représentant le vendeur si renseignée.|
|Vendeur - en qualité de|Texte court|Qualité ou fonction du représentant du vendeur.|
|Acheteur - code opérateur BNIA|Référentiel|Rattachement de l’acheteur.|
|Acheteur - représenté par|Texte court|Personne représentant l’acheteur si renseignée.|
|Acheteur - en qualité de|Texte court|Qualité ou fonction du représentant de l’acheteur.|
|Date de début|Date|Date de départ de l’engagement.|
|Durée du contrat|Nombre|Durée exprimée en années.|
|Volume annuel engagé|Nombre|Volume annuel d’Armagnac concerné.|
|Compte d’âge|Référentiel|Compte d’âge de l’obligation produit.|
|Millésime|Texte court|Millésime si spécifié dans l’engagement.|
|Appellation|Référentiel|Appellation si spécifiée dans l’engagement.|
|`TAV`|Nombre|Titre alcoométrique volumique attendu si spécifié.|
|Mode de retiraison|Référentiel|Valeur de type`En une fois`ou`En plusieurs fois`.|
|Retiraison - volume|Nombre|Volume prévu lorsque la retiraison est précisée.|
|Retiraison - date limite|Date|Date maximale de retiraison lorsque la clause existe.|
|Prix 1re campagne|Nombre|Prix de base de la première campagne.|
|Modalités de révision du prix|Texte long|Texte décrivant la règle de révision.|

<!-- p.115 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Acompte activé|Booléen|Indique si un acompte est prévu.|
|Acompte - pourcentage|Nombre|Pourcentage d’acompte lorsqu’il existe.|
|Mode de paiement|Référentiel|Valeur de type`En une fois`ou`En plusieurs fois`.|
|Paiement - montant|Nombre|Montant ou base de paiement à l’unité retenue.|
|Paiement - date limite|Date|Date maximale de règlement lorsqu’elle est précisée.|

### C.4.1.3 Proposition d’engagement Armagnac
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant proposition Armagnac|Identifiant|Référence interne unique de la proposition.|
|Date de la proposition|Date|Date de création ou de formalisation.|
|Acheteur - code opérateur BNIA|Référentiel|Rattachement de l’acheteur à la fiche opérateur.|
|Vendeur - code opérateur BNIA|Référentiel|Rattachement du vendeur à la fiche opérateur.|
|Courtier|Texte court|Intermédiaire commercial s’il existe.|
|Courtier à|Texte court|Modalité de courtage associée.|
|Compte d’âge|Référentiel|Compte d’âge du produit proposé.|
|Volume|Nombre|Volume concerné par la proposition.|
|Prix|Nombre|Prix proposé.|
|Unité de prix|Référentiel|Valeur de type`€/hlAP`ou`€/hl volume`.|
|Commune|Texte court|Commune si précisée dans le dossier.|
|Cépage|Texte court|Cépage si précisé dans le dossier.|
|Appellation|Référentiel|Appellation si précisée dans le dossier.|
|Conditions et calendrier de<br>retiraison|Texte long|Bloc libre décrivant les modalités de retrait.|
|Acompte activé|Booléen|Indique si un acompte est prévu.|
|Acompte - pourcentage|Nombre|Pourcentage d’acompte lorsqu’il existe.|
|Paiement comptant|Booléen|Indique un règlement comptant.|
|Paiement à`30`jours fin de mois|Booléen|Indique l’usage de cette modalité de paiement.|
|Échéancier|Texte long|Calendrier libre de paiement lorsqu’il existe.|

## C.4.2 Contrats vin
### C.4.2.1 Contrat achat de vin de distillation
### C.4.2.1.1 Socle du contrat
|**Champ**<br>Identifiant contrat vin BNIA|**Type**<br>Identifiant|**Description**<br>Référence interne attribuée lors de l’intégration du flux.|
|---|---|---|

<!-- p.116 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Numéro de contrat ODG|Identifiant|Référence source portée par l’ODG.|
|Date du contrat|Date|Date de formalisation du contrat.|
|Campagne|Texte court|Campagne de rattachement du contrat.|
|Acheteur - code opérateur BNIA|Référentiel|Rattachement de l’acheteur.|
|Vendeur - code opérateur BNIA|Référentiel|Rattachement du vendeur.|
|Commentaire|Texte long|Observation portée par le dossier.|

### C.4.2.1.2 Lignes du contrat
|**Champ**|**Type**|**Description**|
|---|---|---|
|Lignes du contrat vin|Table fille|Ensemble des lignes produit rattachées au contrat.|
|Ligne - appellation|Référentiel|Appellation ou objet produit porté par la ligne.|
|Ligne - commune|Texte court|Commune d’origine ou de rattachement.|
|Ligne - cépage|Texte court|Cépage porté par la ligne.|
|Ligne - n° de cuve|Texte court|Référence de cuve lorsqu’elle existe.|
|Ligne - degré|Nombre|Degré alcoolique.|
|Ligne - volume|Nombre|Volume en litres ou unité métier équivalente.|
|Ligne - volume alcool pur|Nombre|Volume en alcool pur exploitable en contrôle et statistique.|
|Ligne - prix départ HT|Nombre|Prix de départ hors taxes de la ligne.|

### C.4.2.1.3 Synchronisation et traçabilité
|**Champ**|**Type**|**Description**|
|---|---|---|
|Date d’intégration BNIA|Date|Date d’intégration initiale du contrat dans la plateforme<br>BNIA.|
|Source du flux ODG|Référentiel|Source ou connecteur métier à l’origine de la<br>synchronisation du contrat.|
|Date de dernière<br>synchronisation|Date|Dernière date de mise à jour du contrat par le flux`ODG`.|

### C.4.2.1.4 Documents liés
|**Champ**|**Type**|**Description**|
|---|---|---|
|Document`PDF`du contrat|Texte court|Référence au document édité ou synchronisé.|

### C.4.2.2 Pluri-annuel vin

<!-- p.117 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant pluri-annuel vin|Identifiant|Référence interne du dossier.|
|Vendeur - code opérateur BNIA|Référentiel|Rattachement du vendeur.|
|Vendeur - représenté par|Texte court|Personne représentant le vendeur si renseignée.|
|Vendeur - en qualité de|Texte court|Qualité ou fonction du représentant.|
|Acheteur - code opérateur BNIA|Référentiel|Rattachement de l’acheteur.|
|Acheteur - représenté par|Texte court|Personne représentant l’acheteur si renseignée.|
|Acheteur - en qualité de|Texte court|Qualité ou fonction du représentant.|
|Objet du contrat|Référentiel|Valeur de type`Raisins`,`Moûts`ou`Vin de distillation`.|
|Date de départ|Date|Date de début de l’engagement.|
|Durée du contrat|Nombre|Durée exprimée en années.|
|Volume annuel engagé|Nombre|Volume annuel prévu dans l’engagement.|
|Commune|Texte court|Commune précisée dans les caractéristiques.|
|Cépage|Texte court|Cépage précisé dans les caractéristiques.|
|Appellation|Référentiel|Appellation précisée dans les caractéristiques.|
|`TAV`|Nombre|Critère qualitatif alcoométrique.|
|Microdistillation|Texte court|Critère qualitatif ou résultat associé.|
|Acidité volatile|Texte court|Critère qualitatif du dossier.|
|`Sur lies`|Texte court|Critère qualitatif du dossier.|
|Autres critères qualitatifs|Texte long|Complément de qualification libre.|
|Mode de retiraison|Référentiel|Valeur de type`En une fois`ou`En plusieurs fois`.|
|Retiraison - volume|Nombre|Volume concerné par la clause de retiraison si précisé.|
|Retiraison - date limite|Date|Date maximale de retiraison si précisée.|
|Prix 1re campagne|Nombre|Prix de base de la première campagne.|
|Modalités de révision du prix|Texte long|Règle de révision pour les campagnes suivantes.|
|Acompte activé|Booléen|Indique si un acompte est prévu.|
|Acompte - pourcentage|Nombre|Pourcentage d’acompte lorsqu’il existe.|
|Mode de paiement|Référentiel|Valeur de type`En une fois`ou`En plusieurs fois`.|
|Paiement - montant|Nombre|Montant ou base de paiement.|
|Paiement - date limite|Date|Date maximale de règlement si précisée.|
|Observations BNIA|Texte long|Commentaire interne de suivi du dossier.|

### C.4.2.3 Proposition d’engagement vin
|**Champ**|**Type**|**Description**|
|---|---|---|
|Identifiant proposition vin|Identifiant|Référence interne du dossier.|
|Date de la proposition|Date|Date de création ou de formalisation.|
|Statut validation acheteur|Référentiel|État de validation porté par l’acheteur.|

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