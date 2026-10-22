import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageUtilisateurs } from '@/features/acces/page-utilisateurs'

/** Les filtres de la liste, gardés dans l'adresse (`useFiltresAdresse`). */
export const Route = createFileRoute('/bo/acces/utilisateurs/')({
  validateSearch: z.object({
    recherche: z.coerce.string().optional(),
    statut: z.enum(['ACTIF', 'BLOQUE']).optional(),
  }),
  component: PageUtilisateurs,
})
