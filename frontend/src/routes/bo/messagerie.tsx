import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageMessagerie } from '@/features/messagerie/page-messagerie'

/** Spec 15 §6.1 : la messagerie du BNIA, conversations de tous les opérateurs. */
export const Route = createFileRoute('/bo/messagerie')({
  validateSearch: z.object({
    nonLues: z.boolean().optional(),
    conversation: z.number().optional(),
    message: z.number().optional(),
  }),
  component: function Messagerie() {
    const selection = Route.useSearch()
    const navigate = Route.useNavigate()
    return (
      <PageMessagerie
        espace='bo'
        selection={selection}
        onSelection={(s) => navigate({ search: s })}
      />
    )
  },
})
