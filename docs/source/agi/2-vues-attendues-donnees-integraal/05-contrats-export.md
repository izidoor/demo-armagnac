<!-- Source : specification-reprise-donnees-agi-v1_1-2026-07-30.pdf · Vues attendues données InteGraal · pp. 11-12 -->

<!-- p.11 -->
données doivent être repris et unifiés dans un objet unique de la plateforme cible, la déclaration de fin de travaux de distillation. La vue extraite doit donc permettre de rapprocher chaque contrat `Distillation à façon` de la déclaration correspondante. 

## 2.4.1 Vue Contrat France > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat France|||
|Numéro VISA|||
|Date contrat|||
|Date intégration|||
|Type de contrat||Achat Armagnac / Achat vin de<br>distillation / Distillation à façon|
|Identifiant acheteur|`professionnel`||
|Identifiant vendeur|`professionnel`||
|Lieu de distillation||A domicile / Chez un bouilleur|
|Méthode de distillation||Double chauffe / Continue|
|Cépage||Monocépage / Assemblage|
|Destination||Eau de vie blanche / Autres|
|Commentaire|||

## 2.4.2 Vue Contrat France > Lignes produits
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contrat France|`contrat France`||
|Appellation|||
|Compte d’âge|||
|Millésime|||
|Dénomination|||
|Volume|||
|Degré|||
|Volume alcool pur|||
|Prix par hl A.P.|||
|Prix par litre vol|||
|Mtt Total|||

# 2.5 Contrats Export
## 2.5.1 Vue Contrat export > Informations générales

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

## 2.5.2 Vue Contrat export > Lignes produits
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