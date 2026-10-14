# Facturation : décisions d'architecture (phase E)

Principe (2026-10-14) : architecture hexagonale stricte et modularisation Maven.

## A-FAC-1 : comment une source parle à `facturation`

### Option A (retenue le 2026-10-14) : module d'API ; ports et adapters des deux côtés

```mermaid
flowchart LR
    subgraph DEC[bnia-declarations]
        DC[core<br/>port Facturation]
        DI[infra<br/>adapter FacturationAdapter<br/>adapter ArticlesCotisation<br/>écouteur des événements]
    end
    subgraph FAC[bnia-facturation]
        FA[api<br/>DemandeFactures<br/>CatalogueArticles<br/>événements, types]
        FC[core]
        FI[infra]
    end
    DI --> DC
    DI --> FA
    FC --> FA
    FI --> FC
```

- Le core de `declarations` ne connaît que **son** port `Facturation`.
- Son infra fait le pont avec l'API de `facturation`.
- La frontière est garantie par Maven : `declarations` ne voit que `bnia-facturation-api`.

### Option B : module d'API appelé directement par le core de la source

```mermaid
flowchart LR
    subgraph DEC[bnia-declarations]
        DC[core<br/>appelle DemandeFactures<br/>implémente CatalogueArticles]
        DI[infra]
    end
    subgraph FAC[bnia-facturation]
        FA[api]
        FC[core]
        FI[infra]
    end
    DC --> FA
    DI --> DC
    FC --> FA
    FI --> FC
```

