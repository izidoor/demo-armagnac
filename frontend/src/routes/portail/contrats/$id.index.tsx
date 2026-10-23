import { createFileRoute } from '@tanstack/react-router'
import { PageContrat } from '@/features/contrats/page-contrat'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/portail/contrats/$id/')({
  component: function Contrat() {
    const { id } = Route.useParams()
    const { actif } = useMoi()
    if (!actif) return null
    return (
      <PageContrat
        key={id}
        espace='portail'
        operateur={actif.operateur}
        id={Number(id)}
      />
    )
  },
})
