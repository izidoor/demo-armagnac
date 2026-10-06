# Interactions avec l'extérieur : ce que demande vraiment le CDC

> Analyse de lecture du CDC v1.1 (2026-07-30) et de la spec AGI. Document de travail, à compléter avec les réponses du BNIA.
> Références : `§` = section du CDC, `p.` = page du PDF, `C.n` = annexe C.

## 1. Grille de lecture

Le CDC écrit « doit » presque partout, ce qui ne permet pas de distinguer à lui seul une exigence ferme d'un souhait. On trie donc selon la formulation :

| Statut | Signal dans le texte | Exemple |
|---|---|---|
| **Imposé** | Produit, flux ou format nommé, sans alternative | Zeendoc (§6.2), Otentik/CEV (§4.8.7), matrice Excel DSM négociant (§4.4.10) |
| **Obligatoire** | « doit » sur un comportement précis | Signature de la DSM, détection des doublons ODG |
| **Indicatif** | « de type », « ou équivalent », « devra être proposé », « préconisé » | Fournisseur de signature, XML ou JSON, Next.js/Symfony |
| **Dépendant d'un tiers** | « Une prise de contact sera nécessaire » | AGI, Zeendoc, TimeTonic, douanes, agence armagnac.fr |
| **À arbitrer** | « sera arbitré prochainement » | Pluri-annuels et propositions d'engagement |

Et pour chaque point, quatre niveaux de description : **précis** (on peut spécifier sans poser de question), **partiel**, **flou**, **absent**.

## 2. Vue d'ensemble

