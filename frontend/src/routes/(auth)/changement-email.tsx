import { z } from 'zod'
import { createFileRoute } from '@tanstack/react-router'
import { ConfirmerEmail } from '@/features/auth/liens'

export const Route = createFileRoute('/(auth)/changement-email')({
  validateSearch: z.object({ jeton: z.string().catch('') }),
  component: function RouteChangementEmail() {
    const { jeton } = Route.useSearch()
    return <ConfirmerEmail jeton={jeton} />
  },
})
