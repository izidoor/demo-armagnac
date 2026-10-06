import { createFileRoute } from '@tanstack/react-router'
import { langues } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/langues')({
  component: () => <PageListeSimple config={langues} />,
})
