import { type ReactNode, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, type LinkProps, useNavigate } from '@tanstack/react-router'
import {
  Banknote,
  Building2,
  FileSearch,
  FileWarning,
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
  listerPieces,
  listerToutesDsm,
  rechercherOperateurs,
} from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { FILIERE, type TypeDeclarant } from '@/components/metier/filiere'
import { useContratsBnia } from '@/features/contrats/api'
import { GraphiqueComptes } from '@/features/declarations/graphiques-stocks'
import { dansGroupe as dans } from '@/features/declarations/libelles'
import { echueImpayee } from '@/features/facturation/libelles'
import { useNouveauxMessages } from '@/features/messagerie/nouveaux'
import { useDroits, useMoi } from '@/features/session/session'
import { Activite, GrilleActivites, Section, Valeur } from './activite'
import { GraphiqueExportations } from './graphique-exportations'

type Module = 'declarations' | 'facturation'

const entier = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 })

/** Chiffres de démonstration, pour les contextes qui n'existent pas encore. */
const DEMO = {
  certificatsExport: 7,
  commandesBoutique: 9,
}

const lit = (droits: Droits | undefined, module: Module) =>
  !!droits && droits[module] !== 'AUCUN'

/**
 * Tableau de bord du back-office (CDC §4.1) : l'état des lieux de l'interprofession (opérateurs et stock déclaré
 * de chaque type, recherche rapide d'un opérateur), les activités à traiter, puis deux graphiques. Chaque
 * indicateur ouvre le listing préfiltré correspondant. Les blocs suivent les droits de l'utilisateur sur
 * l'opérateur BNIA : seuls ceux des modules qu'il lit s'affichent, et leurs lectures ne partent qu'alors. Les
 * chiffres viennent de la base quand le contexte existe (DSM, factures, messages), ceux des opérateurs et
 * des stocks de la filière (FILIERE), ainsi que les contrats en attente d'approbation (RG-CA-25) ; les certificats export,
 * commandes boutique et les graphiques sont des données de démonstration, marquées comme
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
        <div className='grid gap-6 lg:grid-cols-2 xl:grid-cols-3'>
          <CarteType
            type='PRODUCTEUR'
            libelle='Producteurs'
            icone={Grape}
            couleur='var(--chart-5)'
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
      )}
      <Section titre='Activités'>
        <GrilleActivites>
          {declarations && <ActivitesDsm />}
          {facturation && <FacturesAValider />}
          {facturation && <FacturesImpayees />}
          <Activite
            libelle='Demandes de certificat export'
            icone={Stamp}
            valeur={DEMO.certificatsExport}
            demo
          />
          {operateurs && <ContratsEnAttente />}
          <Activite
            libelle='Commandes boutique en attente'
            icone={ShoppingBag}
            valeur={DEMO.commandesBoutique}
            demo
          />
          <NouveauxMessages />
        </GrilleActivites>
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
 * total, qui ouvre les stocks déclarés, pour qui lit les déclarations. Chiffres de la filière (FILIERE).
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
  const { operateurs, stockHl } = FILIERE[type]
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
          <Valeur valeur={entier.format(operateurs)} />
        </Lien>
        {avecStock && (
          <Lien to='/bo/declarations/stocks'>
            <p className='text-muted-foreground text-sm'>Stock total déclaré</p>
            {/* « hl AP » d'un bloc : sur mobile, l'unité passe entière à la ligne. */}
            <Valeur valeur={`${entier.format(stockHl)} hl\u00a0AP`} />
          </Lien>
        )}
      </CardContent>
    </Card>
  )
}

/**
 * Recherche rapide d'un opérateur actif (CDC §4.1) : un résultat ouvre sa fiche ; Entrée (ou « Rechercher » du
 * clavier d'un téléphone), sans mention à l'écran, ouvre le listing des opérateurs filtré par le texte, inactifs compris.
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
            placeholder='3 caractères au moins'
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

/** Spec 15 §6.1, RG-MSG-11 : les messages non lus du BNIA ; un clic ouvre la messagerie, non lues en gras. */
function NouveauxMessages() {
  const { data } = useNouveauxMessages()
  return (
    <Activite
      libelle='Nouveaux messages'
      icone={Mail}
      valeur={data?.nombre}
      lien={{ to: '/bo/messagerie' }}
    />
  )
}

/**
 * Les DSM, en trois compteurs calculés sur la page DSM : à relancer (périodes absentes, brouillons, compléments
 * demandés, en retard, RG-DEC-40 ; la carte, en alerte, n'apparaît qu'alors), à traiter par le BNIA, validées à
 * facturer (RG-DEC-34, 43).
 */
function ActivitesDsm() {
  const dsm = useQuery({
    queryKey: ['declarations', 'toutes', 'tableau-de-bord'],
    queryFn: () => listerToutesDsm({ throwOnError: true }).then((r) => r.data),
  })
  const toutes = dsm.data
  const aRelancer = toutes?.filter(
    (d) => dans('A_SAISIR')(d) && d.enRetard
  ).length
  return (
    <>
      {!!aRelancer && (
        <Activite
          libelle='DSM à relancer'
          icone={FileWarning}
          valeur={aRelancer}
          lien={{
            to: '/bo/declarations',
            search: { etat: 'A_SAISIR', retard: true },
          }}
          alerte
        />
      )}
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

/**
 * RG-FAC-13, 33 : les factures échues et impayées, transmises à Zeendoc, non payées, leur échéance passée ;
 * affichée en alerte, seulement s'il y en a. Ouvre l'onglet « Émises », où leurs lignes sont teintées.
 */
function FacturesImpayees() {
  const emises = useQuery({
    queryKey: ['facturation', 'pieces', { etat: 'EMISE' }],
    queryFn: () =>
      listerPieces({ query: { etat: 'EMISE' }, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const echues = emises.data?.filter(echueImpayee).length
  if (!echues) return null
  return (
    <Activite
      libelle='Factures impayées'
      icone={Banknote}
      valeur={echues}
      lien={{ to: '/bo/facturation', search: { onglet: 'emises' } }}
      alerte
    />
  )
}

/** RG-CA-25 : les contrats en projet, que le BNIA suit en lecture ; ouvre la liste filtrée. */
function ContratsEnAttente() {
  const contrats = useContratsBnia({})
  return (
    <Activite
      libelle="Contrats en attente d'approbation"
      icone={Handshake}
      valeur={contrats.data?.filter((c) => c.etat === 'PROJET').length}
      lien={{ to: '/bo/contrats', search: { etat: 'PROJET' } }}
    />
  )
}
