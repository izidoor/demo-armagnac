import { createFileRoute } from '@tanstack/react-router'
import { PageClasseTva } from '@/features/referentiels/classes-tva/page-classe-tva'

export const Route = createFileRoute('/bo/parametrage/classes-tva/$id')({
  component: function Detail() {
    const { id } = Route.useParams()
    return <PageClasseTva id={Number(id)} />
  },
})
