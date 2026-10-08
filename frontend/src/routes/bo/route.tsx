import { createFileRoute } from '@tanstack/react-router'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { espaceBackOffice } from '@/components/layout/data/espaces'
import { exigerSession } from '@/features/session/session'

/** Back-office : l'opérateur actif est le BNIA (RG-OPE-49). */
export const Route = createFileRoute('/bo')({
  beforeLoad: ({ context, location }) =>
    exigerSession(context.queryClient, location.href, 'bo'),
  component: () => <AuthenticatedLayout espace={espaceBackOffice} />,
})
