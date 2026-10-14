# ADR-022 : Port `Transactions`

- Statut : accepté
- Date : 2026-10-14
- Spec : `docs/specs/08-facturation/spec.md` (décision A-FAC-6) ; schémas :
  `docs/specs/08-facturation/architecture.md`

## Contexte
Chaque méthode publique d'un `@UseCase` s'exécute dans une transaction, de propagation `REQUIRED`
(`UseCaseConfiguration`, `bnia-boot`). Les cas d'usage sont en Java pur (ADR-014) : ils ne peuvent pas
ouvrir de transaction eux-mêmes.

L'émission en lot de `facturation` (RG-FAC-19) doit émettre chaque brouillon dans **sa propre**
transaction. Un brouillon refusé n'annule pas les autres et ne consomme pas de numéro. Le compte rendu
par brouillon (numéro ou motif du refus) est une règle de gestion.

## Décision
- **Port `Transactions`** dans `bnia-commun-core` (`commun.application`) : exécuter une action dans une
  nouvelle transaction et en rendre le résultat, ou l'exception levée.
- **Adapter** dans `bnia-commun-infra` : `TransactionTemplate` avec la propagation `REQUIRES_NEW`.
- Le cas d'usage de lot (`EmettreLot`) appelle, à travers le port, le cas d'usage unitaire
  (`EmettrePiece`) pour chaque élément, puis assemble le compte rendu.

Options écartées :
- **Le controller boucle** sur le cas d'usage unitaire : aucun code nouveau, mais le compte rendu, une
  règle de gestion, irait dans l'adapter REST ;
- **un `@UseCase` non transactionnel**, marqué par un attribut : pas de port, mais une exception à la
  règle « tout cas d'usage est transactionnel », et un comportement qui dépend des proxys Spring.

## Conséquences
- `CLAUDE.md` cite le port `Transactions` dans `bnia-commun-core`.
- Le port ne sert qu'aux traitements par lot, où chaque élément est indépendant. Un cas d'usage
  ordinaire reste dans la transaction unique de son appel.
- Les tests de cas d'usage utilisent une implémentation immédiate du port, qui exécute l'action sans
  transaction.
