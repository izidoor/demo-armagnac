import { useState, type ReactNode } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft } from 'lucide-react'
import { toast } from 'sonner'
import {
  annulerInvitation,
  changerRoleMembre,
  consulterContact,
  consulterInvitation,
  consulterJournalContact,
  consulterJournalMembre,
  consulterMembre,
  modifierContact,
  modifierFiche,
  reactiverMembre,
  relancerInvitation,
  retirerMembre,
  supprimerContact,
  suspendreMembre,
  type Contact,
  type Droits,
  type Invitation,
  type Membre,
} from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits, useMoi } from '@/features/session/session'
import { cleOperateur, type Espace } from '../commun/api'
import { Lecture } from '../commun/champs'
import {
  cles,
  ROLES,
  ROLES_DESCRIPTION,
  STATUTS_INVITATION,
  type Role,
} from '../commun/libelles'
import { DialogueInvitation } from './dialogue-invitation'
import { FormulaireFiche, FormulaireIdentiteContact } from './formulaires'
import { TableJournal } from './journal'
import { type GenrePersonne, useNavigationPersonnes } from './navigation'

const dateHeure = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

/** Fiche d'une personne de l'annuaire (spec 02 §6.3), commune aux deux espaces. */
export function PagePersonne({
  espace,
  operateur,
  genre,
  id,
}: {
  espace: Espace
  operateur: number
  genre: GenrePersonne
  id: number
}) {
  const droits = useDroits(operateur)
  if (!droits) return <Chargement />
  const props = { espace, operateur, id, droits }
  if (genre === 'contact') return <PageContact {...props} />
  if (genre === 'membre') return <PageMembre {...props} />
  return <PageInvitation {...props} />
}

type PropsPersonne = {
  espace: Espace
  operateur: number
  id: number
  droits: Droits
}

function Chargement() {
  return (
    <PageMetier titre='Personne' description='Chargement…'>
      {null}
    </PageMetier>
  )
}

function Retour({ espace, operateur }: { espace: Espace; operateur: number }) {
  const navigation = useNavigationPersonnes(espace, operateur)
  return (
    <Button
      variant='link'
      className='h-auto p-0'
      onClick={() => navigation.annuaire()}
    >
      <ArrowLeft className='size-4' /> Annuaire
    </Button>
  )
}

function Section({ titre, children }: { titre: string; children: ReactNode }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{titre}</CardTitle>
      </CardHeader>
      <CardContent>{children}</CardContent>
    </Card>
  )
}

function useRafraichir(operateur: number) {
  const queryClient = useQueryClient()
  return () =>
    queryClient.invalidateQueries({ queryKey: cleOperateur(operateur) })
}

