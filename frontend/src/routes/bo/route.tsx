import { createFileRoute } from '@tanstack/react-router'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { espaceBackOffice } from '@/components/layout/data/espaces'

export const Route = createFileRoute('/bo')({
  component: () => <AuthenticatedLayout espace={espaceBackOffice} />,
})
