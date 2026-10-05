<!-- Source : specification-reprise-donnees-agi-v1_1-2026-07-30.pdf · Vues attendues données InteGraal · pp. 8-9 -->

<!-- p.8 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Adresse ligne 2|||
|Adresse ligne 3|||
|Code postal|||
|Ville|||
|Pays|`paramétrage - pays`||
|Observation|||

# 2.2 Déclarations de stocks et mouvements
Les vues `DSM` distinguent une entête et cinq vues de détail correspondant aux onglets visibles. 

## 2.2.1 Vue Déclaration stock et mouvement > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant déclaration<br>i|||
|Identifiant professionnel|`professionnel`|i|
|Dest Code||À confirmer ce qu’il représente<br>et son utilité|
|Date de saisie|||
|Date d’impression|||
|Date de facture|||
|Date de transfert compta|||
|Date d’échéance LCR|||
|Relance 1|||
|Relance 2|||
|Relance 3|||
|Nombre de relances|||
|Mois / année de déclaration||MMAAAA<br>i|
|Annexe||Nom du fichier - Fichier à<br>rendre accessible|
|Observation|||
|Total cotisation HT|||

## 2.2.2 Vue Déclaration stock et mouvement > Contrats acheteur / vendeur
Si une liaison existe entre la déclaration et les contrats, les vues correspondantes devront être mise en oeuvre avec : 

- identifiant de déclaration; 

- identifiant du contrat; 

- distinction acheteur / vendeur si nécessaire.

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