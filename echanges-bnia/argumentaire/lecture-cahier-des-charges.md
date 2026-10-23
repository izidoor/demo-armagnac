# Lecture du cahier des charges

Préparation de la présentation de l'offre au BNIA, mardi 27 octobre 2026 à 10h30. Répond à la question 12 de `statut-entreprise.md` : « Vous écrivez que notre cahier des charges manque d'informations essentielles. Lesquelles ? »

## Ce que le comité doit penser en sortant

**« Il a lu notre cahier des charges plus à fond que les autres, et il y a réfléchi plus à fond que les autres. »**

L'auteur du cahier des charges est un prestataire concurrent, candidat lui aussi, absent de la salle. Parler du document, jamais de son auteur ; laisser la démonstration faire la comparaison.

## Le constat

Le cahier des charges est un **dictionnaire** : il décrit ce que contient Integraal, module par module, champ par champ. Il ne décrit pas **comment la filière travaille** : la chaîne qui relie un contrat, sa retiraison, la DSM de l'acheteur et celle du vendeur, la vente export, le certificat, la cotisation. Chaque module est traité comme une île.

Il passe ainsi à côté de trois choses qui font la valeur de la plateforme. Ce sont des questions qu'un prestataire de conseil aurait posées au BNIA en rédigeant le cahier des charges, et qui auraient donné lieu à un vrai travail avec lui. Elles n'ont pas été posées.

## 1. La chaîne : chaque fait déclaré une seule fois

**Ce qui se passe aujourd'hui, et que le cahier des charges reconduit.** Un achat d'Armagnac est saisi dans un contrat, puis redéclaré par l'acheteur dans sa DSM, puis par le vendeur dans la sienne, et justifié en rattachant le contrat. Une vente export est déclarée dans le suivi export, redéclarée dans la DSM, et le même DAE est déposé deux fois (p.26 et p.38).

Le seul préremplissage prévu est le stock de début de période (§4.4.3, p.23). Pourtant **9 des 26 colonnes** de la DSM reprennent des volumes déjà connus : achats et ventes négoce par les contrats (colonnes 2, 3, 20, 21), distillation par l'ODG (4), export par le suivi export (22 à 25).

**Le symptôme le plus net : le contrat « exécuté » écrit à l'envers** (§4.4.7, p.27) :

> « Un `Contrat achat Armagnac` est exécuté lorsqu'il est validé par les parties et par le BNIA et que les volumes mentionnés dans le contrat couvrent les volumes déclarés dans la `DSM`. »

C'est la DSM qui rendrait le contrat exécuté. La réalité est l'inverse : on signe, on retire, et **la DSM en est la conséquence**. Quand on ne voit pas la chaîne, on finit par l'écrire à l'envers.

**La valeur métier.**
- L'opérateur **vérifie** sa DSM au lieu de la saisir.
- Le BNIA ne rapproche plus à la main : l'achat de l'acheteur et la vente du vendeur concordent par construction, puisqu'ils viennent de la même retiraison.
- Les chiffres de l'observatoire deviennent cohérents : volumes et prix viennent des mêmes faits. Aujourd'hui, le prix moyen d'achat prévu mêle des volumes pris dans les DSM et des valeurs prises dans les contrats (§4.10, p.48).

## 2. Les cycles de vie : ce qui arrive à un objet dans le temps

**Les contrats.** Le cahier des charges en donne les champs et trois validations, de l'acheteur, du vendeur et du BNIA (§4.5.1, p.30-31) ; les statuts n'ont aucune valeur (annexe C.4, p.113). Rien sur la retiraison en plusieurs fois, le solde, le refus d'une partie, l'avenant, la clôture.

Un exemple chiffré : un contrat de 100 hl AP retiré par lots de 25 hl AP. Chaque mois, le contrat « couvre » les 25 hl AP déclarés ; rien ne tient le solde ; le même contrat pourrait justifier 25 hl AP pendant douze mois, soit 300 hl AP.

