import { createFileRoute } from '@tanstack/react-router'
import { type GenrePersonne } from '@/features/operateurs/personnes/navigation'
import { PagePersonne } from '@/features/operateurs/personnes/page-personne'

export const Route = createFileRoute(
  '/bo/operateurs/$id/personnes/$genre/$personne'
)({
  component: function Personne() {
    const { id, genre, personne } = Route.useParams()
    return (
      <PagePersonne
        key={`${genre}-${personne}`}
        espace='bo'
        operateur={Number(id)}
        genre={genre as GenrePersonne}
        id={Number(personne)}
      />
    )
  },
})
