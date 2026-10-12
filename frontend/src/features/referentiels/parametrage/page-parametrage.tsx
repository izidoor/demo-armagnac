import { Outlet } from '@tanstack/react-router'
import { PageSections } from '@/components/layout/page-sections'
import { PageImbriquee } from '@/components/metier/page-imbriquee'
import { sections, useSectionActive } from './sections-parametrage'

/** Paramétrage (spec 01, §6.1) : sections à gauche, référentiels de la section à droite. */
export function PageParametrage() {
  return (
    <PageSections
      titre='Paramétrage'
      description='Référentiels communs aux opérateurs : codes géographiques, qualification, commercialisation, TVA.'
      sections={sections}
      active={useSectionActive()?.cle}
    >
      <PageImbriquee value={true}>
        <Outlet />
      </PageImbriquee>
    </PageSections>
  )
}
