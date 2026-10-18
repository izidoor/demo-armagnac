import { useState } from 'react'
import {
  type CollisionDetection,
  DndContext,
  type DragEndEvent,
  DragOverlay,
  type DragStartEvent,
  KeyboardSensor,
  PointerSensor,
  pointerWithin,
  rectIntersection,
  useDroppable,
  useSensor,
  useSensors,
} from '@dnd-kit/core'
import {
  arrayMove,
  rectSortingStrategy,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { ChevronDown, Filter, GripVertical, X } from 'lucide-react'
import { createPortal } from 'react-dom'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover'
import {
  type Champ,
  type Composition,
  type Zone,
  affichageDe,
  valeursDe,
} from './calcul'

type Emplacement = Zone | 'champs'

const ZONES: { zone: Emplacement; libelle: string; vide: string }[] = [
  {
    zone: 'champs',
    libelle: 'Champs',
    vide: 'Tous les champs sont placés.',
  },
  {
    zone: 'lignes',
    libelle: 'Lignes',
    vide: 'Glissez ici un champ (ou cliquez-le).',
  },
  { zone: 'colonnes', libelle: 'Colonnes', vide: 'Glissez ici un champ.' },
  { zone: 'filtres', libelle: 'Filtres', vide: 'Glissez ici un champ.' },
]

const PREFIXE_ZONE = 'zone:'

/** Le pointeur d'abord (zones vides comprises), puis le recouvrement. */
const collision: CollisionDetection = (args) => {
  const sous = pointerWithin(args)
  return sous.length > 0 ? sous : rectIntersection(args)
}

/**
 * La composition d'un tableau croisé, à la Excel : les champs se glissent entre les zones *Champs*,
 * *Lignes*, *Colonnes* et *Filtres* (au pointeur, ou au clavier : espace puis flèches), et s'y ordonnent. Un
 * clic sur un champ libre le met en lignes ; × le retire. Un champ en filtre choisit ses valeurs.
 */
export function CompositionTableau<T>({
  champs,
  donnees,
  composition,
  onChange,
}: {
  champs: Champ<T>[]
  donnees: T[]
  composition: Composition
  onChange: (composition: Composition) => void
}) {
  const capteurs = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 4 } }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  )
  const places = new Set([
    ...composition.lignes,
    ...composition.colonnes,
    ...composition.filtres,
  ])
  const contenu: Record<Emplacement, string[]> = {
    champs: champs.map((c) => c.id).filter((id) => !places.has(id)),
    lignes: composition.lignes,
    colonnes: composition.colonnes,
    filtres: composition.filtres,
  }
  const parId = new Map(champs.map((c) => [c.id, c]))
  const emplacementDe = (id: string): Emplacement | undefined =>
    id.startsWith(PREFIXE_ZONE)
      ? (id.slice(PREFIXE_ZONE.length) as Emplacement)
      : (Object.keys(contenu) as Emplacement[]).find((z) =>
          contenu[z].includes(id)
        )

  const deplacer = (id: string, vers: Emplacement, index?: number) => {
    const de = emplacementDe(id)
    if (!de) return
    const suivant: Record<Emplacement, string[]> = {
      champs: [...contenu.champs],
      lignes: [...contenu.lignes],
      colonnes: [...contenu.colonnes],
      filtres: [...contenu.filtres],
    }
    if (de === vers) {
      if (index === undefined) return
      suivant[vers] = arrayMove(suivant[vers], suivant[vers].indexOf(id), index)
    } else {
      suivant[de] = suivant[de].filter((x) => x !== id)
      const liste = suivant[vers]
      liste.splice(index ?? liste.length, 0, id)
    }
    onChange({
      ...composition,
      lignes: suivant.lignes,
      colonnes: suivant.colonnes,
      filtres: suivant.filtres,
    })
  }

  // Le champ en cours de glisser : son aperçu suit le pointeur, au-dessus de la page.
  const [enCours, setEnCours] = useState<string | null>(null)
  const surDebut = ({ active }: DragStartEvent) => setEnCours(String(active.id))

  const surFin = ({ active, over }: DragEndEvent) => {
    setEnCours(null)
    if (!over || active.id === over.id) return
    const vers = emplacementDe(String(over.id))
    if (!vers) return
    const index = String(over.id).startsWith(PREFIXE_ZONE)
      ? undefined
      : contenu[vers].indexOf(String(over.id))
    deplacer(String(active.id), vers, index)
  }

  return (
    <DndContext
      sensors={capteurs}
      collisionDetection={collision}
      onDragStart={surDebut}
      onDragEnd={surFin}
      onDragCancel={() => setEnCours(null)}
    >
      <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
        {ZONES.map(({ zone, libelle, vide }) => (
          <ZoneDepot
            key={zone}
            zone={zone}
            libelle={libelle}
            vide={vide}
            ids={contenu[zone]}
          >
            {contenu[zone].map((id) => {
              const champ = parId.get(id)
              if (!champ) return null
              return (
                <Pastille
                  key={id}
                  id={id}
                  libelle={champ.libelle}
                  onClic={
                    zone === 'champs' ? () => deplacer(id, 'lignes') : undefined
                  }
                  onRetirer={
                    zone === 'champs' ||
                    (zone === 'filtres' && champ.nonCumulable)
                      ? undefined
                      : () => deplacer(id, 'champs')
                  }
                >
                  {zone === 'filtres' && (
                    <ChoixValeurs
                      champ={champ}
                      valeurs={valeursDe(donnees, champ)}
                      retenues={composition.selection[id]}
                      onChange={(retenues) =>
                        onChange({
                          ...composition,
                          selection: {
                            ...composition.selection,
                            [id]: retenues,
                          },
                        })
                      }
                    />
                  )}
                </Pastille>
              )
            })}
          </ZoneDepot>
        ))}
      </div>
      {createPortal(
        <DragOverlay>
          {enCours && (
            <div className='bg-background flex h-8 cursor-grabbing items-center gap-1 rounded-md border ps-1 pe-2 text-sm shadow-lg'>
              <GripVertical className='text-muted-foreground size-3.5' />
              {parId.get(enCours)?.libelle}
            </div>
          )}
        </DragOverlay>,
        document.body
      )}
    </DndContext>
  )
}

