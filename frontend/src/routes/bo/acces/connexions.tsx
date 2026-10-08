import { createFileRoute } from '@tanstack/react-router'
import { PageConnexions } from '@/features/acces/page-connexions'

export const Route = createFileRoute('/bo/acces/connexions')({
  component: PageConnexions,
})
