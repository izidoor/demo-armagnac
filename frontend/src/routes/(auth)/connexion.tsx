import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { Connexion } from '@/features/auth/connexion'

export const Route = createFileRoute('/(auth)/connexion')({
  validateSearch: z.object({ redirect: z.string().optional() }),
  component: function RouteConnexion() {
    const { redirect } = Route.useSearch()
    return <Connexion redirect={redirect} />
  },
})
