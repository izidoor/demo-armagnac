<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 71-72 -->

<!-- p.71 -->
# 5.7 Ventes export
Le portail doit permettre à l’opérateur de transmettre au BNIA le détail de ses ventes à l’export. Ces données alimentent le suivi sortie export et les statistiques export. 

## 5.7.1 Saisie et import
Le portail doit permettre : 

- la saisie directe des lignes de vente export; 

- le téléchargement du modèle `Excel` BNIA; 

- l’import du fichier `Excel` complété; 

- la consultation des lignes déjà transmises et de leur état. 

Le modèle `Excel` et les colonnes attendues sont décrits dans le module Suivi des sorties export du back-office BNIA. 

## 5.7.2 Contrôles et transmission
Le portail doit appliquer les mêmes contrôles que le back-office BNIA, en saisie manuelle comme après import d’un fichier. 

Les anomalies doivent être restituées avec le motif et la ligne concernée. L’opérateur doit pouvoir corriger sa saisie ou réimporter un fichier corrigé. 

La transmission n’est possible qu’une fois les anomalies levées. Les lignes transmises sont ensuite mises à disposition du BNIA, qui conserve la validation définitive du suivi sortie export.

<!-- p.72 -->
# 5.8 Certificats export
Le portail doit permettre à un opérateur de demander un certificat export et de suivre les certificats mis à sa disposition par le BNIA. Un certificat correspond à un pays de destination. 

## 5.8.1 Demande de certificat
Le portail doit permettre de déposer une demande de certificat pour une expédition export. 

La demande doit permettre de transmettre au BNIA les éléments nécessaires à l’instruction, notamment : 

- le pays de destination; 

- le destinataire; 

- les éléments utiles sur les produits exportés; 

- les documents justificatifs attendus, notamment la facture, la `DRM` ou le `DAE` . 

## 5.8.2 Historique des demandes et certificats
L’opérateur doit pouvoir retrouver : 

- les demandes en cours d’instruction; 

- les certificats délivrés; 

- les dates de demande et de mise à disposition; 

- le statut de la demande. 

Lorsqu’un certificat a été délivré, le portail doit permettre son téléchargement au format `PDF` . Le certificat mis à disposition porte le `QR code` de contrôle `CEV` et les documents justificatifs qui l’accompagnent.