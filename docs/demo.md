# Démonstration

Données fictives chargées par le profil `demo` (§7.1 des specs 01, 02, 03, 05, 06, 08 et 15), par les cas
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
| `admin@producteur.fr` | Jeanne Durand | Maison Durand, Condom (opérateur actif) et Eauze | `ADMIN` des deux | Portail : « Mon entreprise », annuaire, invitations ; passage d'un chai à l'autre ; vend dans le fil des contrats |
| `agent@producteur.fr` | Sophie Martin | Maison Durand, Condom | `AGENT` | Portail : fiche et contacts gérés, accès en lecture |
| `observateur@producteur.fr` | Pierre Roy | Maison Durand, Condom ; Eauze, accès suspendu | `OBSERVATEUR` | Portail en lecture seule ; Eauze n'est pas proposé (accès suspendu) |
| `admin@negociant.fr` | Henri Castex | Armagnacs de Gascogne (`N320001`) | `ADMIN` | Portail d'un négociant : tout, dont les accès ; achète dans le fil des contrats |
| `agent@negociant.fr` | Julie Darrigade | Armagnacs de Gascogne | `AGENT` | Portail : fiche et contacts gérés, accès en lecture |
| `observateur@negociant.fr` | Thomas Barrère | Armagnacs de Gascogne | `OBSERVATEUR` | Portail en lecture seule |
| `admin@larressingle.fr` | Marie Lacoste | Maison Larressingle (`N320002`) | `ADMIN` | Portail : DSM envoyée, qui attend le BNIA |
| `admin@landes.fr` | Bernard Dufau | Négoce des Landes (`N400001`) | `ADMIN` | Portail : DSM en brouillon avec une anomalie |
| `admin@laubade.fr` | Hélène Larrieu | Domaine de Laubade (`000003`) | `ADMIN` | Portail d'un producteur en sommeil : DSM 2024-2025 validée, en attente d'un nouveau point de départ |
| `saisie@demo.fr` | Saisie Démo | Les quinze opérateurs ajoutés pour la facturation (`N320010` à `N470010`, `000020` à `000029`) | `AGENT` de chacun | Compte technique qui a saisi leurs DSM ; sélecteur à quinze opérateurs |

Depuis le 2026-10-21, la démonstration du portail tient dans les comptes `@negociant.fr` (compléments demandés)
et `@producteur.fr` (anomalie à corriger) : ce sont les seuls que présente la réponse à l'appel d'offres. Les
contrats se jouent sur les deux mêmes (« Contrats »).

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

Les dates sont calculées depuis le jour du chargement, noté M (octobre 2026 pour un chargement en octobre
2026). Une DSM ne s'envoie qu'à partir du lendemain de la fin de sa période (spec 06 RG-CA-24) : la
démonstration des négociants porte donc sur le mois précédent, M−1. Négoce des Landes et Maison Larressingle
partent du **premier du mois précédent** (le 1er septembre 2026), Armagnacs de Gascogne du **premier du mois,
douze mois avant** (spec 08 §7.1), avec un stock fixé par Anne Lamarque : leur première DSM est celle de ce
mois.

