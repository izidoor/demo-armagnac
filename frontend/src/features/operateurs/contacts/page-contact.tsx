import { useState } from 'react'
import { format, parseISO } from 'date-fns'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { fr } from 'date-fns/locale'
import { ArrowLeft } from 'lucide-react'
import { toast } from 'sonner'
import { type Contact } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Checkbox } from '@/components/ui/checkbox'
import { Switch } from '@/components/ui/switch'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { BadgeEtat } from '@/components/metier/badge-etat'
import { PageMetier } from '@/components/metier/page-metier'
import { apiOperateur, cleOperateur, type Espace } from '../commun/api'
import {
  ACTIONS_JOURNAL,
  MODULES,
  cles,
  type ModulePortail,
} from '../commun/libelles'
import { FormulaireContact } from './formulaire-contact'

type Action = 'desactiver' | 'reactiver' | 'supprimer'

/**
 * Fiche d'un contact (spec 02, §6.1 et §6.2) : identité, bloc « Portail » (accès, administrateur,
 * modules), journal (RG-OPE-34), et actions dont les effets sont annoncés avant confirmation
 * (RG-OPE-22, 23, 29, 33).
 */
export function PageContact({
  espace,
  operateur,
  contact: idContact,
}: {
  espace: Espace
  operateur: number
  contact: number
}) {
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const api = apiOperateur(espace, operateur)
  const cle = ['contact', operateur, idContact]
  const requete = useQuery({
    queryKey: cle,
    queryFn: () => api.contact(idContact),
  })
  const journal = useQuery({
    queryKey: ['journal', operateur, idContact],
    queryFn: () => api.journal(idContact),
  })
  const [action, setAction] = useState<Action | null>(null)

  const rafraichir = () => {
    queryClient.invalidateQueries({ queryKey: cle })
    queryClient.invalidateQueries({
      queryKey: ['journal', operateur, idContact],
    })
    queryClient.invalidateQueries({ queryKey: cleOperateur(operateur) })
  }
  const retour = () =>
    espace === 'bo'
      ? navigate({
          to: '/bo/operateurs/$id',
          params: { id: String(operateur) },
        })
      : navigate({ to: '/portail/contacts' })

  const mutation = useMutation({
    mutationFn: async (a: Action) => {
      if (a === 'desactiver') await api.desactiverContact(idContact)
      else if (a === 'reactiver') await api.reactiverContact(idContact)
      else await api.supprimerContact(idContact)
    },
    onSuccess: (_r, a) => {
      setAction(null)
      if (a === 'supprimer') {
        toast.success('Contact supprimé.')
        queryClient.invalidateQueries({ queryKey: cleOperateur(operateur) })
        retour()
      } else {
        rafraichir()
      }
    },
    onError: () => setAction(null),
  })

  const contact = requete.data
  if (!contact) {
    return (
      <PageMetier
        titre='Contact'
        description={requete.isError ? 'Contact introuvable.' : 'Chargement…'}
      >
        {null}
      </PageMetier>
    )
  }

  return (
    <PageMetier
      titre={contact.nomComplet}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Button variant='link' className='h-auto p-0' onClick={retour}>
            <ArrowLeft className='size-4' />{' '}
            {espace === 'bo' ? 'Fiche opérateur' : 'Contacts'}
          </Button>
          <BadgeEtat actif={contact.actif} />
          {contact.administrateur && <Badge>Administrateur</Badge>}
          {contact.contactPrincipal && (
            <Badge variant='outline'>Contact principal</Badge>
          )}
          {contact.destinataireFactures && (
            <Badge variant='outline'>Destinataire des factures</Badge>
          )}
        </span>
      }
      actions={
        <div className='flex gap-2'>
          <Button
            variant='outline'
            onClick={() =>
              setAction(contact.actif ? 'desactiver' : 'reactiver')
            }
          >
            {contact.actif ? 'Désactiver' : 'Réactiver'}
          </Button>
          <Button variant='destructive' onClick={() => setAction('supprimer')}>
            Supprimer
          </Button>
        </div>
      }
    >
      <div className='space-y-4'>
        <Card>
          <CardHeader>
            <CardTitle>Identité et coordonnées</CardTitle>
          </CardHeader>
          <CardContent>
            <FormulaireContact
              contact={contact}
              enregistrer={(saisie) => api.modifierContact(idContact, saisie)}
              onSucces={() => {
                toast.success('Contact enregistré.')
                rafraichir()
              }}
            />
          </CardContent>
        </Card>
        <BlocPortail
          key={`${contact.acces}-${contact.administrateur}-${contact.modules.join()}`}
          espace={espace}
          contact={contact}
          operateur={operateur}
          onEnregistre={rafraichir}
        />
        <Card>
          <CardHeader>
            <CardTitle>Journal</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date</TableHead>
                  <TableHead>Auteur</TableHead>
                  <TableHead>Action</TableHead>
                  <TableHead>Changements</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {journal.data?.map((l, i) => (
                  <TableRow key={i}>
                    <TableCell className='whitespace-nowrap'>
                      {format(parseISO(l.date), 'dd/MM/yyyy HH:mm', {
                        locale: fr,
                      })}
                    </TableCell>
                    <TableCell>{l.auteur}</TableCell>
                    <TableCell>{ACTIONS_JOURNAL[l.action]}</TableCell>
                    <TableCell className='text-sm'>
                      {l.changements.map((c, j) => (
                        <div key={j}>
                          <span className='font-medium'>{c.champ}</span> :{' '}
                          {c.avant ?? '—'} → {c.apres ?? '—'}
                        </div>
                      ))}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
      <ConfirmDialog
        open={action !== null}
        onOpenChange={(o) => !o && setAction(null)}
        title={
          action === 'supprimer'
            ? 'Supprimer le contact'
            : action === 'desactiver'
              ? 'Désactiver le contact'
              : 'Réactiver le contact'
        }
        desc={<Effets action={action} contact={contact} />}
        confirmText={
          action === 'supprimer'
            ? 'Supprimer'
            : action === 'desactiver'
              ? 'Désactiver'
              : 'Réactiver'
        }
        cancelBtnText='Annuler'
        destructive={action !== 'reactiver'}
        isLoading={mutation.isPending}
        handleConfirm={() => action && mutation.mutate(action)}
      />
    </PageMetier>
  )
}

/** Effets annoncés avant confirmation (RG-OPE-22, 23, 29, 33). */
function Effets({
  action,
  contact,
}: {
  action: Action | null
  contact: Contact
}) {
  if (action === 'reactiver') {
    return (
      <span>
        {contact.nomComplet} redeviendra actif, sans accès au portail : l'accès
        se réaccorde ensuite.
      </span>
    )
  }
  const effets = [
    contact.acces && "perdra l'accès au portail pour cet opérateur",
    contact.contactPrincipal && 'ne sera plus le contact principal',
    contact.destinataireFactures && 'ne recevra plus les factures',
  ].filter(Boolean)
  return (
    <span>
      {action === 'supprimer'
        ? `${contact.nomComplet} sera supprimé ; son journal est conservé.`
        : `${contact.nomComplet} sera désactivé.`}
      {effets.length > 0 && <> Il {effets.join(', ')}.</>}
    </span>
  )
}

/** Bloc « Portail » : accès (e-mail requis), administrateur (tous les modules), modules (RG-OPE-29 à 31). */
function BlocPortail({
  espace,
  contact,
  operateur,
  onEnregistre,
}: {
  espace: Espace
  contact: Contact
  operateur: number
  onEnregistre: () => void
}) {
  const [acces, setAcces] = useState(contact.acces)
  const [administrateur, setAdministrateur] = useState(contact.administrateur)
  const [modules, setModules] = useState<ModulePortail[]>(contact.modules)
  const [erreur, setErreur] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn: () =>
      apiOperateur(espace, operateur).modifierDroits(contact.id, {
        acces,
        administrateur: acces && administrateur,
        modules: acces ? modules : [],
      }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setErreur(null)
      toast.success('Droits enregistrés.')
      onEnregistre()
    },
    onError: (error) => {
      const p = lireProbleme(error)
      setErreur(p.regle ? `${p.detail} (${p.regle})` : (p.detail ?? p.titre))
    },
  })

  return (
    <Card>
      <CardHeader>
        <CardTitle>Portail</CardTitle>
      </CardHeader>
      <CardContent className='space-y-4'>
        {erreur && (
          <Alert variant='destructive'>
            <AlertDescription>{erreur}</AlertDescription>
          </Alert>
        )}
        {!contact.email && (
          <p className='text-muted-foreground text-sm'>
            L'accès au portail exige un e-mail.
          </p>
        )}
        <div className='flex flex-wrap gap-6'>
          <label className='flex items-center gap-2 text-sm'>
            <Switch
              checked={acces}
              disabled={!contact.actif || (!contact.email && !acces)}
              onCheckedChange={(v) => {
                setAcces(v)
                if (!v) setAdministrateur(false)
              }}
            />
            Accès au portail
          </label>
          <label className='flex items-center gap-2 text-sm'>
            <Switch
              checked={administrateur}
              disabled={!acces}
              onCheckedChange={setAdministrateur}
            />
            Administrateur
          </label>
        </div>
        {acces && (
          <div className='grid gap-2 sm:grid-cols-2 lg:grid-cols-3'>
            {cles(MODULES).map((m) => (
              <label key={m} className='flex items-center gap-2 text-sm'>
                <Checkbox
                  checked={administrateur || modules.includes(m)}
                  disabled={administrateur}
                  onCheckedChange={(coche) =>
                    setModules(
                      coche ? [...modules, m] : modules.filter((x) => x !== m)
                    )
                  }
                />
                {MODULES[m]}
              </label>
            ))}
          </div>
        )}
        {espace === 'portail' && (
          <p className='text-muted-foreground text-sm'>
            L'opérateur garde toujours au moins un administrateur actif : pour
            retirer le dernier, adressez-vous au BNIA.
          </p>
        )}
        <div className='flex justify-end'>
          <Button
            onClick={() => mutation.mutate()}
            disabled={mutation.isPending}
          >
            Enregistrer les droits
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
