import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PagePieces } from '@/features/facturation/page-pieces'

export const Route = createFileRoute('/bo/facturation/')({
  validateSearch: z.object({
    client: z.string().optional(),
    onglet: z.enum(['emises', 'brouillons']).optional(),
  }),
  component: function Pieces() {
    const { client, onglet } = Route.useSearch()
    return <PagePieces client={client} onglet={onglet ?? 'emises'} />
  },
})
