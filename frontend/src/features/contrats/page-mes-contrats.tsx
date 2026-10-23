import { Link } from '@tanstack/react-router'
import { Plus } from 'lucide-react'
import { useFiltresAdresse } from '@/hooks/use-filtres-adresse'
import { Button } from '@/components/ui/button'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits, useMoi } from '@/features/session/session'
import { useMesContrats } from './api'
import { ONGLETS, type Onglet } from './libelles'
import { ListeContrats } from './liste-contrats'
import { Pastilles } from './pastilles'

/**
 * Portail, menu « Contrats » (spec 06 §6.2, RG-CA-25) : les contrats de l'opérateur actif, par onglets : à approuver
 * (un projet attend ma partie), en négociation (il attend l'autre), conclus, exécutés, annulés ou refusés. L'onglet est
 * gardé dans l'adresse. « Nouveau contrat » pour qui gère le module.
 */
export function PageMesContrats() {
  const { actif } = useMoi()
  if (!actif) return null
  return <MesContrats key={actif.operateur} operateur={actif.operateur} />
}

function MesContrats({ operateur }: { operateur: number }) {
  const [filtres, changer] = useFiltresAdresse({
    onglet: 'A_APPROUVER' as Onglet,
  })
  const contrats = useMesContrats(operateur)
  const droits = useDroits(operateur)
  const tous = contrats.data ?? []
  const nombre = (o: Onglet) => tous.filter((c) => c.onglet === o).length
  return (
    <PageMetier
      titre='Contrats'
      description="Vos contrats d'achat d'Armagnac en fût, de la négociation à l'exécution."
      actions={
        droits?.contrats === 'GESTION' && (
          <Button asChild className='max-md:w-full'>
            <Link to='/portail/contrats/nouveau'>
              <Plus /> Nouveau contrat
            </Link>
          </Button>
        )
      }
    >
      <Pastilles
        valeur={filtres.onglet}
        onChanger={(onglet) => changer({ onglet })}
        onglets={ONGLETS.map((o) => ({
          valeur: o.valeur,
          libelle: o.libelle,
          nombre: contrats.isSuccess ? nombre(o.valeur) : undefined,
        }))}
      />
      <ListeContrats
        espace='portail'
        operateur={operateur}
        contrats={tous.filter((c) => c.onglet === filtres.onglet)}
        chargement={contrats.isPending}
        vide={
          filtres.onglet === 'A_APPROUVER'
            ? 'Aucun contrat n’attend votre approbation.'
            : 'Aucun contrat.'
        }
      />
    </PageMetier>
  )
}
