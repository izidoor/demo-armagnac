<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 105-107 -->

<!-- p.105 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Contact - fonction|Texte court|Fonction ou rôle dans la structure.|
|Contact - centres d’intérêt|Choix multiple|Un ou plusieurs centres d’intérêt issus du référentiel,<br>utilisés pour cibler les communications adressées au<br>contact.|
|Contact - téléphone<br>professionnel|Texte court|Coordonnée téléphonique principale.|
|Contact - téléphone personnel /<br>secondaire|Texte court|Coordonnée complémentaire si conservée.|
|Contact - mél|Texte court|Adresse électronique du contact; obligatoire pour l’accès<br>portail.|
|Contact - adresse|Texte court|Adresse postale du contact si distincte de celle de<br>l’opérateur.|
|Contact - code postal|Texte court|Code postal du contact.|
|Contact - ville|Texte court|Ville du contact.|
|Contact - pays|Référentiel|Pays du contact.|
|Contact - observation|Texte long|Commentaire interne ou précision de gestion.|

### C.1.9.2 Accès portail et habilitations locales
|**Champ**|**Type**|**Description**|
|---|---|---|
|Contact - accès portail activé|Booléen|Active ou non un accès pour ce contact.|
|Contact - administrateur du|Booléen|Indique si le contact peut administrer les autres accès|
|compte||portail.|
|Contact - droits par module|Choix multiple|Liste des modules du portail autorisés pour ce contact.|

# C.2 Gestion des accès et habilitations
## C.2.1 Compte utilisateur interne BNIA
### C.2.1.1 Identité, statut et sécurité d’accès
|**Champ**|**Type**|**Description**|
|---|---|---|
|Comptes utilisateurs BNIA|Table fille|Ensemble des accès internes de la plateforme pour les<br>utilisateurs BNIA.|
|Compte - prénom|Texte court|Prénom de l’utilisateur interne.|
|Compte - nom|Texte court|Nom de l’utilisateur interne.|
|Compte - adresse électronique<br>professionnelle|Identifiant|Adresse de référence pour la connexion, les notifications<br>d’accès et la recherche d’un compte.|
|Compte - statut|Référentiel|État du compte, avec au minimum`actif`,`désactivé`,<br>`bloqué temporairement`,`en attente d'activation`.|

<!-- p.106 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Compte - rôle attribué|Référentiel|Rôle unique affecté à l’utilisateur interne BNIA.|
|Compte - double<br>authentification requise|Booléen|Indique si la double authentification est imposée pour ce<br>compte.|
|Compte - double<br>authentification activée|Booléen|Confirme que le second facteur est effectivement activé.|
|Compte - initialisation du mot de<br>passe en attente|Booléen|Suit les comptes créés dont le parcours initial n’est pas<br>terminé.|
|Compte - dernière connexion<br>réussie|Date|Dernière connexion valide conservée pour l’administration<br>et l’audit.|
|Compte - nombre d’échecs<br>récents|Nombre|Compteur utile au suivi des tentatives échouées.|
|Compte - fin de blocage<br>temporaire|Date|Date ou échéance de déblocage automatique lorsque le<br>compte est bloqué.|
|Compte - commentaire<br>d’administration|Texte long|Commentaire interne sur le compte, sa gestion ou son<br>suivi.|

## C.2.2 Rôle utilisateur BNIA
### C.2.2.1 Définition du rôle
|**Champ**|**Type**|**Description**|
|---|---|---|
|Rôles utilisateurs|Table fille|Ensemble des rôles composables utilisés par le BNIA.|
|Rôle - nom|Texte court|Libellé du rôle, par exemple métier, consultation, facturation<br>ou administration.|
|Rôle - description|Texte long|Description synthétique de la finalité du rôle.|
|Rôle - statut|Référentiel|Rôle actif ou désactivé, sans suppression de l’historique.|
|Rôle - administrateur global|Booléen|Identifie le rôle générique donnant accès à l’ensemble des<br>fonctions internes lorsque le BNIA le décide.|
|Rôle - utilisateurs rattachés|Donnée<br>calculée|Nombre ou liste des utilisateurs affectés au rôle.|

### C.2.2.2 Droits associés au rôle
|**Champ**|**Type**|**Description**|
|---|---|---|
|Droits du rôle|Table fille|Liste des droits composant le rôle.|
|Droit - module|Référentiel|Module ou périmètre fonctionnel concerné.|
|Droit - action|Référentiel|Niveau d’action autorisé, par exemple`Lecture`,`Écriture`,<br>`Facturer`,`Générer`,`Gérer`.|
|Droit - activé|Booléen|Indique si le droit est effectivement inclus dans le rôle.|

<!-- p.107 -->
## C.2.3 Journal des accès et actions sensibles
### C.2.3.1 Historique d’audit
|**Champ**|**Type**|**Description**|
|---|---|---|
|Journal des accès|Table fille|Historique des connexions et des changements sensibles<br>liés aux accès.|
|Événement - date|Date|Date de la connexion ou de l’action tracée.|
|Événement - type|Référentiel|Nature de l’événement, par exemple connexion réussie,<br>échec, création de compte, changement de rôle,<br>désactivation.|
|Événement - utilisateur<br>concerné|Référentiel|Compte interne visé par l’événement.|
|Événement - auteur de l’action|Référentiel|Utilisateur ou administrateur à l’origine de l’action lorsque<br>cette information existe.|
|Événement - rôle concerné|Référentiel|Rôle ajouté, retiré ou modifié lorsqu’un changement<br>d’habilitation est tracé.|
|Événement - résultat|Référentiel|Résultat de l’action, par exemple`succès`,`échec`,`blocage`<br>`déclenché`.|
|Événement - détail|Texte long|Détail utile au support, au contrôle interne ou à l’audit.|

# C.3 Déclarations de stocks et mouvements
## C.3.1 Déclaration DSM
### C.3.1.1 Pilotage de la déclaration
|**Champ**|**Type**|**Description**|
|---|---|---|
|Déclarations DSM|Table fille|Ensemble des déclarations de stocks et mouvements<br>suivies par le BNIA.|
|Déclaration - opérateur|Référentiel|Référence de l’opérateur déclarant.|
|Déclaration - période|Texte court|Période de déclaration, mensuelle, annuelle ou regroupée<br>selon la périodicité autorisée.|
|Déclaration - type de déclarant|Référentiel|Profil déclaratif`producteur`ou`négociant`.|
|Déclaration - mode<br>d’alimentation|Référentiel|Saisie manuelle, import BNIA ou import depuis logiciel<br>opérateur.|
|Déclaration - état|Référentiel|État de traitement, avec au minimum`En attente de`<br>`saisie`,`À facturer`,`Facturée`,`Validée`.|
|Déclaration - observation<br>générale|Texte long|Commentaire porté sur l’ensemble de la déclaration.|
|Déclaration - total cotisation HT|Donnée<br>calculée|Total calculé à partir des flux soumis à cotisation.|
|Déclaration - date de saisie|Date|Date de création ou de saisie de la déclaration.|