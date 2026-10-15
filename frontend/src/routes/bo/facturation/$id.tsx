import { createFileRoute } from '@tanstack/react-router'
import { PagePiece } from '@/features/facturation/page-piece'

export const Route = createFileRoute('/bo/facturation/$id')({
  component: function Piece() {
    const { id } = Route.useParams()
    return <PagePiece key={id} id={Number(id)} />
  },
})
