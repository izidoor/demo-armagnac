import { createFileRoute } from '@tanstack/react-router'
import { MotDePasseOublie } from '@/features/auth/mot-de-passe'

export const Route = createFileRoute('/(auth)/mot-de-passe-oublie')({
  component: MotDePasseOublie,
})
