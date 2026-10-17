import { createFileRoute } from '@tanstack/react-router'
import { espaceBackOffice } from '@/components/layout/data/espaces'
import { Accueil } from '@/features/accueil'
import { TableauDeBordBnia } from '@/features/accueil/tableau-de-bord-bnia'

export const Route = createFileRoute('/bo/')({
  component: () => (
    <Accueil espace={espaceBackOffice} tableauDeBord={<TableauDeBordBnia />} />
  ),
})
