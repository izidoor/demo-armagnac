import { type ReactNode } from 'react'
import { ActionsEntete } from '@/components/layout/actions-entete'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { type Espace } from '@/components/layout/types'
import { Search } from '@/components/search'

type AccueilProps = {
  espace: Espace
  /** Cartes propres à l'espace. */
  children?: ReactNode
  /** Tableau de bord de l'espace, au-dessus des cartes. */
  tableauDeBord?: ReactNode
}

export function Accueil({ espace, children, tableauDeBord }: AccueilProps) {
  return (
    <>
      <Header fixed>
        <Search placeholder='Rechercher' />
        <ActionsEntete />
      </Header>

      <Main>
        <div className='mb-4'>
          <h1 className='mb-2 text-[1.75rem] leading-tight font-bold tracking-tight'>
            {tableauDeBord ? 'Tableau de bord' : espace.nom}
          </h1>
          <p className='text-muted-foreground'>
            {tableauDeBord ? espace.nom : 'Accueil'}
          </p>
        </div>
        {tableauDeBord && <div className='mb-10'>{tableauDeBord}</div>}
        {children && (
          <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-3'>
            {children}
          </div>
        )}
      </Main>
    </>
  )
}
