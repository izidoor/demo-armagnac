import { createFileRoute } from '@tanstack/react-router'
import { espaceBackOffice } from '@/components/layout/data/espaces'
import { Accueil } from '@/features/accueil'

export const Route = createFileRoute('/bo/')({
  component: () => <Accueil espace={espaceBackOffice} />,
})
