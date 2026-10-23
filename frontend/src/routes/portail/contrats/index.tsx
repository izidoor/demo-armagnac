import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageMesContrats } from '@/features/contrats/page-mes-contrats'

/** L'onglet de la liste, gardé dans l'adresse (`useFiltresAdresse`). */
export const Route = createFileRoute('/portail/contrats/')({
  validateSearch: z.object({
    onglet: z
      .enum([
        'A_APPROUVER',
        'EN_NEGOCIATION',
        'CONCLUS',
        'EXECUTES',
        'ANNULES_REFUSES',
      ])
      .optional(),
  }),
  component: PageMesContrats,
})
