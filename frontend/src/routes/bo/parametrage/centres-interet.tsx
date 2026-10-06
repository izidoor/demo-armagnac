import { createFileRoute } from '@tanstack/react-router'
import { centresInteret } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/centres-interet')({
  component: () => <PageListeSimple config={centresInteret} />,
})
