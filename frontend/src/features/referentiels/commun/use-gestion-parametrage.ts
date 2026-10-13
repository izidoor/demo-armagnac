import { useDroits, useMoi } from '@/features/session/session'

/**
 * Vrai si l'utilisateur connecté gère le module Paramétrage (RG-OPE-31) : créer, modifier, désactiver et
 * supprimer les valeurs des référentiels. Dans le back-office, l'opérateur actif est le BNIA.
 */
export function useGestionParametrage() {
  const { actif } = useMoi()
  return useDroits(actif?.operateur ?? 0)?.parametrage === 'GESTION'
}
