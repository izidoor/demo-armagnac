# Glossaire : langage ubiquitaire

> Source de vérité du vocabulaire. Les termes ne sont jamais traduits ni renommés (ADR-001).
> `p.N` renvoie aux pages du CDC v1.1 (`docs/source/cdc/`). ⚠ renvoie à `questions-ouvertes.md`.
> Colonne « Code » : nom du type ou du concept dans le code (français, sans accents).

## 1. Acteurs

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| BNIA | `Bnia` | Bureau National Interprofessionnel de l'Armagnac, maître d'ouvrage. Dans la plateforme, c'est un **opérateur** de type `BNIA`, unique ; ses agents sont son *personnel*. | p.96, décision du 2026-10-08 |
| Personnel BNIA | — | Membres de l'opérateur BNIA. Leur rôle vaut pour tous les opérateurs ; eux seuls créent et ferment les opérateurs, bloquent les utilisateurs, gèrent le paramétrage. Remplace l'« utilisateur BNIA » du CDC (§4.2.2, C.2.1), qui n'est plus une population distincte. | p.14, décision du 2026-10-08 |
| Rôle | `Role` | Niveau d'un rattachement, liste fermée définie par l'application : `OBSERVATEUR` (lit tout), `AGENT` (gère Contacts, Paramétrage et Déclarations), `ADMIN` (gère aussi Accès). Un rôle par rattachement. Remplace les rôles composés par le BNIA du CDC (§4.2.1), écart assumé. | p.14-17, spec 02 §3.2.7 |
| Module | `Module` | Ensemble de fonctions sur lequel un rôle lit ou gère : Contacts (contacts, fiches professionnelles, fiche et présentation de l'opérateur), Accès (invitations, rôles, suspensions, retraits), Paramétrage (référentiels, opérateur BNIA seulement), Déclarations (DSM, pièces, commentaires, point de départ, stocks déclarés ; spec 05 §3.7). Chaque nouveau contexte en ajoute un. | p.15-17, spec 02 §3.2.7, spec 05 |
| Opérateur | `Operateur` | Producteur, négociant, autre structure ou le BNIA lui-même, ayant une fiche dans le référentiel BNIA. Ses personnes sont des contacts ou des membres. | p.95 |
| Type d'opérateur | `TypeOperateur` | `BNIA`, `PRODUCTEUR`, `NEGOCIANT` ou `AUTRE`. Pilote la périodicité par défaut, les colonnes DSM saisissables, les DRM, l'assiette de cotisation et la forme du code. Un `AUTRE` (distillateur, courtier…) ou le `BNIA` (unique, créé par l'application) ne déclare pas : ni DSM ni périodicité. Liste fermée du contexte `operateurs`, **pas** un référentiel administrable. Probablement l'ancien « type de tiers » d'InteGraal (Q-REF-1). | p.18, 25, 28, 101 |
| Code opérateur BNIA | `CodeOperateur` | Négociant : `N` + n° de département (tiré du code postal) + incrément sur 4 chiffres par département. Producteur et `AUTRE` : incrément sur 6 chiffres. Proposé à la création (plus grand numéro existant + 1), unique, modifiable ; non partagé avec l'ODG. Les autres contextes référencent l'opérateur par son identifiant technique. | p.21 |
| Groupe opérateur | — | Texte qui regroupe plusieurs fiches (chais) d'un même opérateur ; deux fiches sont du même groupe si leurs textes sont égaux à la casse, aux accents et aux espaces près (H-OPE-1). | p.18, 20 |
| Contact principal | — | Personne de l'annuaire de l'opérateur (contact ou membre), désignée par sa fiche professionnelle comme point d'entrée de la structure. | p.18, 101 |
| Identifiant tiers ODG | — | Identifiant de l'opérateur dans l'ODG, unique s'il est renseigné ; la passerelle ODG rattache ses flux par lui, puis par CVI ou SIRET. | p.101 |
| Destinataire des factures | — | Sur la fiche opérateur : une personne de l'annuaire qui a un e-mail, ou une adresse e-mail saisie ; lu et figé par la facturation. | p.123 |
| Fiche incomplète | — | Fiche opérateur enregistrée sans tout ce qu'il faut pour facturer ou certifier (adresse, code postal, ville, pays, région) ; signalée, jamais bloquée. | décision du 2026-10-06 |
| Présentation de l'opérateur | `PresentationOperateur` | Marques, circuits, visites, labels, œnotourisme et médailles d'un opérateur ; agrégat distinct de la fiche, de même identifiant. Saisie sans flux vers l'annuaire public. | p.19-20, 103-104 |
| Médaille et concours | `MedailleConcours` | Distinction obtenue par un opérateur : concours, année, distinction (or, argent, bronze, autre), produit. | p.20, 104 |
| Fiche annuaire opérateur | — | Fiche de présentation d'un opérateur sur armagnac.fr. Hors périmètre de la plateforme. | p.6, 98 |
| Contact | `Contact` | Personne connue d'un opérateur, **sans compte** : titre, nom, prénom, e-mail facultatif, et sa fiche professionnelle. L'opérateur en est propriétaire. Dans un opérateur, une personne est contact ou membre, jamais les deux ; un même e-mail dans deux opérateurs donne deux contacts indépendants. | p.20-21, 104-105, vision |
| Utilisateur | `Utilisateur` | Personne qui possède un compte : titre, nom, prénom, e-mail (unique, identifiant de connexion), statut `ACTIF` ou `BLOQUE`, opérateur actif. Propriétaire de son identité, qu'elle seule modifie. N'existe qu'une fois son e-mail prouvé (invitation acceptée) ; supprimé avec son dernier rattachement. | p.14, 60, 105, vision |
| Membre | — | Utilisateur rattaché à un opérateur, avec un rôle. | vision |
| Rattachement | `Rattachement` | Accès d'un utilisateur à un opérateur : un rôle et un statut `ACTIF` ou `SUSPENDU` ; un au plus par opérateur et par utilisateur. Suit le C.13.2. | p.60, 135, vision |
| Fiche professionnelle | `FicheProfessionnelle` | Informations d'une personne dans un opérateur, propriété de l'opérateur : fonction, téléphones, adresse, centres d'intérêt, observation. Une par contact et par rattachement ; reste la même quand la personne passe de contact à membre ou l'inverse. | p.104-105, vision |
| Invitation | `Invitation` | Proposition d'accès envoyée par e-mail, avec un rôle, depuis un contact ou pour une personne nouvelle ; `EN_ATTENTE`, `ACCEPTEE`, `ANNULEE` ou `EXPIREE` (7 jours). L'accepter prouve l'e-mail et crée le rattachement. | p.59, 79, vision |
| Annuaire | — | Liste d'un opérateur réunissant ses contacts, ses membres et ses invitations en attente. À ne pas confondre avec la *fiche annuaire opérateur* d'armagnac.fr. | p.78, vision |
| Suspension | — | Statut `SUSPENDU` d'un rattachement : l'accès à cet opérateur seul est coupé, les autres restent. | vision UC-15 |
| Blocage par le BNIA | — | Statut `BLOQUE` d'un utilisateur, posé avec un motif par un ADMIN BNIA : plus aucune connexion, dans aucun opérateur. Distinct du *blocage temporaire*. | p.17, vision UC-05 |
| ~~Administrateur~~ | — | Remplacé le 2026-10-08 par le rôle `ADMIN` d'un rattachement. | p.78-79, 105 |
| ~~Module portail~~ | ~~`ModulePortail`~~ | Remplacé le 2026-10-08 : plus de droits par module sur la personne ; voir *Rôle* et *Module*. | p.78-79 |
| ~~Compte portail~~ | ~~`ComptePortail`~~ | Remplacé le 2026-10-08 par *Utilisateur* (identité, `operateurs`) et *Compte de connexion* (preuve d'identité, `authentification`). | p.60 |
| Compte de connexion | `CompteConnexion` | Dans `authentification` : identifiant de connexion (l'e-mail de l'utilisateur), mot de passe haché, blocage temporaire. Ni nom ni droit. Créé, renommé et supprimé avec l'utilisateur. | p.17, 135, spec 03 |
| Double authentification | — | Code à 6 chiffres envoyé par e-mail à chaque connexion, pour tous (`000000` en dev et démo), sauf sur un *navigateur de confiance*. Pas de TOTP. | p.17, 60, spec 03 |
| Navigateur de confiance | — | Navigateur dispensé de code pendant 30 jours pour un compte ; invalidé par un changement de mot de passe. | p.17, spec 03 |
| Blocage temporaire | — | Refus de toute connexion pendant 30 minutes après 3 échecs (mot de passe ou code faux) ; levé par « mot de passe oublié ». | p.17, 59, spec 03 |
| Opérateur actif | — | Opérateur dans lequel l'utilisateur travaille, attribut de l'utilisateur ; il décide de l'espace (BNIA : back-office ; autre : portail) et filtre ce qui est visible. Remplace « compte opérateur actif » (C.13.2). | p.60-62, vision UC-21 |
| Destinataire | `Destinataire` | Tiers destinataire d'une vente export, rattaché à un pays, qualifié par un `TypeTiers`. Distinct des opérateurs ; vit dans le contexte `exportation`, pas dans `referentiels`. | p.40-41, 129 |
| Type tiers | `TypeTiers` | Catégorie d'acteur d'un destinataire export (importateur, distributeur…). Liste administrable du contexte `exportation`. À ne pas confondre avec `TypeOperateur`. | p.53, 129-130 |

## 2. Produit et mesures

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Campagne de distillation | `Campagne` | Du 1er septembre N au 31 mars N+1. | p.94 |
| Millésime | `Millesime` | Concept partagé (`commun.domain`). Année de la campagne de distillation (campagne 2025/2026 → millésime 2025). | p.95, 98 |
| Appellation | `Appellation` | Concept partagé (`commun.domain`). Liste fermée des AOC : Armagnac, Bas Armagnac, Armagnac Ténarèze, Haut Armagnac, Blanche Armagnac. | p.102 |
| Compte d'âge | `CompteDAge` | Concept partagé (`commun.domain`). Âge commercial, qui évolue chaque 1er avril. Millésime N : `00` avant le 01/04/N+1, `0` jusqu'au 31/03/N+2, puis `1`, `2`… `00` et `0` sont deux valeurs distinctes (ce n'est donc pas un entier). Sans borne ; la borne `20+` est celle de la DSM (`CompteDAgeDsm`). | p.39, 98-99 |
| Compte d'âge de DSM | `CompteDAgeDsm` | Compte d'âge d'une ligne de DSM, borné : `00`, `0`, `1` … `19`, `20+`. Calculé depuis le millésime pour une ligne `MB`, saisi sinon ; une ligne `MV` ne dépasse pas le compte de son millésime. Partie publiée de `declarations`. | p.39-40, 98-99, spec 05 RG-DEC-12 |
| Vieillissement | — | Au 1er avril, seul le fût vieillit : une ligne `MB` est recalculée, une ligne `NB` prend +1 (borné à `20+`) ; `MV` et `NV` gardent leur compte. Les lignes devenues identiques fusionnent. | p.98, spec 05 RG-DEC-14 |
| Assemblage | `Assemblage` | Mélange de plusieurs Armagnacs ; son compte d'âge est celui de l'eau-de-vie la plus jeune. | p.94 |
| Mention commerciale | `MentionCommerciale` | Déduite du compte d'âge : `00` et `0` aucune ; `1` à `3` VS ou \*\*\* ; `4` à `9` VSOP ; `10`+ Hors d'Âge, Extra, Réserve. | p.99 |
| Clé de conditionnement | `CleConditionnement` | `MB` millésime sous bois, `MV` millésime sous verre, `NB` assemblage sous bois, `NV` assemblage sous verre. | p.96 |
| Dénomination | `Denomination` | Désignation commerciale (« qualité ou dénomination ») d'une ligne `NB` ou `NV`, qui n'a pas de millésime. Texte libre, comparé sans tenir compte de la casse, des accents ni des espaces. | p.23, spec 05 RG-DEC-10, 11 |
| Volume d'alcool pur | `VolumeAlcoolPur` | Concept partagé (`commun.domain`). Volume × TAV. Unité DSM : litre d'alcool pur (LAP) ; statistiques et cotisation en `hlAP` (litres ÷ 100). Stocké en litres (`BigDecimal`, 2 décimales), jamais en flottant ; signé (un stock calculé peut être négatif), un volume saisi est positif ou nul. | p.23, 96, spec 05 A-DEC-8 |
| Format hl.l,cl | `FormatHlLCl` | Présentation d'un volume : 129 550,3 L s'affiche `1 295.50,30`. Éditions Excel (format de cellule `#0\.00.00`, valeur en litres) et PDF, fichiers ; la saisie à l'écran est en litres décimaux. | p.96, spec 05 H-DEC-4 |
| TAV | `Tav` | Titre alcoométrique volumique (% vol), exprimé à 20 °C sur les certificats. | p.42, 97 |
| Bouteille / Vrac | `TypeConditionnement` | Distinction portée par les lignes de vente export. | p.38 |
| Alambic | `Alambic` | Appareil de distillation détenu par un opérateur : n° d'agrément, type (armagnacais ou double chauffe, H-OPE-3), capacité. L'opérateur est « détenteur d'alambic » s'il en a au moins un. | p.19, 102 |
| Diffusion des millésimes | `DiffusionMillesimes` | Accord d'un opérateur pour l'usage de ses stocks par millésime dans les statistiques du BNIA : refus (par défaut), partiel (liste des millésimes diffusables) ou total. | p.19, 102 |

## 3. Déclarations de stocks et mouvements

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| DSM | `DeclarationStocksMouvements` | Déclaration de stocks et mouvements d'un opérateur pour une période. Format unique producteur/négociant : 5 onglets, 26 colonnes numérotées. L'opérateur l'initie, la remplit et l'envoie ; le BNIA l'instruit et la valide, ce qui la fige. | p.22-24, spec 05 |
| Périodicité DSM | `PeriodiciteDsm` | Mensuelle, trimestrielle, semestrielle ou annuelle, portée par la fiche opérateur. Sans effet dans le prototype : la période découle du type (`PeriodeDeclaration`). | p.28, 101, spec 05 §2.2 |
| Statut de déclaration de stock | `StatutDeclarationStock` | `OUI`, `NON`, `SOMMEIL`, `CLOTURE`, sur la fiche opérateur. Seul `OUI` rend une DSM attendue (H-OPE-2). | p.102 |
| Période de déclaration | `PeriodeDeclaration` | Du 1er avril au 31 mars pour un producteur, mois civil pour un négociant. Commence le lendemain de la dernière DSM validée, ou au point de départ. Ni chevauchement, ni trou. | p.22, spec 05 RG-DEC-03, 04 |
| Point de départ déclaratif | `PointDeDepartDeclaratif` | Début de la première période d'un opérateur et son stock initial, ligne par ligne, fixés par le BNIA ; modifiable tant que la première DSM qui en est issue n'est pas validée ; un nouveau peut être fixé après une interruption. | spec 05 §3.3 |
| Échéance | — | Date limite d'envoi d'une DSM : le 5 du mois qui suit la période (négociant), le 30 avril qui suit (producteur). | spec 05 RG-DEC-39 |
| Période attendue | — | Celle de la DSM non validée de l'opérateur, sinon celle qui suit sa dernière DSM validée. **Absente** si aucune DSM n'est initiée ; **en retard** si l'échéance est passée et que la DSM n'est ni envoyée ni en instruction. | p.27, spec 05 RG-DEC-40 |
| Colonne DSM, colonne grisée | `ColonneDsm` | Une des 26 colonnes du format unique, avec son onglet, son sens, son assiette, sa ligne de cotisation et son justificatif. Huit colonnes (2, 3, 5, 9, 11, 19, 24, 25) sont grisées pour un producteur : non saisissables, non importées, exclues des totaux, des contrôles et de la cotisation. | p.23-25, spec 05 §3.1 |
| Ligne DSM | `LigneDsm` | Unique dans une déclaration sur (dénomination ou millésime, clé de conditionnement, compte d'âge). Origine `REPRISE` (stock initial ou DSM précédente : ne se supprime pas) ou `AJOUT`. | p.23, spec 05 RG-DEC-11, 13 |
| Stock début / fin | `StockDebut`, `StockFin` | Le début (col. 1) reprend la fin (col. 26) de la DSM précédente, vieillie au 1er avril, ou le stock initial du point de départ ; seules les lignes de stock strictement positif sont reprises. La fin est calculée : 1 + 2 + 3 + Σ(4 à 9) − Σ(10 à 25). | p.23-24, spec 05 RG-DEC-14, 17 |
| Contrôle de cohérence | `ControleCoherence` | Joué à la saisie, à l'import et avant l'envoi ou la validation, avec le motif et le niveau (déclaration, onglet, ligne). Les **anomalies de calcul** (stock de fin négatif, Σ6 ≠ Σ12, Σ5 ≠ Σ11, compte `MV` trop vieux) bloquent l'envoi ; les **pièces manquantes** (justificatifs, DRM) bloquent la validation. | p.26-27, spec 05 RG-DEC-18, 29 |
| États d'une DSM | `EtatDsm` | `BROUILLON` → `ENVOYEE` → `EN_INSTRUCTION` → `VALIDEE`, avec `COMPLEMENTS_DEMANDES` (retour à l'opérateur, puis renvoi). « À facturer » et « facturée » ne sont pas des états : ils se lisent dans la projection des factures. | p.22, 108, spec 05 RG-DEC-20 |
| Instruction | — | Travail du BNIA sur une DSM envoyée : prise en instruction, commentaires, demande de compléments (motif obligatoire), validation. | spec 05 RG-DEC-20, 24 |
| Commentaire | `Commentaire` | Message de l'opérateur ou du BNIA sur une DSM ou une ligne, dans tout état ; « interne » s'il est du BNIA et caché à l'opérateur. Remplace la note de contrôle et le commentaire de contrôle d'un justificatif. | p.108, 111, spec 05 RG-DEC-24 |
| Journal des déclarations | `JournalDeclarations` | Trace des actions sur les DSM et le point de départ d'un opérateur, en ajout seul, sans valeurs avant et après. | p.29, spec 05 RG-DEC-23 |
| Justificatif | `Justificatif` | PDF prouvant un mouvement, avec son type de mouvement (achat, autres fabrications, vente négoce, vente export, réintégration) et son type de pièce (facture, DAE, avoir, déclaration de fabrication douanes ; contrat avec `contratsarmagnac`), rattaché à la DSM ou à une ligne. Déposeur et date tracés. | p.25-26, 95, spec 05 RG-DEC-26 |
| DRM | `Drm` | Déclaration récapitulative mensuelle transmise aux douanes. Producteurs uniquement : un PDF par mois de la période (12 pour une DSM annuelle). | p.26, spec 05 RG-DEC-27 |
| DAE / DAI | `Dae`, `Dai` | Document d'accompagnement électronique ; document annuel d'inventaire. | p.96 |
| DIA | — | Déclaration d'inventaire annuel déposée aux douanes ; les pertes de la colonne 10 en reprennent le détail, sa date est portée par la DSM (le CDC écrit « DAI » en C.3.2.5). | p.109, modèles Excel, spec 05 H-DEC-12 |
| Signature de la DSM | `SignatureDsm` | Validation par l'opérateur d'un PDF signé via un service tiers. Hors périmètre du prototype (décision du 2026-10-12). | p.66 |
| Stock déclaré | — | Stock de fin de la dernière DSM validée d'un opérateur, ligne par ligne, daté de sa fin de période. | spec 05 RG-DEC-42 |
| Stock estimé | — | Stock déclaré vieilli à une date, plus les entrées connues depuis (achats retirés, fins de travaux du propriétaire), moins les ventes retirées. Livré avec `contratsarmagnac` et `distillation`. | spec 05 §5.2 |
| Engagements | — | Volumes des contrats de vente validés, pas encore retirés. | spec 05 §5.2 |
| Stock disponible | — | Stock estimé moins les engagements. | spec 05 §5.2 |
| Bilan prévisionnel | — | Stock disponible d'un vendeur au grain d'un contrat, comparé aux volumes demandés ; lu par `contratsarmagnac` avant validation. | spec 05 §5.2 |

## 4. Contrats

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Contrat achat Armagnac | `ContratAchatArmagnac` | Achat/vente d'Armagnac entre opérateurs, saisi par le BNIA ou par un opérateur. Trois validations tracées séparément : acheteur, vendeur, BNIA. Le BNIA peut valider sans les parties, et invalider. | p.30-31 |
| Contrat exécuté | `ContratExecute` | Contrat dont toutes les retiraisons ont été effectuées (décision du 2026-10-12). Une ligne d'achat ou de vente de DSM se rattache aux retiraisons de sa période (règle livrée avec `contratsarmagnac`). | p.27, analyse 05 |
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
| Cotisation (CVO) | `Cotisation` | Cotisation volontaire obligatoire : assiette de la DSM × taux `CVO` en vigueur à la fin de la période, détaillée par ligne de cotisation. Synonyme : `CVE`, interchangeable dans le CDC (décision du 2026-10-12). | p.27-28, 96, spec 05 RG-DEC-31 à 33 |
| Assiette de cotisation | `AssietteCotisation` | Colonnes 14, 16 à 19 et 21 à 25 de la DSM, colonnes grisées exclues (producteur : 14, 16, 17, 18, 21, 22, 23). Écarts à §4.9.3 : H-DEC-1 et 2 de la spec 05. | p.27-28, 44-45, spec 05 RG-DEC-31 |
| Grille de cotisation | `GrilleCotisation`, `TauxCotisation` | Taux `CVO` en €/hlAP et pourcentage exonéré de TVA, datés en chaîne continue (sans trou ni chevauchement), administrés dans le Paramétrage. Remplace « barème » (`BaremeCotisation`) : le CDC dit « grille des cotisations » (C.10.3). | p.52, 128, spec 05 RG-DEC-30 |
| Ligne de cotisation | `LigneCotisation` | Ligne du détail de la cotisation et de la facture : sorties France (col. 14, 17, 18, 19, 21), sorties export (22, 23, 24), EDV pour un autre produit au marché intérieur (16) et à l'export (25) ; chacune avec son volume en hlAP, sa part non assujettie et sa part assujettie à la TVA. | facture d'exemple, spec 05 RG-DEC-33 |
| Facture | `Facture` | Types : cotisation DSM, cotisation fin de travaux, manuelle ou provisionnelle, commande boutique. PDF conservé par la plateforme. | p.16, 44 |
| Avoir | `Avoir` | Manuel, ou sur tout ou partie d'une facture ; reprend le taux ou l'exonération de la facture d'origine. | p.46 |
| État de règlement | `EtatReglement` | Statut de traitement/paiement restitué par Zeendoc d'après le n° de facture. Zeendoc n'est jamais la source de vérité des factures. | p.46, 83, 139 |

## 8. Référentiels

Données administrées par le BNIA (contexte `referentiels`, spec 01). Une valeur inutilisée se supprime ;
une valeur utilisée se désactive : **inactive**, elle n'est plus proposée à la saisie mais reste affichée
là où elle est utilisée (ADR-011).

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
| Contenance | `Contenance` | Volume reconnu d'un contenant, en litres (`BigDecimal`). Volume unique dans le référentiel ; entre dans le calcul du volume d'alcool pur des ventes export. | p.40, 131 |
| Classe de TVA | `ClasseTva` | Catégorie de taxe (code, libellé, active) portant un historique de taux. | p.52, 127 |
| Taux de TVA | `TauxTva` | Taux d'une classe sur une période (début, fin facultative). Pas de chevauchement dans une classe. Figé sur les factures et avoirs qui l'appliquent. | p.52, 127 |
| Marque | `Marque` | Marque commerciale exploitée par un opérateur (portée France ou Export sur la fiche). Créable depuis la fiche opérateur, au back-office comme au portail ; libellé unique à la casse et aux accents près. | p.19, 131 |
| Circuit de distribution | `CircuitDistribution` | Canal de vente des marques d'un opérateur. | p.19, 131 |
| Produit commercialisé | `ProduitCommercialise` | Type de produit proposé par un opérateur (Armagnac, Blanche, Floc…). Ne pas confondre avec le produit de facturation ni le produit boutique. | p.20, 131 |
| Titre | `Titre` | Civilité ou forme d'appel (fiches, contacts, destinataires). | p.53, 129 |
| Label | `Label` | Label ou distinction touristique d'un opérateur. | p.19, 130 |
| Type de visite | `TypeVisite` | Visite ou prestation d'accueil proposée par un opérateur. | p.19, 130 |
| Langue | `Langue` | Langue parlée à l'accueil d'un opérateur. Sans rapport avec la langue du certificat. | p.20, 130 |
| Membre BNIA | `MembreBnia` | Statut ou rôle interne au BNIA associé à un opérateur ; liste administrable, sens à préciser (Q-OPE-4). | p.101 |
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
