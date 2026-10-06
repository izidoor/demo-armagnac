# NN : <contexte>

> Contexte `<contexte>` (étape N de `docs/contextes.md`). Statut : **en cours de rédaction**.
> Références : `p.N` = page du CDC v1.1, `AGI p.N` = page de la spec de reprise AGI.
> `H-<CTX>-n` : hypothèse retenue, révisable. `Q-<CTX>-n` : question ouverte, suivie dans cette spec (§10).

## Sommaire

<Généré par `python3 tools/sommaire.py <spec>`, à relancer après chaque modification de titre.>

## 1. Objet

<Ce que le contexte fait, en quelques lignes. Ce qu'il ne fait pas. Où est sa valeur.>

Sources : <sections et pages du CDC et de la spec AGI>.
Usages relevés ailleurs : <sections et pages trouvées par la recherche transverse>.

## 2. Périmètre

### 2.1 Dans le contexte

| Famille | Élément | Code (ADR-001) | Source |
|---|---|---|---|

### 2.2 Hors du contexte

| Élément décrit dans le CDC | Contexte retenu | Motif |
|---|---|---|

## 3. Modèle

<Une sous-section par agrégat ou objet. Pour chacun :>

| Champ | Type | Règle | Source |
|---|---|---|---|

<Après chaque tableau : les écarts avec le CDC et leur justification.>

## 4. Règles de gestion

| Id | Règle | Source |
|---|---|---|
| RG-<CTX>-01 | | |

## 5. Cas d'usage

### 5.1 Écriture

| Cas d'usage | Objets | RG |
|---|---|---|

### 5.2 Lecture

<Ce que le contexte expose aux autres et comment (ports, événements, partie publiée), selon les ADR.>

| Besoin | Consommateur probable |
|---|---|

## 6. Écrans

### 6.1 Back-office

### 6.2 Portail

<Droits du §4.2.5 concernés, et leur application tant que `acces` n'existe pas.>

## 7. Données

### 7.1 Démonstration (profil `demo`)

### 7.2 Reprise AGI

| Objet | Vue AGI | Correspondance |
|---|---|---|

## 8. Décisions d'architecture

| Id | Décision | Écarté | ADR |
|---|---|---|---|
| A-<CTX>-1 | | | |

## 9. Hypothèses

| Id | Hypothèse | Pourquoi |
|---|---|---|
| H-<CTX>-1 | | |

## 10. Questions ouvertes

| Id | Question | Bloque |
|---|---|---|
| Q-<CTX>-1 | | |

## 11. Tests attendus

Chaque RG-<CTX>-nn a au moins un test qui porte son identifiant (CLAUDE.md).
- **Tests de domaine**, sans base : <RG>.
- **Tests de cas d'usage**, avec repository simulé, puis IT : <RG>.

## Journal

| Date | Changement |
|---|---|
