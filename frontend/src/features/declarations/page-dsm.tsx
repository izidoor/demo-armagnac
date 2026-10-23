import { useEffect, useMemo, useState } from 'react'
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query'
import {
  Link,
  useCanGoBack,
  useNavigate,
  useRouter,
} from '@tanstack/react-router'
import {
  ArrowLeft,
  Building2,
  CircleCheck,
  ChevronDown,
  CircleHelp,
  Download,
  Ellipsis,
  FileDown,
  FileSpreadsheet,
  FileUp,
  FileWarning,
  History,
  Paperclip,
  Receipt,
  Send,
  TriangleAlert,
} from 'lucide-react'
import { toast } from 'sonner'
import { type AnomalieDsm, type Dsm, type ManqueDsm } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { cn } from '@/lib/utils'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
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
import { Textarea } from '@/components/ui/textarea'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { BadgePlein } from '@/components/metier/badge-statut'
import { OngletsClasseur } from '@/components/metier/onglets-classeur'
import { PageMetier } from '@/components/metier/page-metier'
import { ActionDanger, ZoneDanger } from '@/components/metier/zone-danger'
import { SectionMessages } from '@/features/messagerie/section-messages'
import { AideDsm } from './aide-dsm'
import {
  apiDeclarations,
  cleDeclarations,
  type Espace,
  usePiecesDsm,
} from './api'
import {
  type Brouillon,
  brouillonDe,
  brouillonLisible,
  saisieDe,
  sommes,
} from './brouillon'
import { ListeDenominations } from './champs-identite'
import { OngletCotisation } from './cotisation'
import { DialogueLigne } from './dialogue-ligne'
import { formatDate, formatDateHeure } from './format'
import { GrilleDsm, type TriLignes } from './grille-dsm'
import { DialogueImportExcel } from './import-excel'
import { TableJournalDeclarations } from './journal'
import { ONGLETS, type Onglet, TYPES_DECLARANT, STATUT_ETAT } from './libelles'
import { SectionNotesInternes } from './notes-internes'
import { OngletPieces } from './pieces'

/**
 * Écran d'une DSM, commun au back-office et au portail (spec 05 §6.3) : en-tête, cinq onglets du format
 * unique, les onglets des pièces et de la cotisation en classeur, panneau des anomalies et des manques,
 * messages et, au back-office, notes internes sous le classeur (spec 15) ; l'historique, hors du formulaire, dans un panneau latéral ; le PDF dans un
 * nouvel onglet du navigateur, l'Excel téléchargé ; l'import du modèle Excel (lot 6). Les actions viennent du serveur,
 * selon l'état et le côté de l'utilisateur (RG-DEC-01, 20 à 22, 24, 28) : l'opérateur saisit et envoie tant
 * que la DSM est chez lui ; le BNIA l'instruit ; l'un ou l'autre supprime un brouillon et dépose des pièces.
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
  const queryClient = useQueryClient()
  // L'import vit hors de l'éditeur, recréé à chaque version : ses avertissements restent à l'écran.
  const [importOuvert, setImportOuvert] = useState(false)
  const [modificationsPerdues, setModificationsPerdues] = useState(false)
  const dsm = requete.data
  if (!dsm) {
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
  return (
    <>
      <EditeurDsm
        key={`${dsm.id}-${dsm.version}`}
        espace={espace}
        dsm={dsm}
        onImporter={(modifie) => {
          setModificationsPerdues(modifie)
          setImportOuvert(true)
        }}
      />
      <DialogueImportExcel
        titre={`Importer le modèle Excel dans la DSM de ${dsm.periode.libelle}`}
        description={
          <>
            Le fichier remplace les colonnes 2 à 25 ; ses lignes se rapprochent
            de celles de la DSM par leur identité. Une ligne reprise absente du
            fichier reste à 0, une ligne ajoutée absente est supprimée. Les
            colonnes 1 et 26 et la date de DIA restent celles de la DSM. Un
            fichier en erreur est refusé en entier.
            {modificationsPerdues && (
              <strong className='mt-2 block'>
                Vos modifications non enregistrées seront perdues.
              </strong>
            )}
          </>
        }
        ouvert={importOuvert}
        onOuvertChange={setImportOuvert}
        importer={(fichier) =>
          apiDeclarations(operateur).importer(dsm.id, fichier, dsm.version)
        }
        onImporte={() =>
          queryClient.invalidateQueries({
            queryKey: cleDeclarations(operateur),
          })
        }
      />
    </>
  )
}

/** L'onglet des pièces, après les cinq du format unique (spec 05 §6.3). */
const PIECES = 'PIECES'

