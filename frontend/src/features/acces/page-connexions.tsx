import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { listerConnexions, type LigneConnexion } from '@/api/generated'
import { cn } from '@/lib/utils'
import { Input } from '@/components/ui/input'
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
import { CarteListe, CartesListe } from '@/components/metier/cartes-liste'
import { PageMetier } from '@/components/metier/page-metier'
import { Pagination } from '@/components/metier/pagination'
import { usePagination } from '@/components/metier/use-pagination'

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
  DOUBLE_AUTHENTIFICATION_ACTIVEE: 'Double authentification activée',
  DOUBLE_AUTHENTIFICATION_DESACTIVEE: 'Double authentification désactivée',
}

const TOUS = 'tous'
const dateHeure = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'short',
  timeStyle: 'medium',
})

/**
 * Journal des connexions (spec 03 §6.2, RG-AUT-13), filtré par événement et période, par pages de 25.
 *
 * Selon la largeur du contenu, comme la liste des opérateurs (34 px de marges) : le détail prend la place qui reste et
 * se tronque (8rem au moins) ; le tableau fait alors 754 px ; sous 50rem, une carte par événement (`SEUILS`).
 */
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
  const { lignes, pagination } = usePagination(journal.data, 25)
  return (
    <PageMetier
      titre='Journal des connexions'
      description='Connexions, échecs, blocages et mots de passe, du plus récent au plus ancien.'
    >
      <div className='mb-4 flex flex-wrap items-center gap-2'>
        <Select value={evenement} onValueChange={setEvenement}>
          <SelectTrigger
            aria-label='Événement'
            className='h-9 w-64 max-sm:w-full'
          >
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TOUS}>Tous les événements</SelectItem>
            {(Object.keys(EVENEMENTS) as Evenement[]).map((e) => (
              <SelectItem key={e} value={e}>
                {EVENEMENTS[e]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <div className='flex items-center gap-2 text-sm max-sm:w-full'>
          <span className='text-muted-foreground shrink-0'>Du</span>
          <Input
            type='date'
            aria-label='Du'
            className='h-9 w-40 max-sm:min-w-0 max-sm:flex-1'
            value={du}
            onChange={(e) => setDu(e.target.value)}
          />
          <span className='text-muted-foreground shrink-0'>au</span>
          <Input
            type='date'
            aria-label='Au'
            className='h-9 w-40 max-sm:min-w-0 max-sm:flex-1'
            value={au}
            onChange={(e) => setAu(e.target.value)}
          />
        </div>
      </div>
      <div className={cn('rounded-md border', SEUILS.tableau)}>
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
            {lignes?.length === 0 || !lignes ? (
              <TableRow>
                <TableCell
                  colSpan={5}
                  className='text-muted-foreground h-16 text-center'
                >
                  {journal.isPending ? 'Chargement…' : 'Aucun événement.'}
                </TableCell>
              </TableRow>
            ) : null}
            {(lignes ?? []).map((l, i) => (
              <TableRow key={i}>
                <TableCell className='whitespace-nowrap'>
                  {dateHeure.format(new Date(l.date))}
                </TableCell>
                <TableCell>{l.identifiantSaisi}</TableCell>
                <TableCell>{l.libelle}</TableCell>
                <TableCell
                  className='w-full max-w-0 min-w-32 truncate'
                  title={l.detail ?? undefined}
                >
                  {l.detail}
                </TableCell>
                <TableCell>{l.adresseIp}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <CartesListe
        requete={journal}
        vide='Aucun événement.'
        masquee={SEUILS.cartes}
      >
        {(lignes ?? []).map((l, i) => (
          <CarteListe key={i} titre={l.libelle}>
            {l.identifiantSaisi && (
              <div className='truncate text-sm'>{l.identifiantSaisi}</div>
            )}
            {l.detail && (
              <div className='text-muted-foreground text-sm break-words'>
                {l.detail}
              </div>
            )}
            <div className='text-muted-foreground text-sm'>
              {[dateHeure.format(new Date(l.date)), l.adresseIp]
                .filter(Boolean)
                .join(' · ')}
            </div>
          </CarteListe>
        ))}
      </CartesListe>
      <Pagination {...pagination} />
    </PageMetier>
  )
}

const SEUILS = {
  tableau: '@max-[50rem]/content:hidden',
  cartes: '@min-[50rem]/content:hidden',
}
