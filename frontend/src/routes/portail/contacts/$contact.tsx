import { createFileRoute } from '@tanstack/react-router'
import { PageContactPortail } from '@/features/operateurs/portail/page-contacts-portail'

export const Route = createFileRoute('/portail/contacts/$contact')({
  component: function Fiche() {
    const { contact } = Route.useParams()
    return <PageContactPortail key={contact} contact={Number(contact)} />
  },
})
