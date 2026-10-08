import { createFileRoute } from '@tanstack/react-router'
import { type GenrePersonne } from '@/features/operateurs/personnes/navigation'
import { PagePersonne } from '@/features/operateurs/personnes/page-personne'
import { useMoi } from '@/features/session/session'

export const Route = createFileRoute('/portail/annuaire/$genre/$personne')({
  component: function Personne() {
    const { genre, personne } = Route.useParams()
    const { actif } = useMoi()
    if (!actif) return null
    return (
      <PagePersonne
        key={`${genre}-${personne}`}
        espace='portail'
        operateur={actif.operateur}
        genre={genre as GenrePersonne}
        id={Number(personne)}
      />
    )
  },
})
