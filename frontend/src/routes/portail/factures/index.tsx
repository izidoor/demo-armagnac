import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageMesFactures } from '@/features/facturation/portail-factures'
import { useMoi } from '@/features/session/session'

/** Les filtres de la liste, gardés dans l'adresse (`useFiltresAdresse`). */
export const Route = createFileRoute('/portail/factures/')({
  validateSearch: z.object({
    numero: z.coerce.string().optional(),
    periode: z.coerce.string().optional(),
    sens: z.enum(['FACTURE', 'AVOIR']).optional(),
    du: z.string().optional(),
    au: z.string().optional(),
  }),
  component: function MesFactures() {
    const { actif } = useMoi()
    if (!actif) return null
    return <PageMesFactures key={actif.operateur} operateur={actif.operateur} />
  },
})
