import { createFileRoute } from '@tanstack/react-router'
import { zonesCommerciales } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/zones-commerciales')({
  component: () => <PageListeSimple config={zonesCommerciales} />,
})
