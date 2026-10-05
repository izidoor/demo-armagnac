<!-- Source : specification-reprise-donnees-agi-v1_1-2026-07-30.pdf · Vues attendues données InteGraal · pp. 10-11 -->

<!-- p.10 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant facture<br>i|||
|Identifiant professionnel|`professionnel`||
|Numéro de facture|||
|Mois de facturation||AAAAMM|
|Type de facture|`référentiels -`<br>`type de facture`||
|Total HT|||
|Port|||
|Taux TVA|||
|Montant TVA|||
|Total TTC|||
|Date d’édition|||
|Intitulé pied de facture|`référentiels -`<br>`intitulé pied de`<br>`facture`||
|Observation d’entête|||
|Observation de pied|||
|Détail Produit<br>i|||
|Identifiant déclaration<br>i|`déclaration stock`<br>`et mouvement`|Dans le cas d’une facturation<br>de CVO sur une DSM|
|Identifiant contrat France|`contrat France`|Dans le cas d’une facturation<br>sur la distillation|

## 2.3.2 Vue Facture > Lignes
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant facture|`facture`||
|Identifiant produit|`paramétrage -`<br>`produit`||
|Libellé|||
|Volume HI|||
|Tarif|||
|Total|||

## 2.3.3 Vue Facture > PDF
Si stockées dans le système, les factures PDF devront être mises à disposition et identifiables pour pouvoir être rattachées à la facture correspondante. 

# 2.4 Contrats France
Les contrats de type `Distillation à façon` d’ `InteGraal` décrivent la même opération que les déclarations de fin de travaux de l’extranet `pro.armagnac.fr` (Section 3.7). Ces deux jeux de

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