import { createFileRoute } from '@tanstack/react-router'
import { typesVisite } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/types-visite')({
  component: () => <PageListeSimple config={typesVisite} />,
})
