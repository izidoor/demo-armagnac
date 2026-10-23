import { useNavigate } from '@tanstack/react-router'
import { type ContratListe } from '@/api/generated'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { BadgePlein } from '@/components/metier/badge-statut'
import { CarteListe, CartesListe } from '@/components/metier/cartes-liste'
import { CelluleOuvrir, EnteteOuvrir } from '@/components/metier/cellule-ouvrir'
import { Pagination } from '@/components/metier/pagination'
import { TAILLE_PAGE, usePagination } from '@/components/metier/use-pagination'
import { formatDateHeure, formatEuros } from '@/features/declarations/format'
import { type Espace } from './api'
import { formatHlAp } from './format'
import { ETATS, PARTIES } from './libelles'

/**
 * Une liste de contrats (RG-CA-25), du plus récemment modifié au plus ancien ; une ligne ouvre le contrat. Au portail
 * (`operateur`, l'opérateur actif), la colonne « Avec » nomme l'autre partie et son rôle ; au back-office, les deux
 * parties. Une ligne par contrat ; selon la largeur du contenu, le tableau perd la date puis le volume, et sous 40rem
 * une carte par contrat.
 */
export function ListeContrats({
  espace,
  operateur,
  contrats,
  chargement = false,
  vide = 'Aucun contrat.',
}: {
  espace: Espace
  operateur?: number
  contrats: ContratListe[]
  chargement?: boolean
  vide?: string
}) {
  const navigate = useNavigate()
  const ouvrir = (id: number) =>
    espace === 'bo'
      ? navigate({ to: '/bo/contrats/$id', params: { id: String(id) } })
      : navigate({ to: '/portail/contrats/$id', params: { id: String(id) } })
  const { lignes, pagination } = usePagination(contrats, TAILLE_PAGE)
  const parties = (c: ContratListe) => {
    if (operateur === undefined)
      return `${c.vendeur.raisonSociale} → ${c.acheteur.raisonSociale}`
    const autreEstVendeur = c.acheteur.id === operateur
    const autre = autreEstVendeur ? c.vendeur : c.acheteur
    return `${autre.raisonSociale} (${PARTIES[autreEstVendeur ? 'VENDEUR' : 'ACHETEUR'].toLowerCase()})`
  }
  const produit = (c: ContratListe) =>
    c.nombreLignes > 1
      ? `${c.produit} + ${c.nombreLignes - 1} ligne${c.nombreLignes > 2 ? 's' : ''}`
      : c.produit
  return (
    <>
      <div className='rounded-md border @max-[40rem]/content:hidden'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Numéro</TableHead>
              <TableHead>État</TableHead>
              <TableHead className='w-full'>
                {operateur === undefined ? 'Vendeur → acheteur' : 'Avec'}
              </TableHead>
              <TableHead>Produit</TableHead>
              <TableHead className='text-right @max-[52rem]/content:hidden'>
                Volume
              </TableHead>
              <TableHead className='text-right'>Montant HT</TableHead>
              <TableHead className='@max-[64rem]/content:hidden'>
                Modifié le
              </TableHead>
              <EnteteOuvrir />
            </TableRow>
          </TableHeader>
          <TableBody>
            {contrats.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={8}
                  className='text-muted-foreground h-16 text-center'
                >
                  {chargement ? 'Chargement…' : vide}
                </TableCell>
              </TableRow>
            )}
            {(lignes ?? []).map((c) => (
              <TableRow
                key={c.id}
                className='cursor-pointer'
                onClick={() => ouvrir(c.id)}
              >
                <TableCell className='font-mono font-medium whitespace-nowrap'>
                  {c.numero}
                </TableCell>
                <TableCell>
                  <BadgePlein statut={ETATS[c.etat].statut}>
                    {ETATS[c.etat].libelle}
                  </BadgePlein>
                </TableCell>
                <TableCell
                  className='w-full max-w-0 truncate'
                  title={parties(c)}
                >
                  {parties(c)}
                </TableCell>
                <TableCell className='whitespace-nowrap'>
                  {produit(c)}
                </TableCell>
                <TableCell className='text-right whitespace-nowrap tabular-nums @max-[52rem]/content:hidden'>
                  {formatHlAp(c.volumeAp)}
                </TableCell>
                <TableCell className='text-right whitespace-nowrap tabular-nums'>
                  {formatEuros(c.montantHt)}
                </TableCell>
                <TableCell className='whitespace-nowrap @max-[64rem]/content:hidden'>
                  {formatDateHeure(c.modifieLe)}
                </TableCell>
                <CelluleOuvrir
                  libelle={`Ouvrir le contrat ${c.numero}`}
                  onOuvrir={() => ouvrir(c.id)}
                />
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <CartesListe
        requete={{ isPending: chargement, isError: false, data: contrats }}
        vide={vide}
        masquee='@min-[40rem]/content:hidden'
      >
        {(lignes ?? []).map((c) => (
          <CarteListe
            key={c.id}
            onOuvrir={() => ouvrir(c.id)}
            titre={
              <span className='flex flex-wrap items-center gap-2'>
                <span className='font-mono'>{c.numero}</span>
                <BadgePlein statut={ETATS[c.etat].statut}>
                  {ETATS[c.etat].libelle}
                </BadgePlein>
              </span>
            }
          >
            <div className='truncate text-sm'>{parties(c)}</div>
            <div className='text-muted-foreground text-sm'>
              {[
                produit(c),
                formatHlAp(c.volumeAp),
                formatEuros(c.montantHt),
              ].join(' · ')}
            </div>
          </CarteListe>
        ))}
      </CartesListe>
      <Pagination {...pagination} />
    </>
  )
}