function ZoneDepot({
  zone,
  libelle,
  vide,
  ids,
  children,
}: {
  zone: Emplacement
  libelle: string
  vide: string
  ids: string[]
  children: React.ReactNode
}) {
  const { setNodeRef, isOver } = useDroppable({ id: PREFIXE_ZONE + zone })
  return (
    <div
      ref={setNodeRef}
      className={cn(
        'min-h-20 rounded-md border border-dashed p-2 transition-colors',
        zone === 'champs' && 'bg-muted/40 border-solid',
        isOver && 'border-primary bg-primary/5'
      )}
    >
      <p className='text-muted-foreground mb-2 text-xs font-medium tracking-wide uppercase'>
        {libelle}
      </p>
      <SortableContext items={ids} strategy={rectSortingStrategy}>
        <div className='flex flex-wrap gap-1.5'>
          {children}
          {ids.length === 0 && (
            <p className='text-muted-foreground text-xs italic'>{vide}</p>
          )}
        </div>
      </SortableContext>
    </div>
  )
}

function Pastille({
  id,
  libelle,
  onClic,
  onRetirer,
  children,
}: {
  id: string
  libelle: string
  onClic?: () => void
  onRetirer?: () => void
  children?: React.ReactNode
}) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id })
  return (
    <div
      ref={setNodeRef}
      style={{ transform: CSS.Translate.toString(transform), transition }}
      className={cn(
        'bg-background flex h-8 items-center gap-1 rounded-md border ps-1 pe-1 text-sm shadow-xs',
        isDragging && 'border-dashed opacity-40'
      )}
    >
      <span
        className='text-muted-foreground flex cursor-grab touch-none items-center active:cursor-grabbing'
        aria-label={`Déplacer « ${libelle} »`}
        {...attributes}
        {...listeners}
      >
        <GripVertical className='size-3.5' />
      </span>
      {onClic ? (
        <button
          type='button'
          className='hover:text-primary px-0.5'
          onClick={onClic}
          title='Mettre en lignes'
        >
          {libelle}
        </button>
      ) : (
        <span className='px-0.5'>{libelle}</span>
      )}
      {children}
      {onRetirer && (
        <button
          type='button'
          className='text-muted-foreground hover:text-foreground rounded-sm'
          aria-label={`Retirer « ${libelle} »`}
          onClick={onRetirer}
        >
          <X className='size-3.5' />
        </button>
      )}
    </div>
  )
}