- **Armagnacs de Gascogne (`N320001`, négociant)** : six lignes de stock initial (`NB` « XO » 12, `NB`
  « VSOP » 5, `NV` « VSOP » 5, `MB` 2010, `MV` 2015 compte 6, `NB` « Blanche » `00`). Onze DSM
  **validées**, de M−12 à M−2, aux volumes de la facture d'exemple (sorties France 839,11 L, export 2 254,15 L,
  avec le DAE de l'export), toutes facturées (voir « Facturation »). DSM de M−1
  envoyée par Julie Darrigade, prise en instruction puis en **compléments demandés** par Anne Lamarque, motif
  « DAE manquants pour l'export » : 40 L de XO passés dans l'assemblage VSOP (colonnes 12 et 6), sorties
  France 839,11 L et export 2 254,15 L (les volumes de la facture d'exemple) ; après le motif, un message de
  Julie et la réponse de Luc Dubos (messagerie), deux notes **internes** d'Anne Lamarque (sur la DSM et la ligne XO). Aucune pièce : il manque un justificatif pour
  les ventes négoce et un pour l'export. La DSM du mois courant reste à initier : l'achat du contrat s'y
  inscrira (voir « Contrats »).
- **Maison Larressingle (`N320002`, négociant)** : DSM du mois précédent **envoyée** par Marie Lacoste, avec
  le DAE de chacun de ses deux exports : elle attend l'instruction du BNIA, qui peut la valider.
- **Négoce des Landes (`N400001`, négociant)** : DSM de M−1 en **brouillon**, remplie par Bernard Dufau,
  avec une anomalie : les assemblages en entrée (colonne 6, 50 L) et en sortie (colonne 12, 60 L)
  diffèrent de 10 L.
- **Maison Durand, Condom (`000001`, producteur)** : point de départ au 1er avril de l'an dernier (2025)
  ; DSM de cette année-là **validée**, avec `NB` « Hors d'âge » 19 et `NB` « Hors d'âge » `20+`, `MB`
  2015, `NB` « Blanche » `00`, avec ses **douze DRM** et le DAE de son export (ligne `NB` « Hors d'âge »
  `20+`), tous sur le même PDF de démonstration. La DSM de l'année en cours est en **brouillon**, vieillie
  au 1er avril, avec une anomalie : les assemblages en entrée (colonne 6, 50 L, première ligne) et en sortie
  (colonne 12, 60 L, deuxième ligne) diffèrent de 10 L.
- **Maison Durand, Eauze (`000002`, producteur)** : aucun point de départ.
- **Domaine de Laubade (`000003`, producteur)**, compte `admin@laubade.fr` (Hélène Larrieu, `ADMIN`) : point
  de départ au 1er avril d'il y a deux ans (2024), DSM 2024-2025 **validée** avec ses douze DRM, puis statut
  de déclaration passé à **`SOMMEIL`** : il attend un nouveau point de départ.
- **Grille CVO** (Paramétrage, rubrique Cotisation) : 55,00 €/hlAP à partir du 1er janvier 2021, puis
  60,00 €/hlAP à partir du 1er janvier 2024, 20 % exonérés de TVA. La DSM de Gascogne y vaut 1 855,95 € HT,
  comme la facture d'exemple. Les factures sont décrites plus bas (« Facturation »).

La DSM suivante d'un négociant (reprise) s'initie dès que la précédente est validée : Armagnacs de Gascogne,
une fois M−1 validée (scénario 3), initie celle du mois courant, mais ne l'envoie qu'à partir du 1er du mois
suivant (« Envoyer » désactivé, avec la date). Le vieillissement au 1er avril se montre sur le producteur.

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
   désactivé, avec son motif, et le panneau liste les pièces manquantes. `admin@negociant.fr`, « Mes DSM » :
   la DSM du mois courant s'initie ; « Envoyer » y attend le 1er du mois suivant.
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
   sur la part assujettie (296,95 €), TTC 2 152,90 €. Ajouter un taux de 62,00 € au 1er du mois précédent
   (le taux se lit à la fin de la période) : la cotisation de Gascogne suit, puis supprimer ce taux. Listing « DSM » : total HT sur chaque ligne ;
   les DSM validées sans facture sont dans « Validées », donc à facturer.
9. « Exporter », « PDF » d'une DSM (n'importe quel compte qui la lit, observateur compris) : le PDF s'ouvre dans
   un nouvel onglet, un onglet du format unique par page, volumes au format hl.l,cl, puis la cotisation ;
   « Document provisoire, DSM non validée » sur Gascogne, pas sur la DSM validée de Condom. L'édition
   apparaît à l'historique.
10. `admin@landes.fr` (Bernard Dufau), DSM en brouillon de Négoce des Landes, « Exporter », « Excel » : le
    classeur au format du modèle du BNIA (cinq onglets, volumes au format hl.l,cl, totaux en formules,
    onglet « Cotisation »). Dans le tableur, ajouter une ligne au récapitulatif (`NV` « XO » 10), lui
    mettre 100 L en colonne 22 (onglet des ventes export, même rangée), ramener la colonne 12 (assemblages
    en sortie) à 50 L ; enregistrer, puis « Importer » : l'anomalie des assemblages disparaît, la ligne
    nouvelle est ajoutée, l'import est à l'historique. Taper « abc » dans une cellule de volume, ou changer
    le mois en A8, et réimporter : le fichier est refusé, chaque erreur située (onglet, ligne, colonne),
    rien n'est importé.
