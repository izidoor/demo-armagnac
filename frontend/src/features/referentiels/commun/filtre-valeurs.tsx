import { Search } from 'lucide-react'
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
      <div className='relative w-full sm:w-auto'>
        <Search className='text-muted-foreground absolute start-2.5 top-2.5 size-4' />
        <Input
          placeholder={placeholder}
          aria-label={placeholder}
          value={recherche}
          onChange={(e) => setRecherche(e.target.value)}
          className='h-9 w-full ps-8 sm:w-72'
        />
      </div>
      <Select value={etat} onValueChange={(v) => setEtat(v as Etat)}>
        <SelectTrigger aria-label='État' className='h-9 w-36 max-sm:w-full'>
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
