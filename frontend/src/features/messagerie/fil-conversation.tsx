import { Fragment, useEffect, useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import {
  FileWarning,
  Lock,
  MailOpen,
  Paperclip,
  Pencil,
  Send,
  Trash2,
  X,
} from 'lucide-react'
import { toast } from 'sonner'
import { type Conversation, type Message } from '@/api/generated'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { ConfirmDialog } from '@/components/confirm-dialog'
import {
  cleJour,
  formatHeure,
  libelleJour,
} from '@/components/metier/dates-fil'
import {
  type ActionElement,
  AvatarAuteur,
  MenuActions,
  Separateur,
} from '@/components/metier/fil-messages'
import { formatDateHeure } from '@/features/declarations/format'
import {
  apiMessagerie,
  FORMATS_PIECES,
  formatPieceAdmis,
  urlPieceJointe,
} from './api'

/** RG-MSG-05 : longueur maximale d'un message. */
const TEXTE_MAXIMAL = 4000

/** RG-MSG-14 : pièces jointes par message. */
const PIECES_MAXIMALES = 5

/** Deux messages d'un même auteur à moins de cinq minutes d'écart forment un groupe, sous un seul en-tête. */
const ECART_GROUPE_MS = 5 * 60 * 1000

/** Le choix des pièces jointes d'un message (RG-MSG-14) : cinq au plus, PDF, images ou classeur Excel. */
export function ChoixPieces({
  pieces,
  onChange,
  id,
}: {
  pieces: File[]
  onChange: (pieces: File[]) => void
  id: string
}) {
  return (
    <div className='space-y-2'>
      <div className='flex flex-wrap items-center gap-2'>
        <Button
          type='button'
          variant='outline'
          size='sm'
          disabled={pieces.length >= PIECES_MAXIMALES}
          asChild
        >
          <label htmlFor={id} className='cursor-pointer'>
            <Paperclip className='size-4' /> Joindre un fichier
          </label>
        </Button>
        <Input
          id={id}
          type='file'
          multiple
          accept={FORMATS_PIECES}
          className='sr-only'
          onChange={(e) => {
            const choisis = Array.from(e.target.files ?? [])
            const refuses = choisis.filter((f) => !formatPieceAdmis(f))
            if (refuses.length > 0)
              toast.error(
                `${refuses.map((f) => f.name).join(', ')} : format non accepté (PDF, PNG, JPEG ou Excel .xlsx).`
              )
            const admis = choisis.filter(formatPieceAdmis)
            onChange([...pieces, ...admis].slice(0, PIECES_MAXIMALES))
            e.target.value = ''
          }}
        />
        <span className='text-muted-foreground text-xs'>
          PDF, PNG, JPEG ou Excel (.xlsx), 10 Mo chacun, {PIECES_MAXIMALES} au
          plus.
        </span>
      </div>
      {pieces.length > 0 && (
        <ul className='flex flex-wrap gap-2'>
          {pieces.map((f, i) => (
            <li key={`${f.name}-${i}`}>
              <Badge variant='secondary' className='gap-1'>
                {f.name}
                <button
                  type='button'
                  aria-label={`Retirer ${f.name}`}
                  onClick={() => onChange(pieces.filter((_, j) => j !== i))}
                >
                  <X className='size-3' />
                </button>
              </Badge>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

/** La place d'un message dans le fil : ce qui le précède (séparateurs) et s'il ouvre un groupe d'auteur. */
type Position = {
  nouveauJour: boolean
  premierNonLu: boolean
  debutGroupe: boolean
}

function positions(messages: Message[]): Position[] {
  let nonLuVu = false
  return messages.map((m, i) => {
    const prec = messages[i - 1]
    const nouveauJour = !prec || cleJour(prec.ecritLe) !== cleJour(m.ecritLe)
    const premierNonLu = m.nonLu && !nonLuVu
    if (m.nonLu) nonLuVu = true
    const debutGroupe =
      nouveauJour ||
      premierNonLu ||
      prec.cote !== m.cote ||
      prec.auteur !== m.auteur ||
      new Date(m.ecritLe).getTime() - new Date(prec.ecritLe).getTime() >
        ECART_GROUPE_MS
    return { nouveauJour, premierNonLu, debutGroupe }
  })
}

/**
 * Le fil d'une conversation et sa zone de réponse (spec 15 §6) : messages du plus ancien au plus récent, en bulles
 * (mon côté à droite), regroupés par jour puis par auteur ; ceux qui étaient non lus de mon côté viennent
 * après un séparateur « Nouveaux messages » (RG-MSG-10) ; « modifié » ; mention du motif des compléments ; pour mes
 * messages, modifier et supprimer (RG-MSG-06) ; pour ceux de l'autre côté, « Marquer non lu ». Sans droit d'écrire,
 * la zone de réponse est remplacée par son motif.
 *
 * En panneau (messagerie), le fil occupe la hauteur de son parent et défile seul, la zone de réponse restant en bas ;
 * il s'ouvre sur le message visé, sinon sur le premier non lu, sinon sur le dernier.
 */
export function FilConversation({
  conversation,
  ecrire,
  onChange,
  onNonLu,
  invitation,
  panneau = false,
  messageVise,
}: {
  conversation: Conversation
  /** Écrit un message dans la conversation (ou sur l'objet, si elle n'existe pas encore). */
  ecrire: (texte: string, pieces: File[]) => Promise<unknown>
  /** Après une écriture, une modification ou une suppression : la conversation et les listes se relisent. */
  onChange: () => void
  /**
   * Après un « non lu » : seules les listes se relisent ; relire la conversation l'afficherait, donc la lirait
   * (RG-MSG-10).
   */
  onNonLu: () => void
  /** Ce qu'affiche un fil vide. */
  invitation?: string
  /** Le fil défile seul dans la hauteur de son parent (flex), la zone de réponse en bas. */
  panneau?: boolean
  /** Le message amené à l'écran à l'ouverture (depuis les nouveaux messages). */
  messageVise?: number
}) {
  const [texte, setTexte] = useState('')
  const [pieces, setPieces] = useState<File[]>([])
  const [suppression, setSuppression] = useState<Message | null>(null)
  const defilement = useRef<HTMLDivElement>(null)
  const viseMontre = useRef(false)
  // RG-MSG-14 : pièces jointes en conversation libre seulement.
  const libre = conversation.objet == null
  const nombre = conversation.messages.length

  const cible =
    messageVise ??
    (panneau ? conversation.messages.find((m) => m.nonLu)?.id : undefined)

  useEffect(() => {
    if (cible != null && !viseMontre.current) {
      viseMontre.current = true
      document
        .getElementById(`message-${cible}`)
        ?.scrollIntoView({ block: 'center' })
    } else if (panneau && defilement.current) {
      defilement.current.scrollTop = defilement.current.scrollHeight
    }
  }, [nombre, cible, panneau])

  const ecriture = useMutation({
    mutationFn: () => ecrire(texte, pieces),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setTexte('')
      setPieces([])
      onChange()
    },
  })
  const effacement = useMutation({
    mutationFn: (m: Message) => apiMessagerie.supprimer(m.id),
    onSuccess: () => {
      setSuppression(null)
      toast.success('Message supprimé.')
      onChange()
    },
    onError: () => setSuppression(null),
  })
  const nonLu = useMutation({
    mutationFn: (m: Message) => apiMessagerie.marquerNonLu(m.id),
    onSuccess: () => {
      toast.success('Message marqué non lu.')
      onNonLu()
    },
  })

  const places = positions(conversation.messages)

  return (
    <div className={cn(panneau ? 'flex min-h-0 flex-1 flex-col' : 'space-y-4')}>
      <div
        ref={defilement}
        className={cn(panneau && 'min-h-0 flex-1 overflow-y-auto pe-1')}
      >
        {nombre === 0 ? (
          <p
            className={cn(
              'text-muted-foreground text-sm',
              panneau && 'flex h-full items-center justify-center'
            )}
          >
            {invitation ?? 'Aucun message.'}
          </p>
        ) : (
          <ol className='space-y-1 py-1'>
            {conversation.messages.map((m, i) => (
              <Fragment key={m.id}>
                {places[i].nouveauJour && (
                  <Separateur className='pt-3 first:pt-0'>
                    {libelleJour(m.ecritLe)}
                  </Separateur>
                )}
                {places[i].premierNonLu && (
                  <Separateur className='text-primary pt-2'>
                    Nouveaux messages
                  </Separateur>
                )}
                <MessageDuFil
                  message={m}
                  debutGroupe={places[i].debutGroupe}
                  monCote={conversation.cote}
                  operateur={conversation.operateur.raisonSociale}
                  conversation={conversation.id}
                  onSupprimer={() => setSuppression(m)}
                  onNonLu={() => nonLu.mutate(m)}
                  onModifie={onChange}
                />
              </Fragment>
            ))}
          </ol>
        )}
      </div>

      {conversation.peutEcrire ? (
        <form
          className={cn('space-y-2 border-t pt-4', panneau && 'mt-3')}
          onSubmit={(e) => {
            e.preventDefault()
            ecriture.mutate()
          }}
        >
          <Textarea
            rows={3}
            value={texte}
            maxLength={TEXTE_MAXIMAL}
            placeholder={
              conversation.cote === 'BNIA'
                ? `Écrire à ${conversation.operateur.raisonSociale}…`
                : 'Écrire au BNIA…'
            }
            aria-label='Message'
            onChange={(e) => setTexte(e.target.value)}
          />
          {libre && (
            <ChoixPieces
              id={`pieces-${conversation.id ?? 'nouvelle'}`}
              pieces={pieces}
              onChange={setPieces}
            />
          )}
          <div className='flex items-center justify-between gap-3'>
            <span className='text-muted-foreground text-xs'>
              {conversation.cote === 'BNIA'
                ? libre
                  ? `Lu par ${conversation.auteur ?? 'son auteur'} seulement.`
                  : 'Lu par tous les utilisateurs de l’opérateur.'
                : libre
                  ? 'Lu par le BNIA ; vos collègues ne voient pas cette conversation.'
                  : 'Lu par le BNIA ; vos collègues voient aussi cette conversation.'}
            </span>
            <Button
              type='submit'
              disabled={texte.trim() === '' || ecriture.isPending}
            >
              <Send className='size-4' /> Envoyer
            </Button>
          </div>
        </form>
      ) : (
        conversation.lectureSeule && (
          <p
            className={cn(
              'text-muted-foreground flex items-center gap-2 border-t pt-4 text-sm',
              panneau && 'mt-3'
            )}
          >
            <Lock className='size-4' /> {conversation.lectureSeule}
          </p>
        )
      )}

      <ConfirmDialog
        open={suppression != null}
        onOpenChange={(open) => !open && setSuppression(null)}
        title='Supprimer le message'
        desc='Le message sera supprimé pour tous, sans trace.'
        confirmText='Supprimer'
        cancelBtnText='Annuler'
        destructive
        isLoading={effacement.isPending}
        handleConfirm={() => suppression && effacement.mutate(suppression)}
      />
    </div>
  )
}

function MessageDuFil({
  message: m,
  debutGroupe,
  monCote,
  operateur,
  conversation,
  onSupprimer,
  onNonLu,
  onModifie,
}: {
  message: Message
  /** Le premier message d'un groupe porte l'avatar et l'en-tête (auteur, côté, heure). */
  debutGroupe: boolean
  monCote: Conversation['cote']
  operateur: string
  conversation: number | undefined
  onSupprimer: () => void
  onNonLu: () => void
  onModifie: () => void
}) {
  const [edition, setEdition] = useState(false)
  const [texte, setTexte] = useState(m.texte)
  const modification = useMutation({
    mutationFn: () => apiMessagerie.modifier(m.id, texte),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setEdition(false)
      onModifie()
    },
  })
  const deMonCote = m.cote === monCote
  const bnia = m.cote === 'BNIA'

  const actions: ActionElement[] = []
  if (!deMonCote)
    actions.push({
      libelle: 'Marquer non lu',
      icone: <MailOpen className='size-4' />,
      onClick: onNonLu,
    })
  if (m.deLUtilisateur)
    actions.push({
      libelle: 'Modifier',
      icone: <Pencil className='size-4' />,
      onClick: () => {
        setTexte(m.texte)
        setEdition(true)
      },
    })
  if (m.deLUtilisateur && !m.protege)
    actions.push({
      libelle: 'Supprimer',
      icone: <Trash2 className='size-4' />,
      onClick: onSupprimer,
      destructive: true,
    })

  return (
    <li
      id={`message-${m.id}`}
      className={cn(
        'group flex scroll-mt-4 gap-2',
        deMonCote && 'flex-row-reverse',
        debutGroupe && 'pt-2'
      )}
    >
      <div className='w-8 shrink-0'>
        {debutGroupe && (
          <AvatarAuteur
            nom={m.auteur}
            className={
              bnia
                ? 'bg-primary text-primary-foreground'
                : 'bg-warning-border text-warning-foreground'
            }
          />
        )}
      </div>
      <div
        className={cn(
          'flex min-w-0 flex-col gap-1',
          deMonCote ? 'items-end' : 'items-start',
          edition ? 'w-full max-w-xl' : 'max-w-[85%] sm:max-w-[75%]'
        )}
      >
        {debutGroupe && (
          <div
            className={cn(
              'text-muted-foreground flex flex-wrap items-baseline gap-x-1.5 px-1 text-xs',
              deMonCote && 'justify-end'
            )}
          >
            <span className='text-foreground text-sm font-medium'>
              {m.auteur}
            </span>
            <span>{bnia ? 'BNIA' : operateur}</span>
            <span aria-hidden>·</span>
            <time dateTime={m.ecritLe} title={formatDateHeure(m.ecritLe)}>
              {formatHeure(m.ecritLe)}
            </time>
          </div>
        )}
        {edition ? (
          <div className='w-full space-y-2'>
            <Textarea
              rows={3}
              value={texte}
              maxLength={TEXTE_MAXIMAL}
              onChange={(e) => setTexte(e.target.value)}
              aria-label='Message'
            />
            <div className='flex justify-end gap-3'>
              <Button
                variant='outline'
                size='sm'
                onClick={() => setEdition(false)}
              >
                Annuler
              </Button>
              <Button
                size='sm'
                disabled={texte.trim() === '' || modification.isPending}
                onClick={() => modification.mutate()}
              >
                Enregistrer
              </Button>
            </div>
          </div>
        ) : (
          <div
            className={cn(
              'flex max-w-full items-start gap-1',
              deMonCote && 'flex-row-reverse'
            )}
          >
            <div
              title={
                formatDateHeure(m.ecritLe) +
                (m.modifieLe
                  ? ` · modifié le ${formatDateHeure(m.modifieLe)}`
                  : '')
              }
              className={cn(
                'min-w-0 rounded-2xl px-3.5 py-2 text-[0.9375rem] leading-relaxed',
                deMonCote ? 'bg-primary/10 dark:bg-primary/20' : 'bg-muted',
                debutGroupe && (deMonCote ? 'rounded-se-sm' : 'rounded-ss-sm'),
                m.protege && 'border-destructive/40 border'
              )}
            >
              {m.protege && (
                <p className='text-destructive mb-1 flex items-center gap-1 text-xs font-medium'>
                  <FileWarning className='size-3.5' /> Motif des compléments
                </p>
              )}
              <p className='break-words whitespace-pre-wrap'>
                {m.texte}
                {m.modifieLe && (
                  <span className='text-muted-foreground ms-1.5 text-xs'>
                    (modifié)
                  </span>
                )}
              </p>
              {m.pieces.length > 0 && conversation != null && (
                <ul className='mt-2 flex flex-wrap gap-1.5'>
                  {m.pieces.map((p) => (
                    <li key={p.cle}>
                      <a
                        href={urlPieceJointe(conversation, p.cle)}
                        target='_blank'
                        rel='noopener'
                        className='bg-background/70 hover:bg-background inline-flex items-center gap-1 rounded-md border px-2 py-1 text-xs'
                      >
                        <Paperclip className='size-3' /> {p.nom}
                        <span className='text-muted-foreground'>
                          ({Math.max(1, Math.round(p.taille / 1024))} ko)
                        </span>
                      </a>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <MenuActions actions={actions} libelle='Actions du message' />
          </div>
        )}
      </div>
    </li>
  )
}
