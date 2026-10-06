import { useEffect, useState } from 'react'

/** La valeur, une fois qu'elle n'a plus changé pendant le délai donné. */
export function useDebounce<T>(valeur: T, delai = 300): T {
  const [stable, setStable] = useState(valeur)
  useEffect(() => {
    const minuteur = setTimeout(() => setStable(valeur), delai)
    return () => clearTimeout(minuteur)
  }, [valeur, delai])
  return stable
}
