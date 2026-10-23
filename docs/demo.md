# Démonstration

Données fictives chargées par le profil `demo` (spec 01 §7.1, spec 02 §7.1, spec 03 §7.1, spec 05 §7.1), par les cas
d'usage, au démarrage. Le chargement est relançable : ce qui existe déjà n'est pas recréé.

## Lancer

Depuis `backend/` :

```
./mvnw -pl bnia-boot spring-boot:run -Dspring-boot.run.profiles=dev,demo
```

puis, depuis `frontend/`, `pnpm dev`, et ouvrir http://localhost:5173.

## Se connecter

Une seule page de connexion pour tous (RG-AUT-01), http://localhost:5173/connexion :

- mot de passe de tous les comptes : `demo-armagnac` ;
- double authentification désactivée au départ : la session s'ouvre après le mot de passe. Un ADMIN
  du BNIA l'active dans les paramètres de l'application, section Sécurité (RG-AUT-16) ;
- code de vérification, une fois la double authentification activée : toujours `000000` en `dev` et en
  `demo` (propriété `bnia.authentification.code-fixe`) ; il part aussi par e-mail ;
- e-mails (codes, invitations, liens de mot de passe et de changement d'e-mail) : boîte Mailpit,
  http://localhost:8025, démarrée avec la base par `backend/compose.yaml`.

L'opérateur actif décide de l'espace (RG-OPE-49) : back-office pour le BNIA, portail pour un autre
opérateur. Le sélecteur sous le titre de l'espace change d'opérateur actif.

## Lire l'offre

« Voir l'offre », au bas de la barre latérale des deux espaces, sur grand écran seulement (dès 1024 px), ouvre
en plein écran la réponse à l'appel d'offres : les miniatures des sections à gauche, la section choisie à droite,
les flèches vers la précédente et la suivante, le PDF à télécharger. Les pages sont servies sous `/api/offre/`, donc après connexion seulement.
Elles sont publiées depuis `echanges-bnia/reponse`, après `pnpm build`, par `pnpm publier`, qui réécrit
`backend/bnia-boot/src/main/resources/offre/`, sauf son dossier `diaporama/`.

En tête des miniatures, « Offre | Diaporama » passe au diaporama de la soutenance : diapositives 16:9, mises à
l'échelle de l'écran ; flèches, Page précédente et Page suivante (télécommande), Début et Fin ; un clic avance.
« Projeter » met la diapositive seule en plein écran (Échap en sort). Fermée, la visionneuse retient la diapositive :
on reprend après la démonstration là où on s'est arrêté. Le diaporama est publié depuis `echanges-bnia/diaporama`,
après `pnpm build` (HTML et PDF 16:9 dans `sortie/`), par `pnpm publier`, sous `resources/offre/diaporama/`.
Sous 1024 px, le PDF du diaporama s'ajoute à celui de l'offre.

## Comptes

| E-mail | Personne | Opérateur | Rôle | Ce qu'on y voit |
|---|---|---|---|---|
| `admin@bnia.fr` | Anne Lamarque | BNIA | `ADMIN` | Tout le back-office ; bloquer un utilisateur, désactiver ou supprimer un opérateur, rétrograder le dernier ADMIN d'un opérateur |
| `agent@bnia.fr` | Luc Dubos | BNIA | `AGENT` | Back-office : fiches, contacts et Paramétrage gérés ; Accès en lecture |
| `observateur@bnia.fr` | Claire Sentex | BNIA | `OBSERVATEUR` | Back-office en lecture seule |
| `admin@producteur.fr` | Jeanne Durand | Maison Durand, Condom (opérateur actif) et Eauze | `ADMIN` des deux | Portail : « Mon entreprise », annuaire, invitations ; passage d'un chai à l'autre |
| `agent@producteur.fr` | Sophie Martin | Maison Durand, Condom | `AGENT` | Portail : fiche et contacts gérés, accès en lecture |
| `observateur@producteur.fr` | Pierre Roy | Maison Durand, Condom ; Eauze, accès suspendu | `OBSERVATEUR` | Portail en lecture seule ; Eauze n'est pas proposé (accès suspendu) |
| `admin@negociant.fr` | Henri Castex | Armagnacs de Gascogne (`N320001`) | `ADMIN` | Portail d'un négociant : tout, dont les accès |
| `agent@negociant.fr` | Julie Darrigade | Armagnacs de Gascogne | `AGENT` | Portail : fiche et contacts gérés, accès en lecture |
| `observateur@negociant.fr` | Thomas Barrère | Armagnacs de Gascogne | `OBSERVATEUR` | Portail en lecture seule |
| `admin@larressingle.fr` | Marie Lacoste | Maison Larressingle (`N320002`) | `ADMIN` | Portail : DSM envoyée, qui attend le BNIA |
| `admin@landes.fr` | Bernard Dufau | Négoce des Landes (`N400001`) | `ADMIN` | Portail : DSM en brouillon avec une anomalie |

Depuis le 2026-10-21, la démonstration du portail tient dans les comptes `@negociant.fr` (compléments demandés)
et `@producteur.fr` (anomalie à corriger) : ce sont les seuls que présente la réponse à l'appel d'offres.
| `admin@laubade.fr` | Hélène Lassus | Domaine de Laubade (`000003`) | `ADMIN` | Portail d'un producteur en sommeil : DSM 2024-2025 validée, en attente d'un nouveau point de départ |
| `saisie@demo.fr` | Saisie Démo | Les quinze opérateurs ajoutés pour la facturation (`N320010` à `N470010`, `000020` à `000029`) | `AGENT` de chacun | Compte technique qui a saisi leurs DSM ; sélecteur à quinze opérateurs |

Sans compte :

- Paul Petit, contact de Maison Durand, Condom (maître de chai), désigné contact principal ;
- Marc Lafitte, invité comme `ADMIN` de la Distillerie ambulante Lafitte (`000010`), invitation en
  attente : son lien est dans Mailpit, pour essayer l'acceptation et le premier mot de passe.

## Opérateurs

Le BNIA (type `BNIA`, section « BNIA » des paramètres de l'application, roue crantée du back-office) et seize opérateurs du Gers, des Landes et du
Lot-et-Garonne : producteurs `000001` à `000009`, négociants `N320001`, `N320002`, `N400001`, deux
`AUTRE` (`000010`, `000011`), une fiche incomplète (`000012`) et une fiche inactive (`000013`).

S'y ajoutent, pour le volume de la facturation (spec 08 §7.1), quinze opérateurs fictifs, facturables (SIRET,
n° de TVA, destinataire des factures `comptabilite@<code>.example`, sans compte) : cinq négociants (`N320010`,
`N320011`, `N320012`, `N400010`, `N470010`), toutes leurs DSM mensuelles de l'année validées jusqu'au mois
dernier, sauf Maison Saint-Puy (`N320012`), arrêtée au mois d'avant : sa DSM du mois dernier est absente, en
retard dès le 6 du mois ; dix producteurs (`000020` à `000029`), leur DSM de la dernière année déclarative close validée, avec
ses DRM. Les uns exportent (avec un DAE), les autres non ; les volumes varient d'un mois à l'autre.

## Déclarations (spec 05, lots 1 à 8)

Les dates sont calculées depuis le jour du chargement. Négoce des Landes part du **premier du mois courant**
(le 1er octobre 2026 pour un chargement en octobre 2026), Maison Larressingle du **premier du mois
précédent**, Armagnacs de Gascogne du **premier du mois, onze mois avant** (spec 08 §7.1), avec un stock fixé
par Anne Lamarque : leur première DSM est celle de ce mois.

- **Armagnacs de Gascogne (`N320001`, négociant)** : six lignes de stock initial (`NB` « XO » 12, `NB`
  « VSOP » 5, `NV` « VSOP » 5, `MB` 2010, `MV` 2015 compte 6, `NB` « Blanche » `00`). DSM des onze mois
  précédents **validées**, aux volumes de la facture d'exemple (sorties France 839,11 L, export 2 254,15 L,
  avec le DAE de l'export) : à facturer (voir « Facturation »). DSM du mois
  envoyée par Julie Darrigade, prise en instruction puis en **compléments demandés** par Anne Lamarque, motif
  « DAE manquants pour l'export » : 40 L de XO passés dans l'assemblage VSOP (colonnes 12 et 6), sorties
  France 839,11 L et export 2 254,15 L (les volumes de la facture d'exemple) ; après le motif, un message de
  Julie et la réponse de Luc Dubos (messagerie), deux notes **internes** d'Anne Lamarque (sur la DSM et la ligne XO). Aucune pièce : il manque un justificatif pour
  les ventes négoce et un pour l'export.
- **Maison Larressingle (`N320002`, négociant)** : DSM du mois précédent **envoyée** par Marie Lacoste, avec
  le DAE de chacun de ses deux exports : elle attend l'instruction du BNIA, qui peut la valider.
- **Négoce des Landes (`N400001`, négociant)** : DSM du mois en **brouillon**, remplie par Bernard Dufau,
  avec une anomalie : les assemblages en entrée (colonne 6, 50 L) et en sortie (colonne 12, 60 L)
  diffèrent de 10 L.
- **Maison Durand, Condom (`000001`, producteur)** : point de départ au 1er avril de l'an dernier (2025)
  ; DSM de cette année-là **validée**, avec `NB` « Hors d'âge » 19 et `NB` « Hors d'âge » `20+`, `MB`
  2015, `NB` « Blanche » `00`, avec ses **douze DRM** et le DAE de son export (ligne `NB` « Hors d'âge »
  `20+`), tous sur le même PDF de démonstration. La DSM de l'année en cours est en **brouillon**, vieillie
  au 1er avril, avec une anomalie : les assemblages en entrée (colonne 6, 50 L, première ligne) et en sortie
  (colonne 12, 60 L, deuxième ligne) diffèrent de 10 L.
- **Maison Durand, Eauze (`000002`, producteur)** : aucun point de départ.
- **Domaine de Laubade (`000003`, producteur)**, compte `admin@laubade.fr` (Hélène Lassus, `ADMIN`) : point
  de départ au 1er avril d'il y a deux ans (2024), DSM 2024-2025 **validée** avec ses douze DRM, puis statut
  de déclaration passé à **`SOMMEIL`** : il attend un nouveau point de départ.
- **Grille CVO** (Paramétrage, rubrique Cotisation) : 55,00 €/hlAP à partir du 1er janvier 2021, puis
  60,00 €/hlAP à partir du 1er janvier 2024, 20 % exonérés de TVA. La DSM de Gascogne y vaut 1 855,95 € HT,
  comme la facture d'exemple. Les factures sont décrites plus bas (« Facturation »).

La DSM suivante d'un négociant (reprise) ne s'initie qu'à partir du 1er du mois suivant : la reprise se
montre sur le producteur.

Scénario :

1. `admin@landes.fr`, menu « Déclarations > Mes DSM » : la DSM en brouillon et le point de départ en
   lecture. Ouvrir la DSM : cinq onglets, colonnes numérotées, panneau des anomalies ; un clic sur
   l'anomalie mène à l'onglet. « Envoyer » est désactivé, avec son motif. Saisir 60 L en colonne 6 de la
   ligne « NB VSOP » à la place des 50 L (onglet « Mouvements en entrée ») : l'anomalie disparaît aussitôt
   (vue provisoire). « Envoyer » : la saisie est enregistrée puis envoyée.
2. `admin@bnia.fr`, menu « Déclarations > DSM » : onglet « En cours », état « À prendre en charge » : la file d'attente montre
   Maison Larressingle et Négoce des Landes. Ouvrir la DSM d'Armagnacs de Gascogne (en compléments
   demandés) : sous le classeur, le motif, protégé, et les messages échangés avec Gascogne, puis le carnet
   de notes internes (deux notes d'Anne Lamarque) ; la pastille de la ligne XO mène à sa note. Ouvrir celle
   de Maison Larressingle : « Prendre en charge », puis « Valider » ou « Demander des compléments ».
3. `agent@negociant.fr` : la DSM est en compléments demandés, le motif dans ses messages ; les notes
   internes n'apparaissent pas. Corriger, répondre par un message ; onglet « Pièces », déposer une facture
   pour les ventes négoce et un DAE pour l'export (n'importe quel PDF) ; « Renvoyer » : la DSM est en
   « Compléments envoyés ». Puis `admin@bnia.fr` : elle est en « Compléments reçus » ; « Valider »
   directement, sans la reprendre en instruction ; la DSM est figée. Sans les pièces, « Valider » reste
   désactivé, avec son motif, et le panneau liste les pièces manquantes.
4. `admin@producteur.fr` (Maison Durand, Condom), « Mes DSM » : initier la DSM de l'année. Les lignes
   reprennent le stock de fin de l'année validée ; au 1er avril, `NB` « Hors d'âge » 19 est passée à
   `20+` et a fusionné avec la ligne `20+` ; le compte de la `MB` 2015 a pris un an ; la « Blanche »
   `00` est passée à `0`. Les colonnes grisées du producteur sont visibles et vides.
5. `admin@bnia.fr`, fiche de la Maison Durand, Eauze, onglet « Déclarations » : fixer le point de départ
   (un 1er avril) avec quelques lignes. Puis `admin@producteur.fr`, opérateur actif Eauze, « Mes DSM » :
   initier la DSM de l'année.
6. `admin@bnia.fr`, DSM de la Maison Durand, Condom, validée : onglet « Pièces », les douze mois de la
   période avec leur DRM, le DAE rattaché à sa ligne ; un clic ouvre le PDF dans un nouvel onglet. Plus
   rien ne se dépose ni ne se supprime. Sur la DSM de l'année, une fois initiée et remplie par
   `admin@producteur.fr` : les douze mois sont « attendus », et la validation reste refusée tant qu'un
   mois manque.
7. `admin@bnia.fr`, fiche de Maison Larressingle, onglet « Déclarations » : modifier le stock initial ;
   la colonne 1 de la DSM en cours suit. Changer la date de début est refusé tant que la DSM est en
   cours.
8. `admin@bnia.fr`, roue crantée « Paramètres de l'application », rubrique « Cotisation > Taux CVO » : les deux taux, le
   premier clos au 31/12/2023. Ouvrir la DSM d'Armagnacs de Gascogne, onglet « Cotisation » : sorties
   France 100,69 € + 402,77 €, export 270,50 € + 1 081,99 €, total HT 1 855,95 €, TVA indicative à 20 %
   sur la part assujettie (296,95 €), TTC 2 152,90 €. Ajouter un taux de 62,00 € au 1er du mois courant :
   la cotisation de Gascogne suit, puis supprimer ce taux. Listing « DSM » : total HT sur chaque ligne ;
   les DSM validées sans facture sont dans « Validées », donc à facturer.
9. Bouton « PDF » d'une DSM (n'importe quel compte qui la lit, observateur compris) : le PDF s'ouvre dans
   un nouvel onglet, un onglet du format unique par page, volumes au format hl.l,cl, puis la cotisation ;
   « Document provisoire, DSM non validée » sur Gascogne, pas sur la DSM validée de Condom. L'édition
   apparaît à l'historique.
