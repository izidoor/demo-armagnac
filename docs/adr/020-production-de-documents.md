# ADR-020 : Production de documents (PDF, Excel)

- Statut : accepté
- Date : 2026-10-13
- Spec : `docs/specs/05-declarations/spec.md` (décision A-DEC-9)

## Contexte
La DSM s'édite en Excel et en PDF au format unique, et s'importe depuis le même modèle Excel (spec 05,
RG-DEC-35 à 38) :
- Excel en écriture : la matrice négociant du BNIA, cinq onglets et un onglet « Cotisation », cellules
  en litres au format `#0\.00.00`, colonnes grisées ;
- Excel en lecture : import tout ou rien, erreurs situées ;
- PDF : un onglet par page, en paysage, en-tête, totaux, mention provisoire.

`facturation` (factures, avoirs) et `exportation` (certificats multilingues) produiront aussi des PDF.
`fastexcel` 0.19 sert déjà à l'export Excel des opérateurs et `fastexcel-reader` aux tests ; aucune
bibliothèque PDF n'est en place.

## Décision
- **Excel : `fastexcel` et `fastexcel-reader`**, déjà dans la pile. La matrice est reconstruite par le
  code (même structure, mêmes numéros, mêmes formats), pas copiée d'un fichier modèle.
- **PDF : OpenHTMLtoPDF**, à partir de gabarits HTML et CSS rendus par **Thymeleaf** utilisé seul
  (moteur, sans le web). La mise en page s'écrit comme une page : tableaux, en-têtes répétés,
  pagination, polices embarquées.
- **Un nouveau package d'adapter, `fr.bnia.<contexte>.adapter.document`**, dans le contexte qui produit
  le document : gabarits, rendu PDF, écriture et lecture Excel. Un cas d'usage fournit les données
  (modèle de lecture) ; l'adapter les met en forme. L'import Excel y lit le fichier et rend des données
  brutes que le cas d'usage valide.
- Versions exactes fixées dans le pom racine (`dependencyManagement`).

Options écartées :
- **Apache POI sur une copie du modèle du BNIA** : fidélité parfaite, mais dépendance lourde, modèle à
  maintenir, et grisage variable selon le type d'opérateur ;
- **OpenPDF** : une API de bas niveau, chaque gabarit devient du code de placement ;
- **JasperReports** : éditeur graphique et format de gabarit propres à l'outil, disproportionnés pour le
  prototype.

## Conséquences
- `CLAUDE.md` cite `adapter.document` parmi les packages d'adapter ; les règles ArchUnit des adapters
  s'y appliquent.
- Les gabarits Thymeleaf vivent dans les ressources du module `-infra` du contexte.
- L'Excel produit n'est pas une copie au pixel près du modèle du BNIA (pas de logo) ; il en respecte
  le format, ce qui suffit au cycle édition-réimport.
