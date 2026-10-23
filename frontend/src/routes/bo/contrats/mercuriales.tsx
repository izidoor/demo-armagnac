import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageMercuriales } from '@/features/contrats/page-mercuriales'

/** La période des mercuriales, gardée dans l'adresse ; par défaut, les 12 derniers mois. */
export const Route = createFileRoute('/bo/contrats/mercuriales')({
  validateSearch: z.object({
    du: z.string().optional(),
    au: z.string().optional(),
  }),
  component: PageMercuriales,
})
