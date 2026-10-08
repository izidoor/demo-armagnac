import { createFileRoute } from '@tanstack/react-router'
import { PageAnnuaire } from '@/features/operateurs/portail/page-annuaire'

export const Route = createFileRoute('/portail/annuaire/')({
  component: PageAnnuaire,
})
