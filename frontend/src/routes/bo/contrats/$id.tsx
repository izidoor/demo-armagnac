import { createFileRoute } from '@tanstack/react-router'
import { PageContrat } from '@/features/contrats/page-contrat'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/bo/contrats/$id')({
  component: function Contrat() {
    const { id } = Route.useParams()
    const { actif } = useMoi()
    if (!actif) return null
    return (
      <PageContrat
        key={id}
        espace='bo'
        operateur={actif.operateur}
        id={Number(id)}
      />
    )
  },
})
