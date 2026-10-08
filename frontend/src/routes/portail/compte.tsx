import { createFileRoute } from '@tanstack/react-router'
import { PageCompte } from '@/features/compte/page-compte'

export const Route = createFileRoute('/portail/compte')({
  component: PageCompte,
})
