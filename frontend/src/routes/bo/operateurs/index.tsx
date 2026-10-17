import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageOperateurs } from '@/features/operateurs/page-operateurs'

export const Route = createFileRoute('/bo/operateurs/')({
  validateSearch: z.object({
    type: z.enum(['BNIA', 'PRODUCTEUR', 'NEGOCIANT', 'AUTRE']).optional(),
    recherche: z.string().optional(),
  }),
  component: function Operateurs() {
    const { type, recherche } = Route.useSearch()
    return <PageOperateurs type={type} recherche={recherche} />
  },
})
