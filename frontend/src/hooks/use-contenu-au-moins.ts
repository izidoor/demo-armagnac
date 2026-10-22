import { useLayoutEffect, useState } from 'react'

/**
 * Vrai tant que la zone de contenu (`SidebarInset`, conteneur `@container/content`) fait au moins `rem` de large :
 * le pendant, en JavaScript, des variantes `@min-[…]/content:`, qui suit la barre latérale (ouverte, réduite, tiroir).
 * Mesurée avant l'affichage, puis à chaque redimensionnement.
 */
export function useContenuAuMoins(rem: number) {
  const [atteint, setAtteint] = useState(false)
  useLayoutEffect(() => {
    const zone = document.querySelector('[data-slot="sidebar-inset"]')
    if (!zone) return
    const seuil =
      rem * parseFloat(getComputedStyle(document.documentElement).fontSize)
    const mesurer = () => setAtteint(zone.clientWidth >= seuil)
    mesurer()
    const observateur = new ResizeObserver(mesurer)
    observateur.observe(zone)
    return () => observateur.disconnect()
  }, [rem])
  return atteint
}
