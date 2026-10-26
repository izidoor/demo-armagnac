import { Fragment, type ReactNode } from 'react'
import { BookOpen, Database, FileArchive, Github } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits, useMoi } from '@/features/session/session'

/**
 * Paramètres de l'application > Données : la copie des données, la base au format de pg_dump et les documents
 * déposés en zip, téléchargés par un ADMIN du BNIA (vérifié par le serveur), boutons désactivés pour les autres ;
 * puis, pour tous, le code source et les données sur GitHub et la documentation détaillée (les specs).
 */
export function SectionDonnees() {
  const { actif } = useMoi()
  const adminBnia = useDroits(actif?.operateur ?? 0)?.adminBnia ?? false
  return (
    <div className='pb-8'>
      {[
        <PageMetier
          key='copie'
          titre='Copie des données'
          description='Toutes les données de l’application, à tout moment.'
        >
          <div className='flex flex-wrap gap-2'>
            <Telechargement
              url='/api/sauvegarde/base'
              actif={adminBnia}
              icone={<Database className='size-4' />}
              libelle='Télécharger la base'
            />
            <Telechargement
              url='/api/sauvegarde/documents'
              actif={adminBnia}
              icone={<FileArchive className='size-4' />}
              libelle='Télécharger les documents'
            />
          </div>
          {!adminBnia && (
            <p className='text-muted-foreground mt-3 text-sm'>
              Réservée aux administrateurs du BNIA.
            </p>
          )}
        </PageMetier>,
        <PageMetier
          key='code-source'
          titre='Code source et documentation'
          description='Le dépôt public de l’application et sa documentation détaillée.'
        >
          <div className='flex flex-wrap gap-2'>
            <Lien
              url='https://github.com/izidoor/demo-armagnac/tree/main'
              icone={<Github className='size-4' />}
              libelle='Code source et données sur GitHub'
            />
            <Lien
              url='https://github.com/izidoor/demo-armagnac/tree/main/docs/specs'
              icone={<BookOpen className='size-4' />}
              libelle='Documentation détaillée'
            />
          </div>
        </PageMetier>,
      ].map((enfant, i) => (
        <Fragment key={i}>
          {i > 0 && <Separator className='my-8' />}
          <section>{enfant}</section>
        </Fragment>
      ))}
    </div>
  )
}

function Telechargement({
  url,
  actif,
  icone,
  libelle,
}: {
  url: string
  actif: boolean
  icone: ReactNode
  libelle: string
}) {
  if (!actif) {
    return (
      <Button variant='outline' disabled>
        {icone} {libelle}
      </Button>
    )
  }
  return (
    <Button variant='outline' asChild>
      <a href={url} download>
        {icone} {libelle}
      </a>
    </Button>
  )
}

function Lien({
  url,
  icone,
  libelle,
}: {
  url: string
  icone: ReactNode
  libelle: string
}) {
  return (
    <Button variant='outline' asChild>
      <a href={url} target='_blank' rel='noreferrer'>
        {icone} {libelle}
      </a>
    </Button>
  )
}
