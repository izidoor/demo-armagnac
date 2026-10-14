import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { FileDown, X } from 'lucide-react'
import {
  listerStocksDeclares,
  type LigneStockDeclare,
  type ResultatRechercheOperateur,
} from '@/api/generated'
import { Button } from '@/components/ui/button'
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
import {
  Table,
  TableBody,
  TableCell,
  TableFooter,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { PageMetier } from '@/components/metier/page-metier'
import { RechercheOperateur } from '@/features/operateurs/portail/recherche-operateur'
import { formatDate, formatLitres } from './format'
import { millesimee, TYPES_DECLARANT } from './libelles'

const TOUS = 'tous'

type TypeDeclarant = LigneStockDeclare['type']

/**
 * Stocks déclarés de tous les opérateurs, pour le BNIA (analyse UC-DEC-1, spec 05 §6.1) : le stock de fin de la
 * dernière DSM validée de chacun, ligne par ligne, lignes nulles omises (RG-DEC-42) ; filtres opérateur et
 * type, total ; export Excel pour les douanes, aux mêmes filtres. Une ligne ouvre la DSM dont elle est tirée.
 */
export function PageStocksDeclares() {
  const navigate = useNavigate()
  const [operateur, setOperateur] = useState<ResultatRechercheOperateur | null>(
    null
  )
  const [recherche, setRecherche] = useState(false)
  const [type, setType] = useState<string>(TOUS)
  const query = {
    operateur: operateur?.id,
    type: type === TOUS ? undefined : (type as TypeDeclarant),
  }
  const stocks = useQuery({
    queryKey: ['declarations', 'stocks', query],
    queryFn: () =>
      listerStocksDeclares({ query, throwOnError: true }).then((r) => r.data),
  })
  const lignes = stocks.data ?? []
  const total = lignes.reduce((s, l) => s + l.stock, 0)
  const parametres = new URLSearchParams()
  if (query.operateur) parametres.set('operateur', String(query.operateur))
  if (query.type) parametres.set('type', query.type)

  return (
    <PageMetier
      titre='Stocks déclarés'
      description='Le stock de fin de la dernière DSM validée de chaque opérateur.'
      actions={
        <Button variant='outline' asChild>
          <a href={`/api/declarations/stocks/excel?${parametres}`} download>
            <FileDown className='size-4' /> Exporter en Excel
          </a>
        </Button>
      }
    >
      <div className='mb-4 flex flex-wrap items-end gap-4'>
        <div className='space-y-1'>
          <Label>Opérateur</Label>
          {operateur ? (
            <div className='flex h-9 items-center gap-1 rounded-md border px-3 text-sm'>
              <span className='font-mono'>{operateur.code}</span> ·{' '}
              {operateur.raisonSociale}
              <Button
                variant='ghost'
                size='icon'
                className='size-6'
                aria-label="Retirer le filtre sur l'opérateur"
                onClick={() => setOperateur(null)}
              >
                <X className='size-4' />
              </Button>
            </div>
          ) : (
            <Popover open={recherche} onOpenChange={setRecherche}>
              <PopoverTrigger asChild>
                <Button variant='outline' className='w-56 justify-start'>
                  Tous les opérateurs
                </Button>
              </PopoverTrigger>
              <PopoverContent align='start' className='w-96'>
                <RechercheOperateur
                  onChoisir={(o) => {
                    setOperateur(o)
                    setRecherche(false)
                  }}
                />
              </PopoverContent>
            </Popover>
          )}
        </div>
        <div className='space-y-1'>
          <Label htmlFor='filtre-type-stock'>Type</Label>
          <Select value={type} onValueChange={setType}>
            <SelectTrigger id='filtre-type-stock' className='w-40'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={TOUS}>Tous</SelectItem>
              {Object.entries(TYPES_DECLARANT).map(([valeur, libelle]) => (
                <SelectItem key={valeur} value={valeur}>
                  {libelle}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Opérateur</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Au</TableHead>
              <TableHead>Millésime ou dénomination</TableHead>
              <TableHead>Clé</TableHead>
              <TableHead>Compte d'âge</TableHead>
              <TableHead className='text-right'>Stock (L AP)</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {lignes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={7}
                  className='text-muted-foreground h-16 text-center'
                >
                  {stocks.isPending ? 'Chargement…' : 'Aucun stock déclaré.'}
                </TableCell>
              </TableRow>
            )}
            {lignes.map((l, i) => (
              <TableRow
                key={`${l.dsm}-${i}`}
                className='cursor-pointer'
                onClick={() =>
                  navigate({
                    to: '/bo/operateurs/$id/dsm/$dsm',
                    params: { id: String(l.operateur), dsm: String(l.dsm) },
                  })
                }
              >
                <TableCell>
                  <span className='font-mono'>{l.codeOperateur}</span> ·{' '}
                  {l.raisonSociale}
                </TableCell>
                <TableCell>{TYPES_DECLARANT[l.type]}</TableCell>
                <TableCell>{formatDate(l.au)}</TableCell>
                <TableCell>
                  {millesimee(l.cle) ? l.millesime : l.denomination}
                </TableCell>
                <TableCell className='font-mono'>{l.cle}</TableCell>
                <TableCell>{l.compte}</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatLitres(l.stock)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
          {lignes.length > 0 && (
            <TableFooter>
              <TableRow>
                <TableCell colSpan={6}>Total</TableCell>
                <TableCell className='text-right tabular-nums'>
                  {formatLitres(total)}
                </TableCell>
              </TableRow>
            </TableFooter>
          )}
        </Table>
      </div>
    </PageMetier>
  )
}
