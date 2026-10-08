import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageOperateur } from '@/features/operateurs/fiche/page-operateur'

export const Route = createFileRoute('/bo/operateurs/$id/')({
  validateSearch: z.object({ onglet: z.string().optional() }),
  component: function Fiche() {
    const { id } = Route.useParams()
    const { onglet } = Route.useSearch()
    return (
      <PageOperateur key={id} espace='bo' id={Number(id)} onglet={onglet} />
    )
  },
})
