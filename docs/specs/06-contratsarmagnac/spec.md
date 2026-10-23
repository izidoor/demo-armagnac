# 06 : contratsarmagnac

> Contexte `contratsarmagnac` (étape 6 de `docs/contextes.md`). Statut : **implémentée** (2026-10-23 ; spec terminée le même jour).
> Références : `p.N` = page du CDC v1.1, `AGI p.N` = page de la spec de reprise AGI.
> `H-CA-n` : hypothèse retenue, révisable. `Q-CA-n` : question ouverte, suivie dans cette spec (§10).
> **Écart assumé au CDC** : la [vision du 2026-10-23](vision.md) prime sur le CDC (§4.5, §5.5, C.4) ;
> l'[analyse du 2026-10-23](analyse.md) en tire le cadrage, les cas d'usage, les objets et les flux, acquis
> ici. **Objectif** : la démonstration au BNIA du 2026-10-27, sur téléphone.

## Sommaire

- [1. Objet](#1-objet)
- [2. Périmètre](#2-périmètre)
  - [2.1 Dans le contexte](#21-dans-le-contexte)
  - [2.2 Hors du contexte](#22-hors-du-contexte)
- [3. Modèle](#3-modèle)
  - [3.1 Contrat d'achat Armagnac (`ContratAchatArmagnac`, agrégat)](#31-contrat-dachat-armagnac-contratachatarmagnac-agrégat)
  - [3.2 Version du contrat (`VersionContrat`, entité, ajout seul)](#32-version-du-contrat-versioncontrat-entité-ajout-seul)
  - [3.3 Ligne du contrat (`LigneContrat`, objet-valeur)](#33-ligne-du-contrat-lignecontrat-objet-valeur)
  - [3.4 Retiraison (`Retiraison`, entité du contrat)](#34-retiraison-retiraison-entité-du-contrat)
  - [3.5 Journal du contrat (`JournalContrat`, hors agrégat, ajout seul)](#35-journal-du-contrat-journalcontrat-hors-agrégat-ajout-seul)
  - [3.6 Module « Contrats » (révision de la spec 02 §3.2.7)](#36-module--contrats--révision-de-la-spec-02-327)
  - [3.7 Livré dans `declarations` (révision de la spec 05)](#37-livré-dans-declarations-révision-de-la-spec-05)
- [4. Règles de gestion](#4-règles-de-gestion)
- [5. Cas d'usage](#5-cas-dusage)
  - [5.1 Écriture](#51-écriture)
  - [5.2 Lecture](#52-lecture)
- [6. Écrans](#6-écrans)
  - [6.1 Back-office](#61-back-office)
  - [6.2 Portail](#62-portail)
- [7. Données](#7-données)
  - [7.1 Démonstration (profil `demo`)](#71-démonstration-profil-demo)
  - [7.2 Reprise AGI](#72-reprise-agi)
- [8. Décisions d'architecture](#8-décisions-darchitecture)
- [9. Hypothèses](#9-hypothèses)
- [10. Questions ouvertes](#10-questions-ouvertes)
- [11. Tests attendus](#11-tests-attendus)
- [12. Lots d'implémentation](#12-lots-dimplémentation)
- [Journal](#journal)

## 1. Objet

Le contexte tient le **contrat d'achat Armagnac** entre deux opérateurs, un acheteur et un vendeur, sur de
l'Armagnac en fût, de la négociation à l'exécution : une partie ouvre un projet, l'autre l'approuve, le
modifie ou le refuse ; conclu, le contrat reçoit des retiraisons, déclarées par l'acheteur et validées par
le vendeur. Il livre avec lui ce que le contrat met en mouvement, dans `declarations` : le **stock estimé**
de chaque opérateur, seul contrôle d'un projet et d'une retiraison, et la **DSM préremplie** par les
retiraisons validées, qui en sont aussi le justificatif. Le BNIA consulte les contrats et les
**mercuriales** (analyse du 2026-10-23).

Il ne fait ni validation par le BNIA, ni pluri-annuel, ni proposition d'engagement, ni contrat vin, ni
facture. Sa valeur : un stock suivi entre deux DSM sur des faits confirmés, une DSM qui se remplit seule,
et les prix réels du marché pour le BNIA.

Sources : vision du 2026-10-23 et exemple de contrat CA-05 (annexe de la vision) · §4.5 et §4.5.1 p.30-31
· §5.5 p.67-68 · C.4.1.1 p.112-114 · AGI 3.1 p.16-17 · analyse du 2026-10-23.
Usages relevés ailleurs : §4.4.5 p.25-26 (contrat, justificatif d'un achat ou d'une vente négoce) · §4.4.7
p.27 et §5.4.7 p.65 (rattachement des lignes de la DSM) · §4.1 p.12 et §5.2 p.61 (tableaux de bord) · §4.2
p.15 et §5.13 p.78 (droits du module) · §4.10.4 p.48 (consolidation des achats) · §5.12 p.77
(notifications) · annexe B p.98 (compte d'âge) · glossaire p.95 (justificatif) · AGI 3.2 p.17-18
(conditions de retiraison d'un pluri-annuel : en une ou plusieurs fois, au plus tard) · §7 p.85-87
(reprise).

## 2. Périmètre

### 2.1 Dans le contexte

| Famille | Élément | Code (ADR-001) | Source |
|---|---|---|---|
| Contrat | Contrat d'achat Armagnac : versions du contenu, lignes, approbations des deux parties, états (projet, conclu, exécuté, annulé, refusé) | `ContratAchatArmagnac` | §4.5.1 p.30, C.4.1.1 p.112-113, analyse §3 |
| Contrat | Retiraisons : déclarées par l'acheteur, validées par le vendeur, versions | `Retiraison` | analyse §3 (amende C.4.1.1.1 p.113) |
| Contrat | Journal du contrat, en ajout seul | `JournalContrat` | C.4.1.1.3 p.113-114, §5.5.6 p.68 |
| Contrat | Édition PDF au formulaire CA-05 allégé | — | §4.5.1.4 p.31, C.4.1.1.4 p.114, vision |
| Contrat | E-mails à l'autre partie (projet, modification, retiraison, annulation, clôture) | — | §5.12 p.77, vision |
| Suivi | Listes par état, au portail et au back-office ; onglet « Contrats » de la fiche opérateur | — | §4.5.1.1 p.30, §5.5.1 p.67, décision du 2026-10-23 |
| Suivi | Cartes des tableaux de bord : « Contrats à approuver » (portail), « Contrats en attente d'approbation » (portail et back-office) | — | §5.2 p.61, §4.1 p.12 amendé, décision du 2026-10-23 |
| Suivi | Mercuriales au back-office | `Mercuriale` | §4.10.4 p.48 amendé, vision |
| Livré dans `declarations` | Mouvements de contrat (copie des retiraisons validées) et stock estimé, lu par ce contexte et affiché au portail | `MouvementContrat`, `StockEstime` | analyse §1 et §4, spec 05 §5.2 |
| Livré dans `declarations` | Inscription d'une retiraison validée dans la DSM de sa période, qui justifie son mouvement | — | §4.4.5 p.25-26, analyse §1, décision du 2026-10-23 |

### 2.2 Hors du contexte

| Élément décrit dans le CDC | Contexte retenu | Motif |
|---|---|---|
| Validation et invalidation par le BNIA (§4.5.1.3 p.31, §5.5.3 p.67) | aucun | Supprimées : le contrat est conclu par l'accord des parties, le BNIA consulte (analyse du 2026-10-23, H-CA-1) |
| Saisie d'un contrat par le BNIA (§4.5 p.30) | aucun | Saisie au portail par une partie (analyse du 2026-10-23) |
| Pluri-annuels et propositions d'engagement Armagnac (§4.5.3-4.5.4 p.32-33, §5.5.4-5.5.5 p.67-68) | hors périmètre | Vision ; en arbitrage au BNIA |
| Contrats, pluri-annuels et propositions vin (§4.5.2, 4.5.5-4.5.6 p.31-35, §5.6 p.69) | `contratsvin` | Hors périmètre de la vision |
| Échéancier, rattachement à un pluri-annuel, préavis de rupture, visa du BNIA, courtier (« par l'intermédiaire de …, courtier à … », « Vu le courtier »), mentions finales (CA-05, C.4.1.1.1 p.113, AGI 3.1.1 p.16) | aucun | Retirés du formulaire (vision ; courtier : décision du 2026-10-23) |
| Code payeur (C.4.1.1.1 p.112) | aucun | Aucune facture ne naît d'un contrat |
| Fins de travaux de distillation dans le stock estimé (vision) | `distillation` | Non implémenté : terme nul aujourd'hui (analyse du 2026-10-23) |
| Exports en bouteilles des négociants dans le stock estimé | `exportation` | Hors contexte ; le grain du stock estimé (bois et verre) le permettra (décision du 2026-10-23) |
| Conversation du contrat et notes internes du BNIA (fiche `contextes.md` §6) | aucun | Les versions et leur écart portent la négociation, le journal trace, le BNIA consulte (décision du 2026-10-23) |
| Consolidation statistique des achats (§4.10.4 p.48) | `statistiques`, plus tard | Les mercuriales sont ici ; `statistiques` pourra les lire |
| Notifications et relances génériques (§5.12 p.77, C.14 p.136) | `notifications` (non implémenté) | Le contexte envoie ses propres e-mails par un port, comme `operateurs` |
| Reprise des contrats d'InteGraal et de pro.armagnac.fr (§7 p.85-87, AGI 3.1) | aucun | Reprise hors périmètre du prototype |

## 3. Modèle

Le modèle part des objets et invariants de l'analyse du 2026-10-23 (§3). Volumes en litres d'alcool pur
(`VolumeAlcoolPur`, ADR-009) dans le domaine ; affichés en hl AP ou hl.l,cl selon l'écran.

### 3.1 Contrat d'achat Armagnac (`ContratAchatArmagnac`, agrégat)

| Champ | Type | Règle | Source |
|---|---|---|---|
| numéro | texte `CA-AAAA-NNNN` | Attribué à l'ouverture : année de l'ouverture, rang dans l'année ; unique, jamais réattribué (RG-CA-06) | C.4.1.1.1 p.112 |
| acheteur, vendeur | référence `Operateur` (identifiant, clé étrangère, ADR-011) | Deux opérateurs distincts, ni l'un ni l'autre l'opérateur BNIA ; aucune restriction de type (RG-CA-02) | §4.5.1.2 p.30, analyse du 2026-10-23 |
| ouvert par | `Partie` : `ACHETEUR`, `VENDEUR` | La partie qui a ouvert le projet | §5.5.2 p.67 |
| état | `EtatContrat` : `PROJET`, `CONCLU`, `EXECUTE`, `ANNULE`, `REFUSE` | `PROJET` à l'ouverture ; transitions RG-CA-07 à 10 et 17 | analyse §3 |
| versions | liste de `VersionContrat` (§3.2) | Au moins une ; ajout seul ; la dernière est la version courante | analyse §3 |
| approbations | au plus une par côté : numéro de version approuvée, auteur (identifiant et copie du nom), date | RG-CA-07, 08 | C.4.1.1.3 p.113, analyse §3 |
| date du contrat | date | Date de la conclusion ; vide en projet | CA-05 (« Établi … le »), C.4.1.1.1 p.112 |
| campagne | texte `AAAA-AAAA` | Calculée : du 1er avril au 31 mars, à la date du contrat, sinon à la date du jour (H-CA-6) | CA-05 (« Campagne 2026-2027 »), AGI 3.1.1 p.16 |
| clôture | motif (`ANNULATION`, `SANS_PLUS_DE_RETIRAISON`), auteur, date | Contrat `ANNULE` ou `EXECUTE` par clôture (RG-CA-17) | vision |
| retiraisons | liste de `Retiraison` (§3.4) | Seulement sur un contrat `CONCLU` (RG-CA-13) | analyse §3 |

**Écarts au CDC** (analyse du 2026-10-23) : plus de validation ni de statut BNIA, de visa, de type de
contrat, de code payeur, de rattachement à un pluri-annuel ou à une proposition (C.4.1.1.1 p.112-113) ; la
saisie validée reste modifiable jusqu'à la conclusion (§5.5.6 p.68).

### 3.2 Version du contrat (`VersionContrat`, entité, ajout seul)

| Champ | Type | Règle | Source |
|---|---|---|---|
| numéro | entier | 1 à l'ouverture, +1 à chaque modification | analyse §3 |
| auteur | côté, identifiant de l'utilisateur et copie de son nom, date | Celui qui a ouvert ou modifié ; il approuve sa version (RG-CA-07) | C.4.1.1.3 p.113 |
| lignes | liste de `LigneContrat` (§3.3) | Au moins une (RG-CA-03) | C.4.1.1.2 p.113 |
| caractéristiques complémentaires | texte long facultatif | Tient lieu du « commentaire » de C.4.1.1.1 : convenu entre les parties, imprimé (décision du 2026-10-23) | CA-05 §2, C.4.1.1.1 p.113 |
| conditions et calendrier de retiraison | texte long facultatif | Imprimé | C.4.1.1.1 p.113, CA-05 §3 |
| à retirer avant le | date facultative | Affichée dans les listes, sans effet sur le stock | analyse du 2026-10-23, AGI 3.2 p.17-18 |
| paiement | solde `COMPTANT` ou `TRENTE_JOURS_FIN_DE_MOIS` ; acompte facultatif, en % | RG-CA-03 | CA-05 §4, AGI 3.1.1 p.16 |

### 3.3 Ligne du contrat (`LigneContrat`, objet-valeur)

| Champ | Type | Règle | Source |
|---|---|---|---|
| appellation | `Appellation` (ADR-009) | Obligatoire ; le PDF coche les appellations des lignes (décision du 2026-10-23) | C.4.1.1.2 p.113, CA-05 |
| millésime ou dénomination | `Millesime`, ou texte court | L'un ou l'autre, jamais les deux ; toujours en bois (RG-CA-05) | C.4.1.1.2 p.113, vision |
| compte d'âge | `CompteDAge` (ADR-009) | Millésime : calculé (RG-CA-04) ; dénomination : saisi | annexe B p.98, C.4.1.1.2 p.113 |
| volume | hl, 2 décimales | Strictement positif | CA-05 (30,00), décision du 2026-10-23 |
| degré | % vol., 2 décimales | Strictement positif, au plus 100 | CA-05 (47,00) |
| prix | € HT / hl AP, 2 décimales | Strictement positif ; seul prix saisi (décision du 2026-10-23) | C.4.1.1.2 p.113, CA-05 |
| volume AP, prix € / l vol, montant HT | calculés | RG-CA-04 | C.4.1.1.2 p.113 |
| grain | calculé : `MB` + millésime, ou `NB` + dénomination + compte d'âge borné à `20+` | Comparé au stock estimé (RG-CA-05, 11) | analyse §3, RG-DEC-11, 12 |

### 3.4 Retiraison (`Retiraison`, entité du contrat)

| Champ | Type | Règle | Source |
|---|---|---|---|
| numéro | entier | Rang dans le contrat | analyse §3 |
| état | `EtatRetiraison` : `A_VALIDER_VENDEUR`, `A_VALIDER_ACHETEUR`, `VALIDEE` | RG-CA-13 à 16 | analyse §3 |
| versions | liste ajout seul : numéro, auteur (côté, identifiant, copie du nom), date ; **date d'enlèvement**, **dernière** (oui ou non), lignes (ligne du contrat, volume en hl ; volume AP calculé au degré de la ligne) | RG-CA-13, 14 | vision, décision du 2026-10-23 |
| approbations | au plus une par côté, sur un numéro de version | Validée quand les deux portent sur la version courante (RG-CA-15) | analyse §3 |

### 3.5 Journal du contrat (`JournalContrat`, hors agrégat, ajout seul)

| Champ | Type | Règle |
|---|---|---|
| date | date-heure | |
| acteur | identifiant de l'utilisateur, sans clé étrangère, et copie de son nom ; côté | ADR-011 |
| contrat | référence `ContratAchatArmagnac` | |
| action | `OUVERTURE`, `MODIFICATION`, `APPROBATION`, `REFUS`, `CONCLUSION`, `RETIRAISON_DECLAREE`, `RETIRAISON_MODIFIEE`, `RETIRAISON_VALIDEE`, `RETIRAISON_RETIREE`, `EXECUTION`, `ANNULATION`, `CLOTURE`, `EDITION_PDF` | RG-CA-19 |
| détail | numéro de version ou de retiraison | |

### 3.6 Module « Contrats » (révision de la spec 02 §3.2.7)

Colonne ajoutée au tableau rôles × modules : `OBSERVATEUR` lit ; `AGENT` et `ADMIN` gèrent. Exception à
RG-OPE-32 : un rôle tenu dans l'opérateur BNIA ne donne que la lecture (RG-CA-01, H-CA-1).

### 3.7 Livré dans `declarations` (révision de la spec 05)

| Objet | Champs | Règle |
|---|---|---|
| `MouvementContrat` (nouveau, schéma `declarations`) | opérateur ; sens (`ACHAT`, `VENTE`) ; date d'enlèvement ; grain (clé, millésime ou dénomination, compte d'âge) ; volume AP ; colonne (2, 3, 20 ou 21) ; contrat (identifiant et numéro copiés), retiraison (identifiant), autre partie : **sans clé étrangère** (analyse §4) ; DSM d'inscription (vide tant que non inscrit) | RG-CA-21 à 23 |
| `StockEstime` (vue) | par opérateur et ligne de DSM : base, achats, ventes, stock estimé ; date de la base | RG-CA-11 |
| `DeclarationsLecture` (partie publiée, ADR-017) | + stock estimé d'un opérateur | RG-CA-11 |
| `RetiraisonValidee` (partie publiée, événement reçu) | contrat (identifiant, numéro), retiraison, date d'enlèvement, vendeur, acheteur, lignes (grain, volume AP) | remplace `ContratValide`, `ContratModifie`, `RetiraisonEffectuee` (A-DEC-1) |

## 4. Règles de gestion

| Id | Règle | Source | Lot |
|---|---|---|---|
| RG-CA-01 | Lire un contrat exige d'en être partie, par son opérateur actif, avec la lecture du module « Contrats », ou d'être de l'opérateur BNIA (tout rôle). Agir exige la gestion du module dans l'opérateur partie ; l'opérateur BNIA n'agit sur aucun contrat. | analyse du 2026-10-23, RG-OPE-31, 32, H-CA-1 | 1 |
| RG-CA-02 | Ouvrir un projet : l'opérateur actif se désigne acheteur ou vendeur et choisit l'autre partie par la recherche restreinte (RG-OPE-37) : un autre opérateur, actif, qui n'est pas le BNIA. Aucun type d'opérateur n'est exclu. | §5.5.2 p.67, analyse et décision du 2026-10-23 | 1 |
| RG-CA-03 | Un contenu a au moins une ligne. Ligne : appellation ; millésime ou dénomination non vide, pas les deux ; compte d'âge saisi pour une dénomination ; volume (hl) et prix (€ HT / hl AP) strictement positifs, degré dans ]0 ; 100], à 2 décimales. Paiement : solde comptant ou à 30 jours fin de mois, acompte facultatif dans ]0 ; 100[ %. « À retirer avant le » facultatif. | C.4.1.1 p.113, CA-05, AGI 3.1 p.16-17, décision du 2026-10-23, H-CA-10 | 1 |
| RG-CA-04 | Compte d'âge d'une ligne millésimée : annexe B, à la date de la version (2009 donne 16 du 1er avril 2026 au 31 mars 2027). Volume AP (L) = volume (hl) × degré ; prix € / l vol = prix × degré / 10 000 ; montant HT = volume AP (hl) × prix ; total du contrat. Arrondis au demi supérieur, 2 décimales. | annexe B p.98, C.4.1.1.2 p.113, CA-05, H-CA-10 | 1 |
| RG-CA-05 | Une ligne est toujours en bois : son grain est `MB` + millésime, ou `NB` + dénomination (comparée comme RG-DEC-11) + compte d'âge borné à `20+` (RG-DEC-12). L'appellation n'entre pas dans le grain. Le compte d'une ligne `NB` est celui de la conclusion ; une retiraison après un 1er avril franchi vise le compte vieilli (+1 par 1er avril, `20+` au plus). | vision, analyse du 2026-10-23, RG-DEC-11, 12, 14, H-CA-11 | 1 |
| RG-CA-06 | Ouvrir crée le contrat en `PROJET`, son numéro et la version 1, approuvée par son auteur ; un e-mail part à l'autre partie (RG-CA-18). | §5.5.2 p.67, vision | 1 |
| RG-CA-07 | Tant que le contrat est en projet, chaque partie peut le modifier : une nouvelle version, approuvée par son auteur seul ; l'approbation de l'autre partie tombe. | vision, §5.5.6 p.68 amendé (H-CA-8) | 1 |
| RG-CA-08 | La partie dont l'approbation manque approuve la version courante. Les deux approbations sur la même version : le contrat est `CONCLU`, daté du jour ; sa campagne est fixée. | vision, §5.5.3 p.67 amendé, H-CA-1 | 1 |
| RG-CA-09 | À la partie qui doit approuver, le contrat montre l'écart entre la dernière version qu'elle a approuvée (la version 1 si elle n'en a approuvé aucune) et la courante : lignes ajoutées ou retirées, champs changés (avant, après), et l'auteur de chaque modification. | vision (« Important ») | 1 |
| RG-CA-10 | La partie qui doit approuver peut refuser le projet, définitivement : `REFUSE`. L'auteur de la version courante ne refuse pas, il la modifie. | vision | 1 |
| RG-CA-11 | Stock estimé d'un opérateur, par ligne de DSM (clé, millésime ou dénomination, compte d'âge) : la base, plus les achats, moins les ventes de ses mouvements de contrat datés après la date de la base, y compris ceux datés dans le futur. Base : stock de fin de sa dernière DSM validée (RG-DEC-42), sinon stock initial de son point de départ en vigueur, vieilli à aujourd'hui (RG-DEC-14 : `MB` recalculé, `NB` +1 par 1er avril franchi, `20+`) ; sans l'une ni l'autre, nul. Sans fin de travaux (`distillation`, non implémenté). | vision, analyse du 2026-10-23, spec 05 §5.2, H-CA-7 | 1 |
| RG-CA-12 | Le vendeur ne peut ouvrir, modifier ni approuver un contenu dont une ligne dépasse son stock estimé au grain, diminué des autres lignes du même grain du contrat. Le contrôle est montré en direct pendant sa saisie et refait à l'enregistrement ; au détail d'un projet que son stock ne couvre pas, le vendeur voit une alerte et « Approuver » est désactivé (décision du 2026-10-23). L'acheteur n'a aucun contrôle de stock sur le contenu. | vision | 1 |
| RG-CA-13 | Sur un contrat `CONCLU`, l'acheteur déclare une retiraison : date d'enlèvement au plus tôt aujourd'hui, dernière ou non, des lignes du contrat avec un volume strictement positif, au plus le reste à retirer de la ligne (volume du contrat moins retiraisons validées ou en cours) ; le stock estimé du vendeur doit la couvrir, et le refus ne montre aucun chiffre à l'acheteur. État `A_VALIDER_VENDEUR` ; e-mail au vendeur. | vision, décision du 2026-10-23, H-CA-3 | 2 |
| RG-CA-14 | La partie qui doit valider une retiraison peut la modifier (date, dernière, lignes, mêmes contrôles) : nouvelle version, approuvée par elle seule, à valider par l'autre (`A_VALIDER_ACHETEUR` ou `A_VALIDER_VENDEUR`) ; l'écart lui est montré comme en RG-CA-09. | vision | 2 |
| RG-CA-15 | La partie qui doit valider une retiraison la valide si sa date d'enlèvement n'est pas passée (validation au plus tard le jour de l'enlèvement) et si le stock estimé du vendeur la couvre : `VALIDEE`, `RetiraisonValidee` publié dans la transaction (ADR-013). Dernière, le contrat est `EXECUTE`. | vision, décision du 2026-10-23, H-CA-3 | 2 |
| RG-CA-16 | L'acheteur retire une retiraison non validée : elle est supprimée, le journal le garde. | décision du 2026-10-23 (souplesse du prototype) | 2 |
| RG-CA-17 | Sur un contrat `CONCLU`, une partie l'annule (`ANNULE`) ou déclare qu'il n'aura plus de retiraison (`EXECUTE`) ; ses retiraisons non validées sont supprimées, les validées restent. E-mail à l'autre partie. | vision | 2 |
| RG-CA-18 | Chaque action qui attend l'autre partie ou la concerne (ouverture, modification, approbation, conclusion, refus, retiraison déclarée, modifiée ou validée, annulation, clôture) envoie un e-mail à ses membres `AGENT` et `ADMIN` actifs, avec le lien vers le contrat. | §5.12 p.77, vision | 1 |
| RG-CA-19 | Le journal garde chaque action du §3.5, acteur et nom copiés ; il se lit avec le contrat. | C.4.1.1.3 p.113-114, §5.5.6 p.68 | 1 |
| RG-CA-20 | L'édition PDF reprend le formulaire CA-05 sans visa, mentions finales, échéancier, rattachement, préavis ni courtier : en-tête et campagne, numéro, parties (identité, adresse, téléphone, n° BNIA, SIRET, CVI du vendeur), lignes et calculs, appellations cochées, caractéristiques complémentaires et mentions fixes, retiraison, paiement, « S'agissant d'un contrat ponctuel, celui-ci est ferme. », signatures de l'acheteur et du vendeur (date et auteur des approbations). Un projet porte « Projet, non conclu ». | §4.5.1.4 p.31, C.4.1.1.4 p.114, vision, CA-05 | 3 |
| RG-CA-21 | À réception de `RetiraisonValidee`, `declarations` crée par ligne un mouvement `ACHAT` pour l'acheteur et un `VENTE` pour le vendeur. Colonne : achat en 2 si le vendeur est `PRODUCTEUR`, en 3 sinon ; vente en 20 si l'acheteur est en région délimitée, en 21 sinon. | analyse du 2026-10-23, §4.4.3 p.23 | 2 |
| RG-CA-22 | Un mouvement s'inscrit dans la DSM de son opérateur dont la période couvre sa date : aussitôt si elle est `BROUILLON` ou `COMPLEMENTS_DEMANDES`, sinon à son initiation (complète RG-DEC-14). Son volume s'ajoute à la colonne, sur la ligne de son grain, ajoutée au besoin (`AJOUT`, colonne 1 à 0, corrigeable). Une colonne grisée pour le déclarant (achat d'un producteur, RG-DEC-16) n'inscrit rien. | analyse et décisions du 2026-10-23 | 2 |
| RG-CA-23 | Un type de mouvement d'une DSM qui porte un mouvement de contrat inscrit a son justificatif (RG-DEC-29) : l'onglet Pièces le montre (« Contrat CA-… , retiraison n »), avec le lien vers le contrat, sans PDF. | §4.4.5 p.25-26, glossaire p.95, décision du 2026-10-23, H-CA-2 | 2 |
| RG-CA-24 | Une DSM ne s'envoie qu'à partir du lendemain de la fin de sa période (révise RG-DEC-20). | décision du 2026-10-23, H-CA-4 | 2 |
| RG-CA-25 | Listes : au portail, par onglets « À approuver » (un projet ou une retiraison attend ma partie), « En négociation » (attend l'autre partie), « Conclus », « Exécutés », « Annulés ou refusés » ; au back-office, tous les contrats par état, filtrables par opérateur et par numéro ; onglet « Contrats » de la fiche opérateur. Cartes : « Contrats à approuver » (portail), « Contrats en attente d'approbation » (portail : attend l'autre partie ; back-office : tous les projets), chacune vers sa liste. | §4.5.1.1 p.30, §5.5.1 p.67, §4.1 p.12, §5.2 p.61, décision du 2026-10-23 | 1 |
| RG-CA-26 | Mercuriales : les contrats `CONCLU` ou `EXECUTE` dont la date du contrat est dans la période choisie (par défaut les 12 derniers mois), regroupés par millésime ou par dénomination et compte d'âge : nombre de contrats, volume en hl AP, prix minimum, moyen pondéré par le volume AP et maximum (€ HT / hl AP). | §4.10.4 p.48 amendé, vision | 3 |

## 5. Cas d'usage

### 5.1 Écriture

| Cas d'usage | Objets | RG |
|---|---|---|
| UC-CA-1 Ouvrir un projet | `ContratAchatArmagnac` | 01 à 06, 11, 12, 18, 19 |
| UC-CA-2 Voir l'écart, approuver, modifier ou refuser | `ContratAchatArmagnac` | 01, 03 à 05, 07 à 12, 18, 19 |
| UC-CA-3 Déclarer une retiraison ; la retirer | `ContratAchatArmagnac`, `Retiraison` | 01, 11, 13, 16, 18, 19 |
| UC-CA-4 Valider ou modifier une retiraison | `Retiraison`, `MouvementContrat`, `DeclarationStocksMouvements` | 01, 11, 14, 15, 18, 19, 21 à 23 |
| UC-CA-5 Annuler, ou déclarer sans plus de retiraison | `ContratAchatArmagnac` | 01, 17 à 19 |
| UC-CA-7 Initier une DSM, mouvements inscrits ; envoyer après la fin de période | `DeclarationStocksMouvements` | 22 à 24 |
| Éditer le PDF | — | 01, 19, 20 |

### 5.2 Lecture

| Besoin | Consommateur probable |
|---|---|
| UC-CA-6 Stock estimé d'un opérateur (`DeclarationsLecture`) | `contratsarmagnac` (RG-CA-12, 13, 15) ; page « Stocks » du portail (RG-CA-11) |
| UC-CA-8, 9 Listes, détail, journal, cartes (RG-CA-01, 09, 19, 25) | portail, back-office, tableaux de bord |
| UC-CA-10 Mercuriales (RG-CA-26) | back-office ; `statistiques`, plus tard |

Ce contexte ne publie qu'un événement, `RetiraisonValidee`, dont le type est dans la partie publiée de
`declarations` (ADR-013, ADR-017) ; il n'a pas de partie publiée en lecture.

## 6. Écrans

Pensés d'abord pour le téléphone, selon les principes du passage en PWA : cartes, formulaires en pleine page,
actions dans « Plus ».

### 6.1 Back-office

- Menu « Contrats » : **Contrats** (listing de tous les contrats, onglets par état, filtres opérateur et
  numéro ; détail en lecture : lignes, versions et écart, retiraisons, journal, PDF) ; **Mercuriales**
  (période, tableau par millésime ou dénomination et compte d'âge).
- Fiche opérateur, onglet « Contrats » : le listing filtré sur l'opérateur, comme acheteur ou vendeur.
- Tableau de bord : carte « Contrats en attente d'approbation », valeur réelle.

### 6.2 Portail

- Menu « Contrats » (module « Contrats ») : la liste en onglets (RG-CA-25), « Nouveau contrat ».
- **Nouveau contrat**, en pleine page : « Je suis l'acheteur / le vendeur », l'autre partie (recherche
  restreinte, en pleine page sur un téléphone, sans l'opérateur actif) ; puis les lignes : le vendeur choisit une ligne de son stock estimé sous bois (millésime ou
  dénomination et compte d'âge remplis, volume AP restant affiché, dépassement signalé en direct) ;
  l'acheteur saisit millésime, ou dénomination et compte d'âge ; volume, degré, prix, calculs en direct ;
  puis conditions (caractéristiques, retiraison, « à retirer avant le », paiement).
- **Détail du contrat** : état et ce qu'il attend, parties, lignes et total ; quand ma partie doit approuver,
  l'écart surligné (avant barré, après) et les actions Approuver, Modifier, Refuser ; au vendeur, l'alerte
  d'un stock qui ne couvre pas le projet (« Approuver » désactivé) ; refusé : « Refusé par …, le … à … » ; Retiraisons (reste à
  retirer par ligne, « Déclarer une retiraison » pour l'acheteur, retiraison à valider avec son écart) ;
  Journal ; « PDF » en en-tête (dans « Plus » une fois conclu, avec Annuler et Plus de retiraison).
- « Déclarations > Stocks déclarés » : colonne du stock estimé à côté du stock déclaré, par ligne (UC-CA-6).
- DSM : lignes ajoutées par un contrat marquées « Contrat CA-… » ; onglet Pièces : le justificatif contrat.
- Tableau de bord : carte « Contrats à approuver », valeur réelle.

## 7. Données

### 7.1 Démonstration (profil `demo`)

| Données | Ce qu'elles montrent | Lot |
|---|---|---|
| Comptes existants : vendeur Jeanne Durand (`admin@producteur.fr`, Maison Durand, Condom ; `MB` 2015 : environ 8,2 hl AP déclarés), acheteur Henri Castex (`admin@negociant.fr`, Armagnacs de Gascogne) | Le fil de la démonstration (analyse §1) : projet bloqué puis corrigé, modification du prix, conclusion, retiraison, stocks estimés, DSM préremplies | 1, 2 |
| Un projet ouvert par Henri (Gascogne acheteur, Durand vendeur, `NB` Hors d'âge `20+`, 3 hl à 45 %, 2 400 € / hl AP), qui attend Jeanne | Un filet si la saisie en direct traîne : Jeanne approuve en un geste | 1 |
| Spec 05 §7.1 révisée : Armagnacs de Gascogne, 11 mois validés jusqu'à M−2, M−1 en compléments demandés ; Négoce des Landes, brouillon M−1 (RG-CA-24) | Le BNIA valide M−1 de Gascogne, Henri initie le mois courant : l'achat y est déjà, avec son justificatif | 2 |
| Une vingtaine de contrats exécutés sur les 12 derniers mois entre les opérateurs hors scénario (Larressingle, Landes, Laubade, opérateurs de la facturation), prix réalistes par millésime et compte d'âge, datés avant leurs DSM validées, sans mouvement de contrat | Mercuriales et listes remplies, stocks intacts | 3 |

### 7.2 Reprise AGI

Hors périmètre du prototype.

## 8. Décisions d'architecture

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-CA-1 | **Parties publiées lues directement** : le core lit `OperateursLecture` (droits, parties, membres, recherche restreinte) et `DeclarationsLecture` (stock estimé), et publie `RetiraisonValidee`, type de `fr.bnia.declarations.domain.partage` ; dépendances `contratsarmagnac` → `declarations` → `operateurs` → `referentiels` → `commun`, sans cycle. | Ports de passe-plat côté consommateur (écartés par ADR-017) ; module `bnia-declarations-api` (interrogation « aligner les parties publiées », après le prototype) | ADR-008, 012, 017 (suivis ; ADR-017 amendé le 2026-10-23) |
| A-CA-2 | **Tables relationnelles** sous la racine `contrat` (Spring Data JDBC) : `contrat`, `version_contrat`, `ligne_contrat`, `approbation`, `retiraison`, `version_retiraison`, `ligne_retiraison`, `approbation_retiraison` ; `journal` à part ; schéma `contratsarmagnac`. Listing, cartes et mercuriales en SQL (`JdbcClient`) sur les lignes de la version courante. | Contenu d'une version en `jsonb` (mapping à la main, mercuriales par `jsonb_array_elements`, aucun précédent) | ADR-004 (suivi), décision du 2026-10-23 |
| A-CA-3 | **Stock estimé et mouvements dans `declarations`** : table `declarations.mouvement_contrat` ; `adapter.evenement` écoute `RetiraisonValidee` dans la transaction de l'émetteur et appelle un cas d'usage qui crée les mouvements et les inscrit dans la DSM modifiable ; l'initiation d'une DSM inscrit les mouvements en attente de sa période ; stock estimé calculé à la lecture (base + mouvements), ajouté à `DeclarationsLecture`. L'échec de l'inscription annule la validation de la retiraison. | Stock estimé matérialisé, mis à jour par événement (deux vérités) ; calcul dans `contratsarmagnac` (vieillissement dupliqué, copie des retiraisons toujours nécessaire au préremplissage) | ADR-013 (suivi), ADR-017 (amendé le 2026-10-23) |
| A-CA-4 | **Référence opaque** : le mouvement de contrat référence le contrat (identifiant et numéro copiés), la retiraison et l'autre partie **sans clé étrangère** : `declarations` ne connaît pas le schéma de `contratsarmagnac`. | Clé étrangère de `declarations` vers `contratsarmagnac` (dépendance de schéma inverse de celle du code) | ADR-011 (amendé le 2026-10-23) |
| A-CA-5 | **E-mails par un port du core** (`Courriels`), adapter SMTP et gabarits Thymeleaf dans `adapter.integration`, comme `operateurs` ; destinataires lus dans `OperateursLecture.personnes` (membres `AGENT` et `ADMIN`). | Service d'e-mail commun dans `bnia-commun-infra` (nouveau transverse pour un troisième usage) ; attendre `notifications` (non implémenté) | — |
| A-CA-6 | **Module `CONTRATS`** ajouté à `Module` (`operateurs`, partie publiée) ; l'exception « lecture seule pour l'opérateur BNIA » est dans `Droits.gere` (les Contrats se gèrent par le seul rôle local, comme la Facturation par le seul rôle tenu au BNIA), et les cas d'usage refusent toute action au nom de l'opérateur BNIA (décision du 2026-10-23, à l'implémentation). | Drapeau « géré par les seules parties » dans `Module` (une règle des contrats dans `operateurs`) ; exception dans les seuls cas d'usage (`DroitsResponse` l'aurait réécrite pour le front) | ADR-016 (amendé le 2026-10-23) |
| A-CA-7 | **Écart calculé dans le domaine** (comparaison de deux versions : lignes ajoutées, retirées, champs changés), exposé par l'API en avant / après ; le front ne fait que l'afficher. | Calcul dans le front (règle hors du domaine, non testée) | — |
| A-CA-8 | **PDF** en `adapter.document` (OpenHTMLtoPDF, gabarit Thymeleaf du formulaire CA-05 allégé). | — | ADR-020 (suivi) |

## 9. Hypothèses

| Id | Hypothèse | Pourquoi |
|---|---|---|
| H-CA-1 | Le contrat est conclu par l'accord des deux parties, sans validation du BNIA, qui consulte les contrats en lecture seule quel que soit son rôle. | Vision du 2026-10-23, analyse du 2026-10-23 ; contre §4.5.1 p.30, §4.5.1.3 p.31, §5.5.3 p.67 et C.4.1.1.3 p.113-114 (validation finale du BNIA, même sans les parties, invalidation). |
| H-CA-2 | Une retiraison validée inscrite dans la DSM justifie à elle seule son mouvement d'achat ou de vente, sans pièce à déposer. | §4.4.5 p.25-26 (contrat, facture ou DAE), glossaire p.95 ; décision du 2026-10-23. |
| H-CA-3 | La date d'une retiraison est sa date d'enlèvement ; la retiraison est déclarée et validée au plus tard le jour de l'enlèvement. | Décision du 2026-10-23 ; le CDC ne connaît qu'un texte « conditions et calendrier » (C.4.1.1.1 p.113). Une retiraison validée tombe ainsi toujours dans la période en cours ou à venir. |
| H-CA-4 | Une DSM ne s'envoie qu'à partir du lendemain de la fin de sa période. | Décision du 2026-10-23 : un mouvement de la période en cours trouve toujours sa DSM modifiable ; le CDC ne dit rien du moment de l'envoi (§4.4.7 p.27, échéance §4.4.8 p.27). |
| H-CA-5 | Aucun type d'opérateur n'est exclu comme acheteur ; l'achat d'un producteur, dont les colonnes d'achat sont grisées, compte dans son stock estimé mais ne s'inscrit pas dans sa DSM. | Analyse et décision du 2026-10-23 ; CA-05 §1 nomme « négociant, bouilleur de profession, coopérative » ; RG-DEC-16. |
| H-CA-6 | La campagne d'un contrat va du 1er avril au 31 mars. | CA-05 (13/10/2026, campagne 2026-2027) ; annexe B p.98 (1er avril) ; période d'un producteur (RG-DEC-03). |
| H-CA-7 | Le stock estimé ne compte que des faits confirmés : DSM validée (ou point de départ), retiraisons validées, y compris datées de quelques jours dans le futur ; une vente conclue non retirée n'est pas déduite. | Vision, décision du 2026-10-23 : une vente conclue peut être annulée ou incomplètement retirée ; le CDC n'a pas de stock estimé. |
| H-CA-8 | Avant la conclusion, chaque partie modifie le projet ; chaque modification est une version, montrée en écart à l'autre partie. | Vision ; contre §5.5.6 p.68 (saisie validée non modifiable, correction par le BNIA). |
| H-CA-9 | Les mercuriales se calculent sur les contrats conclus ou exécutés, à la date du contrat, par millésime ou par dénomination et compte d'âge. | Vision ; §4.10.4 p.48 tirait les volumes des DSM et la valeur des contrats, sans la dénomination. |
| H-CA-10 | Une ligne porte son appellation (le PDF coche celles des lignes) ; son volume se saisit en hl ; seul le prix en € HT / hl AP se saisit, le prix au litre de volume et le montant HT s'en déduisent ; les « caractéristiques complémentaires » tiennent lieu du commentaire du contrat. | Sources contradictoires : appellation par ligne (C.4.1.1.2 p.113) et cases sur le contrat (AGI 3.1.1 p.16, CA-05) ; volume « en litres » (AGI 3.1.2 p.17) contre 30,00 pour 14,10 AP à 47 % (CA-05) ; deux prix (C.4.1.1.2 p.113) liés par le degré ; commentaire (C.4.1.1.1 p.113) absent du CA-05. Décision du 2026-10-23. |
| H-CA-11 | Le compte d'âge d'une ligne `NB` est celui du jour de la conclusion ; une retiraison après un 1er avril franchi vise le compte vieilli (+1 par 1er avril, `20+` au plus). | Sources muettes ; annexe B p.98 (le compte évolue au 1er avril) ; RG-DEC-14 (vieillissement des lignes `NB`). |

## 10. Questions ouvertes

| Id | Question | Bloque | Lot |
|---|---|---|---|
| Q-CA-1 | **Au BNIA** : confirmer que le contrat est conclu par l'accord des parties, sans validation ni invalidation du BNIA, qui consulte (H-CA-1). | Une validation BNIA rajouterait un état, une file d'attente au back-office et une action au journal | 1 |
| Q-CA-2 | **Au BNIA** : confirmer que la retiraison validée, rattachée au mouvement de la DSM, suffit à le justifier (H-CA-2). | Sinon, une facture ou un DAE resterait exigé à la validation de la DSM (RG-DEC-29) | 2 |
| Q-CA-3 | **Au BNIA** : confirmer qu'une retiraison est datée de son enlèvement et validée au plus tard ce jour-là (H-CA-3). | Une retiraison déclarée après coup devrait viser une période passée, dont la DSM peut être envoyée | 2 |
| Q-CA-4 | **Au BNIA** : confirmer qu'une DSM ne s'envoie pas avant la fin de sa période (H-CA-4). | RG-CA-24 et la démo de la spec 05 | 2 |
| Q-CA-5 | **Au BNIA** : un producteur peut-il acheter de l'Armagnac en fût à un autre opérateur, et où le déclare-t-il, ses colonnes d'achat étant grisées (H-CA-5) ? | Restriction de l'acheteur ou inscription de l'achat d'un producteur | 2 |
| Q-CA-6 | **Au BNIA** : confirmer que la campagne d'un contrat va du 1er avril au 31 mars (H-CA-6). | Campagne imprimée sur le PDF | 3 |
| Q-CA-7 | **Au BNIA** : confirmer que le stock estimé ne déduit pas les ventes conclues non retirées (H-CA-7). | Un vendeur peut conclure plus qu'il n'a ; la retiraison qui dépasse est bloquée | 1 |
| Q-CA-8 | **Au BNIA** : confirmer que chaque partie peut modifier le projet avant la conclusion, sans passer par le BNIA (H-CA-8). | Versions et écart de RG-CA-07 et 09 | 1 |
| Q-CA-9 | **Au BNIA** : confirmer le périmètre et les regroupements des mercuriales (H-CA-9). | RG-CA-26 | 3 |
| Q-CA-10 | **Au BNIA** : confirmer le contenu d'une ligne (appellation par ligne, volume en hl, prix saisi en € HT / hl AP) et que les caractéristiques complémentaires tiennent lieu de commentaire (H-CA-10). | Saisie d'une ligne, calculs et PDF | 1 |
| Q-CA-11 | **Au BNIA** : confirmer qu'une retiraison après un 1er avril vise le compte d'âge vieilli d'une ligne `NB` (H-CA-11). | Contrôle du stock et inscription dans la DSM au printemps | 2 |

La colonne « Lot » donne l'ordre de relance du BNIA : le lot que chaque réponse débloque.

## 11. Tests attendus

Chaque RG-CA-nn a au moins un test qui porte son identifiant (CLAUDE.md).
- **Tests de domaine**, sans base : RG-CA-03, 04 (exemple du CA-05 : 30 hl à 47 % donnent 1 410 L AP,
  8,23 € / l vol (1 750 € / hl AP), 24 675 € HT ; 2009 donne 16), 05, 06 à 10 (versions, approbations, écart, refus), 12
  (stock fourni), 13 à 17 (reste à retirer, date, dernière, clôture), 11 et 21 à 22 (stock estimé,
  colonne, inscription, colonne grisée), 24, 26.
- **Tests de cas d'usage**, avec repositories simulés, puis IT : RG-CA-01 (refus par cas d'usage, BNIA en
  lecture seule), 02, 11 (vieillissement au 1er avril, point de départ), 12 et 13 (refus sans chiffre pour
  l'acheteur), 15 (événement dans la transaction, inscription dans la DSM ; échec qui annule la
  validation), 18 (e-mails, Mailpit), 19, 20 (PDF), 22 et 23 (DSM initiée après la retiraison, justificatif
  et validation par le BNIA), 25, 26 ; démonstration (`DemoContratsIT`, `DemoDeclarationsIT` révisé).

## 12. Lots d'implémentation

| Lot | Incrément | Contenu (cas d'usage, écrans, RG) | Démontrable à la fin du lot |
|---|---|---|---|
| 1 | **Négociation et conclusion** | Modules Maven (parent, `-core`, `-infra`), schéma `contratsarmagnac`, `Module.CONTRATS` (spec 02). Dans `declarations` : stock estimé (base seule), lecture publiée, colonne au portail. UC-CA-1, 2, 6, 8, 9 : ouvrir, modifier, approuver, refuser, écart, contrôle du stock, e-mails, journal ; listes, détail, cartes, onglet de la fiche opérateur ; projet d'Henri en démo. RG-CA-01 à 12, 18, 19, 25. | Jeanne ouvre un projet de 30 hl de `MB` 2015 : bloqué, elle le ramène à 15 hl ; Henri reçoit l'e-mail, modifie le prix ; Jeanne voit l'écart, approuve : conclu |
| 2 | **Retiraisons et DSM préremplie** | UC-CA-3, 4, 5, 7 : retiraisons, retrait, clôture ; `RetiraisonValidee`, mouvements de contrat, stock estimé complet, inscription dans la DSM, justificatif contrat, envoi après la fin de période (révision de la spec 05 et de sa démo). RG-CA-13 à 17, 21 à 24. | Henri déclare une retiraison, Jeanne la valide : les stocks estimés bougent, la vente est dans le brouillon de Durand ; le BNIA valide M−1 de Gascogne, Henri initie le mois : l'achat y est, justifié |
| 3 | **PDF et mercuriales** | PDF au formulaire CA-05 allégé ; mercuriales ; historique de démonstration. RG-CA-20, 26. | Le PDF du contrat conclu ; au back-office, la mercuriale du millésime 2015 parmi celles de l'année |

## Journal

| Date | Changement |
|---|---|
| 2026-10-23 | Spec créée (phases A et B) d'après l'analyse du 2026-10-23. Périmètre : contrat, retiraisons, journal, PDF, e-mails, listes, cartes des tableaux de bord et onglet de la fiche opérateur, mercuriales ; livrés dans `declarations` : mouvements de contrat, stock estimé, inscription dans la DSM qui justifie le mouvement. Ni conversation ni notes internes. |
| 2026-10-23 | Phase C : cinq sujets tranchés en séance (ligne saisie en hl, degré et € / hl AP, appellation par ligne ; produit choisi dans son stock par le vendeur ; retiraison datée de l'enlèvement, validée au plus tard ce jour ; DSM envoyée après la fin de sa période, démo de Gascogne et des Landes reculée d'un mois ; démo : historique et un projet d'avance ; aucune restriction de l'acheteur, maintenue). Modèle, RG-CA-01 à 26, cas d'usage, écrans, démo, hypothèses H-CA-1 à 9 et leurs questions, tests, trois lots. |
| 2026-10-23 | Phase E : A-CA-1 à 8 (§8). A-CA-1 suit ADR-017, déjà tranché (lecture directe des parties publiées, sans port de passe-plat) ; versions en tables relationnelles (décision du jour) ; à amender en phase H : ADR-011 (mouvement de contrat sans clé étrangère), ADR-016 (Contrats en lecture seule pour le BNIA), ADR-017 (événement et lecture du stock estimé). |
| 2026-10-23 | Phase F : revue de H-CA-1 à 9 et Q-CA-1 à 9, toutes adressées au BNIA ; ajout de H-CA-10 et 11 (contenu d'une ligne, compte d'âge `NB` après un 1er avril) et de leurs questions ; colonne « Lot » des questions. |
| 2026-10-23 | Phase G : relecture (RG-CA-01 à 26 continues, sourcées, appliquées par un cas d'usage, testées et rangées en lot ; chaque hypothèse a sa question ; pages vérifiées). Type `Cote` renommé `Partie` (collision avec les `Cote` de `declarations` et `messagerie`). Glossaire : contrat et ses états, partie, version, retiraison, mercuriale, stock estimé, mouvement de contrat ; engagements, stock disponible et bilan prévisionnel barrés. |
| 2026-10-23 | Phase H : spec terminée. ADR-011 (mouvement de contrat sans clé étrangère), ADR-016 (module « Contrats », lecture seule pour le BNIA) et ADR-017 (`RetiraisonValidee`, lecture du stock estimé) amendés ; aucun nouvel ADR. Révisions des specs 02 et 05 à faire avec les lots d'implémentation (lot 1 : `Module.CONTRATS` ; lot 2 : mouvements, inscription, justificatif, envoi, démo). |
| 2026-10-23 | Implémentation, lot 1 (négociation et conclusion). Décision en séance : l'exception « lecture seule pour le BNIA » passe dans `Droits.gere` (A-CA-6 et ADR-016 corrigés). Choix d'implémentation : contrat désigné par son identifiant (`/api/operateurs/{operateur}/contrats/{id}` au portail, `/api/contrats/{id}` au back-office) ; modifier, approuver et refuser portent le numéro de la version vue (409 si elle a changé) ; approbations gardées en ajout seul, versions insérées une fois pour toutes, version courante et partie attendue recopiées sur le contrat pour les listes ; écart comparé ligne à ligne par rang, valeurs brutes mises en forme par le front ; un contrat d'autres opérateurs est introuvable (404) ; e-mails en texte simple, comme `operateurs` (A-CA-5 prévoyait des gabarits Thymeleaf) ; numéro par compteur annuel en base ; appellation par défaut Bas Armagnac (l'exemple du CA-05), modifiable ; formulaire en pleine page (route), état local plutôt que `react-hook-form`, le contrôle du stock du vendeur se faisant en direct sur la liste de son stock sous bois ; au portail, le stock estimé a son propre tableau sur « Stocks déclarés » (base, achats, ventes, estimé). Correction du §11 : l'exemple du CA-05 donne 8,23 € / l vol, pas 17,45. |
| 2026-10-23 | Lot 1, retouches après essai : courtier retiré (hors du contexte, comme « Vu le courtier ») ; au détail, alerte au vendeur et « Approuver » désactivé quand son stock estimé ne couvre pas le projet (RG-CA-12, `stockInsuffisant` calculé au serveur) ; « Refusé par …, le … à … » ; autre partie cherchée en pleine page sur téléphone, opérateur actif exclu des résultats (RG-CA-02 le refusait déjà au serveur) ; carte « Contrats en attente d'approbation » au tableau de bord du portail, même libellé au back-office (l'onglet « En négociation » reste) ; démo sans texte de conditions. |
| 2026-10-23 | Implémentation, lot 2 (retiraisons et DSM préremplie). Choix d'implémentation : cas d'usage `ExecuterContrats` (déclarer, modifier, valider, retirer une retiraison ; annuler, clore), écritures partagées avec `NegocierContrats` par un registre (journal et e-mails après enregistrement) ; retiraisons réécrites à chaque enregistrement du contrat, numéro jamais réattribué (compteur sur le contrat) ; une retiraison vise les lignes du contrat par rang ; le stock du vendeur est contrôlé par retiraison, au grain vieilli à la date du jour (H-CA-11), le refus ne chiffre rien pour l'acheteur ; la validation de la dernière retiraison fait tomber les autres non validées ; `RetiraisonValidee` porte aussi l'utilisateur qui valide, pour le journal des DSM ; l'écart d'une retiraison (date, dernière, volume par ligne) réutilise celui du contrat ; au détail, la retiraison que le stock du vendeur ne couvre plus est signalée et « Valider » désactivé (comme RG-CA-12) ; un contrat conclu dont une retiraison attend ma partie se range « À approuver », l'autre partie « En négociation » ; la clôture et l'annulation ne sont gardées qu'au journal (pas de champ « clôture » sur le contrat) ; e-mails : retiraison déclarée, modifiée, validée, annulation, clôture. Spec 05 révisée (mouvements, inscription, justificatif, envoi après la fin de période, démonstration reculée d'un mois). |
| 2026-10-23 | Implémentation, lot 3 (PDF et mercuriales), clôture du contexte. Choix d'implémentation : PDF au portail et au back-office (`EditerContrats`, `PdfContrat`, gabarit `contratsarmagnac/contrat.html`, A4 portrait), noté au journal au portail seulement (le BNIA lit) ; parties imprimées depuis `OperateursLecture.identiteLegale` (révision de la spec 02 : SIRET, CVI, téléphone) ; cases cochées dessinées (la police de base du PDF n'a pas les glyphes de case) ; les deux mentions fixes des caractéristiques complémentaires sont rédigées d'après le relevé de l'annexe de la vision, à comparer au CA-05 ; l'état « Exécuté » ou « Annulé » suit le numéro. Mercuriales calculées dans le domaine (`Mercuriale`, dénomination comparée comme RG-DEC-11), lues en SQL sur la version courante, page « Mercuriales » du menu Contrats (période dans l'adresse, table ou cartes). Démonstration : vingt contrats exécutés entre opérateurs hors scénario, du plus ancien (environ dix mois et demi) au plus récent (cinq semaines), chacun sur une ligne du stock estimé sous bois de son vendeur, au barème 1 300 € / hl AP au compte 7 et 50 € par année de compte (1 750 € au compte 16, comme le CA-05), ouverts par l'acheteur, approuvés par le vendeur, clos sans retiraison, par des instances des cas d'usage réglées sur une horloge propre à la démonstration (patron de `DemoFacturation`), sans e-mail ; ils passent avant le projet d'Henri, qui garde sa place dans la numérotation de l'année. |
