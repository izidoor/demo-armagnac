import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageContratsBnia } from '@/features/contrats/page-contrats-bnia'

/** L'état et le numéro de la liste, gardés dans l'adresse (`useFiltresAdresse`). */
export const Route = createFileRoute('/bo/contrats/')({
  validateSearch: z.object({
    etat: z.enum(['TOUS', 'PROJET', 'CONCLU', 'EXECUTE', 'CLOS']).optional(),
    numero: z.coerce.string().optional(),
  }),
  component: PageContratsBnia,
})
