import { useState, type FormEvent } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'
import { renvoyerCode, seConnecter, verifierCode } from '@/api/generated'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PasswordInput } from '@/components/password-input'
import { CartePublique, MessageErreur } from './commun'
import { useEtatSession } from './use-etat-session'

/**
 * Connexion unique de tous les utilisateurs (RG-AUT-01) : e-mail et mot de passe, puis le code reçu par
 * e-mail, sauf sur un navigateur de confiance (RG-AUT-06). L'opérateur actif décide ensuite de l'espace
 * (RG-OPE-49).
 */
export function Connexion({ redirect }: { redirect?: string }) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const session = useEtatSession()
  const [email, setEmail] = useState('')
  const [motDePasse, setMotDePasse] = useState('')
  const [code, setCode] = useState('')
  const [confiance, setConfiance] = useState(false)
  const [codeDemande, setCodeDemande] = useState(false)
  const etape =
    codeDemande || session.data?.codeAttendu ? 'code' : 'identifiants'

  const entrer = async () => {
    queryClient.removeQueries()
    await navigate({ to: redirect ?? '/', replace: true })
  }

  const identifiants = useMutation({
    mutationFn: () =>
      seConnecter({
        body: { email: email.trim(), motDePasse },
        throwOnError: true,
      }).then((r) => r.data),
    meta: { erreurAffichee: true },
    onSuccess: (r) =>
      r.etape === 'CONNECTE' ? entrer() : setCodeDemande(true),
  })
  const verification = useMutation({
    mutationFn: () =>
      verifierCode({
        body: { code: code.trim(), faireConfiance: confiance },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: entrer,
  })
  const renvoi = useMutation({
    mutationFn: () => renvoyerCode({ throwOnError: true }),
    meta: { erreurAffichee: true },
    onSuccess: () => toast.success('Un nouveau code vous a été envoyé.'),
  })

  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    if (etape === 'identifiants') identifiants.mutate()
    else verification.mutate()
  }

  if (etape === 'code') {
    return (
      <CartePublique
        titre='Code de vérification'
        description='Saisissez le code à six chiffres reçu par e-mail. Il est valable dix minutes.'
      >
        <MessageErreur erreur={verification.error ?? renvoi.error} />
        <form onSubmit={soumettre} className='space-y-4'>
          <div className='space-y-2'>
            <Label htmlFor='code'>Code</Label>
            <Input
              id='code'
              inputMode='numeric'
              autoComplete='one-time-code'
              autoFocus
              value={code}
              onChange={(e) => setCode(e.target.value)}
            />
          </div>
          <label className='flex items-center gap-2 text-sm'>
            <Checkbox
              checked={confiance}
              onCheckedChange={(v) => setConfiance(v === true)}
            />
            Faire confiance à ce navigateur pendant 30 jours
          </label>
          <Button
            type='submit'
            className='w-full'
            disabled={verification.isPending}
          >
            Valider
          </Button>
          <Button
            type='button'
            variant='link'
            className='w-full'
            disabled={renvoi.isPending}
            onClick={() => renvoi.mutate()}
          >
            Renvoyer le code
          </Button>
        </form>
      </CartePublique>
    )
  }

  return (
    <CartePublique
      titre='Connexion'
      description='Une seule connexion pour tous : personnel du BNIA et membres des opérateurs.'
    >
      <MessageErreur erreur={identifiants.error} />
      <form onSubmit={soumettre} className='space-y-4'>
        <div className='space-y-2'>
          <Label htmlFor='email'>E-mail</Label>
          <Input
            id='email'
            type='email'
            autoComplete='username'
            autoFocus
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <div className='space-y-2'>
          <div className='flex items-center justify-between'>
            <Label htmlFor='mot-de-passe'>Mot de passe</Label>
            <Link
              to='/mot-de-passe-oublie'
              className='text-muted-foreground text-sm hover:underline'
            >
              Mot de passe oublié ?
            </Link>
          </div>
          <PasswordInput
            id='mot-de-passe'
            autoComplete='current-password'
            value={motDePasse}
            onChange={(e) => setMotDePasse(e.target.value)}
          />
        </div>
        <Button
          type='submit'
          className='w-full'
          disabled={identifiants.isPending || !email.trim() || !motDePasse}
        >
          Se connecter
        </Button>
      </form>
    </CartePublique>
  )
}
