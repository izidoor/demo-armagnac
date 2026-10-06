import {
  type FieldValues,
  type Path,
  type UseFormReturn,
} from 'react-hook-form'
import { lireProbleme } from '@/lib/probleme'

/**
 * Affiche une erreur de l'API dans un formulaire : sur le champ que vise la règle enfreinte s'il est
 * connu, sinon en tête du formulaire.
 */
export function afficherErreur<T extends FieldValues>(
  form: UseFormReturn<T>,
  error: unknown,
  champsParRegle: Partial<Record<string, Path<T>>> = {}
) {
  const probleme = lireProbleme(error)
  const message = probleme.regle
    ? `${probleme.detail} (${probleme.regle})`
    : (probleme.detail ?? probleme.titre)
  const champ = probleme.regle ? champsParRegle[probleme.regle] : undefined
  if (champ) {
    form.setError(champ, { message })
  } else {
    form.setError('root', { message })
  }
}
