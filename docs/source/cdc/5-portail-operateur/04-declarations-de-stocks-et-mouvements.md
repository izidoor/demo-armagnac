<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Portail opérateur · pp. 64-67 -->

<!-- p.64 -->
# 5.4 Déclarations de stocks et mouvements
Le portail doit permettre aux opérateurs de consulter leur historique de déclarations et de déposer leurs `DSM` dans de bonnes conditions d’usage. L’ergonomie de ce module est un point central du projet. 

## 5.4.1 Historique des déclarations
L’opérateur doit pouvoir retrouver l’ensemble de ses déclarations par période et par état. 

Le listing doit permettre au minimum : 

- la recherche par période; 

- la consultation de l’état de la déclaration; 

- l’ouverture du détail d’une déclaration; 

- le téléchargement de la déclaration au format `Excel` ; 

- l’identification des déclarations en attente d’action. 

## 5.4.2 Remplissage d’une déclaration
Le portail doit permettre le remplissage d’une déclaration à partir du compte opérateur actif. 

L’opérateur ne peut remplir une déclaration que si elle a été créée en amont par le BNIA. 

## 5.4.3 Saisie d’une déclaration
Le portail doit proposer la déclaration selon le format unique décrit dans le module `DSM` du back-office BNIA. 

Les colonnes hors du périmètre déclaratif du type d’opérateur connecté doivent être grisées et non saisissables. Elles restent visibles et conservent leur numéro. 

Le portail doit offrir des écrans lisibles, des totaux compréhensibles et un parcours qui limite les erreurs de saisie. 

Les champs de saisie sont les mêmes que sur le back-office BNIA. 

## 5.4.4 Import d’un fichier
Le portail doit permettre : 

- l’import du modèle `Excel` BNIA; 

- ou l’import de fichiers issus des logiciels opérateurs selon le format de fichier qui sera défini et communiqué aux éditeurs; 

- le préremplissage de la déclaration à partir du fichier importé; 

- l’identification des lignes nécessitant un contrôle ou un justificatif.

<!-- p.65 -->
## 5.4.5 Justificatifs
Certains mouvements doivent pouvoir être justifiés par un document joint. 

Le portail doit permettre : 

- d’identifier les lignes concernées; 

- de déposer les justificatifs attendus; 

- de visualiser les justificatifs déjà transmis; 

- de conserver le lien entre la ligne de déclaration et le document déposé. 

## 5.4.6 Transmission des DRM
Un opérateur de type producteur doit transmettre, avec sa déclaration, les `DRM` adressées aux douanes sur la période déclarée. Cette obligation ne s’applique pas aux négociants. 

Le portail doit attendre une `DRM` au format `PDF` par mois compris dans la période de déclaration. Une `DSM` producteur annuelle appelle donc `12 DRM` . 

Le portail doit permettre : 

- d’afficher la liste des mois attendus et l’état de dépôt de chacun; 

- de déposer la `DRM` de chaque mois; 

- de remplacer une `DRM` déposée tant que la déclaration n’est pas validée; 

- de retrouver les `DRM` transmises depuis l’historique des déclarations. 

Les `DRM` déposées sont archivées et rattachées à la déclaration et au mois concerné. Elles restent consultables par le BNIA depuis le back-office en cas de contrôle. 

## 5.4.7 Contrôles de cohérence
Le portail doit appliquer les mêmes contrôles de cohérence que le back-office BNIA, en saisie manuelle comme après import d’un fichier. 

Les anomalies doivent être restituées de façon directement exploitable par l’opérateur : 

- signalement au niveau de la déclaration, de l’onglet et de la ligne concernée; 

- libellé explicite du motif de l’anomalie; 

- accès direct à la ligne à corriger. 

La validation de la déclaration doit être bloquée tant que : 

- une incohérence de stock, de total ou de reprise du stock de période précédente subsiste; 

- un justificatif attendu est manquant; 

- une `DRM` attendue sur la période est manquante, pour un opérateur de type producteur; 

