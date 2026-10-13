import { createFileRoute } from '@tanstack/react-router'
import { PageDsm } from '@/features/declarations/page-dsm'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/portail/declarations/$dsm')({
  component: function Dsm() {
    const { dsm } = Route.useParams()
    const { actif } = useMoi()
    if (!actif) return null
    return (
      <PageDsm
        key={dsm}
        espace='portail'
        operateur={actif.operateur}
        id={Number(dsm)}
      />
    )
  },
})
