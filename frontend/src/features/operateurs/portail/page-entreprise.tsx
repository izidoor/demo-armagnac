import { PageOperateur } from '../fiche/page-operateur'
import { SansOperateur } from './sans-operateur'
import { useOperateurPortail } from './use-operateur-portail'

/** « Mon entreprise » (spec 02, §6.2) : la fiche de l'opérateur, champs de RG-OPE-36 modifiables. */
export function PageEntreprise() {
  const { operateur, chargement } = useOperateurPortail()
  if (!operateur)
    return <SansOperateur titre='Mon entreprise' chargement={chargement} />
  return <PageOperateur espace='portail' id={operateur.id} />
}
