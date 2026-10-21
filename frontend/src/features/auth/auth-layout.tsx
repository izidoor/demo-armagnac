import { type ReactNode } from 'react'
import { Logo } from '@/assets/logo'
import vignes from '@/assets/vignes.jpg'

type AuthLayoutProps = {
  children: ReactNode
  /** Contenu de la colonne de droite, sur la photo ; absent : une seule colonne centrée. */
  panneau?: ReactNode
}

function Formulaire({ children }: { children: ReactNode }) {
  return (
    <div className='mx-auto flex w-full flex-col justify-center space-y-2 py-8 sm:w-[480px] sm:p-8'>
      <div className='mb-4 flex flex-col items-center gap-3'>
        <h1 className='text-center text-xl leading-tight font-medium'>
          Bureau National
          <br />
          Interprofessionnel de l'Armagnac
        </h1>
        <Logo />
      </div>
      {children}
    </div>
  )
}

/** Gabarit des écrans publics ; sur deux colonnes, la photo d'un vignoble et un panneau en surimpression. */
export function AuthLayout({ children, panneau }: AuthLayoutProps) {
  if (!panneau) {
    return (
      <div className='container grid h-svh max-w-none items-center justify-center'>
        <Formulaire>{children}</Formulaire>
      </div>
    )
  }
  return (
    <div className='grid min-h-svh lg:grid-cols-2'>
      <div className='flex items-center justify-center px-4'>
        <Formulaire>{children}</Formulaire>
      </div>
      <div className='relative hidden lg:block'>
        <img
          src={vignes}
          alt='Rangs de vigne au soleil levant'
          className='absolute inset-0 h-full w-full object-cover object-[75%_center]'
        />
        <div className='absolute inset-0 flex items-center justify-center p-10'>
          <div className='w-full max-w-lg rounded-lg bg-white/85 p-6 text-sm text-neutral-900 shadow-lg backdrop-blur-sm'>
            {panneau}
          </div>
        </div>
        <a
          href='https://unsplash.com/fr/photos/le-soleil-se-couche-sur-un-vignoble-vHds06aM3c8'
          target='_blank'
          rel='noreferrer'
          className='absolute right-3 bottom-2 text-xs text-white/80 hover:underline'
        >
          Photo : Unsplash
        </a>
      </div>
    </div>
  )
}
