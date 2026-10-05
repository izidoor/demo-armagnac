<!-- Source : specification-reprise-donnees-agi-v1_1-2026-07-30.pdf · Vues attendues données pro.armagnac.fr · pp. 16-23 -->

<!-- p.16 -->
# 3 Vues attendues données** **`pro.armagnac.fr`
### 3.1 Contrat Armagnac
#### 3.1.1 Vue Contrat Armagnac > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat Armagnac|||
|Visas BNIA|||
|Campagne|||
|Date<br>i|||
|Identifiant acheteur<br>i|`professionnel`||
|Identifiant vendeur|`professionnel`||
|Courtier|||
|Courtier à|||
|Caractéristiques complémentaires du produit|||
|Appellation Armagnac||booléen|
|Appellation Bas Armagnac||booléen|
|Appellation Armagnac-Ténarèze||booléen|
|Appellation Haut Armagnac||booléen|
|Appellation Blanche Armagnac||booléen|
|Conditions et calendrier de retiraison|||
|Conditions de paiement > Acompte||booléen|
|Conditions de paiement > Pourcentage acompte|||
|Conditions de paiement > Comptant||booléen|
|Conditions de paiement > 30j à compter de la fin de mois<br>de livraison/retiraison||booléen|
|Échéancier|||
|Identifiant proposition d’engagement|`proposition`<br>`d'engagement`<br>`Armagnac`|Lorsque lié à une proposition|
|Identifiant contrat pluri-annuel|`pluri-annuel`<br>`Armagnac`|Lorsque lié à un pluri-annuel|
|Préavis de rupture|||
|Statut validation acheteur||booléen + date si existante?|
|Statut validation vendeur||booléen + date si existante?|
|Statut validation BNIA||booléen + date si existante?|

#### 3.1.2 Vue Contrat Armagnac > Lignes produits
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat Armagnac|`contrat Armagnac`||

<!-- p.17 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Appelation||Armagnac / Bas Armagnac /<br>Armagnac-Ténarèze / Haut<br>Armagnac / Blanche Armagnac|
|Millésime|||
|Compte d’âge|||
|Dénomination|||
|Volume||en litres|
|Degré||en %|
|Prix départ HT HL.AP|||
|Prix départ HT Litre volume|||

### 3.2 Pluri-annuel Armagnac
#### 3.2.1 Vue Pluri-annuel Armagnac > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant pluri-annuel Armagnac|||
|Visas BNIA<br>i|||
|Identifiant vendeur|`professionnel`||
|Vendeur > Représenté par|||
|Vendeur > En qualité de<br>i|||
|Identifiant acheteur|`professionnel`||
|Acheteur > Représenté par|||
|Acheteur > En qualité de|||
|Date de début|||
|Durée||en années|
|Volume annuel|||
|Compte d’âge||null si non spécifié<br>i|
|Millésime||null si non spécifié|
|Appellation||null si non spécifié|
|TAV||null si non spécifié|
|Conditions de retiraison||en une fois / en plusieurs fois|
|Conditions de retiraison > Volume|||
|Conditions de retiraison > Au plus tard||Date|
|Prix première campagne|||
|Modalités de révision|||
|Modalités de paiement > Acompte||en %. null si pas d’acompte|
|Modalités de paiement > Paiement||en une fois / en plusieurs fois|
|Modalités de paiement > Montant||en €/hlAP|
|Modalités de paiement > Au plus tard||Date|

<!-- p.18 -->
### 3.3 Proposition d’engagement Armagnac
#### 3.3.1 Vue Proposition d’engagement Armagnac > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant proposition d’engagement Armagnac|||
|Visa BNIA|||
|Date<br>i|||
|Identifiant acheteur|`professionnel`||
|Identifiant vendeur|`professionnel`||
|Courtier|||
|Courtier à|||
|Compte d’âge|||
|Volume||hlAP|
|Prix|||
|Unité de prix||€/hlAP ou €/hl volume|
|Commune||null si non spécifiée<br>i|
|Cépage||null si non spécifié|
|Appellation||null si non spécifié|
|Conditions et calendrier de retiraison|||
|Conditions de paiement > Acompte||booléen|
|Conditions de paiement > Pourcentage acompte|||
|Conditions de paiement > Comptant||booléen|
|Conditions de paiement > 30j à compter de la fin du mois<br>de livraison/retiraison||booléen|
|Échéancier|||

### 3.4 Contrat Vin
#### 3.4.1 Vue Contrat Vin > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat Vin|||
|Numéro de contrat||Numérotation BNIA|
|Code ODG|||
|Date<br>i|||
|Identifiant acheteur<br>i|`professionnel`||
|Identifiant vendeur|`professionnel`||
|Commentaire|||
|Liaison de rattachement||À préciser|

<!-- p.19 -->
#### 3.4.2 Vue Contrat Vin > Lignes produits
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat Vin|`contrat Vin`||
|Appellation|||
|Commune|||
|Cépage|||
|Numéro de cuve|||
|Degré|||
|Volume|||
|Volume alcool pur|||
|Prix départ HT||en €/°hl|

