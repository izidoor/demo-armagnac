# Glossaire : langage ubiquitaire

> Source de vérité du vocabulaire. Les termes ne sont jamais traduits ni renommés (ADR-001).
> `p.N` renvoie aux pages du CDC v1.1 (`docs/source/cdc/`). ⚠ renvoie à `questions-ouvertes.md`.
> Colonne « Code » : nom du type ou du concept dans le code (français, sans accents).

## 1. Acteurs

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| BNIA | `Bnia` | Bureau National Interprofessionnel de l'Armagnac, maître d'ouvrage. Ses agents sont les *utilisateurs BNIA*. | p.96 |
| Utilisateur BNIA | `UtilisateurBnia` | Compte interne du back-office, porteur d'un `Role`. Blocage 30 min après 3 échecs de connexion. | p.14, 17 |
| Rôle | `Role` | Ensemble de `Droit` (module × action : Lecture, Écriture, Facturer, Générer, Gérer), composé librement par le BNIA. | p.14-17 |
| Opérateur | `Operateur` | Producteur, négociant ou autre structure ayant une fiche dans le référentiel BNIA. Peut interagir avec le portail. | p.95 |
| Type d'opérateur | `TypeOperateur` | `PRODUCTEUR` ou `NEGOCIANT` (plus d'éventuelles autres structures). Pilote la périodicité par défaut, les colonnes DSM saisissables, les DRM, l'assiette de cotisation et la forme du code. Liste fermée du contexte `operateurs`, **pas** un référentiel administrable. Probablement l'ancien « type de tiers » d'InteGraal (Q-REF-1). | p.18, 25, 28, 101 |
| Code opérateur BNIA | `CodeOperateur` | Négociant : `N` + n° de département + incrément sur 4 chiffres. Producteur : incrément sur 6 chiffres. Attribué automatiquement, unique. | p.21 |
| Groupe opérateur | `GroupeOperateur` | Regroupe plusieurs fiches (chais) d'un même opérateur. | p.18 |
| Contact | `Contact` | Personne rattachée à un opérateur ; peut avoir un accès portail. Porte des centres d'intérêt (comptabilité, marketing, production…). | p.20-21 |
| Compte portail | `ComptePortail` | Identité de connexion d'un contact, unique par adresse email, rattachable à plusieurs comptes opérateur avec des droits propres à chacun. | p.60 |
| Compte opérateur actif | `CompteOperateurActif` | Opérateur sélectionné dans la session portail ; toutes les données visibles sont filtrées dessus. | p.60-62 |
| Destinataire | `Destinataire` | Tiers destinataire d'une vente export, rattaché à un pays, qualifié par un `TypeTiers`. Distinct des opérateurs ; vit dans le contexte `exportation`, pas dans `referentiels`. | p.40-41, 129 |
| Type tiers | `TypeTiers` | Catégorie d'acteur d'un destinataire export (importateur, distributeur…). Liste administrable du contexte `exportation`. À ne pas confondre avec `TypeOperateur`. | p.53, 129-130 |

