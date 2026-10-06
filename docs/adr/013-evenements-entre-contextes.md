# ADR-013 : Événements entre contextes

- Statut : accepté
- Date : 2026-10-06
- Spec : `docs/specs/02-operateurs/spec.md` (décision A-OPE-2)

## Contexte
ADR-002 fait communiquer les contextes par ports ou par événements, mais aucun mécanisme d'événements
n'existait. Le premier besoin vient de `operateurs` et `acces` (spec 02, RG-OPE-29 et 32) :
- accorder l'accès portail à un contact doit créer son compte s'il n'existe pas et envoyer l'e-mail
  d'activation ; retirer l'accès doit retirer l'opérateur du compte ;
- la création d'un compte doit verrouiller l'e-mail des contacts qui le portent ; un changement
  d'identifiant dans « Mon compte » doit être reporté sur ces contacts.

D'autres suivront (DSM validée vers `facturation`, fin de travaux reçue, facture émise).

Deux contraintes. `domain` et `application` sont sans Spring (ADR-010, ArchUnit). Et `acces` dépend de
`operateurs`, dont il lit la partie publiée (ADR-012) : si `operateurs` importait les types d'événements
de `acces`, les deux modules Maven formeraient un cycle.

## Décision
- **Transport : Spring `ApplicationEvent`, synchrone.** L'écouteur s'exécute dans la transaction de
  l'émetteur et appelle son propre cas d'usage : tout ou rien. Un accès accordé sans compte, ou l'inverse,
  est impossible.
- **Publication par un port.** `application` publie par une interface (`PublicationEvenements`) ;
  l'adapter qui la traduit en `ApplicationEventPublisher` et les écouteurs (`@EventListener`) vivent dans
  un nouveau package d'adapter, `fr.bnia.<contexte>.adapter.evenement`. Un écouteur ne fait qu'appeler un
  cas d'usage.
- **Types d'événements dans la partie publiée du contexte dont l'autre dépend.** Ce sont des records
  immuables. Pour `operateurs` et `acces`, les quatre types (`AccesPortailAccorde`,
  `AccesPortailRetire`, `CompteCree`, `IdentifiantCompteModifie`) sont dans
  `fr.bnia.operateurs.domain.partage`, ceux que `operateurs` émet comme ceux qu'il accepte. Les
  dépendances restent `acces → operateurs → commun`. Entre deux contextes sans partie publiée, le type
  vit dans le contexte émetteur, et le consommateur en dépend dans son pom, sans cycle.
- **Effets externes après validation.** Un effet hors de la base (e-mail, dépôt chez un tiers) n'est
  déclenché qu'après la validation de la transaction, par le contexte qui le porte.

Options écartées :
- écoute après validation (`@TransactionalEventListener(AFTER_COMMIT)`) dans une transaction séparée :
  un échec du consommateur laisserait un état incohérent, par exemple un accès sans compte, sans reprise
  automatique ;
- types d'événements dans `commun` : le noyau commun se chargerait du vocabulaire des contextes, ce que
  l'ADR-009 écarte ;
- une file de messages (outbox, broker) : fiable et asynchrone, mais disproportionnée pour un monolithe
  prototype à une seule base.

## Conséquences
- `CLAUDE.md` cite `adapter.evenement` parmi les packages d'adapter ; les règles ArchUnit des adapters
  (dépendent de `application`, jamais entre eux) s'y appliquent.
- Un consommateur en échec fait échouer l'action de l'émetteur : le message d'erreur doit le dire à
  l'utilisateur.
- Tant que le contexte consommateur n'existe pas, un événement est publié sans écouteur ; rien ne se
  passe. Les tests du contexte émetteur vérifient la publication, ceux du consommateur sa réaction.
- Passer plus tard à une file ou à l'asynchrone ne touche que les adapters `adapter.evenement` : ni le
  domaine ni les cas d'usage ne changent.
