import { useMoi } from '@/features/session/session'
import { PageOperateur } from '../fiche/page-operateur'

/**
 * « Mon entreprise » (spec 02, §6.2) : la fiche de l'opérateur actif ; les champs de RG-OPE-36
 * modifiables par le module Contacts.
 */
export function PageEntreprise() {
  const { actif } = useMoi()
  if (!actif) return null
  return (
    <PageOperateur
      key={actif.operateur}
      espace='portail'
      id={actif.operateur}
    />
  )
}
