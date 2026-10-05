<!-- Source : cahier-des-charges-bnia-v1_1-2026-07-30.pdf · Annexes · pp. 135-136 -->

<!-- p.135 -->
|**Champ**|**Type**|**Description**|
|---|---|---|
|Montant total`HT`|Donnée<br>calculée|Montant hors taxes calculé sur la ligne.|

# C.13 Comptes et accès opérateur
## C.13.1 Compte utilisateur opérateur
### C.13.1.1 Identité et sécurité du compte
|**Champ**|**Type**|**Description**|
|---|---|---|
|Adresse électronique de<br>connexion|Identifiant|Identifiant unique de connexion de l’utilisateur opérateur.|
|Mot de passe|Texte court|Secret d’authentification du compte, géré selon les règles<br>de sécurité retenues.|
|Double authentification activée|Booléen|Indique si le compte utilise un second facteur<br>d’authentification.|
|Mode de double authentification|Référentiel|Mode retenu, par exemple`courriel`ou`application`<br>`TOTP`.|
|État du compte|Référentiel|Statut du compte : actif, désactivé, bloqué ou en attente<br>d’activation.|
|Date de dernière connexion|Date|Dernière connexion constatée sur le compte.|
|Nombre d’échecs récents|Donnée<br>calculée|Compteur utilisé pour le blocage temporaire après<br>tentatives échouées.|

## C.13.2 Accès opérateur rattaché
### C.13.2.1 Comptes opérateurs accessibles
|**Champ**|**Type**|**Description**|
|---|---|---|
|Compte opérateur accessible|Table fille|Liste des opérateurs auxquels le compte utilisateur peut<br>accéder.|
|Code opérateur|Référentiel|Référence du compte opérateur rattaché.|
|Rôle sur le compte opérateur|Référentiel|Niveau d’administration ou d’usage du contact sur ce<br>compte.|
|Modules autorisés|Choix multiple|Liste des modules du portail ouverts pour ce rattachement,<br>y compris la boutique lorsque le contact y est habilité.|
|Compte opérateur actif|Booléen|Indique le compte actuellement sélectionné dans la<br>session.|

<!-- p.136 -->
# C.14 Notifications et relances
## C.14.1 Notification opérateur
### C.14.1.1 Message adressé à un contact opérateur
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

### C.14.1.2 Destinataires de notification
|**Champ**|**Type**|**Description**|
|---|---|---|
|Règles de destinataires de<br>notification|Table fille|Paramétrage des destinataires ou catégories de contacts<br>recevant les notifications métier.|
|Règle - type de notification|Référentiel|Type de notification concerné par la règle de routage.|
|Règle - module concerné|Référentiel|Module auquel se rattache la notification.|
|Règle - contact opérateur|Référentiel|Contact explicitement destinataire lorsqu’une affectation<br>nominative existe.|
|Règle - niveau d’administration<br>requis|Référentiel|Niveau de droit ou d’administration du contact pouvant<br>conditionner la réception.|
|Règle - envoi activé|Booléen|Indique si la règle de diffusion est active.|