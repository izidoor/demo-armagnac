<!-- Source : specification-reprise-donnees-agi-v1_1-2026-07-30.pdf · Vues attendues données InteGraal · pp. 4-8 -->

<!-- p.4 -->
# 2 Vues attendues données InteGraal
## 2.1 Référentiel des professionnels
Le référentiel professionnel rassemble la fiche principale et les sous-ensembles rattachés à un professionnel. 

### 2.1.1 Vue Professionnel > Informations générales
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|||
|Code professionnel BNIA|||
|Type de tiers|`paramétrage -`<br>`types tiers`||
|Groupe|||
|N° CVI|||
|Titre|`paramétrage -`<br>`titres`||
|Nom / raison|||
|Raison commerciale|||
|Contact principal|||
|Adresse ligne 1|||
|Adresse ligne 2|||
|Adresse ligne 3|||
|Code postal|||
|Ville|||
|Latitude|||
|Longitude<br>f|||
|Accord pour diffusion des informations||booléen|
|Imprimé personnel|||
|Pays|`paramétrage - pays`||
|Téléphone 1|||
|Téléphone 2|||
|Téléphone portable|||
|Fax|||
|Mél|||
|Mél 2|||
|Mél 3|||
|Site web|||
|Membre BNIA|`paramétrage -`<br>`membres BNIA`||
|Site commercial|||
|Dernière déclaration créée|||
|Dernière déclaration saisie|||

<!-- p.5 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Commentaire|||
|SIRET|||
|N° accise|||
|Bureau douanes|||
|Code accès Internet|||
|Production > Région|`paramétrage -`<br>`régions`||
|Production > Déclaration stock||oui / non / sommeil / cloture|
|Production > Chai agréé||booléen|
|Production > Déclaration mode Excel||booléen|
|Production > Détenteur alambic||booléen|
|Production > Appellation Armagnac||booléen|
|Production > Appellation Bas Armagnac||booléen|
|Production > Appellation Armagnac-Ténarèze||booléen|
|Production > Appellation Haut Armagnac||booléen|
|Production > Appellation Blanche Armagnac<br>f||booléen|
|Diffusion millésimes||refus / partiel / total|
|Oenotourisme > Produits commercialisés|`référentiel -`<br>`produits`<br>`commercialisés`|Accueil Armagnac et autres<br>produits, …|
|Oenotourisme > Adhésion Oenotourisme BNIA||booléen|
|Oenotourisme > Présentation du domaine (français)|||
|Oenotourisme > Présentation du domaine (anglais)|||
|Statistiques|refus / partiel / total||

### 2.1.2 Vue Professionnel > Agréments alambic
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|`professionnel`||
|N° agrément|||
|Type d’alambic|||
|Capacité|||

### 2.1.3 Vue Professionnel > Accords diffusion millésime
Pour les professionnels avec accord partiel de diffusion. 

|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|`professionnel`||
|Millésime<br>f|||
|Diffusion||booléen|

<!-- p.6 -->
### 2.1.4 Vue Professionnel > Dérogations Armagnac -40 % vol
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|`professionnel`||
|Date d’accord|||
|Millésime|||
|Litrage|||
|Commentaire|||

### 2.1.5 Vue Professionnel > Marques
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|`professionnel`||
|Portée||France / Export|
|Marque|`paramétrage -`||
||`marques`||
|Web||booléen|

### 2.1.6 Vue Professionnel > Circuits de distribution
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|`professionnel`||
|Portée||France / Export|
|Circuit de distribution|`référentiels -`||
||`circuits de`||
||`distribution`||

### 2.1.7 Vue Professionnel > Visites
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|`professionnel`||
|Identifiant type visite|`paramétrage -`||
||`visites`||
|Commentaire|||

### 2.1.8 Vue Professionnel > Labels touristiques
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|`professionnel`||

<!-- p.7 -->
|**Champ**|**Liaison**<br>**Observation**|
|---|---|
|Identifiant label|`paramétrage -`|
||`labels`|

Commentaire 

### 2.1.9 Vue Professionnel > Langues parlées
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel<br>i|`professionnel`||
|Identifiant langue|`paramétrage -`<br>`langues`||

### 2.1.10 Vue Professionnel > Médailles et concours
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel|`professionnel`||
|Identifiant médaille / concours|`référentiel -`||
||`médailles et`||
||`concours`||

### 2.1.11 Vue Professionnel > Produits commercialisés
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant professionnel<br>i|`professionnel`||
|identifiant type produit commercialisé|`référentiel -`|Armagnac, Blanche, Floc, etc.|
||`types produits`<br>`commercialisés`||
|Libellé produit|||

### 2.1.12 Vue Professionnel > Contacts
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Identifiant contact<br>i|||
|Identifiant professionnel|`professionnel`||
|Code contact|||
|Nom contact|||
|Téléphone professionnel|||
|Téléphone personnel|||
|Mél|||
|Adresse ligne 1|||

<!-- p.8 -->
|**Champ**|**Liaison**|**Observation**|
|---|---|---|
|Adresse ligne 2|||
|Adresse ligne 3|||
|Code postal|||
|Ville|||
|Pays|`paramétrage - pays`||
|Observation|||

## 2.2 Déclarations de stocks et mouvements
Les vues `DSM` distinguent une entête et cinq vues de détail correspondant aux onglets visibles. 

### 2.2.1 Vue Déclaration stock et mouvement > Informations générales
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

### 2.2.2 Vue Déclaration stock et mouvement > Contrats acheteur / vendeur
Si une liaison existe entre la déclaration et les contrats, les vues correspondantes devront être mise en oeuvre avec : 

- identifiant de déclaration; 

- identifiant du contrat; 

- distinction acheteur / vendeur si nécessaire.