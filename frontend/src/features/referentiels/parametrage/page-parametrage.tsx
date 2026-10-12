import { Outlet } from '@tanstack/react-router'
import { Separator } from '@/components/ui/separator'
import { ActionsEntete } from '@/components/layout/actions-entete'
import { Header } from '@/components/layout/header'
import { Main } from '@/components/layout/main'
import { PageImbriquee } from '@/components/metier/page-imbriquee'
import { NavParametrage } from './nav-parametrage'
import { useSectionActive } from './sections-parametrage'

/** Paramétrage (spec 01, §6.1), mis en page comme /settings du gabarit : rubriques à gauche, référentiel à droite. */
export function PageParametrage() {
  const section = useSectionActive()
  return (
    <>
      <Header fixed>
        <ActionsEntete />
      </Header>
      <Main fixed>
        <div className='space-y-0.5'>
          <h1 className='text-2xl font-bold tracking-tight md:text-3xl'>
            Paramétrage
          </h1>
          <p className='text-muted-foreground'>
            Référentiels communs aux opérateurs : codes géographiques,
            qualification, commercialisation, TVA.
          </p>
        </div>
        <Separator className='my-4 lg:my-6' />
        <div className='relative flex flex-1 flex-col space-y-2 overflow-hidden md:flex-row md:space-y-0 md:space-x-12'>
          <aside className='top-0 md:sticky md:w-56 md:shrink-0 md:overflow-y-auto'>
            <NavParametrage />
          </aside>
          {/* relative : les éléments en position absolue (sr-only) restent dans le défilement, sans
              allonger la page. */}
          <div className='relative min-w-0 flex-1 overflow-y-auto p-1 pe-6'>
            {section && (
              <>
                <h3 className='text-lg font-medium'>{section.title}</h3>
                <p className='text-muted-foreground text-sm'>
                  {section.description}
                </p>
                <Separator className='mt-4 mb-8' />
              </>
            )}
            <PageImbriquee value={true}>
              <Outlet />
            </PageImbriquee>
          </div>
        </div>
      </Main>
    </>
  )
}
