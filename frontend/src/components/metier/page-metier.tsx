import { type ReactNode, useContext, useEffect, useRef, useState } from 'react'
import { ActionsEntete } from '@/components/layout/actions-entete'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { PageImbriquee } from './page-imbriquee'

type PageMetierProps = {
  titre: string
  description: ReactNode
  actions?: ReactNode
  children: ReactNode
}

/**
 * Gabarit commun des pages métier : en-tête, titre, description, actions. Dans le Paramétrage, l'en-tête
 * et le cadre viennent de la page Paramétrage, et le titre est celui d'un référentiel, sous l'en-tête de la section.
 */
export function PageMetier({
  titre,
  description,
  actions,
  children,
}: PageMetierProps) {
  const imbriquee = useContext(PageImbriquee)
  const [refTitre, titreSorti] = useTitreSorti()
  const contenu = (
    <>
      <div className='mb-4 flex flex-wrap items-end justify-between gap-2'>
        <div>
          {imbriquee ? (
            <h4 className='text-base font-semibold'>{titre}</h4>
          ) : (
            <h1
              ref={refTitre}
              className='mb-2 text-[1.75rem] leading-tight font-bold tracking-tight'
            >
              {titre}
            </h1>
          )}
          <p
            className={
              imbriquee
                ? 'text-muted-foreground text-sm'
                : 'text-muted-foreground'
            }
          >
            {description}
          </p>
        </div>
        {actions}
      </div>
      {children}
    </>
  )
  if (imbriquee) return contenu
  return (
    <>
      <Header fixed titre={titre} titreVisible={titreSorti}>
        <ActionsEntete />
      </Header>
      <Main>{contenu}</Main>
    </>
  )
}

/** Hauteur de l'en-tête fixe (`h-16`). */
const HAUTEUR_ENTETE = 64

/** Le titre est entièrement passé sous l'en-tête fixe : l'en-tête le reprend, en petit. */
function useTitreSorti() {
  const ref = useRef<HTMLHeadingElement>(null)
  const [sorti, setSorti] = useState(false)
  useEffect(() => {
    const titre = ref.current
    if (!titre) return
    const observateur = new IntersectionObserver(
      ([e]) =>
        setSorti(
          !e.isIntersecting && e.boundingClientRect.top < HAUTEUR_ENTETE
        ),
      { rootMargin: `-${HAUTEUR_ENTETE}px 0px 0px 0px` }
    )
    observateur.observe(titre)
    return () => observateur.disconnect()
  }, [])
  return [ref, sorti] as const
}
