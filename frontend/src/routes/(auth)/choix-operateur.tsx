import { AxiosError } from 'axios'
import { createFileRoute, redirect } from '@tanstack/react-router'
import { ChoixOperateur } from '@/features/auth/liens'
import { requeteMoi } from '@/features/session/session'

export const Route = createFileRoute('/(auth)/choix-operateur')({
  beforeLoad: async ({ context }) => {
    try {
      await context.queryClient.ensureQueryData(requeteMoi)
    } catch (erreur) {
      if (erreur instanceof AxiosError && erreur.response?.status === 401) {
        throw redirect({ to: '/connexion' })
      }
      throw erreur
    }
  },
  component: ChoixOperateur,
})
