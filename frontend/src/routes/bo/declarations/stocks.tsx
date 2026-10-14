import { createFileRoute } from '@tanstack/react-router'
import { PageStocksDeclares } from '@/features/declarations/page-stocks-declares'

export const Route = createFileRoute('/bo/declarations/stocks')({
  component: PageStocksDeclares,
})
