import { useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  ChevronLeft,
  MessageSquarePlus,
  MessagesSquare,
  Search,
  UserX,
  X,
} from 'lucide-react'
import { createPortal } from 'react-dom'
import { toast } from 'sonner'
import { type ResultatRechercheOperateur } from '@/api/generated'
import { cn } from '@/lib/utils'
import { useContenuAuMoins } from '@/hooks/use-contenu-au-moins'
import { useDebounce } from '@/hooks/use-debounce'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { formatDateCourte } from '@/components/metier/dates-fil'
import { PageMetier } from '@/components/metier/page-metier'
import { RechercheOperateur } from '@/features/operateurs/portail/recherche-operateur'
import { useDroits } from '@/features/session/session'
import {
  apiMessagerie,
  cleListesMessagerie,
  cleMessagerie,
  type FiltreConversations,
  type TypeObjet,
} from './api'
import { ChoixPieces, FilConversation } from './fil-conversation'
import { type EspaceMessagerie, LienObjet } from './lien-objet'
import { libelleNouveaux } from './nouveaux'

/**
 * Ce que l'adresse de la messagerie retient : le filtre des non lues, la conversation affichée, le message visé.
 */
type SelectionMessagerie = {
  nonLues?: boolean
  conversation?: number
  message?: number
}

type FiltreType = 'TOUS' | TypeObjet | 'LIBRE'

const LIBELLES_TYPE: Record<TypeObjet, string> = {
  DSM: 'DSM',
  FACTURE: 'Facture',
}

/**
 * La messagerie (spec 15 §6), commune au back-office et au portail : à gauche, mes conversations comme une boîte de
 * réception, non lues en gras, filtrées et cherchées (RG-MSG-15, 16) ; à droite, la conversation choisie, lue en
 * s'affichant (RG-MSG-10), avec le lien vers son objet. Au portail, « Nouvelle conversation » ouvre un sujet libre
 * avec le BNIA, pour qui gère le module Messagerie, l'observateur ne faisant que le lire (RG-MSG-12).
 *
 * Les deux panneaux côte à côte demandent 68rem de contenu (`COTE_A_COTE`), barre latérale comprise : la liste garde
 * ses 26rem, la conversation en a une quarantaine. En dessous, la liste seule ; la conversation choisie s'ouvre
 * par-dessus la zone de contenu (tout l'écran sur un téléphone), sous un bouton de retour à la liste
 * (`ConversationPleinEcran`).
 */
/** Largeur de contenu des deux panneaux côte à côte, en rem ; écrite aussi en entier dans les classes `@…-[68rem]/content`. */
const COTE_A_COTE = 68

