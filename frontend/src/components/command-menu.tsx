import React from 'react'
import { useNavigate } from '@tanstack/react-router'
import {
  ArrowRight,
  ChevronLeft,
  ChevronRight,
  Laptop,
  Moon,
  Sun,
} from 'lucide-react'
import { useSearch } from '@/context/search-provider'
import { useTheme } from '@/context/theme-provider'
import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from '@/components/ui/command'
import { DialogClose } from '@/components/ui/dialog'
import { type NavGroup } from './layout/types'
import { Button } from './ui/button'
import { ScrollArea } from './ui/scroll-area'

type CommandMenuProps = {
  navGroups: NavGroup[]
}

/**
 * La recherche (⌘K) : les pages de l'espace et le thème. Une fenêtre sur un écran large ; sur mobile, une pleine page,
 * sous « Annuler » qui la referme, comme les formulaires de création (`DialogueFormulaire`), le champ prêt à la saisie.
 */
export function CommandMenu({ navGroups }: CommandMenuProps) {
  const navigate = useNavigate()
  const { setTheme } = useTheme()
  const { open, setOpen } = useSearch()

  const runCommand = React.useCallback(
    (command: () => unknown) => {
      setOpen(false)
      command()
    },
    [setOpen]
  )

  return (
    <CommandDialog
      modal
      open={open}
      onOpenChange={setOpen}
      title='Rechercher'
      description='Une page ou une commande.'
      className='max-md:data-[state=closed]:slide-out-to-right max-md:data-[state=closed]:zoom-out-100 max-md:data-[state=open]:slide-in-from-right max-md:data-[state=open]:zoom-in-100 max-md:inset-0 max-md:flex max-md:h-dvh max-md:max-w-none! max-md:translate-x-0 max-md:translate-y-0 max-md:flex-col max-md:rounded-none max-md:border-0 max-md:shadow-none max-md:[&>[data-slot=dialog-close]]:hidden'
    >
      <div className='flex h-14 shrink-0 items-center border-b px-2 md:hidden'>
        <DialogClose asChild>
          <Button variant='ghost' className='gap-1 px-2'>
            <ChevronLeft className='size-5' /> Annuler
          </Button>
        </DialogClose>
      </div>
      {/* Le focus va au champ, pas à « Annuler » : sur mobile, le clavier s'ouvre aussitôt. */}
      <CommandInput
        autoFocus
        placeholder='Rechercher une page ou une commande…'
      />
      <CommandList className='max-md:max-h-none'>
        <ScrollArea
          type='hover'
          className='h-72 pe-1 max-md:h-[calc(100dvh-6.5rem)]'
        >
          <CommandEmpty>Aucun résultat.</CommandEmpty>
          {navGroups.map((group) => (
            <CommandGroup key={group.title} heading={group.title}>
              {group.items.map((navItem, i) => {
                if (navItem.url)
                  return (
                    <CommandItem
                      key={`${navItem.url}-${i}`}
                      value={navItem.title}
                      onSelect={() => {
                        runCommand(() => navigate({ to: navItem.url }))
                      }}
                    >
                      <div className='flex size-4 items-center justify-center'>
                        <ArrowRight className='text-muted-foreground/80 size-2' />
                      </div>
                      {navItem.title}
                    </CommandItem>
                  )

                return navItem.items?.map((subItem, i) => (
                  <CommandItem
                    key={`${navItem.title}-${subItem.url}-${i}`}
                    value={`${navItem.title}-${subItem.url}`}
                    onSelect={() => {
                      runCommand(() => navigate({ to: subItem.url }))
                    }}
                  >
                    <div className='flex size-4 items-center justify-center'>
                      <ArrowRight className='text-muted-foreground/80 size-2' />
                    </div>
                    {navItem.title} <ChevronRight /> {subItem.title}
                  </CommandItem>
                ))
              })}
            </CommandGroup>
          ))}
          <CommandSeparator />
          <CommandGroup heading='Thème'>
            <CommandItem onSelect={() => runCommand(() => setTheme('light'))}>
              <Sun /> <span>Clair</span>
            </CommandItem>
            <CommandItem onSelect={() => runCommand(() => setTheme('dark'))}>
              <Moon className='scale-90' />
              <span>Sombre</span>
            </CommandItem>
            <CommandItem onSelect={() => runCommand(() => setTheme('system'))}>
              <Laptop />
              <span>Système</span>
            </CommandItem>
          </CommandGroup>
        </ScrollArea>
      </CommandList>
    </CommandDialog>
  )
}
