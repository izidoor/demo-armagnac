import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { listerConnexions, type LigneConnexion } from '@/api/generated'
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
import { PageMetier } from '@/components/metier/page-metier'

type Evenement = LigneConnexion['evenement']

/** Événements du journal des connexions (RG-AUT-12). */
const EVENEMENTS: Record<Evenement, string> = {
  CONNEXION_REUSSIE: 'Connexion réussie',
  ECHEC_MOT_DE_PASSE: 'Échec : mot de passe',
  ECHEC_CODE: 'Échec : code',
  BLOCAGE_DECLENCHE: 'Blocage temporaire déclenché',
  CONNEXION_REFUSEE: 'Connexion refusée',
  DECONNEXION: 'Déconnexion',
  MOT_DE_PASSE_DEFINI: 'Premier mot de passe',
  MOT_DE_PASSE_CHANGE: 'Mot de passe changé',
  REINITIALISATION_DEMANDEE: 'Réinitialisation demandée',
  MOT_DE_PASSE_REINITIALISE: 'Mot de passe réinitialisé',
  NAVIGATEUR_DE_CONFIANCE: 'Navigateur de confiance',
}

const TOUS = 'tous'
const dateHeure = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'short',
  timeStyle: 'medium',
})

/** Journal des connexions (spec 03 §6.2, RG-AUT-13), filtré par événement et période. */
export function PageConnexions() {
  const [evenement, setEvenement] = useState(TOUS)
  const [du, setDu] = useState('')
  const [au, setAu] = useState('')
  const journal = useQuery({
    queryKey: ['connexions', evenement, du, au],
    queryFn: () =>
      listerConnexions({
        query: {
          evenement: evenement === TOUS ? undefined : (evenement as Evenement),
          du: du || undefined,
          au: au || undefined,
        },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const lignes = journal.data ?? []
  return (
    <PageMetier
      titre='Journal des connexions'
      description='Connexions, échecs, blocages et mots de passe, du plus récent au plus ancien.'
    >
      <div className='mb-4 flex flex-wrap items-end gap-4'>
        <div className='space-y-2'>
          <Label>Événement</Label>
          <Select value={evenement} onValueChange={setEvenement}>
            <SelectTrigger className='w-64'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={TOUS}>Tous</SelectItem>
              {(Object.keys(EVENEMENTS) as Evenement[]).map((e) => (
                <SelectItem key={e} value={e}>
                  {EVENEMENTS[e]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className='space-y-2'>
          <Label htmlFor='du'>Du</Label>
          <Input
            id='du'
            type='date'
            value={du}
            onChange={(e) => setDu(e.target.value)}
          />
        </div>
        <div className='space-y-2'>
          <Label htmlFor='au'>Au</Label>
          <Input
            id='au'
            type='date'
            value={au}
            onChange={(e) => setAu(e.target.value)}
          />
        </div>
      </div>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Date</TableHead>
              <TableHead>Identifiant saisi</TableHead>
              <TableHead>Événement</TableHead>
              <TableHead>Détail</TableHead>
              <TableHead>Adresse IP</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {lignes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={5}
                  className='text-muted-foreground h-16 text-center'
                >
                  {journal.isPending ? 'Chargement…' : 'Aucun événement.'}
                </TableCell>
              </TableRow>
            )}
            {lignes.map((l, i) => (
              <TableRow key={i}>
                <TableCell className='whitespace-nowrap'>
                  {dateHeure.format(new Date(l.date))}
                </TableCell>
                <TableCell>{l.identifiantSaisi}</TableCell>
                <TableCell>{l.libelle}</TableCell>
                <TableCell>{l.detail}</TableCell>
                <TableCell>{l.adresseIp}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </PageMetier>
  )
}
