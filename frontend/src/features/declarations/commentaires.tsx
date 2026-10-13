import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { MessageSquare, Pencil, Trash2 } from 'lucide-react'
import { toast } from 'sonner'
import { type CommentaireDsm, type Dsm } from '@/api/generated'
import { cn } from '@/lib/utils'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
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
import { apiDeclarations } from './api'
import { formatDateHeure } from './format'
import { identite } from './libelles'

const SUR_LA_DSM = 'dsm'

/**
 * Commentaires d'instruction d'une DSM (spec 05 §3.2.4, §6.3), sous le classeur : ils ne font pas partie de
 * la saisie (décision du 2026-10-13). Le fil, du plus ancien au plus récent ; un commentaire vise la DSM ou
 * une ligne ; « interne » côté BNIA seulement, jamais montré à l'opérateur (RG-DEC-24) ; l'auteur modifie
 * ou supprime le sien, sauf le motif d'une demande de compléments, qui ne se supprime pas (RG-DEC-25).
 */
export function SectionCommentaires({
  dsm,
  commentaires,
  peutCommenter,
  peutInterne,
  ligneCiblee,
  onMontrerLigne,
  onChange,
}: {
  dsm: Dsm
  commentaires: CommentaireDsm[]
  peutCommenter: boolean
  peutInterne: boolean
  /** Les commentaires de cette ligne sont mis en évidence (pastille de la grille). */
  ligneCiblee: number | null
  onMontrerLigne: (ligne: number) => void
  onChange: () => void
}) {
  const api = apiDeclarations(dsm.operateur)
  const [ligne, setLigne] = useState<string>(SUR_LA_DSM)
  const [texte, setTexte] = useState('')
  const [interne, setInterne] = useState(false)
  const [suppression, setSuppression] = useState<CommentaireDsm | null>(null)

  const libelleLigne = (id: number) => {
    const l = dsm.lignes.find((x) => x.id === id)
    return l ? identite(l) : `ligne n° ${id}`
  }

  const ecriture = useMutation({
    mutationFn: () =>
      api.commenter(dsm.id, {
        ligne: ligne === SUR_LA_DSM ? undefined : Number(ligne),
        texte,
        interne: peutInterne && interne,
      }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setTexte('')
      setInterne(false)
      setLigne(SUR_LA_DSM)
      onChange()
    },
  })
  const effacement = useMutation({
    mutationFn: (c: CommentaireDsm) => api.supprimerCommentaire(dsm.id, c.id),
    onSuccess: () => {
      setSuppression(null)
      toast.success('Commentaire supprimé.')
      onChange()
    },
    onError: () => setSuppression(null),
  })

  return (
    <section
      id='commentaires'
      className='bg-card mt-8 scroll-mt-4 rounded-md border p-5 shadow-xs'
    >
      <div className='mb-6 space-y-1.5 border-b pb-4'>
        <h2 className='flex items-center gap-2 text-xl font-semibold'>
          <MessageSquare className='size-5' /> Commentaires
          {commentaires.length > 0 && (
            <Badge variant='secondary'>{commentaires.length}</Badge>
          )}
        </h2>
        <p className='text-muted-foreground text-sm'>
          Les échanges entre l'opérateur et le BNIA sur cette DSM ou l'une de
          ses lignes, dans tous ses états.
        </p>
      </div>

      {commentaires.length === 0 ? (
        <p className='text-muted-foreground text-sm'>Aucun commentaire.</p>
      ) : (
        <ol className='space-y-3'>
          {commentaires.map((c) => (
            <Commentaire
              key={c.id}
              commentaire={c}
              enEvidence={ligneCiblee != null && c.ligne === ligneCiblee}
              peutInterne={peutInterne}
              libelleLigne={libelleLigne}
              onMontrerLigne={onMontrerLigne}
              onSupprimer={() => setSuppression(c)}
              onModifie={onChange}
              operateur={dsm.operateur}
              dsm={dsm.id}
            />
          ))}
        </ol>
      )}

      {peutCommenter && (
        <form
          className='mt-6 space-y-3 border-t pt-4'
          onSubmit={(e) => {
            e.preventDefault()
            ecriture.mutate()
          }}
        >
          <div className='grid gap-3 sm:grid-cols-[minmax(0,20rem)_1fr]'>
            <div className='space-y-1'>
              <Label htmlFor='commentaire-ligne'>Porte sur</Label>
              <Select value={ligne} onValueChange={setLigne}>
                <SelectTrigger id='commentaire-ligne' className='w-full'>
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
              <Label htmlFor='commentaire-texte'>Commentaire</Label>
              <Textarea
                id='commentaire-texte'
                rows={3}
                value={texte}
                onChange={(e) => setTexte(e.target.value)}
              />
            </div>
          </div>
          <div className='flex flex-wrap items-center justify-end gap-4'>
            {peutInterne && (
              <CaseInterne
                id='commentaire-interne'
                valeur={interne}
                onChange={setInterne}
              />
            )}
            <Button
              type='submit'
              disabled={texte.trim() === '' || ecriture.isPending}
            >
              Commenter
            </Button>
          </div>
        </form>
      )}

      <ConfirmDialog
        open={suppression != null}
        onOpenChange={(open) => !open && setSuppression(null)}
        title='Supprimer le commentaire'
        desc='Le commentaire sera supprimé ; la suppression reste tracée à l’historique de la DSM.'
        confirmText='Supprimer'
        cancelBtnText='Annuler'
        destructive
        isLoading={effacement.isPending}
        handleConfirm={() => suppression && effacement.mutate(suppression)}
      />
    </section>
  )
}

