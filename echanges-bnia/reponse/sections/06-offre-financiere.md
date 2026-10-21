<div class="surtitre">Offre financière</div>

# Un prix fixe, des paiements par phase

<p class="accroche">Chaque phase est un forfait, payé seulement une fois qu’elle est validée par le BNIA. L’hébergement est souscrit par le BNIA au prix réel, sans marge intermédiaire. Le premier engagement ne dépasse pas 5 000&nbsp;€.</p>

<div class="chiffres">
<div class="chiffre"><span class="valeur">5 000&nbsp;€</span><span class="libelle">HT pour juger sur pièces, avant toute décision de poursuivre (phase 1)</span></div>
<div class="chiffre"><span class="valeur">53 000&nbsp;€</span><span class="libelle">HT pour la réalisation complète, de la prise en main à l’arrêt d’Integraal</span></div>
<div class="chiffre"><span class="valeur">≈&nbsp;80&nbsp;€</span><span class="libelle">HT par mois d’hébergement, payés directement à l’hébergeur</span></div>
<div class="chiffre"><span class="valeur">0 €</span><span class="libelle">de maintenance facturée jusqu’à fin 2027</span></div>
</div>

## Réalisation

| Phase | Durée | Contenu | Forfait&nbsp;HT |
|---|---|---|---|
| **0 · Prise en main de l'existant** | 1&nbsp;journée | Plan de déploiement confirmé ou ajusté | – |
| **1 · Preuve de capacité sur données réelles** | 2&nbsp;mois | Le socle sur les données du BNIA, en recette : connexion, opérateurs et utilisateurs, DSM, messagerie, notifications | 5&nbsp;000&nbsp;€ |
| **2 · Les DSM sur le portail** | 2&nbsp;mois | Mise en production des DSM ; en recette : suivi export, certificats, facturation | 12&nbsp;000&nbsp;€ |
| **3 · DSM, export et facturation** | 2&nbsp;mois | Mise en production ; en recette : contrats d'achat, fin de distillation | 12&nbsp;000&nbsp;€ |
| **4 · Contrats et distillation** | 3&nbsp;mois | Mise en production ; en recette : statistiques, boutique, gestion documentaire, reprise complète de l'historique | 18&nbsp;000&nbsp;€ |
| **5 · Arrêt d'Integraal** | 1&nbsp;mois | Mise en production finale, dernière synchronisation, arrêt d'Integraal et de pro.armagnac.fr | 6&nbsp;000&nbsp;€ |
| **Total** | | | **53&nbsp;000&nbsp;€** |

Chaque forfait comprend le développement, les tests, la mise en production, la documentation, l’animation du groupe d’adoption, l’accompagnement des opérateurs à chaque ouverture et les relations avec les prestataires externes.

<div class="encadre">
<h3>Pourquoi ce prix ?</h3>
<p>J’estime la charge de travail à neuf mois pour une personne à plein temps. Le prix total représente le salaire d’un chef de projet informatique expérimenté sur cette période.</p>
</div>

### Les connexions aux services externes

| Service | Usage | Dans le prix |
|---|---|---|
| Zeendoc | Archivage des factures | Inclus |
| Otentik | Certificats export | Inclus |
| ODG | Échange des données de la filière | Inclus |
| Signature électronique des DSM | Signature de la DSM par l'opérateur | En option : environ 5&nbsp;000&nbsp;€ HT |

Je ne dispose pas encore de la documentation de Zeendoc, d'Otentik et de l'ODG. Mon prix suppose qu'ils proposent une interface standard (API REST). La prise de contact avec ces services est prévue dès la phase&nbsp;1&nbsp;: si l'un d'eux s'écarte de ce standard, le BNIA en est informé avant de poursuivre, avec un chiffrage.

<p class="encadre"><strong>La signature électronique n’est pas incluse.</strong> La double authentification et la traçabilité de la plateforme prouvent déjà qui a envoyé chaque DSM, avec quel contenu et à quelle date. Si le BNIA souhaite tout de même la signature électronique, la connexion à un service de signature (Yousign, DocuSign…) est estimée à environ 5&nbsp;000&nbsp;€ HT. S’y ajoutent l’abonnement et le coût par signature du service retenu, payés directement par le BNIA.</p>

D'une façon générale, j'estime à environ 5&nbsp;000&nbsp;€ HT la connexion à tout nouveau service externe qui propose une interface standard.

## Hébergement

Le contrat est souscrit au nom du BNIA avant la première mise en production, et réglé directement par lui : aucun frais d'hébergement ne passe par moi, et le BNIA paie le prix public de l'hébergeur, sans marge.

| Service | Hébergeur | Prix HT par mois |
|---|---|---|
| Application | Scalingo, Paris | 28,80 € |
| Écrans (fichiers statiques) | Scalingo, Paris | 7,20 € |
| Base de données PostgreSQL, haute disponibilité | Scalingo, Paris | 40,00 € |
| Stockage des documents, répliqué | Scalingo, Paris | moins de 1 € |
| Envoi des e-mails | Messagerie du BNIA (armagnac.fr) | sans surcoût |
| **Production** | | **environ&nbsp;77 €, soit environ&nbsp;920 € par an** |

## Conditions

- **Prix fermes**, exprimés hors taxes.
- **Paiement à 30 jours**, après validation de chaque phase par le BNIA ; le forfait annuel de maintenance est facturé en début d'année.
- **Liberté du BNIA** : il décide de poursuivre ou d'arrêter à l'issue de la phase 1, sans pénalité.
