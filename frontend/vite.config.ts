import path from 'path'
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react-swc'
import tailwindcss from '@tailwindcss/vite'
import { tanstackRouter } from '@tanstack/router-plugin/vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // Dans un worktree, .env.local (worktree-env.sh, ignoré par git) donne ses propres ports.
  const env = { ...loadEnv(mode, process.cwd(), 'BNIA_'), ...process.env }
  return {
    plugins: [
      tanstackRouter({
        target: 'react',
        autoCodeSplitting: true,
      }),
      react(),
      tailwindcss(),
    ],
    server: {
      // Le backend Spring Boot (profil dev) écoute sur 8080 ; BNIA_API_URL vise une autre instance
      // (lancée depuis un worktree), BNIA_FRONT_PORT fixe le port de celle-ci.
      port: env.BNIA_FRONT_PORT ? Number(env.BNIA_FRONT_PORT) : undefined,
      strictPort: !!env.BNIA_FRONT_PORT,
      proxy: {
        '/api': env.BNIA_API_URL ?? 'http://localhost:8080',
      },
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
  }
})
