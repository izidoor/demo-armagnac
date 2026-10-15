import { createFileRoute } from '@tanstack/react-router'
import { SectionBnia } from '@/features/operateurs/fiche/section-bnia'

export const Route = createFileRoute('/bo/parametrage/bnia')({
  component: SectionBnia,
})
