import { useState, type FormEvent } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import {
  demanderReinitialisation,
  reinitialiserMotDePasse,
} from '@/api/generated'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PasswordInput } from '@/components/password-input'
import { CartePublique, MessageErreur } from './commun'
import { useEtatSession } from './use-etat-session'

/** Longueur minimale d'un mot de passe (RG-AUT-07) ; le serveur la vérifie aussi. */
export const LONGUEUR_MOT_DE_PASSE = 12

/** « Mot de passe oublié » : un lien valable une heure part par e-mail (RG-AUT-08). */
export function MotDePasseOublie() {
  useEtatSession()
  const [email, setEmail] = useState('')
  const demande = useMutation({
    mutationFn: () =>
      demanderReinitialisation({
        body: { email: email.trim() },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
  })
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    demande.mutate()
  }
  return (
    <CartePublique
      titre='Mot de passe oublié'
      description='Un lien pour choisir un nouveau mot de passe vous sera envoyé ; il est valable une heure.'
    >
      {demande.isSuccess ? (
        <Alert>
          <AlertDescription>
            Un e-mail est parti à {email.trim()}. Ouvrez le lien qu'il contient.
          </AlertDescription>
        </Alert>
      ) : (
        <>
          <MessageErreur erreur={demande.error} />
          <form onSubmit={soumettre} className='space-y-4'>
            <div className='space-y-2'>
              <Label htmlFor='email'>E-mail</Label>
              <Input
                id='email'
                type='email'
                autoFocus
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <Button
              type='submit'
              className='w-full'
              disabled={demande.isPending || !email.trim()}
            >
              Envoyer le lien
            </Button>
          </form>
        </>
      )}
      <RetourConnexion />
    </CartePublique>
  )
}

/** Nouveau mot de passe, depuis le lien reçu (RG-AUT-06 à 08) ; lève le blocage temporaire. */
export function Reinitialisation({ jeton }: { jeton: string }) {
  useEtatSession()
  const [motDePasse, setMotDePasse] = useState('')
  const reinitialisation = useMutation({
    mutationFn: () =>
      reinitialiserMotDePasse({
        body: { jeton, motDePasse },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
  })
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    reinitialisation.mutate()
  }
  return (
    <CartePublique titre='Nouveau mot de passe'>
      {reinitialisation.isSuccess ? (
        <Alert>
          <AlertDescription>
            Votre mot de passe est changé. Connectez-vous avec lui.
          </AlertDescription>
        </Alert>
      ) : (
        <>
          <MessageErreur erreur={reinitialisation.error} />
          <form onSubmit={soumettre} className='space-y-4'>
            <ChampNouveauMotDePasse
              valeur={motDePasse}
              onChange={setMotDePasse}
            />
            <Button
              type='submit'
              className='w-full'
              disabled={
                reinitialisation.isPending ||
                motDePasse.length < LONGUEUR_MOT_DE_PASSE
              }
            >
              Enregistrer
            </Button>
          </form>
        </>
      )}
      <RetourConnexion />
    </CartePublique>
  )
}

/** Saisie d'un nouveau mot de passe, avec sa règle (RG-AUT-07). */
export function ChampNouveauMotDePasse({
  valeur,
  onChange,
  libelle = 'Nouveau mot de passe',
}: {
  valeur: string
  onChange: (valeur: string) => void
  libelle?: string
}) {
  return (
    <div className='space-y-2'>
      <Label htmlFor='nouveau-mot-de-passe'>{libelle}</Label>
      <PasswordInput
        id='nouveau-mot-de-passe'
        autoComplete='new-password'
        value={valeur}
        onChange={(e) => onChange(e.target.value)}
      />
      <p className='text-muted-foreground text-sm'>
        {LONGUEUR_MOT_DE_PASSE} caractères au moins.
      </p>
    </div>
  )
}

export function RetourConnexion() {
  return (
    <Link
      to='/connexion'
      className='text-muted-foreground block text-center text-sm hover:underline'
    >
      Retour à la connexion
    </Link>
  )
}
