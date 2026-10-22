import { useEffect, useMemo, useRef, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  ChevronLeft,
  ChevronRight,
  Download,
  Loader2,
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
import { sectionAffichee } from '@/features/suivi/suivi'

/** Publiée par `pnpm publier` (echanges-bnia/reponse), servie après connexion par le back. */
const RACINE = '/api/offre/'

type SectionOffre = { id: string; titre: string; annexe?: string }
/** « 6 », « Annexe A » ; la couverture n'a pas de numéro. */
type SectionNumerotee = SectionOffre & { numero?: string }
type Offre = { pdf: string | null; sections: SectionOffre[] }

async function lireOffre(): Promise<Offre> {
  const reponse = await fetch(`${RACINE}sections.json`)
  if (!reponse.ok) throw new Error(`Offre indisponible (${reponse.status})`)
  return reponse.json()
}

/**
 * « Voir l'offre », au bas de la barre latérale : la réponse à l'appel d'offres, à lire par le BNIA pendant qu'il
 * essaie le prototype. Aux couleurs du document (l'ambre de son gabarit), pas de l'application : ce n'est pas une
 * fonction du prototype. Grand écran seulement (dès `lg`) : la lecture plein écran n'a pas la place sur tablette.
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

/** Miniatures A4 : la page de la section, réduite. */
const LARGEUR = 794
const HAUTEUR = 1123
const ECHELLE = 0.12

/**
 * L'offre en plein écran : à gauche, les miniatures des sections, à la manière d'une barre latérale ; à droite, la
 * section choisie, défilant dans sa page, sous les flèches vers la précédente et la suivante, et « Fermer ». Chaque
 * section est une page HTML, avec la feuille de style du document imprimé, affichée dans une iframe ; le sommaire
 * de la couverture y mène aux sections (postMessage).
 */
function VisionneuseOffre({
  ouverte,
  onOuverteChange,
}: {
  ouverte: boolean
  onOuverteChange: (ouverte: boolean) => void
}) {
  const { data, isPending, isError } = useQuery({
    queryKey: ['offre'],
    queryFn: lireOffre,
    enabled: ouverte,
    staleTime: Infinity,
  })
  const sections = useMemo<SectionNumerotee[]>(
    () =>
      (data?.sections ?? []).map((s, i) => ({
        ...s,
        numero: s.annexe ? `Annexe ${s.annexe}` : i > 0 ? String(i) : undefined,
      })),
    [data]
  )
  const [courante, setCourante] = useState(0)
  const section = sections[courante]
  const precedente = sections[courante - 1]
  const suivante = sections[courante + 1]
  const miniatures = useRef(new Map<string, HTMLButtonElement>())

  useEffect(() => {
    if (section)
      miniatures.current.get(section.id)?.scrollIntoView({
        block: 'nearest',
        behavior: 'smooth',
      })
  }, [section])

  useEffect(() => {
    sectionAffichee(ouverte && section ? section.id : null)
  }, [ouverte, section])

  useEffect(() => {
    if (!ouverte) return
    const surMessage = (e: MessageEvent) => {
      if (e.origin !== window.location.origin) return
      const i = sections.findIndex((s) => s.id === e.data?.offre)
      if (i >= 0) setCourante(i)
    }
    window.addEventListener('message', surMessage)
    return () => window.removeEventListener('message', surMessage)
  }, [ouverte, sections])

  const aller = (i: number) => {
    if (i >= 0 && i < sections.length) setCourante(i)
  }

  return (
    <Dialog open={ouverte} onOpenChange={onOuverteChange}>
      <DialogContent
        showCloseButton={false}
        className='top-0 left-0 flex h-dvh w-screen max-w-none translate-x-0 translate-y-0 gap-0 rounded-none border-0 p-0 sm:max-w-none'
        onKeyDown={(e) => {
          if (e.key === 'ArrowLeft') aller(courante - 1)
          if (e.key === 'ArrowRight') aller(courante + 1)
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
            {data?.pdf && (
              <Button variant='outline' size='sm' className='w-full' asChild>
                <a href={RACINE + data.pdf} download>
                  <Download /> Télécharger le PDF
                </a>
              </Button>
            )}
          </div>
          <nav
            aria-label="Sections de l'offre"
            className='min-h-0 flex-1 space-y-1 overflow-y-scroll p-2 [scrollbar-color:var(--muted-foreground)_transparent] [scrollbar-gutter:stable]'
          >
            {sections.map((s, i) => (
              <button
                key={s.id}
                ref={(el) => {
                  if (el) miniatures.current.set(s.id, el)
                  else miniatures.current.delete(s.id)
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
                    width: LARGEUR * ECHELLE,
                    height: HAUTEUR * ECHELLE,
                  }}
                >
                  <iframe
                    src={`${RACINE}${s.id}.html#miniature`}
                    title=''
                    aria-hidden
                    tabIndex={-1}
                    loading='lazy'
                    className='pointer-events-none absolute top-0 left-0 origin-top-left border-0'
                    style={{
                      width: LARGEUR,
                      height: HAUTEUR,
                      transform: `scale(${ECHELLE})`,
                    }}
                  />
                </span>
                <span className='min-w-0 pt-1 text-sm leading-tight'>
                  {s.numero && (
                    <span className='text-muted-foreground mb-0.5 block text-xs font-medium tabular-nums'>
                      {s.numero}
                    </span>
                  )}
                  <span
                    className={cn(
                      'line-clamp-3',
                      i === courante && 'font-semibold'
                    )}
                  >
                    {s.titre}
                  </span>
                </span>
              </button>
            ))}
          </nav>
        </aside>

        <div className='flex min-w-0 flex-1 flex-col'>
          {/* Les deux boutons toujours au même endroit, de part et d'autre du centre, à un quart de la largeur l'un
              de l'autre ; la marge symétrique laisse la place de « Fermer » sans décentrer. */}
          <header className='bg-background relative grid grid-cols-[1fr_25%_1fr] items-center border-b px-28 py-2'>
            <Fleche
              sens='precedente'
              desactivee={precedente == null}
              onClick={() => aller(courante - 1)}
            />
            <Fleche
              sens='suivante'
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

          <div className='bg-muted relative min-h-0 flex-1'>
            {section ? (
              <iframe
                key={section.id}
                src={`${RACINE}${section.id}.html`}
                title={section.titre}
                className='absolute inset-0 size-full border-0'
              />
            ) : (
              <p className='text-muted-foreground flex h-full items-center justify-center gap-2 text-sm'>
                {isPending && <Loader2 className='size-4 animate-spin' />}
                {isError
                  ? "L'offre n'est pas disponible."
                  : !isPending && "L'offre ne contient aucune section."}
              </p>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}

function Fleche({
  sens,
  desactivee,
  onClick,
}: {
  sens: 'precedente' | 'suivante'
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
          <ChevronLeft /> Section précédente
        </>
      ) : (
        <>
          Section suivante <ChevronRight />
        </>
      )}
    </Button>
  )
}
