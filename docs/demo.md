# Démonstration

Données fictives chargées par le profil `demo` (spec 01 §7.1, spec 02 §7.1, spec 03 §7.1), par les cas
d'usage, au démarrage. Le chargement est relançable : ce qui existe déjà n'est pas recréé.

## Lancer

Depuis `backend/` :

```
./mvnw -pl bnia-boot spring-boot:run -Dspring-boot.run.profiles=dev,demo
```

puis, depuis `frontend/`, `pnpm dev`, et ouvrir http://localhost:5173.

## Se connecter

Une seule page de connexion pour tous (RG-AUT-01), http://localhost:5173/connexion :

- mot de passe de tous les comptes : `demo-bnia-2026` ;
- code de vérification : toujours `000000` en `dev` et en `demo` (propriété
  `bnia.authentification.code-fixe`) ; il part aussi par e-mail ;
- e-mails (codes, invitations, liens de mot de passe et de changement d'e-mail) : boîte Mailpit,
  http://localhost:8025, démarrée avec la base par `backend/compose.yaml`.

L'opérateur actif décide de l'espace (RG-OPE-49) : back-office pour le BNIA, portail pour un autre
opérateur. Le sélecteur sous le titre de l'espace change d'opérateur actif.

## Comptes

| E-mail | Personne | Opérateur | Rôle | Ce qu'on y voit |
|---|---|---|---|---|
| `anne.lamarque@bnia.example` | Anne Lamarque | BNIA | `ADMIN` | Tout le back-office ; bloquer un utilisateur, désactiver ou supprimer un opérateur, rétrograder le dernier ADMIN d'un opérateur |
| `luc.dubos@bnia.example` | Luc Dubos | BNIA | `AGENT` | Back-office : fiches, contacts et Paramétrage gérés ; Accès en lecture |
| `claire.sentex@bnia.example` | Claire Sentex | BNIA | `OBSERVATEUR` | Back-office en lecture seule |
| `jeanne.durand@maison-durand.example` | Jeanne Durand | Maison Durand, Condom (opérateur actif) et Eauze | `ADMIN` des deux | Portail : « Mon entreprise », annuaire, invitations ; passage d'un chai à l'autre |
| `sophie.martin@maison-durand.example` | Sophie Martin | Maison Durand, Condom | `AGENT` | Portail : fiche et contacts gérés, accès en lecture |
| `pierre.roy@maison-durand.example` | Pierre Roy | Maison Durand, Eauze | `OBSERVATEUR`, accès suspendu | Connexion refusée : aucun accès actif (RG-AUT-04) |

Sans compte :

- Paul Petit, contact de Maison Durand, Condom (maître de chai), désigné contact principal ;
- Marc Lafitte, invité comme `ADMIN` de la Distillerie ambulante Lafitte (`000010`), invitation en
  attente : son lien est dans Mailpit, pour essayer l'acceptation et le premier mot de passe.

## Opérateurs

Le BNIA (type `BNIA`, menu « BNIA » du back-office) et seize opérateurs du Gers, des Landes et du
Lot-et-Garonne : producteurs `000001` à `000009`, négociants `N320001`, `N320002`, `N400001`, deux
`AUTRE` (`000010`, `000011`), une fiche incomplète (`000012`) et une fiche inactive (`000013`).
