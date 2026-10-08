import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { AccepterInvitation } from '@/features/auth/liens'

export const Route = createFileRoute('/(auth)/invitation')({
  validateSearch: z.object({ jeton: z.string().catch('') }),
  component: function RouteInvitation() {
    const { jeton } = Route.useSearch()
    return <AccepterInvitation jeton={jeton} />
  },
})
