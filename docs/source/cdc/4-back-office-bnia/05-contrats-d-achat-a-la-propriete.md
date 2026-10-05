<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Back-office BNIA · pp. 30-36 -->

<!-- p.30 -->
# 4.5 Contrats d’achat à la propriété
Le module `Contrats d'achat à la propriété` regroupe les contrats et écritures contractuelles suivis par le back-office BNIA. Il doit permettre de retrouver rapidement un contrat, de le consulter ou de le traiter selon sa nature, et d’assurer les liens utiles avec les opérateurs, le portail, la `DSM` et la facturation. 

Le module regroupe deux types de contrats d’achat à la propriété : 

- `Contrat achat Armagnac` : contrat saisi par le BNIA ou par un opérateur, avec validation des parties et du BNIA; 

- `Contrat achat de vin de distillation` : contrat saisi dans l’outil ODG puis synchronisé dans le back-office BNIA en consultation. 

Il couvre également les objets contractuels rattachés à ces contrats : 

- les pluri-annuels Armagnac; 

- les propositions d’engagement Armagnac; 

- les pluri-annuels vin; 

- les propositions d’engagement vin. 

## 4.5.1 Contrat achat Armagnac
Le `Contrat achat Armagnac` formalise un achat ou une vente d’Armagnac entre opérateurs. Il peut être saisi par un opérateur depuis le portail. Le contrat doit ensuite être validé par les parties et par le BNIA avant d’être exploité dans les autres modules. 

### 4.5.1.1 Listing des contrats
Le module doit permettre : 

- de lister les contrats Armagnac; 

- de rechercher un contrat par date, numéro, acheteur, vendeur ou statut; 

- de créer un contrat et de le compléter sans ressaisie des données opérateur déjà connues; 

- de consulter, modifier et éditer le contrat dans le cadre des droits ouverts. 

### 4.5.1.2 Fiche contrat
Le contrat doit au minimum gérer : 

- la date du contrat; 

- l’identification de l’acheteur et du vendeur : liaison avec le référentiel d’opérateurs; 

- les lignes produit, avec compte d’âge, millésime, appellation, dénomination, volume, degré, volume en alcool pur et prix; 

- les conditions et le calendrier de retiraison; 

- les conditions de paiement;

<!-- p.31 -->
- le commentaire du contrat; 

- le rattachement à un pluri-annuel ou un contrat d’engagement si nécessaire; 

- les références de suivi visibles côté BNIA, notamment le numéro contrat généré automatiquement. 

### 4.5.1.3 Validations
Le système doit tracer distinctement : 

- la validation de l’acheteur; 

- la validation du vendeur; 

- la validation du BNIA. 

Le BNIA doit pouvoir : 

- valider un contrat même si la validation de l’acheteur et/ou du vendeur n’a pas été faite; 

- pouvoir invalider un contrat. 

### 4.5.1.4 Éditions et historique
Le module doit permettre : 

- l’édition du contrat au format PDF; 

- la consultation de l’historique des créations, modifications et validations. 

## 4.5.2 Contrat achat de vin de distillation
Le `Contrat achat de vin de distillation` correspond à un contrat d’achat ou de vente de vin entre opérateurs, saisi dans l’outil ODG puis synchronisé dans le back-office BNIA. Il est consulté en lecture seule par le BNIA. 

### 4.5.2.1 Consultation des contrats
Le module doit permettre : 

- de lister les contrats vin; 

- de rechercher un contrat par date, numéro de contrat, nom du vendeur ou nom de l’acheteur; 

- ■ de consulter le contrat avec son numéro d’incrément BNIA et son numéro de contrat ODG; 

- d’éditer le contrat au format `PDF` .

<!-- p.32 -->
### 4.5.2.2 Fiche contrat
Le contrat Vin est caractérisé notamment par : 

- le numéro d’incrément BNIA attribué automatiquement à l’intégration; 

- le numéro de contrat ODG; 

- la date du contrat et la campagne; 

- l’identification de l’acheteur et du vendeur; 

- le détail produit, avec appellation, commune, cépage, numéro de cuve, degré, volume, volume en alcool pur et prix de départ hors taxes; 

- le commentaire du contrat. 

### 4.5.2.3 Synchronisation et contrôles
Le module doit intégrer : 

- un contrôle de rattachement des parties à la fiche opérateur BNIA; 

- un contrôle empêchant les doublons d’intégration d’un même contrat; 

- une historisation de la date d’intégration et de la source du flux ODG; 

## 4.5.3 Pluri-annuel Armagnac
Le pluri-annuel Armagnac formalise un engagement d’achat/vente d’Armagnac entre opérateurs sur une durée de trois ans, avec reconduction tacite sur trois ans. 

> **⚠ ATTENTION**
Ce module fait actuellement l’objet d’une discussion au sein du BNIA. Son maintien dans le périmètre du projet sera arbitré prochainement. 