## 2. Produit et mesures

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Campagne de distillation | `Campagne` | Du 1er septembre N au 31 mars N+1. | p.94 |
| Millésime | `Millesime` | Année de la campagne de distillation (campagne 2025/2026 → millésime 2025). | p.95, 98 |
| Compte d'âge | `CompteDAge` | Concept partagé (`commun.domain`). Âge commercial, incrémenté chaque 1er avril. Millésime N : `00` avant le 01/04/N+1, `0` jusqu'au 31/03/N+2, puis `1`, `2`… `00` et `0` sont deux valeurs distinctes (ce n'est donc pas un entier). Borne `20+` en DSM, sans borne en export. ⚠ | p.39, 98-99 |
| Assemblage | `Assemblage` | Mélange de plusieurs Armagnacs ; son compte d'âge est celui de l'eau-de-vie la plus jeune. | p.94 |
| Mention commerciale | `MentionCommerciale` | Déduite du compte d'âge : `00` et `0` aucune ; `1` à `3` VS ou \*\*\* ; `4` à `9` VSOP ; `10`+ Hors d'Âge, Extra, Réserve. | p.99 |
| Clé de conditionnement | `CleConditionnement` | `MB` millésime sous bois, `MV` millésime sous verre, `NB` assemblage sous bois, `NV` assemblage sous verre. | p.96 |
| Dénomination | `Denomination` | Désignation commerciale d'une ligne (« qualité ou dénomination »). Alternative au millésime dans la clé d'unicité d'une ligne DSM. | p.23 |
| Volume d'alcool pur | `VolumeAlcoolPur` | Volume × TAV. Unité DSM : litre d'alcool pur (LAP) ; statistiques en `hlAP`. Stocké en litres (`BigDecimal`), jamais en flottant. | p.23, 96 |
| Format hl.l,cl | `FormatHlLCl` | Présentation d'un volume : 129 550,3 L s'affiche `1 295.50,30`. Affichage et fichier d'import export uniquement. | p.96 |
| TAV | `Tav` | Titre alcoométrique volumique (% vol), exprimé à 20 °C sur les certificats. | p.42, 97 |
| Bouteille / Vrac | `TypeConditionnement` | Distinction portée par les lignes de vente export. | p.38 |

## 3. Déclarations de stocks et mouvements

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| DSM | `DeclarationStocksMouvements` | Déclaration de stocks et mouvements d'un opérateur pour une période. Format unique producteur/négociant : 5 onglets, 26 colonnes numérotées. | p.22-24 |
| Période de déclaration | `PeriodeDeclaration` | Annuelle par défaut (producteur), mensuelle (négociant) ; paramétrable mois / trimestre / semestre / année sur la fiche opérateur. Ni chevauchement, ni trou. Le portail ne remplit qu'une DSM créée au préalable par le BNIA. | p.26, 28, 64 |
| Colonne grisée | `ColonneDsm` | Huit colonnes (2, 3, 5, 9, 11, 19, 24, 25) hors périmètre producteur : non saisissables, non importées, exclues des totaux, des contrôles et de la cotisation. | p.24-25 |
| Ligne DSM | `LigneDsm` | Unique dans une déclaration sur (dénomination ou millésime, clé de conditionnement, compte d'âge). | p.23 |
| Stock début / fin | `StockDebut`, `StockFin` | Le début (col. 1) reprend exactement la fin (col. 26) de la déclaration précédente ; la fin est calculée. | p.23-24 |
| Contrôle de cohérence | `ControleCoherence` | Joué à la saisie, à l'import et avant toute validation. Bloque la validation en indiquant le motif et le niveau (déclaration, onglet, ligne). | p.26-27 |
| États d'une DSM | `EtatDsm` | `EN_ATTENTE_DE_SAISIE` → `A_VALIDER` → `A_FACTURER` → `FACTUREE`. ⚠ Le CDC en donne deux versions. | p.22, 108 |
| Justificatif | `Justificatif` | Pièce prouvant un mouvement (contrat, facture, DAE, déclaration de fabrication douanière), rattachée à une déclaration ou à une ligne. Déposeur et date tracés. | p.25-26, 95 |
| DRM | `Drm` | Déclaration récapitulative mensuelle transmise aux douanes (PDF). Producteurs uniquement : une par mois de la période (12 pour une DSM annuelle). | p.26 |
| DAE / DAI | `Dae`, `Dai` | Document d'accompagnement électronique ; document annuel d'inventaire. | p.96 |
| Signature de la DSM | `SignatureDsm` | Validation par l'opérateur d'un PDF signé via un service tiers, archivé avec la déclaration. Toute correction ultérieure exige une nouvelle signature. | p.66 |

## 4. Contrats

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Contrat achat Armagnac | `ContratAchatArmagnac` | Achat/vente d'Armagnac entre opérateurs, saisi par le BNIA ou par un opérateur. Trois validations tracées séparément : acheteur, vendeur, BNIA. Le BNIA peut valider sans les parties, et invalider. | p.30-31 |
| Contrat exécuté | `ContratExecute` | Contrat validé par les parties et le BNIA **et** dont les volumes couvrent ceux déclarés en DSM. Condition pour valider une DSM. | p.27 |
| Contrat vin | `ContratVin` | Contrat d'achat de vin de distillation, saisi dans l'ODG, synchronisé en lecture seule. Porte un n° d'incrément BNIA et le n° de contrat ODG. | p.31-32 |
| Pluri-annuel | `PluriAnnuel` | Engagement sur plusieurs campagnes (Armagnac : 3 ans, reconduction tacite sur 3 ans). Maintien en arbitrage au BNIA. ⚠ | p.32, 34 |
| Proposition d'engagement | `PropositionEngagement` | Objet préparatoire à un accord, avant transformation en contrat. Maintien en arbitrage. ⚠ | p.33, 95 |
| Achat à la propriété | — | Intitulé du chapitre contrats. Non défini dans le CDC : ne pas en faire un type. | p.30 |

## 5. Distillation à façon

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Distillation à façon | `DistillationAFacon` | Distillation réalisée pour le compte d'un opérateur. Ne donne lieu à aucun contrat. | p.94 |
| Déclaration de fin de travaux de distillation | `DeclarationFinTravaux` | Objet unique qui remplace le « contrat Distillation à façon » d'InteGraal et la déclaration de l'extranet. Établie dans l'ODG, synchronisée, **non modifiable** côté BNIA et portail. Déclenche la cotisation. Pas de doublon en cas de retransmission. | p.8, 36, 81 |
| Propriétaire / distillateur | `Proprietaire`, `Distillateur` | Les deux parties de la déclaration, rattachées chacune à un code opérateur BNIA. | p.36 |

## 6. Export

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Suivi sortie export | `SuiviSortieExport` | Ex-« Contrat Export » d'InteGraal. Numéro automatique ; un par (vendeur, pays de destination) ; alimenté par saisie ou import Excel ; justificatif PDF (DAE, DAI ou facture). | p.38-40 |
| Ligne de vente export | `LigneVenteExport` | Date, marque, dénomination ou millésime, compte d'âge, conditionnement, quantité, contenance, degré, volume d'alcool pur calculé, valeur. | p.38-39 |
| Demande de certificat | `DemandeCertificat` | Demande faite par l'opérateur depuis le portail, traitée par le BNIA. | p.41 |
| Certificat export | `CertificatExport` | Certificat d'âge valant attestation d'origine, généré depuis un suivi sortie export : un certificat = un suivi = un pays. Simplifié (une ligne par degré) ou détaillé (par compte d'âge/millésime et degré) selon le pays. Édité en PDF. | p.42 |
| CEV / Otentik | `Cev` | Certificat export authentifiable via la plateforme douanière Otentik (QR code). Complète le PDF sans le remplacer. | p.43, 96 |

## 7. Facturation et cotisations

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Cotisation (CVO) | `Cotisation` | Cotisation volontaire obligatoire, calculée sur l'assiette selon un barème par type d'opérateur et par colonne. ⚠ CVE | p.27-28, 96 |
| Assiette de cotisation | `AssietteCotisation` | DSM négociant : col. 14, 17-19, 21, 22-25. DSM producteur : col. 16, 17-18, 21, 22-23. ⚠ | p.44-45 |
| Barème | `BaremeCotisation` | Taux de cotisation et pourcentage d'exonération de TVA, avec périodes de validité sans chevauchement. | p.16, 45 |
| Facture | `Facture` | Types : cotisation DSM, cotisation fin de travaux, manuelle ou provisionnelle, commande boutique. PDF conservé par la plateforme. | p.16, 44 |
| Avoir | `Avoir` | Manuel, ou sur tout ou partie d'une facture ; reprend le taux ou l'exonération de la facture d'origine. | p.46 |
| État de règlement | `EtatReglement` | Statut de traitement/paiement restitué par Zeendoc d'après le n° de facture. Zeendoc n'est jamais la source de vérité des factures. | p.46, 83, 139 |

## 8. Référentiels

Données administrées par le BNIA (contexte `referentiels`, spec 01). Aucune valeur n'est supprimée :
une valeur **inactive** n'est plus proposée à la saisie mais reste affichée là où elle est utilisée.

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Zone commerciale | `ZoneCommerciale` | Continent ou zone de rattachement d'un pays (Europe, Asie…), géographique. | p.52, 128 |
| Pays | `Pays` | Code pays BNIA, code ISO alpha-2, libellé, zone commerciale, membre de l'UE, langue et niveau de détail du certificat export, âge minimum à l'export. | p.52, 128 |
| Membre de l'UE | — | Indicateur du pays, distinct de sa zone : permet de distinguer l'UE du reste de l'Europe dans les statistiques. | p.47 |
| Langue du certificat | `LangueCertificat` | `FR_EN`, `FR_ES` ou `FR_IT` : langues du texte de certification, selon le pays de destination. | p.42, 122 |
| Niveau de détail du certificat | `NiveauDetailCertificat` | `CUMUL_DEGRE` (certificat simplifié : une ligne par degré), `DETAIL_COMPTE_AGE` ou `DETAIL_MILLESIME` (certificat détaillé), selon le pays de destination. | p.42, 123 |
| Âge minimum à l'export | — | `CompteDAge` minimum attendu pour exporter vers un pays. Une vente plus jeune déclenche une alerte non bloquante (contexte `exportation`). | p.52, 128 |
| Région | `Region` | Région métier de rattachement d'un opérateur, avec un indicateur « en région délimitée ». | p.52, 102, 129 |
| Région délimitée | — | Aire de l'appellation Armagnac. Distingue les opérateurs « région Armagnac / hors région » et les flux de DSM en ou hors région délimitée. | p.24, 102 |
| Contenance | `Contenance` | Volume reconnu d'un contenant, en litres (`BigDecimal`). Identifiée par son volume ; entre dans le calcul du volume d'alcool pur des ventes export. | p.40, 131 |
| Classe de TVA | `ClasseTva` | Catégorie de taxe (code, libellé, active) portant un historique de taux. | p.52, 127 |
| Taux de TVA | `TauxTva` | Taux d'une classe sur une période (début, fin facultative). Pas de chevauchement dans une classe. Figé sur les factures et avoirs qui l'appliquent. | p.52, 127 |
| Marque | `Marque` | Marque commerciale exploitée par un opérateur (portée France ou Export sur la fiche). Créable depuis la fiche opérateur, au back-office comme au portail ; libellé unique à la casse et aux accents près. | p.19, 131 |
| Circuit de distribution | `CircuitDistribution` | Canal de vente des marques d'un opérateur. | p.19, 131 |
| Produit commercialisé | `ProduitCommercialise` | Type de produit proposé par un opérateur (Armagnac, Blanche, Floc…). Ne pas confondre avec le produit de facturation ni le produit boutique. | p.20, 131 |
| Titre | `Titre` | Civilité ou forme d'appel (fiches, contacts, destinataires). | p.53, 129 |
| Label | `Label` | Label ou distinction touristique d'un opérateur. | p.19, 130 |
| Type de visite | `TypeVisite` | Visite ou prestation d'accueil proposée par un opérateur. | p.19, 130 |
| Langue | `Langue` | Langue parlée à l'accueil d'un opérateur. Sans rapport avec la langue du certificat. | p.20, 130 |
| Centre d'intérêt | `CentreInteret` | Domaine d'activité d'un contact (comptabilité, marketing, production), pour cibler les communications. | p.21, 130 |

## 9. Systèmes

| Système | Rôle dans la cible |
|---|---|
| InteGraal (éditeur AGI) | Logiciel métier actuel, base Oracle. Remplacé ; source de la reprise de données. |
| pro.armagnac.fr | Extranet opérateur actuel. Remplacé par le portail ; source de la reprise. |
| armagnac.fr | Site public conservé. Seuls certains contenus sont repris ; la fiche annuaire reste hors périmètre. |
| ODG (outil Innovagro, remplacé par TimeTonic) | Source des contrats vin et des fins de travaux. Flux unique, ODG → BNIA. |
| Zeendoc | SaaS de diffusion et de suivi des factures. Dépôt des PDF, retour d'état de paiement. |
| Otentik | Plateforme douanière du certificat CEV. |
| Service de signature | Tiers de type Yousign / DocuSign / SSL.com, appelé par API. |

Sigles : AOC (appellation d'origine contrôlée), CVI (casier viticole informatisé), DGCCRF (répression des fraudes), EDI, TOTP (code à usage unique, 2e facteur).
