import { createFileRoute } from '@tanstack/react-router'
import { PageEtatStocks } from '@/features/declarations/page-etat-stocks'

export const Route = createFileRoute('/portail/declarations/stocks')({
  component: PageEtatStocks,
})
