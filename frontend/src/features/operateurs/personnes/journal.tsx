import { useQuery } from '@tanstack/react-query'
import { consulterJournalOperateur, type LigneJournal } from '@/api/generated'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { cleOperateur } from '../commun/api'
import { CIBLES_JOURNAL } from '../commun/libelles'

const dateHeure = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

/** Journal des actions sur les personnes et les accès d'un opérateur (RG-OPE-34). */
export function OngletJournal({ operateur }: { operateur: number }) {
  const journal = useQuery({
    queryKey: [...cleOperateur(operateur), 'journal'],
    queryFn: () =>
      consulterJournalOperateur({
        path: { id: operateur },
        throwOnError: true,
      }).then((r) => r.data),
  })
  return <TableJournal lignes={journal.data ?? []} avecCible />
}

/** Lignes du journal, de la plus récente à la plus ancienne, avec le détail des champs changés. */
export function TableJournal({
  lignes,
  avecCible = false,
}: {
  lignes: LigneJournal[]
  avecCible?: boolean
}) {
  return (
    <div className='rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Date</TableHead>
            <TableHead>Action</TableHead>
            {avecCible && <TableHead>Personne</TableHead>}
            <TableHead>Changements</TableHead>
            <TableHead>Par</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {lignes.length === 0 && (
            <TableRow>
              <TableCell
                colSpan={avecCible ? 5 : 4}
                className='text-muted-foreground h-16 text-center'
              >
                Aucune action.
              </TableCell>
            </TableRow>
          )}
          {lignes.map((l, i) => (
            <TableRow key={i}>
              <TableCell className='whitespace-nowrap'>
                {dateHeure.format(new Date(l.date))}
              </TableCell>
              <TableCell>{l.libelle}</TableCell>
              {avecCible && (
                <TableCell>
                  {l.nomCible}
                  <span className='text-muted-foreground'>
                    {' '}
                    · {CIBLES_JOURNAL[l.cible]}
                  </span>
                </TableCell>
              )}
              <TableCell className='text-sm'>
                {l.changements.map((c) => (
                  <div key={c.champ}>
                    {c.champ} : {c.avant ?? '—'} → {c.apres ?? '—'}
                  </div>
                ))}
              </TableCell>
              <TableCell>{l.acteur}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
