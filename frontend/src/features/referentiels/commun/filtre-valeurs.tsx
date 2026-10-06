import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { type Etat, type useFiltreValeurs } from './use-filtre-valeurs'

type BarreFiltreProps = ReturnType<typeof useFiltreValeurs> & {
  placeholder?: string
}

export function BarreFiltre({
  recherche,
  setRecherche,
  etat,
  setEtat,
  placeholder = 'Rechercher un code ou un libellé',
}: BarreFiltreProps) {
  return (
    <div className='mb-4 flex flex-wrap items-center gap-2'>
      <Input
        placeholder={placeholder}
        value={recherche}
        onChange={(e) => setRecherche(e.target.value)}
        className='h-9 w-full sm:w-72'
      />
      <Select value={etat} onValueChange={(v) => setEtat(v as Etat)}>
        <SelectTrigger className='h-9 w-36'>
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value='actifs'>Actifs</SelectItem>
          <SelectItem value='inactifs'>Inactifs</SelectItem>
          <SelectItem value='tous'>Tous</SelectItem>
        </SelectContent>
      </Select>
    </div>
  )
}
