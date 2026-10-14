import { type ReactNode } from 'react'
import { ActionsEntete } from '@/components/layout/actions-entete'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { type Espace } from '@/components/layout/types'
import { Search } from '@/components/search'
import { InfoSystemeCard } from '@/features/systeme/info-systeme-card'

type AccueilProps = {
  espace: Espace
  /** Cartes propres à l'espace, après celles communes. */
  children?: ReactNode
}

export function Accueil({ espace, children }: AccueilProps) {
  return (
    <>
      <Header fixed>
        <Search placeholder='Rechercher' />
        <ActionsEntete />
      </Header>

      <Main>
        <div className='mb-4'>
          <h1 className='mb-2 text-[1.75rem] leading-tight font-bold tracking-tight'>
            {espace.nom}
          </h1>
          <p className='text-muted-foreground'>Accueil</p>
        </div>
        <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-3'>
          <InfoSystemeCard />
          {children}
        </div>
      </Main>
    </>
  )
}