| Interface | Statut | Comportement attendu | Contrat technique (protocole, format, auth) | Données (annexe C) | Chiffrage séparé demandé | Démarrage (§9) |
|---|---|---|---|---|---|---|
| **ODG** (contrats vin, fins de travaux) | Obligatoire ; outil source en transition | **Précis** | **Absent** | **Précis** | Non | Non cité |
| **Zeendoc** | **Imposé** | **Précis** | **Absent** | Partiel (2 champs) | Non | Synchro « plus riche » différée (§9.4) |
| **Signature électronique** | Obligatoire ; fournisseur indicatif | Partiel | **Absent** | **Absent** | **Oui** (3 postes) | Non cité |
| **Otentik / CEV** | **Imposé** | Partiel | **Absent** | **Absent** | **Oui** (3 postes) | Non cité (le bloc export l'est) |
| **Import Excel BNIA** (DSM, ventes export) | **Imposé** | **Précis** | Partiel (matrices non reçues) | **Précis** | Non | Inclus |
| **Format XML/JSON et logiciels opérateurs** | Indicatif | **Flou** | **Absent** | **Absent** | Non | Différé (§9.4) |
| **Reprise InteGraal / pro.armagnac.fr** | Obligatoire (vues SQL imposées) | **Précis** sur le périmètre | Partiel | Partiel (champs sans types) | **Oui** (« document dédié au chiffrage », §1.4) | Prérequis, non exhaustif (§9.2, 9.3.3) |
| **Reprise armagnac.fr** | Obligatoire (prérequis du lancement, contenus « utiles » seulement), dépend d'une agence | Partiel | **Absent** | **Absent** (aucune vue, contrairement à AGI) | Non | Contenus « utiles dès l'ouverture » (§9.3.3), le reste après (§9.4) |
| **E-mails** | Obligatoire | Partiel | Absent | n/a | Non | n/a |
| **Douanes (DAE, DRM, DAI)** | Pas d'interface : dépôt de PDF | **Précis** | n/a | **Précis** | Non | Inclus |
| **Annuaire des opérateurs d'armagnac.fr** | **Hors périmètre déclaré, frontière non décrite** | **Absent** | **Absent** | Partiel (champs de diffusion en C.1) | Non | n/a |
| **Hébergement** | Indicatif | **Flou** | n/a | n/a | Non | n/a |

## 3. Sept constats transversaux

1. **Le CDC dit lui-même que les interfaces ne conditionnent pas l'ouverture.** Le premier temps de mise en service doit prévoir « des modalités de contrôle ou de secours » quand des interfaces ne sont pas industrialisées (§9.1). La mise en service « ne doit pas dépendre […] de l'automatisation complète de toutes les interfaces » (§9.3), et les synchronisations plus riches avec Zeendoc et les imports avancés de DSM viennent après (§9.4). Un démarrage avec repli manuel est donc explicitement acceptable.

2. **La liste officielle des passerelles est incomplète.** Le §6 déclare que les passerelles « explicitement attendues » sont l'ODG et Zeendoc, et l'annexe D ne dessine que ces flux. Or la signature électronique (§5.4.9) et Otentik (§4.8.7) sont exigées ailleurs, avec chacune un chiffrage séparé. Ces deux exigences sont dispersées dans les chapitres métier : un candidat qui ne lit que §6 et D les ignore et sous-chiffre.

3. **Le CDC décrit des comportements, jamais des contrats techniques.** Pour aucune interface on ne trouve de protocole, de format de message, d'authentification, d'environnement de test ou de volumétrie. Cinq tiers doivent être contactés pour l'apprendre : TimeTonic ou son intégrateur, Zeendoc ou son revendeur, AGI, les douanes avec l'exploitant d'Otentik, l'agence d'armagnac.fr.

4. **Ce qui est précis est ce qui se spécifie sans le tiers.** Idempotence, doublons, rejeu sans doublon, statuts d'échange, journal, « une erreur ne bloque jamais la consultation » : tout cela est décrit (§6.1.5, 6.1.6, 6.2.3). C'est implémentable derrière un port avec un adapter simulé, sans connaître les API réelles.

5. **Aucune volumétrie n'est donnée** : ni nombre d'opérateurs, ni DSM par an, ni certificats, ni factures. Or le coût variable de la signature doit être « rapporté au nombre de DSM attendues par an » (§5.4.9). Le CDC demande un chiffrage dont il ne fournit pas l'hypothèse de base.

6. **Les dépendances humaines sont fortes.** AGI est l'éditeur du système remplacé et doit fournir les vues SQL (§7.1.4, 7.2.2). L'outil ODG change (Innovagro vers TimeTonic) sans date. Les douanes doivent s'engager sur Otentik. Rien de cela n'est sous le contrôle du prestataire.

7. **La frontière avec armagnac.fr est le point le moins défini du CDC.** La reprise de contenus n'a ni vue, ni technologie, ni chiffrage séparé (AGI en a un). Surtout, la question de savoir quelle base fait foi pour les données publiques des opérateurs, InteGraal ou l'annuaire du site, n'est posée nulle part. Selon la réponse, un **flux sortant plateforme vers armagnac.fr** peut devenir nécessaire : il ne figure ni au §6 ni à l'annexe D, et n'est pas chiffré (voir §4.10).

## 4. Analyse par interface

### 4.1 ODG (§6.1 p.80-82, §4.5.2, §4.6, annexe D, C.4, C.5)

**Exigé.** Flux unique ODG vers BNIA des contrats vin et des fins de travaux de distillation ; rattachement à une fiche opérateur ; contrôle de présence des identifiants, de complétude, de cohérence des dates et lignes ; détection des resynchronisations ; distinction création, mise à jour, rejet ; aucun doublon quand une fin de travaux est revalidée ou retransmise, tout en conservant les événements reçus ; supervision (horodatage, objets reçus, intégrés, mis à jour, rejetés, motifs, historique des rejets) dans un écran ou journal dédié. Les objets restent non modifiables côté BNIA et portail. Le dispositif doit rester « compatible avec l'outil appelé à le remplacer ».

**Décrit précisément.** Les contrôles, la supervision, les champs de traçabilité (n° de contrat ODG, n° de déclaration ODG, source du flux, date de dernière synchronisation, n° VISA).

**Flou ou absent.**
- Transport, format, authentification, fréquence, volumétrie : « les modalités techniques d'échange devront être arrêtées » (annexe D, p.139).
- Le flux est décrit comme poussé par l'ODG (« l'ODG synchronise vers le BNIA »), sans dire par quel canal ni qui développe côté ODG.
- Date et nature de la bascule Innovagro vers TimeTonic. Exiger la compatibilité avec un système non encore choisi est une exigence sans contenu.
- Le « n° VISA » n'est défini nulle part.
- La table de correspondance des identifiants est « gérée côté ODG » (§6.1.4), alors que la fiche opérateur porte aussi un « identifiant tiers ODG » (C.1) : on ne sait pas où est la référence.
- Les pluri-annuels et propositions d'engagement vin ne font pas partie du flux (§6.1.3 ne cite que contrats vin et fins de travaux) : ils sont **saisis dans le portail** (§5.6.2, 5.6.3). « Vin » ne veut donc pas dire « ODG ».

