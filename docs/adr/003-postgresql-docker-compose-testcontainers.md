# ADR-003 : PostgreSQL, Docker Compose en dev, Testcontainers en test

- Statut : accepté
- Date : 2026-10-05

## Contexte
Le CDC n'impose pas de moteur de base. L'existant est sous Oracle (InteGraal), mais la reprise
passe par des vues exportées (spec AGI), non par une réutilisation du moteur.

## Décision
PostgreSQL. En dev, `compose.yaml` et le support Docker Compose de Spring Boot. Pour les `*IT`,
Testcontainers avec un conteneur partagé entre les classes de test. Pas de H2 ni de base en mémoire.

## Conséquences
- Parité dev / test / prod sur le moteur : pas de surprise de dialecte.
- Docker est requis pour `./mvnw verify`.
- Le support Docker Compose est désactivé en test pour éviter le conflit avec Testcontainers.
- Configuration en `application.yml` + profils `dev`, `demo`, `prod` ; secrets par variables d'environnement.
