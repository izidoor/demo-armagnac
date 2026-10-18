import { Link } from '@tanstack/react-router'
import { ChevronRight, Mail } from 'lucide-react'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { libelleNouveaux, useNouveauxMessages } from './nouveaux'

/**
 * Portail, tableau de bord (spec 15 §6.2, RG-MSG-11) : les messages non lus de l'opérateur actif que je peux lire ;
 * un clic ouvre la messagerie, non lues en gras.
 */
export function CarteNouveauxMessages({ operateur }: { operateur: number }) {
  const { data } = useNouveauxMessages(operateur)
  return (
    <Link
      to='/portail/messagerie'
      className='focus-visible:ring-ring rounded-xl focus-visible:ring-2 focus-visible:outline-none'
    >
      <Card className='hover:bg-muted/40 h-full transition-colors'>
        <CardHeader className='flex flex-row items-center gap-3'>
          <span className='bg-muted text-muted-foreground flex size-9 items-center justify-center rounded-lg'>
            <Mail className='size-[18px]' />
          </span>
          <div className='flex-1'>
            <CardTitle className='text-lg'>Messagerie</CardTitle>
            <CardDescription>Vos échanges avec le BNIA</CardDescription>
          </div>
          <ChevronRight className='text-muted-foreground size-4' />
        </CardHeader>
        <CardContent className='space-y-2'>
          <p className='text-2xl font-semibold tabular-nums'>
            {data ? libelleNouveaux(data.nombre) : '…'}
          </p>
          {data?.messages.slice(0, 2).map((m) => (
            <p key={m.id} className='text-muted-foreground truncate text-sm'>
              <span className='text-foreground font-medium'>{m.intitule}</span>{' '}
              · {m.debut}
            </p>
          ))}
        </CardContent>
      </Card>
    </Link>
  )
}
