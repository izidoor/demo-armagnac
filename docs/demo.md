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

Sans compte :

- Paul Petit, contact de Maison Durand, Condom (maître de chai), désigné contact principal ;
- Marc Lafitte, invité comme `ADMIN` de la Distillerie ambulante Lafitte (`000010`), invitation en
  attente : son lien est dans Mailpit, pour essayer l'acceptation et le premier mot de passe.

## Opérateurs

Le BNIA (type `BNIA`, menu « BNIA » du back-office) et seize opérateurs du Gers, des Landes et du
Lot-et-Garonne : producteurs `000001` à `000009`, négociants `N320001`, `N320002`, `N400001`, deux
`AUTRE` (`000010`, `000011`), une fiche incomplète (`000012`) et une fiche inactive (`000013`).

## Déclarations (spec 05, lot 1)

Les dates sont calculées depuis le jour du chargement : le « mois de départ » est le premier du mois,
quatre mois avant le mois courant (le 1er juin 2026 pour un chargement en octobre 2026).

- **Armagnacs de Gascogne (`N320001`, négociant)** : point de départ fixé par Anne Lamarque au mois de
  départ, six lignes de stock initial (`NB` « XO » 12, `NB` « VSOP » 5, `NV` « VSOP » 5, `MB` 2010,
  `MV` 2015 compte 6, `NB` « Blanche » `00`). Sa DSM de ce mois est en brouillon, remplie par Julie
  Darrigade, avec une anomalie : les assemblages en entrée (colonne 6, 40 L) et en sortie (colonne 12,
  50 L) diffèrent de 10 L.
- **Maison Durand, Eauze (`000002`, producteur)** : aucun point de départ.

Scénario :

1. `agent@negociant.fr`, menu « Déclarations > Mes DSM » : la DSM en brouillon et le point de départ en
   lecture. Ouvrir la DSM : cinq onglets, colonnes numérotées, panneau des anomalies ; un clic sur
   l'anomalie mène à l'onglet. Saisir 10 L en colonne 6 de la ligne « NB VSOP » (onglet « Mouvements en
   entrée ») : l'anomalie disparaît aussitôt (vue provisoire). Ajouter une ligne, saisir un volume qui
   rend un stock de fin négatif, enregistrer : la DSM s'enregistre avec son anomalie. Onglet
   « Historique ».
2. `admin@bnia.fr`, fiche de la Maison Durand, Eauze, onglet « Déclarations » : fixer le point de départ
   (un 1er avril) avec quelques lignes. Puis `admin@producteur.fr`, opérateur actif Eauze, « Mes DSM » :
   initier la DSM de l'année ; les colonnes grisées du producteur sont visibles et vides.
3. `admin@bnia.fr`, fiche d'Armagnacs de Gascogne, onglet « Déclarations » : modifier le stock initial ;
   la colonne 1 de la DSM en cours suit. Changer la date de début est refusé tant que la DSM est en
   cours ; supprimer le brouillon (bouton de la DSM), puis la changer.

