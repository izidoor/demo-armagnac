import { type ReactNode } from 'react'
import { ConfigDrawer } from '@/components/config-drawer'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { ThemeSwitch } from '@/components/theme-switch'

type PageMetierProps = {
  titre: string
  description: ReactNode
  actions?: ReactNode
  children: ReactNode
}

/** Gabarit commun des pages métier : en-tête, titre, description, actions. */
export function PageMetier({
  titre,
  description,
  actions,
  children,
}: PageMetierProps) {
  return (
    <>
      <Header fixed>
        <div className='ms-auto flex items-center gap-4'>
          <ThemeSwitch />
          <ConfigDrawer />
        </div>
      </Header>
      <Main>
        <div className='mb-4 flex flex-wrap items-end justify-between gap-2'>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>{titre}</h1>
            <p className='text-muted-foreground'>{description}</p>
          </div>
          {actions}
        </div>
        {children}
      </Main>
    </>
  )
}
