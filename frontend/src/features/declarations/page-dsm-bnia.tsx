import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { ChevronRight, Receipt, X } from 'lucide-react'
import { toast } from 'sonner'
import {
  type Dsm,
  type DsmListee,
  listerToutesDsm,
  prendreDsmEnInstruction,
  type ResultatRechercheOperateur,
} from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
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
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { PageMetier } from '@/components/metier/page-metier'
import { RechercheOperateur } from '@/features/operateurs/portail/recherche-operateur'
import { Echeance } from './echeance'
import { formatDateHeure, formatEuros } from './format'
import { TYPES_DECLARANT, VARIANTE_ETAT } from './libelles'

const TOUS = 'tous'

type EtatDsm = Dsm['etat']
type TypeDeclarant = Dsm['type']

/** Une période attendue sans DSM (RG-DEC-40), filtrée comme un état. */
const ABSENTE = 'ABSENTE'

/** Les états d'une DSM en cours, du point de vue du BNIA, que l'onglet « En cours » filtre un à un. */
const EN_COURS: Partial<Record<EtatDsm | typeof ABSENTE, string>> = {
  ABSENTE: 'Absente',
  BROUILLON: 'Brouillon',
  ENVOYEE: 'À prendre en charge',
  EN_INSTRUCTION: 'En instruction',
  COMPLEMENTS_DEMANDES: 'Compléments demandés',
  COMPLEMENTS_RECUS: 'Compléments reçus',
}

type Vue = 'en-cours' | 'validees' | 'facturees'

/**
 * Les DSM de tous les opérateurs, pour le BNIA (spec 05 §6.1, UC-DEC-7), en trois onglets : en cours
 * (brouillon, à prendre en charge, en instruction, compléments demandés, compléments reçus ; filtre d'état,
 * « tous » par défaut), validées (sans facture, donc à facturer), facturées (projection des factures,
 * RG-DEC-34). Total HT, calculé ou facturé. Filtres opérateur, type et mois. Une ligne ouvre la DSM.
 * Lot 7 : échéance et retard de chaque ligne ; la période attendue d'un opérateur sans DSM est une ligne
 * « absente » de l'onglet « En cours », qui ouvre l'onglet « Déclarations » de sa fiche (RG-DEC-39, 40).
 */
