import { type ReactNode, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, type LinkProps, useNavigate } from '@tanstack/react-router'
import {
  Building2,
  ChevronRight,
  FilePenLine,
  FileSearch,
  FileText,
  Grape,
  Handshake,
  type LucideIcon,
  Mail,
  Receipt,
  Search,
  ShoppingBag,
  Stamp,
} from 'lucide-react'
import {
  type Droits,
  type DsmListee,
  listerOperateurs,
  listerPieces,
  listerStocksDeclares,
  listerToutesDsm,
  rechercherOperateurs,
} from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
import { Badge } from '@/components/ui/badge'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { GraphiqueComptes } from '@/features/declarations/graphiques-stocks'
import { GROUPES_ETATS } from '@/features/declarations/libelles'
import { useDroits, useMoi } from '@/features/session/session'
import { GraphiqueExportations } from './graphique-exportations'

type TypeDeclarant = 'PRODUCTEUR' | 'NEGOCIANT'
type Module = 'declarations' | 'facturation'

/** Chiffres de démonstration, pour les contextes qui n'existent pas encore. */
const DEMO = {
  certificatsExport: 7,
  contrats: 4,
  commandesBoutique: 9,
  messages: 12,
}

const entier = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 })

/** Un stock en L AP, affiché en hl AP arrondis : « 4 230 hl AP ». */
const enHectolitres = (litres: number) => `${entier.format(litres / 100)} hl AP`

const lit = (droits: Droits | undefined, module: Module) =>
  !!droits && droits[module] !== 'AUCUN'

/**
 * Tableau de bord du back-office (CDC §4.1) : l'état des lieux de l'interprofession (opérateurs et stock déclaré
 * de chaque type, recherche rapide d'un opérateur), les activités à traiter, puis deux graphiques. Chaque
 * indicateur ouvre le listing préfiltré correspondant. Les blocs suivent les droits de l'utilisateur sur
 * l'opérateur BNIA : seuls ceux des modules qu'il lit s'affichent, et leurs lectures ne partent qu'alors. Les
 * chiffres viennent de la base quand le contexte existe (opérateurs, DSM, factures) ; les certificats export,
 * contrats, commandes boutique, messages et les graphiques sont des données de démonstration, marquées comme
 * telles.
 */
export function TableauDeBordBnia() {
  const { actif } = useMoi()
  const droits = useDroits(actif?.operateur ?? 0)
  if (!droits) return <Skeleton className='h-96 w-full' />
  const operateurs = droits.personnelBnia
  const declarations = lit(droits, 'declarations')
  const facturation = lit(droits, 'facturation')
  return (
    <div className='space-y-10'>
      {operateurs && (
        <Section titre="État des lieux de l'interprofession">
          <div className='grid gap-6 lg:grid-cols-2 xl:grid-cols-3'>
            <CarteType
              type='PRODUCTEUR'
              libelle='Producteurs'
              icone={Grape}
              couleur='var(--chart-1)'
              stock={declarations}
            />
            <CarteType
              type='NEGOCIANT'
              libelle='Négociants'
              icone={Building2}
              couleur='var(--chart-2)'
              stock={declarations}
            />
            <RechercheRapide />
          </div>
        </Section>
      )}
      <Section titre='Activités'>
        <div className='grid gap-4 sm:grid-cols-2 xl:grid-cols-4'>
          {declarations && <ActivitesDsm />}
          {facturation && <FacturesAValider />}
          <Activite
            libelle='Demandes de certificat export'
            icone={Stamp}
            valeur={DEMO.certificatsExport}
            demo
          />
          <Activite
            libelle='Contrats Armagnac à valider'
            icone={Handshake}
            valeur={DEMO.contrats}
            demo
          />
          <Activite
            libelle='Commandes boutique en attente'
            icone={ShoppingBag}
            valeur={DEMO.commandesBoutique}
            demo
          />
          <Activite
            libelle='Nouveaux messages'
            icone={Mail}
            valeur={DEMO.messages}
            demo
          />
        </div>
      </Section>
      <Section titre='Évolutions'>
        <div className='grid gap-6 xl:grid-cols-2'>
          {declarations && <GraphiqueComptes />}
          <GraphiqueExportations />
        </div>
      </Section>
    </div>
  )
}

function Section({ titre, children }: { titre: string; children: ReactNode }) {
  return (
    <section>
      <h2 className='mb-4 text-xl font-semibold'>{titre}</h2>
      {children}
    </section>
  )
}

