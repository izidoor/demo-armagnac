import { type RefObject, useEffect, useMemo, useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  ChevronLeft,
  ChevronRight,
  Download,
  Loader2,
  MonitorPlay,
  Presentation,
  ScrollText,
  X,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { sectionAffichee } from '@/features/suivi/suivi'

/**
 * Publiée par `pnpm publier` (echanges-bnia/reponse), servie après connexion par le back ; le diaporama de la
 * soutenance (echanges-bnia/diaporama), dans son sous-dossier.
 */
const RACINE = '/api/offre/'
const RACINE_DIAPORAMA = `${RACINE}diaporama/`

/** L'offre rédigée, section par section, ou le diaporama de la soutenance, diapositive par diapositive. */
type Mode = 'offre' | 'diaporama'

type SectionOffre = { id: string; titre: string; annexe?: string }
type Offre = { pdf: string | null; sections: SectionOffre[] }
type Diaporama = { pdf: string | null; diapositives: Page[] }
/** Une section de l'offre ou une diapositive. */
type Page = { id: string; titre: string }
/** « 6 », « Annexe A » ; la couverture n'a pas de numéro. */
type PageNumerotee = Page & { numero?: string }

/** Pages A4 de l'offre, diapositives 16:9 ; réduites à `echelle` pour les miniatures. */
const FORMATS = {
  offre: { largeur: 794, hauteur: 1123, echelle: 0.12 },
  diaporama: { largeur: 1280, hauteur: 720, echelle: 0.125 },
} satisfies Record<Mode, { largeur: number; hauteur: number; echelle: number }>

async function lire<T>(adresse: string, nom: string): Promise<T> {
  const reponse = await fetch(adresse)
  if (!reponse.ok) throw new Error(`${nom} indisponible (${reponse.status})`)
  return reponse.json()
}

const lireOffre = () => lire<Offre>(`${RACINE}sections.json`, 'Offre')
const lireDiaporama = () =>
  lire<Diaporama>(`${RACINE_DIAPORAMA}diaporama.json`, 'Diaporama')

/**
 * « Voir l'offre », au bas de la barre latérale : la réponse à l'appel d'offres, à lire par le BNIA pendant qu'il
 * essaie le prototype, et le diaporama de la soutenance. Aux couleurs du document (l'ambre de son gabarit), pas de
 * l'application : ce n'est pas une fonction du prototype. Grand écran seulement (dès `lg`) : la lecture plein écran
 * n'a pas la place sur tablette.
 */
export function BoutonOffre() {
  const [ouverte, setOuverte] = useState(false)
  return (
    <>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton
            size='lg'
            tooltip="Voir l'offre"
            onClick={() => setOuverte(true)}
            className='h-14 bg-[#9a5b1e] text-white shadow-sm group-data-[collapsible=icon]:justify-center hover:bg-[#6e3f12] hover:text-white active:bg-[#6e3f12] active:text-white'
          >
            <span className='grid size-9 shrink-0 place-items-center rounded-md bg-white/15 group-data-[collapsible=icon]:size-8'>
              <ScrollText className='size-5' />
            </span>
            <span className='grid leading-tight group-data-[collapsible=icon]:hidden'>
              <span className='text-base font-semibold'>Voir l'offre</span>
              <span className='truncate text-xs opacity-85'>
                Réponse à l'appel d'offres
              </span>
            </span>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
      <VisionneuseOffre ouverte={ouverte} onOuverteChange={setOuverte} />
    </>
  )
}

/**
 * Sur tablette, sous `lg`, à la place de « Voir l'offre » : les PDF de l'offre et du diaporama, ouverts dans la
 * visionneuse de l'appareil, la lecture page par page n'y ayant pas la place. Rien sur téléphone (`AppSidebar`).
 */
export function LienOffrePdf() {
  const petitEcran = window.matchMedia('(max-width: 1023px)').matches
  const offre = useQuery({
    queryKey: ['offre'],
    queryFn: lireOffre,
    enabled: petitEcran,
    staleTime: Infinity,
  })
  const diaporama = useQuery({
    queryKey: ['diaporama'],
    queryFn: lireDiaporama,
    enabled: petitEcran,
    staleTime: Infinity,
  })
  const liens = [
    {
      pdf: offre.data?.pdf,
      racine: RACINE,
      titre: "L'offre (PDF)",
      icone: ScrollText,
    },
    {
      pdf: diaporama.data?.pdf,
      racine: RACINE_DIAPORAMA,
      titre: 'Le diaporama (PDF)',
      icone: Presentation,
    },
  ].flatMap(({ pdf, racine, ...lien }) =>
    pdf ? [{ ...lien, adresse: racine + pdf }] : []
  )
  if (liens.length === 0) return null
  return (
    <SidebarMenu>
      {liens.map(({ adresse, titre, icone: Icone }) => (
        <SidebarMenuItem key={adresse}>
          <SidebarMenuButton
            size='lg'
            asChild
            className='h-14 bg-[#9a5b1e] text-white shadow-sm hover:bg-[#6e3f12] hover:text-white active:bg-[#6e3f12] active:text-white'
          >
            <a href={adresse} target='_blank' rel='noreferrer'>
              <span className='grid size-9 shrink-0 place-items-center rounded-md bg-white/15'>
                <Icone className='size-5' />
              </span>
              <span className='grid leading-tight'>
                <span className='text-base font-semibold'>{titre}</span>
                <span className='truncate text-xs opacity-85'>
                  Réponse à l'appel d'offres
                </span>
              </span>
            </a>
          </SidebarMenuButton>
        </SidebarMenuItem>
      ))}
    </SidebarMenu>
  )
}

/**
 * L'offre ou le diaporama en plein écran : à gauche, le choix entre les deux et les miniatures, à la manière d'une
 * barre latérale ; à droite, la page choisie, sous les flèches vers la précédente et la suivante, et « Fermer ».
 * Chaque page est une page HTML, avec la feuille de style du document, affichée dans une iframe ; le sommaire de la
 * couverture de l'offre y mène aux sections (postMessage). La diapositive est mise à l'échelle de la place, et
 * « Projeter » la passe seule en plein écran. Le mode et la page courante de chacun survivent à la fermeture : on
 * reprend le diaporama là où on l'a quitté pour la démonstration.
 */
function VisionneuseOffre({
  ouverte,
  onOuverteChange,
}: {
  ouverte: boolean
  onOuverteChange: (ouverte: boolean) => void
}) {
  const [mode, setMode] = useState<Mode>('offre')
  const offre = useQuery({
    queryKey: ['offre'],
    queryFn: lireOffre,
    enabled: ouverte,
    staleTime: Infinity,
  })
  const diaporama = useQuery({
    queryKey: ['diaporama'],
    queryFn: lireDiaporama,
    enabled: ouverte && mode === 'diaporama',
    staleTime: Infinity,
  })
  const { data, isPending, isError } = mode === 'offre' ? offre : diaporama
  const racine = mode === 'offre' ? RACINE : RACINE_DIAPORAMA
  const format = FORMATS[mode]
  const pages = useMemo<PageNumerotee[]>(
    () =>
      mode === 'offre'
        ? (offre.data?.sections ?? []).map((s, i) => ({
            ...s,
            numero: s.annexe
              ? `Annexe ${s.annexe}`
              : i > 0
                ? String(i)
                : undefined,
          }))
        : (diaporama.data?.diapositives ?? []).map((d, i) => ({
            ...d,
            numero: String(i + 1),
          })),
    [mode, offre.data, diaporama.data]
  )
  const [courantes, setCourantes] = useState<Record<Mode, number>>({
    offre: 0,
    diaporama: 0,
  })
  const courante = courantes[mode]
  const page = pages[courante]
  const precedente = pages[courante - 1]
  const suivante = pages[courante + 1]
  const miniatures = useRef(new Map<string, HTMLButtonElement>())
  const scene = useRef<HTMLDivElement>(null)
  const [projection, setProjection] = useState(false)

  useEffect(() => {
    if (page)
      miniatures.current.get(page.id)?.scrollIntoView({
        block: 'nearest',
        behavior: 'smooth',
      })
  }, [page])

  useEffect(() => {
    sectionAffichee(
      ouverte && page
        ? mode === 'diaporama'
          ? `diaporama/${page.id}`
          : page.id
        : null
    )
  }, [ouverte, mode, page])

  useEffect(() => {
    if (!ouverte) return
    const surMessage = (e: MessageEvent) => {
      if (e.origin !== window.location.origin) return
      const i = (offre.data?.sections ?? []).findIndex(
        (s) => s.id === e.data?.offre
      )
      if (i >= 0) setCourantes((c) => ({ ...c, offre: i }))
    }
    window.addEventListener('message', surMessage)
    return () => window.removeEventListener('message', surMessage)
  }, [ouverte, offre.data])

  useEffect(() => {
    const surChangement = () =>
      setProjection(
        document.fullscreenElement !== null &&
          document.fullscreenElement === scene.current
      )
    document.addEventListener('fullscreenchange', surChangement)
    return () => document.removeEventListener('fullscreenchange', surChangement)
  }, [])

  const aller = (i: number) => {
    if (i >= 0 && i < pages.length) setCourantes((c) => ({ ...c, [mode]: i }))
  }
  // Le plein écran retire le focus à « Projeter », resté dehors : il passerait au document, hors de la visionneuse,
  // et les touches ne l'atteindraient plus. La scène le prend.
  const projeter = () => {
    scene.current
      ?.requestFullscreen()
      .then(() => scene.current?.focus())
      .catch(() => {})
  }
  const libelles =
    mode === 'offre'
      ? { precedente: 'Section précédente', suivante: 'Section suivante' }
      : {
          precedente: 'Diapositive précédente',
          suivante: 'Diapositive suivante',
        }

  return (
    <Dialog open={ouverte} onOpenChange={onOuverteChange}>
      <DialogContent
        showCloseButton={false}
        className='top-0 left-0 flex h-dvh w-screen max-w-none translate-x-0 translate-y-0 gap-0 rounded-none border-0 p-0 sm:max-w-none'
        // Échap quitte la projection, pas la visionneuse.
        onEscapeKeyDown={(e) => {
          if (projection) e.preventDefault()
        }}
        onKeyDown={(e) => {
          // Les télécommandes de présentation envoient Page précédente et Page suivante.
          const diapo = mode === 'diaporama'
          if (e.key === 'ArrowLeft' || (diapo && e.key === 'PageUp'))
            aller(courante - 1)
          if (e.key === 'ArrowRight' || (diapo && e.key === 'PageDown'))
            aller(courante + 1)
          if (diapo && e.key === 'Home') aller(0)
          if (diapo && e.key === 'End') aller(pages.length - 1)
        }}
      >
        <aside className='bg-background flex w-72 shrink-0 flex-col border-e'>
          <div className='space-y-3 border-b p-4'>
            <div className='flex items-start gap-3'>
              <span className='grid size-9 shrink-0 place-items-center rounded-md bg-[#f7efe5] text-[#9a5b1e]'>
                <ScrollText className='size-5' />
              </span>
              <div className='min-w-0 space-y-1'>
                <DialogTitle className='text-base'>
                  Réponse à l'appel d'offres
                </DialogTitle>
                <DialogDescription className='text-xs'>
                  Observatoire économique et gestion de la Donnée Armagnac ·
                  proposition de Pierre-Yves Pollet
                </DialogDescription>
              </div>
            </div>
            <Tabs value={mode} onValueChange={(m) => setMode(m as Mode)}>
              <TabsList className='w-full'>
                <TabsTrigger value='offre'>
                  <ScrollText /> Offre
                </TabsTrigger>
                <TabsTrigger value='diaporama'>
                  <Presentation /> Diaporama
                </TabsTrigger>
              </TabsList>
            </Tabs>
            {data?.pdf && (
              <Button variant='outline' size='sm' className='w-full' asChild>
                <a href={racine + data.pdf} download>
                  <Download /> Télécharger le PDF
                </a>
              </Button>
            )}
          </div>
          <nav
            aria-label={
              mode === 'offre'
                ? "Sections de l'offre"
                : 'Diapositives du diaporama'
            }
            className='min-h-0 flex-1 space-y-1 overflow-y-scroll p-2 [scrollbar-color:var(--muted-foreground)_transparent] [scrollbar-gutter:stable]'
          >
            {pages.map((p, i) => (
              <button
                key={p.id}
                ref={(el) => {
                  if (el) miniatures.current.set(p.id, el)
                  else miniatures.current.delete(p.id)
                }}
                type='button'
                onClick={() => aller(i)}
                aria-current={i === courante ? 'page' : undefined}
                className={cn(
                  'group flex w-full items-start gap-3 rounded-md p-2 text-start outline-none',
                  'hover:bg-muted focus-visible:ring-ring focus-visible:ring-2',
                  i === courante && 'bg-muted'
                )}
              >
                <span
                  className={cn(
                    'relative block shrink-0 overflow-hidden rounded-sm border bg-white shadow-xs transition',
                    i === courante
                      ? 'ring-offset-background ring-2 ring-[#9a5b1e] ring-offset-1'
                      : 'opacity-75 group-hover:opacity-100'
                  )}
                  style={{
                    width: format.largeur * format.echelle,
                    height: format.hauteur * format.echelle,
                  }}
                >
                  <iframe
                    src={`${racine}${p.id}.html#miniature`}
                    title=''
                    aria-hidden
                    tabIndex={-1}
                    loading='lazy'
                    className='pointer-events-none absolute top-0 left-0 origin-top-left border-0'
                    style={{
                      width: format.largeur,
                      height: format.hauteur,
                      transform: `scale(${format.echelle})`,
                    }}
                  />
                </span>
                <span className='min-w-0 pt-1 text-sm leading-tight'>
                  {p.numero && (
                    <span className='text-muted-foreground mb-0.5 block text-xs font-medium tabular-nums'>
                      {p.numero}
                    </span>
                  )}
                  <span
                    className={cn(
                      'line-clamp-3',
                      i === courante && 'font-semibold'
                    )}
                  >
                    {p.titre}
                  </span>
                </span>
              </button>
            ))}
          </nav>
        </aside>

        <div className='flex min-w-0 flex-1 flex-col'>
          {/* Les deux boutons toujours au même endroit, de part et d'autre du centre, à un quart de la largeur l'un
              de l'autre ; la marge symétrique laisse la place de « Projeter » et de « Fermer » sans décentrer. */}
          <header className='bg-background relative grid grid-cols-[1fr_25%_1fr] items-center border-b px-28 py-2'>
            {mode === 'diaporama' && (
              <Button
                className='absolute start-4 top-1/2 -translate-y-1/2 bg-[#9a5b1e] text-white hover:bg-[#6e3f12]'
                disabled={page == null}
                onClick={projeter}
              >
                <MonitorPlay /> Projeter
              </Button>
            )}
            <Fleche
              sens='precedente'
              libelle={libelles.precedente}
              desactivee={precedente == null}
              onClick={() => aller(courante - 1)}
            />
            <Fleche
              sens='suivante'
              libelle={libelles.suivante}
              desactivee={suivante == null}
              onClick={() => aller(courante + 1)}
            />
            <DialogClose asChild>
              <Button
                variant='outline'
                className='absolute end-4 top-1/2 -translate-y-1/2'
              >
                <X /> Fermer
              </Button>
            </DialogClose>
          </header>

          {page && mode === 'diaporama' ? (
            <SceneDiaporama
              conteneur={scene}
              pages={pages}
              courante={courante}
              projection={projection}
              onSuivante={() => aller(courante + 1)}
            />
          ) : (
            <div className='bg-muted relative min-h-0 flex-1'>
              {page ? (
                <iframe
                  key={page.id}
                  src={`${racine}${page.id}.html`}
                  title={page.titre}
                  className='absolute inset-0 size-full border-0'
                />
              ) : (
                <p className='text-muted-foreground flex h-full items-center justify-center gap-2 text-sm'>
                  {isPending && <Loader2 className='size-4 animate-spin' />}
                  {isError
                    ? `${mode === 'offre' ? "L'offre" : 'Le diaporama'} n'est pas disponible.`
                    : !isPending &&
                      `${mode === 'offre' ? "L'offre ne contient aucune section" : 'Le diaporama ne contient aucune diapositive'}.`}
                </p>
              )}
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}

/**
 * Le diaporama : la diapositive courante, à la plus grande échelle qui tient dans la place. Toutes les diapositives
 * sont chargées, une seule visible : on passe de l'une à l'autre sans attendre ni clignoter. Un clic avance. En
 * projection, la scène occupe seule l'écran, sur fond noir ; focalisable, elle garde le focus après un clic, et les
 * touches continuent d'atteindre la visionneuse.
 */
function SceneDiaporama({
  conteneur,
  pages,
  courante,
  projection,
  onSuivante,
}: {
  conteneur: RefObject<HTMLDivElement | null>
  pages: PageNumerotee[]
  courante: number
  projection: boolean
  onSuivante: () => void
}) {
  const { largeur, hauteur } = FORMATS.diaporama
  const [echelle, setEchelle] = useState(0)

  useEffect(() => {
    const element = conteneur.current
    if (!element) return
    const observateur = new ResizeObserver(([entree]) => {
      const { width, height } = entree.contentRect
      setEchelle(Math.min(width / largeur, height / hauteur))
    })
    observateur.observe(element)
    return () => observateur.disconnect()
  }, [conteneur, largeur, hauteur])

  return (
    <div
      ref={conteneur}
      tabIndex={-1}
      onClick={onSuivante}
      className={cn(
        'flex min-h-0 flex-1 cursor-pointer items-center justify-center overflow-hidden outline-none',
        projection ? 'bg-black' : 'bg-muted p-6'
      )}
    >
      <div
        className={cn(
          'relative shrink-0 overflow-hidden bg-white',
          !projection && 'shadow-sm'
        )}
        style={{ width: largeur * echelle, height: hauteur * echelle }}
      >
        {pages.map((p, i) => (
          <iframe
            key={p.id}
            src={`${RACINE_DIAPORAMA}${p.id}.html`}
            title={p.titre}
            aria-hidden={i !== courante}
            tabIndex={-1}
            className={cn(
              'pointer-events-none absolute top-0 left-0 origin-top-left border-0',
              i !== courante && 'invisible'
            )}
            style={{
              width: largeur,
              height: hauteur,
              transform: `scale(${echelle})`,
            }}
          />
        ))}
      </div>
    </div>
  )
}

function Fleche({
  sens,
  libelle,
  desactivee,
  onClick,
}: {
  sens: 'precedente' | 'suivante'
  libelle: string
  desactivee: boolean
  onClick: () => void
}) {
  return (
    <Button
      variant='outline'
      className={
        sens === 'precedente'
          ? 'justify-self-end'
          : 'col-start-3 justify-self-start'
      }
      disabled={desactivee}
      onClick={onClick}
    >
      {sens === 'precedente' ? (
        <>
          <ChevronLeft /> {libelle}
        </>
      ) : (
        <>
          {libelle} <ChevronRight />
        </>
      )}
    </Button>
  )
}
