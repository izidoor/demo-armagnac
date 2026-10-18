import { createFileRoute } from '@tanstack/react-router'
import { PageArticles } from '@/features/facturation/page-articles'

export const Route = createFileRoute('/bo/facturation/articles')({
  component: PageArticles,
})