### 4.2 Zeendoc (§6.2 p.83, §4.9.8 p.46, §5.10, C.8 p.124)

**Exigé.** Dépôt automatique des PDF de factures ; retour d'état « lorsqu'il est disponible », en poussé ou par interrogation au choix ; états d'échange distingués (en attente de dépôt, déposée, rejetée, en anomalie) ; échec identifiable ; rejeu sans doublon ; historique des retours ; journal exploitable ; une erreur ne bloque jamais la consultation d'une facture. La plateforme conserve ses PDF et son historique : Zeendoc n'est jamais la source de vérité (annexe D). Pas d'export comptable. Le portail affiche au minimum « en attente de paiement » ou « payée » (§5.10.3).

**Flou ou absent.**
- API de Zeendoc (existence, authentification, quotas) : « prise de contact nécessaire avec Zeendoc ou le revendeur ».
- Le retour d'état est laissé au choix « poussé ou interrogé » : rien n'est tranché.
- Aucune liste des états Zeendoc. L'annexe C n'a qu'un champ « état documentaire ou de paiement » qui mélange deux notions.
- Comment « le numéro de facture » retrouve la facture côté Zeendoc (index, classement).
- Format du PDF : la facturation électronique est citée comme finalité de Zeendoc (§6.2.1) sans aucun format de facture structuré mentionné. À clarifier.
- Aujourd'hui il n'y a « pas d'interconnexion automatisée » (§2.1) : le dépôt manuel actuel est le repli naturel.

### 4.3 Signature électronique de la DSM (§5.4.8-5.4.9 p.66, §4.4.4)

**Exigé.** La validation de la DSM par l'opérateur doit être signée électroniquement. Service tiers par API, « de type SSL.com, DocuSign ou Yousign » (donc fournisseur libre). Processus : PDF généré après contrôles, signature par l'opérateur dans le portail, archivage rattaché à la déclaration et à sa période, mise à disposition du BNIA. **Aucune redirection vers une interface tierce.** PDF signé téléchargeable par l'opérateur et consultable sur les périodes clôturées. Toute correction par le BNIA appelle une nouvelle signature. Chiffrage séparé : intégration et paramétrage, abonnement, coûts variables.

**Flou ou absent.**
- **Niveau de signature** (simple, avancée, qualifiée) : non dit. Le but affiché est de prouver « qui, quoi, quand », sans exigence juridique précise.
- **Qui signe** (le contact connecté, le représentant légal) et comment son identité est établie (le compte portail avec 2FA suffit-il ?).
- Conservation (durée, horodatage, format), indisponibilité du service, comportement pour une DSM saisie par le BNIA (le §4.4.4 ne parle que du parcours portail).
- **Aucun champ en annexe C** : ni PDF signé, ni date, ni signataire, ni empreinte.
- La contrainte « sans redirection » dépend de ce que chaque fournisseur permet d'intégrer : à vérifier avant de chiffrer.

### 4.4 Otentik et certificat CEV (§4.8.7 p.43, §5.8.2, C.7)

**Exigé.** Le certificat doit être délivré comme CEV rattaché à Otentik. La plateforme doit transmettre les données du certificat à l'émission, récupérer le QR code et l'apposer, joindre les justificatifs (facture, DRM) et conserver la trace des échanges. Le CEV s'ajoute au PDF sans le remplacer. Chiffrage séparé : intégration et paramétrage, coûts récurrents, coûts variables au volume.

**Ce que dit le CDC en plus.** « Le principe a été validé avec les douanes nationales, qui ont donné leur accord au développement de ce raccordement. » C'est une déclaration d'intention, pas une spécification. Et un encadré avoue que les contraintes constatées « peuvent conduire à revoir l'articulation entre l'édition PDF et le certificat CEV » : c'est le seul endroit où le CDC reconnaît que la conception pourrait changer.

