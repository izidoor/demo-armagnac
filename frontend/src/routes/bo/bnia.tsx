import { createFileRoute, redirect } from '@tanstack/react-router'
import { exigerSession } from '@/features/session/session'

/** Menu « BNIA » (spec 02 §6.1) : la fiche réduite et l'annuaire de l'opérateur BNIA, l'opérateur actif. */
export const Route = createFileRoute('/bo/bnia')({
  beforeLoad: async ({ context, location }) => {
    const { actif } = await exigerSession(
      context.queryClient,
      location.href,
      'bo'
    )
    throw redirect({
      to: '/bo/operateurs/$id',
      params: { id: String(actif.operateur) },
    })
  },
})
