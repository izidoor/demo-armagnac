import { Outlet } from '@tanstack/react-router'
import { PageSections } from '@/components/layout/page-sections'
import { PageImbriquee } from '@/components/metier/page-imbriquee'
import { sections, useSectionActive } from './sections-parametrage'

/**
 * Paramètres de l'application (spec 01, §6.1) : sections à gauche (le BNIA, puis les référentiels),
 * contenu de la section à droite.
 */
export function PageParametrage() {
  return (
    <PageSections
      titre="Paramètres de l'application"
      sections={sections}
      active={useSectionActive()?.cle}
    >
      <PageImbriquee value={true}>
        <Outlet />
      </PageImbriquee>
    </PageSections>
  )
}
