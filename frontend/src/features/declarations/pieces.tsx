import { type FormEvent, useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { FileText, Trash2, Upload } from 'lucide-react'
import { toast } from 'sonner'
import {
  type Dsm,
  type JustificatifDsm,
  type MoisDrm,
  type PiecesDsm,
  type TypeMouvementDsm,
} from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { apiDeclarations } from './api'
import { formatDateHeure } from './format'
import { identite } from './libelles'

const SUR_LA_DSM = 'dsm'

type TypeMouvement = TypeMouvementDsm['type']
type TypePiece = JustificatifDsm['typePiece']

/** Ce que l'on supprime, après confirmation. */
type Suppression =
  | { genre: 'justificatif'; piece: JustificatifDsm }
  | { genre: 'drm'; mois: MoisDrm }

/**
 * Onglet « Pièces » d'une DSM (spec 05 §6.3) : justificatifs groupés par type de mouvement (RG-DEC-26) et,
 * pour un producteur, une DRM par mois de la période (RG-DEC-27). Dépôt en PDF, ouverture dans un nouvel
 * onglet, suppression ; l'opérateur quand la DSM est chez lui, le BNIA tant qu'elle n'est pas validée
 * (RG-DEC-28). Les manques bloquent la validation, pas l'envoi (RG-DEC-29).
 */
export function OngletPieces({
  dsm,
  pieces,
  peutDeposer,
  onChange,
}: {
  dsm: Dsm
  pieces: PiecesDsm | undefined
  peutDeposer: boolean
  onChange: () => void
}) {
  const api = apiDeclarations(dsm.operateur)
  const [depot, setDepot] = useState<TypeMouvement | null>(null)
  const [suppression, setSuppression] = useState<Suppression | null>(null)

  const effacement = useMutation({
    mutationFn: (s: Suppression) =>
      s.genre === 'justificatif'
        ? api.supprimerJustificatif(dsm.id, s.piece.id)
        : api.supprimerDrm(dsm.id, s.mois.mois),
    meta: { erreurAffichee: true },
    onSuccess: (_, s) => {
      setSuppression(null)
      toast.success(
        s.genre === 'justificatif'
          ? `${s.piece.libelleTypePiece} « ${s.piece.document.nom} » supprimé.`
          : `DRM de ${s.mois.libelle} supprimée.`
      )
      onChange()
    },
    onError: () => setSuppression(null),
  })

  if (!pieces) {
    return <p className='text-muted-foreground text-sm'>Chargement…</p>
  }
  const manquants = new Set(
    pieces.manques.flatMap((m) => (m.typeMouvement ? [m.typeMouvement] : []))
  )
  const libelleLigne = (id: number) => {
    const l = dsm.lignes.find((x) => x.id === id)
    return l ? identite(l) : `n° ${id}`
  }

  return (
    <div className='space-y-8'>
      <section className='space-y-4'>
        <div className='flex flex-wrap items-end justify-between gap-2'>
          <div className='space-y-1'>
            <h3 className='text-lg font-semibold'>Justificatifs</h3>
            <p className='text-muted-foreground text-sm'>
              Dès qu'un type de mouvement a un total non nul, au moins une pièce
              de ce type, sur la DSM ou sur une de ses lignes, est attendue
              avant la validation. Le Floc de Gascogne (colonne 15) n'en appelle
              pas.
            </p>
          </div>
          {peutDeposer && (
            <Button onClick={() => setDepot(pieces.typesMouvement[0].type)}>
              <Upload className='size-4' /> Déposer un justificatif
            </Button>
          )}
        </div>
        {pieces.typesMouvement.map((t) => (
          <GroupeJustificatifs
            key={t.type}
            type={t}
            attendu={manquants.has(t.type)}
            pieces={pieces.justificatifs.filter(
              (j) => j.typeMouvement === t.type
            )}
            url={(j) => api.urlJustificatif(dsm.id, j.id)}
            libelleLigne={libelleLigne}
            peutDeposer={peutDeposer}
            onDeposer={() => setDepot(t.type)}
            onSupprimer={(piece) =>
              setSuppression({ genre: 'justificatif', piece })
            }
          />
        ))}
      </section>

      {pieces.mois.length > 0 && (
        <section className='space-y-4'>
          <div className='space-y-1'>
            <h3 className='text-lg font-semibold'>DRM</h3>
            <p className='text-muted-foreground text-sm'>
              Une DRM en PDF par mois de la période. Pour la remplacer,
              supprimez-la puis déposez la nouvelle.
            </p>
          </div>
          <div className='grid gap-2 sm:grid-cols-2 lg:grid-cols-3'>
            {pieces.mois.map((m) => (
              <MoisDeDrm
                key={m.mois}
                mois={m}
                url={api.urlDrm(dsm.id, m.mois)}
                peutDeposer={peutDeposer}
                onDeposer={(fichier) =>
                  api.deposerDrm(dsm.id, m.mois, fichier).then(() => {
                    toast.success(`DRM de ${m.libelle} déposée.`)
                    onChange()
                  })
                }
                onSupprimer={() => setSuppression({ genre: 'drm', mois: m })}
              />
            ))}
          </div>
        </section>
      )}

      {depot && (
        <DialogueJustificatif
          dsm={dsm}
          types={pieces.typesMouvement}
          typeInitial={depot}
          onOpenChange={(open) => !open && setDepot(null)}
          onDepose={() => {
            setDepot(null)
            onChange()
          }}
        />
      )}
      <ConfirmDialog
        open={suppression != null}
        onOpenChange={(open) => !open && setSuppression(null)}
        title={
          suppression?.genre === 'drm'
            ? 'Supprimer la DRM'
            : 'Supprimer le justificatif'
        }
        desc={
          suppression?.genre === 'drm'
            ? `La DRM de ${suppression.mois.libelle} sera supprimée ; le mois redeviendra attendu. La suppression reste tracée à l'historique de la DSM.`
            : `Le fichier « ${suppression?.piece.document.nom ?? ''} » sera supprimé. La suppression reste tracée à l'historique de la DSM.`
        }
        confirmText='Supprimer'
        cancelBtnText='Annuler'
        destructive
        isLoading={effacement.isPending}
        handleConfirm={() => suppression && effacement.mutate(suppression)}
      />
    </div>
  )
}

function GroupeJustificatifs({
  type,
  attendu,
  pieces,
  url,
  libelleLigne,
  peutDeposer,
  onDeposer,
  onSupprimer,
}: {
  type: TypeMouvementDsm
  attendu: boolean
  pieces: JustificatifDsm[]
  url: (j: JustificatifDsm) => string
  libelleLigne: (id: number) => string
  peutDeposer: boolean
  onDeposer: () => void
  onSupprimer: (j: JustificatifDsm) => void
}) {
  return (
    <div className='rounded-md border'>
      <div className='flex flex-wrap items-center justify-between gap-2 border-b px-4 py-2'>
        <div className='flex flex-wrap items-center gap-2'>
          <span className='font-medium'>{type.libelle}</span>
          <span className='text-muted-foreground text-sm'>
            colonnes {type.colonnes.join(', ')} ·{' '}
            {type.pieces.map((p) => p.libelle).join(' ou ')}
          </span>
          {attendu && <Badge variant='destructive'>Pièce attendue</Badge>}
        </div>
        {peutDeposer && (
          <Button variant='ghost' size='sm' onClick={onDeposer}>
            <Upload className='size-4' /> Déposer
          </Button>
        )}
      </div>
      {pieces.length === 0 ? (
        <p className='text-muted-foreground px-4 py-3 text-sm'>
          Aucune pièce n'a été déposée.
        </p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Pièce</TableHead>
              <TableHead>Fichier</TableHead>
              <TableHead>Référence DAE/DAI</TableHead>
              <TableHead>Porte sur</TableHead>
              <TableHead>Déposée</TableHead>
              <TableHead className='w-0' />
            </TableRow>
          </TableHeader>
          <TableBody>
            {pieces.map((j) => (
              <TableRow key={j.id}>
                <TableCell>{j.libelleTypePiece}</TableCell>
                <TableCell>
                  <LienPdf url={url(j)} nom={j.document.nom} />
                </TableCell>
                <TableCell>{j.reference ?? '—'}</TableCell>
                <TableCell>
                  {j.ligne != null ? (
                    `Ligne ${libelleLigne(j.ligne)}`
                  ) : j.ligneSupprimee ? (
                    <span>
                      La DSM{' '}
                      <Badge variant='outline'>
                        ligne supprimée : {j.ligneSupprimee}
                      </Badge>
                    </span>
                  ) : (
                    'La DSM'
                  )}
                </TableCell>
                <TableCell className='text-muted-foreground text-sm'>
                  {formatDateHeure(j.document.deposeLe)} par{' '}
                  {j.document.deposePar}
                </TableCell>
                <TableCell className='py-1'>
                  {peutDeposer && (
                    <Button
                      variant='ghost'
                      size='icon'
                      aria-label={`Supprimer ${j.document.nom}`}
                      onClick={() => onSupprimer(j)}
                    >
                      <Trash2 className='size-4' />
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  )
}

/** Un mois de la période : sa DRM déposée, ou attendue. */
function MoisDeDrm({
  mois,
  url,
  peutDeposer,
  onDeposer,
  onSupprimer,
}: {
  mois: MoisDrm
  url: string
  peutDeposer: boolean
  onDeposer: (fichier: File) => Promise<void>
  onSupprimer: () => void
}) {
  const champ = useRef<HTMLInputElement>(null)
  const depot = useMutation({
    mutationFn: onDeposer,
    meta: { erreurAffichee: true },
  })
  return (
    <div className='flex items-center justify-between gap-2 rounded-md border px-3 py-2'>
      <div className='min-w-0 space-y-0.5'>
        <div className='flex items-center gap-2'>
          <span className='font-medium first-letter:uppercase'>
            {mois.libelle}
          </span>
          {mois.drm ? (
            <Badge variant='secondary'>Déposée</Badge>
          ) : (
            <Badge variant='destructive'>Attendue</Badge>
          )}
        </div>
        {mois.drm && (
          <div className='truncate text-sm'>
            <LienPdf url={url} nom={mois.drm.nom} />
          </div>
        )}
      </div>
      {peutDeposer &&
        (mois.drm ? (
          <Button
            variant='ghost'
            size='icon'
            aria-label={`Supprimer la DRM de ${mois.libelle}`}
            onClick={onSupprimer}
          >
            <Trash2 className='size-4' />
          </Button>
        ) : (
          <>
            <input
              ref={champ}
              type='file'
              accept='application/pdf'
              className='hidden'
              onChange={(e) => {
                const fichier = e.target.files?.[0]
                e.target.value = ''
                if (fichier) depot.mutate(fichier)
              }}
            />
            <Button
              variant='outline'
              size='sm'
              disabled={depot.isPending}
              onClick={() => champ.current?.click()}
            >
              <Upload className='size-4' /> Déposer
            </Button>
          </>
        ))}
    </div>
  )
}

/** Le PDF s'ouvre dans un nouvel onglet. */
function LienPdf({ url, nom }: { url: string; nom: string }) {
  return (
    <a
      href={url}
      target='_blank'
      rel='noreferrer'
      className='inline-flex items-center gap-1 underline-offset-2 hover:underline'
    >
      <FileText className='size-4 shrink-0' />
      <span className='truncate'>{nom}</span>
    </a>
  )
}

/**
 * Déposer un justificatif (RG-DEC-26) : le type de mouvement, puis un type de pièce compatible, la ligne
 * visée ou la DSM, la référence DAE/DAI facultative et le PDF.
 */
function DialogueJustificatif({
  dsm,
  types,
  typeInitial,
  onOpenChange,
  onDepose,
}: {
  dsm: Dsm
  types: TypeMouvementDsm[]
  typeInitial: TypeMouvement
  onOpenChange: (open: boolean) => void
  onDepose: () => void
}) {
  const api = apiDeclarations(dsm.operateur)
  const [typeMouvement, setTypeMouvement] = useState(typeInitial)
  const compatibles = types.find((t) => t.type === typeMouvement)?.pieces ?? []
  const [typePiece, setTypePiece] = useState<TypePiece>(compatibles[0].type)
  const [ligne, setLigne] = useState(SUR_LA_DSM)
  const [reference, setReference] = useState('')
  const [fichier, setFichier] = useState<File | null>(null)

  const depot = useMutation({
    mutationFn: (f: File) =>
      api.deposerJustificatif(dsm.id, f, {
        typeMouvement,
        typePiece,
        ligne: ligne === SUR_LA_DSM ? undefined : Number(ligne),
        reference: reference.trim() || undefined,
      }),
    meta: { erreurAffichee: true },
    onSuccess: (j) => {
      toast.success(`${j.libelleTypePiece} « ${j.document.nom} » déposé.`)
      onDepose()
    },
  })
  const choisirMouvement = (t: TypeMouvement) => {
    setTypeMouvement(t)
    const pieces = types.find((x) => x.type === t)?.pieces ?? []
    if (!pieces.some((p) => p.type === typePiece)) setTypePiece(pieces[0].type)
  }
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    if (fichier) depot.mutate(fichier)
  }

  return (
    <DialogueFormulaire
      ouvert
      onOuvertChange={onOpenChange}
      className='sm:max-w-xl'
      titre='Déposer un justificatif'
      description='Un PDF de 10 Mo au plus, sur la DSM ou sur une de ses lignes.'
    >
      <form onSubmit={soumettre} className='space-y-4'>
        <div className='grid gap-4 sm:grid-cols-2'>
          <div className='space-y-1'>
            <Label htmlFor='piece-mouvement'>Type de mouvement</Label>
            <Select
              value={typeMouvement}
              onValueChange={(v) => choisirMouvement(v as TypeMouvement)}
            >
              <SelectTrigger id='piece-mouvement' className='w-full'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {types.map((t) => (
                  <SelectItem key={t.type} value={t.type}>
                    {t.libelle}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className='space-y-1'>
            <Label htmlFor='piece-type'>Type de pièce</Label>
            <Select
              value={typePiece}
              onValueChange={(v) => setTypePiece(v as TypePiece)}
            >
              <SelectTrigger id='piece-type' className='w-full'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {compatibles.map((p) => (
                  <SelectItem key={p.type} value={p.type}>
                    {p.libelle}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className='space-y-1'>
            <Label htmlFor='piece-ligne'>Porte sur</Label>
            <Select value={ligne} onValueChange={setLigne}>
              <SelectTrigger id='piece-ligne' className='w-full'>
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
            <Label htmlFor='piece-reference'>Référence DAE/DAI</Label>
            <Input
              id='piece-reference'
              value={reference}
              onChange={(e) => setReference(e.target.value)}
              placeholder='Facultative'
            />
          </div>
        </div>
        <div className='space-y-1'>
          <Label htmlFor='piece-fichier'>Fichier PDF</Label>
          <Input
            id='piece-fichier'
            type='file'
            accept='application/pdf'
            onChange={(e) => setFichier(e.target.files?.[0] ?? null)}
          />
        </div>
        <div className='flex justify-end'>
          <Button
            type='submit'
            className='max-md:w-full'
            disabled={!fichier || depot.isPending}
          >
            Déposer
          </Button>
        </div>
      </form>
    </DialogueFormulaire>
  )
}
