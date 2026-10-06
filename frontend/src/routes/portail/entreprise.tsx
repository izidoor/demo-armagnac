import { createFileRoute } from '@tanstack/react-router'
import { PageEntreprise } from '@/features/operateurs/portail/page-entreprise'

export const Route = createFileRoute('/portail/entreprise')({
  component: PageEntreprise,
})