### 4.5.3.1 Contenu du pluri-annuel
Le pluri-annuel est caractérisé notamment par : 

- l’identification du vendeur et de l’acheteur; 

- les informations sur les personnes qui représentent les parties; 

- la date de début du contrat et sa durée; 

- le volume annuel engagé; 

- les caractéristiques produit utiles, notamment compte d’âge, millésime, appellation et `TAV` ; 

- les conditions de retiraison; 

- le prix de la première campagne et les modalités de révision; 

- les modalités de paiement, y compris l’acompte et l’échéancier lorsqu’ils existent.

<!-- p.33 -->
### 4.5.3.2 Gestion du pluri-annuel
Le module doit permettre : 

- de lister et retrouver les pluri-annuels par acheteur, vendeur, numéro, date; 

- saisir un pluri-annuel; 

- de consulter le pluri-annuel; 

- de clôturer un pluri-annuel; 

- de supprimer un pluri-annuel; 

- d’éditer le pluri-annuel au format PDF. 

## 4.5.4 Proposition d’engagement Armagnac
La proposition d’engagement Armagnac prépare un accord entre opérateurs. Elle permet de cadrer le produit, le prix et les principales conditions commerciales. 

> **⚠ ATTENTION**
Ce module fait actuellement l’objet d’une discussion au sein du BNIA. Son maintien ou son adaptation dans le périmètre du projet sera arbitré prochainement. 

### 4.5.4.1 Contenu de la proposition
La proposition est caractérisée notamment par : 

- la date de la proposition; 

- l’identification de l’acheteur et du vendeur; 

- les caractéristiques produit, notamment compte d’âge, volume, prix, unité de prix, commune, cépage et appellation; 

- les conditions et le calendrier de retiraison; 

- les conditions de paiement, avec acompte, paiement comptant, délai à `30` jours et échéancier si nécessaire. 

### 4.5.4.2 Gestion de la proposition
Le module doit permettre : 

> ■ de lister et retrouver les propositions d’engagement par acheteur, vendeur, numéro, date; 

- saisir une proposition d’engagement; 

- de consulter la proposition d’engagement; 

- de supprimer la proposition d’engagement; 

- éditer la proposition au format PDF.

<!-- p.34 -->
## 4.5.5 Pluri-annuel vin
Le pluri-annuel vin formalise un engagement sur plusieurs campagnes autour des raisins, moûts ou vins de distillation. Le back-office BNIA doit en restituer le contenu, les paramètres de durée et les conditions économiques, afin de permettre au BNIA d’en assurer le suivi interne. 

> **⚠ ATTENTION**
Ce module fait actuellement l’objet d’une discussion au sein du BNIA. Son maintien dans le périmètre du projet sera arbitré prochainement. 

### 4.5.5.1 Contenu du pluri-annuel
Le pluri-annuel est caractérisée notamment par : 

- l’identification du vendeur et de l’acheteur; 

- l’objet du contrat; 

- la date de départ et la durée en années; 

- le volume annuel engagé; 

- les caractéristiques produit, notamment commune, cépage et appellation; 

- les indicateurs qualitatifs, notamment `TAV` , microdistillation, acidité volatile, `sur lies` et autres critères; 

- les conditions de retiraison; 

- le prix de la première campagne et les modalités de révision; 

- les modalités de paiement, y compris l’acompte et l’échéancier. 

### 4.5.5.2 Gestion du pluri-annuel
Le module doit permettre : 

- de lister et retrouver les pluri-annuels par acheteur, vendeur, numéro, date; 

- saisir un pluri-annuel; 

- de consulter le pluri-annuel; 

- éditer le pluri-annuel au format PDF. 

## 4.5.6 Proposition d’engagement vin
La proposition d’engagement vin prépare un accord dans le périmètre vin. Le back-office BNIA doit en restituer le contenu, l’avancement par acteur et les paramètres commerciaux ou de distillation utiles au BNIA.

<!-- p.35 -->
### 4.5.6.1 Contenu de la proposition
La proposition est caractérisée notamment par : 

- son numéro, sa date et ses états de validation; 

- l’identification de l’acheteur et du vendeur; 

- le type de produit concerné; 

- le détail produit, avec volume, cépage et prix de départ hors taxes; 

- les conditions et le calendrier de retiraison; 

- les conditions de paiement, avec acompte, paiement comptant, délais et échéancier; 

- les informations de distillation, notamment lieu et méthode. 

### 4.5.6.2 Gestion de la proposition
Le module doit permettre : 

- de lister et retrouver les propositions d’engagement par acheteur, vendeur, numéro, date; 

- saisir une proposition d’engagement; 

- de consulter la proposition d’engagement; 

- de supprimer la proposition d’engagement; 

- d’éditer la proposition au format PDF. 

## 4.5.7 Référentiel des données
Le détail des champs de ce module est décrit dans l’annexe Référentiel des données et objets de gestion.

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