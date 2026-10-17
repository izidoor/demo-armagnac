import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { createColumnHelper } from '@tanstack/react-table'
import { FileDown } from 'lucide-react'
import { consulterVentes, type LigneVendue } from '@/api/generated'
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
import { Separator } from '@/components/ui/separator'
import { EnteteFiltrable } from '@/components/metier/entete-filtrable'
import { filtreValeurs } from '@/components/metier/filtre-valeurs'
import { PageMetier } from '@/components/metier/page-metier'
import { TableDonnees } from '@/components/metier/table-donnees'
import { formatDate, formatEuros } from '@/features/declarations/format'
import { cleFacturation } from './api'
import { GraphiquesVentes } from './graphiques-ventes'

type Periode = { du: string; au: string }

const deux = (n: number) => String(n).padStart(2, '0')
const iso = (annee: number, mois: number, jour: number) =>
  `${annee}-${deux(mois)}-${deux(jour)}`
/** Dernier jour d'un mois (1 à 12). */
const finDeMois = (annee: number, mois: number) =>
  new Date(annee, mois, 0).getDate()

function trimestre(annee: number, numero: number): Periode {
  const debut = (numero - 1) * 3 + 1
  return {
    du: iso(annee, debut, 1),
    au: iso(annee, debut + 2, finDeMois(annee, debut + 2)),
  }
}

/** Les raccourcis de période, calculés au jour du navigateur. */
function raccourcis(): { id: string; libelle: string; periode: Periode }[] {
  const jour = new Date()
  const annee = jour.getFullYear()
  const t = Math.floor(jour.getMonth() / 3) + 1
  const precedent = t === 1 ? trimestre(annee - 1, 4) : trimestre(annee, t - 1)
  return [
    {
      id: 'trimestre',
      libelle: 'Trimestre en cours',
      periode: trimestre(annee, t),
    },
    {
      id: 'trimestre-precedent',
      libelle: 'Trimestre précédent',
      periode: precedent,
    },
    {
      id: 'annee',
      libelle: 'Année en cours',
      periode: { du: iso(annee, 1, 1), au: iso(annee, 12, 31) },
    },
    {
      id: 'annee-precedente',
      libelle: 'Année précédente',
      periode: { du: iso(annee - 1, 1, 1), au: iso(annee - 1, 12, 31) },
    },
  ]
}

const PERSONNALISEE = 'personnalisee'

/**
 * Ventes du back-office (spec 08 §6.1, UC-FAC-8, RG-FAC-30) : les lignes article des factures et avoirs émis dans
 * une période (date d'émission), avoirs déduits. La période, l'année civile en cours par défaut, vaut pour les
 * lignes vendues ; au-dessus, deux graphiques de démonstration. Export : la synthèse de la période en PDF.
 */
export function PageVentes() {
  const choix = raccourcis()
  const [periode, setPeriode] = useState<Periode>(
    choix.find((c) => c.id === 'annee')!.periode
  )
  const valide = !!periode.du && !!periode.au && periode.du <= periode.au
  const ventes = useQuery({
    queryKey: [...cleFacturation, 'ventes', periode],
    queryFn: () =>
      consulterVentes({ query: periode, throwOnError: true }).then(
        (r) => r.data
      ),
    enabled: valide,
  })
  const raccourci = choix.find(
    (c) => c.periode.du === periode.du && c.periode.au === periode.au
  )
  return (
    <PageMetier titre='Ventes' description='Historique des ventes et analyse'>
      <GraphiquesVentes />
      <Separator className='my-10' />
      <h2 className='mb-4 text-xl font-semibold'>Liste des ventes</h2>
      <div className='mb-4 flex flex-wrap items-end gap-3'>
        <div className='space-y-1'>
          <Label htmlFor='ventes-raccourci'>Période</Label>
          <Select
            value={raccourci?.id ?? PERSONNALISEE}
            onValueChange={(id) => {
              const c = choix.find((r) => r.id === id)
              if (c) setPeriode(c.periode)
            }}
          >
            <SelectTrigger id='ventes-raccourci' className='w-48'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {choix.map((c) => (
                <SelectItem key={c.id} value={c.id}>
                  {c.libelle}
                </SelectItem>
              ))}
              {!raccourci && (
                <SelectItem value={PERSONNALISEE} disabled>
                  Personnalisée
                </SelectItem>
              )}
            </SelectContent>
          </Select>
        </div>
        <div className='space-y-1'>
          <Label htmlFor='ventes-du'>Émises du</Label>
          <Input
            id='ventes-du'
            type='date'
            className='w-40'
            value={periode.du}
            onChange={(e) => setPeriode({ ...periode, du: e.target.value })}
          />
        </div>
        <div className='space-y-1'>
          <Label htmlFor='ventes-au'>au</Label>
          <Input
            id='ventes-au'
            type='date'
            className='w-40'
            value={periode.au}
            onChange={(e) => setPeriode({ ...periode, au: e.target.value })}
          />
        </div>
        {valide ? (
          <Button variant='outline' className='ms-auto' asChild>
            <a
              href={`/api/facturation/ventes/pdf?du=${periode.du}&au=${periode.au}`}
              download
            >
              <FileDown className='size-4' /> Exporter en PDF
            </a>
          </Button>
        ) : (
          <Button variant='outline' className='ms-auto' disabled>
            <FileDown className='size-4' /> Exporter en PDF
          </Button>
        )}
      </div>
      {!valide && (
        <p className='text-destructive mb-4 text-sm'>
          La période va d&apos;une date à une date postérieure ou égale.
        </p>
      )}
      <div className='mb-8'>
        <DonneesVentes
          lignes={ventes.data?.lignes ?? []}
          chargement={valide && ventes.isPending}
        />
      </div>
    </PageMetier>
  )
}

