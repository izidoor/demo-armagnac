import { createFileRoute } from '@tanstack/react-router'
import { PageUtilisateurs } from '@/features/acces/page-utilisateurs'

export const Route = createFileRoute('/bo/acces/utilisateurs/')({
  component: PageUtilisateurs,
})
