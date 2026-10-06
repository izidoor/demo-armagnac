import { createFileRoute } from '@tanstack/react-router'
import { circuitsDistribution } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/circuits-distribution')({
  component: () => <PageListeSimple config={circuitsDistribution} />,
})
