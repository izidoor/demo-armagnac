import { createFileRoute } from '@tanstack/react-router'
import { labels } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/labels')({
  component: () => <PageListeSimple config={labels} />,
})
