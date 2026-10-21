import { createFileRoute } from '@tanstack/react-router'
import { PageMesCotisations } from '@/features/facturation/portail-cotisations'

export const Route = createFileRoute('/portail/factures/cotisations')({
  component: PageMesCotisations,
})
