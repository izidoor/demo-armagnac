# ADR-019 : Traitements planifiés

- Statut : accepté
- Date : 2026-10-13
- Spec : `docs/specs/05-declarations/spec.md` (décision A-DEC-7)

## Contexte
Le premier traitement planifié de l'application vient de `declarations` : chaque jour, trouver les
périodes passées en retard et publier `DeclarationEnRetard` une seule fois par période (spec 05,
RG-DEC-40 et 41). D'autres suivront : expiration des invitations, relances de `notifications`,
synchronisations ODG.

Les packages d'adapter connus sont `rest`, `persistence`, `integration` et `evenement` (ADR-002,
ADR-013). Un traitement planifié n'entre dans aucun : il n'est ni une requête, ni une persistance, ni un
tiers, ni un événement.

## Décision
- **Un cas d'usage par traitement**, avec une **horloge injectée** (`java.time.Clock`) : il est
  testable à n'importe quelle date, et les données de démonstration peuvent s'y appuyer.
- **Un nouveau package d'adapter, `fr.bnia.<contexte>.adapter.planification`.** Il contient des
  composants `@Scheduled` (cron configurable par propriété) qui n'appellent qu'un cas d'usage, comme les
  écouteurs de `adapter.evenement`.
- **Idempotence en base.** Un traitement qui produit un effet unique (un événement par période en
  retard) l'enregistre dans une table avec une contrainte d'unicité ; l'effet n'a lieu que si
  l'insertion réussit. Une double exécution, par exemple sur deux instances, est inoffensive.
- **Activation** : `@EnableScheduling` dans `bnia-boot` ; désactivée dans les tests d'intégration, qui
  appellent les cas d'usage directement.

Options écartées :
- **ShedLock** (verrou en base pour qu'une seule instance exécute) : dépendance superflue tant que
  l'idempotence en base protège l'effet ;
- **détection à la lecture** (l'événement part à la première consultation après l'échéance) : l'effet
  dépendrait du passage d'un utilisateur ;
- **cron externe** appelant un endpoint : infrastructure hors de l'application, sans gain.

## Conséquences
- `CLAUDE.md` cite `adapter.planification` parmi les packages d'adapter ; les règles ArchUnit des
  adapters (dépendent de `application`, jamais entre eux) s'y appliquent.
- Chaque traitement documente, dans sa spec, son effet unique et la table qui le garantit.
- Un traitement long ou distribué (plusieurs instances, reprise) pourra adopter ShedLock ou une file
  plus tard, sans toucher aux cas d'usage.
