<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 38-41 -->

<!-- p.38 -->
# 4.7 Suivi des sorties export
Le module de suivi des sorties export doit permettre au BNIA de saisir ou d’importer et de suivre les ventes à l’export, d’alimenter les statistiques export et de fournir la base de génération des certificats d’âge valant attestation d’origine. 

## 4.7.1 Historique des suivis sortie export
L’historique des suivis sortie export est le point d’entrée du module. Il doit permettre de retrouver rapidement un suivi sortie export existant, d’ouvrir sa consultation détaillée et d’accéder à la création d’un nouveau suivi. 

Les filtres attendus portent notamment sur : 

- le numéro du suivi sortie export; 

- le vendeur; 

- le destinataire; 

- le pays de destination (pays identifié dans la fiche du destinataire); 

- la date du suivi sortie export. 

Le listing doit au minimum afficher le numéro du suivi sortie export, sa date, le vendeur, le destinataire, le pays de destination et les repères utiles au suivi du dossier. 

## 4.7.2 Saisie d’un suivi sortie export
La création d’un suivi sortie export doit rester simple à utiliser tout en couvrant le niveau de détail nécessaire aux contrôles et aux restitutions export. 

Le suivi sortie export doit être numéroté automatiquement selon la séquence d’incrément retenue par le BNIA. 

La saisie doit permettre de renseigner : 

- le vendeur, avec rattachement à la fiche opérateur BNIA; 

- le pays de destination, avec rattachement au référentiel des pays; 

- les lignes de vente export; 

- le justificatif rattaché au contrat, sous la forme d’un `PDF` de `DAE` , `DAI` ou de facture. 

Chaque ligne de vente doit permettre de qualifier en particulier : 

- la date de la vente; 

- la marque, lorsqu’elle existe; 

- la dénomination, lorsqu’elle existe; 

- le millésime, lorsqu’il existe; 

- le compte d’âge; 

- le type de conditionnement, notamment `Bouteille` ou `Vrac` ; 

- le nombre de bouteilles ou la quantité en vrac selon le cas; 

- la contenance;

<!-- p.39 -->
- le degré d’alcool; 

- le volume d’alcool pur calculé; 

- la valeur. 

Le dispositif doit permettre de gérer, pour l’export, des comptes d’âge au-delà de `20+` . 

Le détail des champs est décrit dans l’annexe Référentiel des données et objets de gestion. 

## 4.7.3 Alimentation du suivi sortie export
Le suivi sortie export doit pouvoir être alimenté de deux façons : 

- par saisie directe des lignes de vente export; 

- par import d’un fichier `Excel` au format normalisé BNIA. 

Les deux modes doivent être disponibles dans le back-office BNIA et dans le portail opérateur. 

### 4.7.3.1 Format d’import Excel
Le BNIA doit mettre à disposition un modèle `Excel` unique, construit sur le même principe de présentation que la matrice `DSM` négociant. 

Chaque ligne du fichier correspond à une vente export et doit porter : 

- la date de la vente; 

- le pays de destination; 

- le millésime ou la dénomination; 

- la clé : distinction bouteille ou vrac; 

- le compte d’âge; 

- le volume d’alcool pur, exprimé en `Hl.l,cl` ; 

- la valeur. 

Un exemple de ce modèle est fourni en annexe Exemples de fichiers. 

Comme pour les DSM, un format de fichier “informatique” (type `XML` ou `JSON` ) devra être proposé pour être soumis aux éditeurs de logiciels opérateurs afin de leur offrir la possibilité de faire une sortie mensuelle de ce format et de faciliter la transmission par les opérateurs leurs données d’export au BNIA. 

### 4.7.3.2 Contrôles et validation de l’import
L’import doit être contrôlé avant enregistrement, ligne à ligne : 

- conformité du fichier au modèle attendu; 

- format et validité des valeurs de date, de volume et de valeur; 

- correspondance du pays de destination avec le référentiel des pays; 

- cohérence du couple millésime ou dénomination, clé et compte d’âge;

<!-- p.40 -->
- détection des lignes déjà enregistrées sur la même période. 

Les anomalies doivent être restituées avec le motif et la ligne concernée du fichier, et le fichier doit pouvoir être corrigé puis réimporté. 

Les lignes importées ne sont enregistrées qu’après validation. Lorsque l’import est réalisé par l’opérateur depuis le portail, le BNIA doit pouvoir contrôler et corriger les lignes avant validation définitive du suivi sortie export. 

## 4.7.4 Fiche suivi sortie export
La fiche d’un suivi sortie export doit permettre de consulter l’ensemble des informations du dossier sans repasser par la saisie. 

Elle doit notamment permettre : 

- de visualiser les informations générales du suivi sortie export; 

- de consulter le détail des lignes export et leur mode d’alimentation; 

- d’ouvrir le justificatif source rattaché au suivi sortie export; 

- d’accéder à l’historique des certificats export générés pour le suivi sortie export; 

- d’identifier l’historique de création et de modification du suivi sortie export. 

## 4.7.5 Règles de gestion
Le module doit intégrer les règles suivantes : 

- la numérotation du suivi sortie export doit être automatique et unique; 

- le vendeur doit être rattaché à une fiche opérateur BNIA existante; 

- le destinataire doit être rattaché au référentiel des destinataires; 

- en saisie directe, le volume d’alcool pur doit être calculé automatiquement à partir des quantités, contenances et degrés saisis; 

- les comptes d’âge export ne doivent pas être limités à la borne définie sur les DSM de `20+` ; 

- ■ un justificatif source doit pouvoir être conservé en pièce jointe du suivi sortie export pour la traçabilité; 

- à l’import, les lignes doivent être rattachées au suivi sortie export correspondant au vendeur et au pays de destination portés par la ligne, un nouveau suivi étant créé lorsqu’il n’existe pas; 

- le mode d’alimentation, saisie ou import, doit être conservé sur le suivi sortie export; 

- les créations, modifications et imports du suivi sortie export doivent être historisés.

<!-- p.41 -->
# 4.8 Certification export
Le module de certification export doit permettre au BNIA de générer les certificats d’âge valant attestation d’origine nécessaires à certains opérateurs lors de l’export de leurs produits. 

## 4.8.1 Historique des certificats
L’historique des certificats doit permettre de retrouver rapidement un certificat existant et d’ouvrir son édition `PDF` . 

Les certificats doivent être filtrable par : 

- le numéro du certificat; 

- la date du certificat; 

- l’opérateur exportateur; 

- le pays de destination; 

- le nom du destinataire. 

Le listing doit au minimum afficher le numéro, la date, l’opérateur concerné, le pays de destination et le numéro du suivi sortie export lié. 

## 4.8.2 Demandes opérateur
Le module doit permettre au BNIA de lister les demandes de certificat transmises par les opérateurs et d’en assurer le traitement. 

Le listing des demandes doit permettre d’identifier au minimum : 

- l’opérateur demandeur; 

- la date de la demande; 

- le pays de destination; 

- le suivi sortie export concerné; 

- le destinataire : raison sociale, adresse, pays; 

- l’état de traitement de la demande; 

- le lien vers le certificat s’il est créé; 

Depuis cette liste, le BNIA doit pouvoir : 

- ouvrir la demande; 

- contrôler les informations utiles; 

- rattacher ou créer le suivi sortie export de référence si nécessaire; 

- initier la création du certificat; 

- conserver la trace du traitement réalisé.