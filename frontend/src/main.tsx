import { StrictMode } from 'react'
import ReactDOM from 'react-dom/client'
import { AxiosError } from 'axios'
import {
  MutationCache,
  QueryCache,
  QueryClient,
  QueryClientProvider,
} from '@tanstack/react-query'
import { RouterProvider, createRouter } from '@tanstack/react-router'
import { toast } from 'sonner'
import { handleServerError } from '@/lib/handle-server-error'
import { retenirEntree } from '@/hooks/use-retour'
import { demarrerSuivi, ecranAffiche } from '@/features/suivi/suivi'
import { DirectionProvider } from './context/direction-provider'
import { FontProvider } from './context/font-provider'
import { ThemeProvider } from './context/theme-provider'
// Generated Routes
import { routeTree } from './routeTree.gen'
// Styles
import './styles/index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        // eslint-disable-next-line no-console
        if (import.meta.env.DEV) console.log({ failureCount, error })

        if (failureCount >= 0 && import.meta.env.DEV) return false
        if (failureCount > 3 && import.meta.env.PROD) return false

        return !(
          error instanceof AxiosError &&
          [401, 403].includes(error.response?.status ?? 0)
        )
      },
      refetchOnWindowFocus: import.meta.env.PROD,
      staleTime: 10 * 1000, // 10s
    },
  },
  mutationCache: new MutationCache({
    onError: (error, _variables, _context, mutation) => {
      // Un formulaire qui affiche lui-même l'erreur le signale par meta.erreurAffichee.
      if (mutation.meta?.erreurAffichee) return
      handleServerError(error)
    },
  }),
  queryCache: new QueryCache({
    onError: (error, query) => {
      if (error instanceof AxiosError) {
        // Sans session, la garde des routes renvoie déjà à la connexion, sans message.
        if (error.response?.status === 401 && query.queryKey[0] !== 'moi') {
          // Session fermée : déconnexion, inactivité, blocage ou accès retiré (RG-AUT-09).
          toast.error('Session expirée : reconnectez-vous.')
          const redirect = router.history.location.href
          queryClient.clear()
          router.navigate({ to: '/connexion', search: { redirect } })
        }
        if (error.response?.status === 500) {
          toast.error('Erreur interne du serveur')
          // Only navigate to error page in production to avoid disrupting HMR in development
          if (import.meta.env.PROD) {
            router.navigate({ to: '/500' })
          }
        }
        if (error.response?.status === 403) {
          // router.navigate("/forbidden", { replace: true });
        }
      }
    },
  }),
})

// Create a new router instance
const router = createRouter({
  routeTree,
  context: { queryClient },
  defaultPreload: 'intent',
  defaultPreloadStaleTime: 0,
  // Une nouvelle page s'ouvre en haut ; au retour, une page retrouve son défilement (une liste, là où on l'a quittée).
  scrollRestoration: true,
})

// Suivi de l'utilisation (features/suivi) : chaque écran, sous le gabarit de sa route.
router.subscribe('onResolved', ({ toLocation }) => {
  const gabarit =
    router.state.matches[router.state.matches.length - 1]?.fullPath ??
    toLocation.pathname
  ecranAffiche(toLocation.pathname, toLocation.searchStr, gabarit)
})
demarrerSuivi()

// Retour des pages de second niveau (useRetour) : le chemin de chaque entrée de l'historique.
router.subscribe('onResolved', () => retenirEntree(router))

// Application installable (PWA) : le service worker ne garde rien en cache et affiche les notifications push
// (public/sw.js). Aussi en dev, pour essayer le push sur localhost.
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {})
  })
}

// Register the router instance for type safety
declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router
  }
}

declare module '@tanstack/react-query' {
  interface Register {
    mutationMeta: { erreurAffichee?: boolean }
  }
}

// Render the app
const rootElement = document.getElementById('root')!
if (!rootElement.innerHTML) {
  const root = ReactDOM.createRoot(rootElement)
  root.render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider>
          <FontProvider>
            <DirectionProvider>
              <RouterProvider router={router} />
            </DirectionProvider>
          </FontProvider>
        </ThemeProvider>
      </QueryClientProvider>
    </StrictMode>
  )
}
