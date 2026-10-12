import { createFileRoute } from '@tanstack/react-router'
import { SectionQualification } from '@/features/referentiels/parametrage/sections'

export const Route = createFileRoute('/bo/parametrage/qualification')({
  component: SectionQualification,
})
