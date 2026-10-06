import { createFileRoute } from '@tanstack/react-router'
import { PagePays } from '@/features/referentiels/pays/page-pays'

export const Route = createFileRoute('/bo/parametrage/pays')({
  component: PagePays,
})
