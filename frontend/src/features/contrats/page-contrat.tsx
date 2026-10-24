import { type ReactNode, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import {
  AlertTriangle,
  Ban,
  Check,
  FileDown,
  Hourglass,
  Pencil,
  X,
} from 'lucide-react'
import { toast } from 'sonner'
import {
  type ChangementContrat,
  type Contrat,
  type EcartLigneContrat,
  type LigneContrat,
  type PartieContrat,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { cn } from '@/lib/utils'
import { useRetour } from '@/hooks/use-retour'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { BadgePlein } from '@/components/metier/badge-statut'
import { PageMetier } from '@/components/metier/page-metier'
import { ActionDanger, ZoneDanger } from '@/components/metier/zone-danger'
import {
  formatDate,
  formatDateHeure,
  formatEuros,
} from '@/features/declarations/format'
import {
  apiContrats,
  cleContrats,
  type Espace,
  urlPdf,
  useContrat,
} from './api'
import {
  designation,
  formatDegre,
  formatHl,
  formatHlAp,
  formatLeA,
  formatPrix,
  formatPrixLitre,
  valeurChamp,
} from './format'
import { APPELLATIONS, CHAMPS, ETATS, PARTIES, SOLDES } from './libelles'
import {
  AccesRetiraisons,
  CarteRetiraisons,
  CloturerRetiraisons,
} from './retiraisons'
import { Surligne } from './surligne'

/**
 * Le détail d'un contrat (spec 06 §6.2) : ce qu'il attend, ses parties, ses lignes et leurs calculs, ses conditions,
 * ses approbations et son journal. À la partie qui doit approuver, l'écart avec la dernière version qu'elle a approuvée
 * est surligné, l'avant barré (RG-CA-09), et les actions Approuver, Modifier, Refuser sont proposées ; l'autre partie
 * peut encore modifier. Au vendeur, un projet que son stock estimé ne couvre plus est signalé, et ne s'approuve pas
 * (RG-CA-12). Conclu, il reçoit des retiraisons (RG-CA-13 à 16) : dans la page sur ordinateur, dans leur page sur
 * mobile (« Gérer les retiraisons », sous ce qu'il attend), et en bas, « Fin des retiraisons » les clôture (en bas de
 * la page des retiraisons sur mobile) et la zone à risque l'annule (RG-CA-17). Au back-office, la même page, en lecture (RG-CA-01).
 */
export function PageContrat({
  espace,
  operateur,
  id,
}: {
  espace: Espace
  operateur: number
  id: number
}) {
  const contrat = useContrat(espace, operateur, id)
  const retour = useRetour(
    espace === 'bo' ? { to: '/bo/contrats' } : { to: '/portail/contrats' }
  )
  if (contrat.isPending) return <Skeleton className='m-4 h-96' />
  if (contrat.isError || !contrat.data)
    return (
      <p className='text-muted-foreground p-6'>Ce contrat est introuvable.</p>
    )
  return (
    <DetailContrat
      espace={espace}
      operateur={operateur}
      contrat={contrat.data}
      retour={retour}
    />
  )
}

function DetailContrat({
  espace,
  operateur,
  contrat: c,
  retour,
}: {
  espace: Espace
  operateur: number
  contrat: Contrat
  retour: () => void
}) {
  const v = c.version
  const ecart = c.ecart
  const champ = (nom: string) => ecart?.champs.find((x) => x.champ === nom)
  const aMoiDApprouver =
    c.etat === 'PROJET' && c.attend !== undefined && c.attend === c.maPartie
  const queryClient = useQueryClient()
  // RG-CA-20 : le PDF au formulaire CA-05 allégé ; au portail, l'édition entre au journal (RG-CA-19).
  const editerPdf = () => {
    window.open(urlPdf(espace, operateur, c.id), '_blank', 'noopener')
    if (espace === 'portail')
      setTimeout(
        () => queryClient.invalidateQueries({ queryKey: cleContrats }),
        1500
      )
  }
  return (
    <PageMetier
      titre={`Contrat ${c.numero}`}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <BadgePlein statut={ETATS[c.etat].statut}>
            {ETATS[c.etat].libelle}
          </BadgePlein>
          <span>
            Campagne {c.campagne}
            {c.maPartie && ` · vous êtes ${PARTIES[c.maPartie].toLowerCase()}`}
          </span>
        </span>
      }
      retour={{ libelle: 'Retour', onRetour: retour }}
      actions={
        <div className='flex w-full flex-wrap gap-2 md:w-auto'>
          {espace === 'portail' && c.peutAgir && c.etat === 'PROJET' && (
            <Actions operateur={operateur} contrat={c} />
          )}
          {/* Seul, le PDF reste à droite ; avec Approuver, Modifier, Refuser, il partage la ligne. */}
          <Button
            variant='outline'
            onClick={editerPdf}
            className='max-md:flex-1 max-md:only:ms-auto max-md:only:flex-none'
          >
            <FileDown /> PDF
          </Button>
        </div>
      }
    >
      <div className='space-y-4'>
        <Situation contrat={c} aMoiDApprouver={aMoiDApprouver} />
        {c.stockInsuffisant && (
          <Alert variant='destructive'>
            <AlertTriangle />
            <AlertTitle>Vous ne pouvez pas approuver ce projet</AlertTitle>
            <AlertDescription>
              <p>{c.stockInsuffisant}</p>
              <p>Modifiez-le pour le ramener à votre stock, ou refusez-le.</p>
            </AlertDescription>
          </Alert>
        )}
        <AccesRetiraisons espace={espace} contrat={c} />
        <Card>
          <CardHeader>
            <CardTitle>Parties</CardTitle>
          </CardHeader>
          <CardContent className='grid gap-4 @min-[40rem]/content:grid-cols-2'>
            <BlocPartie
              titre='Vendeur'
              partie={c.vendeur}
              moi={c.maPartie === 'VENDEUR'}
            />
            <BlocPartie
              titre='Acheteur'
              partie={c.acheteur}
              moi={c.maPartie === 'ACHETEUR'}
            />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Lignes</CardTitle>
          </CardHeader>
          <CardContent>
            <Lignes
              lignes={v.lignes}
              ecart={ecart?.lignes ?? []}
              volumeAp={v.volumeAp}
              montantHt={v.montantHt}
            />
          </CardContent>
        </Card>
        <CarteRetiraisons
          espace={espace}
          operateur={operateur}
          contrat={c}
          className='max-md:hidden'
        />
        <Card>
          <CardHeader>
            <CardTitle>Conditions</CardTitle>
          </CardHeader>
          <CardContent className='space-y-3 text-sm'>
            <Condition
              titre='Caractéristiques complémentaires'
              changement={champ('caracteristiques')}
            >
              {v.caracteristiques}
            </Condition>
            <Condition
              titre='Conditions et calendrier de retiraison'
              changement={champ('conditionsRetiraison')}
            >
              {v.conditionsRetiraison}
            </Condition>
            <Condition
              titre='À retirer avant le'
              changement={champ('aRetirerAvantLe')}
            >
              {v.aRetirerAvantLe && formatDate(v.aRetirerAvantLe)}
            </Condition>
            <Condition
              titre='Paiement'
              changement={champ('solde') ?? champ('acompte')}
            >
              {SOLDES[v.solde]}
              {v.acompte != null && `, acompte de ${v.acompte} %`}
            </Condition>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Journal</CardTitle>
          </CardHeader>
          <CardContent>
            <ol className='space-y-2 text-sm'>
              {c.journal.map((e, i) => (
                <li key={i} className='flex flex-wrap gap-x-2'>
                  <span className='text-muted-foreground tabular-nums'>
                    {formatDateHeure(e.date)}
                  </span>
                  <span className='font-medium'>
                    {e.libelle}
                    {e.detail != null &&
                      ` (${e.action.startsWith('RETIRAISON') || e.action === 'EXECUTION' ? 'retiraison' : 'version'} ${e.detail})`}
                  </span>
                  <span className='text-muted-foreground'>
                    {e.nom}
                    {e.partie && `, ${PARTIES[e.partie].toLowerCase()}`}
                  </span>
                </li>
              ))}
            </ol>
          </CardContent>
        </Card>
      </div>
      {espace === 'portail' && c.peutAgir && c.etat === 'CONCLU' && (
        <>
          {/* Sur téléphone, la clôture est en bas de la page des retiraisons. */}
          <CloturerRetiraisons
            operateur={operateur}
            contrat={c}
            className='max-md:hidden'
          />
          <AnnulationContrat operateur={operateur} contrat={c} />
        </>
      )}
    </PageMetier>
  )
}

/** Ce que le contrat attend, et de qui ; l'écart en résumé pour la partie qui doit approuver. */
function Situation({
  contrat: c,
  aMoiDApprouver,
}: {
  contrat: Contrat
  aMoiDApprouver: boolean
}) {
  const nomDe = (partie: string) =>
    partie === 'ACHETEUR' ? c.acheteur.raisonSociale : c.vendeur.raisonSociale
  const approbations = c.approbations
    .map(
      (a) =>
        `${a.nom} (${PARTIES[a.partie].toLowerCase()}) le ${formatDateHeure(a.le)}`
    )
    .join(' ; ')
  if (c.etat === 'CONCLU' || c.etat === 'EXECUTE')
    return (
      <Alert variant='success'>
        <Check />
        <AlertTitle>
          {c.etat === 'CONCLU' ? 'Conclu' : 'Exécuté'} le{' '}
          {formatDate(c.dateContrat)}
        </AlertTitle>
        <AlertDescription>
          Approuvé par {approbations}. Montant :{' '}
          {formatEuros(c.version.montantHt)} HT.
        </AlertDescription>
      </Alert>
    )
  if (c.etat !== 'PROJET') {
    const action = c.etat === 'REFUSE' ? 'REFUS' : 'ANNULATION'
    const fin = [...c.journal].reverse().find((e) => e.action === action)
    return (
      <Alert variant='destructive'>
        <X />
        <AlertTitle>
          {ETATS[c.etat].libelle}
          {fin &&
            ` par ${fin.nom}${fin.partie ? ` (${PARTIES[fin.partie].toLowerCase()})` : ''} ${formatLeA(fin.date)}`}
        </AlertTitle>
      </Alert>
    )
  }
  const ecart = c.ecart
  const modifs = ecart && ecart.depuis < ecart.jusqua
  return (
    <Alert variant={aMoiDApprouver ? 'warning' : 'default'}>
      <Hourglass />
      <AlertTitle>
        {aMoiDApprouver
          ? 'Ce projet attend votre approbation'
          : `En attente de l'approbation ${c.attend === 'VENDEUR' ? 'du vendeur' : "de l'acheteur"}, ${nomDe(c.attend!)}`}
      </AlertTitle>
      <AlertDescription>
        <p>
          Version {c.version.numero}, par {c.version.auteur.nom} le{' '}
          {formatDateHeure(c.version.auteur.le)}.
          {modifs &&
            ` Modifiée depuis la version ${ecart.depuis}${aMoiDApprouver ? ' que vous avez approuvée' : ''} : les changements sont surlignés.`}
        </p>
        {modifs && ecart.champs.length > 0 && (
          <ul className='mt-1 list-disc ps-5'>
            {ecart.champs.map((ch) => (
              <li key={ch.champ}>
                {CHAMPS[ch.champ] ?? ch.champ} :{' '}
                <del className='text-muted-foreground'>
                  {valeurChamp(ch.champ, ch.avant)}
                </del>{' '}
                → <strong>{valeurChamp(ch.champ, ch.apres)}</strong>
              </li>
            ))}
          </ul>
        )}
      </AlertDescription>
    </Alert>
  )
}

/** Approuver, Modifier, Refuser (RG-CA-07, 08, 10), selon que ma partie est attendue ou non. */
function Actions({
  operateur,
  contrat: c,
}: {
  operateur: number
  contrat: Contrat
}) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [refus, setRefus] = useState(false)
  const api = apiContrats(operateur)
  const fin = (message: string) => () => {
    toast.success(message)
    queryClient.invalidateQueries({ queryKey: cleContrats })
  }
  const erreur = (e: unknown) => {
    const probleme = lireProbleme(e)
    toast.error(probleme.detail ?? probleme.titre)
    queryClient.invalidateQueries({ queryKey: cleContrats })
  }
  const approuver = useMutation({
    mutationFn: () => api.approuver(c.id, c.version.numero),
    onSuccess: fin('Contrat conclu.'),
    onError: erreur,
    meta: { erreurAffichee: true },
  })
  const refuser = useMutation({
    mutationFn: () => api.refuser(c.id, c.version.numero),
    onSuccess: () => {
      setRefus(false)
      fin('Projet refusé.')()
    },
    onError: erreur,
    meta: { erreurAffichee: true },
  })
  const attendu = c.attend === c.maPartie
  return (
    <div className='flex w-full flex-wrap gap-2 md:w-auto'>
      {attendu && (
        <Button
          onClick={() => approuver.mutate()}
          disabled={approuver.isPending || Boolean(c.stockInsuffisant)}
          className='max-md:flex-1'
        >
          <Check /> Approuver
        </Button>
      )}
      <Button
        variant='outline'
        className='max-md:flex-1'
        onClick={() =>
          navigate({
            to: '/portail/contrats/$id/modifier',
            params: { id: String(c.id) },
          })
        }
      >
        <Pencil /> Modifier
      </Button>
      {attendu && (
        <Button
          variant='outline'
          className='text-destructive max-md:flex-1'
          onClick={() => setRefus(true)}
        >
          <X /> Refuser
        </Button>
      )}
      <ConfirmDialog
        open={refus}
        onOpenChange={setRefus}
        feuilleMobile
        title={`Refuser le projet ${c.numero} ?`}
        desc="Le refus est définitif : le projet ne pourra plus être modifié ni approuvé. Pour proposer d'autres conditions, modifiez-le plutôt."
        confirmText='Refuser'
        destructive
        isLoading={refuser.isPending}
        handleConfirm={() => refuser.mutate()}
      />
    </div>
  )
}

