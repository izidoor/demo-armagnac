import { useMemo, useState } from 'react'
import { BarChart3, FileDown } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  type Champ,
  type Composition,
  croiser,
  type Mesure,
  normaliser,
  type Vue,
} from './calcul'
import { CompositionTableau } from './composition'
import { exporterExcel } from './export'
import { GraphiqueCroise } from './graphique'
import { TableauCroiseVue } from './tableau'

export type { Champ, Mesure, Vue } from './calcul'

const PERSONNALISEE = 'personnalisee'
const PREFIXE_MEMOIRE = 'bnia.tableau-croise.'

function lire(cle: string): Composition | null {
  try {
    const texte = window.localStorage.getItem(PREFIXE_MEMOIRE + cle)
    return texte ? (JSON.parse(texte) as Composition) : null
  } catch {
    return null
  }
}

function retenir(cle: string, composition: Composition) {
  try {
    window.localStorage.setItem(
      PREFIXE_MEMOIRE + cle,
      JSON.stringify(composition)
    )
  } catch {
    // Mémoire du navigateur indisponible : la composition vaut pour la visite.
  }
}

const memesZones = (a: Composition, b: Composition) =>
  a.lignes.join() === b.lignes.join() &&
  a.colonnes.join() === b.colonnes.join() &&
  a.filtres.join() === b.filtres.join()

/**
 * Tableau croisé dynamique (spec 05 §6.1, §6.2) : l'utilisateur compose ses lignes, ses colonnes et ses
 * filtres par glisser-déposer, ou part d'une vue prédéfinie ; le graphique et l'export Excel suivent le
 * tableau. La dernière composition est retenue dans le navigateur, par `cle`. Un champ non cumulable ne se
 * somme jamais à travers ses valeurs.
 */
export function TableauCroise<T>({
  cle,
  donnees,
  champs,
  mesures,
  vues,
  nomFichier,
}: {
  /** Clé de la mémoire de la composition, propre à la page. */
  cle: string
  donnees: T[]
  champs: Champ<T>[]
  mesures: Mesure<T>[]
  vues: Vue[]
  nomFichier: string
}) {
  const [brute, setBrute] = useState<Composition>(
    () => lire(cle) ?? vues[0].composition
  )
  const [graphique, setGraphique] = useState(true)
  const composition = useMemo(
    () => normaliser(brute, champs, donnees),
    [brute, champs, donnees]
  )
  const mesure = mesures.find((m) => m.id === composition.mesure) ?? mesures[0]
  const croisement = useMemo(
    () => croiser(donnees, champs, mesure, composition),
    [donnees, champs, mesure, composition]
  )
  const changer = (suivante: Composition) => {
    setBrute(suivante)
    retenir(cle, suivante)
  }

  const parId = new Map(champs.map((c) => [c.id, c]))
  const nonCumulable = (id: string | undefined) =>
    !!id && !!parId.get(id)?.nonCumulable
  const titreLignes =
    composition.lignes.map((id) => parId.get(id)?.libelle).join(' / ') ||
    'Total'
  // Une vue se compare normalisée : un champ non cumulable qu'elle ne place pas est rangé d'office en filtre.
  const vue = vues.find((v) =>
    memesZones(normaliser(v.composition, champs, donnees), composition)
  )

  return (
    <div className='space-y-4'>
      <CompositionTableau
        champs={champs}
        donnees={donnees}
        composition={composition}
        onChange={changer}
      />
      <div className='flex flex-wrap items-end gap-3'>
        <div className='space-y-1'>
          <Label htmlFor={`${cle}-vue`}>Vue</Label>
          <Select
            value={vue?.id ?? PERSONNALISEE}
            onValueChange={(id) => {
              const choisie = vues.find((v) => v.id === id)
              if (choisie)
                changer({ ...choisie.composition, mesure: composition.mesure })
            }}
          >
            <SelectTrigger id={`${cle}-vue`} className='w-56'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {vues.map((v) => (
                <SelectItem key={v.id} value={v.id}>
                  {v.libelle}
                </SelectItem>
              ))}
              {!vue && (
                <SelectItem value={PERSONNALISEE} disabled>
                  Personnalisée
                </SelectItem>
              )}
            </SelectContent>
          </Select>
        </div>
        {mesures.length > 1 && (
          <div className='space-y-1'>
            <Label htmlFor={`${cle}-mesure`}>Mesure</Label>
            <Select
              value={mesure.id}
              onValueChange={(id) => changer({ ...brute, mesure: id })}
            >
              <SelectTrigger id={`${cle}-mesure`} className='w-48'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {mesures.map((m) => (
                  <SelectItem key={m.id} value={m.id}>
                    {m.libelle}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}
        <div className='ms-auto flex gap-2'>
          <Button
            variant='outline'
            onClick={() => setGraphique((g) => !g)}
            aria-pressed={graphique}
          >
            <BarChart3 className='size-4' />
            {graphique ? 'Masquer le graphique' : 'Afficher le graphique'}
          </Button>
          <Button
            variant='outline'
            onClick={() =>
              exporterExcel(croisement, titreLignes, nomFichier).catch(() =>
                toast.error("L'export Excel a échoué.")
              )
            }
          >
            <FileDown className='size-4' /> Exporter le tableau
          </Button>
        </div>
      </div>
      {graphique && (
        <GraphiqueCroise
          croisement={croisement}
          evolutionEnColonnes={composition.colonnes.some(nonCumulable)}
          evolutionEnLignes={nonCumulable(composition.lignes[0])}
          format={mesure.format}
        />
      )}
      <TableauCroiseVue
        croisement={croisement}
        titreLignes={titreLignes}
        format={mesure.format}
      />
    </div>
  )
}