const nombre = new Intl.NumberFormat('fr-FR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 4,
})

const colonne = createColumnHelper<LigneVendue>()

const COLONNES = [
  colonne.accessor('dateEmission', {
    meta: { titre: 'Émise le' },
    header: ({ column }) => (
      <EnteteFiltrable column={column} affichage={formatDate} />
    ),
    cell: ({ getValue }) => formatDate(getValue()),
    filterFn: filtreValeurs,
  }),
  colonne.accessor('numero', {
    meta: { titre: 'N°' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ row }) => (
      <span className='flex items-center gap-1'>
        <span className='font-mono'>{row.original.numero}</span>
        {row.original.avoir && <Badge variant='outline'>Avoir</Badge>}
      </span>
    ),
    filterFn: filtreValeurs,
  }),
  colonne.accessor(
    (l) => (l.clientCode ? `${l.clientCode} · ${l.acheteur}` : l.acheteur),
    {
      id: 'acheteur',
      meta: { titre: 'Acheteur' },
      header: ({ column }) => <EnteteFiltrable column={column} />,
      filterFn: filtreValeurs,
    }
  ),
  colonne.accessor((l) => (l.origine ? l.origine.reference : 'Avoir'), {
    id: 'origine',
    meta: { titre: 'Objet' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor('code', {
    meta: { titre: 'Code article' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ getValue }) => <span className='font-mono'>{getValue()}</span>,
    filterFn: filtreValeurs,
  }),
  colonne.accessor((l) => l.designation, {
    id: 'article',
    meta: { titre: 'Article' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor('tvaLibelle', {
    meta: { titre: 'TVA' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor('quantite', {
    meta: { titre: 'Quantité' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ row }) =>
      `${nombre.format(row.original.quantite)}${row.original.unite ? ` ${row.original.unite}` : ''}`,
    enableColumnFilter: false,
    enableGrouping: false,
    // RG-FAC-30 : une quantité ne se somme que dans un même article ; un groupe qui en mêle plusieurs n'en a pas.
    aggregationFn: (_colonne, feuilles) =>
      new Set(feuilles.map((r) => r.original.code)).size === 1
        ? feuilles.reduce((s, r) => s + r.original.quantite, 0)
        : undefined,
    aggregatedCell: ({ row, getValue }) => {
      const v = getValue<number | undefined>()
      const unite = row.getLeafRows()[0]?.original.unite
      return v === undefined
        ? null
        : `${nombre.format(v)}${unite ? ` ${unite}` : ''}`
    },
  }),
  colonne.accessor('prixUnitaire', {
    meta: { titre: 'Prix unitaire HT' },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ getValue }) => `${nombre.format(getValue())} €`,
    enableColumnFilter: false,
    enableGrouping: false,
  }),
  colonne.accessor('montantHt', {
    meta: { titre: 'Montant HT', format: formatEuros },
    header: ({ column }) => <EnteteFiltrable column={column} />,
    cell: ({ getValue }) => formatEuros(getValue()),
    enableColumnFilter: false,
    enableGrouping: false,
    aggregationFn: 'sum',
  }),
]

/**
 * *Données* : les lignes vendues de la période, triables et filtrables par colonne, total HT des lignes
 * affichées ; une ligne ouvre sa pièce.
 */
function DonneesVentes({
  lignes,
  chargement,
}: {
  lignes: LigneVendue[]
  chargement: boolean
}) {
  const navigate = useNavigate()
  return (
    <TableDonnees
      cle='ventes'
      donnees={lignes}
      colonnes={COLONNES}
      chiffrees={['quantite', 'prixUnitaire', 'montantHt']}
      masquees={['code']}
      chargement={chargement}
      vide='Aucune facture ni aucun avoir émis dans la période.'
      total={(l) => l.montantHt}
      formatTotal={formatEuros}
      onLigne={(l) =>
        navigate({ to: '/bo/facturation/$id', params: { id: String(l.piece) } })
      }
    />
  )
}
