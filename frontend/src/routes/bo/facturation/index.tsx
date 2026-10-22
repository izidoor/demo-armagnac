import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PagePieces } from '@/features/facturation/page-pieces'

/** Le client, l'onglet et les filtres de la liste, gardés dans l'adresse (`useFiltresAdresse`). */
export const Route = createFileRoute('/bo/facturation/')({
  validateSearch: z.object({
    client: z.coerce.string().optional(),
    onglet: z.enum(['brouillons', 'emises', 'payees']).optional(),
    numero: z.coerce.string().optional(),
    acheteur: z.coerce.string().optional(),
    periode: z.coerce.string().optional(),
    du: z.string().optional(),
    au: z.string().optional(),
  }),
  component: PagePieces,
})
