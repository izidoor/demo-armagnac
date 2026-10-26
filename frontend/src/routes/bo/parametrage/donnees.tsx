import { createFileRoute } from '@tanstack/react-router'
import { SectionDonnees } from '@/features/sauvegarde/section-donnees'

export const Route = createFileRoute('/bo/parametrage/donnees')({
  component: SectionDonnees,
})