/**
 * Les valeurs retenues d'un champ en filtre : cases à cocher ; une seule valeur pour un champ non cumulable
 * (le stock à une date ne s'additionne pas d'une date à l'autre).
 */
function ChoixValeurs<T>({
  champ,
  valeurs,
  retenues,
  onChange,
}: {
  champ: Champ<T>
  valeurs: string[]
  retenues: string[] | undefined
  onChange: (retenues: string[]) => void
}) {
  const [ouvert, setOuvert] = useState(false)
  const toutes = retenues === undefined
  const nombre = toutes ? valeurs.length : retenues.length
  const resume = champ.nonCumulable
    ? retenues?.[0] !== undefined
      ? affichageDe(champ, retenues[0])
      : '—'
    : toutes
      ? 'toutes'
      : `${nombre}/${valeurs.length}`
  const coche = (v: string) => toutes || retenues.includes(v)
  return (
    <Popover open={ouvert} onOpenChange={setOuvert}>
      <PopoverTrigger asChild>
        <Button
          variant='ghost'
          size='sm'
          className={cn(
            'h-6 gap-1 px-1.5 text-xs',
            !toutes && !champ.nonCumulable && 'text-primary'
          )}
        >
          <Filter className='size-3' />
          {resume}
          <ChevronDown className='size-3' />
        </Button>
      </PopoverTrigger>
      <PopoverContent align='start' className='w-64 p-2'>
        {champ.nonCumulable ? (
          <p className='text-muted-foreground mb-2 px-1 text-xs'>
            Une seule valeur : « {champ.libelle} » ne se cumule pas.
          </p>
        ) : (
          <div className='mb-1 flex gap-2 px-1'>
            <Button
              variant='link'
              size='sm'
              className='h-6 px-0 text-xs'
              onClick={() => onChange(valeurs)}
            >
              Tout cocher
            </Button>
            <Button
              variant='link'
              size='sm'
              className='h-6 px-0 text-xs'
              onClick={() => onChange([])}
            >
              Tout décocher
            </Button>
          </div>
        )}
        <div className='max-h-72 overflow-y-auto'>
          <div className='space-y-0.5'>
            {valeurs.map((v) =>
              champ.nonCumulable ? (
                <button
                  key={v}
                  type='button'
                  className={cn(
                    'hover:bg-muted w-full rounded-sm px-2 py-1 text-start text-sm',
                    retenues?.[0] === v && 'bg-muted font-medium'
                  )}
                  onClick={() => {
                    onChange([v])
                    setOuvert(false)
                  }}
                >
                  {affichageDe(champ, v)}
                </button>
              ) : (
                <label
                  key={v}
                  className='hover:bg-muted flex cursor-pointer items-center gap-2 rounded-sm px-2 py-1 text-sm'
                >
                  <Checkbox
                    checked={coche(v)}
                    onCheckedChange={(c) => {
                      const base = toutes ? valeurs : retenues
                      onChange(c ? [...base, v] : base.filter((x) => x !== v))
                    }}
                  />
                  {affichageDe(champ, v)}
                </label>
              )
            )}
          </div>
        </div>
      </PopoverContent>
    </Popover>
  )
}
