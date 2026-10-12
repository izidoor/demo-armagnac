import { useLocation } from '@tanstack/react-router'
import { parametrage } from '@/components/layout/data/espaces'

/** En-tête de chaque section, par adresse (familles de la spec 01 §2.1). */
const descriptions: Record<string, string> = {
  '/bo/parametrage/geographie':
    'Continents et zones commerciales, pays et régions (§4.11.3).',
  '/bo/parametrage/qualification':
    "Titres, labels, types de visite, langues, centres d'intérêt et membres du BNIA (§4.11.4).",
  '/bo/parametrage/commercialisation':
    'Marques, circuits de distribution, produits commercialisés et contenances (§4.11.5).',
  '/bo/parametrage/classes-tva':
    'Classes de TVA et historique de leurs taux (§4.11.1).',
}

/** Une entrée par section ; tous les référentiels d'une section mènent à la même page. */
export const sections = parametrage.items.map((item) => {
  const url = String(item.items?.[0]?.url ?? item.url)
  return {
    title: item.title,
    icon: item.icon,
    url,
    description: descriptions[url],
  }
})

export function useSectionActive() {
  const { pathname } = useLocation()
  return sections.find((s) => pathname.startsWith(s.url))
}
