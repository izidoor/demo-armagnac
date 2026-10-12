import { createFileRoute } from '@tanstack/react-router'
import { SectionCommercialisation } from '@/features/referentiels/parametrage/sections'

export const Route = createFileRoute('/bo/parametrage/commercialisation')({
  component: SectionCommercialisation,
})