11. `admin@bnia.fr`, DSM validée de Maison Durand, Condom : « Exporter », « Excel ». Fiche de Maison Durand,
    Eauze, onglet « Déclarations », « Importer depuis Excel » avec ce fichier : le point de départ est fixé
    au 1er avril qui suit la campagne du fichier, avec son stock de fin, vieilli d'un an.
12. `admin@bnia.fr`, menu « Déclarations > DSM », onglet « En cours » : colonne « Échéance » (le 5 du mois courant pour M−1) ; Maison
    Larressingle, envoyée, attend le BNIA et n'est pas en retard ; Armagnacs de Gascogne, en compléments
    demandés, et Négoce des Landes, en brouillon, le sont dès le 6 du mois. Case « En retard » : ces deux-là ;
    état « Absente » : Maison Saint-Puy, sans DSM du mois dernier, en retard elle aussi ; ligne grisée, dont
    seul l'opérateur mène à sa fiche. Tableau de bord : « DSM à relancer », en liseré rouge, compte ces trois-là (les DSM
    en retard seulement ; sans retard, la carte n'apparaît pas) et ouvre cette vue. Les périodes absentes pas encore en retard ne sont pas listées. La détection quotidienne (6 h) publie `DeclarationEnRetard` une fois par période ; sans
    `notifications`, rien ne part encore.
13. `admin@landes.fr`, « Mes DSM » : bandeau de l'échéance (le 5 du mois courant), passée dès le 6 :
    « envoyez votre déclaration au BNIA dès que possible ». En-tête de la DSM et éditions PDF et Excel :
    l'échéance.
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
17. Même compte, tableau de bord : section « Activités », « DSM à saisir ou compléter », « DSM en retard » (liseré rouge,
    seulement en retard : dès le 6 du mois, sa DSM de M−1 en compléments demandés), « DSM en attente du BNIA », « Factures impayées » (liseré rouge, seulement s'il y en a : la facture
    d'août), « Nouveaux messages », « Contrats à approuver », « Contrats en attente
    d'approbation », « Retiraisons à valider » (chiffres réels, chacun ouvre sa page), puis deux cartes de démonstration (certificats
    export, commandes boutique).

## Facturation (spec 08, lots 1, 2, 3, 5 et 6)

- **Vendeur** (roue crantée « Paramètres de l'application », rubrique « BNIA > Facturation ») : le BNIA, tel qu'imprimé
  sur les factures d'exemple (11 place de la Liberté, Eauze ; SIRET 77699995500029 ; TVA FR55776999955 ;
  IBAN et BIC ; `compta@armagnac.fr`), sans option sur les débits.
- **SIRET et n° de TVA** : Maison Durand (`000001`, `000002`) et les trois négociants ; un destinataire des
  factures `comptabilite@<code>.example` pour chaque négociant. Le Domaine de Laubade (`000003`), qui a une DSM
  validée, n'a pas de SIRET : il n'est pas facturable.
