import { createFileRoute } from '@tanstack/react-router'
import { PageParametrage } from '@/features/referentiels/parametrage/page-parametrage'

export const Route = createFileRoute('/bo/parametrage')({
  component: PageParametrage,
})
