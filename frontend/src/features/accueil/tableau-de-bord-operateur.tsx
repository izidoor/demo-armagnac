import { useQuery } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import {
  ArrowDownUp,
  BadgeCheck,
  Building2,
  FilePenLine,
  FileSearch,
  FileWarning,
  Globe,
  Grape,
  Handshake,
  Hourglass,
  Mail,
  ReceiptEuro,
  Ship,
  Stamp,
  Truck,
  UserRound,
} from 'lucide-react'
import { type Droits, listerMesFactures } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useMesContrats } from '@/features/contrats/api'
import {
  apiDeclarations,
  cleDeclarations,
  useDsms,
  useStockEstime,
} from '@/features/declarations/api'
import { formatDate } from '@/features/declarations/format'
import { dansGroupe } from '@/features/declarations/libelles'
import { cleFacturation } from '@/features/facturation/api'
import { echueImpayee } from '@/features/facturation/libelles'
import { useNouveauxMessages } from '@/features/messagerie/nouveaux'
import { apiOperateur, cleOperateur } from '@/features/operateurs/commun/api'
import {
  APPELLATIONS,
  ROLES,
  TYPES,
} from '@/features/operateurs/commun/libelles'
import { useDroits, useMoi } from '@/features/session/session'
import { Activite, Section, Theme, ThemesActivites } from './activite'

/** Chiffres de démonstration, pour les contextes qui n'existent pas encore. */
const DEMO = {
  suivisExport: 1,
  certificatsExport: 1,
  certificatsDelivres: 12,
}

const entier = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 0 })

/**
 * Tableau de bord du portail, pendant de celui du back-office, pour l'opérateur actif. En tête, l'accueil de
 * l'utilisateur et le résumé de son entreprise, ses nouveaux messages (RG-MSG-11) et ses factures à régler, pour qui
 * lit sa facturation ; puis son activité rangée par thème : ses DSM, pour qui lit ses déclarations ; ses contrats
 * (RG-CA-25), pour qui lit ses contrats ; l'export, données de démonstration marquées comme telles.
 */
export function TableauDeBordOperateur() {
  const { actif } = useMoi()
  if (!actif) return null
  return <Activites key={actif.operateur} operateur={actif.operateur} />
}

function Activites({ operateur }: { operateur: number }) {
  const droits = useDroits(operateur)
  if (!droits) return <Skeleton className='h-48 w-full' />
  const lit = (module: keyof Droits) => droits[module] !== 'AUCUN'
  return (
    <div className='space-y-10'>
      <div className='grid grid-cols-2 gap-3 sm:gap-4 @min-[56rem]/content:grid-cols-4 @min-[56rem]/content:gap-5'>
        <Bienvenue operateur={operateur} stock={lit('declarations')} />
        <NouveauxMessages operateur={operateur} />
        {lit('facturation') && <FacturesARegler operateur={operateur} />}
      </div>
      <Section titre='Votre activité'>
        <ThemesActivites>
          {lit('declarations') && (
            <Theme
              titre='Déclarations de stocks et mouvements'
              icone={ArrowDownUp}
              couleur='var(--chart-3)'
              lien={{ to: '/portail/declarations', libelle: 'Mes DSM' }}
            >
              <ActivitesDsm operateur={operateur} />
            </Theme>
          )}
          {lit('contrats') && (
            <Theme
              titre='Contrats'
              icone={Handshake}
              couleur='var(--chart-5)'
              lien={{ to: '/portail/contrats', libelle: 'Mes contrats' }}
            >
              <ActivitesContrats operateur={operateur} />
            </Theme>
          )}
          <Theme titre='Export' icone={Globe} couleur='var(--chart-4)'>
            <Activite
              libelle='Suivis export à déclarer'
              singulier='Suivi export à déclarer'
              icone={Ship}
              valeur={DEMO.suivisExport}
              demo
            />
            <Activite
              libelle='Demandes de certificat export en cours'
              singulier='Demande de certificat export en cours'
              icone={Stamp}
              valeur={DEMO.certificatsExport}
              demo
            />
            <Activite
              libelle='Certificats export délivrés cette année'
              singulier='Certificat export délivré cette année'
              icone={BadgeCheck}
              valeur={DEMO.certificatsDelivres}
              demo
            />
          </Theme>
        </ThemesActivites>
      </Section>
    </div>
  )
}

