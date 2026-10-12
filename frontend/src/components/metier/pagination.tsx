import {
  ChevronLeftIcon,
  ChevronRightIcon,
  DoubleArrowLeftIcon,
  DoubleArrowRightIcon,
} from '@radix-ui/react-icons'
import { getPageNumbers } from '@/lib/utils'
import { Button } from '@/components/ui/button'

type PaginationProps = {
  page: number
  pages: number
  total: number
  setPage: (page: number) => void
}

/** Barre de pagination, à la manière de celle du gabarit (data-table). */
export function Pagination({ page, pages, total, setPage }: PaginationProps) {
  if (pages <= 1) return null
  return (
    <div className='flex items-center justify-between px-2 pt-4'>
      <p className='text-muted-foreground text-sm'>
        {total} lignes · page {page} sur {pages}
      </p>
      <div className='flex items-center space-x-2'>
        <Button
          variant='outline'
          className='size-8 p-0 @max-md/content:hidden'
          onClick={() => setPage(1)}
          disabled={page === 1}
        >
          <span className='sr-only'>Première page</span>
          <DoubleArrowLeftIcon className='h-4 w-4' />
        </Button>
        <Button
          variant='outline'
          className='size-8 p-0'
          onClick={() => setPage(page - 1)}
          disabled={page === 1}
        >
          <span className='sr-only'>Page précédente</span>
          <ChevronLeftIcon className='h-4 w-4' />
        </Button>
        {getPageNumbers(page, pages).map((numero, index) => (
          <div key={`${numero}-${index}`} className='flex items-center'>
            {numero === '...' ? (
              <span className='text-muted-foreground px-1 text-sm'>...</span>
            ) : (
              <Button
                variant={page === numero ? 'default' : 'outline'}
                className='h-8 min-w-8 px-2'
                onClick={() => setPage(numero as number)}
              >
                <span className='sr-only'>Page</span>
                {numero}
              </Button>
            )}
          </div>
        ))}
        <Button
          variant='outline'
          className='size-8 p-0'
          onClick={() => setPage(page + 1)}
          disabled={page === pages}
        >
          <span className='sr-only'>Page suivante</span>
          <ChevronRightIcon className='h-4 w-4' />
        </Button>
        <Button
          variant='outline'
          className='size-8 p-0 @max-md/content:hidden'
          onClick={() => setPage(pages)}
          disabled={page === pages}
        >
          <span className='sr-only'>Dernière page</span>
          <DoubleArrowRightIcon className='h-4 w-4' />
        </Button>
      </div>
    </div>
  )
}
