import { Outlet, useLocation } from '@tanstack/react-router'
import { useRetour } from '@/hooks/use-retour'
import { PageSections } from '@/components/layout/page-sections'
import { PageImbriquee } from '@/components/metier/page-imbriquee'
import { sections, useSectionActive } from './sections-parametrage'

/**
 * Paramètres de l'application (spec 01, §6.1) : sections à gauche (le BNIA, puis les référentiels),
 * contenu de la section à droite.
 */
export function PageParametrage() {
  // Le détail d'une classe de TVA est une page de second niveau : la flèche de l'en-tête ramène aux classes.
  const detail = /^\/bo\/parametrage\/classes-tva\/[^/]+/.test(
    useLocation().pathname
  )
  const retour = useRetour({ to: '/bo/parametrage/classes-tva' })
  return (
    <PageSections
      titre="Paramètres de l'application"
      sections={sections}
      active={useSectionActive()?.cle}
      retour={detail ? { libelle: 'Retour', onRetour: retour } : undefined}
    >
      {/* La section, à droite du menu des sections, est la zone de contenu de ses tableaux et de ses champs : leurs seuils
          (`@container/content`) portent sur sa largeur, pas sur celle de la page. */}
      <div className='@container/content'>
        <PageImbriquee value={true}>
          <Outlet />
        </PageImbriquee>
      </div>
    </PageSections>
  )
}
