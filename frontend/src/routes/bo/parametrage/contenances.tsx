import { createFileRoute } from '@tanstack/react-router'
import { PageContenances } from '@/features/referentiels/contenances/page-contenances'

export const Route = createFileRoute('/bo/parametrage/contenances')({
  component: PageContenances,
})
