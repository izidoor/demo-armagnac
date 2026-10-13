import { useEffect, useMemo, useState } from 'react'
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import {
  ArrowLeft,
  CircleCheck,
  CircleHelp,
  History,
  TriangleAlert,
} from 'lucide-react'
import { toast } from 'sonner'
import { type AnomalieDsm, type Dsm } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import { TabsContent } from '@/components/ui/tabs'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { OngletsClasseur } from '@/components/metier/onglets-classeur'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits } from '@/features/session/session'
import { AideDsm } from './aide-dsm'
import { apiDeclarations, cleDeclarations, type Espace } from './api'
import {
  type Brouillon,
  brouillonDe,
  brouillonLisible,
  saisieDe,
  sommes,
} from './brouillon'
import { ListeDenominations } from './champs-identite'
import { DialogueLigne } from './dialogue-ligne'
import { formatDateHeure } from './format'
import { GrilleDsm, type TriLignes } from './grille-dsm'
import { TableJournalDeclarations } from './journal'
import {
  ONGLETS,
  type Onglet,
  TYPES_DECLARANT,
  VARIANTE_ETAT,
} from './libelles'

/**
 * Écran d'une DSM, commun au back-office et au portail (spec 05 §6.3) : en-tête, cinq onglets du format
 * unique en classeur, panneau des anomalies ; l'historique, hors du formulaire, dans un panneau latéral.
 * L'opérateur qui gère le module saisit tant que la DSM est chez lui (RG-DEC-01, 21) ; l'opérateur ou le
 * BNIA supprime un brouillon (RG-DEC-22).
 */
export function PageDsm({
  espace,
  operateur,
  id,
}: {
  espace: Espace
  operateur: number
  id: number
}) {
  const requete = useQuery({
    queryKey: [...cleDeclarations(operateur), 'dsm', id],
    queryFn: () => apiDeclarations(operateur).consulter(id),
  })
  const droits = useDroits(operateur)
  const dsm = requete.data
  if (!dsm || !droits) {
    return (
      <PageMetier
        titre='DSM'
        description={
          requete.isError ? "La DSM n'a pas pu être chargée." : 'Chargement…'
        }
      >
        {null}
      </PageMetier>
    )
  }
  // RG-DEC-01, 21 : la saisie est à l'opérateur, par son propre rôle, jamais par celui du BNIA.
  const saisissable =
    dsm.modifiable && droits.role != null && droits.role !== 'OBSERVATEUR'
  const supprimable = dsm.supprimable && droits.declarations === 'GESTION'
  return (
    <EditeurDsm
      key={`${dsm.id}-${dsm.version}`}
      espace={espace}
      dsm={dsm}
      saisissable={saisissable}
      supprimable={supprimable}
    />
  )
}

