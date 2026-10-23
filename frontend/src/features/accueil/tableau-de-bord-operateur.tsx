import { useQuery } from '@tanstack/react-query'
import {
  FilePenLine,
  FileSearch,
  FileText,
  Handshake,
  Hourglass,
  Mail,
  ShoppingBag,
  Stamp,
} from 'lucide-react'
import { listerMesFactures } from '@/api/generated'
import { Skeleton } from '@/components/ui/skeleton'
import { useMesContrats } from '@/features/contrats/api'
import {
  apiDeclarations,
  cleDeclarations,
  useDsms,
} from '@/features/declarations/api'
import { dansGroupe } from '@/features/declarations/libelles'
import { cleFacturation } from '@/features/facturation/api'
import { useNouveauxMessages } from '@/features/messagerie/nouveaux'
import { useDroits, useMoi } from '@/features/session/session'
import { Activite, GrilleActivites, Section } from './activite'

/** Chiffres de démonstration, pour les contextes qui n'existent pas encore. */
const DEMO = {
  certificatsExport: 1,
  commandesBoutique: 1,
}

/**
 * Tableau de bord du portail, pendant de celui du back-office : les activités de l'opérateur actif. Ses DSM à
 * saisir ou compléter et celles qui attendent le BNIA, pour qui lit ses déclarations ; ses factures non payées,
 * pour qui lit sa facturation ; ses nouveaux messages (RG-MSG-11) ; ses contrats à approuver et ceux en attente de l'autre partie (RG-CA-25). Les certificats export et commandes
 * boutique sont des données de démonstration, marquées comme telles.
 */
export function TableauDeBordOperateur() {
  const { actif } = useMoi()
  if (!actif) return null
  return <Activites key={actif.operateur} operateur={actif.operateur} />
}

function Activites({ operateur }: { operateur: number }) {
  const droits = useDroits(operateur)
  if (!droits) return <Skeleton className='h-48 w-full' />
  return (
    <Section titre='Activités'>
      <GrilleActivites>
        {droits.declarations !== 'AUCUN' && (
          <ActivitesDsm operateur={operateur} />
        )}
        {droits.facturation !== 'AUCUN' && (
          <FacturesNonPayees operateur={operateur} />
        )}
        <NouveauxMessages operateur={operateur} />
        {droits.contrats !== 'AUCUN' && (
          <ActivitesContrats operateur={operateur} />
        )}
        <Activite
          libelle='Demandes de certificat export en cours'
          icone={Stamp}
          valeur={DEMO.certificatsExport}
          demo
        />
        <Activite
          libelle='Commandes boutique en attente'
          icone={ShoppingBag}
          valeur={DEMO.commandesBoutique}
          demo
        />
      </GrilleActivites>
    </Section>
  )
}

/**
 * Ses DSM, comptées comme au back-office (RG-DEC-40) : à saisir ou compléter (la période attendue qu'il peut
 * initier, ses brouillons, ses compléments demandés), puis envoyées au BNIA et pas encore validées.
 */
function ActivitesDsm({ operateur }: { operateur: number }) {
  const dsms = useDsms(operateur).data
  const situation = useQuery({
    queryKey: [...cleDeclarations(operateur), 'situation'],
    queryFn: apiDeclarations(operateur).situation,
  }).data
  const aSaisir =
    dsms && situation
      ? dsms.filter(dansGroupe('A_SAISIR')).length +
        (situation.initiable ? 1 : 0)
      : undefined
  return (
    <>
      <Activite
        libelle='DSM à saisir ou compléter'
        icone={FilePenLine}
        valeur={aSaisir}
        lien={{ to: '/portail/declarations' }}
      />
      <Activite
        libelle='DSM en attente du BNIA'
        icone={FileSearch}
        valeur={dsms?.filter(dansGroupe('A_TRAITER')).length}
        lien={{ to: '/portail/declarations' }}
      />
    </>
  )
}

/** Ses factures émises que Zeendoc n'a pas encore vues réglées (RG-FAC-33). */
function FacturesNonPayees({ operateur }: { operateur: number }) {
  const factures = useQuery({
    queryKey: [...cleFacturation, 'portail', operateur, { sens: 'FACTURE' }],
    queryFn: () =>
      listerMesFactures({
        path: { operateur },
        query: { sens: 'FACTURE' },
        throwOnError: true,
      }).then((r) => r.data),
  })
  return (
    <Activite
      libelle='Factures non payées'
      icone={FileText}
      valeur={factures.data?.filter((f) => !f.payeeLe).length}
      lien={{ to: '/portail/factures' }}
    />
  )
}

/** Spec 15 §6.2, RG-MSG-11 : ses messages non lus ; un clic ouvre la messagerie, non lues en gras. */
function NouveauxMessages({ operateur }: { operateur: number }) {
  const { data } = useNouveauxMessages(operateur)
  return (
    <Activite
      libelle='Nouveaux messages'
      icone={Mail}
      valeur={data?.nombre}
      lien={{ to: '/portail/messagerie' }}
    />
  )
}

/**
 * RG-CA-25 : ses projets de contrat qui attendent sa partie, puis ceux qui attendent l'autre partie ; chacun ouvre son
 * onglet de la liste.
 */
function ActivitesContrats({ operateur }: { operateur: number }) {
  const contrats = useMesContrats(operateur).data
  return (
    <>
      <Activite
        libelle='Contrats à approuver'
        icone={Handshake}
        valeur={contrats?.filter((c) => c.onglet === 'A_APPROUVER').length}
        lien={{ to: '/portail/contrats' }}
      />
      <Activite
        libelle="Contrats en attente d'approbation"
        icone={Hourglass}
        valeur={contrats?.filter((c) => c.onglet === 'EN_NEGOCIATION').length}
        lien={{
          to: '/portail/contrats',
          search: { onglet: 'EN_NEGOCIATION' },
        }}
      />
    </>
  )
}
