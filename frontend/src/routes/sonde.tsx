import { createFileRoute } from '@tanstack/react-router'
import { exigerSession } from '@/features/session/session'
import { InfoSystemeCard } from '@/features/systeme/info-systeme-card'

/**
 * Sonde technique (application et base), hors menus : une adresse connue de l'équipe seulement, pas un secret ;
 * la connexion reste exigée, comme pour la lecture qu'elle fait.
 */
export const Route = createFileRoute('/sonde')({
  beforeLoad: async ({ context, location }) => {
    await exigerSession(context.queryClient, location.href)
  },
  component: () => (
    <main className='flex min-h-svh items-center justify-center p-4'>
      <div className='w-full max-w-md'>
        <InfoSystemeCard />
      </div>
    </main>
  ),
})
