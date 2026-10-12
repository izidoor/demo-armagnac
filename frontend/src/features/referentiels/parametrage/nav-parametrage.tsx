import { Link, useNavigate } from '@tanstack/react-router'
import { cn } from '@/lib/utils'
import { buttonVariants } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { sections, useSectionActive } from './sections-parametrage'

/** Menu des sections du Paramétrage, à la manière de /settings du gabarit : liste à gauche, liste déroulante sur mobile. */
export function NavParametrage() {
  const navigate = useNavigate()
  const active = useSectionActive()?.url

  return (
    <>
      <div className='p-1 md:hidden'>
        <Select value={active} onValueChange={(url) => navigate({ to: url })}>
          <SelectTrigger className='h-12 w-full sm:w-64'>
            <SelectValue placeholder='Section' />
          </SelectTrigger>
          <SelectContent>
            {sections.map((s) => (
              <SelectItem key={s.url} value={s.url}>
                <div className='flex items-center gap-2'>
                  {s.icon && <s.icon className='size-4' />}
                  {s.title}
                </div>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <nav className='hidden w-full flex-col gap-1 md:flex'>
        {sections.map((s) => (
          <Link
            key={s.url}
            to={s.url}
            className={cn(
              buttonVariants({ variant: 'ghost' }),
              active === s.url
                ? 'bg-muted hover:bg-accent'
                : 'hover:bg-accent hover:underline',
              'justify-start'
            )}
          >
            {s.icon && <s.icon className='me-2' />}
            {s.title}
          </Link>
        ))}
      </nav>
    </>
  )
}
