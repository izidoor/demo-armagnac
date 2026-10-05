<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 136-137 -->

<!-- p.136 -->
## C.14 Notifications et relances
### C.14.1 Notification opérateur
#### C.14.1.1 Message adressé à un contact opérateur
|**Champ**|**Type**|**Description**|
|---|---|---|
|Type de notification|Référentiel|Nature de l’événement signalé à l’opérateur.|
|Module concerné|Référentiel|Module ou périmètre fonctionnel auquel la notification se<br>rattache.|
|Date d’émission|Date|Date d’envoi ou de mise à disposition.|
|Contact destinataire|Référentiel|Contact opérateur destinataire du message.|
|Type d’objet concerné|Référentiel|Type d’objet lié à la notification, par exemple contrat,`DSM`,<br>facture ou certificat.|
|Objet concerné|Référentiel|Référence du dossier lié à la notification lorsqu’il existe<br>dans la plateforme.|
|Statut d’envoi|Référentiel|État d’émission ou de mise à disposition de la notification.|
|Statut de lecture|Référentiel|Lu ou non lu.|
|Lien d’action|Texte court|Renvoi vers le bon écran du portail.|

#### C.14.1.2 Destinataires de notification
|**Champ**|**Type**|**Description**|
|---|---|---|
|Règles de destinataires de<br>notification|Table fille|Paramétrage des destinataires ou catégories de contacts<br>recevant les notifications métier.|
|Règle - type de notification|Référentiel|Type de notification concerné par la règle de routage.|
|Règle - module concerné|Référentiel|Module auquel se rattache la notification.|
|Règle - contact opérateur|Référentiel|Contact explicitement destinataire lorsqu’une affectation<br>nominative existe.|
|Règle - niveau d’administration<br>requis|Référentiel|Niveau de droit ou d’administration du contact pouvant<br>conditionner la réception.|
|Règle - envoi activé|Booléen|Indique si la règle de diffusion est active.|

<!-- p.137 -->
# D Cartographie des interfaces et flux
Cette annexe présente la cartographie cible des échanges entre la plateforme BNIA, ses environnements utilisateurs et les outils tiers conservés dans le périmètre du cahier des charges. 

## D.1 Vue d’ensemble
*[Schéma supprimé (OCR illisible) : voir le PDF à cette page]*

_Figure D.1_ 

La plateforme BNIA s’articule autour de trois ensembles : 

- le `Back-office BNIA` , utilise par les utilisateurs BNIA pour la gestion, les controles et le suivi des traitements 

- les `Portails operateurs` , utilises par les producteurs et negociants pour la consultation, la saisie et le depot des elements attendus 

- l’ `API et flux d'interconnexion` , utilisee pour gerer les echanges avec les outils tiers de la plateforme 

Le flux `ODG` couvre notamment : 

- les contrats vin 

- les declarations de fin de travaux de distillation 

## D.2 Flux concernés