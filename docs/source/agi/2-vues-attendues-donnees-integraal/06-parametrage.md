<!-- Source : specification-reprise-donnees-agi-v1_1-2026-07-30.pdf · Vues attendues données InteGraal · pp. 12-16 -->

<!-- p.12 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat export|||
|Numéro VISA|||
|Date|||
|Dernière déclaration saisie|||
|Taux de remise|||
|Détails<br>i||booléen|
|Identifiant vendeur<br>i|`professionnel`||
|Identifiant destinataire|`paramétrage -`<br>`destinataires`||
|Fichier DAE||Nom du fichier - Fichier à<br>rendre accessible|
|Code devise|`référentiel -`<br>`devises`||
|Taux devise|||
|Commentaire<br>i|||
|Certificat C.O Fr/A<br>i||booléen|
|Certificat C.A Fr/A<br>i||booléen|
|Certificat C.O Fr/Es<br>i||booléen|
|Certificat C.C Fr/It<br>i||booléen|
|Certificat Type 5||booléen|

### 2.5.2 Vue Contrat export > Lignes produits
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat export|`contrat export`||
|Numéro de ligne|||
|Marque|||
|Dénomination|||
|Millésime|||
|Âge|||
|Caisses|||
|Bouteilles|||
|Flacons|`référentiels -`<br>`flacons`||
|Contenance|`paramétrage -`<br>`contenances`||
|% vol.|||
|Volume Alcool pur|||
|N° dégustation|||
|Prix unitaire|||
|Montant devise|||
|Montant euro|||

<!-- p.13 -->
## 2.6 Paramétrage
### 2.6.1 Vue Paramétrage > Pays
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant pays||Si stocké|
|Code pays|||
|Code ISO|||
|Libellé pays|||
|CA|||
|CA1|||
|CA2|||
|CA3|||
|CA4|||
|CA5|||
|Âge|||
|Continent|||

### 2.6.2 Vue Paramétrage > Régions
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant région||Si stocké|
|Code région|||
|Libellé région|||

### 2.6.3 Vue Paramétrage > Titres
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant titre||Si stocké|
|Code titre|||
|Libellé titre|||

### 2.6.4 Vue Paramétrage > Types tiers
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant type tiers||Si stocké|
|Code type tiers|||
|Libellé type tiers|||

<!-- p.14 -->
### 2.6.5 Vue Paramétrage > Labels
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant label||Si stocké|
|Code label|||
|Libellé label|||

### 2.6.6 Vue Paramétrage > Visites
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant visite||Si stocké|
|Code visite|||
|Libellé visite|||

### 2.6.7 Vue Paramétrage > Cotisations
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant cotisation||Si stocké|
|Type de matrice||Négociant mensuel ou<br>producteur annuel|
|N° colonne ancien|||
|N° colonne écran|||
|Soumis|||
|Taux HI soumis à TVA|||

Taux HI non soumis à TVA 

### 2.6.8 Vue Paramétrage > Contenances
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contenance||Si stocké|
|Valeur de contenance|||

### 2.6.9 Vue Paramétrage > Langues
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant langue||Si stocké|
|Code langue|||
|Libellé langue|||

<!-- p.15 -->
### 2.6.10 Vue Contrats > Destinataires
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant destinataire|||
|Nom destinataire|||
|Ligne adresse 1|||
|Ligne adresse 2|||
|Code postal|||
|Ville|||
|Pays destinataire|`paramétrage - pays`||

### 2.6.11 Vue Contrats > Vendeurs
Champs vendeurs à confirmer et valider l’utilité de cette table, à savoir est-ce que c’est utilisé pour les contrats export ou pour les contrats France. 

|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant vendeur|||
|Code vendeur|||
|Nom vendeur|||
|Adresse ligne 1|||
|Adresse ligne 2|||
|Code postal vendeur|||
|Ville vendeur|||

<!-- p.16 -->
# 3 Vues attendues données** **`pro.armagnac.fr`
## 3.1 Contrat Armagnac
### 3.1.1 Vue Contrat Armagnac > Informations générales
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

### 3.1.2 Vue Contrat Armagnac > Lignes produits
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat Armagnac|`contrat Armagnac`||