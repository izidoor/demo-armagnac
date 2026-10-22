import { type EntreeJournalDeclarations } from '@/api/generated'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { CarteListe, CartesListe } from '@/components/metier/cartes-liste'
import { formatDateHeure } from './format'

/**
 * Journal des déclarations (RG-DEC-23), du plus récent au plus ancien : l'historique d'une DSM, ou toutes
 * les entrées d'un opérateur, brouillons supprimés compris (avec la colonne DSM).
 *
 * Le détail prend la place qui reste et se tronque (texte complet au survol) : une ligne par entrée. Le journal est
 * aussi montré dans un panneau latéral (l'historique d'une DSM), hors de la zone de contenu : son seuil porte donc sur
 * sa propre largeur (`@container/journal`). Sous 40rem avec la colonne DSM (616 px de large naturelle), sous 32rem
 * sans elle (491 px), une carte par entrée.
 */
export function TableJournalDeclarations({
  entrees,
  avecDsm = false,
}: {
  entrees: EntreeJournalDeclarations[]
  avecDsm?: boolean
}) {
  return (
    <div className='@container/journal'>
      <div
        className={
          avecDsm
            ? 'rounded-md border @max-[40rem]/journal:hidden'
            : 'rounded-md border @max-[32rem]/journal:hidden'
        }
      >
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Date</TableHead>
              <TableHead>Action</TableHead>
              {avecDsm && <TableHead>DSM</TableHead>}
              <TableHead>Détail</TableHead>
              <TableHead>Par</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {entrees.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={avecDsm ? 5 : 4}
                  className='text-muted-foreground h-16 text-center'
                >
                  Aucune action.
                </TableCell>
              </TableRow>
            )}
            {entrees.map((e, i) => (
              <TableRow key={i}>
                <TableCell>{formatDateHeure(e.date)}</TableCell>
                <TableCell>{e.libelle}</TableCell>
                {avecDsm && <TableCell>{e.periode ?? '—'}</TableCell>}
                <TableCell
                  className='w-full max-w-0 truncate'
                  title={e.detail ?? undefined}
                >
                  {e.detail ?? ''}
                </TableCell>
                <TableCell>{e.acteur}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <CartesListe
        requete={{ isPending: false, isError: false, data: entrees }}
        vide='Aucune action.'
        masquee={
          avecDsm
            ? '@min-[40rem]/journal:hidden'
            : '@min-[32rem]/journal:hidden'
        }
      >
        {entrees.map((e, i) => (
          <CarteListe key={i} titre={e.libelle}>
            {(avecDsm && e.periode) || e.detail ? (
              <div className='text-sm break-words'>
                {[avecDsm ? e.periode : null, e.detail]
                  .filter(Boolean)
                  .join(' · ')}
              </div>
            ) : null}
            <div className='text-muted-foreground text-sm'>
              {formatDateHeure(e.date)} · {e.acteur}
            </div>
          </CarteListe>
        ))}
      </CartesListe>
    </div>
  )
}
