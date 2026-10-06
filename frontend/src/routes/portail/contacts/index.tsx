import { createFileRoute } from '@tanstack/react-router'
import { PageContactsPortail } from '@/features/operateurs/portail/page-contacts-portail'

export const Route = createFileRoute('/portail/contacts/')({
  component: PageContactsPortail,
})