/**
 * L'accueil : le prénom de l'utilisateur, son rôle et le type de l'entreprise active en badges, sa raison sociale en
 * titre, qui ouvre « Mon entreprise », puis sur une ligne ses appellations, l'une sous l'autre, son stock estimé
 * (RG-CA-11) et sa prochaine DSM (RG-DEC-39) pour qui lit ses déclarations. L'icône du type à sa couleur, comme
 * au back-office.
 */
function Bienvenue({
  operateur,
  stock: avecStock,
}: {
  operateur: number
  stock: boolean
}) {
  const { moi, actif } = useMoi()
  const fiche = useQuery({
    queryKey: [...cleOperateur(operateur), 'fiche'],
    queryFn: apiOperateur(operateur).fiche,
  }).data
  const producteur = actif?.type === 'PRODUCTEUR'
  const couleur = producteur ? 'var(--chart-5)' : 'var(--chart-2)'
  const Icone = producteur ? Grape : Building2
  return (
    <Card className='col-span-2 gap-4 py-5'>
      <CardContent className='space-y-4 px-5'>
        <div className='space-y-1.5'>
          <p className='flex flex-wrap items-center gap-2 text-lg font-medium'>
            Bienvenue, {moi.prenom}
            {actif && (
              <>
                <Badge variant='outline' className='font-normal'>
                  <UserRound /> {ROLES[actif.role]}
                </Badge>
                <Badge variant='outline' className='font-normal'>
                  <Icone style={{ color: couleur }} /> {TYPES[actif.type]}
                </Badge>
              </>
            )}
          </p>
          <Link
            to='/portail/entreprise'
            title='Mon entreprise'
            className='block text-2xl leading-tight font-semibold tracking-tight underline-offset-4 hover:underline sm:text-3xl'
          >
            {actif?.raisonSociale}
          </Link>
        </div>
        <div className='flex flex-wrap items-start gap-x-6 gap-y-3 sm:gap-x-8'>
          {!!fiche?.appellations.length && (
            <div className='flex flex-col items-start gap-1'>
              {fiche.appellations.map((a) => (
                <Badge key={a} variant='outline' className='font-normal'>
                  {APPELLATIONS[a]}
                </Badge>
              ))}
            </div>
          )}
          {avecStock && <ChiffresDeclarations operateur={operateur} />}
        </div>
      </CardContent>
    </Card>
  )
}

/** Son stock estimé, en hl AP (RG-CA-11), et sa prochaine DSM, avec son échéance (RG-DEC-39). */
function ChiffresDeclarations({ operateur }: { operateur: number }) {
  const stock = useStockEstime(operateur).data
  const situation = useSituation(operateur)
  return (
    <div className='flex gap-6 sm:gap-8'>
      <div>
        <p className='text-muted-foreground text-xs'>Stock estimé</p>
        {stock === undefined ? (
          <Skeleton className='mt-1 h-6 w-24' />
        ) : (
          <p className='text-lg font-semibold tabular-nums'>
            {entier.format(
              stock.lignes.reduce((s, l) => s + l.estime, 0) / 100
            )}{' '}
            hl&nbsp;AP
          </p>
        )}
      </div>
      {situation?.periode && (
        <div>
          <p className='text-muted-foreground text-xs'>Prochaine DSM</p>
          <p className='text-lg font-semibold'>{situation.periode.libelle}</p>
          {situation.echeance && (
            <p className='text-muted-foreground text-xs'>
              avant le {formatDate(situation.echeance)}
            </p>
          )}
        </div>
      )}
    </div>
  )
}

/** Sa situation déclarative : période attendue ou en cours, échéance, retard (RG-DEC-39, 40). */
function useSituation(operateur: number) {
  return useQuery({
    queryKey: [...cleDeclarations(operateur), 'situation'],
    queryFn: apiDeclarations(operateur).situation,
  }).data
}

