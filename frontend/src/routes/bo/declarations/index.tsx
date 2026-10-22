import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageDsmBnia } from '@/features/declarations/page-dsm-bnia'

/** L'onglet et les filtres de la liste, gardés dans l'adresse (`useFiltresAdresse`). */
export const Route = createFileRoute('/bo/declarations/')({
  validateSearch: z.object({
    vue: z.enum(['en-cours', 'validees', 'facturees']).optional(),
    etat: z.string().optional(),
    type: z.string().optional(),
    recherche: z.coerce.string().optional(),
    retard: z.boolean().optional(),
  }),
  component: PageDsmBnia,
})
