import { createFileRoute } from '@tanstack/react-router'
import { marques } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/marques')({
  component: () => <PageListeSimple config={marques} />,
})
