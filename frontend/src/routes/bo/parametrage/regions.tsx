import { createFileRoute } from '@tanstack/react-router'
import { PageRegions } from '@/features/referentiels/regions/page-regions'

export const Route = createFileRoute('/bo/parametrage/regions')({
  component: PageRegions,
})
