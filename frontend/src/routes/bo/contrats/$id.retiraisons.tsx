import { createFileRoute } from '@tanstack/react-router'
import { retiraisonsSurOrdinateur } from '@/features/contrats/acces-retiraisons'
import { PageRetiraisons } from '@/features/contrats/page-retiraisons'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/bo/contrats/$id/retiraisons')({
  beforeLoad: ({ params }) => retiraisonsSurOrdinateur('bo', params.id),
  component: function Retiraisons() {
    const { id } = Route.useParams()
    const { actif } = useMoi()
    if (!actif) return null
    return (
      <PageRetiraisons
        key={id}
        espace='bo'
        operateur={actif.operateur}
        id={Number(id)}
      />
    )
  },
})