- **Armagnacs de Gascogne** : ses **onze** DSM validées sont **facturées** et leurs factures **émises** par Anne
  Lamarque, chacune **à la fin du mois qui suit sa période** (DSM d'octobre 2025 émise le 30 novembre 2025, …,
  DSM d'août 2026 émise le 30 septembre 2026), numérotées dans l'ordre des dates (`2025000001`, puis
  `2026000001`…) ; chacune vaut 1 855,95 € HT, 296,95 € de TVA, 2 152,90 € TTC, comme la facture d'exemple ;
  une **note** au journal de la première. Aucune ne reste à facturer.
- **Maison Durand, Condom** : sa DSM validée est facturée et émise à la fin du mois qui suit sa campagne
  (30 avril).
- **Les quinze opérateurs ajoutés** : chaque DSM validée est facturée et émise à la fin du mois qui suit sa
  période (au plus tard la veille) ; les numéros suivent l'ordre des dates, tous opérateurs confondus.
- **Aucun brouillon** de facture au démarrage ; la seule DSM validée à facturer est celle du Domaine de Laubade,
  qui ne l'est pas.
- **Zeendoc** (lot 6, simulé) : toute facture émise **avant le mois courant** est transmise le lendemain de son
  émission, puis payée à son échéance (au plus tard la veille du chargement), sauf **celle de Gascogne émise il y
  a deux mois** (fin août pour un chargement en octobre) : transmise, **impayée**, échéance passée. Les factures
  émises **dans le mois courant** (en octobre, celles des quatre négociants ajoutés à jour, sur leur DSM de
  septembre) restent à transmettre.

Scénario :

1. `admin@bnia.fr`, tableau de bord : « Factures à valider » vaut 0, « Factures impayées », en liseré rouge, compte les
   factures échues et impayées (transmises à Zeendoc, échéance passée) : 1, celle de Gascogne d'août ; elle ouvre
   l'onglet « Émises », où sa ligne est teintée de rouge pâle (de même dans l'onglet « Factures » de la fiche).
   Sans facture échue, la carte n'apparaît pas. Menu « Facturation >
   Factures » : onglet « Brouillons » vide ; onglet « Émises » : les
   factures du mois, à transmettre, et celle de Gascogne d'août, « Transmis », impayée ; onglet « Payées » : les
   autres, « Payé ». Ouvrir une facture de Gascogne : acheteur, dates, échéance au dernier jour du mois suivant
   (« 30 jours fin de mois »), groupe « Commerce de l'Armagnac » avec quatre lignes (part assujettie à 20 %,
   part non assujettie exonérée), sous-total, récapitulatif de TVA, totaux, et son état Zeendoc avec ses dates.
   « PDF » : la facture au nouveau modèle ; bouton « Historique » : demandée, émise, transmise, payée, puis
   consultée (ouverture, PDF).
2. Onglet « Émises » : cocher les factures du mois, « Transmettre à Zeendoc » : elles passent à « Transmis ».
3. Pour facturer : `admin@bnia.fr` valide la DSM envoyée de Maison Larressingle (« Déclarations », scénario 2).
   Menu « Déclarations > DSM », onglet « Validées » : elle y est, avec celle du Domaine de Laubade. L'ouvrir :
   « Facturer » ; on reste sur la DSM, « Brouillon de facture créé », puis « Facturation demandée : voir le
   brouillon ». Au listing, elle est passée dans « Facturées », badge « Brouillon ». Onglet « Brouillons » des
   factures : l'ouvrir, rien n'empêche son émission ; « Émettre » : il prend le numéro suivant ; sa DSM (fiche
   de l'opérateur, onglet « Déclarations ») passe à « facturée », le numéro ouvre la facture.
4. Revenir à « Validées », cocher la DSM du Domaine de Laubade, « Facturer la sélection » : refusé, « Domaine
   de Laubade n'est pas facturable (SIRET manquant) ». Sa fiche le dit (« Non facturable »).
5. Fiche d'Armagnacs de Gascogne, bouton « Factures » : l'historique filtré sur ce client.
6. Paramétrage, « Facturation > Vendeur » : modifier le téléphone ; les factures déjà émises gardent
   l'ancien. Un SIRET faux est refusé.

Brouillons (lot 2) :

7. Valider d'autres DSM pour en facturer plusieurs : celle de M−1 d'Armagnacs de Gascogne, une fois ses
   compléments renvoyés (« Déclarations », scénario 3), celle de Négoce des Landes, une fois envoyée (scénario
   1). « Déclarations > DSM », onglet « Validées » : les cocher, « Facturer la sélection » : un brouillon par DSM.
8. « Facturation > Factures », onglet « Brouillons » : en ouvrir un, « Modifier ». La date de l'opération et
   l'acheteur sont en lecture ; les lignes reçues de la DSM portent un cadenas (seule leur désignation change).
   Corriger la période, ajouter un texte « Régularisation suite au contrôle », le remonter en tête,
   « Enregistrer » : le journal note « Modifiée ». Ajouter un article (« Article ») puis le retirer.
9. Sur un autre brouillon, « Abandonner » : il disparaît, et sa DSM redevient « à facturer » (journal de la
   DSM : « Facturation abandonnée »).
10. Onglet « Brouillons » : tout cocher, « Émettre la sélection » : le compte rendu donne un numéro par
    brouillon, ou le motif d'un écart (par exemple, l'opérateur a perdu son SIRET entre-temps).
