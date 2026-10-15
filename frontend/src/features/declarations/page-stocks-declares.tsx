import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { createColumnHelper } from '@tanstack/react-table'
import { FileDown, X } from 'lucide-react'
import {
  historiqueStocksDeclares,
  listerStocksDeclares,
  type LigneStockDeclare,
  type ResultatRechercheOperateur,
} from '@/api/generated'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { EnteteFiltrable } from '@/components/metier/entete-filtrable'
import { filtreValeurs } from '@/components/metier/filtre-valeurs'
import { PageMetier } from '@/components/metier/page-metier'
import { TableDonnees } from '@/components/metier/table-donnees'
import { RechercheOperateur } from '@/features/operateurs/portail/recherche-operateur'
import { colonnesLigneStock } from './colonnes-stocks'
import { formatDate, formatLitres } from './format'
import { TYPES_DECLARANT } from './libelles'
import { ChoixAnnee, TableauStocks } from './tableau-stocks'

const TOUS = 'tous'

type TypeDeclarant = LigneStockDeclare['type']

/**
 * Stocks déclarés de tous les opérateurs, pour le BNIA (analyse UC-DEC-1, spec 05 §6.1), en deux onglets
 * indépendants, chacun avec ses filtres : *Données*, le stock de fin de la dernière DSM validée de chacun,
 * ligne par ligne (RG-DEC-42), avec l'export Excel pour les douanes ; *Analyse*, le tableau croisé dynamique
 * et le graphique d'une année déclarative (RG-DEC-44).
 */
export function PageStocksDeclares() {
  return (
    <PageMetier
      titre='Stocks déclarés'
      description='Le stock déclaré de chaque opérateur : le stock de fin de ses DSM validées.'
    >
      <Tabs defaultValue='donnees' className='mb-8'>
        <TabsList>
          <TabsTrigger value='donnees'>Données</TabsTrigger>
          <TabsTrigger value='analyse'>Analyse</TabsTrigger>
        </TabsList>
        <TabsContent value='donnees' className='mt-4'>
          <DonneesStocks />
        </TabsContent>
        <TabsContent value='analyse' className='mt-4'>
          <AnalyseStocks />
        </TabsContent>
      </Tabs>
    </PageMetier>
  )
}

/** Filtres opérateur et type de l'onglet *Analyse*, appliqués par le serveur. */
function useFiltres() {
  const [operateur, setOperateur] = useState<ResultatRechercheOperateur | null>(
    null
  )
  const [type, setType] = useState<string>(TOUS)
  return {
    operateur,
    setOperateur,
    type,
    setType,
    query: {
      operateur: operateur?.id,
      type: type === TOUS ? undefined : (type as TypeDeclarant),
    },
  }
}

function Filtres({
  id,
  filtres,
}: {
  id: string
  filtres: ReturnType<typeof useFiltres>
}) {
  const [recherche, setRecherche] = useState(false)
  const { operateur, setOperateur, type, setType } = filtres
  return (
    <>
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
        <Label htmlFor={`${id}-type`}>Type</Label>
        <Select value={type} onValueChange={setType}>
          <SelectTrigger id={`${id}-type`} className='w-40'>
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
    </>
  )
}

const colonne = createColumnHelper<LigneStockDeclare>()

const COLONNES = [
  colonne.accessor((l) => `${l.codeOperateur} · ${l.raisonSociale}`, {
    id: 'operateur',
    header: ({ column }) => (
      <EnteteFiltrable column={column} titre='Opérateur' />
    ),
    cell: ({ row }) => (
      <>
        <span className='font-mono'>{row.original.codeOperateur}</span> ·{' '}
        {row.original.raisonSociale}
      </>
    ),
    filterFn: filtreValeurs,
  }),
  colonne.accessor((l) => TYPES_DECLARANT[l.type], {
    id: 'type',
    header: ({ column }) => <EnteteFiltrable column={column} titre='Type' />,
    filterFn: filtreValeurs,
  }),
  colonne.accessor('au', {
    header: ({ column }) => (
      <EnteteFiltrable column={column} titre='Au' affichage={formatDate} />
    ),
    cell: ({ getValue }) => formatDate(getValue()),
    filterFn: filtreValeurs,
  }),
  ...colonnesLigneStock<LigneStockDeclare>(),
]

/**
 * *Données* : le stock de fin de la dernière DSM validée de chaque opérateur, ligne par ligne, lignes nulles
 * omises (RG-DEC-42) ; dans la table, recherche, tri et filtre par valeurs sur chaque colonne, total des
 * lignes affichées. Une ligne ouvre la DSM dont elle est tirée. L'export Excel pour les douanes rend toutes
 * les lignes.
 */
function DonneesStocks() {
  const navigate = useNavigate()
  const stocks = useQuery({
    queryKey: ['declarations', 'stocks'],
    queryFn: () =>
      listerStocksDeclares({ throwOnError: true }).then((r) => r.data),
  })
  return (
    <TableDonnees
      donnees={stocks.data ?? []}
      colonnes={COLONNES}
      chiffrees={['stock']}
      chargement={stocks.isPending}
      vide='Aucun stock déclaré.'
      total={(l) => l.stock}
      formatTotal={formatLitres}
      onLigne={(l) =>
        navigate({
          to: '/bo/operateurs/$id/dsm/$dsm',
          params: { id: String(l.operateur), dsm: String(l.dsm) },
        })
      }
      actions={
        <Button variant='outline' size='sm' asChild>
          <a href='/api/declarations/stocks/excel' download>
            <FileDown className='size-4' /> Exporter en Excel
          </a>
        </Button>
      }
    />
  )
}

/**
 * *Analyse* : le tableau croisé dynamique et le graphique d'une année déclarative (du 1er avril au 31 mars),
 * à chaque fin de mois écoulée, stock de la dernière DSM validée de chacun à cette date (RG-DEC-44) ; ses
 * propres filtres opérateur et type.
 */
function AnalyseStocks() {
  const filtres = useFiltres()
  const [annee, setAnnee] = useState<number>()
  const historique = useQuery({
    queryKey: ['declarations', 'stocks', 'historique', filtres.query, annee],
    queryFn: () =>
      historiqueStocksDeclares({
        query: { ...filtres.query, annee },
        throwOnError: true,
      }).then((r) => r.data),
  })
  return (
    <>
      <div className='mb-4 flex flex-wrap items-end gap-4'>
        <ChoixAnnee
          historique={historique.data}
          valeur={annee}
          onChange={setAnnee}
        />
        <Filtres id='analyse' filtres={filtres} />
      </div>
      <Card>
        <CardHeader>
          <CardTitle className='text-base'>Tableau croisé dynamique</CardTitle>
        </CardHeader>
        <CardContent>
          {historique.isPending && (
            <p className='text-muted-foreground text-sm'>Chargement…</p>
          )}
          {historique.isSuccess && (
            <TableauStocks
              historique={historique.data}
              avecOperateur
              cle='stocks-declares-bo'
            />
          )}
        </CardContent>
      </Card>
    </>
  )
}