function Commentaire({
  commentaire: c,
  enEvidence,
  peutInterne,
  libelleLigne,
  onMontrerLigne,
  onSupprimer,
  onModifie,
  operateur,
  dsm,
}: {
  commentaire: CommentaireDsm
  enEvidence: boolean
  peutInterne: boolean
  libelleLigne: (id: number) => string
  onMontrerLigne: (ligne: number) => void
  onSupprimer: () => void
  onModifie: () => void
  operateur: number
  dsm: number
}) {
  const [edition, setEdition] = useState(false)
  const [texte, setTexte] = useState(c.texte)
  const [interne, setInterne] = useState(c.interne)
  const modification = useMutation({
    mutationFn: () =>
      apiDeclarations(operateur).modifierCommentaire(dsm, c.id, {
        texte,
        interne: peutInterne && !c.motif && interne,
      }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setEdition(false)
      onModifie()
    },
  })

  return (
    <li
      className={cn(
        'rounded-md border p-3',
        c.interne && 'border-dashed bg-amber-50/50 dark:bg-amber-950/20',
        enEvidence && 'ring-2 ring-amber-400'
      )}
    >
      <div className='flex flex-wrap items-center gap-2 text-sm'>
        <span className='font-medium'>{c.auteur}</span>
        <Badge variant='outline'>
          {c.cote === 'BNIA' ? 'BNIA' : 'Opérateur'}
        </Badge>
        <span className='text-muted-foreground'>
          {formatDateHeure(c.ecritLe)}
          {c.modifieLe && ' · modifié'}
        </span>
        {c.interne && <Badge variant='secondary'>Interne</Badge>}
        {c.motif && <Badge variant='destructive'>Motif des compléments</Badge>}
        {c.deLUtilisateur && !edition && (
          <span className='ms-auto flex gap-1'>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => {
                setTexte(c.texte)
                setInterne(c.interne)
                setEdition(true)
              }}
            >
              <Pencil className='size-4' /> Modifier
            </Button>
            {!c.motif && (
              <Button variant='ghost' size='sm' onClick={onSupprimer}>
                <Trash2 className='size-4' /> Supprimer
              </Button>
            )}
          </span>
        )}
      </div>
      {c.ligne != null && (
        <button
          type='button'
          className='text-primary mt-1 text-sm underline-offset-2 hover:underline'
          onClick={() => onMontrerLigne(c.ligne!)}
        >
          Ligne {libelleLigne(c.ligne)}
        </button>
      )}
      {c.ligneSupprimee && (
        <p className='text-muted-foreground mt-1 text-sm'>
          Ligne supprimée : {c.ligneSupprimee}
        </p>
      )}
      {edition ? (
        <div className='mt-2 space-y-2'>
          <Textarea
            rows={3}
            value={texte}
            onChange={(e) => setTexte(e.target.value)}
            aria-label='Commentaire'
          />
          <div className='flex flex-wrap items-center justify-end gap-3'>
            {peutInterne && !c.motif && (
              <CaseInterne
                id={`interne-${c.id}`}
                valeur={interne}
                onChange={setInterne}
              />
            )}
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
        <p className='mt-2 text-sm whitespace-pre-wrap'>{c.texte}</p>
      )}
    </li>
  )
}

/** RG-DEC-24 : un commentaire interne n'est jamais montré à l'opérateur. */
function CaseInterne({
  id,
  valeur,
  onChange,
}: {
  id: string
  valeur: boolean
  onChange: (valeur: boolean) => void
}) {
  return (
    <div className='flex items-center gap-2'>
      <Checkbox
        id={id}
        checked={valeur}
        onCheckedChange={(v) => onChange(v === true)}
      />
      <Label htmlFor={id} className='font-normal'>
        Interne au BNIA, invisible de l'opérateur
      </Label>
    </div>
  )
}
