<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 36-38 -->

<!-- p.36 -->
# 4.6 Déclarations de fin de travaux de distillation
La déclaration de fin de travaux de distillation documente une opération de distillation à façon. Elle est établie et validée dans l’outil `ODG` , puis synchronisée vers la plateforme BNIA. 

Elle constitue l’objet unique de ce périmètre : il n’existe pas de contrat distinct. La déclaration porte à la fois les données de distillation, les contrôles du BNIA et le déclenchement de la cotisation associée. 

## 4.6.1 Consultation d’une déclaration
La déclaration restitue : 

- son numéro et sa date; 

- les références `ODG` et la date d’intégration dans la plateforme BNIA; 

- l’identification du propriétaire et du distillateur; 

- les paramètres de distillation, notamment lieu, méthode, cépage et destination; 

- le détail produit, avec lignes multiples, volumes, volume en alcool pur et totaux; 

- le commentaire éventuel; 

- le montant des cotisations calculées; 

- l’état de facturation et la facture émise lorsqu’elle existe; 

- l’édition `PDF` associée lorsque celle-ci existe dans l’objet de référence. 

Les données de la déclaration proviennent de l’ `ODG` . Elles ne sont modifiables ni dans le back-office BNIA, ni dans le portail opérateur. 

## 4.6.2 Facturation
La cotisation est calculée et facturée à partir de la déclaration. 

Le BNIA doit disposer d’un écran listant les déclarations `à facturer` . 

Le déclenchement de la facturation doit créer automatiquement une facture préremplie à partir des données de la déclaration, en particulier des volumes de vin distillés. Le système doit conserver le lien entre la déclaration et la facture émise. 

Les règles de calcul et de `TVA` sont décrites dans le module Facturation et cotisations. 

## 4.6.3 Contrôles utiles
Le module doit intégrer : 

- un contrôle de rattachement fiable des parties aux codes opérateur BNIA; 

- un contrôle empêchant la création de doublons lors de synchronisations multiples d’une même déclaration; 

- la conservation des informations nécessaires au contrôle métier du BNIA avant facturation.

<!-- p.37 -->
> **⚠ ATTENTION**
La qualité de ce module dépend directement de la stabilité du flux `ODG` et de l’absence de resynchronisations en doublon sur une même déclaration. Le dispositif cible doit aussi conserver assez d’informations de distillation pour permettre au BNIA d’exercer ses contrôles métier avant facturation.

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