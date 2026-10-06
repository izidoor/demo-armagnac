import { createFileRoute } from '@tanstack/react-router'
import { PageOperateur } from '@/features/operateurs/fiche/page-operateur'

export const Route = createFileRoute('/bo/operateurs/$id/')({
  component: function Fiche() {
    const { id } = Route.useParams()
    return <PageOperateur key={id} espace='bo' id={Number(id)} />
  },
})
