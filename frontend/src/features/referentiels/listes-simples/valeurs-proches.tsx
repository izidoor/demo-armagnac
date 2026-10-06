import { useQuery } from '@tanstack/react-query'
import { type ValeurSimple } from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'

type ValeursProchesProps = {
  libelle: string
  proches: (libelle: string) => Promise<ValeurSimple[]>
  /** Code de la valeur modifiée, à ne pas proposer. */
  sauf?: string
}

/**
 * Valeurs au libellé proche du texte saisi (RG-REF-15) : invite à reprendre ou réactiver une marque
 * existante plutôt qu'à en créer un doublon. Réutilisable dans la fiche opérateur (RG-REF-14).
 */
export function ValeursProches({
  libelle,
  proches,
  sauf,
}: ValeursProchesProps) {
  const texte = useDebounce(libelle.trim())
  const { data } = useQuery({
    queryKey: ['proches', texte],
    queryFn: () => proches(texte),
    enabled: texte.length >= 2,
  })
  const valeurs = (data ?? []).filter((v) => v.code !== sauf)
  if (texte.length < 2 || valeurs.length === 0) return null
  return (
    <span className='block'>
      Libellés proches :{' '}
      {valeurs.map((v, i) => (
        <span key={v.code}>
          {i > 0 && ', '}
          <span className='text-foreground font-medium'>{v.libelle}</span> (
          {v.code}
          {v.actif ? '' : ', inactive'})
        </span>
      ))}
    </span>
  )
}
