import { z } from 'zod'
import { createFileRoute, redirect } from '@tanstack/react-router'
import { PageOperateur } from '@/features/operateurs/fiche/page-operateur'
import { exigerSession } from '@/features/session/session'

export const Route = createFileRoute('/bo/operateurs/$id/')({
  validateSearch: z.object({ onglet: z.string().optional() }),
  // La fiche du BNIA, l'opérateur actif, est dans les paramètres de l'application.
  beforeLoad: async ({ context, location, params }) => {
    const { actif } = await exigerSession(
      context.queryClient,
      location.href,
      'bo'
    )
    if (Number(params.id) === actif.operateur) {
      throw redirect({ to: '/bo/parametrage/bnia' })
    }
  },
  component: function Fiche() {
    const { id } = Route.useParams()
    const { onglet } = Route.useSearch()
    const navigate = Route.useNavigate()
    return (
      <PageOperateur
        key={id}
        espace='bo'
        id={Number(id)}
        onglet={onglet}
        onOnglet={(o) =>
          navigate({ search: { onglet: o }, replace: true, resetScroll: false })
        }
      />
    )
  },
})
