import { cn } from '@/lib/utils'

type MainProps = React.HTMLAttributes<HTMLElement> & {
  fixed?: boolean
  fluid?: boolean
  ref?: React.Ref<HTMLElement>
}

export function Main({ fixed, className, fluid, ...props }: MainProps) {
  return (
    <main
      data-layout={fixed ? 'fixed' : 'auto'}
      className={cn(
        'px-4 py-6',

        // If layout is fixed, make the main container flex and grow
        fixed && 'flex grow flex-col overflow-hidden',

        // If layout is not fluid, set the max-width (1600 px, wide enough for the DSM grids)
        !fluid &&
          '@min-[100rem]/content:mx-auto @min-[100rem]/content:w-full @min-[100rem]/content:max-w-[100rem]',
        className
      )}
      {...props}
    />
  )
}
