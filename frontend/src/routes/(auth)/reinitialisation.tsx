import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { Reinitialisation } from '@/features/auth/mot-de-passe'

export const Route = createFileRoute('/(auth)/reinitialisation')({
  validateSearch: z.object({ jeton: z.string().catch('') }),
  component: function RouteReinitialisation() {
    const { jeton } = Route.useSearch()
    return <Reinitialisation jeton={jeton} />
  },
})