export function PageDsmBnia() {
  const navigate = useNavigate()
  const [operateur, setOperateur] = useState<ResultatRechercheOperateur | null>(
    null
  )
  const [recherche, setRecherche] = useState(false)
  const [type, setType] = useState<string>(TOUS)
  const [mois, setMois] = useState('')
  const [vue, setVue] = useState<Vue>('en-cours')
  const [etat, setEtat] = useState<string>(TOUS)
  const [enRetard, setEnRetard] = useState(false)
  // Une seule lecture, filtrée par opérateur, type et mois : les onglets et leurs nombres se calculent
  // dessus, et suivent donc ces filtres.
  const listing = useQuery({
    queryKey: ['declarations', 'toutes', operateur?.id, type, mois],
    queryFn: () =>
      listerToutesDsm({
        query: {
          operateur: operateur?.id,
          type: type === TOUS ? undefined : (type as TypeDeclarant),
          mois: mois || undefined,
        },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const toutes = listing.data ?? []
  const enCours = toutes.filter((d) => d.etat !== 'VALIDEE')
  const etatDe = (d: DsmListee) => (d.absente ? ABSENTE : d.etat)
  // RG-DEC-34 : une DSM validée sans facture est à facturer ; facturée, elle passe dans le dernier onglet.
  const validees = toutes.filter((d) => d.etat === 'VALIDEE' && !d.facturee)
  const facturees = toutes.filter((d) => d.facturee)
  const dsms =
    vue === 'validees'
      ? validees
      : vue === 'facturees'
        ? facturees
        : enCours
            .filter((d) => etat === TOUS || etatDe(d) === etat)
            .filter((d) => !enRetard || d.enRetard)
  const enCoursVue = vue === 'en-cours'
  const queryClient = useQueryClient()
  const ouvrir = (operateurId: number, id: number) =>
    navigate({
      to: '/bo/operateurs/$id/dsm/$dsm',
      params: { id: String(operateurId), dsm: String(id) },
    })
  // Une période absente n'a pas de DSM : elle ouvre l'onglet « Déclarations » de la fiche opérateur.
  const ouvrirLigne = (d: DsmListee) =>
    d.id == null
      ? navigate({
          to: '/bo/operateurs/$id',
          params: { id: String(d.operateur) },
          search: { onglet: 'declarations' },
        })
      : ouvrir(d.operateur, d.id)
  // RG-DEC-20 : la prise en instruction se fait aussi depuis le listing ; la DSM prise en charge s'ouvre ensuite.
  const priseEnInstruction = useMutation({
    mutationFn: (d: DsmListee) =>
      prendreDsmEnInstruction({
        path: { operateur: d.operateur, id: d.id! },
        body: {},
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: (_, d) => {
      toast.success(
        `DSM de ${d.periode.libelle} de ${d.raisonSociale} prise en charge.`
      )
      queryClient.invalidateQueries({ queryKey: ['declarations'] })
      ouvrirLigne(d)
    },
  })

  return (
    <PageMetier
      titre='DSM'
      description='Les déclarations de stocks et mouvements de tous les opérateurs.'
    >
      <Tabs
        value={vue}
        onValueChange={(v) => setVue(v as Vue)}
        className='mb-4'
      >
        <TabsList>
          <TabsTrigger value='en-cours'>
            En cours <Nombre valeur={enCours.length} pret={listing.isSuccess} />
          </TabsTrigger>
          <TabsTrigger value='validees'>
            Validées{' '}
            <Nombre valeur={validees.length} pret={listing.isSuccess} />
          </TabsTrigger>
          <TabsTrigger value='facturees'>
            {/* Sans nombre : les DSM facturées s'accumulent. */}
            Facturées
          </TabsTrigger>
        </TabsList>
      </Tabs>
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
          <Label htmlFor='filtre-type'>Type</Label>
          <Select value={type} onValueChange={setType}>
            <SelectTrigger id='filtre-type' className='w-40'>
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
        <div className='space-y-1'>
          <Label htmlFor='filtre-mois'>Période contenant le mois</Label>
          <Input
            id='filtre-mois'
            type='month'
            className='w-44'
            value={mois}
            onChange={(e) => setMois(e.target.value)}
          />
        </div>
        {vue === 'en-cours' && (
          <div className='flex h-9 items-center gap-2'>
            <Checkbox
              id='filtre-retard'
              checked={enRetard}
              onCheckedChange={(v) => setEnRetard(v === true)}
            />
            <Label htmlFor='filtre-retard' className='font-normal'>
              En retard seulement
            </Label>
          </div>
        )}
        {vue === 'en-cours' && (
          <div className='space-y-1'>
            <Label htmlFor='filtre-etat'>État</Label>
            <Select value={etat} onValueChange={setEtat}>
              <SelectTrigger id='filtre-etat' className='w-52'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={TOUS}>Tous</SelectItem>
                {Object.entries(EN_COURS).map(([valeur, libelle]) => (
                  <SelectItem key={valeur} value={valeur}>
                    {libelle}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}
      </div>
      {vue === 'validees' && (
        <p className='text-muted-foreground mb-4 flex items-center gap-2 text-sm'>
          <Receipt className='size-4' />
          Les DSM validées, à facturer ; total HT calculé à la grille CVO.
        </p>
      )}
      {vue === 'facturees' && (
        <p className='text-muted-foreground mb-4 flex items-center gap-2 text-sm'>
          <Receipt className='size-4' />
          Les DSM dont la facture a été reçue de la facturation ; total HT
          facturé, factures moins avoirs.
        </p>
      )}
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Opérateur</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Période</TableHead>
              <TableHead>État</TableHead>
              <TableHead>Échéance</TableHead>
              {enCoursVue ? (
                <>
                  <TableHead>Modifiée le</TableHead>
                  <TableHead />
                </>
              ) : (
                <>
                  <TableHead>Envoyée le</TableHead>
                  <TableHead>Validée le</TableHead>
                </>
              )}
              <TableHead className='text-right'>Total HT</TableHead>
              <TableHead className='w-0' />
            </TableRow>
          </TableHeader>
          <TableBody>
            {dsms.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={9}
                  className='text-muted-foreground h-16 text-center'
                >
                  {listing.isPending ? 'Chargement…' : 'Aucune DSM.'}
                </TableCell>
              </TableRow>
            )}
            {dsms.map((d) => (
              <TableRow
                key={d.id ?? `absente-${d.operateur}-${d.periode.debut}`}
                className='cursor-pointer'
                onClick={() => ouvrirLigne(d)}
              >
                <TableCell>
                  <span className='font-mono'>{d.codeOperateur}</span> ·{' '}
                  {d.raisonSociale}
                </TableCell>
                <TableCell>{TYPES_DECLARANT[d.type]}</TableCell>
                <TableCell className='font-medium'>
                  {d.periode.libelle}
                </TableCell>
                <TableCell>
                  <Badge
                    variant={d.etat ? VARIANTE_ETAT[d.etat] : 'outline'}
                    className={d.absente ? 'border-dashed' : undefined}
                  >
                    {d.libelleEtat}
                  </Badge>
                </TableCell>
                <TableCell>
                  <Echeance echeance={d.echeance} enRetard={d.enRetard} />
                </TableCell>
                {enCoursVue ? (
                  <>
                    <TableCell>{formatDateHeure(d.modifieeLe)}</TableCell>
                    <TableCell className='py-1'>
                      {d.prendreEnInstruction && (
                        <Button
                          size='sm'
                          disabled={priseEnInstruction.isPending}
                          onClick={(e) => {
                            e.stopPropagation()
                            priseEnInstruction.mutate(d)
                          }}
                        >
                          Prendre en charge
                        </Button>
                      )}
                    </TableCell>
                  </>
                ) : (
                  <>
                    <TableCell>{formatDateHeure(d.envoyeeLe)}</TableCell>
                    <TableCell>{formatDateHeure(d.valideeLe)}</TableCell>
                  </>
                )}
                <TableCell className='text-right tabular-nums'>
                  {formatEuros(d.totalHt)}
                </TableCell>
                <TableCell>
                  <Button
                    variant='ghost'
                    size='sm'
                    aria-label={
                      d.absente
                        ? `Ouvrir les déclarations de ${d.raisonSociale}`
                        : `Ouvrir la DSM de ${d.periode.libelle} de ${d.raisonSociale}`
                    }
                    onClick={(e) => {
                      e.stopPropagation()
                      ouvrirLigne(d)
                    }}
                  >
                    <ChevronRight className='size-4' />
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </PageMetier>
  )
}

/** Le nombre de DSM d'un onglet, une fois le listing chargé. */
function Nombre({ valeur, pret }: { valeur: number; pret: boolean }) {
  if (!pret) return null
  return (
    <Badge variant='secondary' className='ms-1 px-1.5 tabular-nums'>
      {valeur}
    </Badge>
  )
}
