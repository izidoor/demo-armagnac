import { type ReactNode } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, type LinkProps, useNavigate } from '@tanstack/react-router'
import {
  ArrowDownUp,
  Banknote,
  Building2,
  FileCheck,
  FileInput,
  FileText,
  FileWarning,
  Flame,
  Globe,
  Grape,
  Handshake,
  type LucideIcon,
  Mail,
  MessagesSquare,
  ReceiptEuro,
  RefreshCw,
  ScrollText,
  Search,
  Ship,
  ShoppingBag,
  Stamp,
  Store,
} from 'lucide-react'
import { type Droits, listerPieces, listerToutesDsm } from '@/api/generated'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { FILIERE, type TypeDeclarant } from '@/components/metier/filiere'
import { GraphiqueComptes } from '@/features/declarations/graphiques-stocks'
import { dansGroupe as dans } from '@/features/declarations/libelles'
import { echueImpayee } from '@/features/facturation/libelles'
import { useNouveauxMessages } from '@/features/messagerie/nouveaux'
import { RechercheOperateur } from '@/features/operateurs/portail/recherche-operateur'
import { useDroits, useMoi } from '@/features/session/session'
import { Activite, Section, Theme, ThemesActivites, Valeur } from './activite'
import { GraphiqueExportations } from './graphique-exportations'

type Module = 'declarations' | 'facturation'

const entier = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 })

/** Chiffres de démonstration, pour les contextes qui n'existent pas encore. */
const DEMO = {
  suivisExport: 4,
  certificatsExport: 7,
  commandesBoutique: 9,
  distillation: 3,
}

const lit = (droits: Droits | undefined, module: Module) =>
  !!droits && droits[module] !== 'AUCUN'

/**
 * Tableau de bord du back-office (CDC §4.1) : l'état des lieux de l'interprofession (opérateurs et stock déclaré
 * de chaque type, recherche rapide d'un opérateur), les activités à traiter, rangées par thème (messagerie et
 * boutique, DSM, export, facturation, contrats et ODG), puis deux graphiques. Chaque
 * indicateur ouvre le listing préfiltré correspondant. Les blocs suivent les droits de l'utilisateur sur
 * l'opérateur BNIA : seuls ceux des modules qu'il lit s'affichent, et leurs lectures ne partent qu'alors. Les
 * chiffres viennent de la base quand le contexte existe (DSM, factures, messages), ceux des opérateurs et
 * des stocks de la filière (FILIERE) ; l'export (suivis, certificats, synchronisation Otentik), les commandes
 * boutique, la synchronisation Zeendoc, la distillation, la synchronisation ODG et les graphiques sont des données
 * de démonstration, marquées comme telles.
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
        <ThemesActivites>
          <Theme
            titre='Messagerie'
            icone={MessagesSquare}
            couleur='var(--chart-6)'
          >
            <NouveauxMessages />
          </Theme>
          <Theme titre='Boutique' icone={Store} couleur='var(--chart-6)' suite>
            <Activite
              libelle='Nouvelles commandes'
              singulier='Nouvelle commande'
              icone={ShoppingBag}
              valeur={DEMO.commandesBoutique}
              demo
            />
          </Theme>
          {declarations && (
            <Theme
              titre='Déclarations de stocks et mouvements'
              icone={ArrowDownUp}
              couleur='var(--chart-3)'
              lien={{ to: '/bo/declarations', libelle: 'Toutes les DSM' }}
            >
              <ActivitesDsm />
            </Theme>
          )}
          <Theme titre='Export' icone={Globe} couleur='var(--chart-4)'>
            <Activite
              libelle='Suivis export à valider'
              singulier='Suivi export à valider'
              icone={Ship}
              valeur={DEMO.suivisExport}
              demo
            />
            <Activite
              libelle='Demandes de certificat export'
              singulier='Demande de certificat export'
              icone={Stamp}
              valeur={DEMO.certificatsExport}
              demo
            />
            <Activite
              libelle='Service opérationnel'
              icone={RefreshCw}
              valeur='Otentik'
              pastille
              demo
            />
          </Theme>
          {facturation && (
            <Theme
              titre='Facturation'
              icone={ReceiptEuro}
              couleur='var(--chart-2)'
              lien={{ to: '/bo/facturation', libelle: 'Toutes les factures' }}
            >
              <FacturesAValider />
              <FacturesImpayees />
              <Activite
                libelle='Service opérationnel'
                icone={RefreshCw}
                valeur='Zeendoc'
                pastille
                demo
              />
            </Theme>
          )}
          <Theme
            titre='Contrats & ODG'
            icone={ScrollText}
            couleur='var(--chart-5)'
          >
            {operateurs && (
              <Activite
                libelle='Contrats Armagnac & vin'
                icone={Handshake}
                valeur={null}
                action='Voir tous les contrats'
                lien={{ to: '/bo/contrats' }}
              />
            )}
            <Activite
              libelle='Déclarations de distillation à valider'
              singulier='Déclaration de distillation à valider'
              icone={Flame}
              valeur={DEMO.distillation}
              demo
            />
            <Activite
              libelle='Service opérationnel'
              icone={RefreshCw}
              valeur='ODG'
              pastille
              demo
            />
          </Theme>
        </ThemesActivites>
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
 * Recherche rapide d'un opérateur actif (CDC §4.1), cinq résultats : un résultat ouvre sa fiche, au clic ou au clavier
 * (flèches, Entrée) ; Entrée sans résultat en surbrillance (ou « Rechercher » du clavier d'un téléphone), sans mention à
 * l'écran, ouvre le listing des opérateurs filtré par le texte, inactifs compris.
 */
