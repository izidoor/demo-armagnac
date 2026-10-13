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
- code de vérification : toujours `000000` en `dev` et en `demo` (propriété
  `bnia.authentification.code-fixe`) ; il part aussi par e-mail ;
- e-mails (codes, invitations, liens de mot de passe et de changement d'e-mail) : boîte Mailpit,
  http://localhost:8025, démarrée avec la base par `backend/compose.yaml`.

L'opérateur actif décide de l'espace (RG-OPE-49) : back-office pour le BNIA, portail pour un autre
opérateur. Le sélecteur sous le titre de l'espace change d'opérateur actif.

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
| `admin@larressingle.fr` | Marie Lacoste | Maison Larressingle (`N320002`) | `ADMIN` | Portail : DSM en compléments demandés, à corriger et renvoyer |
| `admin@landes.fr` | Bernard Dufau | Négoce des Landes (`N400001`) | `ADMIN` | Portail : DSM en brouillon avec une anomalie |

Sans compte :

- Paul Petit, contact de Maison Durand, Condom (maître de chai), désigné contact principal ;
- Marc Lafitte, invité comme `ADMIN` de la Distillerie ambulante Lafitte (`000010`), invitation en
  attente : son lien est dans Mailpit, pour essayer l'acceptation et le premier mot de passe.

## Opérateurs

Le BNIA (type `BNIA`, menu « BNIA » du back-office) et seize opérateurs du Gers, des Landes et du
Lot-et-Garonne : producteurs `000001` à `000009`, négociants `N320001`, `N320002`, `N400001`, deux
`AUTRE` (`000010`, `000011`), une fiche incomplète (`000012`) et une fiche inactive (`000013`).

## Déclarations (spec 05, lots 1 à 5)

Les dates sont calculées depuis le jour du chargement. Les trois négociants partent du **premier du mois
courant** (le 1er octobre 2026 pour un chargement en octobre 2026), avec un stock fixé par Anne Lamarque
au dernier jour du mois précédent : leur première DSM est celle du mois en cours.

- **Armagnacs de Gascogne (`N320001`, négociant)** : six lignes de stock initial (`NB` « XO » 12, `NB`
  « VSOP » 5, `NV` « VSOP » 5, `MB` 2010, `MV` 2015 compte 6, `NB` « Blanche » `00`). DSM du mois
  **envoyée** par Julie Darrigade : 40 L de XO passés dans l'assemblage VSOP (colonnes 12 et 6), sorties
  France 839,11 L et export 2 254,15 L (les volumes de la facture d'exemple) ; un commentaire de Julie sur
  la ligne XO, un commentaire **interne** d'Anne Lamarque. Aucune pièce : il manque un justificatif pour
  les ventes négoce et un pour l'export.
- **Maison Larressingle (`N320002`, négociant)** : DSM du mois envoyée par Marie Lacoste, prise en
  instruction puis en **compléments demandés** par Anne Lamarque, motif « DAE manquants pour l'export ».
- **Négoce des Landes (`N400001`, négociant)** : DSM du mois en **brouillon**, remplie par Bernard Dufau,
  avec une anomalie : les assemblages en entrée (colonne 6, 50 L) et en sortie (colonne 12, 60 L)
  diffèrent de 10 L.
- **Maison Durand, Condom (`000001`, producteur)** : point de départ au 1er avril de l'an dernier (2025)
  ; DSM de cette année-là **validée**, avec `NB` « Hors d'âge » 19 et `NB` « Hors d'âge » `20+`, `MB`
  2015, `NB` « Blanche » `00`, avec ses **douze DRM** et le DAE de son export (ligne `NB` « Hors d'âge »
  `20+`), tous sur le même PDF de démonstration. La DSM de l'année en cours est à initier.
- **Maison Durand, Eauze (`000002`, producteur)** : aucun point de départ.
- **Grille CVO** (Paramétrage, rubrique Cotisation) : 55,00 €/hlAP à partir du 1er janvier 2021, puis
  60,00 €/hlAP à partir du 1er janvier 2024, 20 % exonérés de TVA. La DSM de Gascogne y vaut 1 855,95 € HT,
  comme la facture d'exemple. Aucune facture : `facturation` n'existe pas encore, l'onglet « Facturées »
  du listing reste vide.

La DSM suivante d'un négociant (reprise) ne s'initie qu'à partir du 1er du mois suivant : la reprise se
montre sur le producteur.

Scénario :

1. `admin@landes.fr`, menu « Déclarations > Mes DSM » : la DSM en brouillon et le point de départ en
   lecture. Ouvrir la DSM : cinq onglets, colonnes numérotées, panneau des anomalies ; un clic sur
   l'anomalie mène à l'onglet. « Envoyer » est désactivé, avec son motif. Saisir 60 L en colonne 6 de la
   ligne « NB VSOP » à la place des 50 L (onglet « Mouvements en entrée ») : l'anomalie disparaît aussitôt
   (vue provisoire). « Envoyer » : la saisie est enregistrée puis envoyée.
2. `admin@bnia.fr`, menu « Déclarations > DSM » : onglet « En cours », état « À prendre en charge » : la file d'attente montre
   Armagnacs de Gascogne et Négoce des Landes. Ouvrir la DSM d'Armagnacs de Gascogne : commentaires
   sous le classeur, dont l'interne ; la pastille de la ligne XO y mène. « Prendre en charge », puis
   « Demander des compléments » avec un motif.
3. `agent@negociant.fr` : la DSM est en compléments demandés, le motif en commentaire ; le commentaire
   interne n'apparaît pas. Corriger, répondre en commentaire ; onglet « Pièces », déposer une facture
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
8. `admin@bnia.fr`, roue crantée « Paramétrage », rubrique « Cotisation > Taux CVO » : les deux taux, le
   premier clos au 31/12/2023. Ouvrir la DSM d'Armagnacs de Gascogne, onglet « Cotisation » : sorties
   France 100,69 € + 402,77 €, export 270,50 € + 1 081,99 €, total HT 1 855,95 €, TVA indicative à 20 %
   sur la part assujettie (296,95 €), TTC 2 152,90 €. Ajouter un taux de 62,00 € au 1er du mois courant :
   la cotisation de Gascogne suit, puis supprimer ce taux. Listing « DSM » : total HT sur chaque ligne ;
   la DSM validée de Condom est dans « Validées », donc à facturer.
9. Bouton « PDF » d'une DSM (n'importe quel compte qui la lit, observateur compris) : le PDF s'ouvre dans
   un nouvel onglet, un onglet du format unique par page, volumes au format hl.l,cl, puis la cotisation ;
   « Document provisoire, DSM non validée » sur Gascogne, pas sur la DSM validée de Condom. L'édition
   apparaît à l'historique.

