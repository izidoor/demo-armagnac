# Glossaire : langage ubiquitaire

> Source de vérité du vocabulaire. Les termes ne sont jamais traduits ni renommés (ADR-001).
> `p.N` renvoie aux pages du CDC v1.1 (`docs/source/cdc/`). ⚠ renvoie à `questions-ouvertes.md`.
> Colonne « Code » : nom du type ou du concept dans le code (français, sans accents).

## 1. Acteurs

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| BNIA | `Bnia` | Bureau National Interprofessionnel de l'Armagnac, maître d'ouvrage. Dans la plateforme, c'est un **opérateur** de type `BNIA`, unique ; ses agents sont son *personnel*. | p.96, décision du 2026-10-08 |
| Personnel BNIA | — | Membres de l'opérateur BNIA. Leur rôle vaut pour tous les opérateurs ; eux seuls créent et ferment les opérateurs, bloquent les utilisateurs, gèrent le paramétrage. Remplace l'« utilisateur BNIA » du CDC (§4.2.2, C.2.1), qui n'est plus une population distincte. | p.14, décision du 2026-10-08 |
| Rôle | `Role` | Niveau d'un rattachement, liste fermée définie par l'application : `OBSERVATEUR` (lit tout ; gère seulement ses conversations libres, spec 15), `AGENT` (gère Contacts, Paramétrage et Déclarations), `ADMIN` (gère aussi Accès). Un rôle par rattachement. Remplace les rôles composés par le BNIA du CDC (§4.2.1), écart assumé. | p.14-17, spec 02 §3.2.7 |
| Module | `Module` | Ensemble de fonctions sur lequel un rôle lit ou gère : Contacts (contacts, fiches professionnelles, fiche et présentation de l'opérateur), Accès (invitations, rôles, suspensions, retraits), Paramétrage (référentiels, opérateur BNIA seulement), Déclarations (DSM, pièces, notes internes, point de départ, stocks déclarés ; spec 05 §3.7), Facturation (spec 08 §3.12), Messagerie (conversations libres, gérée par les trois rôles ; spec 15 §3.6). Chaque nouveau contexte en ajoute un. | p.15-17, spec 02 §3.2.7, spec 05, 08, 15 |
| Opérateur | `Operateur` | Producteur, négociant, autre structure ou le BNIA lui-même, ayant une fiche dans le référentiel BNIA. Ses personnes sont des contacts ou des membres. | p.95 |
| Type d'opérateur | `TypeOperateur` | `BNIA`, `PRODUCTEUR`, `NEGOCIANT` ou `AUTRE`. Pilote la périodicité par défaut, les colonnes DSM saisissables, les DRM, l'assiette de cotisation et la forme du code. Un `AUTRE` (distillateur, courtier…) ou le `BNIA` (unique, créé par l'application) ne déclare pas : ni DSM ni périodicité. Liste fermée du contexte `operateurs`, **pas** un référentiel administrable. Probablement l'ancien « type de tiers » d'InteGraal (Q-REF-1). | p.18, 25, 28, 101 |
| Code opérateur BNIA | `CodeOperateur` | Négociant : `N` + n° de département (tiré du code postal) + incrément sur 4 chiffres par département. Producteur et `AUTRE` : incrément sur 6 chiffres. Proposé à la création (plus grand numéro existant + 1), unique, modifiable ; non partagé avec l'ODG. Les autres contextes référencent l'opérateur par son identifiant technique. | p.21 |
| Groupe opérateur | — | Texte qui regroupe plusieurs fiches (chais) d'un même opérateur ; deux fiches sont du même groupe si leurs textes sont égaux à la casse, aux accents et aux espaces près (H-OPE-1). | p.18, 20 |
| Contact principal | — | Personne de l'annuaire de l'opérateur (contact ou membre), désignée par sa fiche professionnelle comme point d'entrée de la structure. | p.18, 101 |
| Identifiant tiers ODG | — | Identifiant de l'opérateur dans l'ODG, unique s'il est renseigné ; la passerelle ODG rattache ses flux par lui, puis par CVI ou SIRET. | p.101 |
| Destinataire des factures | — | Sur la fiche opérateur : une personne de l'annuaire qui a un e-mail, ou une adresse e-mail saisie ; lu et figé par la facturation. | p.123 |
| Fiche facturable, fiche certifiable | — | Indicateurs de la fiche opérateur, qui remplacent « fiche incomplète » et nomment chacun ce qui manque. Facturable : SIRET, adresse ligne 1, code postal, ville, pays, destinataire des factures ; certifiable : adresse ligne 1, code postal, ville, pays, région. Signalés, jamais bloquants pour la fiche ; une DSM d'un opérateur non facturable ne se facture pas. Livrés par la spec 08, lot 1. | p.123 ; décision du 2026-10-14 |
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
| Double authentification | — | Code à 6 chiffres envoyé par e-mail à chaque connexion, pour tous (`000000` en dev et démo), sauf sur un *navigateur de confiance*. Pas de TOTP. Activée ou désactivée pour toute l'application par un ADMIN du BNIA ; désactivée au départ (RG-AUT-16). | p.17, 60, spec 03 |
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
| Vieillissement | — | Au 1er avril, seul le fût vieillit : le compte d'âge d'une ligne `MB` est recalculé depuis son millésime, une ligne `NB` prend +1 (borné à `20+`) ; `MV` et `NV` gardent leur compte. Les lignes devenues identiques fusionnent. | p.98, spec 05 RG-DEC-14 |
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
| Année déclarative | `AnneeDeclarative` | Du 1er avril au 31 mars (« 2026-2027 ») : l'année de la période d'un producteur, au début de laquelle le stock vieillit. Les stocks déclarés s'interrogent une année à la fois, sans cumul d'une année à l'autre. À ne pas confondre avec la campagne de distillation. | spec 05 RG-DEC-44 |
| Période de déclaration | `PeriodeDeclaration` | Du 1er avril au 31 mars pour un producteur, mois civil pour un négociant. Commence le lendemain de la dernière DSM validée, ou au point de départ. Ni chevauchement, ni trou. | p.22, spec 05 RG-DEC-03, 04 |
| Point de départ déclaratif | `PointDeDepartDeclaratif` | Début de la première période d'un opérateur et son stock initial, ligne par ligne, fixés par le BNIA ; modifiable tant que la première DSM qui en est issue n'est pas validée ; un nouveau peut être fixé après une interruption. | spec 05 §3.3 |
| Échéance | — | Date limite d'envoi d'une DSM : le 5 du mois qui suit la période (négociant), le 30 avril qui suit (producteur). | spec 05 RG-DEC-39 |
| Période attendue | — | Celle de la DSM non validée de l'opérateur, sinon celle qui suit sa dernière DSM validée. **Absente** si elle a commencé et qu'aucune DSM n'est initiée ; **en retard** si l'échéance est passée et que la DSM est absente, en brouillon ou en compléments demandés. | p.27, spec 05 RG-DEC-40 |
| Colonne DSM, colonne grisée | `ColonneDsm` | Une des 26 colonnes du format unique, avec son onglet, son sens, son assiette, sa ligne de cotisation et son justificatif. Huit colonnes (2, 3, 5, 9, 11, 19, 24, 25) sont grisées pour un producteur : non saisissables, non importées, exclues des totaux, des contrôles et de la cotisation. | p.23-25, spec 05 §3.1 |
| Ligne DSM | `LigneDsm` | Unique dans une déclaration sur (dénomination ou millésime, clé de conditionnement, compte d'âge). Origine `REPRISE` (stock initial ou DSM précédente : ne se supprime pas) ou `AJOUT`. | p.23, spec 05 RG-DEC-11, 13 |
| Stock début / fin | `StockDebut`, `StockFin` | Le début (col. 1) reprend la fin (col. 26) de la DSM précédente, vieillie au 1er avril, ou le stock initial du point de départ ; seules les lignes de stock strictement positif sont reprises. La fin est calculée : 1 + 2 + 3 + Σ(4 à 9) − Σ(10 à 25). | p.23-24, spec 05 RG-DEC-14, 17 |
| Contrôle de cohérence | `ControleCoherence` | Joué à la saisie, à l'import et avant l'envoi ou la validation, avec le motif et le niveau (déclaration, onglet, ligne). Les **anomalies de calcul** (stock de fin négatif, Σ6 ≠ Σ12, Σ5 ≠ Σ11, compte `MV` trop vieux) bloquent l'envoi ; les **pièces manquantes** (justificatifs, DRM) bloquent la validation. | p.26-27, spec 05 RG-DEC-18, 29 |
| États d'une DSM | `EtatDsm` | `BROUILLON` → `ENVOYEE` → `EN_INSTRUCTION` → `VALIDEE`, avec `COMPLEMENTS_DEMANDES` (retour à l'opérateur) puis `COMPLEMENTS_RECUS` (renvoi, validé ou renvoyé par le BNIA sans nouvelle prise en instruction). Libellés selon le côté : `ENVOYEE` « Envoyée » pour l'opérateur, « À prendre en charge » pour le BNIA ; `COMPLEMENTS_RECUS` « Compléments envoyés », « Compléments reçus ». « À facturer » et « facturée » ne sont pas des états : ils se lisent dans la projection des factures. | p.22, 108, spec 05 RG-DEC-20 |
| Instruction | — | Travail du BNIA sur une DSM envoyée : prise en instruction, messages et notes internes, demande de compléments (motif obligatoire, déposé dans la conversation de la DSM), validation. | spec 05 RG-DEC-20, 24 ; spec 15 RG-MSG-08 |
| Commentaire | — | Terme retiré (spec 15, 2026-10-17) : un échange entre l'opérateur et le BNIA est un **message** (§9) ; une observation du BNIA réservée à son personnel est une **note interne**. L'observation générale de la DSM est un message ; la note de transfert en région est supprimée. | p.107-111, spec 15 H-MSG-3, 4 |
| Note interne | `NoteInterne` | Note signée et datée du personnel du BNIA, jamais montrée à l'opérateur, tenue par le contexte de l'objet dans un carnet distinct des messages : pour une DSM, sur la DSM ou une de ses lignes. Remplace la note de contrôle d'une ligne, le commentaire de contrôle d'un justificatif et le commentaire interne. | p.26, 108, 111 ; spec 15 §3.7 |
| Journal des déclarations | `JournalDeclarations` | Trace des actions sur les DSM et le point de départ d'un opérateur, en ajout seul, sans valeurs avant et après. | p.29, spec 05 RG-DEC-23 |
| Justificatif | `Justificatif` | PDF prouvant un mouvement, avec son type de mouvement (achat, autres fabrications, vente négoce, vente export, réintégration) et son type de pièce (facture, DAE, avoir, déclaration de fabrication douanes), rattaché à la DSM ou à une ligne. Déposeur et date tracés. Un mouvement de contrat inscrit justifie aussi son type de mouvement, sans PDF (spec 06). | p.25-26, 95, spec 05 RG-DEC-26, spec 06 RG-CA-23 |
| DRM | `Drm` | Déclaration récapitulative mensuelle transmise aux douanes. Producteurs uniquement : un PDF par mois de la période (12 pour une DSM annuelle). | p.26, spec 05 RG-DEC-27 |
| DAE / DAI | `Dae`, `Dai` | Document d'accompagnement électronique ; document annuel d'inventaire. | p.96 |
| DIA | — | Déclaration d'inventaire annuel déposée aux douanes ; les pertes de la colonne 10 en reprennent le détail, sa date est portée par la DSM (le CDC écrit « DAI » en C.3.2.5). | p.109, modèles Excel, spec 05 H-DEC-12 |
| Signature de la DSM | `SignatureDsm` | Validation par l'opérateur d'un PDF signé via un service tiers. Hors périmètre du prototype (décision du 2026-10-12). | p.66 |
| Stock déclaré | — | Stock de fin de la dernière DSM validée d'un opérateur, ligne par ligne, daté de sa fin de période. À une fin de mois : celui de sa dernière DSM validée finie à cette date, dans la même année déclarative. | spec 05 RG-DEC-42, 44 |
| Stock estimé | `StockEstime` | Par ligne de DSM (clé, millésime ou dénomination, compte d'âge) : stock de fin de la dernière DSM validée, sinon stock initial du point de départ, vieilli à aujourd'hui, plus les achats, moins les ventes des mouvements de contrat datés après. Faits confirmés seuls : une vente conclue non retirée n'est pas déduite. Seul contrôle d'un contrat et d'une retiraison. Fins de travaux avec `distillation`. | spec 05 §5.2, spec 06 RG-CA-11 |
| Mouvement de contrat | `MouvementContrat` | Copie, dans `declarations`, d'une ligne de retiraison validée : un achat pour l'acheteur, une vente pour le vendeur, avec sa colonne de DSM (2 ou 3, 20 ou 21). S'inscrit dans la DSM de sa période et la justifie. Référence au contrat sans clé étrangère. | spec 06 RG-CA-21 à 23 |
| ~~Engagements~~, ~~stock disponible~~, ~~bilan prévisionnel~~ | — | Abandonnés le 2026-10-23 : le stock estimé, sur faits confirmés, est le seul contrôle (spec 06, H-CA-7). | spec 05 §5.2, spec 06 |

## 4. Contrats

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Contrat achat Armagnac | `ContratAchatArmagnac` | Achat d'Armagnac en fût entre deux opérateurs, ouvert au portail par l'une des **parties**, conclu par l'accord des deux, sans validation du BNIA, qui consulte (écart assumé au CDC, vision du 2026-10-23). États (`EtatContrat`) : `PROJET`, `CONCLU`, `EXECUTE`, `ANNULE`, `REFUSE`. Numéro `CA-AAAA-NNNN`. | p.30-31, spec 06 |
| Partie | `Partie` | Côté d'un contrat : `ACHETEUR` ou `VENDEUR`. Distinct du côté BNIA / opérateur de la messagerie et de la DSM. | spec 06 |
| Projet de contrat | état `PROJET` | Contrat en négociation : chaque partie le modifie (une version de plus), l'approuve ou le refuse. | spec 06 RG-CA-06 à 10 |
| Version du contrat | `VersionContrat` | Contenu daté d'un contrat (lignes, conditions), en ajout seul ; son auteur l'approuve. L'**écart** entre la version qu'une partie a approuvée et la courante lui est montré. | spec 06 RG-CA-07, 09 |
| Contrat conclu | état `CONCLU` | Les deux parties ont approuvé la même version ; daté de ce jour. Reçoit des retiraisons. | spec 06 RG-CA-08 |
| Contrat exécuté | état `EXECUTE` | Contrat dont la dernière retiraison a été validée, ou qu'une partie a déclaré sans plus de retiraison (révise la décision du 2026-10-12). | spec 06 RG-CA-15, 17 |
| Retiraison | `Retiraison` | Enlèvement d'une partie des lignes d'un contrat conclu, daté de l'enlèvement : déclarée par l'acheteur, validée (au plus tard le jour de l'enlèvement) par le vendeur, modifiable par qui doit valider ; dans le **reste à retirer**, couverte par le stock estimé du vendeur. Validée, elle devient deux mouvements de contrat. | C.4.1.1.1 p.113 amendé, spec 06 RG-CA-13 à 16 |
| Mercuriale | `Mercuriale` | Synthèse des prix des contrats conclus ou exécutés sur une période, par millésime ou par dénomination et compte d'âge : nombre de contrats, volume en hl AP, prix minimum, moyen pondéré et maximum (€ HT / hl AP). Au back-office. | p.48 amendé, spec 06 RG-CA-26 |
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
| Ligne de cotisation | `LigneCotisation` | Ligne du détail de la cotisation : sorties France (col. 14, 17, 18, 19, 21), sorties export (22, 23, 24), EDV pour un autre produit au marché intérieur (16) et à l'export (25) ; chacune avec son volume en hlAP, sa part non assujettie et sa part assujettie à la TVA. Sur la facture, chaque ligne de cotisation donne deux lignes article, l'une assujettie (S), l'autre non (E). | facture d'exemple, spec 05 RG-DEC-33, spec 08 §3.13 |
| Facture, pièce | `Facture`, `SensPiece` | Pièce légale émise par la lib `facturation` : facture ou avoir (sens `FACTURE` ou `AVOIR`), d'abord brouillon, puis émise, numérotée et figée. Une facture a toujours une origine ; plus de type de facture. | p.44-46, 124-126 ; spec 08 §3.1 |
| Avoir | `Facture` (sens `AVOIR`) | Pièce qui corrige des factures émises : de correction (rattaché à une ou plusieurs factures du même acheteur, ou au contrat du client sur une période, lignes libres, motif) ou d'annulation (une facture, ses lignes à l'identique, qui libère l'origine). Plus d'avoir « manuel » sans rattachement. | p.46, 125 ; spec 08 RG-FAC-22, 23 |
| État de règlement | `EtatZeendoc` | Statut de traitement/paiement restitué par Zeendoc d'après le n° de facture. Dans le prototype, « Payé » (`PAYEE`) d'une pièce transmise, porté par son suivi Zeendoc. Zeendoc n'est jamais la source de vérité des factures. | p.46, 83, 139 ; spec 08 §3.10 |
| Brouillon | — | Facture ou avoir pas encore émis : tout s'y modifie, il s'abandonne (supprimé avec son journal), sans numéro. | spec 08 RG-FAC-17, 18 |
| Facture vivante | — | Brouillon, ou facture émise non annulée ; une origine n'en a jamais plus d'une. | spec 08 RG-FAC-02 |
| Demande de facture | `DemandeFacture`, `DemandeFactures` | Ce qu'une source envoie à `facturation` pour créer un brouillon : origine, client ou acheteur libre, date de l'opération, période, condition de paiement, notes, lignes. | spec 08 §3.11 |
| Source | — | Contexte métier qui demande des factures et expose ses articles : `declarations`, puis `distillation` et `boutique`. | spec 08 §1 |
| Origine | `Origine` | Objet facturé chez la source (DSM, fin de travaux, commande, vente directe) : source, nature et son libellé, identifiant opaque, référence affichable. Remplace le type de facture. | p.124 ; spec 08 §3.4 |
| Article, catalogue d'articles | `Article`, `CatalogueArticles` | Ce qui se facture, identifié par (source, code), avec désignation, unité, prix et catégorie de TVA à une date ; chaque source expose les siens par le port `CatalogueArticles`. Remplace le « référentiel produits » de la facturation. | p.44, 124 ; spec 08 §3.11 |
| Ligne article, ligne informative, groupe | `LigneArticle`, `LigneInformative`, `LigneGroupe` | Lignes ordonnées d'une pièce : un article et sa quantité ; un texte sans montant ; un titre et le sous-total de ses lignes (un niveau). | p.125 ; spec 08 §3.2 |
| Catégorie de TVA | `CategorieTva` | Code EN 16931 d'une ligne article (`S`, `Z`, `E`, `O`), avec son taux et, pour `E` et `O`, son motif. À ne pas confondre avec la classe de TVA de `referentiels`. | spec 08 §3.3 |
| Récapitulatif de TVA | — | TVA d'une pièce, calculée une fois par (catégorie, taux) sur la somme des HT. | spec 08 RG-FAC-05 |
| Acheteur | `Acheteur`, `ClientRef` | Destinataire de la pièce, recopié : catégorie (professionnel ou particulier), nom, SIREN, n° de TVA, adresses, e-mail ; avec un client de l'hôte (un opérateur, par son n° BNIA) ou libre. | p.123 ; spec 08 §3.5 |
| Vendeur | `Vendeur` | Le BNIA, paramétré dans la facturation et recopié sur chaque pièce émise, avec ses mentions (pénalités, indemnité de 40 €, escompte). | spec 08 §3.7 |
| Condition de paiement | `ConditionPaiement` | Nombre de jours, avec ou sans fin de mois (« 30 jours fin de mois »), d'où se calcule l'échéance à l'émission. | spec 08 §3.6 |
| Journal de la pièce | — | Historique en ajout seul d'une facture ou d'un avoir, côté vendeur ou client (consultation au portail), avec les notes internes, jamais imprimées. | spec 08 §3.9 |
| Transmission | `SuiviZeendoc` | Suivi d'une pièce émise dans Zeendoc : transmise (« Transmis », une seule fois), puis payée (« Payé »). Simulé dans le prototype, porté par la pièce ; les états de dépôt `EN_ATTENTE`, `REJETEE`, `EN_ANOMALIE` sont hors prototype. | p.83 ; spec 08 §3.10 |
| Vente directe | — | Vente saisie dans `boutique` hors commande du portail ; remplace la facture manuelle. | p.45 ; analyse `facturation` |
| Ligne vendue | `LigneVendue` | Ligne article d'une pièce émise, vue par les ventes consolidées (avoir en négatif) et par `statistiques`. | spec 08 RG-FAC-30, 31 |

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

## 9. Messagerie et notifications

| Terme | Code | Définition | Réf. |
|---|---|---|---|
| Conversation | `Conversation` | Fil des messages échangés entre un opérateur et le BNIA sur un même sujet : un objet métier ou un sujet libre. Sans destinataire nommé : un message s'adresse à l'autre côté. | spec 15 §3.1 |
| Conversation d'un objet | `ObjetMetier` | Conversation d'une DSM, d'une facture émise (puis d'un contrat, d'une demande de certificat) : au plus une par objet, née au premier message, partagée par tout l'opérateur, supprimée avec l'objet. | spec 15 RG-MSG-01, 04, 07 |
| Conversation libre | `SujetLibre` | Conversation ouverte par un utilisateur du portail, avec un titre, sur le sujet de son choix ; vue de lui seul dans son opérateur et de tout le BNIA ; pièces jointes permises. | spec 15 RG-MSG-02, 12 |
| Message | `Message` | Texte écrit par un utilisateur, côté opérateur ou BNIA, dans une conversation ; modifiable et supprimable par son auteur, sauf un message **protégé** (le motif d'une demande de compléments), qui ne se supprime pas. | spec 15 §3.4, RG-MSG-06 |
| Côté | `Cote` | `OPERATEUR` ou `BNIA`, selon l'opérateur actif de l'acteur. Un message est lu ou non lu par côté, sans dire qui l'a lu. | spec 15 RG-MSG-03, 09 |
| Nouveaux messages | — | Messages non lus de mon côté dans les conversations que je peux lire ; comptés au tableau de bord. Afficher une conversation les lit ; « marquer non lu » en remet un. | spec 15 RG-MSG-10, 11 |
| Objet conversable | `ObjetsConversables`, `TypeObjet` | Objet métier dont le contexte décrit à `messagerie` l'opérateur et le libellé. | spec 15 §3.2, §3.5 |
| Notification push | `Notification`, `MessagePush` | Alerte du système affichée sur les appareils abonnés de ses destinataires, même application fermée : un titre, une phrase sans montant ni volume, un lien. S'ajoute à l'e-mail. Distincte d'un message, écrit par une personne. | spec 11 §3.2, RG-NOT-03 |
| Abonnement push | `Abonnement` | Ce qui permet d'envoyer des notifications push à un appareil : un par appareil où un utilisateur les a activées, depuis Mon compte ; retiré à sa déconnexion. | spec 11 §3.1, RG-NOT-01, 05 |

## 10. Systèmes

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