/** Un nombre en attente de sa lecture. */
function Valeur({ valeur }: { valeur: string | undefined }) {
  return valeur === undefined ? (
    <Skeleton className='h-9 w-24' />
  ) : (
    <span className='block text-3xl font-bold tabular-nums'>{valeur}</span>
  )
}

/** Un bloc cliquable vers une page, au fond marqué au survol. */
function Lien({
  to,
  search,
  children,
}: {
  to: LinkProps['to']
  search?: LinkProps['search']
  children: ReactNode
}) {
  return (
    <Link
      to={to}
      search={search}
      className='hover:bg-muted/60 -m-2 block rounded-md p-2 transition-colors'
    >
      {children}
    </Link>
  )
}

/**
 * Un type de déclarant : nombre d'opérateurs actifs, qui ouvre le listing préfiltré sur ce type ; stock déclaré
 * total (RG-DEC-42), qui ouvre les stocks déclarés, pour qui lit les déclarations.
 */
function CarteType({
  type,
  libelle,
  icone: Icone,
  couleur,
  stock: avecStock,
}: {
  type: TypeDeclarant
  libelle: string
  icone: LucideIcon
  couleur: string
  stock: boolean
}) {
  const operateurs = useQuery({
    queryKey: ['operateurs', 'liste', { type, actif: true }],
    queryFn: () =>
      listerOperateurs({
        query: { type, actif: true },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const stocks = useQuery({
    queryKey: ['declarations', 'stocks', { type }],
    queryFn: () =>
      listerStocksDeclares({ query: { type }, throwOnError: true }).then(
        (r) => r.data
      ),
    enabled: avecStock,
  })
  const stock = stocks.data?.reduce((s, l) => s + l.stock, 0)
  return (
    <Card>
      <CardHeader className='flex flex-row items-center gap-3'>
        <span
          className='flex size-10 items-center justify-center rounded-lg'
          style={{
            background: `color-mix(in oklch, ${couleur} 15%, transparent)`,
            color: couleur,
          }}
        >
          <Icone className='size-5' />
        </span>
        <CardTitle className='text-lg'>{libelle}</CardTitle>
      </CardHeader>
      <CardContent className='grid grid-cols-2 gap-6'>
        <Lien to='/bo/operateurs' search={{ type }}>
          <p className='text-muted-foreground text-sm'>Opérateurs actifs</p>
          <Valeur
            valeur={operateurs.data && entier.format(operateurs.data.length)}
          />
        </Lien>
        {avecStock && (
          <Lien to='/bo/declarations/stocks'>
            <p className='text-muted-foreground text-sm'>Stock total déclaré</p>
            <Valeur
              valeur={stock === undefined ? undefined : enHectolitres(stock)}
            />
          </Lien>
        )}
      </CardContent>
    </Card>
  )
}

/**
 * Recherche rapide d'un opérateur actif (CDC §4.1) : un résultat ouvre sa fiche ; Entrée ouvre le listing des
 * opérateurs filtré par le texte, inactifs compris.
 */
function RechercheRapide() {
  const navigate = useNavigate()
  const [texte, setTexte] = useState('')
  const stable = useDebounce(texte.trim())
  const { data, isFetching } = useQuery({
    queryKey: ['recherche-operateurs', stable],
    queryFn: () =>
      rechercherOperateurs({
        query: { texte: stable },
        throwOnError: true,
      }).then((r) => r.data),
    enabled: stable.length >= 3,
  })
  const resultats = stable.length >= 3 ? (data ?? []).slice(0, 5) : []
  return (
    <Card className='lg:col-span-2 xl:col-span-1'>
      <CardHeader className='flex flex-row items-center gap-3'>
        <span className='bg-muted text-muted-foreground flex size-10 items-center justify-center rounded-lg'>
          <Search className='size-5' />
        </span>
        <div>
          <CardTitle className='text-lg'>Recherche rapide</CardTitle>
          <CardDescription>
            Code, raison sociale ou nom commercial
          </CardDescription>
        </div>
      </CardHeader>
      <CardContent className='space-y-2'>
        <form
          onSubmit={(e) => {
            e.preventDefault()
            if (texte.trim())
              navigate({
                to: '/bo/operateurs',
                search: { recherche: texte.trim() },
              })
          }}
        >
          <Input
            value={texte}
            onChange={(e) => setTexte(e.target.value)}
            placeholder='3 caractères au moins, Entrée pour la liste'
            aria-label='Rechercher un opérateur'
          />
        </form>
        {stable.length >= 3 && !isFetching && resultats.length === 0 && (
          <p className='text-muted-foreground px-2 text-sm'>
            Aucun opérateur actif ne correspond.
          </p>
        )}
        <ul className='text-sm'>
          {resultats.map((o) => (
            <li key={o.id}>
              <Link
                to='/bo/operateurs/$id'
                params={{ id: String(o.id) }}
                className='hover:bg-muted flex items-center gap-1 rounded-md px-2 py-1.5'
              >
                <span className='font-mono'>{o.code}</span>
                <span className='truncate'>· {o.raisonSociale}</span>
                {o.ville && (
                  <span className='text-muted-foreground truncate'>
                    · {o.ville}
                  </span>
                )}
              </Link>
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  )
}

/** Une activité : un nombre de choses à faire, et le listing préfiltré où les faire. */
function Activite({
  libelle,
  icone: Icone,
  valeur,
  lien,
  demo,
}: {
  libelle: string
  icone: LucideIcon
  valeur: number | undefined
  lien?: { to: LinkProps['to']; search?: LinkProps['search'] }
  demo?: boolean
}) {
  const contenu = (
    <Card className='h-full gap-2 py-5'>
      <CardHeader className='flex flex-row items-center justify-between gap-2 px-5'>
        <span className='bg-muted text-muted-foreground flex size-9 items-center justify-center rounded-lg'>
          <Icone className='size-[18px]' />
        </span>
        {demo ? (
          <Badge variant='outline' className='font-normal'>
            Démonstration
          </Badge>
        ) : (
          lien && <ChevronRight className='text-muted-foreground size-4' />
        )}
      </CardHeader>
      <CardContent className='space-y-1 px-5'>
        <Valeur
          valeur={valeur === undefined ? undefined : entier.format(valeur)}
        />
        <p className='text-muted-foreground text-sm'>{libelle}</p>
      </CardContent>
    </Card>
  )
  return lien ? (
    <Link
      to={lien.to}
      search={lien.search}
      className='hover:[&>[data-slot=card]]:border-primary/40 block rounded-xl transition-colors'
    >
      {contenu}
    </Link>
  ) : (
    contenu
  )
}

const etatDe = (d: DsmListee) => (d.absente ? 'ABSENTE' : d.etat)
const dans = (groupe: string) => (d: DsmListee) => {
  const e = etatDe(d)
  return !!e && GROUPES_ETATS[groupe].etats.includes(e)
}

/**
 * Les DSM, en trois compteurs calculés comme les onglets de la page DSM : à saisir ou relancer (périodes
 * absentes, brouillons, compléments demandés), à traiter par le BNIA, validées à facturer (RG-DEC-34, 43).
 */
function ActivitesDsm() {
  const dsm = useQuery({
    queryKey: ['declarations', 'toutes', 'tableau-de-bord'],
    queryFn: () => listerToutesDsm({ throwOnError: true }).then((r) => r.data),
  })
  const toutes = dsm.data
  return (
    <>
      <Activite
        libelle='DSM en attente de saisie'
        icone={FilePenLine}
        valeur={toutes?.filter(dans('A_SAISIR')).length}
        lien={{ to: '/bo/declarations', search: { etat: 'A_SAISIR' } }}
      />
      <Activite
        libelle='DSM en attente de validation'
        icone={FileSearch}
        valeur={toutes?.filter(dans('A_TRAITER')).length}
        lien={{ to: '/bo/declarations', search: { etat: 'A_TRAITER' } }}
      />
      <Activite
        libelle='DSM à facturer'
        icone={Receipt}
        valeur={
          toutes?.filter(
            (d) => d.etat === 'VALIDEE' && !d.facturee && !d.facturationDemandee
          ).length
        }
        lien={{ to: '/bo/declarations', search: { vue: 'validees' } }}
      />
    </>
  )
}

/** Les brouillons de facture à relire et émettre. */
function FacturesAValider() {
  const brouillons = useQuery({
    queryKey: ['facturation', 'pieces', { etat: 'BROUILLON' }],
    queryFn: () =>
      listerPieces({ query: { etat: 'BROUILLON' }, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  return (
    <Activite
      libelle='Factures à valider'
      icone={FileText}
      valeur={brouillons.data?.length}
      lien={{ to: '/bo/facturation', search: { onglet: 'brouillons' } }}
    />
  )
}
