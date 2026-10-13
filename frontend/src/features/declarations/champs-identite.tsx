import { type ReactNode } from 'react'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { CLES, type CleConditionnement, millesimee } from './libelles'
import { type IdentiteSaisie } from './saisie-ligne'

/** Identifiant de la liste des dénominations suggérées, rendue une fois par écran (H-DEC-8). */
const LISTE_DENOMINATIONS = 'denominations-declarees'

/**
 * Les trois champs de l'identité d'une ligne (RG-DEC-10, 12), dans l'ordre des colonnes de la grille :
 * millésime ou dénomination (avec les dénominations déjà employées en suggestion), clé, compte d'âge
 * (calculé pour MB). En ligne et compacts par défaut, la clé n'affichant alors que son code ; avec
 * `etiquettes`, chacun sous son étiquette, à placer dans une grille de trois colonnes.
 */
export function ChampsIdentite({
  valeur,
  onChange,
  compteCalcule,
  etiquettes = false,
}: {
  valeur: IdentiteSaisie
  onChange: (valeur: IdentiteSaisie) => void
  /** Le compte calculé d'une ligne MB, s'il est connu. */
  compteCalcule?: string
  etiquettes?: boolean
}) {
  const m = millesimee(valeur.cle)
  // Les identifiants ne servent qu'aux étiquettes : en ligne, un écran répète les champs.
  const id = (nom: string) => (etiquettes ? `identite-${nom}` : undefined)
  const champ = (nom: string, etiquette: string, contenu: ReactNode) =>
    etiquettes ? (
      <div className='space-y-1.5'>
        <Label htmlFor={id(nom)}>{etiquette}</Label>
        {contenu}
      </div>
    ) : (
      contenu
    )
  const largeur = (compacte: string) => (etiquettes ? 'w-full' : compacte)
  return (
    <>
      {champ(
        'designation',
        'Millésime ou dénomination',
        <Input
          id={id('designation')}
          className={largeur(m ? 'w-24' : 'w-44')}
          inputMode={m ? 'numeric' : 'text'}
          placeholder={m ? 'Millésime, ex. 2010' : 'Dénomination, ex. XO'}
          aria-label={m ? 'Millésime' : 'Dénomination'}
          list={m ? undefined : LISTE_DENOMINATIONS}
          value={valeur.designation}
          onChange={(e) => onChange({ ...valeur, designation: e.target.value })}
        />
      )}
      {champ(
        'cle',
        'Clé',
        <Select
          value={valeur.cle}
          onValueChange={(cle) =>
            onChange({ ...valeur, cle: cle as CleConditionnement })
          }
        >
          <SelectTrigger
            id={id('cle')}
            className={largeur('w-20')}
            aria-label='Clé de conditionnement'
          >
            <SelectValue>
              {etiquettes ? `${valeur.cle} · ${CLES[valeur.cle]}` : valeur.cle}
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            {(Object.keys(CLES) as CleConditionnement[]).map((cle) => (
              <SelectItem key={cle} value={cle}>
                {cle} · {CLES[cle]}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      )}
      {champ(
        'compte',
        "Compte d'âge",
        valeur.cle === 'MB' ? (
          <Input
            id={id('compte')}
            className={largeur('w-28')}
            title='Compte calculé depuis le millésime (annexe B)'
            aria-label="Compte d'âge, calculé"
            value={compteCalcule ?? 'calculé'}
            disabled
            readOnly
          />
        ) : (
          <Input
            id={id('compte')}
            className={largeur('w-28')}
            placeholder="Compte d'âge"
            aria-label="Compte d'âge"
            value={valeur.compte}
            onChange={(e) => onChange({ ...valeur, compte: e.target.value })}
          />
        )
      )}
    </>
  )
}

/** Les dénominations déjà employées par l'opérateur, proposées à la saisie (H-DEC-8). */
export function ListeDenominations({ valeurs }: { valeurs: string[] }) {
  return (
    <datalist id={LISTE_DENOMINATIONS}>
      {valeurs.map((d) => (
        <option key={d} value={d} />
      ))}
    </datalist>
  )
}
