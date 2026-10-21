/** Comptes de démonstration (profil `demo`, docs/demo.md) : un par rôle et par type d'opérateur. */
const COMPTES = [
  { domaine: 'bnia.fr', titre: 'Comptes BNIA (back-office)' },
  { domaine: 'producteur.fr', titre: 'Comptes PRODUCTEUR' },
  { domaine: 'negociant.fr', titre: 'Comptes NÉGOCIANT' },
]
const ROLES = ['admin', 'agent', 'observateur']

/** Panneau de l'écran de connexion : l'objet de l'application et les indications de démonstration. */
export function PanneauDemo() {
  return (
    <div className='space-y-4'>
      <div className='space-y-1'>
        <p className='text-sm font-medium text-neutral-600'>
          Appel d'offres BNIA - 2026
        </p>
        <p className='text-2xl font-bold'>Application de démonstration</p>
      </div>
      <div className='space-y-3'>
        <div>
          <p className='font-medium'>Comptes de démonstration</p>
          <p className='text-xs text-neutral-600'>
            Les comptes portent leur rôle dans le login : l'admin a tous les
            droits ; l'agent gère les modules métiers ; l'observateur a un
            simple accès en lecture.
          </p>
        </div>
        {COMPTES.map((c) => (
          <div key={c.domaine}>
            <p className='text-sm font-semibold'>{c.titre}</p>
            <ul className='font-mono text-sm'>
              {ROLES.map((r) => (
                <li key={r}>
                  {r}@{c.domaine}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <dl className='grid grid-cols-[auto_1fr] gap-x-3 gap-y-1'>
        <dt className='font-semibold text-neutral-900'>
          Tous les mots de passe :
        </dt>
        <dd className='font-mono font-semibold'>demo-armagnac</dd>
        <dt className='text-neutral-600'>Tous les codes de vérification :</dt>
        <dd>
          <span className='font-mono'>000000</span>{' '}
          <span className='text-xs text-neutral-600'>
            (double authentification désactivée au départ)
          </span>
        </dd>
      </dl>
    </div>
  )
}
