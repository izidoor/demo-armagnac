# ADR-016 : Personnes et accès dans `operateurs`

- Statut : accepté ; amendé le 2026-10-23 (module « Contrats », spec 06)
- Date : 2026-10-08
- Amende : ADR-012 (agrégats de `operateurs`)
- Spec : `docs/specs/02-operateurs/spec.md`, révision du 2026-10-08 (§3.2, décisions A-OPE-6 et 7)

## Contexte
La spec 02 portait les droits au portail sur le contact (accès, administrateur, douze modules), avec
un contact par opérateur pour une même personne et un compte portail sans droits dans `acces`. Une
personne avait ainsi plusieurs identités, et le personnel du BNIA formait une population à part, avec
des rôles composés.

La vision du 2026-10-08 pose qu'une information n'a qu'un propriétaire et n'est stockée qu'à un endroit :
- dans un opérateur, une personne est un **contact** (sans compte) ou un **membre** : un **utilisateur**
  (propriétaire de son identité) **rattaché** avec un rôle ; ses informations professionnelles sont dans
  une **fiche professionnelle** de l'opérateur, qui la suit d'un état à l'autre ;
- on devient membre en acceptant une **invitation**, qui prouve l'e-mail ;
- le **BNIA est un opérateur** ; son personnel est géré comme celui des autres.

Il faut décider où vivent ces objets, comment ils se découpent en agrégats, et où se vérifient les
droits.

## Décision
- **Tout le métier des personnes et des accès est dans `operateurs`** : contact, utilisateur, fiche
  professionnelle, rattachement, invitation, changement d'e-mail, droits, journal. L'authentification est
  un autre contexte, sans métier (ADR-015).
- **Rôles fixes et modules.** Un rattachement porte un rôle, `OBSERVATEUR`, `AGENT` ou `ADMIN`, défini
  par l'application. Chaque rôle lit ou gère des modules : Contacts (avec la fiche de l'opérateur),
  Accès, Paramétrage (opérateur BNIA seulement). Tout rattachement actif lit tout son opérateur. Un rôle
  tenu dans l'opérateur BNIA (type `BNIA`, unique) vaut pour tous les opérateurs. **Chaque nouveau
  contexte ajoute son module** au tableau rôles × modules, et sa spec le déclare.
- **Un agrégat par objet** : `Contact`, `FicheProfessionnelle`, `Rattachement`, `Invitation`,
  `Utilisateur` (avec sa demande de changement d'e-mail), chacun son repository ; `Operateur` ne
  contient plus de contacts. Les règles collectives vivent dans les cas d'usage, appuyées par la base :
  - index uniques : e-mail parmi les contacts d'un opérateur ; un rattachement par opérateur et
    utilisateur ; une invitation `EN_ATTENTE` par e-mail et opérateur (index partiel) ; une fiche par
    porteur ;
  - **verrou sur la ligne de l'opérateur** (`select … for update`) dans tout cas d'usage qui change les
    ADMIN d'un opérateur, pour garantir le dernier ADMIN (RG-OPE-35) ;
  - une version par objet (conflit 409 à la seconde modification concurrente).
- **Droits vérifiés dans les cas d'usage seuls.** Le controller passe l'`Acteur` (utilisateur
  connecté) ; chaque cas d'usage obtient ses `Droits` sur l'opérateur concerné et vérifie tout : module et
  niveau, et règles liées aux données (sa propre fiche, champs réservés au BNIA, dernier ADMIN sauf par le
  BNIA). Les droits sont calculés à chaque requête, jamais portés par la session. La partie publiée de
  `operateurs` les expose aux autres contextes ; le front lit « mes droits » pour ses menus.
- **Un journal** des actions sur les personnes et les accès (acteur et cible copiés, avant / après), qui
  remplace le journal des contacts. Les connexions sont au journal de `authentification`.

Options écartées :
- un agrégat `Annuaire` par opérateur (contacts, fiches, rattachements, invitations) : règles locales,
  mais une version pour toutes les personnes de l'opérateur, donc un conflit entre deux fiches modifiées
  en même temps ;
- tout dans `Operateur` : chaque modification de la fiche réécrirait les personnes, conflits entre
  modifications sans rapport ;
- une annotation d'exigence sur chaque endpoint, vérifiée par un intercepteur : déclaratif, mais les
  règles liées aux données resteraient dans les cas d'usage, deux endroits pour un même droit ;
- des rôles composés par le BNIA et des droits délégués par module (CDC §4.2.1, §5.13.2) : écart assumé,
  les trois rôles fixes sont la cible (Q-OPE-16, tranchée).

## Conséquences
- Les événements de l'ADR-013 entre `operateurs` et `acces` disparaissent ; `operateurs` publie ceux que
  `authentification` écoute (ADR-015).
- La partie publiée (ADR-012) perd `ContactVue` et `ModulePortail`, et gagne les droits, l'acteur et les
  trois événements.
- Un cas d'usage oublie-t-il de vérifier les droits ? Rien ne l'empêche à la compilation : chaque cas
  d'usage a un test de refus, et la revue de chaque lot le contrôle.
- Un cas d'usage qui change les ADMIN doit prendre le verrou de l'opérateur ; un test d'intégration
  concurrent le vérifie.
- Plus d'identifiant tiré de la séquence avant l'insertion pour ces objets, chacun étant une racine.
- Le code de la spec 02 (contacts dans `Operateur`, droits portail, journal des contacts) est à
  réimplémenter.

## Amendement du 2026-10-23 (spec 06, décision A-CA-6)
- **Module « Contrats »** ajouté (`Module.CONTRATS`) : `OBSERVATEUR` lit, `AGENT` et `ADMIN` gèrent.
- **Exception à la valeur universelle du rôle BNIA** : un contrat se négocie entre ses deux parties, le
  BNIA le consulte (vision du 2026-10-23). Un rôle tenu dans l'opérateur BNIA ne donne sur ce module que la
  lecture, quel que soit le rôle. L'exception est dans `Droits.gere`, à côté de son inverse, la Facturation
  (gestion par le seul rôle tenu au BNIA, spec 08) : les Contrats se gèrent par le seul rôle local. Le niveau
  envoyé au front (`DroitsResponse`) en découle sans calcul en double ; les cas d'usage de `contratsarmagnac`
  refusent en outre toute action au nom de l'opérateur BNIA (décision du 2026-10-23, à l'implémentation du
  lot 1, qui remplace « `Droits` ne change pas »).
- Écarté : un drapeau « géré par les seules parties » sur `Module`, qui mettrait une règle des contrats
  dans `operateurs` ; l'exception appliquée par les seuls cas d'usage, qui obligeait `DroitsResponse` à la
  réécrire pour que le front ne montre pas la gestion au BNIA.
