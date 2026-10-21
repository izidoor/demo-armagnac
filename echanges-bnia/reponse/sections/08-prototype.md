<div class="surtitre">Prototype</div>

# Un prototype à essayer en ligne

- **Adresse : [bnia-demo.osc-fr1.scalingo.io](https://bnia-demo.osc-fr1.scalingo.io)**  
- Mot de passe, commun à tous les comptes : **demo-armagnac**. 
- Si un code de vérification est demandé : **000000**.  
- Les données sont fictives : tout peut être essayé sans crainte.


## Ce qu'on peut y faire

<div class="definitions">
<div>

### Opérateurs

- Le BNIA consulte et met à jour la fiche de chaque opérateur : coordonnées, contacts, activité.
- L’opérateur gère lui-même les utilisateurs de son entreprise : il les invite et choisit leurs droits.
- Une même personne peut travailler pour plusieurs opérateurs, et passer de l’un à l’autre.

</div>
<div>

### DSM

- L’opérateur saisit sa DSM à l’écran, ou l’importe depuis le fichier Excel du BNIA.
- Les incohérences sont signalées pendant la saisie, avant l’envoi.
- L’opérateur joint ses justificatifs (DRM, DAE, factures).
- Le BNIA instruit la DSM, demande des compléments ou la valide.
- La DSM se télécharge en PDF ou en Excel.
- Le BNIA suit les DSM en retard ou absentes, et les stocks déclarés.

</div>
<div>

### Facturation

- La cotisation de chaque DSM est calculée selon la grille CVO.
- Le BNIA facture une DSM validée, ou plusieurs à la fois.
- Il relit et corrige le projet de facture, puis l’émet.
- La facture se visualise en PDF, côté BNIA comme côté opérateur.
- Le BNIA analyse ses ventes par mois ou par client, et les exporte en Excel.

</div>
</div>

Une **messagerie interne** relie aussi le BNIA et les opérateurs : chaque échange peut être rattaché à une DSM ou à une facture. Les opérateurs ont également la possibilité d'ouvrir des conversations sur des sujets libres avec le BNIA.

<div class="encadre">
<h3>Aucun e-mail n’est réellement envoyé par le prototype</h3>
<p>La plateforme envoie des e-mails à plusieurs occasions : l’invitation d’un nouvel utilisateur, le lien « mot de passe oublié », la confirmation d’un changement d’adresse e-mail ou le code de vérification. Sur le prototype, ces e-mails sont préparés mais ne partent pas : les actions qui les déclenchent peuvent être essayées, sans que le message arrive.</p>
<p>C’est volontaire. Les comptes et les opérateurs sont fictifs : envoyer de vrais e-mails depuis une démonstration ouverte à tous risquerait d’écrire à des adresses réelles, ou de faire passer la plateforme pour un expéditeur indésirable. En production, les e-mails partiront par la messagerie du BNIA (armagnac.fr). Pour essayer le prototype, il suffit d’utiliser les comptes de démonstration présentés ci-après.</p>
</div>

<div class="saut"></div>

## Les comptes de démonstration

Neuf comptes sont déjà créés : ils permettent d’essayer le prototype tout de suite, sans rien configurer, et de se mettre à la place de chaque utilisateur. Ils couvrent les trois places de la filière : le **BNIA**, qui instruit et facture, dans le back-office ; un **producteur** et un **négociant**, qui déclarent, sur le portail. Chacun existe dans les trois rôles :

- **Administrateur** : fait tout, et gère les accès de son entreprise : il invite les utilisateurs et choisit leur rôle.
- **Agent** : fait le travail courant (saisir et envoyer les DSM, tenir la fiche de l’entreprise ; au BNIA, instruire et facturer), sans gérer les accès.
- **Observateur** : consulte tout, sans rien modifier ; il peut écrire au BNIA par la messagerie.

<table class="comptes">
<thead><tr><th>Compte</th><th>Rôle</th><th>Ce qu’on y voit</th></tr></thead>
<tbody>
<tr class="groupe"><th colspan="3">@bnia.fr · le BNIA, back-office</th></tr>
<tr><td>admin@bnia.fr</td><td>Administrateur</td><td>Tout : opérateurs, DSM à instruire, facturation, paramétrage, accès</td></tr>
<tr><td>agent@bnia.fr</td><td>Agent</td><td>Fiches des opérateurs, instruction des DSM, facturation, sans la gestion des accès</td></tr>
<tr><td>observateur@bnia.fr</td><td>Observateur</td><td>Tout le back-office, en lecture seule</td></tr>
<tr class="groupe"><th colspan="3">@producteur.fr · un producteur (Maison Durand, deux chais), portail</th></tr>
<tr><td>admin@producteur.fr</td><td>Administrateur</td><td>DSM annuelle en cours de saisie, avec une incohérence à corriger ; passage d’un chai à l’autre ; utilisateurs de l’entreprise</td></tr>
<tr><td>agent@producteur.fr</td><td>Agent</td><td>DSM, factures et messagerie du chai de Condom</td></tr>
<tr><td>observateur@producteur.fr</td><td>Observateur</td><td>Le portail en lecture seule ; son accès au chai d’Eauze est suspendu</td></tr>
<tr class="groupe"><th colspan="3">@negociant.fr · un négociant (Armagnacs de Gascogne), portail</th></tr>
<tr><td>admin@negociant.fr</td><td>Administrateur</td><td>DSM mensuelles, dont une pour laquelle le BNIA a demandé des compléments ; factures ; utilisateurs de l’entreprise</td></tr>
<tr><td>agent@negociant.fr</td><td>Agent</td><td>DSM, factures et messagerie</td></tr>
<tr><td>observateur@negociant.fr</td><td>Observateur</td><td>Le portail en lecture seule</td></tr>
</tbody>
</table>
