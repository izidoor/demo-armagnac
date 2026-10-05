# ADR-001 : Langue du code et du glossaire

- Statut : accepté
- Date : 2026-10-05

## Contexte
Le CDC, le client et l'éventuel prestataire repreneur sont francophones. Le vocabulaire métier
(DSM, ODG, hlAP, compte d'âge, CVO, millésime) n'a pas d'équivalent anglais fidèle.

## Décision
Le vocabulaire du domaine est en français, sans accents dans les identifiants
(`Operateur`, `CompteDAge`). Le vocabulaire technique est en anglais (`Repository`, `Adapter`,
`UseCase`). `docs/glossaire.md` fait référence et ses termes ne sont jamais traduits.

## Conséquences
- Le code se lit avec le CDC sous les yeux, et la traçabilité exigence → code est directe.
- Mélange FR/EN assumé dans les noms de classes (`DeclarationRepository`).
- Les colonnes SQL suivent la même règle, en snake_case.
