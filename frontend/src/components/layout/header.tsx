import { useEffect, useState } from 'react'
import { cn } from '@/lib/utils'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'

type HeaderProps = React.HTMLAttributes<HTMLElement> & {
  fixed?: boolean
  ref?: React.Ref<HTMLElement>
  /** Le titre de la page, en petit, montré quand celui de la page est passé sous l'en-tête. */
  titre?: React.ReactNode
  titreVisible?: boolean
}

export function Header({
  className,
  fixed,
  titre,
  titreVisible = false,
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
        <SidebarTrigger variant='outline' className='scale-125 md:hidden' />
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
