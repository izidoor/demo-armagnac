import { useEffect, useState } from 'react'
import { ArrowLeft } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'

export type HeaderProps = React.HTMLAttributes<HTMLElement> & {
  fixed?: boolean
  ref?: React.Ref<HTMLElement>
  /** Le titre de la page, en petit, montré quand celui de la page est passé sous l'en-tête. */
  titre?: React.ReactNode
  titreVisible?: boolean
  /** Page de second niveau : sur écran étroit, une flèche de retour vers la page parente remplace le bouton du menu. */
  retour?: { libelle: string; onRetour: () => void }
}

export function Header({
  className,
  fixed,
  titre,
  titreVisible = false,
  retour,
  children,
  ...props
}: HeaderProps) {
  const [offset, setOffset] = useState(0)

  useEffect(() => {
    const onScroll = () => {
      setOffset(document.body.scrollTop || document.documentElement.scrollTop)
    }

    // Add scroll listener to the body
    document.addEventListener('scroll', onScroll, { passive: true })

    // Clean up the event listener on unmount
    return () => document.removeEventListener('scroll', onScroll)
  }, [])

  return (
    <header
      className={cn(
        'z-50 h-16',
        fixed && 'header-fixed peer/header sticky top-0 w-[inherit]',
        offset > 10 && fixed ? 'shadow' : 'shadow-none',
        className
      )}
      {...props}
    >
      <div
        className={cn(
          'relative flex h-full items-center gap-3 p-4 sm:gap-4',
          offset > 10 &&
            fixed &&
            'after:bg-background/20 after:absolute after:inset-0 after:-z-10 after:backdrop-blur-lg'
        )}
      >
        {/* Sur écran large, le bouton du menu est dans la sidebar (AppTitle). */}
        {retour ? (
          <Button
            variant='outline'
            size='icon'
            className='size-7 scale-125 md:hidden'
            onClick={retour.onRetour}
            aria-label={`Retour : ${retour.libelle}`}
          >
            <ArrowLeft />
          </Button>
        ) : (
          <SidebarTrigger variant='outline' className='scale-125 md:hidden' />
        )}
        <Separator orientation='vertical' className='h-6 md:hidden' />
        {titre && (
          <span
            aria-hidden={!titreVisible}
            className={cn(
              'min-w-0 truncate text-base font-semibold transition-opacity duration-200',
              titreVisible ? 'opacity-100' : 'pointer-events-none opacity-0'
            )}
          >
            {titre}
          </span>
        )}
        {children}
      </div>
    </header>
  )
}