- Moins de code (pas de port ni d'adapter côté source).
- Le core de `declarations` dépend d'un autre contexte : l'hexagonale n'est plus stricte.

### Option C : paquets `partage` dans le core (forme actuelle d'ADR-012 et 017)

```mermaid
flowchart LR
    subgraph DEC[bnia-declarations]
        DC[core]
        DI[infra]
    end
    subgraph FAC[bnia-facturation]
        FC[core<br/>dont paquets partage]
        FI[infra]
    end
    DC --> FC
    DI --> DC
    FI --> FC
```

- Pas de module de plus.
- La frontière n'est garantie que par ArchUnit : tout le core de `facturation` est visible de la source.

### Côté `operateurs` (dans les trois options)

```mermaid
flowchart LR
    FC[facturation core<br/>ports Clients, DroitsFacturation]
    FI[facturation infra<br/>adapters]
    OP[operateurs<br/>partie publiée]
    FI --> FC
    FI --> OP
```

## A-FAC-2 : comment `facturation` trouve les catalogues

### Option A (retenue le 2026-10-14) : liste injectée

```mermaid
flowchart LR
    D[declarations infra<br/>ArticlesCotisation]
    S[distillation infra<br/>ArticlesFinTravaux]
    B[boutique infra<br/>ArticlesBoutique]
    API[facturation api<br/>CatalogueArticles]
    FC[facturation core<br/>List of CatalogueArticles<br/>indexée par source]
    D -- implémente --> API
    S -- implémente --> API
    B -- implémente --> API
    FC --> API
```

- Spring injecte tous les catalogues. Une nouvelle source n'a qu'à déclarer son adapter.
- Deux catalogues de même `source()` : refus au démarrage.

### Option B : registre explicite dans `bnia-boot`

```mermaid
flowchart LR
    BOOT[bnia-boot<br/>RegistreCatalogues<br/>declarations, distillation, boutique]
    FC[facturation core]
    BOOT --> FC
```

- La liste des sources se lit en un seul endroit.
- Une ligne de câblage à ajouter pour chaque nouvelle source.

### Option C : un catalogue composite dans `bnia-boot`

```mermaid
flowchart LR
    BOOT[bnia-boot<br/>CatalogueComposite<br/>délègue par source]
    FC[facturation core<br/>un seul CatalogueArticles]
    BOOT --> FC
```

- `facturation` ne sait pas qu'il existe plusieurs sources.
- La logique de répartition par source se trouve dans `bnia-boot`.

## A-FAC-3 : où implémenter les ports de l'hôte (droits, clients)

Ports définis dans le core de `facturation` : `DroitsFacturation` (le nom `Habilitations` est déjà pris
dans `operateurs`) et `Clients`.

### Option A (retenue le 2026-10-14) : adapters dans l'infra de `facturation`

```mermaid
flowchart LR
    FC[facturation core<br/>ports DroitsFacturation, Clients]
    FI[facturation infra<br/>adapters sur operateurs]
    OP[operateurs<br/>partie publiée]
    FI --> FC
    FI --> OP
```

- Conforme à `CLAUDE.md` : l'intégration vit dans le contexte qui définit le port (analyse §4).
- `bnia-facturation-infra` dépend d'`operateurs`.

### Option B : adapters dans `bnia-boot`

```mermaid
flowchart LR
    FC[facturation core<br/>ports DroitsFacturation, Clients]
    BOOT[bnia-boot<br/>adapters hôte]
    OP[operateurs<br/>partie publiée]
    BOOT --> FC
    BOOT --> OP
```

- `bnia-facturation` ne dépend de rien : une lib pure.
- `bnia-boot` porte du code d'intégration, pas seulement du câblage.

### Option C : adapters dans un module hôte dédié

```mermaid
flowchart LR
    FC[facturation core]
    H[bnia-facturation-hote<br/>adapters sur operateurs]
    OP[operateurs<br/>partie publiée]
    H --> FC
    H --> OP
```

- Lib pure, et `bnia-boot` reste du câblage.
- Un module Maven de plus.

## A-FAC-4 : persistance des lignes de la pièce

Spring Data JDBC (ADR-004) ne gère pas l'héritage : les trois sortes de lignes (article, texte, groupe)
doivent se ranger autrement.

### Option A (retenue le 2026-10-14) : une table, une colonne `sorte`

```mermaid
erDiagram
    facture ||--o{ ligne_facture : contient
    ligne_facture |o--o{ ligne_facture : "groupe de"
    ligne_facture {
        bigint id
        bigint facture
        int position
        text sorte "ARTICLE, TEXTE, GROUPE"
        bigint groupe "facultatif"
        text texte "titre ou texte"
        text article_source "article seul"
        text article_code "article seul"
        numeric quantite "article seul"
        numeric prix_unitaire "article seul"
        text categorie_tva "article seul"
        numeric montant_ht "article seul"
    }
```

- Une seule collection dans l'agrégat ; l'ordre se lit d'une requête.
- Des colonnes vides selon la sorte, contrôlées par des contraintes `CHECK`.

### Option B : une table par sorte

```mermaid
erDiagram
    facture ||--o{ ligne_article : contient
    facture ||--o{ ligne_texte : contient
    facture ||--o{ ligne_groupe : contient
```

- Aucune colonne vide.
- La position est partagée entre trois tables : l'ordre se recompose à la lecture.

### Option C : lignes en `jsonb` dans la facture

```mermaid
erDiagram
    facture {
        bigint id
        jsonb lignes
    }
```

- Le plus simple à écrire.
- Les ventes consolidées (UC-FAC-8) doivent lire du JSON : SQL plus lourd.

## A-FAC-5 : numérotation sans trou (RG-FAC-12)

### Option A (retenue le 2026-10-14) : compteur verrouillé dans la transaction d'émission

```mermaid
sequenceDiagram
    participant E as EmettrePiece
    participant N as table numerotation
    E->>N: SELECT ... WHERE annee = 2026 FOR UPDATE
    N-->>E: dernier = 41
    E->>N: UPDATE dernier = 42
    Note over E,N: PDF, événements, commit<br/>(rollback : 42 non consommé)
```

- Sans trou ni doublon, même en cas d'échec : tout est annulé ensemble.
- Les émissions concurrentes s'attendent les unes les autres (quelques millisecondes).

### Option B : séquence PostgreSQL

```mermaid
sequenceDiagram
    participant E as EmettrePiece
    participant S as sequence
    E->>S: nextval
    S-->>E: 42
    Note over E,S: rollback : 42 perdu, trou
```

- Aucune attente.
- Un rollback laisse un trou : contraire à RG-FAC-12.

### Option C : max + 1 avec contrainte d'unicité

```mermaid
sequenceDiagram
    participant E as EmettrePiece
    participant F as table facture
    E->>F: SELECT max(numero) + 1
    E->>F: UPDATE numero (unique)
    Note over E,F: deux émissions simultanées :<br/>une échoue, à rejouer
```

- Pas de table de plus.
- Collision possible en cas de concurrence, avec un rejeu à gérer.

## A-FAC-6 : émission en lot, une transaction par brouillon (RG-FAC-19)

Aujourd'hui, chaque méthode d'un `@UseCase` s'exécute dans une seule transaction (`UseCaseConfiguration`).

### Option A (retenue le 2026-10-14) : cas d'usage de lot et port `Transactions`

```mermaid
flowchart LR
    C[controller] --> L[EmettreLot<br/>core]
    L --> T[port Transactions<br/>core]
    TI[adapter<br/>TransactionTemplate<br/>REQUIRES_NEW] --> T
    L --> E[EmettrePiece<br/>une par brouillon]
```

- Le compte rendu est produit par le cas d'usage ; le core reste Java pur.
- Un port technique de plus, dans `bnia-commun`.

### Option B : le controller boucle

```mermaid
flowchart LR
    C[controller<br/>boucle, compte rendu] --> E[EmettrePiece<br/>une transaction par appel]
```

- Aucun code nouveau.
- Le compte rendu, une règle (RG-FAC-19), se retrouve dans l'adapter REST.

### Option C : cas d'usage de lot non transactionnel

```mermaid
flowchart LR
    C[controller] --> L["EmettreLot<br/>@UseCase(transaction = false)"]
    L --> E[EmettrePiece<br/>sa propre transaction]
```

- Pas de port.
- Une exception à la règle « tout cas d'usage est transactionnel », à marquer dans `@UseCase`.