10. `admin@landes.fr` (Bernard Dufau), DSM en brouillon de Négoce des Landes, bouton « Excel » : le
    classeur au format du modèle du BNIA (cinq onglets, volumes au format hl.l,cl, totaux en formules,
    onglet « Cotisation »). Dans le tableur, ajouter une ligne au récapitulatif (`NV` « XO » 10), lui
    mettre 100 L en colonne 22 (onglet des ventes export, même rangée), ramener la colonne 12 (assemblages
    en sortie) à 50 L ; enregistrer, puis « Importer » : l'anomalie des assemblages disparaît, la ligne
    nouvelle est ajoutée, l'import est à l'historique. Taper « abc » dans une cellule de volume, ou changer
    le mois en A8, et réimporter : le fichier est refusé, chaque erreur située (onglet, ligne, colonne),
    rien n'est importé.
11. `admin@bnia.fr`, DSM validée de Maison Durand, Condom : bouton « Excel ». Fiche de Maison Durand,
    Eauze, onglet « Déclarations », « Importer depuis Excel » avec ce fichier : le point de départ est fixé
    au 1er avril qui suit la campagne du fichier, avec son stock de fin, vieilli d'un an.
12. `admin@bnia.fr`, menu « Déclarations > DSM », onglet « En cours » : colonne « Échéance » ; Maison
    Larressingle, envoyée, attend le BNIA et n'est pas en retard ; Armagnacs de Gascogne, en compléments
    demandés sur le mois courant, n'est pas encore en retard. Case « En retard », état
    « Absente » : Maison Saint-Puy, sans DSM du mois dernier, en retard dès le 6 du mois ; ligne grisée, dont
    seul l'opérateur mène à sa fiche. Les périodes absentes pas encore en retard ne sont pas listées. La détection quotidienne (6 h) publie `DeclarationEnRetard` une fois par période ; sans
    `notifications`, rien ne part encore.