function RechercheRapide() {
  const navigate = useNavigate()
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
      <CardContent>
        <RechercheOperateur
          limite={5}
          placeholder='3 caractères au moins'
          onChoisir={(o) =>
            navigate({ to: '/bo/operateurs/$id', params: { id: String(o.id) } })
          }
          onEntree={(texte) =>
            navigate({ to: '/bo/operateurs', search: { recherche: texte } })
          }
        />
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
      singulier='Nouveau message'
      icone={Mail}
      valeur={data?.nombre}
      lien={{ to: '/bo/messagerie' }}
    />
  )
}

/** Toutes les DSM, lues une fois pour les compteurs du tableau de bord (cache partagé). */
function useToutesDsm() {
  return useQuery({
    queryKey: ['declarations', 'toutes', 'tableau-de-bord'],
    queryFn: () => listerToutesDsm({ throwOnError: true }).then((r) => r.data),
  }).data
}

/**
 * Les DSM, en trois compteurs calculés sur la page DSM : à relancer (périodes absentes, brouillons, compléments
 * demandés, en retard, RG-DEC-40), en alerte s'il y en a ; envoyées, à prendre en charge ; validées, à facturer
 * (RG-DEC-34, 43).
 */
function ActivitesDsm() {
  const toutes = useToutesDsm()
  const aRelancer = toutes?.filter(
    (d) => dans('A_SAISIR')(d) && d.enRetard
  ).length
  return (
    <>
      <Activite
        libelle='DSM à relancer'
        icone={FileWarning}
        valeur={aRelancer}
        lien={{
          to: '/bo/declarations',
          search: { etat: 'A_SAISIR', retard: true },
        }}
        alerte={!!aRelancer}
      />
      <Activite
        libelle='DSM à prendre en charge'
        icone={FileInput}
        valeur={
          toutes?.filter((d) => !d.absente && d.etat === 'ENVOYEE').length
        }
        lien={{ to: '/bo/declarations', search: { etat: 'ENVOYEE' } }}
      />
      <Activite
        libelle='DSM à facturer'
        icone={FileCheck}
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
      singulier='Facture à valider'
      icone={FileText}
      valeur={brouillons.data?.length}
      lien={{ to: '/bo/facturation', search: { onglet: 'brouillons' } }}
    />
  )
}

/**
 * RG-FAC-13, 33 : les factures échues et impayées, transmises à Zeendoc, non payées, leur échéance passée ; en
 * alerte s'il y en a. Ouvre l'onglet « Émises », où leurs lignes sont teintées.
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
  return (
    <Activite
      libelle='Factures impayées'
      singulier='Facture impayée'
      icone={Banknote}
      valeur={echues}
      lien={{ to: '/bo/facturation', search: { onglet: 'emises' } }}
      alerte={!!echues}
    />
  )
}