/** L'onglet de la cotisation, après celui des pièces (spec 05 §6.3, lot 4). */
const COTISATION = 'COTISATION'

/** Les transitions qui demandent une confirmation (RG-DEC-20). */
type Transition = 'envoi' | 'instruction' | 'complements' | 'validation'

function EditeurDsm({
  espace,
  dsm,
  onImporter,
}: {
  espace: Espace
  dsm: Dsm
  /** Ouvre l'import ; {@code modifie} : la saisie en cours sera perdue. */
  onImporter: (modifie: boolean) => void
}) {
  const permis = (action: Dsm['actions'][number]) =>
    dsm.actions.includes(action)
  // RG-DEC-01, 21 : la saisie est à l'opérateur, tant que la DSM est chez lui.
  const saisissable = permis('ENREGISTRER')
  const operateur = dsm.operateur
  const api = apiDeclarations(operateur)
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const router = useRouter()
  const peutRevenir = useCanGoBack()
  const [brouillon, setBrouillon] = useState<Brouillon>(() => brouillonDe(dsm))
  const [modifie, setModifie] = useState(false)
  const [differe, setDiffere] = useState(brouillon)
  const [onglet, setOnglet] = useState<string>('RECAPITULATIF')
  const [ligneEnEvidence, setLigneEnEvidence] = useState<number | null>(null)
  // RG-DEC-45 : par défaut, comme aux éditions PDF et Excel, les lignes par compte d'âge croissant.
  const [tri, setTri] = useState<TriLignes>({
    colonne: 'compte',
    sens: 'croissant',
  })
  const [ajout, setAjout] = useState(false)
  const [suppression, setSuppression] = useState(false)
  const [historiqueOuvert, setHistoriqueOuvert] = useState(false)
  const [aideOuverte, setAideOuverte] = useState(false)
  const [transition, setTransition] = useState<Transition | null>(null)
  const [motif, setMotif] = useState('')
  const [ligneAnnotee, setLigneAnnotee] = useState<number | null>(null)

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
  // Spec 15 §3.7 : le carnet de notes internes, au personnel du BNIA seulement.
  const notes = useQuery({
    queryKey: [...cleDeclarations(operateur), 'dsm', dsm.id, 'notes'],
    queryFn: () => api.notes(dsm.id),
    enabled: espace === 'bo',
  })
  const parLigne = useMemo(() => {
    const compte = new Map<number, number>()
    for (const n of notes.data ?? []) {
      if (n.ligne != null) compte.set(n.ligne, (compte.get(n.ligne) ?? 0) + 1)
    }
    return compte
  }, [notes.data])
  const pieces = usePiecesDsm(operateur, dsm.id)
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
  // RG-DEC-29 : les pièces manquantes bloquent la validation, pas l'envoi.
  const manques = vue.manques
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

  // RG-DEC-43 : on reste sur la DSM, qui montre ensuite « Facturation demandée » (décision du 2026-10-14).
  // Un opérateur non facturable (RG-OPE-02) : « Facturer » est désactivé, un bandeau dit ce qui manque.
  const nonFacturable = dsm.manquePourFacturer.length > 0
  const facturation = useMutation({
    mutationFn: () => api.facturer(dsm.id),
    onSuccess: () => {
      toast.success('Brouillon de facture créé.')
      rafraichir()
      queryClient.invalidateQueries({ queryKey: ['facturation'] })
    },
  })

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
  const ficheOperateur = () =>
    navigate({
      to: '/bo/operateurs/$id',
      params: { id: String(operateur) },
      search: { onglet: 'declarations' },
    })
  // L'écran d'où l'on vient (listing du BNIA, fiche opérateur, Mes DSM) ; à défaut (lien direct), la liste
  // des DSM de l'opérateur.
  const retour = () => {
    if (peutRevenir) router.history.back()
    else if (espace === 'bo') ficheOperateur()
    else navigate({ to: '/portail/declarations' })
  }
  // RG-DEC-35 : le PDF de la DSM enregistrée, pour tout lecteur ; l'édition entre au journal.
  const editerPdf = () => {
    window.open(api.urlPdf(dsm.id), '_blank', 'noopener')
    setTimeout(
      () =>
        queryClient.invalidateQueries({
          queryKey: [...cleDeclarations(operateur), 'dsm', dsm.id, 'journal'],
        }),
      1500
    )
  }
  // RG-DEC-36 : le classeur Excel de la DSM enregistrée, téléchargé ; l'édition entre au journal.
  const editerExcel = () =>
    setTimeout(
      () =>
        queryClient.invalidateQueries({
          queryKey: [...cleDeclarations(operateur), 'dsm', dsm.id, 'journal'],
        }),
      1500
    )
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

  // RG-DEC-20 : « Envoyer » enregistre d'abord la saisie en cours (décision du 2026-10-13).
  const changementEtat = useMutation({
    mutationFn: async (t: Transition) => {
      switch (t) {
        case 'envoi': {
          const version = modifie
            ? (await api.enregistrer(dsm.id, saisieDe(brouillon, dsm.version)))
                .version
            : dsm.version
          return api.envoyer(dsm.id, version)
        }
        case 'instruction':
          return api.prendreEnInstruction(dsm.id, dsm.version)
        case 'complements':
          return api.demanderComplements(dsm.id, motif, dsm.version)
        case 'validation':
          return api.valider(dsm.id, dsm.version)
      }
    },
    meta: { erreurAffichee: true },
    onSuccess: (_, t) => {
      setTransition(null)
      setMotif('')
      toast.success(
        {
          envoi: `DSM de ${dsm.periode.libelle} envoyée au BNIA.`,
          instruction: `DSM de ${dsm.periode.libelle} prise en charge.`,
          complements: `Compléments demandés à ${dsm.raisonSociale}.`,
          validation: `DSM de ${dsm.periode.libelle} validée.`,
        }[t]
      )
      rafraichir()
    },
    onError: () => {
      setTransition(null)
      rafraichir()
    },
  })
  const renvoi = dsm.etat === 'COMPLEMENTS_DEMANDES'
  const motifValidationImpossible =
    manques.length > 0
      ? `${manques.length === 1 ? 'Une pièce manque' : `${manques.length} pièces manquent`} : déposez-les dans l'onglet Pièces.`
      : null
  // Spec 06 RG-CA-24 : avant la fin de la période, « Envoyer » est montré, désactivé, avec la date d'envoi.
  const motifEnvoiImpossible = dsm.envoiPossibleLe
    ? `La DSM s'envoie à partir du ${formatDate(dsm.envoiPossibleLe)}, lendemain de la fin de sa période.`
    : anomalies.length > 0
      ? "Corrigez les anomalies avant l'envoi."
      : erreurApercu || !brouillonLisible(brouillon)
        ? "Corrigez la saisie avant l'envoi."
        : null

  const montrerLigne = (id: number) => {
    const rang = brouillon.lignes.findIndex((l) => l.id === id)
    if (rang < 0) return
    setLigneEnEvidence(rang)
    setTimeout(
      () =>
        document
          .getElementById(`ligne-${onglet}-${rang}`)
          ?.scrollIntoView({ block: 'center' }),
      50
    )
  }
  const montrerNotes = (ligne: number) => {
    setLigneAnnotee(ligne)
    document
      .getElementById('notes-internes')
      ?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

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
      complement={dsm.raisonSociale}
      retour={{ libelle: 'Retour', onRetour: retour }}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Button
            variant='link'
            className='h-auto p-0 max-md:hidden'
            onClick={retour}
          >
            <ArrowLeft className='size-4' />
            Retour
          </Button>
          {espace === 'bo' && (
            <Button
              variant='link'
              className='h-auto p-0'
              onClick={ficheOperateur}
            >
              <Building2 className='size-4' />
              Voir la fiche opérateur
            </Button>
          )}
          <Badge variant='secondary'>{TYPES_DECLARANT[dsm.type]}</Badge>
          <BadgePlein statut={STATUT_ETAT[dsm.etat]}>
            {dsm.libelleEtat}
          </BadgePlein>
          {dsm.valideeLe && (
            <span>
              Validée le {formatDateHeure(dsm.valideeLe)} par {dsm.valideePar}
            </span>
          )}
          {espace === 'bo' && dsm.brouillonFacture != null && (
            <Link
              to='/bo/facturation/$id'
              params={{ id: String(dsm.brouillonFacture) }}
            >
              <Badge variant='outline' className='gap-1'>
                <Receipt className='size-3' /> Facturation demandée : voir le
                brouillon
              </Badge>
            </Link>
          )}
          {modifie && (
            <Badge variant='outline'>Modifications non enregistrées</Badge>
          )}
        </span>
      }
      actions={
        <div className='ms-auto flex flex-wrap justify-end gap-2'>
          {/* Sur un téléphone, les actions de consultation passent dans « Plus » : restent les actions sur la DSM. */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' className='md:hidden'>
                <Ellipsis className='size-4' /> Plus
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='start'>
              <ElementsExport
                urlExcel={api.urlExcel(dsm.id)}
                onExcel={editerExcel}
                onPdf={editerPdf}
                modifie={modifie}
              />
              <DropdownMenuSeparator />
              {saisissable && (
                <DropdownMenuItem onSelect={() => onImporter(modifie)}>
                  <FileUp /> Importer
                </DropdownMenuItem>
              )}
              <DropdownMenuItem onSelect={() => setHistoriqueOuvert(true)}>
                <History /> Historique
              </DropdownMenuItem>
              <DropdownMenuItem onSelect={() => setAideOuverte(true)}>
                <CircleHelp /> Aide
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          {/* Exporter : le classeur au format du modèle, ou le PDF (RG-DEC-35, 36). */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' className='max-md:hidden'>
                <Download className='size-4' /> Exporter
                <ChevronDown className='size-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <ElementsExport
                urlExcel={api.urlExcel(dsm.id)}
                onExcel={editerExcel}
                onPdf={editerPdf}
                modifie={modifie}
              />
            </DropdownMenuContent>
          </DropdownMenu>
          <Button
            variant='outline'
            className='max-md:hidden'
            onClick={() => setHistoriqueOuvert(true)}
          >
            <History className='size-4' /> Historique
          </Button>
          <Button
            variant='outline'
            className='max-md:hidden'
            onClick={() => setAideOuverte(true)}
          >
            <CircleHelp className='size-4' /> Aide
          </Button>
          {saisissable && (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant='outline'
                  className='max-md:hidden'
                  onClick={() => onImporter(modifie)}
                >
                  <FileUp className='size-4' /> Importer
                </Button>
              </TooltipTrigger>
              <TooltipContent>
                Un classeur au format du modèle : ses volumes remplacent ceux de
                la DSM.
              </TooltipContent>
            </Tooltip>
          )}
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
          {(permis('ENVOYER') || dsm.envoiPossibleLe) && (
            <Tooltip>
              <TooltipTrigger asChild>
                <span tabIndex={motifEnvoiImpossible ? 0 : undefined}>
                  <Button
                    onClick={() => setTransition('envoi')}
                    disabled={motifEnvoiImpossible != null}
                  >
                    <Send className='size-4' />{' '}
                    {renvoi ? 'Renvoyer' : 'Envoyer'}
                  </Button>
                </span>
              </TooltipTrigger>
              {motifEnvoiImpossible && (
                <TooltipContent>{motifEnvoiImpossible}</TooltipContent>
              )}
            </Tooltip>
          )}
          {permis('PRENDRE_EN_INSTRUCTION') && (
            <Button onClick={() => setTransition('instruction')}>
              Prendre en charge
            </Button>
          )}
          {permis('DEMANDER_COMPLEMENTS') && (
            <Button
              variant='outline'
              onClick={() => setTransition('complements')}
            >
              Demander des compléments
            </Button>
          )}
          {permis('VALIDER') && (
            <Tooltip>
              <TooltipTrigger asChild>
                <span tabIndex={motifValidationImpossible ? 0 : undefined}>
                  <Button
                    onClick={() => setTransition('validation')}
                    disabled={motifValidationImpossible != null}
                  >
                    Valider
                  </Button>
                </span>
              </TooltipTrigger>
              {motifValidationImpossible && (
                <TooltipContent>{motifValidationImpossible}</TooltipContent>
              )}
            </Tooltip>
          )}
          {permis('FACTURER') && (
            <Tooltip>
              <TooltipTrigger asChild>
                <span tabIndex={nonFacturable ? 0 : undefined}>
                  <Button
                    onClick={() => facturation.mutate()}
                    disabled={nonFacturable || facturation.isPending}
                  >
                    <Receipt className='size-4' /> Facturer
                  </Button>
                </span>
              </TooltipTrigger>
              {nonFacturable && (
                <TooltipContent>
                  {dsm.raisonSociale} n'est pas facturable (RG-OPE-02).
                </TooltipContent>
              )}
            </Tooltip>
          )}
        </div>
      }
    >
      <div className='my-6 space-y-3 empty:hidden'>
        {nonFacturable && (
          <Alert variant='destructive'>
            <TriangleAlert />
            <AlertTitle>Opérateur non facturable</AlertTitle>
            <AlertDescription>
              <p>
                Il manque à la fiche de {dsm.raisonSociale} :{' '}
                {dsm.manquePourFacturer.join(', ')}. La DSM ne peut être
                facturée qu'une fois la fiche complétée (RG-OPE-02).
              </p>
              <Button
                variant='link'
                className='h-auto p-0'
                onClick={ficheOperateur}
              >
                Ouvrir la fiche de l'opérateur
              </Button>
            </AlertDescription>
          </Alert>
        )}
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
        <PanneauAnomalies
          anomalies={anomalies}
          manques={manques}
          onMontrer={montrer}
          onMontrerPieces={() => setOnglet(PIECES)}
        />
      </div>
      <OngletsClasseur
        value={onglet}
        onValueChange={setOnglet}
        onglets={[
          ...ONGLETS.map((o) => ({
            valeur: o.onglet as string,
            libelle: (
              <>
                <o.icone /> {o.libelle}
              </>
            ),
          })),
          {
            valeur: PIECES,
            libelle: (
              <>
                <Paperclip /> Pièces
                {manques.length > 0 && (
                  <Badge variant='destructive'>{manques.length}</Badge>
                )}
              </>
            ),
          },
          {
            valeur: COTISATION,
            libelle: (
              <>
                <Receipt /> Cotisation
              </>
            ),
          },
        ]}
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
              notes={parLigne}
              onNotes={montrerNotes}
            />
          </TabsContent>
        ))}
        <TabsContent value={PIECES} className='space-y-4'>
          <div className='mb-8 space-y-1.5 border-b pt-2 pb-4'>
            <h2 className='text-xl font-semibold'>Pièces</h2>
            <p className='text-muted-foreground text-sm'>
              Les justificatifs des mouvements et, pour un producteur, ses DRM :
              attendus avant la validation par le BNIA.
            </p>
          </div>
          <OngletPieces
            espace={espace}
            dsm={dsm}
            pieces={pieces.data}
            peutDeposer={permis('DEPOSER_PIECES')}
            onChange={rafraichir}
          />
        </TabsContent>
        <TabsContent value={COTISATION} className='space-y-4'>
          <div className='mb-8 space-y-1.5 border-b pt-2 pb-4'>
            <h2 className='text-xl font-semibold'>Cotisation</h2>
            <p className='text-muted-foreground text-sm'>
              La cotisation CVO sur les sorties de l'assiette (colonnes 14, 16 à
              19, 21 à 25), au taux en vigueur à la fin de la période.
            </p>
          </div>
          <OngletCotisation dsm={vue} liens={espace === 'bo'} />
        </TabsContent>
      </OngletsClasseur>
      <SectionMessages
        type='DSM'
        identifiant={String(dsm.id)}
        description={
          espace === 'bo'
            ? `Les échanges avec ${dsm.raisonSociale} sur cette DSM, dans tous ses états ; aussi dans la messagerie.`
            : 'Vos échanges avec le BNIA sur cette DSM, dans tous ses états ; aussi dans la messagerie.'
        }
      />
      {espace === 'bo' && (
        <SectionNotesInternes
          dsm={dsm}
          notes={notes.data ?? []}
          peutAnnoter={permis('ANNOTER')}
          ligneCiblee={ligneAnnotee}
          onMontrerLigne={montrerLigne}
          onChange={() =>
            queryClient.invalidateQueries({
              queryKey: [...cleDeclarations(operateur), 'dsm', dsm.id, 'notes'],
            })
          }
        />
      )}
      {permis('SUPPRIMER') && (
        <ZoneDanger>
          <ActionDanger
            titre='Supprimer la DSM'
            description="Le brouillon est supprimé avec ses lignes, ses messages, ses notes internes et son historique ; la suppression reste tracée au journal de l'opérateur, et la période pourra être réinitiée."
          >
            <Button variant='destructive' onClick={() => setSuppression(true)}>
              Supprimer
            </Button>
          </ActionDanger>
        </ZoneDanger>
      )}
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
        desc={`Le brouillon de ${dsm.periode.libelle} sera supprimé avec ses lignes, ses messages, ses notes internes et son historique ; la suppression reste tracée au journal de l'opérateur. La période pourra être réinitiée.`}
        confirmText='Supprimer'
        cancelBtnText='Annuler'
        destructive
        isLoading={effacement.isPending}
        handleConfirm={() => effacement.mutate()}
      />
      <ConfirmDialog
        open={transition === 'envoi'}
        onOpenChange={(open) => !open && setTransition(null)}
        title={renvoi ? 'Renvoyer la DSM' : 'Envoyer la DSM'}
        desc={`${modifie ? 'Vos modifications seront enregistrées, puis la' : 'La'} DSM de ${dsm.periode.libelle} sera ${renvoi ? 'renvoyée' : 'envoyée'} au BNIA. Vous ne pourrez plus la modifier, sauf s'il vous demande des compléments.${manques.length > 0 ? ` ${manques.length === 1 ? 'Une pièce manque' : `${manques.length} pièces manquent`} : le BNIA ne pourra la valider qu'après leur dépôt.` : ''}`}
        confirmText={renvoi ? 'Renvoyer' : 'Envoyer'}
        cancelBtnText='Annuler'
        isLoading={changementEtat.isPending}
        handleConfirm={() => changementEtat.mutate('envoi')}
      />
      <ConfirmDialog
        open={transition === 'instruction'}
        onOpenChange={(open) => !open && setTransition(null)}
        title='Prendre en charge'
        desc={`La DSM de ${dsm.periode.libelle} de ${dsm.raisonSociale} passe en instruction : vous pourrez ensuite demander des compléments ou la valider.`}
        confirmText='Prendre en charge'
        cancelBtnText='Annuler'
        isLoading={changementEtat.isPending}
        handleConfirm={() => changementEtat.mutate('instruction')}
      />
      <ConfirmDialog
        open={transition === 'complements'}
        onOpenChange={(open) => !open && setTransition(null)}
        title='Demander des compléments'
        desc={`La DSM de ${dsm.periode.libelle} revient à ${dsm.raisonSociale}, qui la corrige puis la renvoie. Le motif est déposé dans les messages de la DSM, visible de l'opérateur.`}
        confirmText='Demander'
        cancelBtnText='Annuler'
        disabled={motif.trim() === ''}
        isLoading={changementEtat.isPending}
        handleConfirm={() => changementEtat.mutate('complements')}
      >
        <div className='space-y-1'>
          <Label htmlFor='motif-complements'>Motif</Label>
          <Textarea
            id='motif-complements'
            rows={3}
            value={motif}
            onChange={(e) => setMotif(e.target.value)}
          />
        </div>
      </ConfirmDialog>
      <ConfirmDialog
        open={transition === 'validation'}
        onOpenChange={(open) => !open && setTransition(null)}
        title='Valider la DSM'
        desc={`La DSM de ${dsm.periode.libelle} de ${dsm.raisonSociale} sera validée : elle ne se modifiera plus, et son stock de fin deviendra le stock de début de la période suivante.`}
        confirmText='Valider'
        cancelBtnText='Annuler'
        isLoading={changementEtat.isPending}
        handleConfirm={() => changementEtat.mutate('validation')}
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

/**
 * Les anomalies de calcul (RG-DEC-18), bloquantes à l'envoi, et les pièces manquantes (RG-DEC-29),
 * bloquantes à la validation ; un clic mène à l'onglet et à la ligne, ou à l'onglet des pièces.
 */
function PanneauAnomalies({
  anomalies,
  manques,
  onMontrer,
  onMontrerPieces,
}: {
  anomalies: AnomalieDsm[]
  manques: ManqueDsm[]
  onMontrer: (a: AnomalieDsm) => void
  onMontrerPieces: () => void
}) {
  if (anomalies.length === 0 && manques.length === 0) {
    return (
      <Alert variant='success'>
        <CircleCheck />
        <AlertTitle>Aucune anomalie ni pièce manquante</AlertTitle>
        <AlertDescription>
          Les contrôles de cohérence sont respectés et les pièces attendues sont
          déposées.
        </AlertDescription>
      </Alert>
    )
  }
  return (
    <>
      {anomalies.length > 0 && (
        <ListeAnomalies anomalies={anomalies} onMontrer={onMontrer} />
      )}
      {manques.length > 0 && (
        <Alert variant='warning'>
          <FileWarning />
          <AlertTitle>
            {manques.length === 1
              ? '1 pièce manquante, attendue avant la validation'
              : `${manques.length} pièces manquantes, attendues avant la validation`}
          </AlertTitle>
          <AlertDescription>
            {/* Sur un contenu étroit, les trois premières ; les autres sont dans l'onglet Pièces. */}
            <ul className='mt-1 space-y-1'>
              {manques.map((m, i) => (
                <li
                  key={i}
                  className={cn(i >= 3 && '@max-[48rem]/content:hidden')}
                >
                  <button
                    type='button'
                    className='text-left underline-offset-2 hover:underline'
                    onClick={onMontrerPieces}
                  >
                    {m.message}
                  </button>
                </li>
              ))}
            </ul>
            {manques.length > 3 && (
              <Button
                variant='link'
                className='h-auto p-0 @min-[48rem]/content:hidden'
                onClick={onMontrerPieces}
              >
                Les {manques.length} pièces dans l'onglet Pièces
              </Button>
            )}
          </AlertDescription>
        </Alert>
      )}
    </>
  )
}

function ListeAnomalies({
  anomalies,
  onMontrer,
}: {
  anomalies: AnomalieDsm[]
  onMontrer: (a: AnomalieDsm) => void
}) {
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

/**
 * Les éditions de la DSM enregistrée, dans un menu : le classeur Excel au format du modèle, téléchargé (RG-DEC-36),
 * et le PDF, dans un nouvel onglet (RG-DEC-35). Avec des modifications en cours, le menu prévient qu'elles n'y sont pas.
 */
function ElementsExport({
  urlExcel,
  onExcel,
  onPdf,
  modifie,
}: {
  urlExcel: string
  onExcel: () => void
  onPdf: () => void
  modifie: boolean
}) {
  return (
    <>
      <DropdownMenuLabel className='text-muted-foreground font-normal'>
        {modifie
          ? 'Exporter la DSM enregistrée, sans les modifications en cours'
          : 'Exporter'}
      </DropdownMenuLabel>
      <DropdownMenuItem asChild>
        <a href={urlExcel} download onClick={onExcel}>
          <FileSpreadsheet /> Excel
        </a>
      </DropdownMenuItem>
      <DropdownMenuItem onSelect={onPdf}>
        <FileDown /> PDF
      </DropdownMenuItem>
    </>
  )
}