export function PageMessagerie({
  espace,
  operateur,
  selection,
  onSelection,
}: {
  espace: EspaceMessagerie
  /** L'opérateur actif, au portail ; absent au back-office. */
  operateur?: number
  selection: SelectionMessagerie
  onSelection: (selection: SelectionMessagerie) => void
}) {
  const [nouvelle, setNouvelle] = useState(false)
  const coteACote = useContenuAuMoins(COTE_A_COTE)
  // Ouverte depuis la liste, le retour revient en arrière dans l'historique : le bouton retour du téléphone et
  // celui de l'écran font de même. Ouverte par un lien (tableau de bord, notification), il n'y a pas de liste où
  // revenir : le retour l'y affiche.
  const [depuisListe, setDepuisListe] = useState(false)
  const retourALaListe = () => {
    if (depuisListe) {
      setDepuisListe(false)
      window.history.back()
    } else onSelection({ nonLues: selection.nonLues })
  }

  return (
    <PageMetier
      titre='Messagerie'
      pleineHauteur
      description={
        espace === 'bo'
          ? 'Les échanges avec les opérateurs : conversations des DSM, des factures et sujets libres.'
          : 'Vos échanges avec le BNIA : conversations de vos DSM, de vos factures et sujets libres.'
      }
      actions={
        espace === 'portail' &&
        operateur !== undefined && (
          <BoutonNouvelleConversation
            operateur={operateur}
            onClick={() => setNouvelle(true)}
          />
        )
      }
    >
      <div className='grid gap-6 lg:min-h-0 lg:flex-1 lg:grid-rows-[minmax(0,1fr)] @min-[68rem]/content:grid-cols-[minmax(0,26rem)_minmax(0,1fr)]'>
        <ListeConversations
          espace={espace}
          operateur={operateur}
          choisie={selection.conversation}
          nonLues={selection.nonLues ?? false}
          onNonLues={(nonLues) =>
            onSelection({ ...selection, nonLues: nonLues || undefined })
          }
          onChoisir={(conversation) => {
            if (!coteACote) setDepuisListe(true)
            onSelection({ nonLues: selection.nonLues, conversation })
          }}
        />
        <div className='min-h-0 min-w-0 @max-[68rem]/content:hidden'>
          {selection.conversation != null && coteACote ? (
            <PanneauConversation
              key={selection.conversation}
              id={selection.conversation}
              message={selection.message}
              espace={espace}
            />
          ) : (
            <section className='bg-card flex h-full min-h-0 flex-col items-center justify-center gap-4 rounded-md border p-5 text-center shadow-xs'>
              <span className='bg-muted text-muted-foreground flex size-16 items-center justify-center rounded-full'>
                <MessagesSquare className='size-8' />
              </span>
              <div className='space-y-1'>
                <p className='font-medium'>Choisissez une conversation</p>
                <p className='text-muted-foreground max-w-xs text-sm'>
                  {espace === 'portail'
                    ? 'Ses messages s’afficheront ici. Pour écrire au BNIA sur un nouveau sujet, ouvrez une nouvelle conversation.'
                    : 'Ses messages s’afficheront ici.'}
                </p>
              </div>
            </section>
          )}
        </div>
      </div>
      {selection.conversation != null && !coteACote && (
        <ConversationPleinEcran onRetour={retourALaListe}>
          <PanneauConversation
            key={selection.conversation}
            id={selection.conversation}
            message={selection.message}
            espace={espace}
            pleinEcran
          />
        </ConversationPleinEcran>
      )}
      {operateur != null && (
        <NouvelleConversation
          open={nouvelle}
          onOpenChange={setNouvelle}
          operateur={operateur}
          onOuverte={(id) => {
            setNouvelle(false)
            onSelection({ conversation: id })
          }}
        />
      )}
    </PageMetier>
  )
}

/** RG-MSG-12 : l'ouverture d'une conversation libre, pour qui gère le module Messagerie ; l'observateur le lit. */
function BoutonNouvelleConversation({
  operateur,
  onClick,
}: {
  operateur: number
  onClick: () => void
}) {
  const droits = useDroits(operateur)
  if (droits?.messagerie !== 'GESTION') return null
  return (
    <Button onClick={onClick}>
      <MessageSquarePlus className='size-4' /> Nouvelle conversation
    </Button>
  )
}

/**
 * RG-MSG-15, 16 : mes conversations, de la plus récemment écrite à la plus ancienne, comme une boîte de réception :
 * celles qui ont des messages non lus de mon côté ressortent en gras, avec leur nombre (RG-MSG-11).
 */
