import { createFileRoute } from '@tanstack/react-router'
import { PageClasseTva } from '@/features/referentiels/classes-tva/page-classe-tva'

export const Route = createFileRoute('/bo/parametrage/classes-tva/$code')({
  component: function Detail() {
    const { code } = Route.useParams()
    return <PageClasseTva code={code} />
  },
})