- une ligne d’achat ou de vente reste rattachée à un `Contrat achat Armagnac` non exécuté.

<!-- p.66 -->
L’opérateur doit pouvoir enregistrer sa saisie en cours et y revenir tant que la déclaration n’est pas validée. 

## 5.4.8 Validation et suivi
Une fois la déclaration complétée et les contrôles de cohérence levés, l’opérateur doit pouvoir la valider et la transmettre au BNIA. 

La validation doit être signée électroniquement par l’opérateur. 

Après validation : 

- la déclaration passe en attente de facturation côté BNIA; 

- la déclaration ne peut plus être modifiée par l’opérateur; 

- le portail doit afficher son état d’avancement; 

- toute correction ultérieure doit passer par le BNIA et nécessitera une nouvelle signature électronique de la part de l’opérateur. 

## 5.4.9 Signature électronique de la DSM
La signature électronique permet au BNIA de prouver qui a transmis la déclaration, ce qu’elle contenait et à quelle date. L’opérateur ne peut plus contester ensuite le contenu qu’il a signé. 

La plateforme doit s’adosser à un service tiers de signature électronique accessible par `API` , de type `SSL.com` , `DocuSign` ou `Yousign` . 

Le processus de signature doit se dérouler ainsi : 

1. génération du `PDF` de la `DSM` remplie, à partir des données ayant passé les contrôles de cohérence; 

2. signature du `PDF` par l’opérateur via le service retenu, dans une expérience intégrée au portail; 

3. stockage et archivage du `PDF` signé, rattaché à la déclaration et à sa période; 

4. mise à disposition du `PDF` signé dans le back-office BNIA, retrouvable depuis la déclaration en cas de contrôle. 

Le parcours de signature doit rester dans le portail : l’opérateur ne doit pas être redirigé vers une interface tierce pour signer sa déclaration. 

Le `PDF` signé doit rester téléchargeable par l’opérateur depuis l’historique de ses déclarations. 

Le chiffrage de la signature électronique des `DSM` doit être fourni et distinguer : 

- les coûts d’intégration et de paramétrage du service retenu; 

- les coûts d’abonnement; 

- les coûts variables liés au volume de signatures, rapportés au nombre de `DSM` attendues par an.

<!-- p.67 -->
# 5.5 Contrats Armagnac
Le portail doit permettre aux opérateurs de consulter, saisir et valider les contrats Armagnac qui les concernent. Il couvre le contrat achat Armagnac, le pluri-annuel Armagnac et la proposition d’engagement Armagnac. 

## 5.5.1 Historique des contrats
L’opérateur doit pouvoir retrouver les contrats sur lesquels il intervient comme acheteur ou comme vendeur. 

Le listing doit permettre : 

- la recherche par date; 

- la recherche par numéro; 

- la recherche par acheteur ou vendeur; 

- la consultation de l’état de validation; 

- l’ouverture du détail d’un contrat. 

## 5.5.2 Contrat achat Armagnac
Le portail doit permettre la saisie d’un contrat d’achat ou de vente d’Armagnac entre opérateurs. 

Lors de la création, le contact connecté doit se désigner comme acheteur ou vendeur pour le compte opérateur actif. Le contrat saisi doit ensuite être visible par les deux parties. 

Les champs de saisie et règle de gestion d’un contrat achat Armagnac sont les mêmes que pour la saisie côté back-office BNIA. Un opérateur qui saisi un contrat doit obligatoirement s’identifier comme acheteur ou vendeur. 

## 5.5.3 Validation d’un contrat
Le portail doit permettre à chaque partie de valider un contrat Armagnac depuis son propre accès. 

Le système doit enregistrer : 

- la validation acheteur; 

- la validation vendeur; 

- la date et le compte ayant réalisé l’action. 

Le BNIA conserve la validation finale dans le back-office. 

## 5.5.4 Pluri-annuel Armagnac
Le portail doit permettre la consultation et la création des contrats pluri-annuels Armagnac.