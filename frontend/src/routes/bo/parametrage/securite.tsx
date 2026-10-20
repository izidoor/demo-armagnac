import { createFileRoute } from '@tanstack/react-router'
import { SectionSecurite } from '@/features/acces/section-securite'

export const Route = createFileRoute('/bo/parametrage/securite')({
  component: SectionSecurite,
})
