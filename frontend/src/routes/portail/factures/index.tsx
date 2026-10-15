import { createFileRoute } from '@tanstack/react-router'
import { PageMesFactures } from '@/features/facturation/portail-factures'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/portail/factures/')({
  component: function MesFactures() {
    const { actif } = useMoi()
    if (!actif) return null
    return <PageMesFactures key={actif.operateur} operateur={actif.operateur} />
  },
})