/**
 * Contrat conclu, en bas de la page : « Annuler le contrat » (RG-CA-17), dans la zone à risque ; ses retiraisons non
 * validées tombent, les validées restent. « Fin des retiraisons », au-dessus, les clôture.
 */
function AnnulationContrat({
  operateur,
  contrat: c,
}: {
  operateur: number
  contrat: Contrat
}) {
  const queryClient = useQueryClient()
  const [confirmation, setConfirmation] = useState(false)
  const api = apiContrats(operateur)
  const enCours = c.retiraisons.some((r) => r.etat !== 'VALIDEE')
  const annuler = useMutation({
    mutationFn: () => api.annuler(c.id),
    onSuccess: () => {
      setConfirmation(false)
      toast.success('Contrat annulé.')
      queryClient.invalidateQueries({ queryKey: cleContrats })
    },
    onError: (e) => {
      const probleme = lireProbleme(e)
      toast.error(probleme.detail ?? probleme.titre)
      queryClient.invalidateQueries({ queryKey: cleContrats })
    },
    meta: { erreurAffichee: true },
  })
  const suite = enCours
    ? ' Ses retiraisons non validées sont supprimées ; les validées restent.'
    : ' Ses retiraisons validées restent.'
  return (
    <ZoneDanger>
      <ActionDanger
        titre='Annuler le contrat'
        description={`L'annulation est définitive ; l'autre partie en est avertie par e-mail.${suite}`}
      >
        <Button variant='destructive' onClick={() => setConfirmation(true)}>
          <Ban /> Annuler le contrat
        </Button>
      </ActionDanger>
      <ConfirmDialog
        open={confirmation}
        onOpenChange={setConfirmation}
        feuilleMobile
        title={`Annuler le contrat ${c.numero} ?`}
        desc={`L'annulation est définitive ; l'autre partie en est avertie par e-mail.${suite}`}
        confirmText='Annuler le contrat'
        destructive
        isLoading={annuler.isPending}
        handleConfirm={() => annuler.mutate()}
      />
    </ZoneDanger>
  )
}

