<div class="surtitre">Prototype</div>

# Un prototype à essayer en ligne

<div class="encadre">

**Adresse : [bnia-demo.osc-fr1.scalingo.io](https://bnia-demo.osc-fr1.scalingo.io)**  
Mot de passe, commun à tous les comptes : **demo-armagnac**. Si un code de vérification est demandé : **000000**.  
Les données sont fictives : tout peut être essayé sans crainte.

</div>

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

Une **messagerie** relie aussi le BNIA et les opérateurs : chaque échange peut être rattaché à une DSM ou à une facture.

## Les comptes de démonstration

| Compte | Espace | Ce qu'on y voit |
|---|---|---|
| admin@bnia.fr | Back-office, administrateur | Tout : opérateurs, DSM à instruire, facturation, accès |
| agent@bnia.fr | Back-office, agent | La gestion des opérateurs, sans la gestion des accès |
| observateur@bnia.fr | Back-office, observateur | Tout le back-office, en lecture seule |
| admin@negociant.fr | Portail négociant, administrateur | DSM mensuelles, dont une pour laquelle le BNIA a demandé des compléments ; factures, messagerie, utilisateurs |
| agent@negociant.fr | Portail négociant, agent | DSM, factures et messagerie |
| observateur@negociant.fr | Portail négociant, observateur | En lecture seule |
| admin@producteur.fr | Portail producteur, administrateur | DSM annuelle en cours de saisie, avec une incohérence à corriger ; deux chais, passage de l’un à l’autre |
