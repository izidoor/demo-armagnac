import { createFileRoute } from '@tanstack/react-router'
import { PageVendeur } from '@/features/facturation/page-vendeur'

export const Route = createFileRoute('/bo/parametrage/facturation')({
  component: PageVendeur,
})
