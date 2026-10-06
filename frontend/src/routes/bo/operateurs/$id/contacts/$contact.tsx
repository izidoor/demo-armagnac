import { createFileRoute } from '@tanstack/react-router'
import { PageContact } from '@/features/operateurs/contacts/page-contact'

export const Route = createFileRoute('/bo/operateurs/$id/contacts/$contact')({
  component: function Fiche() {
    const { id, contact } = Route.useParams()
    return (
      <PageContact
        key={contact}
        espace='bo'
        operateur={Number(id)}
        contact={Number(contact)}
      />
    )
  },
})