/**
 * Ses DSM, comptées comme au back-office (RG-DEC-40) : en retard (la période attendue, son échéance passée : une au
 * plus ; en alerte s'il y en a), à saisir ou compléter (la période attendue qu'il peut initier, ses brouillons, ses
 * compléments demandés), puis envoyées au BNIA et pas encore validées.
 */
function ActivitesDsm({ operateur }: { operateur: number }) {
  const dsms = useDsms(operateur).data
  const situation = useSituation(operateur)
  const aSaisir =
    dsms && situation
      ? dsms.filter(dansGroupe('A_SAISIR')).length +
        (situation.initiable ? 1 : 0)
      : undefined
  return (
    <>
      <Activite
        libelle='DSM en retard'
        icone={FileWarning}
        valeur={situation && (situation.enRetard ? 1 : 0)}
        lien={{ to: '/portail/declarations' }}
        alerte={situation?.enRetard}
      />
      <Activite
        libelle='DSM à saisir ou compléter'
        icone={FilePenLine}
        valeur={aSaisir}
        lien={{ to: '/portail/declarations' }}
      />
      <Activite
        libelle='DSM en instruction par le BNIA'
        icone={FileSearch}
        valeur={dsms?.filter(dansGroupe('A_TRAITER')).length}
        lien={{ to: '/portail/declarations' }}
      />
    </>
  )
}

/**
 * Ses factures à régler : émises, pas encore payées (RG-FAC-33). En alerte (RG-FAC-13) si l'une est échue et
 * impayée ; ouvre ses factures, où ces lignes sont teintées.
 */
function FacturesARegler({ operateur }: { operateur: number }) {
  const factures = useQuery({
    queryKey: [...cleFacturation, 'portail', operateur, { sens: 'FACTURE' }],
    queryFn: () =>
      listerMesFactures({
        path: { operateur },
        query: { sens: 'FACTURE' },
        throwOnError: true,
      }).then((r) => r.data),
  }).data
  return (
    <Activite
      titre='Facturation'
      couleur='var(--chart-2)'
      libelle='Factures à régler'
      singulier='Facture à régler'
      icone={ReceiptEuro}
      valeur={
        factures?.filter((f) => f.sens === 'FACTURE' && f.zeendoc !== 'PAYEE')
          .length
      }
      lien={{ to: '/portail/factures' }}
      alerte={!!factures?.some(echueImpayee)}
    />
  )
}

/** Spec 15 §6.2, RG-MSG-11 : ses messages non lus ; un clic ouvre la messagerie, non lues en gras. */
function NouveauxMessages({ operateur }: { operateur: number }) {
  const { data } = useNouveauxMessages(operateur)
  return (
    <Activite
      titre='Messagerie'
      couleur='var(--chart-6)'
      libelle='Nouveaux messages'
      singulier='Nouveau message'
      icone={Mail}
      valeur={data?.nombre}
      lien={{ to: '/portail/messagerie' }}
    />
  )
}

/**
 * RG-CA-25 : ses projets de contrat qui attendent sa partie, puis ceux qui attendent l'autre partie, puis ses contrats
 * conclus dont une retiraison attend sa validation ; chacun ouvre son onglet de la liste.
 */
function ActivitesContrats({ operateur }: { operateur: number }) {
  const contrats = useMesContrats(operateur).data
  return (
    <>
      <Activite
        libelle='Contrats à approuver'
        singulier='Contrat à approuver'
        icone={Handshake}
        valeur={contrats?.filter((c) => c.onglet === 'A_APPROUVER').length}
        lien={{ to: '/portail/contrats' }}
      />
      <Activite
        libelle="Contrats en attente d'approbation"
        singulier="Contrat en attente d'approbation"
        icone={Hourglass}
        valeur={contrats?.filter((c) => c.onglet === 'EN_NEGOCIATION').length}
        lien={{
          to: '/portail/contrats',
          search: { onglet: 'EN_NEGOCIATION' },
        }}
      />
      <Activite
        libelle='Retiraisons à valider'
        singulier='Retiraison à valider'
        icone={Truck}
        valeur={contrats?.filter((c) => c.retiraisonAValider).length}
        lien={{ to: '/portail/contrats', search: { onglet: 'CONCLUS' } }}
      />
    </>
  )
}
