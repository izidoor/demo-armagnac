import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  rechercherOperateurs,
  type ResultatRechercheOperateur,
} from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
import { Input } from '@/components/ui/input'

/**
 * Recherche d'un autre opérateur au portail (RG-OPE-37), pour désigner l'autre partie d'un contrat :
 * opérateurs actifs, trois caractères au moins, vingt résultats au plus ; code, raison sociale, nom
 * commercial et ville, rien d'autre.
 */
export function RechercheOperateur({
  onChoisir,
}: {
  onChoisir?: (operateur: ResultatRechercheOperateur) => void
}) {
  const [texte, setTexte] = useState('')
  const stable = useDebounce(texte.trim())
  const { data, isFetching } = useQuery({
    queryKey: ['recherche-operateurs', stable],
    queryFn: () =>
      rechercherOperateurs({
        query: { texte: stable },
        throwOnError: true,
      }).then((r) => r.data),
    enabled: stable.length >= 3,
  })
  return (
    <div className='space-y-2'>
      <Input
        value={texte}
        onChange={(e) => setTexte(e.target.value)}
        placeholder='Code, raison sociale ou nom commercial (3 caractères au moins)'
      />
      {stable.length >= 3 && !isFetching && data?.length === 0 && (
        <p className='text-muted-foreground text-sm'>
          Aucun opérateur actif ne correspond.
        </p>
      )}
      <ul className='divide-y text-sm'>
        {data?.map((o) => (
          <li key={o.id}>
            <button
              type='button'
              className='hover:bg-muted w-full px-2 py-1.5 text-start'
              onClick={() => onChoisir?.(o)}
            >
              <span className='font-mono'>{o.code}</span> · {o.raisonSociale}
              {o.nomCommercial && (
                <span className='text-muted-foreground'>
                  {' '}
                  ({o.nomCommercial})
                </span>
              )}
              {o.ville && (
                <span className='text-muted-foreground'> · {o.ville}</span>
              )}
            </button>
          </li>
        ))}
      </ul>
      {data?.length === 20 && (
        <p className='text-muted-foreground text-sm'>
          Vingt premiers résultats : précisez la recherche.
        </p>
      )}
    </div>
  )
}
