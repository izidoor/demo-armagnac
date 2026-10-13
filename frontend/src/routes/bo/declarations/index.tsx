import { createFileRoute } from '@tanstack/react-router'
import { PageDsmBnia } from '@/features/declarations/page-dsm-bnia'

export const Route = createFileRoute('/bo/declarations/')({
  component: PageDsmBnia,
})
