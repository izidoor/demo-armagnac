import { createFileRoute } from '@tanstack/react-router'
import { PageOperateurs } from '@/features/operateurs/page-operateurs'

export const Route = createFileRoute('/bo/operateurs/')({
  component: PageOperateurs,
})
