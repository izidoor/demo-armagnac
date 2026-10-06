import { createFileRoute } from '@tanstack/react-router'
import { espacePortail } from '@/components/layout/data/espaces'
import { Accueil } from '@/features/accueil'
import { CarteRechercheOperateur } from '@/features/operateurs/portail/recherche-operateur'

export const Route = createFileRoute('/portail/')({
  component: () => (
    <Accueil espace={espacePortail}>
      <CarteRechercheOperateur />
    </Accueil>
  ),
})