13. `admin@landes.fr`, « Mes DSM » : bandeau de
    l'échéance à venir (le 5 du mois prochain). En-tête de la DSM et éditions PDF et Excel : l'échéance.
14. `admin@bnia.fr`, fiche du Domaine de Laubade, onglet « Déclarations » : point de départ figé,
    « Fixer un nouveau point de départ » ; le stock proposé est celui de fin de la DSM 2024-2025. Choisir
    le 1er avril de l'année en cours, « Reprendre le stock vieilli à cette date » : `NB` « Hors d'âge »
    prend un an par 1er avril franchi. Enregistrer : le journal note l'interruption, l'ancien point passe
    dans « Points de départ précédents ». Onglet « Production », repasser le statut de déclaration à
    `OUI` ; puis `admin@laubade.fr`, « Mes DSM » : initier la DSM de l'année.
15. `admin@bnia.fr` (ou `observateur@bnia.fr`), menu « Déclarations > Stocks déclarés » : le stock de fin
    de la dernière DSM validée de Maison Durand, Condom et du Domaine de Laubade, ligne par ligne, total ;
    filtres opérateur et type ; « Exporter en Excel » pour les douanes. La fiche de chacun en montre le
    sien, section « Stock déclaré ».
16. `agent@negociant.fr` (Armagnacs de Gascogne), menu « Déclarations > Stocks déclarés » : sans onglets,
    comme au back-office, deux graphiques de démonstration à l'échelle de son stock déclaré (sans choix du
    type), puis son stock de fin, ligne par ligne, avec le lien vers sa DSM.
