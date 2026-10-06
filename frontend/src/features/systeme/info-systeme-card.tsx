import { useQuery } from '@tanstack/react-query'
import { consulterInfoSystemeOptions } from '@/api/generated/@tanstack/react-query.gen'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

export function InfoSystemeCard() {
  const { data, isPending, isError } = useQuery(consulterInfoSystemeOptions())

  return (
    <Card>
      <CardHeader>
        <CardTitle>Info système</CardTitle>
        <CardDescription>Sonde technique : application et base</CardDescription>
      </CardHeader>
      <CardContent className='text-sm'>
        {isPending && <p className='text-muted-foreground'>Chargement…</p>}
        {isError && <p className='text-destructive'>API injoignable.</p>}
        {data && (
          <dl className='grid grid-cols-[auto_1fr] gap-x-4 gap-y-1'>
            <dt className='text-muted-foreground'>Application</dt>
            <dd>
              {data.application} {data.version}
            </dd>
            <dt className='text-muted-foreground'>Base</dt>
            <dd className='break-words'>{data.versionBase}</dd>
          </dl>
        )}
      </CardContent>
    </Card>
  )
}
