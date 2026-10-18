import { createFileRoute } from '@tanstack/react-router'
import { PageMaFacture } from '@/features/facturation/portail-factures'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/portail/factures/$id')({
  component: function MaFacture() {
    const { id } = Route.useParams()
    const { actif } = useMoi()
    if (!actif) return null
    return (
      <PageMaFacture key={id} operateur={actif.operateur} id={Number(id)} />
    )
  },
})
