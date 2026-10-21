import { type ReactNode, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Lock, NotebookPen, Pencil, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { ConfirmDialog } from '@/components/confirm-dialog'
import {
  type ActionElement,
  AvatarAuteur,
  MenuActions,
} from '@/components/metier/fil-messages'
import { formatDateHeure } from '@/features/declarations/format'

/** Ce que le carnet affiche d'une note : les notes internes d'une DSM (RG-DEC-24) ou d'une pièce (RG-FAC-21). */
export type NoteDuCarnet = {
  id: number
  auteur: string
  ecriteLe: string
  modifieeLe?: string | null
  texte: string
  deLUtilisateur: boolean
}

/**
 * Carnet de notes internes du BNIA (spec 15 §3.7), au bas d'une page de détail du back-office : distinct des messages
 * échangés avec l'opérateur, qui ne le voit jamais. Son auteur modifie ou supprime sa note. Utilisé par la DSM, dont
 * une note peut viser une ligne (`cibles`), et par la pièce de facturation.
 */
export function CarnetNotesInternes<N extends NoteDuCarnet>({
  description,
  notes,
  peutAnnoter,
  ecrire,
  modifier,
  supprimer,
  onChange,
  cibles,
  etiquette,
  enEvidence,
}: {
  description: ReactNode
  notes: N[]
  peutAnnoter: boolean
  /** Écrit une note ; `cible` est la valeur choisie parmi `cibles`, absente sans choix. */
  ecrire: (texte: string, cible: string | undefined) => Promise<unknown>
  modifier: (note: N, texte: string) => Promise<unknown>
  supprimer: (note: N) => Promise<unknown>
  /** Après une écriture, une modification ou une suppression : relire les notes. */
  onChange: () => void
  /** Ce que vise une note, la première valeur par défaut ; sans, pas de choix. */
  cibles?: { valeur: string; libelle: string }[]
  /** Pastilles après la date d'une note (sa ligne, par exemple). */
  etiquette?: (note: N) => ReactNode
  enEvidence?: (note: N) => boolean
}) {
  const parDefaut = cibles?.[0]?.valeur
  const [cible, setCible] = useState(parDefaut)
  const [texte, setTexte] = useState('')
  const [suppression, setSuppression] = useState<N | null>(null)

  const ecriture = useMutation({
    mutationFn: () => ecrire(texte, cible),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setTexte('')
      setCible(parDefaut)
      onChange()
    },
  })
  const effacement = useMutation({
    mutationFn: (n: N) => supprimer(n),
    onSuccess: () => {
      setSuppression(null)
      toast.success('Note supprimée.')
      onChange()
    },
    onError: () => setSuppression(null),
  })

  const champTexte = (
    <div className='space-y-1'>
      <Label htmlFor='note-texte'>Note</Label>
      <Textarea
        id='note-texte'
        rows={3}
        className='bg-white'
        value={texte}
        onChange={(e) => setTexte(e.target.value)}
      />
    </div>
  )

  return (
    <section
      id='notes-internes'
      className='border-warning-border bg-warning/60 mt-8 scroll-mt-4 rounded-md border border-dashed p-5 shadow-xs'
    >
      <div className='border-warning-border mb-6 space-y-1.5 border-b pb-4'>
        <h2 className='flex items-center gap-2 text-xl font-semibold'>
          <NotebookPen className='size-5' /> Notes internes
          {notes.length > 0 && (
            <Badge variant='secondary'>{notes.length}</Badge>
          )}
          <Badge variant='outline' className='ms-auto gap-1 font-normal'>
            <Lock className='size-3' /> Visible du BNIA seulement
          </Badge>
        </h2>
        <p className='text-muted-foreground text-sm'>{description}</p>
      </div>

      {notes.length === 0 ? (
        <p className='text-muted-foreground text-sm'>Aucune note.</p>
      ) : (
        <ol className='space-y-3'>
          {notes.map((n) => (
            <Note
              key={n.id}
              note={n}
              enEvidence={enEvidence?.(n) ?? false}
              etiquette={etiquette?.(n)}
              modifier={(texte) => modifier(n, texte)}
              onSupprimer={() => setSuppression(n)}
              onModifie={onChange}
            />
          ))}
        </ol>
      )}

      {peutAnnoter && (
        <form
          className='border-warning-border mt-6 space-y-3 border-t pt-4'
          onSubmit={(e) => {
            e.preventDefault()
            ecriture.mutate()
          }}
        >
          {cibles ? (
            <div className='grid gap-3 sm:grid-cols-[minmax(0,20rem)_1fr]'>
              <div className='space-y-1'>
                <Label htmlFor='note-cible'>Porte sur</Label>
                <Select value={cible} onValueChange={setCible}>
                  <SelectTrigger id='note-cible' className='w-full bg-white'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {cibles.map((c) => (
                      <SelectItem key={c.valeur} value={c.valeur}>
                        {c.libelle}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              {champTexte}
            </div>
          ) : (
            champTexte
          )}
          <div className='flex justify-end'>
            <Button
              type='submit'
              variant='outline'
              disabled={texte.trim() === '' || ecriture.isPending}
            >
              <Lock className='size-4' /> Ajouter la note
            </Button>
          </div>
        </form>
      )}

      <ConfirmDialog
        open={suppression != null}
        onOpenChange={(open) => !open && setSuppression(null)}
        title='Supprimer la note'
        desc='La note sera supprimée du carnet.'
        confirmText='Supprimer'
        destructive
        isLoading={effacement.isPending}
        handleConfirm={() => suppression && effacement.mutate(suppression)}
      />
    </section>
  )
}

function Note({
  note: n,
  enEvidence,
  etiquette,
  modifier,
  onSupprimer,
  onModifie,
}: {
  note: NoteDuCarnet
  enEvidence: boolean
  etiquette: ReactNode
  modifier: (texte: string) => Promise<unknown>
  onSupprimer: () => void
  onModifie: () => void
}) {
  const [edition, setEdition] = useState(false)
  const [texte, setTexte] = useState(n.texte)
  const modification = useMutation({
    mutationFn: () => modifier(texte),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setEdition(false)
      onModifie()
    },
  })

  const actions: ActionElement[] =
    n.deLUtilisateur && !edition
      ? [
          {
            libelle: 'Modifier',
            icone: <Pencil className='size-4' />,
            onClick: () => {
              setTexte(n.texte)
              setEdition(true)
            },
          },
          {
            libelle: 'Supprimer',
            icone: <Trash2 className='size-4' />,
            onClick: onSupprimer,
            destructive: true,
          },
        ]
      : []

  return (
    <li className='group flex gap-2'>
      <AvatarAuteur
        nom={n.auteur}
        className='bg-warning-border text-warning-foreground'
      />
      <div
        className={cn(
          'border-warning-border bg-card min-w-0 flex-1 rounded-2xl rounded-ss-sm border px-3.5 py-2',
          enEvidence && 'ring-warning-foreground ring-2'
        )}
      >
        <div className='flex items-start gap-2'>
          <div className='text-muted-foreground flex min-w-0 flex-1 flex-wrap items-baseline gap-x-1.5 text-xs'>
            <span className='text-foreground text-sm font-medium'>
              {n.auteur}
            </span>
            <time dateTime={n.ecriteLe}>{formatDateHeure(n.ecriteLe)}</time>
            {n.modifieeLe && (
              <span title={formatDateHeure(n.modifieeLe)}>· modifiée</span>
            )}
            {etiquette}
          </div>
          <MenuActions actions={actions} libelle='Actions de la note' />
        </div>
        {edition ? (
          <div className='mt-2 space-y-2'>
            <Textarea
              rows={3}
              value={texte}
              onChange={(e) => setTexte(e.target.value)}
              aria-label='Note'
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
          <p className='mt-1 text-[0.9375rem] leading-relaxed break-words whitespace-pre-wrap'>
            {n.texte}
          </p>
        )}
      </div>
    </li>
  )
}
