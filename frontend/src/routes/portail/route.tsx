import { createFileRoute } from '@tanstack/react-router'
import { AuthenticatedLayout } from '@/components/layout/authenticated-layout'
import { espacePortail } from '@/components/layout/data/espaces'
import { exigerSession } from '@/features/session/session'

/** Portail : l'opérateur actif est un opérateur autre que le BNIA (RG-OPE-49). */
export const Route = createFileRoute('/portail')({
  beforeLoad: ({ context, location }) =>
    exigerSession(context.queryClient, location.href, 'portail'),
  component: () => <AuthenticatedLayout espace={espacePortail} />,
})
