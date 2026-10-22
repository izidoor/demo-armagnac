import { useRef, useState, type CSSProperties, type TouchEvent } from 'react'

/** Au-delà de cette part de sa largeur, lâché, le tiroir se ferme. */
const SEUIL_DISTANCE = 0.3
/** Un geste vif ferme aussi, même court (px/ms), pourvu qu'il dépasse quelques millimètres (px). */
const SEUIL_VITESSE = 0.4
const DISTANCE_VIVE = 30
/** Mouvement à partir duquel le geste est reconnu, horizontal ou vertical (px). */
const SEUIL_SENS = 10

type Geste = {
  x: number
  y: number
  t: number
  horizontal?: boolean
  /** Le dernier décalage, lu au lâcher (l'état peut ne pas être encore rendu). */
  decalage: number
}

/**
 * Fermer un tiroir d'un balayage vers son bord : il suit le doigt, puis se ferme si le geste a été assez long ou assez
 * vif, sinon revient en place. Un geste d'abord vertical reste un défilement. Le tiroir se ferme depuis sa position :
 * l'animation de sortie part de la transformation courante.
 */
export function useBalayageFermeture({
  ouvert,
  fermer,
  cote,
}: {
  ouvert: boolean
  fermer: () => void
  /** Le bord où le tiroir est attaché : on le balaye vers ce bord. */
  cote: 'left' | 'right'
}) {
  const geste = useRef<Geste | null>(null)
  const [decalage, setDecalage] = useState(0)
  const [glisse, setGlisse] = useState(false)
  // À la réouverture, le tiroir repart de sa place, pas du décalage où un balayage l'a fermé.
  const [ouvertAvant, setOuvertAvant] = useState(ouvert)
  if (ouvert !== ouvertAvant) {
    setOuvertAvant(ouvert)
    if (ouvert) setDecalage(0)
  }
  const sens = cote === 'left' ? -1 : 1

  const gestes = {
    onTouchStart: (e: TouchEvent<HTMLElement>) => {
      const t = e.touches[0]
      geste.current = {
        x: t.clientX,
        y: t.clientY,
        t: e.timeStamp,
        decalage: 0,
      }
    },
    onTouchMove: (e: TouchEvent<HTMLElement>) => {
      const g = geste.current
      if (!g) return
      const t = e.touches[0]
      const dx = t.clientX - g.x
      const dy = t.clientY - g.y
      if (g.horizontal === undefined) {
        if (Math.abs(dx) < SEUIL_SENS && Math.abs(dy) < SEUIL_SENS) return
        g.horizontal = Math.abs(dx) > Math.abs(dy)
        if (g.horizontal) setGlisse(true)
      }
      // Vers le bord seulement : dans l'autre sens, le tiroir reste en place.
      if (!g.horizontal) return
      g.decalage = dx * sens > 0 ? dx : 0
      setDecalage(g.decalage)
    },
    onTouchEnd: (e: TouchEvent<HTMLElement>) => {
      const g = geste.current
      geste.current = null
      if (!g?.horizontal) return
      setGlisse(false)
      const distance = g.decalage * sens
      const vitesse = distance / Math.max(e.timeStamp - g.t, 1)
      const largeur = e.currentTarget.getBoundingClientRect().width
      if (
        distance > largeur * SEUIL_DISTANCE ||
        (distance > DISTANCE_VIVE && vitesse > SEUIL_VITESSE)
      )
        fermer()
      else setDecalage(0)
    },
    onTouchCancel: () => {
      geste.current = null
      setGlisse(false)
      setDecalage(0)
    },
  }

  const style: CSSProperties = {
    transform: decalage ? `translateX(${decalage}px)` : undefined,
    transition: glisse ? 'none' : 'transform 200ms ease-out',
    touchAction: 'pan-y',
  }

  return { gestes, style }
}
