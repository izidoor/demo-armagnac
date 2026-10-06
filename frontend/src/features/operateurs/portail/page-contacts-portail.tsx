import { useQuery, useQueryClient } from '@tanstack/react-query'
import { PageMetier } from '@/components/metier/page-metier'
import { apiOperateur, cleOperateur } from '../commun/api'
import { PageContact } from '../contacts/page-contact'
import { OngletContacts } from '../fiche/onglet-contacts'
import { SansOperateur } from './sans-operateur'
import { useOperateurPortail } from './use-operateur-portail'

/**
 * « Contacts » au portail (spec 02, §6.2), réservé à l'administrateur (RG-OPE-35) : tant que acces
 * n'existe pas, l'utilisateur du portail administre l'opérateur de démonstration.
 */
export function PageContactsPortail() {
  const queryClient = useQueryClient()
  const { operateur, chargement } = useOperateurPortail()
  const fiche = useQuery({
    queryKey: [...cleOperateur(operateur?.id ?? 0), 'portail'],
    queryFn: () => apiOperateur('portail', operateur!.id).fiche(),
    enabled: !!operateur,
  })
  if (!operateur)
    return <SansOperateur titre='Contacts' chargement={chargement} />
  return (
    <PageMetier
      titre='Contacts'
      description={`Les personnes de ${operateur.raisonSociale}, et leurs droits au portail.`}
    >
      {fiche.data && (
        <OngletContacts
          espace='portail'
          fiche={fiche.data}
          onEnregistre={() =>
            queryClient.invalidateQueries({
              queryKey: cleOperateur(operateur.id),
            })
          }
        />
      )}
    </PageMetier>
  )
}

export function PageContactPortail({ contact }: { contact: number }) {
  const { operateur, chargement } = useOperateurPortail()
  if (!operateur)
    return <SansOperateur titre='Contact' chargement={chargement} />
  return (
    <PageContact espace='portail' operateur={operateur.id} contact={contact} />
  )
}
