import { createFileRoute } from '@tanstack/react-router'
import { PageTauxCvo } from '@/features/declarations/taux-cvo'

export const Route = createFileRoute('/bo/parametrage/taux-cvo')({
  component: PageTauxCvo,
})