function ListeConversations({
  espace,
  operateur,
  choisie,
  nonLues,
  onNonLues,
  onChoisir,
}: {
  espace: EspaceMessagerie
  operateur?: number
  choisie?: number
  nonLues: boolean
  onNonLues: (nonLues: boolean) => void
  onChoisir: (conversation: number) => void
}) {
  const [type, setType] = useState<FiltreType>('TOUS')
  const [recherche, setRecherche] = useState('')
  const [deLOperateur, setDeLOperateur] =
    useState<ResultatRechercheOperateur | null>(null)
  const [choixOperateur, setChoixOperateur] = useState(false)
  const texte = useDebounce(recherche.trim())
  const filtre: FiltreConversations = {
    operateur: deLOperateur?.id,
    type: type === 'TOUS' || type === 'LIBRE' ? undefined : type,
    libres: type === 'LIBRE' || undefined,
    nonLues: nonLues || undefined,
    recherche: texte || undefined,
  }
  const { data, isPending } = useQuery({
    queryKey: [...cleListesMessagerie, 'conversations', operateur, filtre],
    queryFn: () => apiMessagerie.conversations(operateur, filtre),
  })

  return (
    <div className='flex min-h-0 min-w-0 flex-col gap-3'>
      <div className='relative shrink-0'>
        <Search className='text-muted-foreground absolute start-2.5 top-2.5 size-4' />
        <Input
          className='ps-8'
          placeholder='Chercher dans les conversations'
          value={recherche}
          onChange={(e) => setRecherche(e.target.value)}
          aria-label='Chercher'
        />
      </div>
      <div className='flex shrink-0 flex-wrap items-center gap-3'>
        <Select value={type} onValueChange={(v) => setType(v as FiltreType)}>
          <SelectTrigger className='w-40' aria-label='Type'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='TOUS'>Tous les sujets</SelectItem>
            <SelectItem value='DSM'>DSM</SelectItem>
            <SelectItem value='FACTURE'>Factures</SelectItem>
            <SelectItem value='LIBRE'>Sujets libres</SelectItem>
          </SelectContent>
        </Select>
        {espace === 'bo' &&
          (deLOperateur ? (
            <span className='flex h-9 items-center gap-1 rounded-md border px-3 text-sm'>
              <span className='font-mono'>{deLOperateur.code}</span>
              <Button
                variant='ghost'
                size='icon'
                className='size-6'
                aria-label="Retirer le filtre sur l'opérateur"
                onClick={() => setDeLOperateur(null)}
              >
                <X className='size-4' />
              </Button>
            </span>
          ) : (
            <Popover open={choixOperateur} onOpenChange={setChoixOperateur}>
              <PopoverTrigger asChild>
                <Button variant='outline'>Tous les opérateurs</Button>
              </PopoverTrigger>
              <PopoverContent
                align='start'
                className='w-96 max-w-[calc(100vw-2rem)]'
              >
                <RechercheOperateur
                  onChoisir={(o) => {
                    setDeLOperateur(o)
                    setChoixOperateur(false)
                  }}
                />
              </PopoverContent>
            </Popover>
          ))}
        <div className='flex items-center gap-2'>
          <Checkbox
            id='non-lues'
            checked={nonLues}
            onCheckedChange={(v) => onNonLues(v === true)}
          />
          <Label htmlFor='non-lues' className='font-normal'>
            Non lues seulement
          </Label>
        </div>
      </div>
      {isPending ? (
        <p className='text-muted-foreground text-sm'>Chargement…</p>
      ) : !data || data.length === 0 ? (
        <p className='text-muted-foreground rounded-md border p-4 text-sm'>
          Aucune conversation.
        </p>
      ) : (
        <ul className='min-h-0 divide-y overflow-y-auto rounded-md border'>
          {data.map((c) => {
            const nonLu = c.nonLus > 0
            return (
              <li key={c.id}>
                <button
                  type='button'
                  className={cn(
                    'hover:bg-muted/50 flex w-full gap-2.5 px-3 py-2.5 text-start',
                    choisie === c.id && 'bg-muted hover:bg-muted'
                  )}
                  onClick={() => onChoisir(c.id)}
                >
                  <span className='flex w-2 shrink-0 justify-center pt-2'>
                    {nonLu && (
                      <span className='bg-primary size-2 rounded-full' />
                    )}
                  </span>
                  <span className='min-w-0 flex-1 space-y-0.5'>
                    <span className='flex items-baseline gap-2'>
                      <span
                        className={cn(
                          'truncate',
                          nonLu ? 'font-semibold' : 'font-normal'
                        )}
                      >
                        {c.intitule}
                      </span>
                      <time
                        dateTime={c.dernierMessage}
                        className={cn(
                          'ms-auto shrink-0 text-xs',
                          nonLu
                            ? 'text-primary font-semibold'
                            : 'text-muted-foreground'
                        )}
                      >
                        {c.dernierMessage && formatDateCourte(c.dernierMessage)}
                      </time>
                    </span>
                    <span className='text-muted-foreground flex items-center gap-1.5 text-xs'>
                      <Badge
                        variant={c.objet ? 'default' : 'secondary'}
                        className='shrink-0 px-1.5 py-0 text-[11px] font-normal'
                      >
                        {c.objet ? LIBELLES_TYPE[c.objet.type] : 'Sujet libre'}
                      </Badge>
                      <span className='truncate'>
                        {[
                          espace === 'bo' ? c.operateur.raisonSociale : null,
                          c.auteur,
                        ]
                          .filter(Boolean)
                          .join(' · ')}
                      </span>
                    </span>
                    <span className='flex items-center gap-2'>
                      <span
                        className={cn(
                          'truncate text-sm',
                          nonLu
                            ? 'text-foreground font-medium'
                            : 'text-muted-foreground'
                        )}
                      >
                        {c.extrait}
                      </span>
                      {nonLu && (
                        <Badge
                          className='ms-auto h-5 min-w-5 shrink-0 rounded-full px-1.5 tabular-nums'
                          aria-label={libelleNouveaux(c.nonLus)}
                        >
                          {c.nonLus}
                        </Badge>
                      )}
                    </span>
                  </span>
                </button>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}

/**
 * Sans la place des deux panneaux, la conversation par-dessus la zone de contenu (`SidebarInset`, tout l'écran sur un
 * téléphone), entrée par la droite, sous « Conversations » pour revenir à la liste.
 */
function ConversationPleinEcran({
  onRetour,
  children,
}: {
  onRetour: () => void
  children: React.ReactNode
}) {
  return createPortal(
    <div
      role='dialog'
      aria-modal='true'
      aria-label='Conversation'
      className='bg-background animate-in slide-in-from-right absolute inset-0 z-50 flex flex-col rounded-[inherit] duration-200'
    >
      <div className='flex h-14 shrink-0 items-center border-b px-2'>
        <Button variant='ghost' className='gap-1 px-2' onClick={onRetour}>
          <ChevronLeft className='size-5' /> Conversations
        </Button>
      </div>
      <div className='flex min-h-0 flex-1 flex-col p-4'>{children}</div>
    </div>,
    document.querySelector('[data-slot="sidebar-inset"]') ?? document.body
  )
}

/**
 * La conversation choisie : l'afficher la lit pour mon côté (RG-MSG-10), puis les listes et le compteur se relisent.
 * Elle s'ouvre sur le message visé, sinon sur son premier message non lu, sinon sur le dernier.
 */
function PanneauConversation({
  id,
  message,
  espace,
  pleinEcran = false,
}: {
  id: number
  message?: number
  espace: EspaceMessagerie
  /** Dans `ConversationPleinEcran` : toute la hauteur, sans cadre. */
  pleinEcran?: boolean
}) {
  const queryClient = useQueryClient()
  const { data: conversation, isError } = useQuery({
    queryKey: [...cleMessagerie, 'conversation', id],
    queryFn: () => apiMessagerie.conversation(id),
    refetchOnWindowFocus: false,
  })
  const listes = () =>
    queryClient.invalidateQueries({ queryKey: cleListesMessagerie })

  useEffect(() => {
    if (!conversation) return
    // La lecture a marqué les messages lus : le compteur et les listes changent.
    queryClient.invalidateQueries({ queryKey: cleListesMessagerie })
  }, [conversation, queryClient])

  if (!conversation) {
    return (
      <p className='text-muted-foreground text-sm'>
        {isError ? 'La conversation n’a pas pu être chargée.' : 'Chargement…'}
      </p>
    )
  }
  return (
    <section
      className={cn(
        'flex flex-col',
        pleinEcran
          ? 'min-h-0 flex-1'
          : 'bg-card h-full min-h-0 rounded-md border p-5 shadow-xs'
      )}
    >
      <div className='mb-4 flex shrink-0 flex-wrap items-start gap-3 border-b pb-4'>
        <div className='min-w-0 flex-1 space-y-1'>
          <h2 className='text-xl font-semibold'>
            {conversation.objet?.libelle ?? conversation.titre}
          </h2>
          <p className='text-muted-foreground text-sm'>
            {espace === 'bo' && (
              <>
                <span className='font-mono'>{conversation.operateur.code}</span>{' '}
                {conversation.operateur.raisonSociale}
                {' · '}
              </>
            )}
            {conversation.objet
              ? LIBELLES_TYPE[conversation.objet.type]
              : `Sujet libre ouvert par ${conversation.auteur}`}
          </p>
        </div>
        {conversation.auteurParti && (
          <Badge variant='secondary' className='gap-1'>
            <UserX className='size-3' /> Auteur parti
          </Badge>
        )}
        {conversation.objet && (
          <LienObjet
            objet={conversation.objet}
            operateur={conversation.operateur.id}
            espace={espace}
          />
        )}
      </div>
      <FilConversation
        conversation={conversation}
        ecrire={(texte, pieces) => apiMessagerie.repondre(id, texte, pieces)}
        onChange={() =>
          queryClient.invalidateQueries({ queryKey: cleMessagerie })
        }
        onNonLu={listes}
        panneau
        messageVise={message}
      />
    </section>
  )
}

/** RG-MSG-12, 14 : au portail, un sujet libre avec le BNIA, un premier message et ses pièces jointes. */
function NouvelleConversation({
  open,
  onOpenChange,
  operateur,
  onOuverte,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  operateur: number
  onOuverte: (id: number) => void
}) {
  const queryClient = useQueryClient()
  const [titre, setTitre] = useState('')
  const [texte, setTexte] = useState('')
  const [pieces, setPieces] = useState<File[]>([])
  const ouverture = useMutation({
    mutationFn: () => apiMessagerie.ouvrir(operateur, titre, texte, pieces),
    meta: { erreurAffichee: true },
    onSuccess: (conversation) => {
      setTitre('')
      setTexte('')
      setPieces([])
      toast.success('Conversation envoyée au BNIA.')
      queryClient.invalidateQueries({ queryKey: cleListesMessagerie })
      if (conversation.id != null) onOuverte(conversation.id)
    },
  })
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-xl'>
        <DialogHeader>
          <DialogTitle>Nouvelle conversation avec le BNIA</DialogTitle>
          <DialogDescription>
            Seuls vous et le BNIA verrez cette conversation : les autres
            utilisateurs de votre entreprise ne la voient pas.
          </DialogDescription>
        </DialogHeader>
        <form
          id='nouvelle-conversation'
          className='space-y-4'
          onSubmit={(e) => {
            e.preventDefault()
            ouverture.mutate()
          }}
        >
          <div className='space-y-1'>
            <Label htmlFor='conversation-titre'>Titre</Label>
            <Input
              id='conversation-titre'
              maxLength={120}
              value={titre}
              onChange={(e) => setTitre(e.target.value)}
              placeholder='Import de la matrice refusé'
            />
          </div>
          <div className='space-y-1'>
            <Label htmlFor='conversation-texte'>Message</Label>
            <Textarea
              id='conversation-texte'
              rows={5}
              maxLength={4000}
              value={texte}
              onChange={(e) => setTexte(e.target.value)}
            />
          </div>
          <ChoixPieces
            id='conversation-pieces'
            pieces={pieces}
            onChange={setPieces}
          />
        </form>
        <DialogFooter>
          <Button variant='outline' onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button
            type='submit'
            form='nouvelle-conversation'
            disabled={
              titre.trim() === '' || texte.trim() === '' || ouverture.isPending
            }
          >
            Envoyer
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