### 3.5 Pluri-annuel Vin
#### 3.5.1 Vue Pluri-annuel Vin > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant pluri-annuel Vin|||
|Visa BNIA|||
|Objet du contrat||Raisons / Moûts / Vin de<br>distillation|
|Date de départ|||
|Durée<br>i|||
|Identifiant vendeur|`professionnel`||
|Vendeur > Représenté par|||
|Vendeur > En qualité de<br>i|||
|Identifiant acheteur|`professionnel`||
|Acheteur > Représenté par|||
|Acheteur > En qualité de|||
|Obligations du vendeur > volume annuel||en hl<br>i|
|Caractéristiques > Commune||null si non spécifiée<br>i|
|Caractéristiques > Cépage||null si non spécifié<br>i|
|Caractéristiques > Appellation||null si non spécifié<br>i|
|Caractéristiques > TAV||null si non spécifié|
|Caractéristiques > Microdistillation||null si non spécifié|
|Caractéristiques > Acidité volatile||null si non spécifié|
|Caractéristiques > Sur lies||null si non spécifié<br>i|
|Caractéristiques > Autres||null si non spécifié|
|Conditions de retiraison||en une fois / en plusieurs fois|
|Conditions de retiraison > Volume|||
|Conditions de retiraison > Au plus tard||Date|
|Prix 1ere campagne||€/°hl|

<!-- p.20 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Modalités de révision du prix|||
|Modalités de paiement > Acompte||en %. null si pas d’acompte|
|Modalités de paiement > Paiement||en une fois / en plusieurs fois|
|Modalités de paiement > Montant|||
|Modalités de paiement > Au plus tard||Date|

### 3.6 Proposition d’engagement Vin
#### 3.6.1 Vue Proposition d’engagement Vin > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant proposition d’engagement Vin|||
|Visa BNIA|||
|Date|||
|Statut validation BNIA<br>i|||
|Identifiant acheteur|`professionnel`||
|Identifiant vendeur|`professionnel`||
|Courtier|||
|Courtier à|||
|Caractéristiques du produit||Raisins / Moûts / Vin de<br>distillation|
|Appellation Armagnac||booléen|
|Appellation Bas Armagnac||booléen|
|Appellation Armagnac-Ténarèze||booléen|
|Appellation Haut Armagnac||booléen|
|Appellation Blanche Armagnac||booléen|
|Conditions et calendrier de retiraison|||
|Conditions de paiement > Acompte||booléen|
|Conditions de paiement > Comptant||booléen|
|Conditions de paiement > 45j à compter de la fin du mois<br>de la date d’émission de la facture||booléen|
|Conditions de paiement > 60j à compter de la date<br>d’émission de la facture||booléen|
|Échéancier|||
|Lieu de distillation||Chez l’acheteur / Chez le<br>bouilleur|
|Nom, Prénom Bouilleur|||
|Adresse Bouilleur||f|
|Méthode de distillation||Continue / Double-Chauffe|

#### 3.6.2 Vue Proposition d’engagement Vin > Lignes produits

<!-- p.21 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant proposition d’engagement Vin|`proposition`<br>`d'engagement Vin`||
|Commune|||
|Cépage|||
|Volume||en litres|
|Prix départ HT||en €/°hl|

### 3.7 Déclarations de fin de travaux
Ces déclarations correspondent aux contrats de type `Distillation à façon` d’ `InteGraal` (Section 2.4). Les deux sources décrivent la même opération et doivent alimenter un objet unique dans la plateforme cible, la déclaration de fin de travaux de distillation. 

#### 3.7.1 Vue Déclaration de fin de travaux > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant déclaration de fin de travaux|||
|Numéro de déclaration||Numérotation BNIA|
|Code ODG|||
|Date|||
|Campagne<br>i|||
|Identifiant propriétaire<br>i|`professionnel`||
|Identifiant distillateur|`professionnel`||
|Lieu de distillation||A domicile / Chez un brouilleur<br>f|
|Méthode de distillation||Continue / Double-Chauffe|
|Cépage||Monocépage / Assemblage|
|Destination||Blanche / Autres|
|Commentaire|||

Toute autre donnée non visible sur l’interface Extranet actuellement mais présentant un intérêt métier devra être ajoutée à cette vue si présente dans les tables de la base de données, notamment le rendement de distillation. 

#### 3.7.2 Vue Déclaration de fin de travaux > Lignes produits
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant déclaration de fin de travaux|`déclaration de fin`||
||`de travaux`||
|Appellation||Armagnac / Bas Armagnac /<br>Armagnac-Ténarèze / Haut<br>Armagnac / Blanche Armagnac|

<!-- p.22 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Commune|||
|Cépage|||
|N° de cuve|||
|Degré|||
|Volume||en litres|
|Volume alcool pur||en litres|

### 3.8 Utilisateurs
#### 3.8.1 Vue Utilisateurs extranet > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant utilisateur<br>i|||
|Identifiant professionnel|`professionnel`||
|Email|||
|Prénom|||
|Nom|||

#### 3.8.2 Vue Utilisateurs extranet > Profils
|**Champ**|**Liaison**|**Observation**||
|---|---|---|---|
|Identifiant utilisateur|`utilisateur`|||
|Profil||Admin Professionel|/ Statistiques / Contrats|
|Actif||booléen||

<!-- p.23 -->
## 4 Fichiers
Un certain nombre de fichiers sont déposés ou générés par le système actuel et doivent être repris : 

- factures PDF 

- annexe déclaration de stocks et mouvements. 

- PDF de certificat d’âge? 

Ceux-ci devront être identifiables et rattachable à l’entité liée en fonction de leur nommage ou de la présence du nom du fichier dans la vue correspondante.