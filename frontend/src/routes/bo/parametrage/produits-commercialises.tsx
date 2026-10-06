import { createFileRoute } from '@tanstack/react-router'
import { produitsCommercialises } from '@/features/referentiels/listes-simples/listes'
import { PageListeSimple } from '@/features/referentiels/listes-simples/page-liste-simple'

export const Route = createFileRoute('/bo/parametrage/produits-commercialises')(
  {
    component: () => <PageListeSimple config={produitsCommercialises} />,
  }
)
