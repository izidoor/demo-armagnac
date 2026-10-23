import { Search } from 'lucide-react'
import { useFiltresAdresse, useTexteAdresse } from '@/hooks/use-filtres-adresse'
import { Input } from '@/components/ui/input'
import { PageMetier } from '@/components/metier/page-metier'
import { useContratsBnia } from './api'
import { ListeContrats } from './liste-contrats'
import { Pastilles } from './pastilles'

type FiltreEtat = 'TOUS' | 'PROJET' | 'CONCLU' | 'EXECUTE' | 'CLOS'

const ETATS_FILTRE: { valeur: FiltreEtat; libelle: string }[] = [
  { valeur: 'TOUS', libelle: 'Tous' },
  { valeur: 'PROJET', libelle: 'En négociation' },
  { valeur: 'CONCLU', libelle: 'Conclus' },
  { valeur: 'EXECUTE', libelle: 'Exécutés' },
  { valeur: 'CLOS', libelle: 'Annulés ou refusés' },
]

const dansEtat = (filtre: FiltreEtat, etat: string) =>
  filtre === 'TOUS' ||
  (filtre === 'CLOS' ? etat === 'ANNULE' || etat === 'REFUSE' : etat === filtre)

/**
 * Back-office, menu « Contrats » (spec 06 §6.1, RG-CA-25) : tous les contrats, en lecture (RG-CA-01), par état et par
 * numéro ; l'état et le numéro sont gardés dans l'adresse.
 */
export function PageContratsBnia() {
  const [filtres, changer] = useFiltresAdresse({
    etat: 'TOUS' as FiltreEtat,
    numero: '',
  })
  const [numero, setNumero, numeroStable] = useTexteAdresse(
    filtres.numero,
    (texte) => changer({ numero: texte })
  )
  const contrats = useContratsBnia({ numero: numeroStable || undefined })
  const tous = contrats.data ?? []
  return (
    <PageMetier
      titre='Contrats'
      description="Les contrats d'achat Armagnac entre opérateurs, conclus par l'accord des parties."
    >
      <div className='relative mb-4 w-full sm:w-64'>
        <Search className='text-muted-foreground absolute start-2.5 top-2.5 size-4' />
        <Input
          placeholder='Rechercher un contrat'
          title='Par numéro (CA-2026-…)'
          aria-label='Contrat, par numéro'
          className='h-9 w-full ps-8'
          value={numero}
          onChange={(e) => setNumero(e.target.value)}
        />
      </div>
      <Pastilles
        valeur={filtres.etat}
        onChanger={(etat) => changer({ etat })}
        onglets={ETATS_FILTRE.map((e) => ({
          ...e,
          nombre: contrats.isSuccess
            ? tous.filter((c) => dansEtat(e.valeur, c.etat)).length
            : undefined,
        }))}
      />
      <ListeContrats
        espace='bo'
        contrats={tous.filter((c) => dansEtat(filtres.etat, c.etat))}
        chargement={contrats.isPending}
      />
    </PageMetier>
  )
}