function BlocPartie({
  titre,
  partie,
  moi,
  children,
}: {
  titre: string
  partie: PartieContrat
  moi: boolean
  children?: ReactNode
}) {
  return (
    <div className='space-y-1'>
      <div className='text-muted-foreground text-xs font-medium tracking-wide uppercase'>
        {titre}
        {moi && ' · vous'}
      </div>
      <div className='font-medium'>{partie.raisonSociale}</div>
      <div className='text-muted-foreground text-sm'>
        {[partie.code, partie.ville].filter(Boolean).join(' · ')}
      </div>
      {children}
    </div>
  )
}

function Condition({
  titre,
  changement,
  children,
}: {
  titre: string
  changement?: ChangementContrat
  children: ReactNode
}) {
  const vide = children === null || children === undefined || children === ''
  if (vide && !changement) return null
  return (
    <div>
      <div className='font-medium'>{titre}</div>
      <div className='text-muted-foreground whitespace-pre-line'>
        <Surligne changement={changement}>{vide ? '—' : children}</Surligne>
      </div>
    </div>
  )
}

/**
 * Les lignes et leurs calculs (RG-CA-04) ; les champs changés surlignés, les lignes ajoutées marquées, les lignes
 * retirées barrées. Une ligne par bloc : deux colonnes de valeurs sur un contenu étroit, quatre au-delà.
 */