**La DSM.** Quatre états : « En attente de saisie ; À valider ; À facturer ; Facturée » (§4.4.2, p.22). Aucun échange entre l'opérateur et le BNIA : ni envoi, ni instruction, ni demande de compléments.

**La valeur métier.** Le travail quotidien des agents du BNIA, c'est l'instruction : vérifier, demander, relancer, valider. C'est précisément ce que le cahier des charges ne décrit pas, et ce que le prototype montre.

## 3. Un langage commun pour les produits de la filière

**La dénomination n'est pas codifiée.** Sur une ligne de DSM, la « dénomination / qualité » est un « Texte court » (annexe C.3, p.108). Même chose dans les contrats (annexe C.4) et à l'export, où elle est une « Dénomination commerciale ou qualité produit » (annexe C.6, p.119). Chaque négociant écrira donc ses propres libellés : « Réserve », « Cru », un nom de cuvée, une marque. Le même champ mélange des **mentions réglementées**, que l'annexe B associe aux comptes d'âge (VS, VSOP, Hors d'Âge, Extra, Réserve, p.99), et des **noms commerciaux**. Impossible d'additionner ces lignes d'un opérateur à l'autre.

**L'appellation ne suit pas le stock.** La fiche opérateur connaît quatre appellations : « Armagnac, Bas Armagnac, Armagnac Ténarèze et Haut Armagnac » (annexe C.1, p.102). Les lignes des contrats, des déclarations de distillation et des ventes export portent toutes une appellation codifiée (annexes C.4, C.5, C.6), et les statistiques de production s'en servent comme axe (§4.10.6.1, p.49). **Seule la DSM ne la porte pas.** La chaîne se rompt au milieu : un achat de Bas Armagnac sous contrat devient, dans la DSM, une ligne sans appellation.

**La conséquence pour l'interprofession.** L'état des stocks, qui est le cœur d'un observatoire, ne se fait que par millésime et compte d'âge (§4.10.5, p.48). Le BNIA ne pourra pas savoir combien de Bas Armagnac ou de Ténarèze dorment dans les chais, ni dans quelle qualité. Or c'est l'information qui pèse dans les décisions de la filière.

**La valeur métier.** Un référentiel des produits commun à tous les modules : appellation, mention de vieillissement déduite du compte d'âge, et dénomination choisie dans une liste tenue par le BNIA plutôt que saisie librement. Les stocks de toute l'interprofession s'additionnent enfin, et les contrats, les DSM et l'export parlent la même langue.

À vérifier :
- La Blanche Armagnac, AOC distincte, n'apparaît nulle part dans le cahier des charges. Est-elle suivie par le BNIA ?
- L'annexe B place « Réserve » à partir du compte 10. Dans le cahier des charges de l'appellation, « Réserve » correspond, sauf erreur, au niveau VSOP : à confirmer avant d'en parler.
- Le prototype suit aujourd'hui le cahier des charges : dénomination en texte libre et pas d'appellation sur les lignes de DSM (H-DEC-8). Un membre du comité qui l'a parcouru peut le remarquer. Mieux vaut le présenter comme le premier sujet de la phase 0 que de le laisser découvrir.

## Le fil rouge : la maison

Une seule famille d'images, reprise à plusieurs moments de la présentation. C'est la répétition qui fait que le comité s'en souvient.

| Moment | Formule |
|---|---|
| Le constat | « Le cahier des charges, c'est l'inventaire de la maison, pièce par pièce, meuble par meuble. On sait ce qu'il y a dans chaque pièce, mais pas par où l'on passe de l'une à l'autre. Or c'est le plan qui rend une maison habitable. » |
| La conséquence | « Un inventaire sans plan conduit naturellement à un logiciel-formulaire : des écrans de saisie au-dessus d'un grand tableau. Je vous propose un logiciel qui suit le fonctionnement de la filière et les contraintes des opérateurs. » |
| La chaîne (1) | « Le plan, ce sont les portes entre les pièces : du contrat à la retiraison, de la retiraison à la DSM, de la DSM à la facture. » |
| Le langage commun (3) | « Encore faut-il que chaque pièce porte le même nom sur toutes les portes. » |
| La propriété, en fin de présentation | « Et vous gardez les clés de la maison : le code, l'hébergement et les données sont au nom du BNIA. » |

## La réponse à l'oral, en une minute

> « Le cahier des charges fait l'inventaire complet de ce que contient Integraal : c'est une base précieuse, notamment pour la reprise des données. Mais c'est l'inventaire de la maison, pièce par pièce, meuble par meuble. On sait ce qu'il y a dans chaque pièce, pas par où l'on passe de l'une à l'autre. Et un inventaire sans plan conduit naturellement à un logiciel-formulaire : des écrans de saisie au-dessus d'un grand tableau.
>
> Le plan, ce sont les portes entre les pièces. Prenez un achat d'Armagnac. Il est saisi dans un contrat, puis redéclaré par l'acheteur dans sa DSM, puis par le vendeur dans la sienne. Le cahier des charges va jusqu'à écrire que c'est la DSM qui rend le contrat exécuté. C'est l'inverse : on signe, on retire, et la DSM en est la conséquence.
>
> Dans la plateforme que je propose, chaque fait est déclaré une seule fois. La DSM arrive préremplie, l'opérateur vérifie et complète. Le BNIA n'a plus à rapprocher à la main, et les chiffres de l'observatoire sont cohérents par construction.
>
> Encore faut-il que chaque pièce porte le même nom sur toutes les portes. Dans le cahier des charges, la dénomination d'une ligne de DSM est un texte libre, et la DSM ne porte pas l'appellation, alors que les contrats et l'export la portent. Le BNIA ne pourrait donc pas dire combien de Bas Armagnac ou de Ténarèze il y a dans les chais. Un référentiel des produits commun est le premier chantier que je vous proposerai. Ce travail est compris dans le forfait. »

Puis enchaîner sur la démonstration : l'instruction d'une DSM, et le préremplissage s'il est prêt mardi.

## En réserve, si l'on vous demande un autre exemple

Même comme dictionnaire, le cahier des charges est incomplet :
- **Un mot sans définition** : le statut de déclaration « oui, non, sommeil, clôturé » (annexe C.1, p.102) décide qui doit déclarer et qui est en retard, mais aucune valeur n'est définie.
- **Une notion sans description** : la facture provisionnelle n'apparaît que dans un tableau de droits (p.16) et un nom de fichier d'exemple (p.140).
- **Des sources qui divergent, avec de l'argent en jeu** : pour un négociant, le §4.9.3 (p.44) compte la colonne 25 mais pas la 16, la matrice Excel compte la 16 mais pas la 25, et la facture d'exemple compte les deux. Au taux de cette facture, 60 € HT par hl AP, 10 hl AP en colonne 16 font 600 € d'écart sur une seule DSM.

## Hors entretien : questions pour la phase 0

À poser au BNIA une fois retenu, pas à citer mardi :
- Les comptes d'âge au-delà de 20+ à l'export, sans correspondance avec la DSM (p.39).
- Le destinataire export, absent de la saisie et de l'import (p.38-40).
- Le certificat export demandé avant l'expédition mais accompagné de la DRM (p.42-43, p.72) ; sans refus ni annulation (p.121).
- La distillation contrôlée par le BNIA mais non modifiable, et corrigée par l'ODG après facturation (p.36, p.81).
- Les opérateurs non rattachés à l'ODG, dont rien n'arrive (p.81) ; la passerelle vers TimeTonic (p.80, p.139).
- Les propositions et pluri-annuels vin, qui ne deviennent pas des contrats (p.30, p.95).
- Les catégories « petits producteurs, gros producteurs, coopératives » (p.50), le secret statistique (p.47, p.75), les consentements statistiques (p.19-20, p.75).
- Les destinataires des relances (p.77) ; la facture électronique réduite à un PDF (p.46, p.83).
