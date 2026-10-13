import { createFileRoute } from '@tanstack/react-router'
import { PageDsm } from '@/features/declarations/page-dsm'

export const Route = createFileRoute('/bo/operateurs/$id/dsm/$dsm')({
  component: function Dsm() {
    const { id, dsm } = Route.useParams()
    return (
      <PageDsm
        key={`${id}-${dsm}`}
        espace='bo'
        operateur={Number(id)}
        id={Number(dsm)}
      />
    )
  },
})