function Lignes({
  lignes,
  ecart,
  volumeAp,
  montantHt,
}: {
  lignes: LigneContrat[]
  ecart: EcartLigneContrat[]
  volumeAp: number
  montantHt: number
}) {
  const retirees = ecart.filter((e) => e.nature === 'RETIREE')
  return (
    <div className='space-y-3'>
      {lignes.map((l, rang) => {
        const e = ecart.find((x) => x.rang === rang)
        const ch = (nom: string) =>
          e?.nature === 'MODIFIEE'
            ? e.champs.find((x) => x.champ === nom)
            : undefined
        return (
          <div
            key={rang}
            className={cn(
              'rounded-md border p-3',
              e?.nature === 'AJOUTEE' && 'border-warning-border bg-warning/40'
            )}
          >
            <div className='mb-2 flex flex-wrap items-baseline gap-x-2'>
              <span className='font-medium'>
                <Surligne
                  changement={
                    ch('millesime') ?? ch('denomination') ?? ch('compte')
                  }
                >
                  {designation(l)}
                </Surligne>
              </span>
              <span className='text-muted-foreground text-sm'>
                <Surligne changement={ch('appellation')}>
                  {APPELLATIONS[l.appellation]}
                </Surligne>
                {l.millesime != null && `, compte ${l.compte}`}
              </span>
              {e?.nature === 'AJOUTEE' && (
                <span className='text-warning-foreground text-sm font-medium'>
                  Ligne ajoutée
                </span>
              )}
            </div>
            <dl className='grid gap-x-6 gap-y-1 text-sm @min-[30rem]/content:grid-cols-2 @min-[48rem]/content:grid-cols-3'>
              <Valeur titre='Volume'>
                <Surligne changement={ch('volume')}>
                  {formatHl(l.volume)}
                </Surligne>
              </Valeur>
              <Valeur titre='Degré'>
                <Surligne changement={ch('degre')}>
                  {formatDegre(l.degre)}
                </Surligne>
              </Valeur>
              <Valeur titre='Volume AP'>{formatHlAp(l.volumeAp)}</Valeur>
              <Valeur titre='Prix'>
                <Surligne changement={ch('prix')}>
                  {formatPrix(l.prix)}
                </Surligne>
              </Valeur>
              <Valeur titre='Prix au litre'>
                {formatPrixLitre(l.prixLitreVolume)}
              </Valeur>
              <Valeur titre='Montant HT' fort>
                {formatEuros(l.montantHt)}
              </Valeur>
            </dl>
          </div>
        )
      })}
      {retirees.map((e) => (
        <div
          key={`retiree-${e.rang}`}
          className='text-muted-foreground rounded-md border border-dashed p-3 text-sm'
        >
          <span className='font-medium'>Ligne retirée : </span>
          <del>
            {e.champs
              .filter((x) => x.avant != null)
              .map(
                (x) =>
                  `${CHAMPS[x.champ] ?? x.champ} ${valeurChamp(x.champ, x.avant)}`
              )
              .join(' · ')}
          </del>
        </div>
      ))}
      <div className='flex flex-wrap justify-between gap-2 border-t pt-3 font-medium'>
        <span>Total : {formatHlAp(volumeAp)}</span>
        <span className='tabular-nums'>{formatEuros(montantHt)} HT</span>
      </div>
    </div>
  )
}

function Valeur({
  titre,
  fort,
  children,
}: {
  titre: string
  fort?: boolean
  children: ReactNode
}) {
  return (
    <div className='flex justify-between gap-2'>
      <dt className='text-muted-foreground'>{titre}</dt>
      <dd className={cn('tabular-nums', fort && 'font-semibold')}>
        {children}
      </dd>
    </div>
  )
}
