import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { PageMessagerie } from '@/features/messagerie/page-messagerie'
import { useMoi } from '@/features/session/session'

/** Spec 15 §6.2 : la messagerie de l'opérateur actif. */
export const Route = createFileRoute('/portail/messagerie')({
  validateSearch: z.object({
    nonLues: z.boolean().optional(),
    conversation: z.number().optional(),
    message: z.number().optional(),
  }),
  component: function Messagerie() {
    const selection = Route.useSearch()
    const navigate = Route.useNavigate()
    const { actif } = useMoi()
    if (!actif) return null
    return (
      <PageMessagerie
        espace='portail'
        operateur={actif.operateur}
        selection={selection}
        onSelection={(s) => navigate({ search: s })}
      />
    )
  },
})