17. Même compte, tableau de bord : section « Activités », « DSM à saisir ou compléter », « DSM en attente du
    BNIA », « Factures non payées », « Nouveaux messages » (chiffres réels, chacun ouvre sa page), puis trois
    cartes de démonstration (contrats, certificats export, commandes boutique).

## Facturation (spec 08, lots 1, 2, 3 et 5)

- **Vendeur** (roue crantée « Paramètres de l'application », rubrique « BNIA > Facturation ») : le BNIA, tel qu'imprimé
  sur les factures d'exemple (11 place de la Liberté, Eauze ; SIRET 77699995500029 ; TVA FR55776999955 ;
  IBAN et BIC ; `compta@armagnac.fr`), sans option sur les débits.
- **SIRET et n° de TVA** : Maison Durand (`000001`, `000002`) et les trois négociants ; un destinataire des
  factures `comptabilite@<code>.example` pour chaque négociant. Le Domaine de Laubade (`000003`), qui a une DSM
  validée, n'a pas de SIRET : il n'est pas facturable.
- **Armagnacs de Gascogne** : ses **huit** DSM validées les plus anciennes sont **facturées** et leurs factures
  **émises** par Anne Lamarque, chacune **à la fin du mois qui suit sa période** (DSM de novembre 2025 émise le
  31 décembre 2025, etc.), numérotées dans l'ordre des dates (`2025000001`, puis `2026000001`…) ; chacune vaut
  1 855,95 € HT, 296,95 € de TVA, 2 152,90 € TTC, comme la facture d'exemple ; une **note** au journal de la
  première. La **neuvième** est demandée : un **brouillon** attend son émission. Les **deux** dernières restent
  **à facturer**.
