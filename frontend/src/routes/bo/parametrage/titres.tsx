import { createFileRoute } from '@tanstack/react-router'
import { titres } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/titres')({
  component: () => <PageListeSimple config={titres} />,
})
