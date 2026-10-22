import { useLocation } from '@tanstack/react-router'
import { parametrage } from '@/components/layout/data/espaces'
import { type SectionPage } from '@/components/layout/page-sections'

/** En-tête de chaque section, par adresse (familles de la spec 01 §2.1). */
const descriptions: Record<string, string> = {
  '/bo/parametrage/bnia': 'Fiche, contacts et facturation du BNIA.',
  '/bo/parametrage/geographie':
    'Continents et zones commerciales, pays et régions.',
  '/bo/parametrage/qualification':
    "Titres, labels, types de visite, langues, centres d'intérêt et membres du BNIA.",
  '/bo/parametrage/commercialisation':
    'Marques, circuits de distribution, produits commercialisés et contenances.',
  '/bo/parametrage/classes-tva': 'Classes de TVA et historique de leurs taux.',
  '/bo/parametrage/taux-cvo': 'Grille des taux de la cotisation CVO des DSM.',
  '/bo/parametrage/securite':
    'Double authentification, pour toute l’application.',
}

/** Une entrée par section ; tous les référentiels d'une section mènent à la même page. */
export const sections: SectionPage[] = parametrage.items.map((item) => {
  const url = String(item.items?.[0]?.url ?? item.url)
  return {
    cle: url,
    titre: item.title,
    icon: item.icon,
    url,
    description: descriptions[url],
  }
})

export function useSectionActive() {
  const { pathname } = useLocation()
  return sections.find((s) => pathname.startsWith(s.cle))
}