- **Maison Durand, Condom** : sa DSM validée est facturée et émise à la fin du mois qui suit sa campagne
  (30 avril).
- **Les quinze opérateurs ajoutés** : chaque DSM validée est facturée et émise à la fin du mois qui suit sa
  période (au plus tard la veille) ; les numéros suivent l'ordre des dates, tous opérateurs confondus.

Scénario :

1. `admin@bnia.fr`, menu « Facturation > Factures », onglet « Émises » : les factures d'Armagnacs de Gascogne
   et de Maison Durand. Ouvrir une facture de Gascogne : acheteur, dates, échéance au dernier jour du mois suivant (« 30 jours fin de mois »), groupe
   « Commerce de l'Armagnac » avec quatre lignes (part assujettie à 20 %, part non assujettie exonérée),
   sous-total, récapitulatif de TVA, totaux. « PDF » : la facture au nouveau modèle ; bouton « Historique » :
   demandée, émise, puis consultée (ouverture, PDF).
2. Onglet « Brouillons » : celui d'Armagnacs de Gascogne. L'ouvrir : rien n'empêche son émission ;
   « Émettre » : il prend le numéro suivant. Sa DSM (fiche de l'opérateur, onglet « Déclarations ») passe à
   « facturée », le numéro ouvre la facture.
3. Menu « Déclarations > DSM », onglet « Validées » : les deux DSM restantes d'Armagnacs de Gascogne et celle
   du Domaine de Laubade. Ouvrir une de Gascogne : « Facturer » ; on reste sur la DSM, « Brouillon de facture
   créé », puis « Facturation demandée : voir le brouillon ». Au listing, elle est passée dans « Facturées »,
   badge « Brouillon ».
4. Revenir à « Validées », cocher la DSM du Domaine de Laubade, « Facturer la sélection » : refusé, « Domaine
   de Laubade n'est pas facturable (SIRET manquant) ». Sa fiche le dit (« Non facturable »).
5. Fiche d'Armagnacs de Gascogne, bouton « Factures » : l'historique filtré sur ce client.
6. Paramétrage, « Facturation > Vendeur » : modifier le téléphone ; les factures déjà émises gardent
   l'ancien. Un SIRET faux est refusé.

Brouillons (lot 2) :

7. « Déclarations > DSM », onglet « Validées » : cocher la DSM restante d'Armagnacs de Gascogne, « Facturer la
   sélection » : un brouillon par DSM (au besoin, abandonner d'abord quelques brouillons pour en avoir plusieurs).
8. « Facturation > Factures », onglet « Brouillons » : en ouvrir un, « Modifier ». La date de l'opération et
   l'acheteur sont en lecture ; les lignes reçues de la DSM portent un cadenas (seule leur désignation change).
   Corriger la période, ajouter un texte « Régularisation suite au contrôle », le remonter en tête,
   « Enregistrer » : le journal note « Modifiée ». Ajouter un article (« Article ») puis le retirer.
9. Sur un autre brouillon, « Abandonner » : il disparaît, et sa DSM redevient « à facturer » (journal de la
   DSM : « Facturation abandonnée »).
10. Onglet « Brouillons » : tout cocher, « Émettre la sélection » : le compte rendu donne un numéro par
    brouillon, ou le motif d'un écart (par exemple, l'opérateur a perdu son SIRET entre-temps).
11. Ouvrir la facture émise de Gascogne : en bas de page, le carnet de notes internes et la note de
    démonstration ; en ajouter une, la modifier. Le bouton « Historique » ne montre que le journal.

Portail (lot 3) :

12. `observateur@negociant.fr` (Armagnacs de Gascogne), menu « Facturation > Factures » : les factures émises
    de Gascogne, sans les brouillons. Filtrer par numéro ou par période ; ouvrir la facture, puis « PDF ».
13. `admin@bnia.fr`, la même facture, bouton « Historique » : « Consultée » par Thomas Barrère, côté « Client »
    (ouverture, puis PDF).

Articles et ventes (lot 5) :

14. `admin@bnia.fr`, menu « Facturation > Articles » : les huit articles de la cotisation (part assujettie à
    20 %, part non assujettie exonérée, en hlAP) ; une ligne ouvre la grille CVO du Paramétrage.
15. « Facturation > Ventes » : deux graphiques de démonstration (chiffre d'affaires total, par secteur), puis,
    période « Année en cours », les lignes des factures émises cette année, total HT ; une ligne (ou son
    chevron) ouvre sa facture. « Exporter en PDF » (synthèse par article, TVA, totaux). Période « Année
    précédente » : la facture de décembre 2025.
16. `agent@negociant.fr` (Armagnacs de Gascogne), menu « Facturation > Cotisation » : deux graphiques de
    démonstration à l'échelle d'un négociant (cotisation totale, par ligne), puis ses lignes de cotisation de
    l'année (sorties France et export, parts assujettie et exonérée), total HT ; une ligne ouvre sa facture.

## Messagerie (spec 15, lots 1 à 6)

- **Armagnacs de Gascogne**, DSM du mois courant en compléments demandés : le motif (« DAE manquants pour
  l'export »), protégé, puis un message de Julie Darrigade et la réponse de Luc Dubos, non lue pour Gascogne ;
  deux notes internes d'Anne Lamarque (sur la DSM et sur la ligne XO).
- **Conversations libres de Gascogne** : « Import de la matrice refusé », de Julie Darrigade, l'Excel de sa DSM en
  pièce jointe, avec la réponse du BNIA, qu'Henri Castex (l'ADMIN) ne voit pas ; « Accès aux statistiques du
  marché », d'Henri Castex, que Julie ne voit pas.
- **Dernière facture émise de Gascogne** : la question d'Henri Castex sur le volume facturé, non lue pour le BNIA.

Scénario :

1. `admin@bnia.fr`, tableau de bord : « Nouveaux messages » compte les non lus du BNIA ; un clic ouvre la messagerie :
   les conversations non lues ressortent en gras, avec leur nombre de non lus. Ouvrir la question d'Henri Castex : la
   conversation de la facture s'affiche sur le premier message non lu, le compteur baisse et la ligne perd son gras ; « Ouvrir la facture » mène à la pièce, qui
   montre la même conversation dans sa section « Messages ». Répondre.
2. Sur un message, menu « … », « Marquer non lu » : il est de nouveau compté au tableau de bord, jusqu'à la prochaine
   ouverture de sa conversation.
3. Cocher « Non lues seulement » : les lues disparaissent ; décocher. Filtrer par opérateur (Gascogne), par type (DSM, Factures, Sujets libres), « Non lues
   seulement » ; chercher « debit » ou « DAE » (sans tenir compte des accents ni de la casse). Ouvrir « Import de la
   matrice refusé » : la pièce jointe Excel se télécharge.
4. `agent@negociant.fr` (Julie Darrigade), tableau de bord : la carte « Nouveaux messages » compte la réponse de Luc
   Dubos. Menu « Messagerie » : la conversation de la DSM, celle de la facture, et sa conversation
   libre, pas celle d'Henri Castex. « Nouvelle conversation » : un titre, un message, une capture d'écran (PNG) ;
   seuls Julie et le BNIA la voient.
5. `admin@negociant.fr` (Henri Castex) : sa conversation libre, pas celle de Julie ; la conversation de la DSM et
   celle de la facture, oui. `observateur@negociant.fr` lit la conversation de la DSM sans y écrire, mais écrit dans
   celle de la facture ; il n'a pas « Nouvelle conversation » (l'observateur lit la Messagerie sans la gérer).
