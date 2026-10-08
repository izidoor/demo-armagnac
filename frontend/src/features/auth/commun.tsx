import { type ReactNode } from 'react'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { AuthLayout } from './auth-layout'

/** Le message d'une erreur de l'API, tel quel : les messages de connexion sont explicites (RG-AUT-02). */
export function MessageErreur({ erreur }: { erreur: unknown }) {
  if (!erreur) return null
  const p = lireProbleme(erreur)
  return (
    <Alert variant='destructive'>
      <AlertDescription>{p.detail ?? p.titre}</AlertDescription>
    </Alert>
  )
}

/** Gabarit des écrans publics : une carte, un titre, une description. */
export function CartePublique({
  titre,
  description,
  panneau,
  children,
}: {
  titre: string
  description?: ReactNode
  /** Panneau sur la photo, en seconde colonne (écran de connexion). */
  panneau?: ReactNode
  children: ReactNode
}) {
  return (
    <AuthLayout panneau={panneau}>
      <Card className='gap-4'>
        <CardHeader>
          <CardTitle className='text-lg tracking-tight'>{titre}</CardTitle>
          {description && <CardDescription>{description}</CardDescription>}
        </CardHeader>
        <CardContent className='space-y-4'>{children}</CardContent>
      </Card>
    </AuthLayout>
  )
}
