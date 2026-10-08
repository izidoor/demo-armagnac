import { useEffect, useRef, useState, type FormEvent } from 'react'
import {
  useMutation,
  useQuery,
  useQueryClient,
  useSuspenseQuery,
} from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import {
  accepterInvitation,
  choisirOperateurActif,
  confirmerChangementEmail,
  consulterInvitationParJeton,
  definirPremierMotDePasse,
} from '@/api/generated'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ROLES, ROLES_DESCRIPTION } from '@/features/operateurs/commun/libelles'
import { requeteMoi } from '@/features/session/session'
import { CartePublique, MessageErreur } from './commun'
import {
  ChampNouveauMotDePasse,
  LONGUEUR_MOT_DE_PASSE,
  RetourConnexion,
} from './mot-de-passe'
import { useEtatSession } from './use-etat-session'

/**
 * Accepter une invitation, depuis le lien de l'e-mail (spec 02 §6.3) : un compte existant se connecte
 * d'abord ; une personne nouvelle confirme nom et prénom, puis pose son mot de passe (RG-OPE-42,
 * RG-AUT-15), et la session s'ouvre.
 */
export function AccepterInvitation({ jeton }: { jeton: string }) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const session = useEtatSession()
  const invitation = useQuery({
    queryKey: ['invitation', jeton],
    queryFn: () =>
      consulterInvitationParJeton({ path: { jeton }, throwOnError: true }).then(
        (r) => r.data
      ),
    retry: false,
  })
  const [nom, setNom] = useState<string | null>(null)
  const [prenom, setPrenom] = useState<string | null>(null)
  const [motDePasse, setMotDePasse] = useState('')

  const entrer = async () => {
    queryClient.removeQueries()
    await navigate({ to: '/', replace: true })
  }
  const acceptation = useMutation({
    mutationFn: async () => {
      const i = invitation.data
      const r = await accepterInvitation({
        path: { jeton },
        body: i?.compteExistant
          ? {}
          : {
              titre: i?.titre,
              nom: (nom ?? i?.nom ?? '').trim(),
              prenom: (prenom ?? i?.prenom ?? '').trim(),
            },
        throwOnError: true,
      })
      if (r.data.nouveauCompte) {
        await definirPremierMotDePasse({
          body: { jeton, motDePasse },
          throwOnError: true,
        })
      }
    },
    meta: { erreurAffichee: true },
    onSuccess: entrer,
  })

  if (invitation.isError) {
    return (
      <CartePublique titre='Invitation'>
        <MessageErreur erreur={invitation.error} />
        <RetourConnexion />
      </CartePublique>
    )
  }
  const i = invitation.data
  if (!i) return <CartePublique titre='Invitation'>Chargement…</CartePublique>

  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    acceptation.mutate()
  }
  const connecte = session.data?.connecte === true
  return (
    <CartePublique
      titre={`Invitation : ${i.operateur}`}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          Rôle proposé <Badge variant='secondary'>{ROLES[i.role]}</Badge>
          {ROLES_DESCRIPTION[i.role]}
        </span>
      }
    >
      <MessageErreur erreur={acceptation.error} />
      {i.compteExistant ? (
        connecte ? (
          <Button
            className='w-full'
            disabled={acceptation.isPending}
            onClick={() => acceptation.mutate()}
          >
            Accepter l'invitation
          </Button>
        ) : (
          <>
            <Alert>
              <AlertDescription>
                Un compte existe pour {i.email} : connectez-vous pour accepter.
              </AlertDescription>
            </Alert>
            <Button asChild className='w-full'>
              <Link
                to='/connexion'
                search={{
                  redirect: `/invitation?jeton=${encodeURIComponent(jeton)}`,
                }}
              >
                Se connecter
              </Link>
            </Button>
          </>
        )
      ) : (
        <form onSubmit={soumettre} className='space-y-4'>
          <p className='text-muted-foreground text-sm'>
            Votre identifiant de connexion sera {i.email}.
          </p>
          <div className='grid gap-4 sm:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='prenom'>Prénom</Label>
              <Input
                id='prenom'
                value={prenom ?? i.prenom ?? ''}
                onChange={(e) => setPrenom(e.target.value)}
              />
            </div>
            <div className='space-y-2'>
              <Label htmlFor='nom'>Nom</Label>
              <Input
                id='nom'
                value={nom ?? i.nom}
                onChange={(e) => setNom(e.target.value)}
              />
            </div>
          </div>
          <ChampNouveauMotDePasse
            libelle='Mot de passe'
            valeur={motDePasse}
            onChange={setMotDePasse}
          />
          <Button
            type='submit'
            className='w-full'
            disabled={
              acceptation.isPending || motDePasse.length < LONGUEUR_MOT_DE_PASSE
            }
          >
            Accepter et créer mon accès
          </Button>
        </form>
      )}
    </CartePublique>
  )
}

