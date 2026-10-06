# Questions ouvertes

Objectif : un prototype qui convainc, pas un prototype conforme à 100 %. Deux parties : ce qui bloque vraiment, puis ce qu'on tranche nous-mêmes en notant l'hypothèse.

## 1. Bloquant

**Q1. Les pièces jointes de l'annexe E (p.140) sont-elles dans le dossier de consultation ?**
Il en faut au minimum trois : `ENVOI_DSM_NEGOCIANT.xlsx` (matrice d'import), `FACT CVO NORMALE.pdf` et `CERTIF EXPORT DETAILS.pdf`. Sans elles, les imports et les PDF seront « inspirés de », et la démonstration perd son effet : un membre du comité reconnaît immédiatement sa matrice et ses documents.

**Q2. Connais-tu les vrais taux de cotisation ?**
Barème par colonne de DSM et par type d'opérateur, taux sur fin de travaux, pourcentage d'exonération de TVA. Le CDC n'en donne aucune valeur ; les factures exemples les contiennent peut-être. Sinon, valeurs fictives clairement étiquetées « paramétrage de démonstration » (un taux faux sous le nez du comité serait pire qu'un taux annoncé comme fictif).

**Q3. Sous quelle forme la démonstration sera-t-elle vue ?**
Réponse -> lien que le comité va tester seul. On le déploiera sur mon serveur vps de test à l'adresse armagnac-demo.izidor.fr

## 2. Hypothèses retenues, à trancher (révisables)

| # | Constat dans le CDC | Hypothèse retenue pour le prototype |
|---|---|---|
| H1 | **États d'une DSM** : p.22 donne En attente de saisie → À valider → À facturer → Facturée ; p.108 donne En attente de saisie, À facturer, Facturée, **Validée**. Le tableau de bord (§4.1) distingue « en attente de validation » et « validées non facturées », et p.66 dit qu'après validation opérateur la DSM passe « en attente de facturation ». | Quatre états : `EN_ATTENTE_DE_SAISIE` → `A_VALIDER` (transmise et signée par l'opérateur, ou saisie par le BNIA) → `A_FACTURER` (validée par le BNIA) → `FACTUREE`. « Validée » n'est pas un état distinct. |
| H2 | **Pluri-annuels et propositions d'engagement** (Armagnac et vin) : le BNIA arbitre leur maintien (p.32-35). | Hors prototype. Mentionnés dans l'offre comme options. |
| H3 | **Assiette de cotisation** : §4.4.9 (p.27-28) liste des flux par nom ; §4.9.3 (p.44-45) liste des colonnes par type d'opérateur, avec des écarts (col. 14, « bouteilles », « brandies »). | On suit §4.9.3, plus précis : matrice type d'opérateur × colonne, paramétrable. |
| H4 | **Formules de contrôle de la DSM** : le CDC cite des « colonnes croisées entre entrées et sorties » et un stock de fin « calculé » sans donner les formules. | `stock fin = stock début + Σ entrées − Σ sorties` (en LAP). Les croisements précis sont fixés dans la spec DSM et présentés comme paramétrables. |
| H5 | **CVE et CVO** ont la même définition dans le glossaire (p.96). | Un seul concept, `Cotisation` (CVO). Pas de CVE dans le code. |
| H6 | **Compte d'âge** : la formule « année − millésime − 1 » (annexe B) n'est valable qu'à partir du 1er avril ; borne `20+` en DSM, aucune en export. | Calcul par date d'observation selon la grille B.2. `00` et `0` distincts. |
| H7 | **Signature électronique, Zeendoc, Otentik, ODG** dépendent de services tiers non accessibles. | Adapters simulés derrière les ports : signature (PDF + horodatage + empreinte), Zeendoc (dépôt + retour d'état différé), Otentik (QR code factice), ODG (endpoint d'ingestion). Écrans de supervision réels. |
| H8 | **Imports de logiciels opérateurs** (GrandCru, Isavigne, ACCISES) et format XML/JSON à proposer aux éditeurs (p.28). | Hors prototype. Seul l'import de la matrice Excel BNIA est démontré. |
| H9 | **2FA** : email ou TOTP, avec « se souvenir 30 jours » (p.17). | 2FA par code email (visible dans Mailpit) ; TOTP en option si le temps le permet. |
| H10 | **Reprise de données** : accès aux vues AGI à négocier, schéma Oracle inconnu, aucun type de donnée dans la spec AGI. | Démonstrateur : vues simulées à partir des champs de la spec AGI, transformation, rapport de contrôle (comptages, rattachements, orphelins). |
| H11 | **Contenus armagnac.fr** : reprise à convenir avec l'agence, hors périmètre fonctionnel. | Quelques contenus de démonstration, pas de reprise. |
