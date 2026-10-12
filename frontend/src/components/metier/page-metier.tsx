import { type ReactNode, useContext } from 'react'
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
  const contenu = (
    <>
      <div className='mb-4 flex flex-wrap items-end justify-between gap-2'>
        <div>
          {imbriquee ? (
            <h4 className='text-base font-semibold'>{titre}</h4>
          ) : (
            <h1 className='text-2xl font-bold tracking-tight'>{titre}</h1>
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
      <Header fixed>
        <ActionsEntete />
      </Header>
      <Main>{contenu}</Main>
    </>
  )
}
