import { createFileRoute } from '@tanstack/react-router'
import { PageMesDsm } from '@/features/declarations/page-mes-dsm'

export const Route = createFileRoute('/portail/declarations/')({
  component: PageMesDsm,
})