**Flou ou absent.** Tout le contrat technique : API ou non, documentation, habilitation du BNIA, environnement de test, format transmis, contenu de la réponse (image, URL, identifiant), erreurs, annulation ou rectification d'un certificat déjà émis. Qui supporte les coûts récurrents et variables (le BNIA, les opérateurs) n'est pas dit. Aucun champ en annexe C.7. Absent de §6 et de l'annexe D.

### 4.5 Imports Excel au format BNIA (§4.4.10 p.28-29, §4.7.3 p.39-40, §5.4.4, §5.7.1)

**Exigé.** Pour la DSM : saisie, import du fichier au format BNIA (la matrice négociant existante sert de base, avec grisage selon le type d'opérateur), jamais de DSM validée sans contrôles joués. Pour l'export : modèle Excel unique, contrôles avant enregistrement ligne par ligne (conformité, formats, pays, cohérence millésime/clé/compte d'âge, lignes déjà enregistrées), anomalies avec motif et ligne, correction puis réimport, et pour l'import fait par l'opérateur contrôle BNIA avant validation définitive.

**Décrit précisément.** Les contrôles et les parcours. **Flou :** le contenu exact des matrices, qui sont les pièces de l'annexe E (non reçues) ; le critère d'identité d'une ligne export « déjà enregistrée sur la même période » ; les conversions d'unités (litres d'alcool pur en DSM, hl.l,cl à l'import export).

### 4.6 Format informatique et logiciels opérateurs (§4.4.10 p.28, §4.7.3.1, §5.4.4)

**Exigé.** Un format XML ou JSON « devra être proposé » aux éditeurs, pour les DSM et pour les ventes export, afin qu'ils produisent une sortie mensuelle. Les logiciels « à traiter prioritairement » sont GrandCru, Isavigne et ACCISES. Le portail doit importer des fichiers issus de ces logiciels « selon le format qui sera défini et communiqué aux éditeurs ».

**Lecture.** L'obligation ferme porte sur la **définition d'un format et son import côté plateforme**. La coopération des éditeurs n'est ni dans le périmètre ni garantie. Il n'y a ni schéma, ni versionnement, ni canal de transmission.

**Ambiguïtés.** « Traiter prioritairement » ces trois logiciels : concevoir le format pour eux, ou développer des connecteurs ? ACCISES n'est pas défini dans le glossaire. Le §9.4 renvoie à plus tard les « imports informatiques avancés pour les DSM, au-delà des parcours nécessaires au démarrage ».

### 4.7 Reprise de données (chap. 7 p.84-88, §1.4, §9.2-9.3, spec AGI)

**Exigé.** S'appuyer sur des vues SQL (imposé). Périmètre listé : référentiel des opérateurs et ses sous-ensembles, DSM, factures, contrats, suivis export, destinataires, vendeurs, paramétrages, objets de pro.armagnac.fr, contenus d'armagnac.fr. Conserver la clé de ligne DSM, les liens factures-DSM-contrats, unifier les deux sources de distillation sans doublon, distinguer les cotisations négociants et producteurs.

**Précis.** Le périmètre, objet par objet. **Flou ou absent.**
- Le CDC renvoie par deux fois à une « prise de contact avec AGI » pour l'accès aux vues.
- La spec AGI liste des champs **sans aucun type**, avec quelques « à confirmer » (par exemple « Dest Code »), et dit elle-même qu'elle ne définit pas de schéma exécutable.
- Volumétrie et qualité des données : rien.
- armagnac.fr dépend d'une agence et d'un calendrier de désactivation à établir (voir §4.10).

**Tension.** La reprise est « un prérequis du lancement » (§9.2), mais le démarrage « ne doit pas dépendre de la reprise exhaustive » (§9.3). La lecture cohérente : reprise nécessaire, mais partielle acceptable.

### 4.8 E-mails (§5.1.2 p.59, §4.2.7 p.17, §5.12 p.77)

Exigés : activation de compte, réinitialisation de mot de passe, code de double authentification par e-mail, messages aux contacts. Le fournisseur d'envoi, l'expéditeur et les modèles ne sont pas précisés, et le canal e-mail des notifications n'est pas dit explicitement. Faible enjeu.

### 4.9 Douanes : DAE, DRM, DAI (§4.4.5-4.4.6, §5.4.6, §5.8.1, C.3.3-C.3.4)

Aucune interface automatisée : les opérateurs déposent des PDF, le BNIA les pointe (champ « résultat du contrôle »). C'est précis. Le seul raccordement douanier réclamé est Otentik. Le §3.1 annonce pourtant des « interfaces utiles avec […] les justificatifs douaniers » : ce que le détail des chapitres ne confirme pas.

### 4.10 Site armagnac.fr : annuaire des opérateurs, contenus, boutique (§1.4, §2.1, §2.3.2, §5, §7.3, §9, glossaire, §4.3, C.1, AGI 2.1)

Le CDC n'a pas de chapitre sur armagnac.fr : les mentions sont dispersées. Sous ce seul nom se cachent **trois sujets distincts**.

| Sujet | Nature | Statut |
|---|---|---|
| Reprise unique de contenus vers le portail (§7.3) | Opération de migration | Obligatoire, prérequis du lancement |
| Annuaire public des opérateurs | Relation permanente entre la plateforme et le site | « Hors périmètre », sans dire ce qui circule |
| Désactivation de fonctions de l'espace professionnel du site | Calendrier à établir avec l'agence | Dépend d'un tiers |

**Ce que dit le CDC.**
- Aujourd'hui, armagnac.fr est un site grand public avec un espace professionnel réservé qui sert à des ressources documentaires, des actualités et informations pratiques, une photothèque, et à « la gestion de certaines informations liées à la fiche annuaire opérateur » (§2.3.2, p.9). Le site « porte l'annuaire opérateur » (§2.1, p.7).
- Sont exclus : l'évolution du site public et « la gestion de la fiche opérateur alimentant l'annuaire public » (§1.4) ; la fiche annuaire reste « hors du périmètre **direct** » (glossaire, p.94).
- À reprendre (§7.3.1, p.88) : actualités (catégories, articles, photos), informations pratiques, ressources documentaires (catégories, documents), photothèque (catégories, photos), données de la boutique (catégories, produits, photos). Pas de reprise « fonctionnelle » du site (§7.3.2).
- Une coordination avec « l'agence en charge d'armagnac.fr » est nécessaire pour identifier les données, les modules à masquer ou maintenir hors du portail, et un calendrier de désactivation (encadré p.88).
- Cette reprise est un prérequis du lancement (§9.2), limitée aux contenus « utiles dès l'ouverture » (§9.3.3) ; les reprises complémentaires viennent après (§9.4).

**Ce qui est décrit.** La cible côté plateforme : gestion des contenus (§4.12, C.11), contenus du portail (§5.3), boutique (§4.13, §5.9, C.12). La liste des familles de contenus à reprendre.

**Ce qui est flou ou absent.**
- Aucune vue, aucune technologie du site, aucun mode d'extraction, aucune volumétrie, aucun chiffrage séparé. La spec AGI ne couvre que InteGraal et pro.armagnac.fr.
- **Les contenus « utiles dès l'ouverture »** ne sont pas listés. Les modules de contenus ne figurent ni dans le socle BNIA (§9.3.1) ni dans celui du portail (§9.3.2), alors que leur reprise est exigée au lancement. Qui les maintient avant leur déploiement ?
- **La boutique** n'apparaît pas dans les usages de l'espace professionnel (§2.3.2) mais ses données sont à reprendre depuis le site (§7.3.1). L'historique des commandes n'est mentionné nulle part dans la reprise.
- **La désactivation** suppose un travail côté agence, hors périmètre : le prestataire doit-il le piloter ou seulement s'y coordonner ?
- Les comptes de l'espace professionnel du site ne sont pas à reprendre (seuls ceux de l'extranet le sont, §7.2.1) : le CDC ne dit pas comment les opérateurs y accéderont.

#### La question centrale : annuaire du site ou référentiel opérateur InteGraal, quelle donnée fait foi ?

Les documents ne permettent pas de répondre. Ce qu'on peut établir :

- **Le référentiel InteGraal contient des données à vocation de publication.** Un indicateur « Web » par marque (AGI 2.1.5 ; C.1.4.1 : « peut être diffusée sur le web »), des coordonnées GPS « utiles à la localisation ou à certains usages de diffusion » (C.1.1), un nom commercial « d'usage ou de diffusion », un « accord de diffusion général … sur les supports autorisés », et un bloc de type annuaire touristique.
- **Le CDC cible reprend ces champs dans le back-office** (§4.3.2.3 à 4.3.2.5) et dans la reprise (§7.1.1), alors qu'il exclut « la fiche opérateur alimentant l'annuaire public » : formulation qui suppose qu'une fiche alimente l'annuaire, sans dire laquelle.
- **L'espace professionnel du site sert à gérer des informations de la fiche annuaire** (§2.3.2) : des données sont donc aussi saisies sur le site.
- Un champ « code d'accès Internet historique » (C.1) est à « reprendre, neutraliser ou migrer selon la cible d'authentification » : lien avec un accès au site ou à l'extranet, non précisé.

| Famille | Champs dans le référentiel InteGraal (AGI 2.1, C.1) | Indice de diffusion publique |
|---|---|---|
| Identité et localisation | nom commercial, adresse, GPS, site web, site commercial | « usages de diffusion » (C.1) |
| Marques et circuits | marques France et Export, circuits de distribution | Indicateur « Web » par marque |
| Accueil et œnotourisme | visites, labels touristiques, langues parlées, médailles et concours, produits commercialisés, paiement CB, dégustation, accueil de groupes, présentation du domaine en français et anglais, adhésion œnotourisme BNIA | Vocabulaire d'annuaire touristique ; pas d'indicateur explicite |
| Consentements | accord de diffusion général, accord de diffusion des millésimes, accord statistique | « supports autorisés » ; usage statistique explicite pour les millésimes |
| Données internes | commentaire interne, alambics, dérogations, déclarations de stock | « non diffusable » (C.1) |

**Trois lectures possibles**, avec ce qu'elles impliquent :

| Lecture | Qui fait foi | Conséquences |
|---|---|---|
| **A** | InteGraal ; l'annuaire est une copie publiée | Après la bascule, la plateforme doit alimenter armagnac.fr : **flux sortant non décrit, non chiffré, absent de l'annexe D** ; format, fréquence et respect des accords de diffusion à définir |
| **B** | L'annuaire du site, au moins pour une partie (saisie par les opérateurs) | Les champs du back-office seraient périmés ou à ne pas reprendre ; reprendre depuis AGI importerait des données anciennes ; il faudrait une reprise depuis le site, **pour laquelle aucune vue n'existe** |
| **C** | Mixte, champ par champ, avec synchronisation dans un ou deux sens | Règles de précédence et gestion des conflits à définir ; les deux lectures précédentes s'appliquent à la fois |

Le texte penche vers A (« alimentant », champs de diffusion dans la fiche cible, « hors périmètre direct »), mais le §2.3.2 penche vers B ou C. Rien ne tranche.

**Hypothèse de travail, non figée.** Tant que ce n'est pas tranché : modéliser les champs à vocation de diffusion comme un bloc du contexte `operateurs`, fidèle à C.1, sans rien publier ni construire de flux ; ne rien supposer de la reprise depuis le site.

### 4.11 Hébergement (§8.4 p.90)

« Adapté » et « cohérent » avec l'exploitation, l'ouverture sécurisée du portail et les échanges : aucune exigence mesurable (disponibilité, localisation, sauvegarde, reprise après incident, supervision, environnements). Non chiffrable sans précision.

## 5. À demander au BNIA

Une version rédigée, classée par thème, est dans [`mail-bnia.md`](mail-bnia.md).

### Pour pouvoir chiffrer

1. Les pièces de l'annexe E (matrices Excel, factures, certificats).
2. **Les volumes** : nombre d'opérateurs (producteurs et négociants), DSM par an, certificats par an, factures par an, nombre de DSM signées par an.
3. **Signature** : niveau attendu (simple, avancée, qualifiée), qui signe, durée de conservation, fournisseur déjà sous contrat ou non.
4. **Otentik** : contact et documentation côté douanes, calendrier, raccordement dans le lot ou en option.
5. **Reprise AGI** : modalités d'accès aux vues SQL, jeu de données de test, types et longueurs des colonnes, profondeur d'historique.
6. **Reprise armagnac.fr** : technologie du site, mode d'accès aux contenus, volumes.

### Pour cadrer

7. **ODG** : contact TimeTonic ou intégrateur, date de bascule, sens et canal des échanges, qui développe côté ODG.
8. **Zeendoc** : API, compte de test, états renvoyés, format de facture attendu.
9. **Logiciels opérateurs** : un format seul, ou des connecteurs pour GrandCru, Isavigne, ACCISES ?
10. **Démarrage** : quelles modalités de secours pour les interfaces non prêtes (§9.1) ?
11. **Hébergement** : contraintes de localisation, disponibilité, sauvegarde.

### Pour trancher l'annuaire et armagnac.fr (priorité haute)

12. L'annuaire est-il alimenté par InteGraal, par la saisie sur le site, ou par les deux ?
13. Pour les données communes, quelle base fait foi aujourd'hui ?
14. La plateforme devra-t-elle alimenter l'annuaire après la bascule ? Quelles données, à quelle fréquence ?
15. Où les opérateurs modifieront-ils leur fiche annuaire ?
16. Quels contenus sont indispensables dès l'ouverture ?
17. La boutique est-elle hébergée sur armagnac.fr ? Faut-il reprendre l'historique des commandes ?
18. Qui pilote la désactivation côté site, et à quelle date ?

### À garder pour l'oral (montrent des incohérences du CDC)

États de la DSM (§4.4.2 contre C.3.1.1) ; deux ou trois niveaux de détail du certificat (§4.8.5 contre C.7.2.1) ; taux de cotisation par colonne (ancien système) contre montant unique (§4.11.2) ; volumes export présents dans la DSM et dans les suivis ; champs inexpliqués des pays (CA, CA1 à CA5, Âge) ; « Dest Code » et « n° VISA ».

## 6. Pistes pour la suite (non figées)

- La question de l'annuaire (§4.10) conditionne la forme de `operateurs`, une éventuelle passerelle sortante vers armagnac.fr et la reprise. Tant qu'elle n'est pas tranchée, ne rien publier et ne rien supposer de la reprise depuis le site.
- Chaque interface se prête à un **port** et à des **adapters** (réel, simulé, de repli manuel), puisque le comportement est décrit et le contrat technique non. Un **journal d'échanges commun** (états, rejets, rejeux) porte ce que §6.1.6 et §6.2.3 exigent.
- Le §9 autorise une offre en deux niveaux : démarrage avec procédures de secours, puis automatisation. À arbitrer au moment de rédiger l'offre.
- Les deux interfaces **absentes de §6 et D** (signature, Otentik) méritent d'être nommées en tête de l'offre : un comité qui voit un candidat les avoir repérées lit le CDC plus soigneusement que les autres.

## 7. Corrections à reporter dans les autres documents

- `contextes.md` : `contratsvin` est décrit en « lecture seule » avec ses pluri-annuels et propositions. Seul le **contrat vin** vient de l'ODG ; pluri-annuels et propositions vin sont saisis dans le portail (§5.6.2-5.6.3). *Corrigé dans `contextes.md` lors de la révision de l'ordre.*
- `questions-ouvertes.md` : Q2 et H3 parlent d'un barème « par colonne » ; le CDC cible a un montant unique par type de cotisation (§4.11.2, C.10.3). H7 (adapters simulés) reste valable.
- `contextes.md`, `operateurs` : le contexte porte des champs à vocation de diffusion publique dont la source de vérité est inconnue (voir §4.10). À traiter à l'étape 2 : modéliser sans publier.
- `contextes.md`, étapes 13 et 14 : `boutique` et `contenus` sont placés en fin d'ordre, mais une reprise de leurs contenus est exigée « dès l'ouverture » (§9.3.3). À réexaminer selon la réponse du BNIA sur les contenus indispensables.
