import { useEffect, useRef, useState } from 'react'

/** Hauteur de l'en-tête fixe (`h-16`). */
const HAUTEUR_ENTETE = 64

/** Le titre est entièrement passé sous l'en-tête fixe : l'en-tête le reprend, en petit (`PageMetier`, `PageSections`). */
export function useTitreSorti() {
  const ref = useRef<HTMLHeadingElement>(null)
  const [sorti, setSorti] = useState(false)
  useEffect(() => {
    const titre = ref.current
    if (!titre) return
    const observateur = new IntersectionObserver(
      ([e]) =>
        setSorti(
          !e.isIntersecting && e.boundingClientRect.top < HAUTEUR_ENTETE
        ),
      { rootMargin: `-${HAUTEUR_ENTETE}px 0px 0px 0px` }
    )
    observateur.observe(titre)
    return () => observateur.disconnect()
  }, [])
  return [ref, sorti] as const
}
