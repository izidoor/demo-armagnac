import { type ReactNode, useContext } from 'react'
import { useTitreSorti } from '@/hooks/use-titre-sorti'
import { ActionsEntete } from '@/components/layout/actions-entete'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { PageImbriquee } from './page-imbriquee'

type PageMetierProps = {
  titre: string
  /** Après le titre, séparé par un tiret, en petites capitales (le client d'une facture, par exemple). */
  complement?: string
  description: ReactNode
  actions?: ReactNode
  /**
   * La page occupe exactement la hauteur de l'écran, sans défilement : son contenu remplit la place restante et
   * défile par panneaux (la messagerie). Sur écran étroit, la page défile normalement.
   */
  pleineHauteur?: boolean
  children: ReactNode
}

/**
 * Gabarit commun des pages métier : en-tête, titre, description, actions. Dans le Paramétrage, l'en-tête
 * et le cadre viennent de la page Paramétrage, et le titre est celui d'un référentiel, sous l'en-tête de la section.
 */
export function PageMetier({
  titre,
  complement,
  description,
  actions,
  pleineHauteur = false,
  children,
}: PageMetierProps) {
  const imbriquee = useContext(PageImbriquee)
  const [refTitre, titreSorti] = useTitreSorti()
  const titreComplet = (
    <>
      {titre}
      {complement && (
        <>
          {' – '}
          <span className='[font-variant-caps:small-caps]'>{complement}</span>
        </>
      )}
    </>
  )
  const contenu = (
    <>
      <div className='mb-4 flex shrink-0 flex-wrap items-end justify-between gap-2'>
        <div>
          {imbriquee ? (
            <h4 className='text-base font-semibold'>{titreComplet}</h4>
          ) : (
            <h1
              ref={refTitre}
              className='mb-2 text-[1.75rem] leading-tight font-bold tracking-tight'
            >
              {titreComplet}
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
      {pleineHauteur ? (
        <div className='flex min-h-0 flex-1 flex-col'>{children}</div>
      ) : (
        children
      )}
    </>
  )
  if (imbriquee) return contenu
  return (
    <>
      <Header
        fixed
        titre={complement ? `${titre} – ${complement}` : titre}
        titreVisible={titreSorti}
      >
        <ActionsEntete />
      </Header>
      {pleineHauteur ? (
        <Main fixed className='max-lg:overflow-y-auto'>
          {contenu}
        </Main>
      ) : (
        <Main>{contenu}</Main>
      )}
    </>
  )
}
