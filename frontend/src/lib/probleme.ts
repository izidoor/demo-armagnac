import { AxiosError } from 'axios'

/** Erreur renvoyée par l'API au format ProblemDetail (RFC 9457), voir GestionErreurs côté back. */
type Probleme = {
  statut?: number
  titre: string
  detail?: string
  /** Identifiant de la règle de gestion enfreinte, par exemple RG-REF-11. */
  regle?: string
}

type ProblemDetail = {
  title?: string
  detail?: string
  regle?: string
}

export function lireProbleme(error: unknown): Probleme {
  if (error instanceof AxiosError) {
    const corps = error.response?.data as ProblemDetail | undefined
    return {
      statut: error.response?.status,
      titre: corps?.title ?? 'Erreur',
      detail: corps?.detail ?? error.message,
      regle: corps?.regle,
    }
  }
  return { titre: 'Erreur', detail: String(error) }
}