function EditeurDsm({
  espace,
  dsm,
  saisissable,
  supprimable,
}: {
  espace: Espace
  dsm: Dsm
  saisissable: boolean
  supprimable: boolean
}) {
  const operateur = dsm.operateur
  const api = apiDeclarations(operateur)
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [brouillon, setBrouillon] = useState<Brouillon>(() => brouillonDe(dsm))
  const [modifie, setModifie] = useState(false)
  const [differe, setDiffere] = useState(brouillon)
  const [onglet, setOnglet] = useState<string>('RECAPITULATIF')
  const [ligneEnEvidence, setLigneEnEvidence] = useState<number | null>(null)
  const [tri, setTri] = useState<TriLignes>(null)
  const [ajout, setAjout] = useState(false)
  const [suppression, setSuppression] = useState(false)
  const [historiqueOuvert, setHistoriqueOuvert] = useState(false)
  const [aideOuverte, setAideOuverte] = useState(false)

  // Vue provisoire (décision du 2026-10-13) : 300 ms après la dernière frappe, le serveur recalcule.
  useEffect(() => {
    const minuteur = setTimeout(() => setDiffere(brouillon), 300)
    return () => clearTimeout(minuteur)
  }, [brouillon])
  const lisible = brouillonLisible(differe)
  const apercu = useQuery({
    queryKey: [...cleDeclarations(operateur), 'apercu', dsm.id, differe],
    queryFn: () => api.apercevoir(dsm.id, saisieDe(differe)),
    enabled: modifie && saisissable && lisible,
    placeholderData: keepPreviousData,
    staleTime: Infinity,
    retry: false,
  })
  const denominations = useQuery({
    queryKey: [...cleDeclarations(operateur), 'denominations'],
    queryFn: api.denominations,
    enabled: saisissable,
  })
  const historique = useQuery({
    queryKey: [...cleDeclarations(operateur), 'dsm', dsm.id, 'journal'],
    queryFn: () => api.journalDsm(dsm.id),
    enabled: historiqueOuvert,
  })

  const vue: Dsm = modifie && apercu.data ? apercu.data : dsm
  const calculs = useMemo(
    () => sommes(brouillon, dsm.colonnes),
    [brouillon, dsm.colonnes]
  )
  const anomalies = vue.anomalies
  const comptes = brouillon.lignes.map(
    (l, i) => (l.cle === 'MB' ? vue.lignes[i]?.compte : undefined) ?? l.compte
  )
  const erreurApercu =
    modifie && apercu.error ? lireProbleme(apercu.error) : null

  const changer = (suivant: Brouillon) => {
    setBrouillon(suivant)
    setModifie(true)
  }
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: cleDeclarations(operateur) })

  const enregistrement = useMutation({
    mutationFn: () => api.enregistrer(dsm.id, saisieDe(brouillon, dsm.version)),
    meta: { erreurAffichee: true },
    onSuccess: (enregistree) => {
      toast.success(
        enregistree.anomalies.length === 0
          ? 'DSM enregistrée.'
          : `DSM enregistrée, avec ${enregistree.anomalies.length} anomalie(s) à corriger avant l'envoi.`
      )
      rafraichir()
    },
  })
  const retour = () =>
    espace === 'bo'
      ? navigate({
          to: '/bo/operateurs/$id',
          params: { id: String(operateur) },
          search: { onglet: 'declarations' },
        })
      : navigate({ to: '/portail/declarations' })
  const effacement = useMutation({
    mutationFn: () => api.supprimer(dsm.id),
    onSuccess: () => {
      setSuppression(false)
      toast.success(`DSM de ${dsm.periode.libelle} supprimée.`)
      rafraichir()
      retour()
    },
    onError: () => setSuppression(false),
  })
  const erreurEnregistrement = enregistrement.error
    ? lireProbleme(enregistrement.error)
    : null

  const montrer = (a: AnomalieDsm) => {
    setOnglet(a.onglet)
    setLigneEnEvidence(a.ligne ?? null)
    if (a.ligne != null) {
      setTimeout(
        () =>
          document
            .getElementById(`ligne-${a.onglet}-${a.ligne}`)
            ?.scrollIntoView({ block: 'center' }),
        50
      )
    }
  }

  const volume = (ligne: number, colonne: number, texte: string) =>
    changer({
      ...brouillon,
      lignes: brouillon.lignes.map((l, i) =>
        i === ligne
          ? {
              ...l,
              volumes: l.volumes.map((v, j) => (j === colonne - 1 ? texte : v)),
            }
          : l
      ),
    })

  return (
    <PageMetier
      titre={`DSM de ${dsm.periode.libelle}`}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Button variant='link' className='h-auto p-0' onClick={retour}>
            <ArrowLeft className='size-4' />
            {espace === 'bo' ? dsm.raisonSociale : 'Mes DSM'}
          </Button>
          <span className='font-mono'>{dsm.codeOperateur}</span>
          <Badge variant='secondary'>{TYPES_DECLARANT[dsm.type]}</Badge>
          <Badge variant={VARIANTE_ETAT[dsm.etat]}>{dsm.libelleEtat}</Badge>
          {dsm.envoyeeLe && (
            <span>Envoyée le {formatDateHeure(dsm.envoyeeLe)}</span>
          )}
          {dsm.valideeLe && (
            <span>
              Validée le {formatDateHeure(dsm.valideeLe)} par {dsm.valideePar}
            </span>
          )}
          {modifie && (
            <Badge variant='outline'>Modifications non enregistrées</Badge>
          )}
        </span>
      }
      actions={
        <div className='flex gap-2'>
          <Button variant='outline' onClick={() => setAideOuverte(true)}>
            <CircleHelp className='size-4' /> Aide
          </Button>
          <Button variant='outline' onClick={() => setHistoriqueOuvert(true)}>
            <History className='size-4' /> Historique
          </Button>
          {saisissable && (
            <Button
              onClick={() => enregistrement.mutate()}
              disabled={
                !modifie ||
                !brouillonLisible(brouillon) ||
                enregistrement.isPending
              }
            >
              Enregistrer
            </Button>
          )}
          {supprimable && (
            <Button variant='destructive' onClick={() => setSuppression(true)}>
              Supprimer
            </Button>
          )}
        </div>
      }
    >
      <div className='mb-4 space-y-2'>
        {erreurEnregistrement && (
          <Alert variant='destructive'>
            <TriangleAlert />
            <AlertTitle>Enregistrement refusé</AlertTitle>
            <AlertDescription>
              {erreurEnregistrement.detail ?? erreurEnregistrement.titre}
            </AlertDescription>
          </Alert>
        )}
        {erreurApercu && (
          <Alert variant='destructive'>
            <TriangleAlert />
            <AlertTitle>Saisie à corriger</AlertTitle>
            <AlertDescription>
              {erreurApercu.detail ?? erreurApercu.titre}
            </AlertDescription>
          </Alert>
        )}
        <PanneauAnomalies anomalies={anomalies} onMontrer={montrer} />
      </div>
      <OngletsClasseur
        value={onglet}
        onValueChange={setOnglet}
        onglets={ONGLETS.map((o) => ({
          valeur: o.onglet,
          libelle: (
            <>
              <o.icone /> {o.libelle}
            </>
          ),
        }))}
      >
        {ONGLETS.map((o) => (
          <TabsContent key={o.onglet} value={o.onglet} className='space-y-4'>
            <div className='mb-8 space-y-1.5 border-b pt-2 pb-4'>
              <h2 className='text-xl font-semibold'>{o.libelle}</h2>
              <p className='text-muted-foreground text-sm'>{o.contexte}</p>
            </div>
            {o.onglet === 'MOUVEMENTS_SORTIE' && (
              <ChampDateDia
                valeur={brouillon.dateDia}
                saisissable={saisissable}
                onChange={(dateDia) => changer({ ...brouillon, dateDia })}
              />
            )}
            <GrilleDsm
              onglet={o.onglet as Onglet}
              colonnes={dsm.colonnes}
              lignes={brouillon.lignes}
              calculs={calculs}
              comptes={comptes}
              saisissable={saisissable}
              ligneEnEvidence={ligneEnEvidence}
              tri={tri}
              onTri={setTri}
              onVolume={volume}
              onAjouter={saisissable ? () => setAjout(true) : undefined}
              onSupprimer={(ligne) =>
                changer({
                  ...brouillon,
                  lignes: brouillon.lignes.filter((_, i) => i !== ligne),
                })
              }
            />
          </TabsContent>
        ))}
      </OngletsClasseur>
      <AideDsm open={aideOuverte} onOpenChange={setAideOuverte} />
      <Sheet open={historiqueOuvert} onOpenChange={setHistoriqueOuvert}>
        <SheetContent className='w-full overflow-y-auto sm:max-w-2xl'>
          <SheetHeader>
            <SheetTitle>Historique</SheetTitle>
            <SheetDescription>
              Les actions sur la DSM de {dsm.periode.libelle}, de la plus
              récente à la plus ancienne.
            </SheetDescription>
          </SheetHeader>
          <div className='px-4 pb-4'>
            <TableJournalDeclarations entrees={historique.data ?? []} />
          </div>
        </SheetContent>
      </Sheet>
      {saisissable && <ListeDenominations valeurs={denominations.data ?? []} />}
      {ajout && (
        <DialogueLigne
          onOpenChange={setAjout}
          onAjoute={(ligne) =>
            changer({
              ...brouillon,
              lignes: [
                ...brouillon.lignes,
                {
                  cle: ligne.cle,
                  millesime: ligne.millesime,
                  denomination: ligne.denomination,
                  compte: ligne.compte ?? '',
                  origine: 'AJOUT',
                  volumes: Array.from({ length: 26 }, () => ''),
                  stockDebut: 0,
                },
              ],
            })
          }
        />
      )}
      <ConfirmDialog
        open={suppression}
        onOpenChange={setSuppression}
        title='Supprimer la DSM'
        desc={`Le brouillon de ${dsm.periode.libelle} sera supprimé avec ses lignes et son historique ; la suppression reste tracée au journal de l'opérateur. La période pourra être réinitiée.`}
        confirmText='Supprimer'
        cancelBtnText='Annuler'
        destructive
        isLoading={effacement.isPending}
        handleConfirm={() => effacement.mutate()}
      />
    </PageMetier>
  )
}

