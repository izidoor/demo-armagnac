import * as React from 'react'
import * as TabsPrimitive from '@radix-ui/react-tabs'
import { cn } from '@/lib/utils'

function Tabs({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Root>) {
  return (
    <TabsPrimitive.Root
      data-slot='tabs'
      className={cn('mt-10 flex flex-col gap-4', className)}
      {...props}
    />
  )
}

function TabsList({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.List>) {
  return (
    <TabsPrimitive.List
      data-slot='tabs-list'
      className={cn(
        'text-muted-foreground border-border inline-flex h-11 w-fit items-center justify-center gap-[5px] rounded-lg border p-1',
        className
      )}
      {...props}
    />
  )
}

/**
 * Onglet, dans une barre cernée d'un trait. Actif : en gras, sur un fond légèrement coloré ; une copie
 * invisible du libellé en gras lui réserve sa largeur, pour que les onglets ne bougent pas quand l'onglet
 * actif change. Un trait vertical léger sépare deux onglets, l'onglet actif compris.
 */
function TabsTrigger({
  className,
  children,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Trigger>) {
  return (
    <TabsPrimitive.Trigger
      data-slot='tabs-trigger'
      className={cn(
        "hover:bg-muted data-[state=active]:bg-primary/10 data-[state=active]:text-primary dark:data-[state=active]:bg-primary/20 dark:data-[state=active]:text-primary focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:outline-ring text-foreground dark:text-muted-foreground not-first:before:bg-border relative inline-flex h-full flex-1 items-center justify-center rounded-md border border-transparent px-4 py-1.5 text-base font-medium tracking-wide whitespace-nowrap transition-[color,background-color,box-shadow] [font-variant-caps:all-small-caps] not-first:before:absolute not-first:before:top-1/4 not-first:before:-left-1 not-first:before:h-1/2 not-first:before:w-px focus-visible:ring-[3px] focus-visible:outline-1 disabled:pointer-events-none disabled:opacity-50 data-[state=active]:font-semibold [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
        className
      )}
      {...props}
    >
      <span className='grid'>
        <span
          aria-hidden
          className='invisible col-start-1 row-start-1 inline-flex items-center justify-center gap-1.5 font-semibold'
        >
          {children}
        </span>
        <span className='col-start-1 row-start-1 inline-flex items-center justify-center gap-1.5'>
          {children}
        </span>
      </span>
    </TabsPrimitive.Trigger>
  )
}

function TabsContent({
  className,
  ...props
}: React.ComponentProps<typeof TabsPrimitive.Content>) {
  return (
    <TabsPrimitive.Content
      data-slot='tabs-content'
      className={cn('flex-1 outline-none', className)}
      {...props}
    />
  )
}

export { Tabs, TabsList, TabsTrigger, TabsContent }
