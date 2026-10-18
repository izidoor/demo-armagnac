import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Lock, NotebookPen, Pencil, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { type Dsm, type NoteInterneDsm } from '@/api/generated'
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
import { apiDeclarations } from './api'
import { formatDateHeure } from './format'
import { identite } from './libelles'

const SUR_LA_DSM = 'dsm'

/**
 * Carnet de notes internes d'une DSM (spec 15 §3.7, RG-DEC-24), au back-office seulement : distinct des messages
 * échangés avec l'opérateur, qui ne le voit jamais. Une note vise la DSM ou une ligne (note de contrôle, §4.4.5
 * p.26) ; son auteur la modifie ou la supprime ; écrite par qui gère les Déclarations au BNIA.
 */
export function SectionNotesInternes({
  dsm,
  notes,
  peutAnnoter,
  ligneCiblee,
  onMontrerLigne,
  onChange,
}: {
  dsm: Dsm
  notes: NoteInterneDsm[]
  peutAnnoter: boolean
  /** Les notes de cette ligne sont mises en évidence (pastille de la grille). */
  ligneCiblee: number | null
  onMontrerLigne: (ligne: number) => void
  onChange: () => void
}) {
  const api = apiDeclarations(dsm.operateur)
  const [ligne, setLigne] = useState<string>(SUR_LA_DSM)
  const [texte, setTexte] = useState('')
  const [suppression, setSuppression] = useState<NoteInterneDsm | null>(null)

  const libelleLigne = (id: number) => {
    const l = dsm.lignes.find((x) => x.id === id)
    return l ? identite(l) : `ligne n° ${id}`
  }

  const ecriture = useMutation({
    mutationFn: () =>
      api.annoter(dsm.id, {
        ligne: ligne === SUR_LA_DSM ? undefined : Number(ligne),
        texte,
      }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setTexte('')
      setLigne(SUR_LA_DSM)
      onChange()
    },
  })
  const effacement = useMutation({
    mutationFn: (n: NoteInterneDsm) => api.supprimerNote(dsm.id, n.id),
    onSuccess: () => {
      setSuppression(null)
      toast.success('Note supprimée.')
      onChange()
    },
    onError: () => setSuppression(null),
  })

  return (
    <section
      id='notes-internes'
      className='mt-8 scroll-mt-4 rounded-md border border-dashed border-amber-400 bg-amber-50/60 p-5 shadow-xs dark:border-amber-700 dark:bg-amber-950/20'
    >
      <div className='mb-6 space-y-1.5 border-b border-amber-300 pb-4 dark:border-amber-800'>
        <h2 className='flex items-center gap-2 text-xl font-semibold'>
          <NotebookPen className='size-5' /> Notes internes
          {notes.length > 0 && (
            <Badge variant='secondary'>{notes.length}</Badge>
          )}
          <Badge variant='outline' className='ms-auto gap-1 font-normal'>
            <Lock className='size-3' /> Visible du BNIA seulement
          </Badge>
        </h2>
        <p className='text-muted-foreground text-sm'>
          Le carnet du BNIA sur cette DSM ou l'une de ses lignes : notes de
          contrôle, rapprochements, points à vérifier. L'opérateur ne le voit
          jamais ; pour lui écrire, utilisez les messages.
        </p>
      </div>

      {notes.length === 0 ? (
        <p className='text-muted-foreground text-sm'>Aucune note.</p>
      ) : (
        <ol className='space-y-3'>
          {notes.map((n) => (
            <Note
              key={n.id}
              note={n}
              enEvidence={ligneCiblee != null && n.ligne === ligneCiblee}
              libelleLigne={libelleLigne}
              onMontrerLigne={onMontrerLigne}
              onSupprimer={() => setSuppression(n)}
              onModifie={onChange}
              operateur={dsm.operateur}
              dsm={dsm.id}
            />
          ))}
        </ol>
      )}

      {peutAnnoter && (
        <form
          className='mt-6 space-y-3 border-t border-amber-300 pt-4 dark:border-amber-800'
          onSubmit={(e) => {
            e.preventDefault()
            ecriture.mutate()
          }}
        >
          <div className='grid gap-3 sm:grid-cols-[minmax(0,20rem)_1fr]'>
            <div className='space-y-1'>
              <Label htmlFor='note-ligne'>Porte sur</Label>
              <Select value={ligne} onValueChange={setLigne}>
                <SelectTrigger id='note-ligne' className='w-full bg-white'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={SUR_LA_DSM}>La DSM</SelectItem>
                  {dsm.lignes
                    .filter((l) => l.id != null)
                    .map((l) => (
                      <SelectItem key={l.id} value={String(l.id)}>
                        Ligne {identite(l)}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>
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
          </div>
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
        cancelBtnText='Annuler'
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
  libelleLigne,
  onMontrerLigne,
  onSupprimer,
  onModifie,
  operateur,
  dsm,
}: {
  note: NoteInterneDsm
  enEvidence: boolean
  libelleLigne: (id: number) => string
  onMontrerLigne: (ligne: number) => void
  onSupprimer: () => void
  onModifie: () => void
  operateur: number
  dsm: number
}) {
  const [edition, setEdition] = useState(false)
  const [texte, setTexte] = useState(n.texte)
  const modification = useMutation({
    mutationFn: () =>
      apiDeclarations(operateur).modifierNote(dsm, n.id, { texte }),
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
        className='bg-amber-200 text-amber-900 dark:bg-amber-800 dark:text-amber-100'
      />
      <div
        className={cn(
          'min-w-0 flex-1 rounded-2xl rounded-ss-sm border border-amber-200 bg-white px-3.5 py-2 dark:border-amber-900 dark:bg-amber-950/40',
          enEvidence && 'ring-2 ring-amber-400'
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
            {n.ligne != null && (
              <button
                type='button'
                className='rounded-full bg-amber-100 px-2 py-0.5 font-medium text-amber-900 hover:bg-amber-200 dark:bg-amber-900/60 dark:text-amber-100'
                onClick={() => onMontrerLigne(n.ligne!)}
              >
                Ligne {libelleLigne(n.ligne)}
              </button>
            )}
            {n.ligneSupprimee && (
              <span className='rounded-full border border-dashed px-2 py-0.5 line-through'>
                Ligne {n.ligneSupprimee}
              </span>
            )}
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
