# Vision : contrat d'achat Armagnac

Document de vision de l'utilisateur, reçu le 2026-10-23, reproduit tel quel. Il prime sur le CDC
(§4.5, §5.5, C.4) : l'écart est assumé. Pièce jointe : exemple de contrat transmis par le BNIA
(`Contrat_Exemple.pdf`, formulaire CA-05, campagne 2026-2027), avec les retraits indiqués à la fin.

---

# Module contrat d'achat armagnac

Nous allons commencer les specs du module contrat.
Attention, je ne veux absolumment implémenter ce module selon les recommandations du cahier des charges.
Je sais qu'elles sont parcellaires, voire même fausses.
Nous allons donc nous en écarter vraiment, et c'est assumé.
L'objectif est d'avoir un effet démo sur téléphone.

Le périmètre : nous allons nous occuper uniquement des contrat d'achat d'Armagnac.
Hors périmètre : contrat d'engagement, contrat pluriannuel, tous les contrats vins.


Estimation du stock d'un opérateur :
- les opérateurs déclarent périodiquement leur DSM, ce qui permet d'établir une état du stock à a fin de la chaque période.
- mais entre deux fins de période, comment estimmer le stock d'un opérateur ?
  - on part du stock de la dernière DSM
  - on ajoute les retiraisons ACHAT des contrats d'armagnac validées depuis
  - on retranche les retiraisons VENTE des contrats d'armagnac validées depuis
  - on ajoute les fins de travaux de distillations validés depuis ( hors contexte ici)



- un opérateur initie/remplit un projet de contrat sur le portail. Il peut être l'acheteur ou le vendeur.
  - il remplit le contenu
    - s'il est le vendeur, un contrôle sur ses stocks estimés est effectuée à la saisie et lors de l'enregistrement sur le serveur
    - s'il est l'acheteur, il ne peut pas voir les stocks du vendeur, donc pas de contrôles
    - il valide
    - le contrat apparaît dans la liste des projet de contrat
      - de l'acheteur
      - du vendeu
      - du BNIA
  - l'autre partie reçoit un mail de notification pour valider le contrat
    - si l'autre partie est le vendeur, le système effectue le contrôle du stock
      - si ok, le vendeur peut approuver le projet de contrat
      - si pas ok, l'approbation est bloqué.
        - le vendeur peut refuser définitivement ce projet de contrat
        - le vendeur a la possibilité de modifier le contenu pour qu'il soit en cohérence avec son stock
          - il valide,
          - le projet a été modifié -> l'autre partie reçoit un mail de notification
    - si l'autre partie est l'acheteur
      - il peut refuser définitivement ce projet de contrat
      - il peut modifier le contenu
        - il valide,
        - le projet a été modifié, -> l'autre partie reçoit un mail de notification
  - quand le projet de contrat, sans modification, a été validé par les deux parties
    - le contrat est déclaré "conclu"
    - le contrat apparaît dans la liste des contrats conclus
      - de l'acheteur
      - du vendeur
      - de l'acheteur
    - les volumes déclarées et les dates prévionnelles de retiraison sont utilisées pour l'estimation du stock de chaque opérateur
- quand l'acheteur veut effectuer une retiraison
  - depuis le portail, il sélectionne le contrat et il déclare le contenu de la retiraison
    - il déclare si cette retiraison est la dernière du contrat
    - le contenu de la retiraison doit être un sous ensemble du contenu du contrat, ou de ce qu'il reste aprés les retiraison déja effectuées. Le système effectue un contrôle à la saisie et à l'enregistrement ET le vendeur DOIT toujours avoir en stock estimé les quantités nécessaires à la retiraison. Le système effectue un contrôle à la saisie et à l'enregistrement
      - si ce n'est pas le cas, il faut modifier la saisie
    - il valide
  - le vendeur reçoit une notification pour valider la retiraison
    - il peut valider
    - il peut modifier le contenu
      - il valide
      - la retiraison a été modifié -> l'acheteur reçoit un mail de notification
  - quand la retiraison a été validée sans modification par les deux parties,
    - elle apparait dans la liste des retiraisons du contrat
    - l'estimation du stock des opérateurs est impactée
    - la prochaine DSM sera préremplie par le système en prenant en compte la/les retiraisons effectuées pendant la période de la DSM
