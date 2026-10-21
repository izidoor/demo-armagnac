import { createFileRoute } from '@tanstack/react-router'
import { espacePortail } from '@/components/layout/data/espaces'
import { Accueil } from '@/features/accueil'
import { TableauDeBordOperateur } from '@/features/accueil/tableau-de-bord-operateur'

export const Route = createFileRoute('/portail/')({
  component: () => (
    <Accueil
      espace={espacePortail}
      tableauDeBord={<TableauDeBordOperateur />}
    />
  ),
})
