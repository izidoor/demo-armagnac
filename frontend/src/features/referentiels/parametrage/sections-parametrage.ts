import { useLocation } from '@tanstack/react-router'
import { parametrage } from '@/components/layout/data/espaces'
import { type SectionPage } from '@/components/layout/page-sections'

/** En-tête de chaque section, par adresse (familles de la spec 01 §2.1). */
const descriptions: Record<string, string> = {
  '/bo/parametrage/bnia':
    'Fiche, contacts et facturation du BNIA (spec 02 §6.1, spec 08 §6.1).',
  '/bo/parametrage/geographie':
    'Continents et zones commerciales, pays et régions (§4.11.3).',
  '/bo/parametrage/qualification':
    "Titres, labels, types de visite, langues, centres d'intérêt et membres du BNIA (§4.11.4).",
  '/bo/parametrage/commercialisation':
    'Marques, circuits de distribution, produits commercialisés et contenances (§4.11.5).',
  '/bo/parametrage/classes-tva':
    'Classes de TVA et historique de leurs taux (§4.11.1).',
  '/bo/parametrage/taux-cvo':
    'Grille des taux de la cotisation CVO des DSM (§4.11.2).',
  '/bo/parametrage/securite':
    'Double authentification, pour toute l’application (spec 03, RG-AUT-16).',
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
