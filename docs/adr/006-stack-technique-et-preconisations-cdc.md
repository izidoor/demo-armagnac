# ADR-006 : Écart assumé avec les technologies préconisées par le CDC

- Statut : accepté
- Date : 2026-10-05

## Contexte
Le CDC (§8.2, §8.3) préconise « React / Next.js » pour les interfaces et un framework back
« de type Symfony, Laravel ou équivalent ». L'objectif affiché : maintenabilité et reprise
possible par une autre agence.

## Décision
- Backend : Spring Boot (Java), jugé « équivalent » : framework moderne, très répandu, architecture
  hexagonale vérifiée au build.
- Frontend : React avec TanStack Router/Query sur la base ShadcnAdmin (Vite, Tailwind, shadcn/ui),
  sans Next.js. Le besoin est celui de deux applications authentifiées (back-office, portail) : le
  rendu serveur n'apporte rien ici.

## Conséquences
- L'écart doit être argumenté dans la réponse à l'appel d'offres (maintenabilité, enforcer, ArchUnit,
  matrice de conformité CDC → code → test).
- À vérifier dans le règlement de consultation : ces préconisations sont-elles obligatoires ou indicatives ?
