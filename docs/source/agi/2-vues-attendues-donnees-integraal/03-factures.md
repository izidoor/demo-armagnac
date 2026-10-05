<!-- Source : specification-reprise-donnees-agi-v1_1-2026-07-30.pdf · Vues attendues données InteGraal · pp. 9-10 -->

<!-- p.9 -->
## 2.2.3 Vue Déclaration stock et mouvement > Lignes
Les colonnes devront être ajustées s’il existe des distinctions dans la base actuelles sur les mouvements entrées/sorties entre la version de déclaration producteur et la version négociant. 

|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant déclaration|`déclaration stock`<br>`et mouvement`||
|Année / dénomination|||
|Clé||MB / MV / NB / NV|
|Compte d’âge|||
|Stock début de mois||(1) Négociant / Producteur|
|Achat > Achats propriété||(2) Négociant|
|Achat > Achats négoce||(3) Négociant|
|Entrées > Distillation propre||(4) Négociant(2) Producteur|
|Entrées > Changement de bois à sous-verre||(5) Négociant|
|Entrées > Coupes||(6) Négociant(3) Producteur|
|Entrées > Transfert chai région délimitée||(7) Négociant|
|Entrées > Transfert chai hors région délimitée||(8) Négociant|
|Entrées > Réintégration||(9) Négociant|
|Sorties > Pertes sur inventaire||(10) Négociant(5) Producteur|
|Sorties > Changement de bois à sous-verre||(11) Négociant|
|Sorties > Coupes||(12) Négociant(4) Producteur|
|Sorties > Transfert chai région délimitée||(13) Négociant|
|Sorties > Transfert chai hors région délimitée||(14) Négociant|
|Sorties > Fabrication Floc de Gascogne||(15) Négociant(6) Producteur|
|Sorties > Fabrication autres||(16) Négociant(7) Producteur|
|Ventes France > Bouteille -3 litres||(17) Négociant(8) Producteur|
|Ventes France > Armagnac vrac +3 litres||(18) Négociant(9) Producteur|
|Ventes France > Brandies||(19) Négociant|
|Ventes France > Négoce région délimitée||(20) Négociant(10) Producteur|
|Ventes France > Négoce hors région délimitée||(21) Négociant(11) Producteur|
|Ventes Export > Bouteille -3 litres||(22) Négociant(12) Producteur|
|Ventes Export > Armagnac vrac +3 litres||(23) Négociant(13) Producteur|
|Ventes Export > Brandies / fabrications||(24) Négociant|
|Ventes Export > Vins vinés<br>i||(25) Négociant|
|Stock fin de mois||(26) Négociant(14) Producteur|

# 2.3 Factures
## 2.3.1 Vue Facture > Informations générales

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