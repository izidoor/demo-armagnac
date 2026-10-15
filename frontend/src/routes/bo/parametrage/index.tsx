import { createFileRoute, redirect } from '@tanstack/react-router'

/** La roue crantée ouvre la première section. */
export const Route = createFileRoute('/bo/parametrage/')({
  beforeLoad: () => {
    throw redirect({ to: '/bo/parametrage/bnia' })
  },
})
