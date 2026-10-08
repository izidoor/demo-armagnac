import { createFileRoute, redirect } from '@tanstack/react-router'
import { espaceDe, exigerSession } from '@/features/session/session'

/** L'application s'ouvre dans l'espace de l'opérateur actif (RG-OPE-49). */
export const Route = createFileRoute('/')({
  beforeLoad: async ({ context, location }) => {
    const { actif } = await exigerSession(context.queryClient, location.href)
    throw redirect({ to: espaceDe(actif) === 'bo' ? '/bo' : '/portail' })
  },
})
