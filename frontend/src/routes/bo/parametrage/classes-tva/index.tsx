import { createFileRoute } from '@tanstack/react-router'
import { PageClassesTva } from '@/features/referentiels/classes-tva/page-classes-tva'

export const Route = createFileRoute('/bo/parametrage/classes-tva/')({
  component: PageClassesTva,
})
