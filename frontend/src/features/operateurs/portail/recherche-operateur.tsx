import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Command as CommandPrimitive } from 'cmdk'
import {
  rechercherOperateurs,
  type ResultatRechercheOperateur,
} from '@/api/generated'
import { cn } from '@/lib/utils'
import { useDebounce } from '@/hooks/use-debounce'
import { CLASSES_CHAMP } from '@/components/ui/input'

/**
 * Recherche d'un opérateur actif (RG-OPE-37) : trois caractères au moins, vingt résultats au plus ; code, raison
 * sociale, nom commercial et ville, rien d'autre. Au portail, pour désigner l'autre partie d'un contrat ; au tableau de
 * bord du BNIA, pour ouvrir une fiche. `exclure` retire un opérateur des résultats : l'opérateur actif, qui ne peut être
 * l'autre partie de son propre contrat.
 *
 * Au clavier (`cmdk`, le `Command` de shadcn, sans filtre local : le serveur cherche) : les flèches parcourent les
 * résultats, Entrée choisit celui en surbrillance. Sans `onEntree`, le premier est en surbrillance d'emblée ; avec, rien
 * ne l'est avant une flèche ou la souris, et Entrée donne alors le texte saisi à `onEntree`.
 */
export function RechercheOperateur({
  onChoisir,
  onEntree,
  exclure,
  limite = 20,
  autoFocus,
  placeholder = 'Code, raison sociale ou nom commercial (3 caractères au moins)',
}: {
  onChoisir?: (operateur: ResultatRechercheOperateur) => void
  /** Entrée sans résultat en surbrillance : le texte saisi (le listing des opérateurs filtré, au tableau de bord). */
  onEntree?: (texte: string) => void
  exclure?: number
  limite?: number
  autoFocus?: boolean
  placeholder?: string
}) {
  const [texte, setTexte] = useState('')
  // Avec `onEntree`, la surbrillance (que `cmdk` met d'emblée sur le premier résultat) ne se montre qu'une fois la
  // liste parcourue, aux flèches ou à la souris ; avant, Entrée donne le texte.
  const [parcourue, setParcourue] = useState(!onEntree)
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
  const resultats =
    stable.length >= 3
      ? (data ?? []).filter((o) => o.id !== exclure).slice(0, limite)
      : []
  return (
    <CommandPrimitive
      shouldFilter={false}
      loop
      label='Rechercher un opérateur'
      className='space-y-2'
      onKeyDown={(e) => {
        if (parcourue) return
        if (e.key === 'Enter' && texte.trim()) {
          e.preventDefault()
          onEntree?.(texte.trim())
        } else if (e.key === 'ArrowDown' && resultats.length > 0) {
          // La première flèche montre le premier résultat, déjà choisi par `cmdk`, sans passer au suivant.
          e.preventDefault()
          setParcourue(true)
        } else if (e.key === 'ArrowUp') setParcourue(true)
      }}
    >
      <CommandPrimitive.Input
        value={texte}
        onValueChange={(t) => {
          setTexte(t)
          setParcourue(!onEntree)
        }}
        autoFocus={autoFocus}
        placeholder={placeholder}
        className={CLASSES_CHAMP}
      />
      {stable.length >= 3 && !isFetching && resultats.length === 0 && (
        <p className='text-muted-foreground px-2 text-sm'>
          Aucun opérateur actif ne correspond.
        </p>
      )}
      <CommandPrimitive.List
        className={cn(
          'text-sm',
          parcourue && '[&_[data-selected=true]]:bg-muted'
        )}
        onPointerMoveCapture={() => setParcourue(true)}
      >
        {resultats.map((o) => (
          <CommandPrimitive.Item
            key={o.id}
            value={String(o.id)}
            onSelect={() => onChoisir?.(o)}
            className='cursor-pointer rounded-md px-2 py-1.5 max-md:py-3'
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
          </CommandPrimitive.Item>
        ))}
      </CommandPrimitive.List>
      {limite === 20 && data?.length === 20 && (
        <p className='text-muted-foreground px-2 text-sm'>
          Vingt premiers résultats : précisez la recherche.
        </p>
      )}
    </CommandPrimitive>
  )
}
