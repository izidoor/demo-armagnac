import { createFileRoute } from '@tanstack/react-router'
import { Skeleton } from '@/components/ui/skeleton'
import { useContrat } from '@/features/contrats/api'
import { FormulaireContrat } from '@/features/contrats/formulaire-contrat'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/portail/contrats/$id/modifier')({
  component: function ModifierContrat() {
    const { id } = Route.useParams()
    const { actif } = useMoi()
    const contrat = useContrat('portail', actif?.operateur ?? 0, Number(id))
    if (!actif) return null
    if (!contrat.data) return <Skeleton className='m-4 h-96' />
    return (
      <FormulaireContrat
        key={`${contrat.data.id}-${contrat.data.version.numero}`}
        operateur={actif.operateur}
        contrat={contrat.data}
      />
    )
  },
})
