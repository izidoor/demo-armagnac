import { createFileRoute } from '@tanstack/react-router'
import { FormulaireContrat } from '@/features/contrats/formulaire-contrat'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/portail/contrats/nouveau')({
  component: function NouveauContrat() {
    const { actif } = useMoi()
    if (!actif) return null
    return (
      <FormulaireContrat key={actif.operateur} operateur={actif.operateur} />
    )
  },
})