/** Un contact : identité, fiche, invitation ; suppression avec ses effets (RG-OPE-45). */
function PageContact({ espace, operateur, id, droits }: PropsPersonne) {
  const rafraichir = useRafraichir(operateur)
  const navigation = useNavigationPersonnes(espace, operateur)
  const cle = [...cleOperateur(operateur), 'contact', id]
  const contact = useQuery({
    queryKey: cle,
    queryFn: () =>
      consulterContact({
        path: { operateur, contact: id },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const journal = useQuery({
    queryKey: [...cleOperateur(operateur), 'contact', id, 'journal'],
    queryFn: () =>
      consulterJournalContact({
        path: { operateur, contact: id },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const [suppression, setSuppression] = useState(false)
  const [invitation, setInvitation] = useState(false)
  const supprimer = useMutation({
    mutationFn: () =>
      supprimerContact({
        path: { operateur, contact: id },
        throwOnError: true,
      }),
    onSuccess: () => {
      toast.success('Contact supprimé.')
      rafraichir()
      navigation.annuaire()
    },
    onSettled: () => setSuppression(false),
  })

  const c = contact.data
  if (!c) return <Chargement />
  const gestion = droits.contacts === 'GESTION'
  const effets = effetsSuppression(c)
  return (
    <PageMetier
      titre={c.nomComplet}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Retour espace={espace} operateur={operateur} />
          <Badge variant='outline'>Contact</Badge>
          {c.contactPrincipal && <Badge variant='secondary'>Principal</Badge>}
          {c.destinataireFactures && (
            <Badge variant='secondary'>Destinataire des factures</Badge>
          )}
          {c.invitationEnAttente && (
            <Badge variant='secondary'>
              Invitation envoyée ({ROLES[c.invitationEnAttente.role]}), expire
              le {dateHeure.format(new Date(c.invitationEnAttente.expireLe))}
            </Badge>
          )}
        </span>
      }
      actions={
        <div className='flex gap-2'>
          {droits.acces === 'GESTION' &&
            (c.invitationEnAttente ? (
              <ActionsInvitation
                operateur={operateur}
                invitation={c.invitationEnAttente.id}
              />
            ) : (
              <Button onClick={() => setInvitation(true)}>Inviter</Button>
            ))}
          {gestion && (
            <Button variant='destructive' onClick={() => setSuppression(true)}>
              Supprimer
            </Button>
          )}
        </div>
      }
    >
      <div className='space-y-4'>
        <Section titre='Identité'>
          <FormulaireIdentiteContact
            contact={c}
            modifiable={gestion}
            enregistrer={(body) =>
              modifierContact({
                path: { operateur, contact: id },
                body,
                throwOnError: true,
              })
            }
            onSucces={() => {
              toast.success('Enregistré.')
              rafraichir()
            }}
          />
        </Section>
        <Section titre='Fiche professionnelle'>
          <FormulaireFiche
            fiche={c.fiche}
            modifiable={gestion}
            enregistrer={(body) =>
              modifierFiche({
                path: { operateur, fiche: c.fiche.id },
                body,
                throwOnError: true,
              })
            }
            onSucces={() => {
              toast.success('Enregistré.')
              rafraichir()
            }}
          />
        </Section>
        <Section titre='Journal'>
          <TableJournal lignes={journal.data ?? []} />
        </Section>
      </div>
      {invitation && (
        <DialogueInvitation
          operateur={operateur}
          contact={c}
          open={invitation}
          onOpenChange={setInvitation}
          onInvite={rafraichir}
        />
      )}
      <ConfirmDialog
        open={suppression}
        onOpenChange={setSuppression}
        title='Supprimer le contact'
        desc={`${c.nomComplet} et sa fiche professionnelle seront supprimés.${effets}`}
        confirmText='Supprimer'
        cancelBtnText='Annuler'
        destructive
        isLoading={supprimer.isPending}
        handleConfirm={() => supprimer.mutate()}
      />
    </PageMetier>
  )
}

/** RG-OPE-22, 23, 45 : ce que la suppression entraîne, dit avant de confirmer. */
function effetsSuppression(c: Contact) {
  const effets = [
    c.contactPrincipal && "l'opérateur n'aura plus de contact principal",
    c.destinataireFactures &&
      "l'opérateur n'aura plus de destinataire des factures",
    c.invitationEnAttente && "l'invitation en attente sera annulée",
  ].filter(Boolean)
  return effets.length ? ` Par conséquent, ${effets.join(', ')}.` : ''
}

type ActionMembre = 'suspendre' | 'reactiver' | 'retirer'

/** Un membre : identité (lue chez l'utilisateur), rôle, statut, fiche ; actions du module Accès. */
function PageMembre({ espace, operateur, id, droits }: PropsPersonne) {
  const rafraichir = useRafraichir(operateur)
  const navigation = useNavigationPersonnes(espace, operateur)
  const { moi } = useMoi()
  const cle = [...cleOperateur(operateur), 'membre', id]
  const membre = useQuery({
    queryKey: cle,
    queryFn: () =>
      consulterMembre({
        path: { operateur, membre: id },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const journal = useQuery({
    queryKey: [...cleOperateur(operateur), 'membre', id, 'journal'],
    queryFn: () =>
      consulterJournalMembre({
        path: { operateur, membre: id },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const [action, setAction] = useState<ActionMembre | null>(null)
  const [conserver, setConserver] = useState(true)
  const role = useMutation({
    mutationFn: (r: Role) =>
      changerRoleMembre({
        path: { operateur, membre: id },
        body: { role: r },
        throwOnError: true,
      }),
    onSuccess: () => {
      toast.success('Rôle changé.')
      rafraichir()
    },
  })
  const executer = useMutation({
    mutationFn: async (a: ActionMembre) => {
      const path = { operateur, membre: id }
      if (a === 'suspendre') await suspendreMembre({ path, throwOnError: true })
      else if (a === 'reactiver')
        await reactiverMembre({ path, throwOnError: true })
      else
        await retirerMembre({
          path,
          body: { conserverContact: conserver },
          throwOnError: true,
        })
    },
    onSuccess: (_r, a) => {
      rafraichir()
      if (a === 'retirer') {
        toast.success('Accès retiré.')
        navigation.annuaire()
      }
    },
    onSettled: () => setAction(null),
  })

  const m = membre.data
  if (!m) return <Chargement />
  const acces = droits.acces === 'GESTION'
  const sienne = m.utilisateur === moi.id
  return (
    <PageMetier
      titre={m.nomComplet}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Retour espace={espace} operateur={operateur} />
          <Badge>{ROLES[m.role]}</Badge>
          {m.statut === 'SUSPENDU' && (
            <Badge variant='secondary'>Accès suspendu</Badge>
          )}
          {m.bloqueParBnia && (
            <Badge variant='destructive'>Bloqué par le BNIA</Badge>
          )}
          {m.dernierAdmin && <Badge variant='outline'>Dernier ADMIN</Badge>}
          {m.contactPrincipal && <Badge variant='secondary'>Principal</Badge>}
          {m.destinataireFactures && (
            <Badge variant='secondary'>Destinataire des factures</Badge>
          )}
        </span>
      }
      actions={
        acces && (
          <div className='flex gap-2'>
            <Button
              variant='outline'
              onClick={() =>
                setAction(m.statut === 'ACTIF' ? 'suspendre' : 'reactiver')
              }
            >
              {m.statut === 'ACTIF' ? "Suspendre l'accès" : "Réactiver l'accès"}
            </Button>
            <Button variant='destructive' onClick={() => setAction('retirer')}>
              Retirer l'accès
            </Button>
          </div>
        )
      }
    >
      <div className='space-y-4'>
        <Section titre='Identité et accès'>
          <div className='grid gap-4 md:grid-cols-4'>
            <Lecture label='Nom' valeur={m.nomComplet} />
            <Lecture label='E-mail' valeur={m.email} />
            <Lecture
              label='Membre depuis'
              valeur={dateHeure.format(new Date(m.depuis))}
            />
            <div className='space-y-2'>
              <span className='text-sm font-medium'>Rôle</span>
              <Select
                value={m.role}
                disabled={!acces || role.isPending}
                onValueChange={(r) => role.mutate(r as Role)}
              >
                <SelectTrigger className='w-full'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {cles(ROLES).map((r) => (
                    <SelectItem key={r} value={r}>
                      {ROLES[r]}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <p className='text-muted-foreground text-xs'>
                {ROLES_DESCRIPTION[m.role]}
              </p>
            </div>
          </div>
          <p className='text-muted-foreground mt-2 text-sm'>
            Le nom et l'e-mail sont ceux de son compte : seule la personne les
            modifie, dans « Mon compte ».
          </p>
        </Section>
        <Section titre='Fiche professionnelle'>
          <FormulaireFiche
            fiche={m.fiche}
            modifiable={droits.contacts === 'GESTION' || sienne}
            enregistrer={(body) =>
              modifierFiche({
                path: { operateur, fiche: m.fiche.id },
                body,
                throwOnError: true,
              })
            }
            onSucces={() => {
              toast.success('Enregistré.')
              rafraichir()
            }}
          />
        </Section>
        <Section titre='Journal'>
          <TableJournal lignes={journal.data ?? []} />
        </Section>
      </div>
      <ConfirmDialog
        open={action !== null}
        onOpenChange={(o) => !o && setAction(null)}
        title={
          action === 'retirer'
            ? "Retirer l'accès"
            : action === 'suspendre'
              ? "Suspendre l'accès"
              : "Réactiver l'accès"
        }
        desc={descriptionAction(action, m)}
        confirmText='Confirmer'
        cancelBtnText='Annuler'
        destructive={action !== 'reactiver'}
        isLoading={executer.isPending}
        handleConfirm={() => action && executer.mutate(action)}
      >
        {action === 'retirer' && (
          <label className='flex items-center gap-2 text-sm'>
            <Checkbox
              checked={conserver}
              onCheckedChange={(v) => setConserver(v === true)}
            />
            Conserver cette personne dans les contacts
          </label>
        )}
      </ConfirmDialog>
    </PageMetier>
  )
}

/** RG-OPE-35, 43, 44 : les effets d'une action sur un membre, dits avant de confirmer. */
function descriptionAction(action: ActionMembre | null, m: Membre) {
  if (action === 'suspendre')
    return `${m.nomComplet} n'aura plus accès à cet opérateur, jusqu'à sa réactivation. Ses autres opérateurs ne changent pas.`
  if (action === 'reactiver')
    return `${m.nomComplet} retrouvera son accès, avec le rôle ${ROLES[m.role]}.`
  const designations = [
    m.contactPrincipal && 'contact principal',
    m.destinataireFactures && 'destinataire des factures',
  ].filter(Boolean)
  return `${m.nomComplet} ne sera plus membre de cet opérateur. Sans la case cochée, sa fiche professionnelle est supprimée${designations.length ? ` et ses désignations (${designations.join(', ')}) tombent` : ''}. Si c'était son dernier opérateur, son compte est supprimé.`
}

/** Une invitation : état, relance, annulation (RG-OPE-41). */
function PageInvitation({ espace, operateur, id, droits }: PropsPersonne) {
  const queryClient = useQueryClient()
  const cle = [...cleOperateur(operateur), 'invitation', id]
  const invitation = useQuery({
    queryKey: cle,
    queryFn: () =>
      consulterInvitation({
        path: { operateur, invitation: id },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const i = invitation.data
  if (!i) return <Chargement />
  const nom = i.nom ? `${i.prenom ?? ''} ${i.nom}`.trim() : i.email
  const enAttente = i.statut === 'EN_ATTENTE'
  return (
    <PageMetier
      titre={`Invitation : ${nom}`}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Retour espace={espace} operateur={operateur} />
          <Badge variant='secondary'>{STATUTS_INVITATION[i.statut]}</Badge>
        </span>
      }
      actions={
        droits.acces === 'GESTION' &&
        enAttente && (
          <ActionsInvitation
            operateur={operateur}
            invitation={id}
            onSucces={(i) => queryClient.setQueryData(cle, i)}
          />
        )
      }
    >
      <Section titre='Invitation'>
        <div className='grid gap-4 md:grid-cols-3'>
          <Lecture label='E-mail' valeur={i.email} />
          <Lecture label='Rôle proposé' valeur={ROLES[i.role]} />
          <Lecture
            label='Expire le'
            valeur={dateHeure.format(new Date(i.expireLe))}
          />
          <Lecture label='Fonction' valeur={i.fonction} />
          <Lecture
            label='Téléphone professionnel'
            valeur={i.telephoneProfessionnel}
          />
          <Lecture
            label='Envoyée'
            valeur={`${dateHeure.format(new Date(i.envoyeeLe))}, par ${i.envoyeePar}`}
          />
        </div>
      </Section>
    </PageMetier>
  )
}

/**
 * RG-OPE-41 : relancer (nouveau lien, nouvelle expiration, nouvel e-mail) ou annuler une invitation en
 * attente ; le contact dont elle part reste inchangé. Depuis la page de l'invitation ou la fiche du contact.
 */
function ActionsInvitation({
  operateur,
  invitation,
  onSucces,
}: {
  operateur: number
  invitation: number
  onSucces?: (invitation: Invitation) => void
}) {
  const rafraichir = useRafraichir(operateur)
  const action = useMutation({
    mutationFn: (a: 'relancer' | 'annuler') =>
      (a === 'relancer'
        ? relancerInvitation({
            path: { operateur, invitation },
            throwOnError: true,
          })
        : annulerInvitation({
            path: { operateur, invitation },
            throwOnError: true,
          })
      ).then((r) => r.data),
    onSuccess: (i: Invitation, a) => {
      onSucces?.(i)
      toast.success(
        a === 'relancer' ? 'Invitation relancée.' : 'Invitation annulée.'
      )
      rafraichir()
    },
  })
  return (
    <div className='flex gap-2'>
      <Button
        variant='outline'
        disabled={action.isPending}
        onClick={() => action.mutate('relancer')}
      >
        Relancer l'invitation
      </Button>
      <Button
        variant='destructive'
        disabled={action.isPending}
        onClick={() => action.mutate('annuler')}
      >
        Annuler l'invitation
      </Button>
    </div>
  )
}