-  quand il a été déclaré qu'une retiraison était la dernière d'un contrat
  - le contrat est réputé "exécuté"
  - le contrat apparait dans la liste des contrat exécutés
    - de l'acheteur
    - du vendeur
    - du bnia

  Cas limites :
  - un contrat conclu peut-il être annulé?
    - oui, unilatéralement
      - l'autre partie est notifiée
      - ceci est porté au journal du contrat
  - est-il possible de déclarer, "hors retiraison", qu'un contrat n'aura plus de retiraison ?
    - oui, unilatéralement
      - l'autre partie est notifiée
      - ceci est porté au journal du contrat


  Important  :
  - il faut que les modifications effectuées ( projet de contrat, retiraison) soient enregistrées, de façon à pouvoir présenter la liste des modifications à l'autre partie quand elle devra revalider.


  Exploitation par le BNIA
  - dans le BO, le BNIA a une page "Mercuriales" qui affiche la synthèse des contrats par
    - compte d'âge, millésime ou dénomination (les achats/ventes se font toujours en BOIS, pas en VERRE -> pas de clé pour le suivi)
    - nbr de contrat sur une période
    - prix minimum, prix maximum, volume concerné
    - prix moyen ( moyenne pondérée par les volumes)

  Je te joins également un pdf d'un exemple de contrat d'achat transmis par le BNIA.
  - partie 4 : ne prend pas en compte "Echéancier", c'est hors context ici
  - partie 6: à supprimer, hors contexte ici
  - partie 7 : à supprimer
  - partie signature : il faut juste garder Acheteur et Vendeur
  - les mentions finales en bas du document sont à retirer

---

## Annexe : contenu de l'exemple de contrat (CA-05)

Relevé le 2026-10-23 depuis `Contrat_Exemple.pdf` (une page), identités masquées dans l'original.

| Partie du formulaire | Contenu |
|---|---|
| En-tête | « CONTRAT D'ACHAT D'ARMAGNAC conforme aux dispositions de l'article L. 631-24 du code rural et de la pêche maritime et aux dispositions de l'accord interprofessionnel du BNIA du 09 novembre 2012 » ; campagne (2026 - 2027) ; cadre « Visa du BNIA » : N° (503144), date (29/09/2026) |
| 1) Acheteur (négociant, bouilleur de profession, coopérative) | Nom, prénom ; adresse ; CP, ville ; téléphone ; N° BNIA ; N° SIRET |
| Vendeur | Nom, prénom ; adresse ; CP, ville ; téléphone ; N° BNIA ; N° SIRET ; N° CVI ; « par l'intermédiaire de …, courtier à … » |
| 2) Marché « conclu après agréage par l'acheteur » | Lignes : appellation, compte d'âge, millésime, volume, degré, volume AP, prix départ HT € / HL AP, prix départ HT € / litre volume. Exemple : Bas-armagnac, 16, 2009, 30.00, 47.00, 14.10, 1 750.00 ; Bas-armagnac, 7, 2018, 30.00, 48.20, 14.46, 1 300.00 |
| Caractéristiques complémentaires | Texte libre (vide). Mentions fixes : produits non retournables après prise en charge sauf non-conformité légale ou réglementaire ; qualité saine, loyale et marchande, AOC. Cases : Armagnac, Bas Armagnac (cochée), Armagnac-Ténarèze, Haut Armagnac, Blanche Armagnac |
| 3) Conditions et calendrier de retiraison | Texte libre (vide) |
| 4) Conditions de paiement | Cases : acompte de … ; comptant ; 30 j à compter de la fin du mois de livraison/retiraison (cochée). Échéancier : hors contexte (vision) |
| 5) Modalité de révision | Mention fixe : « S'agissant d'un contrat ponctuel, celui-ci est ferme. » |
| 6) Rattachement à un pluriannuel ou une proposition d'engagement | Non (coché) / Oui ; « ne pourra pas faire l'objet d'un renouvellement tacite ». À supprimer (vision) |
| 7) Préavis de rupture | Vide. À supprimer (vision) |
| Signatures | « Établi à … le 13/10/2026 » ; Le Vendeur, L'Acheteur, Vu le Courtier. Garder Acheteur et Vendeur (vision) |
| Mentions finales | Clause de réserve de propriété (art. 2367 C. civ.) ; « Le premier exemplaire revêtu du visa du BNIA vaut récépissé » ; « Un exemplaire du document d'accompagnement validé devra être impérativement adressé au B.N.I.A. ». À retirer (vision) |
