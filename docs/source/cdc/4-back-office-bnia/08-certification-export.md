<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 41-44 -->

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

<!-- p.42 -->
## 4.8.3 Génération d’un certificat
Un certificat export doit être créé sur la base d’un suivi sortie export. 

Le dispositif doit permettre : 

- de sélectionner le suivi sortie export de référence; 

- de générer automatiquement le numéro du certificat; 

- d’associer le certificat à une date d’édition. 

Un certificat correspond à un suivi sortie export et donc à un pays de destination rattaché au destinataire. 

## 4.8.4 Contenu du certificat
Le certificat doit reprendre les informations utiles au contrôle et à l’usage opérateur, notamment : 

- les informations BNIA; 

- le numéro et la date du certificat; 

- le texte de certification BNIA, en français et en anglais, espagnol ou italien en fonction du modèle associé dans le paramétrage du pays; 

- les coordonnées de l’expéditeur; 

- les coordonnées du destinataire; 

- le pays d’origine et le pays de destination; 

- le détail des produits à l’export avec volumes (hl.l,cl), taux d’alcool (TAV à 20°C) et volume d’alcool pur (hl.l,cl); 

- le bloc de signature du directeur du BNIA; 

- le `QR code` de contrôle `CEV` . 

## 4.8.5 Présentation du détail produit
Lors de la création d’un certificat, deux niveaux de détails sont proposés, et sont utilisés selon le paramétrage du pays de destination : 

- certificat simplifié : reprend une seule ligne par degré d’alcool avec le cumul des volumes associés; 

- certificat détaillé : une ligne par comptes d’âge/millésimes et degré d’alcool associé avc les volumes cumulés. 

## 4.8.6 Consultation et mise à disposition
Le certificat doit être généré au format `PDF` . 

Le module doit permettre : 

- de consulter le certificat généré dans le back-office BNIA; 

- de le mettre à disposition sur le portail opérateur.

<!-- p.43 -->
## 4.8.7 Certificat** **`CEV` et raccordement** **`Otentik`
Le certificat export doit être délivré comme certificat `CEV` rattaché à la plateforme douanière `Otentik` . Le `QR code` porté par le certificat permet aux autorités douanières et au destinataire de vérifier l’authenticité du document présenté, dans le pays d’origine comme dans le pays de destination. 

Le principe a été validé avec les douanes nationales, qui ont donné leur accord au développement de ce raccordement. 

La plateforme doit : 

- transmettre à `Otentik` les données du certificat au moment de son émission; 

- récupérer le `QR code` de contrôle et l’apposer sur le certificat mis à disposition de l’opérateur; 

- joindre au certificat les documents justificatifs attendus, notamment la facture et la `DRM` ; 

- conserver pour chaque certificat la trace des échanges avec la plateforme douanière. 

Le certificat `CEV` s’ajoute à l’édition `PDF` : il en complète le contrôle et ne la remplace pas. 

Le chiffrage du raccordement à `Otentik` doit être fourni et distinguer : 

- les coûts d’intégration et de paramétrage de la passerelle; 

- les coûts récurrents liés au service; 

- les coûts variables liés au volume de certificats émis. 

> **⚠ ATTENTION**
La mise en place de la passerelle suppose une mise en relation avec les services douaniers et l’exploitant de la plateforme `Otentik` dès le démarrage du projet. Les contraintes techniques constatées à cette occasion peuvent conduire à revoir l’articulation entre l’édition `PDF` et le certificat `CEV` .

<!-- p.44 -->
# 4.9 Facturation et cotisations
Le module de facturation doit permettre au BNIA d’émettre les factures de cotisations sur `DSM` , les factures de cotisations sur fin de travaux de distillation, les factures diverses, les factures issues des commandes boutique et les avoirs, puis d’en assurer la diffusion documentaire et le suivi de transmission vers `Zeendoc` . 

## 4.9.1 Historique des factures
L’historique des factures est le point d’entrée du module. Il doit permettre de retrouver rapidement une facture existante, d’ouvrir son détail et de consulter son `PDF` . 

Les filtres attendus portent notamment sur : 

- le numéro de facture; 

- le type de facture; 

- l’opérateur facturé; 

- la période facturée; 

- la date de facture. 

Le listing doit afficher au minimum le numéro de facture, sa date, le tiers facturé, le type de facture, les montants `HT` , `TVA` et `TTC` . 

## 4.9.2 Référentiel produits
La codification des produits permet d’identifier les produits utilisés à alimenter les factures manuelles. 

Ils sont caractérisés par : 

- un code; 

- une dénomination; 

- un taux de TVA. 

## 4.9.3 Facturation des cotisations sur** **`DSM`
La facturation des cotisations doit pouvoir être déclenchée à partir d’une `DSM` . 

Les volumes qui servent de base de calcul pour la cotisation sont : 

### ■ pour les DSM négociant : 

   - les sorties transfert chai Armagnac hors région délimitée (colonne 14 de la DSM); 

   - les ventes France hors chai Armagnac (colonnes 17, 18, 19 de la DSM); 

   - les ventes France négoce hors région délimitée (colonne 21 de la DSM); 

   - les ventes export (colonnes 22, 23, 24, 25 de la DSM). 

- pour les DSM producteur :