11. Ouvrir la première facture émise de Gascogne (novembre 2025) : en bas de page, le carnet de notes internes
    et la note de démonstration ; en ajouter une, la modifier. Le bouton « Historique » ne montre que le journal.

Portail (lot 3) :

12. `observateur@negociant.fr` (Armagnacs de Gascogne), menu « Facturation > Factures » : les factures émises
    de Gascogne, sans les brouillons. Filtrer par numéro ou par période ; ouvrir la facture, puis « PDF ».
    La facture d'août, échue et impayée, a sa ligne teintée de rouge pâle. Tableau de bord (`agent@negociant.fr`
    ou `admin@negociant.fr`, qui lisent la facturation) : « Factures impayées », en liseré rouge, vaut 1, la facture
    d'août ; sans facture échue, la carte n'apparaît pas.
13. `admin@bnia.fr`, la même facture, bouton « Historique » : « Consultée » par Thomas Barrère, côté « Client »
    (ouverture, puis PDF).

Articles et ventes (lot 5) :

14. `admin@bnia.fr`, menu « Facturation > Articles » : les huit articles de la cotisation (part assujettie à
    20 %, part non assujettie exonérée, en hlAP) ; une ligne ouvre la grille CVO du Paramétrage.
15. « Facturation > Ventes » : deux graphiques de démonstration (chiffre d'affaires total, par secteur), puis,
    période « Année en cours », les lignes des factures émises cette année, total HT ; une ligne (ou son
    chevron) ouvre sa facture. « Exporter en PDF » (synthèse par article, TVA, totaux). Période « Année
    précédente » : les factures de novembre et de décembre 2025.
16. `agent@negociant.fr` (Armagnacs de Gascogne), menu « Facturation > Cotisation » : deux graphiques de
    démonstration à l'échelle d'un négociant (cotisation totale, par ligne), puis ses lignes de cotisation de
    l'année (sorties France et export, parts assujettie et exonérée), total HT ; une ligne ouvre sa facture.

## Messagerie (spec 15, lots 1 à 6)

- **Armagnacs de Gascogne**, DSM de M−1 en compléments demandés : le motif (« DAE manquants pour
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

## Contrats (spec 06, lots 1 à 3)

- **Le fil** se joue sur deux comptes : Jeanne Durand (`admin@producteur.fr`, Maison Durand, Condom) vend,
  Henri Castex (`admin@negociant.fr`, Armagnacs de Gascogne) achète. Le stock estimé de Condom part de sa DSM
  2025-2026 validée, vieillie à aujourd'hui : 8,17 hl AP de `MB` 2015 notamment.
- **Un projet d'avance**, ouvert par Henri : Gascogne acheteur, Condom vendeur, une ligne `NB` « Hors d'âge »
  `20+`, 3 hl à 45 % (1,35 hl AP), 2 400 € HT / hl AP, soit 3 240,00 € HT, solde à 30 jours fin de mois. Il
  attend Jeanne : un filet si la saisie en direct traîne, elle l'approuve en un geste.
- **L'historique** : vingt contrats exécutés, conclus entre dix mois et demi et cinq semaines avant le
  chargement, entre des opérateurs hors du fil (acheteurs : Maison Larressingle, Négoce des Landes, les
  négociants de la facturation ; vendeurs : Domaine de Laubade, les producteurs de la facturation). Chacun
  porte une ligne du stock sous bois de son vendeur, au barème de 1 300 € HT / hl AP au compte 7 et 50 € par
  année de compte (1 750 € au compte 16, comme le CA-05), à quelques dizaines d'euros près. Clos sans
  retiraison : aucun stock n'a bougé. Ils remplissent les listes du back-office et les mercuriales.

Le BNIA lit tous les contrats et n'agit sur aucun (ADR-016) ; chaque action qui attend l'autre partie lui
envoie un e-mail (ses `AGENT` et `ADMIN`), dans Mailpit.

Scénario :

1. `admin@producteur.fr` (Jeanne), tableau de bord : « Contrats à approuver » compte le projet d'Henri. Menu
   « Contrats », onglet « À approuver » : il y est (pour aller vite, l'ouvrir et « Approuver » : conclu).
2. « Nouveau contrat » : « Je suis le vendeur », l'autre partie Armagnacs de Gascogne (Condom n'est pas
   proposé). Ligne choisie dans son stock estimé sous bois : `MB` 2015, 8,17 hl AP restants ; 30 hl à 47 %,
   1 750 € HT / hl AP : 14,10 hl AP, le dépassement est signalé en direct et l'enregistrement refusé. Ramener
   à 15 hl (7,05 hl AP, 12 337,50 € HT) : le projet part, il attend Henri ; l'e-mail est dans Mailpit.
3. `admin@negociant.fr` (Henri), onglet « À approuver » : le projet de Jeanne. « Modifier », prix à 1 800 € :
   il attend de nouveau Jeanne.
4. `admin@producteur.fr` : l'écart est surligné (avant barré, puis après, avec son auteur). « Approuver » :
   le contrat est conclu, daté du jour. « Plus » : « PDF », le formulaire CA-05 allégé, signatures comprises
   (un projet porte « Projet, non conclu »).
5. `admin@negociant.fr`, le contrat conclu, carte « Retiraisons » : « Déclarer une retiraison », enlèvement
   aujourd'hui, dernière, le reste à retirer de la ligne. Au-delà du stock de Jeanne, le refus ne donne aucun
   chiffre. La retiraison attend Jeanne ; le contrat reste dans « Conclus ».
6. `admin@producteur.fr`, tableau de bord : « Retiraisons à valider » mène à l'onglet « Conclus », où le
   contrat porte « Retiraison à valider », en tête. L'ouvrir, la retiraison « À valider par vous » :
   « Valider » (au plus tard le jour de l'enlèvement) ; dernière, le contrat passe à « Exécutés ». Menu « Déclarations > Stocks déclarés »,
   tableau « Stock estimé » : la `MB` 2015 a baissé de la vente. La DSM 2026-2027 de Condom, en brouillon, a
   reçu la vente sur sa ligne `MB` 2015, marquée « Contrat CA-… » ; onglet « Pièces », le justificatif
   « Contrat CA-…, retiraison 1 », lien vers le contrat.
7. `admin@negociant.fr`, « Stocks déclarés » : le stock estimé de Gascogne a pris l'achat. L'achat attend la
   DSM du mois courant : une fois M−1 validée par le BNIA (« Déclarations », scénario 3), « Mes DSM »,
   initier le mois : une ligne `MB` 2015 ajoutée, l'achat en colonne 2 (vendeur producteur), justifié par le
   contrat dans l'onglet « Pièces ». « Envoyer » attend le 1er du mois suivant.
8. `admin@bnia.fr`, menu « Contrats > Contrats » : tous les contrats, onglets « En négociation »,
   « Conclus », « Exécutés », « Annulés ou refusés », filtres opérateur et numéro ; un contrat s'ouvre en
   lecture, sans action. Tableau de bord : « Contrats en attente d'approbation » compte les projets. Fiche de
   Maison Durand, Condom, onglet « Contrats » : les siens, comme acheteur ou vendeur.
9. « Contrats > Mercuriales », sur les douze derniers mois : par millésime, ou par dénomination et compte
   d'âge, nombre de contrats, volume en hl AP, prix minimum, moyen pondéré et maximum. Le millésime 2015
   compte le contrat de Jeanne et d'Henri, à 1 800 €.
10. Le reste, sur un nouveau projet ou le contrat d'avance : « Refuser » (définitif, « Refusé par …, le … à
    … ») ; sur un contrat conclu, « Plus » : « Annuler le contrat » ou « Plus de retiraison » (les
    retiraisons non validées tombent) ; une retiraison non validée, « Retirer » par Henri, ou « Modifier » et
    renvoyer par la partie attendue.