/**
 * Date de DIA, facultative (RG-DEC-19, H-DEC-12) : celle de l'inventaire qui justifie les pertes de la
 * colonne 10, d'où sa place dans l'onglet des sorties.
 */
function ChampDateDia({
  valeur,
  saisissable,
  onChange,
}: {
  valeur: string
  saisissable: boolean
  onChange: (dateDia: string) => void
}) {
  return (
    <div className='space-y-1'>
      <Label htmlFor='date-dia'>Date de la DIA</Label>
      <Input
        id='date-dia'
        type='date'
        className='w-44'
        disabled={!saisissable}
        value={valeur}
        onChange={(e) => onChange(e.target.value)}
      />
      <p className='text-muted-foreground text-sm'>
        Facultative : date de l'inventaire qui justifie les pertes de la colonne
        10.
      </p>
    </div>
  )
}

/** Les anomalies de calcul (RG-DEC-18), bloquantes à l'envoi ; un clic mène à l'onglet et à la ligne. */
function PanneauAnomalies({
  anomalies,
  onMontrer,
}: {
  anomalies: AnomalieDsm[]
  onMontrer: (a: AnomalieDsm) => void
}) {
  if (anomalies.length === 0) {
    return (
      <Alert>
        <CircleCheck />
        <AlertTitle>Aucune anomalie</AlertTitle>
        <AlertDescription>
          Les contrôles de cohérence sont respectés.
        </AlertDescription>
      </Alert>
    )
  }
  return (
    <Alert variant='destructive'>
      <TriangleAlert />
      <AlertTitle>
        {anomalies.length === 1
          ? '1 anomalie, à corriger avant l’envoi'
          : `${anomalies.length} anomalies, à corriger avant l’envoi`}
      </AlertTitle>
      <AlertDescription>
        <ul className='mt-1 space-y-1'>
          {anomalies.map((a, i) => (
            <li key={i}>
              <button
                type='button'
                className='text-left underline-offset-2 hover:underline'
                onClick={() => onMontrer(a)}
              >
                {a.message}
              </button>
            </li>
          ))}
        </ul>
      </AlertDescription>
    </Alert>
  )
}
