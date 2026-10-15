import * as React from 'react'
import { cn } from '@/lib/utils'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'

/**
 * Onglets d'un formulaire en classeur, à la manière d'un tableur : des languettes peu arrondies posées sur
 * le cadre du formulaire, l'onglet actif ouvert sur son contenu. À réserver aux parties d'une même saisie ;
 * ce qui n'en est pas une (historique, actions) reste hors du classeur.
 */
export function OngletsClasseur({
  value,
  onValueChange,
  onglets,
  children,
}: {
  value: string
  onValueChange: (value: string) => void
  onglets: { valeur: string; libelle: React.ReactNode }[]
  /** Les `TabsContent` des onglets, rendus dans le cadre. */
  children: React.ReactNode
}) {
  return (
    <Tabs value={value} onValueChange={onValueChange} className='mt-0 gap-0'>
      <TabsList className='h-auto w-full justify-start gap-0.5 overflow-x-auto rounded-none border-0 bg-transparent p-0 ps-2'>
        {onglets.map((o) => (
          <TabsTrigger
            key={o.valeur}
            value={o.valeur}
            className={cn(
              'relative -mb-px h-auto flex-none rounded-t-sm rounded-b-none border border-b-0 px-5 py-2.5 text-base font-medium tracking-normal normal-case [font-variant-caps:normal] before:hidden data-[state=active]:font-medium',
              'bg-muted/60 text-muted-foreground border-border/60 hover:bg-muted hover:text-foreground',
              'data-[state=active]:bg-card data-[state=active]:text-foreground data-[state=active]:border-border data-[state=active]:z-10 data-[state=active]:shadow-none',
              'data-[state=active]:border-t-primary data-[state=active]:border-t-2',
              'dark:data-[state=active]:bg-card dark:data-[state=active]:text-foreground dark:data-[state=active]:border-border dark:data-[state=active]:border-t-primary dark:text-muted-foreground'
            )}
          >
            {o.libelle}
          </TabsTrigger>
        ))}
      </TabsList>
      <div className='bg-card rounded-md rounded-tl-none border p-5 shadow-xs'>
        {children}
      </div>
    </Tabs>
  )
}
