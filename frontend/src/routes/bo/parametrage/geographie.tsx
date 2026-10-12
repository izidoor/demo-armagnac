import { createFileRoute } from '@tanstack/react-router'
import { SectionGeographie } from '@/features/referentiels/parametrage/sections'

export const Route = createFileRoute('/bo/parametrage/geographie')({
  component: SectionGeographie,
})
