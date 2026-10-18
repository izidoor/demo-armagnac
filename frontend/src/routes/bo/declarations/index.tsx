import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageDsmBnia } from '@/features/declarations/page-dsm-bnia'

export const Route = createFileRoute('/bo/declarations/')({
  validateSearch: z.object({
    vue: z.enum(['en-cours', 'validees', 'facturees']).optional(),
    etat: z.string().optional(),
  }),
  component: function Dsm() {
    const { vue, etat } = Route.useSearch()
    return <PageDsmBnia vue={vue} etat={etat} />
  },
})
