import { Item, Root } from '@radix-ui/react-radio-group'
import { CircleCheck } from 'lucide-react'
import { IconThemeDark } from '@/assets/custom/icon-theme-dark'
import { IconThemeLight } from '@/assets/custom/icon-theme-light'
import { IconThemeSystem } from '@/assets/custom/icon-theme-system'
import { cn } from '@/lib/utils'
import { useTheme } from '@/context/theme-provider'

const modes = [
  { valeur: 'light', libelle: 'Clair', icone: IconThemeLight },
  { valeur: 'dark', libelle: 'Sombre', icone: IconThemeDark },
  { valeur: 'system', libelle: 'Système', icone: IconThemeSystem },
] as const

/** Mode clair, sombre ou celui du système ; appliqué aussitôt, retenu par le navigateur. */
export function Affichage() {
  const { theme, setTheme } = useTheme()
  return (
    <Root
      value={theme}
      onValueChange={(v) => setTheme(v as typeof theme)}
      aria-label="Mode d'affichage"
      className='grid max-w-md grid-cols-3 gap-4 max-md:pe-3'
    >
      {modes.map(({ valeur, libelle, icone: Icone }) => (
        <Item
          key={valeur}
          value={valeur}
          className='group outline-none'
          aria-label={libelle}
        >
          <div
            className={cn(
              'ring-border relative rounded-[6px] ring-[1px]',
              'group-data-[state=checked]:ring-primary group-data-[state=checked]:shadow-2xl',
              'group-focus-visible:ring-2'
            )}
          >
            <CircleCheck
              aria-hidden='true'
              className={cn(
                'fill-primary size-6 stroke-white',
                'group-data-[state=unchecked]:hidden',
                'absolute top-0 right-0 translate-x-1/2 -translate-y-1/2'
              )}
            />
            <Icone className='fill-primary stroke-primary' />
          </div>
          <div className='mt-1 text-xs'>{libelle}</div>
        </Item>
      ))}
    </Root>
  )
}
