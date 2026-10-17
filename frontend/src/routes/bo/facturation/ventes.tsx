import { createFileRoute } from '@tanstack/react-router'
import { PageVentes } from '@/features/facturation/page-ventes'

export const Route = createFileRoute('/bo/facturation/ventes')({
  component: PageVentes,
})
