import { createFileRoute } from '@tanstack/react-router'
import { espacePortail } from '@/components/layout/data/espaces'
import { Accueil } from '@/features/accueil'

export const Route = createFileRoute('/portail/')({
  component: () => <Accueil espace={espacePortail} />,
})
