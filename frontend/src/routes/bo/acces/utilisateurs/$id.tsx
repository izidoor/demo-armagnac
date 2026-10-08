import { createFileRoute } from '@tanstack/react-router'
import { PageUtilisateur } from '@/features/acces/page-utilisateurs'

export const Route = createFileRoute('/bo/acces/utilisateurs/$id')({
  component: function Utilisateur() {
    const { id } = Route.useParams()
    return <PageUtilisateur key={id} id={Number(id)} />
  },
})
