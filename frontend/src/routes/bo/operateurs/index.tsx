import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageOperateurs } from '@/features/operateurs/page-operateurs'

/** Les filtres de la liste, gardés dans l'adresse (`useFiltresAdresse`). */
export const Route = createFileRoute('/bo/operateurs/')({
  validateSearch: z.object({
    type: z.enum(['BNIA', 'PRODUCTEUR', 'NEGOCIANT', 'AUTRE']).optional(),
    recherche: z.coerce.string().optional(),
    etat: z.enum(['inactifs', 'tous']).optional(),
    completude: z.string().optional(),
  }),
  component: PageOperateurs,
})
