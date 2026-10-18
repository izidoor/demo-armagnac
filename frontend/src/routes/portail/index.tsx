import { createFileRoute } from '@tanstack/react-router'
import { espacePortail } from '@/components/layout/data/espaces'
import { Accueil } from '@/features/accueil'
import { CarteNouveauxMessages } from '@/features/messagerie/carte-nouveaux-messages'
import { CarteRechercheOperateur } from '@/features/operateurs/portail/recherche-operateur'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/portail/')({
  component: function AccueilPortail() {
    const { actif } = useMoi()
    return (
      <Accueil espace={espacePortail}>
        {actif && <CarteNouveauxMessages operateur={actif.operateur} />}
        <CarteRechercheOperateur />
      </Accueil>
    )
  },
})