/** Confirmer un nouvel e-mail, depuis le lien reçu (RG-OPE-48) : il devient l'identifiant de connexion. */
export function ConfirmerEmail({ jeton }: { jeton: string }) {
  const session = useEtatSession()
  const confirmation = useMutation({
    mutationFn: () =>
      confirmerChangementEmail({ body: { jeton }, throwOnError: true }),
    meta: { erreurAffichee: true },
  })
  const { mutate: confirmer } = confirmation
  const lance = useRef(false)
  useEffect(() => {
    // Le jeton CSRF est posé par l'appel de session : confirmer une fois qu'il a répondu.
    if (session.isSuccess && !lance.current) {
      lance.current = true
      confirmer()
    }
  }, [session.isSuccess, confirmer])
  return (
    <CartePublique titre='Changement d’e-mail'>
      <MessageErreur erreur={confirmation.error} />
      {confirmation.isSuccess && (
        <Alert>
          <AlertDescription>
            Votre nouvel e-mail est confirmé : c'est désormais votre identifiant
            de connexion.
          </AlertDescription>
        </Alert>
      )}
      {confirmation.isPending && <p>Confirmation…</p>}
      <Link
        to='/'
        className='text-muted-foreground block text-center text-sm hover:underline'
      >
        Ouvrir l'application
      </Link>
    </CartePublique>
  )
}

/** Choix de l'opérateur actif parmi les rattachements utilisables (RG-OPE-49). */
export function ChoixOperateur() {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const { data: moi } = useSuspenseQuery(requeteMoi)
  const choix = useMutation({
    mutationFn: (operateur: number) =>
      choisirOperateurActif({ body: { operateur }, throwOnError: true }),
    meta: { erreurAffichee: true },
    onSuccess: async (r) => {
      queryClient.setQueryData(requeteMoi.queryKey, r.data)
      await navigate({ to: '/', replace: true })
    },
  })
  const utilisables = moi.rattachements.filter((r) => r.utilisable)
  return (
    <CartePublique
      titre={`Bonjour ${moi.prenom}`}
      description='Choisissez l’opérateur pour lequel vous travaillez.'
    >
      <MessageErreur erreur={choix.error} />
      {utilisables.length === 0 && (
        <Alert>
          <AlertDescription>
            Vous n'avez plus d'accès actif : contactez l'opérateur ou le BNIA.
          </AlertDescription>
        </Alert>
      )}
      <div className='space-y-2'>
        {utilisables.map((r) => (
          <Button
            key={r.id}
            variant='outline'
            className='h-auto w-full justify-between py-3'
            disabled={choix.isPending}
            onClick={() => choix.mutate(r.operateur)}
          >
            <span className='text-start'>
              <span className='block font-medium'>{r.raisonSociale}</span>
              <span className='text-muted-foreground block text-xs'>
                {r.type === 'BNIA' ? 'Back-office' : `Portail · ${r.code}`}
              </span>
            </span>
            <Badge variant='secondary'>{ROLES[r.role]}</Badge>
          </Button>
        ))}
      </div>
    </CartePublique>
  )
}
