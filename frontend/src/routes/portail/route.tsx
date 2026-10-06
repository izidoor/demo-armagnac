import { createFileRoute } from '@tanstack/react-router'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { espacePortail } from '@/components/layout/data/espaces'

export const Route = createFileRoute('/portail')({
  component: () => <AuthenticatedLayout espace={espacePortail} />,
})
