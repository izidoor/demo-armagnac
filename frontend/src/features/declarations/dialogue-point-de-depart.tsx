import { type FormEvent, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Plus, Trash2 } from 'lucide-react'
import {
  type PointDeDepart,
  type PropositionPointDeDepart,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { apiDeclarations, cleDeclarations } from './api'
import { ChampsIdentite, ListeDenominations } from './champs-identite'
import { formatDate, lireLitres, texteLitres } from './format'
import { depuisApi, type IdentiteSaisie, versApi } from './saisie-ligne'

type LigneSaisie = IdentiteSaisie & { volume: string }

/**
 * Le BNIA fixe ou modifie le point de départ d'un producteur ou d'un négociant (analyse UC-DEC-3,
 * RG-DEC-06 à 08) : date de début et stock initial, ligne par ligne ; une liste vide est permise. La DSM
 * en cours suit le stock initial modifié. Un nouveau point de départ (RG-DEC-09) part du stock proposé : le stock
 * de fin de la dernière DSM validée, vieilli à la date retenue, recalculé à la demande quand la date change.
 */
export function DialoguePointDeDepart({
  operateur,
  producteur,
  point,
  proposition,
  onOpenChange,
  onEnregistre,
}: {
  operateur: number
  producteur: boolean
  point: PointDeDepart | null
  /** RG-DEC-09 : un nouveau point de départ, à partir de ce stock proposé. */
  proposition?: PropositionPointDeDepart
  onOpenChange: (open: boolean) => void
  onEnregistre: () => void
}) {
  const [debut, setDebut] = useState(point?.debut ?? proposition?.debut ?? '')
  const enLignes = (source: PointDeDepart['lignes']): LigneSaisie[] =>
    source.map((l) => ({ ...depuisApi(l), volume: texteLitres(l.volume) }))
  const [lignes, setLignes] = useState<LigneSaisie[]>(
    enLignes(point?.lignes ?? proposition?.lignes ?? [])
  )
  const denominations = useQuery({
    queryKey: [...cleDeclarations(operateur), 'denominations'],
    queryFn: apiDeclarations(operateur).denominations,
  })
  const api = apiDeclarations(operateur)
  const enregistrement = useMutation({
    mutationFn: () => {
      const body = {
        debut,
        lignes: lignes.map((l) => ({
          ...versApi(l),
          volume: lireLitres(l.volume) ?? Number.NaN,
        })),
        version: point?.version,
      }
      return point ? api.modifier(point.id, body) : api.fixer(body)
    },
    meta: { erreurAffichee: true },
    onSuccess: () => {
      onOpenChange(false)
      onEnregistre()
    },
  })
  const recalcul = useMutation({
    mutationFn: () => api.proposition(debut),
    meta: { erreurAffichee: true },
    onSuccess: (p) => p && setLignes(enLignes(p.lignes)),
  })
  const volumesInvalides = lignes.some((l) => lireLitres(l.volume) === null)
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    enregistrement.mutate()
  }
  const erreur = enregistrement.error
    ? lireProbleme(enregistrement.error)
    : null
  const changer = (i: number, ligne: LigneSaisie) =>
    setLignes(lignes.map((l, j) => (j === i ? ligne : l)))

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>
            {proposition
              ? 'Fixer un nouveau point de départ'
              : point
                ? 'Modifier le point de départ'
                : 'Fixer le point de départ'}
          </DialogTitle>
          <DialogDescription>
            Début de la première période et stock initial, en litres d'alcool
            pur. Le début est{' '}
            {producteur
              ? 'un 1er avril pour un producteur'
              : 'le premier du mois pour un négociant'}
            .{' '}
            {proposition
              ? `Il commence après le ${formatDate(proposition.derniere.fin)}, fin de la dernière DSM validée (${proposition.derniere.libelle}) ; le précédent reste consultable et une interruption est inscrite au journal. Le stock proposé est le stock de fin de cette DSM, vieilli à la date de début.`
              : "Tant que la première DSM n'est pas validée, le point de départ se modifie et la DSM en cours suit le stock initial."}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={soumettre} className='space-y-4'>
          {erreur && (
            <Alert variant='destructive'>
              <AlertDescription>
                {erreur.detail ?? erreur.titre}
              </AlertDescription>
            </Alert>
          )}
          <div className='space-y-2'>
            <Label htmlFor='debut-point'>Début</Label>
            <div className='flex flex-wrap items-center gap-2'>
              <Input
                id='debut-point'
                type='date'
                className='w-44'
                value={debut}
                onChange={(e) => setDebut(e.target.value)}
              />
              {proposition && (
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  disabled={debut === '' || recalcul.isPending}
                  onClick={() => recalcul.mutate()}
                >
                  Reprendre le stock vieilli à cette date
                </Button>
              )}
            </div>
          </div>
          <div className='space-y-2'>
            <Label>Stock initial</Label>
            {lignes.length === 0 && (
              <p className='text-muted-foreground text-sm'>
                Aucune ligne : l'opérateur part d'un stock nul.
              </p>
            )}
            {lignes.map((l, i) => (
              <div key={i} className='flex flex-wrap items-center gap-2'>
                <ChampsIdentite
                  valeur={l}
                  onChange={(identite) => changer(i, { ...l, ...identite })}
                />
                <Input
                  className='w-32 text-right'
                  inputMode='decimal'
                  placeholder='Litres AP'
                  aria-label='Volume en litres AP'
                  aria-invalid={lireLitres(l.volume) === null}
                  value={l.volume}
                  onChange={(e) => changer(i, { ...l, volume: e.target.value })}
                />
                <Button
                  type='button'
                  variant='ghost'
                  size='icon'
                  aria-label='Retirer la ligne'
                  onClick={() => setLignes(lignes.filter((_, j) => j !== i))}
                >
                  <Trash2 className='size-4' />
                </Button>
              </div>
            ))}
            <Button
              type='button'
              variant='outline'
              size='sm'
              onClick={() =>
                setLignes([
                  ...lignes,
                  { cle: 'NB', designation: '', compte: '', volume: '' },
                ])
              }
            >
              <Plus className='size-4' /> Ajouter une ligne
            </Button>
            <ListeDenominations valeurs={denominations.data ?? []} />
          </div>
          <div className='flex justify-end'>
            <Button
              type='submit'
              disabled={
                enregistrement.isPending || debut === '' || volumesInvalides
              }
            >
              Enregistrer
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}
