<!-- Source : specification-reprise-donnees-agi-v1_1-2026-07-30.pdf · Objet du document · pp. 3-4 -->

<!-- p.3 -->
# 1 Objet du document
**Objet du document.** Le présent document recense les vues et les champs à mettre à disposition par AGI (Adour Gestion Informatique) pour préparer la reprise des données depuis l’existant. AGI est l’éditeur de la solution `InteGraal` et en assure actuellement la maintenance, ainsi que celle de l’extranet opérateur. 

**Périmètre de cette version.** Le document couvre, vue par vue, les informations à reprendre et les liaisons à conserver entre les objets issus d’ `InteGraal` . Les données concernées sont aujourd’hui stockées dans une base `Oracle` . 

**Nature du livrable.** Le document décrit des vues logiques attendues. Il ne définit pas un schéma `SQL` exécutable ni un nommage imposé des colonnes techniques.

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