import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bell, BellOff, Send } from 'lucide-react'
import { toast } from 'sonner'
import { essayerNotifications } from '@/api/generated'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  activer,
  desactiver,
  type EtatPush,
  lireClePublique,
  lireEtat,
} from './push'

const CLE_ETAT = ['notifications', 'etat-appareil']

/** Android range les notifications d'une application web dans le volet, sans bannière : réglage du téléphone. */
const ANDROID = /Android/.test(navigator.userAgent)

/**
 * « Notifications sur cet appareil », dans Mon compte (spec 11 §6) : chacun active le push sur chacun de ses appareils,
 * sans module ni rôle (RG-OPE-33). Les e-mails partent dans tous les cas (RG-CA-18).
 */
export function NotificationsAppareil() {
  const queryClient = useQueryClient()
  const cle = useQuery({
    queryKey: ['notifications', 'cle-publique'],
    queryFn: lireClePublique,
    staleTime: Infinity,
  })
  const etat = useQuery({
    queryKey: [...CLE_ETAT, cle.data],
    queryFn: () => lireEtat(cle.data ?? null),
    enabled: cle.isSuccess,
  })
  const retenir = (nouvel: EtatPush) =>
    queryClient.setQueryData([...CLE_ETAT, cle.data], nouvel)

  const activation = useMutation({
    mutationFn: () => activer(cle.data ?? ''),
    onSuccess: (nouvel) => {
      retenir(nouvel)
      if (nouvel === 'activees')
        toast.success(
          'Notifications activées sur cet appareil. Envoyez-vous un essai pour vérifier.'
        )
    },
  })
  const desactivation = useMutation({
    mutationFn: desactiver,
    onSuccess: (nouvel) => {
      retenir(nouvel)
      toast.success('Notifications désactivées sur cet appareil.')
    },
  })
  const essai = useMutation({
    mutationFn: () => essayerNotifications({ throwOnError: true }),
    onSuccess: () =>
      toast.success(
        "Notification d'essai envoyée : elle arrive dans quelques secondes sur vos appareils abonnés."
      ),
  })

  const occupe =
    activation.isPending || desactivation.isPending || essai.isPending

  return (
    <div className='max-w-xl space-y-4'>
      <p className='text-muted-foreground text-sm'>
        Un contrat ou une retiraison qui vous attend, et ce que fait l'autre
        partie, s'affichent aussitôt sur ce téléphone, cette tablette ou cet
        ordinateur, même l'application fermée. Les e-mails continuent de partir.
      </p>
      {etat.data === undefined ? null : etat.data === 'indisponible' ? (
        <p className='text-sm'>
          {cle.data
            ? 'Ce navigateur ne reçoit pas de notifications.'
            : "Cette installation n'envoie pas de notifications."}
        </p>
      ) : etat.data === 'a-installer' ? (
        <Alert>
          <AlertDescription>
            Sur iPhone et iPad, les notifications ne fonctionnent que depuis
            l'application installée : dans Safari, touchez Partager, puis « Sur
            l'écran d'accueil », et ouvrez BNIA depuis son icône.
          </AlertDescription>
        </Alert>
      ) : etat.data === 'refusees' ? (
        <Alert>
          <AlertDescription>
            Les notifications de BNIA sont bloquées sur cet appareil. Pour les
            rétablir : sur iPhone, Réglages, Notifications, BNIA ; sur Android,
            appui long sur l'icône BNIA, Infos sur l'appli, Notifications ; sur
            ordinateur, le cadenas à gauche de l'adresse.
          </AlertDescription>
        </Alert>
      ) : etat.data === 'activees' ? (
        <div className='space-y-3'>
          <Badge variant='secondary'>
            <Bell /> Activées sur cet appareil
          </Badge>
          {ANDROID && (
            <p className='text-muted-foreground text-sm'>
              Pour qu'elles s'affichent en bannière, avec un son : Paramètres,
              Applications, BNIA, Notifications, « Afficher en pop-up » (ou
              appui long sur une notification de BNIA, puis « Paramètres »).
            </p>
          )}
          <div className='flex flex-wrap gap-2'>
            <Button disabled={occupe} onClick={() => essai.mutate()}>
              <Send /> Envoyer une notification d'essai
            </Button>
            <Button
              variant='outline'
              disabled={occupe}
              onClick={() => desactivation.mutate()}
            >
              <BellOff /> Désactiver
            </Button>
          </div>
        </div>
      ) : (
        <Button disabled={occupe} onClick={() => activation.mutate()}>
          <Bell /> Activer les notifications
        </Button>
      )}
    </div>
  )
}
