import { defineConfig } from '@hey-api/openapi-ts'

// Client généré depuis le contrat exporté par le backend (ADR-007).
// Régénérer après toute évolution de l'API : pnpm gen:api
export default defineConfig({
  input: '../docs/api/openapi.json',
  output: {
    path: 'src/api/generated',
    format: 'prettier',
  },
  plugins: [
    { name: '@hey-api/client-axios', runtimeConfigPath: './src/api/client-config.ts' },
    '@hey-api/typescript',
    '@hey-api/sdk',
    '@tanstack/react-query',
  ],
})
